/**
 *
 */
package accessibility;
import android.view.accessibility.AccessibilityEvent;


/**
 * @author byc
 *
 */
public interface AccessibilityJob {
    String[] getTargetPackageName();//获取要处理的包；
    boolean isTargetPackageName(String pkg);//是否是目标包；
    boolean isEnable();//是否可用；
    void onCreateJob(Acc101Service service);//创建工作；
    void onStopJob();//停止工作；
    void onReceiveJob(AccessibilityEvent event);//事件驱动；
    void EventStart();//开始事件处理；
    void EventTimeStart();//开始定时事件处理；
    void closeEventWorking();//关闭事件处理；
    void onWorking();//刷新处理流程；
    void TimeStart();//开始刷新处理；
    void closeTimeWorking();//关闭刷新处理；
}
