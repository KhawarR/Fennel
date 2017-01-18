package wal.fennel.dropbox;

/**
 * Created by Khawar on 22/12/2016.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;

/**
 * Created by Khawar on 22/12/2016.
 */

public class UploadTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private File file;
    private Context context;
    private String dropboxPath;

    public UploadTask(DbxClientV2 dbxClient, File file, Context context, String dropboxPath) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.context = context;
        this.dropboxPath = dropboxPath;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Upload to Dropbox
            InputStream inputStream = new FileInputStream(file);
            dbxClient.files().uploadBuilder(dropboxPath + file.getName()) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream);
            Log.d("Upload Status", "Success");

            MixpanelAPI mixPanel = MixpanelAPI.getInstance(context, MixPanelConstants.MIXPANEL_TOKEN);

            if(mixPanel != null) {
                if(dropboxPath.equalsIgnoreCase(Constants.DropboxConstants.FARMER_LOGS_DROPBOX_PATH)) {
                    mixPanel.track(MixPanelConstants.Event.FARMER_LOG_UPLOADED);
                } else {
                    mixPanel.track(MixPanelConstants.Event.DEBUG_LOG_UPLOADED);
                }
            }
        } catch (DbxException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}

