package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    currentUser: UserEntity?,
    onLogin: (email: String, pass: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onResetPassword: (email: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onNavigateToRegister: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forgot Password Dialog State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isSendingReset by remember { mutableStateOf(false) }
    var resetDialogMessage by remember { mutableStateOf<String?>(null) }
    var isResetSuccess by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Real-time validation computed states
    val isEmailValid = email.isEmpty() || (email.contains("@") && email.contains(".") && email.trim().length >= 5)
    val isEmailError = email.isNotEmpty() && !isEmailValid
    val isPasswordValid = password.isEmpty() || password.length >= 6
    val isPasswordError = password.isNotEmpty() && !isPasswordValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("login_screen_container")
    ) {
        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CyberGuard AI • Sign In",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Identity Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00363A))
                        .border(1.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FIREBASE AUTHENTICATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SOC Analyst Access Terminal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sign in with Email & Password to protect threat data",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error Banner
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                color = Color(0xFF3E1212),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF8A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Login Form Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "ENTER ACCOUNT CREDENTIALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isEmailError) Color(0xFFFF5252) else CyberCyan) },
                    trailingIcon = {
                        if (email.isNotEmpty()) {
                            if (isEmailError) {
                                Icon(Icons.Default.Error, contentDescription = "Invalid email", tint = Color(0xFFFF5252))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Valid email", tint = CyberEmerald)
                            }
                        }
                    },
                    isError = isEmailError,
                    supportingText = {
                        if (isEmailError) {
                            Text("Enter a valid email address (e.g. analyst@domain.com)", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                        } else if (email.isNotEmpty()) {
                            Text("Valid email format", color = CyberEmerald, fontSize = 11.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEmailError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isEmailError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = if (isPasswordError) Color(0xFFFF5252) else CyberCyan) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (password.isNotEmpty()) {
                                if (isPasswordError) {
                                    Icon(Icons.Default.Warning, contentDescription = "Weak password", tint = Color(0xFFFF5252))
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = "Valid password length", tint = CyberEmerald)
                                }
                            }
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    isError = isPasswordError,
                    supportingText = {
                        if (isPasswordError) {
                            Text("Password must be at least 6 characters (${password.length}/6)", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                        } else if (password.isNotEmpty()) {
                            Text("Password length OK (${password.length} chars)", color = CyberEmerald, fontSize = 11.sp)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPasswordError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isPasswordError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                // Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan,
                        modifier = Modifier
                            .clickable {
                                resetEmail = email
                                resetDialogMessage = null
                                isResetSuccess = false
                                showForgotPasswordDialog = true
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .testTag("forgot_password_button")
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both email and password."
                            return@Button
                        }
                        isLoading = true
                        onLogin(email, password) { success, msg ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
                                onNavigateHome()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIGN IN WITH FIREBASE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick Demo Analyst Sign In
                OutlinedButton(
                    onClick = {
                        email = "analyst@cyberguard.ai"
                        password = "Password123!"
                        focusManager.clearFocus()
                        isLoading = true
                        onLogin(email, password) { success, msg ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Welcome Lead SOC Analyst!", Toast.LENGTH_SHORT).show()
                                onNavigateHome()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_demo_login_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = CyberEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QUICK DEMO ANALYST LOGIN", color = CyberEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation link to Registration Screen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an operator account? ",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Register Here",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                modifier = Modifier
                    .clickable { onNavigateToRegister() }
                    .padding(4.dp)
                    .testTag("nav_to_register_text")
            )
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            val isResetEmailValid = resetEmail.contains("@") && resetEmail.contains(".") && resetEmail.trim().length >= 5
            val isResetEmailError = resetEmail.isNotEmpty() && !isResetEmailValid

            AlertDialog(
                onDismissRequest = {
                    if (!isSendingReset) showForgotPasswordDialog = false
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Enter your work email address below. We will send a Firebase password reset link to your inbox.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetDialogMessage = null
                            },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = if (isResetEmailError) Color(0xFFFF5252) else CyberCyan)
                            },
                            trailingIcon = {
                                if (resetEmail.isNotEmpty()) {
                                    if (isResetEmailError) {
                                        Icon(Icons.Default.Error, contentDescription = "Invalid", tint = Color(0xFFFF5252))
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = CyberEmerald)
                                    }
                                }
                            },
                            isError = isResetEmailError,
                            supportingText = {
                                if (isResetEmailError) {
                                    Text("Enter a valid email address", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_password_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isResetEmailError) Color(0xFFFF5252) else CyberCyan,
                                unfocusedBorderColor = if (isResetEmailError) Color(0xFFFF5252) else CyberBorder
                            )
                        )

                        resetDialogMessage?.let { msg ->
                            Surface(
                                color = if (isResetSuccess) Color(0xFF0F382A) else Color(0xFF3E1212),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isResetSuccess) CyberEmerald else Color(0xFFFF5252)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = if (isResetSuccess) CyberEmerald else Color(0xFFFF8A8A),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isResetEmailValid) {
                                resetDialogMessage = "Please enter a valid email address."
                                isResetSuccess = false
                                return@Button
                            }
                            isSendingReset = true
                            resetDialogMessage = null
                            onResetPassword(resetEmail) { success, msg ->
                                isSendingReset = false
                                isResetSuccess = success
                                resetDialogMessage = msg
                            }
                        },
                        enabled = !isSendingReset && isResetEmailValid,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("send_reset_email_button")
                    ) {
                        if (isSendingReset) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Send Reset Link", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false },
                        enabled = !isSendingReset,
                        modifier = Modifier.testTag("cancel_reset_password_button")
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
