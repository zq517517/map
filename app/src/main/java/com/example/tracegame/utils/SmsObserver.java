package com.example.tracegame.utils;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.example.tracegame.models.SmsInfo;

/**
 * Created by Administrator on 2016/12/25.
 */
public class SmsObserver extends ContentObserver {

    private ContentResolver mResolver;
    public Handler smsHandler;

    public SmsObserver(ContentResolver mResolver, Handler handler) {
        super(handler);
        this.mResolver = mResolver;
        this.smsHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        Cursor mCursor = mResolver.query(Uri.parse("content://sms/inbox"),
                new String[] { "_id", "address", "read", "body", "thread_id" },
                "read=?", new String[] { "0" }, "date desc");

        if (mCursor == null) {
            return;
        } else {
            while (mCursor.moveToNext()) {
                SmsInfo _smsInfo = new SmsInfo();

                int _inIndex = mCursor.getColumnIndex("_id");
                if (_inIndex != -1) {
                    _smsInfo._id = mCursor.getString(_inIndex);
                }

                int thread_idIndex = mCursor.getColumnIndex("thread_id");
                if (thread_idIndex != -1) {
                    _smsInfo.thread_id = mCursor.getString(thread_idIndex);
                }

                int addressIndex = mCursor.getColumnIndex("address");
                if (addressIndex != -1) {
                    _smsInfo.smsAddress = mCursor.getString(addressIndex);
                }

                int bodyIndex = mCursor.getColumnIndex("body");
                if (bodyIndex != -1) {
                    _smsInfo.smsBody = mCursor.getString(bodyIndex);
                }

                int readIndex = mCursor.getColumnIndex("read");
                if (readIndex != -1) {
                    _smsInfo.read = mCursor.getString(readIndex);
                }

                Message msg = smsHandler.obtainMessage();
                msg.what = 0;
                msg.obj = _smsInfo;
                smsHandler.sendMessage(msg);
            }
        }

        mCursor.close();
    }
}