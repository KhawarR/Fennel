package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.activities.LoginActivity;
import wal.fennel.adapters.FarmerStatusAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.WebApi;
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

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.lvFarmers)
    ListView mLvFarmers;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    CircleImageView cIvIconRight;

    private Farmer farmer;

    private ArrayList<Task> dummyTaskList = new ArrayList<>();

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

        dummyDataPop();

        FarmerStatusAdapter adapter = new FarmerStatusAdapter(getActivity(), dummyTaskList);
        // Create the list view and bind the adapter
        mLvFarmers.setAdapter(adapter);
    }

    private void dummyDataPop(){

        Task doing = new Task("", "DOING", "", "", "", "", true);
        Task task1 = new Task("1", "DummyTask1", "2016-11-10", null, "2016-11-30", Constants.STR_IN_PROGRESS, false);
        Task task2 = new Task("2", "DummyTask2", "2016-11-10", null, "2016-11-20", Constants.STR_IN_PROGRESS, false);

        Task notStarted = new Task("", "NOT STARTED", "", "", "", "", true);
        Task task3 = new Task("3", "DummyTask3", null, "2016-11-15", "2016-11-20", Constants.STR_NOT_STARTED, false);
        Task task4 = new Task("4", "DummyTask4", null, null, "2016-11-20", Constants.STR_NOT_STARTED, false);

        Task done = new Task("", "DONE", "", "", "", "", true);
        Task task5 = new Task("5", "DummyTask5", "2016-11-10", "2016-11-15", "2016-11-20", Constants.STR_COMPLETED, false);
        Task task6 = new Task("6", "DummyTask6", "2016-11-10", "2016-11-15", "2016-11-13", Constants.STR_COMPLETED, false);

        dummyTaskList.add(doing);
        dummyTaskList.add(task1);
        dummyTaskList.add(task2);
        dummyTaskList.add(notStarted);
        dummyTaskList.add(task3);
        dummyTaskList.add(task4);
        dummyTaskList.add(done);
        dummyTaskList.add(task5);
        dummyTaskList.add(task6);

    }

    @Override
    public void onResume(){
        super.onResume();
        getFarmerTaskItems();
    }

    private void getFarmerTaskItems(){

        loadingStarted();

        if(farmer.farmerTasks.size() > 0){
            String farmingTaskIds = "";
            for (int i = 0; i < farmer.farmerTasks.size(); i++) {
                String id = farmer.farmerTasks.get(i).taskId;
                id = "'" + id + "'";

                farmingTaskIds = farmingTaskIds + id;

                if(i + 1 != farmer.farmerTasks.size()){
                    farmingTaskIds = farmingTaskIds + ",";
                }
            }

            WebApi.getFarmingTaskItems(farmerStatusCallback, farmingTaskIds);
        }
    }

    private Callback<ResponseBody> farmerStatusCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private void parseData(String data) throws JSONException {

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
