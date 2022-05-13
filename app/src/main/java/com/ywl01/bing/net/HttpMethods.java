package com.ywl01.bing.net;


import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ywl01 on 2017/1/17.
 * 执行网络请求的所有方法
 */

public class HttpMethods {
    private static final int DEFAULT_TIMEOUT = 5;
    private Retrofit retrofit;
    private DataBaseServices dataBaseServices;
//    private FileUploadService fileUploadService;
//    private DelFileService delFileService;
//    private GetUpdateInfoService getUpdateInfoService;

    private HttpMethods() {
        System.out.println("http methods constract");
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        okBuilder.connectTimeout(5, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .baseUrl(Urls.BASE_URL)
                .client(okBuilder.build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(new ToStringConverterFactory())
                .build();
        dataBaseServices = retrofit.create(DataBaseServices.class);
    }

    public void getSqlResult(Observer<String> subscriber, String action, String sql) {
        System.out.println("sql:" + sql);
        Observable<String> observable = dataBaseServices.getResult(action, sql);
        execute(observable,subscriber);
    }

//    public void uploadFile(Observer<String> observer, RequestBody fileDir, MultipartBody.Part file) {
//        Observable<String> observable = fileUploadService.upload(fileDir,file);
//        execute(observable,observer);
//    }
//
//    public void delFile(Observer<String> observer, String filePath) {
//        Observable<String> observable = delFileService.delFile(filePath);
//        execute(observable,observer);
//    }
//
//    public void getUpdateInfo(Observer<String> observer){
//        Observable<String> observable = getUpdateInfoService.getAppInfo();
//        execute(observable,observer);
//    }

    private void execute(Observable observable, Observer observer){
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static HttpMethods getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final HttpMethods INSTANCE = new HttpMethods();
    }
}
