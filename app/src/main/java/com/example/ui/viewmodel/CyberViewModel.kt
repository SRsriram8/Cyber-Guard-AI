package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CyberGuardRepository
import com.example.data.local.AppDatabase
import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.QuizProgressEntity
import com.example.data.local.SavedReportEntity
import com.example.data.local.UserEntity
import com.example.data.preferences.FontSizeOption
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CyberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CyberGuardRepository(db.chatDao(), db.reportDao(), db.bookmarkDao(), db.progressDao(), db.userDao())
    private val settingsRepository = SettingsRepository(application)

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Active Chat Session State
    private val _activeSessionId = MutableStateFlow<String?>(null)

    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeSessionId.flatMapLatest { sessionId ->
        if (sessionId == null) flowOf(emptyList())
        else repository.getMessagesForSession(sessionId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<SavedReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress: StateFlow<List<QuizProgressEntity>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.DARK)

    val fontSize: StateFlow<FontSizeOption> = settingsRepository.fontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FontSizeOption.MEDIUM)

    val language: StateFlow<String> = settingsRepository.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")

    val offlineMode: StateFlow<Boolean> = settingsRepository.offlineModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val anonymizeLogs: StateFlow<Boolean> = settingsRepository.anonymizeLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // UI Loading & Streaming State
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText: StateFlow<String?> = _streamingText.asStateFlow()

    // Cloud Sync State
    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    // Active Tool selection
    private val _selectedTool = MutableStateFlow("GENERAL")
    val selectedTool: StateFlow<String> = _selectedTool.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    init {
        viewModelScope.launch {
            allSessions.collect { sessions ->
                if (_activeSessionId.value == null && sessions.isNotEmpty()) {
                    _activeSessionId.value = sessions.first().id
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.loggedInUserEmailFlow.collect { savedEmail ->
                if (!savedEmail.isNullOrBlank()) {
                    val user = repository.getUserByEmail(savedEmail)
                    _currentUser.value = user
                    syncCloudChatHistory()
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    fun syncCloudChatHistory(onResult: ((Boolean, String) -> Unit)? = null) {
        val userEmail = _currentUser.value?.email ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
        if (userEmail.isNullOrBlank()) {
            onResult?.invoke(false, "No logged-in account found to sync.")
            return
        }

        viewModelScope.launch {
            _isSyncingCloud.value = true
            _syncStatusMessage.value = "Syncing chats with Firestore..."
            val result = repository.syncCloudChatHistory(userEmail)
            _isSyncingCloud.value = false
            result.fold(
                onSuccess = { count ->
                    val msg = "Cloud sync successful ($count chats synced)."
                    _syncStatusMessage.value = msg
                    onResult?.invoke(true, msg)
                },
                onFailure = { err ->
                    val msg = "Sync failed: ${err.localizedMessage ?: "Unknown error"}"
                    _syncStatusMessage.value = msg
                    onResult?.invoke(false, msg)
                }
            )
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val fbResult = FirebaseAuthManager.signInWithEmailAndPassword(email, password)
            fbResult.fold(
                onSuccess = { authedEmail ->
                    var user = repository.getUserByEmail(authedEmail)
                    if (user == null) {
                        user = repository.registerUser(
                            username = authedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                            email = authedEmail,
                            passwordHash = password,
                            role = "Security Analyst",
                            organization = "Global Defense SOC"
                        )
                    } else {
                        repository.updateLastLogin(authedEmail)
                    }
                    settingsRepository.setLoggedInUserEmail(authedEmail)
                    _currentUser.value = user
                    syncCloudChatHistory()
                    onResult(true, "Firebase authentication successful!")
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Authentication failed."
                    val localUser = repository.getUserByEmail(email.trim())
                    if (localUser != null && localUser.passwordHash == password) {
                        repository.updateLastLogin(email.trim())
                        settingsRepository.setLoggedInUserEmail(email.trim())
                        _currentUser.value = localUser
                        onResult(true, "Signed in locally")
                    } else {
                        onResult(false, errorMsg)
                    }
                }
            )
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        role: String,
        organization: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val fbResult = FirebaseAuthManager.createUserWithEmailAndPassword(username, email, password)
            fbResult.fold(
                onSuccess = { authedEmail ->
                    val newUser = repository.registerUser(
                        username = username,
                        email = authedEmail,
                        passwordHash = password,
                        role = role,
                        organization = organization
                    )
                    settingsRepository.setLoggedInUserEmail(authedEmail)
                    _currentUser.value = newUser
                    syncCloudChatHistory()
                    onResult(true, "Firebase registration successful!")
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Registration failed."
                    val existing = repository.getUserByEmail(email.trim())
                    if (existing != null) {
                        onResult(false, "An account with this email address already exists.")
                        return@fold
                    }
                    val newUser = repository.registerUser(
                        username = username,
                        email = email,
                        passwordHash = password,
                        role = role,
                        organization = organization
                    )
                    settingsRepository.setLoggedInUserEmail(email.trim())
                    _currentUser.value = newUser
                    onResult(true, "Account created successfully")
                }
            )
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, "Please enter your email address.")
                return@launch
            }
            val result = FirebaseAuthManager.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    onResult(true, "Password reset link sent! Check your inbox.")
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Failed to send reset email."
                    onResult(false, errorMsg)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuthManager.signOut()
            settingsRepository.setLoggedInUserEmail(null)
            _currentUser.value = null
        }
    }

    fun setSelectedTool(toolType: String) {
        _selectedTool.value = toolType
    }

    fun setSelectedModel(modelName: String) {
        _selectedModel.value = modelName
    }

    fun startNewSession(title: String = "Cyber Security Analysis") {
        viewModelScope.launch {
            val id = repository.createNewSession(title)
            _activeSessionId.value = id
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    fun sendMessage(
        prompt: String,
        codeSnippet: String? = null,
        toolType: String = _selectedTool.value,
        modelName: String = _selectedModel.value
    ) {
        if (prompt.isBlank() && codeSnippet.isNullOrBlank()) return

        viewModelScope.launch {
            var currentSessionId = _activeSessionId.value
            if (currentSessionId == null) {
                currentSessionId = repository.createNewSession("Cyber Analysis")
                _activeSessionId.value = currentSessionId
            }

            _isGenerating.value = true
            _streamingText.value = ""
            try {
                val streamFlow = repository.sendMessageStream(
                    sessionId = currentSessionId,
                    userPrompt = prompt,
                    toolType = toolType,
                    codeSnippet = codeSnippet,
                    isOfflineMode = offlineMode.value,
                    modelName = modelName
                )
                val accumulated = StringBuilder()
                streamFlow.collect { chunk ->
                    accumulated.append(chunk)
                    _streamingText.value = accumulated.toString()
                }
            } finally {
                _isGenerating.value = false
                _streamingText.value = null
            }
        }
    }

    fun togglePinSession(sessionId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId, isPinned)
        }
    }

    fun toggleFavoriteSession(sessionId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteSession(sessionId, isFavorite)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                _activeSessionId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun saveCurrentReport(title: String, type: String, summary: String, content: String) {
        viewModelScope.launch {
            repository.saveReport(title, type, summary, content)
        }
    }

    fun deleteReport(id: String) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    fun addBookmark(title: String, category: String, snippet: String, content: String) {
        viewModelScope.launch {
            repository.addBookmark(title, category, snippet, content)
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    fun submitQuizScore(topicId: String, topicTitle: String, score: Int, total: Int) {
        viewModelScope.launch {
            repository.updateQuizProgress(topicId, topicTitle, score, total)
        }
    }

    fun toggleThemeMode() {
        viewModelScope.launch {
            val newMode = if (themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
            settingsRepository.setThemeMode(newMode)
        }
    }

    fun setFontSizeOption(option: FontSizeOption) {
        viewModelScope.launch {
            settingsRepository.setFontSize(option)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOfflineMode(enabled)
        }
    }

    fun toggleAnonymizeLogs(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnonymizeLogs(enabled)
        }
    }
}
