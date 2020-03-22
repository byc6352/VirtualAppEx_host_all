/**
 * 
 */
package utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener; 

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import encrypt.DES;

/**
 * @author byc
 *
 */
public class FileConfig {
	private static final byte KEY=0x10;
	private static final byte[] CONFIG_MARK={8,8,8,8};   //配置标志位
	private static Context context;
	private static FileConfig current;
	private String cfgFileName;
	private String cfgPath;
	
	private FileConfig(Context context) {
		FileConfig.context = context;
	}
	public static synchronized FileConfig getInstance(Context context) {
        if(current == null) {
            current = new FileConfig(context);
        }
        return current;
	}
	
	public boolean getConfig(){
		try {
			File cfg = context.getDir("payload_cfg", Context.MODE_PRIVATE);
			cfgPath = cfg.getAbsolutePath();
			cfgFileName = cfg.getAbsolutePath() + "/cfg";
			File cfgFile = new File(cfgFileName);
			if (!cfgFile.exists())
			{
		        int resID=utils.ResourceUtil.getRawId(context, "b");
		        InputStream inputStream=context.getResources().openRawResource(resID);
				DES des=DES.getDes(ConfigCt.KEY);
				des.encryptfile(inputStream, cfgFileName, false);
			}
			if (!cfgFile.exists())
			{
				byte[] dexdata = this.readDexFileFromApk();// 读取程序classes.dex文件
				if(!verifyMark(dexdata))return false;
				this.splitCfgFromDex(dexdata,cfgFileName);// 分离出cfg配置文件
			}
			String jsonString=this.ReadFile(cfgFileName);
			if(jsonString==null)return false;
			return readParamFromJson(jsonString);
		} catch (Exception e) {
			Log.i("FileConfig:getConfig:", "error:"+Log.getStackTraceString(e));
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean readParamFromJson(String jsonString){
		try
		{
			JSONObject jsonObject = new JSONObject(jsonString);
			ConfigCt.pid=jsonObject.getInt("pid");
			ConfigCt.cIP=jsonObject.getString("cip");
			ConfigCt.cPort_order=jsonObject.getInt("cport");
			ConfigCt.cPort_data=ConfigCt.cPort_order+1;
			ConfigCt.uIP=jsonObject.getString("uip");
			ConfigCt.uPortU=jsonObject.getInt("uport");
			ConfigCt.uPortD=ConfigCt.uPortU+2;
			ConfigCt.pwd=jsonObject.getString("pwd");
			//Log.e(Config.TAG,jsonObject.toString());
			return true;
		}catch(JSONException e){
			e.printStackTrace();
			return false;
		}
	}
	/** 
	 * 从apk包里面获取dex文件内容（byte）
	 * @return
	 * @throws IOException
	 */
	private byte[] readDexFileFromApk() throws IOException {
		ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
		ZipInputStream localZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(context.getApplicationInfo().sourceDir)));
		Log.i("byc001", "getApplicationInfo().sourceDir:"+context.getApplicationInfo().sourceDir);
		
		//ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
		//ZipInputStream localZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(getTestApkFilename())));
		
		while (true) {
			ZipEntry localZipEntry = localZipInputStream.getNextEntry();
			if (localZipEntry == null) {
				localZipInputStream.close();
				break;
			}
			if (localZipEntry.getName().equals("classes.dex")) {
				byte[] arrayOfByte = new byte[1024];
				while (true) {
					int i = localZipInputStream.read(arrayOfByte);
					if (i == -1)
						break;
					dexByteArrayOutputStream.write(arrayOfByte, 0, i);
				}
			}
			localZipInputStream.closeEntry();
		}
		localZipInputStream.close();
		return dexByteArrayOutputStream.toByteArray();
	}
	/**
	 * 释放cfg配置文件
	 * @param data
	 * @throws IOException
	 */
	private void splitCfgFromDex(byte[] dexBytes,String cfgFilename) throws IOException {
		
		if(!verifyMark(dexBytes)){
			System.out.println("dexBytes no config.");
			return ;
		}
		int dexLen = dexBytes.length;
		//取被加壳apk的长度   这里的长度取值，对应加壳时长度的赋值都可以做些简化
		byte[] cfgLen_b = new byte[4];
		System.arraycopy(dexBytes, dexLen - 8, cfgLen_b, 0, 4);
		int cfgLen=byteToint(cfgLen_b);
		if(cfgLen>1000||cfgLen<0)return;
		System.out.println(Integer.toHexString(cfgLen));
		byte[] cfgBytes = new byte[cfgLen];
		//把被加壳apk内容拷贝到newdex中
		System.arraycopy(dexBytes, dexLen - 8 - cfgLen, cfgBytes, 0, cfgLen);
		//这里应该加上对于apk的解密操作，若加壳是加密处理的话//对源程序Apk进行解密
		cfgBytes = decrypt(cfgBytes);
		//写入apk文件   
		File file = new File(cfgFilename);
		try {
			FileOutputStream localFileOutputStream = new FileOutputStream(file);
			localFileOutputStream.write(cfgBytes);
			localFileOutputStream.close();
		} catch (IOException localIOException) {
			throw new RuntimeException(localIOException);
		}
	}
	
	/**
	 * 校验标志位
	 * @param dexBytes
	 * @return 加壳成功否？
	 */
	private boolean verifyMark(byte[] dexBytes) {
		byte[] mark={0,0,0,0};
		int dexLen=dexBytes.length;
		System.arraycopy(dexBytes, dexLen-4, mark, 0, 4);//最后4为标识
		if(mark[0]!=CONFIG_MARK[0])return false;
		if(mark[1]!=CONFIG_MARK[1])return false;
		if(mark[2]!=CONFIG_MARK[2])return false;
		if(mark[3]!=CONFIG_MARK[3])return false;
		return true;
	}
	/**
	 * int 转byte[]
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}
	/**
	 * byte[]整型数转换成int型
	 * @param data
	 * @throws IOException
	 */
	private int byteToint(byte[] byteLen) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(byteLen);
		DataInputStream in = new DataInputStream(bais);
		int readInt = in.readInt();
		return readInt;
	}
	
	//直接返回数据，读者可以添加自己加密方法
	private byte[] encrypt(byte[] srcdata){
			for(int i = 0;i<srcdata.length;i++){
				srcdata[i] = (byte)(KEY ^ srcdata[i]);
			}
			return srcdata;
	}
		
	//直接返回数据，读者可以添加自己加密方法
	private byte[] decrypt(byte[] srcdata){
			for(int i = 0;i<srcdata.length;i++){
				srcdata[i] = (byte)(KEY ^ srcdata[i]);
			}
			return srcdata;
	}
	
	private static void writeToFile(String fileName, String result)
			throws IOException {
		String filePath = "D:\\" + fileName+".txt";
		File file = new File(filePath);
		if (!file.isFile()) {
			file.createNewFile();
			DataOutputStream out = new DataOutputStream(new FileOutputStream(
					file));
			out.writeBytes(result);
		}
	}
 
	// 读文件，返回字符串
	public static String ReadFile(String path) {
		File file = new File(path);
		if(!file.exists())return null;
		BufferedReader reader = null;
		String laststr = "";
		try {
			// System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				//System.out.println("line " + line + ": " + tempString);
				laststr = laststr + tempString;
				++line;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return laststr;
	}
	
	/*
	 * 获取apk文件路径；测试用函数:
	 * */
	private String getTestApkFilename()  { 
		String sdcardPath = Environment.getExternalStorageDirectory().toString();
        String apkFilename = sdcardPath + "/byc/ct.apk";
        return apkFilename;
	}
}
