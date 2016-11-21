package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.picasso.NetworkPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.adapters.MyFarmersAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.WebApi;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;
import wal.fennel.views.FontTextView;
import wal.fennel.views.TitleBarLayout;

/**
 * Created by Khawar on 15/11/2016.
 */
public class FarmerStatus extends BaseFragment {

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.lvFarmers)
    ListView mLvFarmers;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    CircleImageView cIvIconRight;

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
        if(!thumbUrl.isEmpty())
        {
            if(NetworkHelper.isNetAvailable(getActivity()))
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            else
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
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
        {
            if(NetworkHelper.isNetAvailable(getActivity()))
                MyPicassoInstance.getInstance().load(farmer.thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
            else
                MyPicassoInstance.getInstance().load(farmer.thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);

        }

        FontTextView tvFarmerName = (FontTextView) myHeader.findViewById(R.id.tvFullName);
        FontTextView tvFullName = (FontTextView) myHeader.findViewById(R.id.tvLocation);
        FontTextView tvMobile = (FontTextView) myHeader.findViewById(R.id.tvMobile);

        tvFarmerName.setText(farmer.fullName);
        tvFullName.setText(farmer.villageName + ", " + farmer.subLocation);
        tvMobile.setText("MOBILE " + farmer.mobileNumber);

        mLvFarmers.addHeaderView(myHeader);

        MyFarmersAdapter adapter = new MyFarmersAdapter(getActivity(), Singleton.getInstance().mySignupsList);
        // Create the list view and bind the adapter
        mLvFarmers.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        getFarmerTaskItems();
    }

    private void getFarmerTaskItems(){
        if(farmer.farmerTasks.size() > 0){
            for (int i = 0; i < farmer.farmerTasks.size(); i++) {
                final Task task = farmer.farmerTasks.get(i);
                WebApi.getFarmingTaskItems(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.i("","");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                }, task.taskId);
            }
        }
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
