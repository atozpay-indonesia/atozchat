package com.example.atozchatlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.AtozChat.*
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.model.Chat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_chat_room_personal.*
import java.util.*

class PersonalChatRoomActivity : AppCompatActivity() {
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val chatList: MutableList<Chat> = ArrayList()
    private val chatListAdapter = ChatListAdapter(chatList)

    private var chatRoomName: String? = null
    private var senderUserId: String? = null
    private var senderUserName: String? = null
    private var recipientUserId: String? = null
    private var recipientUserName: String? = null

    companion object {
        private const val PERSONAL_CHAT_TYPE_OUTGOING = 1
        private const val PERSONAL_CHAT_TYPE_INCOMING = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)

        rv_chat.apply {
            layoutManager = LinearLayoutManager(
                this@PersonalChatRoomActivity,
                RecyclerView.VERTICAL,
                false
            )
            adapter = chatListAdapter
        }
        setupFirestoreData()

        button_send.setOnClickListener {
            if (et_chat_message.text.isNotBlank()){
                sendMessage(et_chat_message.text.toString())
                et_chat_message.setText("")
            }
        }
    }

    private fun setupFirestoreData() {
        senderUserId = intent.getStringExtra(INTENT_NAME_SENDER_USER_ID)
        senderUserName = intent.getStringExtra(INTENT_NAME_SENDER_USER_NAME)
        recipientUserId = intent.getStringExtra(INTENT_NAME_RECIPIENT_USER_ID)
        recipientUserName = intent.getStringExtra(INTENT_NAME_RECIPIENT_USER_NAME)

        chatRoomName = intent.getStringExtra(INTENT_NAME_PERSONAL_ROOM_NAME)
        val collectionReference = db.collection("messaging")
            .document(chatRoomName!!)
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
            var chatType: Int
            val senderId = documents[i].getLong("sender_id")?.toInt()
            val currentUserIdInt = senderUserId!!.toInt()
            chatType = if (senderId == currentUserIdInt) {
                PERSONAL_CHAT_TYPE_OUTGOING
            } else {
                PERSONAL_CHAT_TYPE_INCOMING
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

    private fun sendMessage(message: String){
        val chat = Chat(
            null,
            senderUserId!!.toInt(),
            senderUserName,
            recipientUserId!!.toInt(),
            recipientUserName,
            message,
            null
        )

        db.collection("messaging")
            .document(chatRoomName!!)
            .collection("chat")
            .add(chat.toMap()!!)
            .addOnSuccessListener {
                // what happen if new chat insertion succeeded
            }
            .addOnFailureListener {
                // what happen if new chat insertion failed
            }

    }
}