package com.megaphone.skoozi;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.megaphone.skoozi.base.BaseActivity;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.SharedPrefsButler;

public class UserAccountActivity extends BaseActivity implements OnMapReadyCallback {
    private static final String TAG = UserAccountActivity.class.getSimpleName();

    private Location selfLocation;
    private GoogleMap notificationMap;
    private TextInputLayout nicknameEditContainer;
    private ImageButton nicknameEditDone;
    private TextView nickname;
    private TextView userSignedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        nicknameEditContainer = (TextInputLayout) findViewById(R.id.nickname_edit_container);
        nicknameEditDone = (ImageButton) findViewById(R.id.nickname_edit_done);
        nickname = (TextView) findViewById(R.id.user_nickname);
        setupToolbar();

        userSignedAs = (TextView) findViewById(R.id.user_signed_as);
        userSignedAs.setText(
                getString(R.string.user_details_signed_in, SkooziApplication.getUserAccount().name));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ConnectionUtil.hasNetwork(coordinatorLayout)) connectToGoogleApi();

//        if (mapFragment != null)  mapFragment.getMapAsync(this);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        notificationMap = map;
//        if (selfLocation != null) updateQuestionMap(10); // TODO: 2016-02-02 need to figure out correct radius value to use here
    }

    @Override
    protected void googleAccountSelected(String accountName) {
        super.googleAccountSelected(accountName);
    }

    @Override
    protected void googleAccountNotSelected() {
        super.googleAccountNotSelected();
        AccountUtil.displayAccountSignInErrorMessage(this, coordinatorLayout);
    }

    @Override
    protected void oAuthAuthenticationGranted() {
        super.oAuthAuthenticationGranted();
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
//        selfLocation = PermissionUtil.tryGetLatestLocation(UserAccountActivity.this, getGoogleApiClient());
//        updateQuestionMap(10); // TODO: 2016-02-02 need to figure out correct radius value to use here
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        if (SharedPrefsButler.getUserNickname() == null) {
            toolbar.setTitle("");
            nicknameEditContainer.setVisibility(View.VISIBLE);
            nicknameEditDone.setVisibility(View.VISIBLE);
            nickname.setVisibility(View.GONE);
        } else {
            toolbar.setTitle(SharedPrefsButler.getUserNickname());
            nicknameEditContainer.setVisibility(View.GONE);
            nicknameEditDone.setVisibility(View.GONE);
            nickname.setVisibility(View.VISIBLE);
        }
    }
}
