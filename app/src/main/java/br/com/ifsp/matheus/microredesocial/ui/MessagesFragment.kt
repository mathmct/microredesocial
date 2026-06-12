package br.com.ifsp.matheus.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.ifsp.matheus.microredesocial.adapter.UserAdapter
import br.com.ifsp.matheus.microredesocial.adapter.UserWithLastMessage
import br.com.ifsp.matheus.microredesocial.databinding.FragmentMessagesBinding
import br.com.ifsp.matheus.microredesocial.model.Message
import br.com.ifsp.matheus.microredesocial.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun loadUsers() {
        val currentUserEmail = auth.currentUser?.email ?: return
        db.collection("usuarios").get().addOnSuccessListener { snapshot ->
            val binding = _binding ?: return@addOnSuccessListener
            val users = mutableListOf<User>()
            for (doc in snapshot.documents) {
                val user = doc.toObject(User::class.java)
                if (user != null && user.email != currentUserEmail) {
                    users.add(user)
                }
            }
            
            val userListWithMessages = mutableListOf<UserWithLastMessage>()
            if (users.isEmpty()) {
                adapter.updateList(emptyList())
                return@addOnSuccessListener
            }

            var loadedCount = 0
            for (user in users) {
                val chatRoomId = if (currentUserEmail < user.email) "${currentUserEmail}_${user.email}" else "${user.email}_$currentUserEmail"
                
                db.collection("conversas").document(chatRoomId).collection("mensagens")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { msgSnapshot ->
                        val lastMessage = if (!msgSnapshot.isEmpty) msgSnapshot.documents[0].toObject(Message::class.java) else null
                        
                        // Check for unread
                        db.collection("conversas").document(chatRoomId).collection("mensagens")
                            .whereEqualTo("remetenteId", user.email)
                            .whereEqualTo("lida", false)
                            .get()
                            .addOnSuccessListener { unreadSnapshot ->
                                userListWithMessages.add(UserWithLastMessage(user, lastMessage, !unreadSnapshot.isEmpty))
                                loadedCount++
                                if (loadedCount == users.size) {
                                    userListWithMessages.sortByDescending { it.lastMessage?.timestamp ?: 0L }
                                    adapter.updateList(userListWithMessages)
                                }
                            }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}