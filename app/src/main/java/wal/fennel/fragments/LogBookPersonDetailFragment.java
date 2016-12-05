package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import wal.fennel.R;
import wal.fennel.adapters.TeamLogBookAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.views.FontTextView;

/**
 * Created by irfanayaz on 12/5/16.
 */
public class LogBookPersonDetailFragment extends BaseFragment {

    @Bind(R.id.logbook_listview)
    ListView logbookListView;
    TeamLogBookAdapter logBookAdapter;

    public static LogBookPersonDetailFragment newInstance(Farmer clickedFarmer) {
        LogBookPersonDetailFragment fragment = new LogBookPersonDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", clickedFarmer);
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

        ArrayList<String> descriptionList = new ArrayList<>();

        descriptionList.add("Alex checked out of Bahati Kahandi's farm");
        descriptionList.add("Alex updated Bahati Kahandi's hole count to 122");
        descriptionList.add("Alex completed a survey at Bahati Kahandi's farm");
        descriptionList.add("Alex checked into Bahati Kahandi's farm");

        logBookAdapter = new TeamLogBookAdapter(getActivity(), descriptionList);
        logbookListView.setAdapter(logBookAdapter);

        ViewGroup myHeader = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.header_farmer_info, logbookListView, false);
        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);

        FontTextView tvFarmerName = (FontTextView) myHeader.findViewById(R.id.tvFullName);
        tvFarmerName.setText("Alex Ferguson");
        FontTextView tvLocation = (FontTextView) myHeader.findViewById(R.id.tvLocation);
        tvLocation.setVisibility(View.GONE);
        FontTextView tvMobile = (FontTextView) myHeader.findViewById(R.id.tvMobile);
        tvMobile.setText("090078601");

        logbookListView.addHeaderView(myHeader);


//        CircleImageView ivFarmerThumb = (CircleImageView) myHeader.findViewById(R.id.ivFarmerThumb);
//        {
//            if(NetworkHelper.isNetAvailable(getActivity()))
//                MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
//            else
//                MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
//
//        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }
}
