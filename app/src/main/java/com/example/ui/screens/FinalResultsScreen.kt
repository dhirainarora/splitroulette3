package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.engine.SplitParticipant
import com.example.engine.SplitResult
import com.example.ui.Screen
import com.example.ui.SplitUiState
import com.example.ui.SplitViewModel
import com.example.ui.components.BiggestHitBadge
import com.example.ui.components.ExactSumVerificationBadge
import com.example.ui.components.PrimaryElectricButton
import com.example.ui.components.SecondaryCardButton
import com.example.ui.components.SplitHeader
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
fun FinalResultsScreen(
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val result = state.activeResult ?: return
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            SplitHeader(
                title = "Fate Results",
                showBack = true,
                isSoundEnabled = state.isSoundEnabled,
                onBack = { viewModel.navigateTo(Screen.Home) },
                onToggleSound = { viewModel.toggleSound() },
                onOpenHistory = { viewModel.navigateTo(Screen.History) },
                onOpenSettings = { viewModel.navigateTo(Screen.Settings) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricVioletSurface)
                            .border(1.dp, ElectricViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${result.mode.name} MODE COMPLETED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricVioletLight,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Fate Has Spoken",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Total Bill Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(DarkCardElevated, DarkCard)
                                )
                            )
                            .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL BILL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "${result.participants.size} Friends",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = result.formattedTotal,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            ExactSumVerificationBadge(note = result.roundingNote)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Ranked Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(result.participants) { participant ->
                    ParticipantResultRow(participant = participant)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryElectricButton(
                    text = "SHARE RESULTS",
                    icon = Icons.Default.Share,
                    onClick = {
                        shareSplitResults(context, result)
                    }
                )

                SecondaryCardButton(
                    text = "NEW SPLIT",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel.startNewSplit()
                    }
                )
            }
        }
    }
}

@Composable
fun ParticipantResultRow(
    participant: SplitParticipant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(
                1.dp,
                if (participant.isBiggestHit) ElectricViolet.copy(alpha = 0.5f) else DarkBorderSubtle,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (participant.isBiggestHit) ElectricVioletSurface else DarkCardElevated)
                        .border(
                            1.dp,
                            if (participant.isBiggestHit) ElectricViolet else DarkBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${participant.rank}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (participant.isBiggestHit) ElectricVioletLight else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participant.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (participant.isBiggestHit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            BiggestHitBadge()
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pctFormatted = String.format(java.util.Locale.US, "%.1f%%", participant.percentageOfTotal)
                        Text(
                            text = "$pctFormatted of bill • ",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        val diffColor = if (participant.diffFromAverageCents >= 0) RouletteCoral else EmeraldExact
                        Text(
                            text = "${participant.formattedDiff} vs avg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = diffColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = participant.formattedAmount,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (participant.isBiggestHit) ElectricVioletLight else TextPrimary
            )
        }
    }
}

fun shareSplitResults(context: Context, result: SplitResult) {
    SoundEffects.playTick()

    val sb = StringBuilder()
    sb.append("⚡ Split Roulette\n")
    sb.append("“Don’t split the bill. Let fate split it.”\n\n")
    sb.append("💰 Total Bill: ${result.formattedTotal} (${result.participants.size} People • ${result.mode.name} Mode)\n")
    sb.append("🛡️ ${result.roundingNote}\n\n")
    sb.append("🏆 RANKED BREAKDOWN:\n")

    result.participants.forEach { p ->
        val pct = String.format(java.util.Locale.US, "%.1f%%", p.percentageOfTotal)
        val hitTag = if (p.isBiggestHit) " • BIGGEST HIT 🔥" else ""
        sb.append("${p.rank}. ${p.name} — ${p.formattedAmount} ($pct • ${p.formattedDiff} vs avg$hitTag)\n")
    }

    sb.append("\n🎯 Fate Has Spoken via Split Roulette!")
    val textToShare = sb.toString()

    // Copy to clipboard as convenient fallback
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Split Roulette Results", textToShare))
    Toast.makeText(context, "Copied breakdown to clipboard!", Toast.LENGTH_SHORT).show()

    // Launch share sheet
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Fate Split Results")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)
}
