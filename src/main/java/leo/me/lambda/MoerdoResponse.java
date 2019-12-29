package leo.me.lambda;

import leo.me.anki.AnkiDeck;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.vo.HistoryRecord;
import leo.me.lambda.vo.UserInfo;

import java.util.List;

public class MoerdoResponse {

    /**
     * for read-words call
     */
    private byte[] audioData;
    private String uri;
    /**
     * For every request, carry userInfo.
     */
    private UserInfo userInfo;

    /**
     * For list history call.
     */
    private List<HistoryRecord> historyRecords;

    /**
     * This is for get open-id call.
     */
    private String openId;

    /**
     * This is for AnkiWeb command
     */
    private GetCardsResponse ankiCards;

    private List<AnkiDeck> ankiDecks;

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

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public List<HistoryRecord> getHistoryRecords() {
        return historyRecords;
    }

    public void setHistoryRecords(List<HistoryRecord> historyRecords) {
        this.historyRecords = historyRecords;
    }

    public GetCardsResponse getAnkiCards() {
        return ankiCards;
    }

    public void setAnkiCards(GetCardsResponse ankiCards) {
        this.ankiCards = ankiCards;
    }

    public List<AnkiDeck> getAnkiDecks() {
        return ankiDecks;
    }

    public void setAnkiDecks(List<AnkiDeck> ankiDecks) {
        this.ankiDecks = ankiDecks;
    }
}
