package com.megaphone.skoozi;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public static NearbyFragment newInstance(Activity callback) {
//        public static NearbyFragment newInstance(OnMapReadyCallback callback) {
        NearbyFragment fragment = new NearbyFragment();
        fragment.mCallback = (OnMapReadyCallback) callback;
        fragment.mMapQCallback = (OnMapQuestionsCallback) callback;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyFragment() {}// Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_nearby, container, false);

        nearbyListView = (RecyclerView) rootView.findViewById(R.id.nearby_recycler);
        nearbyListView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        nearbyListView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        nearbyListView.addItemDecoration(mItemDecoration);
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        MapFragment nearbyMap = (MapFragment)  getFragmentManager().findFragmentById(R.id.nearby_map);
        if (nearbyMap != null) {
            nearbyMap.getMapAsync(mCallback);
        }
    }

    public void updateNearbyQuestions(List<Question> questions) {
        NearbyRecyclerViewAdapter mNearbyListAdapter = new NearbyRecyclerViewAdapter(getActivity(), questions);
        nearbyListView.setAdapter(mNearbyListAdapter);
    }

}
