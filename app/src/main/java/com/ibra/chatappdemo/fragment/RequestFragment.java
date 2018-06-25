package com.ibra.chatappdemo.fragment;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ibra.chatappdemo.adapter.RequestAdapter;
import com.ibra.chatappdemo.listener.IntefaceListener;
import com.ibra.chatappdemo.model.Request;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.widget.ChatWidget;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment  {

    public static final String WIDGET_PREF = "WIDGET_PREF";
    public static final String EDITOR_WIDGET_PREF = "EDITOR_WIDGET_PREF";
    private static final String TAG = RequestFragment.class.getCanonicalName();
    private static final String LIST_POSITION = "LIST_POSITION";

    RecyclerView requestList;
    String currentId;
    DatabaseReference requestRef,userRef;
    private String requestId;

    ConstraintLayout reqItemLayout;
    ArrayList<User> widgetList = new ArrayList<>();
    RequestAdapter requestAdapter;

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();
    private int listPosition ;


    public RequestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragemt_request,container,false);




        requestList =(RecyclerView) view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList.setHasFixedSize(true);
        requestAdapter = new RequestAdapter(getContext(),users);
        requestList.setAdapter(requestAdapter);

        // get list position after rotation
        if(savedInstanceState != null){
            Log.d("fromRequestFragment","savedInstanceState not null");
            listPosition = savedInstanceState.getInt(LIST_POSITION);
        }else listPosition = 0;


        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.friend_req_table))
                .child(currentId);

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("fromRequestFragment","inside ondatachange");
                users.clear();
                ids.clear();
//                requestAdapter.notifyDataSetChanged();
                saveRequestIntoSharedPref();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data != null) {
                        Request request = data.getValue(Request.class);
                        if (request.getRequest_type().equals("receive")) {
                            final String friendId = data.getKey();
                            userRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("fromRequestFragment","insideondatachange2");
                                    if (dataSnapshot != null) {
                                        User user = dataSnapshot.getValue(User.class);
                                        users.add(user);
                                        ids.add(friendId);
                                        Log.d("fromRequestFragment","usersize is"+users.size());
                                        Log.d("fromRequestFragment","friendId is "+friendId);
                                        requestAdapter.notifyAdapter(users,ids);
//                                        Log.d("fromRequestFragment","listpositionis "+listPosition);
                                        requestList.smoothScrollToPosition(listPosition);
                                        saveRequestIntoSharedPref();

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            }

        });

        return view;
    }

    private void saveRequestIntoSharedPref() {
        if(getActivity() != null) {
            SharedPreferences mPref = getActivity().getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mPref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(users);
            Log.d("WidgetProcess", "json in saveInfo is " + json);
            editor.putString(EDITOR_WIDGET_PREF, json);
            editor.apply();
            updateWidget(getContext());
        }
    }

    private void updateWidget(Context context) {
        Log.d("WidgetProcess","inside updateWidget");
        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
        ComponentName componentName = new ComponentName(context, ChatWidget.class);
        int[]appwidgetIds = manager.getAppWidgetIds(componentName);
        manager.notifyAppWidgetViewDataChanged(appwidgetIds,R.id.widget_list_view);
    }



    static class RequestListHolder extends RecyclerView.ViewHolder{

        TextView name;
        CircleImageView image;
        Button confirmBtn,declineBtn;
        public RequestListHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.user_name_list_req);
            confirmBtn = (Button) itemView.findViewById(R.id.confirm_btn);
            declineBtn = (Button) itemView.findViewById(R.id.decline_button);
            image = (CircleImageView) itemView.findViewById(R.id.user_image_list_req);

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        listPosition =((LinearLayoutManager)requestList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(TAG,"fromsavedinsta position is "+listPosition);
        outState.putInt(LIST_POSITION,listPosition);
    }
}
