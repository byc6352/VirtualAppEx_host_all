/**
 *
 */
package accessibility.app;

import accessibility.Acc101Service;
import accessibility.AccessibilityHelper;
import accessibility.BaseAccessibilityJob;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import order.order;

/**
 * @author ASUS
 *
 */
public class ExeClick  extends BaseAccessibilityJob {
	private static ExeClick current;
	private boolean bFind=false;//查找到
	private boolean bSuc=false;//点击成功
	private int mType=order.CMD_POS;//点击类型
	private ExeClick(int clickType) {
		super(null);
		mType=clickType;
	}
	public static synchronized ExeClick getInstance(int clickType) {
		if(current == null) {
			current = new ExeClick(clickType);
		}else{
			current.mType=clickType;
		}
		return current;
	}
	@Override
	public void onCreateJob(Acc101Service service) {
		super.onCreateJob(service);

	}
	@Override
	public void onStopJob() {
		super.onStopJob();

	}
	@Override
	public void onWorking(){

	}
	@Override
	public void onReceiveJob(AccessibilityEvent event) {
		super.onReceiveJob(event);
		if(!mIsEventWorking)return;
		if(!mIsTargetPackageName)return;

	}
	/*
	 * 点击线程
	 * */
	public void click(final Point pos){
		if (service==null)return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AccessibilityNodeInfo rootNode=service.getRootInActiveWindow();
					if(rootNode==null)return;
					bFind=false;
					bSuc=false;
					recycle(rootNode,pos);
					if(!bSuc){
						bFind=false;
						recyclePerent(rootNode,pos);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 滑动线程
	 * */
	public void slide(final Point pos1,final Point pos2){
		if (service==null)return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AccessibilityNodeInfo rootNode=service.getRootInActiveWindow();
					if(rootNode==null)return;
					bFind=false;
					bSuc=false;
					recycleScroll(rootNode,pos1,pos2);
				} catch (Exception e) {
					e.printStackTrace();
				}//try {
			}// public void run() {
		}).start();//new Thread(new Runnable() {
	}
	/*
	 * 查找并点击 子控件
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public  void recycle(AccessibilityNodeInfo info,Point pos) {
		if(bFind)return;
		if (info.getChildCount() == 0) {
			compAndClick(info,pos);
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycle(info.getChild(i),pos);
				}
			}
		}
	}
	/*
	 * 查找并点击 父控件
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public  void recyclePerent(AccessibilityNodeInfo info,Point pos) {
		if(bFind)return;
		if (info.getChildCount() == 0) {

		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recyclePerent(info.getChild(i),pos);
					if(info.isClickable())compAndClick(info,pos);
				}
			}
		}
	}
	/*
	 * 比较并点击
	 * */
	public  boolean compAndClick(AccessibilityNodeInfo info,Point pos) {
		Rect outBounds=new Rect();
		info.getBoundsInScreen(outBounds);
		//Log.i(ConfigCt.TAG, "outBounds:" + outBounds);
		if((outBounds.left<=pos.x&&pos.x<=outBounds.right)&&(outBounds.top<=pos.y&&pos.y<=outBounds.bottom)){
			if(mType==order.CMD_POS)
				bSuc=AccessibilityHelper.performClick(info);
			if(mType==order.CMD_LONG_CLICK)
				bSuc=AccessibilityHelper.performLongClick(info);
			bFind=true;
			return bSuc;
		}
		return false;
	}
	/*
	 * 查找并滑动控件
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public  void scroll(AccessibilityNodeInfo info,Point pos1,Point pos2) {
		if(info.isScrollable()==false)return;
		bFind=true;
		if(pos2.y>pos1.y)
			info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
		else
			info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
	}
	/*
	 * 查找并滑动控件
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public  void recycleScroll(AccessibilityNodeInfo info,Point pos1,Point pos2) {
		if(bFind)return;
		if (info.getChildCount() == 0) {
			if(info.isScrollable()){
				scroll(info,pos1,pos2);
			}
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycleScroll(info.getChild(i),pos1,pos2);
					if(info.isScrollable()){
						scroll(info,pos1,pos2);
					}
				}
			}
		}
	}
}
