package wal.fennel.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import wal.fennel.application.Fennel;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Khawar on 4/11/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(NetworkHelper.isNetAvailable(context)){

            FennelUtils.uploadFarmerLogFile(context);

            if(!PreferenceHelper.getInstance().readIsSyncInProgress()){
                if(WebApi.isSyncRequired()){
                    String lastSyncTime = PreferenceHelper.getInstance().readNetworkChangeSyncStamp();
                    String currentTime = FennelUtils.getFormattedTime(System.currentTimeMillis(), Constants.STR_TIME_FORMAT_YYYY_MM_DD);

                    if(lastSyncTime.equalsIgnoreCase(currentTime)){
                        Log.i("NetworkChangeReceiver" , "Already synced for today");
                    }
                    else {
                        PreferenceHelper.getInstance().writeNetworkChangeSyncStamp(currentTime);
                        WebApi.syncAll(null);
                    }
                }
                else {
                    WebApi.getFullServerData();
                }
            }
        }
    }
}