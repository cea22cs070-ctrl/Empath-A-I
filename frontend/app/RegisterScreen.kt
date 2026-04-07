@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var place by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔹 BACKGROUND
        Image(
            painter = painterResource(id = R.drawable.bgregister),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 BACK BUTTON
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clickable { onBackToLogin() }
        )

        // 🔹 TITLE
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Register",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Your journey starts here",
                color = Color.White.copy(0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 🔹 REGISTER CARD
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 90.dp)
                .shadow(14.dp, RoundedCornerShape(22.dp))
                .background(
                    Color(0xFF0B2A4A).copy(alpha = 0.92f),
                    RoundedCornerShape(22.dp)
                )
                .padding(16.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Text(
                    text = "Fill to get your space",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                @Composable
                fun field(value: String, onChange: (String) -> Unit, hint: String) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        placeholder = { Text(hint) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // ✅ FIXED
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(0.6f),
                            focusedPlaceholderColor = Color.White.copy(0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(0.6f)
                        )
                    )
                }

                field(name, { name = it }, "Name")
                field(email, { email = it }, "Email")

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                field(age, { age = it }, "Age")

                // Gender
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Male", "Female", "Other").forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = gender == it,
                                onClick = { gender = it },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4FC3F7))
                            )
                            Text(it, color = Color.White)
                        }
                    }
                }

                field(place, { place = it }, "Place")
                field(securityQuestion, { securityQuestion = it }, "Security Question")
                field(securityAnswer, { securityAnswer = it }, "Security Answer")

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }

                        scope.launch {
                            try {
                                val response = AuthApiClient.authApi.register(
                                    RegisterRequest(
                                        name,
                                        email,
                                        password,
                                        age.toInt(),
                                        gender,
                                        place,
                                        securityQuestion,
                                        securityAnswer
                                    )
                                )

                                if (response.isSuccessful) onRegisterSuccess()
                                else errorMessage = "Registration failed"
                            } catch (e: Exception) {
                                errorMessage = "Unable to connect"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                ) {
                    Text("Register")
                }

                if (errorMessage.isNotBlank()) {
                    Text(errorMessage, color = Color(0xFFFF6B6B))
                }
            }
        }
    }
}