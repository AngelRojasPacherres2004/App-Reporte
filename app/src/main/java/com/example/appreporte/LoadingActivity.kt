package com.example.appreporte

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.progressindicator.LinearProgressIndicator

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading)
        
        val progressBar = findViewById<LinearProgressIndicator>(R.id.progressBar)
        
        // Animación de la barra de carga (0 a 100 en 3 segundos)
        ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Al terminar la animación, ir al Dashboard correspondiente según el rol
                    val rol = intent.getStringExtra("USER_ROL")
                    val targetActivity = when (rol) {
                        "admin" -> AdminDashboardActivity::class.java
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    
                    val intent = Intent(this@LoadingActivity, targetActivity)
                    intent.putExtra("USER_ROL", rol)
                    startActivity(intent)
                    finish() // Cerrar la pantalla de carga
                }
            })
            start()
        }

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}