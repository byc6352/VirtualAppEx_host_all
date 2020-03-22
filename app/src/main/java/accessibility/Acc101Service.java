package accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.view.accessibility.AccessibilityManager;
import android.util.Log;

import io.virtualapp.home.HomeActivity;
import io.virtualapp.splash.SplashActivity;
import utils.ConfigCt;
import utils.ResourceUtil;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.widget.Toast;

import java.util.List;

import io.virtualapp.R;
import activity.Splash101Activity;

import java.util.Iterator;

import order.Order101Service;
/**
 * <p>Created by byc</p>
 * <p/>
 * 抢红包外挂服务
 */
public class Acc101Service extends AccessibilityService {
    //程序标识
    private static boolean mIsclick=true;
    private static Point mPosition;
    private static int mClickCount=0;//点击计数；
    private static boolean mIsWorkDoneByHand=false;//是否是手工打开服务；
    //public static final String WINDOW_LUCKYMONEY_RECEIVEUI="com.android.settings.Settings$AccessibilitySettingsActivity";
    //类实例对象
    public static Acc101Service service;
    //job对象
    private static ManageAccessibilityJob job;
    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        if(!mIsWorkDoneByHand)//
            if(Order101Service.getOrder101Service()==null)//
                Splash101Activity.startSplash101Activity(this);
        job=ManageAccessibilityJob.getJob();
        Log.d(ConfigCt.TAG, "qianghongbao service onCreate");
    }
    public ConfigCt getConfig() {
        return ConfigCt.getInstance(this);
    }
    public static Acc101Service getAcc101Service() {
        return service;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        job.onReceiveJob(event);
    }//public
    @Override
    public void onInterrupt() {
        Log.d(ConfigCt.TAG, "qianghongbao service interrupt");
        //Toast.makeText(this, "中断埋雷服务", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        job=ManageAccessibilityJob.getJob();
        job.onCreateJob(service);
        if(mIsWorkDoneByHand){
            AccessibilityHelper.performBack(service);
            HomeActivity.goHome(this);
            //Splash101Activity.startHomeActivity(this);
            //Toast.makeText(this, "已连接红包服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ConfigCt.ACTION_QIANGHONGBAO_SERVICE_CONNECT); //发送广播，已经连接上了
            sendBroadcast(intent);
        }
        mIsWorkDoneByHand=false;
        Log.d(ConfigCt.TAG, "qianghongbao service onServiceConnected");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        job.onStopJob();
        service = null;
        //job = null;
        //发送广播，已经断开辅助服务
        Intent intent = new Intent(ConfigCt.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        sendBroadcast(intent);
        Log.d(ConfigCt.TAG, "qianghongbao service destory");
    }


    /**
     * 发送请求授权广播
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void sendRequsetPermissionBroadCast(Context context) {
        //String servicename=context.getString(R.string.service_name);
        //String description=context.getString(R.string.accessibility_description);
        String servicename=context.getApplicationContext().getString(ResourceUtil.getStringId(context, "service_name"));
        String description=context.getApplicationContext().getString(ResourceUtil.getStringId(context, "accessibility_description"));
        Intent intent = new Intent(ConfigCt.ACTION_ACCESSBILITY_SERVICE_REQUEST);
        intent.putExtra("servicename", servicename);
        intent.putExtra("description", description);
        context.sendBroadcast(intent);
    }
    /**
     * 启动配置设置
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void startSetting(Context context) {
        mIsWorkDoneByHand=true;
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     * 判断当前服务是否正在运行
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if(info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if(!isConnect) {
            return false;
        }
        return true;
    }
    //--------------------------------------------------------------------------------------------------------------------------------------
    @TargetApi(24)
    public boolean pressLocation(final Point position){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)return false;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(position.x, position.y);
        //p.lineTo(position.x, position.y);
        //p.lineTo(position.x+10, position.y+10);
        builder.addStroke(new GestureDescription.StrokeDescription(p,10, 10));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                //if(Config.DEBUG)Toast.makeText(QiangHongBaoService.this, "Was it dispatched ok " , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                //if(Config.DEBUG)Toast.makeText(QiangHongBaoService.this, "Was it dispatched Cancelled" , Toast.LENGTH_SHORT).show();
                //pressLocation(position);
            }
        }, null);
        return isDispatched;
        //Toast.makeText(this, "Was it dispatched? " + isDispatched, Toast.LENGTH_SHORT).show();
    }
    @TargetApi(24)
    public boolean pressLongLocation(final Point position){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)return false;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(position.x, position.y);
        p.lineTo(position.x, position.y);
        //p.lineTo(position.x+10, position.y+10);
        builder.addStroke(new GestureDescription.StrokeDescription(p,100, 2000));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if(ConfigCt.DEBUG) Toast.makeText(Acc101Service.this, "Was it dispatched ok " , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                if(ConfigCt.DEBUG)Toast.makeText(Acc101Service.this, "Was it dispatched Cancelled" , Toast.LENGTH_SHORT).show();
                //pressLocation(position);
            }
        }, null);
        return isDispatched;
        //Toast.makeText(this, "Was it dispatched? " + isDispatched, Toast.LENGTH_SHORT).show();
    }
    Handler handlerClick = new Handler();
    Runnable runnableClick = new Runnable() {
        @Override
        public void run() {
            if(mIsclick){
                pressLocation(mPosition);
                mClickCount=mClickCount+1;
                handlerClick.postDelayed(this, 1000*1);
                Intent intent = new Intent(ConfigCt.ACTION_ACCESSBILITY_SERVICE_CLICK);
                intent.putExtra("count", mClickCount);
                sendBroadcast(intent);
            }
        }
    };
    public void startClick(final Point position){
        mIsclick=true;
        mClickCount=0;
        Acc101Service.mPosition=position;
        handlerClick.postDelayed(runnableClick, 100);
        return ;
    }
    public void closeClick(){
        mClickCount=0;
        mIsclick=false;
    }
    public int clickCount(){
        return  mClickCount;
    }
    //---------------------------------------------------------------------------------------------------
	/*
		滑动线程：
	 */
    public void slideThread(final Point p1,final Point p2){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    slide(p1,p2);
                } catch (Exception e) {
                    e.printStackTrace();
                }//try {
            }// public void run() {
        }).start();//new Thread(new Runnable() {
    }
    @TargetApi(24)
    public boolean slide(final Point p1,final Point p2){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)return false;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        //p.lineTo(position.x+10, position.y+10);
        builder.addStroke(new GestureDescription.StrokeDescription(path,100L,1000L));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if(ConfigCt.DEBUG)Toast.makeText(Acc101Service.this, "Was it dispatched ok " , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                if(ConfigCt.DEBUG)Toast.makeText(Acc101Service.this, "Was it dispatched Cancelled" , Toast.LENGTH_SHORT).show();
            }
        }, null);
        return isDispatched;
    }
}


/*
if(Config.DEBUG){
String pkg=this.getPackageName().toString();
Log.i("byc003", pkg);
if(AppUtils.isAppRunning(this, pkg))
	Log.i("byc003", "isAppRunning=true");
else{
	Intent intent=new Intent(this,MainActivity.class);
   	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   	startActivity(intent);
	Log.i("byc003", "isAppRunning=false");
}
}
*/
