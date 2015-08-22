package com.megaphone.skoozi.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

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
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }
}
