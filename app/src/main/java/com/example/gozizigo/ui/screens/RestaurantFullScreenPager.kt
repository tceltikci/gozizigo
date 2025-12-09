package com.example.gozizigo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.gozizigo.MainActivity


// shapes
import androidx.compose.foundation.shape.RoundedCornerShape

// Your models & helpers
import com.example.gozizigo.model.Restaurant


// Google Maps Compose imports
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState

// Google Maps SDK imports
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.BitmapDescriptorFactory




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RestaurantFullScreenPager(
    restaurants: List<Restaurant>,
    userLat: Double,
    userLng: Double
) {
    val pagerState = rememberPagerState(pageCount = { restaurants.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        // Pass user location to the restaurant card
        RestaurantFullScreen(
            restaurant = restaurants[page],
            userLat = userLat,
            userLng = userLng
        )
    }
}

@Composable
fun RestaurantFullScreen(restaurant: Restaurant,
                         userLat: Double,
                         userLng: Double) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        if (userLat == 0.0 && userLng == 0.0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Getting location…")
            }
        } else {
            LiveMapWithUser(
                userLat = userLat,
                userLng = userLng,
                restaurantLat = restaurant.lat,
                restaurantLng = restaurant.lng
            )
        }




        // --- CONTENT AREA ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 300.dp),  // overlap image
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(Modifier.height(16.dp))

            // Restaurant Name
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            // Rating + Distance Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ⭐⭐⭐⭐☆ (mock rating)
                Text("⭐ 4.${(1..9).random()}", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.width(16.dp))

                // Distance badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE8F1FF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = restaurant.distance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2C4EFF)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category Tags (always mock)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Döner", "Fast Food", "Casual").forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFFF2F2F2))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = tag, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Short description mock
            Text(
                text = "Popular local döner spot with great portion sizes and fast service.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(Modifier.height(32.dp))

            val activity = LocalContext.current.findActivity() as? MainActivity

            Button(
                onClick = {
                    val route = "directions/${restaurant.lat}/${restaurant.lng}"
                    activity?.navController?.navigate(route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
            ) {
                Text(
                    text = "Show Walking Directions",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }
    }
}
@Composable
fun LiveMapWithUser(
    userLat: Double,
    userLng: Double,
    restaurantLat: Double,
    restaurantLng: Double
) {
    val cameraPositionState = rememberCameraPositionState()
    val user = LatLng(userLat, userLng)
    val dest = LatLng(restaurantLat, restaurantLng)

    // Just a straight line between user and restaurant
    val routePoints = remember(userLat, userLng, restaurantLat, restaurantLng) {
        listOf(user, dest)
    }

    // Center camera so both points fit nicely
    LaunchedEffect(userLat, userLng, restaurantLat, restaurantLng) {
        val boundsBuilder = LatLngBounds.builder()
        boundsBuilder.include(user)
        boundsBuilder.include(dest)
        val bounds = boundsBuilder.build()

        cameraPositionState.move(
            CameraUpdateFactory.newLatLngBounds(bounds, 120)
        )
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false
        ),
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            minZoomPreference = 15f,
            maxZoomPreference = 15f
        )
    ) {

        // 🔵 User marker
        Marker(
            state = MarkerState(position = user),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            title = "You"
        )

        // 🔴 Restaurant marker
        Marker(
            state = MarkerState(position = dest),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
            title = "Destination"
        )

        // 🟦 Straight "walking path" preview
        Polyline(
            points = routePoints,
            color = Color(0xFF007AFF),
            width = 12f
        )
    }
}


/*

@Composable
fun LiveMapWithUser(
    userLat: Double,
    userLng: Double,
    restaurantLat: Double,
    restaurantLng: Double
) {
    val cameraPositionState = rememberCameraPositionState()
    val user = LatLng(userLat, userLng)
    val dest = LatLng(restaurantLat, restaurantLng)

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Fetch route once when card loads
    LaunchedEffect(userLat, userLng, restaurantLat, restaurantLng) {
        val response = RetrofitClient.api.getWalkingRoute(
            origin = "${userLat},${userLng}",
            destination = "${restaurantLat},${restaurantLng}",
            apiKey = "YOUR_API_KEY"
        )

        if (response.routes.isNotEmpty()) {
            val polyline = response.routes[0].overview_polyline.points
            routePoints = PolylineDecoder.decode(polyline)
        }

        // Fit both markers & route
        val boundsBuilder = LatLngBounds.builder()
        boundsBuilder.include(user)
        boundsBuilder.include(dest)
        routePoints.forEach { boundsBuilder.include(it) }

        cameraPositionState.move(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
        )
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false
        ),
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            minZoomPreference = 15f,
            maxZoomPreference = 15f
        )
    ) {

        // 🔵 USER LOCATION MARKER
        Marker(
            state = MarkerState(position = user),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // 🔴 RESTAURANT MARKER
        Marker(
            state = MarkerState(position = dest),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )

        // 🟦 WALKING ROUTE (polyline)
        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = Color(0xFF007AFF),
                width = 12f
            )
        }
    }
*/




