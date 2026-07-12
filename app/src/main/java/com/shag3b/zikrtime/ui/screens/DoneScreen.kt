package com.shag3b.zikrtime.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shag3b.zikrtime.R
import com.shag3b.zikrtime.StatsManager

@Composable
fun DoneScreen(isMorning: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val stats = if (isMorning) StatsManager.getMorningStats(context) else StatsManager.getEveningStats(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Period Name Header with Icon
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMorning) Color(0xFFFF9800).copy(alpha = 0.1f) else Color(0xFF9C27B0).copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, if (isMorning) Color(0xFFFF9800) else Color(0xFF9C27B0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    if (isMorning) {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            drawCircle(color = Color(0xFFFF9800), radius = 8.dp.toPx())
                        }
                    } else {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            drawArc(
                                color = Color(0xFF9C27B0),
                                startAngle = -110f,
                                sweepAngle = 220f,
                                useCenter = false,
                                topLeft = Offset(center.x - 8.dp.toPx(), center.y - 8.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 16.dp.toPx()),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Period Name
                    Text(
                        text = if (isMorning) stringResource(R.string.morning_stats) else stringResource(R.string.evening_stats),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (isMorning) Color(0xFFFF9800) else Color(0xFF9C27B0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Card - All 4 stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompletionStatItem(
                        icon = "✅",
                        value = stats.totalCompleted.toString(),
                        label = stringResource(R.string.total_completed),
                        color = Color(0xFF4CAF50)
                    )
                    CompletionStatItem(
                        icon = "🔥",
                        value = stats.currentStreak.toString(),
                        label = stringResource(R.string.current_streak),
                        color = Color(0xFFFF5722)
                    )
                    CompletionStatItem(
                        icon = "🏆",
                        value = stats.bestStreak.toString(),
                        label = stringResource(R.string.best_streak),
                        color = Color(0xFFFFC107)
                    )
                    CompletionStatItem(
                        icon = "❌",
                        value = stats.daysForgotten.toString(),
                        label = stringResource(R.string.days_forgotten),
                        color = Color(0xFFE91E63)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Completion Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Green checkmark icon
                    Canvas(modifier = Modifier.size(100.dp)) {
                        // Circle
                        drawCircle(
                            color = Color(0xFF2E7D32),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 8f)
                        )
                        // Checkmark
                        val path = Path().apply {
                            moveTo(size.width * 0.25f, size.height * 0.5f)
                            lineTo(size.width * 0.45f, size.height * 0.7f)
                            lineTo(size.width * 0.75f, size.height * 0.35f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF2E7D32),
                            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = stringResource(R.string.done_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    Text(
                        text = stringResource(R.string.done_message),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = Color(0xFF5F6368)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Additional message
                    Text(
                        text = stringResource(R.string.app_ready),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF43A047)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Back to Home button
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A2332)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.back_home),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Share App button
                    OutlinedButton(
                        onClick = {
                            val appName = context.getString(R.string.app_name)
                            val packageName = context.packageName
                            val playStoreLink = "https://play.google.com/store/apps/details?id=$packageName"
                            val mediaFireLink = "https://www.mediafire.com/folder/q6ra32g3qway3/Athkar"

                            // Check if app is published on Play Store
                            val isOnPlayStore = try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName"))
                                context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
                            } catch (e: Exception) {
                                false
                            }

                            // Smart link selection
                            val shareText = if (isOnPlayStore) {
                                // App is on Play Store - send BOTH links (Play Store first)
                                """
                                $appName - تطبيق أذكار الصباح والمساء
                                
                                📥 حمل التطبيق / Download App:
                                
                                📱 Google Play (الخيار الأول):
                                $playStoreLink
                                
                                📦 رابط بديل / Backup Link:
                                $mediaFireLink
                                """.trimIndent()
                            } else {
                                // App NOT on Play Store yet - send MediaFire only
                                """
                                $appName - تطبيق أذكار الصباح والمساء
                                
                                📥 حمل التطبيق / Download App:
                                $mediaFireLink
                                """.trimIndent()
                            }

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, appName)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_app)))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1A2332)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF1A2332))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.share_app),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact link - OUTSIDE the card
            Text(
                text = "Contact",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    color = Color(0xFF2196F3), // Blue link color
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://github.com/Shag3b")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun CompletionStatItem(icon: String, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatCard(icon: String, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E)
            ),
            textAlign = TextAlign.Center
        )
    }
}
