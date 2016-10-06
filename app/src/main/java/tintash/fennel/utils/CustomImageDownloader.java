package tintash.fennel.utils;

import com.nostra13.universalimageloader.core.assist.FlushedInputStream;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

/**
 * Created by Khawar on 6/10/2016.
 */
public class CustomImageDownloader implements ImageDownloader {

    /** {@value} */
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    /** {@value} */
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

    private int connectTimeout;
    private int readTimeout;

    public CustomImageDownloader() {
        this(DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }

    public CustomImageDownloader(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public InputStream getStream(String imageUri, Object extra) throws IOException {

        URI uri = null;
        try {
             uri = new URI(imageUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + PreferenceHelper.getInstance().readToken());
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return new FlushedInputStream(new BufferedInputStream(conn.getInputStream()));
    }
}
