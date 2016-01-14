package com.megaphone.skoozi;

import android.accounts.AccountManager;
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

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.PermissionUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;


public class PostQuestionActivity extends BaseActivity {

    private static final String TAG = "PostQuestionActivty";
    public static final String BROADCAST_POST_QUESTION_RESULT = "com.megaphone.skoozi.broadcast.POST_QUESTION_RESULT";
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";
    public static final String EXTRA_QUESTION_KEY  = "com.megaphone.skoozi.extra.QUESTION_KEY";

    private EditText postQuestionText;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;
    private Button postQuestionButton;
    private Location selfLocation;
    private boolean requestInProgress;
    private Question postQuestion;

    private BroadcastReceiver questionInsertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PostQuestionActivity.BROADCAST_POST_QUESTION_RESULT)) {
                String questionKey = intent.getStringExtra(PostQuestionActivity.EXTRA_QUESTION_KEY);
                if (questionKey == null) {
                    //TODO: use a Snackbar here with option to retry :)
                    Toast.makeText(context, "Looks like there was an error. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    showThread(questionKey);
                }
            }
        }
    };

    private AccountUtil.GoogleAuthTokenExceptionListener tokenListener = new AccountUtil.GoogleAuthTokenExceptionListener() {
        @Override
        public void handleGoogleAuthException(final UserRecoverableAuthException exception) {
            // Because this call comes from the IntentService, we must ensure that the following
            // code instead executes on the UI thread.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountUtil.resolveAuthExceptionError(PostQuestionActivity.this, exception);
                }
            });
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AccountUtil.REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    // Receiving a result from the AccountPicker
                    SkooziApplication.setUserAccount(this, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    String action = data.getStringExtra(AccountUtil.EXTRA_USER_ACCOUNT_ACTION); //can return null
                    if (action != null && action.equals(ACTION_NEW_QUESTION)) {
                        postQuestionButton.setEnabled(true); // ok to proceed
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    postQuestionButton.setEnabled(false);
                    AccountUtil.displayAccountLoginErrorMessage(coordinatorLayout);
                }
                break;
        }
    }

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

//        try {
//            if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
//                if (SkooziApplication.getUserAccount() == null) {
//                    AccountUtil.pickUserAccount(PostQuestionActivity.this, ACTION_NEW_QUESTION);
//                } else {
//                    postQuestionButton.setEnabled(true);
//                }
//
////                MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_question_map);
////                if (mMapFragment != null) {
////                    mMapFragment.getMapAsync(this);
////                }
//
//                setupLocalBroadcastPair();
//            }
//        } catch (Exception e) {
//            Log.d(TAG, e.getMessage());
//        }
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
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(PostQuestionActivity.BROADCAST_POST_QUESTION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(questionInsertReceiver, mIntentFilter);
    }

    private void showThread(String questionKey) {
        if (questionKey != null) {
            postQuestion.key = questionKey;
            Intent threadIntent = new Intent(this, ThreadActivity.class);
            Bundle questionBundle = new Bundle();
            questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, postQuestion);
            threadIntent.putExtras(questionBundle);
            startActivity(threadIntent);
        } else {
            Toast.makeText(this, "Looks like there was an error :(", Toast.LENGTH_SHORT).show();
        }
        postQuestionText.setText("");
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
        String postContent = postQuestionText.getText().toString().trim();
        if (isContentValid(postContent)) {
            if (selfLocation == null) {
                //todo: need to add in display error message for disabled GPS/location
                //ie. postLocation == null condition
            } else {
                // TODO: 2016-01-13 should use tryPostQuestionToApi() 
                LatLng postLocation = new LatLng(selfLocation.getLatitude(), selfLocation.getLongitude());
                postQuestion = new Question(SkooziApplication.getUserAccount().name, postContent,
                        null, System.currentTimeMillis() / 1000L,
                        postLocation.latitude, postLocation.longitude);
                SkooziQnARequestService.startActionInsertNewQuestion(PostQuestionActivity.this, tokenListener,
                        postQuestion);
                hideKeyboard();
            }
        }
    }

    private boolean isContentValid(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(coordinatorLayout, R.string.new_question_error_message, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
//    private void tryPostQuestionToApi() {
//        if (requestInProgress) return;
//        if (selfLocation == null)  return; // can't do anything without a location
//        if (!isContentValid(postCon))
//        setupLocalBroadcastPair();
//        if (ConnectionUtil.hasNetwork(coordinatorLayout)) {
//            progressBar.setVisibility(View.VISIBLE);
//            SkooziQnAUtil.quesListRequest(getActivity(), authTokenListener,
//                    searchOrigin, getSearchRadiusKm());
//            requestInProgress = true;
//        }
//    }
}
