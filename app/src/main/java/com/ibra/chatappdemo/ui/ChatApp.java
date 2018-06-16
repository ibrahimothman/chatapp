package com.ibra.chatappdemo.ui;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatApp extends Application  {

    FirebaseAuth mAuth;
    DatabaseReference userDatabase;
    private String currentId;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("fromApplication", "inside");

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentId = mAuth.getCurrentUser().getUid();
            final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentId);
            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        userDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        userDatabase.child("online").setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}

