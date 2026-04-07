@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/* -------------------- STEP ENUM -------------------- */
enum class ForgotStep { EMAIL, SECURITY, RESET }

/* -------------------- SCREEN -------------------- */
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {
    var step by remember { mutableStateOf(ForgotStep.EMAIL) }

    var email by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.bgforgot),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .clickable { onBackToLogin() }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .shadow(18.dp, RoundedCornerShape(22.dp))
                .background(
                    Color(0xFF0B2A4A).copy(alpha = 0.94f),
                    RoundedCornerShape(22.dp)
                )
                .padding(24.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text(
                    text = "Forgot Password",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )

                when (step) {

                    /* ---------- STEP 1 : EMAIL ---------- */
                    ForgotStep.EMAIL -> {

                        InputField(email, { email = it }, "Email")

                        PrimaryButton("Next") {
                            if (email.isBlank()) {
                                error = "Enter email"
                                return@PrimaryButton
                            }

                            loading = true
                            scope.launch {
                                try {
                                    val response =
                                        AuthApiClient.authApi.forgotPasswordEmail(
                                            ForgotEmailRequest(email)
                                        )

                                    if (response.isSuccessful) {
                                        securityQuestion =
                                            response.body()?.securityQuestion.orEmpty()

                                        step = ForgotStep.SECURITY
                                        error = ""
                                    } else {
                                        error = "Email not found"
                                    }
                                } catch (e: Exception) {
                                    error = "Email not found"
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }

                    /* ---------- STEP 2 : SECURITY ---------- */
                    ForgotStep.SECURITY -> {

                        Text(
                            text = securityQuestion,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        InputField(
                            securityAnswer,
                            { securityAnswer = it },
                            "Your Answer"
                        )

                        PrimaryButton("Verify") {
                            if (securityAnswer.isBlank()) {
                                error = "Answer required"
                                return@PrimaryButton
                            }

                            loading = true
                            scope.launch {
                                try {
                                    AuthApiClient.authApi.forgotPasswordVerify(
                                        ForgotVerifyRequest(email, securityAnswer)
                                    )
                                    step = ForgotStep.RESET
                                    error = ""
                                } catch (e: Exception) {
                                    error = "Wrong security answer"
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }

                    /* ---------- STEP 3 : RESET ---------- */
                    ForgotStep.RESET -> {

                        PasswordField(
                            value = newPassword,
                            onChange = { newPassword = it },
                            label = "New Password",
                            visible = showNew
                        ) { showNew = !showNew }

                        PasswordField(
                            value = confirmPassword,
                            onChange = { confirmPassword = it },
                            label = "Confirm Password",
                            visible = showConfirm
                        ) { showConfirm = !showConfirm }

                        PrimaryButton("Update Password") {
                            if (newPassword != confirmPassword) {
                                error = "Passwords do not match"
                                return@PrimaryButton
                            }

                            loading = true
                            scope.launch {
                                try {
                                    AuthApiClient.authApi.forgotPasswordReset(
                                        ForgotResetRequest(email, newPassword)
                                    )
                                    onBackToLogin()
                                } catch (e: Exception) {
                                    error = "Reset failed"
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }
                }

                if (error.isNotBlank()) {
                    Text(error, color = Color(0xFFFF6B6B))
                }
            }
        }
    }
}

/* -------------------- REUSABLE UI -------------------- */

@Composable
fun InputField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    toggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(label) },
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = toggle) {
                Icon(
                    if (visible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = Color.White)
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
    ) {
        Text(text)
    }
}