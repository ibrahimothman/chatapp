package com.ibra.chatappdemo.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;


public class NotificationReminder  {

    private static final String REMINDER_JOB_TAG = "MY_NOTIFICATION_JOB";
    private static boolean sInitialized;

    synchronized public static void scheduleChargingReminder(@NonNull final Context context) {


        // If the job has already been initialized, return
        if (sInitialized) return;


        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job job = dispatcher.newJobBuilder()
                .setService(jobDispatcher.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        12*60*60,
                        12*60*60))

                .setReplaceCurrent(true)
                .build();


        dispatcher.schedule(job);


        // Set sInitialized to true to mark that we're done setting up the job
        /* The job has been initialized */
        sInitialized = true;
    }
}
