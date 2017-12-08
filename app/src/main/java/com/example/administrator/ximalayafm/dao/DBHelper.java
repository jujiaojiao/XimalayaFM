package com.example.administrator.ximalayafm.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String NAME = "XIMALAYA.db";
    public static final int VSERDION = 1;
    private String CREATE_XIMALAYAFM_TABLE = "create table if not exists  ximalayaFM( id LONG PRIMARY KEY ,track_title TEXT ,track_tags TEXT,track_intro TEXT,cover_url_small TEXT,cover_url_middle TEXT,cover_url_large,announcer TEXT,duration INTERGER,song_path TEXT)";
    public DBHelper(Context context){
        super(context, NAME, null, VSERDION);
    }

    public DBHelper(Context context, String name, String factory, int version) {
        super(context, NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_XIMALAYAFM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
