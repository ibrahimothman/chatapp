package com.ibra.chatappdemo.adapter;

import android.content.Context;
import android.renderscript.ScriptIntrinsicYuvToRGB;
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
import com.ibra.chatappdemo.model.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private Context context;
    private ArrayList<Message> messages;
    private String currentId;
    private String currentUserImage;
    private String friendImage;
    private String messageImage;

    public MessageAdapter(Context context) {
        this.context = context;
        currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }





    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.message_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.bind(position);

    }

    public void notifyAdapter(ArrayList<Message>messages){
        if(this.messages != messages){
            this.messages = messages;
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if(messages != null) return messages.size();
        else return 0;
    }

    public class MessageHolder extends RecyclerView.ViewHolder{

        TextView content;
        CircleImageView image;

        public MessageHolder(View itemView) {
            super(itemView);
            content = (TextView)itemView.findViewById(R.id.message_content);
            image = (CircleImageView) itemView.findViewById(R.id.image_message);

        }


        public void bind(int position){

            Log.d("fromAdapter","bind");
            String from_user_id = messages.get(position).getFrom();



            if(from_user_id.equals(currentId)){
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentId);
                currentUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot != null){
                            currentUserImage = dataSnapshot.child(context.getString(R.string.thumb_image_key)).getValue().toString();
                            Log.d("fromAdapter","current is "+currentUserImage);
                            if(currentUserImage != null){
                                Picasso.get().load(currentUserImage).placeholder(R.drawable.thumb_default_image).into(image); }
//                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            else{
                DatabaseReference friendUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(from_user_id);
                friendUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot != null){
                            friendImage = dataSnapshot.child(context.getString(R.string.thumb_image_key)).getValue().toString();
                            Log.d("fromAdapter","friend is "+friendImage);
                            if(friendImage != null){
                                Picasso.get().load(friendImage).placeholder(R.drawable.thumb_default_image).into(image);
                            }
//                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }



            content.setText(messages.get(position).getMessage());




        }
    }
}
