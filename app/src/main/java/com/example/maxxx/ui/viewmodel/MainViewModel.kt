package com.example.maxxx.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxxx.ui.model.Place
import com.example.maxxx.data.PlaceRepository
import com.example.maxxx.data.RouteRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MainViewModel(
    private val placeRepository: PlaceRepository = PlaceRepository(),
    private val routeRepository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _searchResults = MutableLiveData<List<Place>>()
    val searchResults: LiveData<List<Place>> = _searchResults

    private val _routePolyline = MutableLiveData<List<LatLng>>()
    val routePolyline: LiveData<List<LatLng>> = _routePolyline

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        _searchResults.value = placeRepository.searchPlaces(query)
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }

    fun fetchRoute(destination: LatLng) {
        val origin = _userLocation.value ?: return
        viewModelScope.launch {
            val polyline = routeRepository.getRoute(origin, destination)
            _routePolyline.value = polyline
        }
    }
}
