package com.ywl01.bing.map;

import static com.microsoft.maps.platformabstraction.IO.getApplicationContext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;

import com.microsoft.maps.GPSMapLocationProvider;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapCameraChangedEventArgs;
import com.microsoft.maps.MapCameraChangingEventArgs;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapElementTappedEventArgs;
import com.microsoft.maps.MapHoldingEventArgs;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapUserLocation;
import com.microsoft.maps.MapUserLocationTrackingState;
import com.microsoft.maps.MapView;
import com.microsoft.maps.OnMapCameraChangedListener;
import com.microsoft.maps.OnMapCameraChangingListener;
import com.microsoft.maps.OnMapElementTappedListener;
import com.microsoft.maps.OnMapHoldingListener;
import com.ywl01.bing.R;
import com.ywl01.bing.activities.AddMarkerActivity;
import com.ywl01.bing.activities.BaseActivity;
import com.ywl01.bing.activities.EditBuildingActivity;
import com.ywl01.bing.activities.EditHouseActivity;
import com.ywl01.bing.activities.EditMarkerActivity;
import com.ywl01.bing.beans.BaseBean;
import com.ywl01.bing.beans.BuildingBean;
import com.ywl01.bing.beans.HouseBean;
import com.ywl01.bing.beans.MarkerBean;
import com.ywl01.bing.events.LocationEvent;
import com.ywl01.bing.events.MapLevelChangeEvent;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.TableName;
import com.ywl01.bing.net.User;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.BuildingObserver;
import com.ywl01.bing.observers.HouseObserver;
import com.ywl01.bing.observers.MarkerObserver;
import com.ywl01.bing.observers.RoadObserver;
import com.ywl01.bing.utils.AppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Observer;

public class MapListener implements
        OnMapCameraChangedListener,
        OnMapCameraChangingListener,
        OnMapHoldingListener,
        OnMapElementTappedListener,
        BaseObserver.OnNextListener {

    private MapView mapView;
    private MapElementLayer houseLayer;
    private MapElementLayer buildingLayer;
    private MapElementLayer markLayer;
    private MapElementLayer gpsLayer;
    private MapElementLayer roadLayer;

    private long preLoadTime = 0;

    private HouseObserver houseObserver;
    private BuildingObserver buildingObserver;
    private MarkerObserver markObserver;
    private RoadObserver roadObserver;

    //保存跳转到其他activity时的zoomlevel，当返回时系统给的zoomlevel和跳转时的不一样。
    private double saveZoomLevel;

    public MapListener(MapView mapView) {
        this.mapView = mapView;
        houseLayer = new MapElementLayer();
        houseLayer.addOnMapElementTappedListener(this);
        mapView.getLayers().add(houseLayer);

        buildingLayer = new MapElementLayer();
        mapView.getLayers().add(buildingLayer);
        buildingLayer.addOnMapElementTappedListener(this);

        markLayer = new MapElementLayer();
        mapView.getLayers().add(markLayer);
        markLayer.addOnMapElementTappedListener(this);

        gpsLayer = new MapElementLayer();
        mapView.getLayers().add(gpsLayer);
        gpsLayer.addOnMapElementTappedListener(this);

        roadLayer = new MapElementLayer();
        mapView.getLayers().add(roadLayer);

        EventBus.getDefault().register(this);
    }

    private Timer timer = new Timer();
    private int delay = 500;

    @Override
    public boolean onMapCameraChanged(MapCameraChangedEventArgs args) {
        preLoadTime = System.currentTimeMillis();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - preLoadTime >= delay) {
                    System.out.println("camera run");
                    loadAllData(mapView.getZoomLevel());
                }
            }
        }, delay);

        if (mapView.getZoomLevel() < 14) {
            clearElements();
        }

        MapLevelChangeEvent event = new MapLevelChangeEvent();
        event.mapZoom = mapView.getZoomLevel();
        event.dispatch();
        return false;
    }

    @Override
    public boolean onMapHolding(MapHoldingEventArgs args) {
        System.out.println("holding");
        if (User.id == 0) {
            AppUtils.showToast("必须登陆才能添加");
            return false;
        }
        double zoomLevel = mapView.getZoomLevel();
        if (zoomLevel < 19) {
            MapScene scene = MapScene.createFromLocationAndZoomLevel(args.location, 19.5);
            mapView.setScene(scene, MapAnimationKind.NONE);
        } else {
            System.out.println("add........");
            double y = args.location.getPosition().getLatitude();
            double x = args.location.getPosition().getLongitude();
            Bundle data = new Bundle();
            data.putDouble("x", x);
            data.putDouble("y", y);
            saveZoomLevel = mapView.getZoomLevel();
            AppUtils.startActivity(AddMarkerActivity.class, data);
        }

        return false;
    }

    private void loadData(String tableName, BaseObserver observer) {
        System.out.println("map level:" + mapView.getZoomLevel());
        String sql = SqlFactory.selectMarkersByBound(mapView.getBounds(), mapView.getZoomLevel(), tableName);
        HttpMethods.getInstance().getSqlResult(observer, SqlAction.SELECT, sql);
        observer.setOnNextListener(this);
    }

    private void loadRoadData(String tableName, BaseObserver observer) {
        System.out.println("map level:" + mapView.getZoomLevel());
        String sql = SqlFactory.selectRoad(mapView.getBounds(), mapView.getZoomLevel(), tableName);
        HttpMethods.getInstance().getSqlResult(observer, SqlAction.SELECT, sql);
        observer.setOnNextListener(this);
    }

    @Override
    public void onNext(Object data, Observer observer) {
        System.out.println("data complete" + data);
        List<MapElement> icons = (List<MapElement>) data;
        if (observer == houseObserver) {
            houseLayer.getElements().clear();
            for (int i = 0; i < icons.size(); i++) {
                MapIcon icon = (MapIcon) icons.get(i);
                houseLayer.getElements().add(icon);
            }
        } else if (observer == buildingObserver) {
            System.out.println("building data come" + icons.size());
            buildingLayer.getElements().clear();
            for (int i = 0; i < icons.size(); i++) {
                MapIcon icon = (MapIcon) icons.get(i);
                buildingLayer.getElements().add(icon);
            }
        } else if (observer == markObserver) {
            markLayer.getElements().clear();
            for (int i = 0; i < icons.size(); i++) {
                MapIcon icon = (MapIcon) icons.get(i);
                markLayer.getElements().add(icon);
            }
        } else if (observer == roadObserver) {
            roadLayer.getElements().clear();
            for (int i = 0; i < icons.size(); i++) {
                MapPolyline icon = (MapPolyline) icons.get(i);
                roadLayer.getElements().add(icon);
            }
        }
    }

    private void clearElements() {
        mapView.getLayers().clear();
        mapView.getLayers().add(houseLayer);
        mapView.getLayers().add(buildingLayer);
        mapView.getLayers().add(markLayer);
//        mapView.getLayers().add(gpsLayer);
        mapView.getLayers().add(roadLayer);

//        houseLayer.getElements().clear();
//        buildingLayer.getElements().clear();
//        markLayer.getElements().clear();
//        gpsLayer.getElements().clear();
//        roadLayer.getElements().clear();
    }

    @Override
    public boolean onMapElementTapped(MapElementTappedEventArgs e) {
        MapElement element = e.mapElements.get(0);
        BaseBean data = (BaseBean) element.getTag();
        if (data == null) {
            gpsLayer.getElements().clear();
            return false;
        }
        System.out.println("userId:" + User.id + "data.userID:" + data.insertUser);
        if (User.id == data.insertUser) {
            Bundle args = new Bundle();
            args.putParcelable("data", data);
            if (data instanceof HouseBean)
                AppUtils.startActivity(EditHouseActivity.class, args);
            else if (data instanceof BuildingBean) {
                AppUtils.startActivity(EditBuildingActivity.class, args);
            } else if (data instanceof MarkerBean) {
                AppUtils.startActivity(EditMarkerActivity.class, args);
            }
            saveZoomLevel = mapView.getZoomLevel();
        } else {
            AppUtils.showToast("添加的用戶才能修改或刪除");
        }
        return false;
    }

    @Subscribe
    public void refreshAll(TypeEvent event) {
        if (event.type == TypeEvent.REFRESH_MARKERS) {
            clearElements();
            //从其他activity回来后刷新时用保存的zoomlevel
            loadAllData(saveZoomLevel);
        }
    }

    MapIcon gpsIcon;

    @Subscribe
    public void userLocation(LocationEvent event) {
        if (gpsIcon == null) {
            gpsIcon = new MapIcon();
            int size = 64;
            Drawable drawable = ResourcesCompat.getDrawable(BaseActivity.currentActivity.getResources(), R.drawable.marker, null);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            MapImage mi = new MapImage(bitmap);
            gpsIcon.setImage(mi);
        }
        gpsLayer.getElements().add(gpsIcon);
        Geopoint gp = new Geopoint(event.lat, event.lng);
        gpsIcon.setLocation(gp);

        mapView.setScene(
                MapScene.createFromLocationAndZoomLevel(gp, 17),
                MapAnimationKind.NONE);
    }

    private void loadAllData(double zoomLevel) {
        if (zoomLevel > 15) {
            houseObserver = new HouseObserver(zoomLevel);
            loadData(TableName.HOUSE, houseObserver);
            buildingObserver = new BuildingObserver(zoomLevel);
            loadData(TableName.BUILDING, buildingObserver);

            roadObserver = new RoadObserver(zoomLevel);
            roadLayer.setZIndex(1.0f);
            loadRoadData(TableName.ROAD, roadObserver);
        }
        markObserver = new MarkerObserver();
        loadData(TableName.MARKER, markObserver);
    }

    @Override
    public boolean onMapCameraChanging(MapCameraChangingEventArgs e) {
        System.out.println(e.isFirstFrameSinceLastCameraChanged);
        return false;
    }

}

