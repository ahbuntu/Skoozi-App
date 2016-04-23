package com.megaphone.skoozi;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
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
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.base.BaseActivity;
import com.megaphone.skoozi.user.UserMapAreaDialog;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.ConnectionUtil;
import com.megaphone.skoozi.util.MapDecorator;
import com.megaphone.skoozi.util.PermissionUtil;
import com.megaphone.skoozi.util.SharedPrefsButler;

public class UserAccountActivity extends BaseActivity {
    private static final String TAG = UserAccountActivity.class.getSimpleName();

    private Location selfLocation;
    private TextInputLayout nicknameEditContainer;
    private ImageButton nicknameEditDone;
    private TextView nickname;
    private TextView userSignedAs;
    private SignInButton signinButton;
    private EditText nicknameEdit;
    private ImageView homeAreaAction;
    private ImageView workAreaAction;
    private MapFragment homeAreaMap;
    private MapFragment workAreaMap;
    private GoogleMap homeMap;
    private GoogleMap workMap;
    private RelativeLayout homeAreaMapContainer;
    private RelativeLayout workAreaMapContainer;
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
        homeAreaAction = (ImageView) findViewById(R.id.home_area_action);
        workAreaAction = (ImageView) findViewById(R.id.work_area_action);
        homeAreaMap = (MapFragment) getFragmentManager().findFragmentById(R.id.user_area_home_map);
        workAreaMap = (MapFragment) getFragmentManager().findFragmentById(R.id.user_area_work_map);
        homeAreaMapContainer = (RelativeLayout) findViewById(R.id.home_area_map_container);
        workAreaMapContainer = (RelativeLayout  ) findViewById(R.id.work_area_map_container);

        setupToolbar();
        refreshSignedAs();
        refreshNickname();
        setupHomeAndWorkAreas();

//        // The View with the BottomSheetBehavior
//        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
//        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
//        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(View bottomSheet, int newState) {
//                // React to state change
//                Log.e("onStateChanged", "onStateChanged:" + newState);
////                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
////                    fab.setVisibility(View.GONE);
////                } else {
////                    fab.setVisibility(View.VISIBLE);
////                }
//            }
//
//            @Override
//            public void onSlide(View bottomSheet, float slideOffset) {
//                // React to dragging events
//                Log.e("onSlide", "onSlide");
//            }
//        });
//
//        behavior.setPeekHeight(100);
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

        if (homeAreaMap != null) homeAreaMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                homeMap = googleMap;
                if (SharedPrefsButler.getHomeCoords() != null) {
                    updateMap(homeMap, SharedPrefsButler.getHomeCoords(), 5);
                }
            }
        });

        if (workAreaMap != null) workAreaMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                workMap = googleMap;
                if (SharedPrefsButler.getWorkCoords() != null) {
                    updateMap(workMap, SharedPrefsButler.getWorkCoords(), 5);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
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
        selfLocation = PermissionUtil.tryGetLatestLocation(UserAccountActivity.this, getGoogleApiClient());
        if (selfLocation != null) {
            LatLng mapCoords = new LatLng(selfLocation.getLatitude(), selfLocation.getLongitude());

            if (homeMap != null)  updateMap(homeMap, mapCoords, 5);

            if (workMap != null) updateMap(workMap, mapCoords, 5);
        }
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

    private void setupHomeAndWorkAreas() {
        boolean hasHomeArea = SharedPrefsButler.getHomeCoords() != null;
        homeAreaMapContainer.setVisibility(hasHomeArea ? View.VISIBLE : View.GONE);
        setupAreaConfirmation(hasHomeArea, homeAreaMapContainer.getVisibility() == View.VISIBLE, homeAreaAction);
        findViewById(R.id.home_area).setOnClickListener(getHomeAreaClickListener(hasHomeArea));

        boolean hasWorkArea = SharedPrefsButler.getWorkCoords() != null;
        workAreaMapContainer.setVisibility(hasWorkArea ? View.VISIBLE : View.GONE);
        setupAreaConfirmation(hasWorkArea, workAreaMapContainer.getVisibility() == View.VISIBLE, workAreaAction);
        findViewById(R.id.work_area).setOnClickListener(getWorkAreaClickListener(hasWorkArea));
    }

    private View.OnClickListener getHomeAreaClickListener(final boolean hasHomeArea) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle the visibility of the container
                if (homeAreaMapContainer.getVisibility() == View.VISIBLE) {
                    homeAreaMapContainer.setVisibility(View.GONE);
                } else {
                    homeAreaMapContainer.setVisibility(View.VISIBLE);
                }

                setupAreaConfirmation(hasHomeArea, homeAreaMapContainer.getVisibility() == View.VISIBLE,
                        homeAreaAction);
            }
        };
    }

    private View.OnClickListener getWorkAreaClickListener(final boolean hasWorkArea) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle the visibility of the container
                if (workAreaMapContainer.getVisibility() == View.VISIBLE) {
                    workAreaMapContainer.setVisibility(View.GONE);
                } else {
                    workAreaMapContainer.setVisibility(View.VISIBLE);
                }
                setupAreaConfirmation(hasWorkArea, workAreaMapContainer.getVisibility() == View.VISIBLE,
                        workAreaAction);
            }
        };
    }

    private void setupAreaConfirmation(final boolean isAreaSaved, final boolean isMapVisible,
                                       final ImageView areaConfirm) {
        if (!isAreaSaved && isMapVisible) {
            areaConfirm.setVisibility(View.VISIBLE);
            areaConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.home_area_action) {
                        SharedPrefsButler.putFutureHomeCoords(new LatLng
                                (selfLocation.getLatitude(), selfLocation.getLongitude()));
                    } else if (v.getId() == R.id.work_area_action) {
                        SharedPrefsButler.putFutureHomeCoords(new LatLng
                                (selfLocation.getLatitude(), selfLocation.getLongitude()));
                    }
                }
            });
        } else {
            // TODO: 2016-04-23 should really put an option to edit the address
            areaConfirm.setVisibility(View.GONE);
            areaConfirm.setClickable(false);
        }
    }

    private void updateMap(GoogleMap map, LatLng origin, int radiusKm) {
        map.clear(); // important to ensure that everything is cleared
        MapDecorator.drawLocationMarker(map, origin);
        MapDecorator.drawNotificationArea(this, map, origin, radiusKm);

        map.moveCamera(CameraUpdateFactory.zoomTo(12f));
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void displayEnterNicknameMessage() {
        snackbar = Snackbar.make(coordinatorLayout, R.string.snackbar_select_display_name_message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void displaySignInMessage() {
        if (snackbar.isShownOrQueued()) return;

        snackbar = Snackbar.make(coordinatorLayout, getString(R.string.user_details_signed_in, SkooziApplication.getUserAccount().name),
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // TODO: 2016-04-23 Implement the home and work areas as bottom sheet dialogs
    private void displayMapAreaDialog() {
        // http://stackoverflow.com/questions/18206615/how-to-use-google-map-v2-inside-fragment
        BottomSheetDialogFragment bottomSheetDialogFragment = new UserMapAreaDialog();
//        FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.coordinator_layout, bottomSheetDialogFragment, "test");
//        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        bottomSheetDialogFragment.show(getSupportFragmentManager(), "test");
    }
}
