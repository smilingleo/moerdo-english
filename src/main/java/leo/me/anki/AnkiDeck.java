package leo.me.anki;

public class AnkiDeck {
    private String deckName;
    private String deckId;
    private int newWords;
    private int reviewWords;

    public AnkiDeck() {
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public int getNewWords() {
        return newWords;
    }

    public void setNewWords(int newWords) {
        this.newWords = newWords;
    }

    public int getReviewWords() {
        return reviewWords;
    }

    public void setReviewWords(int reviewWords) {
        this.reviewWords = reviewWords;
    }
}
