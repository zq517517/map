package com.example.tracegame.models;

import android.content.ContentValues;

/**
 * Created by Administrator on 2016/12/25.
 */
public class GameModel {

    private String phoneNum;
    private double latitude;
    private double longitude;
    private int type;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ContentValues getContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("phoneNum",phoneNum);
        contentValues.put("latitude",latitude);
        contentValues.put("longitude",longitude);
        contentValues.put("type",type);
        return contentValues;
    }

    @Override
    public String toString() {
        return phoneNum+" "+latitude+" "+longitude+" "+type;
    }
}
