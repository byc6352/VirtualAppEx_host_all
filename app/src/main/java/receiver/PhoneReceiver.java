package receiver;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import media.AudioRecorder;
import utils.ConfigCt;

public class PhoneReceiver extends BroadcastReceiver {
	private static PhoneReceiver current;
	TelephonyManager tm;
	private PhoneReceiver(Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		context.registerReceiver(this, filter);
		tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);//设置一个监听器
	}
	public static synchronized PhoneReceiver getInstance(Context context) {
		if(current == null) {
			current = new PhoneReceiver(context.getApplicationContext());
		}
		return current;
	}

	@Override
	public void onReceive(Context context, Intent intent){
		Log.i(ConfigCt.TAG,"phone:action"+intent.getAction());
		if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){//如果是去电（拨出）
			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Log.i(ConfigCt.TAG,"phone:action:拨出:"+ phoneNumber);
			//setResultData(null);//挂断
			//setResultData("13229923589");//挂断
			AudioRecorder.getInstance().startRecording();
		}else{//查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电
			Log.i(ConfigCt.TAG,"phone:action:来电");


		}
	}
	private PhoneStateListener listener=new PhoneStateListener(){
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);//state 当前状态 incomingNumber,貌似没有去电的API
			Log.i(ConfigCt.TAG,"PhoneStateListener:来电:");
			switch(state){
				case TelephonyManager.CALL_STATE_IDLE:
					Log.i(ConfigCt.TAG,"挂断");
					AudioRecorder.getInstance().stopRecording();;
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					AudioRecorder.getInstance().startRecording();
					Log.i(ConfigCt.TAG,"接听");
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Log.i(ConfigCt.TAG,"响铃:来电号码"+incomingNumber);//输出来电号码
					break;
			}
		}
	};
}