package com.ywl01.bing.observers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.ywl01.bing.R;
import com.ywl01.bing.activities.BaseActivity;
import com.ywl01.bing.beans.BuildingBean;
import com.ywl01.bing.beans.HouseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控标注的数据源
 */

public class BuildingObserver extends BaseObserver<String> {

    private double zoomLevel;

    public BuildingObserver(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    @Override
    protected List<MapIcon> transform(String json) {
        List<BuildingBean> buildings = new Gson().fromJson(json, new TypeToken<List<BuildingBean>>() {
        }.getType());

        List<MapIcon> icons = new ArrayList<>();
        for (int i = 0; i < buildings.size(); i++) {
            BuildingBean building = buildings.get(i);
            MapIcon icon = beanToMapIcon(building);
            icon.setTag(building);
            icons.add(icon);
        }
        return icons;
    }

    private MapIcon beanToMapIcon(BuildingBean building) {

        Geopoint location = new Geopoint(building.y, building.x);

        MapIcon pushpin = new MapIcon();
        pushpin.setLocation(location);
        pushpin.setRotation(building.angle);
        MapImage mi = getMapImage(zoomLevel, building);
        pushpin.setImage(mi);

        return pushpin;
    }

    private MapImage getMapImage(double zoomLevle, BuildingBean building) {
        int textSize = (int) (30 * Math.pow(2,zoomLevle - 16.5));
        String str = building.buildingNumber + "号楼";
        Paint paint = new Paint();
        paint.setTextSize(textSize);

        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        int w = rect.width();
        int h = rect.height();

        Rect rect1 = new Rect(0, 0, w, h);
        System.out.println("building 宽：" + w + "--高：" + h);

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        paint.setColor(Color.YELLOW);
        canvas.drawRect(rect1, paint);
        paint.setColor(Color.RED);
        canvas.drawText(str, 0, h, paint);

        MapImage mi = new MapImage(bitmap);
        return mi;
    }
}
