package com.ibra.chatappdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.adapter.SearchAdapter;
import com.ibra.chatappdemo.helper.OnlineHelper;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllusersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = AllusersActivity.class.getCanonicalName();
    public static final String USER_ID_EXTRA = "user_id_for_profile_activity";
    private static final String LIST_POSITION = "LIST_POSITION";


    private Toolbar mToolbar;
    private RecyclerView usersList;
    private DatabaseReference mDatabase;
    private static String currentUid;
    FirebaseUser currentUser;
    SearchView mSearchView;


    List<User> users = new ArrayList<>();
    SearchAdapter searchAdapter ;
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
        searchAdapter = new SearchAdapter(users,this);
        usersList.setAdapter(searchAdapter);
        usersList.setHasFixedSize(true);

        // setup search view
        mSearchView = (SearchView)findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);

        // get list position after rotation
        if(savedInstanceState != null){
            listPosition = savedInstanceState.getInt(LIST_POSITION);
        }


        // get current user id
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        // get reference from database
        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_table));






    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(!s.isEmpty()){
            searchUsers(s);
        }
        return true;
    }


    public void  searchUsers(final String s){
        users.clear();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    if(user.getuName().contains(s)) {
                        Log.d(TAG,"useris "+user.getuName());
                        user.setuId(data.getKey());
                        users.add(user);
                        searchAdapter.notify(users);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                mDatabase.child(currentUid).child("online").setValue(String.valueOf(System.currentTimeMillis()));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
