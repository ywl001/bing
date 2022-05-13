package com.ywl01.bing.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class RoadBean extends BaseBean implements Parcelable {
    public String name;
    public String shape;
    public int width;

    protected RoadBean(Parcel in) {
        super(in);
        name = in.readString();
        shape = in.readString();
        width = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(shape);
        dest.writeInt(width);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoadBean> CREATOR = new Creator<RoadBean>() {
        @Override
        public RoadBean createFromParcel(Parcel in) {
            return new RoadBean(in);
        }

        @Override
        public RoadBean[] newArray(int size) {
            return new RoadBean[size];
        }
    };
}
