package com.megaphone.skoozi;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.io.IOException;


public class PostQuestionActivity extends ActionBarActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "PostQuestionActivty";
    Toolbar mToolbar;
    GoogleMap newQuestionMap;
    Location mLastLocation;
    LatLng postLocation;
    GoogleApiClient mGoogleApiClient;

    EditText postQuestionText;
    Button postQuestionButton;
    ProgressBar postQuestionProgress;

    Question postQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_question);

        setupToolbar();

        buildGoogleApiClient();

        postQuestionProgress = (ProgressBar) findViewById(R.id.new_question_progress);
        postQuestionText = (EditText) findViewById(R.id.new_question_content);
        postQuestionButton = (Button) findViewById(R.id.post_new_question_button);
        postQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postContent = postQuestionText.getText().toString().trim();
                if (postLocation != null && postContent.length() != 0) {
                    postQuestion = new Question("test", postContent,
                            "what's a key", "why is timestamp string",
                            postLocation.latitude, postLocation.longitude );
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
            //TODO: determine if MainActivity is well and truly the parent activity to all this stuff
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_add_q) {
            return true;
//        } else if (id == R.id.action_home) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            return true;
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_question_map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
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
        if (postLocation != null) {
//            defaultLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            updateCurrentLocation();
        } else {
            //TODO: need to decide what to do if the current locaiton cannot be determined
        }
    }

    private void showThread(String key) {
        if (key != null) {
            postQuestion.setKey(key);
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
                question.setContent(userQuestion.getContent());
                question.setLocationLat(userQuestion.getLocationLat());
                question.setLocationLon(userQuestion.getLocationLon());
                question.setTimestampUTCsec(System.currentTimeMillis()/1000);

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
