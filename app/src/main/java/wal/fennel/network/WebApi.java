package wal.fennel.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import wal.fennel.models.Farmer;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.ResponseModel;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.models.TaskItemOption;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PhotoUtils;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;

import static io.realm.Realm.getDefaultInstance;

/**
 * Created by Khawar on 21/10/2016.
 */
public class WebApi {

    private static Context mContext = null;
    private static WebApi sInstance = null;

    private static int countCalls = 0;
    private static int countFailedCalls = 0;

    private OnSyncCompleteListener onSyncCompleteListener;

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
        } else if (userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA_FIELD_OFFICER, PreferenceHelper.getInstance().readLoginUserId(), facilitators);
        } else {
            query = String.format(NetworkHelper.GET_MY_LOGBOOK_DATA_FACILITATOR, PreferenceHelper.getInstance().readLoginUserId());
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

    public static boolean getAllTaskItemAttachments(Callback<ResponseBody> callback){
        String query = NetworkHelper.QUERY_TASK_ITEM_ATTACHMENTS;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
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

//        countCalls = 0;
        countCalls = getTotalSyncCallCount();
        countFailedCalls = 0;

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
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
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
                            PreferenceHelper.getInstance().clearSession(false);
                            Intent intent = new Intent(mContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            Log.i("Fennel", "facilitator profile picture edited successfully!");

                            String newUrl = NetworkHelper.makeAttachmentUrlFromId(PreferenceHelper.getInstance().readAboutAttId());
                            PreferenceHelper.getInstance().writeAboutAttUrl(newUrl);
                            MyPicassoInstance.getInstance().invalidate(newUrl);

                        } else if (response.code() == 401) {
                            countFailedCalls++;
                            PreferenceHelper.getInstance().clearSession(false);
                            Intent intent = new Intent(mContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            countFailedCalls++;
                            Log.i("Fennel", "facilitator profile picture edit failed!");
                        }
                        checkSyncComplete();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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
        for (int i = 0; i < farmerDbList.size(); i++) {

            final Farmer farmer = farmerDbList.get(i);

            final HashMap<String, Object> farmerMap = getFarmerMap(farmer);
//            countCalls++;
            if(farmer.getFarmerId().isEmpty() || farmer.getFarmerId().startsWith(Constants.STR_FARMER_ID_PREFIX)){
                WebApi.createFarmer(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        countCalls--;

                        String errorMessage = "";
                        if(response.code() == 400 || response.errorBody() != null) {
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

                        if(response.code() == 401)
                        {
                            countFailedCalls++;
                            PreferenceHelper.getInstance().clearSession(false);
                            Intent intent = new Intent(mContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                        else if (((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().isSuccess() == true) || !errorMessage.isEmpty()) {

                            String newFarmerId = "";

                            if(!errorMessage.isEmpty()) {
                                Matcher m = Pattern.compile("\\((.*?)\\)").matcher(errorMessage);
                                while(m.find()) {
                                    newFarmerId = m.group(1);
                                }
                            }

                            if(newFarmerId.isEmpty()){

                                try {
                                    newFarmerId = response.body().getId();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                    String errorCrash = e.getMessage();

                                    if(errorMessage.isEmpty()){
                                        if(response.errorBody() != null){
                                            try {
                                                errorCrash = errorCrash + ": " + response.errorBody().string();
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                    else {
                                        errorCrash = errorCrash + ": " + errorMessage;
                                    }

                                    Exception ee = new Exception("Farmer Create failed - " + errorCrash);
                                    Crashlytics.logException(ee);
                                }
                            }

                            if(!newFarmerId.isEmpty()){
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                farmer.setFarmerId(newFarmerId);
                                realm.commitTransaction();

                                addFarmWithFarmerId(farmer);

                                checkSyncComplete();

                                if(farmer.isFarmerPicDirty())
                                    attachFarmerImageToFarmerObject(farmer);
                                if(farmer.isNatIdCardDirty())
                                    attachFarmerIDImageToFarmerObject(farmer);
                            }
                        }else {
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        t.printStackTrace();
                        checkSyncComplete();
                    }
                }, farmerMap);
            }
            else {
                WebApi.editFarmer(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        countCalls--;
                        if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                            editFarmWithFarmId(farmer);

                            checkSyncComplete();

                            if(farmer.isFarmerPicDirty())
                                attachFarmerImageToFarmerObject(farmer);
                            if(farmer.isNatIdCardDirty())
                                attachFarmerIDImageToFarmerObject(farmer);
                        }
                        else if(response.code() == 401)
                        {
                            countFailedCalls++;
                            PreferenceHelper.getInstance().clearSession(false);
                            Intent intent = new Intent(mContext, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        countCalls--;
                        countFailedCalls++;
                        t.printStackTrace();
                        checkSyncComplete();
                    }
                }, farmer.getFarmerId(), farmerMap);
            }
        }
        //endregion

        PreferenceHelper.getInstance().writeIsSyncInProgress(true);
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
                    checkSyncComplete();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
                    farmerDbObj.setDataDirty(false);
                    realm.commitTransaction();
                }
                else if(response.code() == 401)
                {
                    countFailedCalls++;
                    PreferenceHelper.getInstance().clearSession(false);
                    Intent intent = new Intent(mContext, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                countCalls--;
                countFailedCalls++;
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
                if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
                    farmerDbObj.setDataDirty(false);
                    realm.commitTransaction();

                    checkSyncComplete();
                }
                else if(response.code() == 401)
                {
                    countFailedCalls++;
                    PreferenceHelper.getInstance().clearSession(false);
                    Intent intent = new Intent(mContext, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                countCalls--;
                countFailedCalls++;
                t.printStackTrace();
                checkSyncComplete();
            }
        }, farmer.getFarmId(), farmMap);
    }

    private static void attachFarmerImageToFarmerObject(final Farmer farmer) {

        if (farmer.getThumbUrl() == null || farmer.getThumbUrl().isEmpty()) {
            countCalls--;
            countFailedCalls++;
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
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
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
                        final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
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
        }
    }

    private static void attachFarmerIDImageToFarmerObject(final Farmer farmer) {

        if (farmer.getNationalCardUrl() == null || farmer.getNationalCardUrl().isEmpty()) {
            countCalls--;
            countFailedCalls++;
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
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
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
                        final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.getFarmerId()).findFirst();
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
        }
    }

    private static void checkSyncComplete(){
        if(countCalls == 0) {
            if(countFailedCalls == 0){
                Log.i("Sync process: ", "Sync completed");
            }
            else{
                Log.i("Sync process: ", "Sync finished, but some records failed to sync");
            }
            saveSyncTimeStamp();
            getFullServerData();
            PreferenceHelper.getInstance().writeSessionExpiredSyncReq(false);
            if(WebApi.getInstance().onSyncCompleteListener != null) {
                WebApi.getInstance().onSyncCompleteListener.syncCompleted();
            }

            PreferenceHelper.getInstance().writeIsSyncInProgress(false);
        }
    }

    public static boolean isSyncRequired(){
        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("isDataDirty", true).or().equalTo("isFarmerPicDirty", true).or().equalTo("isNatIdCardDirty", true).findAll();
        if(PreferenceHelper.getInstance().readAboutIsSyncReq() || farmerDbList.size() > 0)
            return true;
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
                            WebApi.getMyFarmerAttachments(myFarmerTasksAttachments);
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

                        Farmer farmerDb = realm.where(Farmer.class).equalTo("farmerId", id).findFirst();
                        if(farmerDb != null)
                        {
                            realm.beginTransaction();
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
                            realm.commitTransaction();
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
    }

    private static void parseMyFarmersData(String data) throws JSONException {

        Log.i("FENNEL", data);
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Map<String,Farmer> farmersMap = new HashMap<>();
        Map<String,Task> tasksMap = new HashMap<>();
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

                Task currentTask;
                if (tasksMap.containsKey(taskName)) {
                    currentTask = (Task) tasksMap.get(taskName);
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    currentTask = realm.createObject(Task.class);
                    currentTask.setTaskId(id);
                    currentTask.setName(taskName);
                    currentTask.setStartedDate(startedDate);
                    currentTask.setCompletionDate(completionDate);
                    currentTask.setDueDate(dueDate);
                    currentTask.setStatus(status);

                    realm.commitTransaction();
//                    currentTask = new Tasks(id, taskName, startedDate, completionDate, dueDate, status);
                    tasksMap.put(taskName, currentTask);
                }

                Farmer currentFarmer;
                RealmList<Task> farmingTasks;
                if (farmersMap.containsKey(farmerIdNumber)) {
                    currentFarmer = (Farmer) farmersMap.get(farmerIdNumber);
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

                    farmersMap.put(farmerIdNumber, currentFarmer);
                }

                if (!(farmersTaskList.contains(currentFarmer))) {
                    farmersTaskList.add(currentFarmer);
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

                Farmer farmer = new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, locationId, subLocation, subLocationId, village, villageId, tree, treeId, isFarmerHome, mobileNumber, "", "", status, false, "", "", null, Constants.FarmerType.MYSIGNUPS);

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
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
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

                    if(j + 1 != farmer.getFarmerTasks().size()){
                        farmingTaskIds = farmingTaskIds + ",";
                    }
                }
            }
        }

        WebApi.getFarmingTaskItems(farmerStatusCallback, farmingTaskIds);
    }

    private static Callback<ResponseBody> farmerStatusCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseTaskItemData(responseStr);
                    Log.i("Parsing" , "Complete" );
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

    private static void parseTaskItemData(String data) throws JSONException {

        // clear old lists
        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            if(Singleton.getInstance().myFarmersList.get(i).getFarmerTasks() != null){

                for (int j = 0; j < Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().size(); j++) {

                    if(Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems() != null){

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems().clear();
                        realm.commitTransaction();
                    }
                }
            }
        }

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        for (int i = 0; i < arrRecords.length(); i++) {

            JSONObject objTask = arrRecords.getJSONObject(i);

            String id = objTask.getString("Id");
            String textValue = objTask.getString("Text_Value__c");
            int sequence = objTask.getInt("Sequence__c");
            String recordType = objTask.getJSONObject("RecordType").getString("Name");
            String name = objTask.getString("Name");
            double latitude = objTask.optDouble("Location__Latitude__s");
            if(Double.isNaN(latitude))
                latitude = 0;
            double longitude = objTask.optDouble("Location__Longitude__s");
            if(Double.isNaN(longitude))
                longitude = 0;
            String gpsTakenTime = objTask.getString("GPS_Taken_Time__c");
            String fileType = objTask.getString("File_Type__c");
            String fileAction = objTask.optString("File_Action__c");
            String farmingTaskId = objTask.getString("Farming_Task__c");
            String description = objTask.getString("Description__c");

            RealmList<TaskItemOption> options = new RealmList<>();

            JSONObject objOptions = objTask.optJSONObject("Task_Item_Options__r");
            if(objOptions != null){
                JSONArray arrOptions = objOptions.getJSONArray("records");
                for (int j = 0; j < arrOptions.length(); j++) {
                    JSONObject objOption = arrOptions.getJSONObject(j);
                    String optionId = objOption.getString("Id");
                    String optionName = objOption.getString("Name");
                    boolean isValue = objOption.getBoolean("Value__c");

                    options.add(new TaskItemOption(optionId, optionName, isValue));
                }
            }

            TaskItem taskItem = new TaskItem(sequence, id, farmingTaskId, name, recordType, description, textValue, fileType, fileAction, gpsTakenTime, latitude, longitude, options, null, null, null, null);

            for (int j = 0; j < Singleton.getInstance().myFarmersList.size(); j++) {

                if(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks() != null){

                    for (int k = 0; k < Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().size(); k++) {

                        if (taskItem.getFarmingTaskId().equalsIgnoreCase(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskId())){
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            if(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems() == null)
                                Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).setTaskItems(new RealmList<TaskItem>());
                            Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems().add(taskItem);
                            realm.commitTransaction();
                        }
                    }
                }
            }
        }
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

                if(i+1 != Singleton.getInstance().myFarmersList.size()){
                    agentIds = agentIds + ",";
                }
            }
        }

        String query = String.format(NetworkHelper.QUERY_MY_LOGBOOK_ATTACHMENTS, agentIds);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        return processCall(apiCall, callback);
    }


    public interface OnSyncCompleteListener{
        void syncCompleted();
    }
}
