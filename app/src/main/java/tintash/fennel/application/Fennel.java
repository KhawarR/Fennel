package tintash.fennel.application;

import android.app.Application;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.fabric.sdk.android.Fabric;
import tintash.fennel.R;
import tintash.fennel.common.database.DatabaseHelper;
import tintash.fennel.network.RestClient;
import tintash.fennel.network.WebService;
import tintash.fennel.network.WebServiceAuth;
import tintash.fennel.utils.CustomImageDownloader;
import tintash.fennel.utils.PreferenceHelper;

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
        initImageLoader();

        PreferenceHelper.initializeInstance(getApplicationContext());
        DatabaseHelper.initializeInstance(this);
    }

    public void initImageLoader() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.dummy_profile)
                .showImageForEmptyUri(R.drawable.dummy_profile)
                .showImageOnFail(R.drawable.dummy_profile)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).imageDownloader(new CustomImageDownloader()).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
    }

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

