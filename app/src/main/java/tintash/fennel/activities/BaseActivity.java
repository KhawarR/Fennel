package tintash.fennel.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.Tracker;

import tintash.fennel.R;
import tintash.fennel.application.Fennel;


public abstract class BaseActivity extends AppCompatActivity {

    long lastClickedTime = -1;
    ProgressDialog mProgressDialog;

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        Fennel application = (Fennel) getApplication();
        mTracker = application.getDefaultTracker();
    }

//    void replaceFragment(Fragment fragment, boolean isBackStack) {
//
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.fragment_container, fragment);
//        if (isBackStack)
//            ft.addToBackStack(null);
//        ft.commit();
//    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
        else
            super.onBackPressed();

    }

    public void loadingStarted() {

        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void loadingFinished() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
