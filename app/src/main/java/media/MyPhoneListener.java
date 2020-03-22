/**
 *
 */
package media;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import utils.ConfigCt;

/**
 * @author Administrator
 *<uses-permission android:name="android.permission.READ_PHONE_STATE" />
 */
public class MyPhoneListener extends PhoneStateListener {
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		try {
			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE: // 当前电话处于闲置状态
					AudioRecorder.getInstance().stopRecording();
					Log.i(ConfigCt.TAG,"当前电话处于闲置状态");
					break;
				case TelephonyManager.CALL_STATE_RINGING: // 当前电话处于零响状态
					Log.i(ConfigCt.TAG,"电话号码为 " + incomingNumber);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK: // 当前电话处于接听状态
					AudioRecorder.getInstance().startRecording();
					Log.i(ConfigCt.TAG,"当前电话处于通话状态");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onCallStateChanged(state, incomingNumber);
	}
}
