package tintash.fennel.network;

import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


public class RestClient {

    /////////////////// STAGING SERVER ////////////////////////

//    private static String BASE_URL_AUTH = "https://svc-dev.iehp.org/IEHPWebApiTokenServiceUAT";
//    private static String BASE_URL = "https://svc-dev.iehp.org/IEHPWebApiUAT";

    /////////// PRODUCTION SERVER ////////////////

    private static String BASE_URL_AUTH = "https://ewebauth.iehp.org/WebApi";
    private static String BASE_URL = "https://ewebserv.iehp.org/WebApiServices";

    private final WebService apiService;


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

    }

    public WebService getService() {
        return apiService;
    }

}
