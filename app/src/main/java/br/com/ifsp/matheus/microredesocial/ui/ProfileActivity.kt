package br.com.ifsp.matheus.microredesocial.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.matheus.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.matheus.microredesocial.util.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var profileImageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meu Perfil"
        binding.toolbarProfile.setNavigationOnClickListener {
            finish()
        }

        val userEmail = firebaseAuth.currentUser?.email
        if (userEmail != null) {
            db.collection("usuarios").document(userEmail).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.nomeCompleto.setText(document.getString("nomeCompleto"))
                        val foto = document.getString("fotoPerfil")
                        if (!foto.isNullOrEmpty()) {
                            try {
                                binding.profilePicture.setImageBitmap(Base64Converter.stringToBitmap(foto))
                            } catch (e: Exception) {
                                Log.e("PROFILE", "Erro ao carregar foto")
                            }
                        }
                    }
                }
        }

        val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.profilePicture.setImageURI(uri)
                binding.profilePicture.post {
                    profileImageBase64 = Base64Converter.drawableToString(binding.profilePicture.drawable)
                }
            }
        }

        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSalvar.setOnClickListener {
            val novoNome = binding.nomeCompleto.text.toString().trim()
            val novaSenha = binding.editPassword.text.toString().trim()

            if (novoNome.isEmpty()) {
                Toast.makeText(this, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userEmail != null) {
                binding.btnSalvar.isEnabled = false
                val updates = mutableMapOf<String, Any>("nomeCompleto" to novoNome)
                profileImageBase64?.let { updates["fotoPerfil"] = it }

                db.collection("usuarios").document(userEmail)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        if (novaSenha.isNotEmpty()) {
                            firebaseAuth.currentUser?.updatePassword(novaSenha)
                                ?.addOnCompleteListener {
                                    Toast.makeText(this, "Perfil e senha atualizados!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                        } else {
                            Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        binding.btnSalvar.isEnabled = true
                        Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}