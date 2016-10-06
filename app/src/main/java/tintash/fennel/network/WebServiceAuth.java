package tintash.fennel.network;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import tintash.fennel.datamodels.Auth;

public interface WebServiceAuth {

    @FormUrlEncoded
    @POST("/services/oauth2/token")
    Call<Auth> postSFLogin(
            @Field("grant_type") String grantType,
            @Field("client_id") String cId,
            @Field("client_secret") String cSecret,
            @Field("username") String username,
            @Field("password") String password,
            @Field("redirect_uri") String redirect_uri);
}