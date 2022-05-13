package com.ywl01.bing.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class BuildingBean extends BaseBean implements Parcelable {
    public String housingName;
    public String buildingNumber;
    public float angle;
    public int countFloor;
    public int countUnit;
    public int countHomesInUnit;
    public String sortType;

    protected BuildingBean(Parcel in) {
        super(in);
        housingName = in.readString();
        buildingNumber = in.readString();
        angle = in.readFloat();
        countFloor = in.readInt();
        countUnit = in.readInt();
        countHomesInUnit = in.readInt();
        sortType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(housingName);
        dest.writeString(buildingNumber);
        dest.writeFloat(angle);
        dest.writeInt(countFloor);
        dest.writeInt(countUnit);
        dest.writeInt(countHomesInUnit);
        dest.writeString(sortType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BuildingBean> CREATOR = new Creator<BuildingBean>() {
        @Override
        public BuildingBean createFromParcel(Parcel in) {
            return new BuildingBean(in);
        }

        @Override
        public BuildingBean[] newArray(int size) {
            return new BuildingBean[size];
        }
    };
}
