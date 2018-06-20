package com.ibra.chatappdemo.ui;

import android.app.AlertDialog;



import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.adapter.ViewPagerAdapter;
import com.ibra.chatappdemo.fragment.ChatFragment;
import com.ibra.chatappdemo.fragment.FreindFragment;
import com.ibra.chatappdemo.fragment.RequestFragment;
import com.ibra.chatappdemo.listener.IntefaceListener;
import com.ibra.chatappdemo.preferenceManage.SharedPreferenceStart;
import com.ibra.chatappdemo.service.NotificationReminder;
import com.ibra.chatappdemo.widget.ChatWidgetRemoteFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IntefaceListener.acceptFriendRequest
,IntefaceListener.declineFriendRequest{


    private static final String TAG = MainActivity.class.getCanonicalName();
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private SharedPreferenceStart prefStart;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ViewPagerAdapter mViewPagerAdapter;
    Fragment[]fragments = {new ChatFragment(),new FreindFragment(),new RequestFragment()};
    FragmentManager fm;
    private String currentId;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        prefStart = new SharedPreferenceStart(this);
        Log.d(TAG,"statusIs "+prefStart.isFirstTime_app());

        if(!prefStart.isFirstTime_app()){
            startActivity(new Intent(this,StartActivity.class));
        }

        //setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.main_activity);

        // setup firebase auth
        mAuth = FirebaseAuth.getInstance();

        // setup view pager
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mTabLayout = (TabLayout)findViewById(R.id.tablayout);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);




        rootRef = FirebaseDatabase.getInstance().getReference();


        // if mainActivity launched from widget go to request fragment
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(ChatWidgetRemoteFactory.WIDGET_EXTRA) != null){
            mViewPager.setCurrentItem(2);
        }




        // start job dispatcher
        NotificationReminder.scheduleChargingReminder(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // sign out option
        if(item.getItemId() == R.id.signout_option) {
            Log.d(TAG, "you press signout option");
            showDialoge();
            return true;
        }

         // setting option
         else if(item.getItemId() == R.id.setting_option){
            Intent intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
            return true;
        }

        // all users option
        else if(item.getItemId() == R.id.allusers_option){
            Intent intent = new Intent(this,AllusersActivity.class);
            startActivity(intent);
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }


    // show dialog to make sure sign out
    private void showDialoge() {
        Log.d(TAG,"show diaolge now");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_signout);
        builder.setCancelable(false);
        builder.setPositiveButton("sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signout();
            }
        });
        builder.setNegativeButton("No",null);
        builder.show();
    }


    // sign out app
    private void signout() {
        mAuth.signOut();
        prefStart.changePref(getString(R.string.first_time));
        updateUi();
    }

    private void updateUi() {
        startActivity(new Intent(this,StartActivity.class));
    }




    @Override
    public void onAcceptRequest(String currentId, String friendId) {

        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.notification_table))
                .child(friendId).push();
        String notificationId = notificationRef.getKey();
        final String current_date = DateFormat.getDateTimeInstance().format(new Date());

        Map<String,Object> friendMap = new HashMap();

        // first remove request friend
        friendMap.put(getString(R.string.friend_req_table)+"/"+currentId+"/"+friendId+"/"+
                getString(R.string.request_type),null);
        friendMap.put(getString(R.string.friend_req_table)+"/"+friendId+"/"+currentId+"/"+
                getString(R.string.request_type),null);

        // add to friends table
        friendMap.put(getString(R.string.friends_table)+"/"+currentId+"/"+friendId+"/date",current_date);
        friendMap.put(getString(R.string.friends_table)+"/"+friendId+"/"+currentId+"/date",current_date);

        friendMap.put(getString(R.string.notification_table)+"/"+friendId+"/"+notificationId+"/"+"from",currentId);
        friendMap.put(getString(R.string.notification_table)+"/"+friendId+"/"+notificationId+"/"+"not_type","accept");

        rootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    Toast.makeText(MainActivity.this, getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                }else{

                    Toast.makeText(MainActivity.this, getString(R.string.friend_acceptance), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onDeclineRequest(String currentId, String friendId) {
        Map<String,Object> declineMap = new HashMap();
        declineMap.put(getString(R.string.friend_req_table)+"/"+currentId+"/"+friendId+"/"+
                getString(R.string.request_type),null);
        declineMap.put(getString(R.string.friend_req_table)+"/"+friendId+"/"+currentId+"/"+
                getString(R.string.request_type),null);

        rootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){

                    Toast.makeText(MainActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.decline_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
