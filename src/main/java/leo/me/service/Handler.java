package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.BASE_ZONE_ID;
import static leo.me.Constants.FREE_USER_CLASS;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import leo.me.exception.ClientSideException;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.internal.http2.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

public interface Handler {
    Log log = LogFactory.getLog(Handler.class);
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
    ObjectMapper objectMapper = ObjectMapperSingleton.getObjectMapper();

    MoerdoResponse handle(MoerdoRequest request);

    default void validateResponseType(String responseType) {
        if (!("link".equalsIgnoreCase(responseType) || "data".equalsIgnoreCase(responseType))) {
            throw new ClientSideException("invalid responseType, must be either 'link' or 'data', but you give: " + responseType);
        }
    }

    default void validateWechatId(String wechatId) {
        if (Strings.isNullOrEmpty(wechatId)) {
            throw new ClientSideException("Missing required argument: wechatId");
        }
    }

    /**
     * Set the last login date.
     * @param request
     * @return
     */
    default UserInfo refreshUserInfo(MoerdoRequest request) {
        String wechatId = request.getWechatId();
        final String userInfoPath = format("%s/userInfo.json", wechatId);
        UserInfo userInfo = new UserInfo();

        final LocalDateTime now = LocalDateTime.now(BASE_ZONE_ID);

        if (s3Client.doesObjectExist(USER_BUCKET_NAME, userInfoPath)) {
            S3Object object = null;
            try {
                object = s3Client.getObject(USER_BUCKET_NAME, userInfoPath);
                String jsonString = IOUtils.toString(object.getObjectContent());
                userInfo = objectMapper.readValue(jsonString, UserInfo.class);
                userInfo.setLastLogin(now.toString());
            } catch (IOException e) {
                throw new ServerSideException("Invalid userInfo.json content found.", e);
            } finally {
                IOUtils.closeQuietly(object, log);
            }
        } else {
            // new user, create the userInfo.json file
            userInfo.setWechatId(wechatId);
            userInfo.setCreatedOn(now.toString());
            userInfo.setLastLogin(now.toString());
            userInfo.setPreference(request.getOptions());
            userInfo.setUserClass(FREE_USER_CLASS);
            try {
                s3Client.putObject(USER_BUCKET_NAME, userInfoPath, objectMapper.writeValueAsString(userInfo));
            } catch (JsonProcessingException e) {
                throw new ServerSideException("Failed to serialize userInfo", e);
            }
        }

        return userInfo;
    }

    default void updateUserInfo(UserInfo userInfo) {
        final String userInfoPath = format("%s/userInfo.json", userInfo.getWechatId());
        try {
            s3Client.putObject(USER_BUCKET_NAME, userInfoPath, objectMapper.writeValueAsString(userInfo));
        } catch (JsonProcessingException e) {
            throw new ServerSideException("Failed to serialize userInfo", e);
        }
    }

    default void evaluateLimit(UserInfo userInfo) {
        // enforce the usage policy
        (new UserUsagePolicy()).evaluate(userInfo, s3Client);
    }

    default String loadImage(String url, Header... headers) {
        String ext = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
        if (ext.length() == 0) {
            throw new ServerSideException("图片地址非法，或者无法解析图片格式." + url);
        }

        if (ext.length() >= 4 && ext.startsWith("jpeg")) {
            ext = "jpeg";
        } else if (ext.length() >= 3){
            ext = ext.substring(0, 3);
        }

        OkHttpClient client = new OkHttpClient();

        Builder builder = new Builder()
                .url(url);
        if (headers != null) {
            Arrays.asList(headers).forEach(header -> builder.header(header.name.utf8(), header.value.utf8()));
        }
        Request loadImgReq = builder
                .get()
                .build();

        try (Response response = client.newCall(loadImgReq).execute()) {
            byte[] fileContent = response.body().bytes();
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            return String.format("data:image/%s;base64, %s", ext, encodedString);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerSideException("加载图片失败。");
        }

    }
}
