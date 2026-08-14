package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PANTALLAS DE NAVEGACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 3.1 (NEXUS ONLY)
 * 
 * Gestiona el renderizado de la pantalla de Bienvenida, Carga y Radio.
 * NIVEL DE PROTECCIÓN 0: PROHIBIDO ALTERAR EL DISEÑO NEXUS O LA LÓGICA DE CANALES.
 * Blindado contra modificaciones en la estructura de la interfaz principal.
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
import androidx.compose.ui.graphics.drawscope.clipRect
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
                "Sincronizando red de ciudades",
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
                fontSize = 24.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(32.dp))

            // --- 🧪 INPUT GLASSMORPHISM ---
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(0.04f),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
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
                            fontWeight = FontWeight.Black, 
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
                    letterSpacing = 2.sp,
                    fontSize = 16.sp,
                    color = Color.Black
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
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit,
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
                    onPendingDialogChange(RadioDialogType.PORTADORA, null)
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
    val mappedUsers = remember(users, state.friends) { users.map { it.copy(isFriend = state.friends.contains(it.nick)) } }

    LaunchedEffect(rx) { if (rx) { while (true) { voiceModulation = ((-5..5).random() / 500f); delay(100) } } else { voiceModulation = 0f } }

    val isQrmAudible = !rx && !isTransmitting && !isBeeping && state.rfGain > state.squelch
    LaunchedEffect(isQrmAudible) {
        if (isQrmAudible) {
            while (true) {
                meterJitter = ((-3..3).random() / 100f) // Oscilación del QRM (3%)
                delay(60)
            }
        } else {
            meterJitter = 0f
        }
    }

    val currentOnMic by rememberUpdatedState(onMic)
    val currentOnNoise by rememberUpdatedState(onNoise)
    
    LaunchedEffect(effectivePtt) { currentOnMic(effectivePtt, myDynamicPower) }
    LaunchedEffect(myDynamicPower) { if (effectivePtt) { currentOnMic(true, myDynamicPower) } }
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { currentOnNoise(noiseVol) }

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
        } else { pttTimer = 0 }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(EliteTheme.DeepGradient)) {
        StarryBackground(activity = if (isTransmitting || rx) 0.6f else 0.15f, isEcoMode = state.isEcoMode)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(16.dp).padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 🏷️ HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onExit) { Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color.Red.copy(0.6f)) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SELECT_NICK, null) }
                    ) {
                        Text("Tu indicativo: ", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(nick, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { if (!state.isInterfaceLocked && isReplayReady) onReplay(); triggerUiSound("click") },
                            color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)),
                            modifier = Modifier.size(38.dp) 
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (replayProgress > 0f) {
                                    CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp, trackColor = Color.Transparent)
                                }
                                Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(18.dp))
                            }
                        }
                        
                        Spacer(Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.05f))
                                .border(1.dp, Color.White.copy(0.1f), CircleShape)
                                .clickable { onPendingDialogChange(RadioDialogType.SETTINGS, null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 📟 PANTALLA DIGITAL ELITE "NEXUS" ---
                Surface(
                    modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)),
                    color = Color.Black.copy(0.7f),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val qrmIntensity = if (state.rfGain > state.squelch) (state.rfGain - state.squelch) else 0f
                        CentinelMonitor(state = state, isTransmitting = isTransmitting, rx = rx, level = if (isTransmitting || rx) mic else (qrmIntensity + meterJitter).coerceIn(0f, 1f), showLeds = false, modifier = Modifier.fillMaxSize().alpha(0.4f))

                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(modifier = Modifier.fillMaxWidth().weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.width(80.dp), horizontalAlignment = Alignment.Start) {
                                    val squelchColor by infiniteTransition.animateColor(
                                        initialValue = Color.White,
                                        targetValue = if (isQrmAudible) LuxeColors.Gold else Color.White,
                                        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
                                        label = "SquelchBlink"
                                    )

                                    TechLabel("SQUELCH", "${(state.squelch * 100).toInt()}%", valueColor = if (isQrmAudible) squelchColor else Color.White) { 
                                        onPendingDialogChange(RadioDialogType.SETTINGS, null) 
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    TechLabel("GANANCIA", "${(state.rfGain * 100).toInt()}%") { onPendingDialogChange(RadioDialogType.SETTINGS, null) }
                                }
                                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val statusText = when { rx -> "RECIBIENDO..."; isTransmitting || isBeeping -> "ON AIR"; else -> "EN ESPERA" }
                                    val statusColor = when { rx -> LuxeColors.Gold; isTransmitting || isBeeping -> Color.Red; else -> Color.White.copy(0.2f) }
                                    Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        val activeLevel = if(isTransmitting || rx) mic else (qrmIntensity + meterJitter).coerceIn(0f, 1f)
                                        repeat(12) { i ->
                                            val isActive = i < (activeLevel * 12)
                                            val ledColor = when { i > 9 -> Color.Red; i > 7 -> Color(0xFFFACC15); else -> LuxeColors.Gold }
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (isActive) ledColor else Color.White.copy(0.04f)).border(1.dp, if (isActive) ledColor.copy(0.2f) else Color.White.copy(0.01f), RoundedCornerShape(2.dp)))
                                        }
                                    }
                                    val topText = when {
                                        rx -> transmitterNick ?: "RECOGIENDO..."
                                        state.channel.trim().uppercase() != state.city.trim().uppercase() -> state.city
                                        else -> null 
                                    }

                                    if (topText != null) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = topText,
                                            color = if(rx) LuxeColors.Gold else Color.White,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.fillMaxWidth().basicMarquee()
                                        )
                                        Spacer(Modifier.height(6.dp))
                                    } else {
                                        // Espacio de seguridad para que el botón no se pegue a los vúmetros
                                        Spacer(Modifier.height(14.dp))
                                    }
                                    
                                    Surface(onClick = { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SELECT_CITY, null) }, modifier = Modifier.fillMaxWidth().height(44.dp), color = LuxeColors.Gold.copy(0.15f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.4f))) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                            Icon(Icons.Rounded.Home, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(text = state.channel, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f).basicMarquee())
                                        }
                                    }
                                }
                                Column(modifier = Modifier.width(80.dp).clip(RoundedCornerShape(8.dp)).clickable { onPendingDialogChange(RadioDialogType.WATTS, null) }, horizontalAlignment = Alignment.End) {
                                    Text("WATTS", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    val wText = if (isTransmitting) "${(myDynamicPower * 15f).toInt()}W" else if(rx) "9.2W" else "0.0W"
                                    Text(wText, color = if(isTransmitting) Color.Red else if(rx) LuxeColors.Gold else Color.White.copy(0.2f), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).drawBehind { drawLine(Color.White.copy(0.1f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }.padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Surface(onClick = { if (!state.isInterfaceLocked) onPendingDialogChange(RadioDialogType.SUBTONO, null) }, color = if(state.subtone != "0000") LuxeColors.Gold.copy(0.1f) else Color.Transparent, shape = RoundedCornerShape(8.dp), border = if(state.subtone != "0000") BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)) else null) {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if(state.subtone == "0000") Icons.Rounded.LockOpen else Icons.Rounded.Lock, null, tint = if(state.subtone != "0000") LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (state.subtone != "0000") "CÓDIGO: ${state.subtone}" else "CANAL ABIERTO", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 🛠️ PANEL DE INSTRUMENTACIÓN (MINIMIZADO) ---
                var showTools by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "HERRAMIENTAS TÁCTICAS",
                        color = LuxeColors.Gold.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    
                    IconButton(
                        onClick = { showTools = !showTools; triggerUiSound("click") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (showTools) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            null,
                            tint = LuxeColors.Gold
                        )
                    }
                }

                AnimatedVisibility(visible = showTools) {
                    Column {
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            // --- BOTONES DINÁMICOS (CONSOLIDADOS) ---
                            item { 
                                TacticalDockIcon(
                                    icon = Icons.Rounded.Radio, 
                                    label = "RADIO FM", 
                                    isActive = bgStationName != null, 
                                    onClick = { onPendingDialogChange(RadioDialogType.FMSCAN, null); triggerUiSound("click") },
                                    onLongClick = { if (bgStationName != null) onBgRadioStop() else onBgRadioScan(state.city, state.bgRadioGenre); triggerUiSound("switch") }
                                ) 
                            }
                            item { 
                                TacticalDockIcon(
                                    icon = if(state.activeProfile != ActivityProfile.NORMAL) Icons.Rounded.Route else Icons.Rounded.Groups, 
                                    label = "RUTA", 
                                    isActive = state.activeProfile != ActivityProfile.NORMAL, 
                                    onClick = { 
                                        if (state.activeProfile == ActivityProfile.NORMAL) onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR, null) 
                                        else onActivityPanelRequest()
                                        triggerUiSound("click")
                                    },
                                    onLongClick = {
                                        if (state.activeProfile != ActivityProfile.NORMAL) {
                                            onPendingDialogChange(RadioDialogType.FINISH_ACTIVITY_CONFIRM, null)
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
                            
                            // --- AJUSTES SOCIALES / RED ---
                            item { 
                                TacticalDockIcon(
                                    icon = Icons.Rounded.Radar, 
                                    label = "RADAR", 
                                    isActive = true, 
                                    onClick = { onPendingDialogChange(RadioDialogType.RADAR_MAP, null); triggerUiSound("click") }
                                ) 
                            }
                            item { 
                                TacticalDockIcon(
                                    icon = Icons.Rounded.Settings, 
                                    label = "EQUIPO", 
                                    isActive = true, 
                                    onClick = { onPendingDialogChange(RadioDialogType.SETTINGS, null); triggerUiSound("click") }
                                ) 
                            }
                            
                            // --- AJUSTES TÉCNICOS ---
                            item { TacticalDockIcon(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) { onStateChange(state.copy(isVoxEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.VOX, null); triggerUiSound("click") } }) }
                            item { TacticalDockIcon(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)); triggerUiSound("switch") }) }
                            item { TacticalDockIcon(icon = Icons.Rounded.SettingsInputAntenna, label = "ECO", isActive = state.isReverbEnabled, onClick = { if (state.isReverbEnabled) { onStateChange(state.copy(isReverbEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.REVERB, null); triggerUiSound("click") } }) }
                            item { TacticalDockIcon(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { if (state.isDspEnabled) { onStateChange(state.copy(isDspEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.DSP, null); triggerUiSound("click") } }) }
                            item { TacticalDockIcon(icon = Icons.Rounded.Headset, label = "MONI", isActive = state.isMonitorEnabled, onClick = { if (state.isMonitorEnabled) { onStateChange(state.copy(isMonitorEnabled = false)); triggerUiSound("switch") } else { onPendingDialogChange(RadioDialogType.MONI, null); triggerUiSound("click") } }) }
                            item { TacticalDockIcon(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE, null); triggerUiSound("click") }) }
                            item { TacticalDockIcon(icon = Icons.AutoMirrored.Rounded.Chat, label = "INVITAR", isActive = true, activeColor = LuxeColors.Green, onClick = { onPendingDialogChange(RadioDialogType.INVITE, null); triggerUiSound("click") }) }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 🎙️ PTT CENTRAL ---
                Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val pttMainColor = when { isTransmitting || isBeeping -> Color.Red; rx -> Color.Green; else -> Color.White }
                    Surface(modifier = Modifier.weight(1f).fillMaxHeight().pointerInput(state.isInterfaceLocked) { detectTapGestures(onPress = { offset -> if (!isPttBlockedByRx && !state.isInterfaceLocked && !rx) { val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset); pttInteractionSource.emit(press); try { tryAwaitRelease() } finally { pttInteractionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press)) } } else { tryAwaitRelease() } }) }, shape = RoundedCornerShape(40.dp), color = if (pttMainColor != Color.White) pttMainColor.copy(0.2f) else Color.White.copy(0.05f), border = BorderStroke(3.dp, if (pttMainColor != Color.White) pttMainColor else Color.White.copy(0.1f))) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (isTransmitting || rx) {
                                repeat(3) { i ->
                                    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.8f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "Wave")
                                    val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(1200, delayMillis = i * 400), RepeatMode.Restart), label = "WaveAlpha")
                                    Box(Modifier.fillMaxSize().scale(scale).border(2.dp, pttMainColor.copy(alpha), RoundedCornerShape(40.dp)))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Mic, null, tint = if (pttMainColor != Color.White) pttMainColor else Color.White.copy(0.3f), modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(when { isTransmitting || isBeeping -> "ON AIR"; rx -> "AIRE: RECIBIENDO"; else -> "PULSAR PARA HABLAR" }, color = if (pttMainColor != Color.White) pttMainColor else Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                    Surface(onClick = { if (!state.isInterfaceLocked) { pttLocked = !pttLocked; onStateChange(state.copy(isPttLatched = pttLocked)); triggerUiSound("switch") } }, modifier = Modifier.size(120.dp), shape = RoundedCornerShape(40.dp), color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f), border = BorderStroke(3.dp, if (pttLocked) Color.Red else Color.White.copy(0.1f))) {
                        Box(contentAlignment = Alignment.Center) { Icon(if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null, tint = if (pttLocked) Color.Red else Color.White.copy(0.3f), modifier = Modifier.size(40.dp)) }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --- 👥 USUARIOS (Refresco en tiempo real sin bloqueo de memoria) ---
                Text(if (state.channel == state.city) "ESTACIONES EN EL CANAL ${state.city}" else "OPERADORES EN ${state.channel}", color = Color.White.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
                
                val allToShow = remember(nick, isTransmitting, state.city, state.channel, mappedUsers) {
                    val myCity = state.city.trim().uppercase()
                    val myChannel = state.channel.trim().uppercase()
                    (listOf(
                        RemoteUser(id = "me", nick = nick, isTransmitting = isTransmitting, city = myCity, channel = myChannel), 
                        RemoteUser(id = "bot", nick = "CONTROL", city = myCity, channel = myChannel, proRole = "SISTEMA", isWorkAvailable = true)
                    ) + mappedUsers.filter { 
                        it.city.trim().uppercase() == myCity && 
                        it.channel.trim().uppercase() == myChannel && 
                        it.nick != nick 
                    }).sortedByDescending { it.isTransmitting }
                }
                LazyRow(modifier = Modifier.fillMaxWidth().height(180.dp), contentPadding = PaddingValues(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(allToShow) { user -> UserCard(user = user, isMe = user.id == "me", onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, onPrivateChat = { onPrivateChat(user.nick); onStateChange(state.copy(isChatVisible = true)) }, onReport = { onReport(user.id) }, onBlock = { onBlock(user.id) }, onClick = { if (user.nick == "CONTROL") onVirtualOperatorTrigger() }) }
                }

                Spacer(Modifier.height(180.dp)) 
            }
        }

        if (radarActivo) {
            Box(modifier = Modifier.fillMaxSize().padding(end = 12.dp, bottom = 120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.width(12.dp).height(180.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(0.3f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(6.dp))) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(radarNivel.coerceIn(0f, 1f)).align(Alignment.BottomCenter).background(brush = Brush.verticalGradient(listOf(Color.Red, LuxeColors.Gold, LuxeColors.ElectricBlue))))
                }
            }
        }

        if (state.isChatVisible) {
            Box(modifier = Modifier.fillMaxSize().clickable(enabled = false) { }) {
                EliteChatOverlay(messages = chatMessages, target = privateChatTarget, currentText = currentChatMessage, onTextChange = { currentChatMessage = it }, onSend = { if (currentChatMessage.text.isNotBlank()) { onSendMessage(currentChatMessage.text, privateChatTarget); currentChatMessage = androidx.compose.ui.text.input.TextFieldValue("") } }, onClose = { onStateChange(state.copy(isChatVisible = false)) }, onDeleteMessage = onDeleteMessage, myNick = nick)
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
                        Text(text = if (target != null) "TERMINAL PRIVADA" else "CANAL CIUDAD", color = LuxeColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(text = target ?: "CANAL PÚBLICO", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
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
                                        Text(msg.senderNick, color = if (isAnuncio) LuxeColors.Gold else Color.White.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
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

/** 
 * 🔒 PROTECTED CORE: MOTOR DE ACTIVIDAD Y NAVEGACIÓN TÁCTICA 
 * SELLADO TOTAL - PROHIBIDA MODIFICACIÓN DE CAPAS Y TRANSPARENCIAS
 */
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
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit,
    bgStationName: String?,
    onBgRadioScan: (String, String) -> Unit,
    onBgRadioStop: () -> Unit = {},
    onBgVolumeChange: (Float) -> Unit = {},
    onBgGenreChange: (String) -> Unit = {},
    onNotification: (AppNotification) -> Unit = {},
    onGetHeading: () -> Float = { 0f },
    nextInstruction: String? = null,
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
    var pttLocked by remember { mutableStateOf(state.isPttLatched) }

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

    LaunchedEffect(externalPtt) {
        if (externalPtt != pttLocked) {
            pttLocked = externalPtt
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
        } else {
            onExecuteEngineeringAction("SHOW_MAP_OVERLAY")
        }

        // Incluimos a todos los de la ruta Y a nosotros mismos (SIEMPRE, con o sin GPS)
        val participants = users.filter { it.channel == state.channel && it.nick != nick && it.lat != null && it.lon != null }
        val me = """{"nick":"${nick} (YO)","lat":${state.motoLatitude ?: "null"},"lon":${state.motoLongitude ?: "null"},"isTransmitting":${(isPressed || voxActive || externalPtt)},"isMe":true}"""

        val json = "[" + (listOf(me) + participants.map { 
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

    // --- 🔍 CONTROL DE ZOOM TÁCTICO ---
    LaunchedEffect(isZoomed) {
        onExecuteEngineeringAction("SET_MAP_ZOOM|${if(isZoomed) 18 else 14}")
    }

    // --- 🎙️ GUÍA POR VOZ TÁCTICA ---
    LaunchedEffect(nextInstruction) {
        if (nextInstruction != null) {
            onExecuteEngineeringAction("SPEAK|$nextInstruction")
        }
    }

    // --- 🛡️ FIX AMETRALLADORA: Separar intención de transmisión del pitido ---
    val effectivePtt = (isPressed || voxActive || externalPtt || pttLocked)
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

    var mapHole by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo base NEGRO TOTAL para evitar transparencias accidentales
            .pointerInput(Unit) {
                detectTapGestures { /* Bloqueo absoluto de clics al fondo */ }
            }
    ) {
        // Capa de Estrellas con el hueco del mapa
        StarryBackground(
            activity = 0.4f,
            hole = if (isMapVisible) mapHole else null
        )
        
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), // Reducido margen lateral
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp)) // Menos margen superior
            
            // --- 📜 HEADER ---
            Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) { // Reducido de 64 a 48
                Surface(
                    onClick = onClose,
                    color = LuxeColors.Red.copy(0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxeColors.Red.copy(0.3f))
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Flag, null, tint = LuxeColors.Red, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("FINALIZAR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).clickable { onPendingDialogChange(RadioDialogType.SELECT_CITY, null) }, 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.city, color = LuxeColors.Gold, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.basicMarquee())
                    Text(if (state.subtone != "0000") "CÓDIGO: ${state.subtone}" else "CANAL: ${state.channel}", color = Color.White.copy(0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    onClick = { if (isReplayReady) onReplay(); triggerUiSound("click") },
                    color = if (isReplayReady) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (isReplayReady) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (replayProgress > 0f) {
                            CircularProgressIndicator(progress = { replayProgress }, modifier = Modifier.fillMaxSize(), color = LuxeColors.Gold, strokeWidth = 2.dp, trackColor = Color.Transparent)
                        }
                        Icon(Icons.Rounded.History, null, tint = if (isReplayReady) LuxeColors.Gold else Color.White.copy(0.3f), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.width(8.dp))
                
                Spacer(Modifier.width(8.dp))
                
                // --- 📤 BOTÓN COMPARTIR ÚNICO (ESTÁNDAR TÁCTICO) ---
                IconButton(onClick = { onShare(state.channel, state.subtone, "ACTIVITY", state.activeProfile.name) }) {
                    Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold.copy(0.8f), modifier = Modifier.size(24.dp)) // Reducido de 28 a 24
                }

                Spacer(Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isGpsPrivacyEnabled) {
                            Icon(Icons.Rounded.Security, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(8.dp))
                            Spacer(Modifier.width(2.dp))
                        }
                        Text("KMS", color = LuxeColors.Gold.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Text(text = "${(routeKms * 10).toInt() / 10.0}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(8.dp)) // Reducido de 12 a 8

            // --- 🗺️ ZONA CENTRAL: RADAR + BOTONES LATERALES ---
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 500.dp) 
                        .clip(RoundedCornerShape(28.dp))
                        // AGUJERO REAL: Usamos BlendMode.Clear para borrar el fondo justo en esta tarjeta
                        .drawBehind {
                            if (isMapVisible) {
                                drawRect(color = Color.Transparent, blendMode = BlendMode.Clear)
                            }
                        }
                        .background(if (isMapVisible) Color.Transparent else Color(0xFF020619).copy(alpha = 0.8f))
                        .border(2.dp, if (isMapVisible) LuxeColors.Green.copy(0.5f) else LuxeColors.Gold.copy(0.3f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                /* CAPA DE MAPA REAL (Fondo absoluto) */
                Box(Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .onGloballyPositioned { layoutCoordinates ->
                        if (isMapVisible) {
                            val position = layoutCoordinates.positionInWindow()
                            val size = layoutCoordinates.size
                            
                            // Guardar para el StarryBackground (unidades Compose Pixels)
                            mapHole = androidx.compose.ui.geometry.Rect(
                                position.x, position.y, 
                                position.x + size.width, position.y + size.height
                            )
                            
                            // Enviar a la Web (Valores directos para CSS)
                            println("DEBUG MAP: Sending geometry ${position.x}, ${position.y}, ${size.width}x${size.height}")
                            onExecuteEngineeringAction("UPDATE_MAP_GEOMETRY|${position.x}|${position.y}|${size.width}|${size.height}")
                        }
                    }
                ) {
                    LaunchedEffect(Unit) {
                        delay(800)
                        val lat = state.motoLatitude ?: 37.3891
                        val lon = state.motoLongitude ?: -5.9845
                        onExecuteEngineeringAction("INIT_REAL_MAP|$lat|$lon")
                    }
                }

                val currentUsers = users.filter { it.channel == state.channel && it.nick != nick && it.lat != null && it.lon != null }
                
                // 2. CAPA DE RADAR (CANVAS TRANSPARENTE)
                if (!isMapVisible) {
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

                // --- 📟 HUD TÁCTICO DE NAVEGACIÓN ---
                if (state.routeDestinationName != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(0.7f))
                            .border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.routeDestinationName.uppercase(),
                                color = LuxeColors.Gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Route, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(state.routeDistanceKm ?: "-- KM", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.Rounded.Schedule, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(state.routeDurationMin ?: "-- MIN", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            
                            // --- 🎙️ INDICACIÓN VISUAL DE VOZ ---
                            if (state.nextNavigationStep != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = state.nextNavigationStep,
                                    color = LuxeColors.ElectricBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    maxLines = 2
                                )
                            }
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

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp).padding(vertical = 8.dp)
                        .background(Color.Black.copy(0.8f), RoundedCornerShape(20.dp))
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState()), 
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TacticalDockIconActivity(icon = Icons.Rounded.Mic, label = "VOX", isActive = state.isVoxEnabled, onClick = { if (state.isVoxEnabled) onStateChange(state.copy(isVoxEnabled = false)) else onPendingDialogChange(RadioDialogType.VOX, null) })
                    TacticalDockIconActivity(icon = if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, label = "DISC", isActive = state.isDiscreteModeEnabled, onClick = { onPendingDialogChange(RadioDialogType.DISCRETE, null) })
                    TacticalDockIconActivity(icon = Icons.Rounded.MusicNote, label = "BEEP", isActive = state.isRogerBeepEnabled, onClick = { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)) })
                    TacticalDockIconActivity(
                        icon = if (state.isGpsPrivacyEnabled) Icons.Rounded.Security else Icons.Rounded.LocationOff, 
                        label = "ZONA", 
                        isActive = state.isGpsPrivacyEnabled, 
                        onClick = { 
                            val newState = !state.isGpsPrivacyEnabled
                            onStateChange(state.copy(isGpsPrivacyEnabled = newState)) 
                            if (newState) onPendingDialogChange(RadioDialogType.HELP_PRIVACY, null)
                        }, 
                        activeColor = LuxeColors.ElectricBlue
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp).padding(vertical = 8.dp)
                        .background(Color.Black.copy(0.8f), RoundedCornerShape(20.dp))
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState()), 
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TacticalDockIconActivity(icon = Icons.Rounded.GraphicEq, label = "DSP", isActive = state.isDspEnabled, onClick = { if (state.isDspEnabled) onStateChange(state.copy(isDspEnabled = false)) else onPendingDialogChange(RadioDialogType.DSP, null) })
                    
                    TacticalDockIconActivity(
                        icon = Icons.Rounded.Radio, 
                        label = "RADIO", 
                        isActive = bgStationName != null, 
                        isBlinking = bgStationName == null && isMusicInGroup,
                        onClick = { 
                            if (bgStationName != null) onBgRadioStop() 
                            else {
                                if (routeDj?.bgGenre != null) onStateChange(state.copy(bgRadioGenre = routeDj.bgGenre))
                                onPendingDialogChange(RadioDialogType.FMSCAN, null) 
                            }
                        }
                    )
                    
                    // --- GRUPO MAPA COMPACTO ---
                    TacticalDockIconActivity(
                        icon = if (isMapVisible) Icons.Rounded.Map else Icons.Rounded.LayersClear, 
                        label = "MAPA", 
                        isActive = isMapVisible, 
                        onClick = { isMapVisible = !isMapVisible; triggerUiSound("switch") },
                        activeColor = if (isMapVisible) LuxeColors.Gold else Color.Gray
                    )

                    if (isMapVisible) {
                        TacticalDockIconActivity(icon = Icons.Rounded.Satellite, label = "SAT", isActive = false, onClick = { onExecuteEngineeringAction("TOGGLE_SATELLITE"); triggerUiSound("click") })
                        TacticalDockIconActivity(icon = Icons.Rounded.Traffic, label = "TRAFIC", isActive = false, onClick = { onExecuteEngineeringAction("TOGGLE_TRAFFIC"); triggerUiSound("click") })
                        TacticalDockIconActivity(icon = if (isZoomed) Icons.Rounded.ZoomOutMap else Icons.Rounded.ZoomIn, label = "ZOOM", isActive = isZoomed, onClick = { isZoomed = !isZoomed; triggerUiSound("click") })
                        TacticalDockIconActivity(icon = if (isHeadingUpEnabled) Icons.Rounded.Explore else Icons.Rounded.CompassCalibration, label = "RUMBO", isActive = isHeadingUpEnabled, onClick = { isHeadingUpEnabled = !isHeadingUpEnabled; triggerUiSound("switch") }, activeColor = LuxeColors.ElectricBlue)
                    }

                    if (state.routeDestinationName == null) {
                        TacticalDockIconActivity(icon = Icons.Rounded.AddLocationAlt, label = "RUTA", isActive = false, onClick = { onPendingDialogChange(RadioDialogType.ROUTE_PLANNER, null) }, activeColor = LuxeColors.Gold)
                    }

                    val mapsUrl = state.myGpsUrl
                    TacticalDockIconActivity(icon = Icons.Rounded.Explore, label = "MAPS", isActive = mapsUrl != null, onClick = { if (mapsUrl != null) uriHandler.openUri(mapsUrl) else onNotification(AppNotification("BÚSQUEDA GPS", "Sin posición fija.", NotificationType.Warning)) }, activeColor = LuxeColors.Green)
                }
            }
        }

            Spacer(Modifier.height(8.dp)) // Reducido de 12 a 8

            // --- 👥 LISTA DE PARTICIPANTES EN RUTA ---
            if (routeParticipants.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(48.dp), // Reducido de 60 a 48
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
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
                            shape = RoundedCornerShape(12.dp),
                            color = if (user.isTransmitting) Color.Red.copy(0.2f) else Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (user.isTransmitting) Color.Red else if (hasGps) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (user.isTransmitting) {
                                        val infinite = rememberInfiniteTransition()
                                        val scale by infinite.animateFloat(1f, 1.4f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
                                        Box(Modifier.size(8.dp).scale(scale).background(Color.Red, CircleShape))
                                    } else {
                                        Icon(
                                            if (hasGps) Icons.Rounded.GpsFixed else Icons.Rounded.GpsOff,
                                            null,
                                            tint = if (hasGps) LuxeColors.Gold else Color.White.copy(0.3f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    user.nick,
                                    color = if (user.isTransmitting) Color.Red else Color.White,
                                    fontSize = 11.sp, // Reducido de 13 a 11
                                    fontWeight = FontWeight.Black
                                )
                                // --- 🎵 ICONO DJ ---
                                if (user.bgGenre != null) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Rounded.MusicNote, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // --- 🛠️ PTT ---
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp), // Más compacto aún (80dp)
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pttMainColor = when {
                    isTransmittingState || isBeeping -> Color.Red
                    rx -> Color.Green
                    else -> Color.White
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                    shape = RoundedCornerShape(20.dp), 
                    color = if (pttMainColor != Color.White) pttMainColor.copy(0.2f) else Color.White.copy(0.08f), 
                    border = BorderStroke(2.dp, if (pttMainColor != Color.White) pttMainColor else Color.White.copy(0.2f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = if (pttMainColor == Color.Red || pttMainColor == Color(0xFFF97316)) Icons.Rounded.Mic else if (rx) Icons.Rounded.VolumeUp else Icons.Rounded.MicNone, contentDescription = null, tint = if (pttMainColor != Color.White) pttMainColor else Color.White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                when {
                                    isTransmittingState || isBeeping -> "AIRE"
                                    rx -> "RX"
                                    else -> "HABLAR"
                                }, 
                                color = if (pttMainColor != Color.White) pttMainColor else Color.White, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Black
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
                    modifier = Modifier.size(80.dp), 
                    shape = RoundedCornerShape(20.dp),
                    color = if (pttLocked) Color.Red.copy(0.2f) else Color.White.copy(0.05f),
                    border = BorderStroke(2.dp, if (pttLocked) Color.Red else Color.White.copy(0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (pttLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, 
                            null, 
                            tint = if (pttLocked) Color.Red else Color.White.copy(0.3f),
                            modifier = Modifier.size(28.dp)
                        )
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
                .size(40.dp) // Reducido de 48 a 40
                .clip(RoundedCornerShape(10.dp))
                .clickable { onClick(); triggerUiSound("click") },
            color = if (isActive) activeColor.copy(0.2f) else if (isBlinking) LuxeColors.ElectricBlue.copy(0.15f) else Color.Black.copy(0.6f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.2.dp, if (isActive) activeColor else if (isBlinking) LuxeColors.ElectricBlue else Color.White.copy(0.15f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isActive) activeColor else Color.White, 
                    modifier = Modifier.size(22.dp).graphicsLayer { // Reducido de 26 a 22
                        shadowElevation = 8f
                        shape = CircleShape
                        clip = false
                    }
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
