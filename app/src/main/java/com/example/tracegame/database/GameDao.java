package com.example.tracegame.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tracegame.models.GameModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/25.
 */
public class GameDao {

    public static final String DB_NAME = "gameDb";

    private static GameDao gameDao;

    private SQLiteDatabase mTraceDb;

    private GameDao(Context context) {
        GameOpenHelper dbHelper = new GameOpenHelper(context, DB_NAME, null, 1);
        mTraceDb = dbHelper.getWritableDatabase();
    }

    public static GameDao getInstance(Context context) {
        if (gameDao == null) {
            gameDao = new GameDao(context);
        }
        return gameDao;
    }

    public boolean insert(GameModel gameModel){
        try {
            mTraceDb.insert("game", null, gameModel.getContentValues());
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean delete(String phoneNum){
        try {
            mTraceDb.delete("game", "phoneNum=?", new String[]{phoneNum});
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean update(GameModel gameModel){
        try {
            mTraceDb.update("game", gameModel.getContentValues(), "phoneNum=?", new String[]{gameModel.getPhoneNum()});
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public List<GameModel> getPerson(int type){
        String sql = "select * from game where type=" + type;
        List<GameModel> gameModelList = new ArrayList<>();
        Cursor cursor = mTraceDb.rawQuery(sql, null);
        if(cursor == null)
            return gameModelList;
        while (cursor.moveToNext()){
            GameModel gameModel = new GameModel();
            gameModel.setPhoneNum(cursor.getString(1));
            gameModel.setLatitude(cursor.getDouble(2));
            gameModel.setLongitude(cursor.getDouble(3));
            gameModel.setType(cursor.getInt(4));
            gameModelList.add(gameModel);
        }
        cursor.close();
        return gameModelList;
    }

}
