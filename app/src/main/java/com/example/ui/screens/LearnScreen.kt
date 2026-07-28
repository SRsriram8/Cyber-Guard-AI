package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuizProgressEntity
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceVariantDark

data class LearningModule(
    val id: String,
    val title: String,
    val description: String,
    val totalQuizzes: Int = 3,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

@Composable
fun LearnScreen(
    progressList: List<QuizProgressEntity>,
    onSubmitQuizScore: (String, String, Int, Int) -> Unit
) {
    var activeModuleForQuiz by remember { mutableStateOf<LearningModule?>(null) }

    val modules = remember {
        listOf(
            LearningModule(
                id = "NETWORKING",
                title = "Networking & Protocols",
                description = "TCP/IP, Subnetting, TLS 1.3, DNSSEC, ARP Spoofing & VPNs",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "Which protocol is responsible for resolving IP addresses to MAC addresses?",
                        options = listOf("DNS", "ARP", "DHCP", "ICMP"),
                        correctAnswerIndex = 1,
                        explanation = "ARP (Address Resolution Protocol) maps dynamic IP addresses to physical MAC addresses."
                    ),
                    QuizQuestion(
                        id = 2,
                        question = "Which port is standard for encrypted HTTPS traffic?",
                        options = listOf("80", "443", "8080", "22"),
                        correctAnswerIndex = 1,
                        explanation = "Port 443 is used for secure HTTPS connections with SSL/TLS encryption."
                    )
                )
            ),
            LearningModule(
                id = "LINUX",
                title = "Linux System Security",
                description = "File permissions, SUID binaries, iptables, SELinux & Systemd",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What permission numerical value corresponds to rwxr-xr--?",
                        options = listOf("754", "755", "644", "700"),
                        correctAnswerIndex = 0,
                        explanation = "rwx = 7, r-x = 5, r-- = 4, giving 754."
                    )
                )
            ),
            LearningModule(
                id = "OWASP",
                title = "OWASP Top 10 & Web Security",
                description = "SQLi, XSS, CSRF, SSRF, Broken Auth & Insecure Deserialization",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What is the primary defense against SQL Injection?",
                        options = listOf("Input length limits", "Parameterized Queries / Prepared Statements", "MD5 Hashing", "HTTPS"),
                        correctAnswerIndex = 1,
                        explanation = "Prepared statements ensure parameters are treated strictly as data, never executable SQL."
                    )
                )
            ),
            LearningModule(
                id = "ETHICAL_HACKING",
                title = "Ethical Hacking & Recon",
                description = "Reconnaissance, Nmap, Metasploit, Privilege Escalation & Pivoting",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "Which Nmap scan flag performs a SYN stealth scan?",
                        options = listOf("-sT", "-sS", "-sU", "-sA"),
                        correctAnswerIndex = 1,
                        explanation = "-sS initiates a TCP SYN stealth scan without completing full 3-way handshakes."
                    )
                )
            ),
            LearningModule(
                id = "CRYPTOGRAPHY",
                title = "Cryptography & PKI",
                description = "AES-256, RSA, ECC, Hashing (SHA-256), Digital Signatures & Certificates",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "Is SHA-256 a symmetric encryption algorithm?",
                        options = listOf("Yes", "No, it is a one-way cryptographic hash function", "Yes, it uses public keys", "No, it is a cipher stream"),
                        correctAnswerIndex = 1,
                        explanation = "SHA-256 is a one-way hash algorithm, not an encryption scheme."
                    )
                )
            ),
            LearningModule(
                id = "CLOUD_SECURITY",
                title = "Cloud Security & IAM",
                description = "AWS IAM, Azure Sentinel, GCP Security Command Center & Zero Trust",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What is the core principle of Zero Trust?",
                        options = listOf("Trust everything inside corporate firewall", "Never trust, always verify", "Encrypt only backups", "Disable all API keys"),
                        correctAnswerIndex = 1,
                        explanation = "Zero Trust assumes breach and requires strict identity verification for every request."
                    )
                )
            ),
            LearningModule(
                id = "FORENSICS",
                title = "Digital Forensics & Memory Analysis",
                description = "Volatility framework, Memory Dumps, Autopsy, Registry Forensics & Timeline Creation",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "Which tool is widely used for volatile RAM memory analysis?",
                        options = listOf("Volatility", "Wireshark", "Nmap", "Metasploit"),
                        correctAnswerIndex = 0,
                        explanation = "Volatility framework is the industry standard for analyzing volatile RAM artifacts."
                    )
                )
            ),
            LearningModule(
                id = "REVERSE_ENG",
                title = "Reverse Engineering & Ghidra",
                description = "Assembly x86/x64, Decompilers (Ghidra, IDA Pro), PE Headers & Unpacking",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What is the 2-byte magic header for Windows Executables (PE)?",
                        options = listOf("PK", "MZ", "ELF", "PDF"),
                        correctAnswerIndex = 1,
                        explanation = "MZ (Mark Zbikowski) is the magic header identifier for Windows PE files."
                    )
                )
            ),
            LearningModule(
                id = "THREAT_INTEL",
                title = "Threat Intelligence & STIX/TAXII",
                description = "MITRE ATT&CK, Cyber Kill Chain, Threat Actor Attribution & IoC Sharing",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "How many main phases are in the Lockheed Martin Cyber Kill Chain?",
                        options = listOf("5", "7", "10", "12"),
                        correctAnswerIndex = 1,
                        explanation = "The Cyber Kill Chain has 7 phases: Recon, Weaponization, Delivery, Exploitation, Installation, C2, Action on Objectives."
                    )
                )
            ),
            LearningModule(
                id = "SOC_OPS",
                title = "SOC Operations & SIEM",
                description = "Splunk, Elastic SIEM, SOAR Playbooks, Alert Triage & Incident Metrics",
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What does SOAR stand for in SOC operations?",
                        options = listOf("Security Orchestration, Automation, and Response", "System Operations and Recovery", "Secure Offline Architecture Rules", "Single Sign On Authorization Record"),
                        correctAnswerIndex = 0,
                        explanation = "SOAR stands for Security Orchestration, Automation, and Response."
                    )
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("learn_screen_container")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SECURITY ACADEMY & MODULES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )

        Text(
            text = "Complete interactive modules and test your cybersecurity skills with quizzes:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (activeModuleForQuiz != null) {
            QuizView(
                module = activeModuleForQuiz!!,
                onClose = { activeModuleForQuiz = null },
                onSubmit = { score, total ->
                    onSubmitQuizScore(activeModuleForQuiz!!.id, activeModuleForQuiz!!.title, score, total)
                    activeModuleForQuiz = null
                }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(modules) { module ->
                    val progress = progressList.find { it.topicId == module.id }
                    ModuleCard(
                        module = module,
                        progress = progress,
                        onStartQuiz = { activeModuleForQuiz = module }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: LearningModule,
    progress: QuizProgressEntity?,
    onStartQuiz: () -> Unit
) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (progress != null) {
                    Text(
                        text = "${progress.scorePercentage}% Score",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = module.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (progress != null) "Retake Knowledge Quiz" else "Start Module Quiz",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuizView(
    module: LearningModule,
    onClose: () -> Unit,
    onSubmit: (Int, Int) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var showExplanation by remember { mutableStateOf(false) }

    val currentQuestion = module.questions[currentQuestionIndex]

    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${module.title} (Question ${currentQuestionIndex + 1}/${module.questions.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = currentQuestion.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    Surface(
                        onClick = {
                            if (!showExplanation) selectedOption = index
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (showExplanation) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariantDark)
                        .border(1.dp, CyberEmerald, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentQuestion.explanation,
                        fontSize = 12.sp,
                        color = CyberEmerald
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedOption != null) {
                        if (!showExplanation) {
                            if (selectedOption == currentQuestion.correctAnswerIndex) {
                                score++
                            }
                            showExplanation = true
                        } else {
                            if (currentQuestionIndex + 1 < module.questions.size) {
                                currentQuestionIndex++
                                selectedOption = null
                                showExplanation = false
                            } else {
                                onSubmit(score, module.questions.size)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text(
                    text = if (!showExplanation) "Submit Answer" else if (currentQuestionIndex + 1 < module.questions.size) "Next Question" else "Finish Quiz",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
