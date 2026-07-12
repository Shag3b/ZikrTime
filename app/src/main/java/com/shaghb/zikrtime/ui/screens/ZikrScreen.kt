package com.shaghb.zikrtime.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaghb.zikrtime.R
import com.shaghb.zikrtime.data.Zikr
import com.shaghb.zikrtime.data.ZikrRepository
import com.shaghb.zikrtime.AzkarProgressManager
import com.shaghb.zikrtime.LanguageManager
import kotlinx.coroutines.delay

@Composable
fun ZikrScreen(
    isMorning: Boolean,
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    val azkar = remember {
        if (isMorning)
            ZikrRepository.loadMorningAzkar(context)
        else
            ZikrRepository.loadEveningAzkar(context)
    }

    // Restore saved progress
    val savedProgress = remember {
        if (isMorning) {
            AzkarProgressManager.getMorningProgress(context)
        } else {
            AzkarProgressManager.getEveningProgress(context)
        }
    }

    var currentIndex by remember { mutableIntStateOf(savedProgress.first) }
    var repeatCount by remember { mutableIntStateOf(
        if (savedProgress.first == 0 && savedProgress.second == 1) {
            azkar[0].count  // First time, use initial count
        } else {
            savedProgress.second  // Resume with saved count
        }
    ) }
    var secondsLeft by remember { mutableIntStateOf(3) }
    var canGoNext by remember { mutableStateOf(false) }

    // Toggle state for Franco/Meaning (only used in English mode)
    var showFranco by remember { mutableStateOf(true) }

    // Save progress whenever it changes
    LaunchedEffect(currentIndex, repeatCount) {
        if (isMorning) {
            AzkarProgressManager.saveMorningProgress(context, currentIndex, repeatCount)
        } else {
            AzkarProgressManager.saveEveningProgress(context, currentIndex, repeatCount)
        }
    }

    val currentZikr: Zikr = azkar[currentIndex]
    val isArabic = LanguageManager.isArabic(context)

    LaunchedEffect(currentIndex) {
        secondsLeft = 3
        canGoNext = false

        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }

        canGoNext = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Progress indicator - Compact design
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.zikr_counter, currentIndex + 1, azkar.size),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF1A2332)
                        )
                    )

                    // Circular progress indicator - Smaller
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(32.dp)) {
                            val progress = (currentIndex + 1).toFloat() / azkar.size.toFloat()
                            drawCircle(
                                color = Color(0xFFE0E0E0),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFF4CAF50),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${((currentIndex + 1).toFloat() / azkar.size * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zikr Text Card - Takes most of the screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // Determine which text to display based on language and toggle
                val zikrText = when {
                    isArabic -> currentZikr.text  // Arabic mode: show Arabic text
                    showFranco -> currentZikr.textFranco  // English mode + Franco toggle
                    else -> currentZikr.textMeaning  // English mode + Meaning toggle
                }

                // ALWAYS make the card scrollable to ensure all text is visible
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = zikrText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            lineHeight = 44.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A2332),
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle button for Franco/Meaning (only for English mode)
            if (!isArabic) {
                Button(
                    onClick = { showFranco = !showFranco },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = stringResource(if (showFranco) R.string.show_meaning else R.string.show_franco),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Counter Card - Compact design
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat counter (changes as user clicks)
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            stringResource(R.string.repeat_left),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFFE65100).copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$repeatCount",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 28.sp
                            )
                        )
                    }

                    // Total count (fixed, shows original zikr count)
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            stringResource(R.string.total_count),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF5F6368)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${currentZikr.count}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5F6368),
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (repeatCount > 0) {
                Button(
                    onClick = { repeatCount-- },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(
                        stringResource(R.string.decrease_counter),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!canGoNext) {
                Text(
                    pluralStringResource(R.plurals.wait, secondsLeft, secondsLeft),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF9E9E9E)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Next button - requires timer
            if (canGoNext) {
                Button(
                    onClick = {
                        if (currentIndex < azkar.lastIndex) {
                            currentIndex++
                            repeatCount = azkar[currentIndex].count
                        } else {
                            // Just trigger completion - cleanup handled in onFinished
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        stringResource(R.string.next),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Previous button - no timer needed, always enabled, only show if not on first azkar
            if (currentIndex > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        currentIndex--
                        repeatCount = azkar[currentIndex].count
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    enabled = true  // Always enabled, no timer
                ) {
                    Text(
                        stringResource(R.string.previous),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
