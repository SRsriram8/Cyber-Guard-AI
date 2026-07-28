package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.util.PdfExportUtils
import com.example.ui.components.CyberMarkdownText
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceVariantDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    selectedTool: String,
    isGenerating: Boolean,
    streamingText: String? = null,
    onSendMessage: (String, String?, String) -> Unit,
    onSelectTool: (String) -> Unit,
    onNewChat: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var codeSnippetText by remember { mutableStateOf("") }
    var showSnippetInput by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf(selectedTool) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val samplePrompts = listOf(
        "Explain ransomware and how it spreads.",
        "What is SQL Injection and how to prevent it?",
        "Explain Kerberos Authentication protocol.",
        "How does HTTPS & TLS 1.3 encryption work?",
        "What is Zero Trust Security Architecture?",
        "Explain Buffer Overflow vulnerabilities.",
        "What is the difference between IDS and IPS?"
    )

    val toolsList = listOf(
        "GENERAL" to "General AI",
        "MALWARE" to "Malware Analysis",
        "CODE" to "Code Audit",
        "LINUX" to "Linux Admin",
        "CVE" to "CVE Explorer",
        "THREAT" to "Log Hunting",
        "SCRIPT" to "Script Gen",
        "NETWORKING" to "Networking",
        "INCIDENT" to "Incident Response"
    )

    LaunchedEffect(messages.size, streamingText, isGenerating) {
        val hasStreaming = !streamingText.isNullOrEmpty()
        val totalItems = messages.size + if (hasStreaming || isGenerating) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_screen_container")
    ) {
        // Top Tool Selector & New Chat Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(toolsList) { (key, name) ->
                    val isSelected = activeTool == key
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeTool = key
                            onSelectTool(key)
                        },
                        label = {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        PdfExportUtils.exportChatToPdf(
                            context = context,
                            chatTitle = activeTool,
                            messages = messages
                        )
                    },
                    enabled = messages.isNotEmpty(),
                    modifier = Modifier.testTag("export_chat_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        tint = if (messages.isNotEmpty()) CyberCyan else Color.Gray
                    )
                }

                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "New Chat",
                        tint = CyberCyan
                    )
                }
            }
        }

        Divider(color = CyberBorder, thickness = 0.5.dp)

        // Chat Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                EmptyChatView(
                    samplePrompts = samplePrompts,
                    onSelectPrompt = { prompt ->
                        inputText = prompt
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (!streamingText.isNullOrEmpty()) {
                        item {
                            ChatMessageItem(
                                message = ChatMessageEntity(
                                    sessionId = "",
                                    role = "assistant",
                                    content = streamingText + " ▌",
                                    toolType = activeTool
                                )
                            )
                        }
                    } else if (isGenerating) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // Code Snippet drawer if active
        AnimatedVisibility(visible = showSnippetInput) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariantDark)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATTACH CODE / LOG SNIPPET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )

                        IconButton(
                            onClick = { showSnippetInput = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = codeSnippetText,
                        onValueChange = { codeSnippetText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("snippet_text_field"),
                        placeholder = { Text("Paste code, log lines, or payload...", fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }

        // Bottom Input Field Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showSnippetInput = !showSnippetInput },
                    modifier = Modifier.testTag("attach_snippet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Attach Code Snippet",
                        tint = if (codeSnippetText.isNotBlank()) CyberEmerald else CyberCyan
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = {
                        Text(
                            text = "Ask CyberGuard AI...",
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = CyberBorder,
                        focusedBorderColor = CyberCyan
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() || codeSnippetText.isNotBlank()) {
                            val msg = inputText
                            val snippet = if (codeSnippetText.isNotBlank()) codeSnippetText else null
                            inputText = ""
                            codeSnippetText = ""
                            showSnippetInput = false

                            onSendMessage(msg, snippet, activeTool)

                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_message_button"),
                    containerColor = CyberCyan,
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatView(
    samplePrompts: List<String>,
    onSelectPrompt: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00363A))
                    .border(1.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CyberGuard AI Assistant",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ask any security question or tap a suggestion below:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(samplePrompts) { prompt ->
            Surface(
                onClick = { onSelectPrompt(prompt) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = prompt,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00363A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) CyberCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) CyberCyan.copy(alpha = 0.5f) else CyberBorder
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val textToCopy = if (!message.codeSnippet.isNullOrBlank()) {
                                    "${message.content}\n\nCode Snippet:\n${message.codeSnippet}"
                                } else {
                                    message.content
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("User Message", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("copy_user_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (!message.codeSnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SNIPPET",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Code Snippet", message.codeSnippet)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied snippet!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .testTag("copy_user_snippet_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy snippet",
                                            tint = CyberEmerald,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = message.codeSnippet,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberEmerald
                                )
                            }
                        }
                    }
                } else {
                    CyberMarkdownText(text = message.content)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = CyberCyan,
            strokeWidth = 2.dp
        )
        Text(
            text = "CyberGuard AI is analyzing query...",
            fontSize = 12.sp,
            color = CyberCyan,
            fontWeight = FontWeight.Medium
        )
    }
}
