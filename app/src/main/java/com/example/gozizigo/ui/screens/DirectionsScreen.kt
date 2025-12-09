package com.example.gozizigo.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gozizigo.MainActivity
import com.example.gozizigo.map.PolylineDecoder
import com.example.gozizigo.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

/* ---------------------------------------------------------
   SAFELY GET ACTIVITY FROM ANY COMPOSE CONTEXT
--------------------------------------------------------- */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/* ---------------------------------------------------------
   DIRECTIONS SCREEN
--------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionsScreen(
    restaurantLat: Double,
    restaurantLng: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity() as? MainActivity

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val restaurantPos = LatLng(restaurantLat, restaurantLng)
    val cameraState = rememberCameraPositionState()

    /* ---------------------------------------------------------
       COLLECT LOCATION UPDATES FROM MAINACTIVITY (StateFlow)
    --------------------------------------------------------- */
    val locationState = activity?.locationFlow?.collectAsState()

    LaunchedEffect(locationState?.value) {
        val loc = locationState?.value ?: return@LaunchedEffect

        // Prevent fake (0.0,0.0) "Africa ocean" bug
        if (loc.lat == 0.0 && loc.lng == 0.0) return@LaunchedEffect

        userLat = loc.lat
        userLng = loc.lng
    }

    /* ---------------------------------------------------------
       PERMISSION HANDLING + START LOCATION UPDATES
    --------------------------------------------------------- */
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) activity?.startLocationUpdates()
    }

    LaunchedEffect(activity) {
        if (activity == null) return@LaunchedEffect

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            activity.startLocationUpdates()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* ---------------------------------------------------------
       FETCH WALKING ROUTE WHEN USER MOVES
    --------------------------------------------------------- */
    LaunchedEffect(userLat, userLng) {
        // Wait for real GPS
        if (userLat == null || userLng == null) return@LaunchedEffect
        if (userLat == 0.0 && userLng == 0.0) return@LaunchedEffect   // <-- FIX

        try {
            val response = RetrofitClient.api.getWalkingRoute(
                origin = "${userLat},${userLng}",
                destination = "$restaurantLat,$restaurantLng",
                apiKey = "YOUR_API_KEY"
            )

            if (response.routes.isNotEmpty()) {
                val encoded = response.routes[0].overview_polyline.points
                routePoints = PolylineDecoder.decode(encoded)
            }

            // Camera bounding
            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(restaurantPos)
            boundsBuilder.include(LatLng(userLat!!, userLng!!))
            routePoints.forEach { boundsBuilder.include(it) }

            cameraState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* ---------------------------------------------------------
       UI
    --------------------------------------------------------- */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Walking Directions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    )
    { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Wait until GPS is real
            if (userLat == null ||
                userLng == null ||
                (userLat == 0.0 && userLng == 0.0)
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Getting GPS location…")
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        compassEnabled = false
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    )
                ) {

                    /* USER MARKER */
                    Marker(
                        state = MarkerState(position = LatLng(userLat!!, userLng!!)),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        title = "You"
                    )

                    /* DESTINATION MARKER */
                    Marker(
                        state = MarkerState(position = restaurantPos),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        title = "Destination"
                    )

                    /* WALKING ROUTE */
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = Color(0xFF0066FF),
                            width = 12f
                        )
                    }
                }
            }
        }
    }
}
