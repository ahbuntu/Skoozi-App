package com.megaphone.skoozi.nearby;

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
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.base.BaseActivity;
import com.megaphone.skoozi.PendingMapUpdate;
import com.megaphone.skoozi.PostQuestionActivity;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.ConnectionUtil;
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
public class MainActivity extends BaseActivity implements NearbyFragment.NearbyQuestionsListener,
        OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 11;
    private static final int RADIUS_TRANSPARENCY = 64; //75%

    private MapFragment mapFragment;
    private GoogleMap nearbyMap;
    private NearbyFragment nearbyFragment;
    private Location latestLocation;
    private PendingMapUpdate pendingMapUpdate;

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
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.nearby_map);

        if (savedInstanceState != null) {
            nearbyFragment = (NearbyFragment)
                    getSupportFragmentManager().findFragmentByTag(NearbyFragment.getFragmentTag());
            Log.d(TAG, "onCreate: found fragment from a non null savedInstanceState");
        } else if (nearbyFragment == null) {
            nearbyFragment = NearbyFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, nearbyFragment, NearbyFragment.getFragmentTag())
                    .commit();
            Log.d(TAG, "onCreate: instanting new Nearby fragment");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FloatingActionButton nearby_fab = (FloatingActionButton) findViewById(R.id.nearby_fabBtn);
        nearby_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostQuestionActivity.class);
                startActivity(intent);
            }
        });

        if (mapFragment != null)  mapFragment.getMapAsync(this);

        if (ConnectionUtil.hasNetwork(coordinatorLayout)) connectToGoogleApi();
    }

    @Override
    protected void onGoogleApiConnected() {
        super.onGoogleApiConnected();
        updateLatestLocation();
    }

    // Interface implementations

    @Override
    public void onMapReady(GoogleMap map) {
        nearbyMap = map;
        if (pendingMapUpdate == null) return;

        // there is an outstanding update
        nearbyMap.clear(); // important to ensure that everything is cleared
        onSearchAreaUpdated(pendingMapUpdate.origin, pendingMapUpdate.radius);
        onQuestionsAvailable(pendingMapUpdate.questions);

        // indicate that map update is finished
        pendingMapUpdate = null;
    }

    /**
     * This method can set a pending update for the Map if it is currently unavailable
     * @param origin the location that will be used for the pending update
     * @param radius the radius that will be used for the pending update
     * @return
     */

    @Override
    public void onSearchAreaUpdated(Location origin, int radius) {
        if (canUpdateMap(origin, radius)) {
            nearbyMap.clear(); // important to ensure that everything is cleared
            updateCurrentLocation(origin);
            updateSearchRadiusCircle(origin, radius);
        }
    }

    @Override
    public void onQuestionsAvailable(List<Question> questions) {
        if (questions == null) return;
        if (canUpdateMap(questions)) {
            LatLng questionLocation;
            for (Question question : questions) {
                questionLocation = new LatLng(question.locationLat, question.locationLon);
                nearbyMap.addMarker(new MarkerOptions().position(questionLocation));
            }
        }
    }

    private void updateLatestLocation() {
        latestLocation = PermissionUtil.tryGetLatestLocation(MainActivity.this, getGoogleApiClient());
        if (latestLocation != null) {
            try {
                nearbyFragment.updateSearchOrigin(latestLocation);
            } catch (Exception e) {
                Log.e(TAG, "updateLatestLocation: VERY STRANGE. nearbyFragment is null. Stacktrace to follow");
                e.printStackTrace();
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(this.getString(R.string.app_name));
    }

    private void updateCurrentLocation(Location origin) {
        LatLng searchLocation = new LatLng(origin.getLatitude(), origin.getLongitude());
        nearbyMap.addMarker(new MarkerOptions()
                .position(searchLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Current location"));
        nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, DEFAULT_ZOOM));
    }

    private boolean canUpdateMap(Location origin, int radius) {
        // todo: should display things on map only if the map is visible
        if (nearbyMap == null) {
            // set values so that map will be updated when available
            pendingMapUpdate = new PendingMapUpdate(origin, radius);
            return false;
        }
        pendingMapUpdate = null;
        return true;
    }

    private boolean canUpdateMap(List<Question> questions) {
        if (nearbyMap == null && pendingMapUpdate != null) {
            pendingMapUpdate.setMapQuestionsMarkers(questions);
            return false;
        }
        pendingMapUpdate = null;
        return true;
    }

    private void updateSearchRadiusCircle(Location origin, int radius) {
        // Instantiates a new CircleOptions object and defines the center and radius
        int radiusColorRgb = ContextCompat.getColor(this, R.color.accent_material_light);
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(origin.getLatitude(), origin.getLongitude()))
                .fillColor(Color.argb(RADIUS_TRANSPARENCY,
                        Color.red(radiusColorRgb),
                        Color.green(radiusColorRgb),
                        Color.blue(radiusColorRgb)))
                .radius(radius*1000); // need this in metres
        nearbyMap.addCircle(circleOptions);
    }

}
