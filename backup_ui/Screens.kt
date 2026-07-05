package com.sagon.on

/**
 * 🔒 ON AIR SPAIN - CORE ARCHITECTURE
 * DESIGNED & ENGINEERED BY JOSE MANUEL GONZALEZ LORENCE (ELITE TIER DEVELOPER)
 * STACK: KOTLIN MULTIPLATFORM | WEBRTC | PREMIUM UI
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import org.jetbrains.compose.resources.painterResource
import on.shared.generated.resources.Res
import on.shared.generated.resources.logo
import on.shared.generated.resources.mapa_nacional
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions

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
    onConnect: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeRoot")
    val bgGlow by infiniteTransition.animateFloat(
        0.3f, 0.6f, 
        infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "BgGlow"
    )

    var showMicExplain by remember { mutableStateOf(false) }

    if (showMicExplain) {
        AlertDialog(
            onDismissRequest = { showMicExplain = false },
            containerColor = Color(0xFF0F172A),
            modifier = Modifier.border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(28.dp)),
            icon = { 
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(LuxeColors.ElectricBlue.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.VerifiedUser, 
                        null, 
                        tint = LuxeColors.ElectricBlue, 
                        modifier = Modifier.size(32.dp)
                    ) 
                }
            },
            title = { 
                Text(
                    "CONEXIÓN SEGURA", 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 2.sp,
                    color = Color.White
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Para transmitir tu voz a la red, el sistema requiere acceso al micrófono.", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(0.9f)
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Lock, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("SIN GRABACIONES: Tu voz es efímera.", fontSize = 11.sp, color = Color.White.copy(0.6f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Shield, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("CONTROL TOTAL: Solo tú decides cuándo hablar.", fontSize = 11.sp, color = Color.White.copy(0.6f))
                    }
                }
            },
            confirmButton = {
                LuxeButton(
                    text = "ACTIVAR MICRÓFONO",
                    onClick = { 
                        showMicExplain = false
                        onMicAccept()
                        // Disparamos una pulsación fantasma para que el navegador pida el permiso
                        onMicRequest(true, 0.7f)
                        onMicRequest(false, 0f)
                        onConnect()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    containerColor = LuxeColors.ElectricBlue,
                    contentColor = Color.White
                )
            }
        )
    }

    // Efecto de sonido al entrar (Sistema Listo)
    LaunchedEffect(Unit) {
        triggerUiSound("switch")
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))))
    ) {
        // Fondo de estrellas con aurora sutil azulada
        StarryBackground(activity = (totalUsers.toFloat() / 20f).coerceIn(0.1f, 1f), isEcoMode = false)
        
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            IconButton(
                onClick = onInstall,
                modifier = Modifier.size(24.dp).align(Alignment.TopEnd).clip(CircleShape).background(Color.White.copy(0.05f))
            ) {
                Icon(Icons.Rounded.AppShortcut, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(12.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 440.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 🖼️ LOGO INTEGRADO CON GLOW AZUL ---
            var showLogo by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(100); showLogo = true }

            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = OvershootInterpolator(1.5f).toEasing())),
            ) {
                val logoPulse by infiniteTransition.animateFloat(
                    1f, 1.05f, 
                    infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "LogoPulse"
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp) // Tamaño optimizado para evitar scroll
                        .scale(logoPulse)
                        .drawBehind {
                            // Aura azul eléctrica sutil (Sin fondo blanco)
                            drawCircle(
                                LuxeColors.ElectricBlue.copy(alpha = 0.15f * bgGlow),
                                radius = size.width * 0.8f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Logo On Air Spain",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 🔵 STATUS BANNER (AZUL PROFESIONAL) ---
            Surface(
                color = LuxeColors.ElectricBlue.copy(0.1f),
                shape = CircleShape,
                border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.2f))
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val dotAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))
                    Box(Modifier.size(5.dp).clip(CircleShape).background(LuxeColors.ElectricBlue.copy(alpha = dotAlpha)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (totalUsers > 0) "RED ACTIVA: $totalUsers ESTACIONES" else "SISTEMA ONLINE",
                        color = Color.White.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // --- ⌨️ PANEL DE IDENTIFICACIÓN COMPACTO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(LuxeColors.LiquidGlass)
                    .border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("IDENTIFÍCATE - INDICATIVO / NICK", color = LuxeColors.ElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nick, 
                    onValueChange = { 
                        if (it.length <= 12) {
                            // Filtro estricto: Solo A-Z y 0-9 para evitar errores en base de datos
                            val filtered = it.uppercase().filter { c -> c in 'A'..'Z' || c in '0'..'9' }
                            onNickChange(filtered)
                            if(filtered.isNotEmpty()) triggerUiSound("click")
                        }
                    },
                    placeholder = { Text("TU ESTACIÓN...", color = Color.White.copy(0.2f), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxeColors.ElectricBlue,
                        unfocusedBorderColor = Color.White.copy(0.1f),
                        cursorColor = LuxeColors.ElectricBlue
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                LuxeButton(
                    text = "ABRIR CANAL", 
                    onClick = { 
                        triggerUiSound("static")
                        if (!hasAcceptedMic) {
                            showMicExplain = true
                        } else {
                            onConnect() 
                        }
                    }, 
                    enabled = nick.length >= 3, 
                    modifier = Modifier.fillMaxWidth().height(54.dp), 
                    containerColor = LuxeColors.ElectricBlue, 
                    contentColor = Color.White
                )

                Spacer(Modifier.height(24.dp))
                
                // --- 🛡️ CLÁUSULA DE EXENCIÓN DE RESPONSABILIDAD (BLINDAJE TOTAL) ---
                Surface(
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Gavel, null, tint = LuxeColors.Gold, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("REGLAS DE LA RADIO", color = LuxeColors.Gold, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Esto es una radio para hablar entre personas. No somos una empresa de trabajo ni damos empleo. Lo que hables con otros o los tratos que hagas son responsabilidad tuya. ¡Usa el sentido común y sé educado!",
                            color = Color.White.copy(0.4f),
                            fontSize = 8.sp,
                            lineHeight = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text("LA RED DE VOZ MÁS AUTÉNTICA DE ESPAÑA", color = Color.White.copy(0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// Helper para easing de Overshoot (no disponible directo en compose core a veces)
private class OvershootInterpolator(val tension: Float) {
    fun toEasing() = Easing { x ->
        val t = x - 1.0f
        t * t * ((tension + 1) * t + tension) + 1.0f
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioPanel(
    nick: String, mic: Float, users: List<RemoteUser>, rx: Boolean, transmitterNick: String? = null, isBeeping: Boolean = false,
    isCodedRx: Boolean = false,
    onNoise: (Float) -> Unit, onMic: (Boolean, Float) -> Unit,
    onShare: (String, String, String?, String?) -> Unit, onExit: () -> Unit, state: RadioState, onStateChange: (RadioState) -> Unit,
    onLogoutConfirm: () -> Unit = {},
    onMinimizeRequest: () -> Unit = {},
    showExitConfirmExternal: Boolean = false,
    onExitConfirmDismiss: () -> Unit = {},
    externalPtt: Boolean = false,
    externalPttBlocked: Boolean = false,
    replayProgress: Float = 0f,
    isReplayReady: Boolean = false,
    chatMessages: List<ChatMessage> = emptyList(),
    onSendMessage: (String, String?) -> Unit = { _, _ -> },
    onPrivateChat: (String) -> Unit = {},
    onPublicChat: () -> Unit = {},
    onNotification: (AppNotification) -> Unit,
    onReport: (String) -> Unit = {},
    onBlock: (String) -> Unit = {},
    onReplay: () -> Unit = {},
    onGpsRequestPro: (callback: (String?) -> Unit) -> Unit = { it(null) }
) {
    var pttLocked by remember { mutableStateOf(state.isPttLatched) }
    var isPttBlockedByRx by remember { mutableStateOf(false) }
    var voxActive by remember { mutableStateOf(false) }
    var voxHangTimer by remember { mutableStateOf(0) }
    var isChatVisible by remember { mutableStateOf(false) }
    
    // --- 💬 CHAT ENGINE: FOCO Y TECLADO ---
    val chatFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isChatVisible) {
        if (isChatVisible) {
            delay(400) // Tiempo para que la animación Glassmorphism se asiente
            chatFocusRequester.requestFocus()
        }
    }

    // Sincronización con PTT Externo (Auriculares/Bluetooth)
    LaunchedEffect(externalPtt) {
        if (externalPtt != pttLocked) {
            pttLocked = externalPtt
        }
    }

    // Sincronización con bloqueo de sistema (JS/Llamadas)
    LaunchedEffect(externalPttBlocked) {
        if (externalPttBlocked) {
            isPttBlockedByRx = true
        }
    }

    // Reset del bloqueo visual tras un tiempo
    LaunchedEffect(isPttBlockedByRx) {
        if (isPttBlockedByRx) {
            delay(800)
            isPttBlockedByRx = false
        }
    }

    var meterJitter by remember { mutableStateOf(0f) }
    var pttTimer by remember { mutableStateOf(0L) }
    var currentChatMessage by remember { mutableStateOf("") }
    var privateChatTarget by remember { mutableStateOf<String?>(null) }
    var voiceModulation by remember { mutableStateOf(0f) }
    val pttInteractionSource = remember { MutableInteractionSource() }
    val isPttPressed by pttInteractionSource.collectIsPressedAsState()
    val pttScale by animateFloatAsState(if (isPttPressed) 0.95f else 1f, label = "PTTScale")

    var isEqualizerVisible by remember { mutableStateOf(false) }

    // --- 💬 AUTO-SCROLL ENGINE: MANTIENE LA TERMINAL AL DÍA ---
    val chatListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(chatMessages.size, isChatVisible) {
        if (isChatVisible && chatMessages.isNotEmpty()) {
            delay(100) // Pequeño margen para renderizado
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // --- ⚡ MOTOR DE POTENCIA DINÁMICA & PISOTÓN (GAMA ALTA) ---
    var myDynamicPower by remember { mutableStateOf(state.veteranPower) }
    val effectivePtt = isPttPressed || pttLocked || voxActive
    val isTransmitting = effectivePtt && pttTimer < 50

    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            // Empezamos desde nuestra base de veterano
            myDynamicPower = state.veteranPower 
            while (isTransmitting) {
                delay(1000)
                // Subimos potencia por tiempo de emisión (calentamiento)
                myDynamicPower = (myDynamicPower + 0.005f).coerceIn(state.veteranPower, 1.0f)
                
                // Bonificación de veteranía: Por cada 10 segundos hablando, 
                // subimos un poco la base permanente de veteranía
                if (myDynamicPower >= state.veteranPower + 0.01f) {
                    onStateChange(state.copy(
                        veteranPower = (state.veteranPower + 0.001f).coerceIn(0.7f, 0.95f),
                        lastActiveTimestamp = 0L // Se reseteará al guardar
                    ))
                }
            }
        } else {
            myDynamicPower = state.veteranPower
        }
    }

    // Detección de Colisiones (Pisotón)
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

    // --- 🛡️ SISTEMA ANTI-PORTADORA (SHIELD) ---
    var portadoraOffenseTimer by remember { mutableStateOf(0) }
    var showPortadoraDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isTransmitting, mic) {
        if (isTransmitting) {
            if (mic < 0.12f) { // Umbral de silencio/ruido de fondo
                delay(1000)
                portadoraOffenseTimer++
                if (portadoraOffenseTimer >= 15) {
                    showPortadoraDialog = true
                    // Sirena disuasoria cada 2 segundos mientras persista la ofensa
                    if (portadoraOffenseTimer % 2 == 0) triggerUiSound("siren")
                }
                if (portadoraOffenseTimer >= 25) {
                    // --- ⚡ BANEO FULMINANTE ---
                    onStateChange(state.copy(veteranPower = 0.7f)) // Reset de potencia
                    onLogoutConfirm() // Cierre de App y limpieza
                }
            } else {
                portadoraOffenseTimer = 0
                showPortadoraDialog = false
            }
        } else {
            portadoraOffenseTimer = 0
            showPortadoraDialog = false
        }
    }

    // Efecto de sonido de pisotón (Heterodino)
    LaunchedEffect(isBeingSteppedOn, isReceivingCollision) {
        if (isBeingSteppedOn || isReceivingCollision) {
            triggerUiSound("static") // Usamos static como base del pisotón
            delay(500)
            vibratePtt() // Vibración de alerta por pisotón
        }
    }
    
    // --- 📳 FEEDBACK HÁPTICO PTT (GAMA ALTA) ---
    val noiseVol = if (!rx && !isTransmitting) (if (state.squelch > state.rfGain) 0f else (state.rfGain - state.squelch)).coerceIn(0f, 1f) else 0f
    
    // Sincronización Instantánea: Mapeo directo sin bloqueo de memoria para que la lista sea tan rápida como el vúmetro
    val mappedUsers = users.map { it.copy(isFriend = state.friends.contains(it.nick)) }

    LaunchedEffect(rx) { if (rx) { while (true) { voiceModulation = ((-5..5).random() / 500f); delay(100) } } else { voiceModulation = 0f } }

    var showSubtoneDialog by remember { mutableStateOf(false) }
    var showMicExplainDialog by remember { mutableStateOf(false) }
    var tempSubtone by remember { mutableStateOf(state.subtone) }
    var showRadarDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var isPrivateSelection by remember { mutableStateOf(false) }
    var newChannelName by remember { mutableStateOf("") }
    var showExitConfirm by remember { mutableStateOf(false) }
    var showDeleteDataConfirm by remember { mutableStateOf(false) }
    val isShowingExit = showExitConfirm || showExitConfirmExternal

    if (showDeleteDataConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDataConfirm = false },
            containerColor = LuxeColors.Red,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            modifier = Modifier.border(2.dp, Color.White, RoundedCornerShape(28.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.DeleteForever, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("BORRAR RASTRO DIGITAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "Esta acción eliminará tu indicativo, amigos, favoritos y veteranía de este dispositivo de forma permanente. Cumplimos con tu derecho al olvido (GDPR). ¿Estás seguro?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDataConfirm = false
                        onLogoutConfirm() // Reutilizamos el flujo de limpieza profunda
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuxeColors.Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("BORRAR TODO", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataConfirm = false }) {
                    Text("CANCELAR", color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPortadoraDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = LuxeColors.Red,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            modifier = Modifier.border(2.dp, Color.White, RoundedCornerShape(28.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Gavel, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("¡ADVERTENCIA DE SEGURIDAD!", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = { 
                Text(
                    "Se ha detectado el envío de portadora continua sin voz. Respete el turno y el uso del canal nacional.\n\nSI PERSISTE, SU INDICATIVO Y VETERANÍA SERÁN ELIMINADOS EN 10 SEGUNDOS.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
            },
            confirmButton = { }
        )
    }

    if (isShowingExit) {
        AlertDialog(
            onDismissRequest = { 
                showExitConfirm = false
                onExitConfirmDismiss()
            },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AppShortcut, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("MINIMIZAR APP", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = { 
                Column {
                    Text("La radio seguirá funcionando en segundo plano para que no pierdas la comunicación.", fontSize = 13.sp, color = Color.White.copy(0.7f))
                    Spacer(Modifier.height(12.dp))
                    Text("Para apagarla por completo, desliza la aplicación hacia arriba en el menú de aplicaciones recientes de tu teléfono.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onMinimizeRequest()
                        onExitConfirmDismiss()
                        showExitConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("MINIMIZAR", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showExitConfirm = false
                    onExitConfirmDismiss()
                }) { Text("CANCELAR", color = Color.White.copy(0.4f)) }
            }
        )
    }

    if (showSubtoneDialog) {
        AlertDialog(
            onDismissRequest = { showSubtoneDialog = false },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp)),
            title = { Text("CONFIGURAR SUBTONO (CTCSS)", fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "Al activar un subtono creas un canal privado. Solo quienes tengan tu mismo código podrán escucharte y hablar contigo.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color.White.copy(0.7f)
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = tempSubtone,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) tempSubtone = it },
                        placeholder = { Text("0000", color = Color.White.copy(0.2f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LuxeColors.Gold,
                            unfocusedBorderColor = Color.White.copy(0.1f)
                        )
                    )
                    Text("Introduce 4 cifras (ej: 1234)", color = LuxeColors.Gold.copy(0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = {
                AnimatedVisibility(
                    visible = tempSubtone.length == 4,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val finalSub = tempSubtone.padStart(4, '0')
                            onStateChange(state.copy(subtone = finalSub))
                            onShare(state.channel, finalSub, null, null)
                            showSubtoneDialog = false
                        }) {
                            Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("COMPARTIR", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val finalSub = tempSubtone.padStart(4, '0')
                            onStateChange(state.copy(subtone = finalSub))
                            showSubtoneDialog = false
                        }) { Text("ACTIVAR", color = LuxeColors.Gold, fontWeight = FontWeight.Bold) }
                    }
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.subtone != "0000") {
                        TextButton(onClick = {
                            onStateChange(state.copy(subtone = "0000"))
                            showSubtoneDialog = false
                            onNotification(AppNotification("SUBTONO", "Desactivado", NotificationType.Info))
                        }) {
                            Text("DESACTIVAR", color = LuxeColors.Red, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    TextButton(onClick = { showSubtoneDialog = false }) {
                        Text("CANCELAR", color = Color.White.copy(0.4f))
                    }
                }
            }
        )
    }

    if (showMicExplainDialog) {
        AlertDialog(
            onDismissRequest = { showMicExplainDialog = false },
            containerColor = Color(0xFF0F172A), // Slate 900 para profundidad
            modifier = Modifier.border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(28.dp)),
            icon = { 
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(LuxeColors.ElectricBlue.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.VerifiedUser, 
                        null, 
                        tint = LuxeColors.ElectricBlue, 
                        modifier = Modifier.size(32.dp)
                    ) 
                }
            },
            title = { 
                Text(
                    "CONEXIÓN SEGURA", 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 2.sp,
                    color = Color.White
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Para transmitir tu voz a la red, el sistema requiere acceso al micrófono.", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(0.9f)
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Lock, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("SIN GRABACIONES: Tu voz es efímera.", fontSize = 11.sp, color = Color.White.copy(0.6f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Shield, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("CONTROL TOTAL: Solo tú decides cuándo hablar.", fontSize = 11.sp, color = Color.White.copy(0.6f))
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Al pulsar 'ACTIVAR', el sistema solicitará el permiso oficial para comenzar tu comunicación.", 
                        fontSize = 10.sp, 
                        textAlign = TextAlign.Center,
                        color = LuxeColors.ElectricBlue.copy(0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                LuxeButton(
                    text = "ACTIVAR MICRÓFONO",
                    onClick = { 
                        showMicExplainDialog = false
                        onStateChange(state.copy(hasAcceptedMicExplain = true))
                        // Disparamos una pulsación fantasma para que el navegador pida el permiso
                        onMic(true, 0.7f)
                        onMic(false, 0f)
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    containerColor = LuxeColors.ElectricBlue,
                    contentColor = Color.White
                )
            }
        )
    }

    if (showCreateChannelDialog) {
        val activeRooms = mappedUsers.filter { it.city == state.city && it.channel != "GENERAL" }
            .groupBy { it.channel }.mapValues { it.value.size }
            .keys.toList().sorted()

        AlertDialog(
            onDismissRequest = { showCreateChannelDialog = false }, 
            containerColor = LuxeColors.DeepSea, 
            titleContentColor = LuxeColors.Gold, 
            textContentColor = Color.White, 
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AddCircleOutline, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("CREAR O ENTRAR CANAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Escribe el nombre de un nuevo canal o selecciona uno de los activos en ${state.city}.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = newChannelName, 
                        onValueChange = { if (it.length <= 15) newChannelName = it.uppercase() }, 
                        placeholder = { Text("NOMBRE DE CANAL/SALA", color = Color.White.copy(0.2f)) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(16.dp), 
                        singleLine = true, 
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = LuxeColors.Gold)
                    )

                    Spacer(Modifier.height(16.dp))

                    // --- 🔒 OPCIÓN DE CANAL PRIVADO ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isPrivateSelection) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f))
                            .clickable { isPrivateSelection = !isPrivateSelection }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPrivateSelection) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            null,
                            tint = if (isPrivateSelection) LuxeColors.Gold else Color.White.copy(0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("CANAL PRIVADO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (isPrivateSelection) "Solo entrarán quienes sepan el subtono." else "Cualquiera podrá entrar y escuchar.",
                                color = Color.White.copy(0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isPrivateSelection,
                            onCheckedChange = { isPrivateSelection = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold, checkedTrackColor = LuxeColors.Gold.copy(0.3f))
                        )
                    }

                    if (isPrivateSelection) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tempSubtone, 
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) tempSubtone = it }, 
                            placeholder = { Text("SUBTONO (4 CIFRAS)", color = Color.White.copy(0.2f)) }, 
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(16.dp), 
                            singleLine = true, 
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuxeColors.Gold, focusedBorderColor = LuxeColors.Gold)
                        )
                        Text("Ejemplo: 1234. Comparte este número con tus amigos.", color = LuxeColors.Gold.copy(0.6f), fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp, start = 8.dp))
                    }
                    
                    if (activeRooms.isNotEmpty()) {
                        val cityChannels = mappedUsers.filter { it.city == state.city && it.channel != "GENERAL" }
                            .groupBy { it.channel }

                        Spacer(Modifier.height(20.dp))
                        Text("CANALES DISPONIBLES:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.6f))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeRooms) { room ->
                                val usersInRoom = cityChannels[room] ?: emptyList()
                                val count = usersInRoom.size
                                val isPrivate = usersInRoom.any { it.subtone != "0000" }
                                val isFav = state.favoriteChannels.contains(room)
                                Surface(
                                    onClick = { 
                                        onStateChange(state.copy(channel = room))
                                        showCreateChannelDialog = false 
                                    },
                                    color = Color.White.copy(0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isFav) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.1f))
                                ) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            null,
                                            tint = if (isFav) LuxeColors.Gold else Color.White.copy(0.2f),
                                            modifier = Modifier.size(10.dp).clickable {
                                                onStateChange(state.copy(
                                                    favoriteChannels = if (isFav) state.favoriteChannels - room else state.favoriteChannels + room
                                                ))
                                                triggerUiSound("click")
                                            }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        if (isPrivate) {
                                            Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold, modifier = Modifier.size(10.dp))
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text(room, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(6.dp))
                                        Text("$count", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (newChannelName.isNotBlank()) {
                        val isSubtoneValid = !isPrivateSelection || tempSubtone.length == 4
                        IconButton(
                            onClick = {
                                val finalSubtone = if (isPrivateSelection) tempSubtone else "0000"
                                onShare(newChannelName, finalSubtone, null, null)
                            },
                            enabled = isSubtoneValid
                        ) {
                            Icon(
                                Icons.Rounded.Share, 
                                "Compartir", 
                                tint = if (isSubtoneValid) LuxeColors.Gold else LuxeColors.Gold.copy(0.2f)
                            )
                        }
                    }
                    
                    val canEnter = newChannelName.isNotBlank() && (!isPrivateSelection || tempSubtone.length == 4)
                    TextButton(
                        onClick = { 
                            if (canEnter) { 
                                onStateChange(state.copy(
                                    channel = newChannelName,
                                    subtone = if (isPrivateSelection) tempSubtone else "0000"
                                ))
                                showCreateChannelDialog = false 
                            } 
                        },
                        enabled = canEnter
                    ) { 
                        Text(
                            "ENTRAR", 
                            color = if (canEnter) LuxeColors.Gold else LuxeColors.Gold.copy(0.3f), 
                            fontWeight = FontWeight.Black
                        )
                    } 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChannelDialog = false }) {
                    Text("CANCELAR", color = Color.White.copy(0.4f))
                }
            }
        )
    }

    if (showRadarDialog) {
        val citiesWithPeople = users.groupBy { it.city }.mapValues { it.value.size }
            .toList().sortedByDescending { it.second }

        AlertDialog(
            onDismissRequest = { showRadarDialog = false },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Radar, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("RADAR NACIONAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Actividad en tiempo real sobre el territorio nacional.", fontSize = 12.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(16.dp))
                    
                    NationalRadarMap(
                        users = users,
                        onCitySelect = { city ->
                            onStateChange(state.copy(city = city, channel = "GENERAL"))
                            showRadarDialog = false
                            triggerUiSound("switch")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRadarDialog = false }) { Text("CERRAR", color = Color.White.copy(0.4f)) }
            }
        )
    }
    
    // =======================================================
    // 🔒 HARD-LOCK: MOTOR DE AUDIO (INTOCABLE - PRIORIDAD 0)
    // =======================================================
    val currentOnMic by rememberUpdatedState(onMic)
    val currentOnNoise by rememberUpdatedState(onNoise)
    
    LaunchedEffect(isTransmitting, myDynamicPower) { 
        currentOnMic(isTransmitting, myDynamicPower)
    }
    
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { 
        currentOnNoise(noiseVol) 
    }
    // =======================================================

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
                    triggerUiSound("static") // Beep de escaneo profesional
                    val nextCity = SPAIN_CITIES[(SPAIN_CITIES.indexOf(loopState.city).coerceAtLeast(0) + 1) % SPAIN_CITIES.size]
                    currentOnStateChange(loopState.copy(city = nextCity)) 
                }
            }
        }
    }

    LaunchedEffect(effectivePtt) { if (effectivePtt && state.isScanning) onStateChange(state.copy(isScanning = false)) }
    LaunchedEffect(effectivePtt) { if (effectivePtt) { pttTimer = 0; while (effectivePtt && pttTimer < 50) { delay(1000); pttTimer++ }; if (pttTimer >= 50) { pttLocked = false; onNotification(AppNotification("TOT", "Seguridad: PTT cortado (50s)", NotificationType.Warning)) } } }
    
    // --- 🔊 AVISO EDUCATIVO: SQUELCH INTELIGENTE ---
    var hasShownSquelchAvisoThisSession by remember { mutableStateOf(false) }
    var isInitialLaunch by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000) // Esperamos 3 segundos tras iniciar para evitar el aviso al entrar
        isInitialLaunch = false
    }

    LaunchedEffect(state.squelch) {
        if (!isInitialLaunch && !state.hasSeenSquelchWarning && state.squelch >= 0.99f) {
            onNotification(AppNotification(
                "SQUELCH AL MÁXIMO", 
                "Has cerrado el silenciador por completo. Solo oirás estaciones extremadamente potentes. Para uso normal, bájalo hasta que empiece el ruido y luego súbelo un poco.", 
                NotificationType.Warning
            ))
            onStateChange(state.copy(hasSeenSquelchWarning = true))
        }
    }

    LaunchedEffect(noiseVol) { if (noiseVol > 0f) { while (true) { meterJitter = ((-2..2).random() / 100f) * noiseVol; delay(60) } } else { meterJitter = 0f } }
    LaunchedEffect(mic, state.isVoxEnabled, state.voxSensitivity, isBeeping, rx) {
        if (state.isVoxEnabled && !rx && !isBeeping) {
            val threshold = 1.05f - (state.voxSensitivity * 0.9f)
            if (mic > threshold) { voxActive = true; voxHangTimer = 40 }
            else if (voxActive) { if (voxHangTimer > 0) voxHangTimer-- else voxActive = false }
        } else { voxActive = false; voxHangTimer = 0 }
    }
    LaunchedEffect(state.squelch, state.rfGain, rx, isTransmitting) { onNoise(noiseVol) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(LuxeColors.BackgroundGradient)) {
        // --- 🔄 REPLAY OVERLAY (MODO HISTORIAL) ---
        androidx.compose.animation.AnimatedVisibility(
            visible = replayProgress > 0f,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LuxeColors.DeepSea.copy(0.9f))
                    .border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "Replay")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "Pulse"
                    )
                    
                    Icon(
                        Icons.Rounded.History, null, 
                        tint = LuxeColors.Gold, 
                        modifier = Modifier.size(24.dp).scale(if (state.isEcoMode) 1f else pulseScale)
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(Modifier.weight(1f)) {
                        Text("REPRODUCIENDO HISTORIAL", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(0.1f))) {
                            Box(Modifier.fillMaxWidth(replayProgress).fillMaxHeight().background(LuxeColors.Gold))
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier.size(40.dp).background(LuxeColors.Red.copy(0.15f), CircleShape).border(1.dp, LuxeColors.Red.copy(0.3f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Stop, null, tint = LuxeColors.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "ON AIR SPAIN", 
                    color = Color.White, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    nick, 
                    color = LuxeColors.Gold, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 2.sp,
                    maxLines = 1
                )
            }
            
            // --- 🔄 BOTONES DE ACCIÓN RÁPIDA (GAMA ALTA) ---
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                var showHelpDialog by remember { mutableStateOf(false) }
                var showBlacklistDialog by remember { mutableStateOf(false) }

                if (showHelpDialog) {
                    OnboardingDialog(onDismiss = { showHelpDialog = false })
                }
                
                if (showBlacklistDialog) {
                    BlacklistDialog(
                        blockedUsers = state.blockedUsers,
                        allUsers = users,
                        onUnblock = { id -> onStateChange(state.copy(blockedUsers = state.blockedUsers - id)) },
                        onDismiss = { showBlacklistDialog = false }
                    )
                }

                if (state.blockedUsers.isNotEmpty()) {
                    IconButton(
                        onClick = { if (!state.isInterfaceLocked) showBlacklistDialog = true },
                        modifier = Modifier.size(24.dp).background(LuxeColors.Red.copy(0.1f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Block, null, tint = LuxeColors.Red, modifier = Modifier.size(12.dp))
                    }
                }

                val infiniteProTransition = rememberInfiniteTransition()
                val proGlowScale by infiniteProTransition.animateFloat(
                    1f, 1.2f, infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                )
                val proGlowAlpha by infiniteProTransition.animateFloat(
                    0.2f, 0.6f, infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                )

                Box(contentAlignment = Alignment.Center) {
                    // Círculo de brillo animado detrás del maletín
                    Box(
                        Modifier
                            .size(28.dp)
                            .graphicsLayer(scaleX = proGlowScale, scaleY = proGlowScale, alpha = proGlowAlpha)
                            .background(LuxeColors.ElectricBlue.copy(0.3f), CircleShape)
                    )
                    IconButton(
                        onClick = { if (!state.isInterfaceLocked) onStateChange(state.copy(isWorkModeActive = true)) },
                        modifier = Modifier.size(24.dp).background(LuxeColors.ElectricBlue.copy(0.2f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Work, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(14.dp))
                    }
                }

                IconButton(
                    onClick = { 
                        if (!state.isInterfaceLocked) {
                            val newMode = !state.isEcoMode
                            onStateChange(state.copy(isEcoMode = newMode))
                            onNotification(
                                AppNotification(
                                    title = if (newMode) "MODO ECO ACTIVADO" else "MODO PREMIUM",
                                    message = if (newMode) "Ahorrando batería: Efectos visuales en pausa." else "Efectos visuales y aurora reactivados.",
                                    type = if (newMode) NotificationType.Success else NotificationType.Info
                                )
                            )
                        }
                    }, 
                    modifier = Modifier.size(24.dp).background(if (state.isEcoMode) Color(0xFF4CAF50).copy(0.2f) else Color.White.copy(0.05f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Eco, null, tint = if (state.isEcoMode) Color(0xFF4CAF50) else Color.White.copy(0.4f), modifier = Modifier.size(12.dp)) 
                }

                // --- 🗑️ BOTÓN GDPR (DERECHO AL OLVIDO) ---
                IconButton(
                    onClick = { if (!state.isInterfaceLocked) showDeleteDataConfirm = true },
                    modifier = Modifier.size(24.dp).background(Color.White.copy(0.05f), CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep, 
                        null, 
                        tint = Color.White.copy(0.4f), 
                        modifier = Modifier.size(12.dp)
                    )
                }

                IconButton(onClick = { if (!state.isInterfaceLocked) showHelpDialog = true }, modifier = Modifier.size(24.dp).background(Color.White.copy(0.05f), CircleShape)) { 
                    Icon(Icons.Rounded.QuestionMark, null, tint = LuxeColors.Gold.copy(0.7f), modifier = Modifier.size(12.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(32.dp)).background(Color.Black).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp))) {
                StarryBackground(activity = if (rx || isTransmitting) 1f else 0f, isEcoMode = state.isEcoMode)

                // --- 🔒 BOTÓN DE BLOQUEO Y RADAR (LADO DERECHO) ---
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { onStateChange(state.copy(isInterfaceLocked = !state.isInterfaceLocked)) },
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (state.isInterfaceLocked) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f), CircleShape)
                    ) {
                        Icon(
                            if (state.isInterfaceLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            null,
                            tint = if (state.isInterfaceLocked) LuxeColors.Gold else Color.White.copy(0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (!state.isInterfaceLocked) showRadarDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(0.05f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Radar, 
                            null, 
                            tint = LuxeColors.Gold.copy(0.7f), 
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // --- 🛡️ AVISO DE SATURACIÓN DE CANAL (GAMA ALTA) ---
                val channelUsersCount = mappedUsers.count { it.city == state.city && it.channel == state.channel }
                if (channelUsersCount > 15 && !isChatVisible) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(0.6f))
                            .border(1.dp, Color(0xFFF59E0B).copy(0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("SALA CONCURRIDA: SE RECOMIENDA USAR CANALES SECUNDARIOS", color = Color(0xFFF59E0B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!isChatVisible) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- 📏 VÚMETRO PROTAGONISTA (GAMA ALTA) ---
                        Box(Modifier.fillMaxWidth().height(160.dp)) {
                            val activeTransmitter = remember(rx, transmitterNick, mappedUsers) {
                                if (rx && transmitterNick != null) {
                                    mappedUsers.find { it.nick == transmitterNick && it.isTransmitting }
                                } else null
                            }
                            
                            AnalogMeter(
                                value = when {
                                    isBeeping -> 0.95f;
                                    isTransmitting -> myDynamicPower + (mic * 0.15f); 
                                    rx -> (activeTransmitter?.txPower ?: 0.7f);
                                    isCodedRx -> 0.85f; 
                                    else -> (state.rfGain * 0.1f) + (noiseVol * 0.2f) + meterJitter 
                                },
                                isTransmitting = isTransmitting || isBeeping,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // --- 🎙️ INDICATIVO DEBAJO DE LA AGUJA ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .offset(y = (-4).dp), // Ajuste fino para que no pise el pivote
                            contentAlignment = Alignment.Center
                        ) {
                            val displayName = if (isTransmitting) nick else (transmitterNick ?: "")
                            val isVisible = (rx && transmitterNick != null) || isTransmitting || isCodedRx
                            
                            val isFriendTalking = remember(displayName, state.friends) {
                                val cleanName = displayName.trim().uppercase()
                                state.friends.any { it.trim().uppercase() == cleanName }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = OvershootInterpolator(1.2f).toEasing()), initialScale = 0.8f),
                                exit = fadeOut(tween(500)) + scaleOut(targetScale = 0.9f)
                            ) {
                                val nameTransition = rememberInfiniteTransition(label = "NameEnigma")
                                val glowIntensity by nameTransition.animateFloat(
                                    initialValue = 0.4f, targetValue = 0.8f,
                                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "Glow"
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val showActionIcon = (rx && transmitterNick != null) || isTransmitting
                                    if (showActionIcon) {
                                        Icon(
                                            imageVector = if (isTransmitting) Icons.Rounded.RecordVoiceOver else (if (isFriendTalking) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder),
                                            null,
                                            tint = if (isTransmitting) LuxeColors.Red else LuxeColors.Green,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .graphicsLayer(alpha = glowIntensity)
                                                .clickable(enabled = !isTransmitting) {
                                                    if (transmitterNick != null) {
                                                        val cleanTarget = transmitterNick.trim().uppercase()
                                                        val isAlreadyFriend = state.friends.any { it.trim().uppercase() == cleanTarget }
                                                        onStateChange(
                                                            state.copy(
                                                                friends = if (isAlreadyFriend) {
                                                                    state.friends.filterNot { it.trim().uppercase() == cleanTarget }.toSet()
                                                                } else {
                                                                    state.friends + transmitterNick
                                                                }
                                                            )
                                                        )
                                                        triggerUiSound("click")
                                                    }
                                                }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    } else if (isCodedRx && !rx) {
                                        Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp).graphicsLayer(alpha = glowIntensity))
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    Text(
                                        text = if (isCodedRx && !rx && !isTransmitting) "SEÑAL CODIFICADA" else displayName,
                                        color = if (isTransmitting) LuxeColors.Red else (if (isCodedRx && !rx) LuxeColors.Gold else LuxeColors.Green),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            shadow = Shadow(
                                                color = (if (isTransmitting) LuxeColors.Red else (if (isCodedRx && !rx) LuxeColors.Gold else LuxeColors.Green)).copy(alpha = 0.6f * glowIntensity),
                                                offset = Offset(0f, 0f),
                                                blurRadius = 15f * glowIntensity
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        // --- 📡 LÍNEA MAESTRA DE ESTADO (REDISEÑO PREMIUM CENTRADO) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(64.dp) // Un poco más alta para mayor presencia
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.verticalGradient(listOf(Color.White.copy(0.07f), Color.Transparent)))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Izquierda: Favorito
                            val isCityFav = state.favoriteCities.contains(state.city)
                            IconButton(
                                onClick = { 
                                    if (!state.isInterfaceLocked) {
                                        onStateChange(state.copy(favoriteCities = if (isCityFav) state.favoriteCities - state.city else state.favoriteCities + state.city))
                                        triggerUiSound("click")
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    if (isCityFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, 
                                    null, 
                                    tint = if (isCityFav) LuxeColors.Gold else Color.White.copy(0.2f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Flecha Izquierda (Premium)
                            IconButton(
                                onClick = {
                                    if (!state.isInterfaceLocked) {
                                        val idx = SPAIN_CITIES.indexOf(state.city)
                                        val next = if (idx > 0) SPAIN_CITIES[idx-1] else SPAIN_CITIES.last()
                                        onStateChange(state.copy(city = next, channel = "GENERAL")) 
                                        triggerUiSound("static")
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft, 
                                    null, 
                                    tint = LuxeColors.Gold.copy(0.6f), 
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Centro: Selector de Ciudad y Sala (Protagonista)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { if (!state.isInterfaceLocked) showCreateChannelDialog = true },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "CH ${SPAIN_CITIES.indexOf(state.city) + 1}", 
                                        color = LuxeColors.Gold, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        state.city, 
                                        color = Color.White, 
                                        fontSize = 15.sp, 
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    if (state.channel == "GENERAL") "CANAL GENERAL" else "CANAL/SALA: ${state.channel}", 
                                    color = LuxeColors.Gold.copy(0.7f), 
                                    fontSize = 9.sp, 
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    maxLines = 1
                                )
                            }

                            // Flecha Derecha (Premium)
                            IconButton(
                                onClick = {
                                    if (!state.isInterfaceLocked) {
                                        val idx = SPAIN_CITIES.indexOf(state.city)
                                        val next = if (idx < SPAIN_CITIES.size - 1) SPAIN_CITIES[idx+1] else SPAIN_CITIES.first()
                                        onStateChange(state.copy(city = next, channel = "GENERAL")) 
                                        triggerUiSound("static")
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight, 
                                    null, 
                                    tint = LuxeColors.Gold.copy(0.6f), 
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    
                    IconButton(onClick = { if (!state.isInterfaceLocked) onStateChange(state.copy(isScanning = !state.isScanning)) }, modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp, start = 16.dp)) {
                        Icon(Icons.Rounded.Sensors, null, tint = if (state.isScanning) LuxeColors.Red else Color.White.copy(0.2f), modifier = Modifier.size(20.dp))
                    }

                    IconButton(onClick = { if (!state.isInterfaceLocked) onShare(state.channel, state.subtone, null, null) }, modifier = Modifier.align(Alignment.TopStart).padding(top = 52.dp, start = 16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Share, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(20.dp))
                            Text("INVITAR", color = Color.White.copy(0.3f), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = { if (!state.isInterfaceLocked) onReplay() }, modifier = Modifier.align(Alignment.TopStart).padding(top = 88.dp, start = 16.dp)) {
                        val replayColor = if (replayProgress > 0f) LuxeColors.Gold 
                                          else if (isReplayReady) {
                                              val infiniteTransition = rememberInfiniteTransition(label = "ReplayReady")
                                              val alpha by infiniteTransition.animateFloat(
                                                  initialValue = 0.6f, targetValue = 0.9f,
                                                  animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "Alpha"
                                              )
                                              LuxeColors.Green.copy(alpha = if (state.isEcoMode) 1f else alpha)
                                          } else Color.White.copy(0.2f)
                        
                        Icon(Icons.Rounded.History, null, tint = replayColor, modifier = Modifier.size(20.dp))
                    }
                }

                // CHAT OVERLAY (ESTILO TERMINAL)
                if (isChatVisible) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.95f)) // Más opaco para mejor contraste
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (privateChatTarget != null) {
                                    IconButton(
                                        onClick = { 
                                            privateChatTarget = null
                                            onPublicChat() 
                                            triggerUiSound("click")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = LuxeColors.Gold)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    if (privateChatTarget != null) "TERMINAL PRIVADA: $privateChatTarget" 
                                    else "TERMINAL PÚBLICA - ${if (state.channel == "GENERAL") "CIUDAD" else state.channel}", 
                                    color = LuxeColors.Gold, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { isChatVisible = false }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.03f))
                                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
                            ) {
                                if (chatMessages.isEmpty()) {
                                    Text(
                                        "SIN MENSAJES RECIENTES", 
                                        color = Color.White.copy(0.1f), 
                                        fontSize = 9.sp, 
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                androidx.compose.foundation.lazy.LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    state = chatListState
                                ) {
                                    items(chatMessages, key = { it.id }) { msg ->
                                        Row(Modifier.padding(vertical = 4.dp)) {
                                            Text(
                                                text = "${msg.senderNick}:",
                                                color = LuxeColors.Gold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = currentChatMessage,
                                onValueChange = { if (it.length <= 60) currentChatMessage = it },
                                placeholder = { Text("ESCRIBE UN MENSAJE...", color = Color.White.copy(0.2f), fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .focusRequester(chatFocusRequester)
                                    .onKeyEvent {
                                        if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                            if (currentChatMessage.isNotBlank()) {
                                                onSendMessage(currentChatMessage, privateChatTarget)
                                                currentChatMessage = ""
                                                triggerUiSound("click")
                                            }
                                            true
                                        } else false
                                    },
                                shape = RoundedCornerShape(18.dp),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (currentChatMessage.isNotBlank()) {
                                                onSendMessage(currentChatMessage, privateChatTarget)
                                                currentChatMessage = ""
                                                triggerUiSound("click")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Send, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Send,
                                    keyboardType = KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (currentChatMessage.isNotBlank()) {
                                            onSendMessage(currentChatMessage, privateChatTarget)
                                            currentChatMessage = ""
                                            triggerUiSound("click")
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxeColors.Gold,
                                    unfocusedBorderColor = Color.White.copy(0.1f),
                                    cursorColor = LuxeColors.Gold
                                )
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            // --- 🚪 BOTÓN VOLVER (MÁS CLARO) ---
                            LuxeButton(
                                text = "VOLVER A EMISORA",
                                onClick = { 
                                    isChatVisible = false 
                                    triggerUiSound("switch")
                                },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                containerColor = Color.White.copy(0.1f),
                                contentColor = Color.White
                            )
                        }
                    }
                }
            }

            // SECTOR SUBTONO ELIMINADO PARA REDUCIR VERTICALIDAD
            
            Spacer(Modifier.height(24.dp))

            // --- 🎚️ PANEL DE CONTROLES MAESTROS (ESTÉTICA PREMIUM) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(0.3f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 8.dp), 
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlKnob("M. LIBRES", state.isVoxEnabled, Icons.Rounded.Mic) { if (!state.isInterfaceLocked) { onStateChange(state.copy(isVoxEnabled = !state.isVoxEnabled)); triggerUiSound("switch") } }
                    ControlKnob("CHAT", isChatVisible, Icons.AutoMirrored.Rounded.Chat) { 
                        isChatVisible = !isChatVisible
                        if (isChatVisible) onPublicChat() 
                        triggerUiSound("click") 
                    }
                    ControlKnob("MONITOR", state.isMonitorEnabled, Icons.Rounded.Headset) { if (!state.isInterfaceLocked) { onStateChange(state.copy(isMonitorEnabled = !state.isMonitorEnabled)); triggerUiSound("switch") } }
                    ControlKnob("REVERB/ECO", state.isEchoEnabled, Icons.Rounded.GraphicEq) { if (!state.isInterfaceLocked) { onStateChange(state.copy(isEchoEnabled = !state.isEchoEnabled)); triggerUiSound("switch") } }
                    ControlKnob("R. BEEP", state.isRogerBeepEnabled, Icons.Rounded.MusicNote) { if (!state.isInterfaceLocked) { onStateChange(state.copy(isRogerBeepEnabled = !state.isRogerBeepEnabled)); triggerUiSound("switch") } }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 🎙️ MOTOR PTT ERGONÓMICO (ZONA DEL PULGAR) ---
            Row(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BOTÓN PTT PRINCIPAL
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(pttScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (isTransmitting) Color(0xFFFF0000).copy(0.25f) else Color.White.copy(0.05f),
                                    if (isTransmitting) Color(0xFFFF0000).copy(0.15f) else Color.Transparent
                                )
                            )
                        )
                        .border(
                            2.dp,
                            if (isTransmitting) Color(0xFFFF0000)
                            else if (rx) Color(0xFF22C55E)
                            else if (isPttBlockedByRx) Color(0xFFF59E0B)
                            else LuxeColors.GlassBorder,
                            RoundedCornerShape(24.dp)
                        )
                        .then(if (isTransmitting || rx) {
                            val infiniteTransition = rememberInfiniteTransition(label = "PTTGlow")
                            val glowAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.15f, targetValue = 0.45f,
                                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Glow"
                            )
                            val glowColor = if (isTransmitting) Color(0xFFFF0000) else Color(0xFF22C55E)
                            Modifier.drawBehind {
                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                                        center = center,
                                        radius = size.width * 1.5f
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                                )
                            }
                        } else Modifier)
                        .pointerInput(isPttBlockedByRx, state.isInterfaceLocked) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (!isPttBlockedByRx && !state.isInterfaceLocked) {
                                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                        pttInteractionSource.emit(press)
                                        tryAwaitRelease()
                                        pttInteractionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when {
                                isTransmitting -> "ON AIR"
                                rx -> "RECIBIENDO"
                                isPttBlockedByRx -> "ESPERE"
                                else -> "HABLAR"
                            }, 
                            color = when {
                                isTransmitting -> Color(0xFFFF0000)
                                rx -> Color(0xFF22C55E)
                                isPttBlockedByRx -> Color(0xFFF59E0B)
                                else -> Color.White.copy(0.6f)
                            },
                            fontSize = 18.sp, // Un poco más grande para mejor visibilidad
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 2.sp,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                shadow = Shadow(
                                    color = if (isTransmitting) Color(0xFFFF0000).copy(0.4f)
                                            else if (rx) Color(0xFF22C55E).copy(0.4f)
                                            else Color.Transparent,
                                    offset = Offset(0f, 0f),
                                    blurRadius = 15f
                                )
                            )
                        )
                        if (!isTransmitting && !rx && !isPttBlockedByRx) {
                            Text("TOCA PARA TRANSMITIR", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // BOTÓN PTT LOCK (CANDADO)
                val pttLockInteractionSource = remember { MutableInteractionSource() }
                val isPttLockPressed by pttLockInteractionSource.collectIsPressedAsState()
                val pttLockScale by animateFloatAsState(if (isPttLockPressed) 0.92f else 1f, label = "PTTLockScale")

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(pttLockScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (pttLocked) LuxeColors.Red.copy(0.15f) else Color.White.copy(0.05f))
                        .border(1.dp, if (pttLocked) LuxeColors.Red else Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .pointerInput(state.isInterfaceLocked) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (!state.isInterfaceLocked) {
                                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                        pttLockInteractionSource.emit(press)
                                        tryAwaitRelease()
                                        pttLockInteractionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                        
                                        pttLocked = !pttLocked
                                        onStateChange(state.copy(isPttLatched = pttLocked)) 
                                        triggerUiSound("switch")
                                    }
                                }
                            )
                        }, 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (pttLocked) Icons.Rounded.Mic else Icons.Rounded.MicNone, 
                        null, 
                        tint = if (pttLocked) LuxeColors.Red else Color.White.copy(0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // --- 🎚️ DESLIZADORES DINÁMICOS DE BOTONES ---
            AnimatedVisibility(
                visible = state.isVoxEnabled || state.isMonitorEnabled || state.isEchoEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(LuxeColors.GlassWhite)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isVoxEnabled) {
                        LuxeSlider("SENSIBILIDAD MANOS LIBRES", state.voxSensitivity, LuxeColors.Gold) { if (!state.isInterfaceLocked) onStateChange(state.copy(voxSensitivity = it)) }
                    }
                    if (state.isMonitorEnabled) {
                        LuxeSlider("ESCUCHAR MI VOZ (RETORNO)", state.monitorVolume, LuxeColors.ElectricBlue) { if (!state.isInterfaceLocked) onStateChange(state.copy(monitorVolume = it)) }
                    }
                    if (state.isEchoEnabled) {
                        LuxeSlider("MODO: REVERB (IZQ) / ECO (DER)", state.echoDelay, LuxeColors.Red) { if (!state.isInterfaceLocked) onStateChange(state.copy(echoDelay = it)) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(LuxeColors.GlassDeep).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(28.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LuxeSlider("FILTRO SQUELCH", state.squelch, LuxeColors.Gold) { if (!state.isInterfaceLocked) onStateChange(state.copy(squelch = it)) }
                LuxeSlider("VOLUMEN GENERAL", state.rfGain, LuxeColors.ElectricBlue) { if (!state.isInterfaceLocked) onStateChange(state.copy(rfGain = it)) }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { isEqualizerVisible = !isEqualizerVisible }
            ) {
                Text("ECUALIZADOR", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                Icon(
                    if (isEqualizerVisible) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    null,
                    tint = Color.White.copy(0.2f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            AnimatedVisibility(
                visible = isEqualizerVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Slider(state.bass, { if (!state.isInterfaceLocked) onStateChange(state.copy(bass = it)) }, colors = SliderDefaults.colors(thumbColor = Color.White)); Text("GRAVES", fontSize = 7.sp, color = Color.White.copy(0.3f)) }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Slider(state.mid, { if (!state.isInterfaceLocked) onStateChange(state.copy(mid = it)) }, colors = SliderDefaults.colors(thumbColor = Color.White)); Text("MEDIOS", fontSize = 7.sp, color = Color.White.copy(0.3f)) }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Slider(state.treble, { if (!state.isInterfaceLocked) onStateChange(state.copy(treble = it)) }, colors = SliderDefaults.colors(thumbColor = Color.White)); Text("AGUDOS", fontSize = 7.sp, color = Color.White.copy(0.3f)) }
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CANALES ACTIVOS EN ESTA CIUDAD", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                val scanAlpha by rememberInfiniteTransition().animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))
                Text("SCANNING...", color = LuxeColors.Gold.copy(alpha = scanAlpha), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            LazyRow(contentPadding = PaddingValues(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Combinamos salas con gente + sala actual + favoritos para que siempre sean visibles y gestionables
                val activeChannels = mappedUsers.filter { it.city == state.city }.groupBy { it.channel }
                val channelsToShow = (activeChannels.keys + state.channel + state.favoriteChannels).distinct().sorted()
                
                items(channelsToShow) { ch -> 
                    val usersInCh = activeChannels[ch] ?: emptyList()
                    val isPrivate = usersInCh.any { it.subtone != "0000" }
                    val isGeneral = ch == "GENERAL"
                    val displayName = if (isGeneral) state.city else ch
                    val isFav = state.favoriteChannels.contains(ch)
                    
                    ChannelCard(
                        name = displayName, 
                        userCount = usersInCh.size, 
                        isActive = ch == state.channel,
                        isFavorite = isFav,
                        isPrivate = isPrivate,
                        isGeneral = isGeneral,
                        onFavoriteClick = {
                            if (!state.isInterfaceLocked) {
                                onStateChange(state.copy(
                                    favoriteChannels = if (isFav) state.favoriteChannels - ch else state.favoriteChannels + ch
                                ))
                                triggerUiSound("click")
                            }
                        },
                        onDelete = if (ch != "GENERAL") {
                            {
                                if (!state.isInterfaceLocked) {
                                    // Lógica de borrado inteligente
                                    var newState = state.copy(
                                        favoriteChannels = state.favoriteChannels - ch
                                    )
                                    // Si es la sala actual, te eyecta a GENERAL
                                    if (state.channel == ch) {
                                        newState = newState.copy(channel = "GENERAL")
                                    }
                                    onStateChange(newState)
                                    triggerUiSound("switch")
                                    onNotification(AppNotification("SALA", "Has salido y eliminado la sala $ch", NotificationType.Info))
                                }
                            }
                        } else null,
                        onClick = { if (!state.isInterfaceLocked) onStateChange(state.copy(channel = ch)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("ESTACIONES EN ESTE CANAL", color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            
            val localUser = remember(nick, isTransmitting, state.city, state.channel, state.subtone, state.myProRole, state.isProSeeking, state.myWorkStatus, state.myIsSOS) {
                RemoteUser(
                    id = "me",
                    nick = nick,
                    isTransmitting = isTransmitting,
                    city = state.city,
                    channel = state.channel,
                    subtone = state.subtone,
                    isFriend = false,
                    proRole = state.myProRole,
                    isProSeeking = state.isProSeeking,
                    isWorkAvailable = state.myWorkStatus,
                    isSOS = state.myIsSOS,
                    proReputation = 1.0f
                )
            }
            
            val allToShow = (listOf(localUser) + mappedUsers.filter { 
                it.city == state.city && 
                it.channel == state.channel && 
                it.nick != nick &&
                !state.blockedUsers.contains(it.id)
            }).sortedByDescending { it.isTransmitting || it.isFriend }

            LazyRow(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentPadding = PaddingValues(vertical = 12.dp), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allToShow) { user -> 
                    UserCard(
                        user = user, 
                        isMe = user.id == "me",
                        onFriendToggle = { onStateChange(state.copy(friends = if (user.isFriend) state.friends - user.nick else state.friends + user.nick)) }, 
                        onPrivateChat = { privateChatTarget = user.nick; isChatVisible = true; onPrivateChat(user.nick) },
                        onReport = { onReport(user.id) },
                        onBlock = { onBlock(user.id) }
                    ) 
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = state.isWorkModeActive,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ProfessionalTerminal(
                state = state,
                users = users,
                onStateChange = onStateChange,
                onReportPro = onReport,
                onNotification = onNotification,
                onSharePro = { platform -> onShare(state.channel, state.subtone, state.myProRole, platform) },
                onReplayPro = onReplay,
                onMicPro = onMic,
                myPower = myDynamicPower,
                onGpsRequest = onGpsRequestPro,
                onClose = { onStateChange(state.copy(isWorkModeActive = false)) }
            )
        }
    }
}
}
