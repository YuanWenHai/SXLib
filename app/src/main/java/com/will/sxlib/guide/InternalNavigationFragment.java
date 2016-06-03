package com.will.sxlib.guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.will.sxlib.R;
import com.will.sxlib.base.BaseFragment;

/**
 * Created by Will on 2016/6/1.
 */
public class InternalNavigationFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_internal_navigation,null);
        return view;
    }
}
