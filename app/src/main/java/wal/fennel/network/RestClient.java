package wal.fennel.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FarmerFieldsExclusion;


public class RestClient {

    //TODO Test to Production
/////////////////// STAGING SERVER ////////////////////////
    private static String BASE_URL_AUTH = "https://test.salesforce.com";
    private static String BASE_URL = "https://test.salesforce.com";
/////////////////// PRODUCTION SERVER //////////////////////
//    private static String BASE_URL_AUTH = "https://login.salesforce.com";
//    private static String BASE_URL = "https://login.salesforce.com";

    private WebService apiService;
    private WebServiceAuth apiServiceAuth;

    private Gson gson;

    public RestClient() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        builder.setExclusionStrategies(new FarmerFieldsExclusion());

        gson = builder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiService = retrofit.create(WebService.class);

        setService(BASE_URL_AUTH, true);
    }

    public void setApiBaseUrl(String newApiBaseUrl) {
        BASE_URL = newApiBaseUrl;
        setService(BASE_URL, false);
    }

    private void setService(String baseUrl, boolean isAuth)
    {
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
                okHttpBuilder.connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);
                okHttpBuilder.writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);
                okHttpBuilder.readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(okHttpBuilder.build())
                        .build();
                if(isAuth)
                    apiServiceAuth = retrofit.create(WebServiceAuth.class);
                else
                    apiService = retrofit.create(WebService.class);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } else{
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            if(isAuth)
                apiServiceAuth = retrofit.create(WebServiceAuth.class);
            else
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
