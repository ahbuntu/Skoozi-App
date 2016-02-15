package com.megaphone.skoozi;

import android.accounts.Account;
import android.app.Application;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.SharedPrefsUtil;

import io.fabric.sdk.android.Fabric;

public class SkooziApplication extends Application {
    private static final String TAG = SkooziApplication.class.getSimpleName();

    public static String accessToken;
    private static SkooziApplication singleton;
    private static Account userAccount; // initialized onCreate() of application
    private static boolean signinAckDisplayed;

    // Returns the application instance
    public static SkooziApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        singleton = this;
        getUserAccount();
    }

    public static void setUserAccount(String userEmail) {
        userAccount = AccountUtil.getAccountForName(singleton.getApplicationContext(), userEmail);
    }

    public static Account getUserAccount() {
        if (userAccount == null) {
            setUserAccount(SharedPrefsUtil.getInstance()
                    .getString(SharedPrefsUtil.KEY_ACCOUNT_NAME, null));
        }
        return userAccount;
    }

    public static boolean hasUserAccount() {
        return userAccount != null;
    }

    public static boolean shouldDisplaySignInAck() {
        return !signinAckDisplayed;
    }

    public static void displaySignInAck(CoordinatorLayout coordinatorLayout) {
        Account account = getUserAccount();
        if (account == null) {
            Log.d(TAG, "displaySignInAck: user does not have a saved user account");
            return;
        }
        AccountUtil.displayAccountSignedInMessage(coordinatorLayout, account.name);
        signinAckDisplayed = true;
    }
}
