package com.will.sxlib.util;

import android.widget.Toast;

import com.will.sxlib.base.MyApplication;

/**
 * Created by will on 2016/10/12.
 */

public class CommonUtil {
    private static Toast mToast;
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
            mToast = Toast.makeText(MyApplication.getGlobalContext(),message,length);
        }
        mToast.setText(message);
        mToast.show();
    }
}
