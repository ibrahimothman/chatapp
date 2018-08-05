package com.ibra.chatappdemo.fragment;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.Friend;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.model.Widget;
import com.ibra.chatappdemo.ui.AllusersActivity;
import com.ibra.chatappdemo.ui.ChatActivity;
import com.ibra.chatappdemo.ui.UserProfileActivity;
import com.ibra.chatappdemo.widget.ChatWidget;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FreindFragment extends Fragment {

    private static final String TAG = FreindFragment.class.getCanonicalName();

    public static final String USER_INFO_EXTRA = "user_info_extra";
    private static final String LIST_POSITION = "LIST_POSITION";


    private DatabaseReference friendDatabase,userDatabase;
    private RecyclerView friendList;
    private String friendId;
    private String currentId;
    private User user;
    private int listPosition = 0;


    public FreindFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend,container,false);

        friendList = (RecyclerView) view.findViewById(R.id.friend_list);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));
        friendList.setHasFixedSize(true);

        if(getActivity() != null) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                friendDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.friends_table))
                        .child(currentId);

                FirebaseRecyclerAdapter<Friend, FriendListHolder> adapter =
                        new FirebaseRecyclerAdapter<Friend, FriendListHolder>(
                                Friend.class, R.layout.user_list_item, FriendListHolder.class, friendDatabase
                        ) {
                            @Override
                            protected void populateViewHolder(final FriendListHolder viewHolder, final Friend model, int position) {
                                final String friendId = getRef(position).getKey();

                                userDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_table)).child(friendId);
                                userDatabase.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null) {
                                            user = dataSnapshot.getValue(User.class);
                                            viewHolder.friendName.setText(user.getuName());
                                            Picasso.get().load(user.getuThumb()).placeholder(R.drawable.thumb_default_image).into(viewHolder.friendImage);
                                            viewHolder.date.setText(model.getDate());

                                            if (dataSnapshot.hasChild(getString(R.string.online))) {
                                                if (dataSnapshot.child(getString(R.string.online)).getValue().equals("true")) {
                                                    viewHolder.onlineIcon.setVisibility(View.VISIBLE);
                                                } else
                                                    viewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                                            } else
                                                viewHolder.onlineIcon.setVisibility(View.INVISIBLE);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();

                                    }
                                });

                                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String[] options = {"view profile", "send message"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle(R.string.choose_option_dialoge);
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (i == 0) {
                                                    // view profile
                                                    Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                                                    userProfileIntent.putExtra(AllusersActivity.USER_ID_EXTRA, friendId);
                                                    getActivity().startActivity(userProfileIntent);
                                                } else if (i == 1) {
                                                    // send message
                                                    Intent userProfileIntent = new Intent(getContext(), ChatActivity.class);
                                                    userProfileIntent.putExtra(AllusersActivity.USER_ID_EXTRA, friendId);
                                                    Log.d("fromfriendfragment", "friendId is " + friendId);
                                                    getActivity().startActivity(userProfileIntent);
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });

                            }
                        };


                // get list position after rotation
                if (savedInstanceState != null) {
                    Log.d("fromRequestFragment", "savedInstanceState not null");
                    listPosition = savedInstanceState.getInt(LIST_POSITION);
                } else listPosition = 0;

                if (listPosition >= 0) {
                    friendList.smoothScrollToPosition(listPosition);
                }
                friendList.setAdapter(adapter);
            }

        }

        return view;
    }




    public static class FriendListHolder extends RecyclerView.ViewHolder  {

        TextView date;
        TextView friendName;
        CircleImageView friendImage;
        ImageView onlineIcon;
        View view;
        public FriendListHolder(View itemView) {
            super(itemView);
            view = itemView;
            date = (TextView) itemView.findViewById(R.id.user_status_list);
            friendName = (TextView) itemView.findViewById(R.id.user_name_list);
            friendImage = (CircleImageView) itemView.findViewById(R.id.user_image_list);
            onlineIcon = (ImageView) itemView.findViewById(R.id.online_icon);
        }


    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(friendList.getLayoutManager() != null) {
            listPosition = ((LinearLayoutManager) friendList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            Log.d(TAG, "fromsavedinsta position is " + listPosition);
            outState.putInt(LIST_POSITION, listPosition);
        }
    }
}
