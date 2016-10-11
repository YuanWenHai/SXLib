package com.will.sxlib.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.will.sxlib.bean.Book;
import com.will.sxlib.bean.FavoriteItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/8/13.
 */
public class DBManager {
    private SQLiteDatabase db;
    private static DBManager instance;
    public static DBManager getInstance(){
        if(instance == null){
            synchronized (DBManager.class){
                if(instance == null){
                    instance = new DBManager();
                }
            }
        }
        return instance;
    }
    private DBManager(){
        DBHelper dbHelper = new DBHelper();
        db = dbHelper.getWritableDatabase();
    }
    public List<FavoriteItem> getAllItems(){
        Cursor cursor = db.rawQuery("SELECT * FROM favorite order by _id desc",null);
        List<FavoriteItem> list = new ArrayList<>();
        FavoriteItem item ;
        while (cursor.moveToNext()){
            item = new FavoriteItem();
            item.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            item.setState(cursor.getInt(cursor.getColumnIndex("state")));
            item.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            item.setPublicationDate(cursor.getString(cursor.getColumnIndex("publish_date")));
            item.setPublisher(cursor.getString(cursor.getColumnIndex("press")));
            item.setIsbn(cursor.getString(cursor.getColumnIndex("isbn")));
            item.setCoverUrl(cursor.getString(cursor.getColumnIndex("cover")));
            item.setBookNumber(cursor.getString(cursor.getColumnIndex("book_number")));
            list.add(item);
        }
        cursor.close();
        return list;
    }

    public FavoriteItem queryByTitle(String title){
        Cursor cursor = db.rawQuery("SELECT * FROM favorite WHERE title=?",new String[]{title});
        FavoriteItem item = null;
        if(cursor.moveToNext()){
            item = new FavoriteItem();
            item.setState(cursor.getInt(cursor.getColumnIndex("state")));
            item.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        }
        cursor.close();
        return item;
    }
    public void updateState(String title, int state){
        ContentValues cv = new ContentValues();
        cv.put("state",state);
        String[] args = new String[]{title};
        db.update("favorite",cv,"title=?",args);
    }
    public void deleteItemByTitle(String title){
        db.delete("favorite","title = ?",new String[]{title});
    }
    public void add(Book book,int state){
        ContentValues value = new ContentValues();
        value.put("title",book.getTitle());
        value.put("author",book.getAuthor());
        value.put("publish_date",book.getPublicationDate());
        value.put("press",book.getPublisher());
        value.put("isbn",book.getIsbn());
        value.put("state",state);
        value.put("cover",book.getCoverUrl());
        value.put("book_number",book.getBookNumber());
        db.insert("favorite","title",value);
    }
}
