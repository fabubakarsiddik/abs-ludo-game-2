package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdManager
import com.example.ui.LudoViewModel
import com.example.ui.components.InAppAdOverlay
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    LOBBY,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: LudoViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(ScreenState.LOBBY) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            ScreenState.LOBBY -> {
                                LobbyScreen(
                                    viewModel = viewModel,
                                    onStartGame = {
                                        currentScreen = ScreenState.GAME
                                    }
                                )
                            }
                            ScreenState.GAME -> {
                                GameScreen(
                                    viewModel = viewModel,
                                    onBackToLobby = {
                                        currentScreen = ScreenState.LOBBY
                                    }
                                )
                            }
                        }
                    }

                    // Global in-app ad overlay if active
                    val activeAd = AdManager.activeAd
                    if (activeAd != null && activeAd.isShowing) {
                        InAppAdOverlay(
                            adState = activeAd,
                            onDismiss = { rewardGranted ->
                                AdManager.dismissActiveAd(rewardGranted)
                            }
                        )
                    }
                }
            }
        }
    }
}
