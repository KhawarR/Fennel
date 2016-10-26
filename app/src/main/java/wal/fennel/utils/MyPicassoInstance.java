package wal.fennel.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import wal.fennel.BuildConfig;
import wal.fennel.network.Session;

/**
 * Created by Khawar on 18/10/2016.
 */
public class MyPicassoInstance {

    private static Picasso picasso;

    public static synchronized void initializeInstance(Context context) {
        if (picasso == null) {
            picasso = init(context);
        }
    }

    public static synchronized Picasso getInstance() {
        if (picasso == null) {
            throw new IllegalStateException(MyPicassoInstance.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return picasso;
    }

    private static Picasso init(Context context)
    {
        File cache = CacheUtils.createDefaultCacheDir(context);
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(cache, CacheUtils.calculateDiskCacheSize(cache)))
                .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", Session.getAuthToken())
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        return new Picasso.Builder(context)
                .defaultBitmapConfig(Bitmap.Config.RGB_565)
                .indicatorsEnabled(BuildConfig.DEBUG)
                .downloader(new OkHttp3Downloader(client))
                .build();
    }
}
