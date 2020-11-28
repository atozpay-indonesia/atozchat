package com.example.atozchat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.AtozChat
import kotlinx.android.synthetic.main.item_list_room.view.*

class RoomListAdapter(private val roomList: List<ChatRoom>, private val context: Context) : RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUserName = view.tv_user_name
        private val parentLayout = view.parent_layout

        fun bindRoom(room: ChatRoom, context: Context) {
            tvUserName.text = room.senderName

            parentLayout.setOnClickListener {
                AtozChat.initChatAtozpay(context)
                    .setSenderUserId(room.senderId!!.toString())
                    .setSenderUserName(room.senderName)
                    .startChat()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_room, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindRoom(roomList[position], context)
    }

    override fun getItemCount(): Int = roomList.size
}