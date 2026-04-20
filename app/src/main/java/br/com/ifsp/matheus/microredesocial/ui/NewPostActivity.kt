package br.com.ifsp.matheus.microredesocial.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.databinding.ActivityNewPostBinding
import br.com.ifsp.matheus.microredesocial.util.Base64Converter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var city: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarNewPost)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova Publicação"
        binding.toolbarNewPost.setNavigationOnClickListener {
            finish()
        }

        val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.postImage.setImageURI(uri)
            }
        }

        binding.btnSelectImage.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnGetLocation.setOnClickListener {
            binding.txtCity.text = "Buscando localização..."
            obterLocalizacao()
        }

        binding.btnPost.setOnClickListener {
            val description = binding.editDescription.text.toString().trim()
            if (description.isEmpty()) {
                Toast.makeText(this, "Preencha a descrição", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = firebaseAuth.currentUser?.email ?: return@setOnClickListener
            binding.btnPost.isEnabled = false

            db.collection("usuarios").document(userEmail).get().addOnSuccessListener { document ->
                val nomeUsuario = document.getString("nomeCompleto") ?: userEmail
                val fotoUsuario = document.getString("fotoPerfil") ?: ""
                val imagePostString = Base64Converter.drawableToString(binding.postImage.drawable)

                val post = hashMapOf(
                    "texto" to description,
                    "imagem" to imagePostString,
                    "cidade" to (city ?: "Desconhecida"),
                    "autor" to nomeUsuario,
                    "fotoAutor" to fotoUsuario,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("posts").add(post).addOnSuccessListener {
                    Toast.makeText(this, "Postado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { e ->
                    binding.btnPost.isEnabled = true
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses?.isNotEmpty() == true) {
                        city = addresses[0].locality ?: addresses[0].subAdminArea
                        binding.txtCity.text = "Cidade: $city"
                    }
                } catch (e: Exception) {
                    binding.txtCity.text = "Erro ao obter cidade"
                }
            }
        }
    }
}