package com.example.appreporte

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityPostDetalleBinding

data class Comment(val author: String, val content: String, val time: String)

class PostDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetalleBinding
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var postId: Int = -1
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Obtener datos del post desde el Intent
        postId = intent.getIntExtra("POST_ID", -1)
        val author = intent.getStringExtra("POST_AUTHOR") ?: "Autor"
        val title = intent.getStringExtra("POST_TITLE") ?: "Título"
        val content = intent.getStringExtra("POST_CONTENT") ?: "Contenido"
        val time = intent.getStringExtra("POST_TIME") ?: "HACE MOMENTOS"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: "Usuario"
        val userRol = intent.getStringExtra("USER_ROL") ?: ""

        // Mostrar datos del post en la vista incluida
        binding.includedPost.tvAuthorName.text = author
        binding.includedPost.tvTitle.text = title
        binding.includedPost.tvContent.text = content
        binding.includedPost.tvTime.text = time
        binding.includedPost.tvCommentsCount.visibility = View.GONE // No necesitamos mostrar el conteo aquí

        // Si es padre, mostrar botón de queja
        if (userRol == "usuario") {
            binding.btnQueja.visibility = View.VISIBLE
            binding.btnQueja.setOnClickListener {
                showQuejaDialog()
            }
        }

        setupRecyclerView()
        loadComments()
        setupCommentInput()
    }

    private fun showQuejaDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Realizar Queja")
        
        val input = EditText(this)
        input.hint = "Explique el motivo de su queja..."
        builder.setView(input)

        builder.setPositiveButton("Enviar") { _, _ ->
            val content = input.text.toString()
            if (content.isNotEmpty()) {
                if (dbHelper.addComplaint(postId, userEmail, content)) {
                    Toast.makeText(this, "Queja enviada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar queja", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun loadComments() {
        comments.clear()
        if (postId != -1) {
            val dbComments = dbHelper.getCommentsByPost(postId)
            for (c in dbComments) {
                comments.add(Comment(c.first, c.second, c.third))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
    }

    private fun setupCommentInput() {
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text?.toString() ?: ""
            if (text.isNotEmpty() && postId != -1) {
                val time = "AHORA"
                if (dbHelper.addComment(postId, userEmail, text, time)) {
                    comments.add(Comment(userEmail, text, time))
                    adapter.notifyItemInserted(comments.size - 1)
                    binding.rvComments.scrollToPosition(comments.size - 1)
                    binding.etComment.text?.clear()
                    Toast.makeText(this, "Comentario enviado", Toast.LENGTH_SHORT).show()
                }
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
