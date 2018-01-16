package hynra.com;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    private boolean isApprove;
    private MapboxMap mapboxMap;
    private MarkerView mainMarker;
    private LatLng position1 = new LatLng(-6.897286, 107.612867);
    private LatLng position2 = new LatLng(-6.891407, 107.613210);
    private int count = 0;
    MarkerAnimation markerAnimation;


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

        markerAnimation = new MarkerAnimation();
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

        Icon icon = IconFactory.getInstance(this).fromResource(R.mipmap.bike);
        mainMarker = mapboxMap.addMarker(new MarkerViewOptions()
                .position(position1)
                .icon(icon)
        );

        startHttpReceive();
    }





    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {


        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
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




    private void startHttpReceive() {
        final int total = 67;

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(count == total){
                    count = 0;
                }else count += 1;
                AndroidNetworking.post("http://167.205.7.226:33302/receive")
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .addBodyParameter("counter", ""+count)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                  //  Log.i("test", response.toString());
                                    position2.setLatitude(response.getJSONObject("resp").getDouble("Latitude"));
                                    position2.setLongitude(response.getJSONObject("resp").getDouble("Longitude"));
                                    mainMarker.setRotation((float) MarkerBearing.bearing(
                                            mainMarker.getPosition().getLatitude(), mainMarker.getPosition().getLongitude(),
                                            position2.getLatitude(), position2.getLongitude()
                                    ));

                                    /*ValueAnimator markerAnimator = ObjectAnimator.ofObject(mainMarker, "position",
                                            new LatLngEvaluator(), mainMarker.getPosition(), position2);
                                    markerAnimator.setDuration(1000);
                                    markerAnimator.setInterpolator(new LinearInterpolator());
                                    markerAnimator.start();
                                    mapView.invalidate();*/
                                    markerAnimation.animate(mapView, mainMarker, position2, 1000);
                                    mapboxMap.animateCamera(CameraUpdateFactory
                                            .newLatLng(position2)
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            @Override
                            public void onError(ANError error) {
                                Log.i("test", error.getErrorDetail());
                            }
                        });
            }

            public void onFinish() {

            }
        }.start();
    }



}
