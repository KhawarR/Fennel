package tintash.fennel.network;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tintash.fennel.datamodels.LoginResponse;
import tintash.fennel.models.Farmer;
import tintash.fennel.models.FarmerResponse;

public interface WebService {


//    @GET("/oauth2/token")
//    public void postSFLogin(@Header("Authorization") String token,
//                            Callback<Auth> response);


    @GET("/services/data/{apiVersion}/query")
    public Call<LoginResponse> query(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @POST ("/services/data/{apiVersion}/sobjects/Farmer__c/")
    public Call<FarmerResponse> addFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body Farmer farmer);


}