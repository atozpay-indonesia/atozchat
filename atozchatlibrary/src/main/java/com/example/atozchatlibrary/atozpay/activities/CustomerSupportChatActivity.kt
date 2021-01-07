package com.example.atozchatlibrary.atozpay.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.atozchatlibrary.AtozChat.*
import com.example.atozchatlibrary.R.layout.activity_chat_room_personal
import com.example.atozchatlibrary.atozpay.model.Chat
import com.example.atozchatlibrary.atozpay.recyclerviewadapter.ChatListAdapter
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_AUTO_GENERATED
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_CHAT_BODY
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_SENDER_ID
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_SENDER_NAME
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_DOC_FIELD_NAME_TIME_SENT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.CHAT_SNIPPET_LENGTH
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.COLLECTION_ROOT_ROOM
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.COLLECTION_ROOT_CS
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.COLLECTION_ROOT_OPENING_MESSAGES
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.COLLECTION_ROOT_ROOM_CHAT
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.PERSONAL_CHAT_TYPE_INCOMING
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.PERSONAL_CHAT_TYPE_INCOMING_LOADING
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.PERSONAL_CHAT_TYPE_OUTGOING
import com.example.atozchatlibrary.atozpay.utilities.Constants.Companion.ROOM_DOC_FIELD_LAST_CHAT_SENDER_ID
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
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_chat_room_personal.*
import java.util.*


class CustomerSupportChatActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var rootQuery: CollectionReference
    private val chatList: MutableList<Chat> = ArrayList()
    private val chatListAdapter = ChatListAdapter(chatList, this)
    private var chatRoomName: String? = null
    private var firstUserId: String? = null
    private var firstUserName: String? = null
    private var secondUserId: String? = null
    private var secondUserName: String? = null
    private var senderUserId: String? = null
    private var senderUserName: String? = null
    private var chatSnippet: String? = null
    private var isNewSession = true
    private var isSessionJustEnded = false
    private var roomListSnapshotListener: ListenerRegistration? = null
    private var chatListSnapshotListener: ListenerRegistration? = null
    private var roomFieldFirstUserId: String? = null
    private var roomFieldFirstUserName: String? = null
    private var roomFieldIsNewChatAvailable: Boolean = false
    private var roomFieldLastChat: String? = null
    private var roomFieldSecondUserId: String? = null
    private var roomFieldSecondUserName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_chat_room_personal)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        db = FirebaseFirestore.getInstance()
        db.firestoreSettings = settings
        rootQuery = db.collection(COLLECTION_ROOT_ROOM)

        rv_chat.apply {
            layoutManager = LinearLayoutManager(
                this@CustomerSupportChatActivity,
                RecyclerView.VERTICAL,
                false
            )
            adapter = chatListAdapter
        }

        button_send.setOnClickListener {
            proceedSendMessage()
        }

        iv_back.setOnClickListener {
            onBackPressed()
        }

        layout_action_start_session.setOnClickListener {
            setupNewChatSession()
        }
    }

    override fun onResume() {
        super.onResume()
//        showCustomerSupportLoading()
        showCustomerSupportNew()
        showSessionStateNew()
        setupDocumentReference()
    }

    override fun onPause() {
        super.onPause()
        updateUserStatus(false)
        detachSnapshotListener()
    }

    private fun detachSnapshotListener() {
        roomListSnapshotListener?.remove()
        chatListSnapshotListener?.remove()
        roomListSnapshotListener = null
        chatListSnapshotListener = null
    }

    private fun setupDocumentReference() {
        senderUserId = intent.getStringExtra(INTENT_NAME_SENDER_USER_ID)
        senderUserName = intent.getStringExtra(INTENT_NAME_SENDER_USER_NAME)
        firstUserId = senderUserId
        firstUserName = senderUserName
        chatRoomName = "$senderUserId"
        setupRoomDocListener()
    }

    private fun showSessionStateNew() {
        // toolbar
        showCustomerSupportNew()

        // body
        rv_chat.visibility = View.GONE
        layout_body_end_session.visibility = View.GONE
        layout_body_new_session.visibility = View.VISIBLE
        layout_chat.visibility = View.GONE

        isNewSession = true
        chatSnippet = "-"
        chatList.clear()
        chatListAdapter.notifyDataSetChanged()

        detachSnapshotListener()
    }

    private fun showSessionStateLive() {
        // body
        layout_body_new_session.visibility = View.GONE
        layout_body_end_session.visibility = View.GONE
        rv_chat.visibility = View.VISIBLE
        layout_chat.visibility = View.VISIBLE
    }

    private fun showSessionStateEnd() {
        // toolbar
        showCustomerSupportNew()

        // body
        rv_chat.visibility = View.GONE
        layout_body_new_session.visibility = View.GONE
        layout_body_end_session.visibility = View.VISIBLE
        layout_chat.visibility = View.GONE

        chatSnippet = "-"
        isNewSession = true
        isSessionJustEnded = true
        chatList.clear()
        chatListAdapter.notifyDataSetChanged()

        detachSnapshotListener()
    }

    private fun showCustomerSupportData(csId: String?) {
        sfl_cs_loading.stopShimmer()
        db.collection(COLLECTION_ROOT_CS).whereEqualTo("uid", csId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    layout_cs_data_loading.visibility = View.GONE
                    layout_cs_data.visibility = View.VISIBLE
                    layout_state_new_session.visibility = View.GONE
                    tv_cs_name.text = document.getString("alias")
                    Glide.with(this).load(document.getString("img_url")).into(iv_cs_avatar)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun showCustomerSupportLoading() {
        layout_state_new_session.visibility = View.GONE
        layout_cs_data.visibility = View.GONE
        layout_cs_data_loading.visibility = View.VISIBLE
        sfl_cs_loading.startShimmer()
    }

    private fun showCustomerSupportNew() {
        layout_cs_data.visibility = View.GONE
        layout_cs_data_loading.visibility = View.GONE
        layout_state_new_session.visibility = View.VISIBLE
    }

    private fun setupNewChatSession() {
        showSessionStateLive()
        layout_chat.visibility = View.GONE
        showLoadingOpeningMessage()

        // get opening messages from server
        db.collection(COLLECTION_ROOT_OPENING_MESSAGES).orderBy("id", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    sendOpeningMessage(
                        document.getString("message")!!,
                        document.getString("auto_text")!!
                    )
                }
            }
    }

    private fun setupChatSnapshotListener() {
        layout_chat.visibility = View.VISIBLE
        val collectionReference =
            rootQuery.document(chatRoomName!!).collection(COLLECTION_ROOT_ROOM_CHAT)
                .orderBy(CHAT_DOC_FIELD_NAME_TIME_SENT, Query.Direction.ASCENDING)

        chatListSnapshotListener =
            collectionReference.addSnapshotListener(EventListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    return@EventListener
                }

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                    populateChatList(queryDocumentSnapshots.documents)
                }
            })
    }

    private fun setupRoomDocListener() {
        roomListSnapshotListener =
            rootQuery.document(chatRoomName!!).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                    "Local"
                else
                    "Server"

                if (snapshot != null && snapshot.exists()) {
                    // chat room exist
                    showSessionStateLive()
                    Log.d(TAG, "$source data: ${snapshot.data}")

                    chatSnippet = snapshot.getString(ROOM_DOC_FIELD_NAME_LAST_CHAT)
                    updateUserStatus(true)
                    roomFieldFirstUserId = snapshot.getString(ROOM_DOC_FIELD_NAME_FIRST_USER_ID)
                    roomFieldFirstUserName = snapshot.getString(ROOM_DOC_FIELD_NAME_FIRST_USER_NAME)
                    roomFieldIsNewChatAvailable =
                        snapshot.getBoolean(ROOM_DOC_FIELD_NAME_IS_NEW_CHAT_AVAILABLE)!!
                    roomFieldLastChat = snapshot.getString(ROOM_DOC_FIELD_NAME_LAST_CHAT)
                    roomFieldSecondUserId = snapshot.getString(ROOM_DOC_FIELD_NAME_SECOND_USER_ID)
                    roomFieldSecondUserName =
                        snapshot.getString(ROOM_DOC_FIELD_NAME_SECOND_USER_NAME)

                    if (isNewSession) {
                        setupChatSnapshotListener()
                    }

                    isNewSession = false
                    isSessionJustEnded = false

                    if (roomFieldSecondUserId != null && !roomFieldSecondUserId.equals("")) {
                        showCustomerSupportData(roomFieldSecondUserId)
                    } else {
//                        showCustomerSupportLoading()
                    }
                } else {
                    Log.d(TAG, "$source data: null")
                    if (isSessionJustEnded) {
                        showSessionStateEnd()
                    } else {
                        if (!isNewSession) {
                            showSessionStateEnd()
                        } else {
                            showSessionStateNew()
                        }
                    }
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
            chatType = if (senderId == currentUserId) {
                PERSONAL_CHAT_TYPE_OUTGOING
            } else {
                PERSONAL_CHAT_TYPE_INCOMING
            }


            val chat = Chat(
                chatType,
                documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_ID),
                documents[i].getString(CHAT_DOC_FIELD_NAME_SENDER_NAME),
                documents[i].getString(CHAT_DOC_FIELD_NAME_CHAT_BODY),
                documents[i].getBoolean(CHAT_DOC_FIELD_NAME_AUTO_GENERATED)!!,
                documents[i].getTimestamp(CHAT_DOC_FIELD_NAME_TIME_SENT)
            )
            chatList.add(chat)
        }
        chatListAdapter.notifyDataSetChanged()
        rv_chat.smoothScrollToPosition(chatList.size - 1)
    }

    private fun proceedSendMessage() {
        if (et_chat_message.text.isNotBlank()) {
            showSessionStateLive()
            if (roomFieldSecondUserId != null && !roomFieldSecondUserId.equals("")) {
                showCustomerSupportData(roomFieldSecondUserId)
            } else {
//                showCustomerSupportLoading()
            }
            val message = et_chat_message.text.toString()
            et_chat_message.setText("")
            setNewMessageLocally(message)
            sendMessage(message)
        }
    }

    private fun setNewMessageLocally(message: String) {
        val chat = Chat(
            PERSONAL_CHAT_TYPE_OUTGOING,
            senderUserId,
            senderUserName,
            message,
            false,
            null
        )
        chatList.add(chat)
        chatListAdapter.notifyDataSetChanged()
        rv_chat.smoothScrollToPosition(chatList.size - 1)
    }

    private fun sendMessage(message: String) {
        val chat = Chat(
            null,
            senderUserId,
            senderUserName,
            message,
            false,
            null
        )

        chatSnippet = message
        if (chatSnippet!!.length > CHAT_SNIPPET_LENGTH) {
            chatSnippet = chatSnippet!!.substring(0, CHAT_SNIPPET_LENGTH)
            chatSnippet = chatSnippet.plus("...")
        }

        val room: HashMap<String, Any?>?

        if (isNewSession) {
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
                ROOM_DOC_FIELD_NAME_SESSION_END_AT to null,
                ROOM_DOC_FIELD_LAST_CHAT_SENDER_ID to firstUserId
            )

            db.collection(COLLECTION_ROOT_ROOM)
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
                ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to true,
                ROOM_DOC_FIELD_LAST_CHAT_SENDER_ID to firstUserId
            )

            db.collection(COLLECTION_ROOT_ROOM)
                .document(chatRoomName!!)
                .update(room)
                .addOnSuccessListener {
                    addNewChat(chat, false)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                }
        }

    }

    private fun addNewChat(chat: Chat, isNew: Boolean) {
        val docRef = db.collection(COLLECTION_ROOT_ROOM).document(chatRoomName!!)
        docRef.get()
            .addOnSuccessListener {
                docRef.collection(COLLECTION_ROOT_ROOM_CHAT)
                    .add(chat.toMap())
                    .addOnSuccessListener {
                        // what happen if new chat insertion succeeded
                        if (isNew) {
                            setupDocumentReference()
                        }
                    }
                    .addOnFailureListener {
                        // what happen if new chat insertion failed
                    }
            }
            .addOnFailureListener {

            }
    }

    private fun sendOpeningMessage(message: String, autoText: String) {
        val chat = Chat(
            null,
            "opening-message",
            autoText,
            message,
            true,
            null
        )

        chatSnippet = message
        if (chatSnippet!!.length > CHAT_SNIPPET_LENGTH) {
            chatSnippet = chatSnippet!!.substring(0, CHAT_SNIPPET_LENGTH)
            chatSnippet = chatSnippet.plus("...")
        }

        val room: HashMap<String, Any?>?

        if (isNewSession) {
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
                ROOM_DOC_FIELD_NAME_SESSION_END_AT to null,
                ROOM_DOC_FIELD_LAST_CHAT_SENDER_ID to firstUserId
            )

            db.collection(COLLECTION_ROOT_ROOM)
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
                ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to true,
                ROOM_DOC_FIELD_LAST_CHAT_SENDER_ID to firstUserId
            )

            db.collection(COLLECTION_ROOT_ROOM)
                .document(chatRoomName!!)
                .update(room)
                .addOnSuccessListener {
                    addNewChat(chat, false)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                }
        }

    }

    private fun showLoadingOpeningMessage(){
        val chat = Chat(
            PERSONAL_CHAT_TYPE_INCOMING_LOADING,
            "",
            "",
            "",
            false,
            null
        )

        chatList.add(chat)
        chatListAdapter.notifyDataSetChanged()
    }

    private fun updateUserStatus(isOnline: Boolean) {
        val room = hashMapOf(
            ROOM_DOC_FIELD_NAME_IS_CUSTOMER_ONLINE to isOnline
        )

        db.collection(COLLECTION_ROOT_ROOM)
            .document(chatRoomName!!)
            .update(room as Map<String, Any>)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}