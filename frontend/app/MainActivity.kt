package com.empathai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import com.empathai.app.ui.theme.EmpathAITheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EmpathAITheme {

                var currentScreen by remember { mutableStateOf("LOGIN") }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() + scaleIn(initialScale = 0.98f) togetherWith
                                fadeOut() + scaleOut(targetScale = 1.02f)
                    },
                    label = "ScreenTransition"
                ) { screen ->

                    when (screen) {

                        // LOGIN
                        "LOGIN" -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    currentScreen = "MENU"
                                },
                                onRegisterClick = {
                                    currentScreen = "REGISTER"
                                },
                                onForgotPasswordClick = {
                                    currentScreen = "FORGOT"
                                }
                            )
                        }

                        // REGISTER
                        "REGISTER" -> {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    currentScreen = "LOGIN"
                                },
                                onBackToLogin = {
                                    currentScreen = "LOGIN"
                                }
                            )
                        }

                        // FORGOT PASSWORD
                        "FORGOT" -> {
                            ForgotPasswordScreen(
                                onBackToLogin = {
                                    currentScreen = "LOGIN"
                                }
                            )
                        }

                        // MAIN MENU
                        "MENU" -> {
                            MainMenuScreen(
                                onChatClick = { currentScreen = "CHAT" },
                                onMoodHistoryClick = { currentScreen = "MOOD" },
                                onAnalysisClick = { currentScreen = "ANALYSIS" }, // ✅ Added
                                onSettingsClick = { currentScreen = "SETTINGS" },
                                onLogoutClick = {

                                    val sharedPref = getSharedPreferences("empath_prefs", MODE_PRIVATE)
                                    val userEmail = sharedPref.getString("user_email", "") ?: ""

                                    if (userEmail.isNotBlank()) {
                                        lifecycleScope.launch {
                                            try {
                                                ApiClient.apiService.endSession(userEmail)
                                            } catch (e: Exception) {
                                                // ignore error
                                            }
                                        }
                                    }

                                    sharedPref.edit().clear().apply()
                                    currentScreen = "LOGIN"
                                }
                            )
                        }

                        // CHAT
                        "CHAT" -> {
                            ChatScreen(
                                onBackClick = {
                                    currentScreen = "MENU"
                                }
                            )
                        }

                        // MOOD HISTORY
                        "MOOD" -> {
                            MoodHistoryScreen(
                                onBack = { currentScreen = "MENU" }
                            )
                        }

                        // ✅ ANALYSIS SCREEN (NEW)
                        "ANALYSIS" -> {
                            val sharedPref = getSharedPreferences("empath_prefs", MODE_PRIVATE)
                            val userEmail = sharedPref.getString("user_email", "") ?: ""

                            AnalysisScreen(
                                userId = userEmail,
                                onBack = { currentScreen = "MENU" }
                            )
                        }

                        // SETTINGS
                        "SETTINGS" -> {
                            SettingsScreen(
                                onBack = { currentScreen = "MENU" }
                            )
                        }
                    }
                }
            }
        }
    }

    // STORE SESSION WHEN APP STOPS
    override fun onStop() {
        super.onStop()

        val sharedPref = getSharedPreferences("empath_prefs", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "") ?: ""

        if (userEmail.isNotBlank()) {
            lifecycleScope.launch {
                try {
                    ApiClient.apiService.endSession(userEmail)
                } catch (e: Exception) {
                    // ignore error
                }
            }
        }
    }
}