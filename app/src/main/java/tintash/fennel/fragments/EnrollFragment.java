package tintash.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("ViewCreated Enroll ");

        titleBarLayout.setOnIconClickListener(this);

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

        final Farmer newFarmer = new Farmer();
        newFarmer.setFirstName(etFirstName.getText() != null ? etFirstName.getText().toString() : "");
        newFarmer.setSecondName(etSecondName.getText() != null ? etSecondName.getText().toString() : "");
        newFarmer.setSurname(etSurname.getText() != null ? etSurname.getText().toString() : "");
        newFarmer.setIdNumber(etIdNumber.getText() != null ? etIdNumber.getText().toString() : "");
        newFarmer.setMobileNumber(etMobileNumber.getText() != null ? etMobileNumber.getText().toString() : "");

        Call<ResponseModel> apiCall = Fennel.getWebService().addFarmer(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, newFarmer);
        apiCall.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null && response.body().success == true) {
                    Log.i("LP", "Farmer Added To Server");
                    addFarmerToDB(newFarmer, response.body().id, true);
                    addFarmWithFarmerId(response.body().id);
                } else {
                    addFarmerToDB(newFarmer, null, false);
                }
                Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());
                addFarmerToDB(newFarmer, null, false);
            }
        });
    }

    private void addFarmerToDB(Farmer newFarmer, String id, boolean synced) {

        DatabaseHelper.getInstance().insertFarmer(newFarmer, id, synced);
    }

    private void addFarmToDB(Farm newFarm, String id, boolean synced) {
        DatabaseHelper.getInstance().insertFarm(newFarm, id, synced);
    }

    private void addFarmWithFarmerId(String id) {

        final Farm newFarm = new Farm();
        newFarm.setFacilitatorId(PreferenceHelper.getInstance().readFacilitatorId());
        newFarm.setFarmerId(id);
        newFarm.setLocation(location != null ? location : "");
        newFarm.setSubLocation(subLocation != null ? subLocation : "");
        newFarm.setVillageName(village != null ? village : "");
        newFarm.setTreeSpecies(treeSpecies != null ? treeSpecies : "");

        Call<ResponseModel> apiCall = Fennel.getWebService().addFarm(Session.getAuthToken(), "application/json", NetworkHelper.API_VERSION, newFarm);
        apiCall.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null && response.body().success == true) {
                    Log.i("LP", "Farm Added To Server");
                    addFarmToDB(newFarm, response.body().id, true);
                } else {
                    addFarmToDB(newFarm, null, false);
                }
                Log.i("LP", ((response.body() != null) ? response.body().toString() : ""));
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.i("LP", t.getMessage().toString());
                addFarmToDB(newFarm, null, false);
            }
        });
    }

    @OnClick(R.id.txtSubmitApproval)
    void onClickSubmitForApproval(View view) {

    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
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
