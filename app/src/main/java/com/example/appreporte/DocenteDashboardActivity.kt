package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.appreporte.databinding.ActivityDashboardDocenteBinding

class DocenteDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDocenteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Detectar el modo actual al iniciar para poner el ícono correcto
        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Si es modo noche, mostramos el SOL; si es día, la LUNA
        if (isNightMode) {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            binding.ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        // 2. Lógica del clic para cambiar el tema
        binding.ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                // Si estamos en noche, pasamos a modo claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                // Si estamos en día, pasamos a modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // 3. Navegación Inferior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_reportes -> {
                    // RF-06: Gestión de quejas
                    startActivity(Intent(this, DocenteComplaintsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }
}