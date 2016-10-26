package com.oneplus.myradar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

/**
 * Created by Oneplus on 2016/10/24.
 */
public class DeleteSMSService extends Service{

    private String phoneNumber = null;
    private String body = null;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneNumber = intent.getStringExtra("phoneNumber");
        body = intent.getStringExtra("body");
        phoneNumber = intent.getStringExtra("phoneNumber");
        body = intent.getStringExtra("body");
        DeleteThread deleteThread = new DeleteThread(phoneNumber, body);
        deleteThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public class DeleteThread extends Thread {

        private String phoneNumber = null;
        private String body = null;

        public DeleteThread(String phoneNumber, String body) {
            // TODO Auto-generated constructor stub
            this.phoneNumber = phoneNumber;
            this.body = body;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(10000);
                deleteSMS(getApplicationContext(),phoneNumber,body);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            stopSelf();
        }

        private void deleteSMS(Context context, String phoneNumber, String smscontent)
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
                    if (body.equals(smscontent) && phone.equals(phoneNumber))
                    {
                        int id = isRead.getInt(isRead.getColumnIndex("_id"));

                        context.getContentResolver().delete(
                                Uri.parse("content://sms"), "_id=" + id, null);
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
