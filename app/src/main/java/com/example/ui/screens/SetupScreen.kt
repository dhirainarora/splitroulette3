package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.engine.CurrencyConfig
import com.example.engine.RoundingOption
import com.example.engine.SplitEngine
import com.example.engine.SplitMode
import com.example.ui.Screen
import com.example.ui.SplitUiState
import com.example.ui.SplitViewModel
import com.example.ui.components.FlowProgressIndicator
import com.example.ui.components.ModeSelectionCard
import com.example.ui.components.PrimaryElectricButton
import com.example.ui.components.RoundingSelectionCard
import com.example.ui.components.SecondaryCardButton
import com.example.ui.components.SplitHeader
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBg
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

// -------------------------------------------------------------
// STEP 1: PARTICIPANTS
// -------------------------------------------------------------
@Composable
fun SetupParticipantsScreen(
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            SplitHeader(
                title = "Participants",
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
        ) {
            FlowProgressIndicator(step = 1, stepTitle = "Participants")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "How many people?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select between 2 and 20 friends participating in this split.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (state.peopleCount > 2) DarkCardElevated else DarkSurface)
                            .border(
                                1.dp,
                                if (state.peopleCount > 2) DarkBorder else DarkBorderSubtle,
                                CircleShape
                            )
                            .clickable(enabled = state.peopleCount > 2) {
                                SoundEffects.playTick()
                                viewModel.setPeopleCount(state.peopleCount - 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease count",
                            tint = if (state.peopleCount > 2) TextPrimary else TextTertiary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.peopleCount} PEOPLE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricVioletLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Group Size",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (state.peopleCount < 20) DarkCardElevated else DarkSurface)
                            .border(
                                1.dp,
                                if (state.peopleCount < 20) DarkBorder else DarkBorderSubtle,
                                CircleShape
                            )
                            .clickable(enabled = state.peopleCount < 20) {
                                SoundEffects.playTick()
                                viewModel.setPeopleCount(state.peopleCount + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase count",
                            tint = if (state.peopleCount < 20) TextPrimary else TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 5, 6, 8, 10).forEach { num ->
                        val isSelected = state.peopleCount == num
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricVioletSurface else DarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricViolet else DarkBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    SoundEffects.playTick()
                                    viewModel.setPeopleCount(num)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ElectricVioletLight else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Participant Names",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Name text fields
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.participantNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardElevated)
                                    .border(1.dp, DarkBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricVioletLight
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { viewModel.updateName(index, it) },
                                placeholder = {
                                    Text(
                                        text = "Person ${index + 1}",
                                        color = TextTertiary,
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (name.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateName(index, "") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TextTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkCard,
                                    unfocusedContainerColor = DarkCard,
                                    focusedBorderColor = ElectricViolet,
                                    unfocusedBorderColor = DarkBorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        }
                    }
                }

                if (state.validationError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRedBg)
                            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.validationError,
                            fontSize = 12.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                PrimaryElectricButton(
                    text = "CONTINUE TO BILL →",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.validateAndProceedFromParticipants()
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: BILL AMOUNT
// -------------------------------------------------------------
@Composable
fun SetupBillScreen(
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val billVal = state.billAmountText.toDoubleOrNull() ?: 0.0
    val avgShare = if (state.peopleCount > 0 && billVal > 0.0) billVal / state.peopleCount else 0.0
    val formattedAvg = SplitEngine.formatCurrency((avgShare * 100).toLong(), state.selectedCurrency)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            SplitHeader(
                title = "Bill Amount",
                showBack = true,
                isSoundEnabled = state.isSoundEnabled,
                onBack = { viewModel.navigateTo(Screen.SetupParticipants) },
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
        ) {
            FlowProgressIndicator(step = 2, stepTitle = "Bill Amount")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "How much is the bill?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter the exact total amount from your receipt or check.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Currency selector pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurrencyConfig.ALL.forEach { curr ->
                        val isSelected = state.selectedCurrency.code == curr.code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricVioletSurface else DarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricViolet else DarkBorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    SoundEffects.playTick()
                                    viewModel.setCurrency(curr)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${curr.symbol} ${curr.code}",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ElectricVioletLight else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Big amount input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCard)
                        .border(1.5.dp, if (state.billAmountText.isNotEmpty()) ElectricViolet else DarkBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.selectedCurrency.symbol,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricVioletLight
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = state.billAmountText,
                            onValueChange = { viewModel.setBillAmountText(it) },
                            placeholder = {
                                Text(
                                    text = "0",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Baseline comparison box
                if (billVal > 0.0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricVioletSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricVioletLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Equal Split Baseline: $formattedAvg each",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Across all ${state.peopleCount} friends. Fate will decide who pays more and who pays less!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                if (state.validationError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRedBg)
                            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.validationError,
                            fontSize = 12.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom CTA buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryElectricButton(
                    text = "CONTINUE TO RANDOMNESS →",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.validateAndProceedFromBill()
                    }
                )
                SecondaryCardButton(
                    text = "← BACK TO PARTICIPANTS",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.navigateTo(Screen.SetupParticipants)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: RANDOMNESS & READY TO SPLIT
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupRandomnessScreen(
    state: SplitUiState,
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val billVal = state.billAmountText.toDoubleOrNull() ?: 0.0
    val formattedBill = SplitEngine.formatCurrency((billVal * 100).toLong(), state.selectedCurrency)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            SplitHeader(
                title = "Randomness",
                showBack = true,
                isSoundEnabled = state.isSoundEnabled,
                onBack = { viewModel.navigateTo(Screen.SetupBill) },
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
        ) {
            FlowProgressIndicator(step = 3, stepTitle = "Fate Configuration")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Choose Randomness & Notes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your chaos level and clean denomination rounding.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mode Cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SplitMode.entries.forEach { mode ->
                        ModeSelectionCard(
                            mode = mode,
                            isSelected = state.selectedMode == mode,
                            onSelect = { viewModel.setMode(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Denomination Rounding",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Denomination Optimization: Shares are rounded to clean denominations for easy UPI or cash payment. Any odd receipt remainder is balanced to keep all other shares clean.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoundingOption.entries.forEach { opt ->
                        val isSelected = state.selectedRounding == opt
                        RoundingSelectionCard(
                            option = opt,
                            isSelected = isSelected,
                            onSelect = { viewModel.setRounding(opt) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ready To Split Review Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "READY TO SPLIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricVioletLight,
                                letterSpacing = 1.2.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricVioletSurface)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${state.selectedMode.name} MODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricVioletLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Bill",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                                Text(
                                    text = formattedBill,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Participants",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                                Text(
                                    text = "${state.peopleCount} Friends",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chips of participant names
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.participantNames.forEachIndexed { idx, name ->
                                val displayName = name.trim().ifEmpty { "Person ${idx + 1}" }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkCardElevated)
                                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom CTA buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryElectricButton(
                    text = "LET FATE DECIDE",
                    icon = Icons.Default.Bolt,
                    onClick = {
                        viewModel.startFateCalculation()
                    }
                )
                SecondaryCardButton(
                    text = "← BACK TO BILL",
                    onClick = {
                        viewModel.navigateTo(Screen.SetupBill)
                    }
                )
            }
        }
    }
}
