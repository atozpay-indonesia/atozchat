package com.example.atozchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.activeandroid.query.Select
import com.example.atozchatlibrary.AtozChat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val roomList: MutableList<ChatRoom> = ArrayList()
    private val roomListAdapter = RoomListAdapter(roomList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv_room.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = roomListAdapter
        }

        saveDummyList()
        loadRoom()
    }

    private fun saveDummyList() {
        val csId = 456
        val csName = "Customer Service 01"

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
            "chat-$userId1-$csId",
            "$userId1",
            userName1,
            "$csId",
            "$csName"
        )

        val chatRoom2 = ChatRoom(
            "chat-$userId2-$csId",
            "$userId2",
            userName2,
            "$csId",
            csName
        )

        val chatRoom3 = ChatRoom(
            "chat-$userId3-$csId",
            "$userId3",
            userName3,
            "$csId",
            csName
        )

        val chatRoom4 = ChatRoom(
            "chat-$userId4-$csId",
            "$userId4",
            userName4,
            "$csId",
            csName
        )

        val chatRoom5 = ChatRoom(
            "chat-$userId5-$csId",
            "$userId5",
            userName5,
            "$csId",
            csName
        )

        val chatRoom6 = ChatRoom(
            "chat-$userId6-$csId",
            "$userId6",
            userName6,
            "$csId",
            csName
        )

        val chatRoom7 = ChatRoom(
            "chat-$userId7-$csId",
            "$userId7",
            userName7,
            "$csId",
            csName
        )

        val chatRoom8 = ChatRoom(
            "chat-$userId8-$csId",
            "$userId8",
            userName8,
            "$csId",
            csName
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
}