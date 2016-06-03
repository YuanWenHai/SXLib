package com.will.sxlib.base;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by Will on 2016/5/25.
 */
public class BaseActivity extends AppCompatActivity {
    Toast toast;

    /**
     * 显示一个toast，默认时长为短
     * @param message 内容
     */
    public void showToast(String message){
        if(toast == null){
            toast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        }
        toast.setText(message);
        toast.show();
    }
}
