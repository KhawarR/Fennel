package wal.fennel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class PreferenceHelper {

    private static final String PREF_NAME = "fennel_prefs";

    private static final String KEY_TOKEN = "_token";
    private static final String KEY_INSTANCE_URL = "_instance_url";
    private static final String KEY_USER_ID = "_user_id";
    private static final String KEY_PASSWORD = "_password";
    private static final String KEY_LOGIN_USER_ID = "_log_user_id";
    private static final String KEY_FIRST_RUN = "_first_run";
    private static final String KEY_LOGIN_USER_TYPE = "_login_user_type";
    private static final String KEY_USER_EMP_ID = "_user_emp_id";

    private static final String KEY_ABOUT_ME_FN = "_about_fn";
    private static final String KEY_ABOUT_ME_MN = "_about_mn";
    private static final String KEY_ABOUT_ME_LN = "_about_ln";
    private static final String KEY_ABOUT_ME_FO_NAME = "_about_fo_name";
    private static final String KEY_ABOUT_ME_FM_NAME = "_about_fm_name";
    private static final String KEY_ABOUT_ME_ATT_ID = "_about_att_id";


    private static PreferenceHelper sInstance;
    private final SharedPreferences mPref;

    private PreferenceHelper(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceHelper(context);
        }
    }

    public static synchronized PreferenceHelper getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferenceHelper.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public void writeToken(String token) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public String readToken() {
        String user = mPref.getString(KEY_TOKEN, "");
        return user;
    }

    public void writeInstanceUrl(String url) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_INSTANCE_URL, url);
        editor.commit();
    }

    public String readInstanceUrl() {
        String url = mPref.getString(KEY_INSTANCE_URL, "");
        return url;
    }

    public void writeUserId(String id) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_USER_ID, id);
        editor.commit();
    }

    public String readUserId() {
        String userId = mPref.getString(KEY_USER_ID, "");
        return userId;
    }

    public void writePassword(String password) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String readPassword() {
        String password = mPref.getString(KEY_PASSWORD, "");
        return password;
    }

    public void writeLoginUserType(String type) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_LOGIN_USER_TYPE, type);
        editor.commit();
    }

    public String readLoginUserType() {
        String value = mPref.getString(KEY_LOGIN_USER_TYPE, "");
        return value;
    }

    public void writeLoginUserId(String id) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_LOGIN_USER_ID, id);
        editor.commit();
    }

    public String readLoginUserId() {
        String id = mPref.getString(KEY_LOGIN_USER_ID, "");
        return id;
    }

    public void writeAboutFN(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_FN, value);
        editor.commit();
    }

    public String readAboutFN() {
        String value = mPref.getString(KEY_ABOUT_ME_FN, "");
        return value;
    }

    public void writeAboutMN(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_MN, value);
        editor.commit();
    }

    public String readAboutMN() {
        String value = mPref.getString(KEY_ABOUT_ME_MN, "");
        return value;
    }

    public void writeAboutLN(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_LN, value);
        editor.commit();
    }

    public String readAboutLN() {
        String value = mPref.getString(KEY_ABOUT_ME_LN, "");
        return value;
    }

    public void writeAboutFOname(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_FO_NAME, value);
        editor.commit();
    }

    public String readAboutFOname() {
        String value = mPref.getString(KEY_ABOUT_ME_FO_NAME, "");
        return value;
    }

    public void writeAboutFMname(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_FM_NAME, value);
        editor.commit();
    }

    public String readAboutFMname() {
        String value = mPref.getString(KEY_ABOUT_ME_FM_NAME, "");
        return value;
    }

    public void writeAboutAttId(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_ATT_ID, value);
        editor.commit();
    }

    public String readAboutAttId() {
        String value = mPref.getString(KEY_ABOUT_ME_ATT_ID, "");
        return value;
    }

    public void writeFirstRun(boolean fRun) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_FIRST_RUN, fRun);
        editor.commit();
    }

    public boolean readFirstRun() {
        boolean firstRun = mPref.getBoolean(KEY_FIRST_RUN, true);
        return firstRun;
    }

    public void clearSession() {
        writeToken("");
        writeInstanceUrl("");
        writeLoginUserId("");
        writeUserId("");
        writePassword("");
        writeLoginUserType("");

        writeAboutFN("");
        writeAboutMN("");
        writeAboutLN("");
        writeAboutFOname("");
        writeAboutFMname("");
        writeAboutAttId("");
    }

    public void writeUserEmployeeId(String userEmpId) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_USER_EMP_ID, userEmpId);
        editor.commit();
    }

    public String readUserEmployeeId() {
        String value = mPref.getString(KEY_USER_EMP_ID, "");
        return value;
    }
}
