package com.megaphone.skoozi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Material design sliding tab implementation taken from
 * http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
 */
/**
 * Material design toolbar implementation taken from
 * http://antonioleiva.com/material-design-everywhere/
 */
/**
 * Refer to this for backwards compatibility
 * http://stackoverflow.com/questions/26449454/extending-activity-or-actionbaractivity
 */
public class MainActivity extends ActionBarActivity
                            implements OnMapReadyCallback, NearbyFragment.OnMapQuestionsCallback, NearbyFragment.OnQuestionItemSelected,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    public static final String BROADCAST_QUESTIONS_LIST_RESULT = "com.megaphone.skoozi.broadcast.QUESTIONS_LIST_RESULT";
    public static final String EXTRAS_QUESTIONS_LIST  = "com.megaphone.skoozi.extras.QUESTIONS_LIST";

    Toolbar mToolbar;
    GoogleMap nearbyMap;
    private NearbyFragment nearbyFragment;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    Marker defaultMarker, currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

//        pickUserAccount();
        if (findViewById(R.id.main_fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            /**
             * when it's better to use newInstance
             * http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
             */
            // Create a new Fragment to be placed in the activity layout
            nearbyFragment = NearbyFragment.newInstance(this);

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, nearbyFragment).commit();
        }

        buildGoogleApiClient();
    }


    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private void pickUserAccount() {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        //TODO: need to put in check to see if there is only one account - if so, use that as default
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, true, null, null, null, null); //alwaysPromptForAccount = true
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
//                Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
            }
        }
        // Handle the result from exceptions
//        ...
    }

    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private int counter = 0;
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter mIntentFilter = new IntentFilter(BROADCAST_QUESTIONS_LIST_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
        counter++;
        try {
            SkooziQnARequestService.startActionGetQuestionsList(this);
//            URL get_questions_url = new URL("http://valued-pact-89315.appspot.com/_get_questions");
//            new GetQuestionsTask().execute(get_questions_url);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
//        catch (MalformedURLException e) {
//            Log.d(TAG, e.getMessage());
//        }

//        pickUserAccount();
        Log.d(TAG, "onResume has been called - " + counter + " times");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add_q) {
            Intent intent = new Intent(this, PostQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_home) {
            //TODO: maybe do a refresh?
//            Toast.makeText(this,"nearby activity",Toast.LENGTH_SHORT).show();
//            return true;
        } else if (id == R.id.action_my_activity) {
            Toast.makeText(this,"my activity",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            updateCurrentLocation();
        }
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
        if (nearbyMap != null) {
            if (defaultMarker != null) {
                defaultMarker.remove();
            }
            LatLng currentLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            nearbyMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        nearbyMap = map;
        LatLng defaultLocation;
        if (mLastLocation != null) {
            updateCurrentLocation();
        } else {
            defaultLocation = new LatLng(43.6532,-79.3832);
            // Move the camera instantly to Toronto
            defaultMarker = nearbyMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Default location"));
            nearbyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
        }
    }

    @Override
    public void onMapQuestion(double lat, double lon) {
        LatLng questionLocation = new LatLng(lat,lon);
        nearbyMap.addMarker(new MarkerOptions()
                .position(questionLocation));
    }

    private void updateNearbyList(List<Question> questions) {
        if (nearbyFragment != null) {
            nearbyFragment.updateNearbyQuestions(questions);
        }
    }


    @Override
    public void onQuestionSelected(Question mQuestion) {
        Intent threadIntent = new Intent(this, ThreadActivity.class);
        Bundle questionBundle = new Bundle();
        questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, mQuestion);
        threadIntent.putExtras(questionBundle);
        startActivity(threadIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Question> questions = intent.getParcelableArrayListExtra(EXTRAS_QUESTIONS_LIST);
            Log.d(TAG, String.valueOf(questions.size()));
            updateNearbyList(questions);
        }
    };

    private boolean isDeviceOnline() {
        //TODO: this method needs to check whether network connectivity is avaiable
        //https://developer.android.com/training/basics/network-ops/connecting.html
        return true;
    }

    //http://stackoverflow.com/questions/10400428/can-i-use-androids-accountmanager-for-getting-oauth-access-token-for-appengine
    private final static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private final static String USERINFO_PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String SCOPE = "oauth2:" + USERINFO_EMAIL_SCOPE + " " + USERINFO_PROFILE_SCOPE;


    private Account getAccountForName(Context context, String username) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google"); // gmail.com is within google.com type
        if (accounts != null) {
            for (Account account : accounts) {
                if (account.name.equals(username)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {

        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
//                new GetUsernameTask(this, mEmail, SCOPE).execute();
                new GetUsernameTask(this,getAccountForName(this,mEmail)).execute();
            } else {
//                Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
                //TODO: need to dislay proper error message
            }
        }
    }

    public void handleGoogleAuthTokenException(UserRecoverableAuthException exception) {
        //TODO: need to determine how to properlyt handle this
    }
    private class GetQuestionsTask extends AsyncTask<URL, Integer, List<Question>> {

        protected List<Question> doInBackground(URL... urls) {
            long totalSize = 0;
            List<Question> downloadedQuestions = new ArrayList<>();

            String questionsStringified = getNearbyQuestions(urls[0].toString());
            try {
                JSONObject result = new JSONObject(questionsStringified);
                JSONArray questionsArray = result.getJSONArray("result");
                for (int i = 0; i < questionsArray.length(); i++) {
                    JSONObject jsonobject = questionsArray.getJSONObject(i);
                    String added_by = jsonobject.getString("added_by");
                    String content = jsonobject.getString("content");
                    String key = jsonobject.getString("key");
                    String timestamp = jsonobject.getString("timestamp");
                    JSONObject locationJSON = jsonobject.getJSONObject("location");
                    String locationLat = locationJSON.getString("lat");
                    String locationLon = locationJSON.getString("lon");
                    downloadedQuestions.add(new Question(added_by, content, key, timestamp,
                            Double.parseDouble(locationLat), Double.parseDouble(locationLon)));
                }
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return downloadedQuestions;
        }

        /**
         * Implementation taken from
         * http://www.vogella.com/tutorials/AndroidJSON/article.html
         * @param getPath
         * @return
         */
        public String getNearbyQuestions(String getPath) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getPath);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.d(TAG, "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(List<Question> result) {
            updateNearbyList(result);
        }
    }


}
