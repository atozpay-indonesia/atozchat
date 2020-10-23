package com.example.atozchatlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.model.Chat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_chat_room_personal.*
import java.util.ArrayList

class PersonalChatRoomActivity : AppCompatActivity() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val chatList: MutableList<Chat> = ArrayList()
    private val chatListAdapter = ChatListAdapter(chatList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)

        rv_chat.apply {
            layoutManager = LinearLayoutManager(this@PersonalChatRoomActivity, RecyclerView.VERTICAL, false)
            adapter = chatListAdapter
        }
        setupFirestoreData()
    }

    private fun setupFirestoreData() {
        val collectionReference = db.collection("messaging")
            .document("chat-123-456")
            .collection("chat")
        collectionReference.orderBy("time_sent", Query.Direction.ASCENDING)
            .addSnapshotListener(EventListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    return@EventListener
                }

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                    populateChatList(queryDocumentSnapshots.documents)
                }
            })
    }

    private fun populateChatList(documents: List<DocumentSnapshot>) {
        chatList.clear()
        for (i in documents.indices) {
            // check if sender id match current user id
            // to identify chat type (outgoing or incoming)
            var chatType = documents[i].getLong("sender_id")?.toInt()
            val userId = 123 // for development purposes only
            chatType = if (chatType == userId) {
                1
            } else {
                2
            }

            val chat = Chat(
                chatType,
                documents[i].getLong("sender_id")?.toInt(),
                documents[i].getString("sender_name"),
                documents[i].getLong("recipient_id")?.toInt(),
                documents[i].getString("recipient_name"),
                documents[i].getString("chat_body"),
                documents[i].getTimestamp("time_sent")
            )
            chatList.add(chat)
        }
        chatListAdapter.notifyDataSetChanged()
    }
}