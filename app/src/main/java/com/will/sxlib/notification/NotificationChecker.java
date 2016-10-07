package com.will.sxlib.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import com.will.sxlib.R;
import com.will.sxlib.bean.MyBook;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.SPHelper;
import com.will.sxlib.util.UserOperationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Will on 2016/10/2.
 */
public class NotificationChecker {
    UserOperationHelper helper;
    Context context;
    public NotificationChecker(Context context){
        helper = UserOperationHelper.getInstance(SPHelper.getAccount(),SPHelper.getPassword());
        this.context = context;
    }




    public void check(){
        if(SPHelper.getReturnNotificationState() ){
            fetchData();
        }
    }
    private void fetchData(){
        helper.getLoanData(new UserOperationHelper.RenewCallback() {
            @Override
            public void onResponse(List<MyBook> list) {
                analyseData(list);
            }

            @Override
            public void onFailure(ErrorCode code) {

            }
        });
    }
    private void analyseData(List<MyBook> list){
        for(MyBook book :list){
            int dayCount = compareDate(book.getReturnDate());
            if(dayCount <= 60 ){
                makeNotification("距离还书日期还剩"+dayCount+"天");
            }
        }
    }

    private int compareDate(String dateStr){
        String date = dateStr.replace("应还日期:","");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date returnDate = null;
        try{
            returnDate = format.parse(date);
        }catch (ParseException p){
            p.printStackTrace();
        }
        Date currentDate = new Date();
        long result = (returnDate.getTime() - currentDate.getTime())/(24*3600*1000);
        return (int) result;
    }
    private boolean checkDate(){
        Calendar c = Calendar.getInstance();
        int currentDay = c.get(Calendar.DAY_OF_YEAR);
        int lastDay = SPHelper.getLastNotifiedDate();
        if(currentDay != lastDay){
            SPHelper.setLastNotifiedDate(currentDay);
            return true;
        }
        return false;
    }

    private void makeNotification(String message){
        Notification.Builder builder = new Notification.Builder(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSmallIcon(R.drawable.logo)
                .setContentTitle("还书提示")
                .setContentText(message)
                .setSound(alarmSound)
                .setVibrate(new long[]{0,300});
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int)(Math.random()),builder.build());
    }
}
