package com.ibra.chatappdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.adapter.MessageAdapter;
import com.ibra.chatappdemo.dialog.ProgressDialoge;
import com.ibra.chatappdemo.helper.TimeAgo;
import com.ibra.chatappdemo.model.Message;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

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
    private Button sendBtn;
    ImageButton sendImageBtn;
    private RecyclerView messageList;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int pos = 0;
    private int firstListPosition;





    RelativeLayout chatLayout;

    private DatabaseReference mRootRef;

    private String friendId,currentId;
    String onlineState,userName,userImage;
    private String lastItemKey,prevItemKey;
    private StorageReference imageStorageRef;
    private ProgressDialoge mProgressDialoge;
    private Bitmap thumbnail_bitmap;


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
        sendBtn = (Button) findViewById(R.id.send_message_btn);
        sendImageBtn = (ImageButton) findViewById(R.id.send_image_btn);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);

        // recycler view
        messageList = (RecyclerView)findViewById(R.id.messages_list);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        messageList.setHasFixedSize(true);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messages);
        messageList.setAdapter(messageAdapter);


        chatLayout = (RelativeLayout)findViewById(R.id.chat_activity);



        if(savedInstanceState != null){
            Log.d("fromchatactivity","saved is not null");
            pageNumber = savedInstanceState.getInt(FINAL_PAGE_NUMBER);
            firstListPosition = savedInstanceState.getInt(FIRST_LIST_POSITION);
            Log.d("fromchatactivity","fromoncreatepositionis"+ firstListPosition);
        }else firstListPosition = 0;





        //get friend id from intent
        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra(AllusersActivity.USER_ID_EXTRA) != null){
            friendId =  intent.getStringExtra(AllusersActivity.USER_ID_EXTRA);
            Log.d(TAG,"friendid is "+friendId);
        }else {
            Bundle bundle = getIntent().getExtras();
            if(bundle != null && bundle.getString(getString(R.string.from_user_id)) != null){
                Log.d(TAG,"fromuserprofile not null");
                friendId = bundle.getString(getString(R.string.from_user_id));
            }else Log.d(TAG,"fromuserprofile is null");

        }

        // get current user id
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG,"currentid is "+currentId);
        }

        // get friend info
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // make message seen when user open chat
        makeMessageSeen();

        mRootRef.child(getString(R.string.users_table)).child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Log.d("fromChatActivity", "data not null");
                    userName = dataSnapshot.child(getString(R.string.username_key)).getValue().toString();
                    userImage = dataSnapshot.child(getString(R.string.thumb_image_key)).getValue().toString();
                    nameTxt.setText(userName);
                    Picasso.get().load(userImage).placeholder(R.drawable.thumb_default_image).into(image);
                    if(dataSnapshot.hasChild(getString(R.string.online))){
                        onlineState = dataSnapshot.child(getString(R.string.online)).getValue().toString();
                        if (onlineState != null) {
                            Log.d("fromChatActivity", "onlinestat not null");
                            if (onlineState.equals("true")) {
                                lastseenTxt.setText(R.string.online);
                            } else {
                                Long timeAgo = Long.parseLong(onlineState);
                                lastseenTxt.setText(TimeAgo.getTimeAgo(timeAgo));
                                Log.d("fromchatActivity", TimeAgo.getTimeAgo(timeAgo));
                            }
                        } else Log.d("fromChatActivity", "onlinestate  null");
                    }
                }else Log.d("fromChatActivity", "data  null");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            }
        });



        retreiveMessages();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickupImage();
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

    private void pickupImage() {
        // pick an image from gallery
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.select_image)), PICK_IMAGE);
    }

    // when image picked and return to app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialoge = new ProgressDialoge(this);
                mProgressDialoge.showProgressDialoge(getString(R.string.waiting));
                Uri resultUri = result.getUri();
                saveImageProfile(resultUri);

            }

        }
    }

    private void saveImageProfile(Uri resultUri) {
        DatabaseReference user_message_push = mRootRef.child(getString(R.string.messages_table))
                .child(currentId).child(friendId).push();

        final String pushKey = user_message_push.getKey();

        StorageReference mainImageRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.messages_image))
                .child(pushKey+".jpg");

        mainImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    mProgressDialoge.dismissProgressDialoge();
                    String mainImageLink = task.getResult().getDownloadUrl().toString();
                    Log.d(TAG,"imagelink "+mainImageLink);
                    sendImage(mainImageLink,pushKey);

                }
                else Toast.makeText(ChatActivity.this,getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendImage(String mainImageLink,String pushId) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Message message = new Message();
        message.setMessage(mainImageLink);
        message.setSeen("false");
        message.setTime(String.valueOf(System.currentTimeMillis()*-1));
        message.setType("image");
        message.setFrom(currentId);
        message.setTime(String.valueOf(System.currentTimeMillis()));


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
        mRootRef.child(getString(R.string.chat_table)).child(currentId).child(friendId).child(getString(R.string.seen)).setValue(true);
        mRootRef.child(getString(R.string.chat_table)).child(currentId).child(friendId).child(getString(R.string.time_stamp)).setValue(System.currentTimeMillis()*-1);

        mRootRef.child(getString(R.string.chat_table)).child(friendId).child(currentId).child(getString(R.string.seen)).setValue(false);
        mRootRef.child(getString(R.string.chat_table)).child(friendId).child(currentId).child(getString(R.string.time_stamp)).setValue(System.currentTimeMillis()*-1);

        mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Toast.makeText(getApplicationContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                    }
                }
            });

    }

    private void makeMessageSeen() {
        mRootRef.child("Chat").child(currentId).child(friendId).child("seen").setValue(true);
    }


    private void sendMessage() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        String messageContent = messageTxt.getText().toString();
        if(!TextUtils.isEmpty(messageContent)){
            Message message = new Message();
            message.setMessage(messageContent);
            message.setSeen("false");
            message.setTime(String.valueOf(System.currentTimeMillis()*-1));
            message.setType("text");
            message.setFrom(currentId);
            message.setTime(String.valueOf(System.currentTimeMillis()));


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
            mRootRef.child(getString(R.string.chat_table)).child(currentId).child(friendId).child(getString(R.string.seen)).setValue(true);
            mRootRef.child(getString(R.string.chat_table)).child(currentId).child(friendId).child(getString(R.string.time_stamp)).setValue(System.currentTimeMillis()*-1);

            mRootRef.child(getString(R.string.chat_table)).child(friendId).child(currentId).child(getString(R.string.seen)).setValue(false);
            mRootRef.child(getString(R.string.chat_table)).child(friendId).child(currentId).child(getString(R.string.time_stamp)).setValue(System.currentTimeMillis()*-1);

            mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                   if(databaseError != null){
                       Toast.makeText(getApplicationContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
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
                            Log.d(TAG,"message "+message.getMessage());

                            pos++;
                            if (pos == 1) {
                                lastItemKey = dataSnapshot.getKey();
                                prevItemKey = dataSnapshot.getKey();
                                Log.d(TAG, "lastKeyIs " + lastItemKey);
                            }

                            messageAdapter.notifyAdapter(messages);
                            Log.d("fromchatactivity","firstpositionis"+ firstListPosition);
                            if(firstListPosition >= 0) {
                                messageList.scrollToPosition(firstListPosition);
                            }
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
