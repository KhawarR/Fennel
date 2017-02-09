package wal.fennel.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.activities.SplashActivity;
import wal.fennel.application.Fennel;
import wal.fennel.datamodels.Auth;
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.models.DashboardTask;
import wal.fennel.models.FarmVisit;
import wal.fennel.models.FarmVisitLog;
import wal.fennel.models.Farmer;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.LogTaskItem;
import wal.fennel.models.ResponseModel;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.models.TaskItemOption;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.MixPanelConstants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PhotoUtils;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;

import static io.realm.Realm.getDefaultInstance;
import static wal.fennel.utils.Constants.FARMING_STATE_ONTIME;
import static wal.fennel.utils.Constants.STR_FACILITATOR;

/**
 * Created by Khawar on 21/10/2016.
 */
public class WebApi {

    private static final String TAG = "WebApi";
    private static Map<String, Map<String, String>> visitLogFarmingTasks = null;
    private static RealmList<Farmer> pendingFarmersList = new RealmList<>();
    private OnSyncCompleteListener onSyncCompleteListener;
    private static Context mContext = null;
    private static WebApi sInstance = null;

    private static int countFailedCalls = 0;
    private static int countCalls = 0;

    private static MixpanelAPI mixPanel;

    private WebApi(Context context){
        mContext = context;
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WebApi(context);
            mixPanel = MixpanelAPI.getInstance(context, MixPanelConstants.MIXPANEL_TOKEN);
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
        String loginQuery = String.format(NetworkHelper.QUERY_LOGIN_1, username, password);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, loginQuery);
        return processCall(apiCall, callback);
    }

    public static boolean getAboutMeInfo(Callback<ResponseBody> callback) {
        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_1, PreferenceHelper.getInstance().readUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMySignUps(Callback<ResponseBody> callback){
        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS_1, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyfarmerTasks(Callback<ResponseBody> callback){
        String query = String.format(NetworkHelper.GET_MY_FARMERS_TASKS, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyLogbookData(Callback<ResponseBody> callback, String fieldOffiers, String facilitators){

        String query = null;
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if (userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
            query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA_FIELD_MANAGER, PreferenceHelper.getInstance().readLoginUserId(), fieldOffiers, facilitators);
            if (!fieldOffiers.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FIELD_OFFICER_ADDON_LOGBOOK, fieldOffiers);
            }
            if (!facilitators.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FACILITATOR_ADDON_LOGBOOK, facilitators);
            }
        } else if (userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA_FIELD_OFFICER, PreferenceHelper.getInstance().readLoginUserId(), facilitators);
            if (!facilitators.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FACILITATOR_ADDON_LOGBOOK, facilitators);
            }
        } else {
            query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA_FACILITATOR, PreferenceHelper.getInstance().readLoginUserId());
        }

//        String query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyDashboardData(Callback<ResponseBody> callback, String fieldOffiers, String facilitators){

        String query = null;
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if (userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
            query = String.format(NetworkHelper.GET_MY_DASHBOARD_DATA_FIELD_MANAGER, PreferenceHelper.getInstance().readLoginUserId(), fieldOffiers, facilitators);
            if (!fieldOffiers.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FIELD_OFFICER_ADDON_DASHBOARD, fieldOffiers);
            }
            if (!facilitators.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FACILITATOR_ADDON_DASHBOARD, facilitators);
            }
            query = query + NetworkHelper.DASHBOARD_QUERY_ORDER;
        } else if (userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            query = String.format(NetworkHelper.GET_MY_DASHBOARD_DATA_FIELD_OFFICER, PreferenceHelper.getInstance().readLoginUserId(), facilitators);
            if (!facilitators.trim().isEmpty()) {
                query = query + String.format(NetworkHelper.FACILITATOR_ADDON_DASHBOARD, facilitators);
            }
            query = query + NetworkHelper.DASHBOARD_QUERY_ORDER;
        } else {
            query = String.format(NetworkHelper.GET_MY_DASHBOARD_DATA_FACILITATOR, PreferenceHelper.getInstance().readLoginUserId());
        }

//        String query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getFOAndFacDataForLogbook(Callback<ResponseBody> callback, String queryStr) {
        String query = String.format(queryStr, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyFarmerAttachments(Callback<ResponseBody> callback){

        String farmerIds = "";

        for (int i = 0; i < Singleton.getInstance().mySignupsList.size(); i++) {

            Farmer farmer = Singleton.getInstance().mySignupsList.get(i);

            if(!farmer.isHeader() && !farmer.getFarmerId().isEmpty()){
                String id = farmer.getFarmerId();

                id = "'" + id + "'";

                farmerIds = farmerIds + id;

                if(i+1 != Singleton.getInstance().mySignupsList.size()){
                    farmerIds = farmerIds + ",";
                }
            }
        }

        if (farmerIds.isEmpty()) {
            return false;
        }

        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS_ATTACHMENTS, farmerIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyFarmerTaskAttachments(Callback<ResponseBody> callback){

        String farmerIds = "";

        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            Farmer farmer = Singleton.getInstance().myFarmersList.get(i);

            if(!farmer.isHeader() && !farmer.getFarmerId().isEmpty()){
                String id = farmer.getFarmerId();

                id = "'" + id + "'";

                farmerIds = farmerIds + id;

                if(i+1 != Singleton.getInstance().myFarmersList.size()){
                    farmerIds = farmerIds + ",";
                }
            }
        }

        if (farmerIds.isEmpty())
            return false;

        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS_ATTACHMENTS, farmerIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getAboutMeAttachment(Callback<ResponseBody> callback){
        String queryTable = "Employee__c";
//        String userType = PreferenceHelper.getInstance().readLoginUserType();
//        if(userType.equalsIgnoreCase(Constants.STR_FACILITATOR))
//            queryTable = "Facilitator__c";
//        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
//            queryTable = "Field_Officer__c";
//        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
//            queryTable = "Field_Manager__c";

        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_ATTACHMENT, queryTable, PreferenceHelper.getInstance().readUserEmployeeId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static Response getLocations() throws IOException {
        String locationsQuery = NetworkHelper.GET_LOCATIONS;
        Call<ResponseBody> apiCall = Fennel.getWebService().querySynchronous(Session.getAuthToken(), NetworkHelper.API_VERSION, locationsQuery);
        Response<ResponseBody> res = apiCall.execute();
        return res;
    }

    public static Response getSubLocations() throws IOException {
        String subLocationsQuery = NetworkHelper.GET_SUB_LOCATIONS;
        Call<ResponseBody> apiCall = Fennel.getWebService().querySynchronous(Session.getAuthToken(), NetworkHelper.API_VERSION, subLocationsQuery);
        Response<ResponseBody> res = apiCall.execute();
        return res;
    }

    public static Response getVillages() throws IOException {
        String villagesQuery = NetworkHelper.GET_VILLAGES;
        Call<ResponseBody> apiCall = Fennel.getWebService().querySynchronous(Session.getAuthToken(), NetworkHelper.API_VERSION, villagesQuery);
        Response<ResponseBody> res = apiCall.execute();
        return res;
    }

    public static Response getTrees() throws IOException {
        String treesQuery = NetworkHelper.GET_TREES;
        Call<ResponseBody> apiCall = Fennel.getWebService().querySynchronous(Session.getAuthToken(), NetworkHelper.API_VERSION, treesQuery);
        Response<ResponseBody> res = apiCall.execute();
        return res;
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

//    public static boolean getFarmingTaskItems(Callback<ResponseBody> callback, String farmingTaskId){
//        String query = String.format(NetworkHelper.GET_FARMER_TASK_ITEMS, farmingTaskId);
//        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
//        return processCall(apiCall, callback);
//    }

    public static boolean getFarmingTaskItems(Callback<ResponseBody> callback, String farmingTaskIds){
        String query = String.format(NetworkHelper.GET_FARMER_TASK_ITEMS, farmingTaskIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getAllTaskItemAttachments(Callback<ResponseBody> callback, String taskItemIds){
        String query = String.format(NetworkHelper.QUERY_TASK_ITEM_ATTACHMENTS, taskItemIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean createFarmVisit(Callback<ResponseModel> callback, HashMap<String, Object> farmVisitMap) {
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmVisit(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmVisitMap);
        return processCall(apiCall, callback);
    }

    public static boolean createFarmVisitLog(Callback<ResponseModel> callback, HashMap<String, Object> farmVisitLogMap) {
        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmVisitLog(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmVisitLogMap);
        return processCall(apiCall, callback);
    }

    public static boolean editFarmingTask(Callback<ResponseBody> callback, String farmingTaskId, HashMap<String, Object> farmingTaskMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editFarmingTask(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmingTaskId, farmingTaskMap);
        return processCall(apiCall, callback);
    }

    public static boolean editTaskItem(Callback<ResponseBody> callback, String taskItemId, HashMap<String, Object> taskItemMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editTaskItem(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, taskItemId, taskItemMap);
        return processCall(apiCall, callback);
    }

    public static boolean editTaskItemOption(Callback<ResponseBody> callback, String taskItemOptionId, HashMap<String, Object> taskItemOptionMap){
        Call<ResponseBody> apiCall = Fennel.getWebService().editTaskItemOption(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, taskItemOptionId, taskItemOptionMap);
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

    public static void setSyncCompleteListener(OnSyncCompleteListener onSyncCompleteListener) {
        WebApi.getInstance().onSyncCompleteListener = onSyncCompleteListener;
    }

    public static void syncAll(OnSyncCompleteListener onSyncCompleteListener){

        FennelUtils.appendDebugLog("--------------------------------------------- sync started");
//        countCalls = 0;
        countCalls = getTotalSyncCallCount();
        countFailedCalls = 0;

        FennelUtils.appendDebugLog("Total Count calls: " + countCalls);

        WebApi.getInstance().onSyncCompleteListener = onSyncCompleteListener;

        //region About Me portion
        if(PreferenceHelper.getInstance().readAboutIsSyncReq()){
            String imagePath = NetworkHelper.getUploadPathFromUri(PreferenceHelper.getInstance().readAboutAttUrl());
//            countCalls++;

            String attAboutId = PreferenceHelper.getInstance().readAboutAttId();
            if (attAboutId == null || attAboutId.isEmpty()) {
                WebApi.addAboutMeImage(imagePath, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        Log.i("FENNEL", "About me image upload onResponce.. Count Calls: " + countCalls);
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "facilitator profile picture uploaded successfully!");

                            String responseStr = null;

                            try {
                                responseStr = response.body().string();
                                String aboutAttId = getAttachmentIdFromUploadSuccess(responseStr);
                                PreferenceHelper.getInstance().writeAboutAttId(aboutAttId);
                                String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(aboutAttId);
                                PreferenceHelper.getInstance().writeAboutAttUrl(thumbUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            checkSyncComplete();
                            PreferenceHelper.getInstance().writeAboutIsSyncReq(false);
                        }
                        else if(response.code() == 401)
                        {
                            countFailedCalls++;
                            sessionExpireRedirect();
                        } else {
                            String errorMessage = "";
                            if(response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string().toString();
                                    JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                    errorMessage = objError.getString("message");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Exception e = new Exception("AboutMeImage create failed - " + response.code() + " - "  + errorMessage);
                            Crashlytics.logException(e);
                            if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                                sessionExpireRedirect();
                            }
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.i("FENNEL", "About me image upload onFailure.. Count Calls: " + countCalls);
                        countCalls--;
                        countFailedCalls++;
                        t.printStackTrace();
                        checkSyncComplete();
                    }
                });
            } else {
                WebApi.addAboutMeImage(imagePath, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        Log.i("FENNEL", "About me image edit onResponce.. Count Calls: " + countCalls);

                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "facilitator profile picture edited successfully!");

                            String newUrl = NetworkHelper.makeAttachmentUrlFromId(PreferenceHelper.getInstance().readAboutAttId());
                            PreferenceHelper.getInstance().writeAboutAttUrl(newUrl);
                            MyPicassoInstance.getInstance().invalidate(newUrl);

                        } else if (response.code() == 401) {
                            countFailedCalls++;
                            sessionExpireRedirect();
                        } else {
                            String errorMessage = "";
                            if(response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string().toString();
                                    JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                    errorMessage = objError.getString("message");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Exception e = new Exception("AboutMeImage Edit failed - " + response.code() + " - "  + errorMessage);
                            Crashlytics.logException(e);
                            if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                                sessionExpireRedirect();
                            }
                            countFailedCalls++;
                            Log.i("Fennel", "facilitator profile picture edit failed!");
                        }
                        checkSyncComplete();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.i("FENNEL", "About me image edit onFailure.. Count Calls: " + countCalls);
                        countCalls--;
                        countFailedCalls++;
                        checkSyncComplete();
                        Log.i("Fennel", "facilitator profile picture edit failed!");
                    }
                });
            }
        }
        //endregion

        //region Farmers
        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("isDataDirty", true).findAll();
        RealmList<Farmer> farmerRealmList = new RealmList<>();
        farmerRealmList.addAll(farmerDbList);
        processFarmerCalls(farmerRealmList);
        //endregion

        //region FarmVisits
        RealmResults<FarmVisit> farmVisits = Realm.getDefaultInstance().where(FarmVisit.class).equalTo("isDataDirty", true).findAll();
        processFarmVisitCalls(farmVisits);
        //endregion

        //region TaskItems
        RealmResults<TaskItem> taskItems = Realm.getDefaultInstance().where(TaskItem.class).equalTo("isDataDirty", true).findAll();
        processTaskItemCalls(taskItems);
        //endregion

        //region TaskItemOption
        RealmResults<TaskItemOption> taskItemOptions = Realm.getDefaultInstance().where(TaskItemOption.class).equalTo("isDataDirty", true).findAll();
        processTaskItemOptionCalls(taskItemOptions);
        //endregion

        //region TaskItemPics
        RealmResults<TaskItem> taskItemPics = Realm.getDefaultInstance().where(TaskItem.class).equalTo("isPicUploadDirty", true).findAll();
        processTaskItemPicsCalls(taskItemPics);
        //endregion

        PreferenceHelper.getInstance().writeIsSyncInProgress(true);
    }

    private static void sessionExpireRedirect(){
        PreferenceHelper.getInstance().clearSession(false);
        Intent intent = new Intent(mContext, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private static void processFarmerCalls(RealmList<Farmer> incomingFarmerList){

        ArrayList<String> arrFarmerIds = new ArrayList<>();
        RealmList<Farmer> iterativeFarmerList = new RealmList<>();
        iterativeFarmerList.addAll(incomingFarmerList);
        pendingFarmersList.clear();

        for (int i = 0; i < iterativeFarmerList.size(); i++) {

            final Farmer farmer = iterativeFarmerList.get(i);
            if(arrFarmerIds.contains(farmer.getIdNumber())){
                FennelUtils.appendDebugLog("Farmer ID goes pending: " + farmer.getIdNumber());
                int countToMinus = 2;
                if (farmer.isFarmerPicDirty())
                    countToMinus++;
                if (farmer.isNatIdCardDirty())
                    countToMinus++;
                countCalls = countCalls - countToMinus;
                pendingFarmersList.add(farmer);
                checkSyncComplete();
            } else {
                arrFarmerIds.add(farmer.getIdNumber());

                final HashMap<String, Object> farmerMap = getFarmerMap(farmer);
                if (farmer.getFarmerId().isEmpty() || farmer.getFarmerId().startsWith(Constants.STR_FARMER_ID_PREFIX)) {
                    WebApi.createFarmer(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                            countCalls--;

                            FennelUtils.appendDebugLog("Farmer create response: " + farmer.getIdNumber() + " - " + response.code());

                            String newFarmerId = "";
                            String errorMessage = "";
                            if(response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string().toString();
                                    FennelUtils.appendDebugLog("Farmer create error response: " + farmer.getIdNumber() + " - " + response.code() + " - " + errorMessage);
                                    JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                    errorMessage = objError.getString("message");

                                    if(!errorMessage.isEmpty()) {
                                        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(errorMessage);
                                        while(m.find()) {
                                            newFarmerId = m.group(1);
                                            Log.i("Existing farmer", "Existing farmer ID: " + newFarmerId);
                                            FennelUtils.appendDebugLog("Existing farmer: ID number = " + farmer.getIdNumber() + " | Id = " + newFarmerId);
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(response.code() == 401) {
                                FennelUtils.appendDebugLog("Farmer create Session Expired, redirected: " + farmer.getIdNumber());
                                countFailedCalls++;
                                sessionExpireRedirect();
                            }
                            else if (!newFarmerId.isEmpty() || ((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().isSuccess() == true)) {

                                if(newFarmerId.isEmpty() && response.body() != null){
                                    newFarmerId = response.body().getId();
//                                    try {
//                                        newFarmerId = response.body().id;
//                                    }
//                                    catch (NullPointerException e){
//                                        e.printStackTrace();
//                                        String errorCrash = e.getMessage();
//
//                                        if(errorMessage.isEmpty()){
//                                            if(response.errorBody() != null){
//                                                try {
//                                                    errorCrash = errorCrash + ": " + response.errorBody().string();
//                                                } catch (IOException e1) {
//                                                    e1.printStackTrace();
//                                                }
//                                            }
//                                        }
//                                        else {
//                                            errorCrash = errorCrash + ": " + errorMessage;
//                                        }
//
//                                        Exception ee = new Exception("Farmer Create failed - " + errorCrash);
//                                        Crashlytics.logException(ee);
//                                    }
                                }

                                if(newFarmerId.isEmpty()){
                                    FennelUtils.appendDebugLog("Farmer create failed, ID empty: " + farmer.getIdNumber());
                                    countFailedCalls++;
                                    adjustCountCallFailedFarmer(farmer);
                                    checkSyncComplete();
                                }
                                else {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    farmer.setFarmerId(newFarmerId);
                                    realm.commitTransaction();

                                    FennelUtils.appendDebugLog("Farmer create finished: " + farmer.getIdNumber());

                                    addFarmWithFarmerId(farmer);

                                    checkSyncComplete();

                                    if (farmer.isFarmerPicDirty())
                                        attachFarmerImageToFarmerObject(farmer);
                                    if (farmer.isNatIdCardDirty())
                                        attachFarmerIDImageToFarmerObject(farmer);
                                }
                            }else {
                                FennelUtils.appendDebugLog("Farmer Create failed - " + response.code() + " - "  + errorMessage);
                                Exception e = new Exception("Farmer Create failed - " + response.code() + " - "  + errorMessage);
                                Crashlytics.logException(e);
                                if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                                    sessionExpireRedirect();
                                }
                                countFailedCalls++;
                                adjustCountCallFailedFarmer(farmer);
                                checkSyncComplete();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            countCalls--;
                            countFailedCalls++;
                            FennelUtils.appendDebugLog("Farmer create Failed: " + t.getMessage());
                            adjustCountCallFailedFarmer(farmer);
                            t.printStackTrace();
                            checkSyncComplete();
                        }
                    }, farmerMap);
                } else {
                    WebApi.editFarmer(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            countCalls--;

                            FennelUtils.appendDebugLog("Farmer edit response: " + farmer.getIdNumber() + " - " + response.code());

                            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                                FennelUtils.appendDebugLog("Farmer edit finished: " + farmer.getIdNumber());

                                if(farmer.getFarmId().startsWith(Constants.STR_FARMER_ID_PREFIX) || farmer.getFarmId().isEmpty() || farmer.getFarmId() == null){
                                    addFarmWithFarmerId(farmer);
                                } else {
                                    editFarmWithFarmId(farmer);
                                }

                                checkSyncComplete();

                                if (farmer.isFarmerPicDirty())
                                    attachFarmerImageToFarmerObject(farmer);
                                if (farmer.isNatIdCardDirty())
                                    attachFarmerIDImageToFarmerObject(farmer);
                            }
                            else if(response.code() == 401){
                                FennelUtils.appendDebugLog("Farmer edit Session Expired, redirected: " + farmer.getIdNumber());
                                countFailedCalls++;
                                sessionExpireRedirect();
                            } else {
                                String errorMessage = "";
                                if(response.errorBody() != null) {
                                    try {
                                        errorMessage = response.errorBody().string().toString();
                                        JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                        errorMessage = objError.getString("message");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                FennelUtils.appendDebugLog("Farmer edit failed - " + response.code() + " - "  + errorMessage);
                                Exception e = new Exception("Farmer Edit failed - " + response.code() + " - "  + errorMessage);
                                Crashlytics.logException(e);
                                if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                                    sessionExpireRedirect();
                                }
                                countFailedCalls++;
                                adjustCountCallFailedFarmer(farmer);
                                checkSyncComplete();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            countCalls--;
                            countFailedCalls++;
                            FennelUtils.appendDebugLog("Farmer edit Failed: " + t.getMessage());
                            adjustCountCallFailedFarmer(farmer);
                            t.printStackTrace();
                            checkSyncComplete();
                        }
                    }, farmer.getFarmerId(), farmerMap);
                }
            }
        }
    }

    private static void processFarmVisitCalls(RealmResults<FarmVisit> farmVisits) {
        for (int i = 0; i < farmVisits.size(); i++) {
            final FarmVisit farmVisit = farmVisits.get(i);
            if(farmVisit.getFarmVisitId().startsWith(Constants.STR_FARMER_ID_PREFIX)) {
                final HashMap<String, Object> farmVisitMap = getFarmVisitMap(farmVisit);
                WebApi.createFarmVisit(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        countCalls--;
                        Log.i("FENNEL", "Farm visit onResponce.. Count Calls: " + countCalls);

                        FennelUtils.appendDebugLog("FarmVisit create response: " + farmVisit.getFarmVisitId() + " - " + response.code());

                        String newFarmVisitId = "";
                        String errorMessage = "";
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string().toString();
                                FennelUtils.appendDebugLog("FarmVisit create error response: " + farmVisit.getFarmVisitId() + " - " + response.code() + " - " + errorMessage);
                                JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                errorMessage = objError.getString("message");

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (response.code() == 401) {
                            FennelUtils.appendDebugLog("FarmVisit create Session Expired, redirected: " + farmVisit.getFarmVisitId());
                            countFailedCalls++;
                            sessionExpireRedirect();
                        } else if (((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().isSuccess() == true)) {
                            newFarmVisitId = response.body().getId();

                            if (newFarmVisitId.isEmpty()) {
                                FennelUtils.appendDebugLog("FarmVisit create failed" + farmVisit.getFarmVisitId());
                                countFailedCalls++;
                                adjustCountCallFailedFarmVisitLog(farmVisit.getFarmVisitId());
                                checkSyncComplete();
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                String localFarmVisitId = farmVisit.getFarmVisitId();

                                RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).equalTo("farmVisitId", localFarmVisitId).findAll();
                                for (int j = 0; j < farmVisitLogs.size(); j++) {
                                    farmVisitLogs.get(j).setFarmVisitId(newFarmVisitId);
                                }

                                farmVisit.setFarmVisitId(newFarmVisitId);
                                farmVisit.setDataDirty(false);
                                realm.commitTransaction();

                                FennelUtils.appendDebugLog("FarmVisit create finished: " + farmVisit.getFarmVisitId());

                                processFarmVisitLogCalls(farmVisit);

                                checkSyncComplete();
                            }
                        } else {
                            FennelUtils.appendDebugLog("FarmVisit Create failed - " + response.code() + " - " + errorMessage);
                            Exception e = new Exception("FarmVisit Create failed - " + response.code() + " - " + errorMessage);
                            Crashlytics.logException(e);
                            if (errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)) {
                                sessionExpireRedirect();
                            }
                            countFailedCalls++;
                            adjustCountCallFailedFarmVisitLog(farmVisit.getFarmVisitId());
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        Log.i("FENNEL", "Farm visit onFailure.. Count Calls: " + countCalls);

                        FennelUtils.appendDebugLog("FarmVisit create Failed: " + t.getMessage());
                        t.printStackTrace();
                        adjustCountCallFailedFarmVisitLog(farmVisit.getFarmVisitId());
                        checkSyncComplete();
                    }
                }, farmVisitMap);
            } else {
                countCalls--;
                Log.i("FENNEL", "Farm visit id not local.. Count Calls: " + countCalls);
                processFarmVisitLogCalls(farmVisit);
                checkSyncComplete();
            }
        }
    }

    private static void processFarmVisitLogCalls(final FarmVisit farmVisit) {

        RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).equalTo("farmVisitId", farmVisit.getFarmVisitId()).findAll();

        for (int i = 0; i < farmVisitLogs.size(); i++) {
            final FarmVisitLog farmVisitLog = farmVisitLogs.get(i);
            final HashMap<String, Object> farmVisitLogMap = getFarmVisitLogMap(farmVisitLog);

            WebApi.createFarmVisitLog(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    countCalls--;
                    Log.i("FENNEL", "Farm visit log onResponse.. Count Calls: " + countCalls);

                    FennelUtils.appendDebugLog("FarmVisitLog create response: " + farmVisitLog.getFarmVisitId() + " - " + response.code());

                    String errorMessage = "";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string().toString();
                            FennelUtils.appendDebugLog("FarmVisitLog create error response: " + farmVisitLog.getFarmVisitId() + " - " + response.code() + " - " + errorMessage);
                            JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                            errorMessage = objError.getString("message");

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (response.code() == 401) {
                        FennelUtils.appendDebugLog("FarmVisitLog create Session Expired, redirected: " + farmVisitLog.getFarmVisitId());
                        countFailedCalls++;
                        sessionExpireRedirect();
                    } else if (((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().isSuccess() == true)) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        farmVisit.setDataDirty(false);
                        farmVisitLog.setDataDirty(false);
                        realm.commitTransaction();

                        FennelUtils.appendDebugLog("FarmVisitLog create finished: " + farmVisitLog.getFarmVisitId());

                        processFarmingTaskCalls();

                        checkSyncComplete();
                    } else {
                        FennelUtils.appendDebugLog("FarmVisitLog Create failed - " + response.code() + " - " + errorMessage);
                        Exception e = new Exception("FarmVisitLog Create failed - " + response.code() + " - " + errorMessage);
                        Crashlytics.logException(e);

                        boolean deleteFromRealm = false;

                        if (errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)) {
                            sessionExpireRedirect();
                        } else if(errorMessage.equalsIgnoreCase(Constants.STR_RESPONSE_ENTITIY_DELETED)) {
                            deleteFromRealm = true;
                        }

                        countFailedCalls++;
                        adjustCountCallFailedFarmingTasks(farmVisitLog.getFarmingTaskId(), deleteFromRealm);
                        checkSyncComplete();
                    }
                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    countCalls--;
                    countFailedCalls++;
                    Log.i("FENNEL", "Farm visit log onFailure.. Count Calls: " + countCalls);

                    adjustCountCallFailedFarmingTasks(farmVisitLog.getFarmingTaskId(), false);
                    FennelUtils.appendDebugLog("FarmVisitLog create Failed: " + t.getMessage());
                    t.printStackTrace();
                    checkSyncComplete();
                }
            }, farmVisitLogMap);
        }
    }

    private static void processFarmingTaskCalls() {
        RealmResults<Task> farmingTasks = Realm.getDefaultInstance().where(Task.class).equalTo("isDataDirty", true).findAll();
        for (int i = 0; i < farmingTasks.size(); i++) {
            final Task farmingTask = farmingTasks.get(i);
            final HashMap<String, Object> farmingTaskMap = getFarmingTaskMap(farmingTask);
            WebApi.editFarmingTask(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    Log.i("FENNEL", "Farming task onResponse.. Count Calls: " + countCalls);


                    FennelUtils.appendDebugLog("FarmingTask edit response: " + farmingTask.getTaskId() + " - " + response.code());

                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                        FennelUtils.appendDebugLog("FarmingTask edit finished: " + farmingTask.getTaskId());
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        farmingTask.setDataDirty(false);
                        realm.commitTransaction();

                        checkSyncComplete();
                    }
                    else if(response.code() == 401){
                        FennelUtils.appendDebugLog("FarmingTask edit Session Expired, redirected: " + farmingTask.getTaskId());
                        countFailedCalls++;
                        sessionExpireRedirect();
                    } else {
                        String errorMessage = "";
                        if(response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string().toString();
                                JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                errorMessage = objError.getString("message");
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        FennelUtils.appendDebugLog("FarmingTask edit failed - " + response.code() + " - "  + errorMessage);
                        Exception e = new Exception("FarmingTask Edit failed - " + response.code() + " - "  + errorMessage);
                        Crashlytics.logException(e);
                        if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                            sessionExpireRedirect();
                        }
                        countFailedCalls++;
                        checkSyncComplete();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    countCalls--;
                    countFailedCalls++;
                    Log.i("FENNEL", "Farming task onFailure.. Count Calls: " + countCalls);

                    FennelUtils.appendDebugLog("FarmingTask edit Failed: " + t.getMessage());
                    t.printStackTrace();
                    checkSyncComplete();
                }
            }, farmingTask.getTaskId(), farmingTaskMap);
        }
    }

    private static void processTaskItemCalls(RealmResults<TaskItem> taskItems) {
        for (int i = 0; i < taskItems.size(); i++) {
            final TaskItem taskItem = taskItems.get(i);
            final HashMap<String, Object> taskItemMap = getTaskItemMap(taskItem);
            WebApi.editTaskItem(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    Log.i("FENNEL", "Task items onResponse.. Count Calls: " + countCalls);

                    FennelUtils.appendDebugLog("TaskItem edit response: " + taskItem.getId() + " - " + response.code());

                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                        FennelUtils.appendDebugLog("TaskItem edit finished: " + taskItem.getId());
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        taskItem.setDataDirty(false);
                        realm.commitTransaction();

                        checkSyncComplete();
                    }
                    else if(response.code() == 401){
                        FennelUtils.appendDebugLog("TaskItem edit Session Expired, redirected: " + taskItem.getId());
                        countFailedCalls++;
                        sessionExpireRedirect();
                    } else {
                        String errorMessage = "";
                        if(response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string().toString();
                                JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                errorMessage = objError.getString("message");
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        FennelUtils.appendDebugLog("TaskItem edit failed - " + response.code() + " - "  + errorMessage);
                        Exception e = new Exception("TaskItem Edit failed - " + response.code() + " - "  + errorMessage);
                        Crashlytics.logException(e);
                        if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                            sessionExpireRedirect();
                        }
                        countFailedCalls++;
                        checkSyncComplete();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    countCalls--;
                    countFailedCalls++;
                    Log.i("FENNEL", "Task Items onFailure.. Count Calls: " + countCalls);

                    FennelUtils.appendDebugLog("TaskItem edit Failed: " + t.getMessage());
                    t.printStackTrace();
                    checkSyncComplete();
                }
            }, taskItem.getId(), taskItemMap);
        }
    }

    private static void processTaskItemPicsCalls(RealmResults<TaskItem> taskItemPics) {
        for (int i = 0; i < taskItemPics.size(); i++) {
            final TaskItem taskItem = taskItemPics.get(i);

            if(taskItem.isPicUploadDirty()) {
                attachImageToTaskItemObject(taskItem);
            }
        }
    }

    private static void processTaskItemOptionCalls(RealmResults<TaskItemOption> taskItemOptions) {
        for (int i = 0; i < taskItemOptions.size(); i++) {
            final TaskItemOption taskItemOption = taskItemOptions.get(i);
            final HashMap<String, Object> taskItemOptionMap = getTaskItemOptionMap(taskItemOption);
            WebApi.editTaskItemOption(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    Log.i("FENNEL", "Task Item option onResponse.. Count Calls: " + countCalls);


                    FennelUtils.appendDebugLog("TaskItemOption edit response: " + taskItemOption.getId() + " - " + response.code());

                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                        FennelUtils.appendDebugLog("TaskItemOption edit finished: " + taskItemOption.getId());
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        taskItemOption.setDataDirty(false);
                        realm.commitTransaction();

                        checkSyncComplete();
                    }
                    else if(response.code() == 401){
                        FennelUtils.appendDebugLog("TaskItemOption edit Session Expired, redirected: " + taskItemOption.getId());
                        countFailedCalls++;
                        sessionExpireRedirect();
                    } else {
                        String errorMessage = "";
                        if(response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string().toString();
                                JSONObject objError = new JSONObject(new JSONArray(errorMessage).getJSONObject(0).toString());
                                errorMessage = objError.getString("message");
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        FennelUtils.appendDebugLog("TaskItemOption edit failed - " + response.code() + " - "  + errorMessage);
                        Exception e = new Exception("TaskItemOption Edit failed - " + response.code() + " - "  + errorMessage);
                        Crashlytics.logException(e);
                        if(errorMessage.equalsIgnoreCase(Constants.URL_NOT_SET_ERROR_MESSAGE)){
                            sessionExpireRedirect();
                        }
                        countFailedCalls++;
                        checkSyncComplete();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    countCalls--;
                    countFailedCalls++;
                    Log.i("FENNEL", "Task Item option onFailure.. Count Calls: " + countCalls);

                    FennelUtils.appendDebugLog("TaskItemOption edit Failed: " + t.getMessage());
                    t.printStackTrace();
                    checkSyncComplete();
                }
            }, taskItemOption.getId(), taskItemOptionMap);
        }
    }

    private static void adjustCountCallFailedFarmer(Farmer farmer){
        // -- for farm call
        countCalls--;

        if(farmer.isFarmerPicDirty())
            countCalls--;
        if(farmer.isNatIdCardDirty())
            countCalls--;
    }

    private static void adjustCountCallFailedFarmVisitLog(String farmVisitId){

        RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).equalTo("farmVisitId", farmVisitId).findAll();
        if(farmVisitLogs != null && farmVisitLogs.size() > 0) {
            countCalls = countCalls - farmVisitLogs.size();
        }
    }

    private static void adjustCountCallFailedFarmingTasks(String farmingTaskId, boolean deleteFromRealm){

        RealmResults<Task> farmingTasks = Realm.getDefaultInstance().where(Task.class).equalTo("isDataDirty", true).equalTo("taskId", farmingTaskId).findAll();
        if(farmingTasks != null && farmingTasks.size() > 0) {
            countCalls = countCalls - farmingTasks.size();
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if(deleteFromRealm) {

            for (int i = 0; i < farmingTasks.size(); i++) {
                farmingTasks.get(i).deleteFromRealm();
            }

            RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).equalTo("farmingTaskId", farmingTaskId).findAll();
            if (farmVisitLogs != null && farmVisitLogs.size() > 0) {
                for (int i = 0; i < farmVisitLogs.size(); i++) {
                    farmVisitLogs.get(i).deleteFromRealm();
                }
            }
        }
        realm.commitTransaction();
    }

    private static String getAttachmentIdFromUploadSuccess(String data) {
        JSONObject responseJson = null;
        String attachmentId = null;
        try {
            responseJson = new JSONObject(data);
            attachmentId = responseJson.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attachmentId;
    }

    public static void addAboutMeImage(String imagePath, Callback<ResponseBody> callback){
        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "picture");
        attachmentMap.put("Name", "profile_picture.png");
        String attAboutId = PreferenceHelper.getInstance().readAboutAttId();
        if (attAboutId == null || attAboutId.isEmpty()) {
            attachmentMap.put("ParentId", PreferenceHelper.getInstance().readUserEmployeeId());
        }
        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        MyPicassoInstance.getInstance().invalidate(thumbUrl);

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

//        bmp = PhotoUtils.decodeSampledBitmapFromResource(imagePath);
        bmp = PhotoUtils.getBitmapFromPath(imagePath);

        if(bmp != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byteArrayImage = bos.toByteArray();
            try {
                bos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(imagePath));
        }

        if(byteArrayImage != null){
            RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

            if (attAboutId == null || attAboutId.isEmpty()) {
                WebApi.addAttachment(callback, entityBody, imageBody);
            } else {
                WebApi.editAttachment(callback, attAboutId, entityBody, imageBody);
            }
        }
        else {
            Exception e = new Exception("ByteArrayImage: addAboutMeImage() - " + imagePath);
            Crashlytics.logException(e);
            Log.i("ByteArrayImage" , "addAboutMeImage() - " + imagePath);
        }
    }

    private static HashMap<String, Object> getFarmerMap(Farmer farmer) {

        final HashMap<String, Object> newFarmerMap = new HashMap<>();
        newFarmerMap.put("First_Name__c", farmer.getFirstName());
        newFarmerMap.put("Middle_Name__c", farmer.getSecondName());
        newFarmerMap.put("Last_Name__c", farmer.getSurname());
        newFarmerMap.put("Name", farmer.getIdNumber());
        newFarmerMap.put("Mobile_Number__c", farmer.getMobileNumber());
        newFarmerMap.put("Gender__c", farmer.getGender().trim().equalsIgnoreCase("male") ? "Male" : "Female");
        newFarmerMap.put("Leader__c", farmer.isLeader() ? 1 : 0);

        return newFarmerMap;
    }

    private static HashMap<String, Object> getFarmVisitMap(FarmVisit farmVisit) {

        final HashMap<String, Object> newFarmVisitMap = new HashMap<>();
        newFarmVisitMap.put("Farmer__c", farmVisit.getFarmerId());
        newFarmVisitMap.put("Shamba__c", farmVisit.getShambaId());
        newFarmVisitMap.put("Visited_Date__c", farmVisit.getVisitedDate());

        if(farmVisit.getLoggedInPersonRole().equalsIgnoreCase(Constants.STR_FACILITATOR)) {
            newFarmVisitMap.put("Facilitator__c", farmVisit.getLoggedInPersonId());
        } else if(farmVisit.getLoggedInPersonRole().equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            newFarmVisitMap.put("Field_Officer__c", farmVisit.getLoggedInPersonId());
        } else if(farmVisit.getLoggedInPersonRole().equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
            newFarmVisitMap.put("Field_Manager__c", farmVisit.getLoggedInPersonId());
        }

        return newFarmVisitMap;
    }

    private static HashMap<String, Object> getFarmingTaskMap(Task farmingTask) {

        final HashMap<String, Object> newFarmingTaskMap = new HashMap<>();
        newFarmingTaskMap.put("Status__c", farmingTask.getStatus());

        return newFarmingTaskMap;
    }

    private static HashMap<String, Object> getTaskItemMap(TaskItem taskItem) {

        final HashMap<String, Object> newTaskItemMap = new HashMap<>();
        newTaskItemMap.put("Completed__c", taskItem.isTaskDone());
        if(taskItem.getTextValue() != null && !taskItem.getTextValue().equalsIgnoreCase("null")) {
            newTaskItemMap.put("Text_Value__c", taskItem.getTextValue());
        }
        if(taskItem.getGpsTakenTime() != null && !taskItem.getGpsTakenTime().equalsIgnoreCase("null")) {
            newTaskItemMap.put("GPS_Taken_Time__c", taskItem.getGpsTakenTime());
        }
        if(taskItem.getLatitude() != 0) {
            newTaskItemMap.put("Location__Latitude__s", taskItem.getLatitude());
        }
        if(taskItem.getLongitude() != 0) {
            newTaskItemMap.put("Location__Longitude__s", taskItem.getLongitude());
        }

        return newTaskItemMap;
    }

    private static HashMap<String, Object> getTaskItemOptionMap(TaskItemOption taskItemOption) {

        final HashMap<String, Object> newTaskItemOptionMap = new HashMap<>();
        newTaskItemOptionMap.put("Value__c", taskItemOption.isValue());

        return newTaskItemOptionMap;
    }

    private static HashMap<String, Object> getFarmVisitLogMap(FarmVisitLog farmVisitLog) {

        final HashMap<String, Object> newFarmVisitLogMap = new HashMap<>();
        newFarmVisitLogMap.put("Farm_Visit__c", farmVisitLog.getFarmVisitId());
        newFarmVisitLogMap.put("Farming_Task__c", farmVisitLog.getFarmingTaskId());

        return newFarmVisitLogMap;
    }

    private static HashMap<String, Object> getFarmMap(Farmer farmer) {

        final HashMap<String, Object> newFarmMap = new HashMap<>();
        newFarmMap.put("LocationLookup__c", farmer.getLocationId());
        newFarmMap.put("Sub_LocationLookup__c", farmer.getSubLocationId());
        newFarmMap.put("Village__c", farmer.getVillageId());
        newFarmMap.put("Tree_Specie__c", farmer.getTreeSpeciesId());
        newFarmMap.put("Is_Farmer_Home__c", farmer.isFarmerHome() ? true : false);

        if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FACILITATOR))
        {
            newFarmMap.put("Facilitator__c", PreferenceHelper.getInstance().readLoginUserId());
            newFarmMap.put("Facilitator_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }
        else if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
        {
            newFarmMap.put("Field_Officer_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }
        else if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
        {
            newFarmMap.put("Field_Manager_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }

        if (farmer.getSignupStatus() != null && !farmer.getSignupStatus().isEmpty()) {
            newFarmMap.put("Sign_Up_Status__c", farmer.getSignupStatus());
        }

        return newFarmMap;
    }

    private static void addFarmWithFarmerId(final Farmer farmer) {
        HashMap<String, Object> farmMap = getFarmMap(farmer);
        farmMap.put("Farmer__c", farmer.getFarmerId());
//        countCalls++;
        WebApi.createFarm(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                countCalls--;
                if (response.body() != null && response.body().isSuccess() == true) {
                    Log.i(farmer.getFullName(), "Farm Synced" );
                    FennelUtils.appendDebugLog("Farm created: " + farmer.getIdNumber());
                    checkSyncComplete();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                    farmerDbObj.setDataDirty(false);
                    realm.commitTransaction();
                }
                else if(response.code() == 401)
                {
                    FennelUtils.appendDebugLog("Farm create session expired, redirected: " + farmer.getIdNumber());
                    countFailedCalls++;
                    sessionExpireRedirect();
                } else {
                    String errorMessage = "-";

                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FennelUtils.appendDebugLog("Farm create Sync Failed: " + farmer.getIdNumber() + " - " + errorMessage);
                    FennelUtils.appendDebugLog("Farm create Sync Failed village: " + farmer.getVillageId() + " - " + farmer.getVillageName());
                    Log.i(farmer.getFullName(), "Farm create Sync Failed: " +  errorMessage);
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                countCalls--;
                countFailedCalls++;
                checkSyncComplete();
                FennelUtils.appendDebugLog("Farm create Sync Failed: " + t.getMessage());
                t.printStackTrace();
            }
        }, farmMap);
    }

    private static void editFarmWithFarmId(final Farmer farmer) {

        HashMap<String, Object> farmMap = getFarmMap(farmer);
        farmMap.put("Farmer__c", farmer.getFarmerId());
//        countCalls++;
        WebApi.editFarm(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                countCalls--;

                FennelUtils.appendDebugLog("Farm edit response: " + farmer.getIdNumber() + " - " + response.code());

                if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                    FennelUtils.appendDebugLog("Farm edited: " + farmer.getIdNumber());

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                    farmerDbObj.setDataDirty(false);
                    realm.commitTransaction();

                    checkSyncComplete();
                }
                else if(response.code() == 401)
                {
                    FennelUtils.appendDebugLog("Farm edit session expired, redirected: " + farmer.getIdNumber());
                    countFailedCalls++;
                    sessionExpireRedirect();
                } else {
                    String errorMessage = "-";

                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FennelUtils.appendDebugLog("Farm edit Sync Failed: " + farmer.getIdNumber() + " - " + errorMessage);
                    Log.i(farmer.getFullName(), "Farm edit Sync Failed: " +  errorMessage);
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                countCalls--;
                countFailedCalls++;
                FennelUtils.appendDebugLog("Farm edit Sync Failed: " + t.getMessage());
                t.printStackTrace();
                checkSyncComplete();
            }
        }, farmer.getFarmId(), farmMap);
    }

    private static void attachFarmerImageToFarmerObject(final Farmer farmer) {

        if (farmer.getThumbUrl() == null || farmer.getThumbUrl().isEmpty()) {
            countCalls--;
            countFailedCalls++;
            checkSyncComplete();
            return;
        }

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "picture");
        attachmentMap.put("Name", "profile_picture.png");
        if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.getFarmerId());
        }
        else
        {
            MyPicassoInstance.getInstance().invalidate(farmer.getThumbUrl());
        }

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        String imagePath = NetworkHelper.getUploadPathFromUri(farmer.getThumbUrl());
//        bmp = PhotoUtils.decodeSampledBitmapFromResource(imagePath);
        bmp = PhotoUtils.getBitmapFromPath(imagePath);

        if(bmp != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byteArrayImage = bos.toByteArray();
            try {
                bos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(imagePath));
        }

//        countCalls++;
        if(byteArrayImage != null){
            RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

            if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
                WebApi.addAttachment(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "farmer profile picture uploaded successfully!");
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                            farmerDbObj.setFarmerPicDirty(false);
                            realm.commitTransaction();
                        } else {
                            countFailedCalls++;
                            Log.i("Fennel", "farmer profile picture upload failed!");
                        }
                        checkSyncComplete();
                    }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    countCalls--;
                    countFailedCalls++;
                    checkSyncComplete();
                    Log.i("Fennel", "farmer profile picture upload failed!");
                }
            }, entityBody, imageBody);
            } else {
                WebApi.editAttachment(new Callback<ResponseBody>() {
                @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                        Log.i("Fennel", "farmer profile picture edited successfully!");
                        Singleton.getInstance().farmerIdtoInvalidate = farmer.getFarmerId();
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                        farmerDbObj.setFarmerPicDirty(false);
                        realm.commitTransaction();
                    } else {
                        countFailedCalls++;
                        Log.i("Fennel", "farmer profile picture edit failed!");
                    }
                    checkSyncComplete();
                }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        checkSyncComplete();
                        Log.i("Fennel", "farmer profile picture edit failed!");
                    }
                }, farmer.getThumbAttachmentId(), entityBody, imageBody);
            }
        }
        else {
            countCalls--;
            countFailedCalls++;
            Exception e = new Exception("ByteArrayImage: attachFarmerImageToFarmerObject() - " + imagePath);
            Crashlytics.logException(e);
            Log.i("ByteArrayImage" , "attachFarmerImageToFarmerObject() - " + imagePath);
            checkSyncComplete();
        }
    }

    private static void attachFarmerIDImageToFarmerObject(final Farmer farmer) {

        if (farmer.getNationalCardUrl() == null || farmer.getNationalCardUrl().isEmpty()) {
            countCalls--;
            countFailedCalls++;
            checkSyncComplete();
            return;
        }

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "ID");
        attachmentMap.put("Name", "national_id.png");
        if(farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.getFarmerId());
        }
        else
        {
            String urlServer = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
            MyPicassoInstance.getInstance().invalidate(urlServer);
            MyPicassoInstance.getInstance().invalidate(farmer.getNationalCardUrl());
        }

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        String imagePath = NetworkHelper.getUploadPathFromUri(farmer.getNationalCardUrl());
//        bmp = PhotoUtils.decodeSampledBitmapFromResource(imagePath);
        bmp = PhotoUtils.getBitmapFromPath(imagePath);

        if(bmp != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byteArrayImage = bos.toByteArray();
            try {
                bos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(imagePath));
        }

//        countCalls++;
        if(byteArrayImage != null){
            RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

            if (farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
                WebApi.addAttachment(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "farmer ID picture uploaded successfully!");
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                            farmerDbObj.setNatIdCardDirty(false);
                            realm.commitTransaction();
                        } else {
                            countFailedCalls++;
                            Log.i("Fennel", "farmer ID picture upload failed!");
                        }
                        checkSyncComplete();
                    }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("Fennel", "farmer ID picture upload failed!");
                    countCalls--;
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }, entityBody, imageBody);
        } else {
            WebApi.editAttachment(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                        Log.i("Fennel", "farmer ID picture edited successfully!");
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).equalTo("farmId", farmer.getFarmId()).findFirst();
                        farmerDbObj.setNatIdCardDirty(false);
                        realm.commitTransaction();
                    } else {
                        Log.i("Fennel", "farmer ID picture edit failed!");
                        countFailedCalls++;
                    }
                    checkSyncComplete();
                }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.i("Fennel", "farmer ID picture edit failed!");
                        countCalls--;
                        countFailedCalls++;
                        checkSyncComplete();
                    }
                }, farmer.getNationalCardAttachmentId(), entityBody, imageBody);
            }
        }
        else {
            countCalls--;
            countFailedCalls++;
            Exception e = new Exception("ByteArrayImage: attachFarmerIDImageToFarmerObject() - " + imagePath);
            Crashlytics.logException(e);
            Log.i("ByteArrayImage" , "attachFarmerIDImageToFarmerObject() - " + imagePath);
            checkSyncComplete();
        }
    }

    private static void attachImageToTaskItemObject(final TaskItem taskItem) {

        if (taskItem.getAttachmentPath() == null || taskItem.getAttachmentPath().isEmpty()) {
            Log.i("FENNEL", "Task Item Attachment NO PATH.. Count Calls: " + countCalls);
            countCalls--;
            countFailedCalls++;
            checkSyncComplete();
            return;
        }

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "FileUploaded");
        attachmentMap.put("Name", "picture.png");
        if (taskItem.getAttachmentId() == null || taskItem.getAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", taskItem.getId());
        }
        else
        {
            MyPicassoInstance.getInstance().invalidate(taskItem.getAttachmentPath());
        }

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        String imagePath = NetworkHelper.getUploadPathFromUri(taskItem.getAttachmentPath());
//        bmp = PhotoUtils.decodeSampledBitmapFromResource(imagePath);
        bmp = PhotoUtils.getBitmapFromPath(imagePath);

        if(bmp != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byteArrayImage = bos.toByteArray();
            try {
                bos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(imagePath));
        }

//        countCalls++;
        if(byteArrayImage != null){
            RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

            if (taskItem.getAttachmentId() == null || taskItem.getAttachmentId().isEmpty()) {
                WebApi.addAttachment(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        Log.i("FENNEL", "Task Item attachment onResponce.. Count Calls: " + countCalls);

                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "TaskItem picture uploaded successfully!");

                            String strResponse = "";
                            String newPicId = "";

                            try {
                                strResponse = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(!strResponse.isEmpty()) {
                                JSONObject objReponse = null;
                                try {
                                    objReponse = new JSONObject(strResponse);
                                    newPicId = objReponse.getString("id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            final TaskItem taskItemDBObj = realm.where(TaskItem.class).equalTo("id", taskItem.getId()).findFirst();
                            taskItemDBObj.setPicUploadDirty(false);
                            taskItemDBObj.setAttachmentId(newPicId);
                            realm.commitTransaction();
                        } else {
                            countFailedCalls++;
                            Log.i("Fennel", "TaskItem picture upload failed!");
                        }
                        checkSyncComplete();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        Log.i("FENNEL", "Task Item attachment onFailure.. Count Calls: " + countCalls);
                        checkSyncComplete();
                        Log.i("Fennel", "TaskItem picture upload failed!");
                    }
                }, entityBody, imageBody);
            } else {
                WebApi.editAttachment(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        Log.i("FENNEL", "Task Item attachment edit onResponse.. Count Calls: " + countCalls);

                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "TaskItem picture edited successfully!");
                            Singleton.getInstance().taskItemPicIdtoInvalidate = taskItem.getId();
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            final TaskItem taskItemDBObj = realm.where(TaskItem.class).equalTo("id", taskItem.getId()).findFirst();
                            taskItemDBObj.setPicUploadDirty(false);
                            realm.commitTransaction();
                        } else {
                            countFailedCalls++;
                            Log.i("Fennel", "TaskItem picture edit failed!");
                        }
                        checkSyncComplete();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        Log.i("FENNEL", "Task Item attachment edit onFailure.. Count Calls: " + countCalls);

                        checkSyncComplete();
                        Log.i("Fennel", "TaskItem picture edit failed!");
                    }
                }, taskItem.getAttachmentId(), entityBody, imageBody);
            }
        }
        else {
            countCalls--;
            countFailedCalls++;
            Log.i("FENNEL", "Task Item attachment byte array image NULL.. Count Calls: " + countCalls);

            Exception e = new Exception("ByteArrayImage: attachImageToTaskItemObject() - " + imagePath);
            Crashlytics.logException(e);
            Log.i("ByteArrayImage" , "attachImageToTaskItemObject() - " + imagePath);
            checkSyncComplete();
        }
    }

    private static void checkSyncComplete(){
        if(countCalls == 0) {

            if(pendingFarmersList.size() > 0){
                int countToAdd = pendingFarmersList.size() * 2;
                for (int i = 0; i < pendingFarmersList.size(); i++) {
                    Farmer pendingFarmer = pendingFarmersList.get(i);
                    if(pendingFarmer.isFarmerPicDirty())
                        countToAdd++;
                    if(pendingFarmer.isNatIdCardDirty())
                        countToAdd++;
                }
                countCalls = countToAdd;
                processFarmerCalls(pendingFarmersList);
            }
            else {
                if(countFailedCalls == 0){
                    Log.i("Sync process: ", "Sync completed");
                    Toast.makeText(mContext, "Sync completed", Toast.LENGTH_SHORT).show();
                    getFullServerData();
                }
                else{
                    Log.i("Sync process: ", "Sync finished, but some records failed to sync");
                    Toast.makeText(mContext, "Sync partially completed", Toast.LENGTH_SHORT).show();
                }
                saveSyncTimeStamp();
                PreferenceHelper.getInstance().writeSessionExpiredSyncReq(false);
                if(WebApi.getInstance().onSyncCompleteListener != null) {
                    WebApi.getInstance().onSyncCompleteListener.syncCompleted();
                }
                mixPanel.track(MixPanelConstants.Event.SYNC_COMPLETED);

                PreferenceHelper.getInstance().writeIsSyncInProgress(false);

                FennelUtils.uploadDebugLogFile(mContext);
            }
        }
    }

    public static boolean isSyncRequired(){
        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("isDataDirty", true).or().equalTo("isFarmerPicDirty", true).or().equalTo("isNatIdCardDirty", true).findAll();
        RealmResults<FarmVisit> farmVisits = Realm.getDefaultInstance().where(FarmVisit.class).equalTo("isDataDirty", true).findAll();
        RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).findAll();
        RealmResults<Task> farmingTasks = Realm.getDefaultInstance().where(Task.class).equalTo("isDataDirty", true).findAll();
        RealmResults<TaskItem> taskItems = Realm.getDefaultInstance().where(TaskItem.class).equalTo("isDataDirty", true).or().equalTo("isPicUploadDirty", true).findAll();
        RealmResults<TaskItemOption> taskItemsOptions = Realm.getDefaultInstance().where(TaskItemOption.class).equalTo("isDataDirty", true).findAll();
        if(PreferenceHelper.getInstance().readAboutIsSyncReq() || farmerDbList.size() > 0 ||
                farmVisits.size() > 0 || farmVisitLogs.size() > 0 || farmingTasks.size() > 0 ||
                taskItems.size() > 0 || taskItemsOptions.size() > 0) {
            return true;
        }
        return false;
    }

    public static int getTotalSyncCallCount(){
        RealmResults<Farmer> dataCalls = Realm.getDefaultInstance().where(Farmer.class).equalTo("isDataDirty", true).findAll();
        RealmResults<Farmer> farmerPicCalls = Realm.getDefaultInstance().where(Farmer.class).equalTo("isFarmerPicDirty", true).findAll();
        RealmResults<Farmer> natIdCalls = Realm.getDefaultInstance().where(Farmer.class).equalTo("isNatIdCardDirty", true).findAll();

        int count = 0;

        if(dataCalls != null)
            count = count + (dataCalls.size() * 2);
        if(farmerPicCalls != null)
            count = count + farmerPicCalls.size();
        if(natIdCalls != null)
            count = count + natIdCalls.size();

        if(PreferenceHelper.getInstance().readAboutIsSyncReq())
            count = count + 1;

        RealmResults<FarmVisit> farmVisits = Realm.getDefaultInstance().where(FarmVisit.class).equalTo("isDataDirty", true).findAll();
        RealmResults<FarmVisitLog> farmVisitLogs = Realm.getDefaultInstance().where(FarmVisitLog.class).equalTo("isDataDirty", true).findAll();
        RealmResults<Task> farmingTasks = Realm.getDefaultInstance().where(Task.class).equalTo("isDataDirty", true).findAll();
        RealmResults<TaskItem> farmingTaskItems = Realm.getDefaultInstance().where(TaskItem.class).equalTo("isDataDirty", true).or().equalTo("isPicUploadDirty", true).findAll();
        RealmResults<TaskItem> farmingTaskItemAttachments = Realm.getDefaultInstance().where(TaskItem.class).equalTo("isPicUploadDirty", true).findAll();
        RealmResults<TaskItemOption> farmingTaskItemOptions = Realm.getDefaultInstance().where(TaskItemOption.class).equalTo("isDataDirty", true).findAll();

        if(farmVisits != null)
            count = count + farmVisits.size();
        if(farmVisitLogs != null)
            count = count + farmVisitLogs.size();
        if(farmingTasks != null)
            count = count + farmingTasks.size();
        if(farmingTaskItems != null)
            count = count + farmingTaskItems.size();
        if(farmingTaskItemOptions != null)
            count = count + farmingTaskItemOptions.size();
        if (farmingTaskItemAttachments != null)
            count = count + farmingTaskItemAttachments.size();

        return count;
    }

    public static void saveSyncTimeStamp(){
        long yourMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date resultDate = new Date(yourMillis);
        String syncTime = sdf.format(resultDate);
        PreferenceHelper.getInstance().writeLastSyncTime(syncTime);
    }

    public static void getFullServerData(){

        WebApi.getMySignUps(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";
                    try {
                        responseStr = response.body().string();
                        parseMySignupsData(responseStr);
                        WebApi.getMyFarmerAttachments(myFarmersAttachmentsCallback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

        WebApi.getAboutMeAttachment(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        String attId = parseAboutMeDataAttachment(responseStr);
                        PreferenceHelper.getInstance().writeAboutAttId(attId);
                        if(!attId.isEmpty())
                        {
                            String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(attId);
                            PreferenceHelper.getInstance().writeAboutAttUrl(thumbUrl);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

        WebApi.getMyfarmerTasks(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.code() == 200) {
                        String responseStr = "";

                        try {
                            responseStr = response.body().string();
                            parseMyFarmersData(responseStr);
                            getFarmerTaskItems();
                            WebApi.getMyFarmerTaskAttachments(myFarmerTasksAttachments);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

        String query = null;
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if (userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
            query = NetworkHelper.GET_FO_FAC_FOR_FM;
        } else if (userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            query = NetworkHelper.GET_FAC_FOR_FO;
        } else {
            WebApi.getMyLogbookData(logbookDataCallback, "", "");
            WebApi.getMyDashboardData(dashboardDataCallback, "", "");
            return;
        }

        WebApi.getFOAndFacDataForLogbook(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        String responseStr = "";

                        try {
                            responseStr = response.body().string();
                            if (PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
                                parseMyLogbookFOFacData(responseStr);
                            } else if (PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
                                parseMyLogbookFacData(responseStr);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        }, query);
    }

    private static Callback<ResponseBody> myFarmerTasksAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyFarmersAttachmentData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMyFarmersAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = getDefaultInstance();
        realm.beginTransaction();

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmerObj = arrRecords.getJSONObject(i);
                String id = farmerObj.getString("Id");

                String farmerPicId = "";
                String farmerNatId = "";

                JSONObject attachmentObj = farmerObj.optJSONObject("Attachments");
                if(attachmentObj != null)
                {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        String description = objAttachment.getString("Description").toLowerCase().trim();
                        if(description.contains("pic") || description.contains("photo"))
                        {
                            farmerPicId = objAttachment.getString("Id");
                        }
                        else if(description.contains("id"))
                        {
                            farmerNatId = objAttachment.getString("Id");
                        }
                    }
                }

                ArrayList<Farmer> allFarmerTasks = Singleton.getInstance().myFarmersList;

                for (int j = 0; j < allFarmerTasks.size(); j++) {
                    final Farmer farmer = allFarmerTasks.get(j);

                    if(farmer.getFarmerId().equalsIgnoreCase(id))
                    {
                        farmer.setThumbAttachmentId(farmerPicId);
                        farmer.setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getThumbAttachmentId());
                        if(!farmerPicId.isEmpty())
                        {
                            farmer.setThumbUrl(thumbUrl);
                        }

                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
                        if(!farmerNatId.isEmpty())
                        {
                            farmer.setNationalCardUrl(natIdUrl);
                        }

                        Farmer farmerDb = realm.where(Farmer.class).equalTo("farmerId", id).equalTo("farmId", farmer.getFarmId()).findFirst();
                        if(farmerDb != null)
                        {
                            farmerDb.setThumbAttachmentId(farmerPicId);
                            farmerDb.setNationalCardAttachmentId(farmerNatId);
                            if(!farmerPicId.isEmpty())
                            {
                                farmerDb.setThumbUrl(thumbUrl);
                            }
                            if(!farmerNatId.isEmpty())
                            {
                                farmerDb.setNationalCardUrl(natIdUrl);
                            }
                        }

                        MyPicassoInstance.getInstance().load(thumbUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "Farmer Pic: " + farmer.getThumbUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "Farmer Pic: " + farmer.getThumbUrl());
                            }
                        }*/);

                        MyPicassoInstance.getInstance().load(natIdUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "NAT ID: " + farmer.getNationalCardUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "NAT ID: " + farmer.getNationalCardUrl());
                            }
                        }*/);

                        break;
                    }
                }
            }

//            tasksAdapter.notifyDataSetChanged();
        }

        realm.commitTransaction();
    }

    private static void parseMyFarmersData(String data) throws JSONException {

        Log.i("FENNEL", data);
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Map<String,Farmer> farmersMap = new HashMap<>();
//        Map<String,Task> tasksMap = new HashMap<>();
        List<Farmer> farmersTaskList = new ArrayList<>();

        if(arrRecords.length() > 0)
        {

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject taskObj = arrRecords.getJSONObject(i);

                String taskName = taskObj.getString("Name");
                String id = taskObj.getString("Id");
                String status = taskObj.getString("Status__c");
                String startedDate = taskObj.getString("Started_Date__c");
                String dueDate = taskObj.getString("Due_Date__c");
                String completionDate = taskObj.getString("Completion_Date__c");

                JSONObject shambaObj = taskObj.getJSONObject("Shamba__r");
                String farmId = taskObj.getString("Shamba__c");
                JSONObject farmerObj = shambaObj.getJSONObject("Farmer__r");
                String farmerId = shambaObj.getString("Farmer__c");
                String farmerName = farmerObj.getString("FullName__c");
                String mobileNumber = farmerObj.getString("Mobile_Number__c");
                String farmerIdNumber = farmerObj.getString("Name");
                String subLocationName = shambaObj.getJSONObject("Sub_LocationLookup__r").getString("Name");
                String villageName = shambaObj.getJSONObject("Village__r").getString("Name");
                String signupStatus = shambaObj.optString("Sign_Up_Status__c");

                if (signupStatus.equalsIgnoreCase(Constants.STR_APPROVED)) {

                    Task currentTask;
//                    if (tasksMap.containsKey(taskName)) {
//                        currentTask = (Task) tasksMap.get(taskName);
//                    } else {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();

                        currentTask = realm.createObject(Task.class);
                        currentTask.setTaskId(id);
                        currentTask.setName(taskName);
                        currentTask.setStartedDate(startedDate);
                        currentTask.setCompletionDate(completionDate);
                        currentTask.setDueDate(dueDate);
                        currentTask.setStatus(status);
                        currentTask.setTaskShambaId(farmId);
                        currentTask.setTaskFarmerId(farmerId);

                        realm.commitTransaction();
//                    currentTask = new Tasks(id, taskName, startedDate, completionDate, dueDate, status);
//                        tasksMap.put(taskName, currentTask);
//                    }

                    Farmer currentFarmer;
                    RealmList<Task> farmingTasks;
                    if (farmersMap.containsKey(farmId)) {
                        currentFarmer = (Farmer) farmersMap.get(farmId);
                        farmingTasks = currentFarmer.getFarmerTasks();
                        if (farmingTasks != null) {
                            farmingTasks.add(currentTask);
                        } else {
                            farmingTasks = new RealmList<>();
                            farmingTasks.add(currentTask);
                            currentFarmer.setFarmerTasks(farmingTasks);
                        }

                    } else {
                        currentFarmer = new Farmer();
                        currentFarmer.setFarmerId(farmerId);
                        currentFarmer.setFarmId(farmId);
                        currentFarmer.setIdNumber(farmerIdNumber);
                        currentFarmer.setFullName(farmerName);
                        currentFarmer.setMobileNumber(mobileNumber);
                        currentFarmer.setSubLocation(subLocationName);
                        currentFarmer.setVillageName(villageName);
                        currentFarmer.setHeader(false);
                        currentFarmer.setFarmerType(Constants.FarmerType.MYFARMERTASKS);

                        farmingTasks = new RealmList<>();
                        farmingTasks.add(currentTask);
                        currentFarmer.setFarmerTasks(farmingTasks);

                        farmersMap.put(farmId, currentFarmer);
                        farmersTaskList.add(currentFarmer);

                    }

//                    if (!(farmersTaskList.contains(currentFarmer))) {
//                        farmersTaskList.add(currentFarmer);
//                    }
                }
            }
        }

        addFarmerTasksToDB(farmersTaskList);
        Singleton.getInstance().myFarmersList = (ArrayList<Farmer>) farmersTaskList;
    }

    private static void addFarmerTasksToDB(List<Farmer> farmersTaskList) {

        Realm realm = getDefaultInstance();

        RealmResults<Farmer> farmerDbList = realm.where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYFARMERTASKS.toString()).findAll();
        realm.beginTransaction();
        farmerDbList.deleteAllFromRealm();
        realm.commitTransaction();

        realm.beginTransaction();
        for (int i = 0; i < farmersTaskList.size(); i++) {
            // Save to DB
            final Farmer farmerDbObj = realm.createObject(Farmer.class);
            farmerDbObj.setAllValues(farmersTaskList.get(i));
        }
        realm.commitTransaction();
    }

    private static Callback<ResponseBody> myFarmersAttachmentsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseFarmerAttachmentData(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMySignupsData(String data) throws JSONException {

        ArrayList<Farmer> incompleteFarmersList = new ArrayList<>();
        ArrayList<Farmer> pendingFarmersList = new ArrayList<>();
        ArrayList<Farmer> approvedFarmersList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmObj = arrRecords.getJSONObject(i);

                String id = "";
                String farmId = "";
                String location = "";
                String locationId = "";
                String subLocation = "";
                String subLocationId = "";
                String tree = "";
                String treeId = "";
                String village = "";
                String villageId = "";
                String fullName = "";
                String firstName = "";
                String secondName = "";
                String surname = "";
                String idNumber = "";
                String gender = "";
                String mobileNumber = "";
                boolean isFarmerHome = false;
                boolean leader = false;

                farmId = farmObj.getString("Id");
                JSONObject objLocation = farmObj.optJSONObject("LocationLookup__r");
                if(objLocation != null)
                {
                    location = objLocation.getString("Name");
                    locationId = objLocation.getString("Id");
                }

                JSONObject objSubLocation = farmObj.optJSONObject("Sub_LocationLookup__r");
                if(objSubLocation != null)
                {
                    subLocation = objSubLocation.getString("Name");
                    subLocationId = objSubLocation.getString("Id");
                }

                JSONObject objVillage = farmObj.optJSONObject("Village__r");
                if(objVillage != null)
                {
                    village = objVillage.getString("Name");
                    villageId = objVillage.getString("Id");
                }


                JSONObject objTree = farmObj.optJSONObject("Tree_Specie__r");
                if(objTree != null)
                {
                    tree = objTree.getString("Name");
                    treeId = objTree.getString("Id");
                }

                id = farmObj.optString("Farmer__c");
                if(id != null && id.equalsIgnoreCase("null")) id = "";

                JSONObject objFarmer = farmObj.optJSONObject("Farmer__r");
                if(objFarmer != null)
                {
                    fullName = objFarmer.getString("FullName__c");
                    if(fullName.equalsIgnoreCase("null")) fullName = "";
                    firstName = objFarmer.getString("First_Name__c");
                    if(firstName.equalsIgnoreCase("null")) firstName = "";
                    secondName = objFarmer.getString("Middle_Name__c");
                    if(secondName.equalsIgnoreCase("null")) secondName = "";
                    surname = objFarmer.getString("Last_Name__c");
                    if(surname.equalsIgnoreCase("null")) surname = "";
                    idNumber = objFarmer.getString("Name");
                    if(idNumber.equalsIgnoreCase("null")) idNumber = "";
                    gender = objFarmer.getString("Gender__c");
                    if(gender.equalsIgnoreCase("null")) gender = "";
                    mobileNumber = objFarmer.getString("Mobile_Number__c");
                    if(mobileNumber.equalsIgnoreCase("null")) mobileNumber = "";
                    leader = objFarmer.getBoolean("Leader__c");
                }

                isFarmerHome = farmObj.optBoolean("Is_Farmer_Home__c");

//                String status = farmObj.getString("Status__c");
                String status = farmObj.getString("Sign_Up_Status__c");

                String strLastModifiedDate = farmObj.getString("LastModifiedDate");
                Date lastModifiedDate = FennelUtils.getLastModifiedDateFromString(strLastModifiedDate, Constants.STR_TIME_FORMAT_YYYY_MM_DD_T_HH_MM_SS);

                Farmer farmer = new Farmer(lastModifiedDate, id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, locationId, subLocation, subLocationId, village, villageId, tree, treeId, isFarmerHome, mobileNumber, "", "", status, false, "", "", null, Constants.FarmerType.MYSIGNUPS);

                if(status.equalsIgnoreCase(Constants.STR_ENROLLED))
                {
                    incompleteFarmersList.add(farmer);
                }
                else if(status.equalsIgnoreCase(Constants.STR_PENDING))
                {
                    pendingFarmersList.add(farmer);
                }
                else if(status.equalsIgnoreCase(Constants.STR_APPROVED))
                {
                    approvedFarmersList.add(farmer);
                }
            }
        }

        Singleton.getInstance().mySignupsList.clear();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Farmer> farmerDbList = realm.where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYSIGNUPS.toString()).findAll();
        realm.beginTransaction();
        farmerDbList.deleteAllFromRealm();
        realm.commitTransaction();

//        realm.beginTransaction();
//        realm.delete(Farmer.class);
//        realm.commitTransaction();

        if(incompleteFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(approvedFarmersList);
        }

        for (int i = 0; i < Singleton.getInstance().mySignupsList.size(); i++) {
            // Save to DB
            realm.beginTransaction();
            final Farmer farmerDbObj = realm.createObject(Farmer.class);
            farmerDbObj.setAllValues(Singleton.getInstance().mySignupsList.get(i));
            realm.commitTransaction();
        }

        Intent resultsIntent=new Intent(Constants.MY_SIGNPS_BROADCAST_ACTION);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.sendBroadcast(resultsIntent);
    }

    private static String parseAboutMeDataAttachment(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0)
        {
            JSONObject facObj = arrRecords.getJSONObject(0);
            JSONObject attachmentObj = facObj.optJSONObject("Attachments");
            if(attachmentObj != null)
            {
                JSONArray attRecords = attachmentObj.getJSONArray("records");
                if(attRecords.length() > 0)
                {
                    JSONObject objFarmerPhoto = attRecords.getJSONObject(0);
                    String idAttachment = objFarmerPhoto.getString("Id");
                    return idAttachment;
                }
            }
        }
        return "";
    }

    private static void parseFarmerAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = Realm.getDefaultInstance();

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmerObj = arrRecords.getJSONObject(i);
                String id = farmerObj.getString("Id");

                String farmerPicId = "";
                String farmerNatId = "";

                JSONObject attachmentObj = farmerObj.optJSONObject("Attachments");
                if(attachmentObj != null)
                {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        String description = objAttachment.getString("Description").toLowerCase().trim();
                        if(description.contains("pic") || description.contains("photo"))
                        {
                            farmerPicId = objAttachment.getString("Id");
                        }
                        else if(description.contains("id"))
                        {
                            farmerNatId = objAttachment.getString("Id");
                        }
                    }
                }

                RealmResults<Farmer> farmerDbList = realm.where(Farmer.class).equalTo("farmerId", id).findAll();
                if(farmerDbList != null && farmerDbList.size() > 0)
                {
                    for (int k = 0; k < farmerDbList.size(); k++) {
                        realm.beginTransaction();
                        farmerDbList.get(k).setThumbAttachmentId(farmerPicId);
                        farmerDbList.get(k).setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmerDbList.get(k).getThumbAttachmentId());
                        if(!farmerPicId.isEmpty())
                        {
                            farmerDbList.get(k).setThumbUrl(thumbUrl);
                        }
                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmerDbList.get(k).getNationalCardAttachmentId());
                        if(!farmerNatId.isEmpty())
                        {
                            farmerDbList.get(k).setNationalCardUrl(natIdUrl);
                        }
                        realm.commitTransaction();

                        MyPicassoInstance.getInstance().load(thumbUrl).fetch(/*new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.i("Fetch success", "Farmer Pic: " + farmerDb.getThumbUrl());
                        }

                        @Override
                        public void onError() {
                            Log.i("Fetch failed", "Farmer Pic: " + farmerDb.getThumbUrl());
                        }
                    }*/);

                        MyPicassoInstance.getInstance().load(natIdUrl).fetch(/*new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.i("Fetch success", "NAT ID: " + farmerDb.getNationalCardUrl());
                        }

                        @Override
                        public void onError() {
                            Log.i("Fetch failed", "NAT ID: " + farmerDb.getNationalCardUrl());
                        }
                    }*/);

                    }
                }
            }

            Intent resultsIntent=new Intent(Constants.MY_SIGNPS_BROADCAST_ACTION);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
            localBroadcastManager.sendBroadcast(resultsIntent);
        }
    }

    private static void getFarmerTaskItems(){

        String farmingTaskIds = "";

        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            Farmer farmer = Singleton.getInstance().myFarmersList.get(i);

            if(farmer.getFarmerTasks().size() > 0){
                for (int j = 0; j < farmer.getFarmerTasks().size(); j++) {
                    String id = farmer.getFarmerTasks().get(j).getTaskId();
                    id = "'" + id + "'";

                    farmingTaskIds = farmingTaskIds + id;

                    if (i + 1 != Singleton.getInstance().myFarmersList.size() || j + 1 != farmer.getFarmerTasks().size()) {
                        farmingTaskIds = farmingTaskIds + ",";
                    }
                }
            }
        }

        WebApi.getFarmingTaskItems(farmerStatusCallback, farmingTaskIds);
    }

    private static void getTaskItemsAttachments() {
        String taskItemIds = "";

        for (int i = 0; i < Singleton.getInstance().taskItems.size(); i++) {

            TaskItem taskItem = Singleton.getInstance().taskItems.get(i);
            String id = taskItem.getId();
            id = "'" + id + "'";

            taskItemIds = taskItemIds + id;

            if (i + 1 != Singleton.getInstance().taskItems.size()) {
                taskItemIds = taskItemIds + ",";
            }

        }
        if (!taskItemIds.isEmpty()) {
            WebApi.getAllTaskItemAttachments(taskItemsAttachments, taskItemIds);
        }
    }

    private static Callback<ResponseBody> farmerStatusCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    ArrayList<TaskItem> allTaskItems = parseTaskItemData(responseStr);
                    Singleton.getInstance().taskItems = allTaskItems;
                    getTaskItemsAttachments();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static ArrayList<TaskItem> parseTaskItemData(String data) throws JSONException {

        Log.w("FENNEL", "DELETING TASK ITEMS");
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(TaskItem.class);
        realm.delete(TaskItemOption.class);
        realm.commitTransaction();


        ArrayList<TaskItem> taskItems = new ArrayList<>();

        // clear old lists
        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            if (Singleton.getInstance().myFarmersList.get(i).getFarmerTasks() != null) {

                for (int j = 0; j < Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().size(); j++) {

                    if (Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems() != null) {

                        realm.beginTransaction();
                        Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems().clear();
                        realm.commitTransaction();
                    }
                }
            }
        }

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject objTask = arrRecords.getJSONObject(i);

                String id = objTask.getString("Id");
                boolean isDone = objTask.getBoolean("Completed__c");
                String textValue = objTask.getString("Text_Value__c");
                int sequence = objTask.getInt("Sequence__c");
                String recordType = objTask.getJSONObject("RecordType").getString("Name");
                String name = objTask.getString("Name");
                double latitude = objTask.optDouble("Location__Latitude__s");
                if (Double.isNaN(latitude))
                    latitude = 0;
                double longitude = objTask.optDouble("Location__Longitude__s");
                if (Double.isNaN(longitude))
                    longitude = 0;
                String gpsTakenTime = objTask.getString("GPS_Taken_Time__c");
                String fileType = objTask.getString("File_Type__c");
                String fileActionType = objTask.getString("File_Action__c");
                String fileActionPerformed = objTask.getString("Action_Performed__c");
                String farmingTaskId = objTask.getString("Farming_Task__c");
                String description = objTask.getString("Description__c");

                RealmList<TaskItemOption> options = new RealmList<>();

                JSONObject objOptions = objTask.optJSONObject("Task_Item_Options__r");
                if (objOptions != null) {
                    JSONArray arrOptions = objOptions.getJSONArray("records");
                    for (int j = 0; j < arrOptions.length(); j++) {
                        JSONObject objOption = arrOptions.getJSONObject(j);
                        String optionId = objOption.getString("Id");
                        String optionName = objOption.getString("Name");
                        boolean isValue = objOption.getBoolean("Value__c");

                        realm.beginTransaction();
                        TaskItemOption taskItemOption = realm.createObject(TaskItemOption.class);
                        taskItemOption.setId(optionId);
                        taskItemOption.setName(optionName);
                        taskItemOption.setValue(isValue);
                        options.add(taskItemOption);
                        realm.commitTransaction();
                    }
                }
                realm.beginTransaction();
                TaskItem taskItem = realm.createObject(TaskItem.class);
                taskItem.setSequence(sequence);
                taskItem.setId(id);
                taskItem.setFarmingTaskId(farmingTaskId);
                taskItem.setName(name);
                taskItem.setRecordType(recordType);
                taskItem.setDescription(description);
                taskItem.setTextValue(textValue);
                taskItem.setFileType(fileType);
                taskItem.setFileActionType(fileActionType);
                taskItem.setGpsTakenTime(gpsTakenTime);
                taskItem.setLatitude(latitude);
                taskItem.setLongitude(longitude);
                taskItem.setOptions(options);
                taskItem.setAttachmentPath("");
                taskItem.setTaskDone(isDone);
                realm.commitTransaction();

                taskItems.add(taskItem);

                Log.i(TAG, "TaskItems1: " + taskItems.size());

                for (int j = 0; j < Singleton.getInstance().myFarmersList.size(); j++) {

                    if (Singleton.getInstance().myFarmersList.get(j).getFarmerTasks() != null) {

                        for (int k = 0; k < Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().size(); k++) {

                            if (taskItem.getFarmingTaskId().equalsIgnoreCase(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskId())) {
                                realm.beginTransaction();
                                if (Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems() == null) {
                                    Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).setTaskItems(new RealmList<TaskItem>());
                                }
                                Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems().add(taskItem);
                                realm.commitTransaction();
                            }
                        }
                    }
                }
            }
        }
        return taskItems;
    }

    public static boolean downloadAttachmentForAttachmentId(String attachmentId, Callback<ResponseBody> callback) {

        Call<ResponseBody> apiCall = Fennel.getWebService().downloadAttachmentForTask(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId);
        return processCall(apiCall, callback);
    }

    public static boolean getAllVisitLogsForMyLogbook(Callback<ResponseBody> allVisitLogsCallback, String farmingTaskIds) {
        String query = String.format(NetworkHelper.GET_ALL_LOGBOOK_DATA, farmingTaskIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, allVisitLogsCallback);
    }

    public static boolean getMyLogBookAttachments(Callback<ResponseBody> callback){

        String agentIds = "";

        for (int i = 0; i < Singleton.getInstance().fieldAgentsVisitLogs.size(); i++) {

            FieldAgent fieldAgent = Singleton.getInstance().fieldAgentsVisitLogs.get(i);

            if(!fieldAgent.isHeader() && !fieldAgent.getAgentId().isEmpty()){
                String id = fieldAgent.getAgentEmployeeId();

                id = "'" + id + "'";

                agentIds = agentIds + id;

                if(i+1 != Singleton.getInstance().fieldAgentsVisitLogs.size()){
                    agentIds = agentIds + ",";
                }
            }
        }
        if (agentIds.isEmpty())
            return false;

        String query = String.format(NetworkHelper.QUERY_MY_LOGBOOK_ATTACHMENTS, agentIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }

    public static boolean getMyDashboardAttachments(Callback<ResponseBody> callback){

        String agentIds = "";

        for (int i = 0; i < Singleton.getInstance().dashboardFieldAgents.size(); i++) {

            DashboardFieldAgent fieldAgent = Singleton.getInstance().dashboardFieldAgents.get(i);

            if(!fieldAgent.isHeader() && !fieldAgent.getAgentId().isEmpty()){
                String id = fieldAgent.getAgentEmployeeId();

                id = "'" + id + "'";

                agentIds = agentIds + id;

                if(i+1 != Singleton.getInstance().dashboardFieldAgents.size()){
                    agentIds = agentIds + ",";
                }
            }
        }

        if (agentIds.isEmpty()) {
            return false;
        }

        String query = String.format(NetworkHelper.QUERY_MY_LOGBOOK_ATTACHMENTS, agentIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }


    public interface OnSyncCompleteListener{
        void syncCompleted();
    }

    private static void parseMyLogbookFOFacData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String fieldOfficers = "";
        String facilitators = "";
        if (arrRecords.length() > 0) {

            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject fieldOfficerObj = arrRecords.getJSONObject(i);
                String foId = fieldOfficerObj.getString("Id");
                fieldOfficers = fieldOfficers + "'" + foId + "'";
                if (i < arrRecords.length() - 1) {
                    fieldOfficers = fieldOfficers + ",";
                }

                JSONObject facilitatorsObj = fieldOfficerObj.getJSONObject("Facilitators__r");
                JSONArray facilitatorsArray = facilitatorsObj.getJSONArray("records");

                if (facilitatorsArray.length() > 0) {

                    for (int j = 0; j < facilitatorsArray.length(); j++) {
                        JSONObject facilitatorObj = facilitatorsArray.getJSONObject(j);
                        String facId = facilitatorObj.getString("Id");
                        facilitators = facilitators + "'" + facId + "'";
                        if (i < arrRecords.length() - 1 || j < facilitatorsArray.length() - 1) {
                            facilitators = facilitators + ",";
                        }
                    }
                }
            }
        }
        WebApi.getMyLogbookData(logbookDataCallback, fieldOfficers, facilitators);
        WebApi.getMyDashboardData(dashboardDataCallback, fieldOfficers, facilitators);
    }

    private static void parseMyLogbookFacData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String facilitators = "";
        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject facilitatorsObj = arrRecords.getJSONObject(i);
                String facId = facilitatorsObj.getString("Id");
                facilitators = facilitators + "'" + facId + "'";
                if (i < arrRecords.length() - 1) {
                    facilitators = facilitators + ",";
                }
            }
        }
        WebApi.getMyLogbookData(logbookDataCallback, "", facilitators);
        WebApi.getMyDashboardData(dashboardDataCallback, "", facilitators);
    }

    static Callback<ResponseBody> dashboardDataCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyDashboardData(responseStr);
                        WebApi.getMyDashboardAttachments(myDashboardAttachmentCallback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMyDashboardData(String responseStr) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(DashboardFieldAgent.class);
        realm.delete(DashboardTask.class);
        realm.commitTransaction();

        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");
        HashMap<String, DashboardFieldAgent> dashboardAgents = null;
        ArrayList<DashboardFieldAgent> dashboardAgentsList = new ArrayList<>();

        if (arrRecords.length() > 0) {
            dashboardAgents = new HashMap<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                String id = null;
                String name = null;
                String employeeId = null;
                String phone = null;
                JSONObject employeeObj = null;
                String taskName = null;
                String taskId = null;
                String dueDate = null;
                String completionDate = null;
                String agentType = null;
                boolean isCompleted = false;

                int state = FARMING_STATE_ONTIME;

                DashboardFieldAgent dashboardFieldAgent = null;
                DashboardTask task = null;

                JSONObject farmingTaskObj = arrRecords.getJSONObject(i);

                String statusStr = farmingTaskObj.optString("Status__c");
                if (statusStr.equalsIgnoreCase(Constants.STR_COMPLETED)) {
                    isCompleted = true;
                }
                JSONObject shambaObj = farmingTaskObj.optJSONObject("Shamba__r");

                JSONObject facilitatorObj = shambaObj.optJSONObject("Facilitator_Signup__r");
                JSONObject fieldOfficerObj = shambaObj.optJSONObject("Field_Officer_Signup__r");
                JSONObject fieldManagerObj = shambaObj.optJSONObject("Field_Manager_Signup__r");

                String signupStatus = shambaObj.optString("Sign_Up_Status__c");

                if (signupStatus.equalsIgnoreCase(Constants.STR_APPROVED)) {

                    if (fieldManagerObj != null) {
                        id = fieldManagerObj.optString("Id");
                        name = fieldManagerObj.optString("Name");
                        phone = fieldManagerObj.optString("Phone__c");
                        employeeObj = fieldManagerObj.optJSONObject("Employee__r");
                        employeeId = employeeObj.optString("Name");
                        agentType = Constants.STR_FIELD_MANAGER;
                    } else if (fieldOfficerObj != null) {
                        id = fieldOfficerObj.optString("Id");
                        name = fieldOfficerObj.optString("Name");
                        phone = fieldOfficerObj.optString("Phone__c");
                        employeeObj = fieldOfficerObj.optJSONObject("Employee__r");
                        agentType = Constants.STR_FIELD_OFFICER;
                        employeeId = employeeObj.optString("Name");
                    } else if (facilitatorObj != null) {
                        id = facilitatorObj.optString("Id");
                        name = facilitatorObj.optString("Name");
                        phone = facilitatorObj.optString("Phone__c");
                        employeeId = facilitatorObj.optString("Employee_ID__c");
                        agentType = Constants.STR_FACILITATOR;
                    }
                    taskName = farmingTaskObj.optString("Name");
                    taskId = farmingTaskObj.optString("Id");
                    dueDate = farmingTaskObj.optString("Due_Date__c");
                    completionDate = farmingTaskObj.optString("Completion_Date__c");

                    SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date due = null;
                    Date completion = null;
                    try {
                        if (dueDate != null && !dueDate.equalsIgnoreCase("null"))
                            due = serverFormat.parse(dueDate);
                        if (completionDate != null && !completionDate.equalsIgnoreCase("null"))
                            completion = serverFormat.parse(completionDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if ((completion != null && completion.getTime() > due.getTime()) || (completion == null && !isCompleted && due.getTime() < System.currentTimeMillis())) {
                        state = Constants.FARMING_STATE_LATE;
                    }

                    if (taskName.equalsIgnoreCase("Task1")) {
                        Log.i("FENNEL", "Catch!");
                    }

                    RealmList<DashboardTask> dashboardTasks = null;

                    if (dashboardAgents.get(id) != null) {
                        dashboardFieldAgent = dashboardAgents.get(id);
                        dashboardTasks = dashboardFieldAgent.getDashboardTasks();
                        boolean isFound = false;
                        for (DashboardTask dashboardTask : dashboardTasks) {
                            if (dashboardTask.getTaskName().equalsIgnoreCase(taskName)) {
                                isFound = true;
                                realm.beginTransaction();
                                {
                                    dashboardTask.setTotalCount(dashboardTask.getTotalCount() + 1);
                                    if (state == Constants.FARMING_STATE_LATE)
                                        dashboardTask.setState(state);
                                    if (isCompleted) {
                                        dashboardTask.setCompleted(dashboardTask.getCompleted() + 1);
                                        realm.commitTransaction();
                                        break;
                                    }
                                }
                                realm.commitTransaction();
                            }
                        }
                        if (!isFound) {
                            realm.beginTransaction();
                            {
                                task = realm.createObject(DashboardTask.class);
                                task.setTaskName(taskName);
                                task.setTaskId(taskId);
                                task.setDueDate(dueDate);
                                task.setCompletionDate(completionDate);
                                task.setTotalCount(task.getTotalCount() + 1);
                                task.setState(state);

                                if (isCompleted) {
                                    task.setCompleted(task.getCompleted() + 1);
                                }
                                dashboardTasks.add(task);
                            }
                            realm.commitTransaction();
                        }
                    } else {
                        realm.beginTransaction();
                        {
                            dashboardFieldAgent = realm.createObject(DashboardFieldAgent.class);
                            dashboardFieldAgent.setAgentId(id);
                            dashboardFieldAgent.setAgentName(name);
                            dashboardFieldAgent.setAgentEmployeeId(employeeId);
                            dashboardFieldAgent.setAgentNumber(phone);
                            dashboardFieldAgent.setAgentType(agentType);

                            task = realm.createObject(DashboardTask.class);
                            task.setTaskName(taskName);
                            task.setTaskId(taskId);
                            task.setDueDate(dueDate);
                            task.setCompletionDate(completionDate);
                            task.setTotalCount(task.getTotalCount() + 1);
                            task.setState(state);
                            if (isCompleted) {
                                task.setCompleted(task.getCompleted() + 1);
                            }
                            dashboardTasks = dashboardFieldAgent.getDashboardTasks();
                            dashboardTasks.add(task);
                        }
                        realm.commitTransaction();

                        dashboardAgents.put(id, dashboardFieldAgent);
                        dashboardAgentsList.add(dashboardFieldAgent);
                    }
                }
            }
        }
        Singleton.getInstance().dashboardFieldAgents = dashboardAgentsList;
    }

    private static Callback<ResponseBody> myDashboardAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyDashboardAttachments(responseStr);
                    }catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMyDashboardAttachments(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = getDefaultInstance();

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject agentObj = arrRecords.getJSONObject(i);
                String id = agentObj.getString("Id");
                String name = agentObj.getString("Name");
                String agentAttachmentId  = null;

                JSONObject attachmentObj = agentObj.optJSONObject("Attachments");
                if(attachmentObj != null)
                {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        agentAttachmentId = objAttachment.getString("Id");
                    }
                }

                ArrayList<DashboardFieldAgent> allAgentsList = Singleton.getInstance().dashboardFieldAgents;

                for (int j = 0; j < allAgentsList.size(); j++) {
                    final DashboardFieldAgent fieldAgent = allAgentsList.get(j);

                    if(fieldAgent.getAgentEmployeeId().equalsIgnoreCase(name))
                    {
                        String attachmentUrl = NetworkHelper.makeAttachmentUrlFromId(agentAttachmentId);

                        realm.beginTransaction();
                        fieldAgent.setAgentAttachmentUrl(agentAttachmentId);
                        realm.commitTransaction();

                        MyPicassoInstance.getInstance().load(attachmentUrl).fetch();
                        break;
                    }
                }
            }
        }
    }

    private static Callback<ResponseBody> logbookDataCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyLogbookData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMyLogbookData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String farmingTaskIds = "";
        if (arrRecords.length() > 0) {
            visitLogFarmingTasks = new HashMap<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                Map<String, String> taskMap = new HashMap<>();
                JSONObject farmingTaskObj = arrRecords.getJSONObject(i);

                String taskId = farmingTaskObj.getString("Farming_Task__c");
                JSONObject farmVisit = farmingTaskObj.getJSONObject("Farm_Visit__r");
                JSONObject fieldManager = farmVisit.optJSONObject("Field_Manager__r");
                JSONObject fieldOfficer = farmVisit.optJSONObject("Field_Officer__r");
                JSONObject facilitator = farmVisit.optJSONObject("Facilitator__r");
                String name = null;
                String agentType = null;
                String agentId = null;
                String agentPhone = null;
                String agentEmployeeId = null;
                JSONObject employeeObj = null;

                if (fieldManager != null) {
                    name = fieldManager.getString("Name");
                    agentType = Constants.STR_FIELD_MANAGER;
                    agentId = fieldManager.getString("Id");
                    agentPhone = fieldManager.optString("Phone__c");
                    employeeObj = fieldManager.optJSONObject("Employee__r");
                    agentEmployeeId = employeeObj.optString("Name");
                } else if (fieldOfficer != null) {
                    name = fieldOfficer.getString("Name");
                    agentType = Constants.STR_FIELD_OFFICER;
                    agentId = fieldOfficer.getString("Id");
                    agentPhone = fieldOfficer.optString("Phone__c");
                    employeeObj = fieldOfficer.optJSONObject("Employee__r");
                    agentEmployeeId = employeeObj.optString("Name");
                } else if (facilitator != null) {
                    name = facilitator.getString("Name");
                    agentType = STR_FACILITATOR;
                    agentId = facilitator.getString("Id");
                    agentPhone = facilitator.optString("Phone__c");
                    agentEmployeeId = facilitator.optString("Employee_ID__c");
                }
                taskMap.put("agentName", name);
                taskMap.put("agentType", agentType);
                taskMap.put("Id", taskId);
                taskMap.put("agentId", agentId);
                taskMap.put("agentPhone", agentPhone);
                taskMap.put("agentEmployeeId", agentEmployeeId);
                visitLogFarmingTasks.put(taskId, taskMap);
                farmingTaskIds = farmingTaskIds + "'" + taskId + "'";
                if (i < arrRecords.length() - 1) {
                    farmingTaskIds = farmingTaskIds + ",";
                }
            }
        }
        if (farmingTaskIds != null && !farmingTaskIds.isEmpty()) {
            WebApi.getAllVisitLogsForMyLogbook(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            String responseStr = "";

                            try {
                                responseStr = response.body().string();
                                parseAllVisitLogsDataForLogbook(responseStr);
                                WebApi.getMyLogBookAttachments(myLogbookAttachmentCallback);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            }, farmingTaskIds);
        }
    }

    private static void parseAllVisitLogsDataForLogbook(String responseStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");
        ArrayList<Task> allTasks = null;

        if (arrRecords.length() > 0) {
            allTasks = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject farmingTaskObj = (JSONObject) arrRecords.get(i);
                String farmingTaskId = farmingTaskObj.getString("Id");
                Map<String, String> taskMap = visitLogFarmingTasks.get(farmingTaskId);

                Task visitLogTask = new Task();
                visitLogTask.setAgentId(taskMap.get("agentId"));
                visitLogTask.setAgentName(taskMap.get("agentName"));
                visitLogTask.setAgentType(taskMap.get("agentType"));
                visitLogTask.setAgentPhoneNumber(taskMap.get("agentPhone"));
                visitLogTask.setAgentEmployeeId(taskMap.get("agentEmployeeId"));
                visitLogTask.setTaskId(taskMap.get("Id"));
                visitLogTask.setStatus(farmingTaskObj.optString("Status__c"));
                visitLogTask.setStartedDate(farmingTaskObj.optString("Started_Date__c"));
                visitLogTask.setName(farmingTaskObj.optString("Name"));
                visitLogTask.setDueDate(farmingTaskObj.optString("Due_Date__c"));
                visitLogTask.setCompletionDate(farmingTaskObj.optString("Completion_Date__c"));


                JSONObject shambaObj = (JSONObject) farmingTaskObj.getJSONObject("Shamba__r");
                String shambaName = shambaObj.optString("Village_Name__c");
                if (shambaName != null) {
                    visitLogTask.setShambaName(shambaName);
                }
                String farmerName = shambaObj.getJSONObject("Farmer__r").getString("FullName__c");
                visitLogTask.setFarmerName(farmerName);

                JSONObject taskItemObj = farmingTaskObj.optJSONObject("Task_Items__r");
                if (taskItemObj != null) {
                    JSONArray taskItemRecords = taskItemObj.getJSONArray("records");
                    if (taskItemRecords.length() > 0) {
                        for (int j = 0; j < taskItemRecords.length(); j++) {

                            JSONObject taskItem = (JSONObject) taskItemRecords.get(j);
                            String id = taskItem.optString("Id");
                            String textValue = taskItem.optString("Text_Value__c");
                            int sequence = taskItem.optInt("Sequence__c");
                            String recordType = taskItem.optJSONObject("RecordType").getString("Name");
                            String name = taskItem.optString("Name");
                            double latitude = taskItem.optDouble("Location__Latitude__s");
                            if (Double.isNaN(latitude))
                                latitude = 0;
                            double longitude = taskItem.optDouble("Location__Longitude__s");
                            if (Double.isNaN(longitude))
                                longitude = 0;
                            String lastModifiedDate = taskItem.optString("LastModifiedDate");
                            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                            Date lastModified = null;
                            try {
                                lastModified = serverFormat.parse(lastModifiedDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String gpsTakenTime = taskItem.getString("GPS_Taken_Time__c");
                            String fileType = taskItem.getString("File_Type__c");
                            String fileActionType = taskItem.getString("File_Action__c");
                            String fileActionPerformed = taskItem.optString("Action_Performed__c");
                            String description = taskItem.getString("Description__c");

                            RealmList<TaskItemOption> options = new RealmList<>();

                            TaskItem newTaskItem = new TaskItem(sequence, id, taskMap.get("Id"), name, recordType, description, textValue, fileType, fileActionType, fileActionPerformed, gpsTakenTime, latitude, longitude, options, lastModified, visitLogTask.getAgentName(), farmerName, null, false, "", "", false, false);
                            visitLogTask.getTaskItems().add(newTaskItem);
                        }
                    }
                    allTasks.add(visitLogTask);
                }
            }
            parseVisitLogsFromTasks(allTasks);
        }
    }
    private static void parseVisitLogsFromTasks(ArrayList<Task> allTasks) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(FieldAgent.class);
        realm.commitTransaction();

        realm.beginTransaction();
        Map<String, FieldAgent> agentsMap = new HashMap<>();
        ArrayList<FieldAgent> allAgentsList = new ArrayList<>();

        for (Task taskObj : allTasks) {
            FieldAgent agent = null;
            String agentId = taskObj.getAgentId();
            if (agentsMap.get(agentId) != null) {
                agent = agentsMap.get(agentId);
            } else {
                agent = realm.createObject(FieldAgent.class);
                agent.setAgentId(taskObj.getAgentId());
                agent.setName(taskObj.getAgentName());
                agent.setAgentType(taskObj.getAgentType());
                agent.setAgentEmployeeId(taskObj.getAgentEmployeeId());
                agentsMap.put(agentId, agent);
                allAgentsList.add(agent);
            }
            RealmList<TaskItem> allItems = taskObj.getTaskItems();
            for (TaskItem item : allItems) {
                item.setAgentName(taskObj.getAgentName());

                LogTaskItem taskItem = realm.createObject(LogTaskItem.class);
                taskItem.setSequence(item.getSequence());
                taskItem.setId(item.getId());
                taskItem.setFarmingTaskId(item.getFarmingTaskId());
                taskItem.setName(item.getName());
                taskItem.setRecordType(item.getRecordType());
                taskItem.setDescription(item.getDescription());
                taskItem.setTextValue(item.getTextValue());
                taskItem.setFileType(item.getFileType());
                taskItem.setFileActionType(item.getFileActionType());
                taskItem.setFileActionPerformed(item.getFileActionPerformed());
                taskItem.setGpsTakenTime(item.getGpsTakenTime());
                taskItem.setLatitude(item.getLatitude());
                taskItem.setLongitude(item.getLongitude());
                taskItem.setOptions(item.getOptions());
                taskItem.setDateModified(item.getDateModified());
                taskItem.setAgentName(item.getAgentName());
                taskItem.setFarmerName(item.getFarmerName());
                taskItem.setAgentAttachmentId(item.getAgentAttachmentId());
                taskItem.setTaskDone(item.isTaskDone());
                taskItem.setAttachmentPath(item.getAttachmentPath());
                agent.getVisitLogs().add(taskItem);
                Log.i(TAG, "TaskItems2: " + agent.getVisitLogs().size());
            }
        }
        realm.commitTransaction();
        Singleton.getInstance().fieldAgentsVisitLogs = allAgentsList;
    }



    private static Callback<ResponseBody> myLogbookAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyLogbookAttachments(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private static void parseMyLogbookAttachments(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = getDefaultInstance();

        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject agentObj = arrRecords.getJSONObject(i);
                String id = agentObj.getString("Id");
                String name = agentObj.getString("Name");
                String agentAttachmentId = null;

                JSONObject attachmentObj = agentObj.optJSONObject("Attachments");
                if (attachmentObj != null) {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        agentAttachmentId = objAttachment.getString("Id");
                    }
                }

                ArrayList<FieldAgent> allAgentsList = Singleton.getInstance().fieldAgentsVisitLogs;

                for (int j = 0; j < allAgentsList.size(); j++) {
                    final FieldAgent fieldAgent = allAgentsList.get(j);

                    if (fieldAgent.getAgentEmployeeId().equalsIgnoreCase(name)) {
                        String attachmentUrl = NetworkHelper.makeAttachmentUrlFromId(agentAttachmentId);

                        realm.beginTransaction();

                        fieldAgent.setAgentAttachmentUrl(agentAttachmentId);
                        for (LogTaskItem item : fieldAgent.getVisitLogs()) {
                            item.setAgentAttachmentId(agentAttachmentId);
                        }

                        realm.commitTransaction();

                        MyPicassoInstance.getInstance().load(attachmentUrl).fetch();
                        break;
                    }
                }
            }

//            tasksAdapter.notifyDataSetChanged();
        }
    }



    private static void parseAllTasksAttachments(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject taskAttachmentRecord = arrRecords.getJSONObject(i);
                final String taskItemId = taskAttachmentRecord.getString("Id");
                final String fileType = taskAttachmentRecord.optString("File_Type__c");
                JSONObject attachments = taskAttachmentRecord.optJSONObject("Attachments");
                if (attachments != null && fileType != null) {

                    JSONArray attRecords = attachments.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject attachmentObj = attRecords.getJSONObject(j);
                        final String attachmentId = attachmentObj.getString("Id");
                        final String attachmentParentId = attachmentObj.getString("ParentId");
                        final String filename = attachmentObj.optString("Name");
                        String[] separated = filename.split("\\.");
                        final String ext = separated[separated.length - 1];

                        Call<ResponseBody> apiCall = Fennel.getWebService().downloadAttachmentForTask(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId);
                        try {
//                            if (getActivity() == null)
//                                Fennel.initWebApi();
//                            if (NetworkHelper.isNetAvailable(getActivity())) {
                                apiCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.code() == Constants.RESPONSE_SUCCESS) {
                                            String attachmentName = attachmentId + "." + ext;
                                            try {
                                                File path = Environment.getExternalStorageDirectory();
                                                File folder = new File(path + "/" + "Fennel", "TaskAttachments");
                                                if (!folder.exists()) {
                                                    folder.mkdirs();
                                                }
                                                File file = new File(Environment.getExternalStorageDirectory() + "/Fennel/TaskAttachments", attachmentName);
//                                                File file = new File(path, attachmentName);
                                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                try {
                                                    // Writes bytes from the specified byte array to this file output stream
                                                    fileOutputStream.write(response.body().bytes());
                                                } catch (FileNotFoundException e) {
                                                    System.out.println("File not found" + e);
                                                } catch (IOException ioe) {
                                                    System.out.println("Exception while writing file " + ioe);
                                                } finally {
                                                    // close the streams using close method
                                                    try {
                                                        if (fileOutputStream != null) {
                                                            fileOutputStream.close();
                                                            updateTaskItemWithAttachment(taskItemId, file.getAbsolutePath(), attachmentId);
                                                        } else {
                                                            file.delete();
                                                        }
                                                    } catch (IOException ioe) {
                                                        System.out.println("Error while closing stream: " + ioe);
                                                    }
                                                }
//                                                Log.i("FENNEL", "Write Success!");
                                            } catch (IOException e) {
                                                Log.e("FENNEL", "Error while writing file!");
                                                Log.e("FENNEL", e.toString());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        System.out.println(t.toString());
                                    }
                                });
//                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void updateTaskItemWithAttachment(String taskItemId, String filePath, String attachmentId) {
        ArrayList<TaskItem> allTaskItems = Singleton.getInstance().taskItems;
        if (allTaskItems == null)
            return;
        for (TaskItem taskItem : allTaskItems) {
            if (taskItem.isValid() && taskItem.getId().equals(taskItemId)) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                String uri = NetworkHelper.getUriFromPath(filePath);
                taskItem.setAttachmentId(attachmentId);
                taskItem.setAttachmentPath(uri);
                realm.commitTransaction();

                break;
            }
        }
    }

    private static Callback<ResponseBody> taskItemsAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseAllTasksAttachments(responseStr);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };


}
