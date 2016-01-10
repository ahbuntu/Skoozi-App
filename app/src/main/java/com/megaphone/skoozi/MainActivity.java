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
import android.support.v7.widget.Toolbar;
import android.view.View;

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

import java.util.List;

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
public class MainActivity extends BaseActivity implements NearbyFragment.NearbyQuestionsListener {
    private static final String TAG = "MainActivity";
    private static final int DEFAULT_ZOOM = 11;
    private static final int RADIUS_TRANSPARENCY = 64; //75%

    public static final String EXTRAS_QUESTIONS_LIST  = "com.megaphone.skoozi.extras.QUESTIONS_LIST";
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";

    private GoogleApiClientBroker googleApiBroker;
    private GoogleApiClient googleApiClient;

    private CoordinatorLayout coordinatorLayout;
    private GoogleMap nearbyMap;
    private NearbyFragment nearbyFragment;
    private boolean resolvingGoogleApiError = false;// Bool to track whether the app is already resolving an error
    private Location latestLocation;
    private Marker defaultMarker;

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
                resolvingGoogleApiError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
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
                    updateLatestLocation();
                }
            }
        }
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

//        googleApiBroker = new GoogleApiClientBroker(this);
//        initGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        FloatingActionButton nearby_fab = (FloatingActionButton) findViewById(R.id.nearby_fabBtn);
        nearby_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryNewQuestion();
            }
        });

        if (ConnectionUtil.hasGps(this, coordinatorLayout) && googleApiBroker == null) {
            googleApiBroker = new GoogleApiClientBroker(this);
            // assuming that both googleApiBroker & googleApiClient become null together
            if (!resolvingGoogleApiError) {
                initGoogleApiClient();
                if (googleApiClient != null) googleApiClient.connect();
            }
        } else {
            updateLatestLocation();
        }
    }

    private void initGoogleApiClient() {
        if (googleApiClient != null) return;
        if (ConnectionUtil.hasGps(this, coordinatorLayout)){
            googleApiClient = googleApiBroker
                    .getGoogleApiClient(new GoogleApiClientBroker.BrokerResultListener() {
                        @Override
                        public void onConnected() {
                            updateLatestLocation();
                        }
                    });
        }
    }

    private void updateLatestLocation() {
        latestLocation = PermissionUtil.tryGetLatestLocation(MainActivity.this, googleApiClient);
        if (latestLocation != null) nearbyFragment.updateSearchOrigin(latestLocation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) googleApiClient.disconnect();
    }

    @Override
    public void onSearchAreaUpdated(Location origin, double radius) {
        // todo: implement update of map in here
    }

    @Override
    public void onQuestionsAvailable(List<Question> questions) {
        if (nearbyMap == null) return;
        LatLng questionLocation;
        for (Question question : questions) {
            questionLocation = new LatLng(question.locationLat, question.locationLon);
            nearbyMap.addMarker(new MarkerOptions().position(questionLocation));
        }
    }

//    @Override
//    public void onQuestionSelected(Question mQuestion) {
//        Intent threadIntent = new Intent(this, ThreadActivity.class);
//        Bundle questionBundle = new Bundle();
//        questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, mQuestion);
//        threadIntent.putExtras(questionBundle);
//        startActivity(threadIntent);
//    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(this.getString(R.string.app_name));
    }

//    private void initGoogleApiClient() {
//        if (googleApiClient == null
//                && ConnectionUtil.hasGps(getApplicationContext(), coordinatorLayout)) {
//            googleApiClient = googleApiBroker.buildLocationClient(
//                    new GoogleApiClientBroker.BrokerResultListener() {
//                        @Override
//                        public void onConnected() {
//                            getLatestLocation();
//                        }});
//        }
//    }

//    private void getLatestLocation() {
//        latestLocation = PermissionUtil.tryGetLatestLocation(MainActivity.this, googleApiClient);
//        if (latestLocation != null) {
//            updateCurrentLocation();
//            nearbyFragment.updateSelfLocation(latestLocation);
//        }
//    }

    private void tryNewQuestion() {
        if (SkooziApplication.getUserAccount() == null) {
            AccountUtil.pickUserAccount(MainActivity.this, ACTION_NEW_QUESTION);
        } else {
            Intent intent = new Intent(MainActivity.this, PostQuestionActivity.class);
            startActivity(intent);
        }
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

    public NearbyFragment.NearbyQuestionsListener requestNearbyListener() {
        return this;
    }
}
