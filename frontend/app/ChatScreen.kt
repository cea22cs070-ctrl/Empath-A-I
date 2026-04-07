@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("empath_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPref.getString("user_email", "") ?: ""

    val isDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 6..17
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 🔥 State preserved when navigating back
    var userInput by rememberSaveable { mutableStateOf("") }
    var userMessage by rememberSaveable { mutableStateOf("") }
    var modelMessage by rememberSaveable {
        mutableStateOf("Welcome back. How are you feeling today?")
    }
    var isTyping by rememberSaveable { mutableStateOf(false) }
    var avatarState by rememberSaveable { mutableStateOf(AvatarState.INTRO) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        avatarState = AvatarState.NORMAL
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background Image
        Image(
            painter = painterResource(if (isDay) R.drawable.bgday else R.drawable.bgnight),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ Top App Bar (Back button fixed properly)
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            title = {
                Text(
                    "Empath AI",
                    color = Color(0xFF3A6EA5),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mood History", fontFamily = Poppins) },
                        onClick = { menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", fontFamily = Poppins) },
                        onClick = { menuExpanded = false }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0B1C2D)
            )
        )

        // Avatar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .padding(top = 56.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedContent(
                targetState = avatarState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            ) { state ->
                val res = when (state) {
                    AvatarState.INTRO -> R.drawable.avtintro
                    AvatarState.TALKING -> R.drawable.avttaking
                    AvatarState.NORMAL -> R.drawable.avtnormal
                }

                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset(y = 90.dp),
                    contentScale = ContentScale.FillHeight
                )
            }
        }

        // Chat Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 450.dp)
                .background(Color(0xFF0B1C2D))
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {

                // Assistant Bubble
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E3A5F), shape = MaterialTheme.shapes.large)
                        .padding(14.dp)
                ) {
                    Text(
                        if (isTyping) "Typing…" else modelMessage,
                        color = Color.White,
                        fontFamily = Poppins
                    )
                }

                Spacer(Modifier.height(12.dp))

                // User Bubble
                if (userMessage.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3A6EA5), shape = MaterialTheme.shapes.large)
                                .padding(14.dp)
                        ) {
                            Text(
                                userMessage,
                                color = Color.White,
                                fontFamily = Poppins
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Input Field
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = {
                    Text("Type your message...", color = Color.White.copy(0.6f))
                },
                textStyle = TextStyle(color = Color.White, fontFamily = Poppins),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // Send Button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (userInput.isNotBlank()) {
                        val message = userInput
                        userMessage = message
                        userInput = ""

                        avatarState = AvatarState.TALKING
                        isTyping = true

                        scope.launch {
                            try {
                                val response = ApiClient.apiService.sendMessage(
                                    ChatRequest(
                                        user_id = userEmail,
                                        message = message
                                    )
                                )
                                modelMessage = response.reply
                            } catch (e: Exception) {
                                modelMessage = "Unable to connect. Please try again."
                            } finally {
                                isTyping = false
                                avatarState = AvatarState.NORMAL
                            }
                        }
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}