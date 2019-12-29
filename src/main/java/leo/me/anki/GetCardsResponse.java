package leo.me.anki;

import java.util.List;

public class GetCardsResponse {

    private List<AnkiCard> cards;
    private List<Integer> counts;

    public GetCardsResponse(List<AnkiCard> cards, List<Integer> counts) {
        this.cards = cards;
        this.counts = counts;
    }

    public GetCardsResponse() {
    }

    public List<AnkiCard> getCards() {
        return cards;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public void setCards(List<AnkiCard> cards) {
        this.cards = cards;
    }

    public void setCounts(List<Integer> counts) {
        this.counts = counts;
    }
}
