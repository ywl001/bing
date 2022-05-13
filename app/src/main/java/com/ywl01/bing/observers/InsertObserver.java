package com.ywl01.bing.observers;

/**
 * Created by ywl01 on 2017/1/29.
 * 插入监控返回id
 */

public class InsertObserver extends BaseObserver<String> {
    @Override
    protected Long transform(String data) {
        return Long.parseLong(data);
    }
}
