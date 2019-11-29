package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.GET_OPENID_URI;

import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.google.common.base.Strings;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class GetOpenIdHandler implements Handler {

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        OkHttpClient client = new OkHttpClient();
        MoerdoResponse rtn = new MoerdoResponse();

        String appId = System.getenv("APP_ID");
        String appSecret = System.getenv("APP_SECRET");
        if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(appSecret)) {
            throw new IllegalStateException("Environment variables APP_ID, APP_SECRET are missing.");
        }

        String code = request.getCode();
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalArgumentException("Required argument 'code' is missing.");
        }

        String url = format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", GET_OPENID_URI, appId, appSecret, code);
        Request httpRequest = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseStr = response.body().string();
            Map<String, Object> responseMap = ObjectMapperSingleton.getObjectMapper().readValue(responseStr, Map.class);
            if (responseMap.containsKey("openid")) {
                rtn.setOpenId((String)responseMap.get("openid"));
            } else {
                throw new IllegalStateException("Failed to get open-id, response:" + responseMap.toString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get open-id due to ", e);
        }
        return rtn;
    }
}
