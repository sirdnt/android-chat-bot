package com.dnt.chatbot.api;

import android.os.AsyncTask;
import android.util.Log;

import com.dnt.chatbot.BuildConfig;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * Created by sir.dnt@gmail.com on 3/3/19.
 * This AsyncTask is responsible for fetch data from Dialogflow
 */
public class IntentRequestTask extends AsyncTask<AIRequest, Void, AIResponse> {

    private static final String TAG = "IntentRequestTask";

    public interface IntentRequestTaskListener {
        void onRequestResult(AIResponse result);
    }

    private IntentRequestTaskListener mListener;
    private AIDataService mDataService;

    public static AIRequest welcomeRequest() {
        AIEvent event = new AIEvent("Welcome");
        AIRequest welcomeRequest = new AIRequest();
        welcomeRequest.setEvent(event);
        return welcomeRequest;
    }

    public static AIRequest queryRequest(String query) {
        AIRequest request = new AIRequest();
        request.setQuery(query);
        return request;
    }

    public IntentRequestTask(IntentRequestTaskListener listener) {
        this.mListener = listener;
        final AIConfiguration config = new AIConfiguration(BuildConfig.DIALOGFLOW_AGENT_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        this.mDataService = new AIDataService(config);
    }

    @Override
    protected AIResponse doInBackground(AIRequest... requests) {
        if (BuildConfig.DEBUG && requests.length <= 0)
            throw new RuntimeException("please specify request to do the job");
        try {
            final AIResponse response = mDataService.request(requests[0]);
            return response;
        } catch (AIServiceException e) {
            Log.e(TAG, "doInBackground: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(AIResponse aiResponse) {
        super.onPostExecute(aiResponse);
        if (mListener != null) {
            mListener.onRequestResult(aiResponse);
        }
    }
}
