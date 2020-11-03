package leo.me.utils;

public class CharUtils {

    /**
     * 判定输入的是否是汉字
     *
     * @param c 被校验的字符
     * @return true代表是汉字
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * the example sentence is: `例：english sentence. 中文例句。`
     *
     * @param str
     * @return
     */
    public static String filterOutChinese(String str) {
        String result = str.substring(2);
        StringBuilder sb = new StringBuilder();
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isChinese(chars[i])) {
                sb.append(chars[i]);
            } else {
                return sb.toString();
            }
        }
        return sb.toString();
    }

    public static String filterOutEnglish(String str) {
        String left = str.substring(2);
        StringBuilder sb = new StringBuilder(str.substring(0, 2));
        char[] chars = left.toCharArray();
        boolean metChinese = false;
        for (int i = 0; i < chars.length; i++) {
            if (isChinese(chars[i])) {
                metChinese = true;
            }
            if (metChinese) {
                sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    public static String ssmlEscape(String string) {
        StringBuilder sb = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c == '"') {
                sb.append("&quot;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String encodeBucketName(String word) {
        return word.replaceAll("\\s", "_").toLowerCase();
    }
}
