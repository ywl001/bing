package com.ywl01.bing.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class MarkerBean extends BaseBean implements Parcelable {
    public String name;
    public String managerName;
    public String telephone;

    protected MarkerBean(Parcel in) {
        super(in);
        name = in.readString();
        managerName = in.readString();
        telephone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(managerName);
        dest.writeString(telephone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MarkerBean> CREATOR = new Creator<MarkerBean>() {
        @Override
        public MarkerBean createFromParcel(Parcel in) {
            return new MarkerBean(in);
        }

        @Override
        public MarkerBean[] newArray(int size) {
            return new MarkerBean[size];
        }
    };
}
