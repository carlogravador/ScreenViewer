package com.example.screenviewer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_ID = "screeCastServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Cast Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notifManager = getSystemService(NotificationManager.class);
            notifManager.createNotificationChannel(serviceChannel);
        }
    }
}
