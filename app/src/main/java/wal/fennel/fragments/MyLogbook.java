package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.models.FieldAgent;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.TitleBarLayout;

import static wal.fennel.R.id.tvperson;
import static wal.fennel.R.id.tvteam;


public class MyLogbook extends BaseFragment {


    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    CircleImageView cIvIconRight;

    @Bind(R.id.tvperson)
    TextView tvPerson;

    @Bind(R.id.tvteam)
    TextView tvTeam;

    @Bind(R.id.tabview)
    LinearLayout tabView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_logbook, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if(!thumbUrl.isEmpty())
        {
            if(NetworkHelper.isNetAvailable(getActivity()))
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            else
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
        }


        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if (userType.equalsIgnoreCase(Constants.STR_FACILITATOR)) {
            tabView.setVisibility(View.GONE);
        } else {
            tabView.setVisibility(View.VISIBLE);
        }

        showTeamViewFragment();
        tvTeam.setSelected(true);

    }

    public void showTeamViewFragment() {
        BaseFragment teamLogBookFragment = (BaseFragment) getChildFragmentManager().findFragmentByTag(Constants.TEAM_LOGBOOK_TAG);
        if (teamLogBookFragment == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.logbook_layout, new TeamLogBookFragment());
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }
        titleBarLayout.setTxtLeft("");
    }

    public void showPersonViewFragment() {
        BaseFragment personLogBookFragment = (BaseFragment) getChildFragmentManager().findFragmentByTag(Constants.PERSON_LOGBOOK_TAG);
        if (personLogBookFragment == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.logbook_layout, new PersonLogBookFragment());
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }
        titleBarLayout.setTxtLeft("");
    }

    public void addPersonDetailViewFragment(FieldAgent fieldAgent) {
        BaseFragment personDetailLogBookFragment = (BaseFragment) getChildFragmentManager().findFragmentByTag(Constants.PERSON_DETAIL_LOGBOOK_TAG);
        if (personDetailLogBookFragment == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            transaction.add(R.id.logbook_layout, LogBookPersonDetailFragment.newInstance(fieldAgent));
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }
    }

    public boolean popFragment() {
        Log.e("fennel", "pop fragment: " + getChildFragmentManager().getBackStackEntryCount());
        boolean isPop = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isPop = true;
            getChildFragmentManager().popBackStack();
        }
        return isPop;
    }


    @OnClick({tvteam, tvperson})
    void onClickViewSelection(View view) {
        tvTeam.setSelected(false);
        tvPerson.setSelected(false);
        view.setSelected(true);

        if (tvPerson.isSelected()) {
            showPersonViewFragment();
        } else if (tvTeam.isSelected()) {
            showTeamViewFragment();
        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
//        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

}
