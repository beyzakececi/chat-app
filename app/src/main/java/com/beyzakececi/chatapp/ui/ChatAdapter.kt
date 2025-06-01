package com.beyzakececi.chatapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beyzakececi.chatapp.R
import com.beyzakececi.chatapp.model.Message
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvText: TextView = itemView.findViewById(R.id.tvText)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.tvText.text = message.text

        // Firestore’dan gelen Timestamp? öncelikle Date’e dönüştürelim
        val timeStamp: Timestamp? = message.timestamp
        if (timeStamp != null) {
            val dateObj: Date = timeStamp.toDate()
            // “HH:mm” formatında string’e çevirelim
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.tvTime.text = sdf.format(dateObj)
        } else {
            // Eğer henüz serverTimestamp atanmadıysa boş veya placeholder kullanabilirsiniz
            holder.tvTime.text = ""
        }
    }

    override fun getItemCount(): Int = messages.size

    /** Yeni mesaj eklendiğinde listeyi güncelleyecek basit metot */
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
