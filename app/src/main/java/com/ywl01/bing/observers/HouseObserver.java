package com.ywl01.bing.observers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;


import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapElementCollisionBehavior;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.ywl01.bing.R;
import com.ywl01.bing.activities.BaseActivity;
import com.ywl01.bing.beans.HouseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控标注的数据源
 */

public class HouseObserver extends BaseObserver<String> {

    private double zoomLevel;
    public HouseObserver(double zoomLevel){
        this.zoomLevel = zoomLevel;
    }
    @Override
    protected List<MapIcon> transform(String json) {
        List<HouseBean> houses = new Gson().fromJson(json, new TypeToken<List<HouseBean>>() {
        }.getType());

        List<MapIcon> icons = new ArrayList<>();
        for (int i = 0; i < houses.size(); i++) {
            HouseBean house = houses.get(i);
            if(house.houseName== null){
                house.houseName = "";
            }
            MapIcon icon = beanToMapIcon(house);
            icon.setTag(house);
            icons.add(icon);
        }
        return icons;
    }

    private MapIcon beanToMapIcon(HouseBean house) {

        Geopoint location = new Geopoint(house.y, house.x);

        MapIcon pushpin = new MapIcon();
        pushpin.setLocation(location);
        pushpin.setRotation(house.angle);
        MapImage mi = getMapImage(zoomLevel, house);
        pushpin.setImage(mi);

        return pushpin;
    }

    private MapImage getMapImage(double zoomLevle, HouseBean house) {
        if (zoomLevle > 14 && zoomLevle <= 18) {
            int size = 12;
            Drawable drawable = ResourcesCompat.getDrawable(BaseActivity.currentActivity.getResources(), R.drawable.house5, null);
            Drawable drawable2 = ResourcesCompat.getDrawable(BaseActivity.currentActivity.getResources(), R.drawable.house4, null);
            int width = (int) (size * (zoomLevle - 14));
            int height = (int) (size * (zoomLevle - 14));
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if(house.houseName != null){
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }else {
                drawable2.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable2.draw(canvas);
            }
            MapImage mi = new MapImage(bitmap);
            return mi;
        } else if (zoomLevle > 18) {
            //显示名字
            int w = 65;
            int h = 40;
            Drawable drawable = ResourcesCompat.getDrawable(BaseActivity.currentActivity.getResources(), R.drawable.ic_house, null);
            int width = (int) (w * Math.pow(2,zoomLevle - 18));
            int height = (int) (h * (zoomLevle - 17));
            ;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            paint.setTextSize((float) (15 * Math.pow(2,zoomLevle - 18)));
            canvas.drawText(
                    house.houseName,
                    (float) (width * 0.15),
                    height - 15,
                    paint
            );

            MapImage mi = new MapImage(bitmap);
            return mi;
        }
        return null;
    }
}
