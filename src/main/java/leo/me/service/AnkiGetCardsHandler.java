package leo.me.service;

import leo.me.anki.AnkiWebClient;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

/**
 * required parameters: openId, deckId, ankiUsername, ankiPassword
 */
public class AnkiGetCardsHandler extends AbstractAnkiHandler {

    public AnkiGetCardsHandler(AnkiWebClient client) {
        super(client);
    }

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = setOrRefreshAnkiCookie(request);

        final String ankiCookie = userInfo.getAnkiCookie();

        client.selectDeck(userInfo.getAnkiWebCookie(), request.getDeckId());
        GetCardsResponse ankiResponse = client.getCards(ankiCookie, request.getBatchAnswer());

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setAnkiCards(ankiResponse);

        return response;
    }
}
