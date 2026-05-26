package br.com.ifsp.matheus.microredesocial.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import br.com.ifsp.matheus.microredesocial.R
import br.com.ifsp.matheus.microredesocial.databinding.ActivityPostDetailBinding
import br.com.ifsp.matheus.microredesocial.model.Post
import java.text.SimpleDateFormat
import java.util.Locale

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("POST_DATA", Post::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("POST_DATA") as? Post
        }

        post?.let {
            binding.txtAuthorDetail.text = it.autor
            binding.txtDescriptionDetail.text = it.texto
            binding.txtCityDetail.text = if (it.cidade.isNotEmpty()) "📍 ${it.cidade}" else getString(R.string.loading)
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.txtDateDetail.text = "Postado em: ${sdf.format(it.timestamp)}"

            if (it.imagem.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(it.imagem, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgDetail.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}