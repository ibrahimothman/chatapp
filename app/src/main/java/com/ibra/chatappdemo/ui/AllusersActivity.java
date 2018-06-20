package com.ibra.chatappdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllusersActivity extends AppCompatActivity {

    private static final String TAG = AllusersActivity.class.getCanonicalName();
    public static final String USER_ID_EXTRA = "user_id_for_profile_activity";
    private Toolbar mToolbar;
    private RecyclerView usersList;
    private DatabaseReference mDatabase;
    private static String currentUid;

    private String TAg = "sdasd";
    ArrayList<User> users = new ArrayList<>();
    FirebaseRecyclerAdapter<User,UserListViewHolder> adapter;


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


        // get current user id
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // get reference from database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");


        adapter = new FirebaseRecyclerAdapter<User, UserListViewHolder>(
                User.class,R.layout.user_list_item,UserListViewHolder.class,mDatabase
        ) {
            @Override
            protected void populateViewHolder(UserListViewHolder viewHolder, User model, int position) {

                final String profileUid = getRef(position).getKey();

                if(profileUid.equals(currentUid)){
                    Log.d(TAG,"yourprofile");
                    viewHolder.setName("You");
                }else viewHolder.setName(model.getuName());

                viewHolder.setStatus(model.getuStatus());

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


    }

    public static  class UserListViewHolder extends RecyclerView.ViewHolder{


        CircleImageView image;
        TextView name;
        TextView status;
        View mView;
        public UserListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            image =(CircleImageView) itemView.findViewById(R.id.user_image_list);
            name =(TextView) itemView.findViewById(R.id.user_name_list);
            status =(TextView) itemView.findViewById(R.id.user_status_list);


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
    }






}
