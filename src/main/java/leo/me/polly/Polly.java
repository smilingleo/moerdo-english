package leo.me.polly;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.Voice;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class Polly {

    private final Voice chineseVoice;
    private final Voice englishVoice;
    private final AmazonPollyClient polly;
    private final PollyConfig config;
    public Polly(PollyConfig config) {
        this.config = config;
        // create an Amazon Polly client in a specific region
        polly = new AmazonPollyClient(new DefaultAWSCredentialsProviderChain(),
                new ClientConfiguration());
        polly.setRegion(config.getRegion());
        // Create describe voices request.
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

        // Synchronously ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);

        englishVoice = describeVoicesResult.getVoices().stream()
                .filter(voice -> "US English".equals(voice.getLanguageName()) && config.getEnglishVoiceId().equals(voice.getId()))
                .findFirst().get();
        chineseVoice = describeVoicesResult.getVoices().stream()
                .filter(voice -> "Chinese Mandarin".equals(voice.getLanguageName()) && config.getChineseVoiceId().equals(voice.getId()))
                .findFirst().get();
    }

    public byte[] encodeChinese(String text) {
        return encode(text, chineseVoice);
    }

    public byte[] encodeEnglish(String text) {
        return encode(text, englishVoice);
    }

    private byte[] encode(String text, Voice voice) {
        SynthesizeSpeechResult synthRes = getSynthesizeSpeechResult(text, voice);

        try (InputStream audioStream = synthRes.getAudioStream()) {
            return ByteStreams.toByteArray(audioStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private SynthesizeSpeechResult getSynthesizeSpeechResult(String text, Voice voice) {
        System.out.println(text);
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText("<speak>" + text + "</speak>")
                .withTextType(TextType.Ssml)
                .withVoiceId(voice.getId())
                .withOutputFormat(OutputFormat.Mp3);
        return polly.synthesizeSpeech(synthReq);
    }

    public PollyConfig getConfig() {
        return config;
    }
}
