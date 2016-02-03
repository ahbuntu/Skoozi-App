package com.megaphone.skoozi.util;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.R;

public class MapDecorator {
    private static final int DEFAULT_ZOOM = 11;
    private static final int RADIUS_TRANSPARENCY = 64; //75%

    public static void drawLocationMarker(GoogleMap map, Location origin) {
        if (map == null || origin == null) return;

        LatLng searchLocation = new LatLng(origin.getLatitude(), origin.getLongitude());
        map.addMarker(new MarkerOptions()
                .position(searchLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Current location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, DEFAULT_ZOOM));
    }

    public static void drawNotificationArea(Context context, GoogleMap map,
                                            Location origin, int radius) {
        if (map == null || origin == null ) return;

        int radiusColorRgb = ContextCompat.getColor(context, R.color.accent_material_light);
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(origin.getLatitude(), origin.getLongitude()))
                .fillColor(Color.argb(RADIUS_TRANSPARENCY,
                        Color.red(radiusColorRgb),
                        Color.green(radiusColorRgb),
                        Color.blue(radiusColorRgb)))
                .radius(radius*1000); // need this in metres
        map.addCircle(circleOptions);
    }
}
