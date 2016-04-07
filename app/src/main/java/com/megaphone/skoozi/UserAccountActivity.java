package com.megaphone.skoozi;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
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
    private SignInButton signinButton;
    private EditText nicknameEdit;
    private Snackbar snackbar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.user_account_coordinator_layout);
        nicknameEditContainer = (TextInputLayout) findViewById(R.id.nickname_edit_container);
        nicknameEditDone = (ImageButton) findViewById(R.id.nickname_edit_done);
        nickname = (TextView) findViewById(R.id.user_nickname);
        nicknameEdit = (EditText) findViewById(R.id.user_nickname_edit);
        userSignedAs = (TextView) findViewById(R.id.user_signed_as);
        signinButton = (SignInButton) findViewById(R.id.sign_in_button);

        setupToolbar();
        refreshSignedAs();
        refreshNickname();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem userItem = menu.findItem(R.id.action_user_profile);
        if (userItem != null) {
            userItem.setVisible(false);
            userItem.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
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
        toolbar.setTitle(SharedPrefsButler.getUserNickname() == null ? "" : SharedPrefsButler.getUserNickname());
    }

    private void refreshSignedAs() {
        if (SkooziApplication.getUserAccount() == null) {
            userSignedAs.setText(R.string.user_sign_in_instruction);
            signinButton.setVisibility(View.VISIBLE);
            signinButton.setSize(SignInButton.SIZE_STANDARD);
            signinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AccountUtil.pickUserAccount(UserAccountActivity.this);
                }
            });
        } else {
            userSignedAs.setText(
                    getString(R.string.user_details_signed_in, SkooziApplication.getUserAccount().name));
        }
    }

    private void refreshNickname() {
        if (SharedPrefsButler.getUserNickname() == null) {
            nicknameEditContainer.setVisibility(View.VISIBLE);
            setupNicknameEdit(true);
            nickname.setVisibility(View.GONE);
            displayEnterNicknameMessage();
        } else {
            nicknameEditContainer.setVisibility(View.GONE);
            nicknameEditDone.setVisibility(View.GONE);
            nickname.setVisibility(View.VISIBLE);
            nickname.setText(getString(R.string.user_saved_display_name, SharedPrefsButler.getUserNickname()));
        }
    }

    private void setupNicknameEdit(boolean enableEdit) {
        if (enableEdit) {
            nicknameEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s.toString())) {
                        nicknameEditDone.setVisibility(View.INVISIBLE);
                    } else
                        nicknameEditDone.setVisibility(View.VISIBLE);
                }
            });
            nicknameEditDone.setVisibility(View.INVISIBLE);
            setupNicknameEditButton();
        }
    }

    private void setupNicknameEditButton() {
        nicknameEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserAccountActivity.this);

                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPrefsButler.putFutureUserNickname(nicknameEdit.getText().toString());
                        refreshNickname();
                        snackbar.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                builder.setMessage(R.string.user_dialog_message)
                        .setTitle(R.string.user_dialog_title);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void displayEnterNicknameMessage(){
        snackbar =  Snackbar.make(coordinatorLayout, R.string.snackbar_select_display_name_message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void displaySignInMessage(){
        if (snackbar.isShownOrQueued()) return;

        snackbar =  Snackbar.make(coordinatorLayout, getString(R.string.user_details_signed_in, SkooziApplication.getUserAccount().name),
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
