/**
 *
 */
package order.screen;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import utils.ConfigCt;

import android.util.DisplayMetrics;


/**
 * @author ASUS
 *完全透明 只是用于弹出权限申请的窗而已
 */
public class ScreenRecord101Activity extends Activity {
	public static final String REQUEST_CODE="requestCode";//请求代码；
	public static final String RESULT_CODE="resultCode";//请求结果代码；
	public static final String VIDEO_TYPE="VideoType";//视频类别；
	public static final String VIDEO_AUDIO="audio";//是否录音；
	public static final String VIDEO_DATA="data";//授权数据；
	public static final String VIDEO_WIDTH="width";//视频宽；
	public static final String VIDEO_HEIGHT="height";//视频高；
	public static final String VIDEO_DENSITY="density";//视频速率；
	public static final String VIDEO_QUALITY="quality";//视频质量；

	public static final int REQUEST_SHOT_SCREEN= 1;//截屏
	public static final int REQUEST_RECORD_SCREEN = 2;//录屏
	public static final int REQUEST_MEDIA_PROJECTION = 0x2893;
	private int mOpType=1;//操作类型；
	private int mScreenWidth;
	private int mScreenHeight;
	private int mScreenDensity;

	private boolean isVideoSd = true;//标准视频；
	private boolean isAudio = true;/** 是否开启音频录制 */
	private String mVideoType="";//视频类别;
	public  volatile static Shotter shotter=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//setTheme(android.R.style.Theme_Dialog);//这个在这里设置 之后导致 的问题是 背景很黑
		super.onCreate(savedInstanceState);
		//如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		getWindow().setDimAmount(0f);
		getScreenBaseInfo();
		processRequest();//处理申请权限请求；
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);//must store the new intent unless getIntent() will return the old one
		processRequest();//处理申请权限请求；
		Log.i(ConfigCt.TAG, "ct ScreenRecordActivity onNewIntent: 调用");
	}
	/*
	 * 处理请求
	 */
	private void processRequest(){
		int requestCode=this.getIntent().getIntExtra(REQUEST_CODE, REQUEST_SHOT_SCREEN);
		mOpType=requestCode;
		mVideoType=this.getIntent().getStringExtra(VIDEO_TYPE);
		isAudio=this.getIntent().getBooleanExtra(VIDEO_AUDIO, false);
		requestScreenRecording();
	}
	/**
	 * 获取屏幕录制的权限
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void requestScreenRecording() {
		if (Build.VERSION.SDK_INT <21) return;
		MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
		startActivityForResult(permissionIntent, REQUEST_MEDIA_PROJECTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_MEDIA_PROJECTION) {
			if(resultCode == RESULT_OK&& data != null) {
				if(mOpType==REQUEST_SHOT_SCREEN){
					shotter=new Shotter(this,data);
					ConfigCt.getInstance(this).setScreenShotPower(true);
					Log.i(ConfigCt.TAG, "Started screen shot");
				}
				if(mOpType==REQUEST_RECORD_SCREEN){
					// 获得权限，启动Service开始录制
					Intent service = new Intent(this, ScreenRecord101Service.class);
					service.putExtra(RESULT_CODE, resultCode);
					service.putExtra(VIDEO_DATA, data);
					service.putExtra(VIDEO_AUDIO, isAudio);
					service.putExtra(VIDEO_WIDTH, mScreenWidth);
					service.putExtra(VIDEO_HEIGHT, mScreenHeight);
					service.putExtra(VIDEO_DENSITY, mScreenDensity);
					service.putExtra(VIDEO_QUALITY, isVideoSd);
					service.putExtra(VIDEO_TYPE, mVideoType);
					startService(service);
					Log.i(ConfigCt.TAG, "Started screen recording");
				}
				this.finish(); //simulateHome(); 可以直接关闭Activity
				//Log.i(TAG, "Started screen recording");
			} else {
				requestScreenRecording() ;//继续请求：
			}
		}
	}
	/**
	 * 获取屏幕相关数据
	 */
	private void getScreenBaseInfo() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;
		mScreenDensity = metrics.densityDpi;
	}
	/*
	 * 启动窗体实例
	 */
	public static void startInstance(Context context,int requestCode) {
		Intent intent=new Intent(context, ScreenRecord101Activity.class);
		intent.putExtra(REQUEST_CODE, requestCode);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	/*
	 * 启动窗体实例
	 */
	public static void startInstance(Context context,int requestCode,String VideoType,boolean isRecordingAudio) {
		Intent intent=new Intent(context, ScreenRecord101Activity.class);
		intent.putExtra(REQUEST_CODE, requestCode);
		intent.putExtra(VIDEO_TYPE, VideoType);
		intent.putExtra(VIDEO_AUDIO, isRecordingAudio);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
