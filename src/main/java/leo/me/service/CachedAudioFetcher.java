package leo.me.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static leo.me.Constants.SYSTEM_BUCKET_NAME;
import static leo.me.Constants.USER_BUCKET_NAME;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import leo.me.anki.AnkiNote;
import leo.me.anki.AnkiNoteItemGroup;
import leo.me.anki.NoteItem;
import leo.me.exception.ServerSideException;
import leo.me.polly.Polly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.common.value.qual.IntRange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CachedAudioFetcher {

    private static Log log = LogFactory.getLog(CachedAudioFetcher.class);
    private String wechatId;
    private AmazonS3 s3Client;
    private Polly polly;
    private String timeStamp;
    private int exampleLimit;

    public CachedAudioFetcher(String wechatId, AmazonS3 s3Client, Polly polly, String timeStamp, int exampleLimit) {
        this.wechatId = wechatId;
        this.s3Client = s3Client;
        this.polly = polly;
        this.timeStamp = timeStamp;
        this.exampleLimit = exampleLimit;
    }

    public String audioData(List<AnkiNote> ankiNotes, List<NoteItem> options) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (AnkiNote ankiNote : ankiNotes) {
                for (NoteItem item : options) {
                    if (NoteItem.WORD.equals(item)
                            || NoteItem.SPELL.equals(item)
                            || NoteItem.GENERAL_CHINESE.equals(item)) {
                        for (int i=0; i<item.getRepeat(); i++) {
                            outputStream.write(loadOrRead(ankiNote, item, null));
                        }
                    }
                }
                Stream<AnkiNoteItemGroup> stream = ankiNote.getItemGroups().stream();
                if (exampleLimit > 0 && exampleLimit <= ankiNote.getItemGroups().size()) {
                    stream = stream.limit(exampleLimit);
                } else if (exampleLimit == 0) {
                    stream = Stream.empty();
                }

                List<AnkiNoteItemGroup> groups = stream.collect(Collectors.toList());
                for (int i = 1; i <= groups.size(); i++) {
                    for (NoteItem item : options) {
                        if (NoteItem.DETAILED_CHINESE.equals(item)
                                || NoteItem.INTERPRETATION_ENGLISH.equals(item)
                                || NoteItem.EXAMPLE_CHINESE.equals(item)
                                || NoteItem.EXAMPLE_ENGLISH.equals(item)) {
                            for (int j=0; j<item.getRepeat(); j++) {
                                outputStream.write(loadOrRead(ankiNote, item, Integer.valueOf(i)));
                            }
                        }
                    }
                }
            }

            byte[] audioBytes = outputStream.toByteArray();
            try (ByteArrayInputStream baIn = new ByteArrayInputStream(audioBytes)) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("audio/mpeg3");
                metadata.addUserMetadata("x-amz-meta-title", format("%s.mp3", timeStamp));
                metadata.setContentLength(audioBytes.length);

                final String fileName = format("%s/audio/%s.mp3", wechatId, timeStamp);
                log.info(format("writing file: %s, size: %d", fileName, audioBytes.length));
                PutObjectRequest putRequest = new PutObjectRequest(USER_BUCKET_NAME, fileName, baIn, metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead);
                s3Client.putObject(putRequest);
                URL uri = s3Client.getUrl(USER_BUCKET_NAME, fileName);
                return uri.toString();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] loadOrRead(AnkiNote ankiNote, NoteItem item, Integer idx) {
        String name = idx == null ? format("%s/%s.mp3", ankiNote.getWord(), item.getShortName())
                : format("%s/%d-%s.mp3", ankiNote.getWord(), idx.intValue(), item.getShortName());

        S3Object object = null;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/mpeg3");
        metadata.addUserMetadata("x-amz-meta-title", name);

        boolean existed = s3Client.doesObjectExist(SYSTEM_BUCKET_NAME, name);

        byte[] bytes = null;
        if (!existed) {
            if (idx != null) {
                AnkiNoteItemGroup group = ankiNote.getItemGroups().get(idx.intValue() - 1);

                switch (item) {
                    case DETAILED_CHINESE:
                        if (!isNullOrEmpty(group.getChinese())) {
                            bytes = polly.encodeChinese(group.getChinese());
                        }
                        break;
                    case INTERPRETATION_ENGLISH:
                        if (!isNullOrEmpty(group.getExplanation())) {
                            bytes = polly.encodeEnglish(group.getExplanation());
                        }
                        break;
                    case EXAMPLE_CHINESE:
                        if (!isNullOrEmpty(group.getChineseExample())) {
                            bytes = polly.encodeChinese(group.getChineseExample());
                        }
                        break;
                    case EXAMPLE_ENGLISH:
                        if (!isNullOrEmpty(group.getEnglishExample())) {
                            bytes = polly.encodeEnglish(group.getEnglishExample());
                        }
                        break;
                }
            } else {
                switch (item) {
                    case WORD:
                        if (!isNullOrEmpty(ankiNote.getWord())) {
                            bytes = polly.encodeEnglish(ankiNote.getEncodedWord(polly.getConfig()));
                        }
                        break;
                    case SPELL:
                        if (!isNullOrEmpty(ankiNote.getSpell())) {
                            bytes = polly.encodeEnglish(ankiNote.getSpell());
                        }
                        break;
                    case GENERAL_CHINESE:
                        if (!isNullOrEmpty(ankiNote.getChinese())) {
                            bytes = polly.encodeChinese(ankiNote.getChinese());
                        }
                        break;
                }
            }

        }

        if (bytes != null) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                metadata.setContentLength(bytes.length);
                log.info(format("writing file: %s, size: %d", name, bytes.length));

                s3Client.putObject(SYSTEM_BUCKET_NAME, name, inputStream, metadata);
            } catch (IOException e) {
                throw new ServerSideException("存储音频文件时发生错误，请联系作者。", e);
            }
            return bytes;
        }

        if (bytes == null && !existed) {
            log.info(format("The file: %s doesn't exist and does not generate either", name));
            return new byte[0];
        }

        // file must exist here.
        try {
            object = s3Client.getObject(SYSTEM_BUCKET_NAME, name);
            bytes = IOUtils.toByteArray(object.getObjectContent());
            log.info(format("read from %s, the byte length: %d", name, bytes.length));
            return bytes;
        } catch (IOException e) {
            throw new ServerSideException("读取文件:" + name + "内容时发生错误。");
        } finally {
            IOUtils.closeQuietly(object, log);
        }
    }
}
