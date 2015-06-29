package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class ThreadActivity extends ActionBarActivity
        implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    static final String EXTRA_QUESTION = "com.megaphone.skoozi.extras.question_parcel";
    static final String BROADCAST_THREAD_ANSWERS_RESULT = "com.megaphone.skoozi.broadcast.THREAD_ANSWERS_RESULT";
    static final String EXTRAS_THREAD_ANSWERS  = "com.megaphone.skoozi.extras.THREAD_ANSWERS";

    private CollapsingToolbarLayout collapsingToolbar;
    private GoogleMap newQuestionMap;

    private Question threadQuestion;

    private List<Answer> threadAnswers;
    private RecyclerView threadAnswerRecycler;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            threadAnswers = intent.getParcelableArrayListExtra(ThreadActivity.EXTRAS_THREAD_ANSWERS);
            updateThreadAnswerList();
        }
    };


//    private ListView threadAnswerListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_thread);

        setupToolbar();

        threadQuestion = getIntent().getParcelableExtra(EXTRA_QUESTION);
        setActivityTitle(threadQuestion.getAuthor());

        //TODO: need to put in a check to ensure that threadQuestion is properly retrieved

//        threadAnswerListView = (ListView) findViewById(R.id.thread_answer_list);
        threadAnswerRecycler = (RecyclerView) findViewById(R.id.thread_answer_recycler);
        threadAnswerRecycler.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        threadAnswerRecycler.setLayoutManager(mLayoutManager);

    }

    /**
     * Creating The Toolbar and setting it as the Toolbar for the activity
     * home as up set to true
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show menu icon
        final ActionBar ab = getSupportActionBar();
//        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    }

    private void setActivityTitle(String title) {
        collapsingToolbar.setTitle(title);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
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
        ThreadRecyclerViewAdapter mAdapter = new ThreadRecyclerViewAdapter(this, threadAnswers);

        // This is the code to provide a sectioned list
        // https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
        List<ThreadSectionedAdapter.Section> sections = new ArrayList<>();
        //Sections
        sections.add(new ThreadSectionedAdapter.Section(0,threadQuestion.getContent()));

        ThreadSectionedAdapter mSectionedAdapter = new
                ThreadSectionedAdapter(this,R.layout.section_thread,R.id.section_question_content,mAdapter);
        ThreadSectionedAdapter.Section[] dummy = new ThreadSectionedAdapter.Section[sections.size()];
        mSectionedAdapter.setSections(sections.toArray(dummy));

        threadAnswerRecycler.setAdapter(mSectionedAdapter);
    }

}
