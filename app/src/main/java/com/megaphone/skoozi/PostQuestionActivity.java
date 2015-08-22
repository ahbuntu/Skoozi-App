package com.megaphone.skoozi;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appspot.skoozi_959.skooziqna.Skooziqna;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsPostResponse;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;

import java.io.IOException;


public class PostQuestionActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "PostQuestionActivty";
    public static final String ACTION_NEW_QUESTION  = "com.megaphone.skoozi.action.NEW_QUESTION";

    Toolbar mToolbar;
    GoogleMap newQuestionMap;
    Location mLastLocation;
    LatLng postLocation;
    GoogleApiClient mGoogleApiClient;

    private EditText postQuestionText;
    private ProgressBar postQuestionProgress;
    private CoordinatorLayout coordinatorLayout;
    private Button postQuestionButton;

    Question postQuestion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_question);

        setupToolbar();

        buildGoogleApiClient();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.new_question_coordinator_layout);
        postQuestionProgress = (ProgressBar) findViewById(R.id.new_question_progress);
        postQuestionText = (EditText) findViewById(R.id.new_question_content);
        postQuestionButton = (Button) findViewById(R.id.post_new_question);
        postQuestionButton.setEnabled(false);
        postQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postContent = postQuestionText.getText().toString().trim();
                if (validContent(postContent) && postLocation != null) {
                    postQuestion = new Question("test", postContent,
                            "what's a key", (long) 123123,
                            postLocation.latitude, postLocation.longitude);
                    new InsertQuestionAsyncTask().execute(postQuestion);
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
//        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean validContent(String value) {
        if (TextUtils.isEmpty(value)) {
            Snackbar.make(coordinatorLayout, R.string.new_question_error_message, Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ConnectionUtil.isDeviceOnline()) {
                if (SkooziApplication.getUserAccount() == null) {
                    AccountUtil.pickUserAccount(PostQuestionActivity.this, ACTION_NEW_QUESTION);
                } else {
                    postQuestionButton.setEnabled(true);
                }

                MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_question_map);
                if (mMapFragment != null) {
                    mMapFragment.getMapAsync(this);
                }
            } else {
                displayNetworkErrorMessage();
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
                    if (action != null && action.equals(ACTION_NEW_QUESTION)) {
                        postQuestionButton.setEnabled(true); // ok to proceed
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    postQuestionButton.setEnabled(false);
                    displayAccountLoginErrorMessage();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (!mResolvingError) {  // more about this later
        mGoogleApiClient.connect();
//        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation == null)
            //todo: display persistent message
            return;
        updateCurrentLocation();
    }
    @Override
    public void onConnectionSuspended (int cause) {
        Log.d(TAG, "BOO - connection suspended due to " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the next section.
        Log.d(TAG, "connection to GPS failed for " + result.toString());
    }
    private void updateCurrentLocation() {
        if (newQuestionMap != null) {
            postLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            newQuestionMap.addMarker(new MarkerOptions()
                    .position(postLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current location"));
            newQuestionMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        newQuestionMap = map;
        if (postLocation == null) {
            //TODO: show persistent message that GPS needs to be enabled in order to post new question
            return;
        }
        updateCurrentLocation();
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

    private void displayNetworkErrorMessage() {
        Snackbar.make(coordinatorLayout, R.string.no_network_message, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }

    private void displayAccountLoginErrorMessage() {
        Snackbar.make(coordinatorLayout, R.string.no_account_login_message, Snackbar.LENGTH_LONG)
                .show();
    }

    private class InsertQuestionAsyncTask extends AsyncTask<Question, Void, String> {
        private Skooziqna skooziqnaService;

        @Override
        protected void onPreExecute() {
            postQuestionProgress.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(postQuestionText.getWindowToken(), 0);
        }

        /**
         * Calls REST API to insert question
         */
        @Override
        protected String doInBackground(Question... params) {
            String postKey = null;
            if (skooziqnaService == null) { // do this once
                Skooziqna.Builder builder = new Skooziqna.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(getString(R.string.app_api_url))
                        // turn off compression when running against local devappserver (via emulator)
//                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                        @Override
//                        public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
//                            request.setDisableGZipContent(true);
//                        }
//                    })
                        ; //end devserver options
                skooziqnaService = builder.build();
            }
            Question userQuestion = params[0];
            try {
                CoreModelsQuestionMessage question = new CoreModelsQuestionMessage();

                question.setEmail("proper@proper.com");
                question.setContent(userQuestion.content);
                question.setLocationLat(userQuestion.locationLat);
                question.setLocationLon(userQuestion.locationLon);
                question.setTimestampUnix(System.currentTimeMillis() / 1000L);

                CoreModelsPostResponse insertResponse = skooziqnaService.question().insert(question).execute();
                //TODO: figure out if I need to do anything with this
                postKey = insertResponse.getPostKey();

            } catch (IOException e) {
                // TODO: Check for network connectivity before starting the AsyncTask.
                Log.e(TAG, e.getMessage());
            }
            return postKey;
        }

        @Override
        protected void onPostExecute(String result) {
            postQuestionProgress.setVisibility(View.INVISIBLE);
            showThread(result);
        }

    }


}
