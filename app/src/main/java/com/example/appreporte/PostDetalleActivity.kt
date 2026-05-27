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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Comment(val author: String, val content: String, val time: String)

class PostDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetalleBinding
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var postId: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Obtener datos del post desde el Intent
        postId = intent.getStringExtra("POST_ID") ?: ""
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
                val complaintMap = hashMapOf(
                    "postId" to postId,
                    "parentEmail" to userEmail,
                    "content" to content,
                    "status" to "en proceso",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("complaints").add(complaintMap).addOnSuccessListener {
                    Toast.makeText(this@PostDetalleActivity, "Queja enviada correctamente", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this@PostDetalleActivity, "Error al enviar queja", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun loadComments() {
        if (postId.isNotEmpty()) {
            firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    comments.clear()
                    snapshot?.documents?.forEach { doc ->
                        val author = doc.getString("author") ?: ""
                        val content = doc.getString("content") ?: ""
                        val time = doc.getString("time") ?: ""
                        comments.add(Comment(author, content, time))
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
    }

    private fun setupCommentInput() {
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text?.toString() ?: ""
            if (text.isNotEmpty() && postId.isNotEmpty()) {
                val time = "AHORA"
                val commentMap = hashMapOf(
                    "postId" to postId,
                    "author" to userEmail,
                    "content" to text,
                    "time" to time,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("comments").add(commentMap)
                binding.etComment.text?.clear()
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
