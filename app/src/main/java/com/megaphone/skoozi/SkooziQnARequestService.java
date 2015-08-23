package com.megaphone.skoozi;

import android.accounts.Account;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.appspot.skoozi_959.skooziqna.Skooziqna;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsAnswerMessage;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsAnswerMessageCollection;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsPostResponse;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessage;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessageCollection;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.megaphone.skoozi.util.AccountUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class SkooziQnARequestService extends IntentService {

    private static final String TAG = "SkooziQnARequestService";
    private static final String ACTION_GET_THREAD_ANSWERS = "com.megaphone.skoozi.action.GET_THREAD_ANSWERS";
    private static final String ACTION_GET_QUESTIONS_LIST = "com.megaphone.skoozi.action.GET_QUESTIONS_LIST";
    private static final String ACTION_INSERT_QUESTION_ANSWER = "com.megaphone.skoozi.action.INSERT_QUESTION_ANSWER";
    private static final String ACTION_INSERT_NEW_QUESTION = "com.megaphone.skoozi.action.INSERT_NEW_QUESTION";
    private static final String EXTRA_QUESTION_KEY = "com.megaphone.skoozi.extra.QUESTION_KEY";
    private static final String EXTRA_QUESTION_PARCEL = "com.megaphone.skoozi.extra.QUESTION_PARCEL";
    private static final String EXTRA_ANSWER_PARCEL = "com.megaphone.skoozi.extra.ANSWER_PARCEL";
    private static final String EXTRA_LATITUDE = "com.megaphone.skoozi.extra.LATITUDE";
    private static final String EXTRA_LONGITUDE = "com.megaphone.skoozi.extra.LONGITUDE";
    private static final String EXTRA_RADIUS = "com.megaphone.skoozi.extra.RADIUS";

    //http://stackoverflow.com/questions/10400428/can-i-use-androids-accountmanager-for-getting-oauth-access-token-for-appengine
    private final static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private final static String USERINFO_PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String SCOPE = "oauth2:" + USERINFO_EMAIL_SCOPE + " " + USERINFO_PROFILE_SCOPE;

    private static Context intentContext;
    private Skooziqna skooziqnaService;
    private static AccountUtil.GoogleAuthTokenExceptionListener authExceptionlistener;

    /**
     * Starts this service to perform action Get Thread Answers with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionGetThreadAnswers(Context context, AccountUtil.GoogleAuthTokenExceptionListener listener,
                                                   String question_key) {
        intentContext = context;
        authExceptionlistener = listener;
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_GET_THREAD_ANSWERS);
        intent.putExtra(EXTRA_QUESTION_KEY, question_key);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Get Questions List. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionGetQuestionsList(Context context,  AccountUtil.GoogleAuthTokenExceptionListener listener,
                                                   LatLng currentLocation, double radius_km) {
        intentContext = context;
        authExceptionlistener = listener;
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_GET_QUESTIONS_LIST);
        intent.putExtra(EXTRA_LATITUDE, currentLocation == null ? 0 : currentLocation.latitude);
        intent.putExtra(EXTRA_LONGITUDE, currentLocation == null ? 0 : currentLocation.longitude);
        intent.putExtra(EXTRA_RADIUS, radius_km);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Insert Answer for Question. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionInsertAnswer(Context context,  AccountUtil.GoogleAuthTokenExceptionListener listener,
                                               String question_key, Answer userAnswer) {
        intentContext = context;
        authExceptionlistener = listener;
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_INSERT_QUESTION_ANSWER);
        intent.putExtra(EXTRA_QUESTION_KEY, question_key);
        intent.putExtra(EXTRA_ANSWER_PARCEL, userAnswer);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Insert New Question. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionInsertNewQuestion(Context context,  AccountUtil.GoogleAuthTokenExceptionListener listener,
                                               Question userQuestion) {
        intentContext = context;
        authExceptionlistener = listener;
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_INSERT_NEW_QUESTION);
        intent.putExtra(EXTRA_QUESTION_PARCEL, userQuestion);
        context.startService(intent);
    }


    // TODO: Check for network connectivity before starting the Service.
    public SkooziQnARequestService() {
        super("SkooziQnARequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || SkooziApplication.getUserAccount() == null) {
            Log.d(TAG, "intent or user account was null");
            return;
        }
        try {
            SkooziApplication.accessToken = fetchToken();
            if (SkooziApplication.accessToken  == null) return;

            final String action = intent.getAction();
            if (ACTION_GET_THREAD_ANSWERS.equals(action)) {
                final String key = intent.getStringExtra(EXTRA_QUESTION_KEY);
                handleActionGetThreadAnswers(key);
            } else if (ACTION_GET_QUESTIONS_LIST.equals(action)) {
                final double lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
                final double lon = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);
                final double radius = intent.getDoubleExtra(EXTRA_RADIUS, MainActivity.DEFAULT_RADIUS_METRES / 1000); //API expects in km
                handleActionGetQuestionsList(lat, lon, radius);
            } else if (ACTION_INSERT_QUESTION_ANSWER.equals(action)) {
                final String key = intent.getStringExtra(EXTRA_QUESTION_KEY);
                final Answer mAnswer = intent.getParcelableExtra(EXTRA_ANSWER_PARCEL);
                handleActionInsertAnswer(key, mAnswer);
            } else if(ACTION_INSERT_NEW_QUESTION.equals(action)) {
                final Question mQuestion = intent.getParcelableExtra(EXTRA_QUESTION_PARCEL);
                handleActionInsertQuestion(mQuestion);
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * Create the SkooziQnAAPi service instance
     */
    private void initializeApiConnection() {
        if (skooziqnaService == null) { // do this once
            Skooziqna.Builder builder = new Skooziqna.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(getString(R.string.app_api_url));
            skooziqnaService = builder.build();
        }
    }

    /**
     * Handle action Get Thread Answers in the background thread with the provided params.
     */
    private void handleActionGetThreadAnswers(String question_key) {
        initializeApiConnection();
        ArrayList<Answer> threadAnswers = null;
        try {
            CoreModelsAnswerMessageCollection threadRepsonse =  skooziqnaService.question().listAnswers()
                    .setId(question_key)
                    .setOauthToken(SkooziApplication.accessToken)
                    .execute();
            List<CoreModelsAnswerMessage> threadAnswerMessages = threadRepsonse.getAnswers();
            if (threadAnswerMessages != null) {
                threadAnswers = new ArrayList<>(threadAnswerMessages.size());
                for (CoreModelsAnswerMessage answerMessage : threadAnswerMessages) {
                    threadAnswers.add(new Answer(
                            answerMessage.getIdUrlsafe(),
                            question_key,
                            answerMessage.getEmail(),
                            answerMessage.getContent(),
                            answerMessage.getTimestampUnix(),
                            answerMessage.getLocationLat(),
                            answerMessage.getLocationLon()));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Intent localIntent = new Intent(ThreadActivity.BROADCAST_THREAD_ANSWERS_RESULT)
                .putParcelableArrayListExtra(ThreadActivity.EXTRAS_THREAD_ANSWERS, threadAnswers);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Get Questions List in the background thread
     */
    private void handleActionGetQuestionsList(double lat, double lon, double radius) {
        initializeApiConnection();
        ArrayList<Question> questionList = null;
        try {
            CoreModelsQuestionMessageCollection questionsListResponse;
            if (lat == 0 || lon == 0) {
                //need to get ALL since no location information was provided
                questionsListResponse =  skooziqnaService.questions().list()
                        .execute();
            } else {
                questionsListResponse =  skooziqnaService.questions().list()
                        .setLat(lat).setLon(lon).setRadiusKm(radius)
                        .execute();
            }

            List<CoreModelsQuestionMessage> questionMessages =  questionsListResponse.getQuestions();
            questionList = new ArrayList<>(questionMessages.size());
            for (CoreModelsQuestionMessage questionMessage: questionMessages) {
                questionList.add(new Question(
                        questionMessage.getEmail(),
                        questionMessage.getContent(),
                        questionMessage.getIdUrlsafe(),
                        questionMessage.getTimestampUnix(),
                        questionMessage.getLocationLat(),
                        questionMessage.getLocationLon()));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // if no questions, return null; service should not handle this
        Intent localIntent = new Intent(MainActivity.BROADCAST_QUESTIONS_LIST_RESULT)
                .putParcelableArrayListExtra(MainActivity.EXTRAS_QUESTIONS_LIST, questionList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Insert New Answer in the background thread
     */
    private void handleActionInsertAnswer(String questionKey, Answer userAnswer) {
        initializeApiConnection();
        String postKey = null;
        try {
            CoreModelsAnswerMessage answerMsg = new CoreModelsAnswerMessage();

            answerMsg.setQuestionUrlsafe(questionKey);
            answerMsg.setEmail(userAnswer.author);
            answerMsg.setContent(userAnswer.content);
            answerMsg.setLocationLat(userAnswer.locationLat);
            answerMsg.setLocationLon(userAnswer.locationLon);
            answerMsg.setTimestampUnix(userAnswer.timestamp);

            CoreModelsPostResponse insertResponse = skooziqnaService.answer()
                    .insert(answerMsg)
                    .setOauthToken(SkooziApplication.accessToken)
                    .execute();
            postKey = insertResponse.getPostKey();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Intent localIntent = new Intent(ThreadActivity.BROADCAST_POST_ANSWER_RESULT)
                .putExtra(ThreadActivity.EXTRAS_ANSWER_KEY, postKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Insert New Answer in the background thread
     */
    private void handleActionInsertQuestion(Question userQuestion) {
        initializeApiConnection();
        String postKey = null;
        try {
            CoreModelsQuestionMessage questionMsg = new CoreModelsQuestionMessage();

            questionMsg.setEmail(userQuestion.author);
            questionMsg.setContent(userQuestion.content);
            questionMsg.setLocationLat(userQuestion.locationLat);
            questionMsg.setLocationLon(userQuestion.locationLon);
            questionMsg.setTimestampUnix(userQuestion.timestamp);

            CoreModelsPostResponse insertResponse = skooziqnaService.question()
                    .insert(questionMsg)
                    .setOauthToken(SkooziApplication.accessToken)
                    .execute();
            postKey = insertResponse.getPostKey();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Intent localIntent = new Intent(PostQuestionActivity.BROADCAST_POST_QUESTION_RESULT)
                .putExtra(PostQuestionActivity.EXTRA_QUESTION_KEY, postKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    private static String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(intentContext, SkooziApplication.getUserAccount(), SCOPE);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
//            MainActivity mainActivityRef = (MainActivity) intentContext;
//            mainActivityRef.handleGoogleAuthTokenException(userRecoverableException);
            authExceptionlistener.handleGoogleAuthException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.e(TAG, fatalException.getMessage());
        }
        return null;
    }

}
