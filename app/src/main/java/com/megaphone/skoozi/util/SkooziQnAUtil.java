package com.megaphone.skoozi.util;

import android.content.Context;
import android.location.Location;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import com.google.android.gms.maps.model.LatLng;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.api.SkooziQnARequestService;
import com.megaphone.skoozi.model.Question;

public class SkooziQnAUtil {
    private static final LatLng DEFAULT_LOCATION = new LatLng(43.6532,-79.3832);
    public static final int DEFAULT_RADIUS_METRES = 10000;

    public static final String BROADCAST_QUESTIONS_LIST_RESULT = "skoozi.broadcast.QUESTIONS_LIST_RESULT";
    public static final String BROADCAST_POST_QUESTION_RESULT = "skoozi.broadcast.POST_QUESTION_RESULT";
    public static final String BROADCAST_THREAD_ANSWERS_RESULT = "skoozi.broadcast.THREAD_ANSWERS_RESULT";
    public static final String BROADCAST_POST_ANSWER_RESULT = "skoozi.broadcast.POST_ANSWER_RESULT";

    public static final String EXTRA_RESULT_SUCCESS = "skoozi.extra.result.SUCCESS";

    public static final String EXTRA_QUESTION_KEY  = "skoozi.extra.QUESTION_KEY";
    public static final String EXTRA_ANSWER_KEY = "com.megaphone.skoozi.extra.ANSWER_KEY";
    public static final String EXTRA_THREAD_ANSWERS = "com.megaphone.skoozi.extra.THREAD_ANSWERS";
    public static final String EXTRA_QUESTIONS_LIST = "skoozi.extra.QUESTIONS_LIST";

    public static void displayNoQuestionsMessage(CoordinatorLayout layoutView) {
        Snackbar.make(layoutView, R.string.no_questions_message, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_action_undo, clickListener)
                .show();
    }

    /**
     * Response from the Api is sent via a Local Broadcast. Prior to issuing this call, ensure that
     * a Local Broadcast Receiver is setup with the following IntentFilter -
     * SkooziQnAUtil.BROADCAST_QUESTIONS_LIST_RESULT
     */
    public static void quesListRequest(Context context,
                                       AccountUtil.GoogleAuthTokenExceptionListener tokenListener,
                                       Location searchOrigin, int searchRadiusKm) {
        SkooziQnARequestService.startActionGetQuestionsList(context, tokenListener
                , new LatLng(searchOrigin.getLatitude(), searchOrigin.getLongitude())
                , (long) searchRadiusKm);
    }

    /**
     * Response from the Api is sent via a Local Broadcast. Prior to issuing this call, ensure that
     * a Local Broadcast Receiver is setup with the following IntentFilter -
     * SkooziQnAUtil.BROADCAST_POST_QUESTION_RESULT
     */
    public static void postQuestionRequest(Context context,
                                       AccountUtil.GoogleAuthTokenExceptionListener tokenListener,
                                       Question question) {
        SkooziQnARequestService.startActionInsertNewQuestion(context, tokenListener, question);
    }
}
