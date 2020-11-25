package com.example.atozchat

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table

@Table(name = "chat_room")
data class ChatRoom(
    @Column(name = "room_name", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    val roomName: String? = null,

    @Column(name = "sender_id")
    val senderId: String? = null,

    @Column(name = "sender_name")
    val senderName: String? = null,

    @Column(name = "recipient_id")
    val recipientId: String? = null,

    @Column(name = "recipient_name")
    val recipientName: String? = null,
) : Model()