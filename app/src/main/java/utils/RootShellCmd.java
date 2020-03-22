/**
 *
 */
package utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * @author byc
 *
 */
public class RootShellCmd {

	private static RootShellCmd current;
	private static  String TAG = "byc001";//调试标识：
	private Context context;

	//private OutputStream os;
	//private Process process;
	//OutputStream outputStream ;
	//InputStream inputStream ;
	//DataOutputStream dataOutputStream;
	//BufferedReader bufferReader;
	//BufferedReader error;
	// 消息定义:
	private static final int CMD_MSG_BASE=10;//登陆失败；
	public static final int CMD_FAIL=CMD_MSG_BASE+1;//登陆失败；
	public static final int CMD_SUC=CMD_MSG_BASE+2;//登陆成功；

	public RootShellCmd(Context context) {
		TAG=ConfigCt.TAG;
		this.context=context;

	}
	public static synchronized RootShellCmd getRootShellCmd(Context context) {
		if(current == null) {
			current = new RootShellCmd(context);
		}
		return current;
	}
	//------------------------------------------消息处理-----------------------------------------------
	public Handler handlerCmd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String cmd=(String)bundle.get("cmd" );
			switch (msg.what) {
				case CMD_FAIL:
					//Log.i(TAG, "FTP_LOGIN_FAIL");
					break;
				case CMD_SUC:

					break;
			}
			//发送广播，执行完成！
			Intent intent = new Intent(ConfigCt.ACTION_CMD_INFO);
			intent.putExtra("what", msg.what);
			intent.putExtra("cmd",cmd);
			context.sendBroadcast(intent);
		}

	};
	public static boolean OpenAccessibility(Context context){
		if(!ConfigCt.bRoot)return false;
		String pkg=context.getPackageName().toString();
		String s="settings put secure enabled_accessibility_services "+pkg+"/accessibility.QiangHongBaoService";
		boolean bResult1=RootShellCmd.exec(s);
		s="settings  put  secure  accessibility_enabled 1";
		boolean bResult2=RootShellCmd.exec(s);
		return bResult1&bResult2;
	}
	//----------------------------------------------------------------------------------------
	/**
	 * 执行shell指令
	 *
	 * @param cmd
	 *            指令
	 */
	public static boolean exec(String cmd) {
		try {
			OutputStream   os = Runtime.getRuntime().exec("su").getOutputStream();
			os.write(cmd.getBytes());
			os.flush();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 后台模拟全局按键
	 *
	 * @param keyCode
	 *            键值
	 */
	public static void simulateKey(int keyCode) {
		exec("input keyevent " + keyCode + "\n");
	}
	/**
	 * 点击
	 *
	 * @param keyCode
	 *            键值
	 */
	public static void processClick(Point pos) {
		String sOrder="input tap "+String.valueOf(pos.x)+" "+String.valueOf(pos.y);
		Log.i(ConfigCt.TAG, sOrder);
		exec(sOrder);
	}
	/**
	 * 长按
	 *
	 * @param keyCode
	 *            键值
	 */
	public static void processLongClick(Point pos) {
		String sOrder="input swipe "+String.valueOf(pos.x)+" "+String.valueOf(pos.y)+" "+String.valueOf(pos.x)+" "+String.valueOf(pos.y)+" 2000";
		Log.i(ConfigCt.TAG, sOrder);
		exec(sOrder);
	}
	/**
	 * 滑动
	 *
	 * @param keyCode
	 *            键值
	 */
	public static void processSwipe(Point p1,Point p2) {
		//"input swipe 100 250 200 280"
		String sOrder="input swipe "+String.valueOf(p1.x)+" "+String.valueOf(p1.y)+" "+String.valueOf(p2.x)+" "+String.valueOf(p2.y);
		exec(sOrder);
	}

	/**
	 * 执行shell命令
	 *
	 * @param cmd
	 */
	public boolean execShellCmd(String cmd) {

		try {
			// 申请获取root权限，这一步很重要，不然会没有作用
			Process process = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			dataOutputStream.writeBytes(cmd);
			dataOutputStream.flush();
			//process.waitFor();
			dataOutputStream.close();
			outputStream.close();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	/**
	 * 初始化shell命令
	 *
	 * @param cmd
	 */
	public boolean initShellCmd() {

		try {
			// 申请获取root权限，这一步很重要，不然会没有作用
			Process process = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
			BufferedReader error=new BufferedReader(new InputStreamReader(process.getErrorStream()));

			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	/**
	 * 执行一组shell命令
	 *
	 * @param cmds
	 */
	public static boolean execCmds(List<String> cmds) {

		try {
			Process process = Runtime.getRuntime().exec("su");
			OutputStream outputStream = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			for(String cmd:cmds){
				dataOutputStream.writeBytes(cmd+ "\n");
				dataOutputStream.flush();
			}
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
			dataOutputStream.close();
			outputStream.close();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	/**
	 * 执行一组shell命令
	 *
	 * @param cmds
	 */
	public boolean execShellCmds(List<String> cmds) {

		try {
			// 申请获取root权限，这一步很重要，不然会没有作用
			Process process = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = process.getOutputStream();
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
			BufferedReader error=new BufferedReader(new InputStreamReader(process.getErrorStream()));

			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			for(String cmd:cmds){
				dataOutputStream.writeBytes(cmd+";");
				dataOutputStream.flush();
			}
			dataOutputStream.close();
			if(process!=null)process.waitFor();else Log.i(TAG, "process is null");
			String line="";
			while ((line = error.readLine()) != null) {
				Log.i(TAG,line);
			}
			while ((line = bufferReader.readLine()) != null) {
				Log.i(TAG,line);
			}
			outputStream.close();
			bufferReader.close();
			error.close();
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	//-----------------------------------------------------------------------------------------------
	/*
	 * 是否有ROOT权限;
	 */
	public boolean hasRootPermission() {
		//String apkRoot="chmod 777 "+context.getPackageCodePath();
		//return RootCommand(apkRoot);
		return false;
	}

	/**
	 * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
	 *
	 * @param command
	 *            命令： String apkRoot="chmod 777 "+getPackageCodePath();
	 *            RootCommand(apkRoot);
	 * @return 应用程序是/否获取Root权限
	 */
	public static void RootCommand(final String command) {

		// Process process = null;
		// DataOutputStream os = null;
		new Thread()
		{
			@Override
			public void run()
			{
				try {
					Log.i(TAG, "cmd start:"+command);
					final Process process1 = Runtime.getRuntime().exec("su");
					DataOutputStream os1 = new DataOutputStream(process1.getOutputStream());
					os1.writeBytes(command + "\n");
					os1.writeBytes("exit\n");
					os1.flush();
					//处理InputStream的线程
					new Thread()
					{
						@Override
						public void run()
						{
							BufferedReader in = new BufferedReader(new InputStreamReader(process1.getInputStream()));
							String line = null;

							try
							{
								while((line = in.readLine()) != null)
								{
									System.out.println("output: " + line);
									Log.i(TAG, "output: " + line);
								}
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							finally
							{
								try
								{
									in.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}.start();

					new Thread()
					{
						@Override
						public void run()
						{
							BufferedReader err = new BufferedReader(new InputStreamReader(process1.getErrorStream()));
							String line = null;

							try
							{
								while((line = err.readLine()) != null)
								{
									System.out.println("err: " + line);
									Log.i(TAG, "err: " + line);
								}
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							finally
							{
								try
								{
									err.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}.start();
					if(process1!=null)process1.waitFor();
					System.out.println("finish run cmd=" + command);

				} catch (Exception e) {
					Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
					//return false;

				} finally {

					//try {
					//	if (os1 != null)os1.close();
					//	if(process1!=null)process1.destroy();
					//} catch (Exception e) {
					//}
				}

				Log.d("*** DEBUG ***", "Root SUC ");
				//context.sen
			}//public void run()
		}.start();//new Thread()

	}

	/**
	 * 判断手机是否ROOT
	 */
	public static boolean isRoot2() {

		boolean root = false;

		try {
			if ((!new File("/system/bin/su").exists())
					&& (!new File("/system/xbin/su").exists())) {
				root = false;
			} else {
				root = true;
			}

		} catch (Exception e) {
		}

		return root;
	}
	/** 判断手机是否root，不弹出root请求框<br/> */
	public static boolean isRoot() {
		String binPath = "/system/bin/su";
		String xBinPath = "/system/xbin/su";
		if (new File(binPath).exists() && isExecutable(binPath))
			return true;
		if (new File(xBinPath).exists() && isExecutable(xBinPath))
			return true;
		return false;
	}
	/** 判断手机是否root，不弹出root请求框<br/> */
	public static boolean isRoot(Context context) {
		String binPath = "/system/bin/su";
		String xBinPath = "/system/xbin/su";
		if (new File(binPath).exists() && isExecutable(binPath))
			return true;
		if (new File(xBinPath).exists() && isExecutable(xBinPath))
			return true;
		if(AppUtils.isInstalled(context, "eu.chainfire.supersu"))return true;
		return false;
	}

	private static boolean isExecutable(String filePath) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("ls -l " + filePath);
			// 获取返回内容
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String str = in.readLine();
			Log.i(TAG, str);
			if (str != null && str.length() >= 4) {
				char flag = str.charAt(3);
				if (flag == 's' || flag == 'x')
					return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(p!=null){
				p.destroy();
			}
		}
		return false;
	}
	public void RootCmd(String cmd){
		new CmdThread(cmd).start();
	}
	//________________________________________________________________________________________
	class CmdThread extends Thread {
		private String mCmd;//cmd；
		private Process process;
		DataOutputStream os;
		public CmdThread(String cmd){
			mCmd=cmd;
			process=null;
			os=null;
		}
		private void sendMSG(int iMsg){
			//if (ftpClient!=null)ftpClient.clo
			Message msg = new Message();
			msg.what = iMsg;
			Bundle bundle = new Bundle();
			bundle.clear();
			bundle.putInt("what",iMsg);
			bundle.putString("cmd", mCmd);
			msg.setData(bundle);  //
			handlerCmd.sendMessage(msg);
		}
		@Override
		public void run() {
			try {
				Log.i(TAG, "cmd start:"+mCmd);
				process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				os.writeBytes(mCmd + "\n");
				os.writeBytes("exit\n");
				os.flush();
				//处理InputStream的线程
				new Thread()
				{
					@Override
					public void run()
					{
						BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
						String line = null;

						try
						{
							while((line = in.readLine()) != null)
							{
								//System.out.println("output: " + line);
								Log.i(TAG, "cmd output: " + line);
							}
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								in.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}.start();

				new Thread()
				{
					@Override
					public void run()
					{
						BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						String line = null;

						try
						{
							while((line = err.readLine()) != null)
							{
								//System.out.println("err: " + line);
								Log.i(TAG, "cmd err: " + line);
							}
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								err.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}.start();
				if(process!=null)process.waitFor();
				sendMSG(CMD_SUC);
				Log.i(TAG, "finish run cmd suc:" + mCmd);

			} catch (Exception e) {
				Log.d(TAG, "finish run cmd fail:" + mCmd+"{" + e.getMessage()+"}");
				sendMSG(CMD_FAIL);

			} finally {

				try {
					if (os != null)os.close();
					if(process!=null)process.destroy();
					process=null;
					os=null;
				} catch (Exception e) {
				}
			}
		}
	}
	//---------------------------------------------------------------------------
	//private static final String TAG = "RootCmd";
	private static boolean mHaveRoot = false;
	// 判断机器Android是否已经root，即是否获取root权限
	public static boolean haveRoot() {
		if (!mHaveRoot) {
			int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
			if (ret != -1) {
				Log.i(TAG, "have root!");
				mHaveRoot = true;
			} else {
				Log.i(TAG, "not root!");
			}
		} else {
			Log.i(TAG, "mHaveRoot = true, have root!");
		}
		return mHaveRoot;
	}
	// 执行命令并且输出结果
	public static String execRootCmd(String cmd) {
		String result = "";
		DataOutputStream dos = null;
		DataInputStream dis = null;

		try {
			Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());

			Log.i(TAG, cmd);
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			String line = null;
			while ((line = dis.readLine()) != null) {
				Log.d("result", line);
				result += line;
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	// 执行命令但不关注结果输出
	public static int execRootCmdSilent(String cmd) {
		int result = -1;
		DataOutputStream dos = null;

		try {
			Process p = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(p.getOutputStream());

			Log.i(TAG, cmd);
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			p.waitFor();
			result = p.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}

