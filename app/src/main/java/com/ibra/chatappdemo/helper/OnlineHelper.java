package com.ibra.chatappdemo.helper;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class OnlineHelper extends Application {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String uid;
    DatabaseReference userReference;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        if(currentUser != null){
            // cuurent user has already signing in before
            userReference = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.users_table)).child(uid);
            userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // if user close app
                            userReference.child("online").onDisconnect()
                                    .setValue(String.valueOf(System.currentTimeMillis()));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }


    }

    public static boolean isOnForeground(Context context) throws ExecutionException, InterruptedException {
        return new ForegroundCheckTask().execute(context).get();

    }

    public static class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }


    }


}
