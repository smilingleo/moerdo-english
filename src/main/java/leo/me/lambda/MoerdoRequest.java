package leo.me.lambda;

import static leo.me.Constants.CMD_READ_WORDS;
import static leo.me.Constants.FREE_USER_CLASS;

import leo.me.anki.BatchAnswer;

import java.util.List;

public class MoerdoRequest {

    /**
     * Not Null, this is end user wechat id.
     */
    private String wechatId;

    /**
     * Command type: READ_WORDS, READ_TEXT, LIST_HISTORY, CHANGE_USER
     *
     */
    private String command = CMD_READ_WORDS;

    /**
     * Vocabulary mode.
     * Must be comma separated english word.
     *
     * Also used in GetImages command.
     */
    private List<String> words;

    /**
     * English word reading options. Only valid if `words` is not null.
     */
    private String options = "g,w,s,d,i,c,e,p";

    /**
     * Valid for `words` mode.
     */
    private int exampleLimit = -1;

    /**
     * Text to Speech mode.
     * Must less than 5000 words.
     */
    private String text;


    /**
     * Image content data (base64 encoded)
     * Only for CMD_SAVE_IMAGE
     *
     */
    private String imageContent;

    /**
     * Image url of selected image
     * Only for CMD_USE_IMAGE
     */
    private String imageUrl;

    /**
     * valid options: link, data
     */
    private String responseType = "link";


    /**
     * user class, only valid in CHANGE_USER command
     */
    private String userClass = FREE_USER_CLASS;

    /**
     * client code, used to get wechat openid, only valid in GET_OPENID
     */
    private String code;

    /**
     * deckId, used only for AnkiWeb related commands.
     */
    private String deckId;

    /**
     * used only for LIST_ANKI_DECKS, GET_CARDS
     */
    private String ankiUsername;
    private String ankiPassword;

    /**
     * answers of the previous batch
     */
    private BatchAnswer batchAnswer = BatchAnswer.empty();

    public MoerdoRequest() {
    }

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public int getExampleLimit() {
        return exampleLimit;
    }

    public void setExampleLimit(int exampleLimit) {
        this.exampleLimit = exampleLimit;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public BatchAnswer getBatchAnswer() {
        return batchAnswer;
    }

    public void setBatchAnswer(BatchAnswer batchAnswer) {
        this.batchAnswer = batchAnswer;
    }

    public String getAnkiUsername() {
        return ankiUsername;
    }

    public void setAnkiUsername(String ankiUsername) {
        this.ankiUsername = ankiUsername;
    }

    public String getAnkiPassword() {
        return ankiPassword;
    }

    public void setAnkiPassword(String ankiPassword) {
        this.ankiPassword = ankiPassword;
    }
}
