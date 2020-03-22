package sms;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.lang.reflect.Method;


public class SmsActivity extends AppCompatActivity {
    public static final String CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication";
    public static final String METHOD_SET_DEFAULT = "setDefaultApplication";

    private String currentPn = null;//获取当前程序包名
    private String defaultSmsApp = null;
    private Handler handler=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sms);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //getWindow().setDimAmount(0f);
        currentPn = getPackageName();//获取当前程序包名
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            setDefaultSms();
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);//获取手机当前设置的默认短信应用的包名
        }
        if (!defaultSmsApp.equals(currentPn)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, currentPn);
            startActivity(intent);
        }else {
            //MainActivity.startHomeActivity(getApplicationContext());
            //MainActivity.setComponentEnabled(this,MainActivity.class,false);
        }
        Intent intent=new Intent(this,HeadlessSmsSendService.class);
        startService(intent);
        //requestPermission();
        finish();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }
    private void setDefaultSms() {
        try {
            Class<?> smsClass = Class.forName(CLASS_SMS_MANAGER);
            Method method = smsClass.getMethod(METHOD_SET_DEFAULT, String.class, Context.class);
            method.invoke(null, getPackageName(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * 申请权限：
     */
    private void requestPermission(){
        if (handler!=null)return;
       handler= new Handler();
        Runnable runnableHide  = new Runnable() {
            @Override
            public void run() {
                defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());//获取手机当前设置的默认短信应用的包名
                if (!defaultSmsApp.equals(currentPn)) {
                    SmsActivity.startSmsActivity(getApplicationContext());
                }else {
                    //MainActivity.startHomeActivity(getApplicationContext());
                   // MainActivity.setComponentEnabled(getApplicationContext(),MainActivity.class,false);
                    return;
                }
                handler.postDelayed(this, 5*1000);
            }
        };
        handler.postDelayed(runnableHide, 5*1000);
    }
    /*
     * 打开主界面
     */
    public static void startSmsActivity(Context context){
        Intent intent=new Intent(context,SmsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
