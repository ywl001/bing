package com.ywl01.bing.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public abstract class BaseActivity extends AppCompatActivity {
    // 对所有的activity进行管理
    public static List<Activity> activities = new LinkedList<Activity>();
    public static BaseActivity currentActivity;

    public Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized (activities) {
            activities.add(this);
        }
        currentActivity = this;
        if (getIntent() != null)
            data = getIntent().getExtras();

        // 初始化view
        initView();
        initData();
        // 初始化actionBar
        initActionBar();
    }

    protected void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("base onDestory");
        synchronized (activities) {
            activities.remove(this);
        }
    }

    @Override
    protected void onResume() {
        System.out.println("base on resume");
        super.onResume();
        currentActivity = this;
    }

    @Override
    protected void onPause() {
        System.out.println("base on pause");
        super.onPause();
        currentActivity = null;
    }

    public void exitApp() {
        // 遍历所有的activity，finish
        ListIterator<Activity> iterator = activities.listIterator();
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            next.finish();
        }

        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
        System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
    }

    public void addFragment(Fragment fragment, int resID) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(resID, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    public void addFragment(Fragment from, Fragment to, int resID) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(from)
                .add(resID, to)
                .commit();
    }

    public void removeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
    }

    public void hideSoftkey() {
        //隐藏软键盘
        if (getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected abstract void initView();

    protected abstract void initActionBar();

}
