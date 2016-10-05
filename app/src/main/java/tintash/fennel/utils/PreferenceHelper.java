package tintash.fennel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class PreferenceHelper {

    private static final String PREF_NAME = "fennel_prefs";

    private static final String KEY_TOKEN = "_token";
    private static final String KEY_USER_ID = "_user_id";
    private static final String KEY_PASSWORD = "_password";
    private static final String KEY_FACILITATOR_ID = "_fac_id";

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
        String user = mPref.getString(KEY_TOKEN, null);
        return user;
    }

    public void writeUserId(String id) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_USER_ID, id);
        editor.commit();
    }

    public String readUserId() {
        String userId = mPref.getString(KEY_USER_ID, null);
        return userId;
    }

    public void writePassword(String password) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String readPassword() {
        String password = mPref.getString(KEY_PASSWORD, null);
        return password;
    }

    public void writeFacilitatorId(String id) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_FACILITATOR_ID, id);
        editor.commit();
    }

    public String readFacilitatorId() {
        String id = mPref.getString(KEY_FACILITATOR_ID, null);
        return id;
    }
}
