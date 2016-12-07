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

        ArrayList<String> descriptionList = new ArrayList<>();
        descriptionList.add("Alex checked out of Agnes Mwaro's farm");
        descriptionList.add("Alex updated Agnes Mwaro's hole count to 185");
        descriptionList.add("Alex checked into Agnes Mwaro's farm");
        descriptionList.add("Alex checked out of Bahati Kahandi's farm");
        descriptionList.add("Alex updated Bahati Kahandi's hole count to 122");
        descriptionList.add("Alex completed a survey at Bahati Kahandi's farm");
        descriptionList.add("Alex checked into Bahati Kahandi's farm");
        descriptionList.add("Alex checked out of Hawar Bhai's farm");
        descriptionList.add("Alex updated Hawar Bhai's hole count to 200");
        descriptionList.add("Alex checked into Hawar Bhai's farm");

        logBookAdapter = new TeamLogBookAdapter(getActivity(), descriptionList);
        logbookListView.setAdapter(logBookAdapter);
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

}
