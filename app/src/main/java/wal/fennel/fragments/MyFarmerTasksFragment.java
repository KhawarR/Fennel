package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wal.fennel.R;
import wal.fennel.activities.LoginActivity;
import wal.fennel.adapters.FarmerTasksAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Tasks;
import wal.fennel.network.NetworkHelper;
import wal.fennel.network.WebApi;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;
import wal.fennel.views.TitleBarLayout;

import static io.realm.Realm.getDefaultInstance;

/**
 * Created by irfanayaz on 11/15/16.
 */
public class MyFarmerTasksFragment extends BaseFragment implements AdapterView.OnItemClickListener, TextWatcher {

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.et_search)
    EditText searchText;

    @Bind(R.id.lv_farmer_tasks)
    ListView farmerTasks;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private CircleImageView cIvIconRight;

    private ArrayList<Farmer> allFarmerTasks;

    private FarmerTasksAdapter tasksAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_farmer_tasks, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        farmerTasks.setOnItemClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);
        searchText.addTextChangedListener(this);
        getMyFarmerTasksData();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadAttachment();
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

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void getMyFarmerTasksData() {

        if(mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        loadingStarted();
        WebApi.getMyfarmerTasks(myFarmerTasksCallback);

    }

    private void parseFarmerAttachmentData(String data) throws JSONException {
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

                for (int j = 0; j < allFarmerTasks.size(); j++) {
                    final Farmer farmer = allFarmerTasks.get(j);
                    if (farmer.isHeader)
                        continue;
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

            tasksAdapter.notifyDataSetChanged();
        }
    }

    private void parseData(String data) throws JSONException {

        Log.i("FENNEL", data);
        JSONObject jsonObject = new JSONObject(data);
        JSONArray arrRecords = jsonObject.getJSONArray("records");

        Map<String,Farmer> farmersMap = new HashMap<>();
        Map<String,Tasks> tasksMap = new HashMap<>();
        List<Farmer> farmersTaskList = new ArrayList<>();
//            allFarmerTasks = new ArrayList<>();

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
                String farmerIdNumber = farmerObj.getString("Name");
                String subLocationName = shambaObj.getJSONObject("Sub_LocationLookup__r").getString("Name");
                String villageName = shambaObj.getJSONObject("Village__r").getString("Name");

                Tasks currentTask;
                if (tasksMap.containsKey(id)) {
                    currentTask = (Tasks) tasksMap.get(id);
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    currentTask = realm.createObject(Tasks.class);
                    currentTask.setTaskId(id);
                    currentTask.setName(taskName);
                    currentTask.setStartedDate(startedDate);
                    currentTask.setCompletionDate(completionDate);
                    currentTask.setDueDate(dueDate);
                    currentTask.setStatus(status);

                    realm.commitTransaction();
//                    currentTask = new Tasks(id, taskName, startedDate, completionDate, dueDate, status);
                    tasksMap.put(id, currentTask);
                }

                Farmer currentFarmer;
                RealmList<Tasks> farmingTasks;
                if (farmersMap.containsKey(farmerIdNumber)) {
                   currentFarmer = (Farmer) farmersMap.get(farmerIdNumber);
                    farmingTasks = currentFarmer.getFarmerTasks();
                    if (farmingTasks != null) {
                        farmingTasks.add(currentTask);
                    } else {
                        farmingTasks = new RealmList<>();
                        farmingTasks.add(currentTask);
                        currentFarmer.farmerTasks = farmingTasks;
                    }

                } else {
                    currentFarmer = new Farmer();
                    currentFarmer.farmerId = farmerId;
                    currentFarmer.farmId = farmId;
                    currentFarmer.idNumber = farmerIdNumber;
                    currentFarmer.fullName = farmerName;
                    currentFarmer.subLocation = subLocationName;
                    currentFarmer.villageName = villageName;
                    currentFarmer.isHeader = false;
                    currentFarmer.setFarmerType(Constants.FarmerType.MYFARMERTASKS);

                    farmingTasks = new RealmList<>();
                    farmingTasks.add(currentTask);
                    currentFarmer.farmerTasks = farmingTasks;

                    farmersMap.put(farmerIdNumber, currentFarmer);
                }

                if (!(farmersTaskList.contains(currentFarmer))) {
                    farmersTaskList.add(currentFarmer);
                }

//                allFarmerTasks.add(new Farmer("", "", taskName, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYFARMERTASKS));
//                allFarmerTasks.add(currentFarmer);
            }
        }
        //TODO: uncomment this
//        addFarmerTasksToDB(farmersTaskList);
        Singleton.getInstance().myFarmersTaskList = (ArrayList<Farmer>) farmersTaskList;
        parseDataForMyFarmers(farmersTaskList);

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

    private void parseDataForMyFarmers(List<Farmer> farmerList) {

        Map<String,Farmer> farmersMap = new HashMap<>();
        Map<String,Tasks> tasksMap = new HashMap<>();
        List<List<Farmer>> farmersTaskList = new ArrayList<List<Farmer>>();

        for (Farmer currentFarmer : farmerList) {
            for (Tasks currentTask : currentFarmer.farmerTasks) {
                boolean taskFound = false;
                for (List<Farmer> taskList : farmersTaskList) {
                    Farmer taskObject = (Farmer) taskList.get(0);
                    if (taskObject.farmerId.equals(currentTask.taskId)) {
                        taskFound = true;
                        taskList.add(currentFarmer);
                        break;
                    }
                }
                if (!taskFound) {
                    ArrayList<Farmer> newTaskList = new ArrayList<>();
                    newTaskList.add(new Farmer(currentTask.taskId, "", currentTask.name, "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYFARMERTASKS));
                    newTaskList.add(currentFarmer);
                    farmersTaskList.add(newTaskList);
                }
            }
        }

        allFarmerTasks = new ArrayList<Farmer>();
        for (List<Farmer> listOfFarmer : farmersTaskList) {
            allFarmerTasks.addAll(listOfFarmer);
        }

        tasksAdapter = new FarmerTasksAdapter(getActivity(), allFarmerTasks);
        farmerTasks.setAdapter(tasksAdapter);

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
//                        PreferenceHelper.getInstance().writeFirstRun(false);
                        parseData(responseStr);
                        WebApi.getMyFarmerAttachments(myFarmersAttachments);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
        tasksAdapter.filter(text);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
