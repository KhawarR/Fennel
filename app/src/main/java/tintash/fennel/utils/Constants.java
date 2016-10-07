package tintash.fennel.utils;

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


    public static final String STR_INCOMPLETE = "INCOMPLETE";
    public static final String STR_PENDING = "PENDING";
    public static final String STR_APPROVED = "APPROVED";

    public static final String STR_ENROLL_FARMER = "ENROLL FARMER";
    public static final String STR_EDIT_FARMER = "EDIT FARMER";

    public static final int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_SUCCESS_ADDED = 201;
    public static final int RESPONSE_SUCCESS_NO_CONTENT = 204;
}
