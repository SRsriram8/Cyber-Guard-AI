package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple

data class SecurityToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val themeColor: Color
)

@Composable
fun ToolsScreen(
    onSelectTool: (String) -> Unit
) {
    val toolsList = remember {
        listOf(
            SecurityToolItem(
                id = "MALWARE",
                title = "Malware Analyzer",
                subtitle = "Deep scan APK, EXE, DLL, ZIP, PDF & Office docs",
                category = "REVERSE & FORENSICS",
                icon = Icons.Default.BugReport,
                themeColor = CyberCyan
            ),
            SecurityToolItem(
                id = "CODE",
                title = "Code Security Audit",
                subtitle = "Scan Python, Kotlin, C++, JS, Rust for OWASP Top 10",
                category = "SECURE CODING",
                icon = Icons.Default.Security,
                themeColor = CyberEmerald
            ),
            SecurityToolItem(
                id = "LINUX",
                title = "Linux Assistant",
                subtitle = "Terminal commands, iptables, systemd, SELinux, Docker & K8s",
                category = "SYSADMIN & OS",
                icon = Icons.Default.Terminal,
                themeColor = Color(0xFF64B5F6)
            ),
            SecurityToolItem(
                id = "THREAT",
                title = "Threat Hunting",
                subtitle = "Analyze WinEvent, Syslog, Apache & Firewall logs",
                category = "SOC OPERATIONS",
                icon = Icons.Default.Radar,
                themeColor = CyberPurple
            ),
            SecurityToolItem(
                id = "CVE",
                title = "CVE Explorer",
                subtitle = "Search vulnerabilities, CVSS v3.1 scores & patches",
                category = "VULN MANAGEMENT",
                icon = Icons.Default.PestControl,
                themeColor = Color(0xFFFFAB00)
            ),
            SecurityToolItem(
                id = "SCRIPT",
                title = "Script Generator",
                subtitle = "Generate defensive Python, Bash, YARA, Sigma & Snort rules",
                category = "DEFENSIVE AUTOMATION",
                icon = Icons.Default.IntegrationInstructions,
                themeColor = Color(0xFFFF4081)
            ),
            SecurityToolItem(
                id = "NETWORKING",
                title = "Networking Assistant",
                subtitle = "TCP/IP, TLS, VPN, Wireshark & Nmap defensive analysis",
                category = "NETWORK SECURITY",
                icon = Icons.Default.Router,
                themeColor = Color(0xFF1DE9B6)
            ),
            SecurityToolItem(
                id = "INCIDENT",
                title = "Incident Response",
                subtitle = "NIST 6-phase IR guide & executive report generator",
                category = "INCIDENT MGMT",
                icon = Icons.Default.ReportProblem,
                themeColor = Color(0xFFFF5252)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("tools_screen_container")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "CYBERSECURITY TOOLKIT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )

        Text(
            text = "Select a specialized tool to perform in-depth analysis:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(toolsList) { tool ->
                ToolGridCard(
                    tool = tool,
                    onClick = { onSelectTool(tool.id) }
                )
            }
        }
    }
}

@Composable
private fun ToolGridCard(
    tool: SecurityToolItem,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        onClick = onClick,
        borderColor = tool.themeColor.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(tool.themeColor.copy(alpha = 0.15f))
                        .border(1.dp, tool.themeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.themeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = tool.category,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = tool.themeColor,
                    letterSpacing = 0.5.sp
                )
            }

            Column {
                Text(
                    text = tool.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tool.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Launch Tool",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tool.themeColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = tool.themeColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
