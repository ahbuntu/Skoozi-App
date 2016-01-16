package com.megaphone.skoozi;

import android.accounts.AccountManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;

/**
 * Created by ahmadul.hassan on 2016-01-09.
 */
abstract public class BaseActivity extends AppCompatActivity {
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";

    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

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
                    // Receiving a result from the AccountPicker
                    SkooziApplication.setUserAccount(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    String action = data.getStringExtra(AccountUtil.EXTRA_USER_ACCOUNT_ACTION); //can return null
                    if (action != null && action.equals(ACTION_NEW_QUESTION)) {
                        throw new RuntimeException("Need to decide what to do");
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
//                    AccountUtil.displayAccountLoginErrorMessage(coordinatorLayout);
                    throw new RuntimeException("Need to decide what to do");
                }
                return;
            case AccountUtil.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR:
//            case REQUEST_CODE_RECOVER_FROM_AUTH_ERROR: //todo: figure out HOW/WHEN this is received
                if (resultCode == RESULT_OK) {
                    // Receiving a result that follows a GoogleAuthException, try auth again
//                    getUsername();
                }
                return;
            case GoogleApiClientBroker.GOOGLE_API_REQUEST_RESOLVE_ERROR:
//                resolvingGoogleApiError = false; // TODO: 2016-01-13 is this needed?
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                        googleApiClient.connect();
                    }
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
        googleApiClient = googleApiBroker
                .getGoogleApiClient(new GoogleApiClientBroker.BrokerResultListener() {
                    @Override
                    public void onConnected() {
                        onGoogleApiConnected();
                    }
                });
    }
//                initGoogleApiClient();
//                googleApiClient.connect();
//        } else {
//            if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
//                googleApiClient.connect();
//            }
//            updateLatestLocation();
//        }

//        if (ConnectionUtil.hasGps(this, coordinatorLayout)){
//
//        }
//    }

    // TODO: 2016-01-13 this two methods can be extracted out to an interface
    protected void connectToGoogleApi() {
        if (getGoogleApiClient() == null) return;
        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                googleApiClient.connect();
        }
    }

    protected void onGoogleApiConnected() {}
}
