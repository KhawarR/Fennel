package tintash.fennel.network;

import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


public class RestClient {

    /////////////////// STAGING SERVER ////////////////////////

    private static String BASE_URL_AUTH = "https://test.salesforce.com";
    private static String BASE_URL = "https://test.salesforce.com";

    /////////// PRODUCTION SERVER ////////////////

//    private static String BASE_URL = "";

    private WebService apiService;
    private final WebServiceAuth apiServiceAuth;


    public RestClient() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'hh:mm:ss");


        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BASE_URL)

                .setConverter(new GsonConverter(builder.create()))
                .build();
        apiService = restAdapter.create(WebService.class);

        restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BASE_URL_AUTH)

                .setConverter(new GsonConverter(builder.create()))
                .build();
        apiServiceAuth = restAdapter.create(WebServiceAuth.class);


    }

    public void setApiBaseUrl(String newApiBaseUrl) {
        BASE_URL = newApiBaseUrl;
        GsonBuilder builder = new GsonBuilder();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new GsonConverter(builder.create())).setEndpoint(BASE_URL)
                .build();
        apiService = restAdapter.create(WebService.class);
    }

    public WebService getService() {
        return apiService;
    }

    public WebServiceAuth getAuthService() {
        return apiServiceAuth;
    }

}
