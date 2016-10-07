package com.will.sxlib.bean;

/**
 * Created by Will on 2016/10/2.
 */
public class FavoriteItem {
    public static final int EXSIT = 0;
    public static final int NON_EXSIT = 1;
    private String title;
    private int state;
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setState(int state){
        this.state = state;
    }
    public int getState(){
        return state;
    }
}
