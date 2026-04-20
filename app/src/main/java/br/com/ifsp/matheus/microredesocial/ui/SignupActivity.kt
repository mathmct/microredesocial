package br.com.ifsp.matheus.microredesocial.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSignup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Criar Conta"
        binding.toolbarSignup.setNavigationOnClickListener {
            finish()
        }

        binding.btnCreate.setOnClickListener {
            val nome = binding.nomeCompleto.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val senha = binding.password.text.toString().trim()
            val senhaNovamente = binding.passwordAgain.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != senhaNovamente) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = hashMapOf(
                            "nomeCompleto" to nome,
                            "email" to email
                        )
                        db.collection("usuarios").document(email).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}