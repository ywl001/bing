package com.ywl01.bing.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class HouseBean extends BaseBean implements Parcelable {
    public String houseName = "";
    public String houseNumber;
    public float angle;


    protected HouseBean(Parcel in) {
        super(in);
        houseName = in.readString();
        houseNumber = in.readString();
        angle = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(houseName);
        dest.writeString(houseNumber);
        dest.writeFloat(angle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HouseBean> CREATOR = new Creator<HouseBean>() {
        @Override
        public HouseBean createFromParcel(Parcel in) {
            return new HouseBean(in);
        }

        @Override
        public HouseBean[] newArray(int size) {
            return new HouseBean[size];
        }
    };
}
