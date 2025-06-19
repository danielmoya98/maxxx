package com.example.maxxx.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maxxx.R
import com.example.maxxx.data.PlaceRepository
import com.example.maxxx.data.RouteRepository
import com.example.maxxx.databinding.ActivityMainBinding
import com.example.maxxx.ui.viewmodel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var map: GoogleMap? = null
    private val viewModel: MainViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    PlaceRepository(),
                    RouteRepository()
                ) as T
            }

            private fun RouteRepository(): RouteRepository {
                TODO("Not yet implemented")
            }
        }
    }
    private lateinit var adapter: SearchResultsAdapter
    private var polyline: Polyline? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        setupMapFragment()
        setupLocationButton()
        setupSearchBar()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.searchResultsRecycler
        adapter = SearchResultsAdapter(emptyList(), onItemClick = { selectedPlace ->
            binding.searchBar.setText(selectedPlace.name)
            binding.searchResultsRecycler.visibility = View.GONE
            hideKeyboard()
            animateToLocationSmoothly(selectedPlace.location)
            // Mostrar el cardview informativo
            binding.placeCardContainer.root.visibility = View.VISIBLE
            binding.placeCardContainer.placeName.text = selectedPlace.name
            binding.placeCardContainer.placeAddress.text = selectedPlace.address
            // Agrega más campos si tienes más info
            // Solicitar ruta al ViewModel
            viewModel.fetchRoute(selectedPlace.location)
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


    private fun setupLocationButton() {
        binding.btnMyLocation.setOnClickListener {
            checkLocationPermissionAndFetchLocation()
        }
    }

    private fun checkLocationPermissionAndFetchLocation() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getDeviceLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Podrías mostrar un diálogo aquí explicando por qué necesitas el permiso
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getDeviceLocation()
        } else {
            // Manejar caso de denegación
        }
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    viewModel.updateUserLocation(it.latitude, it.longitude)
                    animateToLocationSmoothly(userLatLng)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupSearchBar() {
        val clearIcon = R.drawable.ic_clear

        fun updateClearIcon() {
            val icon = if (binding.searchBar.text?.isNotEmpty() == true) {
                resources.getDrawable(clearIcon, null)
            } else null

            binding.searchBar.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        }

        binding.searchBar.setOnTouchListener { _, event ->
            val drawableEnd = binding.searchBar.compoundDrawables[2] ?: return@setOnTouchListener false
            val touchX = event.x.toInt()
            val iconStart = binding.searchBar.width - binding.searchBar.paddingEnd - drawableEnd.intrinsicWidth
            if (touchX >= iconStart) {
                binding.searchBar.text?.clear()
                true
            } else false
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                updateClearIcon()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
        viewModel.routePolyline.observe(this) { points ->
            drawRoute(points)
        }
    }

    private fun drawRoute(points: List<LatLng>) {
        map?.clear()
        polyline = map?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(resources.getColor(R.color.black, null))
                .width(12f)
        )
        if (points.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            points.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            val padding = 100
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            map?.animateCamera(cu)
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

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map?.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        val initialLocation = LatLng(-19.03332, -65.26274)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))

        viewModel.userLocation.value?.let {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    private fun animateToLocationSmoothly(location: LatLng) {
        map?.clear()

        val marker = map?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Ubicación seleccionada")
        )

        marker?.let { bounceMarker(it) }

        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(17f)
            .tilt(45f)
            .bearing(90f)
            .build()

        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun bounceMarker(marker: Marker) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 1500
        animator.interpolator = BounceInterpolator()
        animator.addUpdateListener { animation ->
            val t = animation.animatedValue as Float
            marker.setAnchor(0.5f, 1.0f + t)
        }
        animator.start()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }
}