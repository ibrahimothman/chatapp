package com.ibra.chatappdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.listener.IntefaceListener;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestHolder> {


    private Context context;
    private ArrayList<User> users;
    private IntefaceListener.acceptFriendRequest acceptListener;
    private IntefaceListener.declineFriendRequest declineListener;
    private String cuurentId;
    private ArrayList<String> friendIds;

    public RequestAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
        acceptListener = (IntefaceListener.acceptFriendRequest) context;
        declineListener = (IntefaceListener.declineFriendRequest) context;
//        this.friendId = friendId;
        cuurentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestHolder(LayoutInflater.from(context)
                .inflate(R.layout.request_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, final int position) {
        holder.name.setText(users.get(position).getuName());
        Picasso.get().load(users.get(position).getuThumb()).placeholder(R.drawable.thumb_default_image)
                .into(holder.image);

        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineListener.onDeclineRequest(cuurentId,friendIds.get(position));
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptListener.onAcceptRequest(cuurentId,friendIds.get(position));
            }
        });
    }

    public void notifyAdapter(ArrayList<User> users, ArrayList<String> friendIds){
        if(friendIds != null && users != null) {
            Log.d("fromRequestFragment","fromanotifiyadapterdapter");
            this.friendIds = friendIds;
            this.users = users;
            this.notifyDataSetChanged();
        }


    }

    @Override
    public int getItemCount() {
        if (users.size() != 0 )return users.size();
        else return 0;
    }

    class RequestHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name;
        Button accept,decline;

        public RequestHolder(View itemView) {
            super(itemView);
            image = (CircleImageView)itemView.findViewById(R.id.user_image_list_req);
            name = (TextView) itemView.findViewById(R.id.user_name_list_req);
            accept = (Button) itemView.findViewById(R.id.confirm_btn);
            decline = (Button)itemView.findViewById(R.id.decline_button);
        }
    }
}
