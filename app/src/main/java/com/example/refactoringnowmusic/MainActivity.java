package com.example.refactoringnowmusic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.*;
import android.support.v4.media.session.*;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean serviceOnFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("通知へのアクセス")
                    .setMessage("このアプリを利用するには通知へのアクセスを許可する必要があります。\n設定画面からアプリを選択し有効化してください。")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else{
            if(!serviceOnFlag){
                serviceOnFlag = true;
                startService(new Intent(MainActivity.this,MediaBrowserService.class));
                Log.d("MainActivity","started service MediaBrowserService");
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        //mediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}