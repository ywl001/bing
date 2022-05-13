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
import com.ywl01.bing.beans.MarkerBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控标注的数据源
 */

public class MarkerObserver extends BaseObserver<String> {
    @Override
    protected List<MapIcon> transform(String json) {
        List<MarkerBean> markers = new Gson().fromJson(json, new TypeToken<List<MarkerBean>>() {
        }.getType());

        List<MapIcon> icons = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            MarkerBean marker = markers.get(i);
            MapIcon icon = beanToMapIcon(marker);
            icon.setTag(marker);
            icons.add(icon);
        }
        return icons;
    }

    private MapIcon beanToMapIcon(MarkerBean marker) {

        Geopoint location = new Geopoint(marker.y, marker.x);

        MapIcon pushpin = new MapIcon();
        pushpin.setLocation(location);
        pushpin.setTitle(marker.name);
        MapImage mi = getMapImage(marker);
        pushpin.setImage(mi);
        return pushpin;
    }

    private MapImage getMapImage(MarkerBean marker) {
            int size = 24;
            Drawable drawable = ResourcesCompat.getDrawable(BaseActivity.currentActivity.getResources(), R.drawable.marker, null);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            MapImage mi = new MapImage(bitmap);
            return mi;
    }
}
