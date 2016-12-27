package wal.fennel.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.ArrayList;

import wal.fennel.R;
import wal.fennel.fragments.Login;

public class LoginActivity extends BaseActivity {

    private final int permsRequestCode = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (savedInstanceState == null) {
            replaceFragment(new Login(), false);
        }

        checkPermissions();
    }

    private void checkPermissions() {

        ArrayList<String> permissionsList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    permsRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case permsRequestCode: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0/*
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED*/) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            } break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
