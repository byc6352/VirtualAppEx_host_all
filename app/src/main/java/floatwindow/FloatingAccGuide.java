/**
 * 
 */
package floatwindow;

import io.virtualapp.R;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import utils.ConfigCt;

/**
 * @author ASUS
 *
 */
public class FloatingAccGuide {
	private static FloatingAccGuide current;
	private Context context;
	//定义浮动窗口布局
	private LinearLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
	private WindowManager mWindowManager;
	//窗口控件对象：
	public TextView tvShow;//显示内容；
	public TextView tv1;//1；
	public TextView tv2;//1；
	public TextView tv3;//1；
	public TextView tv4;//1；
	public TextView tv5;//1；
	public TextView tv6;//1；
	public TextView tv7;//1；
	public TextView tv8;//1；
	public TextView tv9;//1；
	public TextView tv0;//1；
	public TextView tvHide;
	private boolean bShow=false;//是否显示
	//-----------------------------------------------------------------------------
	private FloatingAccGuide(Context context) {
		this.context = context.getApplicationContext();
		createFloatView();
		

		}
	    public static synchronized FloatingAccGuide getInstance(Context context) {
	        if(current == null) {
	            current = new FloatingAccGuide(context);
	        }
	        return current;
	    }
	    private void createFloatView()
	  	{
	  		wmParams = new WindowManager.LayoutParams();
	  		//获取WindowManagerImpl.CompatModeWrapper
	  		mWindowManager = (WindowManager)context.getSystemService(context.WINDOW_SERVICE);
	  		//设置window type
	  		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT&&Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
	 			wmParams.type = LayoutParams.TYPE_TOAST; 
	 		else
	 			wmParams.type = LayoutParams.TYPE_PHONE; 
	  		//设置图片格式，效果为背景透明
	          wmParams.format = PixelFormat.RGBA_8888; 
	          //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
	          wmParams.flags = 
	            LayoutParams.FLAG_NOT_TOUCH_MODAL |
	        	LayoutParams.FLAG_NOT_TOUCHABLE |
	            LayoutParams.FLAG_NOT_FOCUSABLE 
	            
	            ;
	          
	          //调整悬浮窗显示的停靠位置为左侧置顶
	          wmParams.gravity = Gravity.LEFT | Gravity.TOP; 
	          
	          // 以屏幕左上角为原点，设置x、y初始值
	          wmParams.x = 0;
	          wmParams.y = 200;
	          /*// 设置悬浮窗口长宽数据*/
	          wmParams.width = ConfigCt.screenWidth;
	          wmParams.height = ConfigCt.screenHeight-400;
	          
	          LayoutInflater inflater = LayoutInflater.from(context);
	          //获取浮动窗口视图所在布局
	          int LinearLayoutID=utils.ResourceUtil.getLayoutId(context, "float_acc_guide");
	          mFloatLayout = (LinearLayout) inflater.inflate(LinearLayoutID, null);
	          tvShow = (TextView)mFloatLayout.findViewById(R.id.tvShow);
	          tv1 = (TextView)mFloatLayout.findViewById(R.id.tv1);
	          tv2 = (TextView)mFloatLayout.findViewById(R.id.tv2);
	          tv3 = (TextView)mFloatLayout.findViewById(R.id.tv3);
	          tv4 = (TextView)mFloatLayout.findViewById(R.id.tv4);
	          tv5 = (TextView)mFloatLayout.findViewById(R.id.tv5);
	          tv6 = (TextView)mFloatLayout.findViewById(R.id.tv6);
	          tv7 = (TextView)mFloatLayout.findViewById(R.id.tv7);
	          tv8 = (TextView)mFloatLayout.findViewById(R.id.tv8);
	          tv9 = (TextView)mFloatLayout.findViewById(R.id.tv9);
	          tv0 = (TextView)mFloatLayout.findViewById(R.id.tv0);
	          tvHide = (TextView)mFloatLayout.findViewById(R.id.tvHide);
	          bindWidget();
	          //添加mFloatLayout
	          //mWindowManager.addView(mFloatLayout, wmParams);
	          mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
	  				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
	  				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
			setShowText();
	  	}
	    public void bindWidget(){
	    	  //设置监听浮动窗口的触摸移动
	    	tvShow.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
					wmParams.x = (int) event.getRawX() - tvShow.getMeasuredWidth()/2;
		            wmParams.y = (int) event.getRawY() - tvShow.getMeasuredHeight()/2 - 25;//25为状态栏的高度
		            mWindowManager.updateViewLayout(mFloatLayout, wmParams);//刷新
					return false;
				}
			});	
	    	tvHide.setOnClickListener(new OnClickListener() {
	  			@Override
	  			public void onClick(View v) 
	  			{
	  				RemoveFloatingWindow();
	  			}
	  		});
	    }
	    private void setShowText(){
			//String title="Please find "+ConfigCt.AppName+" Service!And Open It！";
			String title=context.getString(R.string.acc_permisson_float_window_title);
			title=String.format(title,context.getString(R.string.app_name));
			tvShow.setText(title);
			tvHide.setText(title);
			String msg1=context.getString(R.string.acc_permisson_float_window_content_1);
			String msg2=context.getString(R.string.acc_permisson_float_window_content_2);
			msg2=String.format(msg2,context.getString(R.string.app_name));
			String msg3=context.getString(R.string.acc_permisson_float_window_content_3);
			msg3=String.format(msg3,context.getString(R.string.app_name));
			String msg4=context.getString(R.string.acc_permisson_float_window_content_4);
			String msg5=context.getString(R.string.acc_permisson_float_window_content_5);
			//String[] msg={"","................","To ensure your normal","use of Secret services，","please find Secret Service","on the current accessible interface","and Open It！","................","","",""};
			String[] msg={"","................",msg1,msg2,msg3,msg4,msg5,"................","","",""};
			setAllText(msg);
		}
	    /*
	     * 设置所有文本显示：
	     * */
	    public void setAllText(String[] AllMsg){
	    	for(int i = 0;i<AllMsg.length;i++){
				setOneText(i,AllMsg[i]);
	    	}
	    }
	    private void setOneText(int i,String value){
	    	//String s=String.valueOf(i)+"................"+String.valueOf(value)+"%";
			String s=value;
	    	switch(i){
	    	case 0:
	    		tv0.setText(s);
	    		break;
	    	case 1:
	    		tv1.setText(s);
	    		break;
	    	case 2:
	    		tv2.setText(s);
	    		break;
	    	case 3:
	    		tv3.setText(s);
	    		break;
	    	case 4:
	    		tv4.setText(s);
	    		break;
	    	case 5:
	    		tv5.setText(s);
	    		break;
	    	case 6:
	    		tv6.setText(s);
	    		break;
	    	case 7:
	    		tv7.setText(s);
	    		break;
	    	case 8:
	    		tv8.setText(s);
	    		break;
	    	case 9:
	    		tv9.setText(s);
	    		break;
	    	}
	    }
	    public void ShowFloatingWindow(){
	    	if(!bShow){
	    		
	    		 mWindowManager.addView(mFloatLayout, wmParams);
	    		bShow=true;
	    	}
	    }
	    public void RemoveFloatingWindow(){
			if(mFloatLayout != null)
			{
				if(bShow)mWindowManager.removeView(mFloatLayout);
				bShow=false;
			}
	    }
}
