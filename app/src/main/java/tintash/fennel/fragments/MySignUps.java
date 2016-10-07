package tintash.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.adapters.MySignupsAdapter;
import tintash.fennel.application.Fennel;
import tintash.fennel.models.Farmer;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.PreferenceHelper;
import tintash.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class MySignUps extends BaseFragment implements View.OnClickListener {

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.lv_farmers)
    ListView mLvFarmers;

    EditText etSearch;

    RelativeLayout rlAdd;

    ArrayList<Farmer> myFarmers = new ArrayList<>();

    MySignupsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_sign_ups, container, false);
        ButterKnife.bind(this, view);

//        populateDummyData();
        getMySignups();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);

        LayoutInflater myinflater = getActivity().getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)myinflater.inflate(R.layout.header_mysignups_list, mLvFarmers, false);

        rlAdd = (RelativeLayout) myHeader.findViewById(R.id.rl_add);
        rlAdd.setOnClickListener(this);

        etSearch = (EditText) myHeader.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = etSearch.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });

        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);
        mLvFarmers.addHeaderView(myHeader);

        mLvFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= mLvFarmers.getHeaderViewsCount();
                Farmer farmer = myFarmers.get(position);
                if(!farmer.isHeader())
                {
                    ((BaseContainerFragment) getParentFragment()).addFragment(EnrollFragment.newInstance(Constants.STR_EDIT_FARMER, farmer), true);
                }
            }
        });
    }

    private void getMySignups()
    {
        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS, PreferenceHelper.getInstance().readFacilitatorId());
        loadingStarted();
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(mySignupsCallback);
    }

    private void getMyFarmerAttachments()
    {
        String query = NetworkHelper.FARMER_QUERY;
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(myFarmersAttachments);
    }

    private Callback<ResponseBody> myFarmersAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseFarmerAttachmentData(responseStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private Callback<ResponseBody> mySignupsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    parseData(responseStr);
                    getMyFarmerAttachments();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            loadingFinished();
            t.printStackTrace();
        }
    };

    private void parseFarmerAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmerObj = arrRecords.getJSONObject(i);
                String id = farmerObj.getString("Id");

                String farmerPicId = "";
                String farmerNatId = "";

                JSONObject attachmentObj = farmerObj.optJSONObject("Attachments");
                if(attachmentObj != null)
                {
                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    if(attRecords.length() > 0)
                    {
                        JSONObject objFarmerPhoto = attRecords.getJSONObject(0);
                        farmerPicId = objFarmerPhoto.getString("Id");
                    }

                    if(attRecords.length() > 1)
                    {
                        JSONObject objFarmerPhoto = attRecords.getJSONObject(1);
                        farmerNatId = objFarmerPhoto.getString("Id");
                    }
                }

                for (int j = 0; j < myFarmers.size(); j++) {
                    Farmer farmer = myFarmers.get(j);
                    if(farmer.getFarmerId().equalsIgnoreCase(id))
                    {
                        farmer.setThumbUrl(farmerPicId);
                        farmer.setFarmerIdPhotoUrl(farmerNatId);
                        break;
                    }
                }
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void parseData(String data) throws JSONException {

        myFarmers.clear();

        ArrayList<Farmer> incompleteFarmersList = new ArrayList<>();
        ArrayList<Farmer> pendingFarmersList = new ArrayList<>();
        ArrayList<Farmer> approvedFarmersList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0)
        {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmObj = arrRecords.getJSONObject(i);

                String id = "";
                String farmId = "";
                String location = "";
                String subLocation = "";
                String tree = "";
                String village = "";
                String fullName = "";
                String firstName = "";
                String secondName = "";
                String surname = "";
                String idNumber = "";
                String gender = "";
                String mobileNumber = "";
                boolean leader = false;

                farmId = farmObj.getString("Id");

                JSONObject objLocation = farmObj.optJSONObject("Location__r");
                if(objLocation != null)
                {
                    location = objLocation.getString("Name");
                }

                JSONObject objSubLocation = farmObj.optJSONObject("Sub_Location__r");
                if(objSubLocation != null)
                {
                    subLocation = objSubLocation.getString("Name");
                }

                JSONObject objTree = farmObj.optJSONObject("Tree__r");
                if(objTree != null)
                {
                    tree = objTree.getString("Name");
                }

                JSONObject objVillage = farmObj.optJSONObject("Village__r");
                if(objVillage != null)
                {
                    village = objVillage.getString("Name");
                }

                JSONObject objFarmer = farmObj.optJSONObject("Farmers__r");
                if(objFarmer != null)
                {
                    id = objFarmer.getString("Id");
                    if(id.equalsIgnoreCase("null")) id = "";
                    fullName = objFarmer.getString("FullName__c");
                    if(fullName.equalsIgnoreCase("null")) fullName = "";
                    firstName = objFarmer.getString("First_Name__c");
                    if(firstName.equalsIgnoreCase("null")) firstName = "";
                    secondName = objFarmer.getString("Middle_Name__c");
                    if(secondName.equalsIgnoreCase("null")) secondName = "";
                    surname = objFarmer.getString("Last_Name__c");
                    if(surname.equalsIgnoreCase("null")) surname = "";
                    idNumber = objFarmer.getString("Name");
                    if(idNumber.equalsIgnoreCase("null")) idNumber = "";
                    gender = objFarmer.getString("Gender__c");
                    if(gender.equalsIgnoreCase("null")) gender = "";
                    mobileNumber = objFarmer.getString("Mobile_Number__c");
                    if(mobileNumber.equalsIgnoreCase("null")) mobileNumber = "";
                    leader = objFarmer.getBoolean("Leader__c");
                }

                String status = farmObj.getString("Status__c");

                if(status.equalsIgnoreCase(Constants.STR_INCOMPLETE))
                {
                    incompleteFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, subLocation, village, tree, false, mobileNumber, "", "", "", status, false));
                }
                else if(status.equalsIgnoreCase(Constants.STR_PENDING))
                {
                    pendingFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, subLocation, village, tree, false, mobileNumber, "", "", "", status, false));
                }
                else if(status.equalsIgnoreCase(Constants.STR_APPROVED))
                {
                    approvedFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, subLocation, village, tree, false, mobileNumber, "", "", "", status, false));
                }
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }

        if(incompleteFarmersList.size() > 0)
        {
            myFarmers.add(new Farmer("", "", Constants.STR_INCOMPLETE, "", "", "", "", "", false, "", "", "", "", false, "", "", "", "", "", true));
            myFarmers.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            myFarmers.add(new Farmer("", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", false, "", "", "", "", "", true));
            myFarmers.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            myFarmers.add(new Farmer("", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", false, "", "", "", "", "", true));
            myFarmers.addAll(approvedFarmersList);
        }

        // Creating our custom adapter
        adapter = new MySignupsAdapter(getActivity(), myFarmers);
        // Create the list view and bind the adapter
        mLvFarmers.setAdapter(adapter);
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.rl_add:
            {
                ((BaseContainerFragment) getParentFragment()).addFragment(EnrollFragment.newInstance(Constants.STR_ENROLL_FARMER, null), true);
            }
                break;
        }
    }
}
