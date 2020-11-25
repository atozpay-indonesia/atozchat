package com.example.atozchatlibrary.atozpay.activities

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.AtozChat.*
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.atozpay.model.Chat
import com.example.atozchatlibrary.atozpay.recyclerviewadapter.ChatListAdapter
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_CHAT_BODY
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_RECIPIENT_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_RECIPIENT_NAME
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_SENDER_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_SENDER_NAME
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_TIME_SENT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_SNIPPET_LENGTH
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.COLLECTION_ROOT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.PERSONAL_CHAT_TYPE_INCOMING
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.PERSONAL_CHAT_TYPE_OUTGOING
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_CURRENT_CHATS_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_FIRST_USER_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_FIRST_USER_NAME
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_IS_NEW_CHAT_AVAILABLE
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_LAST_CHAT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_LAST_UPDATE
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_SECOND_USER_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_SECOND_USER_NAME
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_SESSION_END_AT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_SESSION_START_AT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_NAME_SESSION_STATUS
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_chat_room_personal.*
import java.util.*
import kotlin.collections.HashMap

class CustomerSupportChatActivity : AppCompatActivity() {
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

    private var isNewSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)

        rv_chat.apply {
            layoutManager = LinearLayoutManager(
                this@CustomerSupportChatActivity,
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

        val docRef = db.collection(COLLECTION_ROOT).document(chatRoomName!!)
        docRef.get().addOnSuccessListener {document ->
            if (document.getString(ROOM_DOC_FIELD_NAME_FIRST_USER_NAME) != null){
                chatSnippet = document.getString(ROOM_DOC_FIELD_NAME_LAST_CHAT)
                updateUserStatus(true)
                isNewSession = false
                chatSessionId = generateChatId(document.getTimestamp(
                    ROOM_DOC_FIELD_NAME_SESSION_START_AT)!!)
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
        collectionReference.orderBy(CHAT_DOC_FIELD_NAME_TIME_SENT, Query.Direction.ASCENDING)
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
            val senderId = documents[i].getLong(CHAT_DOC_FIELD_NAME_SENDER_ID)?.toInt()
            val currentUserIdInt = senderUserId!!.toInt()
            chatType = if (senderId == currentUserIdInt) {
                PERSONAL_CHAT_TYPE_OUTGOING
            } else {
                PERSONAL_CHAT_TYPE_INCOMING
            }

            val chat = Chat(
                chatType,
                documents[i].getLong(CHAT_DOC_FIELD_NAME_SENDER_ID)?.toInt(),
                documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_NAME),
                documents[i].getLong(CHAT_DOC_FIELD_NAME_RECIPIENT_ID)?.toInt(),
                documents[i].getString(CHAT_DOC_FIELD_NAME_RECIPIENT_NAME),
                documents[i].getString(CHAT_DOC_FIELD_NAME_CHAT_BODY),
                documents[i].getTimestamp(CHAT_DOC_FIELD_NAME_TIME_SENT)
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

        val room: HashMap<String, Any?>?

        if (isNewSession){
            room = hashMapOf(
                ROOM_DOC_FIELD_NAME_SECOND_USER_ID to recipientUserId,
                ROOM_DOC_FIELD_NAME_SECOND_USER_NAME to recipientUserName,
                ROOM_DOC_FIELD_NAME_FIRST_USER_ID to senderUserId,
                ROOM_DOC_FIELD_NAME_FIRST_USER_NAME to senderUserName,
                ROOM_DOC_FIELD_NAME_SESSION_STATUS to true,
                ROOM_DOC_FIELD_NAME_IS_NEW_CHAT_AVAILABLE to true,
                ROOM_DOC_FIELD_NAME_LAST_UPDATE to FieldValue.serverTimestamp(),
                ROOM_DOC_FIELD_NAME_LAST_CHAT to chatSnippet,
                ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to true,
                ROOM_DOC_FIELD_NAME_SESSION_START_AT to FieldValue.serverTimestamp(),
                ROOM_DOC_FIELD_NAME_SESSION_END_AT to null
            )

            db.collection(COLLECTION_ROOT)
                .document(chatRoomName!!)
                .set(room)
                .addOnSuccessListener {

                    // add new chat
                    addNewChat(chat, true)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

        } else {
            room = hashMapOf(
                ROOM_DOC_FIELD_NAME_IS_NEW_CHAT_AVAILABLE to true,
                ROOM_DOC_FIELD_NAME_LAST_UPDATE to FieldValue.serverTimestamp(),
                ROOM_DOC_FIELD_NAME_LAST_CHAT to chatSnippet,
                ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to true
            )

            db.collection(COLLECTION_ROOT)
                .document(chatRoomName!!)
                .update(room)
                .addOnSuccessListener {
                    addNewChat(chat, false)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }

    }

    private fun addNewChat(chat: Chat, isNew: Boolean) {

        val docRef = db.collection(COLLECTION_ROOT).document(chatRoomName!!)
        docRef.get()
            .addOnSuccessListener { document ->
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = document.getTimestamp(ROOM_DOC_FIELD_NAME_SESSION_START_AT)!!.seconds * 1000L
                val date = DateFormat.format("ddMMyyyyHHmmss", calendar1).toString()
                Log.d(TAG, "session start at: $date")

                chatSessionId = generateChatId(document.getTimestamp(
                    ROOM_DOC_FIELD_NAME_SESSION_START_AT)!!)

                val room = hashMapOf(
                    ROOM_DOC_FIELD_NAME_CURRENT_CHATS_ID to chatSessionId
                )

                db.collection(COLLECTION_ROOT)
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
            ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to isOnline
        )

        db.collection(COLLECTION_ROOT)
            .document(chatRoomName!!)
            .update(room as Map<String, Any>)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}