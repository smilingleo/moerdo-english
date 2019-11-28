package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.common.base.Strings;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.UserInfo;
import leo.me.utils.DateTimeUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ListHistoryHandler implements Handler {

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = refreshUserInfo(request);

        validateListHistoryRequest(request);
        String fileName = format("%s/audio/%s.mp3", request.getWechatId(), request.getTimestamp());
        boolean exists = s3Client.doesObjectExist(USER_BUCKET_NAME, fileName);

        if (!exists) {
            throw new IllegalArgumentException("History item not found for " + request.getTimestamp());
        }

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setTimestamp(request.getTimestamp());

        if (Objects.equals("link", request.getResponseType())) {
            URL url = s3Client.getUrl(USER_BUCKET_NAME, fileName);
            response.setUri(url.toString());
        } else {
            try (S3Object object = s3Client.getObject(USER_BUCKET_NAME, fileName)) {
                byte[] bytes = IOUtils.toByteArray(object.getObjectContent());
                response.setAudioData(bytes);
            } catch (IOException e) {
                throw new IllegalStateException("failed to load history item due to ", e);
            }
        }

        return response;
    }

    private void validateListHistoryRequest(MoerdoRequest request) {
        validateWechatId(request.getWechatId());
        validateResponseType(request.getResponseType());

        if (Strings.isNullOrEmpty(request.getTimestamp())) {
            throw new IllegalArgumentException("Missing required argument: timestamp");
        }

        if (!DateTimeUtils.isValidInput(request.getTimestamp())) {
            throw new IllegalArgumentException(
                    "Invalid timestamp, it should be something like: 2019-01-01_00-00-00-000, but you gave " + request.getTimestamp());
        }

    }
}
