package com.example.appreporte

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class AppReporteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configuración global de Firestore con persistencia en SQLite (100MB)
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(100 * 1024 * 1024)
                .build())
            .build()
        
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
