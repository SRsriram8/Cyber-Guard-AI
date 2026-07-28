package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberMarkdownText
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.util.PdfExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailScreen(
    toolId: String,
    onBack: () -> Unit,
    onRunAnalysis: (String, String?, String) -> Unit,
    isGenerating: Boolean,
    lastAnalysisResult: String?
) {
    val context = LocalContext.current
    var promptInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("Kotlin") }
    var selectedFileType by remember { mutableStateOf("APK") }

    val toolTitle = when (toolId) {
        "MALWARE" -> "Malware Analyzer"
        "CODE" -> "Code Security Audit"
        "LINUX" -> "Linux Terminal Assistant"
        "THREAT" -> "Threat Hunting & Log Analysis"
        "CVE" -> "CVE Vulnerability Explorer"
        "SCRIPT" -> "Defensive Script Generator"
        "NETWORKING" -> "Networking & Packet Assistant"
        "INCIDENT" -> "Incident Response Guide"
        else -> "Cyber Security Tool"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("tool_detail_screen_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ToolHeaderBanner(toolId = toolId)
            }

            // Controls based on tool type
            item {
                when (toolId) {
                    "CODE" -> {
                        CodeAuditControls(
                            selectedLang = selectedLang,
                            onSelectLang = { selectedLang = it },
                            codeInput = codeInput,
                            onCodeChange = { codeInput = it },
                            onLoadSample = { code, lang ->
                                codeInput = code
                                selectedLang = lang
                            }
                        )
                    }
                    "MALWARE" -> {
                        MalwareControls(
                            selectedFileType = selectedFileType,
                            onSelectFileType = { selectedFileType = it },
                            promptInput = promptInput,
                            onPromptChange = { promptInput = it },
                            onLoadSample = { sample -> promptInput = sample }
                        )
                    }
                    "THREAT" -> {
                        LogHuntingControls(
                            logInput = codeInput,
                            onLogChange = { codeInput = it },
                            onLoadSampleLog = { sample -> codeInput = sample }
                        )
                    }
                    else -> {
                        GeneralToolControls(
                            toolId = toolId,
                            promptInput = promptInput,
                            onPromptChange = { promptInput = it },
                            onLoadPreset = { preset -> promptInput = preset }
                        )
                    }
                }
            }

            // Run Analysis Button
            item {
                Button(
                    onClick = {
                        val finalPrompt = promptInput.ifBlank { "Perform comprehensive defensive analysis." }
                        val finalCode = if (codeInput.isNotBlank()) codeInput else null
                        onRunAnalysis(finalPrompt, finalCode, toolId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("run_tool_analysis_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...", color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute Analysis", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Analysis Results Output Card
            if (!lastAnalysisResult.isNullOrBlank()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ANALYSIS RESULT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )

                        OutlinedButton(
                            onClick = {
                                PdfExportUtils.exportIncidentReportToPdf(
                                    context = context,
                                    toolTitle = toolTitle,
                                    promptInput = promptInput.ifBlank { "Defensive Tool Analysis" },
                                    codeInput = if (codeInput.isNotBlank()) codeInput else null,
                                    analysisResult = lastAnalysisResult
                                )
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberEmerald),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("export_report_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = CyberEmerald
                    ) {
                        CyberMarkdownText(text = lastAnalysisResult)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolHeaderBanner(toolId: String) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "AI Security Engine Ready",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Provide input below or pick a preset sample to test real-time defensive analysis.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CodeAuditControls(
    selectedLang: String,
    onSelectLang: (String) -> Unit,
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onLoadSample: (String, String) -> Unit
) {
    val languages = listOf("Kotlin", "Java", "Python", "JavaScript", "TypeScript", "C", "C++", "Go", "Rust", "PHP", "Bash")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SELECT LANGUAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(languages) { lang ->
                FilterChip(
                    selected = selectedLang == lang,
                    onClick = { onSelectLang(lang) },
                    label = { Text(lang, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                onLoadSample(
                    """
                    val query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'"
                    val result = db.rawQuery(query, null)
                    """.trimIndent(),
                    "Kotlin"
                )
            }) {
                Text("Load SQLi Sample", fontSize = 11.sp, color = CyberCyan)
            }

            TextButton(onClick = {
                onLoadSample(
                    """
                    #python command injection
                    import os
                    user_input = input("Enter host to ping: ")
                    os.system("ping -c 1 " + user_input)
                    """.trimIndent(),
                    "Python"
                )
            }) {
                Text("Load Command Injection", fontSize = 11.sp, color = CyberCyan)
            }
        }

        OutlinedTextField(
            value = codeInput,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("code_audit_input_field"),
            placeholder = { Text("Paste source code snippet here...", fontSize = 12.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        )
    }
}

@Composable
private fun MalwareControls(
    selectedFileType: String,
    onSelectFileType: (String) -> Unit,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    onLoadSample: (String) -> Unit
) {
    val fileTypes = listOf("APK", "EXE", "DLL", "ZIP", "PDF", "DOCX")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("TARGET FILE FORMAT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(fileTypes) { type ->
                FilterChip(
                    selected = selectedFileType == type,
                    onClick = { onSelectFileType(type) },
                    label = { Text(type, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                )
            }
        }

        TextButton(onClick = {
            onLoadSample("Analyse suspicious dropper sample payload_v2.exe (SHA256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855) creating startup registry key HKCU\\Software\\CurrentVersion\\Run")
        }) {
            Text("Load Sample Malware Metadata", fontSize = 11.sp, color = CyberCyan)
        }

        OutlinedTextField(
            value = promptInput,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("malware_description_field"),
            placeholder = { Text("Enter file details, hash, or suspicious behavior notes...", fontSize = 12.sp) }
        )
    }
}

@Composable
private fun LogHuntingControls(
    logInput: String,
    onLogChange: (String) -> Unit,
    onLoadSampleLog: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("LOG ENTRY INPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)

        TextButton(onClick = {
            onLoadSampleLog(
                """
                2026-07-27 02:14:02.102 [AUTH] Failed password for root from 192.168.1.105 port 54211 ssh2
                2026-07-27 02:14:05.410 [AUTH] Failed password for root from 192.168.1.105 port 54212 ssh2
                2026-07-27 02:14:30.001 [AUTH] Accepted password for Administrator from 192.168.1.105 port 54220 ssh2
                2026-07-27 02:15:00.820 [PROCESS_CREATE] Image: powershell.exe -Enc JABjAD0ATgBlAHcALQBPAGIAagBlAGMAdAA...
                """.trimIndent()
            )
        }) {
            Text("Load Brute Force & PowerShell Attack Log Sample", fontSize = 11.sp, color = CyberCyan)
        }

        OutlinedTextField(
            value = logInput,
            onValueChange = onLogChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("log_input_field"),
            placeholder = { Text("Paste Syslog, Windows Event, or Apache logs...", fontSize = 12.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        )
    }
}

@Composable
private fun GeneralToolControls(
    toolId: String,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    onLoadPreset: (String) -> Unit
) {
    val preset = when (toolId) {
        "CVE" -> "Search details for CVE-2024-3094 (XZ Utils Backdoor)"
        "LINUX" -> "How to set up UFW firewall and harden SSH configuration on Ubuntu Server"
        "SCRIPT" -> "Generate YARA rule to detect VirtualAllocEx process injection"
        "NETWORKING" -> "Explain TLS 1.3 handshake and useful Wireshark filters"
        "INCIDENT" -> "Generate an incident report for an unauthorized RDP access event"
        else -> "Perform security analysis"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { onLoadPreset(preset) }) {
            Text("Load Sample Request", fontSize = 11.sp, color = CyberCyan)
        }

        OutlinedTextField(
            value = promptInput,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("general_tool_prompt_field"),
            placeholder = { Text("Enter your request or query...", fontSize = 12.sp) }
        )
    }
}
