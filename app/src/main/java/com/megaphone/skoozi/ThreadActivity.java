package com.megaphone.skoozi;

import android.accounts.AccountManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.List;


public class ThreadActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    public static final String EXTRA_QUESTION = "com.megaphone.skoozi.extra.question_parcel";
    public static final String EXTRA_THREAD_ANSWERS = "com.megaphone.skoozi.extra.THREAD_ANSWERS";
    public static final String EXTRA_ANSWER_KEY = "com.megaphone.skoozi.extra.ANSWER_KEY";
    public static final String BROADCAST_THREAD_ANSWERS_RESULT = "com.megaphone.skoozi.broadcast.THREAD_ANSWERS_RESULT";
    public static final String BROADCAST_POST_ANSWER_RESULT = "com.megaphone.skoozi.broadcast.POST_ANSWER_RESULT";
    public static final String ACTION_THREAD_REPLY = "com.megaphone.skoozi.action.THREAD_REPLY";

    CoordinatorLayout mLayoutView;
    private CollapsingToolbarLayout collapsingToolbar;

    private Question threadQuestion;
    private List<Answer> threadAnswers; //null value is good and well
    private RecyclerView threadAnswerRecycler;
    private ThreadRecyclerViewAdapter mAdapter;
    private Answer latestThreadAnswer;
    private LinearLayout threadReply;
    private EditText answerContent;
    private FloatingActionButton threadReplyFab;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ThreadActivity.BROADCAST_POST_ANSWER_RESULT:
                    String answer_key = intent.getStringExtra(ThreadActivity.EXTRA_ANSWER_KEY);
                    if (answer_key == null) {
                        //TODO: use a Snackbar here with option to retry :)
                        Toast.makeText(context, "Looks like there was an error. Please try again.", Toast.LENGTH_SHORT).show();
                    } else {
                        handleResponseSuccessful();
                    }
                    break;
                case ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT:
                default:
                    threadAnswers = intent.getParcelableArrayListExtra(ThreadActivity.EXTRA_THREAD_ANSWERS);
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
        if (threadQuestion == null) return;

        setActivityTitle(threadQuestion.author);

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
            startMainActivity();
            return true;
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startMainActivity();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ConnectionUtil.isDeviceOnline()) {
                if (SkooziApplication.getUserAccount() == null) {
                    AccountUtil.pickUserAccount(ThreadActivity.this, ACTION_THREAD_REPLY);
                } else {
                    threadReplyFab.setEnabled(true);
                }

                MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.thread_map);
                if (mMapFragment != null) {
                    mMapFragment.getMapAsync(this);
                }
                setupLocalBroadcastPair();
                refreshThreadList();
            } else {
                ConnectionUtil.displayNetworkErrorMessage(mLayoutView);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AccountUtil.REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    // Receiving a result from the AccountPicker
                    SkooziApplication.setUserAccount(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    String action = data.getStringExtra(AccountUtil.EXTRA_USER_ACCOUNT_ACTION); //can return null
                    if (action != null && action.equals(ACTION_THREAD_REPLY)) {
                        threadReplyFab.setEnabled(true); // ok to proceed
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    threadReplyFab.setEnabled(false);
                    AccountUtil.displayAccountLoginErrorMessage(mLayoutView);
                }
                break;
        }
    }

    private void setupLocalBroadcastPair() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT);
        mIntentFilter.addAction(ThreadActivity.BROADCAST_POST_ANSWER_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
    }

    private AccountUtil.GoogleAuthTokenExceptionListener tokenListener = new AccountUtil.GoogleAuthTokenExceptionListener() {
        @Override
        public void handleGoogleAuthException(final UserRecoverableAuthException exception) {
            // Because this call comes from the AsyncTask, we must ensure that the following
            // code instead executes on the UI thread.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountUtil.resolveAuthExceptionError(ThreadActivity.this, exception);
                }
            });
        }
    };
    /**
     * Makes a new call to the SkooziQnA API to get all the answers related to the question
     */
    public void refreshThreadList() {
        if (ConnectionUtil.isDeviceOnline()) {
            SkooziQnARequestService.startActionGetThreadAnswers(this, tokenListener, threadQuestion.key);
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
            latestThreadAnswer = new Answer(threadQuestion.key,
                    SkooziApplication.getUserAccount().name, //TODO: this needs to be fixed once OAuth is setup
                    content,
                    System.currentTimeMillis() / 1000L,
                    12,//TODO: need to figure out the current location
                    12);
            SkooziQnARequestService.startActionInsertAnswer(this, tokenListener, threadQuestion.key, latestThreadAnswer);
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
        if (mAdapter == null || latestThreadAnswer == null) return;
        //todo: need to see if there's a better way to verify that this answer_key correspnds to the
        //one that i sent out
        //one approach could be to generate a local guid and match that one to figure out if this is the right one
        mAdapter.add(0, latestThreadAnswer);
        answerContent = (EditText) findViewById(R.id.thread_reply_content);
        answerContent.setText("");
        hideKeyboard();
        animateFabAppear();
        latestThreadAnswer = null;
    }

    /**
     * This method is called after the API request to get answers for the question has been made
     */
    private void updateThreadAnswerList() {
        mAdapter = new ThreadRecyclerViewAdapter(this, threadAnswers);

        List<ThreadSectionedAdapter.Section> sections = new ArrayList<>();
        //Sections
        sections.add(new ThreadSectionedAdapter.Section(0, threadQuestion));

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

    private boolean isValidContent(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(mLayoutView, R.string.reply_error_message, Snackbar.LENGTH_SHORT).show();
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
