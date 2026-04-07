@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onChatClick: () -> Unit,
    onMoodHistoryClick: () -> Unit,
    onAnalysisClick: () -> Unit,   // ✅ ADDED
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bgmenu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B2A4A).copy(alpha = 0.88f),
                            Color(0xFF123B5C).copy(alpha = 0.88f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = "Empath AI",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "A safe space for your mind",
                    color = Color(0xFFB6C7D6),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(14.dp))

                MenuButton("Chat with Therapist", Icons.Default.Chat, onChatClick)
                MenuButton("Mood History", Icons.Default.Timeline, onMoodHistoryClick)

                // ✅ NEW BUTTON
                MenuButton("See Your Analysis", Icons.Default.Timeline, onAnalysisClick)

                MenuButton("Settings", Icons.Default.Settings, onSettingsClick)

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color.White.copy(alpha = 0.18f))

                MenuButton(
                    text = "Logout",
                    icon = Icons.Default.Logout,
                    onClick = onLogoutClick,
                    background = Color(0xFF4A1E1E).copy(alpha = 0.9f),
                    textColor = Color(0xFFFFB4B4)
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    background: Color = Color(0xFF1F5E82).copy(alpha = 0.9f),
    textColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background)
    ) {
        Icon(icon, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = textColor, fontWeight = FontWeight.Medium)
    }
}