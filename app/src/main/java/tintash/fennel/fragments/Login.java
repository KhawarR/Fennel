package tintash.fennel.fragments;


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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.activities.MainActivity;
import tintash.fennel.application.Fennel;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.utils.PreferenceHelper;

public class Login extends BaseFragment implements Callback<Auth> {

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
//        etId.setText("khawar");
//        etPassword.setText("khawar");

        if(!PreferenceHelper.getInstance().readToken().isEmpty())
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
            String username = "waajay@westagilelabs.com.waldev";
            String password = "walshamba123";
            loadingStarted();
            Call<Auth> call = Fennel.getAuthWebService().postSFLogin(NetworkHelper.GRANT, NetworkHelper.CLIENT_ID, NetworkHelper.CLIENT_SECRET, username, password, NetworkHelper.REDIRECT_URI);
            call.enqueue(this);
        }
    }

    @Override
    public void onResponse(Call<Auth> call, Response<Auth> response) {

        Auth auth = response.body();
        if (getActivity() != null && isAdded() && !isDetached() && auth != null) {
            Session.saveAuth(auth);
            Fennel.restClient.setApiBaseUrl(auth.instance_url);
            String username = etId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

//            CookieManager.setDefault(new CookieManager());
//
//            CookieHandler cookieManager = CookieManager.getDefault();
//            try {
//                Map<String,List<String>> map = new HashMap<>();
//                List<String> l = new ArrayList<>();
//                l.add(auth.access_token);
//                map.put("sid",l);
//                cookieManager.put(new URI("https://c.cs25.content.force.com/"),map);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }

            if (username.length() > 0) {
                String loginQuery = String.format(NetworkHelper.QUERY_LOGIN, username, password);
                Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, loginQuery);
                apiCall.enqueue(loginCallback);
            }
            else
            {
                loadingFinished();
            }
        }
        else
        {
            loadingFinished();
        }
    }

    @Override
    public void onFailure(Call<Auth> call, Throwable t) {
        loadingFinished();
        t.printStackTrace();
        Toast.makeText(getActivity(), "SalesForce Authentication failed", Toast.LENGTH_LONG).show();
    }

    private Callback<ResponseBody> loginCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
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
            else
            {
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
            String id = objRecord.getString("Id");

            JSONObject objFacilitator = objRecord.getJSONObject("Facilitator__r");
            String facilitatorId = objFacilitator.getString("Id");

            PreferenceHelper.getInstance().writeFacilitatorId(facilitatorId);
            PreferenceHelper.getInstance().writeUserId(etId.getText().toString().trim());
            PreferenceHelper.getInstance().writePassword(etPassword.getText().toString().trim());
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
        else
        {
            Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
}
