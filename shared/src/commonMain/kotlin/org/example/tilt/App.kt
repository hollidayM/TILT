package org.example.tilt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun App() {
    val gameEngine = remember { SlotGameEngine() }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2E))
        ) {
            when (gameEngine.currentPhase) {
                GamePhase.MAIN_MENU -> MainMenuScreen(engine = gameEngine)
                GamePhase.SETTINGS -> SettingsScreen(engine = gameEngine)
                GamePhase.PLAYING -> SlotScreen(engine = gameEngine)
                GamePhase.ROUND_SUCCESS -> RoundSuccessScreen(engine = gameEngine)
                GamePhase.SHOP -> ShopScreen(engine = gameEngine)
                GamePhase.GAME_OVER -> GameOverScreen(engine = gameEngine)
            }
        }
    }
}

@Composable
fun MainMenuScreen(engine: SlotGameEngine) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF0000))
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedLetter(
                        letter = "T",
                        initialRotation = -6f,
                        targetRotation = 6f,
                        initialOffsetY = -8f,
                        targetOffsetY = 8f,
                        durationMs = 1200
                    )

                    AnimatedLetter(
                        letter = "I",
                        initialRotation = -24f,
                        targetRotation = -14f,
                        initialOffsetY = 6f,
                        targetOffsetY = -6f,
                        durationMs = 900
                    )

                    AnimatedLetter(
                        letter = "L",
                        initialRotation = -4f,
                        targetRotation = 8f,
                        initialOffsetY = -10f,
                        targetOffsetY = 4f,
                        durationMs = 1100
                    )

                    AnimatedLetter(
                        letter = "T",
                        initialRotation = 8f,
                        targetRotation = -6f,
                        initialOffsetY = 6f,
                        targetOffsetY = -8f,
                        durationMs = 1300
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rogue-like Tactical Slots",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { engine.startNewRun() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(
                        text = "PLAY RUN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { engine.navigateTo(GamePhase.SETTINGS) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282A36))
                ) {
                    Text(
                        text = "SETTINGS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "v1.0.0",
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedLetter(
    letter: String,
    initialRotation: Float,
    targetRotation: Float,
    initialOffsetY: Float,
    targetOffsetY: Float,
    durationMs: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LetterAnim_$letter")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = initialOffsetY,
        targetValue = targetOffsetY,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OffsetY"
    )

    val rotationZ by infiniteTransition.animateFloat(
        initialValue = initialRotation,
        targetValue = targetRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationMs * 1.2).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RotationZ"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
            this.rotationZ = rotationZ
        }
    ) {
        Text(
            text = letter,
            color = Color.Black,
            fontSize = 76.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    }
}

// --- CLASSIC RETRO SLOT MACHINE BAR BADGE ---
@Composable
fun BarSymbolBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(2.dp)
            .border(2.5.dp, Color(0xFF3B0014), RoundedCornerShape(12.dp))
            .padding(2.dp),
        color = Color(0xFFE63956),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFF6B81))
                .border(1.5.dp, Color(0xFF3B0014), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "BAR",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SettingsScreen(engine: SlotGameEngine) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "SETTINGS", color = Color(0xFFBD93F9), fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Sound Effects", color = Color.White, fontSize = 18.sp)
                Switch(checked = engine.soundEnabled, onCheckedChange = { engine.soundEnabled = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Haptic Feedback", color = Color.White, fontSize = 18.sp)
                Switch(checked = engine.hapticsEnabled, onCheckedChange = { engine.hapticsEnabled = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Fast Spin Mode", color = Color.White, fontSize = 18.sp)
                Switch(checked = engine.fastSpinMode, onCheckedChange = { engine.fastSpinMode = it })
            }
        }

        Button(
            onClick = { engine.navigateTo(GamePhase.MAIN_MENU) },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD93F9))
        ) {
            Text(text = "BACK TO MENU", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SlotScreen(engine: SlotGameEngine) {
    val coroutineScope = rememberCoroutineScope()

    if (engine.selectedReelForSymbolPicker != null) {
        AlertDialog(
            onDismissRequest = { engine.cancelFreezeSelection() },
            title = { Text(text = "CHOOSE SYMBOL TO LOCK", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SlotSymbol.values().forEach { symbol ->
                        Surface(
                            color = Color(0xFF44475A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(54.dp).clickable { engine.applyFreezeWithSymbol(symbol) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (symbol == SlotSymbol.BAR) {
                                    BarSymbolBadge(modifier = Modifier.size(44.dp, 28.dp))
                                } else {
                                    Text(text = symbol.label, fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { engine.cancelFreezeSelection() }) {
                    Text("CANCEL", color = Color(0xFFFF5555))
                }
            },
            containerColor = Color(0xFF282A36)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { engine.navigateTo(GamePhase.MAIN_MENU) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44475A))
            ) {
                Text("⚙ Menu", fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (engine.isBankAuditRound) {
                    Surface(color = Color(0xFFFF5555), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(text = "⚠️ BANK AUDIT ACTIVE (-25% Payouts)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = "ROUND ${engine.roundNumber}" + if (engine.roundNumber <= 5) " (LOOSE)" else "",
                    color = if (engine.isBankAuditRound) Color(0xFFFF5555) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Goal: $${engine.targetBankroll} | Spins Left: ${engine.remainingSpins}", color = Color(0xFFBD93F9), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(40.dp))
        }

        // Bankroll Display & Permanent Win Breakdown Banner
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Bankroll: $${engine.bankroll}",
                color = if (engine.bankroll >= engine.targetBankroll) Color(0xFF50FA7B) else Color(0xFFFF5555),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (engine.showWinAnimation) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Surface(
                        color = Color(0xFF50FA7B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🎉 WIN +$${engine.lastWinAmount}! 🎉",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = engine.winReasonText,
                        color = Color(0xFFF1FA8C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (engine.isAwaitingUserAck) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { engine.acknowledgeWinAndContinue() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (engine.bankroll >= engine.targetBankroll) "GO TO ROUND SUMMARY ➔" else "CONTINUE ➔",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Reels Container
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (engine.activeMultiplierBonus > 1.0 || engine.isBarTakeoverActive) {
                Surface(
                    color = Color(0xFFFFB86C),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    val bannerText = when {
                        engine.isBarTakeoverActive && engine.activeMultiplierBonus > 1.0 -> "BAR TAKEOVER + ${engine.activeMultiplierBonus.toInt()}x MULTIPLIER ACTIVE!"
                        engine.isBarTakeoverActive -> "BAR TAKEOVER ACTIVE!"
                        else -> "🔥 ${engine.activeMultiplierBonus.toInt()}x MULTIPLIER ACTIVE!"
                    }
                    Text(
                        text = bannerText,
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (engine.isSelectingFreezeTarget) {
                Surface(color = Color(0xFF8BE9FD), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = "SELECT A REEL TO LOCK", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                engine.reels.forEachIndexed { index, reel ->
                    key("${reel.id}_${engine.reels.size}") {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ReelCard(
                                reel = reel,
                                isSpinning = engine.isSpinning,
                                isExpansionShaking = engine.isExpansionShaking,
                                isTargetable = engine.isSelectingFreezeTarget && !reel.isFrozen,
                                totalReels = engine.reels.size,
                                onReelClick = { engine.promptSymbolSelectionForReel(reel.id) }
                            )
                        }
                        if (index < engine.reels.size - 1) {
                            Spacer(modifier = Modifier.width(if (engine.reels.size > 3) 2.dp else 6.dp))
                        }
                    }
                }
            }
        }

        // Single-Use Per-Spin Coin Tray
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (engine.coinUsedThisSpin) "COIN CONSUMED FOR THIS SPIN" else "SELECT & USE COIN FOR THIS SPIN",
                color = if (engine.coinUsedThisSpin) Color.Gray else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            val activeCoin = engine.equippedCoin
            if (activeCoin != null) {
                Button(
                    onClick = { engine.useEquippedCoin() },
                    enabled = !engine.coinUsedThisSpin && !engine.isSpinning && !engine.isAwaitingUserAck,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engine.coinUsedThisSpin) Color.DarkGray else Color(activeCoin.cardColorHex)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "USE: ${activeCoin.name}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        Text(text = activeCoin.description, fontSize = 10.sp, color = Color.Black)
                    }
                }
            } else {
                Text(text = "No Coins in Deck (Buy in Shop!)", color = Color.DarkGray, fontSize = 12.sp)
            }

            if (engine.inventoryCoins.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    engine.inventoryCoins.forEach { coin ->
                        val isEquipped = engine.equippedCoin == coin
                        Surface(
                            color = if (isEquipped) Color(0xFF50FA7B) else Color(coin.cardColorHex),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable(!engine.isAwaitingUserAck) { engine.equipCoin(coin) }
                        ) {
                            Text(
                                text = coin.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bet Selector & Spin Button
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "SELECT BET SIZE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BetButton(amount = 1, label = "Penny ($1)", engine = engine)
                BetButton(amount = 5, label = "Standard ($5)", engine = engine)
                BetButton(amount = 25, label = "High Roller ($25)", engine = engine)
            }

            Button(
                onClick = { coroutineScope.launch { engine.spin() } },
                enabled = !engine.isSpinning && engine.remainingSpins > 0 && engine.bankroll >= engine.currentBet && !engine.isSelectingFreezeTarget && engine.selectedReelForSymbolPicker == null && !engine.isAwaitingUserAck,
                modifier = Modifier.fillMaxWidth(0.8f).height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD93F9))
            ) {
                Text(
                    text = if (engine.isSpinning) "SPINNING..." else "SPIN ($${engine.currentBet})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RoundSuccessScreen(engine: SlotGameEngine) {
    val roundProfit = engine.bankroll - engine.roundStartBankroll

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉 ROUND ${engine.roundNumber} CLEARED! 🎉",
                color = Color(0xFF50FA7B),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TARGET SMASHED!",
                color = Color(0xFFF1FA8C),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = Color(0xFF282A36),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Goal:", color = Color.Gray, fontSize = 14.sp)
                    Text("$${engine.targetBankroll}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Round Profit:", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = if (roundProfit >= 0) "+$$roundProfit" else "-$$roundProfit",
                        color = if (roundProfit >= 0) Color(0xFF50FA7B) else Color(0xFFFF5555),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Bankroll:", color = Color.Gray, fontSize = 14.sp)
                    Text("$${engine.bankroll}", color = Color(0xFF50FA7B), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }

                Divider(color = Color(0xFF44475A), thickness = 1.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spins Used:", color = Color.Gray, fontSize = 14.sp)
                    Text("${engine.roundSpinsUsed}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spins Carrying Over:", color = Color.Gray, fontSize = 14.sp)
                    Text("➕ ${engine.remainingSpins} SPINS", color = Color(0xFFBD93F9), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Button(
            onClick = { engine.proceedToShop() },
            modifier = Modifier.fillMaxWidth(0.85f).height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B))
        ) {
            Text(
                text = "CONTINUE TO SHOP ➔",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun BetButton(amount: Int, label: String, engine: SlotGameEngine) {
    val isSelected = engine.currentBet == amount

    Button(
        onClick = { engine.setBet(amount) },
        enabled = !engine.isSpinning && !engine.isSelectingFreezeTarget && !engine.isAwaitingUserAck,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF50FA7B) else Color(0xFF44475A),
            contentColor = if (isSelected) Color.Black else Color.White
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShopScreen(engine: SlotGameEngine) {
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "RollShake")
    val rollRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RollRotate"
    )

    val rollScale by animateFloatAsState(
        targetValue = if (engine.isOpeningPack) 1.25f else 1.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "RollScale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "INTERMISSION SHOP", color = Color(0xFFBD93F9), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Bankroll: $${engine.bankroll}", color = Color(0xFF50FA7B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "FEATURED COIN CARDS (RANDOM STOCK)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                engine.shopCoinStock.forEach { shopCoin ->
                    Surface(
                        color = Color(shopCoin.cardColorHex),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(130.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = shopCoin.name, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = shopCoin.description, color = Color.Black, fontSize = 9.sp, minLines = 2)
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { engine.buyIndividualCoin(shopCoin) },
                                enabled = engine.bankroll >= shopCoin.price,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "BUY ($${shopCoin.price})", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(140.dp, 55.dp)
                    .scale(rollScale)
                    .graphicsLayer {
                        if (engine.isOpeningPack) {
                            rotationZ = rollRotation
                        }
                    }
                    .background(Color(0xFFFFD700), shape = RoundedCornerShape(28.dp))
                    .border(3.dp, Color(0xFFFFB86C), shape = RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Surface(modifier = Modifier.size(10.dp, 32.dp), shape = CircleShape, color = Color(0xFFB8860B)) {}
                    }
                    Text(
                        text = if (engine.isOpeningPack) "UNROLLING..." else "MYSTERY ROLL ($25)",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { coroutineScope.launch { engine.buyAndOpenCoinPack() } },
                    enabled = engine.bankroll >= 25 && !engine.isOpeningPack
                ) {
                    Text("Buy Mystery Roll ($25)", fontSize = 11.sp)
                }

                Button(
                    onClick = { engine.buySpins(spinAmount = 2) },
                    enabled = engine.bankroll >= 15 && !engine.isOpeningPack
                ) {
                    Text("+2 Spins ($15)", fontSize = 11.sp)
                }
            }
        }

        Button(
            onClick = { engine.startNextRound() },
            enabled = !engine.isOpeningPack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B))
        ) {
            Text("START ROUND ${engine.roundNumber + 1}", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GameOverScreen(engine: SlotGameEngine) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "💀 RUN FAILED", color = Color(0xFFFF5555), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "You didn't reach $${engine.targetBankroll} in time.", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { engine.startNewRun() },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("RETRY RUN")
        }
    }
}

@Composable
fun ReelCard(
    reel: Reel,
    isSpinning: Boolean,
    isExpansionShaking: Boolean,
    isTargetable: Boolean,
    totalReels: Int = 3,
    onReelClick: () -> Unit
) {
    val density = LocalDensity.current
    val shakePx = with(density) { 8.dp.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "ShakeTransition")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -shakePx,
        targetValue = shakePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 45, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShakeAnimation"
    )

    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 55, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShakeRotation"
    )

    val baseTargetScale = if (reel.isExpanded) 1.08f else 1.0f
    val reelScale by animateFloatAsState(
        targetValue = if (isSpinning && !reel.isFrozen) baseTargetScale * 0.92f else baseTargetScale,
        animationSpec = tween(durationMillis = 75),
        label = "ReelScaleAnimation"
    )

    val borderColor = when {
        reel.isExpanded -> Color(0xFFFF79C6)
        reel.isFrozen -> Color(0xFF00B0FF)
        isTargetable -> Color(0xFF8BE9FD)
        else -> Color(0xFFFFD700)
    }

    val backgroundColor = when {
        reel.isFrozen -> Color(0xFFE0F7FA)
        isTargetable -> Color(0xFF44475A)
        else -> Color.White
    }

    val cardHeight = when {
        totalReels > 5 -> 72.dp
        totalReels > 3 -> 88.dp
        else -> 110.dp
    }
    val symbolFontSize = when {
        totalReels > 5 -> 18.sp
        totalReels > 3 -> 22.sp
        else -> 34.sp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .scale(reelScale)
            .graphicsLayer {
                if (isExpansionShaking || (reel.isExpanded && isExpansionShaking)) {
                    translationX = shakeOffset
                    translationY = shakeOffset / 2f
                    rotationZ = shakeRotation
                }
            }
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (reel.isExpanded || reel.isFrozen || isTargetable) 3.5.dp else 2.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isTargetable) { onReelClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (reel.currentSymbol == SlotSymbol.BAR) {
                BarSymbolBadge()
            } else {
                Text(text = reel.currentSymbol.label, fontSize = symbolFontSize)
            }

            if (reel.isFrozen) {
                Text(text = "LOCKED ❄️", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00B0FF))
            } else if (reel.isExpanded) {
                Text(text = "EXTRA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF79C6))
            } else if (isTargetable) {
                Text(text = "TAP TO LOCK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8BE9FD))
            }
        }
    }
}