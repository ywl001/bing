package com.ywl01.bing.beans;

public class PeopleBean {

    public String name;
    public String pNumber;
    public String birthday;
    public String address;
    public String town;

    @Override
    public String toString() {
        return name + "--"+birthday + "--"+address;
    }
}
