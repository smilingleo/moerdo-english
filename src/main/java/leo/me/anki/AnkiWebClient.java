package leo.me.anki;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import leo.me.exception.ServerSideException;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnkiWebClient {

    private static final String LOGIN_URL = "https://ankiweb.net/account/login";
    private static final String LIST_DECK_URL = "https://ankiweb.net/decks/";
    private static final String GET_CARDS_URL = "https://ankiuser.net/study/getCards";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * List all the decks of current user.
     *
     * @param ankiCookie
     * @return
     */
    public List<AnkiDeck> listDecks(String ankiCookie) {
        OkHttpClient client = new OkHttpClient();
        Request listReq = new Request.Builder()
                .url(LIST_DECK_URL)
                .get()
                .header(":authority:", "ankiweb.net")
                .header(":method:", "GET")
                .header(":path:", "/decks/")
                .header(":scheme:", "https")
                .header("cookie", ankiCookie)
                .build();

        List<AnkiDeck> decks = new LinkedList<>();

        try (Response response = client.newCall(listReq).execute()) {
            String listPageHtml = response.body().string();
            Document doc = Jsoup.parse(listPageHtml);
            Elements elements = doc.select("main.container > div.container-fluid > div.row");
            for (Element element : elements) {
                Element btnElement = element.selectFirst("button[id^=did]");
                String deckId = btnElement.attr("id");
                String deckName = btnElement.text();
                List<Integer> dueNumbers = element.select("div.deckDueNumber > font").stream()
                        .map(ele -> Integer.parseInt(ele.text()))
                        .collect(Collectors.toList());
                AnkiDeck deck = new AnkiDeck();
                deck.setDeckId(deckId);
                deck.setDeckName(deckName);
                deck.setNewWords(dueNumbers.get(0));
                deck.setReviewWords(dueNumbers.get(1));
                decks.add(deck);
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用");
        }
        return decks;
    }

    /**
     * Before a new study, you have to select a deck.
     *
     * @param ankiCookie
     * @param deckId
     */
    public void selectDeck(String ankiCookie, String deckId) {
        OkHttpClient client = new OkHttpClient();

        Request selectDeckReq = new Request.Builder()
                .url(format("%sselect/%s", LIST_DECK_URL, deckId))
                .post(RequestBody.create(null, new byte[0]))
                .header(":authority:", "ankiweb.net")
                .header(":method:", "POST")
                .header(":path:", format("/decks/select/%s", deckId))
                .header(":scheme:", "https")
                .header("x-requested-with", "XMLHttpRequest")
                .header("cookie", ankiCookie)
                .build();

        try (Response response = client.newCall(selectDeckReq).execute()) {
            if (response.code() != 200) {
                throw new ServerSideException("选择卡组失败, statusCode=" + response.code());
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用");
        }

    }

    public GetCardsResponse getCards(String ankiCookie, BatchAnswer batchAnswer) {
        OkHttpClient client = new OkHttpClient();

        final String answers = batchAnswer.getAnswers().stream().map(answer -> answer.toString()).collect(Collectors.joining(","));
        MultipartBody body = new Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("answers", format("[%s]", answers))
                .addFormDataPart("ts", Long.toString(batchAnswer.getTs()))
                .build();
        Request getCardsReq = new Request.Builder()
                .url(GET_CARDS_URL)
                .post(body)
                .header(":authority:", "ankiuser.net")
                .header(":method:", "POST")
                .header(":path:", "/study/getCards")
                .header(":scheme:", "https")
                .header("x-requested-with", "XMLHttpRequest")
                .header("cookie", ankiCookie)
                .build();

        try (Response response = client.newCall(getCardsReq).execute()) {
            if (response.code() == 200) {
                String responseStr = response.body().string();
                Map<String, Object> map = objectMapper.readValue(responseStr, Map.class);
                if (!map.containsKey("cards") || !(map.get("cards") instanceof List)) {
                    throw new ServerSideException("读取卡牌失败。");
                }
                List<ArrayList> cards = ((List) map.get("cards"));
                List<AnkiCard> ankiCards = cards.stream()
                        .map(card -> {
                            final List<String> repeats = ((List<Object>) card.get(5)).stream()
                                    .map(i -> i.toString())
                                    .collect(Collectors.toList());
                            return new AnkiCard(
                                    (long) card.get(0),
                                    (long) card.get(4),
                                    (String) card.get(1),
                                    (String) card.get(2),
                                    (int) card.get(3),
                                    repeats);
                        })
                        .collect(Collectors.toList());
                List<Integer> counts = ((List<Object>) map.get("counts")).stream().map(i -> Integer.parseInt(i.toString())).collect(Collectors.toList());
                return new GetCardsResponse(ankiCards, counts);
            } else {
                throw new ServerSideException("登录Ankiweb站点失败, statusCode=" + response.code());
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用");
        }

    }

    public String getCookie(String ankiUser, String password) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false).build();

        final String csrfToken = getCsrfToken(client);

        MultipartBody body = new Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", ankiUser)
                .addFormDataPart("password", password)
                .addFormDataPart("csrf_token", csrfToken)
                .addFormDataPart("submitted", "1")
                .build();
        Request loginReq = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .header(":authority:", "ankiweb.net")
                .header(":method:", "POST")
                .header(":path:", "/account/login")
                .header(":scheme:", "https")
                .header("cookie", "ankiweb=login")
                .build();

        try (Response response = client.newCall(loginReq).execute()) {
            if (response.code() == 302) {
                String cookieStr = response.header("set-cookie");
                return cookieStr;
            } else {
                throw new ServerSideException("登录Ankiweb站点失败, statusCode=" + response.code());
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用");
        }
    }

    private String getCsrfToken(OkHttpClient client) {
        Request loginReq = new Request.Builder()
                .url(LOGIN_URL)
                .get()
                .build();
        try (Response response = client.newCall(loginReq).execute()) {
            String loginPageHtml = response.body().string();
            Document doc = Jsoup.parse(loginPageHtml);
            Element csrfTokenEle = doc.selectFirst("form#form > input[name=csrf_token]");
            return csrfTokenEle.attributes().get("value");
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用");
        }
    }

    public static void main(String[] args) {
        AnkiWebClient client = new AnkiWebClient();
//        List<AnkiDeck> decks = client.listDecks("ankiweb=s1RZHDm9h2guAL4F");
//        out.println(decks.size());
//        client.selectDeck("ankiweb=s1RZHDm9h2guAL4F", "did1535418586971");
        client.getCards("ankiweb=s1RZHDm9h2guAL4F", BatchAnswer.empty());
    }
}
