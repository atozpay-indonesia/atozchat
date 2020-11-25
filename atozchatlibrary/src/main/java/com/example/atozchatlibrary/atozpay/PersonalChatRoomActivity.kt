package com.example.atozchatlibrary.atozpay

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.AtozChat.*
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.atozpay.model.Chat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_chat_room_personal.*
import java.util.*
import kotlin.collections.HashMap

class PersonalChatRoomActivity : AppCompatActivity() {
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val chatList: MutableList<Chat> = ArrayList()
    private val chatListAdapter = ChatListAdapter(chatList)

    private var chatSessionId: String = "chat"

    private var chatRoomName: String? = null
    private var senderUserId: String? = null
    private var senderUserName: String? = null
    private var recipientUserId: String? = null
    private var recipientUserName: String? = null
    private var chatSnippet: String? = null

    private var isNewSession = false;

    companion object {
        private const val PERSONAL_CHAT_TYPE_OUTGOING = 1
        private const val PERSONAL_CHAT_TYPE_INCOMING = 2
        private const val CHAT_SNIPPET_LENGTH = 50
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
        setupDocumentReference()

        button_send.setOnClickListener {
            if (et_chat_message.text.isNotBlank()){
                sendMessage(et_chat_message.text.toString())
                et_chat_message.setText("")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserStatus(true)
    }

    override fun onPause() {
        super.onPause()
        updateUserStatus(false)
    }

    private fun setupDocumentReference() {
        senderUserId = intent.getStringExtra(INTENT_NAME_SENDER_USER_ID)
        senderUserName = intent.getStringExtra(INTENT_NAME_SENDER_USER_NAME)
        recipientUserId = intent.getStringExtra(INTENT_NAME_RECIPIENT_USER_ID)
        recipientUserName = intent.getStringExtra(INTENT_NAME_RECIPIENT_USER_NAME)
        chatRoomName = intent.getStringExtra(INTENT_NAME_PERSONAL_ROOM_NAME)

        val docRef = db.collection("messaging").document(chatRoomName!!)
        docRef.get().addOnSuccessListener {document ->
            if (document.getString("first_user_name") != null){
                chatSnippet = document.getString("last_chat")
                updateUserStatus(true)
                isNewSession = false
                chatSessionId = generateChatId(document.getTimestamp("session_start_at")!!)
                setupChatSnapshotListener(docRef)
            } else {
                chatSnippet = "-"
                isNewSession = true
            }
        }.addOnFailureListener {
            chatSnippet = "-"
        }
    }

    private fun setupChatSnapshotListener(docRef: DocumentReference) {
        val collectionReference = docRef
            .collection(chatSessionId)
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

        chatSnippet = message
        if (chatSnippet!!.length > CHAT_SNIPPET_LENGTH){
            chatSnippet = chatSnippet!!.substring(0, CHAT_SNIPPET_LENGTH)
            chatSnippet = chatSnippet.plus("...")
        }

        var room: HashMap<String, Any?>? = null

        if (isNewSession){
            room = hashMapOf(
                "second_user_id" to recipientUserId,
                "second_user_name" to recipientUserName,
                "first_user_id" to senderUserId,
                "first_user_name" to senderUserName,
                "session_status" to true,
                "is_new_chat_available" to true,
                "last_update" to FieldValue.serverTimestamp(),
                "last_chat" to chatSnippet,
                "is_customer_online" to true,
                "session_start_at" to FieldValue.serverTimestamp(),
                "session_end_at" to null
            )

            db.collection("messaging")
                .document(chatRoomName!!)
                .set(room)
                .addOnSuccessListener {

                    // add new chat
                    addNewChat(chat, true)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

        } else {
            room = hashMapOf(
                "is_new_chat_available" to true,
                "last_update" to FieldValue.serverTimestamp(),
                "last_chat" to chatSnippet,
                "is_customer_online" to true
            )

            db.collection("messaging")
                .document(chatRoomName!!)
                .update(room)
                .addOnSuccessListener {
                    addNewChat(chat, false)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }

    }

    private fun addNewChat(chat: Chat, isNew: Boolean) {

        val docRef = db.collection("messaging").document(chatRoomName!!)
        docRef.get()
            .addOnSuccessListener { document ->
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = document.getTimestamp("session_start_at")!!.seconds * 1000L
                val date = DateFormat.format("ddMMyyyyHHmmss", calendar1).toString()
                Log.d(TAG, "session start at: $date")

                chatSessionId = generateChatId(document.getTimestamp("session_start_at")!!)

                val room = hashMapOf(
                    "current_chats_id" to chatSessionId
                )

                db.collection("messaging")
                    .document(chatRoomName!!)
                    .update(room as Map<String, Any>)
                    .addOnSuccessListener {
                        docRef.collection(chatSessionId)
                            .add(chat.toMap()!!)
                            .addOnSuccessListener {
                                // what happen if new chat insertion succeeded
                                if (isNew){
                                    isNewSession = false
                                    setupChatSnapshotListener(docRef)
                                }
                            }
                            .addOnFailureListener {
                                // what happen if new chat insertion failed
                            }
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
            .addOnFailureListener {

            }
    }

    private fun generateChatId(timestamp: Timestamp): String {
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis = timestamp.seconds * 1000L
        val date = DateFormat.format("ddMMyyyyHHmmss", calendar1).toString()
        Log.d(TAG, "session start at: $date")

        return "chat-$date"
    }

    private fun updateUserStatus(isOnline: Boolean) {
        val room = hashMapOf(
            "is_customer_online" to isOnline
        )

        db.collection("messaging")
            .document(chatRoomName!!)
            .update(room as Map<String, Any>)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    private fun initializeNewChatSession(docRef: DocumentReference) {
        isNewSession = true

        val collectionReference = docRef
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
}