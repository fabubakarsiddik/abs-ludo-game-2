package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ads.AdManager
import com.example.ads.AdTriggerType
import com.example.model.*
import com.example.sound.SoundManager
import com.example.ui.LudoViewModel
import com.example.ui.components.Dice3DView
import com.example.ui.components.LudoBoardCanvas
import com.example.ui.components.SponsorBannerBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val activePlayer = uiState.players.getOrNull(uiState.activePlayerIndex)
    var isSoundOn by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var showPauseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (uiState.gameMode) {
                                GameMode.VS_COMPUTER -> "ABS Ludo • Vs AI"
                                GameMode.LOCAL_PASS_N_PLAY -> "Pass & Play"
                                GameMode.ONLINE_MULTIPLAYER -> "Live Multiplayer"
                                GameMode.PRIVATE_ROOM -> "Room: ${uiState.roomCode}"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VibrantSlate900
                        )
                        Text(
                            text = uiState.statusMessage,
                            fontSize = 11.sp,
                            color = VibrantIndigo600,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showPauseDialog = true },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VibrantSlate900
                        )
                    }
                },
                actions = {
                    // Sound toggle
                    IconButton(
                        onClick = {
                            isSoundOn = !isSoundOn
                            SoundManager.isSoundEnabled = isSoundOn
                        }
                    ) {
                        Icon(
                            if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = VibrantSlate900
                        )
                    }
                    // Guaranteed 6 Ad Reroll Button
                    IconButton(
                        onClick = { viewModel.rerollWithGuaranteedSix() },
                        modifier = Modifier.testTag("reroll_ad_button")
                    ) {
                        Icon(
                            Icons.Default.Casino,
                            contentDescription = "Reroll 6 Ad",
                            tint = VibrantYellow500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VibrantSurface
                )
            )
        },
        bottomBar = {
            SponsorBannerBar()
        },
        containerColor = VibrantCanvasBg,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Players Row (Green & Yellow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Left: Green (Player 1)
                uiState.players.find { it.color == LudoColor.GREEN }?.let { player ->
                    PlayerMiniCard(
                        player = player,
                        isActive = activePlayer?.id == player.id,
                        modifier = Modifier.width(155.dp)
                    )
                }

                // Top Right: Yellow (Player 2)
                uiState.players.find { it.color == LudoColor.YELLOW }?.let { player ->
                    PlayerMiniCard(
                        player = player,
                        isActive = activePlayer?.id == player.id,
                        modifier = Modifier.width(155.dp)
                    )
                }
            }

            // Center Ludo Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                LudoBoardCanvas(
                    players = uiState.players,
                    activePlayerColor = activePlayer?.color ?: LudoColor.RED,
                    movableTokenIds = uiState.movableTokenIds,
                    theme = uiState.theme,
                    onTokenClick = { token ->
                        viewModel.moveToken(token)
                    },
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }

            // Bottom Players Row (Red & Blue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottom Left: Red (Player 0)
                uiState.players.find { it.color == LudoColor.RED }?.let { player ->
                    PlayerMiniCard(
                        player = player,
                        isActive = activePlayer?.id == player.id,
                        modifier = Modifier.width(155.dp)
                    )
                }

                // Bottom Right: Blue (Player 3)
                uiState.players.find { it.color == LudoColor.BLUE }?.let { player ->
                    PlayerMiniCard(
                        player = player,
                        isActive = activePlayer?.id == player.id,
                        modifier = Modifier.width(155.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Control Bar: Interactive 3D Dice & Emoji Reactions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VibrantSlate100, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quick Emojis
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("🔥", "😂", "👍", "👏", "👑").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(VibrantCanvasBg)
                                    .border(1.dp, VibrantSlate100, CircleShape)
                                    .clickable { viewModel.sendEmoji(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 16.sp)
                            }
                        }
                    }

                    // Turn Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (activePlayer != null) "${activePlayer.name}'s Turn" else "Turn",
                            color = activePlayer?.color?.primaryColor ?: VibrantIndigo600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (uiState.hasRolled) "Select Token" else "Tap Dice",
                            color = VibrantSlate500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Interactive 3D Dice
                    Dice3DView(
                        value = uiState.diceValue,
                        isRolling = uiState.isRolling,
                        isTurn = activePlayer?.type == PlayerType.HUMAN && !uiState.hasRolled,
                        color = activePlayer?.color ?: LudoColor.RED,
                        size = 56.dp,
                        onClick = {
                            viewModel.rollDice()
                        }
                    )
                }
            }

            // Recent Chat Messages Float
            if (uiState.chatMessages.isNotEmpty()) {
                val latest = uiState.chatMessages.last()
                Text(
                    text = "${latest.senderName}: ${latest.emoji}",
                    color = VibrantSlate700,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    // Pause Dialog
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("Leave Match?", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Text(
                    "Are you sure you want to exit the current ABS Ludo match?",
                    color = VibrantSlate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPauseDialog = false
                        onBackToLobby()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantRed500)
                ) {
                    Text("EXIT MATCH", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("RESUME", color = VibrantSlate500)
                }
            },
            containerColor = Color.White
        )
    }

    // Victory Celebration Dialog with Ad Rewards
    if (uiState.showVictoryDialog && uiState.winner != null) {
        Dialog(
            onDismissRequest = { /* Require action */ }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("victory_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("👑", fontSize = 48.sp)
                    Text(
                        "VICTORY!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = VibrantYellow500,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "${uiState.winner.name} won the game!",
                        color = VibrantSlate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(VibrantSlate100, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Reward: ", color = VibrantSlate900)
                        Text("+500 Coins 🪙", color = VibrantYellow500, fontWeight = FontWeight.Bold)
                    }

                    // Double reward ad button
                    Button(
                        onClick = {
                            AdManager.showRewardedAd(AdTriggerType.VICTORY_DOUBLE) {
                                viewModel.claimFreeCoins(1000)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("double_reward_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen500),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DOUBLE TO 1000 COINS (AD)", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Play Again
                    OutlinedButton(
                        onClick = {
                            viewModel.dismissVictoryDialog()
                            viewModel.initDefaultMatch(
                                mode = uiState.gameMode,
                                playerCount = uiState.players.size,
                                difficulty = uiState.difficulty
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantSlate900)
                    ) {
                        Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                    }

                    // Return to Lobby
                    TextButton(
                        onClick = {
                            viewModel.dismissVictoryDialog()
                            onBackToLobby()
                        }
                    ) {
                        Text("RETURN TO LOBBY", color = VibrantSlate500)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerMiniCard(
    player: Player,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border"
    )

    Card(
        modifier = modifier
            .shadow(if (isActive) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .border(
                width = if (isActive) pulseBorder.dp else 1.dp,
                color = if (isActive) player.color.primaryColor else VibrantSlate100,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = VibrantSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(player.color.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.firstOrNull()?.toString() ?: "P",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = VibrantSlate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Home: ${player.finishedCount}/4",
                    color = player.color.primaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
