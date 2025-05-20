package com.example.maxxx.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maxxx.R
import com.example.maxxx.databinding.ActivityMainBinding
import com.example.maxxx.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: GoogleMap
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.searchResultsRecycler
        val adapter = SearchResultsAdapter(emptyList()) { selected ->
            binding.searchBar.setText(selected)
            recyclerView.visibility = View.GONE
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.searchResults.observe(this) { results ->
            adapter.updateData(results)
            recyclerView.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
            animateRecyclerGrowth(recyclerView, results.size)
        }


        // Configurar el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar selector de tipo de mapa
        ArrayAdapter.createFromResource(
            this,
            R.array.map_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.mapTypeSelector.adapter = adapter
        }

        binding.mapTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (position) {
                    0 -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 -> map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    3 -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón para centrar en Sucre
        binding.btnMyLocation.setOnClickListener {
            viewModel.updateUserLocation(-19.03332, -65.26274)
        }

        // Buscar cuando se presiona enter
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchBar.text.toString()
            viewModel.setSearchQuery(query)
            true
        }

        // Observar ubicación
        viewModel.userLocation.observe(this) { location ->
            updateCameraToLocation(location)
        }
    }

    private fun animateRecyclerGrowth(view: View, itemCount: Int) {
        val targetHeight = if (itemCount > 0) itemCount * 120 else 0 // aprox. 120px por ítem
        val anim = ValueAnimator.ofInt(view.height, targetHeight)
        anim.addUpdateListener {
            val value = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        anim.duration = 300
        anim.start()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val sucreLocation = LatLng(-19.03332, -65.26274)
        val cameraPosition = CameraPosition.Builder()
            .target(sucreLocation)
            .zoom(17f)
            .tilt(45f)
            .bearing(90f)
            .build()

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
        }
    }

    private fun updateCameraToLocation(location: LatLng) {
        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(17f)
            .tilt(45f)
            .bearing(90f)
            .build()

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}
