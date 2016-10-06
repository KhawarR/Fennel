package tintash.fennel.network;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tintash.fennel.models.Farmer;
//import tintash.fennel.models.FarmerResponse;

public interface WebService {

//    @GET("/services/data/{apiVersion}/query")
//    Call<SFResponse> loginQuery(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @GET("/services/data/{apiVersion}/query")
    Call<ResponseBody> query(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

//    @POST ("/services/data/{apiVersion}/sobjects/Farmer__c/")
//    public Call<FarmerResponse> addFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body Farmer farmer);
}