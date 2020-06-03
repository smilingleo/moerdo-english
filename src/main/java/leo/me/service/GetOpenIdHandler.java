package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.APP_ID;
import static leo.me.Constants.APP_SECRET;
import static leo.me.Constants.GET_OPENID_URI;

import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.google.common.base.Strings;
import leo.me.exception.ClientSideException;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class GetOpenIdHandler implements Handler {

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        OkHttpClient client = new OkHttpClient();
        MoerdoResponse rtn = new MoerdoResponse();

        if (Strings.isNullOrEmpty(APP_ID) || Strings.isNullOrEmpty(APP_SECRET)) {
            throw new ServerSideException("Environment variables APP_ID, APP_SECRET are missing.");
        }

        String code = request.getCode();
        if (Strings.isNullOrEmpty(code)) {
            throw new ClientSideException("请求中缺失必需参数'code'.");
        }

        String url = format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", GET_OPENID_URI, APP_ID, APP_SECRET, code);
        Request httpRequest = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseStr = response.body().string();
            Map<String, Object> responseMap = ObjectMapperSingleton.getObjectMapper().readValue(responseStr, Map.class);
            if (responseMap.containsKey("openid")) {
                rtn.setOpenId((String)responseMap.get("openid"));
            } else {
                throw new ServerSideException("微信登录失败，未能获取open-id，可能因为:" + responseMap.toString());
            }
        } catch (IOException e) {
            throw new ServerSideException("Failed to get open-id due to ", e);
        }
        return rtn;
    }
}
