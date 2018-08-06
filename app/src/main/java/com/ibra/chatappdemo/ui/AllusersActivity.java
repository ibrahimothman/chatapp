package com.ibra.chatappdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.helper.OnlineHelper;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllusersActivity extends AppCompatActivity {

    private static final String TAG = AllusersActivity.class.getCanonicalName();
    public static final String USER_ID_EXTRA = "user_id_for_profile_activity";
    private static final String LIST_POSITION = "LIST_POSITION";


    private Toolbar mToolbar;
    private RecyclerView usersList;
    private DatabaseReference mDatabase;
    private static String currentUid;
    FirebaseUser currentUser;


    ArrayList<User> users = new ArrayList<>();
    FirebaseRecyclerAdapter<User,UserListViewHolder> adapter;
    private int listPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allusers);

        // setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.all_user_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setup users recycler view
        usersList = (RecyclerView)findViewById(R.id.alluser_list);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        usersList.setHasFixedSize(true);

        // get list position after rotation
        if(savedInstanceState != null){
            listPosition = savedInstanceState.getInt(LIST_POSITION);
        }


        // get current user id
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        // get reference from database
        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_table));



        adapter = new FirebaseRecyclerAdapter<User, UserListViewHolder>(
                User.class,R.layout.user_list_item,UserListViewHolder.class,mDatabase
        ) {
            @Override
            protected void populateViewHolder(UserListViewHolder viewHolder, User model, int position) {
                Log.d(TAG,"insidepopulate");
                final String profileUid = getRef(position).getKey();

                if(profileUid.equals(currentUid)){
                    Log.d(TAG,"yourprofile");
                    viewHolder.setName("You");
                }else viewHolder.setName(model.getuName());

                viewHolder.setStatus(model.getuStatus());

                if(model.getOnline() != null){
                    viewHolder.setOnlineIcon(model.getOnline());
                }

                if(model.getuThumb() != null && !model.getuThumb().equals("default"))
                    viewHolder.setImage(model.getuThumb());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userProfileIntent = new Intent(getApplicationContext(),UserProfileActivity.class);
                        userProfileIntent.putExtra(USER_ID_EXTRA,profileUid);
                        startActivity(userProfileIntent);
                    }
                });


            }
        };

        usersList.setAdapter(adapter);
        Log.d(TAG,"listpositionis "+listPosition);
        if(listPosition >= 0) {
            usersList.smoothScrollToPosition(listPosition);
        }


    }

    public static  class UserListViewHolder extends RecyclerView.ViewHolder{


        CircleImageView image;
        ImageView onlineIcon;
        TextView name;
        TextView status;
        View mView;
        public UserListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            image =(CircleImageView) itemView.findViewById(R.id.user_image_list);
            name =(TextView) itemView.findViewById(R.id.user_name_list);
            status =(TextView) itemView.findViewById(R.id.user_status_list);
            onlineIcon =(ImageView) itemView.findViewById(R.id.online_icon);


        }

        public void setName(String s) {

            name.setText(s);
        }

        public void setStatus(String s) {
            status.setText(s);
        }

        public void setImage(String s) {
            Picasso.get().load(s).placeholder(R.drawable.thumb_default_image).into(image);
        }

        public void setOnlineIcon(String online) {
            if(online.equals("true")){
                onlineIcon.setVisibility(View.VISIBLE);
            }else if(online.equals("false")){
                onlineIcon.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listPosition =((LinearLayoutManager)usersList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(TAG,"fromsavedinsta position is "+listPosition);
        outState.putInt(LIST_POSITION,listPosition);

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("test","main start");
        try {
            if(currentUser != null && OnlineHelper.isOnForeground(this)){
                mDatabase.child(currentUid).child("online").setValue("true");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("test","main stop");
        try {
            if(currentUser != null && !OnlineHelper.isOnForeground(this)) {
                mDatabase.child(currentUid).child("online").setValue("false");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
