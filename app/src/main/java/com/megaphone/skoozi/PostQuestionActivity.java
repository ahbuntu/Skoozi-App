package com.megaphone.skoozi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.PermissionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

public class PostQuestionActivity extends BaseActivity {

    private static final String TAG = "PostQuestionActivty";

    private EditText postQuestionText;
    private ProgressBar progressBar;
    private Button postQuestionButton;
    private Location selfLocation;
    private boolean requestInProgress;
    private Question postQuestion;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver skooziApiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestInProgress = false;
            progressBar.setVisibility(View.GONE);
            if (intent.getAction().equals(SkooziQnAUtil.BROADCAST_POST_QUESTION_RESULT)) {
                String questionKey = intent.getStringExtra(SkooziQnAUtil.EXTRA_QUESTION_KEY);
                if (questionKey == null) {
                    Snackbar.make(coordinatorLayout, R.string.error_posting_new_question, Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    showThread(questionKey);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_question);
        setupToolbar();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.new_question_coordinator_layout);
        progressBar = (ProgressBar) findViewById(R.id.new_question_progress);
        postQuestionText = (EditText) findViewById(R.id.new_question_content);
        setupPostButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ConnectionUtil.hasNetwork(coordinatorLayout)) connectToGoogleApi();

        if (SkooziApplication.hasUserAccount()) postQuestionButton.setEnabled(true);
    }

    @Override
    public void onPause(){
        super.onPause();
        destroyLocalBroadcastPair();
    }

    @Override
    protected void googleAccountSelected(String accountName) {
        super.googleAccountSelected(accountName);
        postQuestionButton.setEnabled(true);
    }

    @Override
    protected void googleAccountNotSelected() {
        AccountUtil.displayAccountSignInErrorMessage(coordinatorLayout);
        postQuestionButton.setEnabled(false);
    }

    @Override
    protected void oAuthAuthenticationGranted() {
        if (requestInProgress) {
            // finish up the requested action
            String content = postQuestionText.getText().toString().trim();
            tryPostQuestionToApi(content);
        }
    }

    @Override
    protected void oAuthAuthenticationDenied() {
        // figure out what to do here
        throw new RuntimeException(TAG + ": not yet implemented");
    }

    @Override
    protected void onGoogleApiConnected() {
        selfLocation = PermissionUtil.tryGetLatestLocation(PostQuestionActivity.this, getGoogleApiClient());
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
        progressBar.setVisibility(View.GONE);
        if (questionKey != null) {
            postQuestionText.setText("");
            postQuestion.key = questionKey;
            Intent threadIntent = new Intent(this, ThreadActivity.class);
            threadIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
            Bundle questionBundle = new Bundle();
            questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, postQuestion);
            threadIntent.putExtras(questionBundle);
            startActivity(threadIntent);
        } else {
            Toast.makeText(this, "Looks like there was an error :(", Toast.LENGTH_SHORT).show();
        }
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
}
