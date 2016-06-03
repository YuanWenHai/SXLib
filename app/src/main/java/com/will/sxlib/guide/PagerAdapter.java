package com.will.sxlib.guide;


import android.app.Fragment;
import android.app.FragmentManager;


/**
 * Created by Will on 2016/6/1.
 */
public class PagerAdapter extends android.support.v13.app.FragmentPagerAdapter {
    private static final int ITEM_COUNT = 4;
    private static final String[] titles = new String[]{"简介","内部布局","省图导引","办卡须知"};
    public PagerAdapter(FragmentManager manager){
        super(manager);
    }
    @Override
    public int getCount(){
        return ITEM_COUNT;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return new LibIntroFragment();
            case 1:
                return new InternalNavigationFragment();
            case 2:
                return  new LibNavigationFragment();

            case 3:
                return new CardNoticeFragment();

            default:
                return new LibIntroFragment();
        }

    }
    @Override
    public CharSequence getPageTitle(int position){
        return titles[position];
    }
}
