package tintash.fennel.utils;

/**
 * Created by Faizan on 11/6/2015.
 */
public class Constants {

    // a const string used in api for param = "name"
    public static final String SERVICE_NAME = "WebMemberPortal";

    // FILE NAMES FOR TEMP CARDS
    public static final String FILENAME_FRONT = "IEHP_temporary_ID_card_front.png";
    public static final String FILENAME_BACK = "IEHP_temporary_ID_card_back.png";

    public static final String MEDICARE = "Medicare";
    public static final String MEDICAL = "Medi-Cal";

    // for handling double tap, cut off time
    /**
     * If user taps within the range of below time,
     * that click/tap will be ignored
     */
    public static final long TIME_DIFF = 2000;

    // minutes before expiry time to refresh token as expiry is 20min, we set 2.5 min here so
    // refresh token be called after 17.5min
    public static final float REFRESH_BEFORE_EXIRY = 2.5f * 60;
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000;
    public static final long MIN_TIME_BW_UPDATES = 5000;

    public static final String STR_INCOMPLETE = "INCOMPLETE";
    public static final String STR_PENDING = "PENDING";
    public static final String STR_APPROVED = "APPROVED";

    public static final String STR_ENROLL_FARMER = "ENROLL FARMER";
    public static final String STR_EDIT_FARMER = "EDIT FARMER";
}
