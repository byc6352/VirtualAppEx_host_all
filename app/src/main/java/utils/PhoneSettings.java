/**
 *
 */
package utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

/**
 * @author Administrator
 *<uses-permission android:name="android.permission.WRITE_SETTINGS"/>
 */
public class PhoneSettings {
	public static final int SCREEN_OFF_TIMEOUT=1000*60*30;//30分钟；
	private static PhoneSettings current;
	public  static Context context;
	private PhoneSettings(Context context) {
		PhoneSettings.context=context;
		setScreenOffTimeOut(context,SCREEN_OFF_TIMEOUT);
		setWifiSleepNever(context);
	}
	public static synchronized PhoneSettings getInstance(Context context) {
		if(current == null) {
			current = new PhoneSettings(context.getApplicationContext());
		}
		return current;
	}
	/*
	 * 设置自动休眠时间；
	 * */
	public static void setScreenOffTimeOut(Context context,int timeOut){
		try{
			Settings.System.putInt(context.getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT,timeOut);
		}catch (Exception localException){
			localException.printStackTrace();
		}
	}
	/*
	 * 设置WIFI不休眠；
	 * */
	public static void setWifiSleepNever(Context context){
		try{
			Settings.System.putInt(context.getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
		}catch (Exception localException){
			localException.printStackTrace();
		}
	}
	/*
	 * 设置屏幕亮度0~255；
	 * */
	public static void setScreenBrightness(Context context,int bright){
		ContentResolver resolver = context.getContentResolver();
		Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
		try{
			Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, bright);
			resolver.notifyChange(uri, null);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
