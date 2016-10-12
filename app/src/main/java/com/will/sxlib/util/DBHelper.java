package com.will.sxlib.util;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.will.sxlib.base.MyApplication;

/**
 * Created by Will on 2016/10/2.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorite.db";
    private static final int DATABASE_VERSION = 1;
    public DBHelper(){
        super(MyApplication.getGlobalContext(),DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorite"+
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT, state BOOLEAN ,author TEXT ," +
                "publish_date TEXT , press TEXT , isbn TEXT, cover TEXT,book_number TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
