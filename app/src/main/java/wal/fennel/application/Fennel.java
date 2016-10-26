package wal.fennel.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;
import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.network.RestClient;
import wal.fennel.network.WebService;
import wal.fennel.network.WebServiceAuth;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/27/2016.
 */
public class Fennel extends Application {
    public static RestClient restClient;
    private Tracker mTracker;

    public static WebService getWebService() {
        if (restClient == null)
            restClient = new RestClient();
        return restClient.getService();
    }

    public static WebServiceAuth getAuthWebService() {
        if (restClient == null)
            restClient = new RestClient();
        return restClient.getAuthService();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        restClient = new RestClient();
//        initImageLoader();

        MyPicassoInstance.initializeInstance(getApplicationContext());
        PreferenceHelper.initializeInstance(getApplicationContext());
        DatabaseHelper.initializeInstance(this);
    }

//    public void initImageLoader() {
//
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.dummy_profile)
//                .showImageForEmptyUri(R.drawable.dummy_profile)
//                .showImageOnFail(R.drawable.dummy_profile)
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .bitmapConfig(Bitmap.Config.RGB_565).build();
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).imageDownloader(new CustomImageDownloader()).defaultDisplayImageOptions(options).build();
//        ImageLoader.getInstance().init(config);
//    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // Tintash Testing
            mTracker = analytics.newTracker("UA-74500732-1");

            mTracker.enableExceptionReporting(true);
//            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


}

