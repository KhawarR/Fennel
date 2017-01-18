package wal.fennel.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.NetworkPolicy;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.BuildConfig;
import wal.fennel.R;
import wal.fennel.dropbox.DropboxClient;
import wal.fennel.dropbox.UploadTask;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.WebApi;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.FontTextView;
import wal.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class AboutMe extends Activity implements TitleBarLayout.TitleBarIconClickListener,
        WebApi.OnSyncCompleteListener {

    private MixpanelAPI mixPanel;

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.tvVersionNumber)
    FontTextView tvVersionNumber;

    @Bind(R.id.tv_first_name)
    FontTextView tvFirstName;

    @Bind(R.id.tv_second_name)
    FontTextView tvSecondName;

    @Bind(R.id.tv_sur_name)
    FontTextView tvSurname;

    @Bind(R.id.tv_field_officer)
    FontTextView tvFieldOfficer;

    @Bind(R.id.tv_field_manager)
    FontTextView tvFieldManager;

    @Bind(R.id.tvSyncTime)
    FontTextView tvSyncTime;

    @Bind(R.id.profile_image)
    CircleImageView cIvProfileMain;

    @Bind(R.id.pbSync)
    ProgressBar pbSync;

    private ImagePickerCallback imagePickerCallback;
    private CameraImagePicker cameraImagePicker;
    private ProgressDialog mProgressDialog;
    private CircleImageView cIvIconRight;
    private ImagePicker imagePicker;

    private String loggedInUserId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_about_me);
        ButterKnife.bind(this);

        mixPanel = MixpanelAPI.getInstance(this, MixPanelConstants.MIXPANEL_TOKEN);
        mixPanel.track(MixPanelConstants.PageView.ABOUT_ME);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.loading));

        imagePicker = new ImagePicker(AboutMe.this);
        cameraImagePicker = new CameraImagePicker(AboutMe.this);

        MyPicassoInstance.getInstance().load(R.drawable.dummy_profile)
                .transform(new CircleViewTransformation())
                .placeholder(R.drawable.dummy_profile)
                .error(R.drawable.dummy_profile).into(cIvProfileMain);
        loggedInUserId = PreferenceHelper.getInstance().readLoginUserId();

        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);
        titleBarLayout.setOnIconClickListener(this);

        if(PreferenceHelper.getInstance().readIsSyncInProgress()){
            WebApi.setSyncCompleteListener(this);
            pbSync.setVisibility(View.VISIBLE);
        }

        populateView();
    }

    private void loadAttachment() {
        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if(!thumbUrl.isEmpty()) {
            if(NetworkHelper.isNetAvailable(AboutMe.this)) {
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            } else {
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
        }
    }

    private void populateView() {
        tvVersionNumber.setText(Constants.STR_FENNEL_VERSION + BuildConfig.VERSION_NAME);
        tvFieldOfficer.setText(PreferenceHelper.getInstance().readAboutFOname());
        tvFieldManager.setText(PreferenceHelper.getInstance().readAboutFMname());
        tvSyncTime.setText(PreferenceHelper.getInstance().readLastSyncTime());
        tvSecondName.setText(PreferenceHelper.getInstance().readAboutMN());
        tvFirstName.setText(PreferenceHelper.getInstance().readAboutFN());
        tvSurname.setText(PreferenceHelper.getInstance().readAboutLN());

        loadAttachment();
    }

    @OnClick(R.id.txtSignOut)
    void onClickSignOut(View view) {

        mixPanel.track(MixPanelConstants.Event.SIGNOUT_BUTTON);

        if(WebApi.isSyncRequired())
            Toast.makeText(getApplicationContext(), "Must sync data before Signout", Toast.LENGTH_SHORT).show();
        else
            showLogoutDialog();
    }

    @OnClick(R.id.txtSyncData)
    void onClickSyncData(View view) {

        pbSync.setVisibility(View.VISIBLE);

        mixPanel.track(MixPanelConstants.Event.MANUAL_SYNC_ACTION);

        if(NetworkHelper.isNetAvailable(getApplicationContext())){

            uploadFarmerLogFile();

            if(!PreferenceHelper.getInstance().readIsSyncInProgress()){
                if(WebApi.isSyncRequired())
                    WebApi.syncAll(AboutMe.this);
                else {
                    pbSync.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Data is already synced", Toast.LENGTH_SHORT).show();
                    WebApi.getFullServerData();
                }
            }else {
                pbSync.setVisibility(View.GONE);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
            pbSync.setVisibility(View.GONE);
        }
    }

    private void uploadDebugLogFile(){

        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(downloadsDirectory.exists()){
            String downloadDirPath = downloadsDirectory.getAbsolutePath();
            String debugLogFileName = PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.DEBUG_LOGS_FILE_NAME;

            File debugLogsFile = new File(downloadDirPath + File.separator + debugLogFileName);
            if(debugLogsFile.exists()) {
                uploadDropboxFile(debugLogsFile, Constants.DropboxConstants.DEBUG_LOGS_DROPBOX_PATH);
            }
            else {
                Crashlytics.logException(new Throwable("Debug log file doesn't exist - " + PreferenceHelper.getInstance().readUserId()));
            }
        }
        else {
            Crashlytics.logException(new Throwable("Download directory doesn't exist"));
        }
    }

    private void uploadFarmerLogFile(){

        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(downloadsDirectory.exists()){
            String downloadDirPath = downloadsDirectory.getAbsolutePath();
            String farmerLogFileName = PreferenceHelper.getInstance().readUserId() + Constants.DropboxConstants.FARMER_LOGS_FILE_NAME;

            File farmerLogsFile = new File(downloadDirPath + File.separator + farmerLogFileName);
            if(farmerLogsFile.exists()) {
                uploadDropboxFile(farmerLogsFile, Constants.DropboxConstants.FARMER_LOGS_DROPBOX_PATH);
            }
            else {
                Crashlytics.logException(new Throwable("Farmer log file doesn't exist - " + PreferenceHelper.getInstance().readUserId()));
            }
        }
        else {
            Crashlytics.logException(new Throwable("Download directory doesn't exist"));
        }
    }

    private void uploadDropboxFile(File file, String fileDropboxPath){
        new UploadTask(DropboxClient.getClient(Constants.DropboxConstants.ACCESS_TOKEN), file, getApplicationContext(), fileDropboxPath).execute();
    }

    @OnClick(R.id.rl_pick_image)
    void onClickPickImage(View view) {

        mixPanel.track(MixPanelConstants.Event.CHANGE_AVATAR_BUTTON);

        showPickerDialog();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(AboutMe.this);
        pickerDialog.setTitle("Do you want to sign out?");
        pickerDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceHelper.getInstance().clearSession(true);
                        Intent intent = new Intent(AboutMe.this, SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                    }
                });
        pickerDialog.setNegativeButton("I'm staying",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
    }

    private void showPickerDialog() {
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(AboutMe.this);
        pickerDialog.setTitle("Choose image from?");
        pickerDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pickImage(true);
                        dialog.dismiss();
                    }
                });
        pickerDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pickImage(false);
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
    }

    private void pickImage(boolean isGallery) {

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                String originalPath = images.get(0).getOriginalPath();
                String uri = NetworkHelper.getUriFromPath(originalPath);
//                if(NetworkHelper.isNetAvailable(AboutMe.this))
//                {
//                    PreferenceHelper.getInstance().writeAboutIsSyncReq(false);
//                    addPictureAttachment(originalPath);
//                }
//                else
//                {
                    PreferenceHelper.getInstance().writeAboutIsSyncReq(true);
                    PreferenceHelper.getInstance().writeAboutAttUrl(uri);
//                }
                MyPicassoInstance.getInstance().load(uri).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(uri).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }

            @Override
            public void onError(String message) {
                // Do error handling
            }
        };

        if (isGallery) {
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
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            } else if (requestCode == Picker.PICK_IMAGE_CAMERA) {
                cameraImagePicker.submit(data);
            }
        }
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {

    }

    @Override
    public void onTitleBarLeftIconClicked(View view) {
        backToMain();
    }

    private void backToMain() {
        mixPanel.track(MixPanelConstants.Event.BACK_BUTTON);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMain();
    }

    @Override
    public void syncCompleted() {
        if(pbSync != null)
            pbSync.setVisibility(View.GONE);

        String syncTime = PreferenceHelper.getInstance().readLastSyncTime();
        if(syncTime == null || syncTime.isEmpty())
            syncTime = "-";

        tvSyncTime.setText(syncTime);

        uploadDebugLogFile();
    }
}
