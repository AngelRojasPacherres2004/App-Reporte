package com.example.appreporte

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appreporte.databinding.ActivityDashboardPadreBinding

class PadreDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPadreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPadreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRole = intent.getStringExtra("USER_ROL") ?: "padre"
        setupUIByRole(userRole)
        setupBottomNavigation()
    }

    private fun setupUIByRole(role: String) {
        binding.tvWelcomeTitle.text = getString(R.string.welcome_parent)
        binding.tvWelcomeDesc.text = getString(R.string.welcome_parent_desc)
    }

    private fun setupBottomNavigation() {
        val userRole = intent.getStringExtra("USER_ROL") ?: "padre"
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_foro -> {
                    val intent = Intent(this, ForoSalonesActivity::class.java)
                    intent.putExtra("USER_ROL", userRole)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
