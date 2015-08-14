package com.megaphone.skoozi;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

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
    private static final String EXTRA_QUESTION_KEY = "com.megaphone.skoozi.extra.QUESTION_KEY";
    private static final String EXTRA_ANSWER_PARCEL = "com.megaphone.skoozi.extra.ANSWER_PARCEL";
    private static final String EXTRA_LATITUDE = "com.megaphone.skoozi.extra.LATITUDE";
    private static final String EXTRA_LONGITUDE = "com.megaphone.skoozi.extra.LONGITUDE";
    private static final String EXTRA_RADIUS = "com.megaphone.skoozi.extra.RADIUS";

    private Skooziqna skooziqnaService;

    /**
     * Starts this service to perform action Get Thread Answers with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionGetThreadAnswers(Context context, String question_key) {
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_GET_THREAD_ANSWERS);
        intent.putExtra(EXTRA_QUESTION_KEY, question_key);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Get Questions List. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionGetQuestionsList(Context context, LatLng currentLocation, double radius_km) {
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
    public static void startActionInsertAnswer(Context context, String question_key, Answer userAnswer) {
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_INSERT_QUESTION_ANSWER);
        intent.putExtra(EXTRA_QUESTION_KEY, question_key);
        intent.putExtra(EXTRA_ANSWER_PARCEL, userAnswer);
        context.startService(intent);
    }


    // TODO: Check for network connectivity before starting the Service.
    public SkooziQnARequestService() {
        super("SkooziQnARequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
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
            }
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
            CoreModelsAnswerMessageCollection threadRepsonse =  skooziqnaService.question().listAnswers().setId(question_key).execute();
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
    private void handleActionInsertAnswer(String question_key, Answer userAnswer) {
        initializeApiConnection();
        String postKey = null;
        try {
            CoreModelsAnswerMessage answerMsg = new CoreModelsAnswerMessage();

            answerMsg.setQuestionUrlsafe(question_key);
            answerMsg.setEmail("response@response.com");
            answerMsg.setContent(userAnswer.content);
            answerMsg.setLocationLat(userAnswer.locationLat);
            answerMsg.setLocationLon(userAnswer.locationLon);
            answerMsg.setTimestampUnix(userAnswer.timestamp);

            CoreModelsPostResponse insertResponse = skooziqnaService.answer().insert(answerMsg).execute();
            postKey = insertResponse.getPostKey();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Intent localIntent = new Intent(ThreadActivity.BROADCAST_POST_ANSWER_RESULT)
                .putExtra(ThreadActivity.EXTRAS_ANSWER_KEY, postKey);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
