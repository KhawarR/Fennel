package wal.fennel.utils;

/**
 * Created by Faizan on 11/6/2015.
 */
public class Constants {



    // for handling double tap, cut off time
    /**
     * If user taps within the range of below time,
     * that click/tap will be ignored
     */
    public static final long TIME_DIFF = 2000;


    public static final String STR_ENROLLED = "ENROLLED";
    public static final String STR_PENDING = "PENDING";
    public static final String STR_APPROVED = "APPROVED";
    public static final String STR_ENROLL_FARMER = "ENROLL FARMER";
    public static final String STR_EDIT_FARMER = "EDIT FARMER";
    public static final String STR_FACILITATOR = "fac";
    public static final String STR_FIELD_OFFICER = "fo";
    public static final String STR_FIELD_MANAGER = "fm";
    public static final String TOAST_NO_INTERNET = "Network problem, please check your connection & try again";
    public static final String TOAST_LOGIN_ERROR = "Login Error: Check network connection or verify login credentials";

    public static final String STR_NOT_STARTED = "NOT STARTED";
    public static final String STR_IN_PROGRESS = "IN PROGRESS";
    public static final String STR_COMPLETED = "COMPLETED";

    public static final String STR_CHECKBOX = "CHECKBOX";
    public static final String STR_TEXT = "TEXT";
    public static final String STR_OPTIONS = "OPTIONS";
    public static final String STR_GPS = "GPS";
    public static final String STR_FILE = "FILE";

    public static final String STR_FARMER_ID_PREFIX = "off-";

    public static final String MY_SIGNPS_BROADCAST_ACTION = "wal.fennel.action.MY_SIGUPS_UPDATED";

    public static final String STR_TIME_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final int TIMEOUT = 30;
    public static final int IMAGE_MAX_DIM = 500;
    public static final int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_SUCCESS_ADDED = 201;
    public static final int RESPONSE_SUCCESS_NO_CONTENT = 204;
    public static final int CORNER_RADIUS = 20;
    public static final String TEAM_LOGBOOK_TAG = "9999";
    public static final String PERSON_LOGBOOK_TAG = "9998";
    public static final String PERSON_DETAIL_LOGBOOK_TAG = "9997";


    public enum FarmerType {
        MYSIGNUPS,
        MYFARMERTASKS,
    }
}
