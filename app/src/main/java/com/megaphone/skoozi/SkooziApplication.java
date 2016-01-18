package com.megaphone.skoozi;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;
import io.fabric.sdk.android.Fabric;

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
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        singleton = this;
    }

    public static void setUserAccount(Context context, String userEmail) {
        userAccount = AccountUtil.getAccountForName(context, userEmail);
    }

    public static Account getUserAccount() {
        return userAccount;
    }

    public static boolean hasUserAccount() {
        return userAccount != null;
    }

}
