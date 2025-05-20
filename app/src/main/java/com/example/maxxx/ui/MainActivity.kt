package com.example.maxxx.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maxxx.R
import com.example.maxxx.databinding.ActivityMainBinding
import com.example.maxxx.ui.model.Place
import com.example.maxxx.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var map: GoogleMap? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupMapFragment()
        setupMapTypeSelector()
        setupLocationButton()
        setupSearchBar()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.searchResultsRecycler

        adapter = SearchResultsAdapter(emptyList(), onItemClick = { selectedPlace ->
            binding.searchBar.setText(selectedPlace.name)
            recyclerView.visibility = View.GONE

            // Mueve la cámara al lugar seleccionado
            updateCameraToLocation(selectedPlace.location)

            // Oculta el teclado al seleccionar un lugar
            hideKeyboard()

        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.searchResults.observe(this) { results ->
            adapter.updateData(results, viewModel.searchQuery.value ?: "")
            recyclerView.isVisible = results.isNotEmpty()
            animateRecyclerGrowth(recyclerView, results.size)
        }
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupMapTypeSelector() {
        val adapterSpinner = android.widget.ArrayAdapter.createFromResource(
            this,
            R.array.map_types,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.mapTypeSelector.adapter = adapterSpinner

        binding.mapTypeSelector.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                map?.mapType = when (position) {
                    1 -> GoogleMap.MAP_TYPE_SATELLITE
                    2 -> GoogleMap.MAP_TYPE_TERRAIN
                    3 -> GoogleMap.MAP_TYPE_HYBRID
                    else -> GoogleMap.MAP_TYPE_NORMAL
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupLocationButton() {
        binding.btnMyLocation.setOnClickListener {
            viewModel.updateUserLocation(-19.03332, -65.26274)
        }
    }

    private fun setupSearchBar() {
        // Búsqueda en tiempo real con addTextChangedListener
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        // También manejo búsqueda al presionar buscar en teclado
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.setSearchQuery(binding.searchBar.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userLocation.observe(this) { location ->
            updateCameraToLocation(location)
        }
    }

    private fun animateRecyclerGrowth(view: View, itemCount: Int) {
        val itemHeightPx = resources.getDimensionPixelSize(R.dimen.recycler_item_height)
        val targetHeight = itemHeightPx * itemCount
        val anim = ValueAnimator.ofInt(view.height, targetHeight)
        anim.addUpdateListener {
            val value = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        anim.duration = 900
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

        map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        map?.uiSettings?.apply {
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

        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }
}
