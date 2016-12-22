package wal.fennel.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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
import wal.fennel.models.ResponseModel;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PhotoUtils;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;

/**
 * Created by Khawar on 21/10/2016.
 */
public class WebApi {

    private static RealmList<Farmer> pendingFarmersList = new RealmList<>();

    private static Context mContext = null;
    private static WebApi sInstance = null;

    private static int countFailedCalls = 0;
    private static int countCalls = 0;

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
                if(farmer.isFarmerPicDirty())
                    countToMinus++;
                if(farmer.isNatIdCardDirty())
                    countToMinus++;
                countCalls = countCalls - countToMinus;
                pendingFarmersList.add(farmer);
                checkSyncComplete();
            }
            else {
                arrFarmerIds.add(farmer.getIdNumber());

                final HashMap<String, Object> farmerMap = getFarmerMap(farmer);
                if(farmer.farmerId.isEmpty() || farmer.farmerId.startsWith(Constants.STR_FARMER_ID_PREFIX)){
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
                                            FennelUtils.appendDebugLog("Existing farmer ID: " + farmer.getIdNumber());
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(response.code() == 401)
                            {
                                FennelUtils.appendDebugLog("Farmer create Session Expired, redirected: " + farmer.getIdNumber());
                                countFailedCalls++;
                                sessionExpireRedirect();
                            }
                            else if (!newFarmerId.isEmpty() || ((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().success == true)) {

                                if(newFarmerId.isEmpty() && response.body() != null){
                                    newFarmerId = response.body().id;
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
                                }
                                else {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    farmer.farmerId = newFarmerId;
                                    realm.commitTransaction();

                                    FennelUtils.appendDebugLog("Farmer create finished: " + farmer.getIdNumber());

                                    addFarmWithFarmerId(farmer);

                                    checkSyncComplete();

                                    if(farmer.isFarmerPicDirty)
                                        attachFarmerImageToFarmerObject(farmer);
                                    if(farmer.isNatIdCardDirty)
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
                }
                else {
                    WebApi.editFarmer(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            countCalls--;

                            FennelUtils.appendDebugLog("Farmer edit response: " + farmer.getIdNumber() + " - " + response.code());

                            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                                FennelUtils.appendDebugLog("Farmer edit finished: " + farmer.getIdNumber());

                                if(farmer.farmId.isEmpty() || farmer.farmId == null){
                                    addFarmWithFarmerId(farmer);
                                } else {
                                    editFarmWithFarmId(farmer);
                                }

                                checkSyncComplete();

                                if(farmer.isFarmerPicDirty)
                                    attachFarmerImageToFarmerObject(farmer);
                                if(farmer.isNatIdCardDirty)
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
                    }, farmer.farmerId, farmerMap);
                }
            }
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
        newFarmerMap.put("First_Name__c", farmer.firstName);
        newFarmerMap.put("Middle_Name__c", farmer.secondName);
        newFarmerMap.put("Last_Name__c", farmer.surname);
        newFarmerMap.put("Name", farmer.idNumber);
        newFarmerMap.put("Mobile_Number__c", farmer.mobileNumber);
        newFarmerMap.put("Gender__c", farmer.gender.trim().equalsIgnoreCase("male") ? "Male" : "Female");
        newFarmerMap.put("Leader__c", farmer.isLeader ? 1 : 0);

        return newFarmerMap;
    }

    private static HashMap<String, Object> getFarmMap(Farmer farmer) {

        final HashMap<String, Object> newFarmMap = new HashMap<>();
        newFarmMap.put("LocationLookup__c", farmer.locationId);
        newFarmMap.put("Sub_LocationLookup__c", farmer.subLocationId);
        newFarmMap.put("Village__c", farmer.villageId);
        newFarmMap.put("Tree_Specie__c", farmer.treeSpeciesId);
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

        if (farmer.signupStatus != null && !farmer.signupStatus.isEmpty()) {
            newFarmMap.put("Sign_Up_Status__c", farmer.signupStatus);
        }

        return newFarmMap;
    }

    private static void addFarmWithFarmerId(final Farmer farmer) {
        HashMap<String, Object> farmMap = getFarmMap(farmer);
        farmMap.put("Farmer__c", farmer.farmerId);
//        countCalls++;
        WebApi.createFarm(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                countCalls--;
                if (response.body() != null && response.body().success == true) {
                    Log.i(farmer.getFullName(), "Farm Synced" );
                    FennelUtils.appendDebugLog("Farm created: " + farmer.getIdNumber());
                    checkSyncComplete();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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
                FennelUtils.appendDebugLog("Farm create Sync Failed: " + t.getMessage());
                t.printStackTrace();
            }
        }, farmMap);
    }

    private static void editFarmWithFarmId(final Farmer farmer) {

        HashMap<String, Object> farmMap = getFarmMap(farmer);
        farmMap.put("Farmer__c", farmer.farmerId);
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
                    final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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
        }, farmer.farmId, farmMap);
    }

    private static void attachFarmerImageToFarmerObject(final Farmer farmer) {

        if (farmer.thumbUrl == null || farmer.thumbUrl.isEmpty()) {
            countCalls--;
            countFailedCalls++;
            return;
        }

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "picture");
        attachmentMap.put("Name", "profile_picture.png");
        if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.farmerId);
        }
        else
        {
            MyPicassoInstance.getInstance().invalidate(farmer.getThumbUrl());
        }

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        String imagePath = NetworkHelper.getUploadPathFromUri(farmer.thumbUrl);
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
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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
                            Singleton.getInstance().farmerIdtoInvalidate = farmer.farmerId;
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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

        if (farmer.nationalCardUrl == null || farmer.nationalCardUrl.isEmpty()) {
            countCalls--;
            countFailedCalls++;
            return;
        }

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "ID");
        attachmentMap.put("Name", "national_id.png");
        if(farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.farmerId);
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

        String imagePath = NetworkHelper.getUploadPathFromUri(farmer.nationalCardUrl);
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
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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
                            final Farmer farmerDbObj = realm.where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
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

                PreferenceHelper.getInstance().writeIsSyncInProgress(false);
            }
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

                Farmer farmer = new Farmer(lastModifiedDate, id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, locationId, subLocation, subLocationId, village, villageId, tree, treeId, isFarmerHome, mobileNumber, "", "", status, false, "", "");

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
        realm.beginTransaction();
        realm.delete(Farmer.class);
        realm.commitTransaction();

        if(incompleteFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", ""));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", ""));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", ""));
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

    public interface OnSyncCompleteListener{
        void syncCompleted();
    }
}
