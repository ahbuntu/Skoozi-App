package com.megaphone.skoozi;

import android.accounts.Account;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;
import com.megaphone.skoozi.util.SharedPrefsUtil;

import io.fabric.sdk.android.Fabric;

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
        if (userAccount == null) {
            setUserAccount(singleton.getApplicationContext(),
                    SharedPrefsUtil.getInstance().getString(SharedPrefsUtil.ACCOUNT_NAME_KEY, null));
        }
        return userAccount;
    }

    public static boolean hasUserAccount() {
        return userAccount != null;
    }

}
