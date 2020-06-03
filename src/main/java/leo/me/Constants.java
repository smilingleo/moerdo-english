package leo.me;

import com.google.common.collect.ImmutableSet;

import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {
    public static final String USER_BUCKET_NAME = "moerdo-user";
    public static final String SYSTEM_BUCKET_NAME = "polly-anki-coca";
    public static final ZoneId BASE_ZONE_ID = ZoneId.of("Asia/Shanghai");
    public static final Pattern OPTION_PATTERN = Pattern.compile("[gdwsceip][1-5](,[gdwsceip][1-5]?)*");


    public static final String FREE_USER_CLASS = "0";
    public static final String PAID_USER_CLASS = "1";
    public static final String ADMIN_USER_CLASS = "9";
    public static final Set<String> VALID_USER_CLASS = ImmutableSet.of(FREE_USER_CLASS, PAID_USER_CLASS, ADMIN_USER_CLASS);


    public static final int FREE_USER_POINT_LIMIT = 45;
    public static final int FREE_USER_WORDS_LIMIT = 20;
    public static final int FREE_USER_TEXT_LIMIT = 1000;

    public static final int PAID_USER_POINT_LIMIT = 100;
    public static final int PAID_USER_WORDS_LIMIT = 50;
    public static final int PAID_USER_TEXT_LIMIT = 3000;


    public static final String CMD_READ_WORDS = "READ_WORDS";
    public static final String CMD_READ_TEXT = "READ_TEXT";
    public static final String CMD_LIST_HISTORY = "LIST_HISTORY";
    public static final String CMD_CHANGE_USER = "CHANGE_USER";
    public static final String CMD_GET_OPENID = "GET_OPENID";
    public static final String CMD_ANKI_LIST_DECK = "ANKI_LIST_DECK";
    public static final String CMD_ANKI_GET_CARDS = "ANKI_GET_CARDS";
    public static final String CMD_ANKI_SET_DECK = "ANKI_SET_DECK";
    public static final String CMD_GET_IMAGES = "GET_IMAGES";


    public static final String CSE_DEVELOPER_KEY = "AIzaSyB-b1A-n6pexojWLvGKQX00XyzL8GsF7QE";
    public static final String CSE_CX = "000801574035462353467:oipoh4dog84";


    public static final String GET_OPENID_URI = "https://api.weixin.qq.com/sns/jscode2session";

    public static final int LIMIT_HISTORY_BY_MONTH = 6;
}
