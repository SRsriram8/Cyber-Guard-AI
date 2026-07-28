package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatSessionEntity
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple

@Composable
fun HomeScreen(
    sessions: List<ChatSessionEntity>,
    onNavigateToChat: (String?) -> Unit,
    onNavigateToTool: (String) -> Unit,
    onNavigateToLearn: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_container"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Dashboard Status Card
        item {
            HeroDashboardCard(onQuickAsk = { onNavigateToChat(null) })
        }

        // Quick Actions Row
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(
                onNewChat = { onNavigateToChat(null) },
                onNavigateToTool = onNavigateToTool
            )
        }

        // Featured Tools Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FAVORITE & RECOMMENDED TOOLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FeaturedToolsGrid(onNavigateToTool = onNavigateToTool)
        }

        // Learning Progress Summary
        item {
            LearningProgressCard(onNavigateToLearn = onNavigateToLearn)
        }

        // Cyber Threat Intelligence Feed
        item {
            Text(
                text = "LIVE THREAT INTEL & ADVISORIES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThreatIntelFeed()
        }

        // Recent Conversations List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ANALYSES & CHATS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                if (sessions.isNotEmpty()) {
                    TextButton(onClick = { onNavigateToChat(null) }) {
                        Text("View All", fontSize = 12.sp, color = CyberCyan)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (sessions.isEmpty()) {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No recent chats yet. Tap 'Ask AI Assistant' to start your first cybersecurity analysis!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.take(4).forEach { session ->
                        RecentChatRow(
                            session = session,
                            onClick = { onNavigateToChat(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroDashboardCard(onQuickAsk: () -> Unit) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = CyberCyan,
        borderWidth = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cyber_hero_banner_1785221742334),
                    contentDescription = "Cyber Security Command Center",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                            Text(
                                text = "SYSTEM INTEGRITY NORMAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberEmerald
                            )
                        }
                        Text(
                            text = "CyberGuard AI Command Center",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Welcome, Security Analyst",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask any security question, paste code snippets, or upload logs for real-time AI threat analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onQuickAsk,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_ask_ai_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch AI Security Assistant",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNewChat: () -> Unit,
    onNavigateToTool: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            QuickActionButton(
                title = "Code Review",
                icon = Icons.Default.Code,
                onClick = { onNavigateToTool("CODE") }
            )
        }
        item {
            QuickActionButton(
                title = "Malware Scan",
                icon = Icons.Default.BugReport,
                onClick = { onNavigateToTool("MALWARE") }
            )
        }
        item {
            QuickActionButton(
                title = "Log Analyzer",
                icon = Icons.Default.Terminal,
                onClick = { onNavigateToTool("THREAT") }
            )
        }
        item {
            QuickActionButton(
                title = "Generate Script",
                icon = Icons.Default.IntegrationInstructions,
                onClick = { onNavigateToTool("SCRIPT") }
            )
        }
        item {
            QuickActionButton(
                title = "Incident Guide",
                icon = Icons.Default.ReportProblem,
                onClick = { onNavigateToTool("INCIDENT") }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyberCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeaturedToolsGrid(onNavigateToTool: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolTile(
                title = "Malware Analyzer",
                description = "Parse executables, PDFs & DLLs for IoCs",
                icon = Icons.Default.BugReport,
                color = CyberCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTool("MALWARE") }
            )
            ToolTile(
                title = "Code Security",
                description = "Detect OWASP top 10 & secret leaks",
                icon = Icons.Default.Security,
                color = CyberEmerald,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTool("CODE") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolTile(
                title = "Threat Hunting",
                description = "Analyze WinEvent, Syslog & Firewall logs",
                icon = Icons.Default.Radar,
                color = CyberPurple,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTool("THREAT") }
            )
            ToolTile(
                title = "CVE Explorer",
                description = "Search CVSS scores & mitigations",
                icon = Icons.Default.PestControl,
                color = Color(0xFFFFAB00),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTool("CVE") }
            )
        }
    }
}

@Composable
private fun ToolTile(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = modifier,
        onClick = onClick,
        borderColor = color.copy(alpha = 0.5f)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LearningProgressCard(onNavigateToLearn: () -> Unit) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigateToLearn
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SECURITY ACADEMY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberEmerald,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Master Ethical Hacking & SOC Ops",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberEmerald,
                    trackColor = CyberBorder
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "4 of 10 Modules Completed • Quiz Master",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go to Learn",
                tint = CyberEmerald
            )
        }
    }
}

@Composable
private fun ThreatIntelFeed() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IntelNewsItem(
            severity = "HIGH",
            title = "Zero-Day Flaw Discovered in Active Directory Kerberos",
            source = "CyberGuard Threat Intelligence • 2 hours ago"
        )
        IntelNewsItem(
            severity = "CRITICAL",
            title = "Critical RCE Patch Issued for Linux Kernel eBPF Module",
            source = "NIST NVD Advisory • 5 hours ago"
        )
    }
}

@Composable
private fun IntelNewsItem(
    severity: String,
    title: String,
    source: String
) {
    val tagColor = if (severity == "CRITICAL") Color(0xFFFF5252) else Color(0xFFFFAB00)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(tagColor.copy(alpha = 0.15f))
                    .border(1.dp, tagColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = severity,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = source,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentChatRow(
    session: ChatSessionEntity,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
