package com.megaphone.skoozi;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.megaphone.skoozi.util.AccountUtil;

/**
 * Created by ahmadul.hassan on 2015-07-26.
 */
public class SkooziApplication extends Application {
    private static SkooziApplication singleton;
    private static Account userAccount;
    public static String accessToken;

    // Returns the application instance
    public static SkooziApplication getInstance() {
        return singleton;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public static void setUserAccount(Context context, String userEmail) {
        userAccount = AccountUtil.getAccountForName(context, userEmail);
    }

    public static Account getUserAccount() {
        return userAccount;
    }

}
