package br.com.ifsp.matheus.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editSenha.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erro no login", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}