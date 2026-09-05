package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ads.ActiveAdState
import com.example.ads.AdManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppAdOverlay(
    adState: ActiveAdState,
    onDismiss: (rewardGranted: Boolean) -> Unit
) {
    val context = LocalContext.current
    var secondsRemaining by remember { mutableIntStateOf(adState.durationSeconds) }
    var isRewardUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        isRewardUnlocked = true
    }

    Dialog(
        onDismissRequest = {
            if (isRewardUnlocked) onDismiss(true)
        },
        properties = DialogProperties(
            dismissOnBackPress = isRewardUnlocked,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60B0F19))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("ad_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Ad Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = Color(0xFFFFB703),
                                contentColor = Color.Black
                            ) {
                                Text("SPONSORED", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = adState.triggerType.title,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        // Countdown or Close Button
                        if (isRewardUnlocked) {
                            IconButton(
                                onClick = { onDismiss(true) },
                                modifier = Modifier
                                    .testTag("ad_close_button")
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Claim Reward",
                                    tint = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$secondsRemaining",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // WebView content or sponsored banner
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val url = request?.url?.toString() ?: ""
                                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                                view?.loadUrl(url)
                                            }
                                            return false
                                        }
                                    }
                                    loadUrl(adState.targetUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Footer CTA Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                AdManager.openAdDirectly(context, adState.targetUrl)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("ad_visit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "VISIT SPONSOR OFFER",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isRewardUnlocked) {
                            TextButton(
                                onClick = { onDismiss(true) },
                                modifier = Modifier.testTag("ad_claim_button")
                            ) {
                                Text(
                                    "✨ REWARD UNLOCKED • CLAIM & RETURN",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Text(
                                "Reward unlocks in $secondsRemaining seconds...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SponsorBannerBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(AdManager.AD_URL_PROFITABLE_CPM) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(2.dp)
            .background(VibrantSurface)
            .border(1.dp, VibrantSlate100)
            .clickable {
                AdManager.openAdDirectly(context, currentUrl)
                currentUrl = AdManager.getNextAdUrl()
            }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(VibrantYellow400),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdsClick,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "ABS Sponsor Offer",
                        color = VibrantSlate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(VibrantIndigo50, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("AD", color = VibrantIndigo600, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text(
                    "Play top games & claim special free rewards!",
                    color = VibrantSlate500,
                    fontSize = 10.sp
                )
            }
        }

        Button(
            onClick = {
                AdManager.openAdDirectly(context, currentUrl)
                currentUrl = AdManager.getNextAdUrl()
            },
            colors = ButtonDefaults.buttonColors(containerColor = VibrantYellow400),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(34.dp).testTag("banner_ad_button")
        ) {
            Text("OPEN", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
        }
    }
}
