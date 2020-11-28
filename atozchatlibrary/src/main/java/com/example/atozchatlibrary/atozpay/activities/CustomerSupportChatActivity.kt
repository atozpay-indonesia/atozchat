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
    private var firstUserId: String? = null
    private var firstUserName: String? = null
    private var secondUserId: String? = null
    private var secondUserName: String? = null

    private var senderUserId: String? = null
    private var senderUserName: String? = null
    private var recipientUserId: String? = ""
    private var recipientUserName: String? = ""
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
        firstUserId = senderUserId
        firstUserName = senderUserName
        chatRoomName = "$senderUserId"

        val docRef = db.collection(COLLECTION_ROOT).document(chatRoomName!!)
        docRef.get().addOnSuccessListener {document ->
            if (document.getString(ROOM_DOC_FIELD_NAME_FIRST_USER_NAME) != null){
                // resuming session
                chatSnippet = document.getString(ROOM_DOC_FIELD_NAME_LAST_CHAT)
                updateUserStatus(true)
                isNewSession = false
                chatSessionId = generateChatId(document.getTimestamp(
                    ROOM_DOC_FIELD_NAME_SESSION_START_AT)!!)

                //update recipient user id and name (if not null)
                if (document.getString(ROOM_DOC_FIELD_NAME_SECOND_USER_ID) != null){
                    secondUserId = document.getString(ROOM_DOC_FIELD_NAME_SECOND_USER_ID)
                    secondUserName = document.getString(ROOM_DOC_FIELD_NAME_SECOND_USER_NAME)
                } else {
                    // start room field listener
                    setupRoomDocListener(docRef)
                }
                setupChatSnapshotListener(docRef)
            } else {
                // will start a new session
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

    private fun setupRoomDocListener(docRef: DocumentReference){
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun populateChatList(documents: List<DocumentSnapshot>) {
        chatList.clear()
        for (i in documents.indices) {
            // check if sender id match current user id
            // to identify chat type (outgoing or incoming)
            var chatType: Int
            val senderId = documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_ID)
            val currentUserId = senderUserId
            chatType = if (senderId.equals(currentUserId)) {
                PERSONAL_CHAT_TYPE_OUTGOING
            } else {
                PERSONAL_CHAT_TYPE_INCOMING
            }

            val chat = Chat(
                chatType,
                documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_ID),
                documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_NAME),
                documents[i].getString(CHAT_DOC_FIELD_NAME_RECIPIENT_ID),
                documents[i].getString(CHAT_DOC_FIELD_NAME_RECIPIENT_NAME),
                documents[i].getString(CHAT_DOC_FIELD_NAME_CHAT_BODY),
                documents[i].getTimestamp(CHAT_DOC_FIELD_NAME_TIME_SENT)
            )
            chatList.add(chat)
        }
        chatListAdapter.notifyDataSetChanged()
    }

    private fun sendMessage(message: String){
        if (recipientUserName == null){
            recipientUserName = ""
        }
        val chat = Chat(
            null,
            senderUserId,
            senderUserName,
            recipientUserId,
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
                ROOM_DOC_FIELD_NAME_SECOND_USER_ID to secondUserId,
                ROOM_DOC_FIELD_NAME_SECOND_USER_NAME to secondUserName,
                ROOM_DOC_FIELD_NAME_FIRST_USER_ID to firstUserId,
                ROOM_DOC_FIELD_NAME_FIRST_USER_NAME to firstUserName,
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