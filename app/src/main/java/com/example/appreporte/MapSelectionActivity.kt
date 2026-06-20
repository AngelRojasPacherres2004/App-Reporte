package com.example.appreporte

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapSelectionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvSelectedAddress: TextView
    private lateinit var btnConfirm: Button
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress)
        btnConfirm = findViewById(R.id.btnConfirmSelection)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnConfirm.setOnClickListener {
            selectedLatLng?.let { latLng ->
                val resultIntent = Intent()
                resultIntent.putExtra("LATITUDE", latLng.latitude)
                resultIntent.putExtra("LONGITUDE", latLng.longitude)
                resultIntent.putExtra("ADDRESS", selectedAddress)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Ubicación por defecto (Lima)
        val defaultLocation = LatLng(-12.046374, -77.042793)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            selectedLatLng = center
            updateAddressText(center.latitude, center.longitude)
        }
    }

    private fun updateAddressText(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                selectedAddress = addresses[0].getAddressLine(0)
                tvSelectedAddress.text = selectedAddress
            } else {
                selectedAddress = "Lat: $lat, Lng: $lng"
                tvSelectedAddress.text = selectedAddress
            }
        } catch (e: Exception) {
            selectedAddress = "Lat: $lat, Lng: $lng"
            tvSelectedAddress.text = selectedAddress
        }
    }
}
