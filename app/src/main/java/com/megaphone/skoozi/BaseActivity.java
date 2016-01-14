package com.megaphone.skoozi;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;

/**
 * Created by ahmadul.hassan on 2016-01-09.
 */
abstract public class BaseActivity extends AppCompatActivity {
    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

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
