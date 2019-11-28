package leo.me.anki;

import static leo.me.utils.CharUtils.ssmlEscape;

import com.google.common.primitives.Chars;
import leo.me.utils.CharUtils;
import leo.me.polly.PollyConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AnkiNoteParser {

    private PollyConfig config;

    public AnkiNoteParser(PollyConfig config) {
        this.config = config;
    }

    public AnkiNote parse(String content) {

        AnkiNote note = new AnkiNote();
        final String word = content.substring(0, content.indexOf("<div") - 1);
        note.setWord(word);
        note.setSpell(Chars.join(",", word.toCharArray()));

        Document doc = Jsoup.parse(content);

        AnkiNoteItemGroup group = null;
        for (Element element : doc.select("div")) {

            if (element.is("div[style=color:BlueViolet]")) {
                // general chinese meaning
                String text = element.text();
                note.setChinese(getBreak() + ssmlEscape(replace(text)));
            } else if (element.is("div[style=color:OrangeRed]")) {
                // chinese meaning detail
                if (group != null) {
                    note.getItemGroups().add(group);
                }
                group = new AnkiNoteItemGroup();
                final String text = getBreak() + ssmlEscape(element.text());
                group.setChinese(text);
            } else if (element.is("div[style=color:DeepSkyBlue]")) {
                // english meaning
                final String text = getBreak() + ssmlEscape(element.text());
                group.setExplanation(text);
            } else if (element.is("div[style=color:DarkGreen]")) {
                // examples
                final String text = element.text();

                String englishSample = getBreak() + ssmlEscape(CharUtils.filterOutChinese(text));
                String chineseSample = getBreak() + ssmlEscape(CharUtils.filterOutEnglish(text));

                group.setChineseExample(chineseSample);
                group.setEnglishExample(englishSample);
            }
        }
        if (group != null) {
            note.getItemGroups().add(group);
        }

        return note;
    }

    private String getBreak() {
        return "<break time=\"" + config.getPause() + "s\"/>";
    }

    private static String replace(String text) {
        return text
                .replace("n.", "名词, ")
                .replace("art.", "冠词, ")
                .replace("adj.", "形容词, ")
                .replace("adv.", "副词, ")
                .replace("aux.", "助动词, ")
                .replace("vi.", "不及物动词, ")
                .replace("vt.", "及物动词, ")
                .replace("v.", "动词, ")
                .replace("conj.", "连词, ")
                .replace("prep.", "介词, ")
                .replace("pron.", "代词, ")
                .replace("adj.", "形容词, ");
    }
}
