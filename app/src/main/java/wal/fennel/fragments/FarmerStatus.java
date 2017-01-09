package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmList;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.adapters.FarmerStatusAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.FontTextView;
import wal.fennel.views.TitleBarLayout;

/**
 * Created by Khawar on 15/11/2016.
 */
public class FarmerStatus extends BaseFragment {

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    CircleImageView cIvIconRight;
    @Bind(R.id.lvFarmers)
    ListView mLvFarmers;

    private Farmer farmer;

    public static FarmerStatus newInstance(String title, Farmer farmer) {
        FarmerStatus fragment = new FarmerStatus();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("farmer", farmer);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_farmer_status, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.farmer = getArguments().getParcelable("farmer");

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if(!thumbUrl.isEmpty()) {
            if(NetworkHelper.isNetAvailable(getActivity())) {
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            } else {
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ViewGroup myHeader = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.header_farmer_info, mLvFarmers, false);
        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);

        CircleImageView ivFarmerThumb = (CircleImageView) myHeader.findViewById(R.id.ivFarmerThumb);
        if(NetworkHelper.isNetAvailable(getActivity())) {
            MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
        } else {
            MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
        }

        FontTextView tvFarmerName = (FontTextView) myHeader.findViewById(R.id.tvFullName);
        FontTextView tvFullName = (FontTextView) myHeader.findViewById(R.id.tvLocation);
        FontTextView tvMobile = (FontTextView) myHeader.findViewById(R.id.tvMobile);

        tvFarmerName.setText(farmer.getFullName());
        tvFullName.setText(farmer.getVillageName() + ", " + farmer.getSubLocation());
        tvMobile.setText("MOBILE " + farmer.getMobileNumber());

        mLvFarmers.addHeaderView(myHeader);

        RealmList<Task> completedTasks = new RealmList<>();
        RealmList<Task> inProgressTasks = new RealmList<>();
        RealmList<Task> notStartedTasks = new RealmList<>();

        final RealmList<Task> allTasks = new RealmList<>();

        for (int i = 0; i < farmer.getFarmerTasks().size(); i++) {
            Task task = farmer.getFarmerTasks().get(i);
            if(task.getStatus().equalsIgnoreCase(Constants.STR_COMPLETED)){
                completedTasks.add(task);
            } else if(task.getStatus().equalsIgnoreCase(Constants.STR_IN_PROGRESS)){
                inProgressTasks.add(task);
            } else if(task.getStatus().equalsIgnoreCase(Constants.STR_NOT_STARTED)){
                notStartedTasks.add(task);
            }
        }

        if(inProgressTasks.size() > 0){
            allTasks.add(new Task("", Constants.STR_IN_PROGRESS, "", "", "", "", true, null, false));
            allTasks.addAll(inProgressTasks);
        }

        if(notStartedTasks.size() > 0){
            allTasks.add(new Task("", Constants.STR_NOT_STARTED, "", "", "", "", true, null, false));
            allTasks.addAll(notStartedTasks);
        }

        if(completedTasks.size() > 0){
            allTasks.add(new Task("", Constants.STR_COMPLETED, "", "", "", "", true, null, false));
            allTasks.addAll(completedTasks);
        }

        FarmerStatusAdapter adapter = new FarmerStatusAdapter(getActivity(), allTasks);
        // Create the list view and bind the adapter
        mLvFarmers.setAdapter(adapter);
        mLvFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= mLvFarmers.getHeaderViewsCount();
                if(!allTasks.get(position).isHeader())
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(VisitLog.newInstance(Constants.STR_VISIT_LOG, farmer, allTasks.get(position)), true);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

    @Override
    public void onTitleBarLeftIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).popFragment();
    }
}
