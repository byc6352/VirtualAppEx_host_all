package lock;

/**
 *
 */

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import utils.MyLog;

/**
 * @author byc
 *
 */
public class AdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "AdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d(TAG, "onEnabled");
        //Toast.makeText(context, "激活成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.d(TAG, "onDisabled");
        //Toast.makeText(context, "取消激活", Toast.LENGTH_SHORT).show();
        //如果取消了激活就再次提示激活
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DeviceMethod.getInstance(context.getApplicationContext()).onActivate();
            }
        }, 3000);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive");
    }
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
           /* // 这里处理 不可编辑设备。这里可以造成死机状态
            Intent intent2 = new Intent(context, NoticeSetting.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
            context.stopService(intent);// 是否可以停止*/

        return "这是一个可选的消息，警告有关禁止用户的请求";
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        // 设备管理：密码己经改变
        //Toast.makeText(context, "设备管理：密码己经改变", Toast.LENGTH_SHORT).show();
        MyLog.i("设备管理：密码己经改变");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        // 设备管理：改变密码失败
        //Toast.makeText(context, "设备管理：改变密码失败", Toast.LENGTH_SHORT).show();
        MyLog.i("设备管理：改变密码失败");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        // 设备管理：改变密码成功
        //Toast.makeText(context, "设备管理：改变密码成功", Toast.LENGTH_SHORT).show();
        MyLog.i("设备管理：改变密码成功");
    }

}

