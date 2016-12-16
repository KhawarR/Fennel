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
import wal.fennel.models.Farmer;
import wal.fennel.utils.Constants;

/**
 * Created by irfanayaz on 12/2/16.
 */
public class PersonLogBookFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @Bind(R.id.person_listview)
    ListView logbookListView;
    PersonLogBookAdapter logBookAdapter;

    ArrayList<Farmer> personsList;

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

        personsList = new ArrayList();
        personsList.add(new Farmer(null, "", "", "FIELD OFFICERS", "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
        personsList.add(new Farmer(null, "", "", "Bahati Kenga Kahindi", "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", false, "", "", null, Constants.FarmerType.MYSIGNUPS));
        personsList.add(new Farmer(null, "", "", "Bahati Kenga Kahindi", "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", false, "", "", null, Constants.FarmerType.MYSIGNUPS));
        personsList.add(new Farmer(null, "", "", "FACILITATORS", "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", true, "", "", null, Constants.FarmerType.MYSIGNUPS));
        personsList.add(new Farmer(null, "", "", "Bahati Kenga Kahindi", "", "", "", "", "", false, "", "", "", "", "", "", "", "", false, "", "", "", "", false, "", "", null, Constants.FarmerType.MYSIGNUPS));

        logBookAdapter = new PersonLogBookAdapter(getActivity(), personsList);
        logbookListView.setAdapter(logBookAdapter);
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Farmer clickedFarmer = personsList.get(position - logbookListView.getHeaderViewsCount());
        ((MyLogbook)getParentFragment()).addPersonDetailViewFragment(clickedFarmer);
    }
}
