package wal.fennel.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Khawar on 4/11/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(NetworkHelper.isNetAvailable(context) && !PreferenceHelper.getInstance().readIsSyncInProgress() && WebApi.isSyncRequired()){
            WebApi.syncAll(null);
        }
    }
}