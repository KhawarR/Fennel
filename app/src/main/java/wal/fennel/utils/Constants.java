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
    public static final String STR_VISIT_LOG = "VISIT LOG";
    public static final String STR_FACILITATOR = "fac";
    public static final String STR_FIELD_OFFICER = "fo";
    public static final String STR_FIELD_MANAGER = "fm";
    public static final String TOAST_NO_INTERNET = "Network problem, please check your connection & try again";
    public static final String TOAST_LOGIN_ERROR = "Login Error: Check network connection or verify login credentials";

    public static final String STR_FARMER_LOG_TYPE_CREATED = "Created";
    public static final String STR_FARMER_LOG_TYPE_EDITED = "Edited";
    public static final String STR_FARMER_LOG_TYPE_SUBMITTED = "Submitted";

    public static final String URL_NOT_SET_ERROR_MESSAGE = "Destination URL not reset. The URL returned from login must be set";

    public static final String STR_NOT_STARTED = "NOT STARTED";
    public static final String STR_IN_PROGRESS = "IN PROGRESS";
    public static final String STR_COMPLETED = "COMPLETED";

    public static final String STR_FARMER_ID_PREFIX = "off-";

    public static final String MY_SIGNPS_BROADCAST_ACTION = "wal.fennel.action.MY_SIGUPS_UPDATED";

    public static final String STR_TIME_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String STR_TIME_FORMAT_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'hh:mm:ss";
    public static final String STR_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd:hh-mm-ss";

    public static final int TIMEOUT = 30;
    public static final int IMAGE_MAX_DIM = 500;
    public static final int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_SUCCESS_ADDED = 201;
    public static final int RESPONSE_SUCCESS_NO_CONTENT = 204;
    public static final int RESPONSE_SESSION_EXPIRE = 401;
    public static final int CORNER_RADIUS = 20;
    public static final String TEAM_LOGBOOK_TAG = "9999";
    public static final String PERSON_LOGBOOK_TAG = "9998";
    public static final String PERSON_DETAIL_LOGBOOK_TAG = "9997";
    public static final String TEAM_DASHBOARD_TAG = "9996";
    public static final String PERSON_DASHBOARD_TAG = "9995";
    public static final String PERSON_DETAIL_DASHBOARD_TAG = "9994";

    public static final String STR_VIEW_MEDIA = "View Media";
    public static final String STR_ATTACH_MEDIA = "Attach Media";
    public static final int FARMING_STATE_ONTIME = 0;
    public static final int FARMING_STATE_LATE = 1;


    public enum FarmerType {
        MYSIGNUPS,
        MYFARMERTASKS,
    }

    public enum TaskItemType {
        Gps,
        File,
        Text,
        Checkbox,
        Options
    }

    public enum FarmingTaskState {
        ONTIME,
        LATE
    }

    public static class DropboxConstants {
        // khawar's account
//        public static final String ACCESS_TOKEN = "fLeIQ1T-kdAAAAAAAAAAC6uQ_f83xp0g0L-GMEFWZmMeXJiya6kGN3atE37KGm7x";
        // Tiffany's account
        public static final String ACCESS_TOKEN = "4945rxRfuokAAAAAAAIwL4p4pys_uFLPsL83s5O3CfdXPAUWfK5tW8uoLv_mU0to";
        public static final String FENNEL_DROPBOX_PATH = "dropbox/Fennel";
        public static final String DEBUG_LOGS_DROPBOX_PATH = "/DebugLogs/";
        public static final String FARMER_LOGS_DROPBOX_PATH = "/FarmerLogs/";
        public static final String FARMER_LOGS_FILE_NAME = "_FarmerLogs.csv";
        public static final String DEBUG_LOGS_FILE_NAME = "_DebugLogs.txt";
    }
}
