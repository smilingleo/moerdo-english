package leo.me.service;

import static com.google.common.base.Strings.isNullOrEmpty;

import leo.me.anki.AnkiNote;
import leo.me.anki.AnkiNoteItemGroup;
import leo.me.anki.NoteItem;
import leo.me.polly.Polly;
import leo.me.polly.PollyConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

public class TtsService {

    private PollyConfig config;
    private Polly polly;

    public TtsService(PollyConfig config, Polly polly) {
        this.config = config;
        this.polly = polly;
    }

    /**
     * The caller is responsible to close the OutputStream.
     * @param options
     * @param outputStream
     * @param ankiNote
     */
    public void textToSpeech(List<NoteItem> options, OutputStream outputStream, AnkiNote ankiNote) {
        options.forEach(item -> {
            try {
                switch (item) {
                    case WORD:
                        if (!isNullOrEmpty(ankiNote.getWord())) {
                            outputStream.write(polly.encodeEnglish(ankiNote.getEncodedWord(polly.getConfig())));
                        }
                        break;
                    case SPELL:
                        if (!isNullOrEmpty(ankiNote.getSpell())) {
                            outputStream.write(polly.encodeEnglish(ankiNote.getSpell()));
                        }
                        break;
                    case GENERAL_CHINESE:
                        if (!isNullOrEmpty(ankiNote.getChinese())) {
                            outputStream.write(polly.encodeChinese(ankiNote.getChinese()));
                        }
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // for each group, determine the order according to the option.
        Stream<AnkiNoteItemGroup> stream = ankiNote.getItemGroups().stream();
        if (config.getExampleLimit() > 0 && config.getExampleLimit() <= ankiNote.getItemGroups().size()) {
            stream = stream.limit(config.getExampleLimit());
        } else if (config.getExampleLimit() == 0) {
            stream = Stream.empty();
        }

        stream.forEach(group -> {
            options.forEach(item -> {
                try {
                    switch (item) {
                        case DETAILED_CHINESE:
                            if (!isNullOrEmpty(group.getChinese())) {
                                outputStream.write(polly.encodeChinese(group.getChinese()));
                            }
                            break;
                        case INTERPRETATION_ENGLISH:
                            if (!isNullOrEmpty(group.getExplanation())) {
                                outputStream.write(polly.encodeEnglish(group.getExplanation()));
                            }
                            break;
                        case EXAMPLE_CHINESE:
                            if (!isNullOrEmpty(group.getChineseExample())) {
                                outputStream.write(polly.encodeChinese(group.getChineseExample()));
                            }
                            break;
                        case EXAMPLE_ENGLISH:
                            if (!isNullOrEmpty(group.getEnglishExample())) {
                                outputStream.write(polly.encodeEnglish(group.getEnglishExample()));
                            }
                            break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        try {
            if (options.contains(NoteItem.PAUSE_BETWEEN_NOTE)) {
                outputStream.write(polly.encodeEnglish("<break time=\"" + config.getPauseBetweenNotes() + "s\"/>"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
