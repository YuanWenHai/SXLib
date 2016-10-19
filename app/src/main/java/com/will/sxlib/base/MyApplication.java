package com.will.sxlib.base;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import cn.bmob.statistics.AppStat;

/**
 * Created by Will on 2016/6/10.
 */
public class MyApplication extends Application {
    private static Context mContext;
    private static final String APP_ID = "af3447364de5157bfeee2bd64176d5e3";
    @Override
    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();
        if(!AppStat.i(APP_ID,null)){
            Log.e("BmobComponent ","initialize failed!");
        }
    }
    public static Context getGlobalContext(){
        return mContext;
    }

}
