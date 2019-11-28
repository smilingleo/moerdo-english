package leo.me.utils;

import static leo.me.Constants.BASE_ZONE_ID;

import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class DateTimeUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss-SSS");
    private static final Pattern PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}");
    public static String now() {
        LocalDateTime dateTime = LocalDateTime.now(BASE_ZONE_ID);
        return dateTime.format(FORMATTER);
    }

    public static boolean isValidInput(String timestamp) {
        return !Strings.isNullOrEmpty(timestamp) && PATTERN.matcher(timestamp).matches();
    }
}
