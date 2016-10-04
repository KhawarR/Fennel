package tintash.fennel.network;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.datamodels.LoginResponse;

public interface WebServiceAuth {


//    @GET("/oauth2/token")
//    public void postSFLogin(@Header("Authorization") String token,
//                            Callback<Auth> response);

    @FormUrlEncoded
    @POST("/services/oauth2/token")
    public Call<Auth> postSFLogin(@Field("grant_type") String grantType,
                                  @Field("client_id") String cId,
                                  @Field("client_secret") String cSecret,
                                  @Field("username") String username,
                                  @Field("password") String password,
                                  @Field("redirect_uri") String redirect_uri);


}