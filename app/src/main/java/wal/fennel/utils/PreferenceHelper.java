package wal.fennel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import io.realm.Realm;

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
    private static final String KEY_LAST_SYNC_TIME = "_last_sync_time";
    private static final String KEY_SESSION_EXPIRE_SYNC_REQ = "_session_expired_sync_req";
    private static final String KEY_NETWORK_CHANGE_SYNC_STAMP = "_key_network_change_sync_stamp";

    private static final String KEY_EMPLOYEE_FULLNAME = "_emp_fullname";
    private static final String KEY_ABOUT_ME_FN = "_about_fn";
    private static final String KEY_ABOUT_ME_MN = "_about_mn";
    private static final String KEY_ABOUT_ME_LN = "_about_ln";
    private static final String KEY_ABOUT_ME_FO_NAME = "_about_fo_name";
    private static final String KEY_ABOUT_ME_FM_NAME = "_about_fm_name";
    private static final String KEY_ABOUT_ME_ATT_ID = "_about_att_id";
    private static final String KEY_ABOUT_ME_ATT_URL = "_about_att_url";
    private static final String KEY_ABOUT_IS_SYNC_REQ = "_about_is_sync_req";

    private static final String KEY_IS_SYNC_IN_PROGRESS = "_is_sync_in_progress";


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

    public void writeAboutAttUrl(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_ABOUT_ME_ATT_URL, value);
        editor.commit();
    }

    public String readAboutAttUrl() {
        String value = mPref.getString(KEY_ABOUT_ME_ATT_URL, "");
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

    public void writeIsSyncInProgress(boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_IS_SYNC_IN_PROGRESS, value);
        editor.commit();
    }

    public boolean readIsSyncInProgress() {
        boolean value = mPref.getBoolean(KEY_IS_SYNC_IN_PROGRESS, false);
        return value;
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

    public void writeLastSyncTime(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_LAST_SYNC_TIME, value);
        editor.commit();
    }

    public String readLastSyncTime() {
        String value = mPref.getString(KEY_LAST_SYNC_TIME, "-");
        return value;
    }

    public void writeEmployeeFullname(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_EMPLOYEE_FULLNAME, value);
        editor.commit();
    }

    public String readEmployeeFullname() {
        String value = mPref.getString(KEY_EMPLOYEE_FULLNAME, "-");
        return value;
    }

    public void writeAboutIsSyncReq(boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_ABOUT_IS_SYNC_REQ, value);
        editor.commit();
    }

    public boolean readAboutIsSyncReq() {
        boolean value = mPref.getBoolean(KEY_ABOUT_IS_SYNC_REQ, false);
        return value && !(PreferenceHelper.getInstance().readAboutAttUrl().startsWith("http"));
    }

    public void writeSessionExpiredSyncReq(boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(KEY_SESSION_EXPIRE_SYNC_REQ, value);
        editor.commit();
    }

    public boolean isSessionExpiredSyncReq() {
        boolean value = mPref.getBoolean(KEY_SESSION_EXPIRE_SYNC_REQ, false);
        return value;
    }

    public void writeNetworkChangeSyncStamp(String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_NETWORK_CHANGE_SYNC_STAMP, value);
        editor.commit();
    }

    public String readNetworkChangeSyncStamp() {
        String value = mPref.getString(KEY_NETWORK_CHANGE_SYNC_STAMP, "-");
        return value;
    }

    public void clearSession(boolean clearAll) {
        writeToken("");
        writeSessionExpiredSyncReq(true);
        if(clearAll){
            writeInstanceUrl("");
            writeLoginUserId("");
            writeUserId("");
            writePassword("");
            writeLoginUserType("");
            writeUserEmployeeId("");
            writeLastSyncTime("");
            writeIsSyncInProgress(false);
            writeSessionExpiredSyncReq(false);
            writeNetworkChangeSyncStamp("");

            writeAboutFN("");
            writeAboutMN("");
            writeAboutLN("");
            writeAboutFOname("");
            writeAboutFMname("");
            writeAboutAttId("");
            writeAboutAttUrl("");
            writeAboutIsSyncReq(false);

            Singleton.getInstance().clearAll();
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
//            realm.delete(Farmer.class);
            realm.deleteAll();
            realm.commitTransaction();
        }
    }
}
