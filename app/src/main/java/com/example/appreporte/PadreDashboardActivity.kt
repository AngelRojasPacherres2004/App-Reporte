package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.appreporte.databinding.ActivityDashboardPadreBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

class PadreDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPadreBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userEmail: String = ""
    private var userRole: String = "usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        userRole = intent.getStringExtra("USER_ROL") ?: "usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setupUI()
        setupBottomNavigation()
        setupClickListeners()
        setupThemeToggle() // Activamos el control del modo oscuro
    }

    private fun setupUI() {
        // Configuramos los títulos del encabezado dinámicamente
        binding.tvHeaderTitle.text = "EduConnect"
        binding.tvHeaderSubtitle.text = "Padre"

        // El saludo de la Welcome Card ya está en el XML, pero lo reforzamos aquí si es necesario
    }

    private fun setupThemeToggle() {
        // Detectar si el sistema ya está en modo noche
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Cambiar el icono inicial (Luna para claro, Sol para oscuro)
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
        // Asegúrate que el ID en el XML sea btnVerForo
        binding.btnVerForo.setOnClickListener {
            navigateToForo()
        }

        // Clic en la foto de perfil (opcional, por si quieres abrir ajustes)
        binding.ivPadreProfile.setOnClickListener {
            Toast.makeText(this, "Perfil de Padre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToForo() {
        val classrooms = dbHelper.getUserClassroomsWithNames(userEmail)
        if (classrooms.size == 1) {
            val intent = Intent(this, ForoDetalleActivity::class.java)
            intent.putExtra("SALON_NAME", classrooms[0].second)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        } else {
            val intent = Intent(this, ForoSalonesActivity::class.java)
            intent.putExtra("USER_ROL", userRole)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_inicio

        // Logica para aumentar ligeramente el tamano del icono del chatbot
        val menuView = binding.bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        val assistantItem = menuView.findViewById<BottomNavigationItemView>(R.id.nav_asistente)

        val iconView = assistantItem?.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)

        iconView?.post {
            val params = iconView.layoutParams
            val density = resources.displayMetrics.density

            // Incremento moderado a 40dp para compensar margenes transparentes de la imagen
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
                    navigateToForo()
                    true
                }
                // --- AQUÍ ESTÁ LA INTEGRACIÓN DEL CHATBOT ---
                R.id.nav_asistente -> {
                    val intent = Intent(this, ChatbotPadreActivity::class.java)
                    startActivity(intent)
                    true
                }
                // ----------------------------------------------
                R.id.nav_reportes -> {
                    Toast.makeText(this, "Reportes en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(this, "Perfil en desarrollo", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}