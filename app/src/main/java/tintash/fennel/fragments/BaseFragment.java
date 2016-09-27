package tintash.fennel.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.ButterKnife;
import butterknife.OnClick;
import tintash.fennel.R;
import tintash.fennel.application.Fennel;
import tintash.fennel.utils.Constants;

public abstract class BaseFragment extends Fragment {

    public Tracker mTracker;
    ProgressDialog mProgressDialog;
    private long lastClickedTime = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mProgressDialog = new ProgressDialog(inflater.getContext());
        mProgressDialog.setMessage(getString(R.string.loading));
        Fennel application = (Fennel) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String screenName = getTrackerScreenName();
        if (screenName != null) {
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    protected abstract String getTrackerScreenName();

//    void replaceFragment(Fragment fragment, boolean isBackStack) {
//
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
////        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
//        ft.replace(R.id.fragment_container, fragment);
//        //ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//
//        if (isBackStack)
//            ft.addToBackStack(null);
//        ft.commit();
//    }
//
//    void addFragment(Fragment fragment) {
//
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
////        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
//        ft.add(R.id.fragment_container, fragment);
//        ft.addToBackStack(null);
//        ft.commit();
//    }

//    @Nullable
//    @OnClick(R.id.parent)
//    void onParent(View view) {
//
//    }


//    @Nullable
//    @OnClick(R.id.back_arrow)
//    void onBackArrowPressed(View view) {
//        getActivity().onBackPressed();
//    }


    public void loadingStarted() {
        if (getActivity() != null && mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void loadingFinished() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * use this method for avoiding multi tap, multi-click
     */
    protected boolean isValidClick() {
        if (System.currentTimeMillis() - lastClickedTime < Constants.TIME_DIFF) {
            return false;
        }
        lastClickedTime = System.currentTimeMillis();
        return true;
    }


}
