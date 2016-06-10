package com.will.sxlib.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by Will on 2016/6/10.
 */
public class MyApplication extends Application {
    private static Context mContext;
    @Override
    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();
    }
    public static Context getGlobalContext(){
        return mContext;
    }
}
