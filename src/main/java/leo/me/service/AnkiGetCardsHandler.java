package leo.me.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static leo.me.Constants.DRAWING_BUCKET_NAME;
import static leo.me.Constants.USER_BUCKET_NAME;
import static leo.me.anki.AnkiWebClient.GET_MEDIA_URL;
import static leo.me.utils.CharUtils.encodeBucketName;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import leo.me.anki.AnkiWebClient;
import leo.me.anki.GetCardsResponse;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import leo.me.lambda.vo.UserInfo;
import okhttp3.internal.http2.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            String keyName = String.format("%s/%s/%s", DRAWING_BUCKET_NAME, encodeBucketName(word), wechatId);
            if (!isNullOrEmpty(word)) {
                try {
                    String imageContent = s3Client.getObjectAsString(USER_BUCKET_NAME, keyName);
                    if (!isNullOrEmpty(imageContent)) {
                        ankiCard.setImageData(imageContent);
                    }
                } catch (AmazonS3Exception e) {
                    System.out.println("failed to load image file:" + keyName);
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
            String txt = element.ownText();
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
                String imageContent = loadImage(GET_MEDIA_URL + imageName, new Header("cookie", cookie));
                front = front.replace(imageName, imageContent);
                ankiCard.setFront(front);
            }

            Matcher backendMatcher = IMG_PATTERN.matcher(backend);
            if (backendMatcher.matches()) {
                String imageName = backendMatcher.group(1);
                String imageContent = loadImage(GET_MEDIA_URL + imageName, new Header("cookie", cookie));
                backend = backend.replace(imageName, imageContent);
                ankiCard.setBackend(backend);
            }
        });
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
        System.out.println(handler.parseWord(front));
    }
}
