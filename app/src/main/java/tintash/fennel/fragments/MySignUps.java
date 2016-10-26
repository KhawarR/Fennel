package tintash.fennel.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tintash.fennel.R;
import tintash.fennel.activities.LoginActivity;
import tintash.fennel.adapters.MySignupsAdapter;
import tintash.fennel.application.Fennel;
import tintash.fennel.common.database.DatabaseHelper;
import tintash.fennel.models.Farmer;
import tintash.fennel.models.Location;
import tintash.fennel.models.SubLocation;
import tintash.fennel.models.Tree;
import tintash.fennel.models.Village;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.network.Session;
import tintash.fennel.utils.CircleViewTransformation;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.MyPicassoInstance;
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

    CircleImageView cIvIconRight;

    ArrayList<Farmer> myFarmers = new ArrayList<>();

    MySignupsAdapter adapter;

    int locationsResponseCounter = 0;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_sign_ups, container, false);
        ButterKnife.bind(this, view);

        getMySignups();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        String aboutMeAttId = PreferenceHelper.getInstance().readAboutAttId();
        if(!aboutMeAttId.isEmpty())
        {
            String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), aboutMeAttId);
            MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);

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
        etSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (etSearch.getRight() - etSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                        return true;
                    }
                }
                return false;
            }
        });
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    return true;
                }
                return false;
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
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_EDIT_FARMER, farmer), true);
                }
            }
        });

//        boolean isFirstRun = PreferenceHelper.getInstance().readFirstRun();
//        if (isFirstRun) {
//            PreferenceHelper.getInstance().writeFirstRun(false);
            getLocationsData();
//        }

        getAboutMeAttachment();
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getMySignups();
        }
    };

    private void getMySignups()
    {
        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        String query = String.format(NetworkHelper.QUERY_MY_SIGNUPS_1, PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId(), PreferenceHelper.getInstance().readLoginUserId());
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

    private void getLocationsData() {

//        mProgressDialog.setMessage("Initializing!");
//        loadingStarted();

        String locationsQuery = NetworkHelper.GET_LOCATIONS;
        Call<ResponseBody> locationsApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, locationsQuery);
        locationsApi.enqueue(getLocationsCallback);

        String subLocationsQuery = NetworkHelper.GET_SUB_LOCATIONS;
        Call<ResponseBody> subLocationsApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, subLocationsQuery);
        subLocationsApi.enqueue(getSubLocationsCallback);

        String villagesQuery = NetworkHelper.GET_VILLAGES;
        Call<ResponseBody> villagesApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, villagesQuery);
        villagesApi.enqueue(getVillagesCallback);

        String treesQuery = NetworkHelper.GET_TREES;
        Call<ResponseBody> treesApi = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, treesQuery);
        treesApi.enqueue(getTreesCallback);

    }

    private Callback<ResponseBody> myFarmersAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
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
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
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

    private Callback<ResponseBody> mySignupsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
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
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
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
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        String description = objAttachment.getString("Description").toLowerCase().trim();
                        if(description.contains("pic") || description.contains("photo"))
                        {
                            farmerPicId = objAttachment.getString("Id");
                        }
                        else if(description.contains("id"))
                        {
                            farmerNatId = objAttachment.getString("Id");
                        }
                    }
//                    if(attRecords.length() > 0)
//                    {
//                        JSONObject objFarmerPhoto = attRecords.getJSONObject(0);
//                        farmerPicId = objFarmerPhoto.getString("Id");
//                    }
//
//                    if(attRecords.length() > 1)
//                    {
//                        JSONObject objFarmerPhoto = attRecords.getJSONObject(1);
//                        farmerNatId = objFarmerPhoto.getString("Id");
//                    }
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
                boolean isFarmerHome = false;
                boolean leader = false;

                farmId = farmObj.getString("Id");
                JSONObject objLocation = farmObj.optJSONObject("LocationLookup__r");
                if(objLocation != null)
                {
                    location = objLocation.getString("Name");
                }

                JSONObject objSubLocation = farmObj.optJSONObject("Sub_LocationLookup__r");
                if(objSubLocation != null)
                {
                    subLocation = objSubLocation.getString("Name");
                }

                JSONObject objVillage = farmObj.optJSONObject("Village__r");
                if(objVillage != null)
                {
                    village = objVillage.getString("Name");
                }


                JSONObject objTree = farmObj.optJSONObject("Tree_Specie__r");
                if(objTree != null)
                {
                    tree = objTree.getString("Name");
                }

                id = farmObj.optString("Farmer__c");
                if(id != null && id.equalsIgnoreCase("null")) id = "";

                JSONObject objFarmer = farmObj.optJSONObject("Farmer__r");
                if(objFarmer != null)
                {
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

                isFarmerHome = farmObj.optBoolean("Is_Farmer_Home__c");

//                String status = farmObj.getString("Status__c");
                String status = farmObj.getString("Sign_Up_Status__c");

                String locationName = DatabaseHelper.getInstance().getLocationNameForId(location);
                String subLocationName = DatabaseHelper.getInstance().getSubLocationNameForId(subLocation);
                String villageName = DatabaseHelper.getInstance().getVillageNameForId(village);
                String treeName = DatabaseHelper.getInstance().getTreeNameForId(tree);


                if(status.equalsIgnoreCase(Constants.STR_INCOMPLETE))
                {
                    incompleteFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, locationName, subLocationName, villageName, treeName, isFarmerHome, mobileNumber, "", "", "", status, false));
                }
                else if(status.equalsIgnoreCase(Constants.STR_PENDING))
                {
                    pendingFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, subLocation, village, tree, isFarmerHome, mobileNumber, "", "", "", status, false));
                }
                else if(status.equalsIgnoreCase(Constants.STR_APPROVED))
                {
                    approvedFarmersList.add(new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, subLocation, village, tree, isFarmerHome, mobileNumber, "", "", "", status, false));
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

    private void parseLocations(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0) {
            ArrayList<Location> allLocations = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject locationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";

                id = locationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = locationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                Location location = new Location(id, name);
                allLocations.add(location);
                DatabaseHelper.getInstance().insertLocation(location);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseSubLocations(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0) {
            ArrayList<SubLocation> allSubLocations = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject subLocationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";
                String locationId = "";

                id = subLocationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = subLocationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                locationId = subLocationObj.getString("Location__c");
                if (locationId.equalsIgnoreCase("null")) locationId = "";

                SubLocation subLocation = new SubLocation(id, name, locationId);
                allSubLocations.add(subLocation);
                DatabaseHelper.getInstance().insertSubLocation(subLocation);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseVillages(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0) {
            ArrayList<Village> allVillages = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject subLocationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";
                String subLocationId = "";

                id = subLocationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = subLocationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                subLocationId = subLocationObj.getString("Sub_Location__c");
                if (subLocationId.equalsIgnoreCase("null")) subLocationId = "";

                Village village = new Village(id, name, subLocationId);
                allVillages.add(village);
                DatabaseHelper.getInstance().inserVillage(village);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseTrees(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0) {
            ArrayList<Tree> allTrees = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject subLocationObj = arrRecords.getJSONObject(i);

                String id = "";
                String name = "";
                String subLocationId = "";

                id = subLocationObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = subLocationObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                subLocationId = subLocationObj.getString("Sub_Location__c");
                if (subLocationId.equalsIgnoreCase("null")) subLocationId = "";

                Tree tree = new Tree(id, name, subLocationId);
                allTrees.add(tree);
                DatabaseHelper.getInstance().insertTree(tree);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).replaceFragment(new AboutMe(), true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.rl_add:
            {
                ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_ENROLL_FARMER, null), true);
            }
                break;
        }
    }

    private Callback<ResponseBody> getLocationsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            locationsResponseCounter++;
//            if (locationsResponseCounter == 4)
//                loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    try {
                        parseLocations(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            locationsResponseCounter++;
            if (locationsResponseCounter == 4)
                loadingFinished();
            t.printStackTrace();
        }
    };

    private Callback<ResponseBody> getSubLocationsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            locationsResponseCounter++;
//            if (locationsResponseCounter == 4)
//                loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    try {
                        parseSubLocations(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            locationsResponseCounter++;
            if (locationsResponseCounter == 4)
                loadingFinished();
            t.printStackTrace();
        }
    };

    private Callback<ResponseBody> getVillagesCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            locationsResponseCounter++;
//            if (locationsResponseCounter == 4)
//                loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    try {
                        parseVillages(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            locationsResponseCounter++;
            if (locationsResponseCounter == 4)
                loadingFinished();
            t.printStackTrace();
        }
    };

    private Callback<ResponseBody> getTreesCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            locationsResponseCounter++;
//            if (locationsResponseCounter == 4)
//                loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    try {
                        parseTrees(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            locationsResponseCounter++;
            if (locationsResponseCounter == 4)
                loadingFinished();
            t.printStackTrace();
        }
    };

    private void getAboutMeAttachment() {
        String queryTable = "Employee__c";
//        String userType = PreferenceHelper.getInstance().readLoginUserType();
//        if(userType.equalsIgnoreCase(Constants.STR_FACILITATOR))
//            queryTable = "Facilitator__c";
//        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER))
//            queryTable = "Field_Officer__c";
//        else if(userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER))
//            queryTable = "Field_Manager__c";

        String query = String.format(NetworkHelper.QUERY_ABOUT_ME_ATTACHMENT, queryTable, PreferenceHelper.getInstance().readUserEmployeeId());
        Call<ResponseBody> apiCall = Fennel.getWebService().query(Session.getAuthToken(), NetworkHelper.API_VERSION, query);
        apiCall.enqueue(aboutMeAttachmentCallback);
    }

    private Callback<ResponseBody> aboutMeAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.code() == 200) {
                String responseStr = "";

                try {
                    responseStr = response.body().string();
                    String attId = parseAboutMeDataAttachment(responseStr);
                    PreferenceHelper.getInstance().writeAboutAttId(attId);
                    if(!attId.isEmpty())
                    {
                        String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), attId);
                        MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
                    }
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

    private String parseAboutMeDataAttachment(String data) throws JSONException {
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

}
