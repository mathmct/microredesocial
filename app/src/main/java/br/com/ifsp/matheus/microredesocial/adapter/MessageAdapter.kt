package br.com.ifsp.matheus.microredesocial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.model.Message
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private var messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.email

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].remetenteId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.txtSent.text = message.texto
        else if (holder is ReceivedViewHolder) holder.txtReceived.text = message.texto
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSent: TextView = view.findViewById(R.id.txtMessageSent)
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtReceived: TextView = view.findViewById(R.id.txtMessageReceived)
    }
}