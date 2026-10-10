package org.example.tilt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class SlotSymbol(val label: String, val weight: Int) {
    CHERRY("🍒", 40),
    LEMON("🍋", 30),
    BAR("🍫", 20),
    SEVEN("7️⃣", 10)
}

data class Reel(
    val id: Int,
    var currentSymbol: SlotSymbol = SlotSymbol.CHERRY,
    var isFrozen: Boolean = false,
    var isExpanded: Boolean = false
)

enum class CoinRarity { COMMON, UNCOMMON, RARE }

data class CoinItem(
    val id: String = Random.nextInt(1000000).toString(),
    val name: String,
    val description: String,
    val rarity: CoinRarity,
    val cardColorHex: Long, // Color tint for individual cards
    val price: Int = 20,
    val action: (engine: SlotGameEngine) -> Unit
)

enum class GamePhase {
    MAIN_MENU,
    SETTINGS,
    PLAYING,
    ROUND_SUCCESS,
    SHOP,
    GAME_OVER
}

class SlotGameEngine {
    var currentPhase by mutableStateOf(GamePhase.MAIN_MENU)
        private set

    var soundEnabled by mutableStateOf(true)
    var hapticsEnabled by mutableStateOf(true)
    var fastSpinMode by mutableStateOf(false)

    var roundNumber by mutableStateOf(1)
        private set
    var targetBankroll by mutableStateOf(60)
        private set
    var remainingSpins by mutableStateOf(12)
        private set

    var bankroll by mutableStateOf(100)
        private set
    var currentBet by mutableStateOf(5)
        private set

    var roundStartBankroll by mutableStateOf(100)
        private set
    var roundSpinsUsed by mutableStateOf(0)
        private set

    // Win Feedback & Breakdown State
    var lastWinAmount by mutableStateOf(0)
        private set
    var winReasonText by mutableStateOf("")
        private set
    var showWinAnimation by mutableStateOf(false)
        private set

    val isBankAuditRound: Boolean
        get() = roundNumber % 5 == 0

    val reels = mutableStateListOf(
        Reel(id = 1),
        Reel(id = 2),
        Reel(id = 3)
    )

    val inventoryCoins = mutableStateListOf<CoinItem>()
    var equippedCoin by mutableStateOf<CoinItem?>(null)
        private set

    // Shop Randomized Inventory Stock
    val shopCoinStock = mutableStateListOf<CoinItem>()

    var isSpinning by mutableStateOf(false)
        private set
    var coinUsedThisSpin by mutableStateOf(false)
        private set

    var isExpansionShaking by mutableStateOf(false)
        private set
    var isBarTakeoverActive by mutableStateOf(false)
        private set

    var isSelectingFreezeTarget by mutableStateOf(false)
        private set
    var selectedReelForSymbolPicker by mutableStateOf<Int?>(null)
        private set
    var activeMultiplierBonus by mutableStateOf(1.0)
        private set

    var totalHighRollerBets by mutableStateOf(0)
    var totalCoinsUsed by mutableStateOf(0)

    var isOpeningPack by mutableStateOf(false)
        private set
    val revealedPackCoins = mutableStateListOf<CoinItem>()

    fun navigateTo(phase: GamePhase) {
        currentPhase = phase
    }

    fun startNewRun() {
        restartGame()
        currentPhase = GamePhase.PLAYING
    }

    fun setBet(amount: Int) {
        if (!isSpinning && !isSelectingFreezeTarget && selectedReelForSymbolPicker == null) {
            currentBet = amount
            if (amount == 25) totalHighRollerBets++
        }
    }

    fun equipCoin(coin: CoinItem) {
        equippedCoin = coin
    }

    private fun getRandomSymbolWeighted(): SlotSymbol {
        val totalWeight = SlotSymbol.values().sumOf { it.weight }
        val randomValue = Random.nextInt(totalWeight)
        var currentWeightSum = 0

        for (symbol in SlotSymbol.values()) {
            currentWeightSum += symbol.weight
            if (randomValue < currentWeightSum) {
                return symbol
            }
        }
        return SlotSymbol.CHERRY
    }

    suspend fun spin() {
        if (isSpinning || remainingSpins <= 0 || currentPhase != GamePhase.PLAYING || bankroll < currentBet) return

        showWinAnimation = false
        cancelFreezeSelection()
        bankroll -= currentBet
        remainingSpins -= 1
        roundSpinsUsed += 1
        isSpinning = true

        val spinTicks = if (fastSpinMode) 6 else 14
        val tickDelay = if (fastSpinMode) 50L else 75L
        val allSymbols = SlotSymbol.values()

        repeat(spinTicks) {
            reels.forEachIndexed { index, _ ->
                val current = reels[index]
                if (!current.isFrozen && !current.isExpanded) {
                    val symbol = if (isBarTakeoverActive) SlotSymbol.BAR else allSymbols.random()
                    reels[index] = current.copy(currentSymbol = symbol)
                }
            }
            delay(tickDelay)
        }

        reels.forEachIndexed { index, _ ->
            val current = reels[index]
            if (!current.isFrozen && !current.isExpanded) {
                val symbol = if (isBarTakeoverActive) SlotSymbol.BAR else getRandomSymbolWeighted()
                reels[index] = current.copy(currentSymbol = symbol)
            }
        }

        calculatePayout()

        // Pause to show win breakdown banner before resolving
        if (showWinAnimation) {
            delay(900)
        }

        resetTurnState()
        isSpinning = false

        checkRoundEndConditions()
    }

    private fun resetTurnState() {
        unfreezeAllReels()
        removeExpandedReels()
        coinUsedThisSpin = false
        isExpansionShaking = false
        isBarTakeoverActive = false
        activeMultiplierBonus = 1.0
    }

    fun useEquippedCoin() {
        val coin = equippedCoin ?: return
        if (coinUsedThisSpin || isSpinning) return

        totalCoinsUsed++
        coinUsedThisSpin = true
        coin.action(this)

        inventoryCoins.remove(coin)
        equippedCoin = inventoryCoins.firstOrNull()
    }

    // --- SHOP ACTIONS ---
    fun refreshShopStock() {
        shopCoinStock.clear()
        val numItems = Random.nextInt(2, 4)
        repeat(numItems) {
            shopCoinStock.add(generateRandomCoin())
        }
    }

    fun buyIndividualCoin(coin: CoinItem) {
        if (bankroll >= coin.price) {
            bankroll -= coin.price
            inventoryCoins.add(coin)
            if (equippedCoin == null) {
                equippedCoin = coin
            }
            shopCoinStock.remove(coin)
        }
    }

    suspend fun buyAndOpenCoinPack() {
        val cost = 25
        if (bankroll < cost || isOpeningPack) return

        bankroll -= cost
        isOpeningPack = true
        revealedPackCoins.clear()

        delay(1200)

        val packSize = Random.nextInt(1, 4)
        repeat(packSize) {
            revealedPackCoins.add(generateRandomCoin())
        }

        inventoryCoins.addAll(revealedPackCoins)
        if (equippedCoin == null) {
            equippedCoin = inventoryCoins.firstOrNull()
        }

        delay(800)
        isOpeningPack = false
    }

    fun expandFlankingReels(symbol: SlotSymbol) {
        val maxId = (reels.maxOfOrNull { it.id } ?: 0)
        val leftReel = Reel(id = maxId + 1, currentSymbol = symbol, isExpanded = true)
        val rightReel = Reel(id = maxId + 2, currentSymbol = symbol, isExpanded = true)

        reels.add(0, leftReel)
        reels.add(rightReel)
    }

    private fun removeExpandedReels() {
        reels.removeAll { it.isExpanded }
    }

    private fun generateRandomCoin(): CoinItem {
        val roll = Random.nextInt(100)
        val rareThreshold = if (totalCoinsUsed > 8) 70 else 85

        return when {
            roll < 55 -> {
                val mult = listOf(2, 4, 6).random()
                val colorHex = when (mult) {
                    2 -> 0xFFFF5555 // Red
                    4 -> 0xFFFFB86C // Orange
                    else -> 0xFFFF79C6 // Pink
                }
                CoinItem(
                    name = "${mult}x Multiplier",
                    description = "Boosts current spin payout by ${mult}x",
                    rarity = CoinRarity.COMMON,
                    cardColorHex = colorHex,
                    price = 15 + (mult * 2),
                    action = { engine ->
                        engine.activeMultiplierBonus = mult.toDouble()
                        engine.calculatePayout()
                    }
                )
            }
            roll < rareThreshold -> {
                CoinItem(
                    name = "Wild Expansion",
                    description = "+2 Flanking extra reels forced to 7s",
                    rarity = CoinRarity.UNCOMMON,
                    cardColorHex = 0xFFFFD700, // Gold
                    price = 30,
                    action = { engine ->
                        engine.isExpansionShaking = true
                        engine.expandFlankingReels(SlotSymbol.SEVEN)
                        engine.calculatePayout()
                    }
                )
            }
            else -> {
                if (Random.nextBoolean()) {
                    CoinItem(
                        name = "Target Freeze",
                        description = "Lock reel & pick symbol",
                        rarity = CoinRarity.RARE,
                        cardColorHex = 0xFF8BE9FD, // Cyan / Light Blue
                        price = 25,
                        action = { engine -> engine.isSelectingFreezeTarget = true }
                    )
                } else {
                    CoinItem(
                        name = "BAR Takeover",
                        description = "+2 Flanking reels & full BAR takeover",
                        rarity = CoinRarity.RARE,
                        cardColorHex = 0xFFBD93F9, // Purple
                        price = 35,
                        action = { engine ->
                            engine.isBarTakeoverActive = true
                            engine.isExpansionShaking = true
                            engine.expandFlankingReels(SlotSymbol.BAR)
                            engine.reels.forEachIndexed { idx, reel ->
                                engine.reels[idx] = reel.copy(currentSymbol = SlotSymbol.BAR)
                            }
                            engine.calculatePayout()
                        }
                    )
                }
            }
        }
    }

    fun promptSymbolSelectionForReel(reelId: Int) {
        val targetIndex = reels.indexOfFirst { it.id == reelId }
        if (targetIndex != -1 && !reels[targetIndex].isFrozen) {
            isSelectingFreezeTarget = false
            selectedReelForSymbolPicker = reelId
        }
    }

    fun applyFreezeWithSymbol(chosenSymbol: SlotSymbol) {
        val reelId = selectedReelForSymbolPicker ?: return
        val targetIndex = reels.indexOfFirst { it.id == reelId }
        if (targetIndex != -1) {
            reels[targetIndex] = reels[targetIndex].copy(
                currentSymbol = chosenSymbol,
                isFrozen = true
            )
            calculatePayout()
        }
        selectedReelForSymbolPicker = null
    }

    fun cancelFreezeSelection() {
        isSelectingFreezeTarget = false
        selectedReelForSymbolPicker = null
    }

    private fun checkRoundEndConditions() {
        if (bankroll >= targetBankroll) {
            currentPhase = GamePhase.ROUND_SUCCESS
            return
        }

        if (remainingSpins <= 0) {
            currentPhase = GamePhase.GAME_OVER
        }
    }

    fun proceedToShop() {
        refreshShopStock()
        currentPhase = GamePhase.SHOP
    }

    fun startNextRound() {
        roundNumber += 1
        val aggressiveBonus = if (totalHighRollerBets > 3) 20 else 0
        val growthFactor = if (roundNumber <= 5) 30 else 70 + (roundNumber * 15) + aggressiveBonus

        targetBankroll += growthFactor

        val baseSpins = if (roundNumber <= 5) 12 else 10
        remainingSpins += baseSpins

        roundStartBankroll = bankroll
        roundSpinsUsed = 0

        showWinAnimation = false
        winReasonText = ""
        coinUsedThisSpin = false
        isExpansionShaking = false
        isBarTakeoverActive = false
        activeMultiplierBonus = 1.0
        cancelFreezeSelection()
        reels.clear()
        reels.addAll(listOf(Reel(id = 1), Reel(id = 2), Reel(id = 3)))
        currentPhase = GamePhase.PLAYING
    }

    fun buySpins(spinAmount: Int) {
        val cost = 15
        if (bankroll >= cost) {
            bankroll -= cost
            remainingSpins += spinAmount
        }
    }

    fun restartGame() {
        roundNumber = 1
        targetBankroll = 60
        bankroll = 100
        roundStartBankroll = 100
        roundSpinsUsed = 0
        remainingSpins = 12
        currentBet = 5
        totalHighRollerBets = 0
        totalCoinsUsed = 0
        showWinAnimation = false
        winReasonText = ""
        coinUsedThisSpin = false
        isExpansionShaking = false
        isBarTakeoverActive = false
        activeMultiplierBonus = 1.0
        inventoryCoins.clear()
        equippedCoin = null
        cancelFreezeSelection()
        reels.clear()
        reels.addAll(listOf(Reel(id = 1), Reel(id = 2), Reel(id = 3)))
    }

    private fun unfreezeAllReels() {
        reels.forEachIndexed { idx, reel ->
            reels[idx] = reel.copy(isFrozen = false)
        }
    }

    fun calculatePayout() {
        val symbolCounts = reels.groupBy { it.currentSymbol }
        var maxHits = 0
        var maxHitSymbol: SlotSymbol? = null

        for ((symbol, matchingReels) in symbolCounts) {
            val count = matchingReels.size
            if (count > maxHits) {
                maxHits = count
                maxHitSymbol = symbol
            } else if (count == maxHits && maxHitSymbol != null) {
                if (getSymbolBaseMultiplier(symbol, count) > getSymbolBaseMultiplier(maxHitSymbol, count)) {
                    maxHitSymbol = symbol
                }
            }
        }

        var hitMultiplier = 0.0
        if (maxHitSymbol != null && maxHits >= 1) {
            hitMultiplier = getSymbolBaseMultiplier(maxHitSymbol, maxHits)
        }

        if (activeMultiplierBonus > 1.0 && hitMultiplier == 0.0) {
            hitMultiplier = 1.0
        }

        var totalPayout = currentBet * hitMultiplier * activeMultiplierBonus

        if (isBankAuditRound) {
            totalPayout *= 0.75
        }

        val payoutInt = totalPayout.toInt()
        if (payoutInt > 0) {
            lastWinAmount = payoutInt

            // Generate detailed breakdown message explaining the hit
            val hitType = when (maxHits) {
                1 -> "SINGLE HITTED ${maxHitSymbol?.label}"
                2 -> "PAIR OF ${maxHitSymbol?.label}'s"
                3 -> "3x MATCH ON ${maxHitSymbol?.label}!"
                4 -> "4x MATCH ON ${maxHitSymbol?.label}!!"
                else -> "5x JACKPOT MATCH ON ${maxHitSymbol?.label}!!!"
            }

            val multText = if (activeMultiplierBonus > 1.0) " (${activeMultiplierBonus.toInt()}x COIN BOOST)" else ""
            winReasonText = "$hitType$multText"
            showWinAnimation = true
            bankroll += payoutInt
        } else {
            showWinAnimation = false
            winReasonText = ""
        }
    }

    private fun getSymbolBaseMultiplier(symbol: SlotSymbol, hits: Int): Double {
        return when (symbol) {
            SlotSymbol.SEVEN -> when {
                hits >= 5 -> 10.0
                hits == 4 -> 7.5
                hits == 3 -> 5.0
                hits == 2 -> 2.5
                else -> 1.5
            }
            SlotSymbol.BAR -> when {
                hits >= 5 -> 7.0
                hits == 4 -> 5.2
                hits == 3 -> 3.5
                hits == 2 -> 2.0
                else -> 1.2
            }
            SlotSymbol.LEMON -> when {
                hits >= 5 -> 5.0
                hits == 4 -> 3.7
                hits == 3 -> 2.5
                hits == 2 -> 1.5
                else -> 0.0
            }
            SlotSymbol.CHERRY -> when {
                hits >= 5 -> 4.0
                hits == 4 -> 3.0
                hits == 3 -> 2.0
                hits == 2 -> 1.2
                else -> 1.0
            }
        }
    }
}