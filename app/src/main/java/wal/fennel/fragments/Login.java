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

import com.mixpanel.android.mpmetrics.MixpanelAPI;

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
import wal.fennel.activities.MainActivity;
import wal.fennel.activities.SplashActivity;
import wal.fennel.application.Fennel;
import wal.fennel.datamodels.Auth;
import wal.fennel.network.Session;
import wal.fennel.network.WebApi;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;
import wal.fennel.utils.PreferenceHelper;

public class Login extends BaseFragment {

    private MixpanelAPI mixPanel;

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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mixPanel = MixpanelAPI.getInstance(getActivity(), MixPanelConstants.MIXPANEL_TOKEN);
        mixPanel.track(MixPanelConstants.PageView.LOGIN);

        // TODO Remove on release
        etId.setText("1211");
        etPassword.setText("pass");

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
                Toast.makeText(getActivity(), Constants.TOAST_LOGIN_ERROR, Toast.LENGTH_LONG).show();
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
            loadingFinished();
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
            proceedToMainScreen();
        }
    }

    private void proceedToMainScreen() {
        PreferenceHelper.getInstance().writeFirstRun(true);
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
}
