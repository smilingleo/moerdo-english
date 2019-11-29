package leo.me.lambda;

public class MoerdoResponse {
    private byte[] audioData;
    private String uri;

    /**
     * For every request, carry userInfo.
     */
    private UserInfo userInfo;

    private String timestamp;

    /**
     * This is for get open-id call.
     */
    private String openId;

    public MoerdoResponse() {
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
