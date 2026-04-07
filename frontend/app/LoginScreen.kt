package com.empathai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current  // ✅ Added safely

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bglogin),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EMPATH AI",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "A safe space to share",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 235.dp)
                .align(Alignment.TopCenter)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    color = Color(0xFF0B2A4A).copy(alpha = 0.93f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "Login to get to your space",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    placeholder = {
                        Text("Email", color = Color.White.copy(alpha = 0.6f))
                    },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    placeholder = {
                        Text("Password", color = Color.White.copy(alpha = 0.6f))
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = Color.White
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter email and password"
                            return@Button
                        }

                        scope.launch {
                            try {
                                val response = AuthApiClient.authApi.login(
                                    LoginRequest(email, password)
                                )

                                if (response.isSuccessful) {

                                    // ✅ SAFE ADDITION (does NOT affect backend)
                                    val sharedPref = context.getSharedPreferences(
                                        "empath_prefs",
                                        android.content.Context.MODE_PRIVATE
                                    )

                                    sharedPref.edit()
                                        .putString("user_email", email)
                                        .apply()

                                    onLoginSuccess()

                                } else {
                                    errorMessage = "Invalid email or password"
                                }

                            } catch (e: Exception) {
                                errorMessage = "Unable to connect to server"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4FC3F7)
                    )
                ) {
                    Text("Login", color = Color.White)
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Forgot Password?",
                    color = Color(0xFF4FC3F7),
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Don’t have an account? Register",
                    color = Color(0xFF4FC3F7),
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}