package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.findActivity
import com.example.audio.SoundEffects
import com.example.engine.SplitEngine
import com.example.ui.SplitUiState
import com.example.ui.SplitViewModel
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletDark
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.ElectricVioletSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@Composable
fun FateAnimationScreen(
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transitionTriggered = remember { AtomicBoolean(false) }

    val handleAnimationFinished = remember(context, viewModel) {
        {
            if (transitionTriggered.compareAndSet(false, true)) {
                val activity = context.findActivity()
                AdMobManager.showInterstitial(activity) {
                    viewModel.onAnimationFinished()
                }
            }
        }
    }

    val participants = state.activeResult?.participants ?: emptyList()
    val names = remember(participants) {
        if (participants.isNotEmpty()) participants.map { it.name }
        else state.participantNames.filter { it.isNotBlank() }.ifEmpty { listOf("Friend 1", "Friend 2") }
    }

    var displayedName by remember { mutableStateOf(names.firstOrNull() ?: "Fate") }
    var displayedAmount by remember { mutableStateOf("...") }
    val progress = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Continuous rotation for spinner glow
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    LaunchedEffect(Unit) {
        val totalDurationMs = 2800L
        val intervalMs = 90L
        val steps = (totalDurationMs / intervalMs).toInt()

        for (i in 0 until steps) {
            val randomName = names.random()
            displayedName = randomName

            val dummyCents = (Random.nextLong(10, 500) * 1000L)
            displayedAmount = SplitEngine.formatCurrency(dummyCents, state.selectedCurrency)

            SoundEffects.playTick()
            progress.animateTo(
                targetValue = (i + 1).toFloat() / steps.toFloat(),
                animationSpec = tween(intervalMs.toInt(), easing = LinearEasing)
            )
        }

        // Final chime and transition
        SoundEffects.playChime()
        delay(150)
        handleAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ElectricVioletSurface)
                    .border(1.dp, ElectricViolet.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ElectricVioletLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FATE IS DECIDING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricVioletLight,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Shuffling Card with Rotating Gradient Border
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                ElectricViolet,
                                DarkCardElevated,
                                ElectricVioletDark,
                                DarkBorder,
                                ElectricViolet
                            )
                        )
                    )
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                        .background(DarkCard)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                                .border(1.dp, ElectricViolet.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricVioletLight,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = displayedName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = displayedAmount,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricVioletLight,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Consulting Fate...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(CircleShape),
                color = ElectricViolet,
                trackColor = DarkBorderSubtle,
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Skip button
            Text(
                text = "Skip animation",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        SoundEffects.playChime()
                        handleAnimationFinished()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
