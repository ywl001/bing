package com.ywl01.bing.map;

import static com.microsoft.maps.platformabstraction.IO.getApplicationContext;

import android.Manifest;
import android.app.Activity;
import android.location.Location;

import com.microsoft.maps.GPSMapLocationProvider;
import com.microsoft.maps.LocationChangedListener;
import com.microsoft.maps.MapUserLocation;
import com.microsoft.maps.MapUserLocationTrackingInterruptedEventArgs;
import com.microsoft.maps.MapUserLocationTrackingState;
import com.microsoft.maps.MapView;
import com.microsoft.maps.OnMapUserLocationTrackingInterruptedListener;
import com.ywl01.bing.utils.MPermissionUtils;

public class UserLocation implements MPermissionUtils.OnPermissionListener {

    private Activity activity;
    private String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private MapView mapView;
    private GPSMapLocationProvider gpsProvider;

    public UserLocation(Activity activity,MapView mapView){
        this.activity = activity;
        this.mapView  = mapView;
        initTrackingState();
    }

    public interface OnLocationChangedListener{
        void onListener(Location location);
    }

    private OnLocationChangedListener locationChangedListener;

    public void setOnLocationChangeListener(OnLocationChangedListener listener){
        this.locationChangedListener = listener;
    }

    private void initTrackingState(){
        MapUserLocation userLocation = mapView.getUserLocation();
        gpsProvider = new GPSMapLocationProvider.Builder(getApplicationContext()).build();
        gpsProvider.setMinTime(500);
        MapUserLocationTrackingState userLocationTrackingState = userLocation.startTracking(gpsProvider);
        if (userLocationTrackingState == MapUserLocationTrackingState.PERMISSION_DENIED) {
            // request for user location permissions and then call startTracking again
            System.out.println("user location no permission");
            MPermissionUtils.requestPermissionsResult(activity, 100, perms, this);
        } else if (userLocationTrackingState == MapUserLocationTrackingState.READY) {
            // handle the case where location tracking was successfully started
            System.out.println("user location ready");
//            userLocation.startTracking(gpsProvider);
            gpsProvider.startTracking();

            gpsProvider.addLocationChangedListener(new LocationChangedListener() {
                @Override
                public void onLocationChanged(Location location) {
                    System.out.println("get location success");
                    System.out.println(location.getLatitude());

                    locationChangedListener.onListener(location);
                }
            });
//            System.out.println(gpsProvider.getLastLocation().getLatitude());
        } else if (userLocationTrackingState == MapUserLocationTrackingState.DISABLED) {
            System.out.println("user location disabled");
            // handle the case where all location providers were disabled
        }
    }

    public void requestLocation(){
        this.gpsProvider.startTracking();

    }

    @Override
    public void onPermissionGranted() {
        MPermissionUtils.showTipsDialog(activity);
    }

    @Override
    public void onPermissionDenied() {
        gpsProvider.addLocationChangedListener(new LocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println(location.getLatitude());
            }
        });
    }
}
