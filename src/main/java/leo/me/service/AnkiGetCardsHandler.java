package leo.me.service;

import leo.me.anki.AnkiCard;
import leo.me.anki.AnkiWebClient;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * required parameters: openId, deckId, ankiUsername, ankiPassword
 */
public class AnkiGetCardsHandler extends AbstractAnkiHandler {

    // Pattern.DOTALL or (?s) tells Java to allow the dot to match newline characters, too.
    private final static Pattern IMG_PATTERN = Pattern.compile(".*<img\\s+[^>]*src=\"([\\w\\-.]+)\"[^>]*/?>.*", Pattern.DOTALL);
    public AnkiGetCardsHandler(AnkiWebClient client) {
        super(client);
    }

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        UserInfo userInfo = setOrRefreshAnkiCookie(request);

        final String ankiCookie = userInfo.getAnkiCookie();

        client.selectDeck(userInfo.getAnkiWebCookie(), request.getDeckId());
        GetCardsResponse ankiResponse = client.getCards(ankiCookie, request.getBatchAnswer());

        transformResponse(ankiResponse, ankiCookie);

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setAnkiCards(ankiResponse);

        return response;
    }

    private void transformResponse(GetCardsResponse ankiResponse, String cookie) {
        ankiResponse.getCards().forEach(ankiCard -> {
            String front = ankiCard.getFront();
            String backend = ankiCard.getBackend();

            Matcher frontMatcher = IMG_PATTERN.matcher(front);
            if (frontMatcher.matches()) {
                String imageName = frontMatcher.group(1);
                String imageContent = client.loadImageData(cookie, imageName);
                front = front.replace(imageName, imageContent);
                ankiCard.setFront(front);
            }

            Matcher backendMatcher = IMG_PATTERN.matcher(backend);
            if (backendMatcher.matches()) {
                String imageName = backendMatcher.group(1);
                String imageContent = client.loadImageData(cookie, imageName);
                backend = backend.replace(imageName, imageContent);
                ankiCard.setBackend(backend);
            }
        });
    }

    public static void main(String[] args) {
        AnkiGetCardsHandler handler = new AnkiGetCardsHandler(new AnkiWebClient());

        GetCardsResponse response = new GetCardsResponse();
        response.setCards(new LinkedList<>());
        String front = "<style>.card {\n font-family: arial;\n font-size: 20px;\n text-align: center;\n color: black;\n background-color: white;\n}\n\n.cloze {\n font-weight: bold;\n color: blue;\n}</style><img src=\"mural.jpeg\">\n<br>\nA <span class=cloze>[...]</span> is a picture painted on a wall.";
        String backend = "";
        response.getCards().add(new AnkiCard(1, 1, front, backend, 1, Arrays.asList("s")));

        handler.transformResponse(response, "ankiweb=eyJrIjogInMxUlpIRG05aDJndUFMNEYiLCAiYyI6IDJ9.iZyn1EtpFMHM2X7NIZLUJHi9fgpwQgCBhbJmaAIfYPg");
        System.out.println(response.getCards().get(0).getFront());
    }
}
