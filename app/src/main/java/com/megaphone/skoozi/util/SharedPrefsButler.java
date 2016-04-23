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

    public static LatLng getHomeArea() {
        double lat = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_HOME_AREA_LAT, 0));
        double lng = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_HOME_AREA_LNG, 0));
        return (lat == 0 && lng == 0) ? null : new LatLng(lat, lng);
    }

    public static void putFutureHomeArea(LatLng location) {
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_HOME_AREA_LAT, Double.doubleToRawLongBits(location.latitude))
                .apply();
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_HOME_AREA_LNG, Double.doubleToRawLongBits(location.longitude))
                .apply();
    }

    public static LatLng getWorkArea() {
        double lat = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_WORK_AREA_LAT, 0));
        double lng = Double.longBitsToDouble(SharedPrefsUtil.getInstance()
                .getLong(SharedPrefsUtil.KEY_WORK_AREA_LNG, 0));
        return (lat == 0 && lng == 0) ? null : new LatLng(lat, lng);
    }

    public static void putFutureWorkArea(LatLng location) {
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_WORK_AREA_LAT, Double.doubleToRawLongBits(location.latitude))
                .apply();
        SharedPrefsUtil.getEditor()
                .putLong(SharedPrefsUtil.KEY_WORK_AREA_LNG, Double.doubleToRawLongBits(location.longitude))
                .apply();
    }
}
