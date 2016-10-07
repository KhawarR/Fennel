package tintash.fennel.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.application.Fennel;
import tintash.fennel.common.database.DatabaseHelper;
import tintash.fennel.models.Farm;
import tintash.fennel.models.Farmer;
import tintash.fennel.models.ResponseModel;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.PreferenceHelper;
import tintash.fennel.views.NothingSelectedSpinnerAdapter;
import tintash.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class EnrollFragment extends BaseContainerFragment implements AdapterView.OnItemSelectedListener {


    @Bind(R.id.spLocation)
    Spinner spLocation;

    @Bind(R.id.spSubLocation)
    Spinner spSubLocation;

    @Bind(R.id.spVillage)
    Spinner spVillage;

    @Bind(R.id.spTree)
    Spinner spTree;

    @Bind(R.id.tvMale)
    TextView tvMale;

    @Bind(R.id.tvFemale)
    TextView tvFemale;

    @Bind(R.id.tvLeaderNo)
    TextView tvLeaderNo;

    @Bind(R.id.tvLeaderYes)
    TextView tvLeaderYes;

    @Bind(R.id.txtFarmerHomeNo)
    TextView txtFarmerHomeNo;

    @Bind(R.id.txtFarmerHomeYes)
    TextView txtFarmerHomeYes;

    @Bind(R.id.txtCreateFarmer)
    TextView txtCreateFarmer;

    @Bind(R.id.txtSubmitApproval)
    TextView txtSubmitApproval;

    @Bind(R.id.et_first_name)
    EditText etFirstName;

    @Bind(R.id.et_second_name)
    EditText etSecondName;

    @Bind(R.id.et_sur_name)
    EditText etSurname;

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.et_id_number)
    EditText etIdNumber;

    @Bind(R.id.et_mobile_number)
    EditText etMobileNumber;

    @Bind(R.id.imgFarmerPhoto)
    ImageView imgFarmerPhoto;

    @Bind(R.id.imgNationalID)
    ImageView imgNationalID;

    private String title;
    private Farmer farmer;
    boolean isEdit = false;

    private DisplayImageOptions options;

    private ImagePicker imagePicker;
    private ImagePickerCallback farmerPhotoPickerCallback;
    private ImagePickerCallback nationalIdPickerCallback;
    private String location;
    private String subLocation;
    private String village;
    private String treeSpecies;

    public static EnrollFragment newInstance(String title, Farmer farmer)
    {
        EnrollFragment fragment = new EnrollFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putSerializable("farmer", farmer);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_enroll, null);
        ButterKnife.bind(this, view);

        float density = getActivity().getResources().getDisplayMetrics().density;
        float px = 10 * density;
        options = new DisplayImageOptions.Builder()
        .displayer(new RoundedBitmapDisplayer((int)px)).build(); // default

        imagePicker = new ImagePicker(EnrollFragment.this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("ViewCreated Enroll ");

        titleBarLayout.setOnIconClickListener(this);

        tvMale.setSelected(true);
        tvFemale.setSelected(false);

        tvLeaderNo.setSelected(true);
        tvLeaderYes.setSelected(false);

        txtFarmerHomeNo.setSelected(true);
        txtFarmerHomeYes.setSelected(false);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsLocation, R.layout.simple_spinner_item);
        spLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), "LOCATION"));
        spLocation.setOnItemSelectedListener(this);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsSubLocation, R.layout.simple_spinner_item);
        spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), "SUB LOCATION"));
        spSubLocation.setOnItemSelectedListener(this);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsVillage, R.layout.simple_spinner_item);
        spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), "VILLAGE"));
        spVillage.setOnItemSelectedListener(this);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsTree, R.layout.simple_spinner_item);
        spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), "TREE SPECIES"));
        spTree.setOnItemSelectedListener(this);

        title = getArguments().getString("title");
        if(title.equalsIgnoreCase(Constants.STR_EDIT_FARMER))
        {
            farmer = (Farmer) getArguments().getSerializable("farmer");
            txtCreateFarmer.setText("SAVE");

            if(!farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_INCOMPLETE))
            {
                disableForm();
            } else {
                isEdit = true;
            }

            populateFarmer();
        }

        titleBarLayout.setTitleText(title);
    }

    private void populateFarmer()
    {
        if(farmer != null)
        {
            etFirstName.setText(farmer.getFirstName());
            etSecondName.setText(farmer.getSecondName());
            etSurname.setText(farmer.getSurname());
            etIdNumber.setText(farmer.getIdNumber());

            if(farmer.getGender().equalsIgnoreCase("male")) {
                tvMale.setSelected(true);
                tvFemale.setSelected(false);
            }
            else {
                tvFemale.setSelected(true);
                tvMale.setSelected(false);
            }

            if(farmer.isLeader()) {
                tvLeaderYes.setSelected(true);
                tvLeaderNo.setSelected(false);
            }
            else {
                tvLeaderNo.setSelected(true);
                tvLeaderYes.setSelected(false);
            }

            if(farmer.getLocation()!= null && !farmer.getLocation().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsLocation, R.layout.simple_spinner_item);
                spLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getLocation()));
            }
            if(farmer.getSubLocation()!= null && !farmer.getSubLocation().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsSubLocation, R.layout.simple_spinner_item);
                spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getSubLocation()));
            }
            if(farmer.getTreeSpecies()!= null && !farmer.getTreeSpecies().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsTree, R.layout.simple_spinner_item);
                spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getTreeSpecies()));
            }
            if(farmer.getVillageName()!= null && !farmer.getVillageName().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsVillage, R.layout.simple_spinner_item);
                spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getVillageName()));
            }

            if(farmer.isFarmerHome()) {
                txtFarmerHomeYes.setSelected(true);
                txtFarmerHomeNo.setSelected(false);
            }
            else {
                txtFarmerHomeNo.setSelected(true);
                txtFarmerHomeYes.setSelected(false);
            }

            etMobileNumber.setText(farmer.getMobileNumber());

            if(farmer.getThumbUrl()!= null && !farmer.getThumbUrl().isEmpty()) ImageLoader.getInstance().displayImage(farmer.getThumbUrl(), imgFarmerPhoto, options);
            if(farmer.getFarmerIdPhotoUrl()!= null && !farmer.getFarmerIdPhotoUrl().isEmpty()) ImageLoader.getInstance().displayImage(farmer.getFarmerIdPhotoUrl(), imgNationalID, options);
        }
    }

    @OnClick({R.id.tvMale, R.id.tvFemale})
    void onClickGenderSelection(View view) {
        tvFemale.setSelected(false);
        tvMale.setSelected(false);
        view.setSelected(true);

    }

    @OnClick({R.id.tvLeaderNo, R.id.tvLeaderYes})
    void onClickLeaderSelection(View view) {
        tvLeaderNo.setSelected(false);
        tvLeaderYes.setSelected(false);
        view.setSelected(true);

    }


    @OnClick({R.id.txtFarmerHomeNo, R.id.txtFarmerHomeYes})
    void onClickFarmerHomeSelection(View view) {
        txtFarmerHomeNo.setSelected(false);
        txtFarmerHomeYes.setSelected(false);
        view.setSelected(true);

    }

    @OnClick(R.id.txtCreateFarmer)
    void onClickCreateFarmer(View view) {

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        loadingStarted();

        if (!isFormFilled()) {
            return;
        }

        if (isEdit) {
            editFarmer();
        } else {
            createFarmer();
        }

    }

    private boolean isFormFilled() {

        boolean goodToGo = true;
        String missingData = "";
        if(etFirstName.getText() == null || etFirstName.getText().equals(""))
        {
            goodToGo = false;
            missingData += "\n- First Name";
        }
        if(etIdNumber.getText() == null || etIdNumber.getText().equals(""))
        {
            goodToGo = false;
            missingData += "\n- ID Number";
        }

        if (!goodToGo) {
            Toast.makeText(getActivity(), "Please fill the following fields: " + missingData, Toast.LENGTH_SHORT).show();
        }

        return goodToGo;

    }

    private void popToSignupsFragment() {
        ((BaseContainerFragment)(getParentFragment())).popFragment();
    }

    private void addFarmerToDB(Farmer newFarmer, String id, boolean synced) {

        DatabaseHelper.getInstance().insertFarmer(newFarmer, id, synced);
    }

    private void addFarmToDB(Farm newFarm, String id, boolean synced) {
        DatabaseHelper.getInstance().insertFarm(newFarm, id, synced);
    }

    private void addFarmWithFarmerId(final Farm farm, String id) {

        Call<ResponseModel> apiCall = Fennel.getWebService().addFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farm);
        apiCall.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null && response.body().success == true) {
                    Log.i("LP", "Farm Added To Server");
                    addFarmToDB(farm, response.body().id, true);
                    Toast.makeText(getContext(), "Farmer Enrolled Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    addFarmToDB(farm, null, false);
                    Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();
                }
                Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));

                loadingFinished();
                popToSignupsFragment();
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());
                addFarmToDB(farm, null, false);
                Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();
                loadingFinished();
                popToSignupsFragment();
            }
        });
    }

    private Farm createFarmWithFarmerId(String farmerId) {

        final Farm newFarm = new Farm();
        if (farmerId != null)
            newFarm.setFarmerId(farmerId);

        newFarm.setFacilitatorId(PreferenceHelper.getInstance().readFacilitatorId());
        newFarm.setFarmerStatus("Incomplete");
        newFarm.setLocation("");//location != null ? location : "");
        newFarm.setSubLocation("");//subLocation != null ? subLocation : "");
        newFarm.setVillageName("");//village != null ? village : "");
        newFarm.setTreeSpecies("");//treeSpecies != null ? treeSpecies : "");
        return newFarm;
    }

    private HashMap<String, Object> getFarmerMap() {

        final HashMap<String, Object> newFarmerMap = new HashMap<>();
        newFarmerMap.put("First_Name__c" , etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        newFarmerMap.put("Middle_Name__c", etSecondName.getText() != null ? etSecondName.getText().toString() : "");
        newFarmerMap.put("Last_Name__c", etSurname.getText() != null ? etSurname.getText().toString() : "");
        newFarmerMap.put("Name", etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
        newFarmerMap.put("Mobile_Number__c", etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");
        newFarmerMap.put("Gender__c" , (tvFemale.isSelected() == true) ? "Female" : "Male");
        newFarmerMap.put("Leader__c", (tvLeaderYes.isSelected() == true) ? 1 : 0);
//        String fullName = ((etFirstName.getText() != null && !etFirstName.getText().equals("")) ? etFirstName.getText().toString() : "") + ((etSecondName.getText() != null && !etSecondName.getText().equals("")) ? " " + etSecondName.getText().toString() : "") + ((etSurname.getText() != null && !etSecondName.getText().equals("")) ? " " + etSurname.getText().toString() : "");
//        newFarmer.setFullName(fullName);

        return newFarmerMap;
    }

    private Farmer getFarmer() {

        final Farmer newFarmer = new Farmer();
        newFarmer.setFirstName(etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        newFarmer.setSecondName(etSecondName.getText() != null ? etSecondName.getText().toString() : "");
        newFarmer.setSurname(etSurname.getText() != null ? etSurname.getText().toString() : "");
        newFarmer.setIdNumber(etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
        newFarmer.setMobileNumber(etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");
        newFarmer.setGender((tvFemale.isSelected() == true) ? "Female" : "Male");
        newFarmer.setLeader((tvLeaderYes.isSelected() == true) ? true : false);

        return newFarmer;
    }

    @OnClick(R.id.txtSubmitApproval)
    void onClickSubmitForApproval(View view) {

    }

    @OnClick(R.id.imgFarmerPhoto)
    void onClickFarmerPhoto(View view) {
        pickFarmerImage();
    }

    @OnClick(R.id.imgNationalID)
    void onClickNationalID(View view) {
        pickNationalIdImage();
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
    }

    private void pickFarmerImage() {
        farmerPhotoPickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                ImageLoader.getInstance().displayImage(images.get(0).getQueryUri(), imgFarmerPhoto, options);
            }

            @Override
            public void onError(String message) {
                // Do error handling
            }
        };
        imagePicker.setImagePickerCallback(farmerPhotoPickerCallback);
        // imagePicker.allowMultiple(); // Default is false
        // imagePicker.shouldGenerateMetadata(false); // Default is true
        // imagePicker.shouldGenerateThumbnails(false); // Default is true
        imagePicker.pickImage();
    }

    private void pickNationalIdImage() {
        nationalIdPickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                ImageLoader.getInstance().displayImage(images.get(0).getQueryUri(), imgNationalID, options);
            }

            @Override
            public void onError(String message) {
                // Do error handling
            }
        };
        imagePicker.setImagePickerCallback(nationalIdPickerCallback);
        // imagePicker.allowMultiple(); // Default is false
        // imagePicker.shouldGenerateMetadata(false); // Default is true
        // imagePicker.shouldGenerateThumbnails(false); // Default is true
        imagePicker.pickImage();
    }

    private void disableForm()
    {
        disableView(etFirstName);
        disableView(etSecondName);
        disableView(etSurname);
        disableView(etIdNumber);
        disableView(tvMale);
        disableView(tvFemale);
        disableView(tvLeaderYes);
        disableView(tvLeaderNo);
        disableView(spLocation);
        disableView(spSubLocation);
        disableView(spTree);
        disableView(spVillage);
        disableView(txtFarmerHomeYes);
        disableView(txtFarmerHomeNo);
        disableView(etMobileNumber);
        disableView(txtCreateFarmer);
        disableView(imgFarmerPhoto);
        disableView(imgNationalID);
        disableView(txtSubmitApproval);
    }

    private void disableView(View view)
    {
        view.setEnabled(false);
        view.setFocusable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                if(imagePicker == null) {
                    imagePicker = new ImagePicker(getActivity());
                    imagePicker.setImagePickerCallback(farmerPhotoPickerCallback);
                }
                imagePicker.submit(data);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinnerView = (Spinner) parent;
        switch (spinnerView.getId()) {
            case R.id.spLocation:
                location = (String) parent.getItemAtPosition(position);
                break;
            case R.id.spSubLocation:
                subLocation = (String) parent.getItemAtPosition(position);
                break;
            case R.id.spVillage:
                village = (String) parent.getItemAtPosition(position);
                break;
            case R.id.spTree:
                treeSpecies = (String) parent.getItemAtPosition(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void editFarmer() {

        final HashMap<String, Object> farmerMap = getFarmerMap();

//        farmer.setFirstName(etFirstName.getText() != null ? etFirstName.getText().toString() : "");
//        farmer.setSecondName(etSecondName.getText() != null ? etSecondName.getText().toString() : "");
//        farmer.setSurname(etSurname.getText() != null ? etSurname.getText().toString() : "");
//        farmer.setIdNumber(etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
//        farmer.setMobileNumber(etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");
//        //String fullName = ((etFirstName.getText() != null && !etFirstName.getText().equals("")) ? etFirstName.getText().toString() : "") + ((etSecondName.getText() != null && !etSecondName.getText().equals("")) ? " " + etSecondName.getText().toString() : "") + ((etSurname.getText() != null && !etSecondName.getText().equals("")) ? " " + etSurname.getText().toString() : "");
//        //farmer.setFullName(fullName);

        Call<ResponseBody> apiCall = Fennel.getWebService().editFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmer.farmerId, farmerMap);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                    Log.i("LP", "Farmer Edited!");
                    Toast.makeText(getContext(), "Farmer Edited Successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getContext(), "Farmer Edit Failed!", Toast.LENGTH_SHORT).show();
                }
                loadingFinished();
                popToSignupsFragment();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());

                Toast.makeText(getContext(), "Farmer Edit Failed!", Toast.LENGTH_SHORT).show();
                loadingFinished();
                popToSignupsFragment();
            }
        });

    }

    private void createFarmer() {

        final HashMap<String, Object> farmerMap = getFarmerMap();

        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, farmerMap);
        apiCall.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                Farmer newFarmer = getFarmer();
                if (response.body() != null && response.body().success == true) {
                    Log.i("LP", "Farmer Added To Server");
                    addFarmerToDB(newFarmer, response.body().id, true);
                    Farm newFarm = createFarmWithFarmerId(response.body().id);
                    addFarmWithFarmerId(newFarm, response.body().id);

                } else {

                    addFarmerToDB(newFarmer, null, false);
                    Farm newFarm = createFarmWithFarmerId(null);
                    addFarmToDB(newFarm, null, false);
                    Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();

                    loadingFinished();
                    popToSignupsFragment();
                }
                Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());
                Farmer newFarmer = getFarmer();
                addFarmerToDB(newFarmer, null, false);
                Farm newFarm = createFarmWithFarmerId(null);
                addFarmToDB(newFarm, null, false);
                Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();
                loadingFinished();
                popToSignupsFragment();
            }
        });
    }
}
