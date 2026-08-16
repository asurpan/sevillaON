package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.0 (PURE RADIO)
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
    onNotification: (String, String, NotificationType) -> Unit,
    onPlaySound: (String) -> Unit = {},
    pendingDialog: RadioDialogType?,
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit
) {
    var pttLocked by remember { mutableStateOf(state.isPttLatched) }
    var isPttBlockedByRx by remember { mutableStateOf(false) }
    
    val pttScale by animateFloatAsState(if (pttLocked) 0.94f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "PttSqueeze")

    LaunchedEffect(externalPttBlocked) { if (externalPttBlocked) isPttBlockedByRx = true }
    LaunchedEffect(isPttBlockedByRx) { if (isPttBlockedByRx) { delay(800); isPttBlockedByRx = false } }

    val currentOnStateChange by rememberUpdatedState(onStateChange)
    val currentOnPlaySound by rememberUpdatedState(onPlaySound)
    val currentRadioState by rememberUpdatedState(state)

    // 🛡️ REGLA DE ORO: EL PTT SIEMPRE DETIENE EL ESCANEO
    val isTransmitting = (pttLocked || externalPtt || isBeeping) && !isPttBlockedByRx
    LaunchedEffect(isTransmitting) {
        if (isTransmitting && state.isScanning) {
            currentOnStateChange(currentRadioState.copy(isScanning = false))
        }
    }

    // 🚀 MOTOR DE ESCANEO PROFESIONAL (ORDEN NUMÉRICO 1-40)
    LaunchedEffect(state.isScanning) {
        if (state.isScanning) {
            val channelToCity = CITY_CHANNELS.entries.groupBy { it.value }.mapValues { it.value.first().key }
            val currentCityBase = state.city.split("-")[0].uppercase()
            var currentCh = CITY_CHANNELS[currentCityBase] ?: 1
            
            while (state.isScanning) {
                val isSquelchOpen = currentRadioState.rfGain > currentRadioState.squelch
                if (isSquelchOpen && !rx) {
                    delay(500)
                    continue
                }

                currentCh = if (currentCh >= 40) 1 else currentCh + 1
                val nextCity = channelToCity[currentCh] ?: "SORIA"
                
                currentOnStateChange(currentRadioState.copy(city = nextCity, channel = nextCity))
                currentOnPlaySound("switch")
                
                delay(400) // 🚀 ESCANEO MÁS RÁPIDO (Velocidad de equipo real)
                
                if (rx) {
                    delay(3000) 
                    if (isTransmitting) break // Ya manejado por el LaunchedEffect superior
                }
            }
        }
    }
    
    val noiseVol = if (!rx && !isTransmitting) (if (state.squelch > state.rfGain) 0f else (state.rfGain - state.squelch)).coerceIn(0f, 1f) else 0f
    
    LaunchedEffect(pttLocked) { onMic(pttLocked, state.veteranPower) }
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { onNoise(noiseVol) }

    Box(modifier = Modifier.fillMaxSize().background(LuxeColors.DeepSea)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- BLOQUE SUPERIOR (Información y S-Meter) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Compacto
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onExit, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color.Red.copy(0.8f), modifier = Modifier.size(20.dp)) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nick, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(onClick = { if (isReplayReady) onReplay() }, color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f), shape = CircleShape, border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                if (replayProgress > 0f) CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp)
                                Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(0.05f)).border(1.dp, Color.White.copy(0.1f), CircleShape).clickable { onPendingDialogChange(RadioDialogType.SETTINGS, null) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Pantalla de la Radio (S-Meter y Canal)
                Surface(
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)), 
                    color = if(rx) Color(0xFF064E3B) else Color.Black, 
                    border = BorderStroke(2.dp, if(rx) Color(0xFF10B981) else LuxeColors.Gold.copy(0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.width(60.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniTechLabel("VOL", "${(state.systemVolume * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.VOLUME_CONTROL, null) }
                                MiniTechLabel("SQL", "${(state.squelch * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.SQUELCH_CONTROL, null) }
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                val statusText = when { 
                                    rx -> transmitterNick?.uppercase() ?: "RECIBIENDO"
                                    isTransmitting || isBeeping -> "AIRE"
                                    else -> "SQUELCH" 
                                }
                                Text(
                                    text = statusText, 
                                    color = if(rx) Color(0xFF10B981) else if(isTransmitting) Color.Red else Color.White.copy(0.2f), 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black, 
                                    letterSpacing = if(rx) 1.sp else 2.sp,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(2.dp)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(15) { i ->
                                        val isActive = i < (mic * 15)
                                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(1.dp)).background(if (isActive) (if(i > 12) Color.Red else if(i > 9) Color(0xFFFACC15) else LuxeColors.Gold) else Color.White.copy(0.05f)))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                
                                // CANAL PRINCIPAL
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(100.dp), 
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val blinkAlpha = remember { Animatable(1f) }
                                    LaunchedEffect(Unit) {
                                        while(true) {
                                            delay((3000..8000).random().toLong())
                                            blinkAlpha.animateTo(0.3f, tween(150))
                                            blinkAlpha.animateTo(1f, tween(150))
                                            if ((0..1).random() == 1) {
                                                delay(100)
                                                blinkAlpha.animateTo(0.3f, tween(100))
                                                blinkAlpha.animateTo(1f, tween(100))
                                            }
                                        }
                                    }

                                    // 🔼 BOTÓN SUBIR CANAL
                                    Surface(
                                        modifier = Modifier.size(54.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().combinedClickable(
                                                onClick = {
                                                    onPlaySound("switch")
                                                    if (state.isScanning) {
                                                        // 🛡️ REGLA: UN CLIC DETIENE EL ESCANEO SI ESTÁ ACTIVO
                                                        onStateChange(state.copy(isScanning = false))
                                                    } else {
                                                        val chToCity = CITY_CHANNELS.entries.groupBy { it.value }.mapValues { it.value.first().key }
                                                        val curCh = CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: 1
                                                        val nextCh = if (curCh >= 40) 1 else curCh + 1
                                                        val nextCity = chToCity[nextCh] ?: "SORIA"
                                                        onStateChange(state.copy(city = nextCity, channel = nextCity))
                                                    }
                                                },
                                                onLongClick = {
                                                    if (state.rfGain > state.squelch) {
                                                        onNotification("AVISO ESCANEO", "CIERRA EL SQUELCH PARA PODER ESCANEAR", NotificationType.Warning)
                                                    } else {
                                                        onPlaySound("switch")
                                                        onStateChange(state.copy(isScanning = true)) 
                                                    }
                                                }
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.KeyboardArrowUp, null, tint = LuxeColors.Gold.copy(0.6f), modifier = Modifier.size(36.dp))
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    // 🛡️ BLOQUE CENTRAL FIJO (BLINDAJE TOTAL CONTRA MOVIMIENTO)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally, 
                                        modifier = Modifier.width(140.dp) 
                                    ) {
                                        Text(
                                            text = "CH ${CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: "00"}", 
                                            color = LuxeColors.Gold, 
                                            fontSize = 38.sp, 
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.clickable { onPendingDialogChange(RadioDialogType.SELECT_CITY, null) }
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically, 
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            IconButton(onClick = { onShare(state.channel, state.subtone, null, null) }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold.copy(0.8f), modifier = Modifier.size(12.dp).graphicsLayer { alpha = blinkAlpha.value })
                                            }
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = state.city.split("-")[0], 
                                                color = Color.White.copy(0.6f), 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                maxLines = 1, 
                                                modifier = Modifier.basicMarquee()
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    // 🔽 BOTÓN BAJAR CANAL
                                    Surface(
                                        modifier = Modifier.size(54.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().combinedClickable(
                                                onClick = {
                                                    onPlaySound("switch")
                                                    if (state.isScanning) {
                                                        // 🛡️ REGLA: UN CLIC DETIENE EL ESCANEO SI ESTÁ ACTIVO
                                                        onStateChange(state.copy(isScanning = false))
                                                    } else {
                                                        val chToCity = CITY_CHANNELS.entries.groupBy { it.value }.mapValues { it.value.first().key }
                                                        val curCh = CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: 1
                                                        val nextCh = if (curCh <= 1) 40 else curCh - 1
                                                        val nextCity = chToCity[nextCh] ?: "SORIA"
                                                        onStateChange(state.copy(city = nextCity, channel = nextCity))
                                                    }
                                                },
                                                onLongClick = {
                                                    if (state.rfGain > state.squelch) {
                                                        onNotification("AVISO ESCANEO", "CIERRA EL SQUELCH PARA PODER ESCANEAR", NotificationType.Warning)
                                                    } else {
                                                        onPlaySound("switch")
                                                        onStateChange(state.copy(isScanning = true)) 
                                                    }
                                                }
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = LuxeColors.Gold.copy(0.6f), modifier = Modifier.size(36.dp))
                                        }
                                    }
                                }
                            }
                            Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.End) {
                                Text("WATTS", color = Color.White.copy(0.3f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Text("${if(isTransmitting) (state.veteranPower * 15f).toInt() else if(rx) 9 else 0}", color = if(isTransmitting) Color.Red else if(rx) LuxeColors.Gold else Color.White.copy(0.2f), fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Herramientas (Minimizadas por defecto)
                var showTools by remember { mutableStateOf(false) }
                Row(modifier = Modifier.fillMaxWidth().clickable { showTools = !showTools }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("HERRAMIENTAS TÁCTICAS", color = LuxeColors.Gold.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Icon(if (showTools) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(16.dp))
                }

                AnimatedVisibility(visible = showTools) {
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { MiniTacticalIcon(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) onStateChange(state.copy(isVoxEnabled = false)) else onPendingDialogChange(RadioDialogType.VOX, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.Headset, label = "MONI", isActive = state.isMonitorEnabled, onClick = { if (state.isMonitorEnabled) onStateChange(state.copy(isMonitorEnabled = false)) else onPendingDialogChange(RadioDialogType.MONI, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { onStateChange(state.copy(isDspEnabled = !state.isDspEnabled)) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)) }) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Lista de Operadores
                Text("OPERADORES EN ZONA", color = Color.White.copy(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())
                LazyRow(modifier = Modifier.fillMaxWidth().height(100.dp), contentPadding = PaddingValues(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Primero mostrarte a ti mismo (si estás en la lista)
                    val me = users.find { it.nick.trim().uppercase() == nick.trim().uppercase() }
                    if (me != null) {
                        item {
                            UserCard(
                                user = me,
                                isMe = true,
                                onFriendToggle = { },
                                onPrivateChat = { },
                                onReport = { },
                                onBlock = { },
                                onAvatarClick = { }
                            )
                        }
                    }

                    // Luego el resto de usuarios filtrados, excluyéndote a ti
                    items(users.filter { 
                        it.city.split("-")[0] == state.city.split("-")[0] && 
                        it.nick.trim().uppercase() != nick.trim().uppercase() 
                    }) { user -> 
                        UserCard(
                            user = user, 
                            isMe = false, 
                            onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, 
                            onPrivateChat = { }, 
                            onReport = { onReport(user.id) }, 
                            onBlock = { onBlock(user.id) },
                            onAvatarClick = { onPendingDialogChange(RadioDialogType.USER_ACTIONS, user.id) }
                        ) 
                    }
                }
            }

            // --- BLOQUE INFERIOR FIJO (PTT) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.5f))
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(0.1f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(16.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(84.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val pttColor = if(isTransmitting) Color.Red else if(rx) Color.Green else LuxeColors.Gold
                    Surface(
                        modifier = Modifier.weight(1.5f).fillMaxHeight().scale(pttScale).pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (!isPttBlockedByRx) {
                                        pttLocked = true
                                        try { awaitRelease() } finally { pttLocked = false }
                                    }
                                }
                            )
                        }, 
                        shape = RoundedCornerShape(20.dp), 
                        color = pttColor.copy(0.15f), 
                        border = BorderStroke(3.dp, pttColor.copy(0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Mic, null, tint = pttColor, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(if(isTransmitting) "AIRE" else if(rx) "RECIBIENDO" else "HABLAR", color = pttColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }
                    
                    Surface(
                        onClick = { pttLocked = !pttLocked; onStateChange(state.copy(isPttLatched = pttLocked)) }, 
                        modifier = Modifier.size(84.dp), 
                        shape = RoundedCornerShape(20.dp), 
                        color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f), 
                        border = BorderStroke(3.dp, if (pttLocked) Color.Red else Color.White.copy(0.15f))
                    ) {
                        Box(contentAlignment = Alignment.Center) { Icon(if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null, tint = if (pttLocked) Color.Red else Color.White.copy(0.4f), modifier = Modifier.size(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniTechLabel(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onClick).padding(2.dp)) {
        Text(label, color = Color.White.copy(0.5f), fontSize = 7.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MiniTacticalIcon(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
        Surface(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
            color = if (isActive) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isActive) LuxeColors.Gold else Color.White.copy(0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (isActive) LuxeColors.Gold else Color.White.copy(0.7f), modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = if (isActive) LuxeColors.Gold else Color.White.copy(0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black)
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
