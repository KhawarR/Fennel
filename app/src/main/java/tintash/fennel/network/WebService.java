package tintash.fennel.network;


import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tintash.fennel.models.ResponseModel;

public interface WebService {

//    @GET("/services/data/{apiVersion}/query")
//    Call<SFResponse> loginQuery(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @GET("/services/data/{apiVersion}/query")
    Call<ResponseBody> query(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @POST ("/services/data/{apiVersion}/sobjects/Farmer__c/")
    Call<ResponseModel> addFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farmer);

    @PATCH("/services/data/{apiVersion}/sobjects/Farmer__c/{farmerId}")
    Call<ResponseBody> editFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("farmerId") String farmerId, @Body HashMap<String, Object> farmer);

    @POST ("/services/data/{apiVersion}/sobjects/Farm__c/")
    Call<ResponseModel> addFarm(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farm);

    @PATCH ("/services/data/{apiVersion}/sobjects/Farm__c/{farmId}")
    Call<ResponseBody> editFarm(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("farmId") String farmId,  @Body HashMap<String, Object> farm);
}