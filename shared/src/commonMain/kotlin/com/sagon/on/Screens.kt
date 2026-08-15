package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 6.0 (RADIO PURA)
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import on.shared.generated.resources.Res
import on.shared.generated.resources.logo

@Composable
fun LoadingScreen(isNightMode: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "Alpha"
    )

    val background = if (isNightMode) LuxeColors.NightGradient else LuxeColors.BackgroundGradient

    Box(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = LuxeColors.Gold,
                    strokeWidth = 2.dp,
                    trackColor = Color.White.copy(0.05f)
                )
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape).alpha(alpha)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "CALIBRANDO FRECUENCIA...",
                color = LuxeColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    nick: String, 
    onNickChange: (String) -> Unit, 
    totalUsers: Int, 
    hasAcceptedMic: Boolean,
    onMicAccept: () -> Unit,
    onConnect: (String?) -> Unit,
    isNightMode: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EliteWelcome")
    val background = if (isNightMode) LuxeColors.NightGradient else LuxeColors.BackgroundGradient

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        StarryBackground(activity = 0.2f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(60.dp))

            Box(contentAlignment = Alignment.Center) {
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "Glow"
                )
                
                Box(
                    Modifier
                        .size(120.dp)
                        .scale(glowScale)
                        .background(LuxeColors.Gold.copy(0.1f), CircleShape)
                        .blur(40.dp)
                )
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.Black,
                    border = BorderStroke(2.dp, LuxeColors.Gold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
            Text("IDENTIFICACIÓN", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("ON AIR SPAIN", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            
            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(0.04f),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                    if (nick.isEmpty()) Text("TU INDICATIVO...", color = Color.White.copy(0.2f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = nick,
                        onValueChange = { if (it.length <= 15) onNickChange(it.uppercase().filter { c -> c in 'A'..'Z' || c in '0'..'9' || c == ' ' || c == '-' }) },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        cursorBrush = SolidColor(LuxeColors.Gold),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { if (nick.isNotBlank()) { if (!hasAcceptedMic) onMicAccept(); onConnect(null) } },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold),
                shape = RoundedCornerShape(20.dp),
                enabled = nick.isNotBlank()
            ) {
                Text("ENTRAR EN LA RADIO", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 16.sp, color = Color.Black)
            }
            
            if (totalUsers > 0) {
                Spacer(Modifier.height(32.dp))
                Surface(color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(LuxeColors.Gold, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("$totalUsers ONLINE", color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioPanel(
    nick: String,
    mic: Float,
    users: List<RemoteUser>,
    rx: Boolean,
    transmitterNick: String?,
    isBeeping: Boolean,
    onNoise: (Float) -> Unit,
    onMic: (Boolean, Float) -> Unit,
    onShare: (String, String, String?, String?) -> Unit,
    onExit: () -> Unit,
    state: RadioState,
    onStateChange: (RadioState) -> Unit,
    externalPtt: Boolean,
    externalPttBlocked: Boolean,
    replayProgress: Float,
    isReplayReady: Boolean,
    onSendMessage: (String, String?) -> Unit,
    onReport: (String) -> Unit,
    onBlock: (String) -> Unit,
    onReplay: () -> Unit,
    pendingDialog: RadioDialogType?,
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit
) {
    var pttLocked by remember { mutableStateOf(state.isPttLatched) }
    var isPttBlockedByRx by remember { mutableStateOf(false) }

    LaunchedEffect(externalPttBlocked) { if (externalPttBlocked) isPttBlockedByRx = true }
    LaunchedEffect(isPttBlockedByRx) { if (isPttBlockedByRx) { delay(800); isPttBlockedByRx = false } }

    val isTransmitting = (pttLocked || externalPtt || isBeeping) && !isPttBlockedByRx
    
    val noiseVol = if (!rx && !isTransmitting) (if (state.squelch > state.rfGain) 0f else (state.rfGain - state.squelch)).coerceIn(0f, 1f) else 0f
    
    LaunchedEffect(pttLocked) { onMic(pttLocked, state.veteranPower) }
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { onNoise(noiseVol) }

    Box(modifier = Modifier.fillMaxSize().background(EliteTheme.DeepGradient)) {
        StarryBackground(activity = if (isTransmitting || rx) 0.6f else 0.15f, isEcoMode = state.isEcoMode)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).padding(bottom = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onExit) { Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color.Red.copy(0.6f)) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tu indicativo: ", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(nick, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = { if (isReplayReady) onReplay() }, color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f), shape = CircleShape, border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)), modifier = Modifier.size(38.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            if (replayProgress > 0f) CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp)
                            Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(0.05f)).border(1.dp, Color.White.copy(0.1f), CircleShape).clickable { onPendingDialogChange(RadioDialogType.SETTINGS, null) }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)), color = Color.Black.copy(0.7f), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.width(80.dp)) {
                                TechLabel("SQUELCH", "${(state.squelch * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.SETTINGS, null) }
                                Spacer(Modifier.height(16.dp))
                                TechLabel("GANANCIA", "${(state.rfGain * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.SETTINGS, null) }
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                val statusText = when { rx -> "RECIBIENDO..."; isTransmitting || isBeeping -> "ON AIR"; else -> "EN ESPERA" }
                                Text(text = statusText, color = if(rx) LuxeColors.Gold else if(isTransmitting) Color.Red else Color.White.copy(0.2f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                Spacer(Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    repeat(12) { i ->
                                        val isActive = i < (mic * 12)
                                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (isActive) (if(i > 9) Color.Red else if(i > 7) Color(0xFFFACC15) else LuxeColors.ElectricBlue) else Color.White.copy(0.04f)))
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(onClick = { onPendingDialogChange(RadioDialogType.SELECT_CITY, null) }, modifier = Modifier.weight(1f).fillMaxHeight(), color = LuxeColors.Gold.copy(0.15f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.4f))) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                            Icon(Icons.Rounded.Home, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(text = "CH ${CITY_CHANNELS[state.city.uppercase()] ?: ""} - ${state.channel}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f).basicMarquee())
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Surface(onClick = { onShare(state.channel, state.subtone, null, null) }, modifier = Modifier.size(44.dp), color = LuxeColors.Gold.copy(0.2f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.Gold)) {
                                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp)) }
                                    }
                                }
                            }
                            Column(modifier = Modifier.width(80.dp), horizontalAlignment = Alignment.End) {
                                Text("WATTS", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Text("${if(isTransmitting) (state.veteranPower * 15f).toInt() else if(rx) 9 else 0}W", color = if(isTransmitting) Color.Red else if(rx) LuxeColors.Gold else Color.White.copy(0.2f), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            var showTools by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CONFIGURACIÓN", color = LuxeColors.Gold.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                IconButton(onClick = { showTools = !showTools }) { Icon(if (showTools) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = LuxeColors.Gold) }
            }

            AnimatedVisibility(visible = showTools) {
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                    item { TacticalDockIcon(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE, null) }) }
                    item { TacticalDockIcon(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) onStateChange(state.copy(isVoxEnabled = false)) else onPendingDialogChange(RadioDialogType.VOX, null) }) }
                    item { TacticalDockIcon(icon = Icons.Rounded.Headset, label = "MONI", isActive = state.isMonitorEnabled, onClick = { if (state.isMonitorEnabled) onStateChange(state.copy(isMonitorEnabled = false)) else onPendingDialogChange(RadioDialogType.MONI, null) }) }
                    item { TacticalDockIcon(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { onStateChange(state.copy(isDspEnabled = !state.isDspEnabled)) }) }
                    item { TacticalDockIcon(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)) }) }
                    item { TacticalDockIcon(icon = Icons.Rounded.Tune, label = "EQUIPO", isActive = true, onClick = { onPendingDialogChange(RadioDialogType.SETTINGS, null) }) }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val pttColor = if(isTransmitting) Color.Red else if(rx) Color.Green else Color.White
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight().pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (!isPttBlockedByRx) {
                                    pttLocked = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        pttLocked = false
                                    }
                                }
                            }
                        )
                    }, 
                    shape = RoundedCornerShape(40.dp), 
                    color = pttColor.copy(0.1f), 
                    border = BorderStroke(3.dp, pttColor.copy(0.4f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Mic, null, tint = pttColor, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(if(isTransmitting) "AIRE" else if(rx) "RECIBIENDO" else "HABLAR", color = pttColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
                Surface(onClick = { pttLocked = !pttLocked; onStateChange(state.copy(isPttLatched = pttLocked)) }, modifier = Modifier.size(120.dp), shape = RoundedCornerShape(40.dp), color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f), border = BorderStroke(3.dp, if (pttLocked) Color.Red else Color.White.copy(0.1f))) {
                    Box(contentAlignment = Alignment.Center) { Icon(if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null, tint = if (pttLocked) Color.Red else Color.White.copy(0.3f), modifier = Modifier.size(40.dp)) }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("OPERADORES EN ${state.city.split("-")[0]}", color = Color.White.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())
            LazyRow(modifier = Modifier.fillMaxWidth().height(180.dp), contentPadding = PaddingValues(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // 🛡️ MOSTRAR TODOS LOS USUARIOS DE LA CIUDAD BASE
                items(users.filter { it.city.split("-")[0] == state.city.split("-")[0] }) { user -> 
                    UserCard(
                        user = user, 
                        isMe = (user.nick.trim().uppercase() == nick.trim().uppercase()), 
                        onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, 
                        onPrivateChat = { }, 
                        onReport = { onReport(user.id) }, 
                        onBlock = { onBlock(user.id) }
                    ) 
                }
            }
        }
    }
}

@Composable
fun TacticalDockIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color = LuxeColors.Gold
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
            color = if (isActive) activeColor.copy(0.15f) else Color.White.copy(0.05f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, if (isActive) activeColor else Color.White.copy(0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (isActive) activeColor else Color.White, modifier = Modifier.size(28.dp)) }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = if (isActive) LuxeColors.Gold else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun TechLabel(label: String, value: String, valueColor: Color = Color.White, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(4.dp)) {
        Text(label, color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(text = value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}
