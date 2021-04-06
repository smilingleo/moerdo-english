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
        // System.out.println("UserInfo used for GetCard:" + userInfo.toString());
        client.selectDeck(userInfo.getAnkiWebCookie(), request.getDeckId());
        GetCardsResponse ankiResponse = client.getCards(ankiCookie, request.getBatchAnswer());

        transformImageTag(ankiResponse, ankiCookie);
        complementMnemonicImages(ankiResponse, request.getWechatId());

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setAnkiCards(ankiResponse);

        return response;
    }



    public static void main(String[] args) {
        AnkiGetCardsHandler handler = new AnkiGetCardsHandler(new AnkiWebClient());

        String front = "\"<style>.card {\n"
                + " font-family: Microsoft Yahei;\n"
                + " background-color: #fdf6e3;\n"
                + " line-height: 200%;\n"
                + " text-align: left;\n"
                + " color: black;\n"
                + "}\n"
                + "#word{\n"
                + "\tpadding-top:15px;\n"
                + "}\n"
                + "#answer{\n"
                + "\theight:4px;\n"
                + "\tcolor:#073642;\n"
                + "\tbackground-color:#073642;\n"
                + "\tborder-width:0px;\n"
                + "\twidth: 80%;\n"
                + "\tmargin:15px auto;\n"
                + "}\n"
                + "#back{\n"
                + "\tmargin:10px 10px;\n"
                + "\tpadding-bottom:10px;\n"
                + "\tLine-height:1.5\n"
                + "}</style><center>\n"
                + "<div id=\"word\">\n"
                + "<span style=\"font-size: 48px;\">gaunt</span>\n"
                + "<br>\n"
                + "<br><span style=\"font-family:'Lucida Sans Unicode',Arial;font-size:30px;\"><div style='color:Black'>英[gɔ:nt]  美[ɡɔnt]</div></span>\n"
                + "</div></center>\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\"";
        System.out.println(Handler.parseWord(front));
    }
}
