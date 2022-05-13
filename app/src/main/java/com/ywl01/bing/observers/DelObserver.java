package com.ywl01.bing.observers;

/**
 * Created by ywl01 on 2017/1/29.
 * 删除监控后返回删除的数量
 */

public class DelObserver extends BaseObserver<String> {
    @Override
    protected Integer transform(String data) {
        return Integer.parseInt(data);
    }
}
