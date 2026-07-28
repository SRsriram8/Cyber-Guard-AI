package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegister: (username: String, email: String, pass: String, role: String, org: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Security Analyst") }
    var organization by remember { mutableStateOf("Global Defense SOC") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Real-time validation computed states
    val isNameError = username.isNotEmpty() && username.trim().length < 2
    val isEmailValid = email.isEmpty() || (email.contains("@") && email.contains(".") && email.trim().length >= 5)
    val isEmailError = email.isNotEmpty() && !isEmailValid

    // Password strength criteria
    val hasMinLength = password.length >= 6
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigitOrSymbol = password.any { it.isDigit() || !it.isLetterOrDigit() }
    val passwordScore = if (password.isEmpty()) 0 else listOf(hasMinLength, hasUppercase, hasDigitOrSymbol).count { it }

    val passwordStrengthText = when {
        password.isEmpty() -> ""
        passwordScore <= 1 -> "Weak"
        passwordScore == 2 -> "Medium"
        else -> "Strong"
    }

    val passwordStrengthColor = when {
        password.isEmpty() -> Color.Gray
        passwordScore <= 1 -> Color(0xFFFF5252)
        passwordScore == 2 -> Color(0xFFFFB300)
        else -> CyberEmerald
    }

    val isPasswordError = password.isNotEmpty() && !hasMinLength
    val isConfirmPasswordError = confirmPassword.isNotEmpty() && confirmPassword != password

    val securityRoles = listOf(
        "Security Analyst",
        "SOC Manager",
        "Penetration Tester",
        "Incident Responder",
        "Malware Researcher",
        "Security Engineer"
    )
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("registration_screen_container")
    ) {
        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CyberGuard AI • Registration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00363A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToReg,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW ANALYST REGISTRATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Create Security Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Register with Email & Password via Firebase Auth",
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

        // Registration Form Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "OPERATOR DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Full Name / Call Sign") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (isNameError) Color(0xFFFF5252) else CyberCyan) },
                    trailingIcon = {
                        if (username.isNotEmpty()) {
                            if (isNameError) {
                                Icon(Icons.Default.Error, contentDescription = "Invalid name", tint = Color(0xFFFF5252))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Valid name", tint = CyberEmerald)
                            }
                        }
                    },
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) {
                            Text("Call sign must be at least 2 characters", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isNameError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isNameError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Work Email Address") },
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
                            Text("Enter a valid work email (e.g. analyst@company.com)", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                        } else if (email.isNotEmpty()) {
                            Text("Valid email format", color = CyberEmerald, fontSize = 11.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEmailError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isEmailError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                // Role Selector
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Security Role") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = CyberCyan) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("register_role_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        securityRoles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    role = roleOption
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Organization / Company") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = CyberCyan) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_org_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password (Min 6 characters)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isPasswordError) Color(0xFFFF5252) else CyberCyan) },
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
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPasswordError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isPasswordError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                // Real-time Password Strength Meter & Checklist
                if (password.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASSWORD STRENGTH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = passwordStrengthText.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = passwordStrengthColor
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (passwordScore / 3f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = passwordStrengthColor,
                            trackColor = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Criteria checklist items
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasMinLength) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (hasMinLength) CyberEmerald else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "At least 6 characters (${password.length}/6)",
                                fontSize = 11.sp,
                                color = if (hasMinLength) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUppercase) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (hasUppercase) CyberEmerald else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Includes uppercase letter (A-Z)",
                                fontSize = 11.sp,
                                color = if (hasUppercase) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasDigitOrSymbol) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (hasDigitOrSymbol) CyberEmerald else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Includes number (0-9) or symbol (@#$)",
                                fontSize = 11.sp,
                                color = if (hasDigitOrSymbol) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = if (isConfirmPasswordError) Color(0xFFFF5252) else CyberCyan) },
                    trailingIcon = {
                        if (confirmPassword.isNotEmpty()) {
                            if (isConfirmPasswordError) {
                                Icon(Icons.Default.Error, contentDescription = "Mismatch", tint = Color(0xFFFF5252))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Matched", tint = CyberEmerald)
                            }
                        }
                    },
                    isError = isConfirmPasswordError,
                    supportingText = {
                        if (isConfirmPasswordError) {
                            Text("Passwords do not match", color = Color(0xFFFF8A8A), fontSize = 11.sp)
                        } else if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                            Text("Passwords match!", color = CyberEmerald, fontSize = 11.sp)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_confirm_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isConfirmPasswordError) Color(0xFFFF5252) else CyberCyan,
                        unfocusedBorderColor = if (isConfirmPasswordError) Color(0xFFFF5252) else CyberBorder,
                        errorBorderColor = Color(0xFFFF5252)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when {
                            username.isBlank() || email.isBlank() || password.isBlank() -> {
                                errorMessage = "Please fill in all required fields."
                            }
                            !email.contains("@") || !email.contains(".") -> {
                                errorMessage = "Please enter a valid email address."
                            }
                            password.length < 6 -> {
                                errorMessage = "Password must be at least 6 characters long."
                            }
                            password != confirmPassword -> {
                                errorMessage = "Passwords do not match."
                            }
                            else -> {
                                isLoading = true
                                onRegister(username, email, password, role, organization) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                        onNavigateHome()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REGISTER ACCOUNT", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Link to Login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an operator account? ",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(4.dp)
                    .testTag("nav_to_login_text")
            )
        }
    }
}
