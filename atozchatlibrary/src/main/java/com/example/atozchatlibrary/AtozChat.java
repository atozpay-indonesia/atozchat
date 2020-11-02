package com.example.atozchatlibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AtozChat {
    static final String TAG = "AtozChat";
    static final boolean localLOGV = false;

    /**
     * Intent Extra for first user id. Used in {@link #openPrivateRoom(Context, String, String)}
     */
    public static final String INTENT_NAME_FIRST_USER_ID = "FIRST_USER_ID";

    /**
     * Intent Extra for second user id. Used in {@link #openPrivateRoom(Context, String, String)}
     */
    public static final String INTENT_NAME_SECOND_USER_ID = "SECOND_USER_ID";

    /**
     * Open private chat room
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param firstUserId   First user ID. Will be used for constructing chat room name.
     * @param secondUserId  Second user ID. Will be used for constructing chat room name
     *
     */
    public static void openPrivateRoom(Context context, String firstUserId, String secondUserId) {
        if (localLOGV) Log.v(TAG, "openPrivateRoom: initiated");
        Intent i = new Intent(context, PersonalChatRoomActivity.class);
        i.putExtra(INTENT_NAME_FIRST_USER_ID, firstUserId);
        i.putExtra(INTENT_NAME_SECOND_USER_ID, secondUserId);
        context.startActivity(i);
    }
}
