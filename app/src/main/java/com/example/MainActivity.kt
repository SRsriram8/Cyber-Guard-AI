package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.preferences.ThemeMode
import com.example.ui.components.CyberBottomBar
import com.example.ui.components.CyberHeader
import com.example.ui.screens.*
import com.example.ui.theme.CyberGuardTheme
import com.example.ui.viewmodel.CyberViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CyberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = themeMode == ThemeMode.DARK

            CyberGuardTheme(darkTheme = isDarkTheme) {
                CyberGuardApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleThemeMode() }
                )
            }
        }
    }
}

@Composable
fun CyberGuardApp(
    viewModel: CyberViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()

    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val bookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val progressList by viewModel.allProgress.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val offlineMode by viewModel.offlineMode.collectAsStateWithLifecycle()
    val anonymizeLogs by viewModel.anonymizeLogs.collectAsStateWithLifecycle()

    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()

    var lastToolAnalysisResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!currentRoute.startsWith("tool_detail/") && currentRoute != "auth" && currentRoute != "login" && currentRoute != "register") {
                CyberHeader(
                    title = "CyberGuard AI",
                    isDarkTheme = isDarkTheme,
                    currentUser = currentUser,
                    onToggleTheme = onToggleTheme,
                    onNavigateToAuth = {
                        navController.navigate("login")
                    }
                )
            }
        },
        bottomBar = {
            if (!currentRoute.startsWith("tool_detail/") && currentRoute != "auth" && currentRoute != "login" && currentRoute != "register") {
                CyberBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    currentUser = currentUser,
                    onLogin = { email, pass, onResult ->
                        viewModel.login(email, pass, onResult)
                    },
                    onResetPassword = { resetEmail, onResult ->
                        viewModel.sendPasswordResetEmail(resetEmail, onResult)
                    },
                    onNavigateToRegister = {
                        navController.navigate("register") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                RegistrationScreen(
                    onRegister = { username, email, pass, role, org, onResult ->
                        viewModel.register(username, email, pass, role, org, onResult)
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("auth") {
                AuthScreen(
                    currentUser = currentUser,
                    onLogin = { email, pass, onResult ->
                        viewModel.login(email, pass, onResult)
                    },
                    onRegister = { username, email, pass, role, org, onResult ->
                        viewModel.register(username, email, pass, role, org, onResult)
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {

                HomeScreen(
                    sessions = sessions,
                    onNavigateToChat = { sessionId ->
                        if (sessionId != null) {
                            viewModel.selectSession(sessionId)
                        } else {
                            viewModel.startNewSession()
                        }
                        navController.navigate("chat")
                    },
                    onNavigateToTool = { toolId ->
                        viewModel.setSelectedTool(toolId)
                        navController.navigate("tool_detail/$toolId")
                    },
                    onNavigateToLearn = {
                        navController.navigate("learn")
                    }
                )
            }

            composable("chat") {
                ChatScreen(
                    messages = messages,
                    selectedTool = selectedTool,
                    isGenerating = isGenerating,
                    streamingText = streamingText,
                    onSendMessage = { prompt, snippet, tool ->
                        viewModel.sendMessage(prompt, snippet, tool)
                    },
                    onSelectTool = { tool ->
                        viewModel.setSelectedTool(tool)
                    },
                    onNewChat = {
                        viewModel.startNewSession()
                    }
                )
            }

            composable("tools") {
                ToolsScreen(
                    onSelectTool = { toolId ->
                        viewModel.setSelectedTool(toolId)
                        navController.navigate("tool_detail/$toolId")
                    }
                )
            }

            composable(
                route = "tool_detail/{toolId}",
                arguments = listOf(navArgument("toolId") { type = NavType.StringType })
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments?.getString("toolId") ?: "GENERAL"
                ToolDetailScreen(
                    toolId = toolId,
                    onBack = { navController.popBackStack() },
                    onRunAnalysis = { prompt, snippet, tool ->
                        viewModel.sendMessage(prompt, snippet, tool)
                        lastToolAnalysisResult = messages.lastOrNull { it.role == "assistant" }?.content
                    },
                    isGenerating = isGenerating,
                    lastAnalysisResult = messages.lastOrNull { it.role == "assistant" && it.toolType == toolId }?.content ?: lastToolAnalysisResult
                )
            }

            composable("learn") {
                LearnScreen(
                    progressList = progressList,
                    onSubmitQuizScore = { topicId, topicTitle, score, total ->
                        viewModel.submitQuizScore(topicId, topicTitle, score, total)
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    currentUser = currentUser,
                    sessions = sessions,
                    reports = reports,
                    bookmarks = bookmarks,
                    themeMode = themeMode,
                    fontSize = fontSize,
                    language = language,
                    offlineMode = offlineMode,
                    anonymizeLogs = anonymizeLogs,
                    isSyncingCloud = isSyncingCloud,
                    syncStatusMessage = syncStatusMessage,
                    onToggleTheme = onToggleTheme,
                    onSetFontSize = { viewModel.setFontSizeOption(it) },
                    onSetLanguage = { viewModel.setLanguage(it) },
                    onToggleOfflineMode = { viewModel.toggleOfflineMode(it) },
                    onToggleAnonymizeLogs = { viewModel.toggleAnonymizeLogs(it) },
                    onDeleteReport = { viewModel.deleteReport(it) },
                    onRemoveBookmark = { viewModel.removeBookmark(it) },
                    onSelectChat = { sessionId ->
                        viewModel.selectSession(sessionId)
                        navController.navigate("chat")
                    },
                    onClearAllHistory = {
                        sessions.forEach { viewModel.deleteSession(it.id) }
                    },
                    onSyncCloudHistory = {
                        viewModel.syncCloudChatHistory()
                    },
                    onNavigateToAuth = {
                        navController.navigate("login")
                    },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

        }
    }
}
