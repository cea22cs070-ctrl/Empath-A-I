@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MoodHistoryScreen(
    onBack: () -> Unit = {}
) {

    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("empath_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPref.getString("user_email", "") ?: ""

    var sessions by remember { mutableStateOf<List<SessionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (userEmail.isNotBlank()) {
            sessions = ApiClient.apiService.getSessions(userEmail)
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bgmood),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Mood History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions) { session ->
                        SessionCard(session)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: SessionResponse) {

    val (mainText, adviceText) = cleanSummary(session.summary)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5).copy(alpha = 0.85f),
                        Color(0xFF1565C0).copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {

            Text(
                text = session.date,
                color = Color(0xFFBBDEFB)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Paragraph (White + Same Size)
            Text(
                text = mainText,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            if (adviceText.isNotEmpty()) {

                Spacer(modifier = Modifier.height(18.dp))

                // Advice Title (Yellow Only This)
                Text(
                    text = "Advice",
                    color = Color(0xFFFFF176),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Advice Body (White Same Size)
                Text(
                    text = adviceText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/* ============================= */
/* SUMMARY CLEANING FUNCTION     */
/* ============================= */

fun cleanSummary(raw: String): Pair<String, String> {

    var text = raw
        .replace("Assistant:", "Advice:")
        .replace("\n", " ")
        .trim()

    val parts = text.split("Advice:")

    var main = parts[0].trim()
    var advice = if (parts.size > 1) parts[1].trim() else ""

    // Remove duplicated ending text
    if (advice.contains(main.takeLast(40))) {
        advice = advice.replace(main.takeLast(40), "")
    }

    return Pair(main, advice)
}