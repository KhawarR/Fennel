package tintash.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import tintash.fennel.R;
import tintash.fennel.adapters.MySignupsAdapter;
import tintash.fennel.models.Farmer;
import tintash.fennel.utils.Constants;
import tintash.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class MySignUps extends BaseFragment implements View.OnClickListener {


    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.lv_farmers)
    ListView mLvFarmers;

    RelativeLayout rlAdd;

    ArrayList<Farmer> myFarmers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_sign_ups, container, false);
        ButterKnife.bind(this, view);

        populateDummyData();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);

        LayoutInflater myinflater = getActivity().getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)myinflater.inflate(R.layout.header_mysignups_list, mLvFarmers, false);

        rlAdd = (RelativeLayout) myHeader.findViewById(R.id.rl_add);
        rlAdd.setOnClickListener(this);
        myHeader.setEnabled(false);
        myHeader.setOnClickListener(null);
        mLvFarmers.addHeaderView(myHeader);
        // Creating our custom adapter
        MySignupsAdapter adapter = new MySignupsAdapter(getActivity(), myFarmers);

        // Create the list view and bind the adapter
        mLvFarmers.setAdapter(adapter);

        mLvFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= mLvFarmers.getHeaderViewsCount();
                Farmer farmer = myFarmers.get(position);
                if(!farmer.isHeader())
                {
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_EDIT_FARMER, farmer), true);
                }
            }
        });

    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).replaceFragment(new AboutMe(), true);
    }

    private void populateDummyData()
    {
        myFarmers.clear();
        myFarmers.add(new Farmer(Constants.STR_INCOMPLETE, "", "", "", "", "", "", true));
        myFarmers.add(new Farmer("Tabu Karisa Karema", "Tabu", "Karisa", "Karema",  "", "Kwa Firi, Mihirini", Constants.STR_INCOMPLETE, false));
        myFarmers.add(new Farmer("Safari Kazungu Zapo", "Safari", "Kazungu", "Zapo", "", "Kombe Nzai, Madzeni", Constants.STR_INCOMPLETE, false));
        myFarmers.add(new Farmer(Constants.STR_PENDING, "", "", "", "", "", "", true));
        myFarmers.add(new Farmer("Kabibi Mumba Nzai", "Kabibi", "Mumba", "Nzai", "", "Mwalimu Shikari, Madzeni", Constants.STR_PENDING, false));
        myFarmers.add(new Farmer("Hadija Kitsao Mujisi", "Hadija", "Kitsao", "Mujisi", "", "Kombe Nzai, Madzeni", Constants.STR_PENDING, false));
        myFarmers.add(new Farmer(Constants.STR_APPROVED, "", "", "", "", "", "", true));
        myFarmers.add(new Farmer("Agnes Dama Mwaro", "Agnes", "Dama", "Mwaro", "", "Madzeni", Constants.STR_APPROVED, false));
        myFarmers.add(new Farmer("Chengo Mumba Nzai", "Chengo", "Mumba", "Nzai", "", "Nidgiria, Kwa Nzai", Constants.STR_APPROVED, false));
        myFarmers.add(new Farmer("Kadii Gohu Nzaro", "Kadii", "Gohu", "Nzaro", "", "Nidgiria, Kwa Nzai", Constants.STR_APPROVED, false));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.rl_add:
            {
                ((BaseContainerFragment) getParentFragment()).replaceFragment(EnrollFragment.newInstance(Constants.STR_ENROLL_FARMER, null), true);
            }
                break;
        }
    }
}
