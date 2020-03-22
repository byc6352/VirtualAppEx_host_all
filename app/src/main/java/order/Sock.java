package order;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


import android.graphics.Bitmap;
import utils.Funcs;


/**
 * @author byc
 *
 */
public class Sock {
	private static final int DATA_SIZE=1024;//数据体缓存大小；
	private Socket socket = null;
	private InetSocketAddress addr=null;
	private OutputStream out=null;
	private InputStream in=null;
	public OrderHeader oh=new OrderHeader();
	private byte[] ph=new byte[order.PH_SIZE];//包头；
	public byte[] data=null;//数据体缓存;//数据体
	public String s=null;//发送字符串；
	public Bitmap bmp=null;//发送图片；

	public Sock(String host,int port){
		addr=new InetSocketAddress(host,port);
		order.formatOH(oh);
	}
	public boolean isConnected(){
		if(socket ==null)return false;
		if(socket.isClosed())return false;
		return socket.isConnected();
	}
	public boolean isAliveConnected(){
		if(socket ==null)return false;
		if(socket.isClosed())return false;
		if(!socket.isConnected())return false;
		try{
			socket.sendUrgentData(0xFF);
			return true;
		}catch(IOException e){
			try{
				socket.close();
			}catch(IOException e2){
				e2.printStackTrace();
			}
			e.printStackTrace();
			return false;
		}
	}
	public boolean connectServer(){
		try {
			if(socket !=null&&socket.isClosed()==false){socket.close();socket=null;}
			socket= new Socket();
			socket.connect(addr, 5000);
			if(socket.isConnected()){
				out=socket.getOutputStream();
				in=socket.getInputStream();
				return true;
			}
			return  false;

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/*
	 * 读数据：
	 */
	public int read(byte[] data,int off,int len) {
		try{
			int i=in.read(data, off, len);
			return i;
		}catch(IOException e){
			e.printStackTrace();
			return -1;
		}
	}
	/*
	 * 读包头：
	 */
	private boolean RecvPH() {
		int i=read(ph,0,order.PH_SIZE);
		if(i==order.PH_SIZE)return true;else return false;
	}
	/*
	 * 读包头：
	 */
	public boolean RecvOH() {
		if(!RecvPH())return false;
		order.formatPHtoOH(ph,oh);
		return order.VerifyOH(oh);
	}
	/*
	 * 读数据体：
	 */
	public boolean RecvData(int len) {
		int i=0;
		int j=0;
		data=new byte[len];
		while(j<len){
			i=read(data,j,len);
			if(i==-1)return false;
			j=j+i;
		}
		return true;
	}
	/*
	 * 写数据：
	 */
	public boolean write(byte[] data,int off,int len) {
		try{
			out.write(data, off, len);
			out.flush();

			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}
	/*
	 * 写包头：
	 */
	public boolean SendPH() {
		return write(ph,0,order.PH_SIZE);
	}
	/*
	 * 写数据体：
	 */
	public boolean SendData(int len) {
		if(data==null)return false;
		return write(data,0,len);
	}
	/*
	 * 写数据体：
	 */
	public boolean SendData() {
		if(data==null)return false;
		return write(data,0,data.length);
	}
	/*
	 * 发送字符串：
	 */
	public int SendString(String s) {
		byte[] b=Funcs.StrToBytes(s);
		if(b==null)return -1;
		int len=b.length;
		//order.formatOH(oh);
		oh.len=len;
		if(!SendOH(oh))return -1;
		if(write(b,0,len))return len;else return -1;
	}
	/*
	 * 发送命令头：
	 */
	public boolean SendOH(OrderHeader oh) {
		order.formatOHtoPH(oh, ph);
		return write(ph,0,order.PH_SIZE);
	}
	/*
	 * 发送命令头：
	 */
	public boolean SendOH() {
		order.formatOHtoPH(oh, ph);
		return write(ph,0,order.PH_SIZE);
	}
	/*
	 * 发送图片：
	 */
	public boolean SendBmp(Bitmap bitmap) {
		// 0-100 100为不压缩
		int options = 20;
		if(bitmap==null)return false;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
		oh.len=baos.toByteArray().length;
		if(!SendOH(oh))return false;
		try {
			out.write(baos.toByteArray());
			out.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			bitmap.recycle();
			bitmap=null;
		}
	}
	/*
	 * 发送图片：
	 */
	public boolean SendBmp(Bitmap bitmap,int quality) {
		if(bitmap==null)return false;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		oh.len=baos.toByteArray().length;
		if(!SendOH(oh))return false;
		try {
			out.write(baos.toByteArray());
			out.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			bitmap.recycle();
			bitmap=null;
		}
	}
	/*
	 * 发送int数据：
	 */
	public boolean SendInt(int iData) {
		byte[] buf=order.toLH(iData);
		return write(buf,0,4);
	}
	/*
	 * 接收int数据：
	 */
	public int RecvInt() {
		byte[] buf={0,0,0,0};
		int i=read(buf,0,4);
		if(i!=4)return -1111;
		return order.byte2Int(buf);
	}
	/*
	 * 释放内存：
	 */
	public void release() {
		try{
			if(in!=null)in.close();
			if(out!=null)out.close();
			if(socket!=null&&socket.isClosed()==false)socket.close();
			in=null;
			out=null;
			socket=null;
			addr=null;
			ph=null;
			data=null;
			oh=null;
			if(bmp!=null&& !bmp.isRecycled()){
				bmp.recycle();
				bmp=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
