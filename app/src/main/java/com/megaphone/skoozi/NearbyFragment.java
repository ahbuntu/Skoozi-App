package com.megaphone.skoozi;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;


/**
 * Fragment to display all nearby questions and answers
 */
public class NearbyFragment extends Fragment {

    private OnMapReadyCallback mCallback;
    private RecyclerView nearbyListView;

    public interface OnMapQuestionsCallback{
        void onMapQuestion(double lat, double lon);
    }

    private OnMapQuestionsCallback mMapQCallback;

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnMapReadyCallback) {
            this.mCallback = (OnMapReadyCallback) activity;
        }
        if (activity instanceof OnMapQuestionsCallback) {
            this.mMapQCallback = (OnMapQuestionsCallback) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_nearby, container, false);

        Spinner radiusSpinner = (Spinner) rootView.findViewById(R.id.radius_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nearby_radius_options, R.layout.main_radius_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (radiusSpinner != null) {
            radiusSpinner.setAdapter(adapter);
        }

        nearbyListView = (RecyclerView) rootView.findViewById(R.id.nearby_recycler);
        if (nearbyListView != null) {
            nearbyListView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            nearbyListView.setLayoutManager(mLayoutManager);
            RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
            nearbyListView.addItemDecoration(mItemDecoration);
        }
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        MapFragment nearbyMap = (MapFragment)  getFragmentManager().findFragmentById(R.id.nearby_map);
        if (nearbyMap != null && mCallback != null) {
            nearbyMap.getMapAsync(mCallback);
        }
    }

    public void updateNearbyQuestions(List<Question> questions, GoogleMap map) {
        if (questions == null) {
            nearbyListView.setAdapter(null);
            return;
        }
        //recycler view will display the questions coords on the map, but the search radius needs to be displayed from Main Activity
        NearbyRecyclerViewAdapter mNearbyListAdapter = new NearbyRecyclerViewAdapter(getActivity(), questions, map);
        nearbyListView.setAdapter(mNearbyListAdapter);
    }
}
