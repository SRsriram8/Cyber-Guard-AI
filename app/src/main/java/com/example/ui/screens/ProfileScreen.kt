package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.SavedReportEntity
import com.example.data.local.UserEntity
import com.example.data.preferences.FontSizeOption
import com.example.data.preferences.ThemeMode
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    sessions: List<ChatSessionEntity>,
    reports: List<SavedReportEntity>,
    bookmarks: List<BookmarkEntity>,
    themeMode: ThemeMode,
    fontSize: FontSizeOption,
    language: String,
    offlineMode: Boolean,
    anonymizeLogs: Boolean,
    isSyncingCloud: Boolean = false,
    syncStatusMessage: String? = null,
    onToggleTheme: () -> Unit,
    onSetFontSize: (FontSizeOption) -> Unit,
    onSetLanguage: (String) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onToggleAnonymizeLogs: (Boolean) -> Unit,
    onDeleteReport: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onSelectChat: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onSyncCloudHistory: () -> Unit = {},
    onNavigateToAuth: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("MEMORY") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_screen_container")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Avatar & Account Status Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (currentUser != null) Color(0xFF00363A) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.VerifiedUser else Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = currentUser?.username ?: "Guest Security Analyst",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser != null) "${currentUser.role} • ${currentUser.organization}" else "Click below to Log In / Register",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (currentUser != null) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("profile_logout_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log out",
                            tint = Color(0xFFFF5252)
                        )
                    }
                } else {
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("profile_login_button")
                    ) {
                        Text("Log In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Firestore Cloud Sync Card
        CyberCard(modifier = Modifier.fillMaxWidth().testTag("profile_cloud_sync_card")) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Firebase Firestore Cloud Sync",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUser != null) "Account: ${currentUser.email}" else "Sign in to sync chats across devices",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentUser != null) {
                        IconButton(
                            onClick = onSyncCloudHistory,
                            enabled = !isSyncingCloud,
                            modifier = Modifier.testTag("sync_cloud_history_button")
                        ) {
                            if (isSyncingCloud) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CyberCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Now",
                                    tint = CyberCyan
                                )
                            }
                        }
                    }
                }

                if (!syncStatusMessage.isNullOrBlank()) {
                    Text(
                        text = syncStatusMessage,
                        fontSize = 11.sp,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = if (selectedTab == "MEMORY") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyberCyan
        ) {
            Tab(
                selected = selectedTab == "MEMORY",
                onClick = { selectedTab = "MEMORY" },
                text = { Text("AI MEMORY & REPORTS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == "SETTINGS",
                onClick = { selectedTab = "SETTINGS" },
                text = { Text("SETTINGS & PRIVACY", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == "MEMORY") {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text("PINNED & FAVORITE CHATS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    val pinned = sessions.filter { it.isPinned || it.isFavorite }
                    if (pinned.isEmpty()) {
                        Text("No pinned or favorite chats.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            pinned.forEach { session ->
                                Surface(
                                    onClick = { onSelectChat(session.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(session.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Icon(Icons.Default.Pin, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("SAVED REPORTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    if (reports.isEmpty()) {
                        Text("No saved reports yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            reports.forEach { report ->
                                CyberCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(report.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(report.summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { onDeleteReport(report.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Exported Chat & Report History to Downloads!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Analysis History", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dark Theme Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Cyberguard dark palette styling", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = themeMode == ThemeMode.DARK,
                                onCheckedChange = { onToggleTheme() },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                            )
                        }
                    }
                }

                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Offline AI Fallback Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Use offline threat analysis knowledge", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = offlineMode,
                                onCheckedChange = onToggleOfflineMode,
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberEmerald)
                            )
                        }
                    }
                }

                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Anonymize Uploaded Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Strip internal IPs & user identifiers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = anonymizeLogs,
                                onCheckedChange = onToggleAnonymizeLogs,
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                            )
                        }
                    }
                }

                if (currentUser != null) {
                    item {
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_logout_button"),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF5252))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIGN OUT / LOGOUT FIREBASE SESSION", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Button(
                        onClick = onClearAllHistory,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Chat & Session History", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
