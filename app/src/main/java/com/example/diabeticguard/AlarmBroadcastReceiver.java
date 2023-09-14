package com.example.diabeticguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Locale;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicine = intent.getStringExtra("medicine");
        String CHANNEL_ID = medicine;// The id of the channel.
        CharSequence name = "Medicine Reminder";// The user-visible name of the channel.
        createNotificationChannel(context, medicine);
// Create a notification and set the notification channel.
        Notification.Builder notification = new Notification.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setContentTitle("It is time for you to take " + medicine.toUpperCase(Locale.ROOT))
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setSubText("Make sure to take your medicine!")
                .setChannelId(CHANNEL_ID);
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(medicine.hashCode(), notification.build());
    }
    private void createNotificationChannel(Context context, String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medicine Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setLightColor(Color.RED);
            channel.setDescription("Make sure to take your medicine!");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}