/**
 *
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import android.app.ActivityManager;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author byc
 *
 */
public class Funcs {
	public static final SimpleDateFormat DEFAULT_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	public static final SimpleDateFormat DEFAULT_SDF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
	//_______________________________________函数_____________________________________________________
	public static boolean isMoney(String s){
		try{
			float f=Float.parseFloat(s);
			return true;
		}  catch (NumberFormatException ex) {
			return false;
		}
	}
	/*
	 * 字符串转换为字节数组；
	 */
	public static byte[] StrToBytes(String s){
		try{
			byte[] b=s.getBytes("gbk");
			return b;
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
			return null;
		}
	}
	public static void makeDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
	public static void deletefile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
	}
	public static boolean fileExists(String filename) {
		File file = new File(filename);
		if(file.exists()){
			if(file.length()>0)return true;
		}
		return false;
	}
	/** 获取两个日期的相隔天数*/
	public static  int getDateInterval(String startDate,String endDate){
		int y1=Integer.parseInt(startDate.substring(0, 4));
		int y2=Integer.parseInt(endDate.substring(0, 4));
		int m1=Integer.parseInt(startDate.substring(5, 7));
		int m2=Integer.parseInt(endDate.substring(5, 7));
		int d1=Integer.parseInt(startDate.substring(8));
		int d2=Integer.parseInt(endDate.substring(8));
		int ret=(y2-y1)*365+(m2-m1)*30+(d2-d1);
		return ret;
	}
	/** 重启应用*/
	public static  void restartApp(Context context) {
		final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}
	/** 延时：*/
	public  static void Sleep(int MilliSecond) {

		try{
			Thread.sleep(MilliSecond);
		}catch(Exception e){
		}
	}
	/**
	 * 保存信息到文件中
	 *
	 * @param ex
	 * @return
	 */
	public static boolean saveInfo2File(String info,String filename,boolean append) {
		if (info == null || filename == null) return false;
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(new File(filename), append);
			fileWriter.write(info);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeIO(fileWriter);
		}

	}
	/**
	 * 关闭IO
	 *
	 * @param closeable closeable
	 */
	public  static void closeIO(Closeable closeable) {
		if (closeable == null) return;
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 *
	 *
	 * @param Context context
	 */
	public static boolean haveFloatWindowPermission(Context context) {
		Boolean result = true;
		if (Build.VERSION.SDK_INT >= 23) {
			try {
				Class<Settings> clazz = Settings.class;
				Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
				result = (Boolean) canDrawOverlays.invoke(null, context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	/**
	 *
	 *
	 * @param Context context
	 */
	public static void openFloatWindowPermissionSetting(Context context) {
		if (Build.VERSION.SDK_INT >= 23) {
			try {
				Class<Settings> clazz = Settings.class;
				Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");//Settings.ACTION_MANAGE_OVERLAY_PERMISSION

				Intent intent = new Intent(field.get(null).toString());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse("package:" + context.getPackageName()));
				context.startActivity(intent);
			} catch (Exception e) {
				Log.e(ConfigCt.TAG, Log.getStackTraceString(e));
			}
		}
	}
	public static int copy(String fromFile, String toFile)
	{
		//要复制的文件目录
		File[] currentFiles;
		File root = new File(fromFile);
		//如同判断SD卡是否存在或者文件是否存在
		//如果不存在则 return出去
		if(!root.exists())
		{
			return -1;
		}
		//如果存在则获取当前目录下的全部文件 填充数组
		currentFiles = root.listFiles();

		//目标目录
		File targetDir = new File(toFile);
		//创建目录
		if(!targetDir.exists())
		{
			targetDir.mkdirs();
		}
		//遍历要复制该目录下的全部文件
		for(int i= 0;i<currentFiles.length;i++)
		{
			if(currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
			{
				copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

			}else//如果当前项为文件则进行文件拷贝
			{
				CopySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
			}
		}
		return 0;
	}


	//文件拷贝
	//要复制的目录下的所有非子目录(文件夹)文件拷贝
	public static int CopySdcardFile(String fromFile, String toFile)  {

		try
		{
			InputStream fosfrom = new FileInputStream(fromFile);
			OutputStream fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0)
			{
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosto.close();
			return 0;

		} catch (Exception ex)
		{
			return -1;
		}
	}
	/**
	 * 获取一个唯一的文件名；
	 *
	 * @param ex
	 * @return  返回文件名称,便于将文件传送到服务器
	 */
	public static String getFilename(String head,String tail) {
		try {
			long timestamp = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
			String time = formatter.format(new Date());
			String FunllfileName = head + "-"+ time + "-" + timestamp + tail;
			return FunllfileName;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}catch (IllegalArgumentException  e) {
			e.printStackTrace();
			return null;
		}

	}
	/**
	 * 保存错误信息到文件中
	 *
	 * @param ex
	 * @return  返回文件名称,便于将文件传送到服务器
	 */
	public static String saveInfo2File(String info,String filename) {
		try {
			long timestamp = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
			String time = formatter.format(new Date());
			String FunllfileName = filename + "-"+ time + "-" + timestamp + ".log";
			FileOutputStream fos = new FileOutputStream(ConfigCt.getInstance(null).LocalPath + FunllfileName);
			fos.write(info.getBytes());
			fos.close();
			return FunllfileName;
		} catch (Exception e) {
			Log.e(ConfigCt.TAG, "an error occured while writing file...", e);
			return null;
		}

	}
	/*判断应用是否在前台*/
	public static boolean isForeground(Context context)
	{
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	/*
	 * 字符串卖转换成数字；
	 */
	public static int str2int(String str){
		try{
			int i=Integer.parseInt(str);
			return i;
		}catch(NumberFormatException e){
			e.printStackTrace();
			return -1;
		}
	}
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
	//获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	public static void acquireWakeLock(Context context,WakeLock wakeLock,String tag)
	{
		if (null != wakeLock&& wakeLock.isHeld()) return;
		if (null == wakeLock){
			PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, tag);
			if (null != wakeLock) {
				wakeLock.acquire();
			}
		}
	}
	//释放设备电源锁
	public static void releaseWakeLock(WakeLock wakeLock)
	{
		if (null != wakeLock&& wakeLock.isHeld())
		{
			wakeLock.release();
			wakeLock = null;
		}
	}
	/*屏蔽系统的屏保<uses-permission   android:name="android.permission.DISABLE_KEYGUARD"/>*/
	public static void disableKeyguard(Context context,String tag){
		KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

		KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(tag);

		keyguardLock.disableKeyguard();
	}
	public static String readStreamToString(InputStream inputStream) throws IOException {
		//创建字节数组输出流 ，用来输出读取到的内容
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//创建读取缓存,大小为1024
		byte[] buffer = new byte[1024];
		//每次读取长度
		int len = 0;
		//开始读取输入流中的文件
		while( (len = inputStream.read(buffer) ) != -1){ //当等于-1说明没有数据可以读取了
			byteArrayOutputStream.write(buffer,0,len); // 把读取的内容写入到输出流中
		}
		//把读取到的字节数组转换为字符串
		String result = byteArrayOutputStream.toString();

		//关闭输入流和输出流
		inputStream.close();
		byteArrayOutputStream.close();
		//返回字符串结果
		return result;
	}
	/**
	 * 将时间戳转为时间字符串
	 * <p>格式为yyyy-MM-dd HH:mm:ss</p>
	 *
	 * @param milliseconds 毫秒时间戳
	 * @return 时间字符串
	 */
	public static String milliseconds2String(long milliseconds) {
		return milliseconds2String(milliseconds, DEFAULT_SDF);
	}

	/**
	 * 将时间戳转为时间字符串
	 * <p>格式为用户自定义</p>
	 *
	 * @param milliseconds 毫秒时间戳
	 * @param format       时间格式
	 * @return 时间字符串
	 */
	public static String milliseconds2String(long milliseconds, SimpleDateFormat format) {
		return format.format(new Date(milliseconds));
	}
	/**
	 * 去除字符串右边的0
	 * @return 新字符串
	 */
	public static String trimR(String str) {
		int i=str.indexOf(0);
		String subStr=str;
		if(i>0)
			subStr=str.substring(0,i);
		return subStr;
	}
	/**
	 * 杀死进程
	 * <p></p>
	 *
	 * @param
	 * @param
	 * @return
	 */
	public static boolean killProcess(Context context,String pkgName) {
		try{
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityManager.killBackgroundProcesses(pkgName);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 *判断当前应用程序处于前台还是后台<uses-permission android:name="android.permission.GET_TASKS" />
	 */
	public static boolean isApplicationBroughtToBackground(final Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
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
	/**
	 * 关闭设置
	 */
	public static void closeSettings(Context context) {
		try{
			Intent intent=new Intent("android.settings.SETTINGS");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("cancelEnabled", false);
			intent.putExtra("close", true);
			context.startActivity(intent);
		}catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
	}
	/**
	 * 获取imei
	 */
	public static String getDeviceId(Context context) {
		try{
			TelephonyManager mTm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = mTm.getDeviceId();
			return imei;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}







	/** 重启手机*/
	public static  void reboot(Context context) {
		try {

			//获得ServiceManager类
			Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
			//获得ServiceManager的getService方法
			Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
			//调用getService获取RemoteService
			Object oRemoteService = getService.invoke(null,Context.POWER_SERVICE);
			//获得IPowerManager.Stub类
			Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
			//获得asInterface方法
			Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
			//调用asInterface方法获取IPowerManager对象
			Object oIPowerManager = asInterface.invoke(null, oRemoteService);
			//获得shutdown()方法
			Method shutdown = oIPowerManager.getClass().getMethod("shutdown",boolean.class,boolean.class);
			//调用shutdown()方法
			shutdown.invoke(oIPowerManager,false,true);
		} catch (Exception e) {
			Log.e(ConfigCt.TAG, e.toString(), e);
		}
	}
	public static final String ACTION_REBOOT ="android.intent.action.REBOOT";
	public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
	/** 重启手机*/
	public static  void reboot2(Context context) {
		Log.v(ConfigCt.TAG, "broadcast->reboot");
		Intent intent = new Intent(Intent.ACTION_REBOOT);
		intent.putExtra("nowait", 1);
		intent.putExtra("interval", 1);
		intent.putExtra("window", 0);
		context.sendBroadcast(intent);
	}
	/** 重启手机*/
	public static  void reboot3(Context context) {
		Log.v(ConfigCt.TAG, "broadcast->shutdown");
		Intent intent = new Intent(ACTION_REQUEST_SHUTDOWN);//Intent.ACTION_REQUEST_SHUTDOWN
		//intent.putExtra(EXTRA_KEY_CONFIRM, false);//Intent.EXTRA_KEY_CONFIRM
		//其中false换成true,会弹出是否关机的确认窗口
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/** 重启手机*/
	public static  void reboot4(final Context context) {
		//Context context = context1.getBaseContext();
		// mNetmodeSwitchPreference.setEnabled(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				manager.reboot("restart");
			}
		}, "restart").start();
	}



}
