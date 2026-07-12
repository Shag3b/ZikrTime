package com.shag3b.zikrtime.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.shag3b.zikrtime.R
import com.shag3b.zikrtime.LanguageManager
import com.shag3b.zikrtime.StatsManager
import com.shag3b.zikrtime.MainActivity

@Composable
fun HomeScreen(
    onMorningClick: () -> Unit,
    onEveningClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context as? MainActivity }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2332)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.home_description),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF5F6368)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Compact Stats Card - All 4 stats in one line
            val morningStats = StatsManager.getMorningStats(context)
            val eveningStats = StatsManager.getEveningStats(context)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.stats_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A2332)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Morning Stats - All 4 in one line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawCircle(color = Color(0xFFFF9800), radius = 4.dp.toPx())
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.morning_stats),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = Color(0xFF5F6368)
                                )
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CompactStatBadge("✅", morningStats.totalCompleted.toString(), Color(0xFF4CAF50))
                            CompactStatBadge("🔥", morningStats.currentStreak.toString(), Color(0xFFFF5722))
                            CompactStatBadge("🏆", morningStats.bestStreak.toString(), Color(0xFFFFC107))
                            CompactStatBadge("❌", morningStats.daysForgotten.toString(), Color(0xFFE91E63))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Evening Stats - All 4 in one line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawArc(
                                    color = Color(0xFF9C27B0),
                                    startAngle = -110f,
                                    sweepAngle = 220f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - 4.dp.toPx(), center.y - 4.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 8.dp.toPx()),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.evening_stats),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = Color(0xFF5F6368)
                                )
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CompactStatBadge("✅", eveningStats.totalCompleted.toString(), Color(0xFF4CAF50))
                            CompactStatBadge("🔥", eveningStats.currentStreak.toString(), Color(0xFFFF5722))
                            CompactStatBadge("🏆", eveningStats.bestStreak.toString(), Color(0xFFFFC107))
                            CompactStatBadge("❌", eveningStats.daysForgotten.toString(), Color(0xFFE91E63))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Morning Azkar Button
            Button(
                onClick = onMorningClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Sunrise icon
                    Canvas(modifier = Modifier.size(32.dp)) {
                        drawCircle(color = Color.White, radius = 8.dp.toPx())
                        for (i in 0..7) {
                            val angle = (i * 45f) * (Math.PI / 180f)
                            val startX = center.x + (12.dp.toPx() * Math.cos(angle)).toFloat()
                            val startY = center.y + (12.dp.toPx() * Math.sin(angle)).toFloat()
                            val endX = center.x + (16.dp.toPx() * Math.cos(angle)).toFloat()
                            val endY = center.y + (16.dp.toPx() * Math.sin(angle)).toFloat()
                            drawLine(
                                color = Color.White,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.morning_button),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Evening Azkar Button
            Button(
                onClick = onEveningClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Crescent Moon icon (Hilal) - Simple and clear
                    Canvas(modifier = Modifier.size(32.dp)) {
                        // Draw a simple crescent using stroke
                        drawArc(
                            color = Color.White,
                            startAngle = -110f,
                            sweepAngle = 220f,
                            useCenter = false,
                            topLeft = Offset(center.x - 10.dp.toPx(), center.y - 10.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.evening_button),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clock icon
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(
                            color = Color(0xFFBDBDBD),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawLine(
                            color = Color(0xFFBDBDBD),
                            start = center,
                            end = Offset(center.x, center.y - 12.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFBDBDBD),
                            start = center,
                            end = Offset(center.x + 14.dp.toPx(), center.y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.morning_azkar_time),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF5F6368)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(color = Color(0xFFFF9800), radius = 6.dp.toPx())
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.fajr_dhuhr),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5F6368)
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                // Small crescent moon - simple arc
                                drawArc(
                                    color = Color(0xFF9C27B0),
                                    startAngle = -110f,
                                    sweepAngle = 220f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - 5.dp.toPx(), center.y - 5.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.asr_maghrib),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5F6368)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact link at the bottom
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

        // Language switcher button - OUTSIDE the scrollable Column to ensure it's clickable
        val currentLang = LanguageManager.getCurrentLanguage(context)
        TextButton(
            onClick = {
                // Toggle language
                val newLang = if (currentLang == "ar") "en" else "ar"
                android.util.Log.d("HomeScreen", "Language button clicked. Current: $currentLang, New: $newLang")

                // Save the new language
                LanguageManager.setLanguage(context, newLang)

                // Recreate activity to apply language change immediately
                if (activity != null) {
                    android.util.Log.d("HomeScreen", "Recreating activity...")
                    activity.recreate()
                } else {
                    android.util.Log.e("HomeScreen", "Activity is null! Cannot recreate.")
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
        ) {
            Text(
                text = if (currentLang == "ar") "English" else "العربية",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF9C27B0)
            )
        }

        // Share App button - Top-left corner
        IconButton(
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
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share App",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CompactStatBadge(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
