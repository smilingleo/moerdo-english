package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.USER_BUCKET_NAME;
import static leo.me.Constants.VALID_USER_CLASS;

import com.google.common.base.Strings;
import leo.me.exception.ClientSideException;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

import java.io.IOException;
import java.util.stream.Collectors;

public class UserManagementHandler implements Handler {

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        validateRequest(request);

        final String wechatId = request.getWechatId();
        final String userInfoPath = format("%s/userInfo.json", wechatId);
        final String jsonStr = s3Client.getObjectAsString(USER_BUCKET_NAME, userInfoPath);
        try {
            UserInfo userInfo = objectMapper.readValue(jsonStr, UserInfo.class);
            userInfo.setUserClass(request.getUserClass());

            // save the userInfo
            s3Client.putObject(USER_BUCKET_NAME, userInfoPath, objectMapper.writeValueAsString(userInfo));

            MoerdoResponse response = new MoerdoResponse();
            response.setUserInfo(userInfo);

            return response;
        } catch (IOException e) {
            throw new ServerSideException("更新用户信息时发生错误:" + userInfoPath, e);
        }
    }

    private void validateRequest(MoerdoRequest request) {
        if (Strings.isNullOrEmpty(request.getUserClass())) {
            throw new ClientSideException("请求中需指定'userClass'参数。");
        }

        if (!VALID_USER_CLASS.contains(request.getUserClass())) {
            throw new ClientSideException(
                    "检测到非法'userClass'参数, 合法项为: " + VALID_USER_CLASS.stream().collect(Collectors.joining(",")));
        }
    }

}
