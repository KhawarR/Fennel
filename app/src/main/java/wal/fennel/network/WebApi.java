package wal.fennel.network;

import android.content.Context;

import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import wal.fennel.application.Fennel;
import wal.fennel.datamodels.Auth;
import wal.fennel.models.ResponseModel;
import wal.fennel.utils.Constants;
import wal.fennel.utils.PreferenceHelper;

/**
 * Created by Khawar on 21/10/2016.
 */
public class WebApi {

    private static Context mContext = null;
    private static WebApi sInstance = null;
    private WebApi(Context context){
        mContext = context;
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WebApi(context);
        }
    }

    public static synchronized WebApi getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(WebApi.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public static boolean salesForceAuth(Callback<Auth> callback, String username, String password) {
        Call<Auth> apiCall = Fennel.getAuthWebService().postSFLogin(NetworkHelper.GRANT, NetworkHelper.CLIENT_ID, NetworkHelper.CLIENT_SECRET, username, password, NetworkHelper.REDIRECT_URI);
        return processCall(apiCall, callback);
    }

    public static boolean login(Callback<ResponseBody> callback, String username, String password) {
        String loginQuery = String.format(NetworkHelper.QUERY_LOGIN, username, password);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, loginQuery);
        return processCall(apiCall, callback);
    }

    public static boolean getAboutMeInfo(Callback<ResponseBody> callback) {
        String query = String.format(NetworkHelper.QUERY_ABOUT_ME, PreferenceHelper.getInstance().readUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMySignUps(Callback<ResponseBody> callback){
        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyFarmerAttachments(Callback<ResponseBody> callback){
        String query = NetworkHelper.QUERY_MY_SIGNUPS_ATTACHMENTS;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getAboutMeAttachment(Callback<ResponseBody> callback){
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
        return processCall(apiCall, callback);
    }

    public static boolean getLocations(Callback<ResponseBody> callback){
        String locationsQuery = NetworkHelper.GET_LOCATIONS;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, locationsQuery);
        return processCall(apiCall, callback);
    }

    public static boolean getSubLocations(Callback<ResponseBody> callback){
        String subLocationsQuery = NetworkHelper.GET_SUB_LOCATIONS;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, subLocationsQuery);
        return processCall(apiCall, callback);
    }

    public static boolean getVillages(Callback<ResponseBody> callback){
        String villagesQuery = NetworkHelper.GET_VILLAGES;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, villagesQuery);
        return processCall(apiCall, callback);
    }

    public static boolean getTrees(Callback<ResponseBody> callback){
        String treesQuery = NetworkHelper.GET_TREES;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, treesQuery);
        return processCall(apiCall, callback);
    }

    public static boolean addAttachment(Callback<ResponseBody> callback, RequestBody entityBody, RequestBody imageBody){
        Call<ResponseBody> apiCall = Fennel.getWebService().addAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, entityBody, imageBody);
        return processCall(apiCall, callback);
    }

    public static boolean editAttachment(Callback<ResponseBody> callback, String attachmentId, RequestBody entityBody, RequestBody imageBody){
        Call<ResponseBody> apiCall = Fennel.getWebService().editAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId, entityBody, imageBody);
        return processCall(apiCall, callback);
    }

    public static boolean createFarmer(Callback<ResponseModel> callback, HashMap<String, Object> farmerMap) {
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmerMap);
        return processCall(apiCall, callback);
    }

    public static boolean editFarmer(Callback<ResponseBody> callback, String farmerId, HashMap<String, Object> farmerMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmerId, farmerMap);
        return processCall(apiCall, callback);
    }

    public static boolean createFarm(Callback<ResponseModel> callback, HashMap<String, Object> farmMap){
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmMap);
        return processCall(apiCall, callback);
    }

    public static boolean editFarm(Callback<ResponseBody> callback, String farmId, HashMap<String, Object> farmMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmId, farmMap);
        return processCall(apiCall, callback);
    }

    private static <T> boolean processCall(Call<T> call, Callback<T> callback){
        try {
            if(mContext == null)
                Fennel.initWebApi();
            if(NetworkHelper.isNetAvailable(mContext)){
                call.enqueue(callback);
                return true;
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }
}
