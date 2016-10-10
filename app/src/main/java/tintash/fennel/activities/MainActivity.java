package tintash.fennel.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import tintash.fennel.R;
import tintash.fennel.fragments.BaseContainerFragment;
import tintash.fennel.fragments.MyDashboard;
import tintash.fennel.fragments.MyDashboardContainerFragment;
import tintash.fennel.fragments.MyFarmerContainerFragment;
import tintash.fennel.fragments.MyFarmers;
import tintash.fennel.fragments.MyLogbook;
import tintash.fennel.fragments.MyLogbookContainerFragment;
import tintash.fennel.fragments.MySignUps;
import tintash.fennel.fragments.MySignUpsContainerFragment;
import tintash.fennel.utils.CustomImageDownloader;

public class MainActivity extends BaseActivity {

    private static final String TAB_1_TAG = "tab_1";
    private static final String TAB_2_TAG = "tab_2";
    private static final String TAB_3_TAG = "tab_3";
    private static final String TAB_4_TAG = "tab_4";
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.getTabWidget().setDividerDrawable(null);

        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_1_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mysignupos)),
                MySignUpsContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_2_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_myfarmers)),
                MyFarmerContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_3_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mydashboard)),
                MyDashboardContainerFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_4_TAG).setIndicator("", ContextCompat.getDrawable(this, R.drawable.selector_mylogbook)),
                MyLogbookContainerFragment.class, null);
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
}
