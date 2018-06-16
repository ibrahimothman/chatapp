package com.ibra.chatappdemo.fragment;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.Chat;
import com.ibra.chatappdemo.ui.AllusersActivity;
import com.ibra.chatappdemo.ui.ChatActivity;
import com.ibra.chatappdemo.widget.ChatWidget;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    public static final String WIDGET_PREF = "WIDGET_PREF";
    public static final String MESSAGE_WIDGET_PREF = "MESSAGE_WIDGET_PREF";
    public static final String NAME_WIDGET_PREF = "NAME_WIDGET_PREF";
    public static final String TIME_WIDGET_PREF = "TIME_WIDGET_PREF";
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference messageRef;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private RecyclerView chatList;
    private SharedPreferences mPreferences;
    private int i = 0;
    private SharedPreferences.Editor editor;
    private String mTime,mMessage,mName;


    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("fromchatfragment","inside");
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        mPreferences = getActivity().getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        editor = mPreferences.edit();


        chatList = (RecyclerView)view.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList.setHasFixedSize(true);


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
        }


        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        messageRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.messages_table))
                        .child(currentUserId);

        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUserId);





        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query mChatquery = mChatRef.orderByChild("timestamp");


        FirebaseRecyclerAdapter<Chat,ChatHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>(
                Chat.class,R.layout.user_list_item,ChatHolder.class,mChatquery
        ) {
            @Override
            protected void populateViewHolder(final ChatHolder viewHolder, Chat model, int position) {
                final String friendId = getRef(position).getKey();
                final boolean isSeen = model.seen;
                final Long time = model.timestamp;
                Query lastMessage = messageRef.child(friendId).limitToLast(1);
                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String message = dataSnapshot.child("message").getValue().toString();
                        mMessage = message;
                        viewHolder.setMessage(message,isSeen);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                        Date date = new Date(time);
                        String messageTime = simpleDateFormat.format(date);
                        mTime = messageTime;

                        editor.putString(MESSAGE_WIDGET_PREF,message);
                        Log.d("fromcahtfragment","message is added");
                        editor.putString(TIME_WIDGET_PREF,messageTime);
                        Log.d("fromcahtfragment","time is added");
                        editor.apply();



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

                // get friend name and image from users teble

                mUserRef.child(friendId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(getString(R.string.username_key)).getValue().toString();
                        mName = name;
                        String image = dataSnapshot.child(getString(R.string.thumb_image_key)).getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String online = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(online);
                        }
                        viewHolder.setName(name);
                        viewHolder.setImage(image);



                        editor.putString(NAME_WIDGET_PREF,name);
                        Log.d("fromcahtfragment","name is added");
                        editor.apply();
                        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
                        ComponentName componentName = new ComponentName(getActivity(), ChatWidget.class);
                        int[]appwidgetIds = manager.getAppWidgetIds(componentName);
                        manager.notifyAppWidgetViewDataChanged(appwidgetIds,R.id.widget_list_view);
                        Log.d("fromcahtfragment","i is"+i);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // go to chat room
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra(AllusersActivity.USER_ID_EXTRA,friendId);
                        startActivity(chatIntent);

                    }
                });

            }
        };

        chatList.setAdapter(adapter);

    }

    public static class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CircleImageView friendImage;
        TextView messageTxt;
        TextView friendName;
        ImageView onlineIcon;
        View mView;
        public ChatHolder(View itemView) {
            super(itemView);
            mView = itemView;
            messageTxt = (TextView)itemView.findViewById(R.id.user_status_list);
            friendName = (TextView)itemView.findViewById(R.id.user_name_list);
            friendImage = (CircleImageView)itemView.findViewById(R.id.user_image_list);
            onlineIcon = (ImageView) itemView.findViewById(R.id.online_icon);
        }

        public void setMessage(String message, boolean isSeen) {
            Log.d("fromchatfrag","is seen? "+isSeen);
            if(isSeen){
                messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.NORMAL);
            }else {
                messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.BOLD);
                messageTxt.setTextColor(Color.BLACK
                );
            }

            messageTxt.setText(message);
        }

        public void setName(String name) {
            friendName.setText(name);
        }

        public void setImage(String image) {
            Picasso.get().load(image).placeholder(R.drawable.thumb_default_image).into(friendImage);
        }

        @Override
        public void onClick(View view) {

        }

        public void setOnline(String online) {
            if(online.equals("true")){
                onlineIcon.setVisibility(View.VISIBLE);
            }else onlineIcon.setVisibility(View.INVISIBLE);
        }
    }


}
