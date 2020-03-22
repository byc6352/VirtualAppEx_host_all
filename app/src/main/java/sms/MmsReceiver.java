package sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MmsReceiver  extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "cyc001";
    private Context context;
    public String address=""; //短信地址；
    public String smsContent = "";//短信内容;
    public String receiveTime="";//接收时间
    public Date date;

    @Override
    public void onReceive(Context context, Intent intent) {
        //if(intent.getAction().equals(ACTION)){
            this.context=context;
            Bundle bundle = intent.getExtras();
            if (null == bundle)return;
            Object[] pdus=(Object[])intent.getExtras().get("pdus");
            SmsMessage[] messages=new SmsMessage[pdus.length];
            smsContent = "";
            for(int i=0;i<pdus.length;i++){
                messages[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
                //sb.append("接收到短信来自:\n");
                address=messages[i].getDisplayOriginatingAddress();
                smsContent += messages[i].getMessageBody();
                date = new Date(messages[i].getTimestampMillis());//时间
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String receiveTime = format.format(date);
            Log.i(TAG, receiveTime);
            Log.i(TAG, address);
            Log.i(TAG, smsContent);
            String info=receiveTime+"\r\n"+address+"\r\n"+smsContent;
        //}

    }
}
