package com.example.tracegame.models;

/**
 * Created by Administrator on 2016/12/25.
 */
public class SmsInfo {
    public String _id = "";
    public String thread_id = "";
    public String smsAddress = "";
    public String smsBody = "";
    public String read = "";
    public int action = 0;
    @Override
    public String toString() {
        return "SmsInfo [_id=" + _id + ", thread_id=" + thread_id
                + ", smsAddress=" + smsAddress + ", smsBody=" + smsBody
                + ", read=" + read + ", action=" + action + "]";
    }

}
