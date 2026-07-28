package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CyberGuardRepository
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.preferences.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val activeSessionId: String? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val selectedTool: String = "GENERAL",
    val selectedModel: String = "gemini-3.5-flash",
    val isGenerating: Boolean = false,
    val streamingText: String? = null,
    val errorMessage: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CyberGuardRepository(db.chatDao(), db.reportDao(), db.bookmarkDao(), db.progressDao(), db.userDao())
    private val settingsRepository = SettingsRepository(application)

    // Active Chat Session State
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    // Active Tool & Model Selections
    private val _selectedTool = MutableStateFlow("GENERAL")
    val selectedTool: StateFlow<String> = _selectedTool.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // Streaming and Generation State
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText: StateFlow<String?> = _streamingText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Active Messages from Room DB
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeSessionId.flatMapLatest { sessionId ->
        if (sessionId == null) flowOf(emptyList())
        else repository.getMessagesForSession(sessionId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Chat Sessions list
    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineMode: StateFlow<Boolean> = settingsRepository.offlineModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Combined UI State
    val uiState: StateFlow<ChatUiState> = combine(
        combine(_activeSessionId, activeMessages, _selectedTool, _selectedModel) { sessionId, messages, tool, model ->
            listOf<Any?>(sessionId, messages, tool, model)
        },
        combine(_isGenerating, _streamingText, _errorMessage) { isGen, streamText, error ->
            listOf<Any?>(isGen, streamText, error)
        }
    ) { group1, group2 ->
        @Suppress("UNCHECKED_CAST")
        ChatUiState(
            activeSessionId = group1[0] as String?,
            messages = group1[1] as List<ChatMessageEntity>,
            selectedTool = group1[2] as String,
            selectedModel = group1[3] as String,
            isGenerating = group2[0] as Boolean,
            streamingText = group2[1] as String?,
            errorMessage = group2[2] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    private var activeGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            allSessions.collect { sessions ->
                if (_activeSessionId.value == null && sessions.isNotEmpty()) {
                    _activeSessionId.value = sessions.first().id
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    fun selectTool(toolType: String) {
        _selectedTool.value = toolType
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
    }

    fun startNewChat(title: String = "Cyber Security Analysis") {
        viewModelScope.launch {
            val id = repository.createNewSession(title)
            _activeSessionId.value = id
        }
    }

    fun sendMessage(
        prompt: String,
        codeSnippet: String? = null,
        toolType: String = _selectedTool.value,
        modelName: String = _selectedModel.value
    ) {
        if (prompt.isBlank() && codeSnippet.isNullOrBlank()) return

        // Cancel any active generation before starting a new one
        stopGeneration()

        activeGenerationJob = viewModelScope.launch {
            var currentSessionId = _activeSessionId.value
            if (currentSessionId == null) {
                currentSessionId = repository.createNewSession("Cyber Analysis")
                _activeSessionId.value = currentSessionId
            }

            _isGenerating.value = true
            _streamingText.value = ""
            _errorMessage.value = null

            try {
                val isOffline = offlineMode.value
                val streamFlow = repository.sendMessageStream(
                    sessionId = currentSessionId,
                    userPrompt = prompt,
                    toolType = toolType,
                    codeSnippet = codeSnippet,
                    isOfflineMode = isOffline,
                    modelName = modelName
                )

                val accumulated = StringBuilder()
                streamFlow.collect { chunk ->
                    accumulated.append(chunk)
                    _streamingText.value = accumulated.toString()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete AI response: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
                _streamingText.value = null
            }
        }
    }

    fun stopGeneration() {
        if (activeGenerationJob?.isActive == true) {
            activeGenerationJob?.cancel()
            activeGenerationJob = null
            _isGenerating.value = false
            _streamingText.value = null
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

    fun clearError() {
        _errorMessage.value = null
    }
}
