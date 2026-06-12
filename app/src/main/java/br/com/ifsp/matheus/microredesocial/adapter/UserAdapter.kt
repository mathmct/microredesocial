package br.com.ifsp.matheus.microredesocial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.model.Message
import br.com.ifsp.matheus.microredesocial.model.User
import br.com.ifsp.matheus.microredesocial.util.Base64Converter

data class UserWithLastMessage(
    val user: User,
    val lastMessage: Message? = null,
    val hasUnread: Boolean = false
)

class UserAdapter(private var items: List<UserWithLastMessage>, private val onClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgUser: ImageView = view.findViewById(R.id.imgUserPhoto)
        val txtName: TextView = view.findViewById(R.id.txtUserName)
        val txtLastMsg: TextView = view.findViewById(R.id.txtLastMessage)
        val unreadIndicator: View = view.findViewById(R.id.unreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = items[position]
        val user = item.user
        holder.txtName.text = user.nomeCompleto
        
        holder.txtLastMsg.text = item.lastMessage?.texto ?: "Nenhuma mensagem"
        holder.unreadIndicator.visibility = if (item.hasUnread) View.VISIBLE else View.GONE

        if (user.fotoPerfil.isNotEmpty()) {
            try {
                holder.imgUser.setImageBitmap(Base64Converter.stringToBitmap(user.fotoPerfil))
            } catch (e: Exception) {
                holder.imgUser.setImageResource(R.mipmap.ic_launcher_round)
            }
        } else {
            holder.imgUser.setImageResource(R.mipmap.ic_launcher_round)
        }

        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<UserWithLastMessage>) {
        items = newList
        notifyDataSetChanged()
    }
}