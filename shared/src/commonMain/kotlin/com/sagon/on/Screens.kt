package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 2.0 (UX OVERHAUL)
 * 
 * Gestiona el renderizado de la pantalla de Bienvenida, Carga y Radio.
 * NIVEL DE PROTECCIÓN 0: PROHIBIDO ALTERAR LÓGICA DE DOCK.
 * Escalado visual para máxima comodidad y legibilidad (Accesibilidad Pro).
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(60.dp)) // Espacio superior elegante

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
                fontSize = 12.sp, // Aumentado de 10 a 12
                fontWeight = FontWeight.Black, 
                letterSpacing = 4.sp
            )
            Text(
                "ON AIR SPAIN", 
                color = Color.White, 
                fontSize = 32.sp, // Aumentado de 28 a 32
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(40.dp))

            // --- 🧪 INPUT GLASSMORPHISM GIGANTE ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp), // Aumentado de 64 a 80
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(0.03f),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (nick.isEmpty()) {
                        Text("TU INDICATIVO...", color = Color.White.copy(0.2f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                            fontSize = 24.sp, // Aumentado de 18 a 24
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 2.sp
                        ),
                        cursorBrush = SolidColor(LuxeColors.Gold),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    if (nick.isNotBlank()) {
                        if (!hasAcceptedMic) onMicAccept()
                        onConnect(startGenre)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(80.dp), // Aumentado de 64 a 80
                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold),
                shape = RoundedCornerShape(24.dp),
                enabled = nick.isNotBlank()
            ) {
                Text(
                    "ENTRAR EN LA RADIO",
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 2.sp,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Info de usuarios activos con estilo minimal (Adaptable a pantallas estrechas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    onClick = { onShowRadar() },
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.Radar, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "RADAR", 
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                if (totalUsers > 0) {
                    Spacer(Modifier.width(10.dp))

                    Surface(
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(6.dp).background(LuxeColors.Gold, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$totalUsers ONLINE", 
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
            
            // --- 🛡️ ESPACIO DE SEGURIDAD PARA EL TECLADO ---
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
    var isCurrentTouchDenied by remember { mutableStateOf(false) } 

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
            pttTimer = 0
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(EliteTheme.DeepGradient)) {
        StarryBackground(activity = if (isTransmitting || rx) 0.6f else 0.15f, isEcoMode = state.isEcoMode)

        Box(modifier = Modifier.fillMaxSize()) {
    Column(
                modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(24.dp).padding(bottom = 100.dp),
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
                        Text("Tu indicativo: ", color = Color.White.copy(0.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(nick, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { if (!state.isInterfaceLocked && isReplayReady) onReplay(); triggerUiSound("click") },
                        color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)),
                        modifier = Modifier.size(44.dp) // Aumentado de 36 a 44
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (replayProgress > 0f) {
                                CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp, trackColor = Color.Transparent)
                            }
                            Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.05f))
                            .border(1.dp, Color.White.copy(0.1f), CircleShape)
                            .clickable { onPendingDialogChange(RadioDialogType.SETTINGS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 📟 PANTALLA DIGITAL ELITE "NEXUS" ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // Reducido para ganar espacio vertical
                    .clip(RoundedCornerShape(32.dp)),
                color = Color.Black.copy(0.6f),
                border = BorderStroke(2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1.2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.width(90.dp), horizontalAlignment = Alignment.Start) {
                                TechLabel("SQUELCH", "${(state.squelch * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.HELP_SQUELCH)
                                }
                                Spacer(Modifier.height(24.dp))
                                TechLabel("GANANCIA", "${(state.rfGain * 100).toInt()}%") {
                                    onPendingDialogChange(RadioDialogType.HELP_GAIN)
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val statusText = if (rx) "RECIBIENDO..." else if(isTransmitting) "EMITIENDO..." else "EN ESPERA"
                                val statusColor = if (rx) Color(0xFF22D3EE) else if(isTransmitting) Color.Red else Color.White.copy(0.2f)
                                
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(6.dp)),
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
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (isActive) ledColor else Color.White.copy(0.06f))
                                                .border(1.dp, if (isActive) ledColor.copy(0.3f) else Color.White.copy(0.02f), RoundedCornerShape(3.dp))
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                
                                val displayChannel = if (state.channel == "GENERAL") "ENTRAR EN BARRIO, PUEBLO O ACTIVIDAD" else state.channel
                                Text(
                                    text = state.city,
                                    color = if(rx) Color(0xFF22D3EE) else Color.White,
                                    fontSize = 32.sp, // Reducido para optimizar espacio
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                        .clickable { 
                                            if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SELECT_CITY) 
                                        },
                                    style = TextStyle(shadow = Shadow(color = statusColor.copy(0.3f), blurRadius = 15f))
                                )
                                
                                Spacer(Modifier.height(8.dp))

                                Surface(
                                    onClick = { 
                                        if (!state.isInterfaceLocked) { 
                                            if (!state.hasAcceptedMicExplain) onPendingDialogChange(RadioDialogType.MIC_REQUEST) 
                                            else onPendingDialogChange(RadioDialogType.CREATE_CHANNEL)
                                        } 
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    color = LuxeColors.Gold.copy(0.2f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, LuxeColors.Gold.copy(0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            if(rx) Icons.Rounded.Person else Icons.Rounded.Home, 
                                            null, 
                                            tint = LuxeColors.Gold, 
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = if(rx) (transmitterNick ?: "ANÓNIMO") else displayChannel,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee()
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPendingDialogChange(RadioDialogType.WATTS) },
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("POTENCIA", color = Color.White.copy(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                val wText = if (isTransmitting) "${(myDynamicPower * 15f).toInt()} W" else if(rx) "9.2 W" else "0.0 W"
                                Text(
                                    wText, 
                                    color = if(isTransmitting) Color.Red else if(rx) Color(0xFF22D3EE) else Color.White.copy(0.2f), 
                                    fontSize = 24.sp, 
                                    fontWeight = FontWeight.Black
                                )
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
                            Surface(
                                onClick = { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SUBTONO) },
                                color = if(state.subtone != "0000") LuxeColors.Gold.copy(0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = if(state.subtone != "0000") BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)) else null
                            ) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if(state.subtone == "0000") Icons.Rounded.LockOpen else Icons.Rounded.Lock, null, tint = if(state.subtone != "0000") LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                                    if (state.subtone != "0000") {
                                        Spacer(Modifier.width(8.dp))
                                        Text("CÓDIGO PRIVADO: ${state.subtone}", color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    } else {
                                        Spacer(Modifier.width(8.dp))
                                        Text("CANAL ABIERTO", color = Color.White.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 🛠️ PANEL DE INSTRUMENTACIÓN TÁCTICA ---
            Text(
                "INSTRUMENTACIÓN TÁCTICA",
                color = LuxeColors.Gold.copy(0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            
            val tacticalScrollState = androidx.compose.foundation.lazy.rememberLazyListState()
            LaunchedEffect(tacticalScrollState) {
                snapshotFlow { tacticalScrollState.firstVisibleItemIndex }
                    .collect {
                        if (tacticalScrollState.isScrollInProgress) {
                            triggerUiSound("click")
                        }
                    }
            }

            LazyRow(
                state = tacticalScrollState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                // --- BOTONES DINÁMICOS (CONSOLIDADOS) ---
                item { 
                    TacticalDockIcon(
                        icon = Icons.Rounded.Radio, 
                        label = "RADIO FM", 
                        isActive = bgStationName != null, 
                        onClick = { onPendingDialogChange(RadioDialogType.FMSCAN); triggerUiSound("click") },
                        onLongClick = { if (bgStationName != null) onBgRadioStop() else onBgRadioScan(state.city, state.bgRadioGenre); triggerUiSound("switch") }
                    ) 
                }
                item { 
                    TacticalDockIcon(
                        icon = if(state.activeProfile != ActivityProfile.NORMAL) Icons.Rounded.Route else Icons.Rounded.Groups, 
                        label = "RUTA", 
                        isActive = state.activeProfile != ActivityProfile.NORMAL, 
                        onClick = { 
                            if (state.activeProfile == ActivityProfile.NORMAL) onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR) 
                            else onActivityPanelRequest()
                            triggerUiSound("click")
                        },
                        onLongClick = {
                            if (state.activeProfile != ActivityProfile.NORMAL) {
                                onPendingDialogChange(RadioDialogType.FINISH_ACTIVITY_CONFIRM)
                                triggerUiSound("click")
                            }
                        }
                    ) 
                }
                item { 
                    TacticalDockIcon(
                        icon = Icons.AutoMirrored.Rounded.Chat, 
                        label = "CHAT", 
                        isActive = state.unreadCount > 0, 
                        onClick = { onStateChange(state.copy(isChatVisible = !state.isChatVisible)); if(!state.isChatVisible) onPublicChat(); triggerUiSound("click") }
                    ) 
                }
                
                // --- AJUSTES TÉCNICOS ---
                item { TacticalDockIcon(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) { onStateChange(state.copy(isVoxEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.VOX); triggerUiSound("click") } }) }
                item { TacticalDockIcon(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)); triggerUiSound("switch") }) }
                item { TacticalDockIcon(icon = Icons.Rounded.SettingsInputAntenna, label = "ECO", isActive = state.isReverbEnabled, onClick = { if (state.isReverbEnabled) { onStateChange(state.copy(isReverbEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.REVERB); triggerUiSound("click") } }) }
                item { TacticalDockIcon(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { if (state.isDspEnabled) { onStateChange(state.copy(isDspEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.DSP); triggerUiSound("click") } }) }
                item { TacticalDockIcon(icon = Icons.Rounded.Headset, label = "MONI", isActive = state.isMonitorEnabled, onClick = { if (state.isMonitorEnabled) { onStateChange(state.copy(isMonitorEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.MONI); triggerUiSound("click") } }) }
                item { TacticalDockIcon(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE); triggerUiSound("click") }) }
                item { TacticalDockIcon(icon = Icons.AutoMirrored.Rounded.Chat, label = "WHATSAPP", isActive = true, activeColor = LuxeColors.Green, onClick = { onShare(state.channel, state.subtone, state.myProRole, null); triggerUiSound("click") }) }
            }

            Spacer(Modifier.height(24.dp))

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp), // Aumentado de 110 a 120
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(if (isTransmitting) 0.97f else 1f)
                        .pointerInput(state.isInterfaceLocked) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (isPttBlockedByRx || rx) {
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
                                            isCurrentTouchDenied = false
                                        }
                                    } else {
                                        tryAwaitRelease()
                                        isCurrentTouchDenied = false
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(40.dp), // Aumentado radio
                    color = if (isTransmitting) Color.Red.copy(0.2f) else if (rx) Color.Green.copy(0.15f) else Color.White.copy(0.05f),
                    border = BorderStroke(3.dp, if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White.copy(0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isTransmitting || rx) {
                            val waveColor = if (isTransmitting) Color.Red else Color.Green
                            repeat(3) { i ->
                                val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.8f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "Wave")
                                val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "WaveAlpha")
                                Box(Modifier.fillMaxSize().scale(scale).border(2.dp, waveColor.copy(alpha), RoundedCornerShape(40.dp)))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Mic, 
                                null, 
                                tint = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White.copy(0.3f), 
                                modifier = Modifier.size(36.dp) // Aumentado de 28 a 36
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                if (isTransmitting) "ON AIR" else if (rx) "AIRE: RECIBIENDO" else "PULSAR PARA HABLAR",
                                color = if (isTransmitting) Color.Red else if (rx) Color.Green else Color.White, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 18.sp, // Aumentado de 15 a 18
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Surface(
                    onClick = { 
                        if (!state.isInterfaceLocked) {
                            pttLocked = !pttLocked
                            onStateChange(state.copy(isPttLatched = pttLocked)) 
                            triggerUiSound("switch")
                        }
                    },
                    modifier = Modifier.size(120.dp), // Aumentado de 110 a 120
                    shape = RoundedCornerShape(40.dp),
                    color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f),
                    border = BorderStroke(3.dp, if (pttLocked) Color.Red else Color.White.copy(0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, 
                            null, 
                            tint = if (pttLocked) Color.Red else Color.White.copy(0.3f),
                            modifier = Modifier.size(40.dp) // Aumentado de 32 a 40
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            val activeRooms = remember(users, state.city, state.channel) {
                val rooms = users.filter { it.city == state.city && it.channel != "GENERAL" }
                    .groupBy { it.channel }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activeRooms) { (room, count) ->
                            val isCurrent = state.channel == room
                            val isGeneral = room == "GENERAL"
                            
                            Surface(
                                onClick = { if (!state.isInterfaceLocked) onStateChange(state.copy(channel = room)) },
                                modifier = Modifier.height(56.dp), // Aumentado de 44 a 56
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCurrent) LuxeColors.Gold.copy(0.15f) else if (isGeneral) LuxeColors.ElectricBlue.copy(0.1f) else Color.White.copy(0.05f),
                                border = BorderStroke(1.5.dp, if (isCurrent) LuxeColors.Gold else if (isGeneral) LuxeColors.ElectricBlue.copy(0.4f) else Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (isGeneral) Icon(Icons.Rounded.Home, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(20.dp))
                                    if (isGeneral) Spacer(Modifier.width(10.dp))
                                    
                                    Text(
                                        text = if (isGeneral) "SALIR AL CANAL PÚBLICO" else room, 
                                        color = if (isGeneral) LuxeColors.ElectricBlue else Color.White, 
                                        fontSize = 14.sp, // Aumentado de 11 a 14
                                        fontWeight = FontWeight.Black
                                    )
                                    
                                    if (count > 0) {
                                        Spacer(Modifier.width(10.dp))
                                        Box(Modifier.background(if (isCurrent) LuxeColors.Gold else Color.White.copy(0.2f), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            Text(count.toString(), color = if (isCurrent) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp).clickable { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.CREATE_CHANNEL) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Text(if (state.channel == "GENERAL") "CANAL PÚBLICO EN ${state.city}" else "OPERADORES EN BARRIO ${state.channel}", color = Color.White.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
            
            val allToShow = remember(nick, isTransmitting, state.city, state.channel, state.subtone, mappedUsers) {
                (listOf(
                    RemoteUser(id = "me", nick = nick, isTransmitting = isTransmitting, city = state.city, channel = state.channel, subtone = state.subtone, isFriend = false),
                    RemoteUser(id = "bot_system", nick = "CONTROL", city = state.city, channel = state.channel, proRole = "SISTEMA", isWorkAvailable = true)
                ) + mappedUsers.filter { it.city == state.city && it.channel == state.channel && it.nick != nick }).sortedByDescending { it.isTransmitting || it.isFriend }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().height(180.dp), // Aumentado de 160 a 180
                contentPadding = PaddingValues(vertical = 12.dp), 
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

            Spacer(Modifier.height(180.dp)) 
            }
        }

        if (radarActivo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 12.dp, bottom = 120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp) // Aumentado de 10 a 12
                        .height(180.dp) // Aumentado de 150 a 180
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.3f))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(6.dp))
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
        
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).background(LuxeColors.Gold.copy(0.1f), CircleShape).border(1.5.dp, LuxeColors.Gold.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (target != null) Icons.Rounded.VpnKey else Icons.Rounded.Groups, null, tint = LuxeColors.Gold, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = if (target != null) "TERMINAL PRIVADA" else "SALA GENERAL", color = LuxeColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(text = target ?: "TODOS LOS OPERADORES", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).background(Color.White.copy(0.05f), CircleShape)) { 
                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(24.dp)) 
                }
            }
            
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = Color.White.copy(0.05f), modifier = Modifier.size(100.dp))
                        Text("ESPERANDO ACTIVIDAD...", color = Color.White.copy(0.1f), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(messages) { msg ->
                        val isAnuncio = msg.text.startsWith("ANUNCIO:")
                        val isMe = msg.senderNick.trim().uppercase() == myNick.trim().uppercase()
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { }, onLongClick = { if (isMe) { messageToDelete = msg; triggerUiSound("click") } }),
                            color = if (isAnuncio) LuxeColors.Gold.copy(0.12f) else Color.White.copy(0.04f),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, if (isAnuncio) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.08f))
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(msg.senderNick, color = if (isAnuncio) LuxeColors.Gold else Color(0xFF22D3EE), fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                                        if (isAnuncio) { Spacer(Modifier.width(12.dp)); Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp)) }
                                    }
                                    if (isMe) Icon(Icons.Rounded.MoreVert, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(20.dp))
                                }
                                Text(msg.text.replace("ANUNCIO: ", ""), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 10.dp), lineHeight = 26.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { if (!currentText.text.contains("ANUNCIO:")) onTextChange(androidx.compose.ui.text.input.TextFieldValue("ANUNCIO: " + currentText.text)) }, shape = RoundedCornerShape(16.dp), color = LuxeColors.Gold.copy(0.15f), border = BorderStroke(1.5.dp, LuxeColors.Gold.copy(0.4f))) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text("ENVIAR AVISO VOZ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                Surface(onClick = { showEmojiPicker = !showEmojiPicker }, shape = CircleShape, color = if (showEmojiPicker) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f), border = BorderStroke(1.5.dp, if (showEmojiPicker) LuxeColors.Gold else Color.White.copy(0.1f)), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.SentimentSatisfiedAlt, null, tint = if (showEmojiPicker) LuxeColors.Gold else Color.White.copy(0.6f), modifier = Modifier.size(24.dp)) }
                }
            }

            if (currentText.text.contains("ANUNCIO:")) {
                Text("📢 ¡MENSAJE DE VOZ AL AIRE! Este texto será leído por todos los compañeros.", color = LuxeColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            }

            AnimatedVisibility(visible = showEmojiPicker) {
                val emojis = listOf("👍", "😎", "📻", "🏍️", "👋", "🔥", "⚠️", "🆘", "👏", "✅")
                LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(emojis) { emoji ->
                        Surface(onClick = { val newText = currentText.text + emoji; onTextChange(androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newText.length))); triggerUiSound("click") }, shape = CircleShape, color = Color.White.copy(0.08f), modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
                        }
                    }
                }
            }
            
            Surface(modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(24.dp), color = Color.White.copy(0.07f), border = BorderStroke(1.5.dp, Color.White.copy(0.12f))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (currentText.text.isEmpty()) Text("ESCRIBE AQUÍ...", color = Color.White.copy(0.25f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        BasicTextField(value = currentText, onValueChange = onTextChange, textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold), cursorBrush = SolidColor(LuxeColors.Gold), modifier = Modifier.fillMaxWidth().onKeyEvent { if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) { onSend(); true } else false }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions(onSend = { onSend() }))
                    }
                    Surface(onClick = onSend, color = LuxeColors.Gold, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Rounded.Send, null, tint = Color.Black, modifier = Modifier.size(24.dp)) }
                    }
                }
            }
        }

        if (messageToDelete != null) {
            AlertDialog(onDismissRequest = { messageToDelete = null }, containerColor = LuxeColors.DeepSea, title = { Text("¿ELIMINAR MENSAJE?", color = Color.White, fontWeight = FontWeight.Black) }, text = { Text("Esta acción borrará el mensaje para todos los operadores.", color = Color.White.copy(0.7f)) }, confirmButton = { TextButton(onClick = { onDeleteMessage(messageToDelete!!.id, target); messageToDelete = null; triggerUiSound("click") }) { Text("BORRAR", color = Color.Red, fontWeight = FontWeight.Black) } }, dismissButton = { TextButton(onClick = { messageToDelete = null }) { Text("CANCELAR", color = Color.White.copy(0.4f)) } })
        }
    }
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
    bgStationName: String?,
    onBgRadioScan: (String, String) -> Unit,
    onBgRadioStop: () -> Unit = {},
    onBgVolumeChange: (Float) -> Unit = {},
    onBgGenreChange: (String) -> Unit = {},
    onNotification: (AppNotification) -> Unit = {},
    onGetHeading: () -> Float = { 0f },
    isBeeping: Boolean,
    externalPtt: Boolean = false,
    externalPttBlocked: Boolean = false,
    replayProgress: Float,
    isReplayReady: Boolean,
    onReplay: () -> Unit,
    onClose: () -> Unit,
    onFinish: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var isZoomed by remember { mutableStateOf(false) }
    var isMapVisible by remember { mutableStateOf(true) }
    var isHeadingUpEnabled by remember { mutableStateOf(false) }
    var routeKms by remember { mutableStateOf(0.0f) }
    var isPttBlockedByRx by remember { mutableStateOf(false) }

    // --- 👥 GESTIÓN DE PARTICIPANTES EN RUTA ---
    val routeParticipants = remember(users, state.channel) {
        users.filter { it.channel == state.channel && it.nick != nick }
    }
    
    // --- 📻 DETECTAR SI HAY MÚSICA EN EL GRUPO (DJ) ---
    val isMusicInGroup = routeParticipants.any { it.bgGenre != null }
    val routeDj = routeParticipants.find { it.bgGenre != null }

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

    // --- 🗺️ SINCRONIZACIÓN CON MAPA REAL (WEB) ---
    LaunchedEffect(users, state.channel, state.motoLatitude, state.motoLongitude, isMapVisible) {
        if (!isMapVisible) {
            onExecuteEngineeringAction("HIDE_MAP_OVERLAY")
            return@LaunchedEffect
        }

        // Incluimos a todos los de la ruta Y a nosotros mismos si tenemos GPS
        val participants = users.filter { it.channel == state.channel && it.nick != nick && it.lat != null && it.lon != null }
        val me = if (state.motoLatitude != null && state.motoLongitude != null) {
            """{"nick":"${nick} (YO)","lat":${state.motoLatitude},"lon":${state.motoLongitude},"isTransmitting":${(isPressed || voxActive || externalPtt)},"isMe":true}"""
        } else null

        val json = "[" + (listOfNotNull(me) + participants.map { 
            """{"nick":"${it.nick}","lat":${it.lat},"lon":${it.lon},"isTransmitting":${it.isTransmitting},"isMe":false}""" 
        }).joinToString(",") + "]"
        
        onExecuteEngineeringAction("UPDATE_MAP_MARKERS|$json")
        
        if (state.motoLatitude != null && state.motoLongitude != null) {
            onExecuteEngineeringAction("CENTER_MAP|${state.motoLatitude}|${state.motoLongitude}")
        }
    }

    // --- 🧭 MODO NAVEGACIÓN (RUMBO ARRIBA) ---
    LaunchedEffect(isHeadingUpEnabled, isMapVisible) {
        if (isHeadingUpEnabled && isMapVisible) {
            while(isHeadingUpEnabled && isMapVisible) {
                val h = onGetHeading()
                onExecuteEngineeringAction("ROTATE_MAP|$h")
                delay(150)
            }
        } else {
            onExecuteEngineeringAction("ROTATE_MAP|0")
        }
    }
    
    var lastLat by remember { mutableStateOf<Double?>(null) }
    var lastLon by remember { mutableStateOf<Double?>(null) }
    var showShareHighlight by remember { mutableStateOf(true) }

    // ELIMINADO: Sincronización forzada de música. Ahora cada uno elige.

    LaunchedEffect(Unit) {
        delay(8000)
        showShareHighlight = false
    }

    LaunchedEffect(state.motoLatitude, state.motoLongitude) {
        val lat = state.motoLatitude
        val lon = state.motoLongitude
        if (lat != null && lon != null) {
            if (lastLat != null && lastLon != null) {
                val dist = calculateDistanceKms(lastLat!!, lastLon!!, lat, lon)
                if (dist > 0.01) routeKms += dist.toFloat()
            }
            lastLat = lat
            lastLon = lon
        }
    }
    
    LaunchedEffect(Unit) {
        onExecuteEngineeringAction("SHOW_BANNER")
        delay(1000)
        onExecuteEngineeringAction("SPEAK|Modo Actividad iniciado. Optimizando audio y GPS para tu ruta.")
    }

    // --- 🛡️ FIX AMETRALLADORA: Separar intención de transmisión del pitido ---
    val effectivePtt = (isPressed || voxActive || externalPtt)
    val isTransmittingState = (effectivePtt && !isPttBlockedByRx) || isBeeping

    LaunchedEffect(effectivePtt) { 
        onMic(effectivePtt, state.veteranPower) 
    }

    val textMeasurer = rememberTextMeasurer()
    val radarLabelStyle = remember { TextStyle(color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Black) }

    // --- MI POSICIÓN (CENTRO) ANIMATIONS ---
    val centerTransition = rememberInfiniteTransition(label = "MyWaves")
    val waveAlpha by centerTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "WaveAlpha"
    )
    val waveRadiusFactor by centerTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "WaveRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { /* Bloqueo absoluto de clics al fondo */ }
            }
    ) {
        StarryBackground(activity = 0.4f)
        
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp)) // Más margen superior
            
            // --- 📜 HEADER ---
            Row(modifier = Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(28.dp)) }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (state.channel == "GENERAL") "MODO ACTIVIDAD" else "ACTIVIDAD: ${state.channel}", color = LuxeColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.basicMarquee())
                    Text(if (state.subtone != "0000") "CÓDIGO PRIVADO: ${state.subtone}" else "RED P2P ACTIVA", color = if (state.subtone != "0000") LuxeColors.Gold.copy(0.7f) else LuxeColors.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    onClick = { if (isReplayReady) onReplay(); triggerUiSound("click") },
                    color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (replayProgress > 0f) {
                            CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp, trackColor = Color.Transparent)
                        }
                        Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.width(8.dp))
                
                // --- 🛡️ BOTÓN WHATSAPP DIRECTO ---
                IconButton(onClick = { onShare(state.channel, state.subtone, "ACTIVITY", "WhatsApp") }) {
                    Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = LuxeColors.Green, modifier = Modifier.size(32.dp))
                }

                Spacer(Modifier.width(4.dp))
                
                // --- 📤 BOTÓN COMPARTIR GENERAL ---
                IconButton(onClick = { onShare(state.channel, state.subtone, "ACTIVITY", state.activeProfile.name) }) {
                    Icon(Icons.Rounded.Share, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(24.dp))
                }

                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isGpsPrivacyEnabled) {
                            Icon(Icons.Rounded.Security, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("KMS", color = LuxeColors.Gold.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                    Text(text = "${(routeKms * 10).toInt() / 10.0}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(LuxeColors.Gold.copy(0.3f), blurRadius = 8f)))
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- 🗺️ ZONA CENTRAL: RADAR + BOTONES LATERALES ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF020617))
                    .border(2.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                // CAPA DE MAPA REAL (Fondo absoluto)
                Box(Modifier.fillMaxSize()) {
                    LaunchedEffect(Unit) {
                        delay(500)
                        onExecuteEngineeringAction("INIT_REAL_MAP|activity-map-container")
                    }
                }

                val currentUsers = users.filter { it.channel == state.channel && it.nick != nick && it.lat != null && it.lon != null }
                
                // 2. CAPA DE RADAR (CANVAS TRANSPARENTE)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2.2f
                    
                    drawCircle(Color.White.copy(0.05f), radius = radius, style = Stroke(1.dp.toPx()))
                    drawCircle(Color.White.copy(0.03f), radius = radius * 0.66f, style = Stroke(1.dp.toPx()))
                    drawCircle(Color.White.copy(0.01f), radius = radius * 0.33f, style = Stroke(1.dp.toPx()))
                    
                    val myLat = state.motoLatitude
                    val myLon = state.motoLongitude
                    
                    if (myLat != null && myLon != null) {
                        val radarRangeKm = if (isZoomed) 5.0 else 25.0
                        
                        currentUsers.forEach { user ->
                            val dist = calculateDistanceKms(myLat, myLon, user.lat!!, user.lon!!)
                            if (dist < radarRangeKm) {
                                val bearing = calculateBearing(myLat, myLon, user.lat!!, user.lon!!)
                                val angleRad = (bearing - 90.0) * PI / 180.0
                                val normalizedDist = (dist / radarRangeKm).toFloat()
                                
                                val blipX = center.x + (cos(angleRad) * radius * normalizedDist).toFloat()
                                val blipY = center.y + (sin(angleRad) * radius * normalizedDist).toFloat()
                                
                                val color = if (user.isTransmitting) Color.Red else LuxeColors.Gold
                                if (user.isTransmitting) {
                                    drawCircle(color.copy(0.3f), radius = 12.dp.toPx(), center = Offset(blipX, blipY))
                                }
                                drawCircle(color, radius = 6.dp.toPx(), center = Offset(blipX, blipY))
                                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(blipX, blipY))
                                
                                val distText = if (dist < 1.0) "${(dist * 1000).toInt()}m" else "${(dist * 10).toInt() / 10.0}km"
                                val labelText = "${user.nick} ($distText)"
                                val textLayout = textMeasurer.measure(labelText, radarLabelStyle)
                                drawText(textLayout, topLeft = Offset(blipX - (textLayout.size.width / 2), blipY + 10.dp.toPx()))
                            }
                        }
                        
                        if (isTransmittingState) {
                            drawCircle(Color.Red.copy(alpha = waveAlpha), radius = radius * waveRadiusFactor, center = center, style = Stroke(2.dp.toPx()))
                            drawCircle(Color.Red.copy(alpha = waveAlpha * 0.5f), radius = radius * waveRadiusFactor * 0.7f, center = center, style = Stroke(1.dp.toPx()))
                        }

                        drawCircle(LuxeColors.ElectricBlue.copy(0.2f), radius = 20.dp.toPx(), center = center)
                        drawCircle(LuxeColors.ElectricBlue.copy(0.4f), radius = 12.dp.toPx(), center = center)
                    }
                }

                // 3. ICONO CENTRAL (FIJO PARA EVITAR SALTOS)
                Box(
                    modifier = Modifier.size(100.dp), // Aumentado para el nombre
                    contentAlignment = Alignment.Center
                ) {
                    val innerTransition = rememberInfiniteTransition(label = "RadarCenter")
                    val glowScale by innerTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.4f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                        label = "Glow"
                    )
                    
                    // --- 🏷️ TU NOMBRE SOBRE TU ICONO ---
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = nick,
                            color = LuxeColors.Gold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Box(contentAlignment = Alignment.Center) {
                            if (isTransmittingState || rx) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .scale(glowScale)
                                        .background((if (isTransmittingState) Color.Red else Color.Green).copy(0.2f), CircleShape)
                                        .blur(15.dp)
                                )
                            }

                            Icon(
                                imageVector = getActivityIcon(state.activeProfile),
                                contentDescription = null,
                                tint = if (isTransmittingState) Color.Red else if (rx) Color.Green else Color.White,
                                modifier = Modifier.size(24.dp).scale(if (isTransmittingState) 1.2f else 1f)
                            )
                        }
                    }
                }
                
                // 4. GPS SEARCH OVERLAY
                if (state.motoLatitude == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable { 
                            onExecuteEngineeringAction("VIBRATE|50")
                            onGpsRequest { }
                        }, 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val gpsTransition = rememberInfiniteTransition()
                            val alpha by gpsTransition.animateFloat(
                                initialValue = 0.4f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse)
                            )
                            Icon(Icons.Rounded.GpsFixed, null, tint = LuxeColors.Gold, modifier = Modifier.size(64.dp).alpha(alpha))
                            Spacer(Modifier.height(16.dp))
                            Text("BÚSQUEDA DE SEÑAL GPS...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Toca para activar ubicación ahora", color = Color.White.copy(0.6f), fontSize = 12.sp)
                        }
                    }
                }

                // 5. BOTONES LATERALES (REPOSICIONADOS PARA EVITAR BORDES)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 22.dp).padding(vertical = 24.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TacticalDockIconActivity(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) onStateChange(state.copy(isVoxEnabled = false)) else onPendingDialogChange(RadioDialogType.VOX) })
                    TacticalDockIconActivity(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE) })
                    TacticalDockIconActivity(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)) })
                    TacticalDockIconActivity(
                        icon = if (state.isGpsPrivacyEnabled) Icons.Rounded.Security else Icons.Rounded.LocationOff, 
                        label = "ZONA", 
                        isActive = state.isGpsPrivacyEnabled, 
                        onClick = { 
                            val newState = !state.isGpsPrivacyEnabled
                            onStateChange(state.copy(isGpsPrivacyEnabled = newState)) 
                            if (newState) onPendingDialogChange(RadioDialogType.HELP_PRIVACY)
                        }, 
                        activeColor = LuxeColors.ElectricBlue
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 22.dp).padding(vertical = 24.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TacticalDockIconActivity(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { if (state.isDspEnabled) onStateChange(state.copy(isDspEnabled = false)) else onPendingDialogChange(RadioDialogType.DSP) })
                    
                    // --- 📻 BOTÓN RADIO: Parpadea si hay música en el grupo ---
                    TacticalDockIconActivity(
                        icon = Icons.Rounded.Radio, 
                        label = "RADIO", 
                        isActive = bgStationName != null, 
                        isBlinking = bgStationName == null && isMusicInGroup,
                        onClick = { 
                            if (bgStationName != null) {
                                onBgRadioStop() 
                            } else {
                                // Si hay música en el grupo, sugerimos ese género al abrir
                                if (routeDj?.bgGenre != null) {
                                    onStateChange(state.copy(bgRadioGenre = routeDj.bgGenre))
                                }
                                onPendingDialogChange(RadioDialogType.FMSCAN) 
                            }
                        }
                    )
                    
                    // --- 🔋 BOTÓN MAPA (AHORRO BATERÍA) ---
                    TacticalDockIconActivity(
                        icon = if (isMapVisible) Icons.Rounded.Map else Icons.Rounded.LayersClear, 
                        label = "MAPA", 
                        isActive = isMapVisible, 
                        onClick = { isMapVisible = !isMapVisible; triggerUiSound("switch") },
                        activeColor = if (isMapVisible) LuxeColors.Gold else Color.Gray
                    )

                    // --- 🧭 BOTÓN RUMBO (NAVEGACIÓN) ---
                    TacticalDockIconActivity(
                        icon = if (isHeadingUpEnabled) Icons.Rounded.Explore else Icons.Rounded.CompassCalibration, 
                        label = "RUMBO", 
                        isActive = isHeadingUpEnabled, 
                        onClick = { 
                            if (isMapVisible) {
                                isHeadingUpEnabled = !isHeadingUpEnabled
                                triggerUiSound("switch") 
                            } else {
                                onNotification(AppNotification("MAPA APAGADO", "Activa el mapa para usar el modo Rumbo.", NotificationType.Info))
                            }
                        },
                        activeColor = LuxeColors.ElectricBlue
                    )

                    TacticalDockIconActivity(icon = if (isZoomed) Icons.Rounded.ZoomOutMap else Icons.Rounded.ZoomIn, label = "ZOOM", isActive = isZoomed, onClick = { isZoomed = !isZoomed; triggerUiSound("click") })
                    
                    // --- 🗺️ BOTÓN GOOGLE MAPS EXTERNO ---
                    val mapsUrl = state.myGpsUrl
                    TacticalDockIconActivity(
                        icon = Icons.Rounded.Explore, 
                        label = "MAPS", 
                        isActive = mapsUrl != null, 
                        onClick = { 
                            if (mapsUrl != null) {
                                uriHandler.openUri(mapsUrl)
                                triggerUiSound("click")
                            } else {
                                onNotification(AppNotification("BÚSQUEDA GPS", "Aún no tenemos tu posición. Toca el centro del radar para forzar la búsqueda.", NotificationType.Warning))
                            }
                        },
                        activeColor = LuxeColors.Green
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- 👥 LISTA DE PARTICIPANTES EN RUTA ---
            if (routeParticipants.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(routeParticipants) { user ->
                        val hasGps = user.lat != null && user.lon != null
                        Surface(
                            onClick = { 
                                if (hasGps) {
                                    onExecuteEngineeringAction("CENTER_MAP|${user.lat}|${user.lon}")
                                    triggerUiSound("click")
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (user.isTransmitting) Color.Red.copy(0.2f) else Color.White.copy(0.05f),
                            border = BorderStroke(1.5.dp, if (user.isTransmitting) Color.Red else if (hasGps) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (user.isTransmitting) {
                                        val infinite = rememberInfiniteTransition()
                                        val scale by infinite.animateFloat(1f, 1.4f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
                                        Box(Modifier.size(10.dp).scale(scale).background(Color.Red, CircleShape))
                                    } else {
                                        Icon(
                                            if (hasGps) Icons.Rounded.GpsFixed else Icons.Rounded.GpsOff,
                                            null,
                                            tint = if (hasGps) LuxeColors.Gold else Color.White.copy(0.3f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    user.nick,
                                    color = if (user.isTransmitting) Color.Red else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                // --- 🎵 ICONO DJ ---
                                if (user.bgGenre != null) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Rounded.MusicNote, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // --- 🛠️ PTT ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .pointerInput(state.isInterfaceLocked) {
                        if (!state.isInterfaceLocked) {
                            coroutineScope {
                                detectTapGestures(
                                    onPress = { offset ->
                                        if (isPttBlockedByRx || rx) {
                                            triggerUiSound("static")
                                        }

                                        if (!isPttBlockedByRx && !state.isInterfaceLocked && !rx) {
                                            val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                            launch { interactionSource.emit(press) }
                                            try {
                                                tryAwaitRelease()
                                            } finally {
                                                launch { interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press)) }
                                            }
                                        } else {
                                            tryAwaitRelease()
                                        }
                                    }
                                )
                            }
                        }
                    }, 
                shape = RoundedCornerShape(32.dp), 
                color = if (isTransmittingState) Color.Red.copy(0.2f) else if (rx) Color.Green.copy(0.15f) else Color.White.copy(0.08f), 
                border = BorderStroke(3.dp, if (isTransmittingState) Color.Red else if (rx) Color.Green else Color.White.copy(0.2f))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if (isTransmittingState) Icons.Rounded.Mic else if (rx) Icons.Rounded.VolumeUp else Icons.Rounded.MicNone, contentDescription = null, tint = if (isTransmittingState) Color.Red else if (rx) Color.Green else Color.White, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.width(20.dp))
                        Text(if (isTransmittingState) "HABLANDO (AIRE)" else if (rx) "AIRE: RECIBIENDO" else "PULSAR PARA HABLAR", color = if (isTransmittingState) Color.Red else if (rx) Color.Green else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // ESPACIO PARA BANNER (ANDROID)
            Spacer(Modifier.height(70.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TacticalDockIconActivity(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isBlinking: Boolean = false,
    onClick: () -> Unit,
    activeColor: Color = LuxeColors.Gold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkAnim")
    val alpha by if (isBlinking) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "Alpha"
        )
    } else remember { mutableStateOf(1f) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer(alpha = alpha)) {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick(); triggerUiSound("click") },
            color = if (isActive) activeColor.copy(0.2f) else if (isBlinking) LuxeColors.ElectricBlue.copy(0.15f) else Color.Black.copy(0.6f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, if (isActive) activeColor else if (isBlinking) LuxeColors.ElectricBlue else Color.White.copy(0.15f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isActive) activeColor else Color.White, 
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) LuxeColors.Gold else Color.White.copy(0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TacticalDockIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    activeColor: Color = LuxeColors.Gold
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .requiredSize(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            color = if (isActive) activeColor.copy(0.15f) else Color.White.copy(0.05f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, if (isActive) activeColor else Color.White.copy(0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isActive) activeColor else Color.White, 
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isActive) LuxeColors.Gold else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TechLabel(label: String, value: String, valueColor: Color = Color.White, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}
