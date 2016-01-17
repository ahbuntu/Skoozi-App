package com.megaphone.skoozi.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;

public class AccountUtil {
    private static final String TAG = "AccountUtil";
    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static final String EXTRA_USER_ACCOUNT_ACTION = "skoozi.extra.USER_ACCOUNT_ACTION";

    private static final String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};

    public interface GoogleAuthTokenExceptionListener {
        void handleGoogleAuthException(UserRecoverableAuthException exception);
    }

    public static void pickUserAccount(Activity activity, String action) {
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

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public static void resolveAuthExceptionError(Activity activity, Exception exception) {
        if (exception instanceof GooglePlayServicesAvailabilityException) {
            Log.d(TAG, "GooglePlayServicesAvailabilityException received");
            // The Google Play services APK is old, disabled, or not present.
            // Show a dialog created by Google Play services that allows
            // the user to update the APK
            int statusCode = ((GooglePlayServicesAvailabilityException) exception)
                    .getConnectionStatusCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                    activity,
                    AccountUtil.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            dialog.show();
        } else if (exception instanceof UserRecoverableAuthException) {
            Log.d(TAG, "UserRecoverableAuthException received");
            // Unable to authenticate, such as when the user has not yet granted
            // the app access to the account, but the user can fix this.
            // Forward the user to an activity in Google Play services.
            Intent intent = ((UserRecoverableAuthException) exception).getIntent();
            activity.startActivityForResult(intent,
                    AccountUtil.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
        } else {
            Log.d(TAG, "Unknow Exception. Message: " + exception.getMessage());
            throw new UnknownError();
        }
    }

    public static void displayAccountSignInErrorMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_account_login_message, Snackbar.LENGTH_LONG)
                .show();
    }

    public static void displayAccountSignedInMessage(CoordinatorLayout layoutView, String name) {
        Snackbar.make(layoutView,
                layoutView.getContext().getString(R.string.account_login_success_message, name),
                Snackbar.LENGTH_LONG).show();
    }
}
