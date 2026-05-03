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

data class Post(val id: Int, val author: String, val title: String, val content: String, val time: String)

class ForoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForoDetalleBinding
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private var userRole: String = "usuario"
    private var userEmail: String = ""
    private var salonName: String = ""
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var complaintsAdapter: ComplaintsActivity.ComplaintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        salonName = intent.getStringExtra("SALON_NAME") ?: "Foro"
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        
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
        val complaints = dbHelper.getAllComplaints(salonName)
        complaintsAdapter.updateData(complaints)
    }

    private fun showStatusDialog(complaintId: Int) {
        val options = arrayOf("no atendido", "en proceso", "atendido")
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar nuevo estado")
            .setItems(options) { _, which ->
                val newStatus = options[which]
                if (dbHelper.updateComplaintStatus(complaintId, newStatus)) {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    loadComplaints()
                }
            }
            .show()
    }

    private fun loadPosts() {
        posts.clear()
        val dbPosts = dbHelper.getPostsBySalon(salonName)
        for (p in dbPosts) {
            posts.add(Post(p.first, p.second.first, p.second.second, p.second.third, p.third))
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupBottomNavigation() {
        val menuRes = when (userRole) {
            "docente" -> R.menu.bottom_nav_menu_docente
            "admin" -> R.menu.bottom_nav_menu_admin
            else -> R.menu.bottom_nav_menu_docente // Por defecto docente o el que corresponda a padre si existe
        }
        binding.bottomNavigation.inflateMenu(menuRes)
        binding.bottomNavigation.selectedItemId = R.id.nav_foro
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    // Navegar a Inicio/Dashboard
                    finish()
                    true
                }
                R.id.nav_foro -> true
                else -> {
                    Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, userEmail, userRole)
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
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
                    val id = dbHelper.addPost(salonName, author, title, content, time).toInt()
                    if (id != -1) {
                        posts.add(0, Post(id, author, title, content, time))
                        adapter.notifyItemInserted(0)
                        binding.rvPosts.scrollToPosition(0)
                        binding.etPostContent.text?.clear()
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
                    val id = dbHelper.addPost(salonName, author, title, desc, time).toInt()
                    if (id != -1) {
                        posts.add(0, Post(id, author, title, desc, time))
                        adapter.notifyItemInserted(0)
                        binding.rvPosts.scrollToPosition(0)
                        Toast.makeText(this, "Publicado con éxito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class PostAdapter(private val posts: List<Post>, private val userEmail: String, private val userRole: String) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
                }
                context.startActivity(intent)
            }
        }

        override fun getItemCount() = posts.size
    }
}
