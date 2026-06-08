package com.example.appreporte

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

data class Comment(
    val author: String = "",
    val content: String = "",
    val time: String = "",
    val timestamp: Long = 0L
)

class PostDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetalleBinding
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var postId: String = ""
    private var userEmail: String = ""
    private var userRole: String = ""
    private var studentId: String = ""
    private var classroomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        postId = intent.getStringExtra("POST_ID") ?: ""
        val author = intent.getStringExtra("POST_AUTHOR") ?: "Autor"
        val title = intent.getStringExtra("POST_TITLE") ?: "Título"
        val content = intent.getStringExtra("POST_CONTENT") ?: "Contenido"
        val time = intent.getStringExtra("POST_TIME") ?: "HACE MOMENTOS"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: "Usuario"
        userRole = intent.getStringExtra("USER_ROL") ?: ""
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""

        binding.includedPost.tvAuthorName.text = author
        binding.includedPost.tvTitle.text = title
        binding.includedPost.tvContent.text = content
        binding.includedPost.tvTime.text = time
        binding.includedPost.tvCommentsCount.visibility = View.GONE

        if (userRole == "usuario") {
            binding.btnQueja.visibility = View.VISIBLE
            binding.btnQueja.setOnClickListener {
                showQuejaDialog()
            }
        }

        setupRecyclerView()
        loadComments()
        setupCommentInput()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val menuRes = when (userRole) {
            "docente" -> R.menu.bottom_nav_menu_docente
            "admin" -> R.menu.bottom_nav_menu_admin
            "usuario" -> R.menu.bottom_nav_menu_padre
            else -> R.menu.bottom_nav_menu_docente
        }
        binding.bottomNavigation.inflateMenu(menuRes)
        binding.bottomNavigation.selectedItemId = R.id.nav_foro

        // Ajuste de icono de asistente si es padre
        if (userRole == "usuario") {
            val menuView = binding.bottomNavigation.getChildAt(0) as? ViewGroup
            val assistantItem = menuView?.findViewById<View>(R.id.nav_asistente)
            val iconView = assistantItem?.findViewById<android.widget.ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
            iconView?.post {
                val params = iconView.layoutParams
                val density = resources.displayMetrics.density
                val sizeInPx = (40 * density).toInt()
                params.width = sizeInPx
                params.height = sizeInPx
                iconView.layoutParams = params
                iconView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    if (userRole == "usuario") {
                        val intent = android.content.Intent(this, PadreDashboardActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        intent.putExtra("USER_ROL", userRole)
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        finish()
                    }
                    true
                }
                R.id.nav_foro -> {
                    finish() // Regresar al ForoDetalleActivity
                    true
                }
                R.id.nav_asistente -> {
                    val intent = android.content.Intent(this, ChatbotPadreActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("STUDENT_ID", studentId)
                    startActivity(intent)
                    true
                }
                R.id.nav_reportes -> {
                    if (userRole == "usuario") {
                        if (studentId.isNotEmpty()) {
                            val intent = android.content.Intent(this, PadreReporteActivity::class.java)
                            intent.putExtra("USER_EMAIL", userEmail)
                            intent.putExtra("STUDENT_ID", studentId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Seleccione un hijo en el Inicio primero", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Funcionalidad para padres", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_perfil -> {
                    val intent = android.content.Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun showQuejaDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Realizar Queja")
        
        val input = EditText(this)
        input.hint = "Explique el motivo de su queja..."
        builder.setView(input)

        builder.setPositiveButton("Enviar") { _, _ ->
            val contentText = input.text.toString().trim()
            if (contentText.isNotEmpty()) {
                val complaintMap = hashMapOf(
                    "postId" to postId,
                    "parentEmail" to userEmail,
                    "content" to contentText,
                    "status" to "en proceso",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("complaints").add(complaintMap).addOnSuccessListener {
                    Toast.makeText(this, "Queja enviada correctamente", Toast.LENGTH_SHORT).show()
                    // CA1: Activar notificación para queja recibida (al administrador/colegio)
                    triggerComplaintNotification(contentText)
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun triggerComplaintNotification(content: String) {
        // En un caso real buscaríamos al admin del colegio, aquí usamos el admin@reporte.com demo
        firestore.collection("users").document("admin@reporte.com").get()
            .addOnSuccessListener { userSnap ->
                val phone = userSnap.getString("phone") ?: ""
                if (phone.isNotEmpty()) {
                    val data = androidx.work.Data.Builder()
                        .putString("student_name", "ADMINISTRADOR")
                        .putString("message", "Nueva queja recibida de $userEmail: $content")
                        .putString("phone", phone)
                        .build()

                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<WhatsAppNotificationWorker>()
                        .setInputData(data)
                        .build()

                    androidx.work.WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
    }

    private fun loadComments() {
        if (postId.isNotEmpty()) {
            firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    
                    if (snapshot != null) {
                        val newComments = snapshot.documents.mapNotNull { doc ->
                            val authorStr = doc.getString("author") ?: ""
                            val contentStr = doc.getString("content") ?: ""
                            val timeStr = doc.getString("time") ?: ""
                            val ts = doc.getLong("timestamp") ?: 0L
                            Comment(authorStr, contentStr, timeStr, ts)
                        }.sortedBy { it.timestamp }

                        comments.clear()
                        comments.addAll(newComments)
                        adapter.notifyDataSetChanged()
                        
                        if (comments.isNotEmpty()) {
                            binding.rvComments.postDelayed({
                                binding.rvComments.smoothScrollToPosition(comments.size - 1)
                            }, 100)
                        }
                    }
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
            val text = binding.etComment.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty() && postId.isNotEmpty()) {
                val commentMap = hashMapOf(
                    "postId" to postId,
                    "author" to userEmail,
                    "content" to text,
                    "time" to "AHORA",
                    "timestamp" to System.currentTimeMillis()
                )
                binding.etComment.text?.clear()
                
                firestore.collection("comments").add(commentMap)
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al enviar", Toast.LENGTH_SHORT).show()
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
