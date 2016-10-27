package wal.fennel.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.Farm;
import wal.fennel.models.Farmer;
import wal.fennel.models.Location;
import wal.fennel.models.ResponseModel;
import wal.fennel.models.SubLocation;
import wal.fennel.models.Tree;
import wal.fennel.models.Village;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.WebApi;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PhotoUtils;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.RoundedCornersTransformation;
import wal.fennel.utils.Singleton;
import wal.fennel.views.NothingSelectedSpinnerAdapter;
import wal.fennel.views.TitleBarLayout;


/**
 * Created by Faizan on 9/27/2016.
 */
public class EnrollFragment extends BaseContainerFragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    @Bind(R.id.scrollView)
    ScrollView scrollView;

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

    @Bind(R.id.lblFirstName)
    TextView lblFirstName;

    @Bind(R.id.lblSurname)
    TextView lblSurname;

    @Bind(R.id.lblIdNumber)
    TextView lblIdNumber;

    @Bind(R.id.lblGender)
    TextView lblGender;

    @Bind(R.id.lblMobileNumber)
    TextView lblMobileNumber;

    @Bind(R.id.llGenderContainer)
    LinearLayout llGenderContainer;

    @Bind(R.id.llLeaderContainer)
    LinearLayout llLeaderContainer;

    @Bind(R.id.llFarmerHomeContainer)
    LinearLayout llFarmerHomeContainer;

    @Bind(R.id.lblLeader)
    TextView lblLeader;

    @Bind(R.id.lblFarmerHome)
    TextView lblFarmerHome;

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

    CircleImageView cIvIconRight;

    private String title;
    private Farmer farmer;
    boolean isEdit = false;

    private CameraImagePicker cameraImagePicker;
    private ImagePicker imagePicker;
    private String location;
    private String subLocation;
    private String village;
    private String treeSpecies;
    private String locationName;
    private String subLocationName;
    private String villageName;
    private String treeSpeciesName;

    private boolean isFarmerPhotoSet = false;
    private boolean isNationalIdPhotoSet = false;

    private String farmerStatus = null;

    private String farmerImageUri = null;
    private String farmerIdImageUri = null;

    //    private int PICKER_REQUEST_FARMER_DEVICE = 12001;
//    private int PICKER_REQUEST_FARMER_CAMERA = 12002;
//    private int PICKER_REQUEST_NAT_ID_DEVICE = 12003;
//    private int PICKER_REQUEST_NAT_ID_CAMERA = 12004;
    private ArrayList<Location> arrLocations = new ArrayList<>();
    private ArrayList<String> strArrLocations = new ArrayList<>();
    private ArrayList<SubLocation> arrSubLocations = new ArrayList<>();
    private ArrayList<String> strArrSubLocations = new ArrayList<>();
    private ArrayList<Village> arrVillages = new ArrayList<>();
    private ArrayList<String> strArrVillages = new ArrayList<>();
    private ArrayList<Tree> arrTrees = new ArrayList<>();
    private ArrayList<String> strArrTrees = new ArrayList<>();
    private Transformation transformation;

    public static EnrollFragment newInstance(String title, Farmer farmer) {
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

        imagePicker = new ImagePicker(EnrollFragment.this);
        cameraImagePicker = new CameraImagePicker(EnrollFragment.this);

        transformation = new RoundedCornersTransformation();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("ViewCreated Enroll ");

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

        tvMale.setSelected(false);
        tvFemale.setSelected(false);

        tvLeaderNo.setSelected(false);
        tvLeaderYes.setSelected(false);

        txtFarmerHomeNo.setSelected(false);
        txtFarmerHomeYes.setSelected(false);

        arrLocations = DatabaseHelper.getInstance().getAllLocations();
        strArrLocations = new ArrayList<>();

        for (int i = 0; i < arrLocations.size(); i++) {
            strArrLocations.add(arrLocations.get(i).name);
        }

        arrSubLocations = DatabaseHelper.getInstance().getAllSubLocations();
        strArrSubLocations = new ArrayList<>();

        for (int i = 0; i < arrSubLocations.size(); i++) {
            strArrSubLocations.add(arrSubLocations.get(i).name);
        }

        arrVillages = DatabaseHelper.getInstance().getAllVillages();
        strArrVillages = new ArrayList<>();

        for (int i = 0; i < arrVillages.size(); i++) {
            strArrVillages.add(arrVillages.get(i).name);
        }

        arrTrees = DatabaseHelper.getInstance().getAllTrees();
        strArrTrees = new ArrayList<>();

        for (int i = 0; i < arrTrees.size(); i++) {
            strArrTrees.add(arrTrees.get(i).name);
        }

        spLocation.setTag(true);
        ArrayAdapter<String> arrayAdapterLoc = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrLocations);
        spLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterLoc, R.layout.spinner_nothing_selected, getContext(), "LOCATION"));
        spLocation.setOnItemSelectedListener(this);
        spLocation.setOnTouchListener(this);

        spSubLocation.setTag(true);
//        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsSubLocation, R.layout.simple_spinner_item);
        ArrayAdapter<String> arrayAdapterSubLoc = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrSubLocations);
        spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterSubLoc, R.layout.spinner_nothing_selected, getContext(), "SUB LOCATION"));
        spSubLocation.setOnItemSelectedListener(this);
        spSubLocation.setOnTouchListener(this);


        spVillage.setTag(true);
//        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsVillage, R.layout.simple_spinner_item);
        ArrayAdapter<String> arrayAdapterVillage = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrVillages);
        spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterVillage, R.layout.spinner_nothing_selected, getContext(), "VILLAGE"));
        spVillage.setOnItemSelectedListener(this);
        spVillage.setOnTouchListener(this);

        spTree.setTag(true);
//        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.optionsTree, R.layout.simple_spinner_item);
        ArrayAdapter<String> arrayAdapterTree = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrTrees);
        spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterTree, R.layout.spinner_nothing_selected, getContext(), "TREE SPECIES"));
        spTree.setOnItemSelectedListener(this);
        spTree.setOnTouchListener(this);

        title = getArguments().getString("title");
        if (title.equalsIgnoreCase(Constants.STR_EDIT_FARMER)) {
            farmer = (Farmer) getArguments().getSerializable("farmer");
            txtCreateFarmer.setText("SAVE");

            if (!farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_INCOMPLETE)) {
                disableForm();
            } else {
                isEdit = true;
            }
            populateFarmer();
        }

        titleBarLayout.setTitleText(title);

        etFirstName.addTextChangedListener(watcher);
        etSecondName.addTextChangedListener(watcher);
        etSurname.addTextChangedListener(watcher);
        etIdNumber.addTextChangedListener(watcher);
        etMobileNumber.addTextChangedListener(watcher);
    }

    private void populateFarmer() {
        if (farmer != null) {
            etFirstName.setText(farmer.getFirstName());
            etSecondName.setText(farmer.getSecondName());
            etSurname.setText(farmer.getSurname());
            etIdNumber.setText(farmer.getIdNumber());

            if (farmer.getGender().equalsIgnoreCase("male")) {
                tvMale.setSelected(true);
                tvFemale.setSelected(false);
            } else {
                tvFemale.setSelected(true);
                tvMale.setSelected(false);
            }

            if (farmer.isLeader()) {
                tvLeaderYes.setSelected(true);
                tvLeaderNo.setSelected(false);
            } else {
                tvLeaderNo.setSelected(true);
                tvLeaderYes.setSelected(false);
            }

            if(farmer.isFarmerHome())
            {
                txtFarmerHomeYes.setSelected(true);
                txtFarmerHomeNo.setSelected(false);
            } else {
                txtFarmerHomeNo.setSelected(true);
                txtFarmerHomeYes.setSelected(false);
            }

            if (farmer.getLocation() != null && !farmer.getLocation().isEmpty()) {
                int index = getPositionForSpinnerArray(farmer.getLocation(), strArrLocations);
                if(index >= 0)
                {
                    updateSubLocFromLocation(arrLocations.get(index).id);
                    ArrayAdapter<String> arrayAdapterSubLoc = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrSubLocations);
                    spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterSubLoc, R.layout.spinner_nothing_selected, getContext(), "SUB LOCATION"));

                    spLocation.setTag(false);
                    spLocation.setSelection(index + 1);
                }
            }
            if (farmer.getSubLocation() != null && !farmer.getSubLocation().isEmpty()) {
                int index = getPositionForSpinnerArray(farmer.getSubLocation(), strArrSubLocations);
                if(index >= 0)
                {
                    spSubLocation.setTag(false);
                    spSubLocation.setSelection(index + 1);
                    updateVillageAndTreeFromSubLocation(arrSubLocations.get(index).id);

                    ArrayAdapter<String> arrayAdapterVillage = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrVillages);
                    spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterVillage, R.layout.spinner_nothing_selected, getContext(), "VILLAGE"));

                    ArrayAdapter<String> arrayAdapterTree = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrTrees);
                    spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterTree, R.layout.spinner_nothing_selected, getContext(), "TREE SPECIES"));
                }
            }
            if (farmer.getVillageName() != null && !farmer.getVillageName().isEmpty()) {
                spVillage.setTag(false);
                spVillage.setSelection(getPositionForSpinnerArray(farmer.getVillageName(), strArrVillages) + 1);
            }
            if (farmer.getTreeSpecies() != null && !farmer.getTreeSpecies().isEmpty()) {
                spTree.setTag(false);
                spTree.setSelection(getPositionForSpinnerArray(farmer.getTreeSpecies(), strArrTrees) + 1);
            }

            if (farmer.isFarmerHome()) {
                txtFarmerHomeYes.setSelected(true);
                txtFarmerHomeNo.setSelected(false);
            } else {
                txtFarmerHomeNo.setSelected(true);
                txtFarmerHomeYes.setSelected(false);
            }

            etMobileNumber.setText(farmer.getMobileNumber());

            String thumbUrl = farmer.getThumbUrl();
            if (thumbUrl != null && !thumbUrl.isEmpty()) {
                imgFarmerPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if(NetworkHelper.isNetAvailable(getActivity()))
                    MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgFarmerPhoto);
                else
                    MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgFarmerPhoto);
                isFarmerPhotoSet = true;
            }

            String natCardUrl = farmer.getNationalCardUrl();
            if (natCardUrl != null && !natCardUrl.isEmpty())
            {
                imgNationalID.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if(NetworkHelper.isNetAvailable(getActivity()))
                    MyPicassoInstance.getInstance().load(natCardUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgNationalID);
                else
                    MyPicassoInstance.getInstance().load(natCardUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgNationalID);
                isNationalIdPhotoSet = true;
            }
        }
    }

    private int getPositionForSpinnerArray(String location, ArrayList<String> array) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equalsIgnoreCase(location)) {
                return i;
            }
        }
        return 0;
    }

    @OnClick({R.id.tvMale, R.id.tvFemale})
    void onClickGenderSelection(View view) {
        tvFemale.setSelected(false);
        tvMale.setSelected(false);
        view.setSelected(true);

        hideKeyboard();

    }

    @OnClick({R.id.tvLeaderNo, R.id.tvLeaderYes})
    void onClickLeaderSelection(View view) {
        tvLeaderNo.setSelected(false);
        tvLeaderYes.setSelected(false);
        view.setSelected(true);

        hideKeyboard();

    }


    @OnClick({R.id.txtFarmerHomeNo, R.id.txtFarmerHomeYes})
    void onClickFarmerHomeSelection(View view) {
        txtFarmerHomeNo.setSelected(false);
        txtFarmerHomeYes.setSelected(false);
        view.setSelected(true);

        hideKeyboard();

    }

    @OnClick(R.id.txtCreateFarmer)
    void onClickCreateFarmer(View view) {
        hideKeyboard();

        if (!isFormFilled()) {
            return;
        }

        loadingStarted();

        farmerStatus = "Interested";
        createOrEditFarmer();
    }

    private void createOrEditFarmer() {

        if (isEdit) {
            editFarmer();
        } else {
            createFarmer();
        }
    }

    private boolean isFormFilled() {

        boolean goodToGo = true;
        String missingData = "";
        View scrollToView = null;
        if (etFirstName.getText() == null || etFirstName.getText().toString().isEmpty()) {
            goodToGo = false;
            missingData += "\n- First Name";
            lblFirstName.setTextColor(getResources().getColor(R.color.dark_red));
            scrollToView = lblFirstName;
        }
        else
        {
            lblFirstName.setTextColor(getResources().getColor(R.color.black));
        }
        if (etSurname.getText() == null || etSurname.getText().toString().isEmpty()) {
            goodToGo = false;
            missingData += "\n- Surname";
            lblSurname.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = lblSurname;
        }
        else
        {
            lblSurname.setTextColor(getResources().getColor(R.color.black));
        }
        if (etIdNumber.getText() == null || etIdNumber.getText().toString().isEmpty()) {
            goodToGo = false;
            missingData += "\n- ID Number";
            lblIdNumber.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = lblIdNumber;
        }
        else
        {
            lblIdNumber.setTextColor(getResources().getColor(R.color.black));
        }
        if(!tvMale.isSelected() && !tvFemale.isSelected())
        {
            goodToGo = false;
            missingData += "\n- Gender";
            lblGender.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = llGenderContainer;
        }
        else
        {
            lblGender.setTextColor(getResources().getColor(R.color.black));
        }
        if(!tvLeaderNo.isSelected() && !tvLeaderYes.isSelected())
        {
            goodToGo = false;
            missingData += "\n- Leader?";
            lblLeader.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = llLeaderContainer;
        }
        else
        {
            lblLeader.setTextColor(getResources().getColor(R.color.black));
        }
        if (spLocation.getSelectedItem() == null) {
            goodToGo = false;
            missingData += "\n- Location";
            spLocation.setBackgroundResource(R.drawable.spinner_bg_error);
            if(scrollToView == null) scrollToView = spLocation;
        }
        else
        {
            spLocation.setBackgroundResource(R.drawable.spinner_bg);
        }
        if (spSubLocation.getSelectedItem() == null) {
            goodToGo = false;
            missingData += "\n- Sub location";
            spSubLocation.setBackgroundResource(R.drawable.spinner_bg_error);
            if(scrollToView == null) scrollToView = spSubLocation;
        }
        else
        {
            spSubLocation.setBackgroundResource(R.drawable.spinner_bg);
        }
        if (spVillage.getSelectedItem() == null) {
            goodToGo = false;
            missingData += "\n- Village";
            spVillage.setBackgroundResource(R.drawable.spinner_bg_error);
            if(scrollToView == null) scrollToView = spVillage;
        }
        else
        {
            spVillage.setBackgroundResource(R.drawable.spinner_bg);
        }
        if (spTree.getSelectedItem() == null) {
            goodToGo = false;
            missingData += "\n- Tree";
            spTree.setBackgroundResource(R.drawable.spinner_bg_error);
            if(scrollToView == null) scrollToView = spTree;
        }
        else
        {
            spTree.setBackgroundResource(R.drawable.spinner_bg);
        }
        if(!txtFarmerHomeNo.isSelected() && !txtFarmerHomeYes.isSelected())
        {
            goodToGo = false;
            missingData += "\n- Farmer Home?";
            lblFarmerHome.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = llFarmerHomeContainer;
        }
        else
        {
            lblFarmerHome.setTextColor(getResources().getColor(R.color.black));
        }
        if (etMobileNumber.getText() == null || etMobileNumber.getText().toString().isEmpty()) {
            goodToGo = false;
            missingData += "\n- Mobile Number";
            lblMobileNumber.setTextColor(getResources().getColor(R.color.dark_red));
            if(scrollToView == null) scrollToView = lblMobileNumber;
        }
        else
        {
            lblMobileNumber.setTextColor(getResources().getColor(R.color.black));
        }

        if (!goodToGo) {
            scrollView.smoothScrollTo(0, scrollToView.getTop());
            Toast.makeText(getActivity(), "Please fill the following fields: " + missingData, Toast.LENGTH_LONG).show();
        }

        return goodToGo;

    }

    private void popToSignupsFragment() {
        ((BaseContainerFragment) (getParentFragment())).popFragment();
    }

//    private void addFarmerToDB(Farmer newFarmer, String id, boolean synced) {
//
//        DatabaseHelper.getInstance().insertFarmer(newFarmer, id, synced);
//    }

//    private void updateFarmer(Farmer newFarmer, boolean synced) {
//        DatabaseHelper.getInstance().updateFarmer(newFarmer, synced);
//    }

//    private void addFarmToDB(Farm newFarm, String id, boolean synced) {
//        DatabaseHelper.getInstance().insertFarm(newFarm, id, synced);
//    }

//    private void updateFarm(Farm newFarm, boolean synced) {
//        DatabaseHelper.getInstance().updateFarm(newFarm, synced);
//    }

    private void addFarmWithFarmerId(String id) {
        HashMap<String, Object> farmMap = getFarmMap();
        farmMap.put("Farmer__c", id);
        WebApi.createFarm(createFarmCallback, farmMap);
    }

    Callback<ResponseModel> createFarmCallback = new Callback<ResponseModel>() {
        @Override
        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            loadingFinished();
            if (response.body() != null && response.body().success == true) {
                Log.i("LP", "Farm Added To Server");
                Toast.makeText(getContext(), "Farmer Enrolled Successfully", Toast.LENGTH_SHORT).show();

//                    addFarmToDB(farm, response.body().id, true);
                popToSignupsFragment();
            } else {
//                    addFarmToDB(farm, null, false);
//                    Toast.makeText(getContext(), "Farmer Enrollment Failed!", Toast.LENGTH_SHORT).show();
                String message = "";
                try {
                    String error = response.errorBody().string();
                    JSONArray arr = new JSONArray(error);
                    JSONObject obj = arr.getJSONObject(0);
                    message = obj.getString("message");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                if(message.isEmpty())
                    message = "Farmer Enrollment Failed";
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
            Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
        }

        @Override
        public void onFailure(Call<ResponseModel> call, Throwable t) {
            Log.i("LP", t.getMessage().toString());
            Toast.makeText(getContext(), "Farmer Enrollment Failed", Toast.LENGTH_SHORT).show();
//                addFarmToDB(farm, null, false);
            loadingFinished();
//                popToSignupsFragment();
        }
    };

    private void editFarmWithFarmId(String farmId) {

        HashMap<String, Object> farmMap = getFarmMap();
        farmMap.put("Farmer__c", farmer.farmerId);
        WebApi.editFarm(editFarmCallback, farmId, farmMap);
    }

    Callback<ResponseBody> editFarmCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("LP", "Farmer Edited Successfully");
                Toast.makeText(getContext(), "Farmer Edited Successfully", Toast.LENGTH_SHORT).show();
//                    updateFarm(farm, true);
                popToSignupsFragment();

            } else {
//                    updateFarm(farm, false);
//                    Toast.makeText(getContext(), "Farmer Edit Failed", Toast.LENGTH_SHORT).show();
                String message = "";
                try {
                    String error = response.errorBody().string();
                    JSONArray arr = new JSONArray(error);
                    JSONObject obj = arr.getJSONObject(0);
                    message = obj.getString("message");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                if(message.isEmpty())
                    message = "Farmer Edit Failed";
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
            Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("LP", t.getMessage().toString());
            Toast.makeText(getContext(), "Farmer Edit Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();

//                updateFarm(farm, false);
            loadingFinished();
//                popToSignupsFragment();
        }
    };

    private Farm createFarmWithFarmerId(String farmerId) {

        final Farm newFarm = new Farm();
        if (farmerId != null)
            newFarm.setFarmerId(farmerId);

        newFarm.setFacilitatorId(PreferenceHelper.getInstance().readLoginUserId());
        newFarm.setFarmerStatus(farmerStatus);
        newFarm.setLocation("");//location != null ? location : "");
        newFarm.setSubLocation("");//subLocation != null ? subLocation : "");
        newFarm.setVillageName("");//village != null ? village : "");
        newFarm.setTreeSpecies("");//treeSpecies != null ? treeSpecies : "");
        return newFarm;
    }

    private HashMap<String, Object> getFarmerMap() {

        final HashMap<String, Object> newFarmerMap = new HashMap<>();
        newFarmerMap.put("First_Name__c", etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        newFarmerMap.put("Middle_Name__c", etSecondName.getText() != null ? etSecondName.getText().toString() : "");
        newFarmerMap.put("Last_Name__c", etSurname.getText() != null ? etSurname.getText().toString() : "");
        newFarmerMap.put("Name", etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
        newFarmerMap.put("Mobile_Number__c", etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");
        newFarmerMap.put("Gender__c", (tvFemale.isSelected() == true) ? "Female" : "Male");
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

    private HashMap<String, Object> getFarmMap() {

        final HashMap<String, Object> newFarmMap = new HashMap<>();
        newFarmMap.put("LocationLookup__c", location);
        newFarmMap.put("Sub_LocationLookup__c", subLocation);
        newFarmMap.put("Village__c", village);
        newFarmMap.put("Tree_Specie__c", treeSpecies);
        newFarmMap.put("Is_Farmer_Home__c", txtFarmerHomeYes.isSelected()? true : false);

        if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FACILITATOR))
        {
            newFarmMap.put("Facilitator__c", PreferenceHelper.getInstance().readLoginUserId());
            newFarmMap.put("Facilitator_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }
        else if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
        {
            newFarmMap.put("Field_Officer_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }
        else if(PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
        {
            newFarmMap.put("Field_Manager_Signup__c", PreferenceHelper.getInstance().readLoginUserId());
        }

        if (farmerStatus != null && !farmerStatus.isEmpty()) {
            newFarmMap.put("Sign_Up_Status__c", farmerStatus);
        }

        return newFarmMap;
    }

    private final TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkEnableSubmit();
        }
    };

    private void checkEnableSubmit() {
        if (etFirstName.getText().toString().isEmpty()
                || etSecondName.getText().toString().isEmpty()
                || etSurname.getText().toString().isEmpty()
                || etIdNumber.getText().toString().isEmpty()
                || etMobileNumber.getText().toString().isEmpty()
                || !isFarmerPhotoSet
                || !isNationalIdPhotoSet
                || spLocation.getSelectedItem() == null
                || spSubLocation.getSelectedItem() == null
                || spVillage.getSelectedItem() == null
                || spTree.getSelectedItem() == null
                || !isEdit) {
            txtSubmitApproval.setEnabled(false);
        } else {
            txtSubmitApproval.setEnabled(true);
        }
    }

    @OnClick(R.id.txtSubmitApproval)
    void onClickSubmitForApproval(View view) {
        hideKeyboard();
        loadingStarted();

        farmerStatus = "Pending";
        createOrEditFarmer();
    }

    @OnClick(R.id.imgFarmerPhoto)
    void onClickFarmerPhoto(View view) {
        hideKeyboard();
        showPickerDialog(true);
//        pickFarmerImage(true);
    }

    @OnClick(R.id.imgNationalID)
    void onClickNationalID(View view) {
        hideKeyboard();
        showPickerDialog(false);
//        pickNationalIdImage(true);
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        hideKeyboard();
//        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

    @Override
    public void onTitleBarLeftIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).popFragment();
    }

    private void showPickerDialog(final boolean isFarmer) {
        AlertDialog.Builder pickerDialog = new AlertDialog.Builder(getActivity());
        pickerDialog.setTitle("Choose image from?");
        pickerDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (isFarmer) {
                            pickFarmerImage(true);
                        } else {
                            pickNationalIdImage(true);
                        }
                        dialog.dismiss();
                    }
                });
        pickerDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (isFarmer) {
                            pickFarmerImage(false);
                        } else {
                            pickNationalIdImage(false);
                        }
                        dialog.dismiss();
                    }
                });
        pickerDialog.show();
    }

    private void pickFarmerImage(boolean isDevice) {
        ImagePickerCallback imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                imgFarmerPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String originalPath = images.get(0).getOriginalPath();
                String uri = Uri.parse("file://" + originalPath).toString();

                MyPicassoInstance.getInstance().load(uri).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgFarmerPhoto);
                farmerImageUri = originalPath;
                isFarmerPhotoSet = true;
                if(farmer == null)
                    farmer = new Farmer();
                farmer.thumbUrl = uri;

                checkEnableSubmit();
            }

            @Override
            public void onError(String message) {
                // Do error handling
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

    private void pickNationalIdImage(boolean isDevice) {
        ImagePickerCallback imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                // Display images
                imgNationalID.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String originalPath = images.get(0).getOriginalPath();
                String uri = Uri.parse("file://" + originalPath).toString();

                MyPicassoInstance.getInstance().load(uri).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(transformation).into(imgNationalID);
                farmerIdImageUri = originalPath;
                isNationalIdPhotoSet = true;
                if(farmer == null)
                    farmer = new Farmer();
                farmer.nationalCardUrl = uri;

                checkEnableSubmit();
            }

            @Override
            public void onError(String message) {
                // Do error handling
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

    private void disableForm() {
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

    private void disableView(View view) {
        view.setEnabled(false);
        view.setFocusable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
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

    private void updateSubLocFromLocation(String locationId)
    {
        arrSubLocations = DatabaseHelper.getInstance().getSubLocationsFromLocation(locationId);
        strArrSubLocations.clear();
        for (int i = 0; i < arrSubLocations.size(); i++) {
            strArrSubLocations.add(arrSubLocations.get(i).name);
        }
    }

    private void updateVillageAndTreeFromSubLocation(String subLocationId)
    {
        arrVillages = DatabaseHelper.getInstance().getVillagesFromSubLocation(subLocationId);
        strArrVillages.clear();
        for (int i = 0; i < arrVillages.size(); i++) {
            strArrVillages.add(arrVillages.get(i).name);
        }

        arrTrees = DatabaseHelper.getInstance().getTreesFromSubLocation(subLocationId);
        strArrTrees.clear();
        for (int i = 0; i < arrTrees.size(); i++) {
            strArrTrees.add(arrTrees.get(i).name);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        hideKeyboard();
        Spinner spinnerView = (Spinner) parent;
        int position = pos - 1;
        switch (spinnerView.getId()) {
            case R.id.spLocation:
            {
                if (position < 0) {
                    location = "";
                    locationName = "";
                }
                else {
                    location = arrLocations.get(position).id;
                    locationName = arrLocations.get(position).name;
                }
                if((boolean)spLocation.getTag())
                {
                    updateSubLocFromLocation(location);

                    ArrayAdapter<String> arrayAdapterSubLoc = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrSubLocations);
                    spSubLocation.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterSubLoc, R.layout.spinner_nothing_selected, getContext(), "SUB LOCATION"));

                    strArrVillages.clear();
                    ArrayAdapter<String> arrayAdapterVillages = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrVillages);
                    spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterVillages, R.layout.spinner_nothing_selected, getContext(), "VILLAGE"));

                    strArrTrees.clear();
                    ArrayAdapter<String> arrayAdapterTrees = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrTrees);
                    spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterTrees, R.layout.spinner_nothing_selected, getContext(), "TREE SPECIES"));
                }
                else
                {
                    spLocation.setTag(true);
                }
            }
                break;
            case R.id.spSubLocation:
            {
                if (position < 0) {
                    subLocation = "";
                    subLocationName = "";
                }
                else {
                    subLocation = arrSubLocations.get(position).id;
                    subLocationName = arrSubLocations.get(position).name;
                }
                if((boolean)spSubLocation.getTag())
                {
                    updateVillageAndTreeFromSubLocation(subLocation);

                    ArrayAdapter<String> arrayAdapterVillages = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrVillages);
                    spVillage.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterVillages, R.layout.spinner_nothing_selected, getContext(), "VILLAGE"));

                    ArrayAdapter<String> arrayAdapterTrees = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, strArrTrees);
                    spTree.setAdapter(new NothingSelectedSpinnerAdapter(arrayAdapterTrees, R.layout.spinner_nothing_selected, getContext(), "TREE SPECIES"));
                }
                else
                {
                    spSubLocation.setTag(true);
                }
            }
                break;
            case R.id.spVillage:
                if (position < 0) {
                    village = "";
                    villageName = "";
                }
                else {
                    village = arrVillages.get(position).id;
                    villageName = arrVillages.get(position).name;
                }
                break;
            case R.id.spTree:
                if (position < 0) {
                    treeSpecies = "";
                    treeSpeciesName = "";
                }
                else {
                    treeSpecies = arrTrees.get(position).id;
                    treeSpeciesName = arrTrees.get(position).name;
                }
                break;
        }
        checkEnableSubmit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void editFarmerInDB() {
        // Save to DB
        String firstName = etFirstName.getText().toString();
        String secondName = etSecondName.getText().toString();
        String surname = etSurname.getText().toString();
        String idNumber = etIdNumber.getText().toString();
        String gender = (tvFemale.isSelected() == true) ? "Female" : "Male";
        boolean leader = tvLeaderYes.isSelected();
        String mobileNumber = etMobileNumber.getText().toString();
        boolean isFarmerHome = txtFarmerHomeYes.isSelected();

        String fullName = (firstName + " " + secondName).trim();
        fullName = (fullName + " " + surname).trim();

        final Farmer farmerDbObj = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerId", farmer.farmerId).findFirst();
        Realm.getDefaultInstance().beginTransaction();
        farmerDbObj.setAllValues(farmer.farmerId, farmer.farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, locationName, subLocationName, villageName, treeSpeciesName, isFarmerHome, mobileNumber, farmer.thumbAttachmentId, farmer.nationalCardAttachmentId, "", farmerStatus, false);
        if (farmerImageUri != null && !farmerImageUri.isEmpty())
            farmerDbObj.thumbUrl = farmer.thumbUrl;
        if (farmerIdImageUri != null && !farmerIdImageUri.isEmpty())
            farmerDbObj.nationalCardUrl = farmer.nationalCardUrl;
        Realm.getDefaultInstance().commitTransaction();

        loadingFinished();
        Log.i("LP", "Farmer Edited Successfully");
        Toast.makeText(getContext(), "Farmer Edited Successfully", Toast.LENGTH_SHORT).show();
        popToSignupsFragment();
    }

    private void editFarmer() {
        if(NetworkHelper.isNetAvailable(getActivity())){
            final HashMap<String, Object> farmerMap = getFarmerMap();
            WebApi.editFarmer(editFarmerCallback, farmer.farmerId, farmerMap);
        }
        else {
            editFarmerInDB();
        }
    }

    Callback<ResponseBody> editFarmerCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("LP", "Farmer Edited!");
//                    updateFarmer(farmer, true);
                Farm newFarm = createFarmWithFarmerId(farmer.farmerId);
                newFarm.farmId = farmer.farmId;
//                    editFarmWithFarmId(newFarm, farmer.farmId);
                editFarmWithFarmId(farmer.farmId);

                attachFarmerImageToFarmerObject(farmer);
                attachFarmerIDImageToFarmerObject(farmer);

            } else {

                String message = "";
                try {
                    String error = response.errorBody().string();
                    JSONArray arr = new JSONArray(error);
                    JSONObject obj = arr.getJSONObject(0);
                    message = obj.getString("message");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                if(message.isEmpty())
                    message = "Farmer Edit Failed";
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                    updateFarmer(farmer, false);
//                    Farm newFarm = createFarmWithFarmerId(farmer.farmerId);
//                    newFarm.farmId = farmer.farmId;
//                    updateFarm(newFarm, false);

                loadingFinished();
//                    popToSignupsFragment();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("LP", t.getMessage().toString());
            String message = t.getMessage();
            Toast.makeText(getContext(), "Farmer Edit Failed: " + message, Toast.LENGTH_SHORT).show();
//                updateFarmer(farmer, false);
//                Farm newFarm = createFarmWithFarmerId(farmer.farmerId);
//                newFarm.farmId = farmer.farmId;
//                updateFarm(newFarm, false);
            loadingFinished();
//                popToSignupsFragment();
        }
    };

    private void createFarmerInDB() {
        // Save to DB
        String id = String.valueOf(System.currentTimeMillis());
        String firstName = etFirstName.getText().toString();
        String secondName = etSecondName.getText().toString();
        String surname = etSurname.getText().toString();
        String idNumber = etIdNumber.getText().toString();
        String gender = (tvFemale.isSelected() == true) ? "Female" : "Male";
        boolean leader = tvLeaderYes.isSelected();
        String mobileNumber = etMobileNumber.getText().toString();
        boolean isFarmerHome = txtFarmerHomeYes.isSelected();

        String fullName = (firstName + " " + secondName).trim();
        fullName = (fullName + " " + surname).trim();

        Realm.getDefaultInstance().beginTransaction();
        final Farmer farmerDbObj = Realm.getDefaultInstance().createObject(Farmer.class);
        farmerDbObj.setAllValues(id, id, fullName, firstName, secondName, surname, idNumber, gender, leader, locationName, subLocationName, villageName, treeSpeciesName, isFarmerHome, mobileNumber, id, id, "", farmerStatus, false);
        if(farmerImageUri != null && !farmerImageUri.isEmpty())
            farmerDbObj.thumbUrl = farmer.thumbUrl;
        if(farmerIdImageUri != null && !farmerIdImageUri.isEmpty())
            farmerDbObj.nationalCardUrl = farmer.nationalCardUrl;
        Realm.getDefaultInstance().commitTransaction();

        loadingFinished();
        Log.i("LP", "Farm Added To DB");
        Toast.makeText(getContext(), "Farmer Enrolled Successfully", Toast.LENGTH_SHORT).show();
        popToSignupsFragment();
    }

    private void createFarmer() {
        if(NetworkHelper.isNetAvailable(getActivity())) {
            final HashMap<String, Object> farmerMap = getFarmerMap();
            WebApi.createFarmer(createFarmerCallback, farmerMap);
        }
        else {
            createFarmerInDB();
        }
    }

    Callback<ResponseModel> createFarmerCallback = new Callback<ResponseModel>() {
        @Override
        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            Farmer newFarmer = getFarmer();
            if ((response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) && response.body() != null && response.body().success == true) {
                Log.i("LP", "Farmer Added To Server");
                newFarmer.farmerId = response.body().id;
//                    addFarmerToDB(newFarmer, response.body().id, true);
//                Farm newFarm = createFarmWithFarmerId(response.body().id);
//                    addFarmWithFarmerId(newFarm, response.body().id);
                addFarmWithFarmerId(response.body().id);

                attachFarmerImageToFarmerObject(newFarmer);
                attachFarmerIDImageToFarmerObject(newFarmer);

            } else {

//                    addFarmerToDB(newFarmer, null, false);
//                    Farm newFarm = createFarmWithFarmerId(null);
//                    addFarmToDB(newFarm, null, false);
//                    Toast.makeText(getContext(), "Farmer Enrollment Failed", Toast.LENGTH_SHORT).show();

                loadingFinished();
//                    popToSignupsFragment();

                String message = "";
                try {
                    String error = response.errorBody().string();
                    JSONArray arr = new JSONArray(error);
                    JSONObject obj = arr.getJSONObject(0);
                    message = obj.getString("message");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                if(message.isEmpty())
                    message = "Farmer Enrollment Failed";
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

            }
            Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
        }

        @Override
        public void onFailure(Call<ResponseModel> call, Throwable t) {
            Log.i("LP", t.getMessage().toString());
            Toast.makeText(getContext(), "Farmer Enrollment Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();

//                Farmer newFarmer = getFarmer();
//                addFarmerToDB(newFarmer, null, false);
//                Farm newFarm = createFarmWithFarmerId(null);
//                addFarmToDB(newFarm, null, false);

            loadingFinished();
//                popToSignupsFragment();
        }
    };

    private void attachFarmerImageToFarmerObject(final Farmer farmer) {

        if (farmerImageUri == null)
            return;

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "picture");
        attachmentMap.put("Name", "profile_picture.png");
        if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.farmerId);
        }
        else
        {
            MyPicassoInstance.getInstance().invalidate(farmer.getThumbUrl());
        }

        JSONObject json = new JSONObject(attachmentMap);

//        File f = new File(farmerImageUri);
//        byte[] byteArrayImage = getByteArrayFromFile(f);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        bmp = PhotoUtils.decodeSampledBitmapFromResource(farmerImageUri);

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
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(farmerImageUri));
        }

//        bmp = BitmapFactory.decodeFile(farmerImageUri);
//        if (bmp == null) {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bmp.compress(Bitmap.CompressFormat.JPEG, 40, bos);
//            byteArrayImage = bos.toByteArray();
//            try {
//                bos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            File f = new File(farmerImageUri);
//            byteArrayImage = getByteArrayFromFile(f);
//        }

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        if (farmer.getThumbAttachmentId() == null || farmer.getThumbAttachmentId().isEmpty()) {
            WebApi.addAttachment(addFarmerPicCallback, entityBody, imageBody);
        } else {
            WebApi.editAttachment(editFarmerPicCallback, farmer.getThumbAttachmentId(), entityBody, imageBody);
        }
    }

    Callback<ResponseBody> addFarmerPicCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("Fennel", "farmer profile picture uploaded successfully!");
                Singleton.getInstance().farmerIdtoInvalidate = farmer.farmerId;
            } else {
                Log.i("Fennel", "farmer profile picture upload failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "farmer profile picture upload failed!");
        }
    };

    Callback<ResponseBody> editFarmerPicCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("Fennel", "farmer profile picture edited successfully!");
                Singleton.getInstance().farmerIdtoInvalidate = farmer.farmerId;
            } else {
                Log.i("Fennel", "farmer profile picture edit failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "farmer profile picture edit failed!");
        }
    };

    private void attachFarmerIDImageToFarmerObject(Farmer farmer) {

        if (farmerIdImageUri == null)
            return;

        HashMap<String, Object> attachmentMap = new HashMap<>();
        attachmentMap.put("Description", "ID");
        attachmentMap.put("Name", "national_id.png");
        if(farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            attachmentMap.put("ParentId", farmer.farmerId);
        }
        else
        {
            MyPicassoInstance.getInstance().invalidate(farmer.getNationalCardUrl());
        }

        JSONObject json = new JSONObject(attachmentMap);

        byte[] byteArrayImage = null;
        Bitmap bmp = null;

        bmp = PhotoUtils.decodeSampledBitmapFromResource(farmerIdImageUri);

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
            byteArrayImage = PhotoUtils.getByteArrayFromFile(new File(farmerIdImageUri));
        }

        RequestBody entityBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), byteArrayImage);

        if (farmer.getNationalCardAttachmentId() == null || farmer.getNationalCardAttachmentId().isEmpty()) {
            WebApi.addAttachment(addFarmerIdPicCallback, entityBody, imageBody);
        } else {
            WebApi.editAttachment(editFarmerIdPicCallback, farmer.getNationalCardAttachmentId(), entityBody, imageBody);
        }
    }

    Callback<ResponseBody> addFarmerIdPicCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                //Save id with attac
                // hment
                Log.i("Fennel", "farmer ID picture uploaded successfully!");
            } else {
                Log.i("Fennel", "farmer ID picture upload failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "farmer ID picture upload failed!");
        }
    };

    Callback<ResponseBody> editFarmerIdPicCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == Constants.RESPONSE_SUCCESS || response.code() == Constants.RESPONSE_SUCCESS_ADDED || response.code() == Constants.RESPONSE_SUCCESS_NO_CONTENT) {
                Log.i("Fennel", "farmer ID picture edited successfully!");
            } else {
                Log.i("Fennel", "farmer ID picture edit failed!");
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.i("Fennel", "farmer ID picture edit failed!");
        }
    };

    private void hideKeyboard() {

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            hideKeyboard();
        }
        return false;
    }
}
