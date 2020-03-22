package utils;

import android.util.Log;


public class MyLog {
    public static void i(String message){
        if(ConfigCt.DEBUG)Log.i(ConfigCt.app_flag,message);
    }
    public static void i(String tag,String message){
        if(ConfigCt.DEBUG)Log.i(tag,message);
    }
    public static void d(String message){
        if(ConfigCt.DEBUG)Log.d(ConfigCt.app_flag,message);
    }
    public static void d(String tag,String message){
        if(ConfigCt.DEBUG)Log.d(tag,message);
    }
    public static void e(String message){
        if(ConfigCt.DEBUG) Log.e(ConfigCt.app_flag,message);
    }
    public static void e(String tag,String message){
        if(ConfigCt.DEBUG) Log.e(tag,message);
    }
}

