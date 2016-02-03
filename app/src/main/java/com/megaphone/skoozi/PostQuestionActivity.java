package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.base.BaseActivity;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.thread.ThreadActivity;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.MapDecorator;
import com.megaphone.skoozi.util.PermissionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

public class PostQuestionActivity extends BaseActivity implements OnMapReadyCallback {
    private static final String TAG = PostQuestionActivity.class.getSimpleName();
    private static final String SMALL_CAPS = "smcp";

    private MapFragment mapFragment;
    private EditText postQuestionText;
    private ProgressBar progressBar;
    private Button postQuestionButton;
    private Location selfLocation;
    private boolean requestInProgress;
    private Question postQuestion;
    private IntentFilter mIntentFilter;
    private TextView postQuestionMapSection;
    private GoogleMap questionMap;

    private BroadcastReceiver skooziApiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SkooziQnAUtil.BROADCAST_POST_QUESTION_RESULT.equals(intent.getAction())) return;

            requestInProgress = false;
            progressBar.setVisibility(View.GONE);

            boolean success = intent.getBooleanExtra(SkooziQnAUtil.EXTRA_RESULT_SUCCESS, false);
            if (success) {
                String questionKey = intent.getStringExtra(SkooziQnAUtil.EXTRA_QUESTION_KEY);
                if (questionKey == null) {
                    Snackbar.make(coordinatorLayout, R.string.error_generic, Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    postQuestionText.setText("");
                    showThread(questionKey);
                }
            } else {
                Snackbar.make(coordinatorLayout, R.string.error_generic, Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_question);
        setupToolbar();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_question_map);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.new_question_coordinator_layout);
        progressBar = (ProgressBar) findViewById(R.id.new_question_progress);
        postQuestionText = (EditText) findViewById(R.id.new_question_content);
        postQuestionMapSection = (TextView) findViewById(R.id.new_question_map_section);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            postQuestionMapSection.setFontFeatureSettings(SMALL_CAPS);
        } else {
            postQuestionMapSection.setAllCaps(true);
        }
            setupPostButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ConnectionUtil.hasNetwork(coordinatorLayout)) connectToGoogleApi();

        if (SkooziApplication.hasUserAccount()) postQuestionButton.setEnabled(true);

        if (mapFragment != null)  mapFragment.getMapAsync(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        destroyLocalBroadcastPair();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        questionMap = map;
        if (selfLocation != null) updateQuestionMap(10); // TODO: 2016-02-02 need to figure out correct radius value to use here
    }

    @Override
    protected void googleAccountSelected(String accountName) {
        super.googleAccountSelected(accountName);
        postQuestionButton.setEnabled(true);
    }

    @Override
    protected void googleAccountNotSelected() {
        super.googleAccountNotSelected();
        AccountUtil.displayAccountSignInErrorMessage(coordinatorLayout);
        postQuestionButton.setEnabled(false);
    }

    @Override
    protected void oAuthAuthenticationGranted() {
        super.oAuthAuthenticationGranted();
        if (requestInProgress) {
            // finish up the requested action
            String content = postQuestionText.getText().toString().trim();
            tryPostQuestionToApi(content);
        }
    }

    @Override
    protected void oAuthAuthenticationDenied() {
        super.oAuthAuthenticationDenied();
        // figure out what to do here
        throw new RuntimeException(TAG + ": not yet implemented");
    }

    @Override
    protected void onGoogleApiConnected() {
        super.onGoogleApiConnected();
        selfLocation = PermissionUtil.tryGetLatestLocation(PostQuestionActivity.this, getGoogleApiClient());
        updateQuestionMap(10); // TODO: 2016-02-02 need to figure out correct radius value to use here
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupLocalBroadcastPair() {
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter(SkooziQnAUtil.BROADCAST_POST_QUESTION_RESULT);
            LocalBroadcastManager.getInstance(this).registerReceiver(skooziApiReceiver, mIntentFilter);
        }
    }

    private void destroyLocalBroadcastPair() {
        mIntentFilter = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(skooziApiReceiver);
    }

    private void showThread(String questionKey) {
        if (questionKey == null) return;

        postQuestion.key = questionKey;
        Intent threadIntent = new Intent(this, ThreadActivity.class);
        threadIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle questionBundle = new Bundle();
        questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, postQuestion);
        threadIntent.putExtras(questionBundle);
        startActivity(threadIntent);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setupPostButton() {
        postQuestionButton = (Button) findViewById(R.id.post_new_question);
        postQuestionButton.setEnabled(false);
        postQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPostButtonClicked();
            }
        });
    }

    private void onPostButtonClicked() {
        String content = postQuestionText.getText().toString().trim();
        if (isContentValid(content)) {
            tryPostQuestionToApi(content);
        }
    }

    private boolean isContentValid(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(coordinatorLayout, R.string.new_question_error_message, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void tryPostQuestionToApi(String content) {
        if (requestInProgress) return;
        if (selfLocation == null)  return; // can't do anything without a location

        setupLocalBroadcastPair();
        if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
            progressBar.setVisibility(View.VISIBLE);
            LatLng postLocation = new LatLng(selfLocation.getLatitude(), selfLocation.getLongitude());
            postQuestion = new Question(SkooziApplication.getUserAccount().name, content,
                    null, System.currentTimeMillis() / 1000L,
                    postLocation.latitude, postLocation.longitude);
            SkooziQnAUtil.postQuestionRequest(PostQuestionActivity.this, tokenListener, postQuestion);
            hideKeyboard();
            requestInProgress = true;
        }
    }

    private void updateQuestionMap(int radius) {
        questionMap.clear(); // important to ensure that everything is cleared
        MapDecorator.drawLocationMarker(questionMap, selfLocation);
        MapDecorator.drawNotificationArea(this, questionMap, selfLocation, radius);
    }
}
