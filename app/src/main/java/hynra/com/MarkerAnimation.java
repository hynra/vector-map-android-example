package hynra.com;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.util.Property;

import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;

public class MarkerAnimation {

    public void animate(final MapView mapView, final MarkerView marker, final LatLng finalPosition, final long duration) {
        LatLngInterpolator interpolator = new LatLngInterpolator.LinearFixed();
        animateMarkerToICS(mapView, marker, finalPosition, interpolator, duration);

    }

    static void animateMarkerToICS(final MapView mapView, final MarkerView marker, LatLng finalPosition, final LatLngInterpolator latlngInterpolator, final long durationInMs) {
        TypeEvaluator<LatLng> typeEvaluator = (fraction, startValue, endValue) -> {
            mapView.invalidate();
            return latlngInterpolator.interpolate(fraction, startValue, endValue);
        };
        Property<MarkerView, LatLng> property = Property.of(MarkerView.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(durationInMs);
        animator.start();
    }
}