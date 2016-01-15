package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
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
    private Location searchOrigin;
    private NearbyQuestionsListener nearbyListener;
    private IntentFilter quesListIntentFilter;
    private boolean requestInProgress;

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
            requestInProgress = false;
            progressBar.setVisibility(View.GONE);
            ArrayList<Question> questions = intent.getParcelableArrayListExtra(SkooziQnAUtil.EXTRAS_QUESTIONS_LIST);
            displayApiResponse(questions);
        }
    };

    public interface NearbyQuestionsListener {
        void onSearchAreaUpdated(Location origin, int radius);
        void onQuestionsAvailable(List<Question> nearbyQuestions);
    }

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
        View rootView = inflater.inflate(R.layout.nearby_fragment, container, false);

        rvList = (RecyclerView) rootView.findViewById(R.id.nearby_recycler);
        radiusSpinner = (Spinner) rootView.findViewById(R.id.radius_spinner);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_circle);

        setupRecyclerView();
        setupRadiusSpinner();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onResume();
        destroyLocalBroadcastPair();
    }

    @Nullable
    private NearbyQuestionsListener getNearbyListener(Context context) {
        if (context instanceof NearbyQuestionsListener) {
            nearbyListener = (NearbyQuestionsListener) context;
            return nearbyListener;
        }
        return null;
    }

    private void setupRecyclerView() {
        rvList.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(mLayoutManager);

        //recycler view will display the questions coords on the map, but the search radius needs to be displayed from Main Activity
        rvListAdapter = new NearbyRecyclerViewAdapter(getActivity(), null, null);
        rvList.setAdapter(rvListAdapter);
    }

    private void setupRadiusSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nearby_radius_options, R.layout.nearby_radius_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radiusSpinner.setAdapter(adapter);

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                tryGetQuestionsFromApi();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void tryGetQuestionsFromApi() {
        if (requestInProgress) return;
        if (searchOrigin == null)  return; // can't do anything without a location

        setupLocalBroadcastPair();
        if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
            progressBar.setVisibility(View.VISIBLE);
            SkooziQnAUtil.quesListRequest(getActivity(), authTokenListener,
                    searchOrigin, getSearchRadiusKm());

            if (getNearbyListener(getActivity()) != null)
                    nearbyListener.onSearchAreaUpdated(searchOrigin, getSearchRadiusKm());

            requestInProgress = true;
        }
    }

    private void setupLocalBroadcastPair() {
        if (quesListIntentFilter == null) {
            quesListIntentFilter = new IntentFilter(SkooziQnAUtil.BROADCAST_QUESTIONS_LIST_RESULT);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(skooziApiReceiver, quesListIntentFilter);
        }
    }

    private void destroyLocalBroadcastPair() {
        quesListIntentFilter = null;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(skooziApiReceiver);
    }

    private int getSearchRadiusKm() {
        String spinnerText = radiusSpinner.getSelectedItem().toString();
        try {
            return Integer.parseInt(spinnerText.split("\\s+")[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error while trying to extract radius from spinner. " );
        }
        return DEFAULT_RADIUS_METRES/1000;
    }

    private void displayApiResponse(List<Question> questions) {
        if (getNearbyListener(getActivity()) != null)
            nearbyListener.onQuestionsAvailable(questions);

        if (questions == null) {
            SkooziQnAUtil.displayNoQuestionsMessage(coordinatorLayout);
        } else {
            rvListAdapter.updateNearbyQuestions(questions);
        }
    }

    // Exposed methods

    public void updateSearchOrigin(Location updatedLocation) {
        searchOrigin = updatedLocation;
        tryGetQuestionsFromApi();
    }
}
