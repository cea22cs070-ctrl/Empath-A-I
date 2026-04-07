package com.empathai.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    userId: String,
    onBack: () -> Unit
) {

    var analysisData by remember { mutableStateOf<AnalysisResponse?>(null) }
    var showTherapistPopup by remember { mutableStateOf(false) }
    var showPsychologistPopup by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {

        try {
            analysisData = ApiClient.apiService.getUserAnalysis(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                    if (location != null) {
                        userLat = location.latitude
                        userLng = location.longitude
                    }

                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Your Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.bganalysis),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 40.dp, vertical = 120.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                analysisData?.let { data ->

                    Text(
                        text = "This is your detailed analysis report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (data.analysis_words.isBlank()) {

                        Text(
                            text = "No analysis available yet.",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                    } else {

                        Text(
                            text = "You are experiencing:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val words = data.analysis_words.split(",")

                        words.forEach { word ->
                            Text(
                                text = "• ${word.trim()}",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        when (data.support_level) {

                            "self_support" -> {
                                Text(
                                    text = "You can focus on self-care and continue chatting to improve your emotional clarity.",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }

                            "therapist_required" -> {

                                Text(
                                    text = "It may help to speak with a therapist.",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Click here to view therapists near you.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Blue,
                                    modifier = Modifier.clickable {
                                        showTherapistPopup = true
                                    }
                                )
                            }

                            "psychologist_required" -> {

                                Text(
                                    text = "We recommend consulting a psychologist for professional support.",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Click here to view psychologists near you.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Blue,
                                    modifier = Modifier.clickable {
                                        showPsychologistPopup = true
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Last Updated: ${data.last_updated}",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // ======================
            // THERAPIST POPUP
            // ======================

            if (showTherapistPopup) {

                val therapists =
                    if (userLat != null && userLng != null)
                        TherapistDatabase.therapists
                            .map { therapist ->
                                Pair(
                                    therapist,
                                    calculateDistance(
                                        userLat!!,
                                        userLng!!,
                                        therapist.latitude,
                                        therapist.longitude
                                    )
                                )
                            }
                            .filter { it.second <= 50 }
                            .sortedBy { it.second }
                            .map { it.first }
                    else
                        TherapistDatabase.therapists

                Dialog(onDismissRequest = { showTherapistPopup = false }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xCC2196F3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                text = "Therapists Near You",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            therapists.forEach { therapist ->

                                Text(
                                    text = therapist.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = therapist.address,
                                    color = Color.White
                                )

                                Text(
                                    text = therapist.phone,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Button(onClick = { showTherapistPopup = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }

            // ======================
            // PSYCHOLOGIST POPUP
            // ======================

            if (showPsychologistPopup) {

                val doctors =
                    if (userLat != null && userLng != null)
                        TherapistDatabase.psychologists
                            .map { doctor ->
                                Pair(
                                    doctor,
                                    calculateDistance(
                                        userLat!!,
                                        userLng!!,
                                        doctor.latitude,
                                        doctor.longitude
                                    )
                                )
                            }
                            .filter { it.second <= 50 }
                            .sortedBy { it.second }
                            .map { it.first }
                    else
                        TherapistDatabase.psychologists

                Dialog(onDismissRequest = { showPsychologistPopup = false }) {

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xCC1976D2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                text = "Psychologists Near You",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            doctors.forEach { doctor ->

                                Text(
                                    text = doctor.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = doctor.address,
                                    color = Color.White
                                )

                                Text(
                                    text = doctor.phone,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Button(onClick = { showPsychologistPopup = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {

    val R = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a =
        sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}