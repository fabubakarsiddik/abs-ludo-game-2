package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor

@Composable
fun Dice3DView(
    value: Int,
    isRolling: Boolean,
    isTurn: Boolean,
    color: LudoColor,
    size: Dp = 68.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "diceGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val rollRotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = if (isRolling) {
            infiniteRepeatable(
                animation = tween(220, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "rollRotation"
    )

    val rollScale by animateFloatAsState(
        targetValue = if (isRolling) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rollScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("dice_button")
            .size(size + 16.dp)
            .scale(if (isTurn && !isRolling) glowScale else 1f)
            .clickable(
                enabled = isTurn && !isRolling,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Outer glow halo if it's active player's turn
        if (isTurn) {
            Box(
                modifier = Modifier
                    .size(size + 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                color.primaryColor.copy(alpha = 0.55f),
                                color.primaryColor.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }

        // 3D Dice Body
        Box(
            modifier = Modifier
                .size(size)
                .rotate(rollRotation)
                .scale(rollScale)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F5F9),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.primaryColor,
                            color.darkColor
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            DiceDots(value = value, dotColor = color.darkColor)
        }
    }
}

@Composable
fun DiceDots(value: Int, dotColor: Color) {
    val dotSize = 10.dp

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Row 1 (Top)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Dot(visible = value in listOf(2, 3, 4, 5, 6), color = dotColor, size = dotSize)
            Dot(visible = false, color = dotColor, size = dotSize)
            Dot(visible = value in listOf(4, 5, 6), color = dotColor, size = dotSize)
        }

        // Row 2 (Center)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Dot(visible = value == 6, color = dotColor, size = dotSize)
            Dot(visible = value in listOf(1, 3, 5), color = if (value == 1) Color(0xFFD32F2F) else dotColor, size = dotSize)
            Dot(visible = value == 6, color = dotColor, size = dotSize)
        }

        // Row 3 (Bottom)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Dot(visible = value in listOf(4, 5, 6), color = dotColor, size = dotSize)
            Dot(visible = false, color = dotColor, size = dotSize)
            Dot(visible = value in listOf(2, 3, 4, 5, 6), color = dotColor, size = dotSize)
        }
    }
}

@Composable
private fun Dot(visible: Boolean, color: Color, size: Dp) {
    if (visible) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.85f))
                    )
                )
                .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
        )
    } else {
        Spacer(modifier = Modifier.size(size))
    }
}
