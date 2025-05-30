package com.example.maxxx.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maxxx.R
import com.example.maxxx.databinding.ActivityMainBinding
import com.example.maxxx.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

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
            animateToLocationSmoothly(selectedPlace.location)
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
        val clearIcon = R.drawable.ic_clear

        // Mostrar o quitar el ícono según haya texto o no
        fun updateClearIcon() {
            val icon = if (binding.searchBar.text?.isNotEmpty() == true) {
                resources.getDrawable(clearIcon, null)
            } else null

            binding.searchBar.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        }

        // Al tocar el ícono de limpiar
        binding.searchBar.setOnTouchListener { v, event ->
            val drawableEnd = binding.searchBar.compoundDrawables[2] ?: return@setOnTouchListener false

            val touchX = event.x.toInt()
            val iconStart = binding.searchBar.width - binding.searchBar.paddingEnd - drawableEnd.intrinsicWidth

            if (touchX >= iconStart) {
                binding.searchBar.text?.clear()
                true
            } else false
        }

        // Escuchar cambios y mostrar ícono
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                updateClearIcon()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Buscar al hacer "Enter"
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.setSearchQuery(binding.searchBar.text.toString())
                true
            } else false
        }
    }


    private fun observeViewModel() {
        viewModel.userLocation.observe(this) { location ->
            animateToLocationSmoothly(location)
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
        animateToLocationSmoothly(sucreLocation)

        map?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
        }

        // Mostrar info cuando se toca el marcador
        map?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    private fun animateToLocationSmoothly(location: LatLng) {
        map?.clear()

        val marker = map?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Ubicación seleccionada")
        )

        // Rebotar el marcador
        marker?.let { bounceMarker(it) }

        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(17f)
            .tilt(45f)
            .bearing(90f)
            .build()

        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    // 👇 Animación de rebote del marcador
    private fun bounceMarker(marker: Marker) {
        val handler = Handler(Looper.getMainLooper())
        val start = System.currentTimeMillis()
        val duration: Long = 1500
        val interpolator = BounceInterpolator()

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - start
                val t = Math.max(1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f)
                marker.setAnchor(0.5f, 1.0f + 2 * t)

                if (t > 0.0) {
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }
}
