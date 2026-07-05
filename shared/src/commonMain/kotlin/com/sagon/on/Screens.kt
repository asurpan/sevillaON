package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 1.5 (REDISEÑO ELITE)
 * 
 * Gestiona el renderizado de la pantalla de Bienvenida, Carga y Radio.
 * Blindado contra modificaciones estructurales en el flujo de navegación.
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
                        if (!hasAcceptedMic) {
                            onMicAccept()
                        } else {
                            onConnect(startGenre)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold),
                shape = RoundedCornerShape(20.dp),
                enabled = nick.isNotBlank()
            ) {
                Text(
                    if (!hasAcceptedMic) "AUTORIZAR MICRÓFONO" else "ACCEDER A LA RED",
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.05f))
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                        .combinedClickable(
                            onClick = { onPendingDialogChange(RadioDialogType.SETTINGS) },
                            onLongClick = {
                                triggerUiSound("click")
                                onHertzSentinelRequest()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 📟 PANTALLA DIGITAL ELITE "NEXUS" ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { 
                        if (!state.isInterfaceLocked) { 
                            if (!state.hasAcceptedMicExplain) onPendingDialogChange(RadioDialogType.MIC_REQUEST) 
                            else onPendingDialogChange(RadioDialogType.SELECT_CITY) 
                        } 
                    },
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
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- 🚥 VÚMETRO DE LEDS LINEAL (MAESTRO ELITE) ---
                        Row(
                            modifier = Modifier.fillMaxWidth().height(22.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            val activeLevel = if(isTransmitting || rx) mic else qrmIntensity
                            repeat(24) { i ->
                                val isActive = i < (activeLevel * 24)
                                val ledColor = when {
                                    i > 20 -> Color.Red
                                    i > 16 -> Color(0xFFFACC15) // Gold
                                    else -> if(rx) Color(0xFF22D3EE) else Color(0xFF4ADE80) // Cyan vs Green
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isActive) ledColor else Color.White.copy(0.04f))
                                        .border(0.5.dp, if (isActive) ledColor.copy(0.3f) else Color.Transparent, RoundedCornerShape(2.dp))
                                        .drawBehind {
                                            if (isActive) {
                                                drawCircle(ledColor, radius = size.width * 4f, alpha = 0.3f)
                                                drawCircle(ledColor, radius = size.width * 1.5f, alpha = 0.5f)
                                            }
                                        }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // --- 🏷️ INFORMACIÓN PRINCIPAL (Layout Profesional) ---
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.width(70.dp), horizontalAlignment = Alignment.Start) {
                                TechLabel("SQUELCH", "${(state.squelch * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.SETTINGS)
                                }
                                Spacer(Modifier.height(16.dp))
                                TechLabel("GANANCIA", "${(state.rfGain * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.SETTINGS)
                                }
                                Spacer(Modifier.height(16.dp))
                                TechLabel("VOX", if(state.isVoxEnabled) "ACTIVO" else "OFF", if(state.isVoxEnabled) LuxeColors.Gold else Color.White.copy(0.3f)) {
                                    onPendingDialogChange(RadioDialogType.SETTINGS)
                                }
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
                                
                                Spacer(Modifier.height(4.dp))
                                
                                Text(
                                    text = if(rx) (transmitterNick ?: "ANÓNIMO") else state.channel,
                                    color = if(rx) Color(0xFF22D3EE) else Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(),
                                    style = TextStyle(shadow = Shadow(color = statusColor.copy(0.3f), blurRadius = 10f))
                                )
                                
                                Spacer(Modifier.height(8.dp))

                                Surface(
                                    color = Color.White.copy(0.08f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.LocationOn, 
                                            null, 
                                            tint = LuxeColors.Gold, 
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = state.city,
                                            color = LuxeColors.Gold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp,
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee(),
                                            style = TextStyle(lineHeight = 10.sp)
                                        )
                                    }
                                }
                            }

                            // Panel Lateral Derecho (Power/Watts)
                            Column(Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
                                Text("POTENCIA", color = Color.White.copy(0.3f), fontSize = 7.sp, fontWeight = FontWeight.Black)
                                val wText = if (isTransmitting) "${(myDynamicPower * 15f).toInt()} W" else if(rx) "9.2 W" else "0.0 W"
                                Text(
                                    wText, 
                                    color = if(isTransmitting) Color.Red else if(rx) Color(0xFF22D3EE) else Color.White.copy(0.2f), 
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Black
                                )
                                
                                Spacer(Modifier.height(12.dp))
                                
                                // MODO DISCRETO (Candado de Voz)
                                Surface(
                                    onClick = { 
                                        if (!state.hasSeenDiscreteIntro) onPendingDialogChange(RadioDialogType.DISCRETE)
                                        else onStateChange(state.copy(isDiscreteModeEnabled = !state.isDiscreteModeEnabled))
                                        triggerUiSound("switch")
                                    },
                                    color = if (state.isDiscreteModeEnabled) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.1f)),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (state.isDiscreteModeEnabled) Icons.Rounded.Lock else Icons.Rounded.LockOpen, 
                                            null, 
                                            tint = if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.4f), 
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // 🏃 MODO ACTIVIDAD (Deportes)
                                Surface(
                                    onClick = { 
                                        if (state.activeProfile == ActivityProfile.NORMAL) {
                                            onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR)
                                        } else {
                                            onActivityPanelRequest()
                                        }
                                        triggerUiSound("click") 
                                    },
                                    color = if (state.activeProfile != ActivityProfile.NORMAL) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, if (state.activeProfile != ActivityProfile.NORMAL) LuxeColors.Gold else Color.White.copy(0.1f)),
                                    modifier = Modifier.size(40.dp) // Aumentado de 28.dp para visibilidad
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val icon = when(state.activeProfile) {
                                            ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                                            ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                                            ActivityProfile.SENDERISMO -> Icons.Rounded.DirectionsWalk
                                            ActivityProfile.MONTANA -> Icons.Rounded.Terrain
                                            ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                                            else -> Icons.Rounded.DirectionsRun
                                        }
                                        Icon(icon, null, tint = if (state.activeProfile != ActivityProfile.NORMAL) LuxeColors.Gold else Color.White.copy(0.4f), modifier = Modifier.size(20.dp)) // Aumentado de 12.dp
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Botón Compartir Integrado
                                Surface(
                                    onClick = { onShare(state.channel, state.subtone, state.myProRole, null); triggerUiSound("click") },
                                    shape = CircleShape,
                                    color = Color.White.copy(0.05f),
                                    border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Share, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        // --- 📊 BARRA DE ESTADO INFERIOR ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(0.1f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                }
                                .padding(top = 12.dp),
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
                                    Text(if(state.subtone == "0000") "CTC: OFF" else "SUB: ${state.subtone}", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            // Reloj / Tiempo TX
                            if (isTransmitting) {
                                val seconds = pttTimer
                                Text("TX TIME: ${seconds}s", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            } else {
                                Text(
                                    "NET: ${if(audioIntegrity) "CONNECTED" else "SYNC ERROR"}", 
                                    color = if(audioIntegrity) Color(0xFF4ADE80).copy(0.6f) else Color.Red, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Botón Ajustes Rápidos (Dentro de la pantalla)
                            Icon(
                                Icons.Rounded.Info,
                                null,
                                tint = Color.White.copy(0.2f),
                                modifier = Modifier.size(16.dp).clickable { onShowHelp() }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 🎛️ DOCK DE CONTROLES RÁPIDOS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    modifier = Modifier.weight(1f).clickable { if (!state.isInterfaceLocked) onReplay() },
                    icon = Icons.Rounded.History,
                    label = "REPLAY 15s",
                    status = if (replayProgress > 0f) "AL AIRE" else if (isReplayReady) "LISTO" else "VACÍO",
                    isActive = isReplayReady,
                    progress = replayProgress
                )

                EliteControlTile(
                    modifier = Modifier.weight(1f).clickable { onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR) },
                    icon = when(state.activeProfile) {
                        ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                        ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                        ActivityProfile.SENDERISMO -> Icons.Rounded.DirectionsWalk
                        ActivityProfile.MONTANA -> Icons.Rounded.Terrain
                        ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                        else -> Icons.Rounded.DirectionsRun
                    },
                    label = "DEPORTES",
                    status = if (state.activeProfile != ActivityProfile.NORMAL) state.activeProfile.name else "OFF",
                    isActive = state.activeProfile != ActivityProfile.NORMAL
                )
                
                // --- 🛰️ ACCESO DIRECTO AL RADAR (ELITE PROMO) ---
                EliteControlTile(
                    modifier = Modifier.weight(1f).clickable { onHertzSentinelRequest() }, 
                    icon = Icons.Rounded.Radar, 
                    label = "RADAR", 
                    status = "HERTZ",
                    isActive = radarActivo
                )

                EliteControlTile(Modifier.weight(1f).clickable { onStateChange(state.copy(isChatVisible = !state.isChatVisible)); if(!state.isChatVisible) onPublicChat() }, Icons.AutoMirrored.Rounded.Chat, "CHAT", "${chatMessages.size} MSGS")
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

            if (activeRooms.isNotEmpty() || state.channel != "GENERAL") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.channel != "GENERAL") "SINTONIZADOR DE BARRIOS" else "BARRIOS Y SALAS ACTIVAS EN ${state.city}", 
                        color = LuxeColors.Gold.copy(0.6f), 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp, 
                        modifier = Modifier.clickable { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.CREATE_CHANNEL) }
                    )
                    
                    if (state.channel != "GENERAL") {
                        IconButton(
                            onClick = { onShare(state.channel, state.subtone, null, null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(14.dp))
                        }
                    }
                }
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
                                    text = if (isGeneral) "VOLVER A GENERAL" else room, 
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
                Spacer(Modifier.height(16.dp))
            }

            // --- 👥 ESTACIONES EN ESTE CANAL ---
            Text(if (state.channel == "GENERAL") "OPERADORES EN ${state.city}" else "OPERADORES EN SALA ${state.channel}", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
            
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

            // --- 🎚️ CONSOLA DE AJUSTES DESLIZABLE ---
            AnimatedVisibility(
                visible = isMasterControlsVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("CONSOLA DE PRECISIÓN", color = LuxeColors.Gold, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(24.dp))
                        
                        EliteSlider("SQUELCH", state.squelch) { onStateChange(state.copy(squelch = it)) }
                        Spacer(Modifier.height(20.dp))
                        EliteSlider("RF GAIN", state.rfGain) { onStateChange(state.copy(rfGain = it)) }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EliteSwitch(Modifier.weight(1f), "R. BEEP", state.isRogerBeepEnabled) { onStateChange(state.copy(isRogerBeepEnabled = it)) }
                            EliteSwitch(Modifier.weight(1f), "MANOS LIBRES", state.isVoxEnabled) { onStateChange(state.copy(isVoxEnabled = it)) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EliteSwitch(Modifier.weight(1f), "MONITOR", state.isMonitorEnabled) { onStateChange(state.copy(isMonitorEnabled = it)) }
                            EliteSwitch(Modifier.weight(1f), "ECHO DSP", state.isEchoEnabled) { onStateChange(state.copy(isEchoEnabled = it)) }
                        }
                        Spacer(Modifier.height(12.dp))
                        EliteSwitch(Modifier.fillMaxWidth(), "CONTROL DE RED (BOT)", state.isSystemVoiceEnabled) { onStateChange(state.copy(isSystemVoiceEnabled = it)) }
                        
                        AnimatedVisibility(visible = state.isVoxEnabled || state.isMonitorEnabled || state.isEchoEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 20.dp)) {
                                if (state.isVoxEnabled) EliteSlider("SENSIBILIDAD VOX", state.voxSensitivity) { onStateChange(state.copy(voxSensitivity = it)) }
                                if (state.isMonitorEnabled) EliteSlider("VOLUMEN MONITOR", state.monitorVolume) { onStateChange(state.copy(monitorVolume = it)) }
                                if (state.isEchoEnabled) EliteSlider("REVERB / ECO", state.echoDelay) { onStateChange(state.copy(echoDelay = it)) }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                        Text("SISTEMA Y SOPORTE", color = LuxeColors.Gold, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Ahorro de Batería (ECO)
                            EliteSwitch(Modifier.weight(1f), "MODO ECO", state.isEcoMode) { onStateChange(state.copy(isEcoMode = it)) }
                            
                            // Sin Restricciones (Ayuda / Guía)
                            Surface(
                                onClick = { onShowHelp() },
                                modifier = Modifier.weight(1.5f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = LuxeColors.ElectricBlue.copy(0.1f),
                                border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.4f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.BatteryChargingFull, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("SIN RESTRICCIONES", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Mapa Radar (Movido aquí)
                            Surface(
                                onClick = { onPendingDialogChange(RadioDialogType.RADAR_MAP) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.Radar, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("MAPA", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            // Tutorial Completo
                            Surface(
                                onClick = { onPendingDialogChange(RadioDialogType.ONBOARDING) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.Info, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("TUTORIAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            // Derecho al Olvido (Borrar datos)
                            Surface(
                                onClick = { onPendingDialogChange(RadioDialogType.DELETE_DATA) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Red.copy(0.1f),
                                border = BorderStroke(1.dp, Color.Red.copy(0.3f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.DeleteSweep, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("OLVIDO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(180.dp)) // 🛡️ FIX: Aumentado para asegurar que NADA quede bajo el banner
            }
        }

        // --- 🛰️ MINI-VÚMETRO DE VIGILANCIA HERTZ (VERTICAL) ---
        if (radarActivo) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 120.dp)
                        .align(Alignment.BottomEnd)
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
                onClose = { onStateChange(state.copy(isChatVisible = false)) }
            )
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

@Composable
fun EliteChatOverlay(
    messages: List<ChatMessage>,
    target: String?,
    currentText: androidx.compose.ui.text.input.TextFieldValue,
    onTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.95f)).padding(24.dp)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose) { 
                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f)) 
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // --- 📜 LISTA DE MENSAJES ELITE ---
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    Text(
                        "ESPERANDO ACTIVIDAD EN LA TERMINAL...", 
                        color = Color.White.copy(0.1f), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(messages) { msg ->
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    msg.senderNick, 
                                    color = LuxeColors.Gold, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                msg.text, 
                                color = Color.White, 
                                fontSize = 14.sp, 
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            // --- 📢 BOTÓN DE ANUNCIO RÁPIDO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pon un anuncio y el locutor lo pondrá en la radio:", 
                    color = LuxeColors.Gold.copy(0.6f), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
                if (currentText.text.isNotBlank()) {
                    Text(
                        "BORRAR TODO",
                        color = Color.Red.copy(0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(bottom = 8.dp, end = 8.dp)
                            .clickable { onTextChange(androidx.compose.ui.text.input.TextFieldValue("")) }
                    )
                }
            }

            Surface(
                onClick = { 
                    if (!currentText.text.contains("ANUNCIO:")) {
                        onTextChange(androidx.compose.ui.text.input.TextFieldValue("ANUNCIO: " + currentText.text))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp).padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = LuxeColors.Gold.copy(0.1f),
                border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f))
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("PUBLICAR ANUNCIO (LECTURA POR VOZ)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            
            // --- ⌨️ INPUT GLASSMORPHISM ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(0.05f),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (currentText.text.isEmpty()) {
                            Text(
                                "ESCRIBE TU MENSAJE AQUÍ...", 
                                color = Color.White.copy(0.2f),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = currentText,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            cursorBrush = SolidColor(LuxeColors.Gold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onKeyEvent {
                                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                        onSend()
                                        true
                                    } else false
                                },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() })
                        )
                    }
                    
                    IconButton(onClick = onSend) {
                        Icon(Icons.AutoMirrored.Rounded.Send, null, tint = LuxeColors.Gold)
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CERRAR TERMINAL", color = LuxeColors.Gold.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
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
        Text(label, color = Color.White.copy(0.3f), fontSize = 8.sp, fontWeight = FontWeight.Black)
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
    onClose: () -> Unit
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
            ActivityProfile.MOTO -> "Modo Moto activado. Filtro de viento listo y guardián de impactos vigilando. Pulsa el PTT grande para hablar con el grupo."
            ActivityProfile.CICLISMO -> "Modo Ciclismo listo. Si detecto una caída fuerte, avisaré a tus compañeros con tu ubicación. ¡Buena ruta!"
            ActivityProfile.MONTANA -> "Modo Montaña activo. El radar GPS te mantendrá localizado. En caso de peligro, usa el protocolo S.O.S."
            else -> "Modo Actividad iniciado. Sintoniza tu canal y comparte la ruta con tus amigos."
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
                        text = if (state.channel == "GENERAL") "MODO ACTIVIDAD" else "GRUPO: ${state.channel}", 
                        color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        if (state.subtone != "0000") "CÓDIGO PRIVADO: ${state.subtone}" else "RED DE MALLA ACTIVA", 
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
                Box(Modifier.fillMaxSize().alpha(if (state.motoLatitude != null) 1f else 0.3f)) {
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
                        // --- 📡 CAPA RADAR TÁCTICO ---
                        Image(
                            painter = painterResource(Res.drawable.hero_city),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().alpha(0.08f),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(LuxeColors.ElectricBlue, BlendMode.Screen)
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
                                    drawCircle(LuxeColors.ElectricBlue.copy(0.15f), radius = 22.dp.toPx() * pulseScale)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(LuxeColors.ElectricBlue.copy(0.2f), CircleShape)
                                    .border(2.dp, LuxeColors.ElectricBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val myIcon = when(profile) {
                                    ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                                    ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                                    ActivityProfile.SENDERISMO -> Icons.Rounded.DirectionsWalk
                                    ActivityProfile.MONTANA -> Icons.Rounded.Terrain
                                    ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                                    else -> Icons.Rounded.DirectionsRun
                                }
                                Icon(myIcon, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(22.dp))
                            }
                        }
                        Text("ESTACIÓN BASE", color = LuxeColors.ElectricBlue, fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                // --- 🛡️ AVISO GPS DESACTIVADO (MOVIDO AL FINAL PARA EVITAR SOLAPAMIENTO) ---
                if (state.motoLatitude == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable { onGpsRequest { } },
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
                    Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Mic, null, tint = if (state.isVoxEnabled) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if(state.isVoxEnabled) "M. LIBRES ON" else "M. LIBRES OFF", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WifiTethering, null, tint = LuxeColors.Green, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("MALLA OK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
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
                title = { Text("GUÍA DE RUTA Y SEGURIDAD", color = LuxeColors.Gold, fontWeight = FontWeight.Black) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("1. CREAR UNA RUTA", fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                        Text("Toca el nombre del grupo arriba para ponerle nombre a tu ruta (ej: RUTA SIERRA). Luego usa el botón compartir para enviar el enlace por WhatsApp. Tus amigos entrarán directamente a tu grupo.", fontSize = 12.sp, color = Color.White.copy(0.7f))
                        
                        Spacer(Modifier.height(12.dp))
                        Text("2. GUARDIÁN DE IMPACTOS", fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                        Text("Si sufres una caída fuerte, la app lo detectará y avisará a tus compañeros con tu GPS tras 15 seg. si no cancelas.", fontSize = 12.sp, color = Color.White.copy(0.7f))

                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = Color.Red.copy(0.1f),
                            border = BorderStroke(1.dp, Color.Red.copy(0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("⚠️ AVISO LEGAL IMPORTANTE", fontWeight = FontWeight.Black, color = Color.Red, fontSize = 10.sp)
                                Text("El uso de auriculares mientras se conduce (bici, moto, coche) puede estar prohibido según la normativa de tráfico de tu zona. El usuario asume toda la responsabilidad legal y de seguridad por el uso de manos libres o dispositivos de audio. ON AIR SPAIN no se hace responsable de sanciones o accidentes derivados del uso indebido de la app en marcha.", 
                                    fontSize = 9.sp, color = Color.White, lineHeight = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = { LuxeButton("ACEPTO Y ENTIENDO", { showHelpDialog = false }, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black) }
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
                        onClose() 
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
