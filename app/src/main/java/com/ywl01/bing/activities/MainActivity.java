package com.ywl01.bing.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.maps.CopyrightDisplay;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapStyleSheets;
import com.microsoft.maps.MapUserInterfaceOptions;
import com.microsoft.maps.MapView;
import com.microsoft.maps.MapAnimationKind;
import com.orhanobut.hawk.Hawk;
import com.ywl01.bing.BuildConfig;
import com.ywl01.bing.R;
import com.ywl01.bing.beans.SPKey;
import com.ywl01.bing.events.InfoEvent;
import com.ywl01.bing.events.LocationEvent;
import com.ywl01.bing.events.MapLevelChangeEvent;
import com.ywl01.bing.events.TypeEvent;
import com.ywl01.bing.map.Location;
import com.ywl01.bing.map.MapListener;
import com.ywl01.bing.map.UserLocation;
import com.ywl01.bing.net.HttpMethods;
import com.ywl01.bing.net.SqlAction;
import com.ywl01.bing.net.SqlFactory;
import com.ywl01.bing.net.User;
import com.ywl01.bing.observers.BaseObserver;
import com.ywl01.bing.observers.CountObserver;
import com.ywl01.bing.utils.DialogUtils;
import com.ywl01.bing.views.LoginDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

public class MainActivity extends BaseActivity {

    private MapView mapView;

    private boolean isLogin;

    @BindView(R.id.btn_login)
    ImageView btnLogin;

    @BindView(R.id.tv_login_info)
    TextView tvLoginInfo;

    @BindView(R.id.tv_zoom_level)
    TextView tvZoomLevel;

    @BindView(R.id.tv_info)
    TextView tvInfo;

    @BindView(R.id.tv_count_house)
    TextView tvCountHouse;

    @BindView(R.id.tv_count_building)
    TextView tvCountBuilding;

    @BindView(R.id.tv_count_marker)
    TextView tvCountMarker;

//    private Location location;

    private int layerIndex = 0;

    private final Geopoint Meng_jin = new Geopoint(34.80843, 112.43888);

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Hawk.init(this).build();
        initMapView();
    }

    @Override
    protected void initData() {
        super.initData();
//        location = new Location(this);
        if (isLogin) {
            getCountInfo();
        }
    }

    private void getCountInfo() {
        //获取采集数量
        CountObserver observer = new CountObserver();
        HttpMethods.getInstance().getSqlResult(observer, SqlAction.SELECT, SqlFactory.selectCount());
        observer.setOnNextListener(new BaseObserver.OnNextListener<String>() {
            @Override
            public void onNext(String data, Observer observer) {
                System.out.println(data);
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    String houseCount;
                    String buildingCount;
                    String markerCount;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject o = jsonArray.getJSONObject(i);
                        System.out.println(o.getString("tableName"));
                        if (o.getString("tableName").equals("house")) {
                            houseCount = o.getString("count");
                            tvCountHouse.setText("-->住宅:" + houseCount + "条");
                        } else if (o.getString("tableName").equals("building")) {
                            buildingCount = o.getString("count");
                            tvCountBuilding.setText("楼栋:" + buildingCount + "条");
                        } else if (o.getString("tableName").equals("marker")) {
                            markerCount = o.getString("count");
                            tvCountMarker.setText("场所:" + markerCount + "条");
                        }
                    }

                } catch (JSONException e) {

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            saveLocation();
            mapView.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            saveLocation();
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            saveLocation();
            mapView.onDestroy();
        }

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    //登陆
    @OnClick(R.id.btn_login)
    public void onLogin() {
        System.out.println("login");
        if (isLogin) {
            DialogUtils.showAlert(this, "提示信息", "确定要退出吗？", "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitLogin();
                }
            }, "取消", null);

        } else {
            LoginDialog loginDialog = new LoginDialog(this, R.style.dialog);
            loginDialog.show();
        }
    }

    @OnClick(R.id.btn_user_location)
    public void onUserLocation() {
//        location.requestLocation();
        UserLocation userLocation = new UserLocation(this,mapView);
    }

    @OnClick(R.id.btn_mark_location)
    public void onMarkerLocation() {
        double lat = Hawk.get(SPKey.MARK_LAT, 34.80843);
        double lng = Hawk.get(SPKey.MARK_LNG, 112.43888);
        mapView.setScene(
                MapScene.createFromLocationAndZoomLevel(new Geopoint(lat, lng), 17),
                MapAnimationKind.NONE);
    }

    @OnClick(R.id.btn_layer)
    public void onLayer() {
        layerIndex++;
        if (layerIndex > 3) {
            layerIndex = 0;
        }

        if (layerIndex == 0) {
            mapView.setMapStyleSheet(MapStyleSheets.aerial());
        } else if (layerIndex == 1) {
            mapView.setMapStyleSheet(MapStyleSheets.aerialWithOverlay());
        } else if (layerIndex == 2) {
            mapView.setMapStyleSheet(MapStyleSheets.roadCanvasLight());
        } else if (layerIndex == 3) {
            mapView.setMapStyleSheet(MapStyleSheets.vibrantLight());
        }
    }

    private void exitLogin() {
        btnLogin.setImageResource(R.drawable.login);
        tvLoginInfo.setText("");
        Hawk.delete(SPKey.USER_ID);
        Hawk.delete(SPKey.USER_NAME);
        Hawk.delete(SPKey.USER_TYPE);
        Hawk.delete(SPKey.REAL_NAME);
        User.id = 0L;
        User.realName = null;
        User.userType = null;
        User.userName = null;

        //退出登录后刷新marker
        TypeEvent.send(TypeEvent.REFRESH_MARKERS);
        //如果有cameraInfo，删除
        tvCountBuilding.setVisibility(View.GONE);
        tvCountHouse.setVisibility(View.GONE);
        tvCountMarker.setVisibility(View.GONE);
        isLogin = false;
    }

    private void initMapView() {
        mapView = new MapView(this, MapRenderMode.VECTOR);  // or use MapRenderMode.RASTER for 2D map
        mapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);
        mapView.setMapStyleSheet(MapStyleSheets.aerial());
        ((FrameLayout) findViewById(R.id.map_view)).addView(mapView);

        //设置map显示范围
        setMapScene();
        setMapUserInterface(mapView);
        setMapListener(mapView);
        initUserView();
    }

    private void initUserView() {
        long userID = Hawk.get(SPKey.USER_ID, 0L);
        if (userID > 0) {
            User.id = userID;
            User.userName = Hawk.get(SPKey.USER_NAME, "");
            User.userType = Hawk.get(SPKey.USER_TYPE, "");
            User.realName = Hawk.get(SPKey.REAL_NAME, "");
            tvLoginInfo.setText(User.realName);
            isLogin = true;
            btnLogin.setImageResource(R.drawable.ic_logout);
            tvCountBuilding.setVisibility(View.VISIBLE);
            tvCountHouse.setVisibility(View.VISIBLE);
            tvCountMarker.setVisibility(View.VISIBLE);
        }
    }

    private void setMapListener(MapView mapView) {
        MapListener mapListener = new MapListener(mapView);
        mapView.addOnMapCameraChangedListener(mapListener);
        mapView.addOnMapHoldingListener(mapListener);
        mapView.addOnMapCameraChangingListener(mapListener);
    }

    private void saveLocation() {
        System.out.println();
        double lat = mapView.getCenter().getPosition().getLatitude();
        double lng = mapView.getCenter().getPosition().getLongitude();
        double zoom = mapView.getZoomLevel();
        Hawk.put(SPKey.EXIT_LAT, lat);
        Hawk.put(SPKey.EXIT_LNG, lng);
        Hawk.put(SPKey.EXIT_ZOOM_LEVEL, zoom);
        System.out.println("save location" + " lat:" + lat + "  lng:" + lng + " zoom:" + zoom);
    }

    private void setMapScene() {
        double lat = Hawk.get(SPKey.EXIT_LAT, 0.0);
        double lng = Hawk.get(SPKey.EXIT_LNG, 0.0);
        double zoom = Hawk.get(SPKey.EXIT_ZOOM_LEVEL, 0.0);
        System.out.println("get location" + "lat:" + lat + " lng:" + lng + "zoom:" + zoom);

        if (lat != 0.0) {
            mapView.setScene(
                    MapScene.createFromLocationAndZoomLevel(new Geopoint(lat, lng), zoom),
                    MapAnimationKind.NONE);
        } else {
            mapView.setScene(
                    MapScene.createFromLocationAndZoomLevel(Meng_jin, 13),
                    MapAnimationKind.DEFAULT);
        }
    }

    private void setMapUserInterface(MapView mapView) {
        MapUserInterfaceOptions option = mapView.getUserInterfaceOptions();
        option.setZoomButtonsVisible(false);
        option.setCompassButtonVisible(false);
        option.setTiltButtonVisible(false);
        option.setRotateGestureEnabled(false);
        option.setCopyrightDisplay(CopyrightDisplay.ALLOW_HIDING);
        option.setTiltGestureEnabled(false);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @Subscribe
    public void onTypeEvent(TypeEvent event) {
        if (event.type == TypeEvent.LOGIN) {
            tvLoginInfo.setText(User.realName);
            btnLogin.setImageResource(R.drawable.ic_logout);
            isLogin = true;
            tvCountBuilding.setVisibility(View.VISIBLE);
            tvCountHouse.setVisibility(View.VISIBLE);
            tvCountMarker.setVisibility(View.VISIBLE);
            getCountInfo();
        } else if (event.type == TypeEvent.GET_COUNT_INFO) {
            getCountInfo();
        }
    }

    //显示当前地图的级别
    @Subscribe
    public void showLevel(MapLevelChangeEvent event) {
        double mapZoom = event.mapZoom;
        String str = String.format("%.2f", mapZoom);
        tvZoomLevel.setText(str);
    }

    @Subscribe
    public void showInfo(InfoEvent e) {
        if (e.isShow) {
            tvInfo.setVisibility(View.VISIBLE);
            tvInfo.setText(e.message);
        } else {
            tvInfo.setVisibility(View.GONE);
        }
    }

}
