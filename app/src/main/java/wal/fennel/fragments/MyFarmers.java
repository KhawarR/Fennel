package wal.fennel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.NetworkPolicy;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.activities.AboutMe;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.CircleViewTransformation;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.PreferenceHelper;
import wal.fennel.views.TitleBarLayout;

public class MyFarmers extends BaseFragment {

    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;
    CircleImageView cIvIconRight;

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
    public void onTitleBarRightIconClicked(View view) {
        startActivity(new Intent(getActivity(), AboutMe.class));
    }
}
