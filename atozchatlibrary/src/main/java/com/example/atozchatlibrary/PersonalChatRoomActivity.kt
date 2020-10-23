package com.example.atozchatlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.model.Chat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class PersonalChatRoomActivity : AppCompatActivity() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val chatList: MutableList<Chat> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)
    }
}