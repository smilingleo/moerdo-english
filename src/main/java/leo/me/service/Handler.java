package leo.me.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static leo.me.Constants.BASE_ZONE_ID;
import static leo.me.Constants.DRAWING_BUCKET_NAME;
import static leo.me.Constants.FREE_USER_CLASS;
import static leo.me.Constants.USER_BUCKET_NAME;
import static leo.me.anki.AnkiWebClient.GET_MEDIA_URL;
import static leo.me.utils.CharUtils.encodeBucketName;

import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import leo.me.Constants;
import leo.me.anki.GetCardsResponse;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;

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

    default void transformImageTag(GetCardsResponse ankiResponse, String cookie) {
        ankiResponse.getCards().forEach(ankiCard -> {
            String front = ankiCard.getFront();
            String backend = ankiCard.getBackend();

            Matcher frontMatcher = Constants.IMG_PATTERN.matcher(front);
            if (frontMatcher.matches()) {
                String imageName = frontMatcher.group(1);
                String imageContent = loadImage(GET_MEDIA_URL + imageName, new Header("cookie", cookie));
                front = front.replace(imageName, imageContent);
                ankiCard.setFront(front);
            }

            Matcher backendMatcher = Constants.IMG_PATTERN.matcher(backend);
            if (backendMatcher.matches()) {
                String imageName = backendMatcher.group(1);
                String imageContent = loadImage(GET_MEDIA_URL + imageName, new Header("cookie", cookie));
                backend = backend.replace(imageName, imageContent);
                ankiCard.setBackend(backend);
            }
        });
    }

    default void complementMnemonicImages(GetCardsResponse ankiResponse, String wechatId) {
        ankiResponse.getCards().forEach(ankiCard -> {
            String word = parseWord(ankiCard.getFront());
            String keyName = String.format("%s/%s/%s", DRAWING_BUCKET_NAME, encodeBucketName(word), wechatId);
            if (!isNullOrEmpty(word)) {
                try {
                    String imageContent = s3Client.getObjectAsString(USER_BUCKET_NAME, keyName);
                    if (!isNullOrEmpty(imageContent)) {
                        ankiCard.setImageData(imageContent);
                    }
                } catch (AmazonS3Exception e) {
                    System.out.println("failed to load image file:" + keyName);
                    if (!"NoSuchKey".equals(e.getErrorCode())) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * Parse the front HTML dom, and traverse the dom to find the word.
     *
     * @param front
     * @return
     */
    static String parseWord(String front) {
        Document doc = Jsoup.parse("<div>" + front + "</div>");
        return findWord(doc.getAllElements());
    }

    static String findWord(Elements elements) {
        for (Element element : elements) {
            String txt = element.ownText();
            if (!isNullOrEmpty(txt) && txt.trim().charAt(0) >= 'A' && txt.trim().charAt(0) <= 'z') {
                return txt.trim();
            }

            if (element.children().size() > 0) {
                String text = findWord(element.children());
                if (!isNullOrEmpty(text)) {
                    return text;
                }
            }
        }
        return "";
    }
}
