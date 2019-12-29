package leo.me.anki;

import java.util.List;

public class AnkiCard {

    private long cardId;
    private long noteId;
    private String front;
    private String backend;
    /**
     * 0: new word, 1: re-study, 2: to review
     */
    private int group;
    private List<String> repeats;

    public AnkiCard(long cardId, long noteId, String front, String backend, int group, List<String> repeats) {
        this.cardId = cardId;
        this.noteId = noteId;
        this.front = front;
        this.backend = backend;
        this.group = group;
        this.repeats = repeats;
    }

    public AnkiCard() {
    }

    public long getCardId() {
        return cardId;
    }

    public long getNoteId() {
        return noteId;
    }

    public String getFront() {
        return front;
    }

    public String getBackend() {
        return backend;
    }

    public List<String> getRepeats() {
        return repeats;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public void setRepeats(List<String> repeats) {
        this.repeats = repeats;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
