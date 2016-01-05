package com.megaphone.skoozi.util;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;

/**
 * Created by ahmadulhassan on 2015-07-01.
 */
public class ConnectionUtil {

    public static boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                SkooziApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isGPSEnabled (Context mContext){
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void displayNetworkErrorMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_network_message, Snackbar.LENGTH_LONG)
                .show();
    }

    public static void displayGpsErrorMessage(CoordinatorLayout layoutView, final Context context) {
        Snackbar.make(layoutView, R.string.no_gps_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_enable_gps, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .show();
    }
}
