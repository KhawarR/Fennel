package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.adapters.TeamLogBookAdapter;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.LogTaskItem;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.views.FontTextView;

/**
 * Created by irfanayaz on 12/5/16.
 */
public class LogBookPersonDetailFragment extends BaseFragment {

    @Bind(R.id.logbook_listview)
    ListView logbookListView;
    TeamLogBookAdapter logBookAdapter;
    FieldAgent fieldAgent = null;

    public static LogBookPersonDetailFragment newInstance(FieldAgent clickedAgent) {
        LogBookPersonDetailFragment fragment = new LogBookPersonDetailFragment();
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

        fieldAgent = (FieldAgent) getArguments().getParcelable("fieldAgent");

        LogTaskItem[] descriptionList = (LogTaskItem[]) fieldAgent.getVisitLogs().toArray(new LogTaskItem[0]);
        ArrayList<LogTaskItem> logsList = new ArrayList<LogTaskItem>(Arrays.asList(descriptionList));

        Collections.sort(logsList, Collections.<LogTaskItem>reverseOrder());

        logBookAdapter = new TeamLogBookAdapter(getActivity(), logsList);
        logbookListView.setAdapter(logBookAdapter);

        ViewGroup myHeader = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.header_farmer_info, logbookListView, false);
        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);

        FontTextView tvFarmerName = (FontTextView) myHeader.findViewById(R.id.tvFullName);
        tvFarmerName.setText(fieldAgent.getName());
        FontTextView tvLocation = (FontTextView) myHeader.findViewById(R.id.tvLocation);
        tvLocation.setVisibility(View.GONE);
        FontTextView tvMobile = (FontTextView) myHeader.findViewById(R.id.tvMobileNumber);
        tvMobile.setText((fieldAgent.getPhoneNumber() != null && !fieldAgent.getPhoneNumber().isEmpty() && !fieldAgent.getPhoneNumber().equalsIgnoreCase("null") ? fieldAgent.getPhoneNumber() : "-"));

        logbookListView.addHeaderView(myHeader);

        ((MyLogbook)(getParentFragment())).titleBarLayout.setTxtLeft("Back");
        ((MyLogbook)(getParentFragment())).titleBarLayout.setOnIconClickListener(this);


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
        ((MyLogbook)(getParentFragment())).titleBarLayout.setTxtLeft("");
        ((MyLogbook)(getParentFragment())).popFragment();
    }
}
