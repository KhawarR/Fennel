package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.picasso.NetworkPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmList;
import wal.fennel.R;
import wal.fennel.adapters.TeamDashboardAdapter;
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.models.DashboardTask;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.views.FontTextView;

/**
 * Created by irfanayaz on 12/25/16.
 */

public class DashboardPersonDetailFragment extends BaseFragment {
    @Bind(R.id.logbook_listview)
    ListView logbookListView;
    TeamDashboardAdapter dashboardAdapter;
    DashboardFieldAgent fieldAgent = null;

    public static DashboardPersonDetailFragment newInstance(DashboardFieldAgent clickedAgent) {
        DashboardPersonDetailFragment fragment = new DashboardPersonDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("fieldAgent", clickedAgent);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.team_logbook_fragment, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        fieldAgent = (DashboardFieldAgent) getArguments().getParcelable("fieldAgent");

        RealmList<DashboardTask> descriptionList = fieldAgent.getDashboardTasks();

        dashboardAdapter = new TeamDashboardAdapter(getActivity(), descriptionList);
        logbookListView.setAdapter(dashboardAdapter);

        ViewGroup myHeader = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.header_farmer_info, logbookListView, false);
        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);

        FontTextView tvFarmerName = (FontTextView) myHeader.findViewById(R.id.tvFullName);
        tvFarmerName.setText(fieldAgent.getAgentName());
        FontTextView tvLocation = (FontTextView) myHeader.findViewById(R.id.tvLocation);
        tvLocation.setVisibility(View.GONE);
        FontTextView tvMobile = (FontTextView) myHeader.findViewById(R.id.tvMobile);
        tvMobile.setText(fieldAgent.getAgentNumber() != null && !fieldAgent.getAgentNumber().isEmpty() && !fieldAgent.getAgentNumber().equalsIgnoreCase("null") ? fieldAgent.getAgentNumber() : "-");

        logbookListView.addHeaderView(myHeader);

        ((MyDashboard)(getParentFragment())).titleBarLayout.setTxtLeft("Back");
        ((MyDashboard)(getParentFragment())).titleBarLayout.setOnIconClickListener(this);


        CircleImageView ivFarmerThumb = (CircleImageView) myHeader.findViewById(R.id.ivFarmerThumb);
        {
            if(NetworkHelper.isNetAvailable(getActivity()))
                MyPicassoInstance.getInstance().load(NetworkHelper.makeAttachmentUrlFromId(fieldAgent.getAgentAttachmentUrl())).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
            else
                MyPicassoInstance.getInstance().load(NetworkHelper.makeAttachmentUrlFromId(fieldAgent.getAgentAttachmentUrl())).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);

        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarLeftIconClicked(View view) {
        ((MyDashboard)(getParentFragment())).titleBarLayout.setTxtLeft("");
        ((MyDashboard)(getParentFragment())).popFragment();
    }

}
