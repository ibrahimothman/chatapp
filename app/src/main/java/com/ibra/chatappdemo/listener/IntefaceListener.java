package com.ibra.chatappdemo.listener;

public class IntefaceListener {

    public static interface acceptFriendRequest{
        void onAcceptRequest(String currentId, String friendId);
    }

    public static interface declineFriendRequest{
        void onDeclineRequest(String currentId, String friendId);
    }
}
