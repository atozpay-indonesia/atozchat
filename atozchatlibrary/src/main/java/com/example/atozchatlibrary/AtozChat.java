package com.example.atozchatlibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.atozchatlibrary.atozpay.activities.CustomerSupportChatActivity;

public class AtozChat {
    public static final String TAG = "AtozChat";
    static final boolean localLOGV = false;

    static AtozChat atozChat;
    Context mContext;

    String personalRoomName;
    String personalSenderUserId;
    String personalSenderUserName;
    String personalRecipientUserId;
    String personalRecipientUserName;

    /**
     * Intent Extra for personal room name
     */
    public static final String INTENT_NAME_PERSONAL_ROOM_NAME = "PERSONAL_ROOM_NAME";

    /**
     * Intent Extra for sender user id
     */
    public static final String INTENT_NAME_SENDER_USER_ID = "SENDER_USER_ID";

    /**
     * Intent Extra for sender user name
     */
    public static final String INTENT_NAME_SENDER_USER_NAME = "SENDER_USER_NAME";

    /**
     * Intent Extra for recipient user id
     */
    public static final String INTENT_NAME_RECIPIENT_USER_ID = "RECIPIENT_USER_ID";

    /**
     * Intent Extra for recipient user name
     */
    public static final String INTENT_NAME_RECIPIENT_USER_NAME = "RECIPIENT_USER_NAME";

    public AtozChat(Context context){
        mContext = context;
    }

    /**
     * Open private chat room
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     *
     */
    public static AtozChat openPersonalRoom(Context context) {
        if (localLOGV) Log.v(TAG, "openPersonalRoom: initiated");
        atozChat = new AtozChat(context);
        return atozChat;
    }

    public AtozChat setRoomName(String roomName){
        personalRoomName = roomName;
        return atozChat;
    }

    public AtozChat setSenderUserId(String senderId){
        personalSenderUserId = senderId;
        return atozChat;
    }

    public AtozChat setSenderUserName(String senderName){
        personalSenderUserName = senderName;
        return atozChat;
    }

    public AtozChat setRecipientUserId(String recipientId){
        personalRecipientUserId = recipientId;
        return atozChat;
    }

    public AtozChat setRecipientUserName(String recipientName){
        personalRecipientUserName = recipientName;
        return atozChat;
    }

    public void startChat(){
        Intent i = new Intent(mContext, CustomerSupportChatActivity.class);
        i.putExtra(INTENT_NAME_PERSONAL_ROOM_NAME, personalRoomName);
        i.putExtra(INTENT_NAME_SENDER_USER_ID, personalSenderUserId);
        i.putExtra(INTENT_NAME_SENDER_USER_NAME, personalSenderUserName);
        i.putExtra(INTENT_NAME_RECIPIENT_USER_ID, personalRecipientUserId);
        i.putExtra(INTENT_NAME_RECIPIENT_USER_NAME, personalRecipientUserName);
        mContext.startActivity(i);
    }
}
