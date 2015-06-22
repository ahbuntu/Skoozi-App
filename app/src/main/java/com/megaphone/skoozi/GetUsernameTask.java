package com.megaphone.skoozi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

/**
 * Created by ahmadul.hassan on 2015-05-31.
 */
public class GetUsernameTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetUsernameTask";
    Activity mActivity;
    String mScope;
    String mEmail;
    Account mAccount;

    GetUsernameTask(Activity activity, String name, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = name;
    }


    GetUsernameTask(Activity activity, Account account) {
        this.mActivity = activity;
        this.mAccount = account;
    }

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
//            String token = fetchToken();
            String token = getAuthToken(mAccount);
            if (token != null) {
                // **Insert the good stuff here.**
                // Use the token to access the user's Google data.
//                    ...
            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TODO: Check for network connectivity before starting the AsyncTask.
            Log.e(TAG, e.getMessage());
        } catch (AccountsException e) {
            // TODO: handle properly
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            //TODO: this should be made more robust - possibly through an interface
            MainActivity mainActivityRef = (MainActivity) mActivity;
            mainActivityRef.handleGoogleAuthTokenException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.e(TAG, fatalException.getMessage());
        }
        return null;
    }

    private String getAuthToken(Account account) throws AccountsException, IOException {
        AccountManager manager = AccountManager.get(mActivity);
        AccountManagerFuture<Bundle> future =  manager.getAuthToken(account, "ah", null, false, null, null);
        Bundle bundle = future.getResult();
        Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
        if (intent != null) {
            //Here you should start intent or throw your own exception that takes the intent and passes it to the other (preferably view) class.
            //This intent is a popup saying that your application want to access accounts. It appears once per installation.
        }
        return bundle.getString(AccountManager.KEY_AUTHTOKEN);
    }
}
