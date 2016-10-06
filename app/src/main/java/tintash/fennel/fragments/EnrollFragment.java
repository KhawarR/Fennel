package tintash.fennel.fragments;

import android.content.Context;
import android.app.Activity;
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
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.models.Farmer;
import tintash.fennel.application.Fennel;
import tintash.fennel.common.database.DatabaseHelper;
import tintash.fennel.models.Farm;
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
        tvLeaderNo.setSelected(true);
        txtFarmerHomeNo.setSelected(true);

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

            if(farmer.getGender().equalsIgnoreCase("male"))
                tvMale.setSelected(true);
            else
                tvFemale.setSelected(true);

            if(farmer.isLeader())
                tvLeaderYes.setSelected(true);
            else
                tvLeaderNo.setSelected(true);

            if(!farmer.getLocation().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsLocation, R.layout.simple_spinner_item);
                spLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getLocation()));
            }
            if(!farmer.getSubLocation().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsSubLocation, R.layout.simple_spinner_item);
                spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getSubLocation()));
            }
            if(!farmer.getTreeSpecies().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsTree, R.layout.simple_spinner_item);
                spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getTreeSpecies()));
            }
            if(!farmer.getVillageName().isEmpty()) {
                ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsVillage, R.layout.simple_spinner_item);
                spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapter, R.layout.spinner_nothing_selected, getContext(), farmer.getVillageName()));
            }

            if(farmer.isFarmerHome())
                txtFarmerHomeYes.setSelected(true);
            else
                txtFarmerHomeNo.setSelected(true);

            etMobileNumber.setText(farmer.getMobileNumber());

            if(!farmer.getThumbUrl().isEmpty()) ImageLoader.getInstance().displayImage(farmer.getThumbUrl(), imgFarmerPhoto, options);
            if(!farmer.getFarmerIdPhotoUrl().isEmpty()) ImageLoader.getInstance().displayImage(farmer.getFarmerIdPhotoUrl(), imgNationalID, options);
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

        final Farmer newFarmer = new Farmer();
        newFarmer.setFirstName(etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        newFarmer.setSecondName(etSecondName.getText() != null ? etSecondName.getText().toString() : "");
        newFarmer.setSurname(etSurname.getText() != null ? etSurname.getText().toString() : "");
        newFarmer.setIdNumber(etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
        newFarmer.setMobileNumber(etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");

        boolean goodToGo = true;
        String missingData = "";
        if(newFarmer.getFirstName().isEmpty())
        {
            goodToGo = false;
            missingData += "\n- First Name";
        }
        if(newFarmer.getIdNumber().isEmpty())
        {
            goodToGo = false;
            missingData += "\n- ID Number";
        }

        if(goodToGo)
        {
            Call<ResponseModel> apiCall = Fennel.getWebService().addFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, newFarmer);
            apiCall.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
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
                addFarmerToDB(newFarmer, null, false);
                Farm newFarm = createFarmWithFarmerId(null);
                addFarmToDB(newFarm, null, false);
                Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();
                loadingFinished();
                popToSignupsFragment();
            }
        });
        }
        else
        {
            Toast.makeText(getActivity(), "Please fill the following fields: " + missingData, Toast.LENGTH_SHORT).show();
        }
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
                    Toast.makeText(getContext(), "Farmer Enrolled Successfully!", Toast.LENGTH_SHORT);
                } else {
                    addFarmToDB(farm, null, false);
                    Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT);
                }
                Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));

                loadingFinished();
                popToSignupsFragment();
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());
                addFarmToDB(farm, null, false);
                Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT);
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
        newFarm.setLocation("");//location != null ? location : "");
        newFarm.setSubLocation("");//subLocation != null ? subLocation : "");
        newFarm.setVillageName("");//village != null ? village : "");
        newFarm.setTreeSpecies("");//treeSpecies != null ? treeSpecies : "");
        return newFarm;
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
}
