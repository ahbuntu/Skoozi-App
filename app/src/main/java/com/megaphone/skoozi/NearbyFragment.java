package com.megaphone.skoozi;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment to display all nearby questions and answers
 */
public class NearbyFragment extends Fragment {

    private OnMapReadyCallback mCallback;
    private ListView nearbyListView;
    private List<Question> listItems;

    public interface OnMapQuestionsCallback{
        void onMapQuestion(double lat, double lon);
    }

    public interface OnQuestionItemSelected{
        void onQuestionSelected(Question mQuestion);
    }

    private OnMapQuestionsCallback mMapQCallback;
    private OnQuestionItemSelected mQuestionItemCallback;

    public static NearbyFragment newInstance(Activity callback) {
//        public static NearbyFragment newInstance(OnMapReadyCallback callback) {
        NearbyFragment fragment = new NearbyFragment();
        fragment.mCallback = (OnMapReadyCallback) callback;
        fragment.mMapQCallback = (OnMapQuestionsCallback) callback;
        fragment.mQuestionItemCallback = (OnQuestionItemSelected) callback;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyFragment() {}// Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_nearby, container, false);
        nearbyListView = (ListView) rootView.findViewById(R.id.nearby_list);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        MapFragment nearbyMap = (MapFragment)  getChildFragmentManager().findFragmentById(R.id.nearby_map);
        if (nearbyMap != null) {
//                mapFragment.getMapAsync(this);
            nearbyMap.getMapAsync(mCallback);
        }
    }

    public void updateNearbyQuestions(List<Question> questions) {
        NearbyListAdapter mNearbyListAdapter = new NearbyListAdapter(questions);
        nearbyListView.setAdapter(mNearbyListAdapter);
        nearbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Question clickedQuestion = listItems.get(position);
                mQuestionItemCallback.onQuestionSelected(clickedQuestion);
            }
        });
    }

    private class NearbyListAdapter extends BaseAdapter {


        public NearbyListAdapter(List<Question> questions) {
            listItems = questions;
        }
        @Override
        public int getCount() {
            /**
             * TODO: this count should be memoized
             * http://stackoverflow.com/questions/8921162/how-to-update-listview-on-scrolling-while-retrieving-data-from-server-in-android
             */
//            return testValues.size();
            return listItems.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listItems.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nearby_list_row, parent, false);
            }
            TextView textAuthor = (TextView) convertView.findViewById(R.id.nearby_list_profile_name);
            TextView textContent = (TextView) convertView.findViewById(R.id.nearby_list_question);

            Question currentQuestion = listItems.get(position);
            textAuthor.setText(currentQuestion.getAuthor());
            textContent.setText(currentQuestion.getContent());

            mMapQCallback.onMapQuestion(currentQuestion.getLocationLat(), currentQuestion.getLocationLon());
            return convertView;
        }
    }
}
