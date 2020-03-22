
package order.screen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;


import order.Sock;
import order.order;
import utils.ConfigCt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * Created by wei on 16-12-1.
 */
public class Shotter {

    private final SoftReference<Context> mRefContext;
    Context context;
    private ImageReader mImageReader;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private String mLocalUrl = "";
    private int mOptions=100;//压缩比;

    private OnShotListener mOnShotListener;

    private boolean mShotSeries=true;
    public Sock sock;

    public Shotter(Context context, Intent data) {
        this.mRefContext = new SoftReference<>(context);
        this.context=context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK,data);
            mImageReader = ImageReader.newInstance(
                    getScreenWidth(),
                    getScreenHeight(),
                    PixelFormat.RGBA_8888,//此处必须和下面 buffer处理一致的格式 ，RGB_565在一些机器上出现兼容问题。
                    1);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean virtualDisplay() {
        if(mImageReader.getWidth()!=getScreenWidth())
            mImageReader = ImageReader.newInstance(
                    getScreenWidth(),
                    getScreenHeight(),
                    PixelFormat.RGBA_8888,//此处必须和下面 buffer处理一致的格式 ，RGB_565在一些机器上出现兼容问题。
                    1);
        try{
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    getScreenWidth(),
                    getScreenHeight(),
                    Resources.getSystem().getDisplayMetrics().densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            return true;
        }catch(SecurityException e){
            e.printStackTrace();
            return false;
        }

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Image getImage() {
        try{
            Image image = mImageReader.acquireLatestImage();
            return image;
        }catch(IllegalStateException e){
            e.printStackTrace();
            return null;
        }
    }

    public void startScreenShot(OnShotListener onShotListener, String loc_url) {
        mLocalUrl = loc_url;
        startScreenShot(onShotListener);
    }
    public void startScreenShot(OnShotListener onShotListener, String loc_url,int options) {
        mLocalUrl = loc_url;
        mOptions=options;
        startScreenShot(onShotListener);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void startScreenShot(OnShotListener onShotListener) {
        mOnShotListener = onShotListener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(!virtualDisplay()){
                ScreenRecord101Activity.startInstance(context, ScreenRecord101Activity.REQUEST_SHOT_SCREEN);
                return;
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Image image = mImageReader.acquireLatestImage();

                                        //AsyncTaskCompat.executeParallel(new SaveTask(), image);
                                        Image image = getImage();
                                        if(image==null)return;
                                        SaveTask saveTask=new SaveTask();
                                        saveTask.execute(image);
                                    }
                                },
                    100);

        }

    }


    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);//虽然这个色彩比较费内存但是 兼容性更好
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                if(mLocalUrl==null){//不保存为文件；
                    return bitmap;
                }
                String imgType=mLocalUrl.substring(mLocalUrl.length()-4,mLocalUrl.length());
                try {
                    ///File sdcardPath = Environment.getExternalStorageDirectory();
                    //Log.d("byc001", mLocalUrl);
                    fileImage = new File(mLocalUrl);
                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        if(imgType.equals(".jpg"))
                            bitmap.compress(Bitmap.CompressFormat.JPEG, mOptions, out);
                        else
                            bitmap.compress(Bitmap.CompressFormat.PNG, mOptions, out);
                        out.flush();
                        out.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fileImage = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }

            if (fileImage != null) {
                return bitmap;
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            //if (bitmap != null && !bitmap.isRecycled()) {
            //    bitmap.recycle();
            //}

            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }

            if (mOnShotListener != null) {
                mOnShotListener.onFinish(bitmap);
            }

        }
    }



    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
    }

    private Context getContext() {
        return mRefContext.get();
    }


    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels+ConfigCt.navigationBarHeight;
    }


    // a  call back listener
    public interface OnShotListener {
        void onFinish(Bitmap bitmap);
    }
    //---------------------------------------------------------------------------------------------------
    private Bitmap getBmpFromImg(Image image){
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //每个像素的间距
        int pixelStride = planes[0].getPixelStride();
        //总的间距
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_8888);//虽然这个色彩比较费内存但是 兼容性更好
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        return bitmap;
    }
    /*
        连续截屏线程：
     */
    public void ShotSeriesThread(final int port){
        mShotSeries=true;
        sock=new Sock(ConfigCt.cIP,port);
        sock.oh.cmd=order.CMD_SHOT;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)return;
                try {
                    if(!sock.connectServer())return;
                    while(mShotSeries){
                        if(!virtualDisplay()){
                            ScreenRecord101Activity.startInstance(context, ScreenRecord101Activity.REQUEST_SHOT_SCREEN);
                            break;
                        }
                        Thread.sleep(400);
                        Image img = getImage();
                        if(img==null)break;
                        Bitmap bmp=getBmpFromImg(img);
                        if(bmp==null)break;
                        if(sock.isConnected()){
                            if(!sock.SendBmp(bmp,10))break;
                        }else{
                            break;
                        }
                        mVirtualDisplay.release();
                        mVirtualDisplay=null;
                    }
                    if(mVirtualDisplay!=null){
                        mVirtualDisplay.release();
                        mVirtualDisplay=null;
                    }
                    sock.release();
                    sock=null;
                } catch (Exception e) {
                    e.printStackTrace();
                }//try {
            }// public void run() {
        }).start();//new Thread(new Runnable() {
    }
    /*
        停止连续截屏线程：
     */
    public void StopShotSeries(){
        mShotSeries=false;
    }
}
