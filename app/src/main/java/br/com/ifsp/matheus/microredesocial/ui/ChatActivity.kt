package br.com.ifsp.matheus.microredesocial.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.ifsp.matheus.microredesocial.adapter.MessageAdapter
import br.com.ifsp.matheus.microredesocial.databinding.ActivityChatBinding
import br.com.ifsp.matheus.microredesocial.model.Message
import br.com.ifsp.matheus.microredesocial.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: MessageAdapter
    private var messages = mutableListOf<Message>()
    private var chatRoomId: String? = null
    private var messagesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.getSerializableExtra("user") as? User ?: return finish()
        val currentEmail = auth.currentUser?.email ?: return finish()

        supportActionBar?.hide()
        binding.toolbarChat.title = user.nomeCompleto
        
        chatRoomId = if (currentEmail < user.email) "${currentEmail}_${user.email}" else "${user.email}_$currentEmail"

        adapter = MessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        listenMessages()

        binding.btnSendMessage.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) sendMessage(text)
        }
    }

    private fun listenMessages() {
        chatRoomId?.let { id ->
            val currentUserEmail = auth.currentUser?.email
            messagesListener = db.collection("conversas").document(id).collection("mensagens")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        messages.clear()
                        for (doc in snapshot.documents) {
                            val message = doc.toObject(Message::class.java)
                            if (message != null) {
                                messages.add(message)
                                // Marca como lida se a mensagem for do outro usuário e estiver como não lida
                                if (message.remetenteId != currentUserEmail && !message.lida) {
                                    doc.reference.update("lida", true)
                                }
                            }
                        }
                        adapter.updateMessages(messages)
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }

    private fun sendMessage(text: String) {
        val currentEmail = auth.currentUser?.email ?: return
        val user = intent.getSerializableExtra("user") as? User ?: return
        val message = Message(
            remetenteId = currentEmail,
            destinatarioId = user.email,
            texto = text,
            timestamp = System.currentTimeMillis(),
            lida = false
        )
        
        chatRoomId?.let { id ->
            db.collection("conversas").document(id).collection("mensagens").add(message)
                .addOnSuccessListener { binding.editMessage.text.clear() }
                .addOnFailureListener { Toast.makeText(this, "Erro ao enviar", Toast.LENGTH_SHORT).show() }
        }
    }
}