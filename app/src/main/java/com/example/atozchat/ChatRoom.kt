package com.example.atozchat

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table

@Table(name = "chat_room")
data class ChatRoom(

    @Column(name = "sender_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    val senderId: Int? = null,

    @Column(name = "sender_name")
    val senderName: String? = null
) : Model()