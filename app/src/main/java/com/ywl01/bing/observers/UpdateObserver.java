package com.ywl01.bing.observers;

/**
 * Created by ywl01 on 2017/1/29.
 * 更新监控信息返回影响的数量
 */

public class UpdateObserver extends BaseObserver<String> {
    @Override
    protected Integer transform(String data) {
        return Integer.parseInt(data);
    }
}
