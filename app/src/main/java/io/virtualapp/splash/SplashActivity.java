package io.virtualapp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.FlurryROMCollector;
import io.virtualapp.home.HomeActivity;
import jonathanfinerty.once.Once;
import permission.FloatWindowManager;
import utils.ConfigCt;

public class SplashActivity extends VActivity {
    public static final int START_MODE_NORMAL=1;
    public static final int START_MODE_BACK=2; //后台
    private static int start_mode=START_MODE_NORMAL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        @SuppressWarnings("unused")
        boolean enterGuide = !Once.beenDone(Once.THIS_APP_INSTALL, VCommends.TAG_NEW_VERSION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (ConfigCt.getInstance(this).isAppHide())setComponentEnabled(this,SplashActivity.class,false);
        VUiKit.defer().when(() -> {
            if (!Once.beenDone("collect_flurry")) {
                FlurryROMCollector.startCollect();
                Once.markDone("collect_flurry");
            }
            long time = System.currentTimeMillis();
            doActionInThread();
            time = System.currentTimeMillis() - time;
            long delta = 3000L - time;
            if (delta > 0) {
                VUiKit.sleep(delta);
            }
        }).done((res) -> {
            openFloatWindow();
            //activity.Splash101Activity.startSplash101Activity(this);
            //HomeActivity.goHome(this);
            //finish();
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        //openFloatWindow();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在此处应该提交应该在当前用户会话之外保留的任何更改的地方
        //openFloatWindow();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在此处应该提交应该在当前用户会话之外保留的任何更改的地方
    }


    private void doActionInThread() {
        if (!VirtualCore.get().isEngineLaunched()) {
            VirtualCore.get().waitForEngine();
        }
    }

    private boolean openFloatWindow(){
        //if(FloatWindowManager.getInstance().applyOrShowFloatWindow(this))return true;
        //Toast.makeText(MainActivity.this, "已授予悬浮窗权限！", Toast.LENGTH_LONG).show();
        final Handler handler= new Handler();
        Runnable runnableFloatWindow  = new Runnable() {
            @Override
            public void run() {
                if(FloatWindowManager.getInstance().applyOrShowFloatWindow(SplashActivity.this)){
                    Log.i(ConfigCt.TAG,"SplashActivity Start.");
                    activity.Splash101Activity.startSplash101Activity(SplashActivity.this);
                    if (start_mode==START_MODE_NORMAL)
                      HomeActivity.goHome(SplashActivity.this);
                    SplashActivity.this.finish();
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnableFloatWindow, 1000);
        return false;
    }

    public static void StartApp(Context context)
    {
        start_mode=START_MODE_BACK;
        setComponentEnabled(context,SplashActivity.class,true);
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //android.os.Process.killProcess(android.os.Process.myPid());
    }
    /**
     控制图标显示和隐藏
     @param clazz
     @param enabled true:显示、 false：隐藏
     */
    public static void setComponentEnabled(Context context,Class<?> clazz, boolean enabled) {
        final ComponentName c = new ComponentName(context, clazz.getName());
        context.getPackageManager().setComponentEnabledSetting(c,enabled?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
    }
}
