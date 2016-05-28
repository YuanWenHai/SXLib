package com.will.sxlib.searchBook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.will.sxlib.R;
import com.will.sxlib.base.BaseFragment;

/**
 * Created by Will on 2016/5/28.
 */
public class NewSearchFragment extends BaseFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view  = inflater.inflate(R.layout.fragment_new_search,null);
        final FloatingSearchView searchView = (FloatingSearchView) view.findViewById(R.id.floating_search_view);
        return view;
    }
}
