package com.will.sxlib.base;

import android.view.View;

import com.will.sxlib.R;
import com.will.sxlib.favorite.FavoriteFragment;
import com.will.sxlib.guide.GuideFragment;
import com.will.sxlib.myBook.MyBookFragment;
import com.will.sxlib.searchBook.SearchFragment;

/**
 * Created by admin on 2016/10/9.
 */

public enum  MyFragments {
    SEARCH(0){
        @Override
        public BaseFragment getFragment() {
            return new SearchFragment();
        }

        @Override
        public String getTag() {
            return TAG_SEARCH;
        }

        @Override
        public void setStatusBar(View view) {
            view.setBackgroundResource(R.drawable.status_bar_bg);
        }

    },
    GUIDE(1){
        @Override
        public BaseFragment getFragment() {
            return new GuideFragment();
        }

        @Override
        public String getTag() {
            return TAG_GUIDE;
        }

        @Override
        public void setStatusBar(View view) {
            view.setBackgroundColor(MyApplication.getGlobalContext().getResources().getColor(R.color.colorPrimaryDark));
        }
    },
    MY_BOOK(2){
        @Override
        public BaseFragment getFragment() {
            return new MyBookFragment();
        }

        @Override
        public String getTag() {
            return TAG_MY_BOOK;
        }

        @Override
        public void setStatusBar(View view) {
            view.setBackgroundColor(MyApplication.getGlobalContext().getResources().getColor(R.color.colorPrimaryDark));
        }
    },
    MY_FAVORITE(3){
        @Override
        public BaseFragment getFragment() {
            return new FavoriteFragment();
        }

        @Override
        public String getTag() {
            return TAG_MY_FAVORITE;
        }

        @Override
        public void setStatusBar(View view) {
            view.setBackgroundColor(MyApplication.getGlobalContext().getResources().getColor(R.color.colorPrimaryDark));
        }
    };

    private int value;
     MyFragments(int value){
        this.value = value;
    }
    public abstract BaseFragment getFragment();
    public abstract String getTag();
    public abstract void setStatusBar(View view);
    private static final String TAG_SEARCH = "search",
                                TAG_GUIDE = "guide",
                                TAG_MY_BOOK = "my_book",
                                TAG_MY_FAVORITE = "my_favorite";
}
