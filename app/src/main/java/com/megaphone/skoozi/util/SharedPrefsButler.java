package com.megaphone.skoozi.util;

import com.google.android.gms.maps.model.LatLng;

public class SharedPrefsButler {

    public static String getUserNickname() {
        return SharedPrefsUtil.getInstance().getString(SharedPrefsUtil.KEY_USER_NICKNAME, null);
    }

    public static void putFutureUserNickname(String userNickname) {
        SharedPrefsUtil.getEditor()
                .putString(SharedPrefsUtil.KEY_USER_NICKNAME, userNickname).apply();
    }

    public static LatLng getHomeCoords() {
        double lat = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_HOME_AREA_LAT, 0));
        double lng = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_HOME_AREA_LNG, 0));
        return (lat == 0 && lng == 0) ? null : new LatLng(lat, lng);
    }

    public static void putFutureHomeCoords(LatLng location) {
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_HOME_AREA_LAT, Double.doubleToRawLongBits(location.latitude))
                .apply();
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_HOME_AREA_LNG, Double.doubleToRawLongBits(location.longitude))
                .apply();
    }

    public static LatLng getWorkCoords() {
        double lat = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_WORK_AREA_LAT, 0));
        double lng = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_WORK_AREA_LNG, 0));
        return (lat == 0 && lng == 0) ? null : new LatLng(lat, lng);
    }

    public static void putFutureWorkCoords(LatLng location) {
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_WORK_AREA_LAT, Double.doubleToRawLongBits(location.latitude))
                .apply();
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_WORK_AREA_LNG, Double.doubleToRawLongBits(location.longitude))
                .apply();
    }
}
