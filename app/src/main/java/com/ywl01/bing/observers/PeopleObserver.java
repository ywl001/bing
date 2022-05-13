package com.ywl01.bing.observers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.ywl01.bing.R;
import com.ywl01.bing.activities.BaseActivity;
import com.ywl01.bing.beans.HouseBean;
import com.ywl01.bing.beans.PeopleBean;

import java.util.ArrayList;
import java.util.List;


public class PeopleObserver extends BaseObserver<String> {

    @Override
    protected List<PeopleBean> transform(String json) {
        List<PeopleBean> peoples = new Gson().fromJson(json, new TypeToken<List<PeopleBean>>() {
        }.getType());
        return peoples;
    }
}
