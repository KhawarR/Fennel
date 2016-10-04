package tintash.fennel.network;


import com.google.gson.GsonBuilder;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {

    /////////////////// STAGING SERVER ////////////////////////

    private static String BASE_URL_AUTH = "https://test.salesforce.com";
    private static String BASE_URL = "https://test.salesforce.com";

    /////////// PRODUCTION SERVER ////////////////

//    private static String BASE_URL = "";

    private WebService apiService;
    private WebServiceAuth apiServiceAuth;


    public RestClient() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'hh:mm:ss");


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(WebService.class);


        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion <= android.os.Build.VERSION_CODES.KITKAT){
            // Do something for lollipop and above versions

            OkHttpClient.Builder okHttpBuilder = null;
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };
            try {
                okHttpBuilder = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory(), tm);
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL_AUTH)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpBuilder.build())
                        .build();
                apiServiceAuth = retrofit.create(WebServiceAuth.class);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } else{
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_AUTH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiServiceAuth = retrofit.create(WebServiceAuth.class);
        }
    }

    public void setApiBaseUrl(String newApiBaseUrl) {
        BASE_URL = newApiBaseUrl;
//        GsonBuilder builder = new GsonBuilder();
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        apiService = retrofit.create(WebService.class);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion <= android.os.Build.VERSION_CODES.KITKAT){
            // Do something for lollipop and above versions

            OkHttpClient.Builder okHttpBuilder = null;
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };
            try {
                okHttpBuilder = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory(), tm);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpBuilder.build())
                        .build();
                apiService = retrofit.create(WebService.class);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } else{
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(WebService.class);
        }


    }

    public WebService getService() {
        return apiService;
    }

    public WebServiceAuth getAuthService() {
        return apiServiceAuth;
    }

}
