package com.megaphone.skoozi.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.megaphone.skoozi.SkooziApplication;

public class SharedPrefsUtil {
    public static final String KEY_ACCOUNT_NAME = "skoozi.KEY_ACCOUNT_NAME";
    public static final String KEY_USER_NICKNAME = "skoozi.KEY_USER_NICKNAME";

    private static final String SKOOZI_SETTINGS = "skoozi_settings";

    // http://stackoverflow.com/questions/16106260/thread-safe-singleton-class
    private static class Holder {
        static final SharedPreferences SETTINGS_INST = SkooziApplication.getInstance()
                .getApplicationContext().getSharedPreferences(SKOOZI_SETTINGS, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getInstance() {
        return Holder.SETTINGS_INST;
    }

    public static SharedPreferences.Editor getEditor() {
        return getInstance().edit();
    }
}
