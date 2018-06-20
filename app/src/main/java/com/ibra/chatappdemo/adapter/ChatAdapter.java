//package com.ibra.chatappdemo.adapter;
//
//import android.appwidget.AppWidgetManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.ibra.chatappdemo.R;
//import com.ibra.chatappdemo.model.Chat;
//import com.ibra.chatappdemo.model.Message;
//import com.ibra.chatappdemo.ui.AllusersActivity;
//import com.ibra.chatappdemo.ui.ChatActivity;
//import com.ibra.chatappdemo.widget.ChatWidget;
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
//
//
//    private ArrayList<Message> chatList;
//    private Context context;
//    FirebaseUser firebaseUser;
//    String uid;
//
//
//
//    public ChatAdapter(ArrayList<Message> chatList, Context context) {
//        this.chatList = chatList;
//        this.context = context;
//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null) uid = firebaseUser.getUid();
//    }
//
//    @NonNull
//    @Override
//    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new ChatHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item,parent,false));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {
//
//        final String from_user = chatList.get(position).getFrom();
//        String to_user = chatList.get(position).getTo();
//        String message = chatList.get(position).getMessage();
//        String seen = chatList.get(position).getSeen();
//
//        String target ="";
//        if(from_user.equals(uid)) target = to_user;
//        else if(to_user.equals(uid)) target = from_user;
//
//
//        FirebaseDatabase.getInstance().getReference().child("users").child(target)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        String name = dataSnapshot.child(context.getString(R.string.username_key)).getValue().toString();
//                        String image = dataSnapshot.child(context.getString(R.string.thumb_image_key)).getValue().toString();
//                        if(dataSnapshot.hasChild("online")){
//                            String onlineState = dataSnapshot.child("online").getValue().toString();
//                            holder.setOnlineIcon(onlineState);
//                        }
//                        holder.setName(name);
//                        holder.setImage(image);
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//        holder.setMessage(message,seen);
//
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(context, ChatActivity.class);
//                if(from_user != null) {
//                    intent.putExtra(AllusersActivity.USER_ID_EXTRA, from_user);
//                }
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return chatList.size();
//    }
//
//    class ChatHolder extends RecyclerView.ViewHolder {
//
//        CircleImageView friendImage;
//        TextView messageTxt;
//        TextView friendName;
//        ImageView onlineIcon;
//
//        public ChatHolder(View itemView) {
//            super(itemView);
//            messageTxt = (TextView)itemView.findViewById(R.id.user_status_list);
//            friendName = (TextView)itemView.findViewById(R.id.user_name_list);
//            friendImage = (CircleImageView)itemView.findViewById(R.id.user_image_list);
//            onlineIcon = (ImageView) itemView.findViewById(R.id.online_icon);
//
//        }
//
//
//
//        public void setMessage(String message,String isSeen) {
//            if (isSeen.equals("true")){
//                messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.NORMAL);
//            }else  messageTxt.setTypeface(messageTxt.getTypeface(),Typeface.BOLD);
//            messageTxt.setText(message);
//        }
//
//        public void setName(String name) {
//            friendName.setText(name);
//        }
//
//        public void setImage(String image) {
//            Picasso.get().load(image).placeholder(R.drawable.thumb_default_image).into(friendImage);
//        }
//
//        public void setOnlineIcon(String onlineState) {
//            if(onlineState.equals("true")){
//                onlineIcon.setVisibility(View.VISIBLE);
//            }else onlineIcon.setVisibility(View.INVISIBLE);
//        }
//
//
//    }
//}
