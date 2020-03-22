/**
 *
 */
package order.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


import utils.ConfigCt;

import android.content.Context;
import android.os.Message;
import order.Order101Service;
import order.Order101Service.DataThread;
import order.Sock;
import order.order;
import utils.Funcs;



/**
 * @author byc
 *
 */
public class FileSystem {
	private static FileSystem current;
	private Context context;
	private static int mFileInfoLen=0;//文件信息长度;

	private FileSystem(Context context) {
		this.context = context;
		mFileInfoLen=8+order.MAX_FILE_NAME+order.MAX_TIME_STR;

	}
	public static synchronized FileSystem getFileSystem(Context context) {
		if(current == null) {
			current = new FileSystem(context);
		}
		return current;
	}
	//----------------------------------------------------数据转换-------------------------------------------------------------
	/*
	 * 格式化文件信息对象为字节流；流块大小：8+8+64;
	 */
	public byte[] FormatFileInfo(FileInfo fi){
		byte[] buf=new byte[mFileInfoLen];
		byte[] temp = null;
		temp=order.toLH(fi.nFileSizeLow);
		System.arraycopy(temp, 0, buf, 0, temp.length);
		temp=order.toLH(fi.nFileSizeHigh);
		System.arraycopy(temp, 0, buf, 4, temp.length);
		temp=Funcs.StrToBytes(fi.ftLastWriteTime);
		System.arraycopy(temp, 0, buf, 8, temp.length);
		temp=Funcs.StrToBytes(fi.cFileName);
		int len=temp.length;
		if(len>order.MAX_FILE_NAME)len=order.MAX_FILE_NAME;
		System.arraycopy(temp, 0, buf, 8+order.MAX_TIME_STR, temp.length);
		return buf;
	}
	/*
	 * 字节流转目录对象：
	 */
	public PhoneDir byte2PhoneDir(byte[] data){
		if(data==null)return null;
		byte[] tmp=new byte[4];
		System.arraycopy(data, 0, tmp,0, 4);
		int rootDir=order.byte2Int(tmp);
		String subDir=null;
		if(data.length>4){
			byte[] tmp2=new byte[data.length-4];
			System.arraycopy(data, 4, tmp2,0,data.length-4);
			subDir=new String(tmp2);
		}
		return new PhoneDir(rootDir,subDir);
	}
	/*
	 * 返回文件的全路径：
	 */
	public String getFullDir(int iRootDir,String subDir){
		String rootDir=null;//获取根目录：
		switch(iRootDir){
			case order.FILE_DIR_EX_SD:
				rootDir=FileUtils.getSDCardBaseDir();
				break;
			case order.FILE_DIR_PHOTO:
				rootDir=FileUtils.getPhotoDir();
				break;
			case order.FILE_DIR_ROOT:
				rootDir=FileUtils.getRootDir();
				break;
		}
		if(rootDir==null)return null;
		if(rootDir.equals("/")){
			if(subDir==null)
				return rootDir;
			else
				return rootDir+subDir;
		}else{
			if(subDir==null)
				return rootDir;
			else
				return rootDir+File.separator+subDir;
		}
	}
	/*
	 * 返回目录下的文件信息：结构体：
	 */
	public byte[] getDirInfo(int iRootDir,String subDir){
		File[] files=null;
		String Dir=getFullDir(iRootDir,subDir);
		File fDir=new File(Dir);
		if (!fDir.exists())return null;
		if (!fDir.isDirectory())return null;
		files=fDir.listFiles();
		int count=files.length;
		if(count==0)return null;
		FileInfo fi=new FileInfo();
		byte[] buf=new byte[count*mFileInfoLen];
		byte[] temp=null;
		int i=0;
		for (File file : files) {
			if (file.isDirectory())
				fi.nFileSizeLow=-1;
			else
				fi.nFileSizeLow=new Long(file.length()).intValue();
			fi.ftLastWriteTime=Funcs.milliseconds2String(file.lastModified());
			fi.cFileName=file.getName();
			temp = FormatFileInfo(fi);
			System.arraycopy(temp, 0, buf, i*mFileInfoLen, temp.length);
			i=i+1;
		}
		temp=null;
		return buf;
	}
	/*
	 * byte[]转换为TransFilesInfo
	 */
	public TransFilesInfo Byte2TransFilesInfo(final byte[] data){
		if(data==null)return null;
		if(data.length!=order.MAX_PATH*2+4*4)return null;
		TransFilesInfo tf=new TransFilesInfo();
		byte[] tmp=new byte[order.MAX_PATH];
		System.arraycopy(data, 0, tmp,0, order.MAX_PATH);
		tf.clientFile=new String(tmp);
		tf.clientFile=Funcs.trimR(tf.clientFile);
		//tmp=new byte[order.MAX_PATH];
		System.arraycopy(data, order.MAX_PATH, tmp,0, order.MAX_PATH);
		tf.serverFile=new String(tmp);
		tf.serverFile=Funcs.trimR(tf.serverFile);
		tmp=new byte[4];
		System.arraycopy(data, order.MAX_PATH*2, tmp,0, 4);
		tf.iRootDir=order.byte2Int(tmp);
		System.arraycopy(data, order.MAX_PATH*2+4, tmp,0, 4);
		int iTmp=order.byte2Int(tmp);
		tf.bUpLoad=(iTmp == 0x00) ? false : true;
		//tmp=new byte[4];
		System.arraycopy(data, order.MAX_PATH*2+8, tmp,0, 4);
		iTmp=order.byte2Int(tmp);
		tf.bFolder=(iTmp == 0x00) ? false : true;
		//tmp=new byte[4];
		System.arraycopy(data, order.MAX_PATH*2+12, tmp,0, 4);
		iTmp=order.byte2Int(tmp);
		tf.bCompleteDel=(iTmp == 0x00) ? false : true;
		return tf;
	}
	/*
	 * RequestFileInfo转换为byte[]
	 */
	public  byte[] RequestFileInfo2Byte(RequestFileInfo requestFileInfo){
		byte[] buf=new byte[order.MAX_PATH+4];
		byte[] tmp=Funcs.StrToBytes(requestFileInfo.fileName);
		System.arraycopy(tmp, 0, buf,0, tmp.length);
		byte[] tmp2={0,0,0,0};
		if(requestFileInfo.bUpLoad)tmp2[3]=1;
		System.arraycopy(tmp2,0, buf,order.MAX_PATH, tmp2.length);
		return buf;
	}
	//--------------------------------------------------执行操作命令-----------------------------------------------------
	/*
	 * 删除文件；
	 */
	public void processDelFile(final byte[] data){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PhoneDir phonDir=byte2PhoneDir(data);
					String filename=getFullDir(phonDir.iRootDir,phonDir.subDir);
					FileUtils.deleteFile(filename);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 发送子目录信息；
	 */
	public void processListFileInfo(final byte[] data){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(data==null)return;
					byte[] tmp=new byte[4];
					System.arraycopy(data, 0, tmp,0, 4);
					int rootDir=order.byte2Int(tmp);
					String subDir=null;
					if(data.length>4){
						byte[] tmp2=new byte[data.length-4];
						System.arraycopy(data, 4, tmp2,0,data.length-4);
						subDir=new String(tmp2);
					}
					byte[] fileListData=getDirInfo(rootDir,subDir);
					if(fileListData==null)return;
					Order101Service.DataThread dataThread= Order101Service.getOrder101Service().getDataThread();
					dataThread.sock.oh.cmd=order.CMD_FILE_LIST;
					dataThread.sock.data=fileListData;
					dataThread.sock.oh.len=fileListData.length;
					dataThread.start();
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 传输文件信息；
	 */
	public void processTransFiles(final byte[] data){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(data==null)return;
					TransFilesInfo transFilesInfo=Byte2TransFilesInfo(data);
					String clientFile=getFullDir(transFilesInfo.iRootDir,transFilesInfo.clientFile);
					transFilesInfo.clientFile=clientFile;
					if(transFilesInfo.bUpLoad){//上传到电脑：
						if(!FileUtils.fileExists(clientFile))return;
					}
					if(transFilesInfo.bFolder){//文件夹
						if(transFilesInfo.bUpLoad){//上传到电脑：
							ZipUtil.zip(clientFile, clientFile+".zip");
							transFilesInfo.clientFile=clientFile+".zip";
						}
					}else{
						//TransFileClientThread(pTF);
					}
					TransFileThread transFileThread=new TransFileThread(transFilesInfo);
					transFileThread.sock.oh.cmd=order.CMD_FILE_TRANS;
					transFileThread.start();
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	//------------------------------------------------------------------------------------------------------------------------------
	/*
	 * 数据传输线程：
	 */
	class TransFileThread extends Thread {
		private String host= "";
		private int port = 8101;//数据传输端口
		private Message msg= null;//消息对象;
		public Sock sock;
		public TransFilesInfo transFilesInfo=null;
		public RequestFileInfo requestFileInfo=null;
		public TransFileThread(TransFilesInfo transFilesInfo) {
			host=ConfigCt.cIP;
			port=ConfigCt.cPort_file;
			sock=new Sock(host,port);
			this.transFilesInfo=transFilesInfo;
		}
		@Override
		public void run() {
			try{
				File file = FileUtils.getFileByPath(transFilesInfo.clientFile);
				if(file==null){release();return;}
				int fileSize=(int)FileUtils.getFileSize(file,FileUtils.BYTE);
				if(transFilesInfo.bUpLoad){//上传文件；
					if(!FileUtils.isFileExists(file)){release();return;}
					if(fileSize==-1){release();return;}
					if(fileSize==0){release();return;}
				}else{
					if(fileSize==-1)fileSize=0;
				}
				if(sock.connectServer()){//连接服务器成功：
					requestFileInfo=new RequestFileInfo(transFilesInfo.serverFile,transFilesInfo.bUpLoad);
					sock.data=RequestFileInfo2Byte(requestFileInfo);
					sock.oh.len=sock.data.length;
					sock.SendOH();
					sock.SendData();
					sock.SendInt(fileSize);
					sock.SendInt(0);
					byte[] buf = new byte[1024];
					int len = -1;
					if(transFilesInfo.bUpLoad){//上传文件；
						int srvFileSize=sock.RecvInt();
						int srvFileSizeHigh=sock.RecvInt();
						if(srvFileSize==-1111||srvFileSizeHigh==-1111){release();return;}
						RandomAccessFile raf = FileUtils.getRandomFile(file,"r");
						if(raf==null){release();return;}
						raf.seek(srvFileSize);

						while((len = raf.read(buf)) != -1){
							sock.write(buf, 0, len);
						}
						raf.close();
						sleep(10000);
						buf=null;
						if(transFilesInfo.bCompleteDel)FileUtils.deleteFile(file);
					}else{//下载文件
						RandomAccessFile raf = FileUtils.getRandomFile(file,"rw");
						if(raf==null){release();return;}
						raf.seek(raf.length());
						while((len=sock.read(buf, 0, buf.length)) !=-1){
							raf.write(buf, 0, len);
						}
						raf.close();
						buf=null;
					}
				}else{//连接服务器失败！
					//dataThreadInfo.bSuc=false;
				}
			}catch(IOException e){
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			release();
		}

		/*
		 * 释放内存：
		 */
		private void release() {
			sock.release();
			sock=null;
			msg=null;
		}
	}
	//---------------------------------------------------------------------------------------------------------------------

	//---------------------------------------------------------------------------------------------------------------------
	/*
	 * 手机目录结构
	 * iRootDir根目录标识；
	 * subDir字目录或文件；
	 */
	public class PhoneDir{
		public int iRootDir;
		public String subDir;
		public PhoneDir(int iRootDir,String subDir){
			this.iRootDir=iRootDir;
			this.subDir=subDir;

		}
	}
	public class FileInfo{
		int nFileSizeLow;//大小；
		int nFileSizeHigh;//大小；
		String ftLastWriteTime;//时间；
		String cFileName;//文件名；
	}
	public class TransFilesInfo{
		String clientFile;
		String serverFile;
		int iRootDir;
		boolean bUpLoad;
		boolean bFolder;
		boolean bCompleteDel;
	}
	private class RequestFileInfo{
		String fileName;
		boolean bUpLoad;
		public RequestFileInfo(String fileName,boolean bUpLoad){
			this.fileName=fileName;
			this.bUpLoad=bUpLoad;

		}
	}
}
