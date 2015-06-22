package com.megaphone.skoozi;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class ThreadActivity extends ActionBarActivity
        implements OnMapReadyCallback {

    private static final String TAG = "ThreadActivity";
    static final String EXTRA_QUESTION = "question_parcel";

    private Toolbar mToolbar;
    private GoogleMap newQuestionMap;

    private Question threadQuestion;
    private TextView textAuthor;
    private TextView textContent;

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

    private class InsertQuestionAsyncTask extends AsyncTask<Question, Void, String> {
        private Skooziqna skooziqnaService = null;

        @Override
        protected void onPreExecute() {
//            postQuestionProgress.setVisibility(View.VISIBLE);
//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(postQuestionText.getWindowToken(), 0);
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
                        .setRootUrl(getString(R.string.app_api_url));
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
//            postQuestionProgress.setVisibility(View.INVISIBLE);
//            if (result != null) {
//                showToast(true);
//            } else {
//                showToast(false);
//            }
        }

    }

}
