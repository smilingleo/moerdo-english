package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.FREE_USER_WORDS_LIMIT;
import static leo.me.Constants.OPTION_PATTERN;
import static leo.me.Constants.PAID_USER_WORDS_LIMIT;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import leo.me.anki.AnkiNoteDao;
import leo.me.anki.AnkiNoteParser;
import leo.me.anki.NoteItem;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;
import leo.me.polly.Polly;
import leo.me.polly.PollyConfig;
import leo.me.utils.DateTimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReadNewWordsHandler implements Handler {

    private static Log log = LogFactory.getLog(ReadNewWordsHandler.class);

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        return readWords(request);
    }

    /**
     * read new words, a new timestamp will be generated.
     *
     * @param request
     * @return
     */
    private MoerdoResponse readWords(MoerdoRequest request) {

        UserInfo userInfo = refreshUserInfo(request);

        evaluateLimit(userInfo);

        validateReadWordsRequest(request);

        validateWordsByUserProfile(userInfo, request);

        final PollyConfig config = new PollyConfig();
        config.setExampleLimit(request.getExampleLimit());
        final Polly polly = new Polly(config);
        final AnkiNoteDao dao = new AnkiNoteDao(":resource:collection.anki2");
        final AnkiNoteParser parser = new AnkiNoteParser(config);

        List<String> notes = dao.findNotes(request.getWords().toArray(new String[request.getWords().size()]));
        final String optionStr = request.getOptions();
        List<NoteItem> options = Arrays.stream(optionStr.split(",")).map(s -> NoteItem.fromShort(s)).collect(Collectors.toList());

        // create new timestamp
        String timestamp = DateTimeUtils.now();

        CachedAudioFetcher fetcher = new CachedAudioFetcher(request.getWechatId(), s3Client, polly, timestamp, request.getExampleLimit());

        // get the audio url
        String url = fetcher.audioData(notes.stream().map(note -> parser.parse(note)).collect(Collectors.toList()), options);

        // generate the txt file
        final String txtFileKey = format("%s/audio/%s.txt", request.getWechatId(), timestamp);
        s3Client.putObject(USER_BUCKET_NAME, txtFileKey,
                request.getWords().stream().collect(Collectors.joining(",")));
        log.info(format("Saved txt file: %s", txtFileKey));

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        if (Objects.equals("link", request.getResponseType())) {
            response.setUri(url);
        } else {
            final String mp3FileKey = format("%s/audio/%s.mp3", request.getWechatId(), timestamp);
            S3Object object = s3Client.getObject(USER_BUCKET_NAME, mp3FileKey);
            try {
                response.setAudioData(IOUtils.toByteArray(object.getObjectContent()));
            } catch (IOException e) {
                throw new IllegalStateException("failed to load file: " + timestamp, e);
            }
        }
        return response;
    }

    private void validateWordsByUserProfile(UserInfo userInfo, MoerdoRequest request) {
        if (userInfo.isFreeUser() && request.getWords().size() > FREE_USER_WORDS_LIMIT) {
            throw new IllegalArgumentException("免费用户只能每次制作不超过10个单词的语音包。");
        }

        if (userInfo.isPaidUser() && request.getWords().size() > PAID_USER_WORDS_LIMIT) {
            throw new IllegalArgumentException("普通付费用户只能每次制作不超过50个单词的语音包。");
        }
    }

    private void validateReadWordsRequest(MoerdoRequest request) {
        validateWechatId(request.getWechatId());
        validateResponseType(request.getResponseType());

        if (request.getWords() == null || request.getWords().isEmpty()) {
            throw new IllegalArgumentException("missing required parameter: 'words'");
        }

        if (!OPTION_PATTERN.matcher(request.getOptions()).matches()) {
            throw new IllegalArgumentException("invalid option: " + request.getOptions());
        }

    }
}
