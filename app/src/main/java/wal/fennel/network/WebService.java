package wal.fennel.network;


import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import wal.fennel.models.ResponseModel;

public interface WebService {

//    @GET("/services/data/{apiVersion}/query")
//    Call<SFResponse> loginQuery(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @GET("/services/data/{apiVersion}/query")
    Call<ResponseBody> querySynchronous(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @GET("/services/data/{apiVersion}/query")
    Call<ResponseBody> query(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Query("q") String query);

    @POST ("/services/data/{apiVersion}/sobjects/Farmer__c/")
    Call<ResponseModel> addFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farmer);

    @PATCH("/services/data/{apiVersion}/sobjects/Farmer__c/{farmerId}")
    Call<ResponseBody> editFarmer(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("farmerId") String farmerId, @Body HashMap<String, Object> farmer);

    @POST ("/services/data/{apiVersion}/sobjects/Shamba__c/")
    Call<ResponseModel> addFarm(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farm);

    @PATCH ("/services/data/{apiVersion}/sobjects/Shamba__c/{farmId}")
    Call<ResponseBody> editFarm(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("farmId") String farmId,  @Body HashMap<String, Object> farm);

    @Multipart
    @POST ("/services/data/{apiVersion}/sobjects/Attachment/")
    Call<ResponseBody> addAttachment(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Part("entity_document") RequestBody postBody, @Part("Body\"; filename=\"image.png\" ") RequestBody imageBody);

    @Multipart
    @PATCH ("/services/data/{apiVersion}/sobjects/Attachment/{attachmentId}")
    Call<ResponseBody> editAttachment(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Path("attachmentId") String attachmentId, @Part("entity_document") RequestBody entityBody, @Part("Body\"; filename=\"image.png\" ") RequestBody imageBody);

    @GET("/services/data/{apiVersion}/sobjects/Attachment/{attachmentId}/body")
    Call<ResponseBody> downloadAttachmentForTask(@Header("Authorization") String token, @Path("apiVersion") String apiVersion, @Path("attachmentId") String attachmentId);

    @POST ("/services/data/{apiVersion}/sobjects/Farm_Visit__c/")
    Call<ResponseModel> addFarmVisit(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farmVisitMap);

    @POST ("/services/data/{apiVersion}/sobjects/Visit_Log__c/")
    Call<ResponseModel> addFarmVisitLog(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Body HashMap<String, Object> farmVisitLogMap);

    @PATCH("/services/data/{apiVersion}/sobjects/Farming_Task__c/{farmingTaskId}")
    Call<ResponseBody> editFarmingTask(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("farmingTaskId") String farmerId, @Body HashMap<String, Object> farmingTask);

    @PATCH("/services/data/{apiVersion}/sobjects/Task_Item__c/{taskItemId}")
    Call<ResponseBody> editTaskItem(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("taskItemId") String farmerId, @Body HashMap<String, Object> taskItem);

    @PATCH("/services/data/{apiVersion}/sobjects/Task_Item_Option__c/{taskItemOptionId}")
    Call<ResponseBody> editTaskItemOption(@Header("Authorization") String token, @Header("Content-Type") String contentType, @Path("apiVersion") String apiVersion, @Path("taskItemOptionId") String farmerId, @Body HashMap<String, Object> taskItemOption);
}