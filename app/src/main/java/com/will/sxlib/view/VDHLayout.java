package com.will.sxlib.view;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by will on 2016/10/13.
 */

public class VDHLayout extends LinearLayout {
    private ViewDragHelper mDragger;

    private View mDragView;

    private Point mAutoBackOriginPos = new Point();

    private OnRemoveCallback mCallback;

    private boolean removed;

    private float lastX,lastY;

    private boolean interceptHorizontalMotion;

    private float autoRemoveMultiplier = 0.5f;

    public VDHLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDragger = ViewDragHelper.create(this, 1f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }


            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild == mDragView) {
                    int viewWidth = releasedChild.getWidth();
                    float releasedX = releasedChild.getX();
                    float releasedY = releasedChild.getY();
                    //向右滑动
                    if(releasedX > viewWidth * autoRemoveMultiplier ){
                        removed = true;
                        mDragger.settleCapturedViewAt(viewWidth,(int) releasedY);
                    //向左滑动
                    }else if( releasedX < -viewWidth * autoRemoveMultiplier){
                        removed = true;
                        mDragger.settleCapturedViewAt(-viewWidth,(int) releasedY);
                    //滑动距离不足view的一半则复位
                    }else{
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                    }
                    invalidate();
                }
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
                break;
            case MotionEvent.ACTION_MOVE:
                float absX = Math.abs(event.getX()-lastX);
                //为横向滑动判定增加了this.width/100 的距离判定，避免过度横向判定
                //但实际上，也只是处理了对父view事件的锁定，即，在此临界值之前，view依旧会横屏滚动，只是不会
                //剥夺父容器对点击事件的处理权。
                if(!interceptHorizontalMotion && absX > (Math.abs(event.getY()-lastY)) + getWidth()/100 ){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    interceptHorizontalMotion = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                interceptHorizontalMotion = false;
                break;
        }
        mDragger.processTouchEvent(event);
            return true;
    }
    public void setAutoRemoveMultiplier(float multiplier){
        if(multiplier == 0){
            throw new RuntimeException("The multiplier must not be zero!");
        }
        autoRemoveMultiplier = Math.min(Math.abs(multiplier),1f);
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
            mDragger.getViewDragState();
        }else if (removed){
            if(mCallback != null){
                mCallback.onRemove(mDragView);
            }
            removed = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        mAutoBackOriginPos.x = mDragView.getLeft();
        mAutoBackOriginPos.y = mDragView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDragView = getChildAt(0);
    }
    public interface OnRemoveCallback {
        void onRemove(View removedView);
    }
    public void setOnRemoveCallback(OnRemoveCallback callback){
        mCallback = callback;
    }
}