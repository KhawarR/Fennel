package wal.fennel.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.NetworkPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.activities.LoginActivity;
import wal.fennel.activities.SplashActivity;
import wal.fennel.adapters.MySignupsAdapter;
import wal.fennel.application.Fennel;
import wal.fennel.common.database.DatabaseHelper;
import wal.fennel.models.Farmer;
import wal.fennel.models.Location;
import wal.fennel.models.SubLocation;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.models.TaskItemOption;
import wal.fennel.models.Tree;
import wal.fennel.models.Village;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.Session;
import wal.fennel.network.WebApi;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MixPanelConstants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;
import wal.fennel.views.TitleBarLayout;

import static io.realm.Realm.getDefaultInstance;

/**
 * Created by Faizan on 9/27/2016.
 */
public class MySignUps extends BaseFragment implements View.OnClickListener {

    private MixpanelAPI mixPanel;

    //region Class Variables & UI
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    @Bind(R.id.lv_farmers)
    ListView mLvMySignups;

    EditText etSearch;
    RelativeLayout rlAdd;
    CircleImageView cIvIconRight;
    SwipeRefreshLayout mSwipeRefreshLayout;

    MySignupsAdapter adapter;

//    RealmList<Farmer> myFarmers = new RealmList<>();
    int locationsResponseCounter = 0;
    //endregion

    ArrayList<TaskItem> allTaskItems = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_sign_ups, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mixPanel = MixpanelAPI.getInstance(getActivity(), MixPanelConstants.MIXPANEL_TOKEN);
        mixPanel.track(MixPanelConstants.PageView.MY_SIGNUPS);

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        LayoutInflater myinflater = getActivity().getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)myinflater.inflate(R.layout.header_mysignups_list, mLvMySignups, false);

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

                        try {
                            String strSearchKey = etSearch.getText().toString().trim();
                            JSONObject props = new JSONObject();
                            props.put(MixPanelConstants.Property.SEARCH_KEY, strSearchKey);
                            mixPanel.track(MixPanelConstants.Event.SEARCH_MYSIGNUP_ACTION, props);
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }

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
                    try {
                        String strSearchKey = etSearch.getText().toString().trim();
                        JSONObject props = new JSONObject();
                        props.put(MixPanelConstants.Property.SEARCH_KEY, strSearchKey);
                        mixPanel.track(MixPanelConstants.Event.SEARCH_MYSIGNUP_ACTION, props);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);
        mLvMySignups.addHeaderView(myHeader);

        mLvMySignups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= mLvMySignups.getHeaderViewsCount();
                Farmer farmer = Singleton.getInstance().mySignupsList.get(position);
                if(!farmer.isHeader())
                {
                    try {
                        JSONObject props = new JSONObject();
                        props.put(MixPanelConstants.Property.MYSIGNUP_STATUS, farmer.getSignupStatus());
                        props.put(MixPanelConstants.Property.MYSIGNUP_NAME, farmer.getFullName());
                        props.put(MixPanelConstants.Property.MYSIGNUP_ID, farmer.getFarmerId());
                        mixPanel.track(MixPanelConstants.Event.MYSIGNUP_MENU_ITEM_ACTION, props);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_EDIT_FARMER, farmer), true);
                }
            }
        });

        getMySignups();
        getDropDownsData();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadAttachment();
//        if(mLvMySignups != null && adapter != null)
//            adapter.notifyDataSetChanged();
        refreshDataFromDB();
        IntentFilter iff= new IntentFilter(Constants.MY_SIGNPS_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onMySignupsUpdated, iff);
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onMySignupsUpdated);
    }

    private void loadAttachment() {
        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if(!thumbUrl.isEmpty())
        {
            if(NetworkHelper.isNetAvailable(getActivity()))
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            else
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getMySignups();
        }
    };

    //region Network calls, callbacks & parsers
    private void getMySignups(){

        if(NetworkHelper.isNetAvailable(getActivity()) && !WebApi.isSyncRequired() && PreferenceHelper.getInstance().readFirstRun())
        {
            getMySignupsFromServer();
            getMyFarmerTasksData();
        }
        else
        {
            getMySignupsFromDB();
            getMyFarmerTasksFromDB();
            if(PreferenceHelper.getInstance().isSessionExpiredSyncReq())
                WebApi.syncAll(null);
        }
    }

    private void getMySignupsFromDB()
    {
        Singleton.getInstance().mySignupsList.clear();

        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        loadingStarted();

        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYSIGNUPS.toString()).findAll();

        ArrayList<Farmer> incompleteFarmersList = new ArrayList<>();
        ArrayList<Farmer> pendingFarmersList = new ArrayList<>();
        ArrayList<Farmer> approvedFarmersList = new ArrayList<>();

        for (int i = 0; i < farmerDbList.size(); i++) {
            Farmer farmer = farmerDbList.get(i);

            if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_ENROLLED))
            {
                incompleteFarmersList.add(farmer);
            }
            else if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_PENDING))
            {
                pendingFarmersList.add(farmer);
            }
            else if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_APPROVED))
            {
                approvedFarmersList.add(farmer);
            }
        }

        if(incompleteFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(approvedFarmersList);
        }

        // Creating our custom adapter
        adapter = new MySignupsAdapter(getActivity(), Singleton.getInstance().mySignupsList);
        // Create the list view and bind the adapter
        mLvMySignups.setAdapter(adapter);

        loadingFinished();
    }

    private void getMyFarmerTasksFromDB()
    {
        Singleton.getInstance().myFarmersList.clear();

        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        loadingStarted();

        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYFARMERTASKS.toString()).findAll();
        Singleton.getInstance().myFarmersList.addAll(farmerDbList);

        loadingFinished();
    }

    private void getMySignupsFromServer()
    {
        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        loadingStarted();
        WebApi.getMySignUps(mySignupsCallback);
        WebApi.getAboutMeAttachment(aboutMeAttachmentCallback);
    }

    private Callback<ResponseBody> mySignupsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            WebApi.saveSyncTimeStamp();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        PreferenceHelper.getInstance().writeFirstRun(false);
                        parseData(responseStr);
                        WebApi.getMyFarmerAttachments(myFarmersAttachments);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    sessionExpireRedirect();
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

    private void parseData(String data) throws JSONException {

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
                String locationId = "";
                String subLocation = "";
                String subLocationId = "";
                String tree = "";
                String treeId = "";
                String village = "";
                String villageId = "";
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
                    locationId = objLocation.getString("Id");
                }

                JSONObject objSubLocation = farmObj.optJSONObject("Sub_LocationLookup__r");
                if(objSubLocation != null)
                {
                    subLocation = objSubLocation.getString("Name");
                    subLocationId = objSubLocation.getString("Id");
                }

                JSONObject objVillage = farmObj.optJSONObject("Village__r");
                if(objVillage != null)
                {
                    village = objVillage.getString("Name");
                    villageId = objVillage.getString("Id");
                }


                JSONObject objTree = farmObj.optJSONObject("Tree_Specie__r");
                if(objTree != null)
                {
                    tree = objTree.getString("Name");
                    treeId = objTree.getString("Id");
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

                Farmer farmer = new Farmer(id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, locationId, subLocation, subLocationId, village, villageId, tree, treeId, isFarmerHome, mobileNumber, "", "", status, false, "", "", null, Constants.FarmerType.MYSIGNUPS);

                if(status.equalsIgnoreCase(Constants.STR_ENROLLED))
                {
                    incompleteFarmersList.add(farmer);
                }
                else if(status.equalsIgnoreCase(Constants.STR_PENDING))
                {
                    pendingFarmersList.add(farmer);
                }
                else if(status.equalsIgnoreCase(Constants.STR_APPROVED))
                {
                    approvedFarmersList.add(farmer);
                }
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }

        Singleton.getInstance().mySignupsList.clear();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Farmer.class);
        realm.commitTransaction();

        if(incompleteFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if(pendingFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if(approvedFarmersList.size() > 0)
        {
            Singleton.getInstance().mySignupsList.add(new Farmer("", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(approvedFarmersList);
        }

        for (int i = 0; i < Singleton.getInstance().mySignupsList.size(); i++) {
            // Save to DB
            realm.beginTransaction();
            final Farmer farmerDbObj = realm.createObject(Farmer.class);
            farmerDbObj.setAllValues(Singleton.getInstance().mySignupsList.get(i));
            realm.commitTransaction();
        }

        // Creating our custom adapter
        adapter = new MySignupsAdapter(getActivity(), Singleton.getInstance().mySignupsList);
        // Create the list view and bind the adapter
        mLvMySignups.setAdapter(adapter);
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
                    sessionExpireRedirect();
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

    private void sessionExpireRedirect(){
        PreferenceHelper.getInstance().clearSession(false);
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    private void parseFarmerAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = Realm.getDefaultInstance();

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
                }

                for (int j = 0; j < Singleton.getInstance().mySignupsList.size(); j++) {
                    final Farmer farmer = Singleton.getInstance().mySignupsList.get(j);
                    if(farmer.getFarmerId().equalsIgnoreCase(id))
                    {
                        realm.beginTransaction();
                        farmer.setThumbAttachmentId(farmerPicId);
                        farmer.setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getThumbAttachmentId());
                        if(!farmerPicId.isEmpty())
                        {
                            farmer.setThumbUrl(thumbUrl);
                        }

                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
                        if(!farmerNatId.isEmpty())
                        {
                            farmer.setNationalCardUrl(natIdUrl);
                        }
                        realm.commitTransaction();

                        RealmResults<Farmer> farmerDbList = realm.where(Farmer.class).equalTo("farmerId", id).findAll();
                        if(farmerDbList != null && farmerDbList.size() > 0)
                        {
                            for (int k = 0; k < farmerDbList.size(); k++) {
                                realm.beginTransaction();
                                farmerDbList.get(k).setThumbAttachmentId(farmerPicId);
                                farmerDbList.get(k).setNationalCardAttachmentId(farmerNatId);
                                if(!farmerPicId.isEmpty())
                                {
                                    farmerDbList.get(k).setThumbUrl(thumbUrl);
                                }
                                if(!farmerNatId.isEmpty())
                                {
                                    farmerDbList.get(k).setNationalCardUrl(natIdUrl);
                                }
                                realm.commitTransaction();
                            }
                        }

                        MyPicassoInstance.getInstance().load(thumbUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "Farmer Pic: " + farmer.getThumbUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "Farmer Pic: " + farmer.getThumbUrl());
                            }
                        }*/);

                        MyPicassoInstance.getInstance().load(natIdUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "NAT ID: " + farmer.getNationalCardUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "NAT ID: " + farmer.getNationalCardUrl());
                            }
                        }*/);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        }
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
                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(attId);
                        PreferenceHelper.getInstance().writeAboutAttUrl(thumbUrl);
                        if(NetworkHelper.isNetAvailable(getActivity()))
                            MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
                        else
                            MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
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

    private void getDropDownsData() {
        WebApi.getLocations(getLocationsCallback);
        WebApi.getSubLocations(getSubLocationsCallback);
        WebApi.getVillages(getVillagesCallback);
        WebApi.getTrees(getTreesCallback);
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
                    sessionExpireRedirect();
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
                    sessionExpireRedirect();
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
                    sessionExpireRedirect();
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
                    sessionExpireRedirect();
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

    private void parseTrees(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if(arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject treeObj = arrRecords.getJSONObject(i).getJSONObject("Tree_Species__r");

                String id = "";
                String name = "";
                String subLocationId = "";

                id = treeObj.getString("Id");
                if (id.equalsIgnoreCase("null")) id = "";

                name = treeObj.getString("Name");
                if (name.equalsIgnoreCase("null")) name = "";

                subLocationId = arrRecords.getJSONObject(i).getString("Sub_Location__c");
                if (subLocationId.equalsIgnoreCase("null")) subLocationId = "";

                Tree tree = new Tree(id, name, subLocationId);
                DatabaseHelper.getInstance().insertTree(tree);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
//        ((BaseContainerFragment) getParentFragment()).replaceFragment(new AboutMe(), true);
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.rl_add:
            {
                mixPanel.track(MixPanelConstants.Event.ENROLL_FARMER_BUTTON);
                ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_ENROLL_FARMER, null), true);
            }
            break;
        }
    }

    private void refreshDataFromDB(){
        if(mLvMySignups != null && adapter != null) {
            getMySignupsFromDB();
            adapter = new MySignupsAdapter(getActivity(), Singleton.getInstance().mySignupsList);
            mLvMySignups.setAdapter(adapter);
        }
    }

    private BroadcastReceiver onMySignupsUpdated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshDataFromDB();
        }
    };

    public void getMyFarmerTasksData() {

        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        loadingStarted();
        WebApi.getMyfarmerTasks(myFarmerTasksCallback);
    }

    private void parseMyFarmersAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = getDefaultInstance();

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
                }

                ArrayList<Farmer> allFarmerTasks = Singleton.getInstance().myFarmersList;

                for (int j = 0; j < allFarmerTasks.size(); j++) {
                    final Farmer farmer = allFarmerTasks.get(j);

                    if(farmer.getFarmerId().equalsIgnoreCase(id))
                    {
                        farmer.setThumbAttachmentId(farmerPicId);
                        farmer.setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getThumbAttachmentId());
                        if(!farmerPicId.isEmpty())
                        {
                            farmer.setThumbUrl(thumbUrl);
                        }

                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
                        if(!farmerNatId.isEmpty())
                        {
                            farmer.setNationalCardUrl(natIdUrl);
                        }

                        Farmer farmerDb = realm.where(Farmer.class).equalTo("farmerId", id).findFirst();
                        if(farmerDb != null)
                        {
                            realm.beginTransaction();
                            farmerDb.setThumbAttachmentId(farmerPicId);
                            farmerDb.setNationalCardAttachmentId(farmerNatId);
                            if(!farmerPicId.isEmpty())
                            {
                                farmerDb.setThumbUrl(thumbUrl);
                            }
                            if(!farmerNatId.isEmpty())
                            {
                                farmerDb.setNationalCardUrl(natIdUrl);
                            }
                            realm.commitTransaction();
                        }

                        MyPicassoInstance.getInstance().load(thumbUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "Farmer Pic: " + farmer.getThumbUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "Farmer Pic: " + farmer.getThumbUrl());
                            }
                        }*/);

                        MyPicassoInstance.getInstance().load(natIdUrl).fetch(/*new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("Fetch success", "NAT ID: " + farmer.getNationalCardUrl());
                            }

                            @Override
                            public void onError() {
                                Log.i("Fetch failed", "NAT ID: " + farmer.getNationalCardUrl());
                            }
                        }*/);

                        break;
                    }
                }
            }

//            tasksAdapter.notifyDataSetChanged();
        }
    }

    private void parseMyFarmersData(String data) throws JSONException {

        Log.i("FENNEL", data);
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Map<String,Farmer> farmersMap = new HashMap<>();
        Map<String,Task> tasksMap = new HashMap<>();
        List<Farmer> farmersTaskList = new ArrayList<>();

        if(arrRecords.length() > 0)
        {

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject taskObj = arrRecords.getJSONObject(i);

                String taskName = taskObj.getString("Name");
                String id = taskObj.getString("Id");
                String status = taskObj.getString("Status__c");
                String startedDate = taskObj.getString("Started_Date__c");
                String dueDate = taskObj.getString("Due_Date__c");
                String completionDate = taskObj.getString("Completion_Date__c");

                JSONObject shambaObj = taskObj.getJSONObject("Shamba__r");
                String farmId = taskObj.getString("Shamba__c");
                JSONObject farmerObj = shambaObj.getJSONObject("Farmer__r");
                String farmerId = shambaObj.getString("Farmer__c");
                String farmerName = farmerObj.getString("FullName__c");
                String mobileNumber = farmerObj.getString("Mobile_Number__c");
                String farmerIdNumber = farmerObj.getString("Name");
                String subLocationName = shambaObj.getJSONObject("Sub_LocationLookup__r").getString("Name");
                String villageName = shambaObj.getJSONObject("Village__r").getString("Name");

                Task currentTask;
                if (tasksMap.containsKey(taskName)) {
                    currentTask = (Task) tasksMap.get(taskName);
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    currentTask = realm.createObject(Task.class);
                    currentTask.setTaskId(id);
                    currentTask.setName(taskName);
                    currentTask.setStartedDate(startedDate);
                    currentTask.setCompletionDate(completionDate);
                    currentTask.setDueDate(dueDate);
                    currentTask.setStatus(status);

                    realm.commitTransaction();
//                    currentTask = new Tasks(id, taskName, startedDate, completionDate, dueDate, status);
                    tasksMap.put(taskName, currentTask);
                }

                Farmer currentFarmer;
                RealmList<Task> farmingTasks;
                if (farmersMap.containsKey(farmerIdNumber)) {
                    currentFarmer = (Farmer) farmersMap.get(farmerIdNumber);
                    farmingTasks = currentFarmer.getFarmerTasks();
                    if (farmingTasks != null) {
                        farmingTasks.add(currentTask);
                    } else {
                        farmingTasks = new RealmList<>();
                        farmingTasks.add(currentTask);
                        currentFarmer.setFarmerTasks(farmingTasks);
                    }

                } else {
                    currentFarmer = new Farmer();
                    currentFarmer.setFarmerId(farmerId);
                    currentFarmer.setFarmId(farmId);
                    currentFarmer.setIdNumber(farmerIdNumber);
                    currentFarmer.setFullName(farmerName);
                    currentFarmer.setMobileNumber(mobileNumber);
                    currentFarmer.setSubLocation(subLocationName);
                    currentFarmer.setVillageName(villageName);
                    currentFarmer.setHeader(false);
                    currentFarmer.setFarmerType(Constants.FarmerType.MYFARMERTASKS);

                    farmingTasks = new RealmList<>();
                    farmingTasks.add(currentTask);
                    currentFarmer.setFarmerTasks(farmingTasks);

                    farmersMap.put(farmerIdNumber, currentFarmer);
                }

                if (!(farmersTaskList.contains(currentFarmer))) {
                    farmersTaskList.add(currentFarmer);
                }
            }
        }

        addFarmerTasksToDB(farmersTaskList);
        Singleton.getInstance().myFarmersList = (ArrayList<Farmer>) farmersTaskList;

    }

    private void addFarmerTasksToDB(List<Farmer> farmersTaskList) {

        Realm realm = getDefaultInstance();
        realm.beginTransaction();
        for (int i = 0; i < farmersTaskList.size(); i++) {
            // Save to DB
            final Farmer farmerDbObj = realm.createObject(Farmer.class);
            farmerDbObj.setAllValues(farmersTaskList.get(i));
        }
        realm.commitTransaction();
    }

    private Callback<ResponseBody> myFarmerTasksCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyFarmersData(responseStr);
                        getFarmerTaskItems();
                        WebApi.getMyFarmerAttachments(myFarmerTasksAttachments);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
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

    private Callback<ResponseBody> myFarmerTasksAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyFarmersAttachmentData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
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

    private void getFarmerTaskItems(){

        loadingStarted();

        String farmingTaskIds = "";

        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            Farmer farmer = Singleton.getInstance().myFarmersList.get(i);

            if(farmer.getFarmerTasks().size() > 0){
                for (int j = 0; j < farmer.getFarmerTasks().size(); j++) {
                    String id = farmer.getFarmerTasks().get(j).getTaskId();
                    id = "'" + id + "'";

                    farmingTaskIds = farmingTaskIds + id;

                    if(i+1 != Singleton.getInstance().myFarmersList.size() || j + 1 != farmer.getFarmerTasks().size()){
                        farmingTaskIds = farmingTaskIds + ",";
                    }
                }
            }
        }

        WebApi.getFarmingTaskItems(farmerStatusCallback, farmingTaskIds);
    }

    private Callback<ResponseBody> farmerStatusCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        ArrayList<TaskItem> allTaskItems = parseTaskItemData(responseStr);
                        Singleton.getInstance().taskItems = allTaskItems;
                        getTaskItemAttachments(allTaskItems);
                        Log.i("Parsing" , "Complete" );
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
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



    private ArrayList<TaskItem> parseTaskItemData(String data) throws JSONException {

        ArrayList<TaskItem> taskItems = null;

        // clear old lists
        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            if(Singleton.getInstance().myFarmersList.get(i).getFarmerTasks() != null){

                for (int j = 0; j < Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().size(); j++) {

                    if(Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems() != null){

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems().clear();
                        realm.commitTransaction();
                    }
                }
            }
        }

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            taskItems = new ArrayList<>();
        }

        for (int i = 0; i < arrRecords.length(); i++) {

            JSONObject objTask = arrRecords.getJSONObject(i);

            String id = objTask.getString("Id");
            String textValue = objTask.getString("Text_Value__c");
            int sequence = objTask.getInt("Sequence__c");
            String recordType = objTask.getJSONObject("RecordType").getString("Name");
            String name = objTask.getString("Name");
            double latitude = objTask.optDouble("Location__Latitude__s");
            if(Double.isNaN(latitude))
                latitude = 0;
            double longitude = objTask.optDouble("Location__Longitude__s");
            if(Double.isNaN(longitude))
                longitude = 0;
            String gpsTakenTime = objTask.getString("GPS_Taken_Time__c");
            String fileType = objTask.getString("File_Type__c");
            String farmingTaskId = objTask.getString("Farming_Task__c");
            String description = objTask.getString("Description__c");

            RealmList<TaskItemOption> options = new RealmList<>();

            JSONObject objOptions = objTask.optJSONObject("Task_Item_Options__r");
            if(objOptions != null){
                JSONArray arrOptions = objOptions.getJSONArray("records");
                for (int j = 0; j < arrOptions.length(); j++) {
                    JSONObject objOption = arrOptions.getJSONObject(j);
                    String optionId = objOption.getString("Id");
                    String optionName = objOption.getString("Name");
                    boolean isValue = objOption.getBoolean("Value__c");

                    options.add(new TaskItemOption(optionId, optionName, isValue));
                }
            }

            TaskItem taskItem = new TaskItem(sequence, id, farmingTaskId, name, recordType, description, textValue, fileType, gpsTakenTime, latitude, longitude, options);
            taskItems.add(taskItem);

            for (int j = 0; j < Singleton.getInstance().myFarmersList.size(); j++) {

                if(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks() != null){

                    for (int k = 0; k < Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().size(); k++) {

                        if (taskItem.getFarmingTaskId().equalsIgnoreCase(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskId())){
                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            if(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems() == null)
                                Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).setTaskItems(new RealmList<TaskItem>());
                            Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems().add(taskItem);
                            realm.commitTransaction();
                        }
                    }
                }
            }
        }

        return taskItems;
    }

    private void getTaskItemAttachments(ArrayList<TaskItem> taskItems) {

        WebApi.getAllTaskItemAttachments(taskItemsAttachments);
    }

    private Callback<ResponseBody> taskItemsAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            loadingFinished();
            if(isValid())
            {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseAllTasksAttachments(responseStr);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(response.code() == 401)
                {
                    PreferenceHelper.getInstance().clearSession(false);
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

    private void parseAllTasksAttachments(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject taskAttachmentRecord = arrRecords.getJSONObject(i);
                final String taskItemId = taskAttachmentRecord.getString("Id");
                final String fileType = taskAttachmentRecord.optString("File_Type__c");
                JSONObject attachments = taskAttachmentRecord.optJSONObject("Attachments");
                if (attachments != null && fileType != null) {

                    JSONArray attRecords = attachments.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject attachmentObj = attRecords.getJSONObject(j);
                        final String attachmentId = attachmentObj.getString("Id");
                        final String attachmentParentId = attachmentObj.getString("ParentId");
                        final String filename = attachmentObj.optString("Name");
                        String[] separated = filename.split("\\.");
                        final String ext = separated[separated.length-1];

                        Call<ResponseBody> apiCall = Fennel.getWebService().downloadAttachmentForTask(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId);
                        try {
                            if(getActivity() == null)
                                Fennel.initWebApi();
                            if(NetworkHelper.isNetAvailable(getActivity())){
                                apiCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.code() == Constants.RESPONSE_SUCCESS) {
                                            String attachmentName = attachmentId + "." + ext;
                                            try {
                                                File path = Environment.getExternalStorageDirectory();
                                                File folder = new File(path + "/" + "Fennel", "TaskAttachments");
                                                if (!folder.exists()) {
                                                    folder.mkdirs();
                                                }
                                                File file = new File(Environment.getExternalStorageDirectory() + "/Fennel/TaskAttachments", attachmentName);
//                                                File file = new File(path, attachmentName);
                                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                try {
                                                    // Writes bytes from the specified byte array to this file output stream
                                                    fileOutputStream.write(response.body().bytes());
                                                }
                                                catch (FileNotFoundException e) {
                                                    System.out.println("File not found" + e);
                                                }
                                                catch (IOException ioe) {
                                                    System.out.println("Exception while writing file " + ioe);
                                                }
                                                finally {
                                                    // close the streams using close method
                                                    try {
                                                        if (fileOutputStream != null) {
                                                            fileOutputStream.close();
                                                            updateTaskItemWithAttachment(taskItemId, attachmentName);
                                                        } else {
                                                            file.delete();
                                                        }
                                                    }
                                                    catch (IOException ioe) {
                                                        System.out.println("Error while closing stream: " + ioe);
                                                    }
                                                }
                                                Log.i("FENNEL", "Write Success!");
                                            } catch (IOException e) {
                                                Log.e("FENNEL", "Error while writing file!");
                                                Log.e("FENNEL", e.toString());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        System.out.println(t.toString());
                                    }
                                });
                            }
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

    }

    private void updateTaskItemWithAttachment(String taskItemId, String filename) {
        ArrayList<TaskItem> allTaskItems = Singleton.getInstance().taskItems;
        for (TaskItem taskItem : allTaskItems) {
            if (taskItem.getId().equals(taskItemId)) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                taskItem.setAttachmentPath(filename);
                realm.commitTransaction();

                break;
            }
        }
    }
}
