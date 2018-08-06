package com.ibra.chatappdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.ui.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ibra.chatappdemo.ui.AllusersActivity.USER_ID_EXTRA;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.UserListViewHolder> {

    private List<User> userList;
    private Context mContext;
    private String profileId;

    public SearchAdapter(List<User> userList, Context mContext) {
        this.userList = userList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserListViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.user_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.setName(user.getuName());
        Log.d("fromSadapetr",user.getuId());
        holder.setStatus(user.getuStatus());
        if(user.getOnline() != null) {
            holder.setOnlineIcon(user.getOnline());
        }
        holder.setImage(user.getuImage());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent userProfileIntent = new Intent(mContext,UserProfileActivity.class);
                userProfileIntent.putExtra(USER_ID_EXTRA,user.getuId());
                mContext.startActivity(userProfileIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(userList != null) return userList.size();
        else return 0;
    }

    public void notify(List<User>userList){
        this.userList = userList;
        this.notifyDataSetChanged();
    }

//    public void setProfileId(String profileId){
//        this.profileId = profileId;
//    }

    public static  class UserListViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        ImageView onlineIcon;
        TextView name;
        TextView status;
        public UserListViewHolder(View itemView) {
            super(itemView);
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
            }else {
                onlineIcon.setVisibility(View.GONE);
            }
        }
    }
}
