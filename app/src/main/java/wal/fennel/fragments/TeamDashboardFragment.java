package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import wal.fennel.R;
import wal.fennel.adapters.TeamDashboardAdapter;
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.models.DashboardTask;
import wal.fennel.utils.Singleton;

/**
 * Created by irfanayaz on 12/22/16.
 */
public class TeamDashboardFragment extends BaseFragment {

    @Bind(R.id.logbook_listview)
    ListView dashboardListView;
    TeamDashboardAdapter dashboardAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.team_logbook_fragment, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        ArrayList<DashboardTask> dashboardTasks = getDataForTeamDashboard();
        Collections.sort(dashboardTasks);

        dashboardAdapter = new TeamDashboardAdapter(getActivity(), dashboardTasks);
        dashboardListView.setAdapter(dashboardAdapter);
    }

    private ArrayList getDataForTeamDashboard() {
        ArrayList<DashboardTask> allDashboardTasks = new ArrayList<>();
        ArrayList<DashboardFieldAgent> agentsDashboardData = Singleton.getInstance().dashboardFieldAgents;
        Map<String, DashboardTask> dashboardMap = new HashMap<>();
        for (DashboardFieldAgent agent : agentsDashboardData) {
            for (DashboardTask task : agent.getDashboardTasks()) {
                DashboardTask currentTask;
                if (dashboardMap.get(task.getTaskName()) != null) {
                    currentTask = dashboardMap.get(task.getTaskName());
                    currentTask.setTotalCount(currentTask.getTotalCount() + task.getTotalCount());
                    currentTask.setCompleted(currentTask.getCompleted() + task.getCompleted());
                } else {
                    currentTask = new DashboardTask(task.getTaskId(), task.getTaskName(), task.getDueDate(), task.getCompletionDate(), task.getTotalCount(), task.getCompleted(), task.getState());
                    allDashboardTasks.add(currentTask);
                    dashboardMap.put(task.getTaskName(), currentTask);
                }
            }
        }
        return allDashboardTasks;
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }
}
