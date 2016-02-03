package com.megaphone.skoozi.api;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.megaphone.skoozi.SkooziApplication;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.AccountUtil;
import com.megaphone.skoozi.util.SkooziQnAUtil;

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
    private static final String ACTION_GET_THREAD_ANSWERS = "skoozi.action.GET_THREAD_ANSWERS";
    private static final String ACTION_GET_QUESTIONS_LIST = "skoozi.action.GET_QUESTIONS_LIST";
    private static final String ACTION_INSERT_QUESTION_ANSWER = "skoozi.action.INSERT_QUESTION_ANSWER";
    private static final String ACTION_INSERT_NEW_QUESTION = "skoozi.action.INSERT_NEW_QUESTION";
    private static final String EXTRA_QUESTION_KEY = "skoozi.extra.QUESTION_KEY";
    private static final String EXTRA_QUESTION_PARCEL = "skoozi.extra.QUESTION_PARCEL";
    private static final String EXTRA_ANSWER_PARCEL = "skoozi.extra.ANSWER_PARCEL";
    private static final String EXTRA_LATITUDE = "skoozi.extra.LATITUDE";
    private static final String EXTRA_LONGITUDE = "skoozi.extra.LONGITUDE";
    private static final String EXTRA_RADIUS = "skoozi.extra.RADIUS";

    //http://stackoverflow.com/questions/10400428/can-i-use-androids-accountmanager-for-getting-oauth-access-token-for-appengine
    private final static String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private final static String USERINFO_PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String SCOPE = "oauth2:" + USERINFO_EMAIL_SCOPE + " " + USERINFO_PROFILE_SCOPE;

    private static Context intentContext;
    private Skooziqna skooziqnaService;
    private static AccountUtil.GoogleAuthTokenExceptionListener authExceptionListener;

    /**
     * Starts this service to perform action Get Thread Answers with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionGetThreadAnswers(Context context, AccountUtil.GoogleAuthTokenExceptionListener listener,
                                                   String question_key) {
        intentContext = context;
        authExceptionListener = listener;
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
        authExceptionListener = listener;
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
        authExceptionListener = listener;
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
        authExceptionListener = listener;
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_INSERT_NEW_QUESTION);
        intent.putExtra(EXTRA_QUESTION_PARCEL, userQuestion);
        context.startService(intent);
    }


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
                final double radius = intent.getDoubleExtra(EXTRA_RADIUS, SkooziQnAUtil.DEFAULT_RADIUS_METRES / 1000); //API expects in km
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
            GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(this,
                    ServerConfig.getCredentialAudience());

            if (credential.getSelectedAccountName() == null) {
                // Not signed in, show login window or request an account.
                credential.setSelectedAccountName(SkooziApplication.getUserAccount().name);
            }

            Skooziqna.Builder builder = new Skooziqna.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), credential)
                    .setRootUrl(ServerConfig.getSkooziUrl());
            skooziqnaService = builder.build();

        }
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString(PREF_ACCOUNT_NAME, accountName);
//        editor.commit();
//        credential.setSelectedAccountName(accountName);
//        this.accountName = accountName;
    }

    /**
     * Handle action Get Thread Answers in the background thread with the provided params.
     */
    private void handleActionGetThreadAnswers(String question_key) {
        Log.d(TAG, String.format("Trying to get all answers for question key %s", question_key));
        boolean success;
        Intent localIntent = new Intent(SkooziQnAUtil.BROADCAST_THREAD_ANSWERS_RESULT);
        initializeApiConnection();
        ArrayList<Answer> threadAnswers = null;
        try {
            CoreModelsAnswerMessageCollection threadRepsonse =  skooziqnaService.question().listAnswers()
                    .setId(question_key)
                    .execute();
            List<CoreModelsAnswerMessage> threadAnswerMessages = threadRepsonse.getAnswers();
            if (threadAnswerMessages != null) {
                threadAnswers = new ArrayList<>(threadAnswerMessages.size());
                for (CoreModelsAnswerMessage answerMessage : threadAnswerMessages) {
                    threadAnswers.add(new Answer(
                            answerMessage.getIdUrlsafe(),
                            question_key,
                            "dummy static value", // FIXME: 2016-01-02 figure out right values
//                            answerMessage.getEmail(),
                            answerMessage.getContent(),
                            answerMessage.getTimestampUnix(),
                            answerMessage.getLocationLat(),
                            answerMessage.getLocationLon()));
                }
            }
            success = true;
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "handleActionGetThreadAnswers: API error");
            e.printStackTrace();
        }

        localIntent.putExtra(SkooziQnAUtil.EXTRA_RESULT_SUCCESS, success);
        localIntent.putParcelableArrayListExtra(SkooziQnAUtil.EXTRA_THREAD_ANSWERS, threadAnswers);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Get Questions List in the background thread
     */
    private void handleActionGetQuestionsList(double lat, double lon, double radius) {
        Log.d(TAG, String.format("Trying to get questions for area with radius %f, centred at %f, %f.",
                radius, lat, lon));
        boolean success;
        Intent localIntent = new Intent(SkooziQnAUtil.BROADCAST_QUESTIONS_LIST_RESULT);
        initializeApiConnection();
        ArrayList<Question> questionList = null;
        try {
            CoreModelsQuestionMessageCollection questionsListResponse;
            questionsListResponse =  skooziqnaService.questions().list()
                    .setLat(lat).setLon(lon).setRadiusKm(radius)
                    .execute();

            List<CoreModelsQuestionMessage> questionMessages =  questionsListResponse.getQuestions();
            if (questionMessages != null) {
                questionList = new ArrayList<>(questionMessages.size());
                for (CoreModelsQuestionMessage questionMessage : questionMessages) {
                    questionList.add(new Question(
                            "dummy static value", // FIXME: 2016-01-02 figure out right values
//                            questionMessage.getEmail(),
                            questionMessage.getContent(),
                            questionMessage.getIdUrlsafe(),
                            questionMessage.getTimestampUnix(),
                            questionMessage.getLocationLat(),
                            questionMessage.getLocationLon()));
                }
            }
            success = true;
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "handleActionGetQuestionsList: API error");
            e.printStackTrace();
        }

        localIntent.putExtra(SkooziQnAUtil.EXTRA_RESULT_SUCCESS, success);
        localIntent.putParcelableArrayListExtra(SkooziQnAUtil.EXTRA_QUESTIONS_LIST, questionList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Insert New Answer in the background thread
     */
    private void handleActionInsertAnswer(String questionKey, Answer userAnswer) {
        Log.d(TAG, "Trying to post a new answer.");
        boolean success;
        Intent localIntent = new Intent(SkooziQnAUtil.BROADCAST_POST_ANSWER_RESULT);
        initializeApiConnection();
        String postKey = null;
        try {
            CoreModelsAnswerMessage answerMsg = new CoreModelsAnswerMessage();

            answerMsg.setQuestionUrlsafe(questionKey);
//            answerMsg.setEmail(userAnswer.author); // FIXME: 2016-01-02 figure out right values
            answerMsg.setContent(userAnswer.content);
            answerMsg.setLocationLat(userAnswer.locationLat);
            answerMsg.setLocationLon(userAnswer.locationLon);
            answerMsg.setTimestampUnix(userAnswer.timestamp);

            CoreModelsPostResponse insertResponse = skooziqnaService.answer()
                    .insert(answerMsg)
                    .execute();
            postKey = insertResponse.getPostKey();
            success = true;
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "handleActionInsertAnswer: API error");
            e.printStackTrace();
        }

        localIntent.putExtra(SkooziQnAUtil.EXTRA_RESULT_SUCCESS, success);
        localIntent.putExtra(SkooziQnAUtil.EXTRA_ANSWER_KEY, postKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Handle action Insert New Question in the background thread
     */
    private void handleActionInsertQuestion(Question userQuestion) {
        Log.d(TAG, "Trying to post a new question.");
        boolean success;
        Intent localIntent = new Intent(SkooziQnAUtil.BROADCAST_POST_QUESTION_RESULT);
        initializeApiConnection();
        String postKey = null;
        try {
            CoreModelsQuestionMessage questionMsg = new CoreModelsQuestionMessage();
            questionMsg.setContent(userQuestion.content);
            questionMsg.setLocationLat(userQuestion.locationLat);
            questionMsg.setLocationLon(userQuestion.locationLon);
            questionMsg.setTimestampUnix(userQuestion.timestamp);

            CoreModelsPostResponse insertResponse = skooziqnaService.question()
                    .insert(questionMsg)
                    .execute();

            Log.d(TAG, insertResponse.toString());
            postKey = insertResponse.getPostKey();
            success = true;
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "handleActionInsertQuestion: API error");
            e.printStackTrace();
        }

        localIntent.putExtra(SkooziQnAUtil.EXTRA_RESULT_SUCCESS, success);
        localIntent.putExtra(SkooziQnAUtil.EXTRA_QUESTION_KEY, postKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Gets an authentication token from Google and handles any GoogleAuthException that may occur.
     *
     */
    private static String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(intentContext, SkooziApplication.getUserAccount(), SCOPE);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            authExceptionListener.handleGoogleAuthException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.e(TAG, fatalException.getMessage());
        }
        return null;
    }

}
