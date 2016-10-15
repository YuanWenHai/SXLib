package com.will.sxlib.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.will.sxlib.base.MyApplication;

/**
 * Created by Will on 2016/10/2.
 */
public class SPHelper {
    private static SharedPreferences configSP = MyApplication.getGlobalContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = configSP.edit();
    /**
     * 获取一个全局SharedPreferences对象，创建名为config.
     * @return configSP
     */
    public static SharedPreferences getConfigSP(){
        return configSP;
    }


    public static void setReturnNotificationState(boolean which){
        editor.putBoolean("return_notification",which).apply();
    }
    public static boolean getReturnNotificationState(){
        return configSP.getBoolean("return_notification",false);
    }



    public static void setLoanableNotificationState(boolean which){
        editor.putBoolean("loanable_notification",which).apply();
    }
    public static boolean getLoanableNotificationState(){
        return configSP.getBoolean("loanable_notification",false);
    }



    public static void setLastReturnNotifiedDate(int day){
        editor.putInt("last_return_notified_date",day).apply();
    }
    public static int getLastReturnNotifiedDate(){
        return configSP.getInt("last_return_notified_date",0);
    }

    public static void setLastLoanNotifiedDate(int day){
        editor.putInt("last_loan_notified_date",day).apply();
    }
    public static int getLastLoanNotifiedDate(){
        return configSP.getInt("last_loan_notified_date",0);
    }


    public static String getAccount(){
        return configSP.getString("account","");
    }
    public static void setAccount(String account){
        editor.putString("account",account).apply();
    }



    public static String getPassword(){
        return configSP.getString("password","");
    }
    public static void setPassword(String password){
        editor.putString("password",password).apply();
    }


    public static void setUserName(String userName){
        editor.putString("user_name",userName).apply();
    }
    public static String getUserName(){
        return configSP.getString("user_name","匿名读者");
    }




    public static boolean getArrowState(){
        return  configSP.getBoolean("arrow_state",false);
    }
    public static void setArrowState(boolean state){
        editor.putBoolean("arrow_state",state).apply();
    }



    public static void clearConfig(){
        editor.clear().commit();
    }

}
