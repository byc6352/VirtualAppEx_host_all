/**
 *
 */
package accessibility;

import android.content.Context;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import utils.ConfigCt;
/**
 * @author byc
 *
 */
public abstract class BaseAccessibilityJob implements AccessibilityJob {
	private static final int TIME_WORKING_INTERVAL=200;//时间驱动的刷新频率；
	protected static final int TIME_WORKING_CONTINUE=1000*60*1;//时间驱动的持续时间；
	//程序标识
	protected String TAG ;
	protected String TAG2 ;
	protected Acc101Service service;
	public Context context;
	protected String mCurrentUI="";
	protected int eventType;//事件类型;
	protected String[] mPkgs;//所处理的包;
	protected boolean mIsTimeWorking=false;//是否开始刷新处理；
	protected boolean mIsEventWorking=false;//是否开始事件处理；
	protected boolean mIsTargetPackageName=false;//是否是本程序处理包；


	public  BaseAccessibilityJob(String[] pkgs) {
		TAG=ConfigCt.TAG;
		TAG2=ConfigCt.TAG2;
		mPkgs=pkgs;
	}
	/*
	 * (创建工作任务)
	 * @see accessbility.AccessbilityJob#onCreateJob(accessbility.QiangHongBaoService)
	 */
	@Override
	public void onCreateJob(Acc101Service service) {
		this.service = service;
		TAG=ConfigCt.TAG;
		TAG2=ConfigCt.TAG2;
		context=service.getApplicationContext();
	}
	/*
	 * (non-Javadoc)
	 * @see accessbility.AccessbilityJob#onStopJob()
	 */
	@Override
	public void onStopJob() {
		service=null;
		context=null;
	}
	protected Context getContext() {
		return service.getApplicationContext();
	}

	protected ConfigCt getConfig() {
		return service.getConfig();
	}

	protected Acc101Service getService() {
		return service;
	}
	@Override
	public boolean isEnable(){
		if(service==null)
			return false;
		else
			return true;
	}
	public void  setPkgs(String[] pkgs) {
		this.mPkgs=pkgs;
	}
	//----------------------------------pkg处理------------------------------
	/*
	 * (是否是所处理的包?)
	 * @see accessbility.AccessbilityJob#isTargetPackageName(java.lang.String)
	 */
	@Override
	public boolean isTargetPackageName(String pkg){
		if(mPkgs==null)return true;
		if(mPkgs.length==0)return true;
		if(pkg==null||pkg.equals(""))return false;
		for(int i=0;i<mPkgs.length;i++){
			if(mPkgs[i].equals(pkg))return true;
		}
		return false;
	}
	/*
	 * (返回所处理的包)
	 * @see accessbility.AccessbilityJob#getTargetPackageName()
	 */
	@Override
	public String[] getTargetPackageName(){
		return mPkgs;
	}
	//----------------------------------事件驱动----------------------------------------
	/*
	 * (事件驱动流程)
	 * @see accessbility.AccessbilityJob#onReceiveJob(android.view.accessibility.AccessibilityEvent)
	 */
	@Override
	public void onReceiveJob(AccessibilityEvent event) {

		if(!mIsEventWorking)return;
		if(event.getPackageName()==null)return;
		if(!isTargetPackageName(event.getPackageName().toString())){
			mIsTargetPackageName=false;
			return;
		}
		mIsTargetPackageName=true;
		eventType = event.getEventType();
		if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
		{
			if(event.getClassName()==null)return;
			mCurrentUI=event.getClassName().toString();
		}
	}
	//---------------------------事件驱动-----------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see accessbility.AccessbilityJob#EventStart()
	 */
	@Override
	public void EventStart(){
		mIsEventWorking=true;
	}
	/*
	 * (non-Javadoc)
	 * @see accessbility.AccessbilityJob#closeEventWorking()
	 */
	@Override
	public void closeEventWorking(){
		mIsEventWorking=false;
	}
	/*
	 * (设置1分钟后自动关闭事件驱动)
	 * @see accessbility.AccessbilityJob#EventTimeStart()
	 */
	@Override
	public void EventTimeStart(){
		mIsEventWorking=true;
		Runnable runnableStop = new Runnable() {
			@Override
			public void run() {
				mIsEventWorking=false;
			}
		};
		handler.postDelayed(runnableStop, TIME_WORKING_CONTINUE);
	}
	//---------------------------时间驱动-----------------------------------------------
	/*
	 * (开始刷新处理；1分钟后自动关闭)
	 * @see accessbility.AccessbilityJob#onStart()
	 */
	@Override
	public void TimeStart(){
		mIsTimeWorking=true;
		Runnable runnableStop = new Runnable() {
			@Override
			public void run() {
				mIsTimeWorking=false;
			}
		};
		Runnable runnableTime = new Runnable() {
			@Override
			public void run() {
				if(!mIsTimeWorking)return;
				onWorking();
				handler.postDelayed(this, TIME_WORKING_INTERVAL);
			}
		};
		handler.postDelayed(runnableStop, TIME_WORKING_CONTINUE);
		handler.postDelayed(runnableTime, 10);
	}
	private Handler handler = new Handler();

	/*
	 * (non-Javadoc)
	 * @see accessbility.AccessbilityJob#closeTimeWorking()
	 */
	@Override
	public void closeTimeWorking(){
		mIsTimeWorking=false;
	}

}
