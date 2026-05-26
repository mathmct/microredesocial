package br.com.ifsp.matheus.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.adapter.PostAdapter
import br.com.ifsp.matheus.microredesocial.databinding.ActivityHomeBinding
import br.com.ifsp.matheus.microredesocial.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sqrt

import android.view.Menu
import android.view.MenuItem

class HomeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PostAdapter
    private var allPosts = mutableListOf<Post>()
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        setSupportActionBar(binding.toolbarHome)
        supportActionBar?.title = "Micro Rede Social"
        adapter = PostAdapter(allPosts)
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFeed.adapter = adapter

        binding.btnNewPost.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_about) {
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            
            if (acceleration > 5) {
                Log.d("SENSOR_TEST", "Shake detectado!")
                if (!isLoading) {
                    vibratePhone()
                    Toast.makeText(this, "Atualizando feed...", Toast.LENGTH_SHORT).show()
                    refreshFeed()
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            Log.d("SENSOR_TEST", "Luminosidade: $lux lux")
            
            if (lux < 10f) {
                binding.toolbarHome.subtitle = "Ambiente escuro - Modo leitura"
            } else {
                binding.toolbarHome.subtitle = ""
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun vibratePhone() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onStart() {
        super.onStart()
        refreshFeed()
    }

    private fun refreshFeed() {
        allPosts.clear()
        lastVisible = null
        isLoading = false
        adapter.updateList(allPosts)
        loadPosts()
    }

    private fun loadPosts() {
        if (isLoading) return
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible!!)
        }

        query.get().addOnSuccessListener { documents ->
            binding.progressBar.visibility = View.GONE
            if (documents != null && !documents.isEmpty) {
                binding.txtEmptyFeed.visibility = View.GONE
                lastVisible = documents.documents[documents.size() - 1]
                val newPosts = documents.toObjects(Post::class.java)
                allPosts.addAll(newPosts)
                adapter.updateList(allPosts)
            } else if (allPosts.isEmpty()) {
                binding.txtEmptyFeed.visibility = View.VISIBLE
                binding.txtEmptyFeed.text = "Nenhuma postagem encontrada."
            }
            isLoading = false
        }.addOnFailureListener { e ->
            isLoading = false
            binding.progressBar.visibility = View.GONE
            Log.e("FIREBASE_HOME", "Erro ao carregar posts: ${e.message}")
            Toast.makeText(this, "Erro ao carregar feed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchPosts(city: String) {
        if (city.isEmpty()) {
            refreshFeed()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        db.collection("posts")
            .whereEqualTo("cidade", city)
            .get()
            .addOnSuccessListener { documents ->
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
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Erro na busca: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}