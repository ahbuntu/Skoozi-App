package com.megaphone.skoozi;

import android.app.Application;

/**
 * Created by ahmadul.hassan on 2015-07-26.
 */
public class SkooziApplication extends Application {
    private static SkooziApplication singleton;

    // Returns the application instance
    public static SkooziApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }






}
