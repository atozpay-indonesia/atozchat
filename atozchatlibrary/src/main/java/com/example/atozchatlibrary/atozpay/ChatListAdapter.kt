package com.example.atozchatlibrary.atozpay

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.R
import com.example.atozchatlibrary.atozpay.model.Chat
import kotlinx.android.synthetic.main.item_list_chat_incoming.view.*
import kotlinx.android.synthetic.main.item_list_chat_outgoing.view.*
import java.util.*

class ChatListAdapter(
    private val chatList: List<Chat>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_OUTGOING = 1
        const val ITEM_TYPE_INCOMING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].type == ITEM_TYPE_OUTGOING) {
            ITEM_TYPE_OUTGOING
        } else {
            ITEM_TYPE_INCOMING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_OUTGOING -> {
                ViewHolderOutGoing(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_outgoing, parent, false)
                )
            }
            ITEM_TYPE_INCOMING -> {
                ViewHolderIncoming(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_incoming, parent, false)
                )
            }
            else -> ViewHolderOutGoing(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_chat_outgoing, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]

        when (chat.type) {
            ITEM_TYPE_INCOMING -> {
                val holderIncoming = holder as ViewHolderIncoming
                holderIncoming.setIncomingData(chat)
            }
            ITEM_TYPE_OUTGOING -> {
                val holderOutgoing = holder as ViewHolderOutGoing
                holderOutgoing.setOutGoingData(chat)
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    class ViewHolderOutGoing(view: View) : RecyclerView.ViewHolder(view) {
        private val tvChatBody = view.tv_chat_body_outgoing
        private val tvSentTime = view.tv_sent_time_outgoing

        fun setOutGoingData(chat: Chat) {
            tvChatBody.text = chat.body
            if (chat.timeSent != null ) {
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = chat.timeSent!!.seconds * 1000L
                val date = DateFormat.format("HH:mm", calendar1).toString()
                tvSentTime.visibility = View.VISIBLE
                tvSentTime.text = date
            } else {
                tvSentTime.visibility = View.INVISIBLE
            }
        }
    }

    class ViewHolderIncoming(view: View) : RecyclerView.ViewHolder(view) {
        private val tvChatBody = view.tv_chat_body_incoming
        private val tvSentTime = view.tv_sent_time_incoming

        fun setIncomingData(chat: Chat) {
            tvChatBody.text = chat.body
            if (chat.timeSent != null ) {
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = chat.timeSent!!.seconds * 1000L
                val date = DateFormat.format("HH:mm", calendar1).toString()
                tvSentTime.visibility = View.VISIBLE
                tvSentTime.text = date
            } else {
                tvSentTime.visibility = View.INVISIBLE
            }
        }
    }
}