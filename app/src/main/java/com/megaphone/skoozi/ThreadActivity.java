package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.skoozi_959.skooziqna.Skooziqna;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsPostResponse;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ThreadActivity extends ActionBarActivity
        implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    static final String EXTRA_QUESTION = "com.megaphone.skoozi.extras.question_parcel";
    static final String BROADCAST_THREAD_ANSWERS_RESULT = "com.megaphone.skoozi.broadcast.THREAD_ANSWERS_RESULT";
    static final String EXTRAS_THREAD_ANSWERS  = "com.megaphone.skoozi.extras.THREAD_ANSWERS";

    private Toolbar mToolbar;
    private GoogleMap newQuestionMap;

    private Question threadQuestion;
    private TextView textAuthor;
    private TextView textContent;

    private List<Answer> threadAnswers;
    private ListView threadAnswerListView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            threadAnswers = intent.getParcelableArrayListExtra(ThreadActivity.EXTRAS_THREAD_ANSWERS);
            updateThreadAnswerList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_thread);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        mToolbar = (Toolbar) findViewById(R.id.new_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        threadQuestion = getIntent().getParcelableExtra(EXTRA_QUESTION);

        threadAnswerListView = (ListView) findViewById(R.id.thread_answer_list);

        textAuthor = (TextView) findViewById(R.id.thread_profile_name);
        textContent = (TextView) findViewById(R.id.thread_question_content);

        textAuthor.setText(threadQuestion.getAuthor());
        textContent.setText(threadQuestion.getContent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //TODO: determine if MainActivity is well and truly the parent activity to all this stuff
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_add_q) {
            return true;
        } else if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.thread_map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }
        IntentFilter mIntentFilter = new IntentFilter(ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
        SkooziQnARequestService.startActionGetThreadAnswers(this, threadQuestion.getKey());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        newQuestionMap = map;
        if (threadQuestion.getLocationLat() != 0.0 && threadQuestion.getLocationLat() != 0.0 ) {
            LatLng postLocation = new LatLng(threadQuestion.getLocationLat(),threadQuestion.getLocationLon());
            newQuestionMap.addMarker(new MarkerOptions()
                    .position(postLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                    .title("Current location"))
            newQuestionMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 15));
        } else {
            //TODO: need to decide what to do if the post location is not present
        }
    }

    private void updateThreadAnswerList() {
        ThreadAnswerListAdapter mThreadListAdapter = new ThreadAnswerListAdapter(threadAnswers);
        threadAnswerListView.setAdapter(mThreadListAdapter);
    }

    private class ThreadAnswerListAdapter extends BaseAdapter {


        public ThreadAnswerListAdapter(List<Answer> answers) {
            threadAnswers = answers;
        }
        @Override
        public int getCount() {
            /**
             * TODO: this count should be memoized
             * http://stackoverflow.com/questions/8921162/how-to-update-listview-on-scrolling-while-retrieving-data-from-server-in-android
             */
            return threadAnswers.size();
        }

        @Override
        public Object getItem(int arg0) {
            return threadAnswers.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.nearby_list_row, parent, false);
            }
            TextView textAuthor = (TextView) convertView.findViewById(R.id.nearby_list_profile_name);
            TextView textContent = (TextView) convertView.findViewById(R.id.nearby_list_question);

            Answer currentAnswer = threadAnswers.get(position);
            textAuthor.setText(currentAnswer.getAuthor());
            textContent.setText(currentAnswer.getContent());

            //TODO: display the answers on the map
//            mMapQCallback.onMapQuestion(currentAnswer.getLocationLat(), currentAnswer.getLocationLon());
            return convertView;
        }
    }



}
