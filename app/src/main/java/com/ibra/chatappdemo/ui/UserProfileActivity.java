package com.ibra.chatappdemo.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = UserProfileActivity.class.getCanonicalName();
    String profileUid;
    String currentUid;
    private DatabaseReference profileOwnerDatabase, friendRequestDatabase,friendDatabase,rootRef;
    private Toolbar mToolbar;
    private ImageView profileImage;
    private TextView status,mobile;
    private Button positiveBtn,negativebtn;
    private int state;
    private String reqType;
    private User user;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // setup ui views
        profileImage = (ImageView)findViewById(R.id.image_user_profile) ;
        status = (TextView)findViewById(R.id.status_text_user_profile);
        mobile = (TextView)findViewById(R.id.mobile_text_user_profile);
        positiveBtn = (Button)findViewById(R.id.positive_option);
        negativebtn = (Button)findViewById(R.id.negative_option);

        // get user id
        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra(AllusersActivity.USER_ID_EXTRA) != null){
            profileUid = intent.getStringExtra(AllusersActivity.USER_ID_EXTRA);
            Log.d(TAG,"uidis "+ profileUid);
        }else {
            Bundle bundle = getIntent().getExtras();
            if(bundle != null && bundle.getString("from_user_id") != null){
                Log.d(TAG,"fromuserprofile not null");
                profileUid = bundle.getString("from_user_id");
            }else Log.d(TAG,"fromuserprofile is null");

        }


        // get current user id
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            rootRef = FirebaseDatabase.getInstance().getReference();

            // get data of user whose id
            profileOwnerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(profileUid);
            profileOwnerDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    setupToolbar();
                    updateUi();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //setup friends table in DB
            friendDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.friends_table));

            // setup friend request table in database
            friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.friend_req_table));

            if (!currentUid.equals(profileUid)) {
                friendRequestDatabase
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "datachaged in FR");
                                if (dataSnapshot.hasChild(profileUid) && dataSnapshot.child(profileUid).hasChild(currentUid)) {
                                    reqType = dataSnapshot.child(profileUid).child(currentUid)
                                            .child(getString(R.string.request_type))
                                            .getValue().toString();
//                                Log.d(TAG, "typeis " + reqType);

                                    if (!reqType.equals(null)) {
                                        // current send to profile so cancel option should only shown
                                        if (reqType.equals("receive")) {
                                            state = 0;
                                            negativebtn.setEnabled(true);
                                            positiveBtn.setVisibility(View.GONE);
                                            negativebtn.setVisibility(View.VISIBLE);
                                            negativebtn.setText(R.string.candel_request_btn);


                                        } // current receive a request from profile so options are(accept and decline)
                                        else if (reqType.equals("send")) {
                                            state = 1;
                                            negativebtn.setEnabled(true);
                                            positiveBtn.setEnabled(true);
                                            positiveBtn.setVisibility(View.VISIBLE);
                                            negativebtn.setVisibility(View.VISIBLE);
                                            negativebtn.setText(R.string.decline_req_btn);
                                            positiveBtn.setText(R.string.accept_req_btn);

                                        }
                                    }// no friend request so options are(send)

                                } else {
                                    // check if you are friends

                                    friendDatabase.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(profileUid) && dataSnapshot.child(profileUid).hasChild(currentUid)) {
                                                // your are friends and options are(unfriend)
                                                negativebtn.setEnabled(true);
                                                positiveBtn.setVisibility(View.GONE);
                                                negativebtn.setVisibility(View.VISIBLE);
                                                negativebtn.setText(R.string.unfriend_btn);
                                                state = 2;
                                            } // no friend request so options are(send)
                                            else {
                                                state = 3;
                                                positiveBtn.setEnabled(true);
                                                negativebtn.setVisibility(View.GONE);
                                                positiveBtn.setVisibility(View.VISIBLE);
                                                positiveBtn.setText(R.string.send_request_btn);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
            // if user explore his profile
            else {
                negativebtn.setVisibility(View.GONE);
                positiveBtn.setEnabled(true);
                positiveBtn.setVisibility(View.VISIBLE);
                positiveBtn.setText("view profile");
                state = 4;
            }

            positiveBtn.setOnClickListener(UserProfileActivity.this);
            negativebtn.setOnClickListener(UserProfileActivity.this);
        }


        // setup friends table in database





    }

    @Override
    public void onClick(View view) {
        // current send to profile so current can cancel it
        if(state == 0) {
            Log.d(TAG, "cancelrequest done");
            if (view == negativebtn ) {
                negativebtn.setEnabled(false);
                cancelRequest();
            }

        }
        // current receive from profile so current can accept or decline
        else if (state == 1) {
            // accept
            if (view == positiveBtn) {
                positiveBtn.setEnabled(false);
                negativebtn.setEnabled(false);
                acceptFriendRequest();
            }// decline
            else  {
                positiveBtn.setEnabled(false);
                negativebtn.setEnabled(false);
                declineFriendRequest();
            }
        }
        // not request friend so current user can send one
        else if(state == 3){
            Log.d(TAG,"sendRequest done");
            if(view == positiveBtn){
                Log.d(TAG,"sendRequest done");
                positiveBtn.setEnabled(false);
                sendFriendRequest();
            }
        }
        // unfriend
        else if(  state == 2){
            if(view == negativebtn)
                Log.d(TAG,"unfriend done");
            negativebtn.setEnabled(false);
            unFriend();
        }
        else if(  state == 4){
            if(view == positiveBtn){
                // launch setting activity
                startActivity(new Intent(this,SettingActivity.class));
            }

        }

    }

    // cancel friend request
    private void cancelRequest() {
        Map<String,Object> cancelMap = new HashMap();
        cancelMap.put(getString(R.string.friend_req_table)+"/"+currentUid+"/"+profileUid+"/"+
                getString(R.string.request_type),null);
        cancelMap.put(getString(R.string.friend_req_table)+"/"+profileUid+"/"+currentUid+"/"+
                getString(R.string.request_type),null);


        rootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    negativebtn.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                }else{
                    if(state == 0) {
                        negativebtn.setVisibility(View.GONE);
                        positiveBtn.setVisibility(View.VISIBLE);
                        positiveBtn.setEnabled(true);
                        positiveBtn.setText(getString(R.string.send_request_btn));
                        state = 3;
                    }
                }
            }
        });

    }


    // send a friend request from current user to profile's owner
    private void sendFriendRequest() {

        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.notification_table))
                .child(profileUid).push();
        String notificationId = notificationRef.getKey();
        // first send request
        Map<String,Object> sendMap = new HashMap();
        sendMap.put(getString(R.string.friend_req_table)+"/"+currentUid+"/"+profileUid+"/"+
                    getString(R.string.request_type),"send");
        sendMap.put(getString(R.string.friend_req_table)+"/"+profileUid+"/"+currentUid+"/"+
                getString(R.string.request_type),"receive");

        sendMap.put(getString(R.string.notification_table)+"/"+profileUid+"/"+notificationId+"/"+"from",currentUid);
        sendMap.put(getString(R.string.notification_table)+"/"+profileUid+"/"+notificationId+"/"+"not_type","request");




        rootRef.updateChildren(sendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    positiveBtn.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                }else{
                    positiveBtn.setVisibility(View.GONE);
                    negativebtn.setVisibility(View.VISIBLE);
                    negativebtn.setEnabled(true);
                    negativebtn.setText(getString(R.string.candel_request_btn));
                    state = 0;
                }
            }
        });


    }





    private void declineFriendRequest() {
        Map<String,Object> declineMap = new HashMap();
        declineMap.put(getString(R.string.friend_req_table)+"/"+currentUid+"/"+profileUid+"/"+
                getString(R.string.request_type),null);
        declineMap.put(getString(R.string.friend_req_table)+"/"+profileUid+"/"+currentUid+"/"+
                getString(R.string.request_type),null);

        rootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    negativebtn.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                }else{
                    negativebtn.setVisibility(View.GONE);
                    positiveBtn.setVisibility(View.VISIBLE);
                    positiveBtn.setEnabled(true);
                    positiveBtn.setText(getString(R.string.send_request_btn));
                    state = 3;
                }
            }
        });

    }

    private void unFriend() {
        Map<String,Object> unfriendMap = new HashMap();
        unfriendMap.put(getString(R.string.friends_table)+"/"+currentUid+"/"+profileUid,null);

        unfriendMap.put(getString(R.string.friends_table)+"/"+profileUid+"/"+currentUid,null);


        rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    negativebtn.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                }else{
                    // not friend yet
                    positiveBtn.setVisibility(View.VISIBLE);
                    positiveBtn.setEnabled(true);
                    positiveBtn.setText(getString(R.string.send_request_btn));
                    negativebtn.setVisibility(View.GONE);
                    state = 3;
                }
            }
        });

    }

    private void acceptFriendRequest() {

        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.notification_table))
                .child(profileUid).push();
        String notificationId = notificationRef.getKey();

        final String current_date = DateFormat.getDateTimeInstance().format(new Date());

        Map<String,Object> friendMap = new HashMap();

        // first remove request friend
        friendMap.put(getString(R.string.friend_req_table)+"/"+currentUid+"/"+profileUid+"/"+
                getString(R.string.request_type),null);
        friendMap.put(getString(R.string.friend_req_table)+"/"+profileUid+"/"+currentUid+"/"+
                getString(R.string.request_type),null);

        // add to friends table
        friendMap.put(getString(R.string.friends_table)+"/"+currentUid+"/"+profileUid+"/date",current_date);
        friendMap.put(getString(R.string.friends_table)+"/"+profileUid+"/"+currentUid+"/date",current_date);

        friendMap.put(getString(R.string.notification_table)+"/"+profileUid+"/"+notificationId+"/"+"from",currentUid);
        friendMap.put(getString(R.string.notification_table)+"/"+profileUid+"/"+notificationId+"/"+"not_type","accept");

        rootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    positiveBtn.setEnabled(true);
                    negativebtn.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                }else{
                    positiveBtn.setVisibility(View.GONE);
                    negativebtn.setVisibility(View.VISIBLE);
                    negativebtn.setText(getString(R.string.unfriend_btn));
                    negativebtn.setEnabled(true);
                    state = 2;
                    Toast.makeText(UserProfileActivity.this,user.getuName()+" "+ getString(R.string.friend_acceptance), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void setupToolbar() {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(!profileUid.equals(currentUid)) getSupportActionBar().setTitle(user.getuName());
        else getSupportActionBar().setTitle("You");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateUi() {
        Picasso.get().load(user.getuImage()).placeholder(R.drawable.main_default_image).into(profileImage);
        status.setText(user.getuStatus());
        mobile.setText(user.getuPhone());
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();;
        return true;
    }



}
