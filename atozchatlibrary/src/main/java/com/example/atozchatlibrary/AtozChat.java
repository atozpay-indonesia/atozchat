package com.example.atozchatlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.atozchatlibrary.atozpay.activities.CustomerSupportChatActivity;

public class AtozChat {
    public static final String TAG = "AtozChat";
    static final boolean localLOGV = false;

    @SuppressLint("StaticFieldLeak")
    static AtozChat atozChat;
    Context mContext;
    String activityTitle;
    String personalSenderUserId;
    String personalSenderUserName;
    String personalSenderFcmToken;

    /**
     * Intent Extra for activity/session title
     */
    public static final String INTENT_NAME_ACTIVITY_TITLE = "ACTIVITY_TITLE";

    /**
     * Intent Extra for sender user id
     */
    public static final String INTENT_NAME_SENDER_USER_ID = "SENDER_USER_ID";

    /**
     * Intent Extra for sender user name
     */
    public static final String INTENT_NAME_SENDER_USER_NAME = "SENDER_USER_NAME";

    /**
     * Intent Extra for sender FCM Token
     */
    public static final String INTENT_NAME_SENDER_FCM_TOKEN = "SENDER_FCM_TOKEN";

    public AtozChat(Context context) {
        mContext = context;
    }

    /**
     * Open atozpay (chat with Customer Support) chat room
     *
     * @param context The context to use.  Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     */
    public static AtozChat initChatAtozpay(Context context) {
        if (localLOGV) Log.v(TAG, "initChatAtozpay: initiated");
        atozChat = new AtozChat(context);
        return atozChat;
    }

    public AtozChat setActivityTitle(String title) {
        activityTitle = title;
        return atozChat;
    }

    public AtozChat setSenderUserId(String senderId) {
        personalSenderUserId = senderId;
        return atozChat;
    }

    public AtozChat setSenderUserName(String senderName) {
        personalSenderUserName = senderName;
        return atozChat;
    }

    public AtozChat setFcmToken(String fcmToken) {
        personalSenderFcmToken = fcmToken;
        return atozChat;
    }

    public void startChat() {
        Intent i = new Intent(mContext, CustomerSupportChatActivity.class);
        i.putExtra(INTENT_NAME_ACTIVITY_TITLE, activityTitle);
        i.putExtra(INTENT_NAME_SENDER_USER_ID, personalSenderUserId);
        i.putExtra(INTENT_NAME_SENDER_USER_NAME, personalSenderUserName);
        i.putExtra(INTENT_NAME_SENDER_FCM_TOKEN, personalSenderFcmToken);
        mContext.startActivity(i);
    }
}
