package com.megaphone.skoozi;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.appspot.skoozi_959.skooziqna.Skooziqna;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsAnswerMessage;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsAnswerMessageCollection;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessage;
import com.appspot.skoozi_959.skooziqna.model.CoreModelsQuestionMessageCollection;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SkooziQnARequestService extends IntentService {
    private static final String TAG = "SkooziQnARequestService";
    private static final String ACTION_GET_THREAD_ANSWERS = "com.megaphone.skoozi.action.GET_THREAD_ANSWERS";
    private static final String ACTION_GET_QUESTIONS_LIST = "com.megaphone.skoozi.action.GET_QUESTIONS_LIST";
    private static final String EXTRA_QUESTION_KEY = "com.megaphone.skoozi.extra.QUESTION_KEY";

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
    public static void startActionGetQuestionsList(Context context) {
        Intent intent = new Intent(context, SkooziQnARequestService.class);
        intent.setAction(ACTION_GET_QUESTIONS_LIST);
        context.startService(intent);
    }

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
                handleActionGetQuestionsList();
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
        try {
            CoreModelsAnswerMessageCollection threadRepsonse =  skooziqnaService.question().listAnswers().setId(question_key).execute();
            if (threadRepsonse.size() > 2) {
                //kind & etag are always returned. LIst<AnswerMessage> is only returned if there are answers for the question
                for (CoreModelsAnswerMessage answer : threadRepsonse.getAnswers()) {
                    //TODO: create an Answer object here
                }
            }
        } catch (IOException e) {
            // TODO: Check for network connectivity before starting the AsyncTask.
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Handle action Get Questions List in the background thread
     */
    private void handleActionGetQuestionsList() {
        initializeApiConnection();
        try {
            CoreModelsQuestionMessageCollection questionsListResponse =  skooziqnaService.questions().list().execute();
            List<CoreModelsQuestionMessage> questionMessages =  questionsListResponse.getQuestions();
            ArrayList<Question> questionList = new ArrayList<>(questionMessages.size());
            for (CoreModelsQuestionMessage questionMessage: questionMessages) {
                questionList.add(new Question(
                        questionMessage.getEmail(),
                        questionMessage.getContent(),
                        "dummykey",
                        questionMessage.getTimestampUTCsec().toString(),
                        questionMessage.getLocationLat(),
                        questionMessage.getLocationLon()));
            }

            Intent localIntent = new Intent(MainActivity.BROADCAST_QUESTIONS_LIST_RESULT)
                    .putParcelableArrayListExtra(MainActivity.EXTRAS_QUESTIONS_LIST, questionList);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        } catch (IOException e) {
            // TODO: Check for network connectivity before starting the AsyncTask.
            Log.e(TAG, e.getMessage());
        }
    }

}
