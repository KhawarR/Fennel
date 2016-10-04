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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    private Callback<LoginResponse> loginCallback = new Callback<LoginResponse>() {
        @Override
        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            loadingFinished();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

        @Override
        public void onFailure(Call<LoginResponse> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
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
        String username = "waajay@westagilelabs.com.waldev";
        String password = "walshamba123";
        loadingStarted();
        Call<Auth> call = Fennel.getAuthWebService().postSFLogin(NetworkHelper.GRANT, NetworkHelper.CLIENT_ID, NetworkHelper.CLIENT_SECRET, username, password, NetworkHelper.REDIRECTURI);
        call.enqueue(this);


    }


    @Override
    public void onResponse(Call<Auth> call, Response<Auth> response) {
        loadingFinished();
        Auth auth = response.body();
        if (getActivity() != null && isAdded() && !isDetached() && auth != null) {
            Toast.makeText(getActivity(), "Login success", Toast.LENGTH_SHORT).show();
            Session.saveAuth(getActivity(), auth);
            Fennel.restClient.setApiBaseUrl(auth.instance_url);
            String username = etId.getText().toString();
            if (username.length() > 0) {
                String loginQuery = "SELECT Id, Name FROM Mobile_Users__c WHERE Name = '" + username + "'";
                loadingStarted();
                Call<LoginResponse> apiCall = Fennel.getWebService().query(Session.getAuthToken(getActivity()), NetworkHelper.API_VERSION,loginQuery);
                apiCall.enqueue(loginCallback);
            }

        }
    }

    @Override
    public void onFailure(Call<Auth> call, Throwable t) {
        loadingFinished();
        t.printStackTrace();
    }
}
