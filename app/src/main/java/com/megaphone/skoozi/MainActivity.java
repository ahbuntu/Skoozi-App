package com.megaphone.skoozi;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.util.ConnectionUtil;

/**
 * Material design sliding tab implementation taken from
 * http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
 */
/**
 * Material design toolbar implementation taken from
 * http://antonioleiva.com/material-design-everywhere/
 */
/**
 * Refer to this for backwards compatibility
 * http://stackoverflow.com/questions/26449454/extending-activity-or-actionbaractivity
 */
public class MainActivity extends AppCompatActivity
                            implements OnMapReadyCallback, NearbyFragment.OnMapQuestionsCallback, NearbyRecyclerViewAdapter.OnQuestionItemSelected,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    public static final String BROADCAST_QUESTIONS_LIST_RESULT = "com.megaphone.skoozi.broadcast.QUESTIONS_LIST_RESULT";
    public static final String EXTRAS_QUESTIONS_LIST  = "com.megaphone.skoozi.extras.QUESTIONS_LIST";

    //http://stackoverflow.com/questions/10400428/can-i-use-androids-accountmanager-for-getting-oauth-access-token-for-appengine
    private final static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private final static String USERINFO_PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String SCOPE = "oauth2:" + USERINFO_EMAIL_SCOPE + " " + USERINFO_PROFILE_SCOPE;

    private static final int GOOGLE_API_REQUEST_RESOLVE_ERROR = 1001; // Request code to use when launching the resolution activity
    public final static int DEFAULT_RADIUS_METRES = 10000;
    private static final LatLng DEFAULT_LOCATION = new LatLng(43.6532,-79.3832);
    private static final int DEFAULT_ZOOM = 10;
    private static final int RADIUS_TRANSPARENCY = 64; //75%

    private CoordinatorLayout mLayoutView;
    private GoogleMap nearbyMap;
    private NearbyFragment nearbyFragment;
    private GoogleApiClient mGoogleApiClient;
    private static final String DIALOG_ERROR = "dialog_error"; // Unique tag for the error dialog fragment
    private boolean mResolvingError = false;// Bool to track whether the app is already resolving an error
    private Location mLastLocation;
    private static int RADIUS_COLOR_RGB;
    private Marker defaultMarker;

    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    private CollapsingToolbarLayout collapsingToolbar;
    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RADIUS_COLOR_RGB = getResources().getColor(R.color.accent_material_light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();
        mLayoutView = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);

        if (findViewById(R.id.main_fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            nearbyFragment = NearbyFragment.newInstance(this);

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, nearbyFragment).commit();
        }

        if (ConnectionUtil.isGPSEnabled(this)) {
            buildGoogleApiClient();
        } else {
            displayGpsErrorMessage();
        }
    }

    /**
     * Creating The Toolbar and setting it as the Toolbar for the activity
     * home as up set to true
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(this.getString(R.string.app_name));
    }


    private void pickUserAccount() {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null); //alwaysPromptForAccount = false
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_ACCOUNT:
                // Receiving a result from the AccountPicker
                if (resultCode == RESULT_OK) {
                    mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    // With the account name acquired, go get the auth token
                    getUsername();
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    displayAccountLoginErrorMessage();
                }
                // Handle the result from exceptions
                break;
            case REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR:
//            case REQUEST_CODE_RECOVER_FROM_AUTH_ERROR: //todo: figure out HOW/WHEN this is received
                if (resultCode == RESULT_OK) {
                    // Receiving a result that follows a GoogleAuthException, try auth again
                    getUsername();
                }
            case GOOGLE_API_REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!mGoogleApiClient.isConnecting() &&
                            !mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    }
                }
                break;
        }
    }

//region Activity Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ConnectionUtil.isDeviceOnline()) {
                IntentFilter mIntentFilter = new IntentFilter(MainActivity.BROADCAST_QUESTIONS_LIST_RESULT);
                LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
                SkooziQnARequestService.startActionGetQuestionsList(this,
                        mLastLocation == null ? DEFAULT_LOCATION : new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        (long) DEFAULT_RADIUS_METRES/1000);
            } else {
                displayNetworkErrorMessage();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        FloatingActionButton nearby_fab = (FloatingActionButton) findViewById(R.id.nearby_fabBtn);
        nearby_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmail == null) {
                    displayAccountLoginErrorMessage();
                    pickUserAccount();
                } else {
                    Intent intent = new Intent(v.getContext(), PostQuestionActivity.class);
                    startActivity(intent);
                }
            }
        });

        if (ConnectionUtil.isGPSEnabled(this) && !mResolvingError) {
            mGoogleApiClient.connect();
        }
//        pickUserAccount();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//endregion

//region GoogleApi calls

    /**
     * https://developers.google.com/android/guides/api-client
     */
    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            updateCurrentLocation();
        }
    }
    @Override
    public void onConnectionSuspended (int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.d(TAG, "BOO - connection suspended due to " + cause);
    }

    /**
     * This callback is important for handling errors that
     * may occur while attempting to connect with Google.
     * @param result contains the reason as to why the connection failed
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "connection to GPS failed for " + result.toString());
        if (mResolvingError) {
            return; // Already attempting to resolve an error.
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, GOOGLE_API_REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect(); // There was an error with the resolution intent. Try again.
            }
        } else {
            mResolvingError = true;
            displayGoogleApiErrorMessage(result.getErrorCode()); // Show dialog using GooglePlayServicesUtil.getErrorDialog()
        }
    }
//endregion

    private void updateCurrentLocation() {
        if (nearbyMap != null) {
            if (defaultMarker != null) {
                defaultMarker.remove();
            }
            LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            nearbyMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .fillColor(Color.argb(RADIUS_TRANSPARENCY,
                            Color.red(RADIUS_COLOR_RGB),
                            Color.green(RADIUS_COLOR_RGB),
                            Color.blue(RADIUS_COLOR_RGB)))
                    .radius(DEFAULT_RADIUS_METRES);

            // Get back the mutable Circle
            Circle circle = nearbyMap.addCircle(circleOptions);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        nearbyMap = map;
        if (mLastLocation != null) {
            updateCurrentLocation();
        } else {
            // Move the camera instantly to Toronto
            defaultMarker = nearbyMap.addMarker(new MarkerOptions()
                    .position(DEFAULT_LOCATION)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Default location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            nearbyMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    @Override
    public void onMapQuestion(double lat, double lon) {
        LatLng questionLocation = new LatLng(lat,lon);
        if (nearbyMap != null) {
            nearbyMap.addMarker(new MarkerOptions()
                    .position(questionLocation));
        }

    }

    private void updateNearbyList(List<Question> questions) {
        if (nearbyFragment != null) {
            nearbyFragment.updateNearbyQuestions(questions, nearbyMap);
        }
    }


    @Override
    public void onQuestionSelected(Question mQuestion) {
        Intent threadIntent = new Intent(this, ThreadActivity.class);
        Bundle questionBundle = new Bundle();
        questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, mQuestion);
        threadIntent.putExtras(questionBundle);
        startActivity(threadIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Question> questions = intent.getParcelableArrayListExtra(MainActivity.EXTRAS_QUESTIONS_LIST);
            if (questions == null)
                //fail silently
                //todo: determine if there's a better approach to this
                return;

            Log.d(TAG, String.valueOf(questions.size()));
            updateNearbyList(questions);
        }
    };



    private Account getAccountForName(Context context, String username) {
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
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {

        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (ConnectionUtil.isDeviceOnline()) {
                new GetUsernameTask(this, mEmail, SCOPE).execute();
            } else {
                displayNetworkErrorMessage();
            }
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleGoogleAuthTokenException(final UserRecoverableAuthException exception) {
        //TODO: need to determine how to properlyt handle this
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (exception instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) exception)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (exception instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = exception.getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

//region Error Message handlers
    private void displayNetworkErrorMessage() {
        Snackbar.make(mLayoutView, R.string.no_network_message, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }

    private void displayAccountLoginErrorMessage() {
        Snackbar.make(mLayoutView, R.string.no_account_login_message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void displayGpsErrorMessage() {
        Snackbar.make(mLayoutView, R.string.no_gps_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_enable_gps, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .show();
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
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
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
            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }
//endregion
}
