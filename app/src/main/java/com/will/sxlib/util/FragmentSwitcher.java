package com.will.sxlib.util;

import android.app.Fragment;
import android.util.Log;

import com.will.sxlib.MainActivity;
import com.will.sxlib.R;
import com.will.sxlib.base.MyFragments;

/**
 * Created by admin on 2016/10/9.
 */

public class FragmentSwitcher {
    private MainActivity mActivity;
    private int containerRes;
    public FragmentSwitcher(MainActivity activity){
        this(activity, R.id.fragment_container);
    }
    public FragmentSwitcher(MainActivity activity,int containerRes){
        mActivity = activity;
        this.containerRes = containerRes;
    }
    public void switchTo(MyFragments which){
        if(!ifFragmentVisible(which)){
            showOrCreateFragment(which);
            hideOtherFragments(which);
            changeStatusBar(which);
        }
    }
    private  void showOrCreateFragment(MyFragments which){
        Fragment fragment;
        fragment = mActivity.getFragmentManager().findFragmentByTag(which.getTag());
        if(fragment == null){
            fragment = which.getFragment();
            mActivity.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                    .add(containerRes,fragment,which.getTag()).commit();
        }else if (!fragment.isVisible()){
            mActivity.getFragmentManager().beginTransaction().
                    setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                    .show(fragment).commit();
        }
    }
    private void hideOtherFragments(MyFragments which){
        for(MyFragments fragmentEnum : MyFragments.values()){
            if(which != fragmentEnum){
                Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(fragmentEnum.getTag());
                if(fragment != null && fragment.isVisible()){
                    mActivity.getFragmentManager().beginTransaction().
                    setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                    .hide(fragment).commit();
                }
            }
        }
    }
    private void changeStatusBar(MyFragments which){
        which.setStatusBar(mActivity.getStatusBar());
    }
    private boolean ifFragmentVisible(MyFragments which){
        Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(which.getTag());
        return fragment != null && fragment.isVisible();
    }
}
