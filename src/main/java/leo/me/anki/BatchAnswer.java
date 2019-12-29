package leo.me.anki;

import java.util.Collections;
import java.util.List;

public class BatchAnswer {
    private List<NoteAnswer> answers;
    private long ts;

    public BatchAnswer(List<NoteAnswer> answers, long ts) {
        this.answers = answers;
        this.ts = ts;
    }

    public BatchAnswer() {
    }

    public static BatchAnswer empty() {
        return new BatchAnswer(Collections.emptyList(), System.currentTimeMillis());
    }

    public List<NoteAnswer> getAnswers() {
        return answers;
    }

    public long getTs() {
        return ts;
    }

    public void setAnswers(List<NoteAnswer> answers) {
        this.answers = answers;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
