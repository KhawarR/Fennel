package tintash.fennel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tintash.fennel.R;
import tintash.fennel.views.TitleBarLayout;

/**
 * Created by Faizan on 9/27/2016.
 */
public class MySignUps extends BaseFragment {


    @Bind(R.id.titleBar)
    TitleBarLayout titleBarLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_sign_ups, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBarLayout.setOnIconClickListener(this);
    }

    @Override
    protected String getTrackerScreenName() {
        return null;
    }

    @OnClick(R.id.rl_add)
    void onClickAdd(View view) {
        ((BaseContainerFragment) getParentFragment()).replaceFragment(new EnrollFragment(), true);
    }

    @Override
    public void onTitleBarRightIconClicked(View view) {
        ((BaseContainerFragment) getParentFragment()).replaceFragment(new AboutMe(), true);
    }
}
