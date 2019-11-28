package leo.me;

import com.google.common.collect.ImmutableSet;

import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {
    public static final String USER_BUCKET_NAME = "moerdo-user";
    public static final String SYSTEM_BUCKET_NAME = "polly-anki-coca";
    public static final ZoneId BASE_ZONE_ID = ZoneId.of("Asia/Shanghai");
    public static final Pattern OPTION_PATTERN = Pattern.compile("[gdwsceip](,[gdwsceip])*");


    public static final String FREE_USER_CLASS = "0";
    public static final String PAID_USER_CLASS = "1";
    public static final String ADMIN_USER_CLASS = "9";
    public static final Set<String> VALID_USER_CLASS = ImmutableSet.of(FREE_USER_CLASS, PAID_USER_CLASS, ADMIN_USER_CLASS);


    public static final int FREE_USER_POINT_LIMIT = 25;
    public static final int FREE_USER_WORDS_LIMIT = 10;
    public static final int FREE_USER_TEXT_LIMIT = 1000;

    public static final int PAID_USER_POINT_LIMIT = 100;
    public static final int PAID_USER_WORDS_LIMIT = 50;
    public static final int PAID_USER_TEXT_LIMIT = 3000;


    public static final String CMD_READ_WORDS = "READ_WORDS";
    public static final String CMD_READ_TEXT = "READ_TEXT";
    public static final String CMD_LIST_HISTORY = "LIST_HISTORY";
    public static final String CMD_CHANGE_USER = "CHANGE_USER";

}
