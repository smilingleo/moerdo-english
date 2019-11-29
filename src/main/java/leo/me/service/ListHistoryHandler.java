package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.LIMIT_HISTORY_BY_MONTH;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.HistoryRecord;
import leo.me.lambda.vo.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For list history, load all the history for the user for the last half year. return data like:
 * <pre>
 * {
 *     userInfo: ...,
 *     historyRecords: [
 *         {
 *             timestamp: '2019-01-01_00-00-00',
 *             url: '...',
 *             words: ''
 *         }
 *     ]
 * }
 * </pre>
 */
public class ListHistoryHandler implements Handler {

    private static Log log = LogFactory.getLog(ListHistoryHandler.class);

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = refreshUserInfo(request);

        validateListHistoryRequest(request);

        MoerdoResponse response = new MoerdoResponse();
        List<HistoryRecord> historyRecords = loadHistory(userInfo);

        log.info(format("List History: Load %d records for wechat id: %s", historyRecords.size(), userInfo.getWechatId()));
        response.setHistoryRecords(historyRecords);

        return response;
    }

    private void validateListHistoryRequest(MoerdoRequest request) {
        validateWechatId(request.getWechatId());
        validateResponseType(request.getResponseType());
    }

    private List<HistoryRecord> loadHistory(UserInfo userInfo) {
        List<S3ObjectSummary> list = new LinkedList<>();

        LocalDate today = LocalDate.now();
        for (int i = 0; i < LIMIT_HISTORY_BY_MONTH; i++) {
            LocalDate start = today.plusMonths(-1 * i);
            final String dateString = start.toString();
            String prefix = format("%s/audio/%s", userInfo.getWechatId(), dateString.substring(0, 8));
            list.addAll(s3Client.listObjects(USER_BUCKET_NAME, prefix).getObjectSummaries());
        }

        // Each timestamp, there should be a pair of files, `.mp3` and `.txt`
        return list.stream()
                .collect(Collectors.groupingBy(summary -> {
                    String key = summary.getKey();
                    return key.substring(key.lastIndexOf("/") + 1, key.lastIndexOf('.'));
                }))
                .entrySet().stream()
                .map(entry -> {
                    String timestamp = entry.getKey();
                    List<S3ObjectSummary> summaries = entry.getValue();
                    HistoryRecord record = new HistoryRecord();
                    record.setTimestamp(timestamp);
                    // audio
                    summaries.stream().filter(summary -> summary.getKey().endsWith(".mp3")).findFirst()
                            .ifPresent(mp3 -> {
                                URL url = s3Client.getUrl(USER_BUCKET_NAME, mp3.getKey());
                                record.setUrl(url.toString());
                            });
                    // content
                    summaries.stream().filter(summary -> summary.getKey().endsWith(".txt")).findFirst()
                            .ifPresent(txt -> {
                                String words = s3Client.getObjectAsString(USER_BUCKET_NAME, txt.getKey());
                                record.setWords(words);
                            });
                    return record;
                })
                .collect(Collectors.toList());
    }
}
