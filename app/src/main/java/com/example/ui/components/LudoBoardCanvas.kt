package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.BoardCoord
import com.example.logic.LudoBoardPath
import com.example.model.*

@Composable
fun LudoBoardCanvas(
    players: List<Player>,
    activePlayerColor: LudoColor,
    movableTokenIds: Set<Int>,
    theme: BoardTheme,
    onTokenClick: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .testTag("ludo_board")
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(theme.boardBg)
            .border(3.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
    ) {
        val boardSizePx = constraints.maxWidth.toFloat()
        val cellSize = boardSizePx / 15f
        val localDensity = LocalDensity.current

        // 1. Draw static board geometry (bases, tracks, stars, home triangles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLudoBoardBackground(cellSize, theme)
        }

        // 2. Draw Interactive Pawn Tokens
        // Collect all tokens and group by coordinate to render nicely
        val allTokens = players.flatMap { it.tokens }
        val activePlayer = players.find { it.color == activePlayerColor }

        // Token animation for movable tokens
        val infiniteTransition = rememberInfiniteTransition(label = "tokenPulse")
        val bounceOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        // Group tokens on the same spot to stack/offset them
        val tokensByCoord = allTokens.groupBy { LudoBoardPath.getTokenCoordinate(it) }

        tokensByCoord.forEach { (coord, tokensOnSpot) ->
            tokensOnSpot.forEachIndexed { index, token ->
                val isMovable = activePlayer?.color == token.color && movableTokenIds.contains(token.id)

                // Sub-pixel stacking offset if multiple tokens share a spot
                val offsetX = when {
                    tokensOnSpot.size == 1 -> 0f
                    index == 0 -> -cellSize * 0.15f
                    index == 1 -> cellSize * 0.15f
                    index == 2 -> -cellSize * 0.15f
                    else -> cellSize * 0.15f
                }
                val offsetY = when {
                    tokensOnSpot.size == 1 -> 0f
                    index <= 1 -> -cellSize * 0.15f
                    else -> cellSize * 0.15f
                }

                val xPosDp = with(localDensity) { (coord.col * cellSize + offsetX).toDp() }
                val yPosDp = with(localDensity) { (coord.row * cellSize + offsetY + if (isMovable) bounceOffset else 0f).toDp() }
                val tokenSizeDp = with(localDensity) { (cellSize * 0.88f).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = xPosDp, y = yPosDp)
                        .size(tokenSizeDp)
                        .scale(if (isMovable) glowScale else 1f)
                        .testTag("token_${token.color.name}_${token.id}")
                        .clickable(
                            enabled = isMovable,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTokenClick(token) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    TokenPawn(
                        color = token.color,
                        isMovable = isMovable,
                        number = token.id + 1
                    )
                }
            }
        }
    }
}

@Composable
fun TokenPawn(
    color: LudoColor,
    isMovable: Boolean,
    number: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Outer pulsing ring for movable token
        if (isMovable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }

        // 3D Styled Token Body
        Box(
            modifier = Modifier
                .fillMaxSize(0.86f)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.lightColor,
                            color.primaryColor,
                            color.darkColor
                        )
                    )
                )
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner token crown ring
            Box(
                modifier = Modifier
                    .fillMaxSize(0.55f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.88f))
                    .border(1.dp, color.darkColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    color = color.darkColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun DrawScope.drawLudoBoardBackground(cellSize: Float, theme: BoardTheme) {
    val trackBorderColor = theme.gridLine

    // 1. Four corner yards (6x6 each)
    val yardSize = cellSize * 6f
    // Green (Top-Left)
    drawRect(LudoColor.GREEN.primaryColor, Offset(0f, 0f), Size(yardSize, yardSize))
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cellSize * 1f, cellSize * 1f),
        size = Size(cellSize * 4f, cellSize * 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Yellow (Top-Right)
    drawRect(LudoColor.YELLOW.primaryColor, Offset(cellSize * 9f, 0f), Size(yardSize, yardSize))
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cellSize * 10f, cellSize * 1f),
        size = Size(cellSize * 4f, cellSize * 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Red (Bottom-Left)
    drawRect(LudoColor.RED.primaryColor, Offset(0f, cellSize * 9f), Size(yardSize, yardSize))
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cellSize * 1f, cellSize * 10f),
        size = Size(cellSize * 4f, cellSize * 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Blue (Bottom-Right)
    drawRect(LudoColor.BLUE.primaryColor, Offset(cellSize * 9f, cellSize * 9f), Size(yardSize, yardSize))
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cellSize * 10f, cellSize * 10f),
        size = Size(cellSize * 4f, cellSize * 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // Base pocket circle pockets
    drawPockets(LudoBoardPath.BASE_POCKETS[LudoColor.GREEN]!!, cellSize, LudoColor.GREEN.primaryColor)
    drawPockets(LudoBoardPath.BASE_POCKETS[LudoColor.YELLOW]!!, cellSize, LudoColor.YELLOW.primaryColor)
    drawPockets(LudoBoardPath.BASE_POCKETS[LudoColor.RED]!!, cellSize, LudoColor.RED.primaryColor)
    drawPockets(LudoBoardPath.BASE_POCKETS[LudoColor.BLUE]!!, cellSize, LudoColor.BLUE.primaryColor)

    // 2. Draw all 52 main track squares
    for (i in 0 until 52) {
        val coord = LudoBoardPath.TRACK_COORDINATES[i]
        val x = coord.col * cellSize
        val y = coord.row * cellSize

        // Starting tiles have player color
        val bgColor = when (i) {
            0 -> LudoColor.RED.primaryColor
            13 -> LudoColor.GREEN.primaryColor
            26 -> LudoColor.YELLOW.primaryColor
            39 -> LudoColor.BLUE.primaryColor
            else -> theme.boardBg
        }

        drawRect(bgColor, Offset(x, y), Size(cellSize, cellSize))
        drawRect(trackBorderColor, Offset(x, y), Size(cellSize, cellSize), style = Stroke(width = 1.2f))

        // Draw Safe Star on safe cells
        if (LudoBoardPath.SAFE_TRACK_INDICES.contains(i)) {
            drawStarIcon(x + cellSize / 2f, y + cellSize / 2f, cellSize * 0.32f)
        }
    }

    // 3. Colored Home stretch tracks (5 tiles each)
    // Red home column (col 7, rows 9..13)
    for (r in 9..13) {
        drawRect(LudoColor.RED.primaryColor, Offset(7f * cellSize, r * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.3f), Offset(7f * cellSize, r * cellSize), Size(cellSize, cellSize), style = Stroke(1.2f))
    }
    // Green home row (row 7, cols 1..5)
    for (c in 1..5) {
        drawRect(LudoColor.GREEN.primaryColor, Offset(c * cellSize, 7f * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.3f), Offset(c * cellSize, 7f * cellSize), Size(cellSize, cellSize), style = Stroke(1.2f))
    }
    // Yellow home column (col 7, rows 1..5)
    for (r in 1..5) {
        drawRect(LudoColor.YELLOW.primaryColor, Offset(7f * cellSize, r * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.3f), Offset(7f * cellSize, r * cellSize), Size(cellSize, cellSize), style = Stroke(1.2f))
    }
    // Blue home row (row 7, cols 9..13)
    for (c in 9..13) {
        drawRect(LudoColor.BLUE.primaryColor, Offset(c * cellSize, 7f * cellSize), Size(cellSize, cellSize))
        drawRect(Color.White.copy(alpha = 0.3f), Offset(c * cellSize, 7f * cellSize), Size(cellSize, cellSize), style = Stroke(1.2f))
    }

    // 4. Center Home destination triangles (rows 6..8, cols 6..8)
    val centerLeft = 6f * cellSize
    val centerTop = 6f * cellSize
    val centerRight = 9f * cellSize
    val centerBottom = 9f * cellSize
    val centerMidX = 7.5f * cellSize
    val centerMidY = 7.5f * cellSize

    // Center Red triangle (bottom)
    val redPath = Path().apply {
        moveTo(centerLeft, centerBottom)
        lineTo(centerRight, centerBottom)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(redPath, LudoColor.RED.primaryColor)

    // Center Green triangle (left)
    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerLeft, centerBottom)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(greenPath, LudoColor.GREEN.primaryColor)

    // Center Yellow triangle (top)
    val yellowPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerRight, centerTop)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(yellowPath, LudoColor.YELLOW.primaryColor)

    // Center Blue triangle (right)
    val bluePath = Path().apply {
        moveTo(centerRight, centerTop)
        lineTo(centerRight, centerBottom)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(bluePath, LudoColor.BLUE.primaryColor)

    // Center border
    drawRect(trackBorderColor, Offset(centerLeft, centerTop), Size(cellSize * 3f, cellSize * 3f), style = Stroke(2f))

    // Center Golden Crown Circle
    drawCircle(
        color = Color(0xFFFFD700),
        radius = cellSize * 0.55f,
        center = Offset(centerMidX, centerMidY)
    )
    drawCircle(
        color = Color(0xFF4A148C),
        radius = cellSize * 0.42f,
        center = Offset(centerMidX, centerMidY)
    )
}

private fun DrawScope.drawPockets(coords: List<BoardCoord>, cellSize: Float, color: Color) {
    coords.forEach { coord ->
        val x = coord.col * cellSize + (cellSize * 0.44f)
        val y = coord.row * cellSize + (cellSize * 0.44f)
        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = cellSize * 0.45f,
            center = Offset(x, y)
        )
        drawCircle(
            color = color,
            radius = cellSize * 0.36f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawStarIcon(cx: Float, cy: Float, radius: Float) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.42f

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = Math.toRadians((i * 36 - 90).toDouble())
        val x = (cx + r * kotlin.math.cos(angle)).toFloat()
        val y = (cy + r * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path, Color(0xFFFFC107))
    drawPath(path, Color(0xFFF57F17), style = Stroke(1.5f))
}
