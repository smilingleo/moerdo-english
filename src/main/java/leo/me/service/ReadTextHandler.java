package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.FREE_USER_TEXT_LIMIT;
import static leo.me.Constants.PAID_USER_TEXT_LIMIT;

import com.google.common.base.Strings;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

public class ReadTextHandler implements Handler {

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = refreshUserInfo(request);

        evaluateLimit(userInfo);

        validateRequest(userInfo, request);

        //TODO: automatically determin the test is english or chinese.
        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        return response;
    }

    private void validateRequest(UserInfo userInfo, MoerdoRequest request) {
        if (Strings.isNullOrEmpty(request.getText())) {
            throw new IllegalArgumentException("Missing required argument 'text'");
        }

        if (userInfo.isFreeUser() && request.getText().length() > FREE_USER_TEXT_LIMIT) {
            throw new IllegalArgumentException(format("免费用户段落阅读字数不能超过%d, 请升级到付费用户。", FREE_USER_TEXT_LIMIT));
        }
        if (userInfo.isPaidUser() && request.getText().length() > PAID_USER_TEXT_LIMIT) {
            throw new IllegalArgumentException(format("段落阅读字数不能超过%d, 请减少字数后重试。", PAID_USER_TEXT_LIMIT));
        }
    }
}
