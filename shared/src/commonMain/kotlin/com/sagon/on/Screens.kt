package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 1.7 (TACTICAL DOCK FIX)
 * 
 * Gestiona el renderizado de la pantalla de Bienvenida, Carga y Radio.
 * Blindado contra modificaciones estructurales en el flujo de navegación.
 * PROHIBIDO ALTERAR BOTONES, ICONOS O LÓGICA DE DOCK SIN NIVEL 0.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource
import on.shared.generated.resources.Res
import on.shared.generated.resources.hero_city
import on.shared.generated.resources.logo
import on.shared.generated.resources.moto
import on.shared.generated.resources.ciclismo
import on.shared.generated.resources.montana
import on.shared.generated.resources.senderismo
import on.shared.generated.resources.socorrista

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
            Text(
                "Sincronizando con la red nacional",
                color = Color.White.copy(0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    nick: String, 
    onNickChange: (String) -> Unit, 
    totalUsers: Int, 
    activeUsers: List<RemoteUser> = emptyList(), 
    onInstall: () -> Unit = {}, 
    hasAcceptedMic: Boolean,
    onMicAccept: () -> Unit,
    onMicRequest: (Boolean, Float) -> Unit,
    onConnect: (String?) -> Unit,
    onShowRadar: () -> Unit = {},
    isNightMode: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EliteWelcome")
    var startGenre by remember { mutableStateOf<String?>(null) }
    
    // --- 🎵 GESTIÓN DE MÚSICA DE INTRO (DESACTIVADA) ---
    LaunchedEffect(Unit) {
        stopIntroMusic()
    }

    val background = if (isNightMode) LuxeColors.NightGradient else LuxeColors.BackgroundGradient

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        // --- 🌌 FONDO DINÁMICO (ESTRELLAS) ---
        StarryBackground(activity = 0.2f)

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 🌀 LOGO FLOTANTE ELITE ---
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
            Text(
                "IDENTIFICACIÓN", 
                color = LuxeColors.Gold, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 4.sp
            )
            Text(
                "ON AIR SPAIN", 
                color = Color.White, 
                fontSize = 28.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(40.dp))

            // --- 🧪 INPUT GLASSMORPHISM ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(0.03f),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (nick.isEmpty()) {
                        Text("TU INDICATIVO...", color = Color.White.copy(0.2f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    BasicTextField(
                        value = nick,
                        onValueChange = { 
                            if (it.length <= 15) {
                                val filtered = it.uppercase().filter { c -> 
                                    c in 'A'..'Z' || c in '0'..'9' || c == ' ' || c == '-' 
                                }
                                onNickChange(filtered)
                            }
                        },
                        textStyle = TextStyle(
                            color = Color.White, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 2.sp
                        ),
                        cursorBrush = SolidColor(LuxeColors.Gold),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { 
                    if (nick.isNotBlank()) {
                        // 🛡️ ACCIÓN QUIRÚRGICA: Aceptamos micro y conectamos en un solo paso
                        if (!hasAcceptedMic) onMicAccept()
                        onConnect(startGenre)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold),
                shape = RoundedCornerShape(20.dp),
                enabled = nick.isNotBlank()
            ) {
                Text(
                    "ENTRAR EN LA RADIO",
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 1.sp,
                    color = Color.Black
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Info de usuarios activos con estilo minimal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = { onShowRadar() },
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.Radar, null, tint = LuxeColors.Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "VER RADAR", 
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                if (totalUsers > 0) {
                    Spacer(Modifier.width(12.dp))

                    Surface(
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(6.dp).background(LuxeColors.Gold, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$totalUsers ONLINE", 
                                color = Color.White.copy(0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
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
    isCodedRx: Boolean,
    voxActiveExternal: Boolean,
    onNoise: (Float) -> Unit,
    onMic: (Boolean, Float) -> Unit,
    onInstall: () -> Unit,
    onShare: (String, String, String?, String?) -> Unit,
    onExit: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onMinimizeRequest: () -> Unit,
    state: RadioState,
    onStateChange: (RadioState) -> Unit,
    externalPtt: Boolean,
    externalPttBlocked: Boolean,
    replayProgress: Float,
    isReplayReady: Boolean,
    chatMessages: List<ChatMessage>,
    forceChatOpen: Boolean,
    forceChatTarget: String?,
    onChatOpenConsumed: () -> Unit,
    onChatTargetConsumed: () -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onDeleteMessage: (String, String?) -> Unit,
    onPrivateChat: (String) -> Unit,
    onPublicChat: () -> Unit,
    onNotification: (AppNotification) -> Unit,
    onReport: (String) -> Unit,
    onBlock: (String) -> Unit,
    onReplay: () -> Unit,
    onGpsRequestPro: (callback: (String?) -> Unit) -> Unit,
    onShowHelp: () -> Unit,
    audioIntegrity: Boolean,
    onAntennaTest: (Boolean) -> Unit,
    bgStationName: String?,
    onBgRadioScan: (String, String) -> Unit,
    onBgRadioStop: () -> Unit,
    onBgVolumeChange: (Float) -> Unit,
    onBgGenreChange: (String) -> Unit,
    onVirtualOperatorTrigger: () -> Unit,
    showExitConfirmExternal: Boolean,
    onExitConfirmDismiss: () -> Unit,
    pendingDialog: RadioDialogType?,
    onPendingDialogChange: (RadioDialogType?) -> Unit,
    radarActivo: Boolean = false,
    radarNivel: Float = 0f,
    onHertzSentinelRequest: () -> Unit = {},
    onActivityPanelRequest: () -> Unit = {}
) {
    var pttLocked by remember { mutableStateOf(state.isPttLatched) }
    var privateChatTarget by remember { mutableStateOf<String?>(null) }
    var isPttBlockedByRx by remember { mutableStateOf(false) }
    var isCurrentTouchDenied by remember { mutableStateOf(false) } // 🛡️ Bloquea el dedo actual tras colisión

    val infiniteTransition = rememberInfiniteTransition(label = "RadioElite")
    val mainScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val chatFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isChatVisible) {
        if (state.isChatVisible) {
            mainScrollState.animateScrollTo(0)
            delay(300)
            try { chatFocusRequester.requestFocus() } catch(e: Exception) {}
        }
    }

    // 🛡️ Silenciar el bot automático al entrar a la radio para evitar interrupciones mientras se está a la escucha
    LaunchedEffect(Unit) {
        if (state.isSystemVoiceEnabled) {
            onStateChange(state.copy(isSystemVoiceEnabled = false))
        }
    }

    LaunchedEffect(externalPtt) {
        if (externalPtt != pttLocked) {
            pttLocked = externalPtt
        }
    }

    LaunchedEffect(externalPttBlocked) {
        if (externalPttBlocked) {
            isPttBlockedByRx = true
        }
    }

    LaunchedEffect(isPttBlockedByRx) {
        if (isPttBlockedByRx) {
            delay(800)
            isPttBlockedByRx = false
        }
    }

    var meterJitter by remember { mutableStateOf(0f) }
    var pttTimer by remember { mutableStateOf(0L) }
    var currentChatMessage by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    var voiceModulation by remember { mutableStateOf(0f) }
    val pttInteractionSource = remember { MutableInteractionSource() }
    val isPttPressed by pttInteractionSource.collectIsPressedAsState()
    val pttScale by animateFloatAsState(
        targetValue = if (isPttPressed) 0.97f else 1f, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PTTScale"
    )

    var isEqualizerVisible by remember { mutableStateOf(false) }
    var isMasterControlsVisible by remember { mutableStateOf(false) }

    val chatListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(forceChatOpen, forceChatTarget) {
        if (forceChatOpen) {
            if (forceChatTarget != null) {
                privateChatTarget = forceChatTarget
                onPrivateChat(forceChatTarget)
            } else {
                privateChatTarget = null
                onPublicChat()
            }
            onStateChange(state.copy(isChatVisible = true))
            onChatOpenConsumed()
            onChatTargetConsumed()
        }
    }

    LaunchedEffect(chatMessages.size, state.isChatVisible) {
        if (state.isChatVisible && chatMessages.isNotEmpty()) {
            delay(100)
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    var myDynamicPower by remember { mutableStateOf(state.veteranPower) }
    val effectivePtt = (isPttPressed && !isCurrentTouchDenied || pttLocked || voxActiveExternal) && state.hasAcceptedMicExplain
    val isTransmitting = ((effectivePtt && !isPttBlockedByRx) || isBeeping) && pttTimer < 90

    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            myDynamicPower = state.veteranPower 
            while (isTransmitting) {
                delay(1000)
                myDynamicPower = (myDynamicPower + 0.005f).coerceIn(state.veteranPower, 1.0f)
                if (myDynamicPower >= state.veteranPower + 0.01f) {
                    onStateChange(state.copy(
                        veteranPower = (state.veteranPower + 0.001f).coerceIn(0.7f, 0.95f),
                        lastActiveTimestamp = 0L 
                    ))
                }
            }
        } else {
            myDynamicPower = state.veteranPower
        }
    }

    val competitors = remember(users, state.city, state.channel, state.subtone, isTransmitting) {
        users.filter { 
            it.city == state.city && 
            it.channel == state.channel && 
            it.subtone == state.subtone && 
            it.isTransmitting &&
            it.nick != nick
        }
    }

    val isBeingSteppedOn = isTransmitting && competitors.isNotEmpty()
    val isReceivingCollision = !isTransmitting && competitors.size > 1

    var portadoraOffenseTimer by remember { mutableStateOf(0) }

    LaunchedEffect(isTransmitting, mic) {
        if (isTransmitting) {
            if (mic < 0.03f) { 
                delay(1000)
                portadoraOffenseTimer++
                if (portadoraOffenseTimer >= 60) { 
                    onPendingDialogChange(RadioDialogType.PORTADORA)
                    if (portadoraOffenseTimer % 5 == 0) triggerUiSound("siren")
                }
                if (portadoraOffenseTimer >= 120) { 
                    onStateChange(state.copy(veteranPower = 0.7f))
                    onLogoutConfirm()
                }
            } else {
                portadoraOffenseTimer = 0
            }
        } else {
            portadoraOffenseTimer = 0
        }
    }

    LaunchedEffect(isBeingSteppedOn, isReceivingCollision) {
        if (isBeingSteppedOn || isReceivingCollision) {
            triggerUiSound("static") 
            delay(500)
        }
    }
    
    val noiseVol = if (!rx && !isTransmitting) (if (state.squelch > state.rfGain) 0f else (state.rfGain - state.squelch)).coerceIn(0f, 1f) else 0f
    val mappedUsers = users.map { it.copy(isFriend = state.friends.contains(it.nick)) }

    LaunchedEffect(rx) { if (rx) { while (true) { voiceModulation = ((-5..5).random() / 500f); delay(100) } } else { voiceModulation = 0f } }

    val currentOnMic by rememberUpdatedState(onMic)
    val currentOnNoise by rememberUpdatedState(onNoise)
    
    LaunchedEffect(effectivePtt, myDynamicPower) { 
        currentOnMic(effectivePtt, myDynamicPower)
    }
    
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { 
        currentOnNoise(noiseVol) 
    }

    val currentOnStateChange by rememberUpdatedState(onStateChange)
    val currentState by rememberUpdatedState(state)

    LaunchedEffect(state.isScanning) {
        if (state.isScanning) {
            if (noiseVol > 0.1f) { onNotification(AppNotification("SQUELCH", "Cierra el filtro para escanear.", NotificationType.Warning)); currentOnStateChange(currentState.copy(isScanning = false)); return@LaunchedEffect }
            while (true) {
                val loopState = currentState
                if (!loopState.isScanning) break
                delay(350)
                if (mappedUsers.any { it.isTransmitting && it.city == loopState.city }) { delay(5000) } 
                else { 
                    triggerUiSound("static") 
                    val nextCity = SPAIN_CITIES[(SPAIN_CITIES.indexOf(loopState.city).coerceAtLeast(0) + 1) % SPAIN_CITIES.size]
                    currentOnStateChange(loopState.copy(city = nextCity)) 
                }
            }
        }
    }

    LaunchedEffect(effectivePtt) { if (effectivePtt && state.isScanning) onStateChange(state.copy(isScanning = false)) }
    
    LaunchedEffect(effectivePtt, isBeeping) {
        if (effectivePtt || isBeeping) {
            pttTimer = 0
            while ((effectivePtt || isBeeping) && pttTimer < 90) {
                delay(1000)
                pttTimer++
            }
        } else {
            if (pttTimer > 0 && state.isRogerBeepEnabled) {
                // El Roger Beep ya se dispara atómicamente en el motor de audio al soltar PTT
                // Se elimina la llamada redundante para evitar desincronización
            }
            pttTimer = 0
        }
    }

    // --- 🔄 LÓGICA DE PULL-TO-REFRESH ELIMINADA (SOLICITUD USUARIO) ---
    
    Box(modifier = Modifier.fillMaxSize().background(EliteTheme.DeepGradient)) {
        StarryBackground(activity = if (isTransmitting || rx) 0.6f else 0.15f, isEcoMode = state.isEcoMode)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // --- 🏷️ HEADER ELITE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onExit) { Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color.Red.copy(0.6f)) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SELECT_CITY) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tu indicativo: ", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(nick, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🔄 REPLAY 15s (Compacto en Header)
                    Surface(
                        onClick = { if (!state.isInterfaceLocked && isReplayReady) onReplay(); triggerUiSound("click") },
                        color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (replayProgress > 0f) {
                                CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp, trackColor = Color.Transparent)
                            }
                            Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.05f))
                            .border(1.dp, Color.White.copy(0.1f), CircleShape)
                            .clickable { onPendingDialogChange(RadioDialogType.SETTINGS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- 📟 PANTALLA DIGITAL ELITE "NEXUS" ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // 🛡️ FIX: AUMENTADA ALTURA PARA QUE QUEPA EL SELECTOR DE BARRIOS
                    .clip(RoundedCornerShape(32.dp)),
                color = Color.Black.copy(0.6f),
                border = BorderStroke(2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // --- 🛰️ MOTOR DE SEÑAL DINÁMICO (VFD GLOW) ---
                    val qrmIntensity = if (state.rfGain > state.squelch) (state.rfGain - state.squelch) else 0f
                    CentinelMonitor(
                        state = state,
                        isTransmitting = isTransmitting,
                        rx = rx,
                        level = if (isTransmitting || rx) mic else qrmIntensity,
                        showLeds = false,
                        modifier = Modifier.fillMaxSize().alpha(0.5f)
                    )

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- 🏷️ INFORMACIÓN PRINCIPAL (Layout Profesional) ---
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.width(70.dp), horizontalAlignment = Alignment.Start) {
                                TechLabel("SQUELCH", "${(state.squelch * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.HELP_SQUELCH)
                                }
                                Spacer(Modifier.height(16.dp))
                                TechLabel("GANANCIA", "${(state.rfGain * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.HELP_GAIN)
                                }
                                // 🛡️ ETIQUETA VOX ELIMINADA (REDUNDANTE)
                            }

                            // Pantalla Central (Identidad)
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val statusText = if (rx) "RECIBIENDO..." else if(isTransmitting) "EMITIENDO..." else "EN ESPERA"
                                val statusColor = if (rx) Color(0xFF22D3EE) else if(isTransmitting) Color.Red else Color.White.copy(0.2f)
                                
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(Modifier.height(8.dp))

                                // --- 🚥 VÚMETRO DE LEDS CENTRADO (Luxe Edition) ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val activeLevel = if(isTransmitting || rx) mic else qrmIntensity
                                    repeat(12) { i ->
                                        val isActive = i < (activeLevel * 12)
                                        val ledColor = when {
                                            i > 9 -> Color.Red
                                            i > 7 -> Color(0xFFFACC15)
                                            else -> if(rx) Color(0xFF22D3EE) else Color(0xFF4ADE80)
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (isActive) ledColor else Color.White.copy(0.06f))
                                                .border(1.dp, if (isActive) ledColor.copy(0.3f) else Color.White.copy(0.02f), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                
                                val displayChannel = if (state.channel == "GENERAL") "ENTRAR EN BARRIO, PUEBLO O ACTIVIDAD" else state.channel
                                // 🔒 NEXUS SWAP: Ciudad (Canal) arriba, Sala (Barrio) debajo
                                Text(
                                    text = state.city,
                                    color = if(rx) Color(0xFF22D3EE) else Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee()
                                        .clickable { 
                                            if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SELECT_CITY) 
                                        },
                                    style = TextStyle(shadow = Shadow(color = statusColor.copy(0.3f), blurRadius = 10f))
                                )
                                
                                Spacer(Modifier.height(8.dp))

                                // --- 🏷️ SELECTOR DE BARRIO / SALA (Rediseñado para que quepa todo) ---
                                Surface(
                                    onClick = { 
                                        if (!state.isInterfaceLocked) { 
                                            if (!state.hasAcceptedMicExplain) onPendingDialogChange(RadioDialogType.MIC_REQUEST) 
                                            else onPendingDialogChange(RadioDialogType.CREATE_CHANNEL)
                                        } 
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), // 🛡️ Menos padding lateral para ganar espacio
                                    color = LuxeColors.Gold.copy(0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp) // 🛡️ Padding interno reducido
                                    ) {
                                        Icon(
                                            if(rx) Icons.Rounded.Person else Icons.Rounded.Home, 
                                            null, 
                                            tint = LuxeColors.Gold, 
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if(rx) (transmitterNick ?: "ANÓNIMO") else displayChannel,
                                            color = Color.White,
                                            fontSize = 11.sp, // 🛡️ Un punto menos para asegurar que cabe "PUEBLO"
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.sp, // 🛡️ Sin espaciado extra para ganar píxeles
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee()
                                        )
                                    }
                                }
                            }

                            // Panel Lateral Derecho (Power/Watts)
                            Column(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPendingDialogChange(RadioDialogType.WATTS) },
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("POTENCIA", color = Color.White.copy(0.3f), fontSize = 7.sp, fontWeight = FontWeight.Black)
                                val wText = if (isTransmitting) "${(myDynamicPower * 15f).toInt()} W" else if(rx) "9.2 W" else "0.0 W"
                                Text(
                                    wText, 
                                    color = if(isTransmitting) Color.Red else if(rx) Color(0xFF22D3EE) else Color.White.copy(0.2f), 
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Black
                                )
                                
                                Spacer(Modifier.height(20.dp))
                                // Nexus Side Panel now focused purely on Power and Status
                            }
                        }

                        // --- 📊 BARRA DE ESTADO INFERIOR ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(0.1f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                }
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtono
                            Surface(
                                onClick = { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SUBTONO) },
                                color = if(state.subtone != "0000") LuxeColors.Gold.copy(0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                border = if(state.subtone != "0000") BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)) else null
                            ) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if(state.subtone == "0000") Icons.Rounded.LockOpen else Icons.Rounded.Lock, null, tint = if(state.subtone != "0000") LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if(state.subtone == "0000") "CÓD. PRIVADO: OFF" else "CÓDIGO: ${state.subtone}", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }

                                // --- 🔐 TACTICAL DOCK: CONTROLES RÁPIDOS (VOX, BEEP, ECO, MONI, DISCRETO, SHARE) ---
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End), 
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 🎤 VOX
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.Mic,
                                        label = "VOX",
                                        isActive = state.isVoxEnabled,
                                        onClick = { 
                                            if (state.isVoxEnabled) {
                                                onStateChange(state.copy(isVoxEnabled = false))
                                                triggerUiSound("switch")
                                            } else {
                                                onPendingDialogChange(RadioDialogType.VOX)
                                                triggerUiSound("click")
                                            }
                                        }
                                    )

                                    // 🔔 ROGER BEEP
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.MusicNote,
                                        label = "BEEP",
                                        isActive = state.isRogerBeepEnabled,
                                        onClick = { 
                                            onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled))
                                            triggerUiSound("switch")
                                        }
                                    )

                                    // 🌀 ECO
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.SettingsInputAntenna,
                                        label = "ECO",
                                        isActive = state.isReverbEnabled,
                                        onClick = { 
                                            if (state.isReverbEnabled) {
                                                onStateChange(state.copy(isReverbEnabled = false))
                                                triggerUiSound("switch")
                                            } else {
                                                onPendingDialogChange(RadioDialogType.REVERB)
                                                triggerUiSound("click")
                                            }
                                        }
                                    )

                                    // 🎚️ DSP
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.GraphicEq,
                                        label = "DSP",
                                        isActive = state.isDspEnabled,
                                        onClick = { 
                                            if (state.isDspEnabled) {
                                                onStateChange(state.copy(isDspEnabled = false))
                                                triggerUiSound("switch")
                                            } else {
                                                onPendingDialogChange(RadioDialogType.DSP)
                                                triggerUiSound("click")
                                            }
                                        }
                                    )

                                    // 🎧 MONITOR
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.Headset,
                                        label = "MONI",
                                        isActive = state.isMonitorEnabled,
                                        onClick = { 
                                            if (state.isMonitorEnabled) {
                                                onStateChange(state.copy(isMonitorEnabled = false))
                                                triggerUiSound("switch")
                                            } else {
                                                onPendingDialogChange(RadioDialogType.MONI)
                                                triggerUiSound("click")
                                            }
                                        }
                                    )

                                    // 👂 DISCRETO
                                    TacticalDockIcon(
                                        icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing,
                                        label = "DISC",
                                        isActive = state.isDiscreteModeEnabled,
                                        onClick = { 
                                            onPendingDialogChange(RadioDialogType.DISCRETE)
                                            triggerUiSound("click")
                                        }
                                    )

                                    // 📢 COMPARTIR
                                    TacticalDockIcon(
                                        icon = Icons.Rounded.Share,
                                        label = "SHARE",
                                        isActive = false,
                                        onClick = { onShare(state.channel, state.subtone, state.myProRole, null); triggerUiSound("click") }
                                    )
                                }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 🎛️ DOCK DE CONTROLES RÁPIDOS (TRI-MODO) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EliteControlTile(
                    modifier = Modifier.weight(1f).pointerInput(bgStationName) {
                        detectTapGestures(
                            onTap = { onPendingDialogChange(RadioDialogType.FMSCAN) },
                            onLongPress = { if (bgStationName != null) onBgRadioStop() else onBgRadioScan(state.city, state.bgRadioGenre) }
                        )
                    },
                    icon = Icons.Rounded.Radio,
                    label = "RADIO FM",
                    status = bgStationName ?: "OFF",
                    isActive = bgStationName != null
                )

                EliteControlTile(
                    modifier = Modifier.weight(1f).combinedClickable(
                        onClick = {
                            if (state.activeProfile == ActivityProfile.NORMAL) {
                                onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR)
                            } else {
                                onActivityPanelRequest()
                            }
                            triggerUiSound("click")
                        },
                        onLongClick = {
                            if (state.activeProfile != ActivityProfile.NORMAL) {
                                onPendingDialogChange(RadioDialogType.FINISH_ACTIVITY_CONFIRM)
                                triggerUiSound("click")
                            }
                        }
                    ), 
                    icon = if(state.activeProfile != ActivityProfile.NORMAL) Icons.Rounded.Route else Icons.Rounded.Groups, 
                    label = "EQUIPO RUTA", 
                    status = if(state.activeProfile != ActivityProfile.NORMAL) state.activeProfile.name else "INICIAR",
                    isActive = state.activeProfile != ActivityProfile.NORMAL,
                    progress = if(state.activeProfile != ActivityProfile.NORMAL) 1f else 0f
                )

                EliteControlTile(
                    modifier = Modifier.weight(1f).clickable { onStateChange(state.copy(isChatVisible = !state.isChatVisible)); if(!state.isChatVisible) onPublicChat() }, 
                    icon = Icons.AutoMirrored.Rounded.Chat, 
                    label = "CHAT VOZ", 
                    status = if(state.unreadCount > 0) "${state.unreadCount} NUEVOS" else "${chatMessages.size} MSGS",
                    isActive = state.unreadCount > 0 || chatMessages.isNotEmpty(),
                    progress = if(state.unreadCount > 0) 1f else 0f
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- 🎤 PTT ELITE & LOCK ---
            Row(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BOTÓN PTT PRINCIPAL
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(if (isTransmitting) 0.97f else 1f)
                        .pointerInput(state.isInterfaceLocked) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (isPttBlockedByRx || rx) {
                                        // 🛡️ Si ya hay alguien hablando, invalidamos este toque desde el inicio
                                        isCurrentTouchDenied = true
                                        triggerUiSound("static")
                                    }

                                    if (!isPttBlockedByRx && !state.isInterfaceLocked && !rx) {
                                        isCurrentTouchDenied = false
                                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                        pttInteractionSource.emit(press)
                                        try {
                                            tryAwaitRelease()
                                        } finally {
                                            pttInteractionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                            isCurrentTouchDenied = false // Reset al levantar
                                        }
                                    } else {
                                        // Esperar a que suelte el dedo aunque esté bloqueado para limpiar el estado
                                        tryAwaitRelease()
                                        isCurrentTouchDenied = false
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(35.dp),
                    color = if (isTransmitting) Color.Red.copy(0.2f) else if (rx) Color.Green.copy(0.15f) else Color.White.copy(0.05f),
                    border = BorderStroke(2.dp, if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White.copy(0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isTransmitting || rx) {
                            val waveColor = if (isTransmitting) Color.Red else Color.Green
                            repeat(3) { i ->
                                val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.8f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "Wave")
                                val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "WaveAlpha")
                                Box(Modifier.fillMaxSize().scale(scale).border(2.dp, waveColor.copy(alpha), RoundedCornerShape(35.dp)))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Mic, 
                                null, 
                                tint = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White.copy(0.3f), 
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                if (isTransmitting) "ON AIR" else if (rx) "AIRE: RECIBIENDO" else "PULSAR PARA HABLAR",
                                color = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 15.sp, 
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // BOTÓN PTT LOCK (CANDADO)
                Surface(
                    onClick = { 
                        if (!state.isInterfaceLocked) {
                            pttLocked = !pttLocked
                            onStateChange(state.copy(isPttLatched = pttLocked)) 
                            triggerUiSound("switch")
                        }
                    },
                    modifier = Modifier.size(110.dp),
                    shape = RoundedCornerShape(35.dp),
                    color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f),
                    border = BorderStroke(2.dp, if (pttLocked) Color.Red else Color.White.copy(0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, 
                            null, 
                            tint = if (pttLocked) Color.Red else Color.White.copy(0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 👥 BARRIOS Y SALAS ACTIVAS ---
            val activeRooms = remember(users, state.city, state.channel) {
                val rooms = users.filter { it.city == state.city && it.channel != "GENERAL" }
                    .groupBy { it.channel }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                
                // Si estamos en una sala, añadimos GENERAL al principio para poder volver
                if (state.channel != "GENERAL") {
                    listOf("GENERAL" to 0) + rooms
                } else {
                    rooms
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (activeRooms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeRooms) { (room, count) ->
                            val isCurrent = state.channel == room
                            val isGeneral = room == "GENERAL"
                            
                            Surface(
                                onClick = { if (!state.isInterfaceLocked) onStateChange(state.copy(channel = room)) },
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) LuxeColors.Gold.copy(0.15f) else if (isGeneral) LuxeColors.ElectricBlue.copy(0.1f) else Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, if (isCurrent) LuxeColors.Gold else if (isGeneral) LuxeColors.ElectricBlue.copy(0.4f) else Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (isGeneral) Icon(Icons.Rounded.Home, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(14.dp))
                                    if (isGeneral) Spacer(Modifier.width(8.dp))
                                    
                                    Text(
                                        text = if (isGeneral) "SALIR AL CANAL PÚBLICO" else room, 
                                        color = if (isGeneral) LuxeColors.ElectricBlue else Color.White, 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.Black
                                    )
                                    
                                    if (count > 0) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(Modifier.background(if (isCurrent) LuxeColors.Gold else Color.White.copy(0.2f), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(count.toString(), color = if (isCurrent) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (state.channel == "GENERAL") {
                    Text(
                        "No hay barrios activos. Toca aquí para crear uno.",
                        color = Color.White.copy(0.2f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp).clickable { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.CREATE_CHANNEL) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // --- 👥 ESTACIONES EN ESTE CANAL ---
            Text(if (state.channel == "GENERAL") "CANAL PÚBLICO EN ${state.city}" else "OPERADORES EN BARRIO ${state.channel}", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
            
            val allToShow = remember(nick, isTransmitting, state.city, state.channel, state.subtone, mappedUsers) {
                (listOf(
                    RemoteUser(id = "me", nick = nick, isTransmitting = isTransmitting, city = state.city, channel = state.channel, subtone = state.subtone, isFriend = false),
                    // --- 🤖 BOT DE CORTESÍA (CONTROL DE RED) ---
                    RemoteUser(id = "bot_system", nick = "CONTROL", city = state.city, channel = state.channel, proRole = "SISTEMA", isWorkAvailable = true)
                ) + mappedUsers.filter { it.city == state.city && it.channel == state.channel && it.nick != nick }).sortedByDescending { it.isTransmitting || it.isFriend }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentPadding = PaddingValues(vertical = 12.dp), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allToShow) { user -> 
                    UserCard(
                        user = user, 
                        isMe = user.id == "me",
                        onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, 
                        onPrivateChat = { onPrivateChat(user.nick); onStateChange(state.copy(isChatVisible = true)) },
                        onReport = { onReport(user.id) },
                        onBlock = { onBlock(user.id) },
                        onClick = {
                            if (user.nick == "CONTROL") {
                                onVirtualOperatorTrigger()
                                triggerUiSound("click")
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Spacer(Modifier.height(32.dp))

            Spacer(Modifier.height(180.dp)) // 🛡️ FIX: Aumentado para asegurar que NADA quede bajo el banner
            }
        }

        // --- 🛰️ MINI-VÚMETRO DE VIGILANCIA HERTZ (VERTICAL) ---
        if (radarActivo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 12.dp, bottom = 120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(0.3f))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(radarNivel.coerceIn(0f, 1f))
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color.Red, LuxeColors.Gold, LuxeColors.ElectricBlue)
                                )
                            )
                    )
                }
            }
        }

        // CHAT OVERLAY
        if (state.isChatVisible) {
            Box(modifier = Modifier.fillMaxSize().clickable(enabled = false) { }) {
                EliteChatOverlay(
                    messages = chatMessages,
                    target = privateChatTarget,
                    currentText = currentChatMessage,
                    onTextChange = { currentChatMessage = it },
                    onSend = { 
                        if (currentChatMessage.text.isNotBlank()) {
                            onSendMessage(currentChatMessage.text, privateChatTarget)
                            currentChatMessage = androidx.compose.ui.text.input.TextFieldValue("")
                        }
                    },
                    onClose = { onStateChange(state.copy(isChatVisible = false)) },
                    onDeleteMessage = onDeleteMessage,
                    myNick = nick
                )
            }
        }
    }
}

@Composable
fun EliteControlTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    status: String,
    isActive: Boolean = false,
    progress: Float = 0f
) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isActive || progress > 0f) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.03f),
        border = BorderStroke(1.dp, if (isActive || progress > 0f) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(LuxeColors.Gold.copy(0.15f))
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp), 
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = if (isActive || progress > 0f) LuxeColors.Gold else Color.White.copy(0.4f), 
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1)
                Text(status, color = if (isActive || progress > 0f) LuxeColors.Gold else Color.White.copy(0.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EliteChatOverlay(
    messages: List<ChatMessage>,
    target: String?,
    currentText: androidx.compose.ui.text.input.TextFieldValue,
    onTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
    onDeleteMessage: (String, String?) -> Unit = { _, _ -> },
    myNick: String = ""
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(Modifier.fillMaxSize().background(EliteTheme.DeepGradient).clickable(enabled = false) { }) {
        StarryBackground(activity = 0.2f)
        
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
            // --- 🏷️ HEADER TÁCTICO ---
            Row(
                Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(LuxeColors.Gold.copy(0.1f), CircleShape).border(1.dp, LuxeColors.Gold.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (target != null) Icons.Rounded.VpnKey else Icons.Rounded.Groups, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (target != null) "TERMINAL PRIVADA" else "SALA GENERAL", 
                            color = LuxeColors.Gold, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = target ?: "TODOS LOS OPERADORES", 
                            color = Color.White, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.White.copy(0.05f), CircleShape)
                ) { 
                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.6f)) 
                }
            }
            
            // --- 📜 LISTA DE MENSAJES (STYLE CARDS) ---
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = Color.White.copy(0.05f), modifier = Modifier.size(80.dp))
                        Text(
                            "ESPERANDO ACTIVIDAD EN LA TERMINAL...", 
                            color = Color.White.copy(0.1f), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(messages) { msg ->
                        val isAnuncio = msg.text.startsWith("ANUNCIO:")
                        val isMe = msg.senderNick.trim().uppercase() == myNick.trim().uppercase()
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = {
                                        if (isMe) {
                                            messageToDelete = msg
                                            triggerUiSound("click")
                                        }
                                    }
                                ),
                            color = if (isAnuncio) LuxeColors.Gold.copy(0.12f) else Color.White.copy(0.04f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (isAnuncio) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.08f))
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            msg.senderNick, 
                                            color = if (isAnuncio) LuxeColors.Gold else Color(0xFF22D3EE), 
                                            fontSize = 13.sp, 
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.5.sp
                                        )
                                        if (isAnuncio) {
                                            Spacer(Modifier.width(10.dp))
                                            Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    if (isMe) {
                                        Icon(Icons.Rounded.MoreVert, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    msg.text.replace("ANUNCIO: ", ""), 
                                    color = Color.White, 
                                    fontSize = 17.sp, 
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            // --- 📢 ACCIONES RÁPIDAS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { 
                            if (!currentText.text.contains("ANUNCIO:")) {
                                onTextChange(androidx.compose.ui.text.input.TextFieldValue("ANUNCIO: " + currentText.text))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = LuxeColors.Gold.copy(0.15f),
                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.4f))
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ENVIAR AVISO VOZ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))

                    // --- 😃 SELECTOR DE EMOJIS RÁPIDOS ---
                    Surface(
                        onClick = { showEmojiPicker = !showEmojiPicker },
                        shape = CircleShape,
                        color = if (showEmojiPicker) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
                        border = BorderStroke(1.dp, if (showEmojiPicker) LuxeColors.Gold else Color.White.copy(0.1f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.SentimentSatisfiedAlt, null, tint = if (showEmojiPicker) LuxeColors.Gold else Color.White.copy(0.6f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (currentText.text.isNotBlank()) {
                    TextButton(onClick = { onTextChange(androidx.compose.ui.text.input.TextFieldValue("")) }) {
                        Text("BORRAR TODO", color = Color.Red.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (currentText.text.contains("ANUNCIO:")) {
                Text(
                    "📢 ¡ATENCIÓN! Este mensaje será leído en voz alta por el altavoz de todos los compañeros que estén en este canal. Úsalo para avisos importantes.",
                    color = LuxeColors.Gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Panel de Emojis
            AnimatedVisibility(visible = showEmojiPicker) {
                val emojis = listOf("👍", "😎", "📻", "🏍️", "👋", "🔥", "⚠️", "🆘", "👏", "✅")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(emojis) { emoji ->
                        Surface(
                            onClick = { 
                                val newText = currentText.text + emoji
                                onTextChange(androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newText.length)))
                                triggerUiSound("click")
                            },
                            shape = CircleShape,
                            color = Color.White.copy(0.08f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
            
            // --- ⌨️ INPUT GLASSMORPHISM ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(0.07f),
                border = BorderStroke(1.dp, Color.White.copy(0.12f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (currentText.text.isEmpty()) {
                            Text(
                                "ESCRIBE TU MENSAJE AQUÍ...", 
                                color = Color.White.copy(0.25f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        BasicTextField(
                            value = currentText,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            cursorBrush = SolidColor(LuxeColors.Gold),
                            modifier = Modifier.fillMaxWidth().onKeyEvent {
                                if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                    onSend()
                                    true
                                } else false
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() })
                        )
                    }
                    
                    Surface(
                        onClick = onSend,
                        color = LuxeColors.Gold,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Rounded.Send, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            TextButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CERRAR TERMINAL", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // --- 🗑️ DIÁLOGO DE BORRADO ---
        if (messageToDelete != null) {
            AlertDialog(
                onDismissRequest = { messageToDelete = null },
                containerColor = LuxeColors.DeepSea,
                title = { Text("¿ELIMINAR MENSAJE?", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("Esta acción borrará el mensaje para todos los operadores.", color = Color.White.copy(0.7f)) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteMessage(messageToDelete!!.id, target)
                        messageToDelete = null
                        triggerUiSound("click")
                    }) { Text("BORRAR", color = Color.Red, fontWeight = FontWeight.Black) }
                },
                dismissButton = {
                    TextButton(onClick = { messageToDelete = null }) { Text("CANCELAR", color = Color.White.copy(0.4f)) }
                }
            )
        }
    }
}

@Composable
private fun TechLabel(label: String, value: String, valueColor: Color = Color.White, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(label, color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black) // 🛡️ Más opacidad (0.6f) y tamaño (9.sp)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

object EliteTheme {
    val DeepGradient = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
}

@Composable
fun ActivityPanel(
    nick: String,
    state: RadioState,
    users: List<RemoteUser>,
    voxActive: Boolean,
    rx: Boolean,
    onStateChange: (RadioState) -> Unit,
    onMic: (Boolean, Float) -> Unit,
    onExecuteEngineeringAction: (String) -> Unit,
    onGpsRequest: (callback: (String?) -> Unit) -> Unit,
    onShare: (String, String, String?, String?) -> Unit,
    onPendingDialogChange: (RadioDialogType?) -> Unit,
    onClose: () -> Unit,
    onFinish: () -> Unit
) {
    val profile = state.activeProfile
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var isZoomed by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var newChannelName by remember { mutableStateOf(state.channel) }
    var showRealMap by remember { mutableStateOf(false) } // 🌍 SELECTOR: RADAR vs MAPA REAL
    val scrollState = rememberScrollState()
    var routeKms by remember { mutableStateOf(0.0f) }
    var lastLat by remember { mutableStateOf<Double?>(null) }
    var lastLon by remember { mutableStateOf<Double?>(null) }

    val announcedSites = remember { mutableStateOf(setOf<String>()) }

    // --- 🏛️ PUNTOS DE INTERÉS HISTÓRICO (PRO) ---
    val historicalSites = remember {
        listOf(
            Triple("Giralda de Sevilla", Pair(37.3862, -5.9925), "A tu derecha tienes la Giralda, el antiguo alminar de la mezquita mayor, ahora campanario de la Catedral. Es el símbolo más icónico de Sevilla."),
            Triple("Torre del Oro", Pair(37.3822, -5.9964), "A la orilla del Guadalquivir verás la Torre del Oro. Se construyó en el siglo trece para cerrar el paso al puerto mediante una cadena."),
            Triple("Puerta del Sol", Pair(40.4168, -3.7038), "Estás en el Kilómetro Cero de España. La Puerta del Sol ha sido testigo de la historia de Madrid desde el siglo quince."),
            Triple("Palacio Real de Madrid", Pair(40.4183, -3.7144), "Frente a ti, el Palacio Real más grande de Europa Occidental. Un tesoro del barroco español construido sobre el antiguo Alcázar."),
            Triple("Sagrada Familia", Pair(41.4036, 2.1744), "La obra maestra de Gaudí. Iniciada en mil ochocientos ochenta y dos, es el máximo exponente de la arquitectura modernista mundial."),
            Triple("Alhambra de Granada", Pair(37.1760, -3.5881), "Contemplas la Alhambra, ciudad palatina andalusí y una de las maravillas del mundo. Esencia viva del Reino de Granada."),
            Triple("Mezquita de Córdoba", Pair(37.8789, -4.7794), "Estás ante la Mezquita-Catedral de Córdoba, un conjunto arquitectónico único que resume siglos de arte omeya y gótico."),
            Triple("Acueducto de Segovia", Pair(40.9481, -4.1184), "Observa esta maravilla de la ingeniería romana. Casi dos mil años aguantando el tiempo sin una gota de cemento entre sus piedras."),
            Triple("Alcázar de Segovia", Pair(40.9525, -4.1325), "Este castillo parece sacado de un cuento. Fue residencia real y una de las fortalezas más importantes de Castilla."),
            Triple("Catedral de Santiago", Pair(42.8806, -8.5446), "Meta final del Camino de Santiago. Esta catedral románica guarda los restos del Apóstol y es el corazón espiritual de Europa."),
            Triple("Basílica del Pilar", Pair(41.6567, -0.8784), "A orillas del Ebro se alza el Pilar de Zaragoza, una joya del barroco y lugar sagrado de la Hispanidad."),
            Triple("Ciudad de las Artes", Pair(39.4542, -0.3503), "Valencia futurista. Este complejo arquitectónico de Santiago Calatrava es un referente mundial del diseño contemporáneo."),
            Triple("Teatro Romano de Mérida", Pair(38.9150, -6.3385), "Bienvenido a la antigua Emérita Augusta. Este teatro romano sigue vivo hoy, acogiendo funciones como hace dos mil años."),
            Triple("Guggenheim Bilbao", Pair(43.2685, -2.9340), "El titanio que transformó Bilbao. Esta obra de Frank Gehry es uno de los museos más icónicos del planeta."),
            Triple("Muralla de Ávila", Pair(40.6558, -4.7013), "Estás rodeando el recinto amurallado medieval mejor conservado de Europa. Más de dos kilómetros de historia intacta."),
            Triple("Monasterio del Escorial", Pair(40.5891, -4.1477), "El sueño de Felipe Segundo. Un complejo monumental que fue centro del poder político de la corona española."),
            Triple("Catedral de Burgos", Pair(42.3408, -3.7043), "Pura elegancia gótica. Esta catedral es Patrimonio de la Humanidad y custodia la tumba del Cid Campeador."),
            Triple("Puente Nuevo de Ronda", Pair(36.7408, -5.1661), "Mira hacia abajo. El tajo de Ronda y su puente del siglo dieciocho son una de las estampas más espectaculares de Andalucía."),
            Triple("Alcázar de Toledo", Pair(39.8581, -4.0203), "Dominando la ciudad de las tres culturas, el Alcázar de Toledo ha sido fortaleza, palacio real y academia militar."),
            Triple("Castillo de Bellver", Pair(39.5639, 2.6193), "Único castillo de planta circular en España. Vigía eterno de la bahía de Palma de Mallorca desde el siglo catorce."),
            Triple("Auditorio de Tenerife", Pair(28.4561, -16.2514), "La gran ola blanca de Santa Cruz. Un hito de la arquitectura expresionista frente al Océano Atlántico."),
            Triple("Teatro Romano de Cartagena", Pair(37.5992, -0.9845), "Descubierto casi por azar, este teatro de la antigua Carthago Nova es una joya escondida del Imperio Romano.")
        )
    }

    // --- 🛣️ MOTOR DE KILOMETRAJE REAL (GPS ODOMETER) ---
    LaunchedEffect(state.motoLatitude, state.motoLongitude) {
        val lat = state.motoLatitude
        val lon = state.motoLongitude
        if (lat != null && lon != null) {
            // 1. Odometer (Mantenemos el cuentakms real)
            if (lastLat != null && lastLon != null) {
                val dist = calculateDistanceKms(lastLat!!, lastLon!!, lat, lon)
                if (dist > 0.01) {
                    routeKms += dist.toFloat()
                }
            }
            lastLat = lat
            lastLon = lon

            // 2. Guía Cultural Inteligente (Radio Tour)
            historicalSites.forEach { (name, coords, desc) ->
                if (!announcedSites.value.contains(name)) {
                    val distToSite = calculateDistanceKms(lat, lon, coords.first, coords.second)
                    if (distToSite < 0.5) { // Si estamos a menos de 500 metros
                        announcedSites.value += name
                        onExecuteEngineeringAction("SPEAK|Atención. $desc")
                    }
                }
            }
        }
    }
    
    // --- 🤖 BIENVENIDA DEL LOCUTOR (EXPLICACIÓN BREVE) ---
    LaunchedEffect(Unit) {
        onExecuteEngineeringAction("SHOW_BANNER") // 💰 ACTIVAR PUBLICIDAD ADMOB

        val welcomeMsg = when(profile) {
            ActivityProfile.MOTO -> "Modo Moto activado. Filtro de viento listo y guardián de impactos vigilando."
            ActivityProfile.CICLISMO -> "Modo Ciclismo listo. Si detecto una caída fuerte, avisaré a tus compañeros."
            ActivityProfile.MONTANA -> "Modo Montaña activo. El radar GPS te mantendrá localizado y el audio filtrado para ráfagas."
            ActivityProfile.PASEO -> "Modo Paseo iniciado. Audio natural y ahorro de batería activado."
            ActivityProfile.SENDERISMO -> "Senderismo activo. Filtro de voz optimizado y radar GPS de baja potencia."
            ActivityProfile.CAMIONEROS -> "Atención camionero. Filtro de cabina activado para eliminar el ruido de rodadura."
            ActivityProfile.CARAVANAS -> "Modo Caravana iniciado. Audio suavizado para viajes largos en grupo."
            ActivityProfile.OFFROAD -> "Modo Offroad táctico. Filtro de vibración y radar de alta persistencia."
            ActivityProfile.TACTICO -> "Canal táctico encriptado. Red de malla operativa y audio militar ultra-nítido."
            ActivityProfile.RUNNING -> "Modo Running listo. VOX adaptado a tu respiración y ritmo."
            ActivityProfile.ESQUI -> "Modo Nieve activado. Filtro para viento gélido y eco de montaña."
            ActivityProfile.VELA -> "Navegación activa. Filtro de mar agresivo para eliminar el ruido de las olas y viento marino."
            ActivityProfile.PARAPENTE -> "Vuelo iniciado. Filtro de aire extremo activado. Prioridad absoluta a la voz."
            ActivityProfile.CAZA -> "Modo Caza activo. Micro de alta sensibilidad para susurros y radar silencioso."
            ActivityProfile.PESCA -> "Modo Pesca iniciado. Silencio absoluto de fondo y máxima duración de batería."
            ActivityProfile.KAYAK -> "Kayak en marcha. Filtro de agua y golpes de remo activado."
            else -> "Modo Actividad iniciado. Optimizando audio y GPS para tu ruta."
        }
        
        delay(1000) // Breve pausa tras la animación de entrada
        onExecuteEngineeringAction("SPEAK|$welcomeMsg")
    }
    
    val isTransmitting = isPressed || voxActive

    LaunchedEffect(isPressed) {
        onMic(isPressed, state.veteranPower)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(enabled = false) { }) {
        StarryBackground(activity = 0.4f)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState), // 🛡️ FIX: Restauramos scroll para evitar que el mapa se encoja a cero
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 🏁 HEADER TÁCTICO (COMPACTO) ---
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White.copy(0.6f))
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.channel == "GENERAL") "MODO ACTIVIDAD" else "BARRIO: ${state.channel}",
                        color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        if (state.subtone != "0000") "CÓDIGO PRIVADO: ${state.subtone}" else "RED P2P ACTIVA",
                        color = if (state.subtone != "0000") LuxeColors.Gold.copy(0.7f) else LuxeColors.Green, 
                        fontSize = 7.sp, fontWeight = FontWeight.Bold
                    )
                }
                // --- 🛣️ CONTADOR DE KMS ÉPICO ---
                Column(horizontalAlignment = Alignment.End) {
                    Text("KMS", color = LuxeColors.Gold.copy(0.6f), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "${(routeKms * 10).toInt() / 10.0}", 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Black,
                        style = TextStyle(shadow = Shadow(LuxeColors.Gold.copy(0.3f), blurRadius = 8f))
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- 🗺️ MAPA TÁCTICO (REAL GPS) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp) 
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020617)) // Azul oscuro táctico profundo
                    .border(2.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background layers (Real Map or City Image)
                Box(Modifier.fillMaxSize().alpha(if (state.motoLatitude != null) 1f else 0.8f)) {
                    if (showRealMap) {
                        // --- 🌍 MAPA CARTOGRÁFICO TÁCTICO (CALLEREO REALISTA) ---
                        Canvas(Modifier.fillMaxSize()) {
                            val zoom = if (isZoomed) 2.5f else 1f
                            val center = Offset(size.width / 2, size.height / 2)
                            
                            // 1. Dibujar Manzanas de la Ciudad
                            val blockSize = 80.dp.toPx() * zoom
                            for(x in -5..5) {
                                for(y in -5..5) {
                                    drawRect(
                                        color = Color(0xFF0F172A),
                                        topLeft = Offset(center.x + x * blockSize + 10, center.y + y * blockSize + 10),
                                        size = androidx.compose.ui.geometry.Size(blockSize - 20, blockSize - 20)
                                    )
                                }
                            }

                            // 2. Dibujar Calles y Avenidas
                            val streetColor = Color.White.copy(0.08f)
                            for(i in -5..5) {
                                // Calles Verticales
                                drawLine(streetColor, Offset(center.x + i * blockSize, 0f), Offset(center.x + i * blockSize, size.height), 2.dp.toPx())
                                // Calles Horizontales
                                drawLine(streetColor, Offset(0f, center.y + i * blockSize), Offset(size.width, center.y + i * blockSize), 2.dp.toPx())
                            }

                            // 3. Avenidas Principales (Resaltadas)
                            drawLine(LuxeColors.Gold.copy(0.05f), Offset(center.x, 0f), Offset(center.x, size.height), 4.dp.toPx())
                            drawLine(LuxeColors.Gold.copy(0.05f), Offset(0f, center.y), Offset(size.width, center.y), 4.dp.toPx())
                        }
                        
                        // Capa de Ambientación
                        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))))
                    } else {
                        // --- 📡 CAPA RADAR TÁCTICO CON FONDO SEGÚN DEPORTE ---
                        val backgroundRes = when(profile) {
                            ActivityProfile.MOTO -> Res.drawable.moto
                            ActivityProfile.CICLISMO -> Res.drawable.ciclismo
                            ActivityProfile.MONTANA, ActivityProfile.SENDERISMO -> Res.drawable.montana
                            ActivityProfile.SOCORRISTAS -> Res.drawable.socorrista
                            else -> Res.drawable.hero_city
                        }
                        Image(
                            painter = painterResource(backgroundRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().alpha(0.35f),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(LuxeColors.ElectricBlue.copy(0.2f), BlendMode.Screen)
                        )
                    }
                }

                val infiniteTransition = rememberInfiniteTransition()
                val scanRotation by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing))
                )

                // Radar Drawing Logic with alpha
                Box(Modifier.fillMaxSize().alpha(if (state.motoLatitude != null) 1f else 0.1f)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val baseRadius = size.minDimension / 2.2f
                        val zoomFactor = if (isZoomed) 0.5f else 2.0f // Rango: 0.5km o 2km
                        val radius = baseRadius
                        
                        // --- 🧭 BRÚJULA Y RANGO ---
                        drawCircle(Color.White.copy(0.03f), radius = radius, style = Stroke(1.dp.toPx()))
                        drawCircle(Color.White.copy(0.02f), radius = radius * 0.66f, style = Stroke(1.dp.toPx()))
                        drawCircle(Color.White.copy(0.01f), radius = radius * 0.33f, style = Stroke(1.dp.toPx()))

                        // Líneas cardinales
                        repeat(4) { i ->
                            val angle = i * 90f * (PI / 180f).toFloat()
                            drawLine(
                                color = Color.White.copy(0.05f),
                                start = Offset(center.x + cos(angle) * (radius - 10.dp.toPx()), center.y + sin(angle) * (radius - 10.dp.toPx())),
                                end = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Haz de escaneo
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Color.Transparent, LuxeColors.ElectricBlue.copy(0.15f), Color.Transparent)),
                            startAngle = scanRotation - 90f,
                            sweepAngle = 45f,
                            useCenter = true
                        )
                    }
                }

                // --- 🛰️ ICONOS DE COMPAÑEROS (PROXIMIDAD REAL) ---
                val myLat = state.motoLatitude
                val myLon = state.motoLongitude
                
                val currentUsers = users.filter { 
                    val sameChannel = it.channel == state.channel && it.nick != nick
                    if (sameChannel) {
                        if (myLat != null && myLon != null && it.lat != null && it.lon != null) {
                            val dist = calculateDistanceKms(myLat, myLon, it.lat, it.lon)
                            dist < 50.0 // Radio de 50km para cobertura total entre pueblos
                        } else {
                            it.city == state.city // Fallback a ciudad si no hay GPS
                        }
                    } else false
                }

                currentUsers.forEach { user ->
                    val userLat = user.lat
                    val userLon = user.lon
                    
                    // Solo dibujamos si ambos tenemos GPS real
                    if (myLat != null && myLon != null && userLat != null && userLon != null) {
                        val distanceKm = calculateDistanceKms(myLat, myLon, userLat, userLon)
                        val bearing = calculateBearing(myLat, myLon, userLat, userLon)
                        
                        // Escalar distancia al radar (Máximo visible según zoom)
                        val maxRadarKm = if (isZoomed) 1.5 else 10.0 // 1.5km o 10km de radio
                        val distPx = (distanceKm / maxRadarKm).coerceIn(0.0, 1.0) * (360.dp.value / 2.2f)
                        
                        val angleRad = (bearing - 90f) * (PI / 180f).toFloat()
                        val x = (distPx * cos(angleRad.toDouble())).dp
                        val y = (distPx * sin(angleRad.toDouble())).dp

                        Box(
                            Modifier
                                .offset(x, y)
                                .clip(CircleShape)
                                .clickable {
                                    onExecuteEngineeringAction("SPEAK|Calculando ruta hacia la posición de ${user.nick}")
                                    uriHandler.openUri("https://www.google.com/maps/dir/?api=1&destination=$userLat,$userLon")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(if(user.isTransmitting) Color.Red.copy(0.2f) else LuxeColors.Gold.copy(0.1f), CircleShape)
                                        .border(2.dp, if(user.isTransmitting) Color.Red else LuxeColors.Gold.copy(0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when(user.activity) {
                                        ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                                        ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                                        ActivityProfile.SENDERISMO -> Icons.Rounded.Terrain
                                        ActivityProfile.PASEO -> Icons.Rounded.DirectionsWalk
                                        ActivityProfile.MONTANA -> Icons.Rounded.Landscape
                                        ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                                        ActivityProfile.CAMIONEROS -> Icons.Rounded.LocalShipping
                                        ActivityProfile.CARAVANAS -> Icons.Rounded.AirportShuttle
                                        ActivityProfile.OFFROAD -> Icons.Rounded.Agriculture
                                        ActivityProfile.TACTICO -> Icons.Rounded.Security
                                        ActivityProfile.RUNNING -> Icons.Rounded.DirectionsRun
                                        ActivityProfile.ESQUI -> Icons.Rounded.DownhillSkiing
                                        ActivityProfile.VELA -> Icons.Rounded.Sailing
                                        ActivityProfile.PARAPENTE -> Icons.Rounded.AirplanemodeActive
                                        ActivityProfile.CAZA -> Icons.Rounded.Radar
                                        ActivityProfile.PESCA -> Icons.Rounded.Phishing
                                        ActivityProfile.KAYAK -> Icons.Rounded.Kayaking
                                        else -> Icons.Rounded.Person
                                    }
                                    Icon(icon, null, tint = if(user.isTransmitting) Color.Red else Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text("${user.nick}\n${(distanceKm * 100).toInt() / 100.0}km", 
                                    color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Black, 
                                    textAlign = TextAlign.Center, lineHeight = 7.sp,
                                    style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 2f)))
                            }
                        }
                    } else {
                        // --- 🛡️ FALLBACK: POSICIÓN SIMULADA SI NO HAY GPS ---
                        // (Mantenemos un pequeño círculo de "Buscando señal" o similar si prefieres)
                    }
                }

                // MI POSICIÓN (CENTRO - FIXED)
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pulseScale by infiniteTransition.animateFloat(1f, 1.3f, infiniteRepeatable(tween(2000), RepeatMode.Reverse))
                        
                        Box(
                            Modifier
                                .size(44.dp)
                                .drawBehind {
                                    drawCircle(if(isTransmitting) Color.Red.copy(0.15f) else LuxeColors.ElectricBlue.copy(0.15f), radius = 22.dp.toPx() * pulseScale)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if(isTransmitting) Color.Red.copy(0.2f) else LuxeColors.ElectricBlue.copy(0.2f), CircleShape)
                                    .border(2.dp, if(isTransmitting) Color.Red else LuxeColors.ElectricBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val myIcon = when(profile) {
                                    ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                                    ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                                    ActivityProfile.SENDERISMO -> Icons.Rounded.Terrain
                                    ActivityProfile.PASEO -> Icons.Rounded.DirectionsWalk
                                    ActivityProfile.MONTANA -> Icons.Rounded.Landscape
                                    ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                                    ActivityProfile.CAMIONEROS -> Icons.Rounded.LocalShipping
                                    ActivityProfile.CARAVANAS -> Icons.Rounded.AirportShuttle
                                    ActivityProfile.OFFROAD -> Icons.Rounded.Agriculture
                                    ActivityProfile.TACTICO -> Icons.Rounded.Security
                                    ActivityProfile.RUNNING -> Icons.Rounded.DirectionsRun
                                    ActivityProfile.ESQUI -> Icons.Rounded.DownhillSkiing
                                    ActivityProfile.VELA -> Icons.Rounded.Sailing
                                    ActivityProfile.PARAPENTE -> Icons.Rounded.AirplanemodeActive
                                    ActivityProfile.CAZA -> Icons.Rounded.Radar
                                    ActivityProfile.PESCA -> Icons.Rounded.Phishing
                                    ActivityProfile.KAYAK -> Icons.Rounded.Kayaking
                                    else -> Icons.Rounded.Person
                                }
                                Icon(myIcon, null, tint = if(isTransmitting) Color.Red else LuxeColors.ElectricBlue, modifier = Modifier.size(22.dp))
                            }
                        }
                        Text(nick, color = if(isTransmitting) Color.Red else LuxeColors.ElectricBlue, fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                // --- 🛡️ AVISO GPS DESACTIVADO (MOVIDO AL FINAL PARA EVITAR SOLAPAMIENTO) ---
                if (state.motoLatitude == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.2f)).clickable { onGpsRequest { } },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.GpsFixed, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("BÚSQUEDA DE SEÑAL GPS...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Toca para activar ubicación", color = Color.White.copy(0.5f), fontSize = 10.sp)
                        }
                    }
                }

                // INDICADOR DE ESCALA Y SELECTOR
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(0.7f))
                        .border(1.dp, LuxeColors.Gold.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!showRealMap) LuxeColors.Gold.copy(0.2f) else Color.Transparent)
                            .clickable { showRealMap = false; triggerUiSound("click") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("RADAR", color = if (!showRealMap) LuxeColors.Gold else Color.White.copy(0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showRealMap) LuxeColors.Gold.copy(0.2f) else Color.Transparent)
                            .clickable { showRealMap = true; triggerUiSound("click") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("MAPA REAL", color = if (showRealMap) LuxeColors.Gold else Color.White.copy(0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                // --- 🔍 CONTROLES FLOTANTES (LUPA / GPS) ---
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // LUPA (ZOOM)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.6f))
                            .border(1.dp, LuxeColors.Gold.copy(0.4f), CircleShape)
                            .clickable { isZoomed = !isZoomed; triggerUiSound("click") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isZoomed) Icons.Rounded.ZoomOutMap else Icons.Rounded.ZoomIn, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                    }
                    
                    // GPS REAL
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.6f))
                            .border(1.dp, LuxeColors.ElectricBlue.copy(0.4f), CircleShape)
                            .clickable { 
                                onGpsRequest { url ->
                                    if (url != null) uriHandler.openUri(url)
                                    else uriHandler.openUri("https://www.google.com/maps")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Map, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(20.dp))
                    }

                    // COMPARTIR RUTA
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.6f))
                            .border(1.dp, LuxeColors.Gold.copy(0.4f), CircleShape)
                            .clickable { 
                                val profileId = state.activeProfile.name
                                onShare(state.channel, state.subtone, "ACTIVITY", profileId)
                                triggerUiSound("click")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- 🎧 ESTADO Y VOX (COMPACTO) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { onStateChange(state.copy(isVoxEnabled = !state.isVoxEnabled)); triggerUiSound("switch") },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.isVoxEnabled) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, if (state.isVoxEnabled) LuxeColors.Gold else Color.White.copy(0.1f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Rounded.Mic, null, tint = if (state.isVoxEnabled) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if(state.isVoxEnabled) "VOX ON" else "VOX OFF", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    onClick = { 
                        onPendingDialogChange(RadioDialogType.DISCRETE)
                        triggerUiSound("click")
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.isDiscreteModeEnabled) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.1f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(
                            if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, 
                            null, 
                            tint = if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.3f), 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if(state.isDiscreteModeEnabled) "DISCRETO" else "PÚBLICO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Rounded.WifiTethering, null, tint = LuxeColors.Green, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("RED OK", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (state.isVoxEnabled) {
                Spacer(Modifier.height(8.dp))
                EliteSlider(
                    label = "SENS. MANOS LIBRES",
                    value = state.voxSensitivity
                ) { onStateChange(state.copy(voxSensitivity = it)) }
            }

            Spacer(Modifier.height(12.dp))

            // --- 🎤 PTT GIGANTE (ALTURA OPTIMIZADA) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                tryAwaitRelease()
                                interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                            }
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                color = if (isTransmitting) Color.Red.copy(0.2f) else if (rx) Color.Green.copy(0.15f) else Color.White.copy(0.08f),
                border = BorderStroke(3.dp, if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White.copy(0.2f))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTransmitting) Icons.Rounded.Mic else if (rx) Icons.Rounded.VolumeUp else Icons.Rounded.MicNone, 
                            contentDescription = null, 
                            tint = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White, 
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            if (isTransmitting) "HABLANDO (AIRE)" else if (rx) "AIRE: RECIBIENDO" else "PULSAR PARA HABLAR", 
                            color = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White, 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            
            LuxeButton(
                text = "CERRAR MODO RUTA",
                onClick = { showCloseConfirm = true },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                containerColor = Color.White.copy(0.1f),
                contentColor = Color.White
            )

            // --- 💰 ESPACIO PUBLICITARIO ADMOB (GENERACIÓN DE INGRESOS) ---
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = { onExecuteEngineeringAction("INSTALL_APP") },
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.AdsClick, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "ESPACIO PATROCINADO", 
                            color = LuxeColors.Gold, 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Anúnciate aquí y llega a toda la comunidad ON AIR.",
                            color = Color.White.copy(0.7f), 
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = { onExecuteEngineeringAction("INSTALL_APP") },
                color = LuxeColors.Gold.copy(0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.GetApp, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "INSTALAR APP OFICIAL: MÁS POTENCIA Y SIN CORTES", 
                        color = Color.White, 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // --- 🛡️ MARGEN DE SEGURIDAD PARA BANNER ---
            Spacer(Modifier.height(100.dp))
        }

        // --- 📱 DIÁLOGOS DE AYUDA Y CONFIGURACIÓN ---
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                containerColor = LuxeColors.Slate900,
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("INGENIERÍA DE AUDIO", color = LuxeColors.Gold, fontWeight = FontWeight.Black) 
                    }
                },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        val optDesc = when(profile) {
                            ActivityProfile.MOTO -> "FILTRO MOTOR (300Hz): Corta el ruido del escape y el viento del casco. El VOX está endurecido para no saltar con las revoluciones."
                            ActivityProfile.CICLISMO -> "FILTRO VIENTO (200Hz): Optimizado para el silbido del aire en el micro. Incluye detección de caídas (G-Force)."
                            ActivityProfile.PARAPENTE -> "FILTRO EXTREMO (280Hz): Diseñado para el flujo de aire constante en vuelo. Voz prioritaria sobre el ruido ambiente."
                            ActivityProfile.VELA -> "FILTRO MARINO (250Hz): Elimina el ruido de las olas y el viento racheado de costa. Radar náutico activado."
                            ActivityProfile.MONTANA -> "FILTRO RÁFAGAS (120Hz): Mantiene la voz nítida frente al viento de montaña. Máxima precisión GPS."
                            ActivityProfile.CAZA -> "MODO SUSURRO: Sensibilidad del micro aumentada al máximo. Permite hablar muy bajo sin perder la conexión."
                            ActivityProfile.TACTICO -> "MODO MILITAR: Audio comprimido para máxima inteligibilidad en combate o misiones. Encriptación de malla activa."
                            ActivityProfile.CAMIONEROS -> "FILTRO RODADURA: Atenúa el ruido de fondo de la cabina y el motor diesel."
                            ActivityProfile.RUNNING -> "VOX RÍTMICO: El algoritmo ignora el sonido de los pasos y la respiración fuerte del corredor."
                            else -> "OPTIMIZACIÓN ESTÁNDAR: Balance entre calidad de audio y consumo de batería."
                        }
                        
                        Surface(
                            color = LuxeColors.Gold.copy(0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = optDesc,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("SEGURIDAD Y RUTA", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 11.sp)
                        Text("• Toca el mapa para ver la ruta real.\n• El botón compartir envía tu posición GPS exacta.\n• Si te separas más de 50km del grupo, entrarás en modo repetidor WiFi.", fontSize = 11.sp, color = Color.White.copy(0.7f))

                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = Color.Red.copy(0.1f),
                            border = BorderStroke(1.dp, Color.Red.copy(0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("⚠️ AVISO LEGAL", fontWeight = FontWeight.Black, color = Color.Red, fontSize = 9.sp)
                                Text("El uso de auriculares conduciendo puede ser ilegal. ON AIR SPAIN no se hace responsable de sanciones.", 
                                    fontSize = 8.sp, color = Color.White.copy(0.9f), lineHeight = 10.sp)
                            }
                        }
                    }
                },
                confirmButton = { LuxeButton("ENTENDIDO", { showHelpDialog = false }, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black) }
            )
        }

        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                containerColor = LuxeColors.Slate800,
                title = { Text("NOMBRE DE TU RUTA", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    OutlinedTextField(
                        value = newChannelName,
                        onValueChange = { if (it.length <= 15) newChannelName = it.uppercase() },
                        label = { Text("Ej: RUTA DOMINGO") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedLabelColor = LuxeColors.Gold, cursorColor = LuxeColors.Gold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                },
                confirmButton = {
                    LuxeButton("GUARDAR RUTA", { 
                        onStateChange(state.copy(channel = newChannelName))
                        showRenameDialog = false 
                    }, newChannelName.isNotBlank(), Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black)
                }
            )
        }

        if (showCloseConfirm) {
            AlertDialog(
                onDismissRequest = { showCloseConfirm = false },
                containerColor = LuxeColors.Slate900,
                icon = { Icon(Icons.Rounded.Warning, null, tint = LuxeColors.Gold) },
                title = { Text("¿FINALIZAR RUTA?", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("¿Estás seguro de que quieres cerrar el modo ruta? Perderás el acceso rápido al PTT y al radar de compañeros.", color = Color.White.copy(0.7f), fontSize = 14.sp) },
                confirmButton = {
                    LuxeButton("SÍ, CERRAR", { 
                        showCloseConfirm = false
                        onExecuteEngineeringAction("HIDE_BANNER") // 💰 OCULTAR PUBLICIDAD AL SALIR
                        
                        // --- 🛡️ FIX: LIMPIEZA TOTAL DE INTERFAZ ANTES DE CERRAR ---
                        // Esto asegura que al volver a la radio el scroll esté libre
                        onStateChange(state.copy(isInterfaceLocked = false))

                        onFinish()
                    }, true, Modifier.fillMaxWidth().height(44.dp), Color.Red, Color.White)
                },
                dismissButton = {
                    TextButton(onClick = { showCloseConfirm = false }) {
                        Text("CONTINUAR RUTA", color = LuxeColors.Gold, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun TacticalDockIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            color = if (isActive) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
            shape = CircleShape,
            border = BorderStroke(1.dp, if (isActive) LuxeColors.Gold else Color.White.copy(0.1f)),
            modifier = Modifier.requiredSize(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    null, 
                    tint = if (isActive) LuxeColors.Gold else Color.White.copy(0.4f), 
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) LuxeColors.Gold else Color.White.copy(0.3f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

