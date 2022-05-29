package com.example.refactoringnowmusic;

import static androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_SUGGESTED;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


public class MediaAppControllerService extends Service {

    private MediaAppDetails mMediaAppDetails;
    private MediaControllerCompat mController;
    private MediaBrowserCompat mBrowser;
    private MediaBrowserCompat mBrowserExtraSuggested;

    final String TAG = "MediaAppControllerService";

    private static final String APP_DETAILS_EXTRA =
            "com.example.android.mediacontroller.APP_DETAILS_EXTRA";

    public MediaAppControllerService() {
    }

    public static Intent buildIntent(final Context context,
                                     final MediaAppDetails appDetails){
        final Intent intent = new Intent(context, MediaAppControllerService.class);
        intent.putExtra(APP_DETAILS_EXTRA, appDetails);
        return intent;
    }

    private MediaAppDetails handleIntent(Intent intent){
        if(intent == null){
            return null;
        }
        final Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey(APP_DETAILS_EXTRA)){
                return extras.getParcelable(APP_DETAILS_EXTRA);
            }
        }
        //メディアアプリの詳細が得られない場合
        if(mMediaAppDetails == null){
            Log.d(TAG,"Couldn't update MediaAppDetails object");
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void setupMediaController() {
        MediaSessionCompat.Token token = mMediaAppDetails.sessionToken;
        if (token == null) {
            token = mBrowser.getSessionToken();
        }
        mController = new MediaControllerCompat(this, token);
        mController.registerCallback(mCallback);

        // Force update on connect.
        mCallback.onPlaybackStateChanged(mController.getPlaybackState());
        mCallback.onMetadataChanged(mController.getMetadata());

        // Ensure views are visible.


        Log.d(TAG, "MediaControllerCompat created");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaAppDetails = handleIntent(intent);
        if (mMediaAppDetails != null) {
            if (mMediaAppDetails.componentName != null) {
                mBrowser = new MediaBrowserCompat(this, mMediaAppDetails.componentName,
                        new MediaBrowserCompat.ConnectionCallback() {
                            @Override
                            public void onConnected() {
                                setupMediaController();

                            }

                            @Override
                            public void onConnectionSuspended() {
                                //TODO(rasekh): shut down browser.

                            }

                            @Override
                            public void onConnectionFailed() {
                            }

                        }, null);
                mBrowser.connect();

                Bundle bundle = new Bundle();
                bundle.putBoolean(EXTRA_SUGGESTED, true);

                mBrowserExtraSuggested = new MediaBrowserCompat(this, mMediaAppDetails.componentName,
                        new MediaBrowserCompat.ConnectionCallback() {
                            @Override
                            public void onConnected() {

                            }

                            @Override
                            public void onConnectionSuspended() {

                            }

                            @Override
                            public void onConnectionFailed() {

                            }

                        }, bundle);
                mBrowserExtraSuggested.connect();
            } else if (mMediaAppDetails.sessionToken != null) {
                setupMediaController();
            } else {
            }
        } else {
            // App details weren't passed in for some reason.
            Log.d(TAG,"Couldn't update MediaAppDetails object");

            // Go back to the launcher ASAP.
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return super.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) { onUpdate(); }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) { onUpdate(); }

        @Override
        public void onSessionDestroyed() {}

        private void onUpdate() {
            MediaMetadataCompat mediaMetadataCompat = mController.getMetadata();
            PlaybackStateCompat playbackState = mController.getPlaybackState();

            Log.d(TAG,mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    +":" +playbackState.getState() + ":" + mCallback.hashCode());
        }
    };
}