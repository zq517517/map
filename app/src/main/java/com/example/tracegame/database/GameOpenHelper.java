package com.example.tracegame.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/12/25.
 */
public class GameOpenHelper extends SQLiteOpenHelper{

    public static final String GAME_TABLE_NAME = "game";

    public static final String CREATE_NEW_TABLE = "create table "+ GAME_TABLE_NAME +" ("
            + "id integer primary key autoincrement, "
            + "phoneNum text, "
            + "latitude text, "
            + "longitude text, "
            + "type text)";

    public GameOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
