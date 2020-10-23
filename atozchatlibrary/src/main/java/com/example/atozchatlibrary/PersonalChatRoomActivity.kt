package com.example.atozchatlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.google.firebase.firestore.FirebaseFirestore

class PersonalChatRoomActivity : AppCompatActivity() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)
    }
}