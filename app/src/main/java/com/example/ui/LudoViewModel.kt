package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.ads.AdTriggerType
import com.example.logic.LudoAI
import com.example.logic.LudoBoardPath
import com.example.model.*
import com.example.sound.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LudoUiState(
    val players: List<Player> = emptyList(),
    val activePlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val hasRolled: Boolean = false,
    val consecutiveSixes: Int = 0,
    val movableTokenIds: Set<Int> = emptySet(),
    val gameMode: GameMode = GameMode.VS_COMPUTER,
    val difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val theme: BoardTheme = BoardTheme.CLASSIC,
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val statusMessage: String = "Roll the dice to start!",
    val chatMessages: List<ChatMessage> = emptyList(),
    val roomCode: String = "ABS-7482",
    val userProfile: UserProfile = UserProfile(),
    val isOnlineSearching: Boolean = false,
    val showVictoryDialog: Boolean = false
)

class LudoViewModel : ViewModel() {
    var uiState by mutableStateOf(LudoUiState())
        private set

    private var aiEngine = LudoAI(GameDifficulty.MEDIUM)

    init {
        initDefaultMatch(GameMode.VS_COMPUTER, 4, GameDifficulty.MEDIUM)
    }

    fun initDefaultMatch(
        mode: GameMode,
        playerCount: Int = 4,
        difficulty: GameDifficulty = GameDifficulty.MEDIUM,
        playerNames: List<String> = emptyList(),
        customRoomCode: String? = null
    ) {
        aiEngine = LudoAI(difficulty)
        val colors = listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE)

        val playerList = mutableListOf<Player>()
        for (i in 0 until playerCount) {
            val color = colors[i]
            val type = when (mode) {
                GameMode.VS_COMPUTER -> if (i == 0) PlayerType.HUMAN else PlayerType.AI
                GameMode.LOCAL_PASS_N_PLAY -> PlayerType.HUMAN
                GameMode.ONLINE_MULTIPLAYER -> if (i == 0) PlayerType.HUMAN else PlayerType.ONLINE_REMOTE
                GameMode.PRIVATE_ROOM -> if (i == 0) PlayerType.HUMAN else PlayerType.ONLINE_REMOTE
            }
            val defaultName = when (mode) {
                GameMode.VS_COMPUTER -> if (i == 0) "You (${color.title})" else "Bot ${color.title}"
                GameMode.LOCAL_PASS_N_PLAY -> "Player ${i + 1} (${color.title})"
                GameMode.ONLINE_MULTIPLAYER -> if (i == 0) uiState.userProfile.name else "Player_${Random.nextInt(100, 999)}"
                GameMode.PRIVATE_ROOM -> if (i == 0) uiState.userProfile.name else "Friend_${Random.nextInt(10, 99)}"
            }
            val name = playerNames.getOrNull(i)?.takeIf { it.isNotBlank() } ?: defaultName
            playerList.add(
                Player(
                    id = i,
                    name = name,
                    color = color,
                    type = type,
                    avatarId = i
                )
            )
        }

        val code = customRoomCode ?: "ABS-${Random.nextInt(1000, 9999)}"

        uiState = uiState.copy(
            players = playerList,
            activePlayerIndex = 0,
            diceValue = 1,
            isRolling = false,
            hasRolled = false,
            consecutiveSixes = 0,
            movableTokenIds = emptySet(),
            gameMode = mode,
            difficulty = difficulty,
            isGameOver = false,
            winner = null,
            statusMessage = "${playerList[0].name}'s turn! Roll the dice.",
            roomCode = code,
            showVictoryDialog = false
        )
    }

    fun rollDice(forcedValue: Int? = null) {
        val state = uiState
        if (state.isRolling || state.hasRolled || state.isGameOver) return

        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return

        uiState = state.copy(isRolling = true)
        SoundManager.playRollSound()

        viewModelScope.launch {
            // Animate rolling effect
            for (i in 0 until 5) {
                delay(70)
                uiState = uiState.copy(diceValue = Random.nextInt(1, 7))
            }

            val finalDice = forcedValue ?: Random.nextInt(1, 7)
            val newConsecutiveSixes = if (finalDice == 6) state.consecutiveSixes + 1 else 0

            // Check 3 consecutive sixes penalty rule
            if (newConsecutiveSixes == 3) {
                uiState = uiState.copy(
                    isRolling = false,
                    hasRolled = false,
                    diceValue = finalDice,
                    consecutiveSixes = 0,
                    statusMessage = "${activePlayer.name} rolled 3 sixes in a row! Turn forfeited."
                )
                delay(1200)
                passTurnToNextPlayer()
                return@launch
            }

            // Determine movable tokens for active player
            val movable = activePlayer.tokens.filter { canMoveToken(it, finalDice) }.map { it.id }.toSet()

            uiState = uiState.copy(
                isRolling = false,
                hasRolled = true,
                diceValue = finalDice,
                consecutiveSixes = newConsecutiveSixes,
                movableTokenIds = movable,
                statusMessage = when {
                    movable.isEmpty() -> "${activePlayer.name} rolled $finalDice. No moves available!"
                    movable.size == 1 && (activePlayer.type != PlayerType.HUMAN) -> "${activePlayer.name} moving token..."
                    else -> "${activePlayer.name} rolled $finalDice. Tap a token to move!"
                }
            )

            if (movable.isEmpty()) {
                delay(1000)
                passTurnToNextPlayer()
            } else if (activePlayer.type != PlayerType.HUMAN) {
                // AI or remote bot move
                delay(700)
                val chosenToken = aiEngine.chooseTokenToMove(activePlayer, finalDice, uiState.players)
                if (chosenToken != null) {
                    moveToken(chosenToken)
                } else {
                    passTurnToNextPlayer()
                }
            } else if (movable.size == 1 && activePlayer.tokens.count { it.isAtBase } == 4 && finalDice == 6) {
                // Auto-move single base token for player convenience
                delay(400)
                val token = activePlayer.tokens.firstOrNull { it.id == movable.first() }
                if (token != null) {
                    moveToken(token)
                }
            }
        }
    }

    fun moveToken(token: Token) {
        val state = uiState
        if (!state.hasRolled || state.isGameOver) return
        val activePlayer = state.players.getOrNull(state.activePlayerIndex) ?: return

        if (token.color != activePlayer.color) return
        if (!state.movableTokenIds.contains(token.id)) return

        viewModelScope.launch {
            val dice = state.diceValue
            val targetStep = if (token.isAtBase) 0 else token.step + dice

            SoundManager.playMoveSound()

            // Update token step
            val updatedTokens = activePlayer.tokens.map {
                if (it.id == token.id) it.copy(step = targetStep) else it
            }

            var extraTurn = dice == 6
            var captureOccurred = false
            var enteredHome = false

            // Check if entered Home finish
            if (targetStep == 56) {
                enteredHome = true
                extraTurn = true
                SoundManager.playVictorySound()
                uiState = uiState.copy(statusMessage = "🎉 ${activePlayer.name} reached Home! Extra turn granted!")
            } else if (targetStep <= 50) {
                // Main track - check capture
                val landingTrackIdx = (activePlayer.color.startIndex + targetStep) % 52
                if (!LudoBoardPath.SAFE_TRACK_INDICES.contains(landingTrackIdx)) {
                    // Check other players on this tile
                    val otherPlayers = state.players.filter { it.id != activePlayer.id }
                    var hasCaptured = false

                    val modifiedOtherPlayers = otherPlayers.map { other ->
                        val hasTokenOnTile = other.tokens.any {
                            LudoBoardPath.getTrackIndex(it) == landingTrackIdx
                        }
                        if (hasTokenOnTile) {
                            hasCaptured = true
                            other.copy(tokens = other.tokens.map {
                                if (LudoBoardPath.getTrackIndex(it) == landingTrackIdx) {
                                    it.copy(step = -1) // Send back to base!
                                } else it
                            })
                        } else other
                    }

                    if (hasCaptured) {
                        captureOccurred = true
                        extraTurn = true
                        SoundManager.playCaptureSound()
                        uiState = uiState.copy(
                            players = state.players.map { p ->
                                if (p.id == activePlayer.id) p.copy(tokens = updatedTokens)
                                else modifiedOtherPlayers.first { it.id == p.id }
                            },
                            statusMessage = "💥 ${activePlayer.name} captured an opponent token! Extra turn!"
                        )
                    }
                } else {
                    SoundManager.playSafeSound()
                }
            }

            val updatedPlayer = activePlayer.copy(tokens = updatedTokens)
            val updatedPlayers = if (captureOccurred) uiState.players.map {
                if (it.id == activePlayer.id) updatedPlayer else it
            } else state.players.map {
                if (it.id == activePlayer.id) updatedPlayer else it
            }

            // Check victory condition
            if (updatedPlayer.hasWon) {
                val winnerProfile = state.userProfile.copy(
                    wins = state.userProfile.wins + (if (updatedPlayer.type == PlayerType.HUMAN) 1 else 0),
                    coins = state.userProfile.coins + 500,
                    matches = state.userProfile.matches + 1
                )
                SoundManager.playVictorySound()
                uiState = uiState.copy(
                    players = updatedPlayers,
                    isGameOver = true,
                    winner = updatedPlayer,
                    showVictoryDialog = true,
                    userProfile = winnerProfile,
                    statusMessage = "👑 ${updatedPlayer.name} WON THE MATCH!"
                )
                return@launch
            }

            uiState = uiState.copy(
                players = updatedPlayers,
                movableTokenIds = emptySet()
            )

            if (extraTurn) {
                delay(800)
                uiState = uiState.copy(
                    hasRolled = false,
                    statusMessage = "${activePlayer.name} gets a BONUS ROLL!"
                )
                if (activePlayer.type != PlayerType.HUMAN) {
                    delay(500)
                    rollDice()
                }
            } else {
                delay(400)
                passTurnToNextPlayer()
            }
        }
    }

    private fun passTurnToNextPlayer() {
        val state = uiState
        if (state.isGameOver) return

        val nextIndex = (state.activePlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]

        uiState = state.copy(
            activePlayerIndex = nextIndex,
            hasRolled = false,
            movableTokenIds = emptySet(),
            consecutiveSixes = 0,
            statusMessage = "${nextPlayer.name}'s turn! Roll the dice."
        )

        if (nextPlayer.type != PlayerType.HUMAN) {
            viewModelScope.launch {
                delay(700)
                rollDice()
            }
        }
    }

    private fun canMoveToken(token: Token, dice: Int): Boolean {
        if (token.isFinished) return false
        if (token.isAtBase) return dice == 6
        return token.step + dice <= 56
    }

    fun rerollWithGuaranteedSix() {
        // Rewarded ad trigger: Watch sponsor ad to reroll and guarantee a 6!
        AdManager.showRewardedAd(AdTriggerType.DICE_REROLL) {
            uiState = uiState.copy(hasRolled = false)
            rollDice(forcedValue = 6)
        }
    }

    fun claimFreeCoins(amount: Int = 500) {
        AdManager.showRewardedAd(AdTriggerType.FREE_COINS) {
            SoundManager.playCoinSound()
            uiState = uiState.copy(
                userProfile = uiState.userProfile.copy(
                    coins = uiState.userProfile.coins + amount
                )
            )
        }
    }

    fun sendEmoji(emoji: String) {
        val activePlayer = uiState.players.getOrNull(0)?.name ?: "You"
        val newMsg = ChatMessage(senderName = activePlayer, emoji = emoji)
        uiState = uiState.copy(chatMessages = (uiState.chatMessages + newMsg).takeLast(6))

        // Trigger bot counter reaction randomly
        if (Random.nextBoolean() && uiState.players.size > 1) {
            viewModelScope.launch {
                delay(1200)
                val bot = uiState.players.filter { it.type != PlayerType.HUMAN }.randomOrNull()
                if (bot != null) {
                    val botEmojis = listOf("🔥", "👏", "😎", "😂", "🎲", "👑")
                    val botMsg = ChatMessage(senderName = bot.name, emoji = botEmojis.random())
                    uiState = uiState.copy(chatMessages = (uiState.chatMessages + botMsg).takeLast(6))
                }
            }
        }
    }

    fun setTheme(newTheme: BoardTheme) {
        uiState = uiState.copy(theme = newTheme)
    }

    fun dismissVictoryDialog() {
        uiState = uiState.copy(showVictoryDialog = false)
    }
}
