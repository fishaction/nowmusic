package com.example.refactoringnowmusic;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import com.example.refactoringnowmusic.tasks.*;
import com.example.refactoringnowmusic.tasks.FindMediaAppsTask;

public class MediaBrowserService extends Service {


    private Snackbar mSnackbar;

    List<Intent> controllerServiceIntents;

    private final String TAG = "MEDIA_BROWSER_SERVICE";
    public static final String ACTION_IS_RUNNING = "com.example.MediaBrowserService_is_running";
    private LocalBroadcastManager localBroadcastManager;
    private android.content.BroadcastReceiver broadcastReceiver;


    //MediaBrowserServiceが起動しているかの確認
    public static Boolean isRunning(Context context){
        return LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(ACTION_IS_RUNNING)
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BroadcastReceiver extends android.content.BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {}
    }

    private final FindMediaAppsTask.AppListUpdatedCallback mBrowserAppsUpdated =
            new FindMediaAppsTask.AppListUpdatedCallback() {
                @Override
                public void onAppListUpdated(
                        @NonNull List<? extends MediaAppDetails> mediaAppDetails) {

                    if (mediaAppDetails.isEmpty()) {
                        return;
                    }
                }
            };
    private final MediaBrowserService.MediaSessionListener mMediaSessionListener =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? new MediaBrowserService.MediaSessionListener()
                    : null;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastReceiver = new MediaBrowserService.BroadcastReceiver();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IS_RUNNING);
        localBroadcastManager.registerReceiver(broadcastReceiver,filter);

        controllerServiceIntents = new ArrayList<>();
        mMediaSessionListener.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (mMediaSessionListener != null) {
            mMediaSessionListener.onStart(this);
        }
        // Update the list of media browser apps in onStart so if a new app is installed it will
        // appear on the list when the user comes back to it.
        new FindMediaBrowserAppsTask(this, mBrowserAppsUpdated).execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mMediaSessionListener != null) {
            mMediaSessionListener.onStop();
        }
        Log.d(TAG,"on Destroy");
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private final class MediaSessionListener {
        private final FindMediaAppsTask.AppListUpdatedCallback mSessionAppsUpdated =
                new FindMediaAppsTask.AppListUpdatedCallback() {
                    @Override
                    public void onAppListUpdated(@NonNull List<? extends MediaAppDetails> mediaAppDetails) {
                        for (Intent i : controllerServiceIntents){
                            stopService(i);
                        }
                        controllerServiceIntents.clear();
                        if (mediaAppDetails.isEmpty()) {
                            Log.d(TAG,"mediaAppDetails is Empty");
                            return;
                        }
                        for (MediaAppDetails m : mediaAppDetails){
                            Intent intent = MediaAppControllerService.buildIntent(MediaBrowserService.this,m);
                            controllerServiceIntents.add(intent);
                            Log.d(TAG,m.appName);
                            startService(intent);
                        }
                    }
                };

        private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsChangedListener =
                list -> mSessionAppsUpdated.onAppListUpdated(
                        MediaAppControllerUtils.getMediaAppsFromControllers(
                                list, getPackageManager(), getResources()));
        private MediaSessionManager mMediaSessionManager;

        void onCreate(){
            mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }


        void onStart(Context context) {
            if (!NotificationListener.isEnabled(context)) {
                Log.d(TAG,"Notification Listener is not Enabled");
                return;
            }
            if (mMediaSessionManager == null) {
                return;
            }
            ComponentName listenerComponent =
                    new ComponentName(context, NotificationListener.class);
            mMediaSessionManager.addOnActiveSessionsChangedListener(
                    mSessionsChangedListener, listenerComponent);
            new FindMediaSessionAppsTask(mMediaSessionManager, listenerComponent,
                    getPackageManager(), getResources(), mSessionAppsUpdated).execute();
        }

        void onStop() {
            if (mMediaSessionManager == null) {
                return;
            }
            mMediaSessionManager.removeOnActiveSessionsChangedListener(mSessionsChangedListener);
        }
    }
}