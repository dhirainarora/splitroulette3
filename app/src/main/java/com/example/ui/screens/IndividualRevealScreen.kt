package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.ui.SplitUiState
import com.example.ui.SplitViewModel
import com.example.ui.components.BiggestHitBadge
import com.example.ui.components.PrimaryElectricButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletDark
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.ElectricVioletSurface
import com.example.ui.theme.EmeraldExact
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RouletteCoral
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun IndividualRevealScreen(
    currentIndex: Int,
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val result = state.activeResult ?: return
    val participants = result.participants
    val totalCount = participants.size
    val currentPerson = participants.getOrNull(currentIndex) ?: return
    val isLast = currentIndex == totalCount - 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "REVEAL ${currentIndex + 1} OF $totalCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricVioletLight,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = {
                        SoundEffects.playTick()
                        viewModel.skipToFinalResults()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close reveal",
                        tint = TextSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalCount) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentIndex) 18.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == currentIndex) ElectricViolet
                                else if (i < currentIndex) ElectricVioletDark
                                else DarkBorder
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Reveal Hero Card
            AnimatedContent(
                targetState = currentPerson,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.92f)).togetherWith(fadeOut())
                },
                label = "revealContent",
                modifier = Modifier.fillMaxWidth()
            ) { person ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(DarkCardElevated, DarkCard)
                            )
                        )
                        .border(
                            1.5.dp,
                            if (person.isBiggestHit) ElectricViolet else DarkBorder,
                            RoundedCornerShape(28.dp)
                        )
                        .padding(vertical = 36.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (person.isBiggestHit) {
                            BiggestHitBadge()
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Rank #${person.rank}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Avatar icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ElectricVioletSurface)
                                .border(1.5.dp, ElectricVioletLight.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = person.name.take(1).uppercase(),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricVioletLight
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = person.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "PAYS EXACTLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = person.formattedAmount,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricVioletLight,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Diff from baseline & percentage
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val diffColor = if (person.diffFromAverageCents >= 0) RouletteCoral else EmeraldExact
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(diffColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${person.formattedDiff} vs avg",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = diffColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f%% of bill", person.percentageOfTotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryElectricButton(
                    text = if (isLast) "SEE ALL RESULTS →" else "NEXT PERSON →",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        viewModel.nextReveal()
                    }
                )

                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundEffects.playTick()
                                viewModel.skipToFinalResults()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Skip to All Results",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
