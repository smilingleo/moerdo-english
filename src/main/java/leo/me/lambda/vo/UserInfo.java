package leo.me.lambda.vo;

import static leo.me.Constants.FREE_USER_CLASS;
import static leo.me.Constants.PAID_USER_CLASS;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.StringJoiner;

public class UserInfo {
    private String wechatId;
    private String name;
    private String createdOn;
    private String lastLogin;
    /**
     * How many points are left, each point can submit a new READ_WORDS command.
     */
    private int leftPoints;
    /**
     * Read preference.
     */
    private String preference;
    /**
     * Free user: 0
     * Paid user: 1
     * Use string-type to make it extensible
     */
    private String userClass;

    /**
     * Used to login AnkiUser.net
     */
    private String ankiCookie;

    private String ankiWebCookie;

    private String cookieExpiredOn;

    public UserInfo() {
    }

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getLeftPoints() {
        return leftPoints;
    }

    public void setLeftPoints(int leftPoints) {
        this.leftPoints = leftPoints;
    }

    @JsonIgnore
    public boolean isFreeUser() {
        return Objects.equals(FREE_USER_CLASS, this.getUserClass());
    }

    @JsonIgnore
    public boolean isPaidUser() {
        return Objects.equals(PAID_USER_CLASS, this.getUserClass());
    }

    public String getAnkiCookie() {
        return ankiCookie;
    }

    public void setAnkiCookie(String ankiCookie) {
        this.ankiCookie = ankiCookie;
    }

    public String getAnkiWebCookie() {
        return ankiWebCookie;
    }

    public void setAnkiWebCookie(String ankiWebCookie) {
        this.ankiWebCookie = ankiWebCookie;
    }

    public String getCookieExpiredOn() {
        return cookieExpiredOn;
    }

    public void setCookieExpiredOn(String cookieExpiredOn) {
        this.cookieExpiredOn = cookieExpiredOn;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserInfo.class.getSimpleName() + "[", "]")
                .add("wechatId='" + wechatId + "'")
                .add("name='" + name + "'")
                .add("createdOn='" + createdOn + "'")
                .add("lastLogin='" + lastLogin + "'")
                .add("leftPoints=" + leftPoints)
                .add("preference='" + preference + "'")
                .add("userClass='" + userClass + "'")
                .add("ankiCookie='" + ankiCookie + "'")
                .add("ankiWebCookie='" + ankiWebCookie + "'")
                .add("cookieExpiredOn='" + cookieExpiredOn + "'")
                .toString();
    }
}
