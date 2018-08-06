package com.ibra.chatappdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.helper.TimeAgo;
import com.ibra.chatappdemo.model.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private Context context;
    private ArrayList<Message> messages;
    private String currentId;
    private String currentUserImage;
    private String name;
    private String friendImage;
    private String messageImage;


    public MessageAdapter(Context context,ArrayList<Message>messages) {
        this.context = context;
        currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.messages = messages;
    }





    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("frommessageadapter","onCreateViewHolder");
        if(viewType == 1)return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.message_list_item_user, parent, false));
        else if(viewType == 2) return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.message_list_item_friend, parent, false));
        else if(viewType == 3) return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.message_list_item_user_image, parent, false));
        else if(viewType == 4) return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.message_list_item_friend_image, parent, false));
        else return null;


    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.bind(position);

    }

    public void notifyAdapter(ArrayList<Message>messages){
       this.messages = messages;
       this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        Log.d("frommessageadapter","getItemCount");
        if(messages != null) return messages.size();
        else return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("frommessageadapter","getItemViewType");
        String from_user_id = messages.get(position).getFrom();
        if(from_user_id != null){
            if(from_user_id.equals(currentId)){
                if(messages.get(position).getType().equals("text"))return 1; // user_text
                else return 3; // user_image
            }else{
                if(messages.get(position).getType().equals("text")) return 2; // friend_text
                else return 4; //friend_image
            }
        }else return 0;


    }

    public class MessageHolder extends RecyclerView.ViewHolder{

        TextView content;
        CircleImageView image;
        TextView nameTxt,timeTxt;
        ImageView messageImageview;

        public MessageHolder(View itemView) {
            super(itemView);
            content = (TextView)itemView.findViewById(R.id.message_content);
            image = (CircleImageView) itemView.findViewById(R.id.image_message);
            nameTxt = (TextView) itemView.findViewById(R.id.text_message_name);
            timeTxt = (TextView) itemView.findViewById(R.id.text_message_time);
            messageImageview = (ImageView) itemView.findViewById(R.id.chat_message_image);

        }


        public void bind(int position){
            Log.d("frommessageadapter","bind");
            String from_user_id = messages.get(position).getFrom();
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.users_table)).child(from_user_id);
            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null){
                        currentUserImage = dataSnapshot.child(context.getString(R.string.thumb_image_key)).getValue().toString();
                        name = dataSnapshot.child(context.getString(R.string.username_key)).getValue().toString();
                        nameTxt.setText(name);
                        Log.d("fromAdapter","current is "+name);
                        if(currentUserImage != null){
                            Picasso.get().load(currentUserImage).placeholder(R.drawable.thumb_default_image).into(image);
                        }
                    } }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


            String messageTime = TimeAgo.getTimeAgo(Long.parseLong(messages.get(position).getTime()));
            timeTxt.setText(messageTime);
            if(messages.get(position).getType().equals("text")) {
                content.setText(messages.get(position).getMessage());
            }else{
                Picasso.get().load(messages.get(position).getMessage()).placeholder(R.drawable.thumb_default_image).into(messageImageview);

            }




        }
    }
}
