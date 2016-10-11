package com.will.sxlib.bean;

/**
 * Created by Will on 2016/10/2.
 */
public class FavoriteItem extends Book{
    public static final int EXSIT = 0;
    public static final int NON_EXSIT = 1;
    private int state;
    public void setState(int state){
        this.state = state;
    }
    public int getState(){
        return state;
    }
}
