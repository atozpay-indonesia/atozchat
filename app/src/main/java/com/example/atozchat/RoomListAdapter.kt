package com.example.atozchat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.AtozChat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.item_list_room.view.*

class RoomListAdapter(private val roomList: List<ChatRoom>, private val context: Context, private val fcmToken: String?) : RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUserName = view.tv_user_name
        private val parentLayout = view.parent_layout

        fun bindRoom(room: ChatRoom, context: Context, fcmToken: String?) {
            tvUserName.text = room.senderName
            parentLayout.setOnClickListener {
                generateFirebaseToken(context, room)
            }
        }

        private fun generateFirebaseToken(context: Context, room: ChatRoom) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(AtozChat.TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                AtozChat.initChatAtozpay(context)
                    .setActivityTitle("Customer Support Chat")
                    .setSenderUserId(room.senderId!!.toString())
                    .setSenderUserName(room.senderName)
                    .setFcmToken(token)
                    .startChat()
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_room, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindRoom(roomList[position], context, fcmToken)
    }

    override fun getItemCount(): Int = roomList.size
}