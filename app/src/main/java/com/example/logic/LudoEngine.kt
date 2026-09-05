package com.example.logic

import com.example.model.*
import kotlin.random.Random

data class BoardCoord(val row: Float, val col: Float)

object LudoBoardPath {
    val SAFE_TRACK_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Standard 52 outer track tile coordinates on 15x15 board
    val TRACK_COORDINATES = listOf(
        BoardCoord(13f, 6f),  // 0: Red start
        BoardCoord(12f, 6f),  // 1
        BoardCoord(11f, 6f),  // 2
        BoardCoord(10f, 6f),  // 3
        BoardCoord(9f, 6f),   // 4
        BoardCoord(8f, 5f),   // 5
        BoardCoord(8f, 4f),   // 6
        BoardCoord(8f, 3f),   // 7
        BoardCoord(8f, 2f),   // 8: Safe star
        BoardCoord(8f, 1f),   // 9
        BoardCoord(8f, 0f),   // 10
        BoardCoord(7f, 0f),   // 11: Green turn
        BoardCoord(6f, 0f),   // 12
        BoardCoord(6f, 1f),   // 13: Green start
        BoardCoord(6f, 2f),   // 14
        BoardCoord(6f, 3f),   // 15
        BoardCoord(6f, 4f),   // 16
        BoardCoord(6f, 5f),   // 17
        BoardCoord(5f, 6f),   // 18
        BoardCoord(4f, 6f),   // 19
        BoardCoord(3f, 6f),   // 20
        BoardCoord(2f, 6f),   // 21: Safe star
        BoardCoord(1f, 6f),   // 22
        BoardCoord(0f, 6f),   // 23
        BoardCoord(0f, 7f),   // 24: Yellow turn
        BoardCoord(0f, 8f),   // 25
        BoardCoord(1f, 8f),   // 26: Yellow start
        BoardCoord(2f, 8f),   // 27
        BoardCoord(3f, 8f),   // 28
        BoardCoord(4f, 8f),   // 29
        BoardCoord(5f, 8f),   // 30
        BoardCoord(6f, 9f),   // 31
        BoardCoord(6f, 10f),  // 32
        BoardCoord(6f, 11f),  // 33
        BoardCoord(6f, 12f),  // 34: Safe star
        BoardCoord(6f, 13f),  // 35
        BoardCoord(6f, 14f),  // 36
        BoardCoord(7f, 14f),  // 37: Blue turn
        BoardCoord(8f, 14f),  // 38
        BoardCoord(8f, 13f),  // 39: Blue start
        BoardCoord(8f, 12f),  // 40
        BoardCoord(8f, 11f),  // 41
        BoardCoord(8f, 10f),  // 42
        BoardCoord(8f, 9f),   // 43
        BoardCoord(9f, 8f),   // 44
        BoardCoord(10f, 8f),  // 45
        BoardCoord(11f, 8f),  // 46
        BoardCoord(12f, 8f),  // 47: Safe star
        BoardCoord(13f, 8f),  // 48
        BoardCoord(14f, 8f),  // 49
        BoardCoord(14f, 7f),  // 50: Red turn
        BoardCoord(14f, 6f)   // 51
    )

    // Base pocket locations for 4 tokens
    val BASE_POCKETS = mapOf(
        LudoColor.GREEN to listOf(
            BoardCoord(1.8f, 1.8f), BoardCoord(1.8f, 3.8f),
            BoardCoord(3.8f, 1.8f), BoardCoord(3.8f, 3.8f)
        ),
        LudoColor.YELLOW to listOf(
            BoardCoord(1.8f, 10.8f), BoardCoord(1.8f, 12.8f),
            BoardCoord(3.8f, 10.8f), BoardCoord(3.8f, 12.8f)
        ),
        LudoColor.RED to listOf(
            BoardCoord(10.8f, 1.8f), BoardCoord(10.8f, 3.8f),
            BoardCoord(12.8f, 1.8f), BoardCoord(12.8f, 3.8f)
        ),
        LudoColor.BLUE to listOf(
            BoardCoord(10.8f, 10.8f), BoardCoord(10.8f, 12.8f),
            BoardCoord(12.8f, 10.8f), BoardCoord(12.8f, 12.8f)
        )
    )

    // 5-tile home track for each color leading to center (step 51 to 55)
    val HOME_COLUMNS = mapOf(
        LudoColor.RED to listOf(
            BoardCoord(13f, 7f), BoardCoord(12f, 7f),
            BoardCoord(11f, 7f), BoardCoord(10f, 7f), BoardCoord(9f, 7f)
        ),
        LudoColor.GREEN to listOf(
            BoardCoord(7f, 1f), BoardCoord(7f, 2f),
            BoardCoord(7f, 3f), BoardCoord(7f, 4f), BoardCoord(7f, 5f)
        ),
        LudoColor.YELLOW to listOf(
            BoardCoord(1f, 7f), BoardCoord(2f, 7f),
            BoardCoord(3f, 7f), BoardCoord(4f, 7f), BoardCoord(5f, 7f)
        ),
        LudoColor.BLUE to listOf(
            BoardCoord(7f, 13f), BoardCoord(7f, 12f),
            BoardCoord(7f, 11f), BoardCoord(7f, 10f), BoardCoord(7f, 9f)
        )
    )

    val CENTER_HOME = BoardCoord(7f, 7f)

    fun getTokenCoordinate(token: Token): BoardCoord {
        if (token.isAtBase) {
            val pockets = BASE_POCKETS[token.color] ?: return CENTER_HOME
            return pockets.getOrElse(token.id) { CENTER_HOME }
        }
        if (token.isFinished) {
            return when (token.color) {
                LudoColor.RED -> BoardCoord(7.5f, 7f)
                LudoColor.GREEN -> BoardCoord(7f, 6.5f)
                LudoColor.YELLOW -> BoardCoord(6.5f, 7f)
                LudoColor.BLUE -> BoardCoord(7f, 7.5f)
            }
        }
        if (token.isInHomeTrack) {
            val homeCol = HOME_COLUMNS[token.color] ?: return CENTER_HOME
            val homeStep = token.step - 51
            return homeCol.getOrElse(homeStep) { CENTER_HOME }
        }
        // Main track
        val trackIdx = (token.color.startIndex + token.step) % 52
        return TRACK_COORDINATES[trackIdx]
    }

    fun getTrackIndex(token: Token): Int? {
        if (token.isAtBase || token.isInHomeTrack || token.isFinished) return null
        return (token.color.startIndex + token.step) % 52
    }
}

class LudoAI(private val difficulty: GameDifficulty) {
    fun chooseTokenToMove(
        player: Player,
        dice: Int,
        allPlayers: List<Player>
    ): Token? {
        val movable = player.tokens.filter { canMoveToken(it, dice) }
        if (movable.isEmpty()) return null
        if (movable.size == 1) return movable.first()

        // Easy: Random choice
        if (difficulty == GameDifficulty.EASY) {
            return movable.random()
        }

        // Medium & Hard: Smart strategic evaluation
        var bestToken: Token? = null
        var highestScore = -1000

        for (token in movable) {
            var score = 0
            val futureStep = if (token.isAtBase) 0 else token.step + dice

            // 1. Prioritize finishing a token (entering home)
            if (futureStep == 56) {
                score += 500
            }

            // 2. Entering safe home track
            if (futureStep in 51..55 && token.step <= 50) {
                score += 250
            }

            // 3. Leaving base on 6
            if (token.isAtBase && dice == 6) {
                score += if (difficulty == GameDifficulty.HARD) 200 else 150
            }

            // 4. Capturing opponent token
            if (futureStep <= 50) {
                val futureTrackIdx = (player.color.startIndex + futureStep) % 52
                if (!LudoBoardPath.SAFE_TRACK_INDICES.contains(futureTrackIdx)) {
                    val opponentTokens = allPlayers.filter { it.color != player.color }
                        .flatMap { it.tokens }
                        .filter { LudoBoardPath.getTrackIndex(it) == futureTrackIdx }

                    if (opponentTokens.isNotEmpty()) {
                        score += if (difficulty == GameDifficulty.HARD) 400 else 250
                    }
                }
            }

            // 5. Landing on a safe star
            if (futureStep <= 50) {
                val futureTrackIdx = (player.color.startIndex + futureStep) % 52
                if (LudoBoardPath.SAFE_TRACK_INDICES.contains(futureTrackIdx)) {
                    score += 180
                }
            }

            // 6. Hard AI: Avoid moving into dangerous zones if opponent is 1..6 steps behind
            if (difficulty == GameDifficulty.HARD && futureStep <= 50) {
                val futureTrackIdx = (player.color.startIndex + futureStep) % 52
                if (!LudoBoardPath.SAFE_TRACK_INDICES.contains(futureTrackIdx)) {
                    val threatened = allPlayers.filter { it.color != player.color }
                        .flatMap { it.tokens }
                        .any { opponent ->
                            val oppIdx = LudoBoardPath.getTrackIndex(opponent)
                            if (oppIdx != null) {
                                val dist = (futureTrackIdx - oppIdx + 52) % 52
                                dist in 1..6
                            } else false
                        }
                    if (threatened) {
                        score -= 80
                    }
                }
            }

            // 7. General forward progress
            score += futureStep

            if (score > highestScore) {
                highestScore = score
                bestToken = token
            }
        }

        return bestToken ?: movable.first()
    }

    private fun canMoveToken(token: Token, dice: Int): Boolean {
        if (token.isFinished) return false
        if (token.isAtBase) return dice == 6
        return token.step + dice <= 56
    }
}
