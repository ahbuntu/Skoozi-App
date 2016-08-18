package com.megaphone.skoozi.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class PermissionUtil {
    private static final String FINE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int REQUEST_PERMISSION_LOCATION = 991;

    @TargetApi(23)
    public static Location tryGetLatestLocation(Activity activity, GoogleApiClient googleApiClient)
    {
        if (Build.VERSION.SDK_INT < 23) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        int fine = ContextCompat.checkSelfPermission(activity, FINE_LOCATION_PERMISSION);
        int coarse = ContextCompat.checkSelfPermission(activity, COARSE_LOCATION_PERMISSION);
        if (fine == PackageManager.PERMISSION_GRANTED && coarse == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, FINE_LOCATION_PERMISSION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, COARSE_LOCATION_PERMISSION)) {
                // TODO: 2016-01-05 show explanation *async&. After explanation try again
            } else {
                String[] permissions = new String[]{FINE_LOCATION_PERMISSION, COARSE_LOCATION_PERMISSION};
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_LOCATION);
                return null;
            }
        }
        return null;
    }
}
