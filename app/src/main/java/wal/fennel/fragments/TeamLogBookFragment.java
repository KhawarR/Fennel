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
import wal.fennel.adapters.TeamLogBookAdapter;
import wal.fennel.models.FieldAgent;
import wal.fennel.models.LogTaskItem;
import wal.fennel.models.TaskItem;
import wal.fennel.utils.Singleton;

/**
 * Created by irfanayaz on 12/2/16.
 */
public class TeamLogBookFragment extends BaseFragment {

    @Bind(R.id.logbook_listview)
    ListView logbookListView;
    TeamLogBookAdapter logBookAdapter;

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
        ArrayList<LogTaskItem> logsList = getDataForTeamLogbook();

        logBookAdapter = new TeamLogBookAdapter(getActivity(), logsList);
        logbookListView.setAdapter(logBookAdapter);
    }

    private ArrayList getDataForTeamLogbook() {
        ArrayList<LogTaskItem> allLogs = new ArrayList<>();
        ArrayList<FieldAgent> agentsData = Singleton.getInstance().fieldAgentsVisitLogs;
        for (FieldAgent agent : agentsData) {
            allLogs.addAll(agent.getVisitLogs());
        }
        return allLogs;
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

}
