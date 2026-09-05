package com.example.model

import androidx.compose.ui.graphics.Color

enum class LudoColor(
    val title: String,
    val primaryColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val startIndex: Int,
    val homeEntryIndex: Int
) {
    RED(
        title = "Red",
        primaryColor = Color(0xFFE53935),
        darkColor = Color(0xFFB71C1C),
        lightColor = Color(0xFFFFCDD2),
        startIndex = 0,
        homeEntryIndex = 50
    ),
    GREEN(
        title = "Green",
        primaryColor = Color(0xFF43A047),
        darkColor = Color(0xFF1B5E20),
        lightColor = Color(0xFFC8E6C9),
        startIndex = 13,
        homeEntryIndex = 11
    ),
    YELLOW(
        title = "Yellow",
        primaryColor = Color(0xFFFDD835),
        darkColor = Color(0xFFF57F17),
        lightColor = Color(0xFFFFF9C4),
        startIndex = 26,
        homeEntryIndex = 24
    ),
    BLUE(
        title = "Blue",
        primaryColor = Color(0xFF1E88E5),
        darkColor = Color(0xFF0D47A1),
        lightColor = Color(0xFFBBDEFB),
        startIndex = 39,
        homeEntryIndex = 37
    )
}

enum class PlayerType {
    HUMAN,
    AI,
    ONLINE_REMOTE
}

enum class GameMode {
    VS_COMPUTER,
    LOCAL_PASS_N_PLAY,
    ONLINE_MULTIPLAYER,
    PRIVATE_ROOM
}

enum class GameDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class BoardTheme(val title: String, val boardBg: Color, val gridLine: Color) {
    CLASSIC("Classic Clean", Color(0xFFFFFFFF), Color(0xFFE0E0E0)),
    ROYAL_WOOD("Royal Wood", Color(0xFF2C1810), Color(0xFF5D4037)),
    NEON_CYBER("Cyber Neon", Color(0xFF0D1117), Color(0xFF30363D)),
    GOLD_EMERALD("Gold Emerald", Color(0xFF0A231C), Color(0xFFD4AF37))
}

data class Token(
    val id: Int, // 0..3
    val color: LudoColor,
    val step: Int = -1 // -1 = yard/base, 0..50 = track, 51..55 = home track, 56 = finished
) {
    val isAtBase: Boolean get() = step == -1
    val isFinished: Boolean get() = step >= 56
    val isInHomeTrack: Boolean get() = step in 51..55
}

data class Player(
    val id: Int,
    val name: String,
    val color: LudoColor,
    val type: PlayerType,
    val avatarId: Int = 0,
    val tokens: List<Token> = List(4) { Token(id = it, color = color) },
    val isWinner: Boolean = false,
    val rank: Int = 0
) {
    val finishedCount: Int get() = tokens.count { it.isFinished }
    val hasWon: Boolean get() = finishedCount == 4
}

data class ChatMessage(
    val senderName: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val name: String = "Shakil Ahmed",
    val coins: Int = 1250,
    val gems: Int = 42,
    val wins: Int = 18,
    val matches: Int = 24,
    val level: Int = 4,
    val avatarIndex: Int = 0
)
