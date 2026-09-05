package com.example.ads

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AdTriggerType(val title: String, val rewardCoins: Int) {
    FREE_COINS("Bonus 500 Coins", 500),
    DAILY_SPIN("Extra Lucky Spin", 250),
    DICE_REROLL("Reroll Dice (Roll a 6)", 0),
    PRE_GAME_BOOST("Match XP Booster", 100),
    VICTORY_DOUBLE("Double Match Rewards", 1000),
    BANNER_SPONSOR("Special Sponsor Offer", 50)
}

data class ActiveAdState(
    val isShowing: Boolean = false,
    val triggerType: AdTriggerType = AdTriggerType.FREE_COINS,
    val targetUrl: String = AdManager.AD_URL_PROFITABLE_CPM,
    val durationSeconds: Int = 5,
    val onRewardGranted: (() -> Unit)? = null
)

object AdManager {
    // 3 Official Ad Network URLs provided in user specification
    const val AD_URL_PROFITABLE_CPM = "https://www.profitableratecpmnetwork.com/ucxmae18v?key=43af0592915933469f38021c023791fe"
    const val AD_URL_OMG10 = "https://omg10.com/4/11705543"
    const val AD_URL_DATA527 = "https://data527.click/0379c1f28ec96f3eb6ba/c3f146d3ff/?placementName=default"

    val ALL_AD_URLS = listOf(
        AD_URL_PROFITABLE_CPM,
        AD_URL_OMG10,
        AD_URL_DATA527
    )

    private var urlIndex = 0

    var activeAd by mutableStateOf<ActiveAdState?>(null)
        private set

    fun getNextAdUrl(): String {
        val url = ALL_AD_URLS[urlIndex % ALL_AD_URLS.size]
        urlIndex++
        return url
    }

    fun openAdDirectly(context: Context, url: String = getNextAdUrl()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    fun showRewardedAd(
        trigger: AdTriggerType,
        customUrl: String? = null,
        onRewarded: () -> Unit
    ) {
        val targetUrl = customUrl ?: when (trigger) {
            AdTriggerType.FREE_COINS, AdTriggerType.DAILY_SPIN -> AD_URL_PROFITABLE_CPM
            AdTriggerType.DICE_REROLL, AdTriggerType.PRE_GAME_BOOST -> AD_URL_OMG10
            AdTriggerType.VICTORY_DOUBLE, AdTriggerType.BANNER_SPONSOR -> AD_URL_DATA527
        }

        activeAd = ActiveAdState(
            isShowing = true,
            triggerType = trigger,
            targetUrl = targetUrl,
            durationSeconds = 5,
            onRewardGranted = onRewarded
        )
    }

    fun dismissActiveAd(rewardEarned: Boolean = true) {
        val current = activeAd
        if (rewardEarned) {
            current?.onRewardGranted?.invoke()
        }
        activeAd = null
    }
}
