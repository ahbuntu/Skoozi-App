package com.megaphone.skoozi;

import java.util.ArrayList;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
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
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;
import com.megaphone.skoozi.util.PermissionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

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
        implements OnMapReadyCallback, NearbyFragment.OnMapQuestionsCallback,
        NearbyRecyclerViewAdapter.OnQuestionItemSelected {

    private static final String TAG = "MainActivity";
    public static final String BROADCAST_QUESTIONS_LIST_RESULT = "com.megaphone.skoozi.broadcast.QUESTIONS_LIST_RESULT";
    public static final String EXTRAS_QUESTIONS_LIST  = "com.megaphone.skoozi.extras.QUESTIONS_LIST";
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";
    private static int RADIUS_COLOR_RGB;

    public final static int DEFAULT_RADIUS_METRES = 10000;
    private static final LatLng DEFAULT_LOCATION = new LatLng(43.6532,-79.3832);
    private static final int DEFAULT_ZOOM = 11;

    private static final int RADIUS_TRANSPARENCY = 64; //75%

    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

    private CollapsingToolbarLayout collapsingToolbar;
    private CoordinatorLayout mLayoutView;
    private ProgressBar nearbyProgress;
    private Spinner radiusSpinner;
    private GoogleMap nearbyMap;

    private NearbyFragment nearbyFragment;
    private boolean mResolvingError = false;// Bool to track whether the app is already resolving an error
    private Location mLastLocation;
    private Marker defaultMarker;


    private AccountUtil.GoogleAuthTokenExceptionListener tokenListener = new AccountUtil.GoogleAuthTokenExceptionListener() {
        @Override
        public void handleGoogleAuthException(final UserRecoverableAuthException exception) {
            // Because this call comes from the AsyncTask, we must ensure that the following
            // code instead executes on the UI thread.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountUtil.resolveAuthExceptionError(MainActivity.this, exception);
                }
            });
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Question> questions = intent.getParcelableArrayListExtra(MainActivity.EXTRAS_QUESTIONS_LIST);
            nearbyProgress = (ProgressBar) mLayoutView.findViewById(R.id.nearby_progress);
            if (nearbyProgress != null) {
                nearbyProgress.setVisibility(View.GONE);
            }
            if (questions == null) {
                //todo: need to retry with larger search radius
                SkooziQnAUtil.displayNoQuestionsMessage(mLayoutView);
//                return;
            }
            updateNearbyList(questions);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AccountUtil.REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    // Receiving a result from the AccountPicker
                    SkooziApplication.setUserAccount(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    String action = data.getStringExtra(AccountUtil.EXTRA_USER_ACCOUNT_ACTION); //can return null
                    if (action != null && action.equals(ACTION_NEW_QUESTION)) {
                        tryNewQuestion();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    AccountUtil.displayAccountLoginErrorMessage(mLayoutView);
                }
                break;
            case AccountUtil.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR:
//            case REQUEST_CODE_RECOVER_FROM_AUTH_ERROR: //todo: figure out HOW/WHEN this is received
                if (resultCode == RESULT_OK) {
                    // Receiving a result that follows a GoogleAuthException, try auth again
//                    getUsername();
                }
            case GoogleApiClientBroker.GOOGLE_API_REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!googleApiClient.isConnecting() &&
                            !googleApiClient.isConnected()) {
                        googleApiClient.connect();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.REQUEST_PERMISSION_LOCATION: {
                if (grantResults.length > 0 // If request is cancelled, result arrays are empty
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    mLastLocation = null;
                }
            }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RADIUS_COLOR_RGB = getResources().getColor(R.color.accent_material_light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        mLayoutView = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
        if (findViewById(R.id.main_fragment_container) != null) {
            if (savedInstanceState == null) {
                nearbyFragment = NearbyFragment.newInstance();
                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_container, nearbyFragment).commit();
            } else {
                return;
            }
        }

        googleApiBroker = new GoogleApiClientBroker(this);
        initGoogleApiClient();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(this.getString(R.string.app_name));
    }

    private void initGoogleApiClient() {
        if (googleApiClient == null) {
            if (ConnectionUtil.isGPSEnabled(this)) {
                googleApiClient = googleApiBroker.buildLocationClient(
                        new GoogleApiClientBroker.BrokerResultListener() {
                            @Override
                            public void onConnected() {
                                getLastLocation();
                            }});
            } else {
                ConnectionUtil.displayGpsErrorMessage(mLayoutView, this);
            }
        }
    }

    private void getLastLocation() {
        mLastLocation = PermissionUtil.tryGetLastLocation(MainActivity.this, googleApiClient);
        if (mLastLocation != null) {
            updateCurrentLocation();
        }
    }

    private int parseSearchRadiusKm(String radiusSpinnerSelectedText) {
        try {
            int radiusKm = Integer.parseInt(radiusSpinnerSelectedText.split("\\s+")[0]);
            return radiusKm; //need to return radius in metres
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error while trying to extract radius from spinner. " );
            e.printStackTrace();
        }
        return DEFAULT_RADIUS_METRES/1000;
    }

    private void getQuestionsFromApi(int radiusKm) {
        IntentFilter mIntentFilter = new IntentFilter(MainActivity.BROADCAST_QUESTIONS_LIST_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
        SkooziQnARequestService.startActionGetQuestionsList(this, tokenListener
                , mLastLocation == null ? DEFAULT_LOCATION : new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())
                , (long) radiusKm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        radiusSpinner = (Spinner) mLayoutView.findViewById(R.id.radius_spinner);

        try {
            if (ConnectionUtil.isDeviceOnline()) {
                getQuestionsFromApi(parseSearchRadiusKm(radiusSpinner.getSelectedItem().toString()));
                if (SkooziApplication.getUserAccount() == null) AccountUtil.pickUserAccount(MainActivity.this, null);
            } else {
                ConnectionUtil.displayNetworkErrorMessage(mLayoutView);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        FloatingActionButton nearby_fab = (FloatingActionButton) findViewById(R.id.nearby_fabBtn);
            nearby_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tryNewQuestion();
                }
            });

        if (ConnectionUtil.isGPSEnabled(this) && !mResolvingError) {
            if (googleApiClient != null) {
                googleApiClient.connect();
            }
        }
    }

    private void setSpinnerListener() {
        if (radiusSpinner == null) return;
        if (spinnerListening) return;

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                nearbyProgress = (ProgressBar) mLayoutView.findViewById(R.id.nearby_progress);
                if (nearbyProgress != null) {
                    nearbyProgress.setVisibility(View.VISIBLE);
                }
                updateSearchRadiusCircle();
                getQuestionsFromApi(parseSearchRadiusKm(parentView.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void tryNewQuestion() {
        if (SkooziApplication.getUserAccount() == null) {
            AccountUtil.pickUserAccount(MainActivity.this, ACTION_NEW_QUESTION);
        } else {
            Intent intent = new Intent(MainActivity.this, PostQuestionActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_my_activity) {
            Toast.makeText(MainActivity.this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSearchRadiusCircle() {
        if (nearbyMap != null && mLastLocation != null) {
            nearbyMap.clear();
            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .fillColor(Color.argb(RADIUS_TRANSPARENCY,
                            Color.red(RADIUS_COLOR_RGB),
                            Color.green(RADIUS_COLOR_RGB),
                            Color.blue(RADIUS_COLOR_RGB)))
                    .radius(radiusSpinner == null
                            ? DEFAULT_RADIUS_METRES
                            : 1000 * parseSearchRadiusKm(radiusSpinner.getSelectedItem().toString())); // need this in metres
            nearbyMap.addCircle(circleOptions);
        }
    }

    boolean spinnerListening = false;
    private void updateCurrentLocation() {
        if (nearbyMap != null) {

            setSpinnerListener();
            if (radiusSpinner != null) {
//                    getQuestionsFromApi(parseSearchRadiusKm(radiusSpinner.getSelectedItem().toString()));
            }

            if (defaultMarker != null) {
                defaultMarker.remove();
            }
            nearbyMap.clear(); // important to ensure that everything is cleared
            LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            nearbyMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

            updateSearchRadiusCircle();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        nearbyMap = map;

        if (mLastLocation != null) {
            updateCurrentLocation();
        }
        //todo: need to determine when/why/how to handle the scenario where mLastLocation = null
//        else {
//            // Move the camera instantly to Toronto
//            defaultMarker = nearbyMap.addMarker(new MarkerOptions()
//                    .position(DEFAULT_LOCATION)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                    .title("Default location"));
//            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
//            nearbyMap.getUiSettings().setZoomControlsEnabled(true);
//        }
    }

    //todo: this method is likely not being used. Investigate if it can be removed.
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
}
