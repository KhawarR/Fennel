package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import wal.fennel.R;
import wal.fennel.adapters.TeamDashboardAdapter;

/**
 * Created by irfanayaz on 12/22/16.
 */
public class TeamDashboardFragment extends BaseFragment {

    @Bind(R.id.logbook_listview)
    ListView dashboardListView;
    TeamDashboardAdapter logBookAdapter;

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
//        ArrayList<TaskItem> logsList = getDataForTeamLogbook();

        logBookAdapter = new TeamDashboardAdapter(getActivity(), null);
        dashboardListView.setAdapter(logBookAdapter);
    }

    private ArrayList getDataForTeamDashboard() {
//        ArrayList<TaskItem> allLogs = new ArrayList<>();
//        ArrayList<FieldAgent> agentsData = Singleton.getInstance().fieldAgentsVisitLogs;
//        for (FieldAgent agent : agentsData) {
//            allLogs.addAll(agent.getVisitLogs());
//        }
//        return allLogs;
        return null;
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }
}
