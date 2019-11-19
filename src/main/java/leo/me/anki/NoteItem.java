package leo.me.anki;

import java.util.Arrays;

public enum NoteItem {
    WORD("word", "w"),
    GENERAL_CHINESE("general-chinese", "g"),
    SPELL("spell", "s"),
    DETAILED_CHINESE("detailed-chinese", "d"),
    INTERPRETATION_ENGLISH("interpretation-english", "i"),
    EXAMPLE_ENGLISH("example-english", "e"),
    EXAMPLE_CHINESE("example-chinese", "c"),
    PAUSE_BETWEEN_NOTE("pause-between-notes", "p");

    private String longName;
    private String shortName;

    NoteItem(String longName, String shortName) {
        this.longName = longName;
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public static NoteItem fromShort(String shortName) {
        return Arrays.stream(NoteItem.values()).filter(i -> i.shortName.equals(shortName)).findFirst().orElse(null);
    }

}
