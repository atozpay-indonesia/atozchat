package com.example.atozchatlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Chat(
    private var senderId: Int? = 0,
    private var senderName: String? = null,
    private var recipientId: Int? = 0,
    private var recipientName: String? = null,
    private var body: String? = null,
    @ServerTimestamp
    private var timeSent: Timestamp? = null
)