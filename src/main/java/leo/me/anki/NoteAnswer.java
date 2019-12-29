package leo.me.anki;

import static java.lang.String.format;

public class NoteAnswer {

    /**
     * CardId
     */
    private long cardId;
    /**
     * the index of which button is pressed, start with 1.
     */
    private int answer;
    /**
     * Duration in milliseconds starting from the card is shown,
     * end at the time the answer button is pressed.
     * Should be measured at client side.
     */
    private long timeSpent;

    public NoteAnswer(long cardId, int answer, long timeSpent) {
        this.cardId = cardId;
        this.answer = answer;
        this.timeSpent = timeSpent;
    }

    public NoteAnswer() {
    }

    public long getCardId() {
        return cardId;
    }

    public int getAnswer() {
        return answer;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    @Override
    public String toString() {
        return format("[%d,%d,%d]", cardId, answer, timeSpent);
    }
}
