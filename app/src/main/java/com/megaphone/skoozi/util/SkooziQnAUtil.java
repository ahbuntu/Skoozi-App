package com.megaphone.skoozi.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import com.megaphone.skoozi.R;
import com.megaphone.skoozi.SkooziApplication;

/**
 * Created by ahmadulhassan on 2015-07-01.
 */
public class SkooziQnAUtil {

    public static void displayNoQuestionsMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_questions_message, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }
}
