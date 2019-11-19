package leo.me.polly;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class PollyConfig {

    private Region region = Region.getRegion(Regions.US_WEST_2);
    private String chineseVoiceId = "Zhiyu";
    private String englishVoiceId = "Salli";
    /**
     * special pause before a new English word.
     */
    private int pauseBeforeWord = 2;
    /**
     * pause between each item.
     */
    private int pause = 1;
    /**
     * pause between notes
     */
    private int pauseBetweenNotes = 2;

    private int exampleLimit = 2;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getChineseVoiceId() {
        return chineseVoiceId;
    }

    public void setChineseVoiceId(String chineseVoiceId) {
        this.chineseVoiceId = chineseVoiceId;
    }

    public String getEnglishVoiceId() {
        return englishVoiceId;
    }

    public void setEnglishVoiceId(String englishVoiceId) {
        this.englishVoiceId = englishVoiceId;
    }

    public int getPauseBeforeWord() {
        return pauseBeforeWord;
    }

    public void setPauseBeforeWord(int pauseBeforeWord) {
        this.pauseBeforeWord = pauseBeforeWord;
    }

    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public int getPauseBetweenNotes() {
        return pauseBetweenNotes;
    }

    public void setPauseBetweenNotes(int pauseBetweenNotes) {
        this.pauseBetweenNotes = pauseBetweenNotes;
    }

    public int getExampleLimit() {
        return exampleLimit;
    }

    public void setExampleLimit(int exampleLimit) {
        this.exampleLimit = exampleLimit;
    }
}
