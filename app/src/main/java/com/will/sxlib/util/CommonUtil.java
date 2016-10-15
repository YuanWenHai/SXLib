package com.will.sxlib.util;

import android.util.DisplayMetrics;
import android.widget.Toast;

import com.will.sxlib.base.MyApplication;

import static com.will.sxlib.base.MyApplication.getGlobalContext;

/**
 * Created by will on 2016/10/12.
 */

public class CommonUtil {
    private static Toast mToast;

    private static int mScreenWidth,mScreenHeight;
    public static void showToast(String message){
        showToast(message,Toast.LENGTH_SHORT);
    }

    /**
     *
     * @param message
     * @param length 0/1，short/long
     */
    public static void showToast(String message,int length){
        if(mToast == null){
            mToast = Toast.makeText(getGlobalContext(),message,length);
        }
        mToast.setText(message);
        mToast.show();
    }
    public static int getScreenWidthInPixels(){
        if(mScreenWidth == 0){
            DisplayMetrics dm = MyApplication.getGlobalContext().getResources().getDisplayMetrics();
            mScreenWidth = dm.widthPixels;
        }
        return mScreenWidth;
    }
    public static int getScreenHeightInPixels(){
        if(mScreenHeight == 0){
            DisplayMetrics dm = MyApplication.getGlobalContext().getResources().getDisplayMetrics();
            mScreenHeight = dm.heightPixels;
        }
        return mScreenHeight;
    }
}
