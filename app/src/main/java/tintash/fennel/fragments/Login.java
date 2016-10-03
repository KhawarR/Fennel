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

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tintash.fennel.R;
import tintash.fennel.activities.MainActivity;
import tintash.fennel.application.Fennel;
import tintash.fennel.datamodels.Auth;
import tintash.fennel.datamodels.LoginResponse;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;

public class Login extends BaseFragment implements Callback<Auth> {


    @Bind(R.id.txtLogin)
    TextView txtLogin;

    @Bind(R.id.etID)
    EditText etId;

    @Bind(R.id.etPassword)
    EditText etPassword;

    private Callback<LoginResponse> loginCallback = new Callback<LoginResponse>() {
        @Override
        public void success(LoginResponse loginResponse, Response response) {
            loadingFinished();
            if(loginResponse.records.size() > 0)
            {
                Toast.makeText(getActivity(), "Login success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
            else
            {
                Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            loadingFinished();
            error.printStackTrace();
        }
    };


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
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @OnClick(R.id.txtLogin)
    void onClickLogin(View view) {
        if(etId.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty())
        {
            Toast.makeText(getActivity(), "Please put username & password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String username = "waajay@westagilelabs.com.waldev";
            String password = "walshamba123";
            loadingStarted();
            Fennel.getAuthWebService().postSFLogin(NetworkHelper.GRANT, NetworkHelper.CLIENT_ID, NetworkHelper.CLIENT_SECRET, username, password, NetworkHelper.REDIRECTURI, this);
        }
    }

    @Override
    public void success(Auth auth, Response response) {
        loadingFinished();
        if (getActivity() != null && isAdded() && !isDetached()) {
            Session.saveAuth(getActivity(), auth);
            Fennel.restClient.setApiBaseUrl(auth.instance_url);
            String username = etId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (username.length() > 0) {
                String loginQuery = "SELECT Id, Name FROM Mobile_Users__c WHERE Name = '" + username + "' AND Password__c = '" + password + "'";
                loadingStarted();
                Fennel.getWebService().query(Session.getAuthToken(getActivity()), loginQuery, NetworkHelper.API_VERSION, loginCallback);
            }
        }
    }

    @Override
    public void failure(RetrofitError error) {
        loadingFinished();
        error.printStackTrace();
        Toast.makeText(getActivity(), "SalesForce Authentication failed", Toast.LENGTH_LONG).show();
    }
}
