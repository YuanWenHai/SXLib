package com.will.sxlib.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import com.will.sxlib.R;
import com.will.sxlib.bean.BookState;
import com.will.sxlib.bean.FavoriteItem;
import com.will.sxlib.bean.MyBook;
import com.will.sxlib.util.CommonUtil;
import com.will.sxlib.util.DBManager;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.NetworkHelper;
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
        //关于还书信息的通知，应对登陆状态进行处理
        if(checkDate()){
            if(SPHelper.getReturnNotificationState() ){
                if(!SPHelper.getUserName().isEmpty()){
                    fetchLoanData();
                }else{
                    CommonUtil.showToast("未登录");
                    SPHelper.setReturnNotificationState(false);
                }
            }
            if(SPHelper.getLoanableNotificationState()){
                fetchBookStateData();
            }
        }
    }
    private void fetchLoanData(){
        helper.getLoanData(new UserOperationHelper.RenewCallback() {
            @Override
            public void onResponse(List<MyBook> list) {
                analyseLoanData(list);
            }

            @Override
            public void onFailure(ErrorCode code) {

            }
        });
    }

    private void fetchBookStateData(){
        List<FavoriteItem> list = DBManager.getInstance().getAllItems();
        for(final FavoriteItem item : list){
           if(item.getState() == FavoriteItem.NON_EXSIT){
               NetworkHelper.getInstance().getBookState(item.getBookNumber(), new NetworkHelper.LoadStateCallback() {
                   @Override
                   public void onResponse(List<BookState> bookState) {
                       if(analyseBookStateData(bookState) == FavoriteItem.EXSIT){
                           makeNotification(item.getTitle()+"已经归还，现可以借阅");
                           DBManager.getInstance().updateState(item.getTitle(),FavoriteItem.EXSIT);
                       }
                   }

                   @Override
                   public void onFailure(ErrorCode code) {
                       CommonUtil.showToast("获取数据失败");
                   }
               });

           }
        }
    }

    private int analyseBookStateData(List<BookState> list){
        for(BookState state :list){
           if(state.getState().equals("在馆")){
               return FavoriteItem.EXSIT;
           }
        }
        return FavoriteItem.NON_EXSIT;
    }
    private void analyseLoanData(List<MyBook> list){
        for(MyBook book :list){
            int dayCount = compareToCurrentDate(book.getReturnDate().replace("应还日期:",""));
            if(dayCount <= 60 ){
                makeNotification("距离还书日期还剩"+dayCount+"天");
            }
        }
    }

    /**
     * 将指定日期与当前日期进行比较，计算方式为： 指定日期 - 当前日期，单位：天
     *<p> 参数格式：yyyy-MM-dd</p>
     * @param dateStr
     * @return
     */
    private int compareToCurrentDate(String dateStr){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date returnDate = null;
        try{
            returnDate = format.parse(dateStr);
        }catch (ParseException p){
            p.printStackTrace();
        }
        Date currentDate = new Date();
        long result = (returnDate.getTime() - currentDate.getTime())/(24*3600*1000);
        return (int) result;
    }

    /**
     * 检查日期并写入sp，避免一天内多次显示
     * @return
     */
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
