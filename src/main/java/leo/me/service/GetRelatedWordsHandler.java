package leo.me.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.nlp.FrequencyFileLoader;
import com.kennycason.kumo.palette.ColorPalette;
import leo.me.exception.ClientSideException;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Give a word, return its related words.
 */
public class GetRelatedWordsHandler implements Handler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Color[] colors = new Color[] {
            new Color(0xff0000),
            new Color(0xfbb034),
            new Color(0xffdd00),
            new Color(0xc1d82f),
            new Color(0x00a4e4),
            new Color(0x8a7967),
            new Color(0x6a737b)
    };
    /**
     * Only `words` is required.
     *
     * @param request
     * @return
     */
    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        if (request.getWords() == null || request.getWords().size() != 1) {
            throw new ClientSideException("查询相关词汇只能传入一个单词。");
        }
        String word = request.getWords().get(0);
        OkHttpClient client = new OkHttpClient();
        Request req = new Builder()
                .url("https://relatedwords.org/api/related?term=" + word)
                .get()
                .build();

        MoerdoResponse res = new MoerdoResponse();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (Response response = client.newCall(req).execute()) {
            if (response.code() == 200) {
                String contentStr = response.body().string();
                List<Map<String, Object>> records = objectMapper.readValue(contentStr, new TypeReference<List<Map<String, Object>>>() {});
                String frequency = records.stream()
                        .limit(30)
                        .map(record -> {
                            String relatedWord = (String) record.get("word");
                            Double score = Double.valueOf(record.get("score").toString());
                            return String.format("%d:%s", Math.round(score * 100), relatedWord);
                        })
                        .collect(Collectors.joining("\n"));

                FrequencyFileLoader loader = new FrequencyFileLoader();
                List<WordFrequency> wordFrequencies = loader.load(IOUtils.toInputStream(frequency));
                Dimension dimension = new Dimension(400, 400);
                WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
                wordCloud.setPadding(0);
                wordCloud.setAngleGenerator(new AngleGenerator(0));
                wordCloud.setBackgroundColor(Color.WHITE);
                wordCloud.setBackground(new RectangleBackground(dimension));
                wordCloud.setColorPalette(new ColorPalette(colors));
                wordCloud.setFontScalar(new LinearFontScalar(15, 40));
                wordCloud.build(wordFrequencies);

                wordCloud.writeToStreamAsPNG(os);
                byte[] bytes = os.toByteArray();
                String encodedStr = Base64.getEncoder().encodeToString(bytes);
                String imgContent = String.format("data:image/png;base64, %s", encodedStr);

                res.setImageContent(imgContent);
            } else {
                throw new ServerSideException("查询关联词汇失败，服务器代码：" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerSideException("查询关联词汇失败。");
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void main(String[] args) {
        GetRelatedWordsHandler handler = new GetRelatedWordsHandler();
        MoerdoRequest req = new MoerdoRequest();
        req.setWords(Arrays.asList("fume"));
        MoerdoResponse resp = handler.handle(req);
        System.out.println(resp.getImageContent());
    }
}
