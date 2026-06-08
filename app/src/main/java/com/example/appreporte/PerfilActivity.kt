package com.example.appreporte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import java.util.Locale

class PerfilActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var tvInitials: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvSchool: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var llAddressSection: LinearLayout
    private lateinit var btnLogout: Button
    private lateinit var btnGetLocation: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val MAP_REQUEST_CODE = 101

    private var currentRole: String = "usuario"
    private var hasPromptedForAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvEmail = findViewById(R.id.tvProfileEmail)
        tvInitials = findViewById(R.id.tvProfileInitials)
        tvRole = findViewById(R.id.chipProfileRole)
        tvSchool = findViewById(R.id.tvProfileSchool)
        tvPhone = findViewById(R.id.tvProfilePhone)
        tvAddress = findViewById(R.id.tvProfileAddress)
        llAddressSection = findViewById(R.id.llAddressSection)
        btnLogout = findViewById(R.id.btnLogout)
        btnGetLocation = findViewById(R.id.btnGetLocation)

        currentRole = intent.getStringExtra("USER_ROL")?.lowercase() ?: "usuario"
        
        btnGetLocation.setOnClickListener { 
            val mapIntent = Intent(this, MapSelectionActivity::class.java)
            startActivityForResult(mapIntent, MAP_REQUEST_CODE)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.menu.clear()
        when (currentRole) {
            "superadmin", "admin" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin)
            "docente" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_docente)
            else -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_padre)
        }
        bottomNav.selectedItemId = R.id.nav_perfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val targetActivity = when (currentRole.lowercase()) {
                        "superadmin" -> SuperAdminDashboardActivity::class.java
                        "admin" -> InicioActivity::class.java
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val navIntent = Intent(this, targetActivity)
                    navIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    navIntent.putExtra("USER_ROL", currentRole)
                    navIntent.putExtra("SCHOOL_ID", tvSchool.text.toString())
                    navIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_gestion -> {
                    val gestionIntent = Intent(this, AdminDashboardActivity::class.java)
                    gestionIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    gestionIntent.putExtra("SCHOOL_ID", tvSchool.text.toString())
                    startActivity(gestionIntent)
                    finish()
                    true
                }
                R.id.nav_foro -> {
                    val foroIntent = Intent(this, ForoActivity::class.java)
                    foroIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    foroIntent.putExtra("USER_ROL", currentRole)
                    foroIntent.putExtra("SCHOOL_ID", tvSchool.text.toString())
                    startActivity(foroIntent)
                    finish()
                    true
                }
                R.id.nav_asistente -> {
                    val asistenteIntent = Intent(this, AsistenteActivity::class.java)
                    asistenteIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    asistenteIntent.putExtra("USER_ROL", currentRole)
                    startActivity(asistenteIntent)
                    finish()
                    true
                }
                R.id.nav_reportes -> {
                    val targetActivity = when (currentRole.lowercase()) {
                        "docente" -> DocenteDashboardActivity::class.java
                        else -> PadreDashboardActivity::class.java
                    }
                    val navIntent = Intent(this, targetActivity)
                    navIntent.putExtra("USER_EMAIL", tvEmail.text.toString())
                    navIntent.putExtra("USER_ROL", currentRole)
                    navIntent.putExtra("SCHOOL_ID", tvSchool.text.toString())
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_perfil -> true
                else -> true
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val logoutIntent = Intent(this, MainActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }

        loadProfileData()

        if (intent.getStringExtra("ACTION") == "EDIT_ADDRESS") {
            val mapIntent = Intent(this, MapSelectionActivity::class.java)
            startActivityForResult(mapIntent, MAP_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK) {
            val address = data?.getStringExtra("ADDRESS") ?: ""
            val lat = data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
            val lng = data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0
            
            if (address.isNotEmpty()) {
                updateAddressInFirestore(address, lat, lng)
            }
        }
    }

    private fun loadProfileData() {
        val intentEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        val dbHelper = DatabaseHelper(this)

        if (intentEmail.isNotEmpty()) {
            tvEmail.text = intentEmail
            tvInitials.text = intentEmail.take(1).uppercase()

            if (intentEmail == "padre1@reporte.com") {
                val lat = -12.036438
                val lng = -76.961094
                val addressText = getAddressFromCoords(lat, lng)
                updateAddressInFirestore(addressText, lat, lng)
            }
            
            FirebaseFirestore.getInstance().collection("users").document(intentEmail)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener

                    if (snapshot != null && snapshot.exists()) {
                        val role = snapshot.getString("rol") ?: currentRole
                        val school = snapshot.getString("school_id") ?: "Sin Colegio"
                        val phone = snapshot.getString("phone") ?: "No registrado"
                        
                        val direccionField = snapshot.get("direccion")

                        val displayAddress = when (direccionField) {
                            is GeoPoint -> getAddressFromCoords(direccionField.latitude, direccionField.longitude)
                            is String -> if (direccionField.isNotEmpty()) direccionField else "No registrado"
                            else -> "No registrado"
                        }

                        tvRole.text = role.uppercase()
                        tvSchool.text = school
                        tvPhone.text = phone
                        tvAddress.text = displayAddress

                        val normalizedRole = role.lowercase()
                        if (normalizedRole == "padre" || normalizedRole == "usuario") {
                            llAddressSection.visibility = android.view.View.VISIBLE
                        } else {
                            llAddressSection.visibility = android.view.View.GONE
                        }

                        dbHelper.syncUserProfile(intentEmail, role, phone, displayAddress)
                        checkMissingData(displayAddress)
                    } else {
                        val localData = dbHelper.getUserData(intentEmail)
                        localData?.let {
                            tvRole.text = it["rol"]?.uppercase()
                            tvSchool.text = "Modo Offline"
                            tvPhone.text = it["phone"]
                            tvAddress.text = it["address"]
                        }
                    }
                }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        updateAddressInFirestore(address, location.latitude, location.longitude)
                    } else {
                        Toast.makeText(this, "No se pudo determinar la dirección", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al obtener dirección: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAddressInFirestore(address: String, lat: Double? = null, lng: Double? = null) {
        val email = tvEmail.text.toString()
        if (email.isNotEmpty()) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(email)
            
            val data = mutableMapOf<String, Any>()
            if (lat != null && lng != null) {
                data["direccion"] = GeoPoint(lat, lng)
                data["address_cache"] = address 
            } else {
                data["direccion"] = address
                data["address_cache"] = FieldValue.delete()
            }
            
            userRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    tvAddress.text = address
                    Toast.makeText(this, "Ubicación actualizada correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getAddressFromCoords(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                "Lat: $lat, Lng: $lng"
            }
        } catch (e: Exception) {
            "Lat: $lat, Lng: $lng"
        }
    }

    private fun checkMissingData(address: String) {
        val normalizedRole = currentRole.lowercase()
        if (!hasPromptedForAddress && address == "No registrado" && (normalizedRole == "usuario" || normalizedRole == "padre")) {
            hasPromptedForAddress = true
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    val mapIntent = Intent(this, MapSelectionActivity::class.java)
                    startActivityForResult(mapIntent, MAP_REQUEST_CODE)
                    Toast.makeText(this, "Por favor, seleccione su domicilio en el mapa para completar su perfil", Toast.LENGTH_LONG).show()
                }
            }, 1000)
        }
    }


}
