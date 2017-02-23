package wal.fennel.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.FennelRealmMigrations;
import wal.fennel.network.RestClient;
import wal.fennel.network.WebApi;
import wal.fennel.network.WebService;
import wal.fennel.network.WebServiceAuth;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/27/2016.
 */
public class Fennel extends Application {

    private static Fennel instance;
    public static synchronized Fennel getInstance(){
        return instance;
    }


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

        Realm.init(getApplicationContext());
//        RealmConfiguration config = new RealmConfiguration.Builder()
//                .schemaVersion(1)
//                .build();
//        Realm.setDefaultConfiguration(config);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(6) // Must be bumped when the schema changes
                .migration(new FennelRealmMigrations()) // Migration to run
                .build();
        Realm.setDefaultConfiguration(config);

        WebApi.initializeInstance(getApplicationContext());
        MyPicassoInstance.initializeInstance(getApplicationContext());
        PreferenceHelper.initializeInstance(getApplicationContext());
        DatabaseHelper.initializeInstance(this);
    }

    public static void initWebApi(){
        WebApi.initializeInstance(getInstance().getApplicationContext());
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

