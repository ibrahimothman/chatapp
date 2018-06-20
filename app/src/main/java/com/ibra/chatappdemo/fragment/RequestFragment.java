package com.ibra.chatappdemo.fragment;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ibra.chatappdemo.R;
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

    RecyclerView requestList;
    String currentId;
    DatabaseReference requestRef,userRef;
    private String requestId;
    IntefaceListener.acceptFriendRequest acceptListener;
    IntefaceListener.declineFriendRequest declineListener;
    ConstraintLayout reqItemLayout;
    ArrayList<User> widgetList = new ArrayList<>();

    public RequestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragemt_request,container,false);
        reqItemLayout = (ConstraintLayout)view.findViewById(R.id.req_list_item_layout);
//        reqItemLayout.setVisibility(View.INVISIBLE);

        acceptListener = (IntefaceListener.acceptFriendRequest) getActivity();
        declineListener = (IntefaceListener.declineFriendRequest) getActivity();

        requestList =(RecyclerView) view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList.setHasFixedSize(true);


        currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.friend_req_table))
                .child(currentId);

        FirebaseRecyclerAdapter<Request,RequestListHolder> adapter =
                new FirebaseRecyclerAdapter<Request, RequestListHolder>(
                        Request.class,R.layout.request_list_item,RequestListHolder.class,requestRef
                ) {
                    @Override
                    protected void populateViewHolder(final RequestListHolder viewHolder, final Request model, int position) {
                        Log.d("fromreqfrag","type is ");
                        if(model.getRequest_type() != null) {
                            if (model.getRequest_type().equals("receive")) {

                                requestId = getRef(position).getKey();
                                userRef = FirebaseDatabase.getInstance().getReference().child("users").child(requestId);
                                userRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null) {
                                            User user = dataSnapshot.getValue(User.class);

                                            viewHolder.name.setText(user.getuName());
                                            Picasso.get().load(user.getuThumb()).placeholder(R.drawable.thumb_default_image).into(viewHolder.image);
                                            widgetList.add(user);
                                            saveRequestIntoSharedPref();

                                            viewHolder.confirmBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    acceptListener.onAcceptRequest(currentId, requestId);
                                                }
                                            });

                                            viewHolder.declineBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    declineListener.onDeclineRequest(currentId, requestId);
                                                }
                                            });


                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
//                        else viewHolder.itemView.setVisibility(View.INVISIBLE);
                   }
                };


        requestList.setAdapter(adapter);



        return view;
    }

    private void saveRequestIntoSharedPref() {
        SharedPreferences mPref = getActivity().getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(widgetList);
        Log.d("WidgetProcess","json in saveInfo is "+json);
        editor.putString(EDITOR_WIDGET_PREF,json);
        editor.apply();
        updateWidget(getContext());
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
}
