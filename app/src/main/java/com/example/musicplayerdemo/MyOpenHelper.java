package com.example.musicplayerdemo;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {

    private static final String db_name = "music.db";
    private static final int version = 1;

    public MyOpenHelper(Context context) {
        super(context,db_name,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table collectedmusic(id integer primary key autoincrement,collectedmusicpath varchar(512))");
    }

    // 当数据库版本升级时候会调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
