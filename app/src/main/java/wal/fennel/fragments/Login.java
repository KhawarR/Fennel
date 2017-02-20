package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.MainActivity;
import wal.fennel.activities.SplashActivity;
import wal.fennel.application.Fennel;
import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.datamodels.Auth;
import wal.fennel.models.Location;
import wal.fennel.models.SubLocation;
import wal.fennel.models.Tree;
import wal.fennel.models.Village;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.Session;
import wal.fennel.network.WebApi;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;
import wal.fennel.utils.PreferenceHelper;

public class Login extends BaseFragment {

    private MixpanelAPI mixPanel;

    private static final String TAG = "LoginFragment";

    @Bind(R.id.etPassword)
    EditText etPassword;

    @Bind(R.id.txtLogin)
    TextView txtLogin;

    @Bind(R.id.etID)
    EditText etId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
//        Log.i(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Log.i(TAG, "onViewCreated()");

        mixPanel = MixpanelAPI.getInstance(getActivity(), MixPanelConstants.MIXPANEL_TOKEN);
        mixPanel.track(MixPanelConstants.PageView.LOGIN);

        // TODO Remove on release
//        etId.setText("99100");
//        etPassword.setText("pass");

        if (!PreferenceHelper.getInstance().readToken().isEmpty() && !PreferenceHelper.getInstance().readLoginUserId().isEmpty()) {
            Fennel.restClient.setApiBaseUrl(PreferenceHelper.getInstance().readInstanceUrl());
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @OnClick(R.id.txtLogin)
    void onClickLogin() {
        if (etId.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.str_user_pass_missing, Toast.LENGTH_SHORT).show();
        } else {

            if (!checkIfUserReturned()) {
                PreferenceHelper.getInstance().clearSession(true);
            }

            try {
                JSONObject props = new JSONObject();
                props.put(MixPanelConstants.Property.USERNAME, etId.getText().toString().trim());
                props.put(MixPanelConstants.Property.PASSWORD, etPassword.getText().toString().trim());
                mixPanel.track(MixPanelConstants.Event.LOGIN_BUTTON, props);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO Test to Production
            String username = getString(R.string.auth_username_test_env);
//            String username = getString(R.string.auth_username_test_prod);
            String password = getString(R.string.auth_password);
            loadingStarted();
            boolean isCallProcessed = WebApi.salesForceAuth(authCallback, username, password);
            if (!isCallProcessed) {
                loadingFinished();
                Toast.makeText(getActivity(), Constants.TOAST_NO_INTERNET, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkIfUserReturned() {
        if (!etId.getText().toString().trim().equalsIgnoreCase(PreferenceHelper.getInstance().readUserId())
                || !etPassword.getText().toString().trim().equalsIgnoreCase(PreferenceHelper.getInstance().readPassword())) {
            return false;
        }
        return true;
    }

    Callback<Auth> authCallback = new Callback<Auth>() {
        @Override
        public void onResponse(Call<Auth> call, Response<Auth> response) {
            Auth auth = response.body();
            if (getActivity() != null && isAdded() && !isDetached() && auth != null) {
                Session.saveAuth(auth);
                Fennel.restClient.setApiBaseUrl(auth.instance_url);
                String username = etId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.length() > 0) {
                    boolean isCallProcessed = WebApi.login(loginCallback, username, password);
                    if (!isCallProcessed) {
                        loadingFinished();
                        Toast.makeText(getActivity(), Constants.TOAST_NO_INTERNET, Toast.LENGTH_LONG).show();
                    }
                } else {
                    loadingFinished();
                }
            } else {
                loadingFinished();
            }
        }

        @Override
        public void onFailure(Call<Auth> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
            Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
        }
    };

    private Callback<ResponseBody> loginCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS) {

                try {
                    String responseStr = response.body().string();
                    parseData(responseStr);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else if (response.code() == Constants.RESPONSE_SESSION_EXPIRE) {
                PreferenceHelper.getInstance().clearSession(false);
                startActivity(new Intent(getActivity(), SplashActivity.class));
                getActivity().finish();
            } else {
                loadingFinished();
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private void parseData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            JSONObject objRecord = arrRecords.getJSONObject(0);
            JSONObject userObj = objRecord.getJSONObject("User_ID__r");
            String username = userObj.getString("Name");
            String password = objRecord.getString("Password__c");
            String userEmpId = objRecord.getString("User_ID__c");
            PreferenceHelper.getInstance().writeUserId(username);
            PreferenceHelper.getInstance().writePassword(password);
            PreferenceHelper.getInstance().writeUserEmployeeId(userEmpId);

            mixPanel.identify(username);
            mixPanel.getPeople().identify(username);
            mixPanel.getPeople().set(MixPanelConstants.Property.EMPLOYEE_ID, username);

            WebApi.getAboutMeInfo(aboutMeCallback);
        } else {
            loadingFinished();
            Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
        }
    }

    private Callback<ResponseBody> aboutMeCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if(isValid()){
                if (response.code() == Constants.RESPONSE_SUCCESS) {

                    try {
                        String responseStr = response.body().string();
                        parseAboutMeData(responseStr);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.str_error_code_message) + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private void parseAboutMeData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            JSONObject objRecord = arrRecords.getJSONObject(0);
//            String id = objRecord.getString("Id");

            String fn = (!(objRecord.getString("First_Name__c").equals("null"))) ? (objRecord.getString("First_Name__c")) : "";
            String mn = (!(objRecord.getString("Middle_Name__c").equals("null"))) ? (objRecord.getString("Middle_Name__c")) : "";
            String ln = (!(objRecord.getString("Last_Name__c").equals("null"))) ? (objRecord.getString("Last_Name__c")) : "";
            String fo_name = "";
            String fm_name = "";

            JSONObject objFacilitator = objRecord.optJSONObject("Facilitators__r");
            JSONObject objFieldOffice = objRecord.optJSONObject("Field_Officers__r");
            JSONObject objFieldManager = objRecord.optJSONObject("Field_Managers__r");

            if (objFacilitator != null) {
                getAndSaveId(objFacilitator, Constants.STR_FACILITATOR);
                JSONArray arrRec = objFacilitator.getJSONArray("records");
                if (arrRec.length() > 0) {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    String employeeName = obj1.optString("Name");
                    PreferenceHelper.getInstance().writeEmployeeFullname(employeeName);
                    JSONObject objFO = obj1.optJSONObject("Field_Officer__r");
                    if (objFO != null) {
                        JSONObject objFOEmployee = objFO.optJSONObject("Employee__r");
                        if (objFOEmployee != null) {
                            fo_name = objFOEmployee.getString("Full_Name__c");
                        }

                        JSONObject objFO_FM = objFO.optJSONObject("Field_Manager__r");
                        if (objFO_FM != null) {
                            JSONObject objFO_FMEmployee = objFO_FM.optJSONObject("Employee__r");
                            if (objFO_FMEmployee != null) {
                                fm_name = objFO_FMEmployee.getString("Full_Name__c");
                            }
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            } else if (objFieldOffice != null) {
                getAndSaveId(objFieldOffice, Constants.STR_FIELD_OFFICER);
                JSONArray arrRec = objFieldOffice.getJSONArray("records");
                if (arrRec.length() > 0) {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    String employeeName = obj1.optString("Name");
                    PreferenceHelper.getInstance().writeEmployeeFullname(employeeName);
                    JSONObject objFM = obj1.optJSONObject("Field_Manager__r");
                    if (objFM != null) {
                        JSONObject objFO_FMEmployee = objFM.optJSONObject("Employee__r");
                        if (objFO_FMEmployee != null) {
                            fm_name = objFO_FMEmployee.getString("Full_Name__c");
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            } else if (objFieldManager != null) {
                getAndSaveId(objFieldManager, Constants.STR_FIELD_MANAGER);
                JSONArray arrRec = objFieldManager.getJSONArray("records");
                if (arrRec.length() > 0) {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    String employeeName = obj1.optString("Name");
                    PreferenceHelper.getInstance().writeEmployeeFullname(employeeName);
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            } else {
                Toast.makeText(getActivity(), "Invalid user", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAboutMeInfo(String fn, String mn, String ln, String fo_name, String fm_name) {
        PreferenceHelper.getInstance().writeAboutFN(fn);
        PreferenceHelper.getInstance().writeAboutMN(mn);
        PreferenceHelper.getInstance().writeAboutLN(ln);
        PreferenceHelper.getInstance().writeAboutFOname(fo_name);
        PreferenceHelper.getInstance().writeAboutFMname(fm_name);

        String fullName = fn;
        fullName = fullName.trim() + " " + mn;
        fullName = fullName.trim() + " " + ln;
        fullName = fullName.trim();

        mixPanel.getPeople().set(MixPanelConstants.Property.DEFAULT_NAME, PreferenceHelper.getInstance().readUserId() + " - " + fullName);
    }

    private void getAndSaveId(JSONObject jsonObject, String type) throws JSONException {
        JSONArray arrRec = jsonObject.getJSONArray("records");
        if (arrRec.length() > 0) {
            JSONObject obj1 = arrRec.getJSONObject(0);
            String idFac = obj1.getString("Id");
            PreferenceHelper.getInstance().writeLoginUserType(type);
            PreferenceHelper.getInstance().writeLoginUserId(idFac);

            getDropDownsData();
        }
    }

    private void proceedToMainScreen() {
        PreferenceHelper.getInstance().writeFirstRun(true);
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void getDropDownsData() {

        if (NetworkHelper.isNetAvailable(getActivity())) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Response<ResponseBody> locationsResponse = WebApi.getLocations();
                        if (locationsResponse.code() == 200) {
                            parseLocations(locationsResponse.body().string());
                        }

                        Response<ResponseBody> subLocationsResponse = WebApi.getSubLocations();
                        if (subLocationsResponse.code() == 200) {
                            parseSubLocations(subLocationsResponse.body().string());
                        }

                        Response<ResponseBody> villagesResponse = WebApi.getVillages();
                        if (villagesResponse.code() == 200) {
                            parseVillages(villagesResponse.body().string());
                        }

                        Response<ResponseBody> treesResponse = WebApi.getTrees();
                        if (treesResponse.code() == 200) {
                            parseTrees(treesResponse.body().string());
                        }

                        loadingFinishedFromBackground();
                        proceedToMainScreen();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        loadingFinishedFromBackground();
                        proceedToMainScreen();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        loadingFinishedFromBackground();
                        proceedToMainScreen();
                    }
                }
            }).start();
        }
    }

    private void loadingFinishedFromBackground() {
        if (isValid()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingFinished();
                }
            });
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        Log.i(TAG, "onAttach()");
//    }
//
//    @Override
//    public void onCreate(Bundle onSaved) {
//        super.onCreate(onSaved);
//        Log.i(TAG, "onCreate()");
//    }
//
//    @Override
//    public void onActivityCreated(Bundle bundle) {
//        super.onActivityCreated(bundle);
//        Log.i(TAG, "onActivityCreated()");
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.i(TAG, "onStart()");
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.i(TAG, "onResume()");
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.i(TAG, "onPause()");
//    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        loadingFinished();
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        Log.i(TAG, "onDestroyView()");
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i(TAG, "onDestroy()");
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        Log.i(TAG, "onDetach()");
//    }

    private void parseLocations(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            DatabaseHelper.getInstance().deleteAllLocations();
            ArrayList<Location> allLocations = new ArrayList<>();

            DatabaseHelper.getInstance().getWritableDatabase().beginTransaction();

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject locationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";

                id = locationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = locationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                Location location = new Location(id, name);
                allLocations.add(location);
                DatabaseHelper.getInstance().insertLocation(location);
            }

            DatabaseHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
            DatabaseHelper.getInstance().getWritableDatabase().endTransaction();
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseSubLocations(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            DatabaseHelper.getInstance().deleteAllSubLocations();
            ArrayList<SubLocation> allSubLocations = new ArrayList<>();

            DatabaseHelper.getInstance().getWritableDatabase().beginTransaction();

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject subLocationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";
                String locationId = "";

                id = subLocationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = subLocationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                locationId = subLocationObj.getString("Location__c");
                if (locationId.equalsIgnoreCase("null")) locationId = "";

                SubLocation subLocation = new SubLocation(id, name, locationId);
                allSubLocations.add(subLocation);

                DatabaseHelper.getInstance().insertSubLocation(subLocation);
            }

            DatabaseHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
            DatabaseHelper.getInstance().getWritableDatabase().endTransaction();
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseVillages(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            DatabaseHelper.getInstance().deleteAllVillages();
            ArrayList<Village> allVillages = new ArrayList<>();

            DatabaseHelper.getInstance().getWritableDatabase().beginTransaction();

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject subLocationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";
                String subLocationId = "";

                id = subLocationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = subLocationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                subLocationId = subLocationObj.getString("Sub_Location__c");
                if (subLocationId.equalsIgnoreCase("null")) subLocationId = "";

                Village village = new Village(id, name, subLocationId);
                allVillages.add(village);
                DatabaseHelper.getInstance().insertVillage(village);
            }

            DatabaseHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
            DatabaseHelper.getInstance().getWritableDatabase().endTransaction();
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseTrees(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            DatabaseHelper.getInstance().deleteAllTrees();

            DatabaseHelper.getInstance().getWritableDatabase().beginTransaction();

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject treeObj = arrRecords.getJSONObject(i).getJSONObject("Tree_Species__r");

                String id = "";
                String name = "";
                String subLocationId = "";

                id = treeObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = treeObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                subLocationId = arrRecords.getJSONObject(i).getString("Sub_Location__c");
                if (subLocationId.equalsIgnoreCase("null")) subLocationId = "";

                Tree tree = new Tree(id, name, subLocationId);
                DatabaseHelper.getInstance().insertTree(tree);
            }

            DatabaseHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
            DatabaseHelper.getInstance().getWritableDatabase().endTransaction();
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }
}
