package com.example.atozchatlibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atozchatlibrary.model.Chat

class ChatListAdapter(
    private val chatList: List<Chat>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_SENDER = 1
        const val ITEM_TYPE_RECIPIENT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].type == ITEM_TYPE_SENDER) {
            ITEM_TYPE_SENDER
        } else {
            ITEM_TYPE_RECIPIENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_SENDER -> {
                ViewHolderSender(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_outgoing, parent, false)
                )
            }
            ITEM_TYPE_RECIPIENT -> {
                ViewHolderRecipient(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_chat_incoming, parent, false)
                )
            }
            else -> ViewHolderSender(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_chat_outgoing, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = chatList.size

    class ViewHolderSender(view: View) : RecyclerView.ViewHolder(view) {

    }

    class ViewHolderRecipient(view: View) : RecyclerView.ViewHolder(view) {

    }
}