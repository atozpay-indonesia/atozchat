package com.example.atozchat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.activeandroid.query.Select
import com.example.atozchatlibrary.AtozChat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var token: String? = null
    private val roomList: MutableList<ChatRoom> = ArrayList()
    private val roomListAdapter = RoomListAdapter(roomList, this, token)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv_room.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = roomListAdapter
        }

        generateFirebaseToken()
    }

    private fun saveDummyList() {
        val userId1 = 8232
        val userName1 = "Muhammad Abdi Alidrus"
        val userId2 = 7673
        val userName2 = "Ricky Robiansyah"
        val userId3 = 9127
        val userName3 = "Tommy Haryanto"
        val userId4 = 2152
        val userName4 = "Gilang Ramadan"
        val userId5 = 1526
        val userName5 = "Dede Badru"
        val userId6 = 61732
        val userName6 = "Samyo Sumarmono"
        val userId7 = 72299
        val userName7 = "Djunaedy Hermawanto"
        val userId8 = 80831
        val userName8 = "Ronny Ramdhani"

        val chatRoom1 = ChatRoom(
            userId1,
            userName1
        )

        val chatRoom2 = ChatRoom(
            userId2,
            userName2
        )

        val chatRoom3 = ChatRoom(
            userId3,
            userName3
        )

        val chatRoom4 = ChatRoom(
            userId4,
            userName4
        )

        val chatRoom5 = ChatRoom(
            userId5,
            userName5
        )

        val chatRoom6 = ChatRoom(
            userId6,
            userName6
        )

        val chatRoom7 = ChatRoom(
            userId7,
            userName7
        )

        val chatRoom8 = ChatRoom(
            userId8,
            userName8
        )

        chatRoom1.save()
        chatRoom2.save()
        chatRoom3.save()
        chatRoom4.save()
        chatRoom5.save()
        chatRoom6.save()
        chatRoom7.save()
        chatRoom8.save()
    }

    private fun loadRoom() {
        val rooms = Select().from(
            ChatRoom::class.java
        ).execute<ChatRoom>()

        roomList.addAll(rooms)
        roomListAdapter.notifyDataSetChanged()
    }

    private fun generateFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(AtozChat.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result

            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(AtozChat.TAG, token)
            Toast.makeText(this, token, Toast.LENGTH_SHORT).show()

            saveDummyList()
            loadRoom()
        })
    }
}