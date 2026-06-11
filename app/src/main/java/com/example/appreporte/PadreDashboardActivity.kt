package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appreporte.databinding.ActivityDashboardPadreBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.firebase.firestore.FirebaseFirestore

class PadreDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPadreBinding
    private var userEmail: String = ""
    private var userRole: String = "usuario"
    
    private lateinit var hijosAdapter: HijosAdapter
    private var selectedStudentId: String = ""
    private var selectedClassroomId: String = ""
    private var selectedClassroomName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupUI()
        setupBottomNavigation()
        setupClickListeners()
        setupThemeToggle()
        loadHijos()
    }

    private fun setupUI() {
        binding.rvHijos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hijosAdapter = HijosAdapter(emptyList()) { hijo ->
            selectedStudentId = hijo["id"] ?: ""
            selectedClassroomId = hijo["classroom_id"] ?: ""
            binding.tvSeleccionaHijoMsg.visibility = View.GONE
            binding.llHijoContent.visibility = View.VISIBLE
            
            // Resolve classroom name for the selected child
            FirebaseFirestore.getInstance().collection("classrooms").document(selectedClassroomId).get()
                .addOnSuccessListener { doc ->
                    selectedClassroomName = doc.getString("name") ?: "Salón"
                }
        }
        binding.rvHijos.adapter = hijosAdapter
        
        binding.tvSeleccionaHijoMsg.visibility = View.VISIBLE
        binding.llHijoContent.visibility = View.GONE
    }

    private fun loadHijos() {
        FirebaseFirestore.getInstance().collection("students")
            .whereEqualTo("parent_email", userEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data?.mapValues { it.value.toString() }?.toMutableMap()
                        data?.put("id", doc.id)
                        data
                    }
                    if (list.isNotEmpty()) {
                        hijosAdapter.updateData(list)
                        binding.tvSeleccionaHijoMsg.visibility = View.GONE
                    } else {
                        binding.tvSeleccionaHijoMsg.text = "No se encontraron hijos registrados con su correo."
                        binding.tvSeleccionaHijoMsg.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun setupThemeToggle() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (isNightMode) {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        binding.ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnVerForo.setOnClickListener {
            if (selectedClassroomId.isNotEmpty()) {
                val intent = Intent(this, ForoDetalleActivity::class.java)
                intent.putExtra("SALON_NAME", selectedClassroomName)
                intent.putExtra("CLASSROOM_ID", selectedClassroomId)
                intent.putExtra("USER_ROL", userRole)
                intent.putExtra("USER_EMAIL", userEmail)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seleccione un hijo primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.findViewById<android.view.View>(R.id.btnPadreHorario)?.setOnClickListener {
            if (selectedStudentId.isNotEmpty()) {
                val intent = Intent(this, PadreHorarioActivity::class.java)
                intent.putExtra("STUDENT_ID", selectedStudentId)
                intent.putExtra("CLASSROOM_ID", selectedClassroomId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seleccione un hijo primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.findViewById<android.view.View>(R.id.btnPadreAsistencia)?.setOnClickListener {
            if (selectedStudentId.isNotEmpty()) {
                val intent = Intent(this, PadreAsistenciaActivity::class.java)
                intent.putExtra("STUDENT_ID", selectedStudentId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seleccione un hijo primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVerReportes.setOnClickListener {
            if (selectedStudentId.isNotEmpty()) {
                val intent = Intent(this, PadreReporteActivity::class.java)
                intent.putExtra("USER_EMAIL", userEmail)
                intent.putExtra("STUDENT_ID", selectedStudentId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seleccione un hijo primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivPadreProfile.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("SCHOOL_ID", getIntent().getStringExtra("SCHOOL_ID") ?: "")
            startActivity(intent)
        }

        binding.root.findViewById<android.view.View>(R.id.btnContactarDocente)?.setOnClickListener {
            if (selectedClassroomId.isNotEmpty()) {
                // Find teacher for this classroom
                FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("rol", "docente")
                    .whereArrayContains("classrooms", selectedClassroomId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val teacherEmail = snapshot.documents[0].id
                            val intent = Intent(this, DirectChatActivity::class.java)
                            intent.putExtra("CURRENT_EMAIL", userEmail)
                            intent.putExtra("TARGET_EMAIL", teacherEmail)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "No hay docente asignado a este salón", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error buscando docente", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Seleccione un hijo primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_inicio

        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        val assistantItem = menuView.findViewById<BottomNavigationItemView>(R.id.nav_asistente)
        val iconView = assistantItem?.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)

        iconView?.post {
            val params = iconView.layoutParams
            val density = resources.displayMetrics.density
            val sizeInPx = (40 * density).toInt()

            params.width = sizeInPx
            params.height = sizeInPx
            iconView.layoutParams = params
            iconView.scaleType = ImageView.ScaleType.FIT_CENTER
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_foro -> {
                    if (selectedClassroomId.isNotEmpty()) {
                        val intent = Intent(this, ForoDetalleActivity::class.java)
                        intent.putExtra("SALON_NAME", selectedClassroomName)
                        intent.putExtra("CLASSROOM_ID", selectedClassroomId)
                        intent.putExtra("USER_ROL", userRole)
                        intent.putExtra("USER_EMAIL", userEmail)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Seleccione un hijo arriba primero para ver su Foro", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_asistente -> {
                    val intent = Intent(this, ChatbotPadreActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    true
                }
                R.id.nav_reportes -> {
                    if (selectedStudentId.isNotEmpty()) {
                        val intent = Intent(this, PadreReporteActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        intent.putExtra("STUDENT_ID", selectedStudentId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Seleccione un hijo arriba primero para ver sus Notas", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}