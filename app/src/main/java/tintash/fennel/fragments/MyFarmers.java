package tintash.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import tintash.fennel.R;
import tintash.fennel.adapters.MySignupsAdapter;
import tintash.fennel.models.Farmer;
import tintash.fennel.network.NetworkHelper;
import tintash.fennel.utils.CircleViewTransformation;
import tintash.fennel.utils.Constants;
import tintash.fennel.utils.MyPicassoInstance;
import tintash.fennel.utils.PreferenceHelper;
import tintash.fennel.views.TitleBarLayout;


public class MyFarmers extends BaseFragment {

    //region Class variables & UI
    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Bind(R.id.lv_farmers)
    ListView mLvFarmers;

    EditText etSearch;

    CircleImageView cIvIconRight;ArrayList<Farmer> myFarmers = new ArrayList<>();

    MySignupsAdapter adapter;

    int locationsResponseCounter = 0;

    SwipeRefreshLayout mSwipeRefreshLayout;
    //endregion

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_farmers, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);
        cIvIconRight = (CircleImageView) titleBarLayout.findViewById(R.id.imgRight);

        String aboutMeAttId = PreferenceHelper.getInstance().readAboutAttId();
        if(!aboutMeAttId.isEmpty())
        {
            String thumbUrl = String.format(NetworkHelper.URL_ATTACHMENTS, PreferenceHelper.getInstance().readInstanceUrl(), aboutMeAttId);
            MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).onlyScaleDown().centerCrop().transform(new CircleViewTransformation()).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).into(cIvIconRight);
        }
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).addFragment(new AboutMe(), true);
    }
}
