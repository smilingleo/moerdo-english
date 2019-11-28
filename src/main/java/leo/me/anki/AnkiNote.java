package leo.me.anki;

import leo.me.polly.PollyConfig;

import java.util.LinkedList;
import java.util.List;

public class AnkiNote {
    private String word = "";
    private String spell = "";
    private String chinese = "";
    private List<AnkiNoteItemGroup> itemGroups = new LinkedList<>();

    public AnkiNote() {
    }

    public String getWord() {
        return word;
    }

    public String getSpell() {
        return spell;
    }

    public void setSpell(String spell) {
        this.spell = spell;
    }

    public String getChinese() {
        return chinese;
    }

    public List<AnkiNoteItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public void setItemGroups(List<AnkiNoteItemGroup> itemGroups) {
        this.itemGroups = itemGroups;
    }

    public String getEncodedWord(PollyConfig config) {
        return getBreakBeforeWord(config) + word + getBreak(config);
    }

    private String getBreak(PollyConfig config) {
        return "<break time=\"" + config.getPause() + "s\"/>";
    }

    private String getBreakBeforeWord(PollyConfig config) {
        return "<break time=\"" + config.getPauseBeforeWord() + "s\"/>";
    }

}
