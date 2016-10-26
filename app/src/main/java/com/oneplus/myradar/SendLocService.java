package com.oneplus.myradar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.oneplus.util.AESEncryptor;

import java.util.ArrayList;

/**
 * Created by Oneplus on 2016/10/16.
 */
public class SendLocService extends Service {

    private String phoneNumber = null;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        phoneNumber = intent.getStringExtra("phoneNumber");
        MyApplication application = (MyApplication)getApplicationContext();
        String smsText = application.getLat()
                + "/" + application.getLon();
        try {
            smsText = AESEncryptor.encrypt(smsText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage(smsText);  //因为一条短信有字数限制，因此要将长短信拆分
        for (String text : list) {
            manager.sendTextMessage(phoneNumber, null, text, null, null);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void deleteSMS(Context context, String phoneNumber, String smscontent)
    {
        try
        {
            // 准备系统短信收信箱的uri地址
            Uri uri = Uri.parse("content://sms/inbox");// 收信箱
            // 查询收信箱里所有的短信
            Cursor isRead =
                    context.getContentResolver().query(uri, null, "read=" + 0,
                            null, null);
            while (isRead.moveToNext())
            {
                String phone =
                        isRead.getString(isRead.getColumnIndex("address")).trim();//获取发信人
                String body =
                        isRead.getString(isRead.getColumnIndex("body")).trim();// 获取信息内容
                if (phone.equals(phoneNumber) && body.equals(smscontent))
                {
                    int id = isRead.getInt(isRead.getColumnIndex("_id"));

                    context.getContentResolver().delete(
                            Uri.parse("content://sms"), "_id=" + id, null);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
