package com.megaphone.skoozi.thread;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.base.BaseActivity;
import com.megaphone.skoozi.DividerItemDecoration;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

import java.util.ArrayList;
import java.util.List;


public class ThreadActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    public static final String EXTRA_QUESTION = "com.megaphone.skoozi.extra.question_parcel";

    public static final String ACTION_THREAD_REPLY = "com.megaphone.skoozi.action.THREAD_REPLY";

    private CollapsingToolbarLayout collapsingToolbar;
    private Question threadQuestion;
    private List<Answer> threadAnswers; //null value is good and well
    private RecyclerView threadAnswerRecycler;
    private ThreadRvAdapter rvAdapter;
    private Answer latestThreadAnswer;
    private LinearLayout threadReply;
    private EditText answerContent;
    private FloatingActionButton threadReplyFab;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver skooziApiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case SkooziQnAUtil.BROADCAST_POST_ANSWER_RESULT:
                    String answer_key = intent.getStringExtra(SkooziQnAUtil.EXTRA_ANSWER_KEY);
                    if (answer_key == null) {
                        Snackbar.make(coordinatorLayout, R.string.error_posting_new_question, Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        handleResponseSuccessful();
                    }
                    break;
                default:
                case SkooziQnAUtil.BROADCAST_THREAD_ANSWERS_RESULT:
                    threadAnswers = intent.getParcelableArrayListExtra(SkooziQnAUtil.EXTRA_THREAD_ANSWERS);
                    updateThreadResponse();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_thread);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.thread_coordinator_layout);
        setupToolbar();

        threadQuestion = getIntent().getParcelableExtra(EXTRA_QUESTION);
        if (threadQuestion == null) return;

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(threadQuestion.author);

        threadAnswerRecycler = (RecyclerView) findViewById(R.id.thread_answer_recycler);
        threadAnswerRecycler.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        threadAnswerRecycler.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        threadAnswerRecycler.addItemDecoration(mItemDecoration);

        threadReply = (LinearLayout) findViewById(R.id.thread_reply_container);
        threadReplyFab = (FloatingActionButton) findViewById(R.id.reply_fab);
        threadReplyFab.setEnabled(false);
        threadReplyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareResponse();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SkooziApplication.hasUserAccount()) threadReplyFab.setEnabled(true);

        MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.thread_map);
        if (mMapFragment != null) mMapFragment.getMapAsync(this);

        tryRefreshResponseThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyLocalBroadcastPair();
    }

    @Override
    protected void googleAccountSelected(String accountName) {
        super.googleAccountSelected(accountName);
        threadReplyFab.setEnabled(true);
    }

    @Override
    protected void googleAccountNotSelected() {
        super.googleAccountNotSelected();
        AccountUtil.displayAccountSignInErrorMessage(coordinatorLayout);
        threadReplyFab.setEnabled(false);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

    }

    private void setupLocalBroadcastPair() {
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(SkooziQnAUtil.BROADCAST_THREAD_ANSWERS_RESULT);
            mIntentFilter.addAction(SkooziQnAUtil.BROADCAST_POST_ANSWER_RESULT);
            LocalBroadcastManager.getInstance(this).registerReceiver(skooziApiReceiver, mIntentFilter);
        }
    }

    private void destroyLocalBroadcastPair() {
        mIntentFilter = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(skooziApiReceiver);
    }

    /**
     * Makes a new call to the SkooziQnA API to get all the answers related to the question
     */
    public void tryRefreshResponseThread() {
        setupLocalBroadcastPair();
        if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
            SkooziQnARequestService.startActionGetThreadAnswers(this, tokenListener, threadQuestion.key);
        }
    }

    /**
     * Creates an instance of Answer and calls the SkooziQnA API intent service to post
     * @param content
     */
    public void insertSkooziServiceAnswer(String content) {
        if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
            latestThreadAnswer = new Answer(threadQuestion.key,
                    SkooziApplication.getUserAccount().name, //TODO: this needs to be fixed once OAuth is setup
                    content,
                    System.currentTimeMillis() / 1000L,
                    12,//TODO: need to figure out the current location
                    12);
            SkooziQnARequestService.startActionInsertAnswer(this, tokenListener, threadQuestion.key, latestThreadAnswer);
        }
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
     * displays the response container and sets up the click listeners
     */
    private void prepareResponse() {
        ImageButton postAnswer = (ImageButton) findViewById(R.id.thread_reply_post);
        answerContent = (EditText) findViewById(R.id.thread_reply_content);
        postAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidContent(answerContent.getText().toString())) {
                    ((ThreadActivity) v.getContext()).insertSkooziServiceAnswer(answerContent.getText().toString());
                }
            }
        });
        animateFabDisappear();
    }

    /**
     * method invoked once a response has been SUCCESSFULLY provided and inserted
     */
    private void handleResponseSuccessful() {
        if (rvAdapter == null || latestThreadAnswer == null) return;
        //todo: need to see if there's a better way to verify that this answer_key correspnds to the
        //one that i sent out
        //one approach could be to generate a local guid and match that one to figure out if this is the right one
        rvAdapter.add(0, latestThreadAnswer);
        answerContent = (EditText) findViewById(R.id.thread_reply_content);
        answerContent.setText("");
        hideKeyboard();
        animateFabAppear();
        latestThreadAnswer = null;
    }

    /**
     * This method is called after the API request to get answers for the question has been made
     */
    private void updateThreadResponse() {
        rvAdapter = new ThreadRvAdapter();
        rvAdapter.setAnswers(threadAnswers);
        rvAdapter.setQuestion(threadQuestion);

        //Sections
        List<ThreadSection> sections = new ArrayList<>();
        sections.add(new ThreadSection(0, "", threadQuestion));

        ThreadSectionedAdapter<ThreadRvAdapter> sectionedAdapter = new ThreadSectionedAdapter<>(rvAdapter);
        sectionedAdapter.setSections(sections);

        threadAnswerRecycler.setAdapter(sectionedAdapter);
    }

    private boolean isValidContent(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(coordinatorLayout, R.string.reply_error_message, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void animateFabDisappear() {
        int cx = threadReply.getWidth() / 2;
        int cy = threadReply.getHeight() / 2;
        int finalRadius = Math.max(threadReply.getWidth(), threadReply.getHeight());

        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(threadReply, cx, cy, 0, finalRadius);
        }

        threadReply.setVisibility(View.VISIBLE);
        if (anim!= null) anim.start();
        threadReplyFab.setVisibility(View.GONE);
    }

//    https://developer.android.com/training/material/animations.html#Reveal
    private void animateFabAppear() {
        int cx = threadReplyFab.getWidth() / 2;
        int cy = threadReplyFab.getHeight() / 2;
        int finalRadius = Math.max(threadReplyFab.getWidth(), threadReplyFab.getHeight());

        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(threadReplyFab, cx, cy, 0, finalRadius);
        }

        threadReplyFab.setVisibility(View.VISIBLE);
        if (anim!= null) anim.start();
        threadReply.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
