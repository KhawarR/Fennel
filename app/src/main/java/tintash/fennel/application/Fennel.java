package tintash.fennel.application;

import android.app.Application;
import android.graphics.Bitmap;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import tintash.fennel.R;
import tintash.fennel.network.RestClient;
import tintash.fennel.network.WebService;

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


    @Override
    public void onCreate() {
        super.onCreate();
        restClient = new RestClient();
        initImageLoader();
    }

    public void initImageLoader() {

        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(android.R.color.darker_gray).showImageOnFail(android.R.color.darker_gray).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisk(true)

                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options).build();

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

