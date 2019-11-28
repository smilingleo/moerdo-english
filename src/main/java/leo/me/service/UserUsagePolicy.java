package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.FREE_USER_POINT_LIMIT;
import static leo.me.Constants.PAID_USER_POINT_LIMIT;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import leo.me.lambda.UserInfo;
import leo.me.utils.DateTimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class UserUsagePolicy {
    private static Log log = LogFactory.getLog(UserUsagePolicy.class);

    public void evaluate(UserInfo userInfo, AmazonS3 s3Client) {
        String now = DateTimeUtils.now();
        String currentMonth = now.substring(0, 8);
        String prefix = format("%s/audio/%s", userInfo.getWechatId(), currentMonth);
        List<S3ObjectSummary> summaries = s3Client.listObjects(USER_BUCKET_NAME, prefix).getObjectSummaries();

        log.info(format("user %s has %d audios in %s", userInfo.getWechatId(), summaries.size(), prefix));

        int pointLimit = 0;
        if (userInfo.isFreeUser()) {
            pointLimit = FREE_USER_POINT_LIMIT;
            if (summaries.size() >= FREE_USER_POINT_LIMIT) {
                throw new RuntimeException("免费用户每月（自然月）只能制作25个语音包，请升级为付费用户。");
            }
        } else if (userInfo.isPaidUser()) {
            pointLimit = PAID_USER_POINT_LIMIT;
            if (summaries.size() >= PAID_USER_POINT_LIMIT) {
                throw new RuntimeException("普通付费用户每月（自然月）只能制作100个语音包，请升级为付费用户。");
            }
        }

        userInfo.setLeftPoints(pointLimit - summaries.size());
    }
}
