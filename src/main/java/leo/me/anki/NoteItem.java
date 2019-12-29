package leo.me.anki;

import com.google.common.base.Strings;

import java.util.Arrays;

public enum NoteItem {
    WORD("word", "w", 1),
    GENERAL_CHINESE("general-chinese", "g", 1),
    SPELL("spell", "s", 1),
    DETAILED_CHINESE("detailed-chinese", "d", 1),
    INTERPRETATION_ENGLISH("interpretation-english", "i", 1),
    EXAMPLE_ENGLISH("example-english", "e", 1),
    EXAMPLE_CHINESE("example-chinese", "c", 1),
    PAUSE_BETWEEN_NOTE("pause-between-notes", "p", 1);

    private String longName;
    private String shortName;
    private int repeat;

    NoteItem(String longName, String shortName, int repeat) {
        this.longName = longName;
        this.shortName = shortName;
        this.repeat = repeat;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setRepeat(int repeat) { this.repeat = repeat; }

    public int getRepeat() { return this.repeat; }

    public static NoteItem fromShort(String shortName) {
        String option = shortName.substring(0, 1);
        String repeatStr = shortName.substring(1);
        NoteItem rtn = Arrays.stream(NoteItem.values()).filter(i -> i.shortName.equals(option)).findFirst().orElse(null);
        if (!Strings.isNullOrEmpty(repeatStr)) {
            rtn.setRepeat(Integer.parseInt(repeatStr));
        }
        return rtn;
    }

}
