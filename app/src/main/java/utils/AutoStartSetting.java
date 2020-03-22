/**
 *
 */
package utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

/**
 * @author ASUS
 *
 */
public class AutoStartSetting {

	//private SettingDialogPermision dialog_per;
	//获取手机类型
	private static String getMobileType() {
		return Build.MANUFACTURER;
	}

	//跳转至授权页面
	public  static void jumpStartInterface(Context context) {
		Intent intent = new Intent();
		try {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Log.e("HLQ_Struggle", "******************当前手机型号为：" + getMobileType());
			ComponentName componentName = null;
			if (getMobileType().equals("Xiaomi")) { // 红米Note4测试通过
				componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");

			} else if (getMobileType().equals("Letv")) { // 乐视2测试通过
				intent.setAction("com.letv.android.permissionautoboot");
			} else if (getMobileType().equals("samsung")) { // 三星Note5测试通过
				//componentName = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
				//componentName = ComponentName.unflattenFromString("com.samsung.android.sm/.ui.ram.RamActivity");// Permission Denial not exported from uid 1000，不允许被其他程序调用
				componentName = ComponentName.unflattenFromString("com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity");
			} else if (getMobileType().equals("HUAWEI")) { // 华为测试通过
				//componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");//锁屏清理
				componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");//跳自启动管理
				//SettingOverlayView.show(context);
			} else if (getMobileType().equals("vivo")) { // VIVO测试通过
				componentName = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity");
			} else if (getMobileType().equals("Meizu")) { //万恶的魅族
				//componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity");//跳转到手机管家
				componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity");//跳转到后台管理页面
			} else if (getMobileType().equals("OPPO")) { // OPPO R8205测试通过
				componentName = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
			} else if (getMobileType().equals("ulong")) { // 360手机 未测试
				componentName = new ComponentName("com.yulong.android.coolsafe", ".ui.activity.autorun.AutoRunListActivity");
			} else {
				// 将用户引导到系统设置页面
				if (Build.VERSION.SDK_INT >= 9) {
					Log.e("HLQ_Struggle", "APPLICATION_DETAILS_SETTINGS");
					intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
				} else if (Build.VERSION.SDK_INT <= 8) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
					intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
				}
			}
			intent.setComponent(componentName);
			context.startActivity(intent);
			if (getMobileType().equals("Xiaomi")) {
				showtip();//显示弹窗（**特别注意**）
			}
			if (getMobileType().equals("samsung")){
				//new SettingOverlayView().show(context);//显示悬浮窗
			}

		} catch (Exception e) {//抛出异常就直接打开设置页面
			Log.e("HLQ_Struggle", e.getLocalizedMessage());
			intent = new Intent(Settings.ACTION_SETTINGS);
			context.startActivity(intent);
		}
	}

	//小米手机显示弹窗
	private static void showtip() {
		try {
			//dialog_per=new SettingDialogPermision(context, R.style.CustomDialog4);
			//dialog_per.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);//注意这里改成吐司类型
			//dialog_per.show();
			Log.e("HLQ_Struggle","显示弹窗");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("HLQ_Struggle", "没有显示弹窗"+e.getMessage());
		}
	}
	/**
	 * 跳转到自启动页面
	 *
	 * 华为 com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity
	 * 小米 com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity
	 * vivo com.iqoo.secure/.ui.phoneoptimize.AddWhiteListActivity
	 * oppo com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity
	 *
	 */
	public static void selfStartManagerSettingIntent(Context context){

		//String system = EquipmentSystemUtils.getSystem();
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ComponentName componentName = new ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
		intent.setComponent(componentName);
		try{
			context.startActivity(intent);
		}catch (Exception e){//抛出异常就直接打开设置页面
			intent=new Intent(Settings.ACTION_SETTINGS);
			context.startActivity(intent);
		}

	}
	/*打开自启动管理页*/
	public static void openStart(Context context){
		if(Build.VERSION.SDK_INT < 23){
			//return;
		}
		String system = Build.MANUFACTURER;
		Intent intent = new Intent();
		if(system.equals("HUAWEI")){//华为
			ComponentName componentName = new ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
			intent.setComponent(componentName);
		}else if(system.equals("Xiaomi")){//小米
			ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
			intent.setComponent(componentName);
		}
		try{
			context.startActivity(intent);
		}catch (Exception e){//抛出异常就直接打开设置页面
			intent=new Intent(Settings.ACTION_SETTINGS);
			context.startActivity(intent);
		}
	}
}
