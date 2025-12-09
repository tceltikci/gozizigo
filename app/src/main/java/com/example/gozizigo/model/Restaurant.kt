package com.example.gozizigo.model

import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val name: String,
    val distance: String,
    val imageUrl: String = "",
    val lat: Double,
    val lng: Double,
    val routePoints: List<LatLng> = emptyList()
)
