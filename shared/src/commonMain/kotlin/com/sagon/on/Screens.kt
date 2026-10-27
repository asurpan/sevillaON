package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.1 (PURE RADIO)
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                "CONECTANDO CON TU CIUDAD EN DIRECTO...",
                color = LuxeColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "Glow"
                )
                
                Box(
                    Modifier
                        .size(100.dp)
                        .scale(glowScale)
                        .background(LuxeColors.Gold.copy(0.1f), CircleShape)
                        .blur(30.dp)
                )
                
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.Black,
                    border = BorderStroke(2.dp, LuxeColors.Gold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp).clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("IDENTIFICACIÓN", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("ON AIR SPAIN", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            
            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(0.04f),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    if (nick.isEmpty()) Text("TU INDICATIVO...", color = Color.White.copy(0.2f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = nick,
                        onValueChange = { 
                            // 🛡️ REGLA: MÁXIMO 32 CARACTERES (PARA INDICATIVOS LARGOS)
                            if (it.length <= 32) onNickChange(it.uppercase().filter { c -> c in 'A'..'Z' || c in '0'..'9' || c == ' ' || c == '-' }) 
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                        cursorBrush = SolidColor(LuxeColors.Gold),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { 
                    if (nick.trim().length >= 2) { 
                        if (!hasAcceptedMic) onMicAccept(); onConnect(null) 
                    } 
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold),
                shape = RoundedCornerShape(16.dp),
                enabled = nick.trim().length >= 2
            ) {
                Text("ENTRAR EN LA RADIO", fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 14.sp, color = Color.Black)
            }

            if (totalUsers > 0) {
                Spacer(Modifier.height(24.dp))
                Surface(color = Color.White.copy(0.05f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(LuxeColors.Gold, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("$totalUsers ONLINE", color = Color.White.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioPanel(
    nick: String,
    mic: Float,
    users: List<RemoteUser>,
    myId: String = "",
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

    // 🛡️ ANIMACIÓN MODO DISCRETO (PARPADEO SUAVE)
    val discreteTransition = rememberInfiniteTransition(label = "DiscreteBlink")
    val discreteAlpha by discreteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DiscreteAlpha"
    )

    // ⏪ ANIMACIÓN REPLAY REPRODUCIENDO (PARPADEO RÍTMICO)
    val replayTransition = rememberInfiniteTransition(label = "ReplayBlink")
    val replayAlpha by replayTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ReplayAlpha"
    )

    var hasSeenDiscreteExplain by remember { mutableStateOf(false) }
    val handleDiscreteToggle = {
        val nextVal = !state.isDiscreteModeEnabled
        onStateChange(state.copy(isDiscreteModeEnabled = nextVal))
        vibratePtt()
        triggerUiSound(if (nextVal) "incoming" else "switch")
        if (nextVal && !hasSeenDiscreteExplain) {
            hasSeenDiscreteExplain = true
            onPendingDialogChange(RadioDialogType.DISCRETE, null)
        }
    }

    LaunchedEffect(externalPttBlocked) { if (externalPttBlocked) isPttBlockedByRx = true }
    LaunchedEffect(isPttBlockedByRx) { if (isPttBlockedByRx) { delay(800); isPttBlockedByRx = false } }

    val currentOnStateChange by rememberUpdatedState(onStateChange)
    val currentOnPlaySound by rememberUpdatedState(onPlaySound)
    val currentRadioState by rememberUpdatedState(state)

    // 🛡️ REGLA DE ORO: EL PTT SIEMPRE DETIENE EL ESCANEO Y EL MODO DISCRETO
    val isTransmitting = (pttLocked || externalPtt || isBeeping) && !isPttBlockedByRx
    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            if (state.isScanning || state.isDiscreteModeEnabled) {
                onStateChange(state.copy(isScanning = false, isDiscreteModeEnabled = false))
            }
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

    AppBackground(style = state.backgroundStyle) {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onPendingDialogChange(RadioDialogType.DELETE_DATA, null) }
                    ) {
                        Text("INDICATIVO FIJO", color = LuxeColors.Gold.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Text(if (nick.isNotBlank()) nick else "RADIO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isPlaying = replayProgress > 0f
                        
                        // ⏪ BOTÓN DE REPLAY (ILUMINACIÓN Y PARPADEO RÍTMICO AL REPRODUCIR)
                        Surface(
                            onClick = { onReplay() }, 
                            color = if (isPlaying) LuxeColors.Gold.copy(alpha = 0.35f * replayAlpha) else if (isReplayReady) LuxeColors.Gold.copy(0.20f) else Color.White.copy(0.08f), 
                            shape = CircleShape, 
                            border = BorderStroke(if (isPlaying) 2.dp else 1.dp, if (isPlaying) LuxeColors.Gold.copy(alpha = replayAlpha) else if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.2f)), 
                            modifier = Modifier.size(34.dp).drawBehind {
                                if (isPlaying) {
                                    drawCircle(
                                        color = LuxeColors.Gold.copy(alpha = 0.30f * replayAlpha),
                                        radius = size.maxDimension * (0.5f + 0.2f * replayAlpha),
                                        center = center
                                    )
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isPlaying) {
                                    CircularProgressIndicator(
                                        progress = { replayProgress }, 
                                        modifier = Modifier.fillMaxSize(), 
                                        color = LuxeColors.Gold.copy(alpha = replayAlpha), 
                                        strokeWidth = 2.5.dp
                                    )
                                }
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.History, 
                                    contentDescription = "Replay", 
                                    tint = if (isPlaying) LuxeColors.Gold.copy(alpha = replayAlpha) else if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.7f), 
                                    modifier = Modifier.size(if (isPlaying) 18.dp else 16.dp).scale(if (isPlaying) 0.85f + 0.25f * replayAlpha else 1f)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        // ❓ BOTÓN DE AYUDA Y MANUAL DE USUARIO
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.05f))
                                .border(1.dp, LuxeColors.Gold.copy(0.4f), CircleShape)
                                .clickable { onPendingDialogChange(RadioDialogType.HELP, null) }, 
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.HelpOutline, 
                                contentDescription = "Manual y Soporte", 
                                tint = LuxeColors.Gold, 
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        // ⚙️ BOTÓN DE AJUSTES
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.05f))
                                .border(1.dp, Color.White.copy(0.1f), CircleShape)
                                .clickable { onPendingDialogChange(RadioDialogType.SETTINGS, null) }, 
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 📺 PANTALLA NEXUS (BORDE CELESTE CIAN DEGRADADO ELEGANTE)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color(0xFF020617),
                    border = BorderStroke(
                        width = 1.5.dp, 
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.70f),
                                Color(0xFF0EA5E9).copy(alpha = 0.30f),
                                Color(0xFF00E5FF).copy(alpha = 0.15f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
                        val availableWidth = maxWidth
                        val sideWidth = (availableWidth * 0.16f).coerceIn(48.dp, 60.dp)
                        val buttonSize = (availableWidth * 0.14f).coerceIn(38.dp, 52.dp)
                        val channelFontSize = (availableWidth.value / 14f).coerceIn(20f, 25f).sp
                        val iconSize = (buttonSize.value * 0.6f).dp

                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            // 📊 COLUMNA IZQUIERDA (CONTROLES)
                            Column(Modifier.width(sideWidth), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MiniTechLabel("VOL", "${(state.systemVolume * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.VOLUME_CONTROL, null) }
                                MiniTechLabel("SQL", "${(state.squelch * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.SQUELCH_CONTROL, null) }
                            }

                            // 📻 CUERPO CENTRAL (S-METER Y CANALES)
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp), 
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // 🛡️ ESTADO (AIRE / RX)
                                val showRemoteRx = rx && !isTransmitting && !isBeeping
                                Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = showRemoteRx,
                                        enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.85f),
                                        exit = fadeOut(tween(400)) + scaleOut(targetScale = 0.85f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF10B981).copy(0.2f), Color(0xFF10B981).copy(0.2f), Color.Transparent)))
                                                .border(BorderStroke(1.dp, Color(0xFF10B981).copy(0.4f)), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = transmitterNick?.uppercase() ?: "RECEPTOR",
                                                color = Color(0xFF10B981),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.2.sp
                                            )
                                        }
                                    }
                                    if (!showRemoteRx) {
                                        Text(
                                            text = if (isTransmitting || isBeeping) "AIRE" else "SEÑAL",
                                            color = if (isTransmitting || isBeeping) Color.Red else Color.White.copy(0.12f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                }

                                // 🚨 REGLA DE ORO CB 27MHz: PORTADORA FIJA Y PISOTÓN BASE (NO ALTERAR LÓGICA DE LEDS FIJOS)
                                // 1. En RX la portadora mantiene los LEDs TOTALMENTE FIJOS marcando el nivel RF real del emisor.
                                // 2. Agente / Bot / Base (BASE/CONTROL) transmite SIEMPRE con potencia a tope (50W -> 20 LEDs Fijos S9+30dB).
                                // 3. Pisotón: Las estaciones de mayor potencia se imponen fijando un nivel de LEDs superior.
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(22.dp).padding(vertical = 1.dp), 
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    val isControlBase = transmitterNick?.startsWith("BASE ") == true || 
                                                         transmitterNick?.startsWith("CONTROL ") == true || 
                                                         transmitterNick?.contains("BASE", ignoreCase = true) == true

                                    val remoteUser = users.find { it.nick.equals(transmitterNick, ignoreCase = true) }
                                    val remotePower = if (isControlBase) 1.0f else (remoteUser?.txPower ?: 0.7f)
                                    val sameCity = isControlBase || (remoteUser != null && remoteUser.city.split("-")[0] == state.city.split("-")[0])
                                    val sameChannel = isControlBase || (remoteUser != null && remoteUser.channel == state.channel)

                                    // 📶 ESCALA RIGUROSA CB 27MHz: Portadora fija marcando potencia RF exacta (Pisotón Base 50W S9+30dB = 20 LEDs Fijos)
                                    val cbSignalFactor = when {
                                        isControlBase -> 1.0f // 💥 ESTACIÓN BASE / BOT (50W): PORTADORA FIJA A TOPE (20 LEDs - S9+30dB)
                                        sameChannel && sameCity -> (0.85f + (remotePower * 0.15f)).coerceIn(0.85f, 1.0f) // SARTENAZO SEGÚN POTENCIA
                                        sameCity -> (0.65f + (remotePower * 0.20f)).coerceIn(0.65f, 0.85f) // Mismo pueblo / ciudad
                                        else -> (0.40f + (remotePower * 0.15f)).coerceIn(0.40f, 0.55f) // Otra provincia
                                    }

                                    val signalLevel = if (isTransmitting) mic else if (rx) cbSignalFactor.coerceIn(0.25f, 1.0f) else 0f
                                    repeat(20) { i ->
                                        val isActive = i < (signalLevel * 20)
                                        val ledColor = when {
                                            i > 16 -> Color(0xFFEF4444)
                                            i > 12 -> Color(0xFFFACC15)
                                            else -> LuxeColors.Green
                                        }
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(1.dp)).background(if (isActive) ledColor else Color.White.copy(0.05f))
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(4.dp))
                                
                                // 🔼🔽 SELECTOR DE CANALES RESPONSIVO
                                Row(
                                    modifier = Modifier.fillMaxWidth(), 
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

                                    // 🔼 SUBIR
                                    Surface(
                                        modifier = Modifier.size(buttonSize),
                                        shape = CircleShape,
                                        color = if (state.isInterfaceLocked) Color.White.copy(0.02f) else Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, if (state.isInterfaceLocked) Color.White.copy(0.02f) else Color.White.copy(0.1f))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().combinedClickable(
                                                enabled = !state.isInterfaceLocked,
                                                onClick = {
                                                    onPlaySound("switch")
                                                    if (state.isScanning) onStateChange(state.copy(isScanning = false))
                                                    else {
                                                        val curCh = CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: 1
                                                        val nextCh = if (curCh >= 40) 1 else curCh + 1
                                                        val nextCity = CITY_CHANNELS.entries.groupBy { it.value }.mapValues { it.value.first().key }[nextCh] ?: "SORIA"
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
                                            Icon(Icons.Rounded.KeyboardArrowUp, null, tint = if (state.isInterfaceLocked) Color.White.copy(0.1f) else LuxeColors.Gold.copy(0.6f), modifier = Modifier.size(iconSize))
                                        }
                                    }

                                    // 🛡️ CANAL CENTRAL (ETIQUETA CRISTAL NEÓN ESMERALDA)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            onClick = { onPendingDialogChange(RadioDialogType.SELECT_CITY, null) },
                                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.40f))
                                        ) {
                                            Text(
                                                text = "CH ${CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: "00"}", 
                                                color = Color(0xFF10B981), 
                                                fontSize = channelFontSize, 
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
                                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onShare(state.channel, state.subtone, null, null) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Share,
                                                    contentDescription = "Compartir Canal",
                                                    tint = Color(0xFF00E5FF),
                                                    modifier = Modifier.size(18.dp).graphicsLayer { alpha = blinkAlpha.value }
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = state.city.split("-")[0],
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp,
                                                    maxLines = 1,
                                                    modifier = Modifier.basicMarquee()
                                                )
                                            }
                                        }
                                    }

                                    // 🔽 BAJAR
                                    Surface(
                                        modifier = Modifier.size(buttonSize),
                                        shape = CircleShape,
                                        color = if (state.isInterfaceLocked) Color.White.copy(0.02f) else Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, if (state.isInterfaceLocked) Color.White.copy(0.02f) else Color.White.copy(0.1f))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().combinedClickable(
                                                enabled = !state.isInterfaceLocked,
                                                onClick = {
                                                    onPlaySound("switch")
                                                    if (state.isScanning) onStateChange(state.copy(isScanning = false))
                                                    else {
                                                        val curCh = CITY_CHANNELS[state.city.split("-")[0].uppercase()] ?: 1
                                                        val nextCh = if (curCh <= 1) 40 else curCh - 1
                                                        val nextCity = CITY_CHANNELS.entries.groupBy { it.value }.mapValues { it.value.first().key }[nextCh] ?: "SORIA"
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
                                            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = if (state.isInterfaceLocked) Color.White.copy(0.1f) else LuxeColors.Gold.copy(0.6f), modifier = Modifier.size(iconSize))
                                        }
                                    }
                                }
                            }

                            // 📊 COLUMNA DERECHA (MODO OREJA, BLOQUEO Y WATTS)
                            Column(Modifier.width(sideWidth), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                                Icon(
                                    imageVector = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing,
                                    contentDescription = null,
                                    tint = if (state.isDiscreteModeEnabled) Color(0xFF87CEEB).copy(alpha = discreteAlpha) else Color.White.copy(0.35f),
                                    modifier = Modifier.size(24.dp).graphicsLayer {
                                        alpha = if (state.isDiscreteModeEnabled) discreteAlpha else 1f
                                    }.clickable { handleDiscreteToggle() }
                                )
                                Spacer(Modifier.height(10.dp))
                                Icon(
                                    imageVector = if (state.isInterfaceLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                    contentDescription = null,
                                    tint = if (state.isInterfaceLocked) LuxeColors.Gold else Color.White.copy(0.40f),
                                    modifier = Modifier.size(22.dp).clickable { onStateChange(state.copy(isInterfaceLocked = !state.isInterfaceLocked)) }
                                )
                                Spacer(Modifier.height(4.dp))
                                val isControlBaseActive = rx && (transmitterNick?.startsWith("BASE ") == true || 
                                                             transmitterNick?.startsWith("CONTROL ") == true || 
                                                             transmitterNick?.contains("BASE", ignoreCase = true) == true)
                                Text("WATTS", color = Color.White.copy(0.45f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                Text(
                                    text = "${if(isTransmitting) (state.veteranPower * 50f).toInt() else if(isControlBaseActive) 50 else if(rx) 9 else 0}", 
                                    color = if(isTransmitting || isControlBaseActive) Color.Red else if(rx) LuxeColors.Gold else Color.White.copy(0.3f), 
                                    fontSize = 20.sp, 
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.combinedClickable(
                                        onClick = { onPendingDialogChange(RadioDialogType.WATTS, null) },
                                        onLongClick = { onPendingDialogChange(RadioDialogType.DIAGNOSTIC, null) }
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Funciones Avanzadas (Minimizadas por defecto)
                var showTools by remember { mutableStateOf(false) }
                Row(modifier = Modifier.fillMaxWidth().clickable { showTools = !showTools }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("FUNCIONES AVANZADAS", color = LuxeColors.Gold.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Icon(if (showTools) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(16.dp))
                }

                AnimatedVisibility(visible = showTools) {
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { MiniTacticalIcon(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { handleDiscreteToggle() }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) onStateChange(state.copy(isVoxEnabled = false)) else onPendingDialogChange(RadioDialogType.VOX, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.Headset, label = "MONI", isActive = state.isMonitorEnabled, onClick = { if (state.isMonitorEnabled) onStateChange(state.copy(isMonitorEnabled = false)) else onPendingDialogChange(RadioDialogType.MONI, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { onPendingDialogChange(RadioDialogType.DSP, null) }) }
                        item { MiniTacticalIcon(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)) }) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Lista de Usuarios en Línea & Radar de Ciudades Activas
                val localUsers = users.filter { it.city.split("-")[0] == state.city.split("-")[0] }
                val otherUsers = users.filter { it.city.split("-")[0] != state.city.split("-")[0] }

                Text("USUARIOS EN LÍNEA", color = Color.White.copy(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())

                if (localUsers.size <= 1 && otherUsers.isNotEmpty()) {
                    val bestActiveCity = otherUsers.groupBy { it.city.split("-")[0] }.maxByOrNull { it.value.size }?.key ?: "MADRID"
                    val totalActiveNationwide = otherUsers.size

                    Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onStateChange(state.copy(city = bestActiveCity, channel = bestActiveCity))
                                onPlaySound("switch")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Rounded.Radar,
                                    contentDescription = "Radar Activo",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🌐 $totalActiveNationwide ESTACIONES EN ESPAÑA",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Modulaciones en vivo en $bestActiveCity",
                                        color = Color.White.copy(0.7f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Surface(
                                color = LuxeColors.Gold,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SINTONIZAR",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else if (localUsers.size <= 1 && otherUsers.isEmpty()) {
                    Surface(
                        color = LuxeColors.Gold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onShare(state.channel, state.subtone, null, null)
                                onPlaySound("switch")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Rounded.CellTower,
                                    contentDescription = "Frecuencia Libre",
                                    tint = LuxeColors.Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "📡 FRECUENCIA LIBRE EN ${state.city.split("-")[0]}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "¡Sé el primero en modular o invita por WhatsApp!",
                                        color = Color.White.copy(0.7f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Surface(
                                color = LuxeColors.Gold,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "INVITAR",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(modifier = Modifier.fillMaxWidth().height(82.dp), contentPadding = PaddingValues(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(localUsers) { user -> 
                            UserCard(
                                user = user, 
                                isMe = (user.id == myId), 
                                onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, 
                                onPrivateChat = { }, 
                                onReport = { onReport(user.id) }, 
                                onBlock = { onBlock(user.id) },
                                onAvatarClick = { onPendingDialogChange(RadioDialogType.USER_ACTIONS, user.id) }
                            ) 
                        }
                    }
                }
            }

            // --- BLOQUE INFERIOR FIJO (PTT + CANDADO DE BLOQUEO) ---
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val pttColor = if (isTransmitting) Color(0xFFEF4444) else LuxeColors.Gold
                    val pttGradient = if (isTransmitting) {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF991B1B), Color(0xFF450A0A), Color(0xFF180202)),
                            radius = 220f
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                pttColor.copy(0.35f),
                                Color(0xFF0891B2).copy(0.20f),
                                Color(0xFF020617).copy(0.90f)
                            ),
                            radius = 240f
                        )
                    }

                    val haptic = LocalHapticFeedback.current
                    Surface(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .scale(pttScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (!isPttBlockedByRx) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            vibratePtt()
                                            pttLocked = true
                                            try { 
                                                awaitRelease() 
                                            } finally { 
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                pttLocked = false 
                                            }
                                        }
                                    }
                                )
                            }, 
                        shape = RoundedCornerShape(22.dp), 
                        color = Color.Transparent, 
                        border = BorderStroke(2.5.dp, Brush.verticalGradient(listOf(pttColor, pttColor.copy(0.3f))))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(pttGradient)
                                .drawBehind {
                                    // 📻 TEXTURA MICRO-REJILLA TÁCTICA DE WALKIE
                                    val gridColor = pttColor.copy(0.08f)
                                    var x = 0f
                                    val step = 8.dp.toPx()
                                    while (x < size.width) {
                                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                                        x += step
                                    }
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                                        y += step
                                    }
                                    // 🌟 GLOSSY SHINE SUPERIOR
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            listOf(Color.White.copy(0.15f), Color.Transparent),
                                            startY = 0f,
                                            endY = size.height * 0.45f
                                        )
                                    )
                                }, 
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Mic, null, tint = pttColor, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (isTransmitting) "AIRE" else "HABLAR", 
                                    color = if (isTransmitting) Color.White else pttColor, 
                                    fontWeight = FontWeight.Black, 
                                    fontSize = 16.sp,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    }
                    
                    Surface(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vibratePtt()
                            pttLocked = !pttLocked
                            onStateChange(state.copy(isPttLatched = pttLocked)) 
                        }, 
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
    Column(modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 2.dp)) {
        Text(label, color = LuxeColors.Gold.copy(0.7f), fontSize = 8.5.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
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
