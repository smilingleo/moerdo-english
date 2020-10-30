package leo.me.anki;

public class AnkiCookies {
    private String ankiWebCookie;
    private String ankiUserCookie;

    public AnkiCookies(String ankiWebCookie, String ankiUserCookie) {
        this.ankiWebCookie = ankiWebCookie;
        this.ankiUserCookie = ankiUserCookie;
    }

    public String getAnkiWebCookie() {
        return ankiWebCookie;
    }

    public String getAnkiUserCookie() {
        return ankiUserCookie;
    }
}
