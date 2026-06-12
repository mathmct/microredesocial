package br.com.ifsp.matheus.microredesocial.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.matheus.microredesocial.adapter.PostAdapter
import br.com.ifsp.matheus.microredesocial.databinding.FragmentFeedBinding
import br.com.ifsp.matheus.microredesocial.model.Post
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PostAdapter
    private var allPosts = mutableListOf<Post>()
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter(allPosts)
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFeed.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val city = binding.editSearchCity.text.toString().trim()
            searchPosts(city)
        }

        binding.recyclerViewFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    if (!isLoading && lastVisible != null) {
                        loadPosts()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshFeed()
    }

    fun refreshFeed() {
        allPosts.clear()
        lastVisible = null
        isLoading = false
        adapter.updateList(allPosts)
        loadPosts()
    }

    private fun loadPosts() {
        if (isLoading) return
        isLoading = true
        _binding?.progressBar?.visibility = View.VISIBLE

        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible!!)
        }

        query.get().addOnSuccessListener { documents ->
            isLoading = false
            val binding = _binding ?: return@addOnSuccessListener
            binding.progressBar.visibility = View.GONE
            if (documents != null && !documents.isEmpty) {
                binding.txtEmptyFeed.visibility = View.GONE
                lastVisible = documents.documents[documents.size() - 1]
                val newPosts = documents.toObjects(Post::class.java)
                allPosts.addAll(newPosts)
                adapter.updateList(allPosts)
            } else if (allPosts.isEmpty()) {
                binding.txtEmptyFeed.visibility = View.VISIBLE
            }
        }.addOnFailureListener { e ->
            isLoading = false
            _binding?.progressBar?.visibility = View.GONE
            if (isAdded) {
                Toast.makeText(context, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchPosts(city: String) {
        if (city.isEmpty()) {
            refreshFeed()
            return
        }

        _binding?.progressBar?.visibility = View.VISIBLE
        db.collection("posts")
            .whereEqualTo("cidade", city)
            .get()
            .addOnSuccessListener { documents ->
                val binding = _binding ?: return@addOnSuccessListener
                binding.progressBar.visibility = View.GONE
                val filteredPosts = documents.toObjects(Post::class.java)
                if (filteredPosts.isEmpty()) {
                    binding.txtEmptyFeed.visibility = View.VISIBLE
                    binding.txtEmptyFeed.text = "Nenhuma postagem em $city"
                } else {
                    binding.txtEmptyFeed.visibility = View.GONE
                }
                val sortedPosts = filteredPosts.sortedByDescending { it.timestamp }
                adapter.updateList(sortedPosts)
            }
            .addOnFailureListener { e ->
                _binding?.progressBar?.visibility = View.GONE
                if (isAdded) {
                    Toast.makeText(context, "Erro na busca", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}