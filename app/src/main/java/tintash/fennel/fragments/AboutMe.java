package tintash.fennel.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
import tintash.fennel.utils.CircleViewTransformation;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.PreferenceHelper;
import tintash.fennel.views.FontTextView;

/**
 * Created by Faizan on 9/27/2016.
 */
public class AboutMe extends BaseFragment {

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

    private ImagePicker imagePicker;
    private ImagePickerCallback imagePickerCallback;
    private CameraImagePicker cameraImagePicker;

    private String pictureAttachmentId = null;
    private String facilitatorId = null;

    private Picasso picasso;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about_me, null);
        ButterKnife.bind(this, view);

        imagePicker = new ImagePicker(AboutMe.this);
        cameraImagePicker = new CameraImagePicker(AboutMe.this);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", Session.getAuthToken())
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        picasso = new Picasso.Builder(getActivity())
                .downloader(new OkHttp3Downloader(client))
                .build();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        picasso.load(R.drawable.dummy_profile).transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);

        populateView();
    }

    private void populateView()
    {
        tvFirstName.setText(PreferenceHelper.getInstance().readAboutFN());
        tvSecondName.setText(PreferenceHelper.getInstance().readAboutMN());
        tvSurname.setText(PreferenceHelper.getInstance().readAboutLN());
        tvFieldOfficer.setText(PreferenceHelper.getInstance().readAboutFOname());
        tvFieldManager.setText(PreferenceHelper.getInstance().readAboutFMname());

        getAboutMeAttachment();
    }

    private void getAboutMeAttachment() {
        String queryTable = "Facilitator__c";
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if(userType.equalsIgnoreCase(Constants.STR_FACILITATOR))
            queryTable = "Facilitator__c";
        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
            queryTable = "Field_Officer__c";
        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
            queryTable = "Field_Manager__c";

        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_ATTACHMENT, queryTable, PreferenceHelper.getInstance().readLoginUserId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeAttachmentCallback);
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
                        if(!attId.isEmpty())
                        {
                            pictureAttachmentId = attId;
                            String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), attId);
                            picasso.load(thumbUrl).transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
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
        PreferenceHelper.getInstance().clearSession();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @OnClick(R.id.rl_pick_image)
    void onClickPickImage(View view) {
        showPickerDialog();
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
//                Toast.makeText(getActivity(), images.size() + "", Toast.LENGTH_SHORT).show();
//                ImageLoader.getInstance().displayImage(images.get(0).getQueryUri(), cIvProfileMain);
                addPictureAttachment(images.get(0).getOriginalPath());
                picasso.load(images.get(0).getQueryUri()).transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
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
//                if(imagePicker == null) {
//                    imagePicker = new ImagePicker(getActivity());
//                    imagePicker.setImagePickerCallback(farmerPhotoPickerCallback);
//                }
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
        if (pictureAttachmentId == null)
            attachmentMap.put("ParentId", facilitatorId);

        JSONObject json = new JSONObject(attachmentMap);

//        File f = new File(farmerImageUri);
//        byte[] byteArrayImage = getByteArrayFromFile(f);

        Bitmap bmp = BitmapFactory.decodeFile(imageUri);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bos);

        byte[] byteArrayImage = bos.toByteArray();

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        if (pictureAttachmentId == null) {

            Call<ResponseBody> attachmentApi = Fennel.getWebService().addAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, entityBody, imageBody);
            attachmentApi.enqueue(new Callback<ResponseBody>() {
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
                    } else {
                        Log.i("Fennel", "facilitator profile picture upload failed!");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("Fennel", "facilitator profile picture upload failed!");
                }
            });
        } else {
            Call<ResponseBody> attachmentApi = Fennel.getWebService().editAttachment(Session.getAuthToken(), NetworkHelper.API_VERSION, pictureAttachmentId, entityBody, imageBody);
            attachmentApi.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                        Log.i("Fennel", "facilitator profile picture edited successfully!");
                    } else {
                        Log.i("Fennel", "facilitator profile picture edit failed!");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("Fennel", "facilitator profile picture edit failed!");
                }
            });
        }
    }

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
}
