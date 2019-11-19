package leo.me.anki;

public class AnkiNoteItemGroup {
    private String chinese;
    private String explanation;
    private String chineseExample;
    private String englishExample;

    public AnkiNoteItemGroup() {
    }

    public String getChinese() {
        return chinese;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getChineseExample() {
        return chineseExample;
    }

    public String getEnglishExample() {
        return englishExample;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setChineseExample(String chineseExample) {
        this.chineseExample = chineseExample;
    }

    public void setEnglishExample(String englishExample) {
        this.englishExample = englishExample;
    }
}
