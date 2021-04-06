package leo.me.service;

import leo.me.anki.AnkiCard;
import leo.me.anki.AnkiNoteDao;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Query words, return AnkiCards.
 *
 */
public class QueryWordsHandler implements Handler {

    private final static String FRONT_FMT = "<div id=\"word\">\n<span style=\"font-size: 48px;\">{{word}}</span></div>";

    /**
     * Request: {
     *     wechatId: '',
     *     words: []
     * }
     *
     * Response: {
     *     ankiCards: []
     * }
     * @param request
     * @return
     */
    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        final AnkiNoteDao dao = new AnkiNoteDao(":resource:collection.anki2");
        List<String> notes = dao.findNotes(
                request.getWords().stream()
                        .map(word -> word.trim().toLowerCase())
                        .collect(Collectors.toList())
                        .toArray(new String[]{})
        );
        List<AnkiCard> ankiCards = notes.stream()
                .map(content -> {
                    int pos = content.indexOf("<div");
                    final String word = content.substring(0, pos - 1);
                    // replace word
                    String front = FRONT_FMT.replace("{{word}}", word);

                    String backendContent = content.substring(pos);
                    String imageData = null;
                    return new AnkiCard(0, 0, word, front + backendContent, imageData, 0, Collections.emptyList());
                })
                .collect(Collectors.toList());
        MoerdoResponse response = new MoerdoResponse();
        GetCardsResponse cardsResponse = new GetCardsResponse(ankiCards, Collections.emptyList());

        complementMnemonicImages(cardsResponse, request.getWechatId());

        response.setAnkiCards(cardsResponse);
        return response;
    }

}
