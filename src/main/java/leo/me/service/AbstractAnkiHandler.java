package leo.me.service;

import com.google.common.base.Strings;
import leo.me.anki.AnkiCookies;
import leo.me.anki.AnkiWebClient;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.vo.UserInfo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractAnkiHandler implements Handler {
    protected AnkiWebClient client;

    public AbstractAnkiHandler(AnkiWebClient client) {
        this.client = client;
    }

    private boolean ankiCookieExpired(String expiredOn) {
        if (Strings.isNullOrEmpty(expiredOn)) {
            return true;
        }
        return LocalDateTime.parse(expiredOn).isBefore(LocalDateTime.now());
    }

    private String parseCookie(String cookieString) {
        Optional<String> expireOpt = Arrays.stream(cookieString.split(";")).filter(item -> item.trim().startsWith("ankiweb")).findFirst();
        return expireOpt.orElseThrow(() -> new ServerSideException("登录AnkiWeb出错，无法获取登录凭证。"));
    }

    public UserInfo setOrRefreshAnkiCookie(MoerdoRequest request) {
        UserInfo userInfo = refreshUserInfo(request);

        // cookie for `ankiuser.net`
        String ankiCookie = userInfo.getAnkiCookie();

        // setOrRefreshCookie
        if (Strings.isNullOrEmpty(ankiCookie) || ankiCookieExpired(userInfo.getCookieExpiredOn())) {
            AnkiCookies cookie = client.getCookie(request.getAnkiUsername(), request.getAnkiPassword());
            ankiCookie = parseCookie(cookie.getAnkiUserCookie());
            userInfo.setAnkiCookie(ankiCookie);
            userInfo.setAnkiWebCookie(parseCookie(cookie.getAnkiWebCookie()));
            // the official site set-cookie: Max-Age=2592000; (30 days)
            String expiredOn = LocalDateTime.now().plusDays(25).toString();
            userInfo.setCookieExpiredOn(expiredOn);
            updateUserInfo(userInfo);
        }

        return userInfo;
    }

}
