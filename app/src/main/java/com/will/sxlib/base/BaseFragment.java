package com.will.sxlib.base;

import android.app.Fragment;
import android.widget.Toast;

/**
 * Created by Will on 2016/5/25.
 */
public class BaseFragment extends Fragment {
    Toast toast;

    /**
     *  展示Toast
     * @param message 内容
     */
    public void showToast(String message){
        if(toast == null){
            toast = Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        }
        toast.setText(message);
        toast.show();
    }
}
