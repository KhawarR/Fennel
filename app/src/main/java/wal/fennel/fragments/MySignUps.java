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

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.NetworkPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import io.realm.Sort;
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
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.models.DashboardTask;
import wal.fennel.models.Farmer;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.Task;
import wal.fennel.models.TaskItem;
import wal.fennel.models.TaskItemOption;
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
import static wal.fennel.utils.Constants.FARMING_STATE_ONTIME;
import static wal.fennel.utils.Constants.STR_FACILITATOR;

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

    private Map<String, Map<String, String>> visitLogFarmingTasks = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CircleImageView cIvIconRight;
    private MySignupsAdapter adapter;
    private RelativeLayout rlAdd;
    private EditText etSearch;

    private int startingCallsCounter = 0;


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
        ViewGroup myHeader = (ViewGroup) myinflater.inflate(R.layout.header_mysignups_list, mLvMySignups, false);

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

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etSearch.getRight() - etSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        try {
                            String strSearchKey = etSearch.getText().toString().trim();
                            JSONObject props = new JSONObject();
                            props.put(MixPanelConstants.Property.SEARCH_KEY, strSearchKey);
                            mixPanel.track(MixPanelConstants.Event.SEARCH_MYSIGNUP_ACTION, props);
                        } catch (JSONException e) {
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
                    } catch (JSONException e) {
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
                if (!farmer.isHeader()) {
                    try {
                        JSONObject props = new JSONObject();
                        props.put(MixPanelConstants.Property.MYSIGNUP_STATUS, farmer.getSignupStatus());
                        props.put(MixPanelConstants.Property.MYSIGNUP_NAME, farmer.getFullName());
                        props.put(MixPanelConstants.Property.MYSIGNUP_ID, farmer.getFarmerId());
                        mixPanel.track(MixPanelConstants.Event.MYSIGNUP_MENU_ITEM_ACTION, props);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_EDIT_FARMER, farmer), true);
                }
            }
        });

        getMySignups();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAttachment();
        refreshDataFromDB();
        IntentFilter iff = new IntentFilter(Constants.MY_SIGNPS_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onMySignupsUpdated, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onMySignupsUpdated);
    }

    private void loadAttachment() {
        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if (!thumbUrl.isEmpty()) {
            if (NetworkHelper.isNetAvailable(getActivity()))
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
    private void getMySignups() {

        loadingStarted();

        if (NetworkHelper.isNetAvailable(getActivity()) && !WebApi.isSyncRequired() && PreferenceHelper.getInstance().readFirstRun()) {
            getMySignupsFromServer();
            getMyFarmerTasksData();
            getFOAndFacilitatorsData();
        } else {
            getMySignupsFromDB();
            getMyFarmerTasksFromDB();
            getMyLogbookDataFromDB();
            getMyDashboardDataFromDB();
            if (PreferenceHelper.getInstance().isSessionExpiredSyncReq()) {
                WebApi.syncAll(null);
            }
            loadingFinished();
        }
    }

    private void getMySignupsFromDB() {
        Singleton.getInstance().mySignupsList.clear();

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);

        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYSIGNUPS.toString()).findAll().sort("lastModifiedTime", Sort.ASCENDING);

        ArrayList<Farmer> incompleteFarmersList = new ArrayList<>();
        ArrayList<Farmer> pendingFarmersList = new ArrayList<>();
        ArrayList<Farmer> approvedFarmersList = new ArrayList<>();

        for (int i = 0; i < farmerDbList.size(); i++) {
            Farmer farmer = farmerDbList.get(i);

            if (farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_ENROLLED)) {
                incompleteFarmersList.add(farmer);
            } else if (farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_PENDING)) {
                pendingFarmersList.add(farmer);
            } else if (farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_APPROVED)) {
                approvedFarmersList.add(farmer);
            }
        }

        if (incompleteFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if (pendingFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if (approvedFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(approvedFarmersList);
        }

        adapter = new MySignupsAdapter(getActivity(), Singleton.getInstance().mySignupsList);
        mLvMySignups.setAdapter(adapter);
    }

    private void getMyFarmerTasksFromDB() {
        Singleton.getInstance().myFarmersList.clear();

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);

        RealmResults<Farmer> farmerDbList = Realm.getDefaultInstance().where(Farmer.class).equalTo("farmerType", Constants.FarmerType.MYFARMERTASKS.toString()).findAll();
        Singleton.getInstance().myFarmersList.addAll(farmerDbList);
    }

    private void getMySignupsFromServer() {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        startingCallsCounter++;
        WebApi.getMySignUps(mySignupsCallback);
        WebApi.getAboutMeAttachment(aboutMeAttachmentCallback);
    }

    private Callback<ResponseBody> mySignupsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            WebApi.saveSyncTimeStamp();
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        PreferenceHelper.getInstance().writeFirstRun(false);
                        parseData(responseStr);
                        startingCallsCounter++;
                        WebApi.getMyFarmerAttachments(myFarmersAttachments);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    sessionExpireRedirect();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private void checkCallsCounter() {
        startingCallsCounter--;
        if(startingCallsCounter == 0) {
            loadingFinished();
        }
    }

    private void parseData(String data) throws JSONException {

        ArrayList<Farmer> incompleteFarmersList = new ArrayList<>();
        ArrayList<Farmer> pendingFarmersList = new ArrayList<>();
        ArrayList<Farmer> approvedFarmersList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        if (arrRecords.length() > 0) {
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
                if (objLocation != null) {
                    location = objLocation.getString("Name");
                    locationId = objLocation.getString("Id");
                }

                JSONObject objSubLocation = farmObj.optJSONObject("Sub_LocationLookup__r");
                if (objSubLocation != null) {
                    subLocation = objSubLocation.getString("Name");
                    subLocationId = objSubLocation.getString("Id");
                }

                JSONObject objVillage = farmObj.optJSONObject("Village__r");
                if (objVillage != null) {
                    village = objVillage.getString("Name");
                    villageId = objVillage.getString("Id");
                }


                JSONObject objTree = farmObj.optJSONObject("Tree_Specie__r");
                if (objTree != null) {
                    tree = objTree.getString("Name");
                    treeId = objTree.getString("Id");
                }

                id = farmObj.optString("Farmer__c");
                if (id != null && id.equalsIgnoreCase("null")) id = "";

                JSONObject objFarmer = farmObj.optJSONObject("Farmer__r");
                if (objFarmer != null) {
                    fullName = objFarmer.getString("FullName__c");
                    if (fullName.equalsIgnoreCase("null")) fullName = "";
                    firstName = objFarmer.getString("First_Name__c");
                    if (firstName.equalsIgnoreCase("null")) firstName = "";
                    secondName = objFarmer.getString("Middle_Name__c");
                    if (secondName.equalsIgnoreCase("null")) secondName = "";
                    surname = objFarmer.getString("Last_Name__c");
                    if (surname.equalsIgnoreCase("null")) surname = "";
                    idNumber = objFarmer.getString("Name");
                    if (idNumber.equalsIgnoreCase("null")) idNumber = "";
                    gender = objFarmer.getString("Gender__c");
                    if (gender.equalsIgnoreCase("null")) gender = "";
                    mobileNumber = objFarmer.getString("Mobile_Number__c");
                    if (mobileNumber.equalsIgnoreCase("null")) mobileNumber = "";
                    leader = objFarmer.getBoolean("Leader__c");
                }

                isFarmerHome = farmObj.optBoolean("Is_Farmer_Home__c");

                String status = farmObj.getString("Sign_Up_Status__c");

                Farmer farmer = new Farmer(null, id, farmId, fullName, firstName, secondName, surname, idNumber, gender, leader, location, locationId, subLocation, subLocationId, village, villageId, tree, treeId, isFarmerHome, mobileNumber, "", "", status, false, "", "", null, Constants.FarmerType.MYSIGNUPS);

                if (status.equalsIgnoreCase(Constants.STR_ENROLLED)) {
                    incompleteFarmersList.add(farmer);
                } else if (status.equalsIgnoreCase(Constants.STR_PENDING)) {
                    pendingFarmersList.add(farmer);
                } else if (status.equalsIgnoreCase(Constants.STR_APPROVED)) {
                    approvedFarmersList.add(farmer);
                }
            }
        } else {
            Toast.makeText(getActivity(), "No record found", Toast.LENGTH_SHORT).show();
        }

        Singleton.getInstance().mySignupsList.clear();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Farmer.class);
        realm.commitTransaction();

        if (incompleteFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_ENROLLED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(incompleteFarmersList);
        }
        if (pendingFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_PENDING, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(pendingFarmersList);
        }
        if (approvedFarmersList.size() > 0) {
            Singleton.getInstance().mySignupsList.add(new Farmer(null, "", "", Constants.STR_APPROVED, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
            Singleton.getInstance().mySignupsList.addAll(approvedFarmersList);
        }

        for (int i = 0; i < Singleton.getInstance().mySignupsList.size(); i++) {
            realm.beginTransaction();
            final Farmer farmerDbObj = realm.createObject(Farmer.class);
            farmerDbObj.setAllValues(Singleton.getInstance().mySignupsList.get(i));
            realm.commitTransaction();
        }

        adapter = new MySignupsAdapter(getActivity(), Singleton.getInstance().mySignupsList);
        mLvMySignups.setAdapter(adapter);
    }

    private Callback<ResponseBody> myFarmersAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
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
                } else if (response.code() == 401) {
                    sessionExpireRedirect();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private void sessionExpireRedirect() {
        try {
            PreferenceHelper.getInstance().clearSession(false);
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void parseFarmerAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = Realm.getDefaultInstance();

        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmerObj = arrRecords.getJSONObject(i);
                String id = farmerObj.getString("Id");

                String farmerPicId = "";
                String farmerNatId = "";

                JSONObject attachmentObj = farmerObj.optJSONObject("Attachments");
                if (attachmentObj != null) {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        String description = objAttachment.getString("Description").toLowerCase().trim();
                        if (description.contains("pic") || description.contains("photo")) {
                            farmerPicId = objAttachment.getString("Id");
                        } else if (description.contains("id")) {
                            farmerNatId = objAttachment.getString("Id");
                        }
                    }
                }

                for (int j = 0; j < Singleton.getInstance().mySignupsList.size(); j++) {
                    final Farmer farmer = Singleton.getInstance().mySignupsList.get(j);
                    if (farmer.getFarmerId().equalsIgnoreCase(id)) {
                        realm.beginTransaction();
                        farmer.setThumbAttachmentId(farmerPicId);
                        farmer.setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getThumbAttachmentId());
                        if (!farmerPicId.isEmpty()) {
                            farmer.setThumbUrl(thumbUrl);
                        }

                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
                        if (!farmerNatId.isEmpty()) {
                            farmer.setNationalCardUrl(natIdUrl);
                        }
                        realm.commitTransaction();

                        RealmResults<Farmer> farmerDbList = realm.where(Farmer.class).equalTo("farmerId", id).findAll();
                        if (farmerDbList != null && farmerDbList.size() > 0) {
                            for (int k = 0; k < farmerDbList.size(); k++) {
                                realm.beginTransaction();
                                farmerDbList.get(k).setThumbAttachmentId(farmerPicId);
                                farmerDbList.get(k).setNationalCardAttachmentId(farmerNatId);
                                if (!farmerPicId.isEmpty()) {
                                    farmerDbList.get(k).setThumbUrl(thumbUrl);
                                }
                                if (!farmerNatId.isEmpty()) {
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
                    if (!attId.isEmpty()) {
                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(attId);
                        PreferenceHelper.getInstance().writeAboutAttUrl(thumbUrl);
                        if (NetworkHelper.isNetAvailable(getActivity()))
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

        if (arrRecords.length() > 0) {
            JSONObject facObj = arrRecords.getJSONObject(0);
            JSONObject attachmentObj = facObj.optJSONObject("Attachments");
            if (attachmentObj != null) {
                JSONArray attRecords = attachmentObj.getJSONArray("records");
                if (attRecords.length() > 0) {
                    JSONObject objFarmerPhoto = attRecords.getJSONObject(0);
                    String idAttachment = objFarmerPhoto.getString("Id");
                    return idAttachment;
                }
            }
        }
        return "";
    }
    //endregion

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_add: {
                mixPanel.track(MixPanelConstants.Event.ENROLL_FARMER_BUTTON);
                ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_ENROLL_FARMER, null), true);
            }
            break;
        }
    }

    private void refreshDataFromDB() {
        if (mLvMySignups != null && adapter != null) {
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
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        startingCallsCounter++;
        WebApi.getMyfarmerTasks(myFarmerTasksCallback);
    }

    public void getMyLogbookData(String fieldOfficers, String facilitators) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        startingCallsCounter++;
        WebApi.getMyLogbookData(myLogbookDataCallback, fieldOfficers, facilitators);
    }

    public void getMyDashboardData(String fieldOfficers, String facilitators) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        startingCallsCounter++;
        WebApi.getMyDashboardData(myDashboardDataCallback, fieldOfficers, facilitators);
    }

    private void parseMyFarmersAttachmentData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = Realm.getDefaultInstance();
        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject farmerObj = arrRecords.getJSONObject(i);
                String id = farmerObj.getString("Id");

                String farmerPicId = "";
                String farmerNatId = "";

                JSONObject attachmentObj = farmerObj.optJSONObject("Attachments");
                if (attachmentObj != null) {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        String description = objAttachment.getString("Description").toLowerCase().trim();
                        if (description.contains("pic") || description.contains("photo")) {
                            farmerPicId = objAttachment.getString("Id");
                        } else if (description.contains("id")) {
                            farmerNatId = objAttachment.getString("Id");
                        }
                    }
                }

                ArrayList<Farmer> allFarmerTasks = Singleton.getInstance().myFarmersList;

                for (int j = 0; j < allFarmerTasks.size(); j++) {
                    final Farmer farmer = allFarmerTasks.get(j);

                    if (farmer.getFarmerId().equalsIgnoreCase(id)) {
                        realm.beginTransaction();
                        farmer.setThumbAttachmentId(farmerPicId);
                        farmer.setNationalCardAttachmentId(farmerNatId);

                        String thumbUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getThumbAttachmentId());
                        if (!farmerPicId.isEmpty()) {
                            farmer.setThumbUrl(thumbUrl);
                        }

                        String natIdUrl = NetworkHelper.makeAttachmentUrlFromId(farmer.getNationalCardAttachmentId());
                        if (!farmerNatId.isEmpty()) {
                            farmer.setNationalCardUrl(natIdUrl);
                        }
                        realm.commitTransaction();

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
        }
    }

    private void parseMyFarmersData(String data) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Task.class);
        realm.commitTransaction();

        Log.i("FENNEL", data);
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Map<String, Farmer> farmersMap = new HashMap<>();
        Map<String, Task> tasksMap = new HashMap<>();
        List<Farmer> farmersTaskList = new ArrayList<>();

        if (arrRecords.length() > 0) {
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
                    realm.beginTransaction();

                    currentTask = realm.createObject(Task.class);
                    currentTask.setTaskId(id);
                    currentTask.setName(taskName);
                    currentTask.setStartedDate(startedDate);
                    currentTask.setCompletionDate(completionDate);
                    currentTask.setDueDate(dueDate);
                    currentTask.setStatus(status);

                    realm.commitTransaction();
                    tasksMap.put(taskName, currentTask);
                }

                Farmer currentFarmer;
                RealmList<Task> farmingTasks;
                if (farmersMap.containsKey(farmerIdNumber)) {
                    currentFarmer = (Farmer) farmersMap.get(farmerIdNumber);
                    farmingTasks = currentFarmer.getFarmerTasks();
                    realm.beginTransaction();
                    if (farmingTasks != null) {
                        farmingTasks.add(currentTask);
                    } else {
                        farmingTasks = new RealmList<>();
                        farmingTasks.add(currentTask);
                        currentFarmer.setFarmerTasks(farmingTasks);
                    }
                    realm.commitTransaction();

                } else {
                    realm.beginTransaction();

                    currentFarmer = realm.createObject(Farmer.class);
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

                    realm.commitTransaction();

                    farmersMap.put(farmerIdNumber, currentFarmer);
                }

                if (!(farmersTaskList.contains(currentFarmer))) {
                    farmersTaskList.add(currentFarmer);
                }
            }
        }
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
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyFarmersData(responseStr);
                        getFarmerTaskItems();
                        WebApi.getMyFarmerTaskAttachments(myFarmerTasksAttachments);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private Callback<ResponseBody> myFarmerTasksAttachments = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyFarmersAttachmentData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private void getFarmerTaskItems() {
        String farmingTaskIds = "";

        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            Farmer farmer = Singleton.getInstance().myFarmersList.get(i);

            if (farmer.getFarmerTasks().size() > 0) {
                for (int j = 0; j < farmer.getFarmerTasks().size(); j++) {
                    String id = farmer.getFarmerTasks().get(j).getTaskId();
                    id = "'" + id + "'";

                    farmingTaskIds = farmingTaskIds + id;

                    if (i + 1 != Singleton.getInstance().myFarmersList.size() || j + 1 != farmer.getFarmerTasks().size()) {
                        farmingTaskIds = farmingTaskIds + ",";
                    }
                }
            }
        }
        startingCallsCounter++;
        WebApi.getFarmingTaskItems(farmerStatusCallback, farmingTaskIds);
    }

    private Callback<ResponseBody> farmerStatusCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        ArrayList<TaskItem> allTaskItems = parseTaskItemData(responseStr);
                        Singleton.getInstance().taskItems = allTaskItems;
                        getTaskItemAttachments(allTaskItems);
                        Log.i("Parsing", "Complete");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private ArrayList<TaskItem> parseTaskItemData(String data) throws JSONException {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(TaskItem.class);
        realm.delete(TaskItemOption.class);
        realm.commitTransaction();


        ArrayList<TaskItem> taskItems = null;

        // clear old lists
        for (int i = 0; i < Singleton.getInstance().myFarmersList.size(); i++) {

            if (Singleton.getInstance().myFarmersList.get(i).getFarmerTasks() != null) {

                for (int j = 0; j < Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().size(); j++) {

                    if (Singleton.getInstance().myFarmersList.get(i).getFarmerTasks().get(j).getTaskItems() != null) {

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

            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject objTask = arrRecords.getJSONObject(i);

                String id = objTask.getString("Id");
                boolean isDone = objTask.getBoolean("Completed__c");
                String textValue = objTask.getString("Text_Value__c");
                int sequence = objTask.getInt("Sequence__c");
                String recordType = objTask.getJSONObject("RecordType").getString("Name");
                String name = objTask.getString("Name");
                double latitude = objTask.optDouble("Location__Latitude__s");
                if (Double.isNaN(latitude))
                    latitude = 0;
                double longitude = objTask.optDouble("Location__Longitude__s");
                if (Double.isNaN(longitude))
                    longitude = 0;
                String gpsTakenTime = objTask.getString("GPS_Taken_Time__c");
                String fileType = objTask.getString("File_Type__c");
                String fileActionType = objTask.getString("File_Action__c");
                String fileActionPerformed = objTask.getString("Action_Performed__c");
                String farmingTaskId = objTask.getString("Farming_Task__c");
                String description = objTask.getString("Description__c");

                RealmList<TaskItemOption> options = new RealmList<>();

                JSONObject objOptions = objTask.optJSONObject("Task_Item_Options__r");
                if (objOptions != null) {
                    JSONArray arrOptions = objOptions.getJSONArray("records");
                    for (int j = 0; j < arrOptions.length(); j++) {
                        JSONObject objOption = arrOptions.getJSONObject(j);
                        String optionId = objOption.getString("Id");
                        String optionName = objOption.getString("Name");
                        boolean isValue = objOption.getBoolean("Value__c");

                        realm.beginTransaction();
                        TaskItemOption taskItemOption = realm.createObject(TaskItemOption.class);
                        taskItemOption.setId(optionId);
                        taskItemOption.setName(optionName);
                        taskItemOption.setValue(isValue);
                        options.add(taskItemOption);
                        realm.commitTransaction();
                    }
                }
                realm.beginTransaction();
                TaskItem taskItem = realm.createObject(TaskItem.class);
                taskItem.setSequence(sequence);
                taskItem.setId(id);
                taskItem.setFarmingTaskId(farmingTaskId);
                taskItem.setName(name);
                taskItem.setRecordType(recordType);
                taskItem.setDescription(description);
                taskItem.setTextValue(textValue);
                taskItem.setFileType(fileType);
                taskItem.setFileActionType(fileActionType);
                taskItem.setGpsTakenTime(gpsTakenTime);
                taskItem.setLatitude(latitude);
                taskItem.setLongitude(longitude);
                taskItem.setOptions(options);
                taskItem.setAttachmentPath("");
                taskItem.setTaskDone(isDone);
                realm.commitTransaction();

                taskItems.add(taskItem);

                for (int j = 0; j < Singleton.getInstance().myFarmersList.size(); j++) {

                    if (Singleton.getInstance().myFarmersList.get(j).getFarmerTasks() != null) {

                        for (int k = 0; k < Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().size(); k++) {

                            if (taskItem.getFarmingTaskId().equalsIgnoreCase(Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskId())) {
                                realm.beginTransaction();
                                if (Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems() == null) {
                                    Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).setTaskItems(new RealmList<TaskItem>());
                                }
                                Singleton.getInstance().myFarmersList.get(j).getFarmerTasks().get(k).getTaskItems().add(taskItem);
                                realm.commitTransaction();
                            }
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
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseAllTasksAttachments(responseStr);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                        final String ext = separated[separated.length - 1];

                        Call<ResponseBody> apiCall = Fennel.getWebService().downloadAttachmentForTask(Session.getAuthToken(), NetworkHelper.API_VERSION, attachmentId);
                        try {
                            if (getActivity() == null)
                                Fennel.initWebApi();
                            if (NetworkHelper.isNetAvailable(getActivity())) {
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
                                                } catch (FileNotFoundException e) {
                                                    System.out.println("File not found" + e);
                                                } catch (IOException ioe) {
                                                    System.out.println("Exception while writing file " + ioe);
                                                } finally {
                                                    // close the streams using close method
                                                    try {
                                                        if (fileOutputStream != null) {
                                                            fileOutputStream.close();
                                                            updateTaskItemWithAttachment(taskItemId, file.getAbsolutePath(), attachmentId);
                                                        } else {
                                                            file.delete();
                                                        }
                                                    } catch (IOException ioe) {
                                                        System.out.println("Error while closing stream: " + ioe);
                                                    }
                                                }
//                                                Log.i("FENNEL", "Write Success!");
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
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void updateTaskItemWithAttachment(String taskItemId, String filePath, String attachmentId) {
        ArrayList<TaskItem> allTaskItems = Singleton.getInstance().taskItems;
        for (TaskItem taskItem : allTaskItems) {
            if (taskItem.getId().equals(taskItemId)) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                String uri = NetworkHelper.getUriFromPath(filePath);
                taskItem.setAttachmentId(attachmentId);
                taskItem.setAttachmentPath(uri);
                realm.commitTransaction();

                break;
            }
        }
    }

    private Callback<ResponseBody> myLogbookDataCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyLogbookData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private Callback<ResponseBody> myDashboardDataCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyDashboardData(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private Callback<ResponseBody> myLogbookFOAndFacDataCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        if (PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
                            parseMyLogbookFOFacData(responseStr);
                        } else if (PreferenceHelper.getInstance().readLoginUserType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
                            parseMyLogbookFacData(responseStr);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private void getFOAndFacilitatorsData() {
        String query = null;
        String userType = PreferenceHelper.getInstance().readLoginUserType();
        if (userType.equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
            query = NetworkHelper.GET_FO_FAC_FOR_FM;
        } else if (userType.equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
            query = NetworkHelper.GET_FAC_FOR_FO;
        } else {
            startingCallsCounter++;
            WebApi.getMyLogbookData(myLogbookDataCallback, "", "");
            startingCallsCounter++;
            WebApi.getMyDashboardData(myDashboardDataCallback, "", "");
            return;
        }
        startingCallsCounter++;
        WebApi.getFOAndFacDataForLogbook(myLogbookFOAndFacDataCallback, query);
    }

    private void parseMyLogbookData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String farmingTaskIds = "";
        if (arrRecords.length() > 0) {
            visitLogFarmingTasks = new HashMap<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                Map<String, String> taskMap = new HashMap<>();
                JSONObject farmingTaskObj = arrRecords.getJSONObject(i);

                String taskId = farmingTaskObj.getString("Farming_Task__c");
                JSONObject farmVisit = farmingTaskObj.getJSONObject("Farm_Visit__r");
                JSONObject fieldManager = farmVisit.optJSONObject("Field_Manager__r");
                JSONObject fieldOfficer = farmVisit.optJSONObject("Field_Officer__r");
                JSONObject facilitator = farmVisit.optJSONObject("Facilitator__r");
                String name = null;
                String agentType = null;
                String agentId = null;
                String agentPhone = null;
                String agentEmployeeId = null;
                JSONObject employeeObj = null;

                if (fieldManager != null) {
                    name = fieldManager.getString("Name");
                    agentType = Constants.STR_FIELD_MANAGER;
                    agentId = fieldManager.getString("Id");
                    agentPhone = fieldManager.getString("Phone__c");
                    employeeObj = fieldOfficer.optJSONObject("Employee__r");
                    agentEmployeeId = employeeObj.optString("Name");
                } else if (fieldOfficer != null) {
                    name = fieldOfficer.getString("Name");
                    agentType = Constants.STR_FIELD_OFFICER;
                    agentId = fieldOfficer.getString("Id");
                    agentPhone = fieldOfficer.getString("Phone__c");
                    employeeObj = fieldOfficer.optJSONObject("Employee__r");
                    agentEmployeeId = employeeObj.optString("Name");
                } else if (facilitator != null) {
                    name = facilitator.getString("Name");
                    agentType = STR_FACILITATOR;
                    agentId = facilitator.getString("Id");
                    agentPhone = facilitator.optString("Phone__c");
                    agentEmployeeId = facilitator.optString("Employee_ID__c");
                }
                taskMap.put("agentName", name);
                taskMap.put("agentType", agentType);
                taskMap.put("Id", taskId);
                taskMap.put("agentId", agentId);
                taskMap.put("agentPhone", agentPhone);
                taskMap.put("agentEmployeeId", agentEmployeeId);
                visitLogFarmingTasks.put(taskId, taskMap);
                farmingTaskIds = farmingTaskIds + "'" + taskId + "'";
                if (i < arrRecords.length() - 1) {
                    farmingTaskIds = farmingTaskIds + ",";
                }
            }
        }
        if (farmingTaskIds != null && !farmingTaskIds.isEmpty()) {
            getAllVisitLogsData(farmingTaskIds);
        }
    }

    private void parseMyDashboardData(String responseStr) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(DashboardFieldAgent.class);
        realm.delete(DashboardTask.class);
        realm.commitTransaction();

        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");
        HashMap<String, DashboardFieldAgent> dashboardAgents = null;
        ArrayList<DashboardFieldAgent> dashboardAgentsList = null;

        if (arrRecords.length() > 0) {
            dashboardAgents = new HashMap<>();
            dashboardAgentsList = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {

                String id = null;
                String name = null;
                String employeeId = null;
                String phone = null;
                JSONObject employeeObj = null;
                String taskName = null;
                String taskId = null;
                String dueDate = null;
                String completionDate = null;
                String agentType = null;
                boolean isCompleted = false;

                int state = FARMING_STATE_ONTIME;

                DashboardFieldAgent dashboardFieldAgent = null;
                DashboardTask task = null;

                JSONObject farmingTaskObj = arrRecords.getJSONObject(i);

                String statusStr = farmingTaskObj.optString("Status__c");
                if (statusStr.equalsIgnoreCase(Constants.STR_COMPLETED)) {
                    isCompleted = true;
                }
                JSONObject shambaObj = farmingTaskObj.optJSONObject("Shamba__r");

                JSONObject facilitatorObj = shambaObj.optJSONObject("Facilitator_Signup__r");
                JSONObject fieldOfficerObj = shambaObj.optJSONObject("Field_Officer_Signup__r");
                JSONObject fieldManagerObj = shambaObj.optJSONObject("Field_Manager_Signup__r");

                if (fieldManagerObj != null) {
                    id = fieldManagerObj.optString("Id");
                    name = fieldManagerObj.optString("Name");
                    phone = fieldManagerObj.optString("Phone__c");
                    employeeObj = fieldManagerObj.optJSONObject("Employee__r");
                    employeeId = employeeObj.optString("Name");
                    agentType = Constants.STR_FIELD_MANAGER;
                } else if (fieldOfficerObj != null) {
                    id = fieldOfficerObj.optString("Id");
                    name = fieldOfficerObj.optString("Name");
                    phone = fieldOfficerObj.optString("Phone__c");
                    employeeObj = fieldOfficerObj.optJSONObject("Employee__r");
                    agentType = Constants.STR_FIELD_OFFICER;
                    employeeId = employeeObj.optString("Name");
                } else if (facilitatorObj != null) {
                    id = facilitatorObj.optString("Id");
                    name = facilitatorObj.optString("Name");
                    phone = facilitatorObj.optString("Phone__c");
                    employeeId = facilitatorObj.optString("Employee_ID__c");
                    agentType = Constants.STR_FACILITATOR;
                }
                taskName = farmingTaskObj.optString("Name");
                taskId = farmingTaskObj.optString("Id");
                dueDate = farmingTaskObj.optString("Due_Date__c");
                completionDate = farmingTaskObj.optString("Completion_Date__c");

                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date due = null;
                Date completion = null;
                try {
                    due = serverFormat.parse(dueDate);
                    completion = serverFormat.parse(completionDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if ((completion != null && completion.getTime() > due.getTime()) || (completion == null && !isCompleted && due.getTime() < System.currentTimeMillis())) {
                    state = Constants.FARMING_STATE_LATE;
                }

                if (taskName.equalsIgnoreCase("Task1")) {
                    Log.i("FENNEL", "Catch!");
                }

                RealmList<DashboardTask> dashboardTasks = null;

                if (dashboardAgents.get(id) != null) {
                    dashboardFieldAgent = dashboardAgents.get(id);
                    dashboardTasks = dashboardFieldAgent.getDashboardTasks();
                    boolean isFound = false;
                    for (DashboardTask dashboardTask : dashboardTasks) {
                        if (dashboardTask.getTaskName().equalsIgnoreCase(taskName)) {
                            isFound = true;
                            realm.beginTransaction();
                            {
                                dashboardTask.setTotalCount(dashboardTask.getTotalCount() + 1);
                                if (state == Constants.FARMING_STATE_LATE)
                                    dashboardTask.setState(state);
                                if (isCompleted) {
                                    dashboardTask.setCompleted(dashboardTask.getCompleted() + 1);
                                    realm.commitTransaction();
                                    break;
                                }
                            }
                            realm.commitTransaction();
                        }
                    }
                    if (!isFound) {
                        realm.beginTransaction();
                        {
                            task = realm.createObject(DashboardTask.class);
                            task.setTaskName(taskName);
                            task.setTaskId(taskId);
                            task.setDueDate(dueDate);
                            task.setCompletionDate(completionDate);
                            task.setTotalCount(task.getTotalCount() + 1);
                            task.setState(state);

                            if (isCompleted) {
                                task.setCompleted(task.getCompleted() + 1);
                            }
                            dashboardTasks.add(task);
                        }
                        realm.commitTransaction();
                    }
                } else {
                    realm.beginTransaction();
                    {
                        dashboardFieldAgent = realm.createObject(DashboardFieldAgent.class);
                        dashboardFieldAgent.setAgentId(id);
                        dashboardFieldAgent.setAgentName(name);
                        dashboardFieldAgent.setAgentEmployeeId(employeeId);
                        dashboardFieldAgent.setAgentNumber(phone);
                        dashboardFieldAgent.setAgentType(agentType);

                        task = realm.createObject(DashboardTask.class);
                        task.setTaskName(taskName);
                        task.setTaskId(taskId);
                        task.setDueDate(dueDate);
                        task.setCompletionDate(completionDate);
                        task.setTotalCount(task.getTotalCount() + 1);
                        task.setState(state);
                        if (isCompleted) {
                            task.setCompleted(task.getCompleted() + 1);
                        }
                        dashboardTasks = dashboardFieldAgent.getDashboardTasks();
                        dashboardTasks.add(task);
                    }
                    realm.commitTransaction();

                    dashboardAgents.put(id, dashboardFieldAgent);
                    dashboardAgentsList.add(dashboardFieldAgent);
                }
            }
        }
        Singleton.getInstance().dashboardFieldAgents = dashboardAgentsList;
    }

    private void parseMyLogbookFOFacData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String fieldOfficers = "";
        String facilitators = "";
        if (arrRecords.length() > 0) {

            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject fieldOfficerObj = arrRecords.getJSONObject(i);
                String foId = fieldOfficerObj.getString("Id");
                fieldOfficers = fieldOfficers + "'" + foId + "'";
                if (i < arrRecords.length() - 1) {
                    fieldOfficers = fieldOfficers + ",";
                }

                JSONObject facilitatorsObj = fieldOfficerObj.getJSONObject("Facilitators__r");
                JSONArray facilitatorsArray = facilitatorsObj.getJSONArray("records");

                if (facilitatorsArray.length() > 0) {

                    for (int j = 0; j < facilitatorsArray.length(); j++) {
                        JSONObject facilitatorObj = facilitatorsArray.getJSONObject(j);
                        String facId = facilitatorObj.getString("Id");
                        facilitators = facilitators + "'" + facId + "'";
                        if (i < arrRecords.length() - 1 || j < facilitatorsArray.length() - 1) {
                            facilitators = facilitators + ",";
                        }
                    }
                }
            }
        }
        getMyLogbookData(fieldOfficers, facilitators);
        getMyDashboardData(fieldOfficers, facilitators);
    }

    private void parseMyLogbookFacData(String responseStr) throws JSONException {
        Log.i("FENNEL", responseStr);
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        String facilitators = "";
        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject facilitatorsObj = arrRecords.getJSONObject(i);
                String facId = facilitatorsObj.getString("Id");
                facilitators = facilitators + "'" + facId + "'";
                if (i < arrRecords.length() - 1) {
                    facilitators = facilitators + ",";
                }
            }
        }
        getMyLogbookData("", facilitators);
        getMyDashboardData("", facilitators);
    }

    private void getAllVisitLogsData(String farmingTaskIds) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        startingCallsCounter++;
        WebApi.getAllVisitLogsForMyLogbook(allVisitLogsCallback, farmingTaskIds);
    }

    private Callback<ResponseBody> allVisitLogsCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseAllVisitLogsDataForLogbook(responseStr);
                        WebApi.getMyLogBookAttachments(myLogbookAttachmentCallback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            checkCallsCounter();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            checkCallsCounter();
        }
    };

    private void parseAllVisitLogsDataForLogbook(String responseStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(responseStr);
        JSONArray arrRecords = jsonObject.getJSONArray("records");
        ArrayList<Task> allTasks = null;

        if (arrRecords.length() > 0) {
            allTasks = new ArrayList<>();
            for (int i = 0; i < arrRecords.length(); i++) {
                JSONObject farmingTaskObj = (JSONObject) arrRecords.get(i);
                String farmingTaskId = farmingTaskObj.getString("Id");
                Map<String, String> taskMap = visitLogFarmingTasks.get(farmingTaskId);

                Task visitLogTask = new Task();
                visitLogTask.setAgentId(taskMap.get("agentId"));
                visitLogTask.setAgentName(taskMap.get("agentName"));
                visitLogTask.setAgentType(taskMap.get("agentType"));
                visitLogTask.setAgentPhoneNumber(taskMap.get("agentPhone"));
                visitLogTask.setAgentEmployeeId(taskMap.get("agentEmployeeId"));
                visitLogTask.setTaskId(taskMap.get("Id"));
                visitLogTask.setStatus(farmingTaskObj.optString("Status__c"));
                visitLogTask.setStartedDate(farmingTaskObj.optString("Started_Date__c"));
                visitLogTask.setName(farmingTaskObj.optString("Name"));
                visitLogTask.setDueDate(farmingTaskObj.optString("Due_Date__c"));
                visitLogTask.setCompletionDate(farmingTaskObj.optString("Completion_Date__c"));


                JSONObject shambaObj = (JSONObject) farmingTaskObj.getJSONObject("Shamba__r");
                String shambaName = shambaObj.optString("Village_Name__c");
                if (shambaName != null) {
                    visitLogTask.setShambaName(shambaName);
                }
                String farmerName = shambaObj.getJSONObject("Farmer__r").getString("FullName__c");
                visitLogTask.setFarmerName(farmerName);

                JSONObject taskItemObj = farmingTaskObj.optJSONObject("Task_Items__r");
                if (taskItemObj != null) {
                    JSONArray taskItemRecords = taskItemObj.getJSONArray("records");
                    if (taskItemRecords.length() > 0) {
                        for (int j = 0; j < taskItemRecords.length(); j++) {

                            JSONObject taskItem = (JSONObject) taskItemRecords.get(j);
                            String id = taskItem.optString("Id");
                            String textValue = taskItem.optString("Text_Value__c");
                            int sequence = taskItem.optInt("Sequence__c");
                            String recordType = taskItem.optJSONObject("RecordType").getString("Name");
                            String name = taskItem.optString("Name");
                            double latitude = taskItem.optDouble("Location__Latitude__s");
                            if (Double.isNaN(latitude))
                                latitude = 0;
                            double longitude = taskItem.optDouble("Location__Longitude__s");
                            if (Double.isNaN(longitude))
                                longitude = 0;
                            String lastModifiedDate = taskItem.optString("LastModifiedDate");
                            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                            Date lastModified = null;
                            try {
                                lastModified = serverFormat.parse(lastModifiedDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String gpsTakenTime = taskItem.getString("GPS_Taken_Time__c");
                            String fileType = taskItem.getString("File_Type__c");
                            String fileActionType = taskItem.getString("File_Action__c");
                            String fileActionPerformed = taskItem.optString("Action_Performed__c");
                            String description = taskItem.getString("Description__c");

                            RealmList<TaskItemOption> options = new RealmList<>();

                            TaskItem newTaskItem = new TaskItem(sequence, id, taskMap.get("Id"), name, recordType, description, textValue, fileType, fileActionType, fileActionPerformed, gpsTakenTime, latitude, longitude, options, lastModified, visitLogTask.getAgentName(), farmerName, null, false, "", "", false, false);
                            visitLogTask.getTaskItems().add(newTaskItem);
                        }
                    }
                    allTasks.add(visitLogTask);
                }
            }
            parseVisitLogsFromTasks(allTasks);
        }
    }

    private void parseVisitLogsFromTasks(ArrayList<Task> allTasks) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(FieldAgent.class);
        realm.commitTransaction();

        realm.beginTransaction();
        Map<String, FieldAgent> agentsMap = new HashMap<>();
        ArrayList<FieldAgent> allAgentsList = new ArrayList<>();

        for (Task taskObj : allTasks) {
            FieldAgent agent = null;
            String agentId = taskObj.getAgentId();
            if (agentsMap.get(agentId) != null) {
                agent = agentsMap.get(agentId);
            } else {
                agent = realm.createObject(FieldAgent.class);
                agent.setAgentId(taskObj.getAgentId());
                agent.setName(taskObj.getAgentName());
                agent.setAgentType(taskObj.getAgentType());
                agent.setAgentEmployeeId(taskObj.getAgentEmployeeId());
                agentsMap.put(agentId, agent);
                allAgentsList.add(agent);
            }
            RealmList<TaskItem> allItems = taskObj.getTaskItems();
            for (TaskItem item : allItems) {
                item.setAgentName(taskObj.getAgentName());

                TaskItem taskItem = realm.createObject(TaskItem.class);
                taskItem.setSequence(item.getSequence());
                taskItem.setId(item.getId());
                taskItem.setFarmingTaskId(item.getFarmingTaskId());
                taskItem.setName(item.getName());
                taskItem.setRecordType(item.getRecordType());
                taskItem.setDescription(item.getDescription());
                taskItem.setTextValue(item.getTextValue());
                taskItem.setFileType(item.getFileType());
                taskItem.setFileActionType(item.getFileActionType());
                taskItem.setFileActionPerformed(item.getFileActionPerformed());
                taskItem.setGpsTakenTime(item.getGpsTakenTime());
                taskItem.setLatitude(item.getLatitude());
                taskItem.setLongitude(item.getLongitude());
                taskItem.setOptions(item.getOptions());
                taskItem.setDateModified(item.getDateModified());
                taskItem.setAgentName(item.getAgentName());
                taskItem.setFarmerName(item.getFarmerName());
                taskItem.setAgentAttachmentId(item.getAgentAttachmentId());
                taskItem.setTaskDone(item.isTaskDone());
                taskItem.setAttachmentPath(item.getAttachmentPath());
                agent.getVisitLogs().add(taskItem);
            }
        }
        realm.commitTransaction();
        Singleton.getInstance().fieldAgentsVisitLogs = allAgentsList;
    }

    private Callback<ResponseBody> myLogbookAttachmentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (isValid()) {
                if (response.code() == 200) {
                    String responseStr = "";

                    try {
                        responseStr = response.body().string();
                        parseMyLogbookAttachments(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    PreferenceHelper.getInstance().clearSession(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private void parseMyLogbookAttachments(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Realm realm = getDefaultInstance();

        if (arrRecords.length() > 0) {
            for (int i = 0; i < arrRecords.length(); i++) {

                JSONObject agentObj = arrRecords.getJSONObject(i);
                String id = agentObj.getString("Id");
                String name = agentObj.getString("Name");
                String agentAttachmentId = null;

                JSONObject attachmentObj = agentObj.optJSONObject("Attachments");
                if (attachmentObj != null) {

                    JSONArray attRecords = attachmentObj.getJSONArray("records");
                    for (int j = 0; j < attRecords.length(); j++) {
                        JSONObject objAttachment = attRecords.getJSONObject(j);
                        agentAttachmentId = objAttachment.getString("Id");
                    }
                }

                ArrayList<FieldAgent> allAgentsList = Singleton.getInstance().fieldAgentsVisitLogs;

                for (int j = 0; j < allAgentsList.size(); j++) {
                    final FieldAgent fieldAgent = allAgentsList.get(j);

                    if (fieldAgent.getAgentEmployeeId().equalsIgnoreCase(name)) {
                        String attachmentUrl = NetworkHelper.makeAttachmentUrlFromId(agentAttachmentId);

                        realm.beginTransaction();
                        fieldAgent.setAgentAttachmentUrl(agentAttachmentId);
                        for (TaskItem item : fieldAgent.getVisitLogs()) {
                            item.setAgentAttachmentId(agentAttachmentId);
                        }
                        realm.commitTransaction();
                        MyPicassoInstance.getInstance().load(attachmentUrl).fetch();
                        break;
                    }
                }
            }
        }
    }

    private void getMyLogbookDataFromDB() {

        Singleton.getInstance().fieldAgentsVisitLogs.clear();

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);

        RealmResults<FieldAgent> visitLogsList = Realm.getDefaultInstance().where(FieldAgent.class).findAll();
        Singleton.getInstance().fieldAgentsVisitLogs.addAll(visitLogsList);
    }

    private void getMyDashboardDataFromDB() {
        Singleton.getInstance().dashboardFieldAgents.clear();

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);

        RealmResults<DashboardFieldAgent> dashboardFieldAgents = Realm.getDefaultInstance().where(DashboardFieldAgent.class).findAll();
        Singleton.getInstance().dashboardFieldAgents.addAll(dashboardFieldAgents);
    }
}
