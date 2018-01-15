package hynra.com;

import android.Manifest;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    private boolean isApprove;
    private MapboxMap mapboxMap;
    private MarkerView mainMarker;
    private LatLng position1 = new LatLng(-6.897286, 107.612867);
    private LatLng position2 = new LatLng(-6.891407, 107.613210);

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl(getResources().getString(R.string.styleUrl));
        checkPermission();

    }

    private void initMap(){




        mapboxMap.getUiSettings().setAllGesturesEnabled(true);
        mapboxMap.getUiSettings().setLogoEnabled(false);
        mapboxMap.getUiSettings().setLogoGravity(Gravity.TOP);
        mapboxMap.getUiSettings().setAttributionGravity(Gravity.TOP);
        mapboxMap.getUiSettings().setAttributionEnabled(false);
        mapboxMap.moveCamera(CameraUpdateFactory
                .newLatLng(position1)
        );
        mapboxMap.addPolygon(new PolygonOptions()
                .addAll(polygonCircleForCoordinate(position1, 500.0))
                .strokeColor(Color.parseColor("#000000"))
                .fillColor(Color.parseColor("#55121212")));

        mainMarker = mapboxMap.addMarker(new MarkerViewOptions()
                .position(position1)
                .title("Bandung")
                .snippet("Bandung")
        );

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {



                mainMarker.setRotation((float) MarkerBearing.bearing(
                        position1.getLatitude(), position1.getLongitude(),
                        position2.getLatitude(), position2.getLongitude()
                ));
                mainMarker.setPosition(position2);
                mapboxMap.animateCamera(CameraUpdateFactory
                        .newLatLng(position2)
                );
                mapView.invalidate();
            }
        }.start();
    }





    private ArrayList<LatLng> polygonCircleForCoordinate(LatLng location, double radius){
        int degreesBetweenPoints = 8; //45 sides
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = location.getLatitude() * Math.PI / 180;
        double centerLonRadians = location.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); //array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        return polygons;
    }



    private void checkPermission(){
        isApprove = false;
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
                    isApprove = true;
                    Log.i("PERMISSION", response.getPermissionName()+" permission granted");

                }

                for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                    isApprove = false;
                    new AlertDialog.Builder(MainActivity.this).setTitle("Persetujuan Dibutuhkan")
                            .setMessage("Aplikasi ini membutuhkan fitur yang memerlukan persetujuan Anda")
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                                finish();

                            })
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                                checkPermission();

                            })
                            .setOnDismissListener(dialog -> finish())
                            .show();
                }

                if(isApprove){
                    mapView.getMapAsync(MainActivity.this);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                showPermissionRationale(token);
            }

        }).check();
    }



    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle("Persetujuan Dibutuhkan")
                .setMessage("Aplikasi ini membutuhkan fitur yang memerlukan persetujuan Anda")
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    token.cancelPermissionRequest();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    token.continuePermissionRequest();
                })
                .setOnDismissListener(dialog -> token.cancelPermissionRequest())
                .show();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        initMap();
    }
}
