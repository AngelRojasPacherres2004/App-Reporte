package com.example.appreporte

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class UIMessage(val sender: String, val content: String, val isMe: Boolean)

class ChatAdapter(private val messages: List<UIMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootLayout: LinearLayout = view as LinearLayout
        val cardMessage: MaterialCardView = view.findViewById(R.id.cardMessage)
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        val tvMessageContent: TextView = view.findViewById(R.id.tvMessageContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvSenderName.text = msg.sender
        holder.tvMessageContent.text = msg.content

        if (msg.isMe) {
            holder.rootLayout.gravity = Gravity.END
            holder.cardMessage.setCardBackgroundColor(holder.itemView.context.getColor(R.color.primary))
            holder.tvSenderName.setTextColor(holder.itemView.context.getColor(R.color.white))
            holder.tvMessageContent.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.rootLayout.gravity = Gravity.START
            holder.cardMessage.setCardBackgroundColor(holder.itemView.context.getColor(R.color.surface))
            holder.tvSenderName.setTextColor(holder.itemView.context.getColor(R.color.primary))
            holder.tvMessageContent.setTextColor(holder.itemView.context.getColor(R.color.on_surface))
        }
    }

    override fun getItemCount() = messages.size
}