/**
 *
 */
package accessibility.app;

import accessibility.Acc101Service;
import accessibility.BaseAccessibilityJob;

import android.app.Notification;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.TextView;
import utils.ConfigCt;
import utils.Funcs;

/**
 * @author ASUS
 *
 */
public class AccessibilitySaveNotification extends BaseAccessibilityJob {
	private static AccessibilitySaveNotification current;
	private String mFilename;
	private AccessibilitySaveNotification() {
		super(null);

	}
	public static synchronized AccessibilitySaveNotification getInstance() {
		if(current == null) {
			current = new AccessibilitySaveNotification();
		}
		return current;
	}
	@Override
	public void onCreateJob(Acc101Service service) {
		super.onCreateJob(service);
		EventStart();
		mFilename=getNotificationFileName();
	}
	@Override
	public void onStopJob() {
		super.onStopJob();

	}
	/*
	 * (刷新处理流程)
	 * @see accessbility.AccessbilityJob#onWorking()
	 */
	@Override
	public void onWorking(){

	}
	@Override
	public void onReceiveJob(AccessibilityEvent event) {
		super.onReceiveJob(event);
		if(!mIsEventWorking)return;
		if(!mIsTargetPackageName)return;
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
		{
			Parcelable data = event.getParcelableData();  //获取Parcelable对象
			if (data instanceof Notification) {//判断是否是Notification对象
				Notification notification = (Notification) data;
				String pkg=event.getPackageName().toString();
				String sTime=Funcs.milliseconds2String( System.currentTimeMillis());
				String text = String.valueOf(notification.tickerText);
				String info=sTime+"("+pkg+")\r\n"+text+"\r\n";
				if(ConfigCt.DEBUG)Log.i(TAG, text);
				Funcs.saveInfo2File(info,mFilename,true);
				//AnalyzeView(notification.contentView,pkg);
			}
		}
	}
	/*
	 * 获取通知文件名；
	 * */
	private String getNotificationFileName(){
		//if(ConfigCt.LocalPath.equals(""))ConfigCt.getInstance(context).getLocalDir();
		String filename=ConfigCt.LocalPath+ConfigCt.appID+"sbn.log";
		return filename;
	}
	private void AnalyzeView(RemoteViews remoteView, String packName) {
		try {
			//把RemoteView apply后变成当前可以处理的View
			ViewGroup rootLayout=null;
			View v1 = remoteView.apply(context, rootLayout);
			//然后就是枚举处理这个View的内容
			EnumGroupViews(v1);
			//展示出来
			//rootLayout.addView(v1);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private void EnumGroupViews(View v1) {
		if(v1 instanceof ViewGroup){
			ViewGroup lav = (ViewGroup)v1;
			int lcCnt = lav.getChildCount();
			for(int i = 0; i < lcCnt; i++)
			{
				View c1 = lav.getChildAt(i);
				if(c1 instanceof ViewGroup)
					EnumGroupViews(c1);//递归处理GroupView
				else if(c1 instanceof TextView) {//TestView则解析里面文本内容
					TextView txt = (TextView)c1;
					String str = txt.getText().toString().trim();
					if(str.length() > 0)
					{
						//这里打印文本内容
					}
					Log.i(TAG, "TextView id:"+ txt.getId() + ".text:" + str);
				}else  {
					Log.w(TAG,"2 other layout:" + c1.toString());

				}
			}
		}
		else {
			Log.w(TAG,"1 other layout:" + v1.toString());
		}
	}
}
