package br.com.ifsp.matheus.microredesocial.ui

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class HomeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
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
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        setSupportActionBar(binding.toolbarHome)

        if (savedInstanceState == null) {
            replaceFragment(FeedFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> replaceFragment(FeedFragment())
                R.id.nav_messages -> replaceFragment(MessagesFragment())
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            }
            true
        }

        binding.btnNewPost.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }

        setupUnreadMessagesBadge()
    }

    private fun setupUnreadMessagesBadge() {
        val currentUserEmail = firebaseAuth.currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()

        // Listener para mensagens não lidas enviadas PARA o usuário atual
        db.collectionGroup("mensagens")
            .whereEqualTo("destinatarioId", currentUserEmail)
            .whereEqualTo("lida", false)
            .addSnapshotListener { snapshot, _ ->
                val unreadCount = snapshot?.size() ?: 0
                val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_messages)
                if (unreadCount > 0) {
                    badge.isVisible = true
                    badge.number = unreadCount
                } else {
                    badge.isVisible = false
                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.btnLogout -> {
                firebaseAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                lastAcceleration = currentAcceleration
                currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = currentAcceleration - lastAcceleration
                acceleration = acceleration * 0.9f + delta
                if (acceleration > 12) {
                    vibratePhone()
                    startActivity(Intent(this, NewPostActivity::class.java))
                }
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                val isNightMode = currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

                if (lux < 10f) {
                    binding.toolbarHome.subtitle = "Modo Leitura"
                    if (!isNightMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                } else {
                    binding.toolbarHome.subtitle = ""
                    if (isNightMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                binding.privacyOverlay.visibility = if (distance < (proximitySensor?.maximumRange ?: 5f)) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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
}