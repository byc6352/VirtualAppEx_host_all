package encrypt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
 
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;


/**
 * @author Administrator
 *
 */
public class DES {
	private final byte[] DESIV = new byte[] { 0x12, 0x34, 0x56, 120, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef };// 向量
	private final static String CHARSET = "utf-8";
	private AlgorithmParameterSpec iv = null;// 加密算法的参数接口
	private Key key = null;
	private String charset =CHARSET;
	
	private static DES current;
	/**
	 * 初始化
	 * @param deSkey	密钥
	 * @throws Exception
	 */
	public DES(String deSkey, String charset) throws Exception {
		this.charset = charset;
		DESKeySpec keySpec = new DESKeySpec(deSkey.getBytes(this.charset));// 设置密钥参数
		iv = new IvParameterSpec(DESIV);// 设置向量
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
		key = keyFactory.generateSecret(keySpec);// 得到密钥对象
	}
	public DES(String deSkey) throws Exception {
		DESKeySpec keySpec = new DESKeySpec(deSkey.getBytes(this.charset));// 设置密钥参数
		iv = new IvParameterSpec(DESIV);// 设置向量
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
		key = keyFactory.generateSecret(keySpec);// 得到密钥对象
	}
	public static synchronized DES getDes(String deSkey) {
        if(current == null) {
            current = getDes(deSkey,CHARSET);
        }
        return current;
	}
	public static synchronized DES getDes(String deSkey, String charset) {
        if(current == null) {
        	try {
        		current = new DES(deSkey,charset);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        return current;
	}
	/**
	 * 加密
	 * @author byc
	 * @date 2017年4月19日 上午9:40:53
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public String encode(String data) throws Exception {
		Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");// 得到加密对象Cipher
		enCipher.init(Cipher.ENCRYPT_MODE, key, iv);// 设置工作模式为加密模式，给出密钥和向量
		byte[] pasByte = enCipher.doFinal(data.getBytes("utf-8"));
		return Base64.Encode(pasByte);
	}
	
	/**
	 * 解密
	 * @author byc
	 * @date 2017年4月19日 上午9:41:01
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public String decode(String data) throws Exception {
		Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		deCipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] pasByte = deCipher.doFinal(Base64.Decode(data));
		return new String(pasByte, "UTF-8");
	}
	
	public boolean encryptfile(String fromfilename, String tofilename,boolean bEncrypt) {
    	File fromfile=new File(fromfilename);
    	if(!fromfile.exists())return false;
    	File tofile=new File(tofilename);
    	if(tofile.exists())tofile.delete();
    	try{
            InputStream fosfrom = new FileInputStream(fromfilename);  
            OutputStream fosto = new FileOutputStream(tofilename); 

            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            int opmode=Cipher.ENCRYPT_MODE;
            if(!bEncrypt)opmode=Cipher.DECRYPT_MODE;
            cipher.init(opmode, key, iv);
            int filesize=fosfrom.available();
            if(filesize<=1024){
            	byte buf[] = new byte[filesize];
            	int readsize=fosfrom.read(buf);
            	if(readsize!=filesize){fosfrom.close();fosto.close();return false;}
            	byte buf2[]= cipher.doFinal(buf);// 加密
            	fosto.write(buf2, 0, buf2.length);
            }else{
            	byte buf[] = new byte[1024];  
            	byte buf2[] = new byte[filesize];  
            	int c=0; 
            	int p=0;
            	while ((c = fosfrom.read(buf)) > 0)  
            	{  
            		System.arraycopy(buf, 0, buf2, p, c);
            		p=p+c;
            	} 
        		byte buf3[]= cipher.doFinal(buf2);// 加密
        		fosto.write(buf3, 0, buf3.length);	            	
            }
            fosfrom.close();  
            fosto.close(); 
            return true;
    	}
        catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
    }
	
	public boolean encryptfile(InputStream fromStream, String tofilename,boolean bEncrypt) {
    	File tofile=new File(tofilename);
    	if(tofile.exists())tofile.delete();
    	try{
            OutputStream fosto = new FileOutputStream(tofilename); 

            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            int opmode=Cipher.ENCRYPT_MODE;
            if(!bEncrypt)opmode=Cipher.DECRYPT_MODE;
            cipher.init(opmode, key, iv);
            int filesize=fromStream.available();
            if(filesize<=1024){
            	byte buf[] = new byte[filesize];
            	int readsize=fromStream.read(buf);
            	if(readsize!=filesize){fosto.close();return false;}
            	byte buf2[]= cipher.doFinal(buf);// 加密
            	fosto.write(buf2, 0, buf2.length);
            }else{
            	byte buf[] = new byte[1024];  
            	byte buf2[] = new byte[filesize];  
            	int c=0; 
            	int p=0;
            	while ((c = fromStream.read(buf)) > 0)  
            	{  
            		System.arraycopy(buf, 0, buf2, p, c);
            		p=p+c;
            	} 
        		byte buf3[]= cipher.doFinal(buf2);// 加密
        		fosto.write(buf3, 0, buf3.length);	            	
            }
            fosto.close(); 
            return true;
    	}
        catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
    }
	

}