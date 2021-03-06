package wal.fennel.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import wal.fennel.R;
import wal.fennel.application.Fennel;
import wal.fennel.utils.Constants;
import wal.fennel.utils.PreferenceHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends Activity {

    private final int TIME_SPLASH = 1500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mHandler.sendEmptyMessageDelayed(0, TIME_SPLASH);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SplashActivity.this.finish();
            PreferenceHelper.getInstance().writeIsSyncInProgress(false);

            if(!PreferenceHelper.getInstance().readToken().isEmpty() && !PreferenceHelper.getInstance().readLoginUserId().isEmpty()) {
                Fennel.restClient.setApiBaseUrl(PreferenceHelper.getInstance().readInstanceUrl());
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        }
    };
}