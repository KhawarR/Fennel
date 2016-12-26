package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.adapters.FarmerStatusAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.FontTextView;
import wal.fennel.views.TitleBarLayout;

public class VisitLog extends BaseFragment {

    private CircleImageView cIvIconRight;
    @Bind(R.id.llTaskItemContainer)
    LinearLayout llTaskItemContainer;
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    @Bind(R.id.stub_farmer_header)
    ViewStub stubFarmerHeader;
    @Bind(R.id.tvTaskHeader)
    FontTextView tvTaskHeader;

    private ArrayList<TaskItem> localTaskItems;
    private RealmResults<TaskItem> taskItems;
    private Farmer farmer;
    private Task task;

    public static VisitLog newInstance(String title, Farmer farmer, Task task) {
        VisitLog fragment = new VisitLog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("farmer", farmer);
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_visit_log, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.farmer = getArguments().getParcelable("farmer");
        this.task = getArguments().getParcelable("task");
        this.taskItems = Realm.getDefaultInstance().where(TaskItem.class).equalTo("farmingTaskId", task.getTaskId()).findAll().sort("sequence", Sort.ASCENDING);
        this.localTaskItems = new ArrayList<>();
        for (int i = 0; i < taskItems.size(); i++) {
            this.localTaskItems.add(new TaskItem(taskItems.get(i)));
        }

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if (!thumbUrl.isEmpty()) {
            if (NetworkHelper.isNetAvailable(getActivity())) {
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            } else {
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
        }

        View farmerHeader = stubFarmerHeader.inflate();
        farmerHeader.setEnabled(false);
        farmerHeader.setOnClickListener(null);

        CircleImageView ivFarmerThumb = (CircleImageView) farmerHeader.findViewById(R.id.ivFarmerThumb);
        if (NetworkHelper.isNetAvailable(getActivity())) {
            MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
        } else {
            MyPicassoInstance.getInstance().load(farmer.getThumbUrl()).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(ivFarmerThumb);
        }

        FontTextView tvFarmerName = (FontTextView) farmerHeader.findViewById(R.id.tvFullName);
        FontTextView tvFullName = (FontTextView) farmerHeader.findViewById(R.id.tvLocation);
        FontTextView tvMobile = (FontTextView) farmerHeader.findViewById(R.id.tvMobile);

        tvFarmerName.setText(farmer.getFullName());
        tvFullName.setText(farmer.getVillageName() + ", " + farmer.getSubLocation());
        tvMobile.setText("MOBILE " + farmer.getMobileNumber());

        tvTaskHeader.setText(task.getName());

        populateTaskItems();
    }

    private void populateTaskItems(){

        llTaskItemContainer.removeAllViews();

        for (int i = 0; i < localTaskItems.size(); i++) {

            View vTaskItem;
            final TaskItem taskItem = localTaskItems.get(i);

            if(taskItem.isTaskDone()){
                vTaskItem = getActivity().getLayoutInflater().inflate(R.layout.template_visit_log_completed, null);
            } else {
                vTaskItem = getActivity().getLayoutInflater().inflate(R.layout.template_visit_log, null);
            }

            FontTextView tvTitle = (FontTextView) vTaskItem.findViewById(R.id.tvTitle);
            FontTextView tvDescription = (FontTextView) vTaskItem.findViewById(R.id.tvDescription);
            final EditText etHoleCount = (EditText) vTaskItem.findViewById(R.id.etInput);
            RelativeLayout rlBlockButton = (RelativeLayout) vTaskItem.findViewById(R.id.rlBlockButton);
            ImageView ivBlockIcon = (ImageView) vTaskItem.findViewById(R.id.ivBlockIcon);
            Spinner spOption = (Spinner) vTaskItem.findViewById(R.id.spOptions);

            if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Gps.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setVisibility(View.GONE);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.GONE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTaskDone(taskItem);
                    }
                });
                if(!taskItem.isTaskDone()) {
                    ivBlockIcon.setImageResource(R.drawable.ic_gps);
                }

            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Checkbox.toString())){

                tvTitle.setText(taskItem.getName());
                if(taskItem.getOptions() != null && taskItem.getOptions().size() > 0){
                    tvDescription.setText(taskItem.getOptions().get(0).getName());
                }
                tvDescription.setVisibility(View.VISIBLE);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.GONE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTaskDone(taskItem);
                    }
                });
                if(!taskItem.isTaskDone()) {
                    ivBlockIcon.setImageResource(R.drawable.ic_tick_white);
                }

            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Text.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setVisibility(View.GONE);
                etHoleCount.setText(taskItem.getTextValue());
                etHoleCount.setVisibility(View.VISIBLE);
                spOption.setVisibility(View.GONE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        taskItem.setTextValue(etHoleCount.getText().toString());
                        setTaskDone(taskItem);
                    }
                });
                if(!taskItem.isTaskDone()) {
                    ivBlockIcon.setImageResource(R.drawable.ic_pencil);
                }

            }  else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.File.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setText(taskItem.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.GONE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTaskDone(taskItem);
                    }
                });
                if(!taskItem.isTaskDone()) {
                    if(taskItem.getFileActionType().equalsIgnoreCase(Constants.STR_VIEW_MEDIA)){
                        ivBlockIcon.setImageResource(R.drawable.ic_eye);
                    } else {
                        ivBlockIcon.setImageResource(R.drawable.ic_camera_white);
                    }
                }
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Options.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setVisibility(View.GONE);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.VISIBLE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTaskDone(taskItem);
                    }
                });
                if(!taskItem.isTaskDone()) {
                    ivBlockIcon.setImageResource(R.drawable.ic_pencil);
                }
            }

            llTaskItemContainer.addView(vTaskItem);
        }
    }

    private void setTaskDone(final TaskItem taskItem) {
//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        taskItem.setTaskDone(!taskItem.isTaskDone());
//        realm.commitTransaction();
        taskItem.setTaskDone(!taskItem.isTaskDone());
        populateTaskItems();
    }

    @OnClick(R.id.txtSave)
    void onClickSaveTask() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        task.setStatus(Constants.STR_IN_PROGRESS);
        for (int i = 0; i < localTaskItems.size(); i++) {

            TaskItem taskItem = localTaskItems.get(i);

            taskItems.get(i).setTaskDone(taskItem.isTaskDone());

            if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Text.toString())){
                taskItems.get(i).setTextValue(taskItem.getTextValue());
            }

        }
        realm.commitTransaction();
        ((BaseContainerFragment) getParentFragment()).popFragment();
    }

    @OnClick(R.id.txtSubmitApproval)
    void onClickSubmitForApprovalTask() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        task.setStatus(Constants.STR_COMPLETED);
        realm.commitTransaction();
        ((BaseContainerFragment) getParentFragment()).popFragment();
    }

    @Override
    public void onResume() {
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
