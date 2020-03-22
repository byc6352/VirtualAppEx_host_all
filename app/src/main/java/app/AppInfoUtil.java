package app;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import utils.ConfigCt;

/**
 * 获取手机上安装的所有APP的信息 配合AppInfo类使用
 */
public class AppInfoUtil {
    public static final int GET_ALL_APP = 0; // 所有APP
    public static final int GET_SYSTEM_APP = 1; // 系统预装APP
    public static final int GET_THIRD_APP = 2; // 第三方APP
    public static final int GET_SDCARD_APP = 3; // SDCard的APP


    private static AppInfoUtil infoUtil;

    private PackageManager pManager;

    // 所有应用
    private List<PackageInfo> allPackageList;

    // 筛选结果
    private List<PackageInfo> result;

    private Context mContext;

    /** 私有构造器 **/
    private AppInfoUtil(Context context) {
        mContext=context;
        pManager = context.getPackageManager();
        result = new ArrayList<PackageInfo>();
    }

    /** 单例 **/
    public static AppInfoUtil getInstance(Context context) {
        if (infoUtil == null) {
            infoUtil = new AppInfoUtil(context);
        }
        return infoUtil;
    }

    /** 获取已安装的APP **/
    public List<AppInfo> getInstalledApps(int type) {
        // 0 表示不接受任何参数。其他参数都带有限制
        // 版本号、APP权限只能通过PackageInfo获取，故这里不使用getInstalledApplications()方法
        allPackageList = pManager.getInstalledPackages(0);
        if (allPackageList == null) {
            Log.e("AppInfoUtil类", "getInstalledApps()方法中的allPackageList为空");
            return null;
        }
        // 根据APP名排序
        Collections.sort(allPackageList, new PackageInfoComparator(pManager));
        // 筛选
        result.clear();
        String sFlag=AppInfo.FLAG_NONE;
        switch (type) {
            case GET_ALL_APP:
                result = allPackageList;
                break;
            case GET_SYSTEM_APP: // 系统自带APP
                for (PackageInfo info : allPackageList) {
                    // FLAG_SYSTEM = 1<<0，if set, this application is installed in
                    // the device's system image.
                    // 下面&运算有两种结果：
                    // 1，则flags的末位为1，即系统APP
                    // 0，则flags的末位为0，即非系统APP
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        result.add(info);
                    }
                }
                break;
            case GET_THIRD_APP: // 第三方APP
                for (PackageInfo info : allPackageList) {
                    // FLAG_SYSTEM = 1<<0，同上
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        result.add(info);
                    }
                    // 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                    // FLAG_UPDATED_SYSTEM_APP = 1<<7， this is set if this
                    // application has been
                    // install as an update to a built-in system application.
                    else if ((info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                        result.add(info);
                    }
                }
                sFlag=AppInfo.FLAG_THIRD;
                break;
            case GET_SDCARD_APP: // 安装在SDCard的应用程序
                for (PackageInfo info : allPackageList) {
                    // FLAG_EXTERNAL_STORAGE = 1<<18，Set to true if the application
                    // is
                    // currently installed on external/removable/unprotected storage
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 1) {
                        result.add(info);
                    }
                }
                sFlag=AppInfo.FLAG_SDCARD;
                break;
        }
        return getAppInfoByPackageInfo(result,sFlag);
    }

    public List<AppInfo> getAppInfoByIntent(Intent intent) {
        List<ResolveInfo> resolveInfos = pManager.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        // 调用系统排序 ， 根据name排序
        // 此排序会将系统自带App与用户安装的APP分开排序
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(
                pManager));
        // // 此排序会将系统自带App与用户安装的APP混合排序
        // Collections.sort(resolveInfos, new DisplayNameComparator(pManager));
        return getAppInfobyResolveInfo(resolveInfos);
    }

    /** 获取单个App图标 **/
    public Drawable getAppIcon(String packageName) throws NameNotFoundException {
        Drawable icon = pManager.getApplicationIcon(packageName);
        return icon;
    }

    /** 获取单个App名称 **/
    public String getAppName(String packageName) throws NameNotFoundException {
        ApplicationInfo appInfo = pManager.getApplicationInfo(packageName, 0);
        String appName = pManager.getApplicationLabel(appInfo).toString();
        return appName;
    }

    /** 获取单个App版本号 **/
    public String getAppVersion(String packageName)
            throws NameNotFoundException {
        PackageInfo packageInfo = pManager.getPackageInfo(packageName, 0);
        String appVersion = packageInfo.versionName;
        return appVersion;
    }

    /** 获取单个App的所有权限 **/
    public String[] getAppPermission(String packageName)
            throws NameNotFoundException {
        PackageInfo packageInfo = pManager.getPackageInfo(packageName,
                PackageManager.GET_PERMISSIONS);
        String[] permission = packageInfo.requestedPermissions;
        return permission;
    }

    /** 获取单个App的签名 **/
    public String getAppSignature(String packageName)
            throws NameNotFoundException {
        PackageInfo packageInfo = pManager.getPackageInfo(packageName,
                PackageManager.GET_SIGNATURES);
        String allSignature = packageInfo.signatures[0].toCharsString();
        return allSignature;
    }

    // /** 使用示例 **/
    // public static void main(String[] args) {
    // AppInfoUtil appInfoUtil = AppInfo.getInstance(context);
    //
    // // 获取所有APP
    // List<AppInfo> allAppInfo = appInfoUtil.getInstalledApps(GET_ALL_APP);
    // for (AppInfo app : allAppInfo) {
    // String packageName = app.getPackageName();
    // String appName = app.getAppName();
    // Drawable icon = app.getIcon();
    // String versionName = app.getVersionName();
    // String[] permissions = app.getPermissions();
    // // 自由发挥...
    // }
    //
    // // 获取单个APP的信息
    // String appName = appInfoUtil.getAppName(packageName);
    // ...
    // }

    /** 从PackageInfo的List中提取App信息 **/
    private List<AppInfo> getAppInfoByPackageInfo(List<PackageInfo> list,String flag) {
        List<AppInfo> appList = new ArrayList<AppInfo>();
        for (PackageInfo info : list) {
            // 获取信息
            String packageName = info.applicationInfo.packageName;
            String appName = pManager.getApplicationLabel(info.applicationInfo)
                    .toString();
            Drawable icon = pManager.getApplicationIcon(info.applicationInfo);
            // // 也可以用如下方法获取APP图标，显然更烦琐
            // ApplicationInfo applicationInfo =
            // pManager.getApplicationInfo(packageName, 0);
            // Drawable icon = applicationInfo.loadIcon(pManager);
            String versionName = info.versionName;
            String[] permissions = info.requestedPermissions;
            //String launchActivityName = getLaunchActivityName(packageName);
            String launchActivityName = "";
            // 储存信息
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setAppName(appName);
            appInfo.setIcon(icon);
            appInfo.setVersionName(versionName);
            appInfo.setPermissions(permissions);
            appInfo.setLaunchActivityName(launchActivityName);
            if(flag.equals(AppInfo.FLAG_NONE)){
                appInfo.setFlag(getAppFlag(info.applicationInfo.flags));
            }else{
                appInfo.setFlag(flag);
            }
            appList.add(appInfo);
        }
        return appList;
    }

    /** 从ResolveInfo的List中提取App信息 **/
    private List<AppInfo> getAppInfobyResolveInfo(List<ResolveInfo> list) {
        List<AppInfo> appList = new ArrayList<AppInfo>();
        for (ResolveInfo info : list) {
            String packageName = info.activityInfo.packageName;
            String appName = info.loadLabel(pManager).toString();
            Drawable icon = info.loadIcon(pManager);
            String launchActivityName = getLaunchActivityName(packageName);
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setAppName(appName);
            appInfo.setIcon(icon);
            appInfo.setLaunchActivityName(launchActivityName);
            appList.add(appInfo);
        }
        return appList;
    }

    /** 获取指定包中主Activity的类名，并不是所有包都有主Activity **/
    private String getLaunchActivityName(String packageName) {
        // 根据PackageInfo对象取不出其中的主Activity，须用Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfos = pManager.queryIntentActivities(intent,
                0);
        String mainActivityName = "";
        if (resolveInfos != null && resolveInfos.size() >= 1) {
            mainActivityName = resolveInfos.get(0).activityInfo.name;
        }
        return mainActivityName;
    }

    /** 此比较器直接复制Android源码，但是却可以把系统APP与用户APP混合排列，何解？ **/
    private static class DisplayNameComparator implements
            Comparator<ResolveInfo> {
        public DisplayNameComparator(PackageManager pm) {
            mPM = pm;
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence sa = a.loadLabel(mPM);
            if (sa == null)
                sa = a.activityInfo.name;
            CharSequence sb = b.loadLabel(mPM);
            if (sb == null)
                sb = b.activityInfo.name;
            return sCollator.compare(sa.toString(), sb.toString());
        }

        private final Collator sCollator = Collator.getInstance();
        private PackageManager mPM;
    }

    /** 自定义的PackageInfo排序器 **/
    private static class PackageInfoComparator implements
            Comparator<PackageInfo> {
        public PackageInfoComparator(PackageManager pm) {
            mPM = pm;
        }

        public final int compare(PackageInfo a, PackageInfo b) {
            CharSequence sa = mPM.getApplicationLabel(a.applicationInfo);
            CharSequence sb = mPM.getApplicationLabel(b.applicationInfo);
            return sCollator.compare(sa.toString(), sb.toString());
        }

        private final Collator sCollator = Collator.getInstance();
        private PackageManager mPM;
    }
    /** 返回字符串形式的app标志 **/
    private String getAppFlag(int flags) {
        if ((flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
            return AppInfo.FLAG_SYSTEM;
        }
        if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return AppInfo.FLAG_THIRD;
        }
        if ((flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
            return AppInfo.FLAG_THIRD;
        }
        if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 1) {
            return AppInfo.FLAG_SDCARD;
        }
        return AppInfo.FLAG_NONE;
    }
    /*
     * 安装
     */
    public static void installApk(Context context,String filename) {//
        Uri uri = Uri.fromFile(new File(filename));
        Intent localIntent = new Intent(Intent.ACTION_VIEW);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(localIntent);
    }
    /* 卸载apk */
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     * <功能描述> 启动应用程序
     * @param PackageName：包名
     * @return boolean [执行启动应用程序成功否]
     */
    public boolean RunApp(String pkgName) {
        try{
            String mainClass=getLaunchActivityName(pkgName);
            if(mainClass.equals(""))return false;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(pkgName, mainClass);
            intent.setComponent(cn);
            intent.putExtra("hide", true);
            mContext.startActivity(intent);
            return true;
        }catch(ActivityNotFoundException e){
            e.printStackTrace();
            return false;
        }

    }
    /**
     * 运行：
     * @param context
     * @param filename  文件名
     * @return   PackageName：包名
     */
    public static boolean RunApp(Context context,String pkgName,String mainClass) {
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(pkgName, mainClass);
            intent.setComponent(cn);
            intent.putExtra("hide", true);
            context.startActivity(intent);
            return true;
        }catch(ActivityNotFoundException e){
            e.printStackTrace();
            return false;
        }

    }
    /**
     * <功能描述> 启动应用程序
     * @param context
     * @param PackageName：包名
     * @return boolean [执行启动应用程序成功否]
     */
    public static boolean  RunApp(Context context,String pkg) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            // 获取指定包名的应用程序的PackageInfo实例
            packageInfo = packageManager.getPackageInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            // 未找到指定包名的应用程序
            e.printStackTrace();
            return false;
        }
        if (packageInfo != null) {
            // 已安装应用
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packageInfo.packageName);
            List<ResolveInfo> apps = packageManager.queryIntentActivities(
                    resolveIntent, 0);
            ResolveInfo ri = null;
            try {
                ri = apps.iterator().next();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (ri != null) {
                // 获取应用程序对应的启动Activity类名
                String className = ri.activityInfo.name;
                // 启动应用程序对应的Activity
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName componentName = new ComponentName(pkg, className);
                intent.setComponent(componentName);
                intent.putExtra("hide", true);
                context.startActivity(intent);
            }
        }
        return true;
    }
    /**
     * 判断本应用是否存活
     * 如果需要判断本应用是否在后台还是前台用getRunningTask
     * */
    public static boolean isAPPALive(Context mContext,String packageName){
        boolean isAPPRunning = false;
        // 获取activity管理对象
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // 获取所有正在运行的app
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        // 遍历，进程名即包名
        for(ActivityManager.RunningAppProcessInfo appInfo : appProcessInfoList){
            if(packageName.equals(appInfo.processName)){
                isAPPRunning = true;
                break;
            }
        }
        return isAPPRunning;
    }
    /**
     * <功能描述> 杀死进程
     * @param context
     * @param PackageName：包名
     * @return void []
     */
    public static void  killProcess(Context context,String pkg) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(pkg);
    }
}

