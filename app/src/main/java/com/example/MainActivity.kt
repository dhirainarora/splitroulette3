package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.Screen
import com.example.ui.SplitViewModel
import com.example.ui.screens.FateAnimationScreen
import com.example.ui.screens.FinalResultsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IndividualRevealScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupBillScreen
import com.example.ui.screens.SetupParticipantsScreen
import com.example.ui.screens.SetupRandomnessScreen
import com.example.ads.AdMobManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBlack
                ) {
                    SplitRouletteApp()
                }
            }
        }
    }
}

@Composable
fun SplitRouletteApp(viewModel: SplitViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Smart back handling for game flow
    BackHandler(enabled = state.currentScreen != Screen.Home) {
        when (state.currentScreen) {
            Screen.SetupParticipants -> viewModel.navigateTo(Screen.Home)
            Screen.SetupBill -> viewModel.navigateTo(Screen.SetupParticipants)
            Screen.SetupRandomness -> viewModel.navigateTo(Screen.SetupBill)
            Screen.FateAnimation -> viewModel.navigateTo(Screen.SetupRandomness)
            is Screen.IndividualReveal -> viewModel.skipToFinalResults()
            Screen.FinalResults -> viewModel.navigateTo(Screen.Home)
            Screen.History -> viewModel.navigateTo(Screen.Home)
            Screen.Settings -> viewModel.navigateTo(Screen.Home)
            is Screen.HistoryDetail -> viewModel.navigateTo(Screen.History)
            Screen.Home -> {}
        }
    }

    AnimatedContent(
        targetState = state.currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            Screen.Home -> HomeScreen(state = state, viewModel = viewModel)
            Screen.SetupParticipants -> SetupParticipantsScreen(state = state, viewModel = viewModel)
            Screen.SetupBill -> SetupBillScreen(state = state, viewModel = viewModel)
            Screen.SetupRandomness -> SetupRandomnessScreen(state = state, viewModel = viewModel)
            Screen.FateAnimation -> FateAnimationScreen(state = state, viewModel = viewModel)
            is Screen.IndividualReveal -> IndividualRevealScreen(
                currentIndex = screen.currentIndex,
                state = state,
                viewModel = viewModel
            )
            Screen.FinalResults -> FinalResultsScreen(state = state, viewModel = viewModel)
            Screen.History -> HistoryScreen(state = state, viewModel = viewModel)
            Screen.Settings -> SettingsScreen(state = state, viewModel = viewModel)
            is Screen.HistoryDetail -> HistoryScreen(state = state, viewModel = viewModel)
        }
    }
}
