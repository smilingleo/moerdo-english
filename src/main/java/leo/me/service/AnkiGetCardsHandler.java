package leo.me.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static leo.me.Constants.DRAWING_BUCKET_NAME;
import static leo.me.Constants.USER_BUCKET_NAME;
import static leo.me.utils.CharUtils.encodeBucketName;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import leo.me.anki.AnkiCard;
import leo.me.anki.AnkiWebClient;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        transformImageTag(ankiResponse, ankiCookie);
        complementMnemonicImages(ankiResponse, request.getWechatId());

        MoerdoResponse response = new MoerdoResponse();
        response.setUserInfo(userInfo);
        response.setAnkiCards(ankiResponse);

        return response;
    }

    private void complementMnemonicImages(GetCardsResponse ankiResponse, String wechatId) {
        ankiResponse.getCards().forEach(ankiCard -> {
            String word = parseWord(ankiCard.getFront());
            if (!isNullOrEmpty(word)) {
                try {
                    String keyName = String.format("%s/%s/%s", DRAWING_BUCKET_NAME, encodeBucketName(word), wechatId);
                    String imageContent = s3Client.getObjectAsString(USER_BUCKET_NAME, keyName);

                    System.out.println("loading image file path:" + keyName);

                    if (!isNullOrEmpty(imageContent)) {
                        ankiCard.setImageData(imageContent);
                    }
                } catch (AmazonS3Exception e) {
                    if (!"NoSuchKey".equals(e.getErrorCode())) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Parse the front HTML dom, and traverse the dom to find the word.
     *
     * @param front
     * @return
     */
    protected String parseWord(String front) {
        Document doc = Jsoup.parse("<div>" + front + "</div>");
        return findWord(doc.getAllElements());
    }

    private String findWord(Elements elements) {
        for (Element element : elements) {
            String txt = element.text();
            if (!isNullOrEmpty(txt) && txt.trim().charAt(0) >= 'A' && txt.trim().charAt(0) <= 'z') {
                return txt.trim();
            }

            if (element.children().size() > 0) {
                String text = findWord(element.children());
                if (!isNullOrEmpty(text)) {
                    return text;
                }
            }
        }
        return "";
    }

    private void transformImageTag(GetCardsResponse ankiResponse, String cookie) {
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

        String front = "<style>.card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: left;\\n color: black;\\n background-color: white;\\n}\\n</style><div style=\\\"text-align: center;\\\">reap</div>";
        System.out.println(handler.parseWord(front));
    }
}
