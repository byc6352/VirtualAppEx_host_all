/**
 *
 */
package utils;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

/**
 * @author Administrator
 *
 */
public class BindConfig {
	private static final byte KEY=0x10;
	private static final byte[] CONFIG_MARK={8,8,8,8};   //配置标志位
	private static final String CLASSES_DEX="classes.dex";
	private static final String WORK_DIR="force";//工作目录；
	private static final String TXT_FILE="cfg.txt";//配置文件；

	public BindConfig(){

	}
	/*
	 * 对apk文件加壳，至dex文件；
	 * apkFile:要加壳的文件；shellFile:壳文件；
	 * @return 加壳成功否？
	 * */
	public boolean addCfgToDex(String cfgFilename,String dexFilename){
		try {
			File dexFile = new File(dexFilename);	//解客dex
			System.out.println("dexFile size:"+dexFile.length());
			byte[] dexArray = readFileBytes(dexFile);//以二进制形式读出dex
			if(verifyMark(dexArray)){
				System.out.println("dexFile already config.");
				return false;
			}
			File cfgFile = new File(cfgFilename);   //配置文件
			System.out.println("cfgFile size:"+cfgFile.length());
			byte[] cfgArray = encrypt(readFileBytes(cfgFile));//以二进制形式读出cfg，并进行加密处理//对cfg进行加密操作

			int cfgLen = cfgArray.length;
			int dexLen = dexArray.length;
			int totalLen = cfgLen + dexLen +4+4;//多出4字节是存放长度的。标志位
			byte[] newdex = new byte[totalLen]; // 申请了新的长度
			//添加解壳代码
			System.arraycopy(dexArray, 0, newdex, 0, dexLen);//先拷贝dex内容
			//添加加密后的解壳数据
			System.arraycopy(cfgArray, 0, newdex, dexLen, cfgLen);//再在dex内容后面拷贝apk的内容
			//添加解壳数据长度
			System.arraycopy(intToByte(cfgLen), 0, newdex, totalLen-8, 4);//为长度
			//添加解壳数据长度
			System.arraycopy(CONFIG_MARK, 0, newdex, totalLen-4, 4);//最后4为标识
			//修改DEX file size文件头
			fixFileSizeHeader(newdex);
			//修改DEX SHA1 文件头
			fixSHA1Header(newdex);
			//修改DEX CheckSum文件头
			fixCheckSumHeader(newdex);
			//保存
			dexFile.createNewFile();
			FileOutputStream localFileOutputStream = new FileOutputStream(dexFile);
			localFileOutputStream.write(newdex);
			localFileOutputStream.flush();
			localFileOutputStream.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
	 * 修改dex头，CheckSum 校验码
	 * @param dexBytes
	 */
	private void fixCheckSumHeader(byte[] dexBytes) {
		Adler32 adler = new Adler32();
		adler.update(dexBytes, 12, dexBytes.length - 12);//从12到文件末尾计算校验码
		long value = adler.getValue();
		int va = (int) value;
		byte[] newcs = intToByte(va);
		//高位在前，低位在前掉个个
		byte[] recs = new byte[4];
		for (int i = 0; i < 4; i++) {
			recs[i] = newcs[newcs.length - 1 - i];
			System.out.println(Integer.toHexString(newcs[i]));
		}
		System.arraycopy(recs, 0, dexBytes, 8, 4);//效验码赋值（8-11）
		System.out.println(Long.toHexString(value));
		System.out.println();
	}


	/**
	 * 修改dex头 sha1值
	 * @param dexBytes
	 * @throws NoSuchAlgorithmException
	 */
	private void fixSHA1Header(byte[] dexBytes)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(dexBytes, 32, dexBytes.length - 32);//从32为到结束计算sha--1
		byte[] newdt = md.digest();
		System.arraycopy(newdt, 0, dexBytes, 12, 20);//修改sha-1值（12-31）
		//输出sha-1值，可有可无
		String hexstr = "";
		for (int i = 0; i < newdt.length; i++) {
			hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		System.out.println(hexstr);
	}

	/**
	 * 修改dex头 file_size值
	 * @param dexBytes
	 */
	private void fixFileSizeHeader(byte[] dexBytes) {
		//新文件长度
		byte[] newfs = intToByte(dexBytes.length);
		System.out.println(Integer.toHexString(dexBytes.length));
		byte[] refs = new byte[4];
		//高位在前，低位在前掉个个
		for (int i = 0; i < 4; i++) {
			refs[i] = newfs[newfs.length - 1 - i];
			System.out.println(Integer.toHexString(newfs[i]));
		}
		System.arraycopy(refs, 0, dexBytes, 32, 4);//修改（32-35）
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
	/**
	 * 以二进制读出文件内容
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] readFileBytes(File file) throws IOException {
		byte[] arrayOfByte = new byte[1024];
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(file);
		while (true) {
			int i = fis.read(arrayOfByte);
			if (i != -1) {
				localByteArrayOutputStream.write(arrayOfByte, 0, i);
			} else {
				return localByteArrayOutputStream.toByteArray();
			}
		}
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
				System.out.println("line " + line + ": " + tempString);
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

}
