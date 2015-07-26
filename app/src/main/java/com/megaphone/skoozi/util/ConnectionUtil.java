package com.megaphone.skoozi.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

}
