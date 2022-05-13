package com.ywl01.bing.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ywl01.bing.BaseApplication;
import com.ywl01.bing.activities.BaseActivity;

/**
 * Created by ywl01 on 2018/3/8.
 */

public class AppUtils {

    ///////////////////////////////////////////////////////////////////////////
    // 返回context
    ///////////////////////////////////////////////////////////////////////////
    public static Context getContext() {
        return BaseApplication.getAppContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 检测网络连接
    ///////////////////////////////////////////////////////////////////////////
    public static boolean isNetConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null&& info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.v("error",e.toString());
        }
        return false;
    }

    public static boolean isWifiConnect(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            // 获取网络连接管理的对象
            NetworkInfo networkInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(networkInfo != null&& networkInfo.isConnected()){
                return true;
            }
            return false;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 单位转换dp px
    ///////////////////////////////////////////////////////////////////////////
    public static int dip2px(int dip)
    {
        // 公式： dp = px / (dpi / 160) px = dp * (dpi / 160)
        // dp = px / denisity
        // px = dp * denisity;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        return (int) (dip * density + 0.5f);
    }

    public static int px2dip(int px)
    {
        // dp = px / denisity
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        return (int) (px / density + 0.5f);
    }

    //获取版本名称
    public static String getVersionName(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo =  packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    //获取版本号
    public static int getVersionCode(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo =  packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //获取应用名称
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo =  packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static Toast toast;
    public static void showToast(String message) {
        if (toast == null) {
            toast = Toast.makeText(BaseActivity.currentActivity,message,Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void startActivity(Class<?> cls){
        Intent intent = new Intent(BaseActivity.currentActivity,cls);
        BaseActivity.currentActivity.startActivity(intent);
    }

    public static void startActivity(Class<?> cls, Bundle args){
        Intent intent = new Intent(BaseActivity.currentActivity,cls);
        intent.putExtras(args);
        BaseActivity.currentActivity.startActivity(intent);
    }

    public static void playSound(int resID){
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), resID);
        mediaPlayer.setVolume(0.50f, 0.50f);
        mediaPlayer.start();
    }

    public static String[] getResArray(int resArrayID) {
        Resources res = getContext().getResources();
        return res.getStringArray(resArrayID);
    }

    public static String getResString(int resStringID) {
        Resources res = getContext().getResources();
        return res.getString(resStringID);
    }
}
