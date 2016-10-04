package tintash.fennel.network;


import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.datamodels.LoginResponse;

public interface WebService {


//    @GET("/oauth2/token")
//    public void postSFLogin(@Header("Authorization") String token,
//                            Callback<Auth> response);


    @GET("/services/data/{apiVersion}/query")
    public Call<LoginResponse> query(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);


}