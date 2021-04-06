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
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnkiWebClient {

    private static final String LOGIN_URL = "https://ankiweb.net/account/login";
    private static final String USER_AUTH_URL = "https://ankiweb.net/account/userAuth?rt=/study/";
    private static final String LIST_DECK_URL = "https://ankiweb.net/decks/";

    private static final String GET_CARDS_URL = "https://ankiuser.net/study/getCards";
    public static final String GET_MEDIA_URL = "https://ankiuser.net/study/media/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .followRedirects(false)
            .followSslRedirects(false).build();
    /**
     * List all the decks of current user.
     *
     * @param ankiCookie
     * @return
     */
    public List<AnkiDeck> listDecks(String ankiCookie) {
        Request listReq = new Request.Builder()
                .url(LIST_DECK_URL)
                .get()
                .header("Cookie", ankiCookie)
                .build();

        List<AnkiDeck> decks = new LinkedList<>();

        try (Response response = okHttpClient.newCall(listReq).execute()) {
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
                .header("x-requested-with", "XMLHttpRequest")
                .header("cookie", ankiCookie)
                .build();

        try (Response response = client.newCall(selectDeckReq).execute()) {
            if (response.code() != 200) {
                throw new ServerSideException("选择卡组失败, statusCode=" + response.code());
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用", e);
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
            throw new ServerSideException(format("从Ankiweb站点读取卡牌失败，请查看ankiweb.net是否可用.  Cookie: %s", ankiCookie), e);
        }

    }

    public AnkiCookies getCookie(String ankiUser, String password) {

        final String csrfToken = getCsrfToken();

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
                .header("Cookie", "ankiweb=login")
                .build();
        try (Response response = okHttpClient.newCall(loginReq).execute()) {
            if (response.code() == 302) {
                String ankiwebCookie = response.header("set-cookie");

                Request authReq = new Request.Builder()
                        .url(USER_AUTH_URL)
                        .header("Cookie", ankiwebCookie)
                        .get()
                        .build();

                try (Response authResp = okHttpClient.newCall(authReq).execute()) {
                    if (authResp.code() == 302) {
                        String ankiUserAuthUrl = authResp.header("location");

                        Request userAuthReq = new Request.Builder()
                                .url(ankiUserAuthUrl)
                                .header("Cookie", "ankiweb=login")
                                .get()
                                .build();
                        try (Response userAuthResp = okHttpClient.newCall(userAuthReq).execute()) {
                            if (userAuthResp.code() == 302) {
                                String ankiuserCookie = userAuthResp.header("set-cookie");
                                return new AnkiCookies(ankiwebCookie, ankiuserCookie);
                            } else {
                                throw new ServerSideException("ankiuser身份验证失败");
                            }
                        }

                    } else {
                        throw new ServerSideException("ankiweb身份验证失败");
                    }
                }
            } else {
                throw new ServerSideException("登录Ankiweb站点失败, statusCode=" + response.code());
            }
        } catch (IOException e) {
            throw new ServerSideException("访问Ankiweb站点失败，请查看ankiweb.net是否可用", e);
        }
    }

    private String getCsrfToken() {
        Request loginReq = new Request.Builder()
                .url(LOGIN_URL)
                .get()
                .build();
        try (Response response = okHttpClient.newCall(loginReq).execute()) {
            String loginPageHtml = response.body().string();
            Document doc = Jsoup.parse(loginPageHtml);
            Element csrfTokenEle = doc.selectFirst("form#form > input[name=csrf_token]");
            return csrfTokenEle.attributes().get("value");
        } catch (IOException e) {
            throw new ServerSideException("获取Ankiweb站点Csrf token失败，请查看ankiweb.net是否可用");
        }
    }

    public static void main(String[] args) {
        AnkiWebClient client = new AnkiWebClient();
//        String userCookie = "ankiweb=eyJrIjogInMxUlpIRG05aDJndUFMNEYiLCAiYyI6IDJ9.iZyn1EtpFMHM2X7NIZLUJHi9fgpwQgCBhbJmaAIfYPg";
        AnkiCookies cookie = client.getCookie("leo.trash.reg@gmail.com", "");
        client.selectDeck(cookie.getAnkiWebCookie(), "did1535418586971");
        GetCardsResponse cards = client.getCards(cookie.getAnkiUserCookie(), BatchAnswer.empty());
        System.out.println(cards.getCards().size());
    }
}
