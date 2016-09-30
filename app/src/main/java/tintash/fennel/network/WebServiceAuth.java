package tintash.fennel.network;


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

public interface WebServiceAuth {


//    @GET("/oauth2/token")
//    public void postSFLogin(@Header("Authorization") String token,
//                            Callback<Auth> response);

    @FormUrlEncoded
    @POST("/services/oauth2/token")
    public void postSFLogin(@Field("grant_type") String grantType,
                            @Field("client_id") String cId,
                            @Field("client_secret") String cSecret,
                            @Field("username") String username,
                            @Field("password") String password,
                            @Field("redirect_uri") String redirect_uri,
                            Callback<Auth> response);





}