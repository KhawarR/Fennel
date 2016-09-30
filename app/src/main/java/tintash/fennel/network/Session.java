package tintash.fennel.network;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import tintash.fennel.datamodels.Auth;
import tintash.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/30/2016.
 */
public class Session {
    public static String getAuthToken(Context context) {
        return "Bearer " + PreferenceHelper.readToken(context);
    }

    public static void saveAuth(Context context, Auth auth) {

        PreferenceHelper.writeToken(context, auth.access_token);

    }
}
