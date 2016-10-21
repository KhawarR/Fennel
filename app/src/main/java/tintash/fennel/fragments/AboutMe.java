package tintash.fennel.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.squareup.picasso.NetworkPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.activities.LoginActivity;
import tintash.fennel.application.Fennel;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.network.WebApi;
import tintash.fennel.utils.CircleViewTransformation;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.MyPicassoInstance;
import tintash.fennel.utils.PhotoUtils;
import tintash.fennel.utils.PreferenceHelper;
import tintash.fennel.views.FontTextView;
import tintash.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class AboutMe extends BaseFragment {

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

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

    @Bind(R.id.profile_image)
    CircleImageView cIvProfileMain;

    CircleImageView cIvIconRight;

    private ImagePicker imagePicker;
    private ImagePickerCallback imagePickerCallback;
    private CameraImagePicker cameraImagePicker;

    private String pictureAttachmentId = null;
    private String loggedInUserId = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about_me, null);
        ButterKnife.bind(this, view);

        imagePicker = new ImagePicker(AboutMe.this);
        cameraImagePicker = new CameraImagePicker(AboutMe.this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyPicassoInstance.getInstance().load(R.drawable.dummy_profile).transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
        loggedInUserId = PreferenceHelper.getInstance().readLoginUserId();

        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        populateView();
        if(!PreferenceHelper.getInstance().readAboutAttId().isEmpty())
        {
            String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), PreferenceHelper.getInstance().readAboutAttId());
            if(NetworkHelper.isNetworkAvailable(getActivity()))
            {
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
            else
            {
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
        }
        WebApi.getAboutMeInfo(aboutMeCallback);
    }

    private void populateView()
    {
        tvFirstName.setText(PreferenceHelper.getInstance().readAboutFN());
        tvSecondName.setText(PreferenceHelper.getInstance().readAboutMN());
        tvSurname.setText(PreferenceHelper.getInstance().readAboutLN());
        tvFieldOfficer.setText(PreferenceHelper.getInstance().readAboutFOname());
        tvFieldManager.setText(PreferenceHelper.getInstance().readAboutFMname());
    }

    private Callback<ResponseBody> aboutMeAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        String attId = parseDataAttachment(responseStr);
                        pictureAttachmentId = attId;
                        if(!attId.isEmpty() && !attId.equalsIgnoreCase(PreferenceHelper.getInstance().readAboutAttId()))
                        {
                            String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), attId);
                            if(NetworkHelper.isNetworkAvailable(getActivity()))
                            {
                                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
                            }
                            else
                            {
                                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else {
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

    private String parseDataAttachment(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0)
        {
            JSONObject facObj = arrRecords.getJSONObject(0);

            JSONObject attachmentObj = facObj.optJSONObject("Attachments");
            if(attachmentObj != null)
            {
                JSONArray attRecords = attachmentObj.getJSONArray("records");
                if(attRecords.length() > 0)
                {
                    JSONObject objFarmerPhoto = attRecords.getJSONObject(0);
                    String idAttachment = objFarmerPhoto.getString("Id");
                    return idAttachment;
                }
            }
        }

        return "";
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        showPickerDialog();
    }

    @OnClick(R.id.txtSignOut)
    void onClickSignOut(View view) {
        showLogoutDialog();
    }

    @OnClick(R.id.rl_pick_image)
    void onClickPickImage(View view) {
        showPickerDialog();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(getActivity());
        pickerDialog.setTitle("Do you want to sign out?");
        pickerDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceHelper.getInstance().clearSession();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
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
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(getActivity());
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
                addPictureAttachment(images.get(0).getOriginalPath());
                MyPicassoInstance.getInstance().load(images.get(0).getQueryUri()).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                MyPicassoInstance.getInstance().load(images.get(0).getQueryUri()).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
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

    public void addPictureAttachment(String imageUri) {

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "picture");
        attachmentMap.put("Name", "profile_picture.png");
        if (pictureAttachmentId == null || pictureAttachmentId.isEmpty()) {
            attachmentMap.put("ParentId", loggedInUserId);
        }
        String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), PreferenceHelper.getInstance().readAboutAttId());
        MyPicassoInstance.getInstance().invalidate(thumbUrl);

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        bmp = PhotoUtils.decodeSampledBitmapFromResource(imageUri);

        if(bmp != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byteArrayImage = bos.toByteArray();
            try {
                bos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(imageUri));
        }

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        if (pictureAttachmentId == null || pictureAttachmentId.isEmpty()) {

            WebApi.addAttachment(addAttachmentCallback, entityBody, imageBody);

        } else {
            WebApi.editAttachment(editAttachmentCallback, pictureAttachmentId, entityBody, imageBody);
        }
    }

    Callback<ResponseBody> addAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("Fennel", "facilitator profile picture uploaded successfully!");
                String responseStr = null;

                try {
                    responseStr = response.body().string();
                    pictureAttachmentId = getAttachmentIdFromUploadSuccess(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(response.code() == 401)
            {
                PreferenceHelper.getInstance().clearSession();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            } else {
                Log.i("Fennel", "facilitator profile picture upload failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "facilitator profile picture upload failed!");
        }
    };

    Callback<ResponseBody> editAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("Fennel", "facilitator profile picture edited successfully!");
            }
            else if(response.code() == 401)
            {
                PreferenceHelper.getInstance().clearSession();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            } else {
                Log.i("Fennel", "facilitator profile picture edit failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "facilitator profile picture edit failed!");
        }
    };

    private String getAttachmentIdFromUploadSuccess(String data) {
        JSONObject responseJson = null;
        String attachmentId = null;
        try {
            responseJson = new JSONObject(data);
            attachmentId = responseJson.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attachmentId;
    }

    private Callback<ResponseBody> aboutMeCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseAboutMeData(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private void parseAboutMeData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            JSONObject objRecord = arrRecords.getJSONObject(0);

            String fn = (!(objRecord.getString("First_Name__c").equals("null"))) ? (objRecord.getString("First_Name__c")) : "";
            String mn = (!(objRecord.getString("Middle_Name__c").equals("null"))) ? (objRecord.getString("Middle_Name__c")) : "";
            String ln = (!(objRecord.getString("Last_Name__c").equals("null"))) ? (objRecord.getString("Last_Name__c")) : "";
            String fo_name = "";
            String fm_name = "";

            JSONObject objFacilitator = objRecord.optJSONObject("Facilitators__r");
            JSONObject objFieldOffice = objRecord.optJSONObject("Field_Officers__r");
            JSONObject objFieldManager = objRecord.optJSONObject("Field_Managers__r");

            if(objFacilitator != null)
            {
                getAndSaveId(objFacilitator, Constants.STR_FACILITATOR);
                JSONArray arrRec = objFacilitator.getJSONArray("records");
                if(arrRec.length() > 0)
                {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    JSONObject objFO = obj1.optJSONObject("Field_Officer__r");
                    if(objFO != null)
                    {
                        JSONObject objFOEmployee = objFO.optJSONObject("Employee__r");
                        if(objFOEmployee != null)
                        {
                            fo_name = objFOEmployee.getString("Full_Name__c");
                        }

                        JSONObject objFO_FM = objFO.optJSONObject("Field_Manager__r");
                        if(objFO_FM != null)
                        {
                            JSONObject objFO_FMEmployee = objFO_FM.optJSONObject("Employee__r");
                            if(objFO_FMEmployee != null)
                            {
                                fm_name = objFO_FMEmployee.getString("Full_Name__c");
                            }
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
            else if(objFieldOffice != null)
            {
                getAndSaveId(objFieldOffice, Constants.STR_FIELD_OFFICER);
                JSONArray arrRec = objFieldOffice.getJSONArray("records");
                if(arrRec.length() > 0)
                {
                    JSONObject obj1 = arrRec.getJSONObject(0);
                    JSONObject objFM = obj1.optJSONObject("Field_Manager__r");
                    if(objFM != null)
                    {
                        JSONObject objFO_FMEmployee = objFM.optJSONObject("Employee__r");
                        if(objFO_FMEmployee != null)
                        {
                            fm_name = objFO_FMEmployee.getString("Full_Name__c");
                        }
                    }
                }
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
            else if(objFieldManager != null)
            {
                getAndSaveId(objFieldManager, Constants.STR_FIELD_MANAGER);
                saveAboutMeInfo(fn, mn, ln, fo_name, fm_name);
            }
        }
    }

    private void saveAboutMeInfo(String fn, String mn, String ln, String fo_name, String fm_name)
    {
        PreferenceHelper.getInstance().writeAboutFN(fn);
        PreferenceHelper.getInstance().writeAboutMN(mn);
        PreferenceHelper.getInstance().writeAboutLN(ln);
        PreferenceHelper.getInstance().writeAboutFOname(fo_name);
        PreferenceHelper.getInstance().writeAboutFMname(fm_name);

        populateView();
    }

    private void getAndSaveId(JSONObject jsonObject, String type) throws JSONException {
        JSONArray arrRec = jsonObject.getJSONArray("records");
        if(arrRec.length() > 0)
        {
            JSONObject obj1 = arrRec.getJSONObject(0);
            String idFac = obj1.getString("Id");
            PreferenceHelper.getInstance().writeLoginUserType(type);
            PreferenceHelper.getInstance().writeLoginUserId(idFac);

            WebApi.getAboutMeAttachment(aboutMeAttachmentCallback);
        }
    }
}
