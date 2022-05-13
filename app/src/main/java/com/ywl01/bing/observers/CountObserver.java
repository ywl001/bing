package com.ywl01.bing.observers;

/**
 * Created by ywl01 on 2017/1/29.
 * 删除监控后返回删除的数量
 */

public class CountObserver extends BaseObserver<String> {
    @Override
    protected String transform(String data) {
        return data;
    }
}
