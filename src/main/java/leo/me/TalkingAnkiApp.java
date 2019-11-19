package leo.me;

import leo.me.anki.AnkiNoteDao;
import leo.me.anki.AnkiNoteParser;
import leo.me.anki.NoteItem;
import leo.me.polly.Polly;
import leo.me.polly.PollyConfig;
import leo.me.service.TtsService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "talking-dict", mixinStandardHelpOptions = true, version = "talking-dict 1.0",
        description = "Text to speech.")
public class TalkingAnkiApp implements Callable<Integer> {

    @Option(names = {"-w", "--words"}, description = "Word list separated by comma.", required = true)
    private String words;
    @Option(names = {"-s", "--option"}, description = "selective items. \nw -- word\ng -- General chinese\n"
            + "s -- spell\nd -- Detailed Chinese\ni -- Interpretation English\n"
            + "e -- Example English\nc -- Example Chinese\np -- Pause between notes\n"
            + "Default: g,w,s,d,i,c,e,p", defaultValue = "g,w,s,d,i,c,e,p")
    private String optionStr;
    @Option(names = {"-o", "--output"}, description = "File path.", defaultValue = "/tmp/words.mp3")
    private String output;

    @Option(names = {"-n", "--example-limit"}, description = "How many examples, 0 for none, -1 for all.", defaultValue = "2")
    private int exampleLimit = 2;

    public static void main(String[] args) {
        int rtn = new CommandLine(new TalkingAnkiApp()).execute(args);
        System.exit(rtn);
    }

    @Override
    public Integer call() throws Exception {

        List<NoteItem> options = Arrays.stream(optionStr.split(",")).map(s -> NoteItem.fromShort(s)).collect(Collectors.toList());

        final PollyConfig config = new PollyConfig();
        config.setExampleLimit(exampleLimit);

        final Polly polly = new Polly(config);
        final AnkiNoteDao dao = new AnkiNoteDao(":resource:collection.anki2");
        final AnkiNoteParser parser = new AnkiNoteParser(config);
        final TtsService service = new TtsService(config, polly);

        List<String> notes = dao.findNotes(words.split(","));

        try (final FileOutputStream outputStream = new FileOutputStream(output, true)) {
            notes.stream()
                    .map(note -> parser.parse(note))
                    .forEach(ankiNote -> {
                        service.textToSpeech(options, outputStream, ankiNote);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
