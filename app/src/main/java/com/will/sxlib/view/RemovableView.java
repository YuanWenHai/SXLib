package com.will.sxlib.view;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.will.sxlib.util.CommonUtil;

/**
 * Created by will on 2016/10/13.
 * 一个简单的滑动组件，只可以有一个ChildView,此ChildView将可以被拖动，在本组件中滑动方向是left——right.
 *  <p>实现一些简单的弹性效果，释放时自动归位/移出屏幕</p>
 * <p>可以通过{@link #setAutoRemoveMultiplier(float)}设定释放时的移除临界值，默认为0.5,也就是View的一半距离</p>
 */

public class RemovableView extends LinearLayout {
    private ViewDragHelper mDragger;

    private View mContentView;

    private int originX,originY;

    private OnRemoveCallback mRemoveCallback;

    private float movedXDistance;

    private boolean cancel;

    private boolean removed;

    private float lastX,lastY;

    private float touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private boolean interceptHorizontalMotion;

    private float autoRemoveMultiplier = 0.5f;

    public RemovableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDragger = ViewDragHelper.create(this, 1f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
        }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                    int viewWidth = releasedChild.getWidth();
                    float releasedX = releasedChild.getX();
                    float releasedY = releasedChild.getY();
                    //向右滑动
                    if(releasedX > viewWidth * autoRemoveMultiplier ){
                        removed = true;
                        mDragger.settleCapturedViewAt(CommonUtil.getScreenWidthInPixels(),(int) releasedY);
                    //向左滑动
                    }else if( releasedX < -viewWidth * autoRemoveMultiplier){
                        removed = true;
                        mDragger.settleCapturedViewAt(-CommonUtil.getScreenWidthInPixels(),(int) releasedY);
                    //滑动距离不足临界值则复位
                    }else{
                        mDragger.settleCapturedViewAt(originX, originY);
                    }
                    invalidate();
            }

        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return  mDragger.shouldInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                movedXDistance = 0;
                cancel = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float absX = Math.abs(event.getX()-lastX);
                movedXDistance += absX;
                //因为重写了onTouchEvent，所以整个layout的onClick将变得不可用，故主动performClick
                //若接受到移动手势，则取消Click的执行
                if(!cancel && movedXDistance > touchSlop){
                    cancel = true;
                }
                //为横向滑动判定增加了minimum touch slop的距离补差，避免过度横向判定
                //但实际上，也只是处理了对父view事件的锁定，即，在此临界值之前，view依旧会横屏滚动，只是不会
                //剥夺父容器对点击事件的处理权。
                if(!interceptHorizontalMotion && absX > (Math.abs(event.getY()-lastY)) + touchSlop){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    interceptHorizontalMotion = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                interceptHorizontalMotion = false;
                if(!cancel){
                    performClick();
                }

                break;
        }
        mDragger.processTouchEvent(event);
            return true;
    }
    public void setAutoRemoveMultiplier(float multiplier){
        if(multiplier == 0){
            throw new IllegalArgumentException("The multiplier must not be zero!");
        }
        autoRemoveMultiplier = Math.min(Math.abs(multiplier),1f);
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
            mDragger.getViewDragState();
        }else if (removed){
            if(mRemoveCallback != null){
                mRemoveCallback.onRemove(mContentView);
            }
            removed = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        originX = mContentView.getLeft();
        originY = mContentView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount()> 1){
            throw new IllegalArgumentException("The child count of RemovableView can only be one!");
        }
        mContentView = getChildAt(0);
    }
    public interface OnRemoveCallback {
        void onRemove(View removedView);
    }
    public interface OnClickListener{
        void onClick(View view);
    }
    public void setOnRemoveCallback(OnRemoveCallback callback){
        mRemoveCallback = callback;
    }
}