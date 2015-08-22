package com.megaphone.skoozi.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.megaphone.skoozi.MainActivity;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;

/**
 * Created by ahmadulhassan on 2015-07-01.
 */
public class AccountUtil {
    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static final String EXTRA_USER_ACCOUNT_ACTION = "com.megaphone.skoozi.extra.USER_ACCOUNT_ACTION";

    public static void pickUserAccount(Activity activity, String action) {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null); //alwaysPromptForAccount = false
        intent.putExtra(EXTRA_USER_ACCOUNT_ACTION, action);
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    public static Account getAccountForName(Context context, String username) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google"); // gmail.com is within google.com type
        if (accounts != null) {
            for (Account account : accounts) {
                if (account.name.equals(username)) {
                    return account;
                }
            }
        }
        return null;
    }

    public static void displayAccountLoginErrorMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_account_login_message, Snackbar.LENGTH_LONG)
                .show();
    }
}
