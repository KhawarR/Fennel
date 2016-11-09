package wal.fennel.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.LoginActivity;
import wal.fennel.application.Fennel;
import wal.fennel.datamodels.Auth;
import wal.fennel.models.Farm;
import wal.fennel.models.Farmer;
import wal.fennel.models.ResponseModel;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PhotoUtils;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;

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

    public static boolean getMyFarmerAttachments(Callback<ResponseBody> callback){
        String query = NetworkHelper.QUERY_MY_SIGNUPS_ATTACHMENTS;
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

    public static void syncAll(OnSyncCompleteListener onSyncCompleteListener){

        countCalls = 0;
        countFailedCalls = 0;

        WebApi.getInstance().onSyncCompleteListener = onSyncCompleteListener;

        //region About Me portion
        if(PreferenceHelper.getInstance().readAboutIsSyncReq()){
            String imagePath = NetworkHelper.getUploadPathFromUri(PreferenceHelper.getInstance().readAboutAttUrl());
            countCalls++;

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
                            PreferenceHelper.getInstance().clearSession();
                            Intent intent = new Intent(mContext, LoginActivity.class);
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
                            PreferenceHelper.getInstance().clearSession();
                            Intent intent = new Intent(mContext, LoginActivity.class);
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
            countCalls++;
            if(farmer.farmerId.isEmpty() || farmer.farmerId.startsWith(Constants.STR_FARMER_ID_PREFIX)){
                WebApi.createFarmer(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        countCalls--;
                        if ((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().success == true) {

                            String newFarmerId = response.body().id;

                            Realm realm = Realm.getDefaultInstance();
                            Realm.getDefaultInstance().beginTransaction();
                            farmer.farmerId = newFarmerId;
                            Realm.getDefaultInstance().commitTransaction();

                            addFarmWithFarmerId(farmer);

                            checkSyncComplete();

                            if(farmer.isFarmerPicDirty)
                                attachFarmerImageToFarmerObject(farmer);
                            if(farmer.isNatIdCardDirty)
                                attachFarmerIDImageToFarmerObject(farmer);
                        }
                        else if(response.code() == 401)
                        {
                            countFailedCalls++;
                            PreferenceHelper.getInstance().clearSession();
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        t.printStackTrace();
                        countFailedCalls++;
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

                            if(farmer.isFarmerPicDirty)
                                attachFarmerImageToFarmerObject(farmer);
                            if(farmer.isNatIdCardDirty)
                                attachFarmerIDImageToFarmerObject(farmer);
                        }
                        else if(response.code() == 401)
                        {
                            countFailedCalls++;
                            PreferenceHelper.getInstance().clearSession();
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            countFailedCalls++;
                            checkSyncComplete();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        countFailedCalls++;
                        checkSyncComplete();
                    }
                }, farmer.farmerId, farmerMap);
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

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        if (attAboutId == null || attAboutId.isEmpty()) {
            WebApi.addAttachment(callback, entityBody, imageBody);
        } else {
            WebApi.editAttachment(callback, attAboutId, entityBody, imageBody);
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
        countCalls++;
        WebApi.createFarm(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                countCalls--;
                if (response.body() != null && response.body().success == true) {
                    checkSyncComplete();
                    Realm realm = Realm.getDefaultInstance();
                    Realm.getDefaultInstance().beginTransaction();
                    final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                    farmerDbObj.setDataDirty(false);
                    Realm.getDefaultInstance().commitTransaction();
                }
                else if(response.code() == 401)
                {
                    countFailedCalls++;
                    PreferenceHelper.getInstance().clearSession();
                    Intent intent = new Intent(mContext, LoginActivity.class);
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
        farmMap.put("Farmer__c", farmer.farmerId);
        countCalls++;
        WebApi.editFarm(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                countCalls--;
                if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {

                    Realm realm = Realm.getDefaultInstance();
                    Realm.getDefaultInstance().beginTransaction();
                    final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                    farmerDbObj.setDataDirty(false);
                    Realm.getDefaultInstance().commitTransaction();

                    checkSyncComplete();
                }
                else if(response.code() == 401)
                {
                    countFailedCalls++;
                    PreferenceHelper.getInstance().clearSession();
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    countFailedCalls++;
                    checkSyncComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                countFailedCalls++;
                checkSyncComplete();
            }
        }, farmer.farmId, farmMap);
    }

    private static void attachFarmerImageToFarmerObject(final Farmer farmer) {

        if (farmer.thumbUrl == null || farmer.thumbUrl.isEmpty())
            return;

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

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        countCalls++;
        if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
            WebApi.addAttachment(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                        Log.i("Fennel", "farmer profile picture uploaded successfully!");
                        Realm realm = Realm.getDefaultInstance();
                        Realm.getDefaultInstance().beginTransaction();
                        final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                        farmerDbObj.setFarmerPicDirty(false);
                        Realm.getDefaultInstance().commitTransaction();
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
                        Realm.getDefaultInstance().beginTransaction();
                        final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                        farmerDbObj.setFarmerPicDirty(false);
                        Realm.getDefaultInstance().commitTransaction();
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

    private static void attachFarmerIDImageToFarmerObject(final Farmer farmer) {

        if (farmer.nationalCardUrl == null || farmer.nationalCardUrl.isEmpty())
            return;

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "ID");
        attachmentMap.put("Name", "national_id.png");
        if(farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.farmerId);
        }
        else
        {
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

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        countCalls++;
        if (farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            WebApi.addAttachment(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    countCalls--;
                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                        Log.i("Fennel", "farmer ID picture uploaded successfully!");
                        Realm realm = Realm.getDefaultInstance();
                        Realm.getDefaultInstance().beginTransaction();
                        final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                        farmerDbObj.setNatIdCardDirty(false);
                        Realm.getDefaultInstance().commitTransaction();
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
                        Realm.getDefaultInstance().beginTransaction();
                        final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
                        farmerDbObj.setNatIdCardDirty(false);
                        Realm.getDefaultInstance().commitTransaction();
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

    private static void checkSyncComplete(){
        if(countCalls == 0) {
            if(countFailedCalls == 0){
//                Toast.makeText(mContext, "Sync completed", Toast.LENGTH_SHORT).show();
                Log.i("Sync process: ", "Sync completed");
            }
            else{
//                Toast.makeText(mContext, "Sync finished, but some records failed to sync", Toast.LENGTH_LONG).show();
                Log.i("Sync process: ", "Sync finished, but some records failed to sync");
            }
            saveSyncTimeStamp();
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

    public static void saveSyncTimeStamp(){
        long yourMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date resultDate = new Date(yourMillis);
        String syncTime = sdf.format(resultDate);
        PreferenceHelper.getInstance().writeLastSyncTime(syncTime);
    }

    public interface OnSyncCompleteListener{
        void syncCompleted();
    }
}
