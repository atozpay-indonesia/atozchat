package com.example.atozchatlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Chat(
    val type: Int? = 0,
    val senderId: Int? = 0,
    val senderName: String? = null,
    val recipientId: Int? = 0,
    val recipientName: String? = null,
    val body: String? = null,
    @ServerTimestamp
    val timeSent: Timestamp? = null
)