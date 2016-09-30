package tintash.fennel.network;


import org.json.JSONObject;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.datamodels.LoginResponse;

public interface WebService {


//    @GET("/oauth2/token")
//    public void postSFLogin(@Header("Authorization") String token,
//                            Callback<Auth> response);


    @GET("/services/data/{apiVersion}/query")
    public void query(@Header("Authorization") String token, @Query("q") String query, @Path("apiVersion") String apiVersion,
                      Callback<LoginResponse> response);


}