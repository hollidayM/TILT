package org.example.tilt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class SlotSymbol(val label: String, val weight: Int) {
    CHERRY("🍒", 40),
    LEMON("🍋", 30),
    BAR("🍫", 20), // Updated to Classic Slot Bar look
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
    val action: (engine: SlotGameEngine) -> Unit
)

enum class GamePhase {
    MAIN_MENU,
    SETTINGS,
    PLAYING,
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

    // Win Animation State
    var lastWinAmount by mutableStateOf(0)
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

    var isSpinning by mutableStateOf(false)
        private set
    var coinUsedThisSpin by mutableStateOf(false)
        private set

    var isExpansionShaking by mutableStateOf(false)
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
        isSpinning = true

        val spinTicks = if (fastSpinMode) 6 else 14
        val tickDelay = if (fastSpinMode) 50L else 75L
        val allSymbols = SlotSymbol.values()

        repeat(spinTicks) {
            reels.forEachIndexed { index, reel ->
                if (!reel.isFrozen && !reel.isExpanded) {
                    reels[index] = reel.copy(currentSymbol = allSymbols.random())
                }
            }
            delay(tickDelay)
        }

        reels.forEachIndexed { index, reel ->
            if (!reel.isFrozen && !reel.isExpanded) {
                reels[index] = reel.copy(currentSymbol = getRandomSymbolWeighted())
            }
        }

        calculatePayout()
        unfreezeAllReels()

        coinUsedThisSpin = false
        isExpansionShaking = false
        activeMultiplierBonus = 1.0
        isSpinning = false

        checkRoundEndConditions()
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

    private fun generateRandomCoin(): CoinItem {
        val roll = Random.nextInt(100)
        val rareThreshold = if (totalCoinsUsed > 8) 70 else 85

        return when {
            roll < 55 -> {
                val mult = listOf(2, 4, 6).random()
                CoinItem(
                    name = "${mult}x Multiplier",
                    description = "Boosts current spin payout by ${mult}x",
                    rarity = CoinRarity.COMMON,
                    action = { engine -> engine.activeMultiplierBonus = mult.toDouble() }
                )
            }
            roll < rareThreshold -> {
                CoinItem(
                    name = "Wild Expansion",
                    description = "+2 Extra reels forced to 7s",
                    rarity = CoinRarity.UNCOMMON,
                    action = { engine ->
                        engine.isExpansionShaking = true
                        engine.reels.add(Reel(id = engine.reels.size + 1, currentSymbol = SlotSymbol.SEVEN, isExpanded = true))
                        engine.reels.add(Reel(id = engine.reels.size + 2, currentSymbol = SlotSymbol.SEVEN, isExpanded = true))
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
                        action = { engine -> engine.isSelectingFreezeTarget = true }
                    )
                } else {
                    CoinItem(
                        name = "BAR Takeover",
                        description = "Guaranteed 3+ BAR match payout",
                        rarity = CoinRarity.RARE,
                        action = { engine ->
                            engine.isExpansionShaking = true
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
        }
        selectedReelForSymbolPicker = null
    }

    fun cancelFreezeSelection() {
        isSelectingFreezeTarget = false
        selectedReelForSymbolPicker = null
    }

    private fun checkRoundEndConditions() {
        if (bankroll >= targetBankroll) {
            currentPhase = GamePhase.SHOP
            return
        }

        if (remainingSpins <= 0) {
            currentPhase = GamePhase.GAME_OVER
        }
    }

    fun startNextRound() {
        roundNumber += 1
        val aggressiveBonus = if (totalHighRollerBets > 3) 20 else 0
        val growthFactor = if (roundNumber <= 5) 30 else 70 + (roundNumber * 15) + aggressiveBonus

        targetBankroll += growthFactor
        remainingSpins = if (roundNumber <= 5) 12 else 10

        showWinAnimation = false
        coinUsedThisSpin = false
        isExpansionShaking = false
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
        remainingSpins = 12
        currentBet = 5
        totalHighRollerBets = 0
        totalCoinsUsed = 0
        showWinAnimation = false
        coinUsedThisSpin = false
        isExpansionShaking = false
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
            if (matchingReels.size > maxHits) {
                maxHits = matchingReels.size
                maxHitSymbol = symbol
            }
        }

        var hitMultiplier = 0.0

        if (maxHits >= 3) {
            hitMultiplier = 1.8
        } else if (maxHits == 2) {
            hitMultiplier = 1.4
        } else if (maxHits == 1) {
            // ONLY BAR, 7, OR CHERRY GET 1.2x SINGLE HITS
            if (maxHitSymbol == SlotSymbol.BAR || maxHitSymbol == SlotSymbol.SEVEN || maxHitSymbol == SlotSymbol.CHERRY) {
                hitMultiplier = 1.2
            }
        }

        var totalPayout = currentBet * hitMultiplier * activeMultiplierBonus

        if (isBankAuditRound) {
            totalPayout *= 0.75
        }

        val payoutInt = totalPayout.toInt()
        if (payoutInt > 0) {
            lastWinAmount = payoutInt
            showWinAnimation = true
            bankroll += payoutInt
        } else {
            showWinAnimation = false
        }
    }
}