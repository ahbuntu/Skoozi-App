package com.megaphone.skoozi.base;

import android.accounts.AccountManager;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;
import com.megaphone.skoozi.nearby.MainActivity;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;

abstract public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

    protected CoordinatorLayout coordinatorLayout;

    protected AccountUtil.GoogleAuthTokenExceptionListener tokenListener = new AccountUtil.GoogleAuthTokenExceptionListener() {
        @Override
        public void handleGoogleAuthException(final UserRecoverableAuthException exception) {
            // Because this call comes from the IntentService, we must ensure that the following
            // code instead executes on the UI thread.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountUtil.resolveAuthExceptionError(BaseActivity.this, exception);
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this, "my activity", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SkooziApplication.getUserAccount() == null) AccountUtil.pickUserAccount(this, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) googleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AccountUtil.REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    // Received valid result from the AccountPicker
                    googleAccountSelected(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    googleAccountNotSelected();
                }
                return;
            case AccountUtil.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR:
//            case REQUEST_CODE_RECOVER_FROM_AUTH_ERROR: //todo: figure out HOW/WHEN this is received
                if (resultCode == RESULT_OK) {
                    // Receiving a result that follows a GoogleAuthException, try auth again
//                    getUsername();
                    oAuthAuthenticationGranted();
                } else {
                    oAuthAuthenticationDenied();
                }
                return;
            case GoogleApiClientBroker.GOOGLE_API_REQUEST_RESOLVE_ERROR:
//                resolvingGoogleApiError = false; // TODO: 2016-01-13 is this needed?
                if (resultCode == RESULT_OK) {
                    googleApiConnectionResolved();
                    // Make sure the app is not already connected or attempting to connect
                    if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                        googleApiClient.connect();
                    }
                } else {
                    googleApiConnectionError();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    public GoogleApiClient getGoogleApiClient() {
        if (googleApiClient == null) initGoogleApiClient();
        return googleApiClient;
    }

    private void initGoogleApiClient() {
        if (googleApiBroker == null) googleApiBroker = new GoogleApiClientBroker(this);
        googleApiClient = googleApiBroker.getGoogleApiClient(
                new GoogleApiClientBroker.BrokerResultListener() {
                    @Override
                    public void onConnected() {
                        onGoogleApiConnected();
                    }
                });
    }

    @CallSuper
    protected void googleAccountSelected(String accountName) {
        Log.d(TAG, "Google Account selected via account picker");
        SkooziApplication.setUserAccount(this, accountName);
        AccountUtil.displayAccountSignedInMessage(coordinatorLayout, accountName);
    }

    @CallSuper
    protected void googleAccountNotSelected() {
        Log.d(TAG, "Google Account not selected from account picker");
    }

    @CallSuper
    protected void oAuthAuthenticationGranted() {
        Log.d(TAG, "OAuth authenticated granted");
    }

    @CallSuper
    protected void oAuthAuthenticationDenied() {
        Log.d(TAG, "OAuth authenticated denied");
    }

    @CallSuper
    protected void googleApiConnectionResolved() {
        Log.d(TAG, "Google API connection resolved by user");
    }

    @CallSuper
    protected void googleApiConnectionError() {
        Log.d(TAG, "Google API connection error");
    }

    // TODO: 2016-01-13 this two methods can be extracted out to an interface
    protected void connectToGoogleApi() {
        if (getGoogleApiClient() == null) return;
        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                googleApiClient.connect();
        }
    }

    @CallSuper
    protected void onGoogleApiConnected() {
        Log.d(TAG, "Googple API Connection established");
    }
}
