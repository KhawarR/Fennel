package wal.fennel.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import wal.fennel.R;
import wal.fennel.fragments.BaseContainerFragment;
import wal.fennel.fragments.MyDashboardContainerFragment;
import wal.fennel.fragments.MyFarmerTasksContainerFragment;
import wal.fennel.fragments.MyLogbookContainerFragment;
import wal.fennel.fragments.MySignUps;
import wal.fennel.fragments.MySignUpsContainerFragment;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;

public class MainActivity extends BaseActivity implements TabHost.OnTabChangeListener, MySignUps.ISwitchTabAfterLoading {

    private MixpanelAPI mixPanel;

    private static final String TAB_1_TAG = "tab_1";
    private static final String TAB_2_TAG = "tab_2";
    private static final String TAB_3_TAG = "tab_3";
    private static final String TAB_4_TAG = "tab_4";
    private FragmentTabHost mTabHost;
    private boolean isAllowedToSwitchTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mixPanel = MixpanelAPI.getInstance(this, MixPanelConstants.MIXPANEL_TOKEN);

//        getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.getTabWidget().setDividerDrawable(null);

        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_1_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mysignupos)),
                MySignUpsContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_2_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_myfarmers)),
                MyFarmerTasksContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_3_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mydashboard)),
                MyDashboardContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_4_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mylogbook)),
                MyLogbookContainerFragment.class, null);

        mTabHost.setOnTabChangedListener(this);
    }

    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;
        String currentTabTag = mTabHost.getCurrentTabTag();
        if (currentTabTag.equals(TAB_1_TAG)) {
            isPopFragment = ((BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(TAB_1_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_2_TAG)) {
            isPopFragment = ((BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(TAB_2_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_3_TAG)) {
            isPopFragment = ((BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(TAB_3_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_4_TAG)) {
            isPopFragment = ((BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(TAB_4_TAG)).popFragment();
        }
        if (!isPopFragment) {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTabChanged(String tabId) {

        String currentTabTag = mTabHost.getCurrentTabTag();
        if (currentTabTag.equals(TAB_1_TAG)) {
            mixPanel.track(MixPanelConstants.Event.TAB_MY_SIGNUPS);
        } else if (currentTabTag.equals(TAB_2_TAG)) {
            mixPanel.track(MixPanelConstants.Event.TAB_MY_FARMERS);
        } else if (currentTabTag.equals(TAB_3_TAG)) {
            mixPanel.track(MixPanelConstants.Event.TAB_MY_DASHBOARD);
        } else if (currentTabTag.equals(TAB_4_TAG)) {
            mixPanel.track(MixPanelConstants.Event.TAB_MY_LOGBOOK);
        }

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    protected void onDestroy() {
        mixPanel.flush();
        super.onDestroy();
    }

    @Override
    public void onSwitchTabAfterLoading() {
        if(isAllowedToSwitchTab) {
            mTabHost.setCurrentTabByTag(TAB_2_TAG);
            isAllowedToSwitchTab = false;
        }
    }
}
