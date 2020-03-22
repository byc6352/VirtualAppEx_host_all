package sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HeadlessSmsSendService extends Service {
    private static final String TAG = "cyc001";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        //return new com.fyc.cp.ProcessConnection.Stub() {};
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService() executed");

        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}


