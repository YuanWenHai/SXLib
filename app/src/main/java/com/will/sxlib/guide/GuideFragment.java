package com.will.sxlib.guide;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.will.sxlib.MainActivity;
import com.will.sxlib.R;
import com.will.sxlib.base.BaseFragment;

;

/**
 * Created by Will on 2016/6/1.
 */
public class GuideFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_guide,null);
        ViewPager pager = (ViewPager) view.findViewById(R.id.guide_viewpager);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.guide_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeDrawerState();
            }
        });
        setToolbarTitle("读者指南");
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.guide_tab_layout);
        pager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        return view;
    }
}
