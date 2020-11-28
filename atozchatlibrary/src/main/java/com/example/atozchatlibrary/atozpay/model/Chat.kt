package com.example.atozchatlibrary.atozpay.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Chat(
    /**
     * to determine the chat type whether incoming or outgoing chat
     * 1 for outgoing chat
     * 2 for incoming chat
     */
    val type: Int? = 0,

    val senderId: String? = null,
    val senderName: String? = null,
    val recipientId: String? = null,
    val recipientName: String? = null,
    val body: String? = null,
    @ServerTimestamp
    val timeSent: Timestamp? = null
) {
    @Exclude
    fun toMap(): Map<String, Any>? {
        val result = HashMap<String, Any>()
        result["chat_body"] = body!!
        result["recipient_id"] = recipientId!!
        result["recipient_name"] = recipientName!!
        result["sender_id"] = senderId!!
        result["sender_name"] = senderName!!
        result["time_sent"] = FieldValue.serverTimestamp()
        return result
    }
}