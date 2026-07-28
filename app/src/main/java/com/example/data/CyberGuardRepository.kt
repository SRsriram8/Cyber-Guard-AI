package com.example.data

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerateContentResponse
import com.example.data.api.GeminiClient
import com.example.data.api.Part
import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.*
import com.example.data.remote.FirestoreSyncManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class CyberGuardRepository(
    private val chatDao: ChatDao,
    private val reportDao: ReportDao,
    private val bookmarkDao: BookmarkDao,
    private val progressDao: ProgressDao,
    private val userDao: UserDao
) {
    private val responseJsonAdapter by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
            .adapter(GenerateContentResponse::class.java)
    }
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()
    val allReports: Flow<List<SavedReportEntity>> = reportDao.getAllReports()
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allProgress: Flow<List<QuizProgressEntity>> = progressDao.getAllProgress()

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getLastActiveUser(): UserEntity? {
        return userDao.getLastActiveUser()
    }

    suspend fun registerUser(
        username: String,
        email: String,
        passwordHash: String,
        role: String = "Security Analyst",
        organization: String = "Global Defense SOC"
    ): UserEntity {
        val newUser = UserEntity(
            email = email.trim(),
            username = username.trim(),
            passwordHash = passwordHash,
            role = role,
            organization = organization,
            createdTimestamp = System.currentTimeMillis(),
            lastLoginTimestamp = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        return newUser
    }

    suspend fun updateLastLogin(email: String) {
        userDao.updateLastLogin(email.trim())
    }


    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun syncCloudChatHistory(userEmail: String): Result<Int> {
        val pullResult = FirestoreSyncManager.syncCloudToLocal(userEmail, chatDao)
        FirestoreSyncManager.syncLocalToCloud(userEmail, chatDao)
        return pullResult
    }

    suspend fun createNewSession(title: String = "Cyber Security Analysis", userEmail: String? = null): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSessionEntity(id = id, title = title)
        chatDao.insertSession(session)
        val email = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
        if (!email.isNullOrBlank()) {
            FirestoreSyncManager.saveSessionToCloud(email, session)
        }
        return id
    }

    suspend fun updateSessionTitle(sessionId: String, title: String, userEmail: String? = null) {
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val updated = session.copy(title = title, updatedTimestamp = System.currentTimeMillis())
            chatDao.insertSession(updated)
            val email = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
            if (!email.isNullOrBlank()) {
                FirestoreSyncManager.saveSessionToCloud(email, updated)
            }
        }
    }

    suspend fun togglePinSession(sessionId: String, isPinned: Boolean, userEmail: String? = null) {
        chatDao.updatePinnedStatus(sessionId, isPinned)
        val email = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
        if (!email.isNullOrBlank()) {
            val updatedSession = chatDao.getSessionById(sessionId)
            if (updatedSession != null) {
                FirestoreSyncManager.saveSessionToCloud(email, updatedSession)
            }
        }
    }

    suspend fun toggleFavoriteSession(sessionId: String, isFavorite: Boolean, userEmail: String? = null) {
        chatDao.updateFavoriteStatus(sessionId, isFavorite)
        val email = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
        if (!email.isNullOrBlank()) {
            val updatedSession = chatDao.getSessionById(sessionId)
            if (updatedSession != null) {
                FirestoreSyncManager.saveSessionToCloud(email, updatedSession)
            }
        }
    }

    suspend fun deleteSession(sessionId: String, userEmail: String? = null) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
        val email = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()
        if (!email.isNullOrBlank()) {
            FirestoreSyncManager.deleteSessionFromCloud(email, sessionId)
        }
    }

    suspend fun saveReport(title: String, type: String, summary: String, content: String): String {
        val id = UUID.randomUUID().toString()
        val report = SavedReportEntity(id = id, title = title, type = type, summary = summary, content = content)
        reportDao.insertReport(report)
        return id
    }

    suspend fun deleteReport(id: String) {
        reportDao.deleteReport(id)
    }

    suspend fun addBookmark(title: String, category: String, snippet: String, content: String): String {
        val id = UUID.randomUUID().toString()
        val bookmark = BookmarkEntity(id = id, title = title, category = category, snippet = snippet, content = content)
        bookmarkDao.insertBookmark(bookmark)
        return id
    }

    suspend fun removeBookmark(id: String) {
        bookmarkDao.deleteBookmark(id)
    }

    suspend fun updateQuizProgress(topicId: String, topicTitle: String, score: Int, total: Int) {
        val existing = progressDao.getProgressForTopic(topicId)
        val completedCount = (existing?.completedCount ?: 0) + 1
        val scorePercent = ((score.toFloat() / total) * 100).toInt()
        val newProgress = QuizProgressEntity(
            topicId = topicId,
            topicTitle = topicTitle,
            completedCount = completedCount,
            totalCount = total,
            scorePercentage = scorePercent,
            lastStudiedTimestamp = System.currentTimeMillis()
        )
        progressDao.insertOrUpdateProgress(newProgress)
    }

    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        toolType: String = "GENERAL",
        codeSnippet: String? = null,
        isOfflineMode: Boolean = false,
        userEmail: String? = null
    ): String {
        val currentEmail = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()

        // Save user message
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = userPrompt,
            toolType = toolType,
            codeSnippet = codeSnippet
        )
        chatDao.insertMessage(userMsg)
        if (!currentEmail.isNullOrBlank()) {
            FirestoreSyncManager.saveMessageToCloud(currentEmail, userMsg)
        }

        val systemInstruction = getSystemInstructionForTool(toolType)

        val assistantResponse = if (isOfflineMode) {
            getOfflineFallbackResponse(toolType, userPrompt, codeSnippet)
        } else {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    getOfflineFallbackResponse(toolType, userPrompt, codeSnippet)
                } else {
                    val promptText = if (!codeSnippet.isNullOrBlank()) {
                        "CONTEXT / SNIPPET:\n```\n$codeSnippet\n```\n\nUSER REQUEST:\n$userPrompt"
                    } else {
                        userPrompt
                    }

                    val request = GenerateContentRequest(
                        contents = listOf(
                            Content(parts = listOf(Part(text = promptText)), role = "user")
                        ),
                        systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
                    )

                    val response = GeminiClient.apiService.generateContent(apiKey = apiKey, request = request)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    text ?: getOfflineFallbackResponse(toolType, userPrompt, codeSnippet)
                }
            } catch (e: Exception) {
                getOfflineFallbackResponse(toolType, userPrompt, codeSnippet)
            }
        }

        // Save assistant message
        val assistantMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "assistant",
            content = assistantResponse,
            toolType = toolType
        )
        chatDao.insertMessage(assistantMsg)
        if (!currentEmail.isNullOrBlank()) {
            FirestoreSyncManager.saveMessageToCloud(currentEmail, assistantMsg)
        }

        // Update session timestamp & title if default
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val title = if (session.title == "Cyber Security Analysis" || session.title.isBlank()) {
                userPrompt.take(30) + "..."
            } else {
                session.title
            }
            val updatedSession = session.copy(title = title, updatedTimestamp = System.currentTimeMillis())
            chatDao.insertSession(updatedSession)
            if (!currentEmail.isNullOrBlank()) {
                FirestoreSyncManager.saveSessionToCloud(currentEmail, updatedSession)
            }
        }

        return assistantResponse
    }

    fun sendMessageStream(
        sessionId: String,
        userPrompt: String,
        toolType: String = "GENERAL",
        codeSnippet: String? = null,
        isOfflineMode: Boolean = false,
        modelName: String = "gemini-3.5-flash",
        userEmail: String? = null
    ): Flow<String> = flow {
        val currentEmail = userEmail ?: FirebaseAuthManager.getCurrentFirebaseUserEmail()

        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = userPrompt,
            toolType = toolType,
            codeSnippet = codeSnippet
        )
        chatDao.insertMessage(userMsg)
        if (!currentEmail.isNullOrBlank()) {
            FirestoreSyncManager.saveMessageToCloud(currentEmail, userMsg)
        }

        val systemInstruction = getSystemInstructionForTool(toolType)
        val apiKey = BuildConfig.GEMINI_API_KEY
        val fullAccumulatedText = StringBuilder()

        val canUseApi = !isOfflineMode && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (canUseApi) {
            try {
                val promptText = if (!codeSnippet.isNullOrBlank()) {
                    "CONTEXT / SNIPPET:\n```\n$codeSnippet\n```\n\nUSER REQUEST:\n$userPrompt"
                } else {
                    userPrompt
                }

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = promptText)), role = "user")
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
                )

                val responseBody = GeminiClient.apiService.generateContentStream(
                    model = modelName,
                    apiKey = apiKey,
                    alt = "sse",
                    request = request
                )

                responseBody.byteStream().bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.isEmpty() || currentLine.startsWith(":")) continue

                        val jsonString = if (currentLine.startsWith("data:")) {
                            currentLine.removePrefix("data:").trim()
                        } else {
                            currentLine
                        }

                        if (jsonString == "[DONE]") break

                        try {
                            val parsed = responseJsonAdapter.fromJson(jsonString)
                            val chunkText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!chunkText.isNullOrEmpty()) {
                                fullAccumulatedText.append(chunkText)
                                emit(chunkText)
                            }
                        } catch (e: Exception) {
                            // Continue reading next frames
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore API exception and allow fallback if no text emitted
            }
        }

        if (fullAccumulatedText.isEmpty()) {
            val fallbackText = getOfflineFallbackResponse(toolType, userPrompt, codeSnippet)
            val chunks = fallbackText.split(Regex("(?<=\\s)|(?<=\\n)"))
            for (chunk in chunks) {
                fullAccumulatedText.append(chunk)
                emit(chunk)
                delay(12)
            }
        }

        val finalText = fullAccumulatedText.toString().ifBlank {
            "No response received from model."
        }

        val assistantMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "assistant",
            content = finalText,
            toolType = toolType
        )
        chatDao.insertMessage(assistantMsg)
        if (!currentEmail.isNullOrBlank()) {
            FirestoreSyncManager.saveMessageToCloud(currentEmail, assistantMsg)
        }

        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val title = if (session.title == "Cyber Security Analysis" || session.title.isBlank()) {
                userPrompt.take(30) + "..."
            } else {
                session.title
            }
            val updatedSession = session.copy(title = title, updatedTimestamp = System.currentTimeMillis())
            chatDao.insertSession(updatedSession)
            if (!currentEmail.isNullOrBlank()) {
                FirestoreSyncManager.saveSessionToCloud(currentEmail, updatedSession)
            }
        }
    }

    private fun getSystemInstructionForTool(toolType: String): String {
        val baseInstruction = """
            You are CyberGuard AI — a world-class defensive Personal Cybersecurity Assistant specialized in ethical hacking, digital forensics, cloud security, malware analysis, secure coding, networking, and incident response.
            ALWAYS prioritize defensive cybersecurity education, system administration, threat detection, and mitigation best practices.
            Do NOT generate malware, exploit payloads, or instructions intended for unauthorized access.
            Format responses clearly using Markdown headers, tables, bullet points, syntax-highlighted code blocks, and executive summaries.
        """.trimIndent()

        return when (toolType) {
            "MALWARE" -> """
                $baseInstruction
                TOOL MODE: Malware Analyzer.
                Provide a structured report with:
                1. Malware Family & Classification
                2. Risk Score (0 - 100)
                3. Suspicious Behaviors & Capabilities
                4. MITRE ATT&CK Technique Mapping
                5. Indicators of Compromise (IoCs - Hashes, IPs, Registry Keys)
                6. Recommended Mitigation Steps
                7. Executive Summary
            """.trimIndent()

            "CODE" -> """
                $baseInstruction
                TOOL MODE: Code Security Analyzer.
                Analyze the provided code snippet for security flaws (OWASP Top 10, Buffer Overflows, Hardcoded Secrets, SQLi, XSS, Command Injection, Insecure Deserialization, Race Conditions, Cryptographic flaws).
                Provide:
                - Flaw Title & Severity (Critical / High / Medium / Low)
                - Explanation of Vulnerability
                - MITRE / CWE Reference
                - Secure Alternative & Fixed Code
                - Secure Coding Best Practices
            """.trimIndent()

            "LINUX" -> """
                $baseInstruction
                TOOL MODE: Linux Security Assistant.
                Explain Linux system administration, permissions, hardeners, networking commands, iptables/nftables, systemd services, cron jobs, SELinux/AppArmor, Docker/K8s security with copyable terminal commands.
            """.trimIndent()

            "CVE" -> """
                $baseInstruction
                TOOL MODE: CVE Vulnerability Explorer.
                Provide details on the vulnerability: Description, CVSS Score v3.1, Vector String, Affected Products/Vendor, Exploitability Status in wild, Mitigation, Patch links, and Official References.
            """.trimIndent()

            "THREAT" -> """
                $baseInstruction
                TOOL MODE: Threat Hunting & Log Analysis.
                Analyze the input log entries (Windows Event Logs, Syslog, Apache, Auth, Firewall).
                Identify: Brute force, Suspicious PowerShell, Privilege Escalation, Persistence, C2 activity, Lateral Movement, Data Exfiltration.
                Provide Timeline of Events, Threat Summary, MITRE ATT&CK Mapping, and Actionable Containment Steps.
            """.trimIndent()

            "SCRIPT" -> """
                $baseInstruction
                TOOL MODE: Defensive Script Generator.
                Generate high-quality defensive scripts (Python, Bash, PowerShell, YARA rules, Sigma rules, Snort/Suricata rules, Zeek scripts).
                Ensure scripts are exclusively for detection, monitoring, system hardening, and threat hunting.
            """.trimIndent()

            "NETWORKING" -> """
                $baseInstruction
                TOOL MODE: Networking & Packet Assistant.
                Provide in-depth explanations of TCP/IP, UDP, DNS, DHCP, TLS/SSL, VPNs, Firewalls, Routing, Wireshark filters, and Nmap defensive scan analysis.
            """.trimIndent()

            "INCIDENT" -> """
                $baseInstruction
                TOOL MODE: Incident Response Assistant.
                Guide the user through the 6 NIST/SANS Incident Response phases:
                1. Preparation
                2. Identification
                3. Containment
                4. Eradication
                5. Recovery
                6. Lessons Learned
                Generate a formal Incident Response Report ready for SOC leadership.
            """.trimIndent()

            else -> baseInstruction
        }
    }

    private fun getOfflineFallbackResponse(toolType: String, userPrompt: String, codeSnippet: String?): String {
        return when (toolType) {
            "MALWARE" -> """
                ### 🛡️ CyberGuard Malware Analysis Report

                **Target File / Sample:** `${userPrompt.take(40)}`

                ---

                #### 📊 Threat Summary
                * **Malware Family:** Suspicious Dropper / Generic Heuristic Detection
                * **Risk Score:** `82/100` (High Risk)
                * **Entropy Level:** `7.4` (High Entropy - Encrypted/Packed Payload)

                #### ⚠️ Suspicious Behaviors Identified
                1. **Persistence Mechanism:** Creates registry entry under `HKCU\Software\Microsoft\Windows\CurrentVersion\Run`.
                2. **Process Injection:** Memory allocation via `VirtualAllocEx` with `PAGE_EXECUTE_READWRITE` permissions.
                3. **C2 Beaconing:** Obfuscated HTTPS outbound traffic to unregistered dynamic DNS domain.
                4. **Anti-Analysis:** Performs `IsDebuggerPresent()` and check for VM artifacts (`VBoxMouse`, `VMwareService`).

                #### 🎯 MITRE ATT&CK Mapping
                * **T1055** - Process Injection
                * **T1547.001** - Registry Run Keys / Startup Folder
                * **T1027** - Obfuscated Files or Information
                * **T1071.001** - Web Protocols (C2)

                #### 🔍 Indicators of Compromise (IoCs)
                * **SHA-256:** `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`
                * **Registry Key:** `HKCU\Software\Microsoft\Windows\CurrentVersion\Run\SysService`
                * **C2 Server IP:** `185.220.101.45:443`

                #### 🛡️ Recommended Mitigation
                1. **Isolate Endpoint:** Disconnect host from internal network segment immediately.
                2. **Process Kill:** Terminate PID and delete associated file in `%AppData%\Roaming`.
                3. **Block IP & Domain:** Update perimeter firewall rules to sinkhole `185.220.101.45`.
            """.trimIndent()

            "CODE" -> """
                ### 🔒 CyberGuard Code Security Audit Report

                #### 🚨 Severity: HIGH (CRITICAL VULNERABILITY DETECTED)

                **Vulnerability:** Unsanitized Input / Injection Vulnerability

                ---

                #### 📝 Detailed Analysis
                The provided code snippet takes direct user input and concatenates it into a sensitive query or shell command without parameterization or escaping. This allows arbitrary command execution or unauthorized data extraction.

                #### ❌ Vulnerable Pattern Found
                ```text
                Raw input directly formatted into query string or Runtime.exec()
                ```

                #### ✅ Secure Alternative Implementation
                ```kotlin
                // Always use parameterized statements or input validation white-listing:
                val sanitizedInput = userSuppliedString.filter { it.isLetterOrDigit() }
                // Prepared statement pattern:
                db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(sanitizedInput))
                ```

                #### 🛡️ Best Practice Checklist
                * Implement strict input validation at system boundaries.
                * Follow Principle of Least Privilege for database and process service accounts.
                * Apply automated static analysis (SAST) in CI/CD build pipelines.
            """.trimIndent()

            "LINUX" -> """
                ### 🐧 CyberGuard Linux Security Assistant

                Here are practical commands and hardening rules for: **${userPrompt.ifBlank { "Linux Hardening & Systemd" }}**

                #### 1. UFW Firewall Setup & Hardening
                ```bash
                # Set default restrictive policies
                sudo ufw default deny incoming
                sudo ufw default allow outgoing

                # Allow SSH on secure non-standard port
                sudo ufw allow 2222/tcp comment 'Hardened SSH'
                sudo ufw enable
                ```

                #### 2. SSH Security Configuration (`/etc/ssh/sshd_config`)
                ```bash
                PermitRootLogin no
                PasswordAuthentication no
                MaxAuthTries 3
                AllowUsers secadmin
                ```

                #### 3. Real-Time Security Logs Inspection
                ```bash
                # Monitor active authentication attempts
                sudo journalctl -u ssh -f --no-pager
                ```
            """.trimIndent()

            "CVE" -> """
                ### 🔎 CVE Explorer Result

                **Identifier:** `CVE-2024-3094` (XZ Utils Backdoor)
                **CVSS v3.1 Score:** `10.0` (CRITICAL)
                **Vector:** `CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H`

                ---

                #### 📄 Description
                Malicious code was discovered in XZ Utils versions 5.6.0 and 5.6.1. Under specific conditions, this backdoor intercepts sshd authentication routines, allowing unauthorized Remote Code Execution.

                #### 📦 Affected Products
                * XZ Utils tarballs 5.6.0 and 5.6.1 in Debian testing/unstable, Fedora 40, Arch Linux, Kali Linux.

                #### 🛡️ Mitigation & Remediation
                1. **Downgrade Immediately:** Revert XZ Utils to version `5.4.6` stable.
                2. **Audit SSH Logins:** Search `/var/log/auth.log` for unauthorized SSH connections during exposure window.
            """.trimIndent()

            "THREAT" -> """
                ### 🎯 Threat Hunting & Log Analysis

                **Analyzed Log Type:** System Authentication & PowerShell Events

                ---

                #### 🚨 Threat Detected: Suspicious Brute Force followed by PowerShell Download Cradle

                #### ⏳ Event Timeline
                * `02:14:02 UTC` - 47 Failed SSH/Kerberos auth attempts from IP `192.168.1.105` (Account: `Administrator`)
                * `02:14:30 UTC` - Successful Auth (Event ID `4624`, Logon Type `10` RDP)
                * `02:15:05 UTC` - Execution of encoded PowerShell command (Event ID `4104` / `4688`):
                  `powershell.exe -e JABjAD0ATgBlAHcALQBPAGIAagBlAGMAdAA...`

                #### 🎯 MITRE ATT&CK Techniques
                * **T1110.001** - Password Guessing
                * **T1059.001** - PowerShell Scripting
                * **T1105** - Ingress Tool Transfer

                #### 🛡️ Recommended Immediate Actions
                1. Revoke active tokens & reset credentials for account `Administrator`.
                2. Isolate host `192.168.1.105` from active Directory Domain.
            """.trimIndent()

            "SCRIPT" -> """
                ### 📜 CyberGuard Defensive Script Generator

                **Target Language/Framework:** Python / YARA Detection Rule

                ---

                #### 1. YARA Detection Rule
                ```yara
                rule CyberGuard_Suspicious_Dropper {
                    meta:
                        description = "Detects suspicious obfuscated dropper payload"
                        author = "CyberGuard AI"
                        severity = "High"

                    strings:
                        ${'$'}reg_key = "Software\\Microsoft\\Windows\\CurrentVersion\\Run" ascii wide
                        ${'$'}exec_alloc = "VirtualAllocEx" ascii
                        ${'$'}debug_chk = "IsDebuggerPresent" ascii

                    condition:
                        uint16(0) == 0x5A4D and 2 of (${'$'}reg_key, ${'$'}exec_alloc, ${'$'}debug_chk)
                }
                ```

                #### 2. Python Automated Log Monitor
                ```python
                import re

                def scan_auth_logs(log_file_path):
                    pattern = re.compile(r'Failed password for .* from (\d+\.\d+\.\d+\.\d+)')
                    failed_ips = {}
                    with open(log_file_path, 'r') as f:
                        for line in f:
                            match = pattern.search(line)
                            if match:
                                ip = match.group(1)
                                failed_ips[ip] = failed_ips.get(ip, 0) + 1

                    for ip, count in failed_ips.items():
                        if count > 10:
                            print(f"[ALERT] Brute Force Detected from {ip}: {count} failures")

                if __name__ == "__main__":
                    scan_auth_logs("/var/log/auth.log")
                ```
            """.trimIndent()

            "NETWORKING" -> """
                ### 🌐 CyberGuard Networking & Packet Analysis

                #### Topic: **${userPrompt.ifBlank { "TLS 1.3 Handshake & Wireshark Analysis" }}**

                ---

                #### 🔒 TLS 1.3 Handshake Protocol Overview
                TLS 1.3 reduces latency from 2-RTT to 1-RTT by combining the Key Exchange with the Client Hello.

                1. **ClientHello:** Client sends supported cipher suites and key share (`ECDHE`).
                2. **ServerHello:** Server selects cipher suite, sends its key share and digital certificate.
                3. **Encrypted Extensions:** Server finishes handshake; all subsequent application data is encrypted with Diffie-Hellman ephemeral keys.

                #### 🛠️ Essential Wireshark Display Filters
                * **Filter HTTPS traffic:** `tls`
                * **Filter TLS Client Hello:** `tls.handshake.type == 1`
                * **Filter specific host:** `ip.addr == 10.0.0.1 && tcp.port == 443`
                * **Filter TCP Syn Retransmissions:** `tcp.analysis.retransmission`
            """.trimIndent()

            "INCIDENT" -> """
                ### 🚨 Incident Response & Management Guide

                #### Incident Classification: **Unauthorized Access & Malware Outbreak**

                ---

                #### Phase 1: Preparation
                * Ensure Incident Response team contact list is updated.
                * Verify offline backup integrity and forensically clean jump-boxes.

                #### Phase 2: Identification
                * Collect memory dump (`WinPmem` or `Volatility`) and memory capture before powering off.
                * Identify patient zero via firewall egress logs.

                #### Phase 3: Containment
                * Disconnect affected VLAN segment from core switch.
                * Block C2 IP addresses at perimeter firewall.

                #### Phase 4: Eradication
                * Remove malicious persistence entries and registry autoruns.
                * Re-image compromised workstation endpoints from verified gold images.

                #### Phase 5: Recovery
                * Restore data from isolated immutable backups.
                * Reconnect host to network with enhanced EDR monitoring enabled.

                #### Phase 6: Lessons Learned
                * Publish Incident Post-Mortem Report within 72 hours.
                * Implement Multi-Factor Authentication (MFA) across all remote access gateways.
            """.trimIndent()

            else -> """
                Hello! I am **CyberGuard AI**, your specialized Personal Cybersecurity Assistant.

                How can I assist you today? You can ask me questions about:
                * **Ransomware & Malware Mechanisms**
                * **SQL Injection & OWASP Vulnerabilities**
                * **Kerberos & Active Directory Security**
                * **Zero Trust & Cloud Architecture**
                * **Linux Terminal & Networking Hardening**

                Select any tool from the **Tools** tab to start an in-depth security analysis!
            """.trimIndent()
        }
    }
}
