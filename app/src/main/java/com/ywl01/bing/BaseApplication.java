package com.ywl01.bing;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

/**
 * Created by ywl01 on 2016/12/31.
 */

public class BaseApplication extends Application {

    private static Context appContext;
    private static int mainThreadID;
    private static Handler mainThreadHandler;
    private static Looper mainThreadLooper;
    private static Thread mainThread;
    public static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;
        mainThreadHandler = new Handler();
        mainThreadID = Process.myTid();
        mainThread = Thread.currentThread();
        mainThreadLooper = getMainLooper();

        instance = this;
    }

    public static BaseApplication getInstance() {
        return instance;
    }


    public static int getMainThreadID() {
        return mainThreadID;
    }

    public static Handler getMainHandler() {
        return mainThreadHandler;
    }

    public static Looper getMainThreadLooper() {
        return mainThreadLooper;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static Context getAppContext() {
        System.out.println("app:context---"+appContext);
        return appContext;
    }
}
