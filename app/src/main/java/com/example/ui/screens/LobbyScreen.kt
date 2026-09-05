package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ads.AdManager
import com.example.ads.AdTriggerType
import com.example.model.BoardTheme
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.sound.SoundManager
import com.example.ui.LudoViewModel
import com.example.ui.components.SponsorBannerBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LudoViewModel,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    var isSoundOn by remember { mutableStateOf(SoundManager.isSoundEnabled) }

    // Dialog states
    var showVsComputerDialog by remember { mutableStateOf(false) }
    var showPassNPlayDialog by remember { mutableStateOf(false) }
    var showOnlineDialog by remember { mutableStateOf(false) }
    var showPrivateRoomDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showLuckySpinDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column {
                SponsorBannerBar()
                // Vibrant Bottom Navigation Bar
                Surface(
                    color = VibrantSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = VibrantSlate100)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play (Active)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { /* Already on Play */ }
                        ) {
                            Text("🎮", fontSize = 22.sp)
                            Text(
                                "PLAY",
                                color = VibrantIndigo600,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }

                        // Spin Wheel
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showLuckySpinDialog = true }
                                .testTag("nav_spin")
                        ) {
                            Text("🎡", fontSize = 22.sp)
                            Text(
                                "SPIN",
                                color = VibrantSlate500,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }

                        // Stats & Rankings
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showLeaderboardDialog = true }
                                .testTag("nav_stats")
                        ) {
                            Text("📊", fontSize = 22.sp)
                            Text(
                                "STATS",
                                color = VibrantSlate500,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }

                        // Themes
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showThemeDialog = true }
                                .testTag("nav_themes")
                        ) {
                            Text("🎨", fontSize = 22.sp)
                            Text(
                                "THEMES",
                                color = VibrantSlate500,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = VibrantCanvasBg,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Header with user profile, coins & gems
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar & Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showLeaderboardDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(VibrantRed500, VibrantYellow500)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.userProfile.name.firstOrNull()?.toString()?.uppercase() ?: "A",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "PLAYER",
                                color = VibrantSlate400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                uiState.userProfile.name,
                                color = VibrantSlate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Coins, Gems & Sound Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(50))
                                .clip(RoundedCornerShape(50))
                                .background(VibrantSurface)
                                .border(1.dp, VibrantSlate100, RoundedCornerShape(50))
                                .clickable { viewModel.claimFreeCoins(500) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("🪙", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${uiState.userProfile.coins}",
                                color = VibrantSlate900,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        // Gems Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(50))
                                .clip(RoundedCornerShape(50))
                                .background(VibrantSurface)
                                .border(1.dp, VibrantSlate100, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("💎", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${uiState.userProfile.gems}",
                                color = VibrantSlate900,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        // Sound Toggle Button
                        IconButton(
                            onClick = {
                                isSoundOn = !isSoundOn
                                SoundManager.isSoundEnabled = isSoundOn
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(VibrantSurface)
                                .border(1.dp, VibrantSlate100, CircleShape)
                        ) {
                            Icon(
                                if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Toggle Sound",
                                tint = VibrantSlate900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Hero Banner: Online Battle
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(14.dp, RoundedCornerShape(24.dp))
                        .testTag("hero_online_battle")
                        .clickable { showOnlineDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantIndigo600)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎲", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "ONLINE BATTLE",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "Play with random players worldwide",
                                color = VibrantIndigo100,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        // Badge: Win x2 Coins
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(50))
                                .background(VibrantYellow400)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "WIN X2 COINS",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // 2x2 Vibrant Grid of Modes
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: Private Room & Local Match
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Red: Private Room
                        VibrantModeCard(
                            title = "PRIVATE\nROOM",
                            subtitle = "Play with friends",
                            icon = "🏠",
                            bgColor = VibrantRed500,
                            textColor = Color.White,
                            subtextColor = VibrantRed100,
                            testTag = "mode_private_room",
                            modifier = Modifier.weight(1f),
                            onClick = { showPrivateRoomDialog = true }
                        )

                        // Green: Local Match
                        VibrantModeCard(
                            title = "LOCAL\nMATCH",
                            subtitle = "Offline pass & play",
                            icon = "🤝",
                            bgColor = VibrantGreen500,
                            textColor = Color.White,
                            subtextColor = VibrantGreen100,
                            testTag = "mode_pass_n_play",
                            modifier = Modifier.weight(1f),
                            onClick = { showPassNPlayDialog = true }
                        )
                    }

                    // Row 2: VS Computer & Tournaments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Yellow: VS Computer
                        VibrantModeCard(
                            title = "VS\nCOMPUTER",
                            subtitle = "Train with AI",
                            icon = "🤖",
                            bgColor = VibrantYellow500,
                            textColor = Color.Black,
                            subtextColor = VibrantYellow900,
                            testTag = "mode_vs_computer",
                            modifier = Modifier.weight(1f),
                            onClick = { showVsComputerDialog = true }
                        )

                        // Blue: Tournaments / Rankings
                        VibrantModeCard(
                            title = "TOURNAMENTS",
                            subtitle = "Win big prizes",
                            icon = "🏆",
                            bgColor = VibrantBlue500,
                            textColor = Color.White,
                            subtextColor = VibrantBlue100,
                            testTag = "mode_online",
                            modifier = Modifier.weight(1f),
                            onClick = { showLeaderboardDialog = true }
                        )
                    }
                }
            }

            // Watch Ads Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .testTag("free_coins_button")
                        .clickable {
                            AdManager.showRewardedAd(AdTriggerType.DAILY_SPIN) {
                                viewModel.claimFreeCoins(500)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantSlate900)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = VibrantSlate700,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VibrantYellow400),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📺", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        "WATCH ADS",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Get +500 Coins & 1 Gem",
                                        color = VibrantYellow400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VibrantIndigo500)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "CLAIM",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // --- MODALS & DIALOGS (Vibrant Palette Styled) ---

    // 1. Vs Computer Dialog
    if (showVsComputerDialog) {
        var selectedCount by remember { mutableIntStateOf(4) }
        var selectedDifficulty by remember { mutableStateOf(GameDifficulty.MEDIUM) }

        AlertDialog(
            onDismissRequest = { showVsComputerDialog = false },
            title = { Text("Play vs Computer", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Player Count:", color = VibrantSlate700, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4).forEach { count ->
                            FilterChip(
                                selected = selectedCount == count,
                                onClick = { selectedCount = count },
                                label = { Text("$count Players") }
                            )
                        }
                    }

                    Text("Select AI Difficulty:", color = VibrantSlate700, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GameDifficulty.values().forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff.name.lowercase().capitalize()) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVsComputerDialog = false
                        viewModel.initDefaultMatch(
                            mode = GameMode.VS_COMPUTER,
                            playerCount = selectedCount,
                            difficulty = selectedDifficulty
                        )
                        onStartGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantYellow500)
                ) {
                    Text("START MATCH", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVsComputerDialog = false }) {
                    Text("CANCEL", color = VibrantSlate500)
                }
            },
            containerColor = Color.White
        )
    }

    // 2. Pass & Play Dialog
    if (showPassNPlayDialog) {
        var playerCount by remember { mutableIntStateOf(4) }
        var p1Name by remember { mutableStateOf("Red Player") }
        var p2Name by remember { mutableStateOf("Green Player") }
        var p3Name by remember { mutableStateOf("Yellow Player") }
        var p4Name by remember { mutableStateOf("Blue Player") }

        AlertDialog(
            onDismissRequest = { showPassNPlayDialog = false },
            title = { Text("Local Pass & Play", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Player Count:", color = VibrantSlate700, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4).forEach { count ->
                            FilterChip(
                                selected = playerCount == count,
                                onClick = { playerCount = count },
                                label = { Text("$count Players") }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = p1Name,
                        onValueChange = { p1Name = it },
                        label = { Text("Player 1 (Red)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Player 2 (Green)") },
                        singleLine = true
                    )
                    if (playerCount >= 3) {
                        OutlinedTextField(
                            value = p3Name,
                            onValueChange = { p3Name = it },
                            label = { Text("Player 3 (Yellow)") },
                            singleLine = true
                        )
                    }
                    if (playerCount == 4) {
                        OutlinedTextField(
                            value = p4Name,
                            onValueChange = { p4Name = it },
                            label = { Text("Player 4 (Blue)") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPassNPlayDialog = false
                        val names = listOf(p1Name, p2Name, p3Name, p4Name).take(playerCount)
                        viewModel.initDefaultMatch(
                            mode = GameMode.LOCAL_PASS_N_PLAY,
                            playerCount = playerCount,
                            playerNames = names
                        )
                        onStartGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen500)
                ) {
                    Text("START GAME", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPassNPlayDialog = false }) {
                    Text("CANCEL", color = VibrantSlate500)
                }
            },
            containerColor = Color.White
        )
    }

    // 3. Online Matchmaking Dialog
    if (showOnlineDialog) {
        var isSearching by remember { mutableStateOf(false) }
        var matchedCount by remember { mutableIntStateOf(1) }

        LaunchedEffect(isSearching) {
            if (isSearching) {
                while (matchedCount < 4) {
                    delay(700)
                    matchedCount++
                }
                delay(600)
                showOnlineDialog = false
                viewModel.initDefaultMatch(
                    mode = GameMode.ONLINE_MULTIPLAYER,
                    playerCount = 4
                )
                onStartGame()
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isSearching) showOnlineDialog = false },
            title = { Text("Online Matchmaking", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = VibrantIndigo600)
                        Text(
                            "Finding live players... ($matchedCount/4)",
                            color = VibrantSlate900,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Connecting to ABS Ludo Global Server",
                            color = VibrantSlate500,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "Match with online Ludo champions worldwide! Entry: 100 Coins",
                            color = VibrantSlate700,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(VibrantSlate100, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Prize Pool: ", color = VibrantSlate900)
                            Text("🪙 400 Coins", color = VibrantYellow500, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isSearching) {
                    Button(
                        onClick = { isSearching = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantIndigo600)
                    ) {
                        Text("FIND MATCH", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isSearching) {
                    TextButton(onClick = { showOnlineDialog = false }) {
                        Text("CANCEL", color = VibrantSlate500)
                    }
                }
            },
            containerColor = Color.White
        )
    }

    // 4. Private Room Dialog
    if (showPrivateRoomDialog) {
        var isCreateTab by remember { mutableStateOf(true) }
        var inputRoomCode by remember { mutableStateOf("") }
        val generatedCode = remember { "ABS-${Random.nextInt(1000, 9999)}" }

        AlertDialog(
            onDismissRequest = { showPrivateRoomDialog = false },
            title = { Text("Play with Friends", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TabRow(
                        selectedTabIndex = if (isCreateTab) 0 else 1,
                        containerColor = VibrantSlate100,
                        contentColor = VibrantSlate900
                    ) {
                        Tab(
                            selected = isCreateTab,
                            onClick = { isCreateTab = true },
                            text = { Text("Create Room") }
                        )
                        Tab(
                            selected = !isCreateTab,
                            onClick = { isCreateTab = false },
                            text = { Text("Join Room") }
                        )
                    }

                    if (isCreateTab) {
                        Text("Share this Room Code with friends:", color = VibrantSlate700, fontSize = 13.sp)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = VibrantSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    generatedCode,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = VibrantRed500,
                                    letterSpacing = 3.sp
                                )
                            }
                        }
                    } else {
                        Text("Enter your friend's Room Code:", color = VibrantSlate700, fontSize = 13.sp)
                        OutlinedTextField(
                            value = inputRoomCode,
                            onValueChange = { inputRoomCode = it.uppercase() },
                            placeholder = { Text("e.g. ABS-1234") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivateRoomDialog = false
                        val code = if (isCreateTab) generatedCode else inputRoomCode
                        viewModel.initDefaultMatch(
                            mode = GameMode.PRIVATE_ROOM,
                            playerCount = 4,
                            customRoomCode = code
                        )
                        onStartGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantRed500)
                ) {
                    Text(if (isCreateTab) "START ROOM" else "JOIN ROOM", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivateRoomDialog = false }) {
                    Text("CANCEL", color = VibrantSlate500)
                }
            },
            containerColor = Color.White
        )
    }

    // 5. Board Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Board Theme", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BoardTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.theme == theme) VibrantSlate100 else Color.White)
                                .border(1.dp, if (uiState.theme == theme) VibrantIndigo600 else VibrantSlate100, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(theme.title, color = VibrantSlate900, fontWeight = FontWeight.SemiBold)
                            if (uiState.theme == theme) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = VibrantIndigo600)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("DONE", color = VibrantIndigo600, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    // 6. Leaderboard / Stats Dialog
    if (showLeaderboardDialog) {
        AlertDialog(
            onDismissRequest = { showLeaderboardDialog = false },
            title = { Text("Player Profile & Stats", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatBox("Matches", "${uiState.userProfile.matches}")
                        StatBox("Wins", "${uiState.userProfile.wins}")
                        val winRate = if (uiState.userProfile.matches > 0) {
                            (uiState.userProfile.wins * 100) / uiState.userProfile.matches
                        } else 0
                        StatBox("Win Rate", "$winRate%")
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Top ABS Ludo Rankings:", fontWeight = FontWeight.Bold, color = VibrantIndigo600)

                    val topRankers = listOf(
                        Triple("1. Shakib_LudoKing", "3,420 Wins", "👑 Gold"),
                        Triple("2. Rahim_DiceMaster", "2,890 Wins", "🥈 Silver"),
                        Triple("3. ${uiState.userProfile.name} (You)", "${uiState.userProfile.wins} Wins", "🥉 Bronze"),
                        Triple("4. Tanjil_Pro", "1,940 Wins", "#4")
                    )

                    topRankers.forEach { (name, winStr, rank) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VibrantSlate100, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, color = VibrantSlate900, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(winStr, color = VibrantIndigo600, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLeaderboardDialog = false }) {
                    Text("CLOSE", color = VibrantSlate900, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    // 7. Lucky Spin Dialog
    if (showLuckySpinDialog) {
        var isSpinning by remember { mutableStateOf(false) }
        var prizeWon by remember { mutableStateOf<String?>(null) }
        var rotationAngle by remember { mutableFloatStateOf(0f) }

        AlertDialog(
            onDismissRequest = { if (!isSpinning) showLuckySpinDialog = false },
            title = { Text("Lucky Fortune Spin", fontWeight = FontWeight.Bold, color = VibrantSlate900) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(rotationAngle)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        VibrantRed500,
                                        VibrantYellow500,
                                        VibrantGreen500,
                                        VibrantBlue500,
                                        VibrantIndigo600,
                                        VibrantRed500
                                    )
                                )
                            )
                            .border(4.dp, VibrantYellow400, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎡", fontSize = 48.sp)
                    }

                    if (prizeWon != null) {
                        Text("🎉 You won $prizeWon!", color = VibrantGreen500, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("Spin to win free coins every day!", color = VibrantSlate700, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                if (!isSpinning) {
                    Button(
                        onClick = {
                            isSpinning = true
                            prizeWon = null
                            coroutineScope.launch {
                                val turns = Random.nextInt(4, 8) * 360f + Random.nextInt(0, 360)
                                rotationAngle += turns
                                delay(1800)
                                val prizes = listOf("250 Coins", "500 Coins", "1000 Coins", "750 Coins")
                                val won = prizes.random()
                                prizeWon = won
                                isSpinning = false
                                SoundManager.playCoinSound()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantYellow500)
                    ) {
                        Text("SPIN NOW", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isSpinning) {
                    TextButton(
                        onClick = {
                            showLuckySpinDialog = false
                            AdManager.showRewardedAd(AdTriggerType.DAILY_SPIN) {
                                viewModel.claimFreeCoins(300)
                            }
                        }
                    ) {
                        Text("Watch Ad (+300 Coins)", color = VibrantIndigo600)
                    }
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun VibrantModeCard(
    title: String,
    subtitle: String,
    icon: String,
    bgColor: Color,
    textColor: Color,
    subtextColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(135.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(icon, fontSize = 28.sp)
            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = subtextColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(VibrantSlate100, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(value, color = VibrantSlate900, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = VibrantSlate500, fontSize = 11.sp)
    }
}

