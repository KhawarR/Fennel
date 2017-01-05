package wal.fennel.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.NetworkPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import wal.fennel.location.GPSTracker;
import wal.fennel.models.FarmVisit;
import wal.fennel.models.FarmVisitLog;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.FontTextView;
import wal.fennel.views.NothingSelectedSpinnerAdapter;
import wal.fennel.views.TitleBarLayout;

public class VisitLog extends BaseFragment  {

    @Bind(R.id.llTaskItemContainer)
    LinearLayout llTaskItemContainer;
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    @Bind(R.id.stub_farmer_header)
    ViewStub stubFarmerHeader;
    @Bind(R.id.tvTaskHeader)
    FontTextView tvTaskHeader;

    private CameraImagePicker cameraImagePicker;
    private CircleImageView cIvIconRight;
    private ImagePicker imagePicker;

    private ArrayList<TaskItem> localTaskItems;
    private RealmResults<TaskItem> taskItems;
    private GPSTracker gps;
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

        imagePicker = new ImagePicker(VisitLog.this);
        cameraImagePicker = new CameraImagePicker(VisitLog.this);

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

    @Override
    public void onResume() {
        super.onResume();
        gps = new GPSTracker(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
    }

    private void populateTaskItems(){

        llTaskItemContainer.removeAllViews();

        for (int i = 0; i < localTaskItems.size(); i++) {

            View vTaskItem;
            final TaskItem taskItem = localTaskItems.get(i);

            if(taskItem.isTaskDone() && !taskItem.getFileActionType().equalsIgnoreCase(Constants.STR_ATTACH_MEDIA)){
                vTaskItem = getActivity().getLayoutInflater().inflate(R.layout.template_visit_log_completed, null);
            } else {
                vTaskItem = getActivity().getLayoutInflater().inflate(R.layout.template_visit_log, null);
            }

            FontTextView tvTitle = (FontTextView) vTaskItem.findViewById(R.id.tvTitle);
            final FontTextView tvDescription = (FontTextView) vTaskItem.findViewById(R.id.tvDescription);
            final EditText etHoleCount = (EditText) vTaskItem.findViewById(R.id.etInput);
            RelativeLayout rlBlockButton = (RelativeLayout) vTaskItem.findViewById(R.id.rlBlockButton);
            final ImageView ivBlockIcon = (ImageView) vTaskItem.findViewById(R.id.ivBlockIcon);
            final RoundedImageView roundedImageView = (RoundedImageView) vTaskItem.findViewById(R.id.ivBlockBackground);
            Spinner spOption = (Spinner) vTaskItem.findViewById(R.id.spOptions);

            if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Gps.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setVisibility(View.VISIBLE);
                String description = taskItem.getDescription() == null ? "" : taskItem.getDescription().trim();
                description = description.equalsIgnoreCase("null") ? "" : description;
                tvDescription.setText(description);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.GONE);

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!taskItem.isTaskDone()){
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            String gpsStamp = getGpsStamp(latitude, longitude);
                            taskItem.setDescription(gpsStamp);
                        }
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
                        if(taskItem.getOptions() != null && taskItem.getOptions().size() > 0) {
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            taskItem.getOptions().get(0).setValue(!taskItem.getOptions().get(0).isValue());
                            realm.commitTransaction();
                        }
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
                        if(taskItem.getFileActionType().equalsIgnoreCase(Constants.STR_VIEW_MEDIA)){
                            String path = taskItem.getAttachmentPath();
                            if(path != null && !path.trim().isEmpty()) {
                                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                                String mimeType = myMime.getMimeTypeFromExtension(FennelUtils.fileExt(path));
                                newIntent.setDataAndType(Uri.fromFile(new File(path)),mimeType);
                                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    getActivity().startActivity(newIntent);
                                    setTaskDone(taskItem);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getActivity(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "File is not available", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            showPickerDialog(roundedImageView, ivBlockIcon, taskItem);
                        }
                    }
                });
                if(taskItem.getFileActionType().equalsIgnoreCase(Constants.STR_VIEW_MEDIA)){
                    if(taskItem.isTaskDone()) {

                    } else {
                        ivBlockIcon.setImageResource(R.drawable.ic_eye);
                    }
                } else {
                    if(taskItem.isTaskDone()) {
                        String path = taskItem.getAttachmentPath();
                        if(path != null && !path.isEmpty()) {

                            if(NetworkHelper.isNetAvailable(getActivity()))
                            {
                                MyPicassoInstance.getInstance().load(path).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().into(roundedImageView);
                            }
                            else
                            {
                                MyPicassoInstance.getInstance().load(path).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().into(roundedImageView);
                            }
                            ivBlockIcon.setVisibility(View.GONE);
                        }
                    } else {
                        roundedImageView.setBackgroundResource(R.drawable.green_rounded_rect);
                        ivBlockIcon.setImageResource(R.drawable.ic_camera_white);
                    }
                }
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Options.toString())){

                tvTitle.setText(taskItem.getName());
                tvDescription.setVisibility(View.GONE);
                etHoleCount.setVisibility(View.GONE);
                spOption.setVisibility(View.VISIBLE);
                final ArrayList<String> arrOptionNames = new ArrayList<>();

                if(taskItem.getOptions() != null) {
                    for (int j = 0; j < taskItem.getOptions().size(); j++) {
                        arrOptionNames.add(taskItem.getOptions().get(j).getName());
                    }
                }

                ArrayAdapter<String> arrayAdapterLoc = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, arrOptionNames);
                spOption.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterLoc, R.layout.spinner_nothing_selected, getContext(), "OPTIONS"));
                spOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        int position = pos - 1;
                        if(position >= 0) {
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            for (int j = 0; j < taskItem.getOptions().size(); j++) {
                                taskItem.getOptions().get(j).setValue(false);
                            }
                            taskItem.getOptions().get(position).setValue(true);
                            realm.commitTransaction();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                rlBlockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTaskDone(taskItem);
                    }
                });
                if(taskItem.isTaskDone()) {
                    for (int j = 0; j < taskItem.getOptions().size(); j++) {
                        if(taskItem.getOptions().get(j).isValue()) {
                            spOption.setSelection(j + 1);
                            break;
                        }
                    }
                } else {
                    ivBlockIcon.setImageResource(R.drawable.ic_pencil);
                }
            }

            llTaskItemContainer.addView(vTaskItem);
        }
    }

    private void showPickerDialog(final RoundedImageView roundedImageView, final ImageView ivIcon, final TaskItem taskItem) {
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(getActivity());
        pickerDialog.setTitle("Choose image from?");
        pickerDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pickFarmerImage(true, roundedImageView, ivIcon, taskItem);
                        dialog.dismiss();
                    }
                });
        pickerDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pickFarmerImage(false, roundedImageView, ivIcon, taskItem);
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
    }

    private void pickFarmerImage(boolean isDevice, final RoundedImageView roundedImageView, final ImageView ivIcon, final TaskItem taskItem) {
        ImagePickerCallback imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                roundedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String originalPath = images.get(0).getOriginalPath();
                String uri = NetworkHelper.getUriFromPath(originalPath);

                taskItem.setAttachmentPath(uri);

                MyPicassoInstance.getInstance().load(uri).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().into(roundedImageView);
                ivIcon.setVisibility(View.GONE);

                taskItem.setTaskDone(true);
            }

            @Override
            public void onError(String message) {
                // Do error handling
                Log.i("LP", message);
            }
        };
        if (isDevice) {
            imagePicker.setImagePickerCallback(imagePickerCallback);
            imagePicker.pickImage();
        } else {
            cameraImagePicker.setImagePickerCallback(imagePickerCallback);
            cameraImagePicker.pickImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            } else if (requestCode == Picker.PICK_IMAGE_CAMERA) {
                cameraImagePicker.submit(data);
            }
        }
    }

    private String getGpsStamp(double latitude, double longitude) {
        int maxStrLength = 9;
        String strLatitude = String.valueOf(latitude);
        try {
             strLatitude = strLatitude.substring(0, maxStrLength);
        } catch (StringIndexOutOfBoundsException e) {
             e.printStackTrace();
        }
        String strLongitude = String.valueOf(longitude);
        try {
            strLongitude = strLongitude.substring(0, maxStrLength);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        String time = FennelUtils.getFormattedTime(System.currentTimeMillis(), Constants.STR_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS);
        String gpsStamp = "Latitude: %s\nLongitude: %s\nTime: %s";

        gpsStamp = String.format(gpsStamp, strLatitude, strLongitude, time);
        return gpsStamp;
    }

    private void setTaskDone(final TaskItem taskItem) {
        taskItem.setTaskDone(!taskItem.isTaskDone());
        populateTaskItems();
    }

    @OnClick(R.id.txtSave)
    void onClickSaveTask() {
        updateTaskItems(Constants.STR_IN_PROGRESS);
    }

    @OnClick(R.id.txtSubmitApproval)
    void onClickSubmitForApprovalTask() {
        updateTaskItems(Constants.STR_COMPLETED);
    }

    private void updateTaskItems(String taskStatus) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        task.setStatus(taskStatus);
        task.setDataDirty(true);
        for (int i = 0; i < localTaskItems.size(); i++) {

            TaskItem taskItem = localTaskItems.get(i);

            taskItems.get(i).setTaskDone(taskItem.isTaskDone());
            taskItems.get(i).setDataDirty(true);

            if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Text.toString())){
                taskItems.get(i).setTextValue(taskItem.getTextValue());
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Gps.toString())){
                taskItems.get(i).setDescription(taskItem.getDescription());
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Checkbox.toString())){
                if(taskItem.getOptions() != null
                        && taskItem.getOptions().size() > 0
                        && taskItems.get(i).getOptions() != null
                        && taskItems.get(i).getOptions().size() > 0) {
                    taskItems.get(i).getOptions().get(0).setValue(taskItem.getOptions().get(0).isValue());
                    taskItems.get(i).getOptions().get(0).setDataDirty(true);
                }
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.File.toString())){
                if(taskItem.getFileActionType().equalsIgnoreCase(Constants.STR_VIEW_MEDIA)){

                } else {
                    taskItems.get(i).setAttachmentPath(taskItem.getAttachmentPath());
                }
            } else if(taskItem.getRecordType().equalsIgnoreCase(Constants.TaskItemType.Options.toString())){
                for (int j = 0; j < taskItem.getOptions().size(); j++) {
                    if(taskItem.getOptions().get(j).isValue()) {
                        taskItems.get(i).getOptions().get(j).setValue(true);
                        taskItems.get(i).getOptions().get(j).setDataDirty(true);
                    }
                }
            }
        }

        RealmResults<FarmVisit> farmVisits = Realm.getDefaultInstance().where(FarmVisit.class).equalTo("shambaId", farmer.getFarmId()).findAll().sort("visitedDate", Sort.DESCENDING);

        boolean createNewFarmVisit = false;
        FarmVisit farmVisit = null;
        if(farmVisits != null && farmVisits.size() > 0) {
            farmVisit = farmVisits.get(0);

            if(!DateUtils.isToday(farmVisit.getVisitedDate())) {
                createNewFarmVisit = true;
            }
        } else {
            createNewFarmVisit = true;
        }

        if (createNewFarmVisit) {
            String farmVisitId = Constants.STR_FARMER_ID_PREFIX + String.valueOf(System.currentTimeMillis());

            farmVisit = realm.createObject(FarmVisit.class);
            farmVisit.setAll(farmVisitId, farmer.getFarmId(), farmer.getFarmerId(),
                    PreferenceHelper.getInstance().readLoginUserId(),
                    PreferenceHelper.getInstance().readLoginUserType(),
                    System.currentTimeMillis(), true);
        } else {
            farmVisit.setDataDirty(true);
        }

        FarmVisitLog visitLog = realm.createObject(FarmVisitLog.class);
        visitLog.setAll(farmVisit.getFarmVisitId(), task.getTaskId(), true);

        realm.commitTransaction();
        ((BaseContainerFragment) getParentFragment()).popFragment();
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