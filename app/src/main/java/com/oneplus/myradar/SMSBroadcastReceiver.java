package com.oneplus.myradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.oneplus.util.AESEncryptor;

import java.util.Date;

/**
 * Created by Oneplus on 2016/10/16.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");   //接收数据
        for (Object p : pdus) {
            byte[] pdu = (byte[]) p;
            SmsMessage message = SmsMessage.createFromPdu(pdu); //根据获得的byte[]封装成SmsMessage
            String originalBody = message.getMessageBody();             //发送内容
            Date date = new Date(message.getTimestampMillis());//发送时间
            String sender = message.getOriginatingAddress();    //短信发送方

            //由于短信余额不足用飞信进行测试，此处用于测试时去掉飞信短信多余部分，最终要去掉此处代码
            //String body = originalBody.substring(4,24);

            String body = null;
            try {
                body = AESEncryptor.decrypt(originalBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String smsText = context.getString(R.string.sms_text);
            if(body.equals(smsText)){
                Intent startIntent = new Intent(context, SendLocService.class);
                startIntent.putExtra("phoneNumber",sender);
                startIntent.setAction("com.oneplus.sendLocService");
                context.startService(startIntent);
                //deleteSMS(context, sender, originalBody);
                startIntent = new Intent(context, DeleteSMSService.class);
                startIntent.putExtra("phoneNumber",sender);
                startIntent.putExtra("body",originalBody);
                startIntent.setAction("com.oneplus.deleteSMSService");
                context.startService(startIntent);
                abortBroadcast();   //中断广播
            }
            else if (body.matches("^(\\d*\\.)?\\d+\\/(\\d*\\.)?\\d+$")) {
                Intent startIntent = new Intent(context, SetLocService.class);
                Bundle bundle = new Bundle();
                bundle.putString("phoneNumber",sender);
                bundle.putString("body",body);
                bundle.putSerializable("date", date);
                startIntent.putExtras(bundle);
                startIntent.setAction("com.oneplus.setLocService");
                context.startService(startIntent);
                //deleteSMS(context, sender, originalBody);
                startIntent = new Intent(context, DeleteSMSService.class);
                startIntent.putExtra("phoneNumber",sender);
                startIntent.putExtra("body",originalBody);
                startIntent.setAction("com.oneplus.deleteSMSService");
                context.startService(startIntent);
                abortBroadcast();   //中断广播
            }
        }
    }

}
