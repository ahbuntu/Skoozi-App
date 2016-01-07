package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display all nearby questions and answers
 */
public class NearbyFragment extends Fragment {
    private final static String TAG = "NearbyFragment";
    private final static int DEFAULT_RADIUS_METRES = 10000;

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView rvList;
    private NearbyRecyclerViewAdapter rvListAdapter;
    private Spinner radiusSpinner;
    private ProgressBar progressBar;
    private Location selfLocation;
    private GoogleMap nearbyMap;

    private AccountUtil.GoogleAuthTokenExceptionListener authTokenListener = new AccountUtil.GoogleAuthTokenExceptionListener() {
        @Override
        public void handleGoogleAuthException(final UserRecoverableAuthException exception) {
            // Because this call comes from the AsyncTask, we must ensure that the following
            // code instead executes on the UI thread.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountUtil.resolveAuthExceptionError(getActivity(), exception);
                }
            });
        }
    };

    private BroadcastReceiver skooziApiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Question> questions = intent.getParcelableArrayListExtra(MainActivity.EXTRAS_QUESTIONS_LIST);
            displayApiResponse(questions);
        }
    };

    public interface OnMapQuestionsCallback{
        void onMapQuestion(double lat, double lon);
    }

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            nearbyMap = googleMap;
//
//            if (mLastLocation != null) {
//                updateCurrentLocation();
//            }
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
    };

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_nearby, container, false);

        rvList = (RecyclerView) rootView.findViewById(R.id.nearby_recycler);
        radiusSpinner = (Spinner) rootView.findViewById(R.id.radius_spinner);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_circle);

        setupRecyclerView();
        setupRadiusSpinner();

        return rootView;
    }

    private void setupRecyclerView() {
        rvList.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        rvList.addItemDecoration(mItemDecoration);

        //recycler view will display the questions coords on the map, but the search radius needs to be displayed from Main Activity
        rvListAdapter = new NearbyRecyclerViewAdapter(getActivity(), null, nearbyMap);
        rvList.setAdapter(rvListAdapter);
    }

    private void setupRadiusSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nearby_radius_options, R.layout.main_radius_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radiusSpinner.setAdapter(adapter);

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                progressBar.setVisibility(View.VISIBLE);
                tryGetQuestionsFromApi();
//                listener.radiusUpdated();
//                getQuestionsFromApi(parseSearchRadiusKm(parentView.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
//        MapFragment nearbyMap = (MapFragment)  getFragmentManager().findFragmentById(R.id.nearby_map);
//        if (nearbyMap != null && mCallback != null) {
//            nearbyMap.getMapAsync(mCallback);
//        }
        addListeners();
        tryGetQuestionsFromApi();
    }

    private void tryGetQuestionsFromApi() {
        if (ConnectionUtil.isDeviceOnline()) {
            if (selfLocation == null) {
                progressBar.setVisibility(View.VISIBLE);
                ConnectionUtil.displayGpsErrorMessage(coordinatorLayout, getActivity());
            } else {
                SkooziQnAUtil.quesListRequest(getActivity(), authTokenListener,
                        selfLocation, getSearchRadiusKm());
            }
        } else {
            ConnectionUtil.displayNetworkErrorMessage(coordinatorLayout);
        }
    }

    private void addListeners() {
        IntentFilter quesListIntentFilter = new IntentFilter(SkooziQnAUtil.BROADCAST_QUESTIONS_LIST_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(skooziApiReceiver, quesListIntentFilter);
    }

    @Override
    public void onPause(){
        super.onResume();
        removeListeners();
    }

    private void removeListeners() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(skooziApiReceiver);
    }

    private void displayApiResponse(List<Question> questions) {
        progressBar.setVisibility(View.GONE);
        if (questions == null) {
            SkooziQnAUtil.displayNoQuestionsMessage(coordinatorLayout);
        } else {
            rvListAdapter.updateNearbyQuestions(questions);
        }
    }

    public void updateSelfLocation(Location latestLocation) {
        selfLocation = latestLocation;
        tryGetQuestionsFromApi();
    }

    public int getSearchRadiusKm() {
        String spinnerText = radiusSpinner.getSelectedItem().toString();
        try {
            return Integer.parseInt(spinnerText.split("\\s+")[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error while trying to extract radius from spinner. " );
        }
        return DEFAULT_RADIUS_METRES/1000;
    }
}
