package com.ibra.chatappdemo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.JobService;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.ui.MainActivity;

public class jobDispatcher extends JobService {


    private static final int WATER_REMINDER_NOTIFICATION_ID = 11;
    private static final int WATER_REMINDER_PENDING_INTENT_ID = 22;





    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getApplicationContext().getString(R.string.charging_reminder_notification_title))
                .setContentText(getApplicationContext().getString(R.string.charging_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        getApplicationContext().getString(R.string.charging_reminder_notification_body)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(getApplicationContext()))
                .setSound(alarmSound)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(WATER_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }


    private PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                WATER_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
