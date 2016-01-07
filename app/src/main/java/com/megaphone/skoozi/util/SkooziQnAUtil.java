package com.megaphone.skoozi.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;
import com.megaphone.skoozi.api.SkooziQnARequestService;

/**
 * Created by ahmadulhassan on 2015-07-01.
 */
public class SkooziQnAUtil {
    public static final String BROADCAST_QUESTIONS_LIST_RESULT = "com.megaphone.skoozi.broadcast.QUESTIONS_LIST_RESULT";

    public static void displayNoQuestionsMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_questions_message, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }

    /**
     * Response from the Api is sent via a Local Broadcast. Prior to issuing this call, ensure that
     * a Local Broadcast Receiver is setup with the following IntentFilter -
     * SkooziQnAUtil.BROADCAST_QUESTIONS_LIST_RESULT
     * @param searchRadiusKm
     */
    public static void quesListRequest(Context context,
                                       AccountUtil.GoogleAuthTokenExceptionListener tokenListener,
                                       Location searchOrigin, int searchRadiusKm) {
        SkooziQnARequestService.startActionGetQuestionsList(context, tokenListener
                , new LatLng(searchOrigin.getLatitude(), searchOrigin.getLongitude())
                , (long) searchRadiusKm);
    }
}
