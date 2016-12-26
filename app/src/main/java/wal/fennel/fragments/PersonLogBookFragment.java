package wal.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import wal.fennel.R;
import wal.fennel.adapters.PersonLogBookAdapter;
import wal.fennel.models.FieldAgent;
import wal.fennel.utils.Constants;
import wal.fennel.utils.Singleton;

/**
 * Created by irfanayaz on 12/2/16.
 */
public class PersonLogBookFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @Bind(R.id.person_listview)
    ListView logbookListView;
    PersonLogBookAdapter logBookAdapter;

    ArrayList<FieldAgent> personsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.person_logbook_fragment, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        logbookListView.setOnItemClickListener(this);

        personsList = getPersonLogbookData();

        logBookAdapter = new PersonLogBookAdapter(getActivity(), personsList);
        logbookListView.setAdapter(logBookAdapter);
    }

    private ArrayList<FieldAgent> getPersonLogbookData() {
        ArrayList<FieldAgent> fieldAgents = Singleton.getInstance().fieldAgentsVisitLogs;
        ArrayList<FieldAgent> fieldOfficers = new ArrayList<>();
        ArrayList<FieldAgent> facilitators = new ArrayList<>();
        for (FieldAgent fieldAgent : fieldAgents) {
            if (fieldAgent.getAgentType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
                fieldOfficers.add(fieldAgent);
            } else if (fieldAgent.getAgentType().equalsIgnoreCase(Constants.STR_FACILITATOR)) {
                facilitators.add(fieldAgent);
            }
        }
        ArrayList<FieldAgent> allAgents = new ArrayList<>();
        if (fieldOfficers.size() > 0) {
            allAgents.add(new FieldAgent("FIELD OFFICERS", "", "", "", "", "", null, true));
            allAgents.addAll(fieldOfficers);
        }
        if (facilitators.size() > 0) {
            allAgents.add(new FieldAgent("FACILITATORS", "", "", "", "", "", null, true));
            allAgents.addAll(facilitators);
        }
        return allAgents;
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FieldAgent clickedAgent = personsList.get(position - logbookListView.getHeaderViewsCount());
        ((MyDashboard)getParentFragment()).addPersonDetailViewFragment(clickedAgent);
    }
}
