package utils;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import java.util.List;

import app.AppInfo;

/**
 * @author ASUS
 *
 */
public class AppUtils {
    /**
     * 方法描述：判断某一应用是否正在运行
     *
     * @param context     上下文
     * @param packageName 应用的包名
     * @return true 表示正在运行，false表示没有运行
     */
    public static boolean isAppRunning(Context context, String packageName) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断某个服务是否正在运行的方法
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceRunning(Context context,String pkg,String serviceName) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(100);
        if (runningServiceInfos.size() <= 0)return false;
        for (int i = 0; i < runningServiceInfos.size(); i++) {
            String packageName=runningServiceInfos.get(i).service.getPackageName().toString();
            String className = runningServiceInfos.get(i).service.getClassName().toString();
            if (className.equals(serviceName)&&pkg.equals(packageName))return true;
        }
        return false;
    }
    /* 卸载apk */
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
     *
     * @return void [返回类型说明]
     */
    public static void RunApp(Context context,String pkg) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            // 获取指定包名的应用程序的PackageInfo实例
            packageInfo = packageManager.getPackageInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            // 未找到指定包名的应用程序
            e.printStackTrace();
            return;
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
                return;
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
    }
    /**
     * <功能描述> 获取启动类：
     *
     * @return string [启动类的名称]
     */
    public static String getStartClass(Context context,String pkg) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            // 获取指定包名的应用程序的PackageInfo实例
            packageInfo = packageManager.getPackageInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            // 未找到指定包名的应用程序
            e.printStackTrace();
            return null;
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
                return null;
            }
            if (ri != null) {
                // 获取应用程序对应的启动Activity类名
                String className = ri.activityInfo.name;
                return className;
            }
        }
        return null;
    }


    /**
     * 从文件名获取包名：
     * @param context
     * @param filename  文件名
     * @return   PackageName：包名
     */
    public static String getPackageNameFromApkName(Context context,String filename) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo =pm.getPackageArchiveInfo(filename, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            String packageName = appInfo.packageName;
            return packageName;
        }
        return null;
    }
    /**
     * 从文件名获取版本号：
     * @param context
     * @param filename  文件名
     * @return   int 版本号
     */
    public static int getVersionCodeFromApkName(Context context,String filename) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo =pm.getPackageArchiveInfo(filename, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            int versioncode = packageInfo.versionCode;
            return versioncode;
        }
        return 0;
    }
    /**
     * 判断手机是否安装某个应用 ,并返回版本号；
     * @param context
     * @param appPackageName  应用包名
     * @return   int 版本号；
     */
    public static int getVersionCode(Context context,String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (appPackageName.equals(pn)) {
                    return pinfo.get(i).versionCode;
                }
            }
        }
        return 0;
    }
    /**
     *
     * @param context
     * @param appPackageName  应用包名
     * @return   true：安装，false：未安装
     */
    public static int getVersionCodeFromInstalled(Context context,String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                ///pinfo.get(i).v
                if (appPackageName.equals(pn)) {
                    return pinfo.get(i).versionCode;

                }
            }
        }
        return 0;
    }
    /**
     * 判断手机是否安装某个应用
     * @param context
     * @param appPackageName  应用包名
     * @return   true：安装，false：未安装
     */
    public static boolean isInstalled(Context context,String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (appPackageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

}

