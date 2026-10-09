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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
                GamePhase.SHOP -> ShopScreen(engine = gameEngine)
                GamePhase.GAME_OVER -> GameOverScreen(engine = gameEngine)
            }
        }
    }
}

@Composable
fun MainMenuScreen(engine: SlotGameEngine) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎰 TILT 🎰", color = Color(0xFFBD93F9), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Rogue-like Tactical Slots", color = Color(0xFFF1FA8C), fontSize = 16.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { engine.startNewRun() },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B))
            ) {
                Text(text = "PLAY RUN", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Button(
                onClick = { engine.navigateTo(GamePhase.SETTINGS) },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44475A))
            ) {
                Text(text = "SETTINGS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Text(text = "v1.0.0", color = Color.Gray, fontSize = 12.sp)
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
                                Text(text = symbol.label, fontSize = 28.sp)
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

        // Bankroll Display & Animated Win Popup
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Bankroll: $${engine.bankroll}",
                color = if (engine.bankroll >= engine.targetBankroll) Color(0xFF50FA7B) else Color(0xFFFF5555),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Animated Win Payout Overlay
            AnimatedVisibility(
                visible = engine.showWinAnimation,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = Color(0xFF50FA7B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "🎉 WIN +$${engine.lastWinAmount}! 🎉",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Reels Container
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (engine.isSelectingFreezeTarget) {
                Surface(color = Color(0xFF8BE9FD), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = "SELECT A REEL TO LOCK", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                engine.reels.forEach { reel ->
                    ReelCard(
                        reel = reel,
                        isSpinning = engine.isSpinning,
                        isExpansionShaking = engine.isExpansionShaking,
                        isTargetable = engine.isSelectingFreezeTarget && !reel.isFrozen,
                        onReelClick = { engine.promptSymbolSelectionForReel(reel.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                    enabled = !engine.coinUsedThisSpin && !engine.isSpinning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engine.coinUsedThisSpin) Color.DarkGray else Color(0xFFFFB86C)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "USE: ${activeCoin.name}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        Text(text = activeCoin.description, fontSize = 10.sp, color = Color.Black)
                    }
                }
            } else {
                Text(text = "No Coins in Deck (Buy Rolls in Shop!)", color = Color.DarkGray, fontSize = 12.sp)
            }

            if (engine.inventoryCoins.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    engine.inventoryCoins.forEach { coin ->
                        val isEquipped = engine.equippedCoin == coin
                        Surface(
                            color = if (isEquipped) Color(0xFF50FA7B) else Color(0xFF44475A),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { engine.equipCoin(coin) }
                        ) {
                            Text(
                                text = coin.name,
                                fontSize = 10.sp,
                                color = if (isEquipped) Color.Black else Color.White,
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
                enabled = !engine.isSpinning && engine.remainingSpins > 0 && engine.bankroll >= engine.currentBet && !engine.isSelectingFreezeTarget && engine.selectedReelForSymbolPicker == null,
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
fun BetButton(amount: Int, label: String, engine: SlotGameEngine) {
    val isSelected = engine.currentBet == amount

    Button(
        onClick = { engine.setBet(amount) },
        enabled = !engine.isSpinning && !engine.isSelectingFreezeTarget,
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎉 TARGET REACHED!", color = Color(0xFF50FA7B), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Current Bankroll: $${engine.bankroll}", color = Color.White, fontSize = 18.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(150.dp, 65.dp)
                    .scale(rollScale)
                    .graphicsLayer {
                        if (engine.isOpeningPack) {
                            rotationZ = rollRotation
                        }
                    }
                    .background(Color(0xFFFFD700), shape = RoundedCornerShape(32.dp))
                    .border(4.dp, Color(0xFFFFB86C), shape = RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Surface(
                            modifier = Modifier.size(12.dp, 40.dp),
                            shape = CircleShape,
                            color = Color(0xFFB8860B)
                        ) {}
                    }
                    Text(
                        text = if (engine.isOpeningPack) "UNROLLING..." else "ROLL OF COINS",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (engine.revealedPackCoins.isNotEmpty() && !engine.isOpeningPack) {
                Text(text = "UNROLLED COINS:", color = Color(0xFF8BE9FD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    engine.revealedPackCoins.forEach { coin ->
                        Surface(color = Color(0xFF44475A), shape = RoundedCornerShape(8.dp)) {
                            Text(text = coin.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { coroutineScope.launch { engine.buyAndOpenCoinPack() } },
                enabled = engine.bankroll >= 25 && !engine.isOpeningPack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy Roll of Coins ($25)")
            }

            Button(
                onClick = { engine.buySpins(spinAmount = 2) },
                enabled = engine.bankroll >= 15 && !engine.isOpeningPack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy +2 Extra Spins Next Round ($15)")
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
    onReelClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShakeTransition")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShakeAnimation"
    )

    val baseTargetScale = if (reel.isExpanded) 1.15f else 1.0f
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
        reel.isFrozen -> Color(0xFF8BE9FD)
        isTargetable -> Color(0xFF44475A)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(80.dp, 110.dp)
            .scale(reelScale)
            .graphicsLayer {
                if (isExpansionShaking) {
                    translationX = shakeOffset
                    translationY = shakeOffset
                }
            }
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .border(
                width = if (reel.isExpanded || isTargetable) 4.dp else 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isTargetable) { onReelClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = reel.currentSymbol.label, fontSize = 36.sp)
            if (isTargetable) {
                Text(text = "TAP TO LOCK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8BE9FD))
            }
        }
    }
}