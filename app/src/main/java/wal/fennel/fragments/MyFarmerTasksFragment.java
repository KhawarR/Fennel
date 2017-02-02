package wal.fennel.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.adapters.FarmerTasksAdapter;
import wal.fennel.models.Farmer;
import wal.fennel.models.Task;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.utils.Singleton;
import wal.fennel.views.TitleBarLayout;

/**
 * Created by irfanayaz on 11/15/16.
 */
public class MyFarmerTasksFragment extends BaseFragment implements AdapterView.OnItemClickListener, TextWatcher, TextView.OnEditorActionListener {

    private CircleImageView cIvIconRight;
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    @Bind(R.id.lv_farmer_tasks)
    ListView farmerTasks;
    @Bind(R.id.et_search)
    EditText searchText;

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
        titleBarLayout.setOnIconClickListener(this);
        farmerTasks.setOnItemClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);
        searchText.addTextChangedListener(this);
        searchText.setOnEditorActionListener(this);
        getMyFarmerTasks();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadAttachment();

        IntentFilter iff= new IntentFilter(Constants.MY_SIGNPS_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onMyFarmerTasksUpdated, iff);
    }

    private BroadcastReceiver onMyFarmerTasksUpdated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            getMyFarmerTasks();
        }
    };

    private void loadAttachment() {
        String thumbUrl = PreferenceHelper.getInstance().readAboutAttUrl();
        if(!thumbUrl.isEmpty()) {
            if(NetworkHelper.isNetAvailable(getActivity())) {
                MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            } else {
                MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
            }
        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideKeyboard();
        if (allFarmerTasks.get(position).isHeader())
            return;
        if(allFarmerTasks.get(position).getFarmerTasks().size() > 0) {
            ((BaseContainerFragment) getParentFragment()).replaceFragment(FarmerStatus.newInstance(Constants.STR_EDIT_FARMER, allFarmerTasks.get(position)), true);
        }
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        startActivity(new Intent(getActivity(), AboutMe.class));
    }

    private void getMyFarmerTasks() {
        ArrayList<Farmer> allFarmerTasks = Singleton.getInstance().myFarmersList;
        parseDataForMyFarmers(allFarmerTasks);
    }

    private void parseDataForMyFarmers(List<Farmer> farmerList) {

        Map<String, Farmer> farmersMap = new HashMap<>();

        List<List<Farmer>> farmersTaskList = new ArrayList<List<Farmer>>();

        for (Farmer currentFarmer : farmerList) {
            for (Task currentTask : currentFarmer.getFarmerTasks()) {
//                if (currentTask.getStatus().equalsIgnoreCase(Constants.STR_NOT_STARTED) || currentTask.getStatus().equalsIgnoreCase(Constants.STR_IN_PROGRESS)) {
                    boolean taskFound = false;
                    ArrayList<Farmer> newTaskList = null;

                    for (List<Farmer> taskList : farmersTaskList) {
                        Farmer taskObject = (Farmer) taskList.get(0);
                        if (taskObject.getFullName().equals(currentTask.getName())) {
                            taskFound = true;
                            newTaskList = (ArrayList<Farmer>) taskList;
                            break;
                        }
                    }
                    if (!taskFound && newTaskList == null) {
                        newTaskList = new ArrayList<>();
                        Date dueDate = FennelUtils.getDateFromString(currentTask.getDueDate());
                        String tempFarmId = Constants.STR_FARMER_ID_PREFIX + String.valueOf(System.currentTimeMillis());
                        newTaskList.add(new Farmer(dueDate, currentTask.getTaskId(), tempFarmId, currentTask.getName(), "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYFARMERTASKS));
                        farmersTaskList.add(newTaskList);
                    }
                    if (farmersMap.get(currentFarmer.getFarmerId()) == null && !currentTask.getStatus().equalsIgnoreCase(Constants.STR_COMPLETED)) {
                        newTaskList.add(currentFarmer);
                        farmersMap.put(currentFarmer.getFarmerId(), currentFarmer);
                    }

//                }
            }
        }

        // add all the lists to a single list and sort based on due date
        allFarmerTasks = new ArrayList<Farmer>();
        for (List<Farmer> listOfFarmer : farmersTaskList) {
            boolean isAdded = false;
            Date currentDate = listOfFarmer.get(0).getLastModifiedTime();
            if (currentDate != null) {
                long currentDueDate = listOfFarmer.get(0).getLastModifiedTime().getTime();
                for (int i = 0; i < allFarmerTasks.size(); i++) {
                    Farmer taskFarmer = (Farmer) allFarmerTasks.get(i);
                    if (!taskFarmer.isHeader()) {
                        continue;
                    }
                    Date dueDate = taskFarmer.getLastModifiedTime();
                        if ( dueDate == null || currentDueDate < dueDate.getTime()) {
                            isAdded = true;
                            allFarmerTasks.addAll(i, listOfFarmer);
                            break;
                        }
                }
            }
            if (!isAdded) {
                allFarmerTasks.addAll(listOfFarmer);
            }
        }

        if (farmerTasks != null) {
            tasksAdapter = new FarmerTasksAdapter(getActivity(), allFarmerTasks);
            farmerTasks.setAdapter(tasksAdapter);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
        allFarmerTasks = tasksAdapter.filter(text);
    }

    @Override
    public void afterTextChanged(Editable s) { }

    @OnClick(R.id.parent_view)
    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideKeyboard();
            return true;
        }
        return false;
    }
}
