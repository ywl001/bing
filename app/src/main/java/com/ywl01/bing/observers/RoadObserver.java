package com.ywl01.bing.observers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolyline;
import com.ywl01.bing.beans.BuildingBean;
import com.ywl01.bing.beans.RoadBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控标注的数据源
 */

public class RoadObserver extends BaseObserver<String> {

    private double zoomLevel;

    public RoadObserver(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    @Override
    protected List<MapPolyline> transform(String json) {

        System.out.println("road json" + json);
        List<RoadBean> roads = new Gson().fromJson(json, new TypeToken<List<RoadBean>>() {
        }.getType());
//
        System.out.println("road" + roads);

        List<MapPolyline> lines = new ArrayList<>();
        for (int i = 0; i <roads.size(); i++) {
            RoadBean road = roads.get(i);
            ArrayList<Geoposition> geopoints = toArrayPoint(road.shape);
            MapPolyline mapPolyline = new MapPolyline();
            mapPolyline.setPath(new Geopath(geopoints));
            mapPolyline.setStrokeColor(Color.YELLOW);
            mapPolyline.setStrokeWidth(3);
//            mapPolyline.setStrokeDashed(true);
            lines.add(mapPolyline);
        }
        return lines;
    }

    private ArrayList<Geoposition> toArrayPoint(String line) {
        ArrayList<Geoposition> geopoints = new ArrayList<Geoposition>();
        String[] points = line.substring(11, line.length() - 2).split(",");
        for (int i = 0; i < points.length; i++) {
            String p = points[i];
            double x = Double.parseDouble(p.split(" ")[0]);
            double y = Double.parseDouble(p.split(" ")[1]);
            geopoints.add(new Geoposition(y, x));
        }
        return geopoints;
    }

}
