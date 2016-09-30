package tintash.fennel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


@SuppressLint("CommitPrefEdits")
public class PreferenceHelper {

    private static final String PF_TOKEN = "_token";
    private static final String PREF_NAME = "fennel_prefs";

    public static void writeToken(Context context, String token) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PF_TOKEN, token);
        editor.commit();
    }


    public static String readToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String user = preferences.getString(PF_TOKEN, null);
        return user;

    }


}
