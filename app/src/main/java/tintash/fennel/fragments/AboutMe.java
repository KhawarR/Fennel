package tintash.fennel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.activities.LoginActivity;
import tintash.fennel.activities.MainActivity;
import tintash.fennel.application.Fennel;
import tintash.fennel.datamodels.SFResponse;
import tintash.fennel.models.Farmer;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.utils.PreferenceHelper;

/**
 * Created by Faizan on 9/27/2016.
 */
public class AboutMe extends BaseFragment {

    @Bind(R.id.et_first_name)
    EditText etFirstName;

    @Bind(R.id.et_second_name)
    EditText etSecondName;

    @Bind(R.id.et_sur_name)
    EditText etSurname;

    @Bind(R.id.et_field_officer)
    EditText etFieldOfficer;

    @Bind(R.id.et_field_manager)
    EditText etFieldManager;

    @Bind(R.id.profile_image)
    CircleImageView cIvProfileMain;

    private ImagePicker imagePicker;
    private ImagePickerCallback imagePickerCallback;

    private Picasso picasso;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about_me, null);
        ButterKnife.bind(this, view);

        OkHttpClient client = new OkHttpClient.Builder()
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

        getAboutMeInfo();

    }

    private void getAboutMeInfo() {
        String query = String.format(NetworkHelper.QUERY_ABOUT_ME, PreferenceHelper.getInstance().readUserId(), PreferenceHelper.getInstance().readPassword());
        loadingStarted();
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeCallback);
    }

    private Callback<ResponseBody> aboutMeCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
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
            } else {
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private void parseData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            JSONObject objRecord = arrRecords.getJSONObject(0);
            String id = objRecord.getString("Id");

            JSONObject objFacilitator = objRecord.getJSONObject("Facilitator__r");
            String name = objFacilitator.getString("Name");
            if (name == null || name.equalsIgnoreCase("null")) name = "";
            String secondName = objFacilitator.getString("Second_Name__c");
            if (secondName == null || secondName.equalsIgnoreCase("null")) secondName = "";
            String surname = objFacilitator.getString("Surname__c");
            if (surname == null || surname.equalsIgnoreCase("null")) surname = "";

            JSONObject objFieldOffice = objFacilitator.getJSONObject("Field_Officer__r");
            String fo_name = objFieldOffice.getString("Name");
            if (fo_name == null || fo_name.equalsIgnoreCase("null")) fo_name = "";

            JSONObject objFieldManager = objFieldOffice.getJSONObject("Field_Manager__r");
            String fm_name = objFieldManager.getString("Name");
            if (fm_name == null || fm_name.equalsIgnoreCase("null")) fm_name = "";

            String facId = objFacilitator.getString("Id");

            etFirstName.setText(name);
            etSecondName.setText(secondName);
            etSurname.setText(surname);
            etFieldOfficer.setText(fo_name);
            etFieldManager.setText(fm_name);

            getAboutMeAttachment(facId);

        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAboutMeAttachment(String id) {
        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_ATTACHMENT, id);
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeAttachmentCallback);
    }

    private Callback<ResponseBody> aboutMeAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    String attId = parseDataAttachment(responseStr);
                    if(!attId.isEmpty())
                    {
                        String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), attId);
                        picasso.load(thumbUrl).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
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
        pickImage();
    }

    @OnClick(R.id.txtSignOut)
    void onClickSignOut(View view) {
        PreferenceHelper.getInstance().clearSession();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @OnClick(R.id.rl_pick_image)
    void onClickPickImage(View view) {
        pickImage();
    }

    private void pickImage() {
        imagePicker = new ImagePicker(AboutMe.this);
        imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
//                Toast.makeText(getActivity(), images.size() + "", Toast.LENGTH_SHORT).show();
//                ImageLoader.getInstance().displayImage(images.get(0).getQueryUri(), cIvProfileMain);
                picasso.load(images.get(0).getQueryUri()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvProfileMain);
            }

            @Override
            public void onError(String message) {
                // Do error handling
            }
        };
        imagePicker.setImagePickerCallback(imagePickerCallback);
        // imagePicker.allowMultiple(); // Default is false
        // imagePicker.shouldGenerateMetadata(false); // Default is true
        // imagePicker.shouldGenerateThumbnails(false); // Default is true
        imagePicker.pickImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                if(imagePicker == null) {
                    imagePicker = new ImagePicker(getActivity());
                    imagePicker.setImagePickerCallback(imagePickerCallback);
                }
                imagePicker.submit(data);
            }
        }
    }
}
