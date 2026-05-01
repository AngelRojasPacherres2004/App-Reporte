package com.example.appreporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityPostDetalleBinding

data class Comment(val author: String, val content: String, val time: String)

class PostDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetalleBinding
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Obtener datos del post desde el Intent
        val author = intent.getStringExtra("POST_AUTHOR") ?: "Autor"
        val title = intent.getStringExtra("POST_TITLE") ?: "Título"
        val content = intent.getStringExtra("POST_CONTENT") ?: "Contenido"
        val time = intent.getStringExtra("POST_TIME") ?: "HACE MOMENTOS"

        // Mostrar datos del post en la vista incluida
        binding.includedPost.tvAuthorName.text = author
        binding.includedPost.tvTitle.text = title
        binding.includedPost.tvContent.text = content
        binding.includedPost.tvTime.text = time
        binding.includedPost.tvCommentsCount.visibility = View.GONE // No necesitamos mostrar el conteo aquí

        setupRecyclerView()
        setupCommentInput()
    }

    private fun setupRecyclerView() {
        // Datos de prueba
        comments.add(Comment("Carlos Ruiz", "Espero que así sea, también tengo esa duda.", "HACE 5 MIN"))
        comments.add(Comment("Ana Belén", "El docente dijo que sí en la última clase.", "HACE 2 MIN"))

        adapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
    }

    private fun setupCommentInput() {
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text?.toString() ?: ""
            if (text.isNotEmpty()) {
                comments.add(Comment("Usuario Actual", text, "AHORA"))
                adapter.notifyItemInserted(comments.size - 1)
                binding.rvComments.scrollToPosition(comments.size - 1)
                binding.etComment.text?.clear()
                Toast.makeText(this, "Comentario enviado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class CommentAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAuthor: TextView = view.findViewById(R.id.tvCommentAuthor)
            val tvContent: TextView = view.findViewById(R.id.tvCommentContent)
            val tvTime: TextView = view.findViewById(R.id.tvCommentTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val comment = comments[position]
            holder.tvAuthor.text = comment.author
            holder.tvContent.text = comment.content
            holder.tvTime.text = comment.time
        }

        override fun getItemCount() = comments.size
    }
}
