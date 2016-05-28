package com.will.sxlib.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Will on 2016/3/31.
 */
public class FABScrollBehavior extends FloatingActionButton.Behavior {
    public FABScrollBehavior(Context context , AttributeSet set){
        super();
    }
    @Override
    public void onNestedScroll(CoordinatorLayout layout, FloatingActionButton fab, View target,
                               int dxConsumed, int dyConsumed, int dyUnconsumed, int dxUnconsumed){
        super.onNestedScroll(layout,fab,target,dyConsumed,dxConsumed,dyUnconsumed,dxUnconsumed);
        if(dyConsumed > 0 && fab.getVisibility() == View.VISIBLE){
            fab.hide();
        }else if (dyConsumed < 0 && fab.getVisibility() == View.GONE){
            fab.show();
        }
    }
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout layout, FloatingActionButton fab,
                                       View directTargetChild, View target, int nestedScrollAxes){
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
