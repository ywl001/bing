package com.ywl01.bing.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseBean implements Parcelable {
    public long id;
    public double y;
    public double x;
    public long insertUser;
    public double displayLevel;

    protected BaseBean(Parcel in) {
        id = in.readLong();
        y = in.readDouble();
        x = in.readDouble();
        insertUser = in.readLong();
        displayLevel = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(y);
        dest.writeDouble(x);
        dest.writeLong(insertUser);
        dest.writeDouble(displayLevel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseBean> CREATOR = new Creator<BaseBean>() {
        @Override
        public BaseBean createFromParcel(Parcel in) {
            return new BaseBean(in);
        }

        @Override
        public BaseBean[] newArray(int size) {
            return new BaseBean[size];
        }
    };
}
