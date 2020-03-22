package order.screen;


import utils.ConfigCt;



/**
 * @author byc
 *
 */
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by wei on 16-9-18.
 * <p>
 * 完全透明 只是用于弹出权限申请的窗而已
 *
 */
public class ScreenShot101Activity extends Activity {

    private static final String REQUEST_CODE="requestCode";
    private static final String COMPONENT_NAME="ComponentName";
    public static final int REQUEST_MEDIA_PROJECTION = 0x2893;
    public static final int REQUEST_MEDIA_DEVICE = 0x2894;
    public static Shotter shotter=null;
    private static ComponentName mComponentName=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setDimAmount(0f);
        processRequest();//处理申请权限请求；

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processRequest();//处理申请权限请求；
        Log.i(ConfigCt.TAG, "ct ScreenShotActivity onNewIntent: 调用");
    }
    /*
     * 处理请求
     */
    private void processRequest(){
        int requestCode=this.getIntent().getIntExtra(REQUEST_CODE, REQUEST_MEDIA_PROJECTION);
        switch(requestCode){
            case REQUEST_MEDIA_PROJECTION:
                requestScreenShot();
                break;
            case REQUEST_MEDIA_DEVICE:
                //this.getIntent().getParcelableExtra(COMPONENT_NAME);
                if(mComponentName!=null)
                    requestDeviceManager(mComponentName);
                break;
        }

    }

    /*
     * 请求
     */
    public void requestScreenShot() {
        if (Build.VERSION.SDK_INT >= 21) {
            try{
                startActivityForResult(
                        ((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION
                );
            }catch(android.content.ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
        else
        {
            //toast("版本过低,无法截屏");
        }
    }
    /*
     * 请求
     */
    public void requestDeviceManager(ComponentName componentName) {
        mComponentName=componentName;
        //启动设备管理 - 在AndroidManifest.xml中设定相应过滤器
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        //权限列表
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        //描述
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活后才能运行本程序！");
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_MEDIA_DEVICE);
    }
    private void toast(String str) {
        Toast.makeText(ScreenShot101Activity.this,str,Toast.LENGTH_LONG).show();
    }
    /*
     * 启动窗体实例
     */
    public static void startInstance(Context context,int requestCode) {
        startInstance(context,requestCode,null);
    }
    /*
     * 启动窗体实例
     */
    public static void startInstance(Context context,int requestCode,ComponentName admin) {
        Intent intent=new Intent(context, ScreenShot101Activity.class);
        intent.putExtra(REQUEST_CODE, requestCode);
        //if(admin!=null)intent.putExtra(COMPONENT_NAME, admin);
        mComponentName=admin;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION: {
                if (resultCode == -1 && data != null) {
                    shotter=new Shotter(ScreenShot101Activity.this,data);
                    ConfigCt.getInstance(ScreenShot101Activity.this).setScreenShotPower(true);
                    finish();
                }else{
                    requestScreenShot();
                }
            }
            break;
            case REQUEST_MEDIA_DEVICE:{
                if ( resultCode == Activity.RESULT_OK){
                    //Intent intent=new Intent(this,LockService.class);
                    //startService(intent);
                } else{
                    requestDeviceManager(mComponentName); //
                }
            }
            break;
        }

    }


}
