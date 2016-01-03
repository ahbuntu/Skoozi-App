package com.megaphone.skoozi.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by ahmadul.hassan on 2016-01-03.
 */
public class GoogleApiClientBroker {
    private static final String TAG = "GoogleApiClientBroker";
    private static final int GOOGLE_API_REQUEST_RESOLVE_ERROR = 1001; // Request code to use when launching the resolution activity
    private static final String DIALOG_ERROR = "dialog_error"; // Unique tag for the error dialog fragment

    private Activity activity;
    private GoogleApiClient googleApiClient;
    private boolean resolvingError = false;// Bool to track whether the app is already resolving an error
    private BrokerResultListener brokerResultListener;

    public interface BrokerResultListener {
        void onConnected();
    }

    public GoogleApiClientBroker(Activity activity) {
        this.activity = activity;
    }

    private GoogleApiClient.ConnectionCallbacks googleApiConnectionCallbacks = new
            GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    if (brokerResultListener != null) brokerResultListener.onConnected();
                }

                @Override
                public void onConnectionSuspended(int cause) {
                    // The connection has been interrupted. Disable any UI components that depend on Google APIs
                    // until onConnected() is called.
                    Log.d(TAG, "connection to Google API suspended" + cause);
                }
            };

    private GoogleApiClient.OnConnectionFailedListener googleApiConnectionFailure = new
            GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult result){
                    Log.d(TAG, "connection to Google API failed for " + result.toString());
                    if (resolvingError) {
                        return; // Already attempting to resolve an error.
                    } else if (result.hasResolution()) {
                        try {
                            resolvingError = true;
                            result.startResolutionForResult(activity, GOOGLE_API_REQUEST_RESOLVE_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            googleApiClient.connect(); // There was an error with the resolution intent. Try again.
                        }
                    } else {
                        resolvingError = true;
                        displayGoogleApiErrorMessage(result.getErrorCode());
                        // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                    }
                }
            };

    public synchronized GoogleApiClient buildLocationClient(BrokerResultListener listener){
        // https://developers.google.com/android/guides/api-client
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(activity)
                 .addConnectionCallbacks(googleApiConnectionCallbacks)
                 .addOnConnectionFailedListener(googleApiConnectionFailure)
                 .addApi(LocationServices.API)
                 .build();
        }
        return googleApiClient;
    }

    /**
     *  Creates a dialog for an error message about connecting to Google Api
     */
    private void displayGoogleApiErrorMessage(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
//        dialogFragment.show(getSupportFragmentManager(), "errordialog"); // FIXME: 2016-01-03 needs ref
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        resolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), GOOGLE_API_REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.d(TAG, "want to dismiss dialog");
//            getActivity().onDialogDismissed(); // FIXME: 2016-01-03 need activity ref
        }
    }
}
