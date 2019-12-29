package leo.me.service;

import leo.me.anki.AnkiWebClient;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

/**
 * required parameters: openId, deckId, ankiUsername, ankiPassword
 */
public class AnkiSetDeckHandler extends AbstractAnkiHandler {

    public AnkiSetDeckHandler(AnkiWebClient client) {
        super(client);
    }

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = setOrRefreshAnkiCookie(request);
        client.selectDeck(userInfo.getAnkiCookie(), request.getDeckId());
        final MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        return response;
    }
}
