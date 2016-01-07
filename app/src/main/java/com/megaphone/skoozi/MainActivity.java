package com.megaphone.skoozi;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.GoogleApiClientBroker;
import com.megaphone.skoozi.util.PermissionUtil;

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
        implements NearbyFragment.OnMapQuestionsCallback, NearbyRecyclerViewAdapter.OnQuestionItemSelected {

    private static final String TAG = "MainActivity";
    public static final String EXTRAS_QUESTIONS_LIST  = "com.megaphone.skoozi.extras.QUESTIONS_LIST";
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";

    private static final int DEFAULT_ZOOM = 11;

    private static final int RADIUS_TRANSPARENCY = 64; //75%

    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

    private CoordinatorLayout coordinatorLayout;
    private GoogleMap nearbyMap;
    private NearbyFragment nearbyFragment;
    private boolean mResolvingError = false;// Bool to track whether the app is already resolving an error
    private Location latestLocation;
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
                    AccountUtil.displayAccountLoginErrorMessage(coordinatorLayout);
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
                    getLatestLocation();
                } else {
                    latestLocation = null;
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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupToolbar();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (findViewById(R.id.main_fragment_container) != null) {
            if (savedInstanceState == null) {
                nearbyFragment = NearbyFragment.newInstance();
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
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(this.getString(R.string.app_name));
    }

    private void initGoogleApiClient() {
        if (googleApiClient == null) {
            if (ConnectionUtil.isGPSEnabled(this)) {
                googleApiClient = googleApiBroker.buildLocationClient(
                        new GoogleApiClientBroker.BrokerResultListener() {
                            @Override
                            public void onConnected() {
                                getLatestLocation();
                            }});
            } else {
                ConnectionUtil.displayGpsErrorMessage(coordinatorLayout, this);
            }
        }
    }

    private void getLatestLocation() {
        latestLocation = PermissionUtil.tryGetLatestLocation(MainActivity.this, googleApiClient);
        if (latestLocation != null) {
            updateCurrentLocation();
            nearbyFragment.updateSelfLocation(latestLocation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SkooziApplication.getUserAccount() == null) AccountUtil.pickUserAccount(this, null);

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
        if (nearbyMap != null && latestLocation != null) {
            nearbyMap.clear();
            // Instantiates a new CircleOptions object and defines the center and radius
            int radiusColorRgb = ContextCompat.getColor(this, R.color.accent_material_light);
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()))
                    .fillColor(Color.argb(RADIUS_TRANSPARENCY,
                            Color.red(radiusColorRgb),
                            Color.green(radiusColorRgb),
                            Color.blue(radiusColorRgb)))
                    .radius(nearbyFragment.getSearchRadiusKm()); // need this in metres
            nearbyMap.addCircle(circleOptions);
        }
    }


    private void updateCurrentLocation() {
        if (nearbyMap != null) {

            if (defaultMarker != null) {
                defaultMarker.remove();
            }
            nearbyMap.clear(); // important to ensure that everything is cleared
            LatLng currentLocation = new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude());
            nearbyMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

            updateSearchRadiusCircle();
        }
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

}
