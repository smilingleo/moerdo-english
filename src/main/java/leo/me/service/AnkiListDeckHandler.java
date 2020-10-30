package leo.me.service;

import leo.me.anki.AnkiDeck;
import leo.me.anki.AnkiWebClient;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

import java.util.List;

/**
 * required parameters: openId, ankiUsername, ankiPassword
 */
public class AnkiListDeckHandler extends AbstractAnkiHandler {

    public AnkiListDeckHandler(AnkiWebClient client) {
        super(client);
    }

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = setOrRefreshAnkiCookie(request);

        List<AnkiDeck> decks = client.listDecks(userInfo.getAnkiWebCookie());

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setAnkiDecks(decks);

        return response;
    }
}
