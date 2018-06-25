package com.ibra.chatappdemo.fragment;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.Chat;
import com.ibra.chatappdemo.model.Message;
import com.ibra.chatappdemo.model.Widget;
import com.ibra.chatappdemo.ui.AllusersActivity;
import com.ibra.chatappdemo.ui.ChatActivity;
import com.ibra.chatappdemo.widget.ChatWidget;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    public static final String WIDGET_PREF = "WIDGET_PREF";
    public static final String EDITOR_WIDGET_PREF = "EDITOR_WIDGET_PREF";
    private static final String TAG = ChatFragment.class.getCanonicalName();
    private static final String LIST_POSITION = "LIST_POSITION";


    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference messageRef;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private RecyclerView chatList;

    private int listPosition ;


    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        Log.d("fromchatfragment","inside");
        View view = inflater.inflate(R.layout.fragment_chat,container,false);





        chatList = (RecyclerView)view.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList.setHasFixedSize(true);

        // get list position after rotation
        if(savedInstanceState != null){
            Log.d("fromRequestFragment","savedInstanceState not null");
            listPosition = savedInstanceState.getInt(LIST_POSITION);
        }else listPosition = 0;



        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

            messageRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.messages_table))
                    .child(currentUserId);

            mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUserId);

            Query mChatquery = mChatRef.orderByChild("timestamp");

            FirebaseRecyclerAdapter<Chat,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                    Chat.class,R.layout.user_list_item,ChatViewHolder.class,mChatquery
            ) {
                @Override
                protected void populateViewHolder(final ChatViewHolder viewHolder, Chat model, int position) {
                    Log.d("fromchatfragment","load data");
                    final String friendId = getRef(position).getKey();
                    final boolean isSeen = model.isSeen();
                    final Long time = model.getTimestamp();

                    // get last message
                    Query lastMessage = messageRef.child(friendId).limitToLast(1);
                    lastMessage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()) {
                                String message = data.child("message").getValue().toString();
                                viewHolder.setMessage(message, String.valueOf(isSeen));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                        }
                    });


                    mUserRef.child(friendId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(getActivity() != null) {
                                if(dataSnapshot.hasChild(getString(R.string.username_key))) {
                                    String name = dataSnapshot.child(getString(R.string.username_key)).getValue().toString();
                                    viewHolder.setName(name);
                                }
                                if(dataSnapshot.hasChild(getString(R.string.thumb_image_key))) {
                                    String image = dataSnapshot.child(getString(R.string.thumb_image_key)).getValue().toString();
                                    viewHolder.setImage(image);
                                }
                                if (dataSnapshot.hasChild("online")) {

                                    String onlineState = dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setOnlineIcon(onlineState);

                                }



                            }


                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra(AllusersActivity.USER_ID_EXTRA, friendId);
                                    startActivity(chatIntent);

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                        }
                    });

                }


            };
            chatList.smoothScrollToPosition(listPosition);
            chatList.setAdapter(adapter);




        }


        return view;
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        CircleImageView friendImage;
        TextView messageTxt;
        TextView friendName;
        ImageView onlineIcon;
        View mView;
        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            messageTxt = (TextView)itemView.findViewById(R.id.user_status_list);
            friendName = (TextView)itemView.findViewById(R.id.user_name_list);
            friendImage = (CircleImageView)itemView.findViewById(R.id.user_image_list);
            onlineIcon = (ImageView) itemView.findViewById(R.id.online_icon);
        }

        public void setMessage(String message,String isSeen) {
            Log.d("fromchatfragment","seen is "+isSeen);
            if (isSeen.equals("true")){
                messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.NORMAL);
            }else  messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.BOLD);
            messageTxt.setText(message);
        }

        public void setName(String name) {
            friendName.setText(name);
        }

        public void setImage(String image) {
            Picasso.get().load(image).placeholder(R.drawable.thumb_default_image).into(friendImage);
        }

        public void setOnlineIcon(String onlineState) {
            if(onlineState.equals("true")){
                onlineIcon.setVisibility(View.VISIBLE);
            }else onlineIcon.setVisibility(View.INVISIBLE);
        }
    }





    private void updateWidget(Context context) {
        Log.d("WidgetProcess","inside updateWidget");
        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
        ComponentName componentName = new ComponentName(context, ChatWidget.class);
        int[]appwidgetIds = manager.getAppWidgetIds(componentName);
        manager.notifyAppWidgetViewDataChanged(appwidgetIds,R.id.widget_list_view);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        listPosition =((LinearLayoutManager)chatList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(TAG,"fromsavedinsta position is "+listPosition);
        outState.putInt(LIST_POSITION,listPosition);
    }




}
