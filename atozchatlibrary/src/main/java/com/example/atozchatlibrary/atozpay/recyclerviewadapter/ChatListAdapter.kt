package com.example.atozchatlibrary.atozpay.recyclerviewadapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.R
import com.example.atozchatlibrary.atozpay.model.Chat
import kotlinx.android.synthetic.main.item_list_chat_incoming.view.*
import kotlinx.android.synthetic.main.item_list_chat_info.view.*
import kotlinx.android.synthetic.main.item_list_chat_outgoing.view.*
import java.util.*

class ChatListAdapter(
    private val chatList: List<Chat>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_OUTGOING = 1
        const val ITEM_TYPE_INCOMING = 2
        const val ITEM_TYPE_INFO = 3
        const val ITEM_TYPE_INCOMING_LOADING = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (chatList[position].type) {
            ITEM_TYPE_OUTGOING -> {
                ITEM_TYPE_OUTGOING
            }
            ITEM_TYPE_INCOMING -> {
                ITEM_TYPE_INCOMING
            }
            ITEM_TYPE_INCOMING_LOADING -> {
                ITEM_TYPE_INCOMING_LOADING
            }
            else -> {
                ITEM_TYPE_INFO
            }
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
            ITEM_TYPE_INCOMING_LOADING -> {
                ViewHolderIncomingLoading(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_incoming_loading, parent, false)
                )
            }
            ITEM_TYPE_INFO -> {
                ViewHolderInfo(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_info, parent, false)
                )
            }
            else -> ViewHolderOutGoing(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_chat_incoming, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]

        when (chat.type) {
            ITEM_TYPE_INCOMING -> {
                val holderIncoming = holder as ViewHolderIncoming
                holderIncoming.setIncomingData(chat, context)
            }
            ITEM_TYPE_OUTGOING -> {
                val holderOutgoing = holder as ViewHolderOutGoing
                holderOutgoing.setOutGoingData(chat)
            }
            ITEM_TYPE_INCOMING_LOADING -> {

            }
            ITEM_TYPE_INFO -> {
                val holderInfo = holder as ViewHolderInfo
                holderInfo.setInfoMsg(chat)
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    class ViewHolderOutGoing(view: View) : RecyclerView.ViewHolder(view) {
        private val tvChatBody = view.tv_chat_body_outgoing
        private val tvSentTime = view.tv_sent_time_outgoing
        private val tvStatusPending = view.iv_status_pending

        fun setOutGoingData(chat: Chat) {
            tvChatBody.text = chat.body
            if (chat.timeSent != null ) {
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = chat.timeSent.seconds * 1000L
                val date = DateFormat.format("HH:mm", calendar1).toString()
                tvStatusPending.visibility = View.GONE
                tvSentTime.visibility = View.VISIBLE
                tvSentTime.text = date
            } else {
                tvSentTime.visibility = View.GONE
                tvStatusPending.visibility = View.VISIBLE
            }
        }
    }

    class ViewHolderIncoming(view: View) : RecyclerView.ViewHolder(view) {
        private val tvChatBody = view.tv_chat_body_incoming
        private val tvSentTime = view.tv_sent_time_incoming
        private val tvAutoGenerateText = view.tv_auto_generated_msg

        fun setIncomingData(chat: Chat, context: Context) {
            tvChatBody.text = chat.body
            if (chat.timeSent != null ) {
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = chat.timeSent.seconds * 1000L
                val date = DateFormat.format("HH:mm", calendar1).toString()
                tvSentTime.visibility = View.VISIBLE
                tvSentTime.text = date
            } else {
                tvSentTime.visibility = View.INVISIBLE
            }

            if (chat.isAutoGenerated){
                tvAutoGenerateText.visibility = View.VISIBLE
                tvAutoGenerateText.text = context.getString(R.string.atozpay_personal_room_auto_text_placeholder, chat.senderName)
            } else {
                tvAutoGenerateText.visibility = View.GONE
            }
        }
    }

    class ViewHolderIncomingLoading(view: View) : RecyclerView.ViewHolder(view) {

    }

    class ViewHolderInfo(view: View) : RecyclerView.ViewHolder(view) {
        private val tvInfoMsg = view.tv_info_msg

        fun setInfoMsg(chat: Chat) {
            tvInfoMsg.text = chat.body
        }
    }
}