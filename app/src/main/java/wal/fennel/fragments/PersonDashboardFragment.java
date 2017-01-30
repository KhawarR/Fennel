package wal.fennel.fragments;

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

import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import wal.fennel.R;
import wal.fennel.adapters.PersonDashboardAdapter;
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.utils.Constants;
import wal.fennel.utils.Singleton;

/**
 * Created by irfanayaz on 12/25/16.
 */

public class PersonDashboardFragment extends BaseFragment implements AdapterView.OnItemClickListener, TextWatcher {

    @Bind(R.id.person_listview)
    ListView logbookListView;
    PersonDashboardAdapter dashboardAdapter;

    ArrayList<DashboardFieldAgent> personsList;

    @Bind(R.id.et_search)
    EditText searchText;

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

        personsList = getPersonDashboardData();

        dashboardAdapter = new PersonDashboardAdapter(getActivity(), personsList);
        logbookListView.setAdapter(dashboardAdapter);

        searchText.addTextChangedListener(this);
    }

    private ArrayList<DashboardFieldAgent> getPersonDashboardData() {
        ArrayList<DashboardFieldAgent> fieldAgents = Singleton.getInstance().dashboardFieldAgents;
        ArrayList<DashboardFieldAgent> fieldManagers = new ArrayList<>();
        ArrayList<DashboardFieldAgent> fieldOfficers = new ArrayList<>();
        ArrayList<DashboardFieldAgent> facilitators = new ArrayList<>();
        for (DashboardFieldAgent fieldAgent : fieldAgents) {
            if (fieldAgent.getAgentType().equalsIgnoreCase(Constants.STR_FIELD_MANAGER)) {
                fieldManagers.add(fieldAgent);
            } else if (fieldAgent.getAgentType().equalsIgnoreCase(Constants.STR_FIELD_OFFICER)) {
                fieldOfficers.add(fieldAgent);
            } else if (fieldAgent.getAgentType().equalsIgnoreCase(Constants.STR_FACILITATOR)) {
                facilitators.add(fieldAgent);
            }
        }
        ArrayList<DashboardFieldAgent> allAgents = new ArrayList<>();

        if (fieldManagers.size() > 0) {
            allAgents.add(new DashboardFieldAgent("FIELD MANAGERS", "", "", "", "", "", null, true));
            allAgents.addAll(fieldManagers);
        }
        if (fieldOfficers.size() > 0) {
            allAgents.add(new DashboardFieldAgent("FIELD OFFICERS", "", "", "", "", "", null, true));
            allAgents.addAll(fieldOfficers);
        }
        if (facilitators.size() > 0) {
            allAgents.add(new DashboardFieldAgent("FACILITATORS", "", "", "", "", "", null, true));
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
        DashboardFieldAgent clickedAgent = personsList.get(position - logbookListView.getHeaderViewsCount());
        ((MyDashboard)getParentFragment()).addPersonDetailViewFragment(clickedAgent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
        dashboardAdapter.filter(text);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}