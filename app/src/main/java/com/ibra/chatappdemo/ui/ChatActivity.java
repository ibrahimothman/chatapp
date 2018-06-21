package com.ibra.chatappdemo.ui;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.adapter.MessageAdapter;
import com.ibra.chatappdemo.helper.TimeAgo;
import com.ibra.chatappdemo.model.Message;
import com.squareup.picasso.Picasso;

import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getCanonicalName();
    private static final int LIMIT_TO_LAST = 5;
    private static final int PICK_IMAGE = 55;
    private static final String FINAL_PAGE_NUMBER = "FINAL_PAGE_NUMBER";
    private static final String FIRST_LIST_POSITION = "FIRST_LIST_POSITION";
    private static final String LAST_LIST_POSITION = "LAST_LIST_POSITION";


    private  int pageNumber = 1;
    private Toolbar mToolbar;
    private TextView nameTxt,lastseenTxt;
    private CircleImageView image;
    private EditText messageTxt;
    private Button sendImageBtn,takeImageBtn;
    private RecyclerView messageList;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int pos = 0;
    private int firstListPosition;





    RelativeLayout chatLayout;

    private DatabaseReference mRootRef;

    private String friendId,currentId;
    String onlineState,userName,userImage;
    private String lastItemKey,prevItemKey;
    private StorageReference imageStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d("fromchatactivity","insideoncreate");

        // setupm toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View chatBar = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(chatBar);

        nameTxt = (TextView)chatBar.findViewById(R.id.username_chat_bar);
        lastseenTxt = (TextView)chatBar.findViewById(R.id.lastseen_chat_bar);
        image = (CircleImageView)chatBar.findViewById(R.id.image_chat_bar);

        messageTxt = (EditText)findViewById(R.id.message_edit_text);
        sendImageBtn = (Button) findViewById(R.id.send_message_btn);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);

        // recycler view
        messageList = (RecyclerView)findViewById(R.id.messages_list);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        messageList.setHasFixedSize(true);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this);
        messageList.setAdapter(messageAdapter);


        chatLayout = (RelativeLayout)findViewById(R.id.chat_activity);



        if(savedInstanceState != null){
            Log.d("fromchatactivity","saved is not null");
            pageNumber = savedInstanceState.getInt(FINAL_PAGE_NUMBER);
            firstListPosition = savedInstanceState.getInt(FIRST_LIST_POSITION);
            Log.d("fromchatactivity","fromoncreatepositionis"+ firstListPosition);
        }





        //get friend id from intent
        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra(AllusersActivity.USER_ID_EXTRA) != null){
            friendId =  intent.getStringExtra(AllusersActivity.USER_ID_EXTRA);
            Log.d(TAG,"friendid is "+friendId);
        }else {
            Bundle bundle = getIntent().getExtras();
            if(bundle != null && bundle.getString("from_user_id") != null){
                Log.d(TAG,"fromuserprofile not null");
                friendId = bundle.getString("from_user_id");
            }else Log.d(TAG,"fromuserprofile is null");

        }

        // get current user id
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG,"currentid is "+currentId);
        }

        // get friend info
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // when current open chat set seen true
        mRootRef.child("Chat").child(currentId).child(friendId).child("seen").setValue(true);

        // if current not has friend as child
        mRootRef.child("Chat").child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(friendId)){
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentId + "/" + friendId+"/"+"seen", false);
                    chatUserMap.put("Chat/" + friendId + "/" + currentId+"/"+"timestamp", ServerValue.TIMESTAMP);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mRootRef.child("users").child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Log.d("fromChatActivity", "data not null");
                    userName = dataSnapshot.child(getString(R.string.username_key)).getValue().toString();
                    userImage = dataSnapshot.child(getString(R.string.thumb_image_key)).getValue().toString();
                    nameTxt.setText(userName);
                    Picasso.get().load(userImage).placeholder(R.drawable.thumb_default_image).into(image);

                    onlineState = dataSnapshot.child("online").getValue().toString();
                    if (onlineState != null) {
                        Log.d("fromChatActivity", "onlinestat not null");
                        if (onlineState.equals("true")) {
                            lastseenTxt.setText("online");
                        } else {
                            Long timeAgo = Long.parseLong(onlineState);
                            lastseenTxt.setText(TimeAgo.getTimeAgo(timeAgo,ChatActivity.this));
                            Log.d("fromchatActivity",TimeAgo.getTimeAgo(timeAgo,ChatActivity.this));
                        }
                    }else Log.d("fromChatActivity", "onlinestate  null");
                }else Log.d("fromChatActivity", "data  null");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });







        retreiveMessages();


        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });




        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNumber++;
                Log.d("fromchatactivity","from onrefresh page is "+pageNumber);
                pos = 0;
                loadMoreMessage();
            }
        });


    }




    private void sendMessage() {
        String messageContent = messageTxt.getText().toString();
        if(!TextUtils.isEmpty(messageContent)){
            Message message = new Message();
            message.setMessage(messageContent);
            message.setSeen("false");
            message.setTime(String.valueOf(System.currentTimeMillis()*-1));
            message.setType("text");
            message.setFrom(currentId);


            DatabaseReference pushIdRef = mRootRef.child(getString(R.string.messages_table)).child(currentId).child(friendId).push();
            String pushId = pushIdRef.getKey();

            String current_user_ref = getString(R.string.messages_table)+"/"+currentId+"/"+friendId;
            String friend_user_ref = getString(R.string.messages_table)+"/"+friendId+"/"+currentId;

            String notificationRef = getString(R.string.notification_table)+"/"+friendId;

            Map<String,Object> messageMap = new HashMap<>();

            // messages
            messageMap.put(current_user_ref+"/"+pushId,message);
            messageMap.put(friend_user_ref+"/"+pushId,message);

            // notification
            messageMap.put(notificationRef+"/"+pushId+"/"+"from",currentId);
            messageMap.put(notificationRef+"/"+pushId+"/"+"not_type","message");

            // conversation
            mRootRef.child("Chat").child(currentId).child(friendId).child("seen").setValue(true);
            mRootRef.child("Chat").child(currentId).child(friendId).child("timestamp").setValue(System.currentTimeMillis()*-1);

            mRootRef.child("Chat").child(friendId).child(currentId).child("seen").setValue(false);
            mRootRef.child("Chat").child(friendId).child(currentId).child("timestamp").setValue(System.currentTimeMillis()*-1);

            mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                   if(databaseError != null){
                       Log.d(TAG,"error is"+databaseError.getMessage().toString());
                   } else{
                        messageTxt.setText("");
//                        messageList.scrollToPosition(messages.size() - 1);
                   }
                }
            });
        }
    }

    private void retreiveMessages() {

        // get current user and friend images


        if(currentId != null && friendId != null) {
            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.messages_table))
                    .child(currentId).child(friendId);
            Query messageQuery = messageRef.limitToLast(pageNumber * LIMIT_TO_LAST);

            messageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        Log.d(TAG, "newchildadded");
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message != null) {
                            Log.d(TAG, "message is " + message.getMessage());
                            messages.add(message);
                            pos++;
                            if (pos == 1) {
                                lastItemKey = dataSnapshot.getKey();
                                prevItemKey = dataSnapshot.getKey();
                                Log.d(TAG, "lastKeyIs " + lastItemKey);
                            }

                            messageAdapter.notifyAdapter(messages);
                            Log.d("fromchatactivity","firstpositionis"+ firstListPosition);
                            messageList.scrollToPosition(firstListPosition);
                            mSwipeRefreshLayout.setRefreshing(false);

                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void loadMoreMessage(){
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.messages_table))
                .child(currentId).child(friendId);
        Query messageQuery = messageRef.orderByKey().endAt(lastItemKey).limitToLast(5);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null){
                    Log.d(TAG,"newchildadded");
                    Message message = dataSnapshot.getValue(Message.class);
                    if(message != null){
                        Log.d(TAG,"message is "+message.getMessage());
                        if(!prevItemKey.equals(dataSnapshot.getKey())){
                            messages.add(pos++,message);
                        }else{
                            prevItemKey = lastItemKey;
                        }

                        if(pos == 1){
                            lastItemKey = dataSnapshot.getKey();
                            Log.d(TAG,"lastKeyIs "+lastItemKey);
                        }

                        messageAdapter.notifyDataSetChanged();
//                        messageList.scrollToPosition(messages.size() - 1);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("fromchatactivity","from onsaved page is "+pageNumber);
        outState.putInt(FINAL_PAGE_NUMBER,pageNumber);
        firstListPosition = ((LinearLayoutManager)messageList.getLayoutManager()).findFirstVisibleItemPosition();
        Log.d("fromchatactivity","from onsaved firstvisible is "+ firstListPosition);
        outState.putInt(FIRST_LIST_POSITION, firstListPosition);



    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
