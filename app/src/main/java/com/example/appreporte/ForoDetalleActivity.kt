package com.example.appreporte

import android.view.ContextThemeWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ActivityForoDetalleBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query

data class Post(val id: String, val author: String, val title: String, val content: String, val time: String)

class ForoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForoDetalleBinding
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private var userRole: String = "usuario"
    private var userEmail: String = ""
    private var salonName: String = ""
    private var studentId: String = ""
    private var classroomId: String = ""
    private lateinit var complaintsAdapter: ComplaintsActivity.ComplaintsAdapter
    private val firestore = FirebaseFirestore.getInstance().apply {
        // Configuración para usar SQLite como caché local (100MB)
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(100 * 1024 * 1024)
                .build())
            .build()
        firestoreSettings = settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        salonName = intent.getStringExtra("SALON_NAME") ?: "Foro"
        classroomId = intent.getStringExtra("CLASSROOM_ID") ?: ""
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        
        binding.toolbar.title = salonName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupComplaintsRecyclerView()
        loadPosts()
        setupPublish()
        setupBottomNavigation()
        setupTabs()
    }

    private fun setupTabs() {
        if (userRole == "docente") {
            binding.tabLayout.visibility = View.VISIBLE
            binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    if (tab?.position == 0) {
                        binding.rvPosts.visibility = View.VISIBLE
                        binding.rvComplaints.visibility = View.GONE
                        binding.fabAddPost.visibility = View.VISIBLE
                    } else {
                        binding.rvPosts.visibility = View.GONE
                        binding.rvComplaints.visibility = View.VISIBLE
                        binding.fabAddPost.visibility = View.GONE
                        loadComplaints()
                    }
                }
                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            })
        }
    }

    private fun setupComplaintsRecyclerView() {
        complaintsAdapter = ComplaintsActivity.ComplaintsAdapter(emptyList()) { complaintId ->
            showStatusDialog(complaintId)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = complaintsAdapter
    }

    private fun loadComplaints() {
        firestore.collection("complaints")
            // Note: In real scenarios, add whereEqualTo("salonName", salonName) if saved
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = mutableListOf<Map<String, String>>()
                snapshot?.documents?.forEach { doc ->
                    val map = mutableMapOf<String, String>()
                    map["id"] = doc.id
                    map["post_title"] = doc.getString("postId") ?: ""
                    map["parent_email"] = doc.getString("parentEmail") ?: ""
                    map["content"] = doc.getString("content") ?: ""
                    map["status"] = doc.getString("status") ?: ""
                    list.add(map)
                }
                complaintsAdapter.updateData(list)
            }
    }

    private fun showStatusDialog(complaintId: String) {
        val options = arrayOf("no atendido", "en proceso", "atendido")
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar nuevo estado")
            .setItems(options) { _, which ->
                val newStatus = options[which]
                firestore.collection("complaints").document(complaintId).update("status", newStatus)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .whereEqualTo("salonName", salonName)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Error en foro: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    posts.clear()
                    snapshot.documents.forEach { doc ->
                        val id = doc.id
                        val author = doc.getString("author") ?: ""
                        val title = doc.getString("title") ?: ""
                        val content = doc.getString("content") ?: ""
                        val time = doc.getString("time") ?: ""
                        posts.add(Post(id, author, title, content, time))
                    }
                    adapter.notifyDataSetChanged()
                    // Si hay nuevos posts, volver arriba para ver el más reciente
                    if (posts.isNotEmpty()) {
                        binding.rvPosts.scrollToPosition(0)
                    }
                }
            }
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
                R.id.nav_foro -> true
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

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, userEmail, userRole) { post, action ->
            if (action == "edit") {
                showEditPostDialog(post)
            } else if (action == "delete") {
                confirmDeletePost(post)
            }
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun showEditPostDialog(post: Post) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null)
        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogTitle)
        val etDesc = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogDesc)

        etTitle.setText(post.title)
        etDesc.setText(post.content)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Modificar Publicación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = etTitle.text?.toString() ?: ""
                val desc = etDesc.text?.toString() ?: ""
                if (title.isNotEmpty() && desc.isNotEmpty()) {
                    firestore.collection("posts").document(post.id)
                        .update(mapOf("title" to title, "content" to desc))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Publicación actualizada", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDeletePost(post: Post) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Publicación")
            .setMessage("¿Estás seguro de que deseas eliminar esto?")
            .setPositiveButton("Eliminar") { _, _ ->
                firestore.collection("posts").document(post.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupPublish() {
        if (userRole == "docente") {
            binding.createPostContainer.visibility = View.GONE
            binding.fabAddPost.visibility = View.VISIBLE
            binding.fabAddPost.setOnClickListener {
                showCreatePostDialog()
            }
        } else {
            binding.createPostContainer.visibility = View.VISIBLE
            binding.fabAddPost.visibility = View.GONE
            binding.etPostTitle.visibility = View.GONE
            
            binding.btnPublish.setOnClickListener {
                val content = binding.etPostContent.text?.toString() ?: ""
                if (content.isNotEmpty()) {
                    val time = "AHORA"
                    val author = userEmail
                    val title = "Comentario"
                    val postMap = hashMapOf(
                        "salonName" to salonName,
                        "author" to author,
                        "title" to title,
                        "content" to content,
                        "time" to time,
                        "timestamp" to System.currentTimeMillis()
                    )
                    binding.etPostContent.text?.clear() // Limpiar antes para rapidez
                    firestore.collection("posts").add(postMap)
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun showCreatePostDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null)
        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogTitle)
        val etDesc = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogDesc)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Nueva Publicación")
            .setView(dialogView)
            .setPositiveButton("Publicar") { _, _ ->
                val title = etTitle.text?.toString() ?: ""
                val desc = etDesc.text?.toString() ?: ""
                if (title.isNotEmpty() && desc.isNotEmpty()) {
                    val time = "AHORA"
                    val author = userEmail
                    val postMap = hashMapOf(
                        "salonName" to salonName,
                        "author" to author,
                        "title" to title,
                        "content" to desc,
                        "time" to time,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("posts").add(postMap).addOnSuccessListener {
                        Toast.makeText(this, "Publicado con éxito", Toast.LENGTH_SHORT).show()
                        // CA1: Activar notificaciones para nuevas publicaciones del docente
                        triggerPostNotification(title, desc)
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun triggerPostNotification(title: String, content: String) {
        // Buscar todos los alumnos de este salón para notificar a sus padres
        val filterField = if (classroomId.isNotEmpty()) "classroom_id" else "salonName"
        val filterValue = if (classroomId.isNotEmpty()) classroomId else salonName

        firestore.collection("students")
            .whereEqualTo(filterField, filterValue)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val studentName = doc.getString("names") ?: "Su hijo(a)"
                    val parentEmail = doc.getString("parent_email") ?: ""
                    
                    if (parentEmail.isNotEmpty()) {
                        firestore.collection("users").document(parentEmail).get()
                            .addOnSuccessListener { userSnap ->
                                val phone = userSnap.getString("phone") ?: ""
                                if (phone.isNotEmpty()) {
                                    val data = androidx.work.Data.Builder()
                                        .putString("student_name", studentName)
                                        .putString("message", "Nueva publicación en el foro: $title")
                                        .putString("phone", phone)
                                        .build()

                                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<WhatsAppNotificationWorker>()
                                        .setInputData(data)
                                        .build()

                                    androidx.work.WorkManager.getInstance(applicationContext).enqueue(workRequest)
                                }
                            }
                    }
                }
            }
    }

    inner class PostAdapter(
        private val posts: List<Post>, 
        private val userEmail: String, 
        private val userRole: String,
        private val onAction: (Post, String) -> Unit
    ) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAuthor: TextView = view.findViewById(R.id.tvAuthorName)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvContent: TextView = view.findViewById(R.id.tvContent)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = posts[position]
            holder.tvAuthor.text = post.author
            holder.tvTitle.text = post.title
            holder.tvContent.text = post.content
            holder.tvTime.text = post.time
            
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = android.content.Intent(context, PostDetalleActivity::class.java).apply {
                    putExtra("POST_ID", post.id)
                    putExtra("POST_AUTHOR", post.author)
                    putExtra("POST_TITLE", post.title)
                    putExtra("POST_CONTENT", post.content)
                    putExtra("POST_TIME", post.time)
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("USER_ROL", userRole)
                    putExtra("STUDENT_ID", studentId)
                    putExtra("CLASSROOM_ID", classroomId)
                }
                context.startActivity(intent)
            }
            
            holder.itemView.setOnLongClickListener {
                if (userEmail == post.author || userRole == "docente" || userRole == "admin") {
                    val options = arrayOf("Modificar", "Eliminar")
                    androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Opciones de Publicación")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> onAction(post, "edit")
                                1 -> onAction(post, "delete")
                            }
                        }
                        .show()
                }
                true
            }
        }

        override fun getItemCount() = posts.size
    }
}
