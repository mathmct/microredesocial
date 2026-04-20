package br.com.ifsp.matheus.microredesocial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.model.Post
import br.com.ifsp.matheus.microredesocial.util.Base64Converter

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        val txtCity: TextView = view.findViewById(R.id.txtCity)
        val txtDescription: TextView = view.findViewById(R.id.txtDescription)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val imgAuthorThumb: ImageView = view.findViewById(R.id.imgAuthorThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        holder.txtAuthor.text = post.autor
        holder.txtCity.text = post.cidade
        holder.txtDescription.text = post.texto
        
        if (post.fotoAutor.isNotEmpty()) {
            try {
                holder.imgAuthorThumb.setImageBitmap(Base64Converter.stringToBitmap(post.fotoAutor))
            } catch (e: Exception) {
                holder.imgAuthorThumb.setImageResource(R.mipmap.ic_launcher_round)
            }
        } else {
            holder.imgAuthorThumb.setImageResource(R.mipmap.ic_launcher_round)
        }

        if (post.imagem.isNotEmpty()) {
            holder.imgPost.visibility = View.VISIBLE
            try {
                holder.imgPost.setImageBitmap(Base64Converter.stringToBitmap(post.imagem))
            } catch (e: Exception) {
                holder.imgPost.visibility = View.GONE
            }
        } else {
            holder.imgPost.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size

    fun updateList(newList: List<Post>) {
        posts = newList
        notifyDataSetChanged()
    }
}