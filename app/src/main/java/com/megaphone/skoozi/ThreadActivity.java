package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.List;


public class ThreadActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    static final String EXTRA_QUESTION = "com.megaphone.skoozi.extras.question_parcel";
    static final String BROADCAST_THREAD_ANSWERS_RESULT = "com.megaphone.skoozi.broadcast.THREAD_ANSWERS_RESULT";
    static final String EXTRAS_THREAD_ANSWERS  = "com.megaphone.skoozi.extras.THREAD_ANSWERS";
    static final String BROADCAST_POST_ANSWER_RESULT = "com.megaphone.skoozi.broadcast.POST_ANSWER_RESULT";
    static final String EXTRAS_ANSWER_KEY  = "com.megaphone.skoozi.extras.ANSWER_KEY";

    CoordinatorLayout mLayoutView;
    private CollapsingToolbarLayout collapsingToolbar;

    private Question threadQuestion;
    private List<Answer> threadAnswers; //null value is good and well
    private RecyclerView threadAnswerRecycler;

    private EditText answerContent;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ThreadActivity.BROADCAST_POST_ANSWER_RESULT:
                    String answer_key = intent.getStringExtra(ThreadActivity.EXTRAS_ANSWER_KEY);
                    if (answer_key == null) {
                        //TODO: use a Snackbar here with option to retry :)
                        Toast.makeText(context, "Looks like there was an error. Please try again.", Toast.LENGTH_SHORT).show();
                    } else {
                        refreshThreadList();
                    }
                    break;
                case ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT:
                default:
                    threadAnswers = intent.getParcelableArrayListExtra(ThreadActivity.EXTRAS_THREAD_ANSWERS);
                    updateThreadAnswerList();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_thread);

        mLayoutView = (CoordinatorLayout) findViewById(R.id.thread_coordinator_layout);
        setupToolbar();

        threadQuestion = getIntent().getParcelableExtra(EXTRA_QUESTION);
        setActivityTitle(threadQuestion.author);

        //TODO: need to put in a check to ensure that threadQuestion is properly retrieved

        threadAnswerRecycler = (RecyclerView) findViewById(R.id.thread_answer_recycler);
//        threadAnswerRecycler.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        threadAnswerRecycler.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        threadAnswerRecycler.addItemDecoration(mItemDecoration);

        ImageButton postAnswer = (ImageButton) findViewById(R.id.thread_reply_post);
        answerContent = (EditText) findViewById(R.id.thread_reply_content);
        postAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validContent(answerContent.getText().toString())) {
                    ((ThreadActivity) v.getContext()).insertSkooziServiceAnswer(answerContent.getText().toString());
                }
            }
        });
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
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    }

    private boolean validContent(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(mLayoutView, R.string.reply_error_message, Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
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
        setupLocalBroadcastPair();
        refreshThreadList();
    }

    private void setupLocalBroadcastPair() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT);
        mIntentFilter.addAction(ThreadActivity.BROADCAST_POST_ANSWER_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
    }

    /**
     * Makes a new call to the SkooziQnA API to get all the answers related to the question
     */
    public void refreshThreadList() {
        if (ConnectionUtil.isDeviceOnline()) {
            SkooziQnARequestService.startActionGetThreadAnswers(this, threadQuestion.key);
        } else {
            displayNetworkErrorMessage();
        }
    }

    /**
     * Creates an instance of Answer and calls the SkooziQnA API intent service to post
     * @param content
     */
    public void insertSkooziServiceAnswer(String content) {
        if (ConnectionUtil.isDeviceOnline()) {
            Answer mAnswer = new Answer(threadQuestion.key,
                    "response@response.com", //TODO: this needs to be fixed once OAuth is setup
                    content,
                    System.currentTimeMillis() / 1000L,
                    12,//TODO: need to figure out the current location
                    12);
            SkooziQnARequestService.startActionInsertAnswer(this, threadQuestion.key, mAnswer);
        } else {
            displayNetworkErrorMessage();
        }
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
        GoogleMap newQuestionMap = map;
        if (threadQuestion.locationLat != 0.0 && threadQuestion.locationLat != 0.0 ) {
            LatLng postLocation = new LatLng(threadQuestion.locationLat,threadQuestion.locationLon);
            newQuestionMap.addMarker(new MarkerOptions()
                    .position(postLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                    .title("Current location"))
            newQuestionMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 15));
        } else {
            //TODO: need to decide what to do if the post location is not present
        }
    }

    /**
     * This method is called after the API request to get answers for the question has been made
     */
    private void updateThreadAnswerList() {
        ThreadRecyclerViewAdapter mAdapter = new ThreadRecyclerViewAdapter(this, threadAnswers);

        List<ThreadSectionedAdapter.Section> sections = new ArrayList<>();
        //Sections
        sections.add(new ThreadSectionedAdapter.Section(0,threadQuestion));

        ThreadSectionedAdapter mSectionedAdapter = new
                ThreadSectionedAdapter(this,R.layout.section_thread,
                R.id.section_question_content, R.id.section_question_timestamp, //represents the section header content
                mAdapter);
        ThreadSectionedAdapter.Section[] dummy = new ThreadSectionedAdapter.Section[sections.size()];
        mSectionedAdapter.setSections(sections.toArray(dummy));

        threadAnswerRecycler.setAdapter(mSectionedAdapter);
    }

    private void displayNetworkErrorMessage() {
        Snackbar.make(mLayoutView, R.string.no_network_message, Snackbar.LENGTH_LONG)
                .show();
    }

}
