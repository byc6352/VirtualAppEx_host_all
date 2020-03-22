package lock;

import accessibility.Acc101Service;
import accessibility.app.ExeClick;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import order.order;
import permission.GivePermission;
import utils.ConfigCt;
import utils.RootShellCmd;
import utils.MyLog;

/**
 * @author ASUS
 *
 */
public class UnlockScreen {
    private static UnlockScreen current;//实例；
    private static final int SLIDE_DOWN_TO_UP=1;//从下到上滑动；
    private static final int SLIDE_UP_TO_DOWN=2;//从上到下滑动；
    private static final int SLIDE_LEFT_TO_RIGHT=3;//从左到右滑动；
    private static final int SLIDE_RIGHT_TO_LEFT=4;//从右到左滑动；
    private int mOrientation=SLIDE_DOWN_TO_UP;//滑动方向
    private Context context;
    private boolean bScreenOn=false;//是否亮屏；
    private boolean bScreenLocked=true;//是否锁屏；
    private IntentFilter filter;
    private BroadcastReceiver mBatInfoReceiver;

    private UnlockScreen(Context context) {
        this.context=context;
    }
    public static synchronized UnlockScreen getInstance(Context context) {
        if(current == null) {
            current = new UnlockScreen(context);
        }else{
            current.context=context;;
        }
        return current;
    }
    /**
     * 解锁屏幕
     */
    public void execUnlockScreen() {
        if(mBatInfoReceiver!=null){
            destoryUnlockScreen();
            return;
        }
        bScreenOn=isScreenOn(context);
        bScreenLocked=isScreenLocked(context);
        if(bScreenOn==false||bScreenLocked==true){//黑屏或者锁屏；

            ScreenReceiver(context);//1.注册监听事件；
            wakeUpAndUnlock(context);//2.执行解锁；
            endUnlockScreen();//3.5分钟后自动终止解锁；
        }
    }
    /**
     * 放弃解锁屏幕
     */
    public void destoryUnlockScreen() {
        if(mBatInfoReceiver!=null){
            context.unregisterReceiver(mBatInfoReceiver);
            mBatInfoReceiver=null;
            MyLog.d("unregisterReceiver(mBatInfoReceiver)");
        }
    }
    /**
     * 连续滑动屏幕
     * orientation滑动方向：SLIDE_DOWN_TO_UP,
     */
    private void seriesSlideScreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(int i=0;i<10;i++){
                        SlideScreen(mOrientation,i);
                        Thread.sleep(1000);

                    }
                    if(mOrientation==SLIDE_DOWN_TO_UP)
                        mOrientation=SLIDE_LEFT_TO_RIGHT;
                    else
                        mOrientation=SLIDE_DOWN_TO_UP;
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }).start();

    }
    /**
     * 滑动屏幕
     * orientation滑动方向：SLIDE_DOWN_TO_UP,
     */
    public void SlideScreen(int orientation,int count) {
        Points p=getPoints(orientation,count);
        //Log.d(ConfigCt.TAG, "正在滑动：起点="+p.start+"；终点="+p.end);
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.N){//
            if(Acc101Service.service!=null){
                Acc101Service.service.slideThread(p.start,p.end);
            }
        }else{
                if(Acc101Service.service!=null){
                    ExeClick.getInstance(order.CMD_SLIDE).slide(p.start,p.end);
                }
        }
    }
    /**
     * 计算起点和终点坐标
     * orientation滑动方向：SLIDE_DOWN_TO_UP,
     */
    private Points getPoints(int orientation,int count) {
        Points p=new Points();
        p.start=new Point();
        p.end=new Point();
        int h=ConfigCt.screenHeight;
        int w=ConfigCt.screenWidth;
        switch(orientation){
            case SLIDE_DOWN_TO_UP:
                p.start.x=w/2+count*10;
                p.start.y=h-200;
                p.end.x=w/2+count*10;
                p.end.y=h-500;
                break;
            case SLIDE_UP_TO_DOWN:
                p.start.x=w/2;
                p.start.y=100;
                p.end.x=w/2;
                p.end.y=h-100;
                break;
            case SLIDE_LEFT_TO_RIGHT:
                p.start.x=100;
                p.start.y=h/2+count*10;
                p.end.x=400;
                p.end.y=h/2+count*10;
                break;
            case SLIDE_RIGHT_TO_LEFT:
                p.start.x=w-100;
                p.start.y=h/2;
                p.end.x=100;
                p.end.y=h/2;
                break;
        }
        return p;
    }
    /**
     * 监听屏幕事件
     */
    private void ScreenReceiver(Context context){
        filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                Log.d(ConfigCt.TAG, "mBatInfoReceiver onReceive");
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    bScreenLocked=isScreenLocked(context);
                    if(bScreenLocked){
                        seriesSlideScreen();//从下到上滑动10次
                    }
                    Log.d(ConfigCt.TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    wakeUpAndUnlock(context);//2.执行解锁；
                    Log.d(ConfigCt.TAG, "screen off");
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    destoryUnlockScreen();
                    Log.d(ConfigCt.TAG, "screen unlock");
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.i(ConfigCt.TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                }
            }
        };
        Log.d(ConfigCt.TAG, "mBatInfoReceiver  registerReceiver");
        context.registerReceiver(mBatInfoReceiver, filter);
    }
    //------------------------------------------------------------------------------------------
    /*
     * 定义起点坐标和终点坐标
     * */
    public class Points {
        Point start;
        Point end;
    }
    //-----------------------------------------公共方法------------------------------------------
    /**
     * 判断是否黑屏
     * @param c
     * @return
     */
    public final static boolean isScreenOn(Context context) {

        //android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(c.KEYGUARD_SERVICE);
        //return !mKeyguardManager.inKeyguardRestrictedInputMode();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        return isScreenOn;
    }
    /**
     * 判断是否锁屏
     * @param c
     * @return
     */
    public final static boolean isScreenLocked(Context context) {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();


    }
    /**
     * 唤醒手机屏幕并解锁
     */
    public static void wakeUpAndUnlock(Context context) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }
    /*
     * 定时器：5分钟后自动终止解锁；
     */
    private void endUnlockScreen(){
        final Handler handler= new Handler();
        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                destoryUnlockScreen();
                //handler.postDelayed(this, 1000*5);
            }
        };
        handler.postDelayed(runnable, 1000*60*5);
    }
}
