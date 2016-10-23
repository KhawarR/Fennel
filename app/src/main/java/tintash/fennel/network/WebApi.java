package tintash.fennel.network;

import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import tintash.fennel.application.Fennel;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.models.ResponseModel;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.PreferenceHelper;

/**
 * Created by Khawar on 21/10/2016.
 */
public class WebApi {

    public static void salesForceAuth(Callback<Auth> authCallback, String username, String password) {
        Call<Auth> call = Fennel.getAuthWebService().postSFLogin(NetworkHelper.GRANT, NetworkHelper.CLIENT_ID, NetworkHelper.CLIENT_SECRET, username, password, NetworkHelper.REDIRECT_URI);
        call.enqueue(authCallback);
    }

    public static void login(Callback<ResponseBody> loginCallback, String username, String password) {
        String loginQuery = String.format(NetworkHelper.QUERY_LOGIN, username, password);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, loginQuery);
        apiCall.enqueue(loginCallback);
    }

    public static void getAboutMeInfo(Callback<ResponseBody> aboutMeCallback) {
        String query = String.format(NetworkHelper.QUERY_ABOUT_ME, PreferenceHelper.getInstance().readUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeCallback);
    }

    public static void getMySignUps(Callback<ResponseBody> mySignUpsCallback)
    {
        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(mySignUpsCallback);
    }

    public static void getMyFarmerAttachments(Callback<ResponseBody> myFarmersAttachments){
        String query = NetworkHelper.QUERY_MY_SIGNUPS_ATTACHMENTS;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(myFarmersAttachments);
    }

    public static void getAboutMeAttachment(Callback<ResponseBody> aboutMeAttachmentCallback){
        String queryTable = "Facilitator__c";
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if(userType.equalsIgnoreCase(Constants.STR_FACILITATOR))
            queryTable = "Facilitator__c";
        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
            queryTable = "Field_Officer__c";
        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
            queryTable = "Field_Manager__c";

        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_ATTACHMENT, queryTable, PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeAttachmentCallback);
    }

    public static void getLocations(Callback<ResponseBody> getLocationsCallback){
        String locationsQuery = NetworkHelper.GET_LOCATIONS;
        Call<ResponseBody> locationsApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, locationsQuery);
        locationsApi.enqueue(getLocationsCallback);
    }

    public static void getSubLocations(Callback<ResponseBody> getSubLocationsCallback){
        String subLocationsQuery = NetworkHelper.GET_SUB_LOCATIONS;
        Call<ResponseBody> subLocationsApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, subLocationsQuery);
        subLocationsApi.enqueue(getSubLocationsCallback);
    }

    public static void getVillages(Callback<ResponseBody> getVillagesCallback){
        String villagesQuery = NetworkHelper.GET_VILLAGES;
        Call<ResponseBody> villagesApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, villagesQuery);
        villagesApi.enqueue(getVillagesCallback);
    }

    public static void getTrees(Callback<ResponseBody> getTreesCallback){
        String treesQuery = NetworkHelper.GET_TREES;
        Call<ResponseBody> treesApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, treesQuery);
        treesApi.enqueue(getTreesCallback);
    }

    public static void addAttachment(Callback<ResponseBody> addAttachmentCallback, RequestBody entityBody, RequestBody imageBody){
        Call<ResponseBody> attachmentApi = Fennel.getWebService().addAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, entityBody, imageBody);
        attachmentApi.enqueue(addAttachmentCallback);
    }

    public static void editAttachment(Callback<ResponseBody> editAttachmentCallback, String attachmentId, RequestBody entityBody, RequestBody imageBody){
        Call<ResponseBody> attachmentApi = Fennel.getWebService().editAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId, entityBody, imageBody);
        attachmentApi.enqueue(editAttachmentCallback);
    }

    public static void createFarmer(Callback<ResponseModel> createFarmerCallback, HashMap<String, Object> farmerMap) {
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmerMap);
        apiCall.enqueue(createFarmerCallback);
    }

    public static void editFarmer(Callback<ResponseBody> editFarmerCallback, String farmerId, HashMap<String, Object> farmerMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmerId, farmerMap);
        apiCall.enqueue(editFarmerCallback);
    }

    public static void createFarm(Callback<ResponseModel> createFarmCallback, HashMap<String, Object> farmMap){
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmMap);
        apiCall.enqueue(createFarmCallback);
    }

    public static void editFarm(Callback<ResponseBody> editFarmCallback, String farmId, HashMap<String, Object> farmMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmId, farmMap);
        apiCall.enqueue(editFarmCallback);
    }

}
