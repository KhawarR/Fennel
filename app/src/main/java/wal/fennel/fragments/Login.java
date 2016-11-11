package wal.fennel.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.LoginActivity;
import wal.fennel.activities.MainActivity;
import wal.fennel.application.Fennel;
import wal.fennel.datamodels.Auth;
import wal.fennel.network.Session;
import wal.fennel.network.WebApi;
import wal.fennel.utils.Constants;
import wal.fennel.utils.PreferenceHelper;

public class Login extends BaseFragment{

    @Bind(R.id.txtLogin)
    TextView txtLogin;

    @Bind(R.id.etID)
    EditText etId;

    @Bind(R.id.etPassword)
    EditText etPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_login, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO Remove on release
        etId.setText("1");
        etPassword.setText("pass");

        if(!PreferenceHelper.getInstance().readToken().isEmpty() && !PreferenceHelper.getInstance().readLoginUserId().isEmpty())
        {
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
    void onClickLogin(View view) {
        if (etId.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please put username & password", Toast.LENGTH_SHORT).show();
        } else {

            if(!etId.getText().toString().trim().equalsIgnoreCase(PreferenceHelper.getInstance().readUserId()) || !etPassword.getText().toString().trim().equalsIgnoreCase(PreferenceHelper.getInstance().readPassword()))
                PreferenceHelper.getInstance().clearSession(true);

//            String username = "waajay@westagilelabs.com.waldev";
            String username = "waajay@westagilelabs.com";
            String password = "walshamba123";
            loadingStarted();
            boolean isCallProcessed = WebApi.salesForceAuth(authCallback, username, password);
            if(!isCallProcessed)
            {
                loadingFinished();
                Toast.makeText(getActivity(), Constants.TOAST_NO_INTERNET, Toast.LENGTH_LONG).show();
            }
        }
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
                    if(!isCallProcessed)
                    {
                        loadingFinished();
                        Toast.makeText(getActivity(), Constants.TOAST_NO_INTERNET, Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    loadingFinished();
                }
            }
            else
            {
                Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
//            try {
//                Toast.makeText(getActivity(), "Authentication failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
                loadingFinished();
            }
        }

        @Override
        public void onFailure(Call<Auth> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
//        Toast.makeText(getActivity(), "Authentication failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
        }
    };

    private Callback<ResponseBody> loginCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseData(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if(response.code() == 401)
            {
                PreferenceHelper.getInstance().clearSession(false);
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
            else
            {
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

        if(arrRecords.length() > 0)
        {
            JSONObject objRecord = arrRecords.getJSONObject(0);
            JSONObject userObj = objRecord.getJSONObject("User_ID__r");
            String username = userObj.getString("Name");
            String password = objRecord.getString("Password__c");
            String userEmpId = objRecord.getString("User_ID__c");
            PreferenceHelper.getInstance().writeUserId(username);
            PreferenceHelper.getInstance().writePassword(password);
            PreferenceHelper.getInstance().writeUserEmployeeId(userEmpId);

            WebApi.getAboutMeInfo(aboutMeCallback);
        }
        else
        {
            loadingFinished();
            Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
        }
    }

    private Callback<ResponseBody> aboutMeCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseAboutMeData(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
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

            if(objFacilitator != null)
            {
                getAndSaveId(objFacilitator, Constants.STR_FACILITATOR);
                JSONArray arrRec = objFacilitator.getJSONArray("records");
                if(arrRec.length() > 0)
                {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    JSONObject objFO = obj1.optJSONObject("Field_Officer__r");
                    if(objFO != null)
                    {
                        JSONObject objFOEmployee = objFO.optJSONObject("Employee__r");
                        if(objFOEmployee != null)
                        {
                            fo_name = objFOEmployee.getString("Full_Name__c");
                        }

                        JSONObject objFO_FM = objFO.optJSONObject("Field_Manager__r");
                        if(objFO_FM != null)
                        {
                            JSONObject objFO_FMEmployee = objFO_FM.optJSONObject("Employee__r");
                            if(objFO_FMEmployee != null)
                            {
                                fm_name = objFO_FMEmployee.getString("Full_Name__c");
                            }
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
            else if(objFieldOffice != null)
            {
                getAndSaveId(objFieldOffice, Constants.STR_FIELD_OFFICER);
                JSONArray arrRec = objFieldOffice.getJSONArray("records");
                if(arrRec.length() > 0)
                {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    JSONObject objFM = obj1.optJSONObject("Field_Manager__r");
                    if(objFM != null)
                    {
                        JSONObject objFO_FMEmployee = objFM.optJSONObject("Employee__r");
                        if(objFO_FMEmployee != null)
                        {
                            fm_name = objFO_FMEmployee.getString("Full_Name__c");
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
            else if(objFieldManager != null)
            {
                getAndSaveId(objFieldManager, Constants.STR_FIELD_MANAGER);
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
            else
            {
                Toast.makeText(getActivity(), "Invalid user", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAboutMeInfo(String fn, String mn, String ln, String fo_name, String fm_name)
    {
        PreferenceHelper.getInstance().writeAboutFN(fn);
        PreferenceHelper.getInstance().writeAboutMN(mn);
        PreferenceHelper.getInstance().writeAboutLN(ln);
        PreferenceHelper.getInstance().writeAboutFOname(fo_name);
        PreferenceHelper.getInstance().writeAboutFMname(fm_name);
    }

    private void getAndSaveId(JSONObject jsonObject, String type) throws JSONException {
        JSONArray arrRec = jsonObject.getJSONArray("records");
        if(arrRec.length() > 0)
        {
            JSONObject obj1 = arrRec.getJSONObject(0);
            String idFac = obj1.getString("Id");
            PreferenceHelper.getInstance().writeLoginUserType(type);
            PreferenceHelper.getInstance().writeLoginUserId(idFac);
            proceedToMainScreen();
        }
    }

    private void proceedToMainScreen()
    {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
}
