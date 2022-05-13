package com.ywl01.bing.observers;

import rx.Observer;

/**
 * Created by ywl01 on 2017/1/21.
 * 所有observer的基类
 */

public abstract class BaseObserver<T> implements Observer<T> {
    private OnNextListener onNextListener;

    @Override
    public void onNext(T data) {
        //System.out.println("on next" + data.toString());
        Object newData = transform(data);
        if (onNextListener != null) {
            onNextListener.onNext(newData,this);
        }
    }

    protected abstract Object transform(T data);

    @Override
    public void onCompleted() {
        System.out.println("on complete");
    }

    @Override
    public void onError(Throwable e) {
        System.out.println("on error：" + e.getMessage());
    }

    public void setOnNextListener(OnNextListener onNextListener){
        this.onNextListener = onNextListener;
    }

   public interface OnNextListener<T>{
        void onNext(T data, Observer observer);
    }

}
