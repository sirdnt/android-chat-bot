package com.dnt.chatbot.controller;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dnt.chatbot.R;
import com.dnt.chatbot.api.IntentRequestTask;
import com.dnt.chatbot.databinding.ActivityMainBinding;
import com.dnt.chatbot.model.Message;
import com.dnt.chatbot.model.User;
import com.google.android.material.snackbar.Snackbar;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.TextToSpeechCallback;

import java.util.Date;
import java.util.List;

import ai.api.model.AIResponse;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    public static final String BOT_CHAT_ID = "0";
    public static final String USER_CHAT_ID = "1";
    private static final int PERMISSIONS_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();

    ActivityMainBinding mBinding;

    private MessagesListAdapter<Message> mMessagesAdapter;
    private ImageLoader mImageLoader;
    private User mBot;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initialize();
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
        Speech.init(this, getPackageName());
    }

    /**
     * method initialize private fields
     */
    private void initialize() {
        mBot = new User(BOT_CHAT_ID, "Bot", null, false);
        mUser = new User(USER_CHAT_ID, "User", null, false);
        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                //Load image go here with other 3rd party
            }
        };
        mMessagesAdapter = new MessagesListAdapter<>(USER_CHAT_ID, this.mImageLoader);
        mBinding.messagesView.setAdapter(mMessagesAdapter);
        ((LinearLayoutManager) mBinding.messagesView.getLayoutManager()).setStackFromEnd(true);

        int[] colors = {
                ContextCompat.getColor(this, android.R.color.holo_orange_light),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        mBinding.speechProgress.setColors(colors);

        int[] heights = {30, 40, 50, 40, 30};
        mBinding.speechProgress.setBarMaxHeightsInDp(heights);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.messagesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                queryWelcomeIntent();
            }
        }, 200);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    private void queryWelcomeIntent() {
        new IntentRequestTask(new IntentRequestTask.IntentRequestTaskListener() {
            @Override
            public void onRequestResult(AIResponse result) {
                String speech = result.getResult().getFulfillment().getSpeech();
                insertBotMessage(speech);
            }
        }).execute(IntentRequestTask.welcomeRequest());
    }

    private void queryIntent(String query) {
        new IntentRequestTask(new IntentRequestTask.IntentRequestTaskListener() {
            @Override
            public void onRequestResult(AIResponse result) {
                if (result != null) {
                    String speech = result.getResult().getFulfillment().getSpeech();
                    insertBotMessage(speech);
                }
            }
        }).execute(IntentRequestTask.queryRequest(query));
    }

    private void insertBotMessage(String message) {
        startTextToSpeech(message);
        mMessagesAdapter.addToStart(new Message("", mBot, message, new Date()), true);
    }

    private void insertUserMessage(String message) {
        mMessagesAdapter.addToStart(new Message("", mUser, message, new Date()), true);
    }

    private void startTextToSpeech(String text) {
        Speech.getInstance().say(text, new TextToSpeechCallback() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart");
            }

            @Override
            public void onCompleted() {
                startListening();
            }

            @Override
            public void onError() {
                Log.d(TAG, "onError");
            }
        });
    }

    private void startListening() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                onRecordAudioPermissionGranted();
            } else {
                // permission denied
                Snackbar.make(mBinding.constraintLayout, getString(R.string.permission_microphone_required), Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void onRecordAudioPermissionGranted() {
        mBinding.speechProgressContainer.setVisibility(View.VISIBLE);

        try {
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(mBinding.speechProgress, MainActivity.this);

        } catch (SpeechRecognitionNotAvailable exc) {
            showSpeechNotSupportedDialog();

        } catch (GoogleVoiceTypingDisabledException exc) {
            showEnableGoogleVoiceTyping();
        }
    }

    @Override
    public void onStartOfSpeech() {
        Log.d(TAG, "onStartOfSpeech");
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        Log.d(TAG, "onSpeechRmsChanged: ");
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        Log.d(TAG, "onSpeechPartialResults: ");
    }

    @Override
    public void onSpeechResult(String result) {
        mBinding.speechProgressContainer.setVisibility(View.GONE);
        Log.d(TAG, "onSpeechResult: " + result);
        insertUserMessage(result);
        queryIntent(result);
    }

    private void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SpeechUtil.redirectUserToGoogleAppOnPlayStore(MainActivity.this);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_google_voice_typing)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .show();
    }
}