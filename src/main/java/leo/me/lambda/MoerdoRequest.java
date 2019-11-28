package leo.me.lambda;

import static leo.me.Constants.CMD_READ_WORDS;
import static leo.me.Constants.FREE_USER_CLASS;

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
     * In case of loading history audio
     */
    private String timestamp;

    /**
     * Text to Speech mode.
     * Must less than 5000 words.
     */
    private String text;

    /**
     * valid options: link, data
     */
    private String responseType = "link";


    /**
     * user class, only valid in CHANGE_USER command
     */
    private String userClass = FREE_USER_CLASS;

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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }
}
