package tintash.fennel.network;

import tintash.fennel.datamodels.Auth;
import tintash.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/30/2016.
 */
public class Session {
    public static String getAuthToken() {
        return "Bearer " + PreferenceHelper.getInstance().readToken();
    }

    public static void saveAuth(Auth auth) {

        PreferenceHelper.getInstance().writeToken(auth.access_token);
        PreferenceHelper.getInstance().writeInstanceUrl(auth.instance_url);

    }
}
