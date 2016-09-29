package tintash.fennel.activities;

import android.os.Bundle;
import android.view.View;

import tintash.fennel.R;
import tintash.fennel.fragments.Login;


public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (savedInstanceState == null)
            replaceFragment(new Login(), false);
    }
}
