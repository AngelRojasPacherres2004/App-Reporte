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

data class Post(val author: String, val title: String, val content: String, val time: String)

class ForoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForoDetalleBinding
    private val posts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private var userRole: String = "usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val salonName = intent.getStringExtra("SALON_NAME") ?: "Foro"
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        
        binding.toolbar.title = salonName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupPublish()
        setupBottomNavigation()
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
        posts.add(Post("Elena Rodríguez", "Consulta sobre Examen", "¿Podrían confirmarme si el examen de matemáticas se mantiene?", "HACE 12 MIN"))
        posts.add(Post("Marcos Silva", "Agradecimiento", "Muchas gracias por la información compartida.", "AYER"))

        adapter = PostAdapter(posts)
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
                    posts.add(0, Post("Padre de Familia", "Comentario", content, "AHORA"))
                    adapter.notifyItemInserted(0)
                    binding.rvPosts.scrollToPosition(0)
                    binding.etPostContent.text?.clear()
                }
            }
        }
    }

    private fun showCreatePostDialog() {
        // Usar un ContextThemeWrapper para asegurar que los componentes de Material tengan el tema correcto
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
                    posts.add(0, Post("Docente", title, desc, "AHORA"))
                    adapter.notifyItemInserted(0)
                    binding.rvPosts.scrollToPosition(0)
                    Toast.makeText(this, "Publicado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
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
                    putExtra("POST_AUTHOR", post.author)
                    putExtra("POST_TITLE", post.title)
                    putExtra("POST_CONTENT", post.content)
                    putExtra("POST_TIME", post.time)
                }
                context.startActivity(intent)
            }
        }

        override fun getItemCount() = posts.size
    }
}
