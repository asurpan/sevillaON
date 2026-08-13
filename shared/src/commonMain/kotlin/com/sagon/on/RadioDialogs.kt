package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - GESTIÓN DE DIÁLOGOS Y CONFIGURACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN 2.0 (MODO RUTA REDISEÑADO)
 * 
 * Gestiona todas las ventanas emergentes y configuraciones tácticas.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RadioDialogType {
    ANTENNA, WATTS, FRIENDS, DSP, RADAR, ECO, LOCK, REPLAY, VOX, MONI, ROGER, REVERB, CHAT, FMSCAN, ADS, 
    INVITE, MIC_REQUEST, DELETE_ROOM, DELETE_DATA, PORTADORA, SUBTONO, CREATE_CHANNEL, RADAR_MAP, SOS_CONFIRM, 
    BLACKLIST, ONBOARDING, SELECT_CITY, SETTINGS, NASA_IMAGE, HERTZ_SENTINEL, DISCRETE, ACTIVITY_SELECTOR, SELECT_NICK, 
    MASTER_HELP, HELP_SQUELCH, HELP_GAIN, HELP_PRIVACY, FINISH_ACTIVITY_CONFIRM, ROUTE_PLANNER, SEARCH_DESTINATION
}

@Composable
fun RadioDialogs(
    type: RadioDialogType?,
    onDismiss: () -> Unit,
    state: RadioState,
    onStateChange: (RadioState) -> Unit,
    onAntennaTest: (Boolean) -> Unit,
    onReplay: () -> Unit,
    onPublicChat: () -> Unit,
    onBgRadioScan: (String, String) -> Unit,
    onBgRadioStop: () -> Unit,
    onShare: (String, String, String?, String?) -> Unit,
    onNotification: (AppNotification) -> Unit,
    onPlaySound: (String) -> Unit = {},
    onLogoutConfirm: () -> Unit,
    onPermissionRequest: (String) -> Unit,
    onMic: (Boolean, Float) -> Unit,
    onGpsRequestPro: (callback: (String?) -> Unit) -> Unit,
    onGpsCityRequestPro: (callback: (String?) -> Unit) -> Unit,
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit,
    onNickChange: (String) -> Unit = {},
    onGetWifiVariance: (Int) -> Float = { _ -> 0f },
    onGetHeading: () -> Float = { 0f },
    onGetTilt: () -> Float = { 0f },
    onEstadoCambio: (Boolean, Float, Int) -> Unit = { _, _, _ -> },
    onExecuteEngineeringAction: (String) -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    users: List<RemoteUser>,
    nick: String,
    channelToDelete: String? = null,
    wifiVerificationResult: String? = null,
    onPrivateChat: (String) -> Unit,
    onShowHelp: () -> Unit = {},
    onHertzSentinelRequest: () -> Unit = {},
    onActivityPanelRequest: () -> Unit = {},
    onWaypointReceived: ((String, Double, Double) -> Unit) -> Unit = { _ -> },
    engineeringPanelVisible: Boolean = false,
    onEngineeringPanelChange: (Boolean) -> Unit = {}
) {
    var tempSubtone by remember(type) { mutableStateOf(state.subtone) }
    var isPrivateSelection by remember(type) { mutableStateOf(false) }

    when (type) {
        RadioDialogType.ANTENNA -> FeatureHelpDialog(
            title = "Sistema de Calibración",
            icon = Icons.Rounded.SettingsInputAntenna,
            description = "Antes de salir al aire, verifica tu modulación. Al activarlo, entrarás en modo de 'retorno local' para oír tu propia voz en tiempo real.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenAntennaIntro = true, isAntennaTesting = true))
                onAntennaTest(true)
                triggerUiSound("switch")
            }
        )
        RadioDialogType.WATTS -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.Speed, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("POTENCIA Y VATAJE (W)", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "Tu indicativo gana potencia real (W) automáticamente cuanto más tiempo pases modulando en la red.",
                        fontSize = 13.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "• Las estaciones nuevas empiezan con 0.7W.\n• El máximo permitido es 15W.\n• A mayor potencia, tu voz tendrá prioridad en caso de colisión con otros operadores.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = LuxeColors.Gold.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f))
                    ) {
                        Text(
                            "Dato actual: Tu potencia es de ${(state.veteranPower * 15f).toInt()} W",
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            color = LuxeColors.Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.FRIENDS -> FeatureHelpDialog(
            title = "Tus Amigos en Oro",
            icon = Icons.Rounded.Favorite,
            description = "Marca a otros usuarios como favoritos. Sus nombres brillarán en ORO para que los identifiques rápido.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenFriendsIntro = true))
            }
        )
        RadioDialogType.DSP -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.GraphicEq, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("PROCESADOR DSP", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxeColors.Gold)
                }
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activar limpieza de voz", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isDspEnabled, onCheckedChange = { onStateChange(state.copy(isDspEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }
                    if (state.isDspEnabled) {
                        Spacer(Modifier.height(24.dp))
                        EliteSlider(
                            label = "INTENSIDAD DEL FILTRO",
                            value = state.dspLevel
                        ) { onStateChange(state.copy(dspLevel = it)) }
                    }
                }
            },
            confirmButton = {
                LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.RADAR -> FeatureHelpDialog(
            title = "Radar Nacional",
            icon = Icons.Rounded.Radar,
            description = "El radar muestra la actividad en tiempo real en toda España.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenRadarMagic = true))
            }
        )
        RadioDialogType.ECO -> FeatureHelpDialog(
            title = "Modo Eco Inteligente",
            icon = Icons.Rounded.Eco,
            iconColor = Color(0xFF4CAF50),
            description = "Ahorra hasta un 40% de batería pausando los efectos visuales avanzados.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenEcoIntro = true, isEcoMode = !state.isEcoMode))
            }
        )
        RadioDialogType.LOCK -> FeatureHelpDialog(
            title = "Bloqueo de Equipo",
            icon = Icons.Rounded.Lock,
            description = "Evita cambios accidentales bloqueando los controles de Canal, Ciudad y Squelch.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenLockIntro = true, isInterfaceLocked = true))
            }
        )
        RadioDialogType.REPLAY -> FeatureHelpDialog(
            title = "Rebobinado (Replay)",
            icon = Icons.Rounded.History,
            description = "¿No has oído bien? El Replay te permite repetir los últimos 15 segundos de radio.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenReplayIntro = true))
                onReplay()
            }
        )

        RadioDialogType.VOX -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Mic, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("CONTROL MANOS LIBRES (VOX)", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxeColors.Gold)
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Activar transmisión por voz", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = state.isVoxEnabled, 
                            onCheckedChange = { onStateChange(state.copy(isVoxEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold)
                        )
                    }
                    
                    if (state.isVoxEnabled) {
                        Spacer(Modifier.height(12.dp))
                        EliteSlider(
                            label = "SENSIBILIDAD",
                            value = state.voxSensitivity
                        ) { onStateChange(state.copy(voxSensitivity = it)) }
                        Text(
                            if(state.voxSensitivity > 0.8f) "MODO RUIDOSO: Ignora el ruido ambiental." else if(state.voxSensitivity < 0.3f) "MODO SENSIBLE: Capta susurros." else "MODO ESTÁNDAR.",
                            fontSize = 9.sp, color = LuxeColors.Gold.copy(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.MONI -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Headset, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("MONITOR DE RETORNO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxeColors.Gold)
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Escuchar mi propia voz", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = state.isMonitorEnabled, 
                            onCheckedChange = { onStateChange(state.copy(isMonitorEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold)
                        )
                    }
                    
                    if (state.isMonitorEnabled) {
                        Spacer(Modifier.height(12.dp))
                        EliteSlider(
                            label = "VOLUMEN MONITOR",
                            value = state.monitorVolume
                        ) { onStateChange(state.copy(monitorVolume = it)) }
                    }
                }
            },
            confirmButton = {
                LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.ROGER -> FeatureHelpDialog(
            title = "Roger Beep",
            icon = Icons.Rounded.MusicNote,
            description = "Emite un tono característico al final de cada transmisión.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenRogerIntro = true, isRogerBeepEnabled = true))
                triggerUiSound("switch")
            }
        )
        RadioDialogType.REVERB -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("EFECTO DE ECO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxeColors.Gold)
                }
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activar procesador de Eco", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isReverbEnabled, onCheckedChange = { onStateChange(state.copy(isReverbEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }
                    if (state.isReverbEnabled) {
                        Spacer(Modifier.height(24.dp))
                        EliteSlider(
                            label = "INTENSIDAD DEL ECO",
                            value = state.reverbLevel
                        ) { onStateChange(state.copy(reverbLevel = it)) }
                    }
                }
            },
            confirmButton = {
                LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(44.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.CHAT -> FeatureHelpDialog(
            title = "Terminal de Texto",
            icon = Icons.AutoMirrored.Rounded.Chat,
            description = "Envía mensajes rápidos a la ciudad o sala actual.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenChatIntro = true, isChatVisible = true))
                onPublicChat()
                triggerUiSound("click")
            }
        )

        RadioDialogType.FMSCAN -> {
            var selectedGenre by remember { mutableStateOf(state.bgRadioGenre) }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Radio, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("SINTONIZADOR FM", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        Text("Selecciona el estilo de emisora que prefieres escuchar en ${state.city}.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                        Spacer(Modifier.height(20.dp))
                        
                        val genres = listOf("MIX", "MUSICA", "NOTICIAS", "PODCAST")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            genres.forEach { genre ->
                                val isSelected = genre == selectedGenre
                                Surface(
                                    onClick = { selectedGenre = genre },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                                    border = BorderStroke(1.dp, if (isSelected) LuxeColors.Gold else Color.White.copy(0.1f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(genre, color = if (isSelected) LuxeColors.Gold else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        LuxeButton(
                            text = "SINTONIZAR SIGUIENTE",
                            onClick = {
                                onStateChange(state.copy(bgRadioGenre = selectedGenre, hasSeenFmScanIntro = true))
                                onDismiss()
                                onBgRadioScan(state.city, selectedGenre)
                            },
                            enabled = true,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            containerColor = LuxeColors.Gold,
                            contentColor = Color.Black
                        )
                        
                        TextButton(
                            onClick = { onDismiss(); onBgRadioStop() },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("APAGAR RADIO FM", color = Color.Red.copy(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("CERRAR", color = Color.White.copy(0.4f)) }
                }
            )
        }
        RadioDialogType.ADS -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Campaign, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("TABLÓN DE ANUNCIOS", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("Boletín de servicio de ${state.city}.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    
                    // --- 🏛️ INFORMACIÓN TURÍSTICA / HISTÓRICA ---
                    if (state.tourismInfo != null) {
                        Surface(
                            color = LuxeColors.ElectricBlue.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AccountBalance, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("GUÍA TURÍSTICA LOCAL", color = LuxeColors.ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(state.tourismInfo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                            }
                        }
                    }

                    // --- 🚗 INFORMACIÓN DGT (CACHÉ) ---
                    if (state.dgtText != null) {
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Warning, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("AVISO DE TRÁFICO", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(Modifier.height(8.dp))
                                
                                if (state.dgtImageUrl != null) {
                                    // Visualización de Cámara DGT
                                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                    LuxeButton(
                                        text = "VER CÁMARA DGT",
                                        onClick = { uriHandler.openUri(state.dgtImageUrl) },
                                        enabled = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).height(44.dp),
                                        containerColor = LuxeColors.ElectricBlue.copy(0.2f),
                                        contentColor = Color.White,
                                        icon = Icons.Rounded.CameraAlt
                                    )
                                }
                                
                                Text(state.dgtText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                            }
                        }
                    }

                    // --- 🚀 INFORMACIÓN NASA (ESPACIAL) ---
                    if (state.nasaImageUrl != null) {
                        Surface(
                            onClick = { onPendingDialogChange(RadioDialogType.NASA_IMAGE, null) },
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AutoAwesome, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("BOLETÍN NASA DISPONIBLE", color = LuxeColors.ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                if (state.nasaImageTitle != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(state.nasaImageTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Pulsa para ver la imagen espacial del día.", color = Color.White.copy(0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LuxeButton(
                        text = "ESCUCHAR BOLETÍN POR VOZ",
                        onClick = {
                            onDismiss()
                            onBgRadioScan(state.city, "ANUNCIOS")
                        },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = LuxeColors.Gold,
                        contentColor = Color.Black,
                        icon = Icons.Rounded.VolumeUp
                    )
                    
                    Spacer(Modifier.height(80.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("CERRAR", color = Color.White.copy(0.4f)) }
            }
        )
        RadioDialogType.INVITE -> FeatureHelpDialog(
            title = "Invitar a la Red",
            icon = Icons.Rounded.Share,
            description = "Comparte un enlace directo por WhatsApp o redes sociales.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenInviteIntro = true))
                onShare(state.channel, state.subtone, null, null)
            }
        )
        RadioDialogType.DISCRETE -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            icon = { Icon(if (state.isDiscreteModeEnabled) Icons.Rounded.HearingDisabled else Icons.Rounded.Hearing, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("MODO DISCRETO", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "Para tu privacidad, cuando este modo está activo, la radio no emitirá voces automáticamente si tienes la pantalla apagada o la app en segundo plano.",
                        fontSize = 13.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Recibirás un aviso visual y tú decides cuándo pulsar para escuchar al compañero. Ideal para el trabajo o lugares públicos.",
                        fontSize = 11.sp,
                        color = LuxeColors.Gold.copy(0.6f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Surface(
                        onClick = { 
                            onStateChange(state.copy(isDiscreteModeEnabled = !state.isDiscreteModeEnabled))
                            triggerUiSound("switch")
                        },
                        color = if (state.isDiscreteModeEnabled) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                if (state.isDiscreteModeEnabled) Icons.Rounded.NotificationsPaused else Icons.Rounded.NotificationsActive,
                                null,
                                tint = if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White.copy(0.4f)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (state.isDiscreteModeEnabled) "MODO DISCRETO: ACTIVADO" else "MODO DISCRETO: DESACTIVADO",
                                color = if (state.isDiscreteModeEnabled) LuxeColors.Gold else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.MASTER_HELP -> FeatureHelpDialog(
            title = "Ajustes de Maestro",
            icon = Icons.Rounded.VpnKey,
            description = "Has desbloqueado la consola de ingeniería. Aquí puedes calibrar el Squelch, la ganancia de RF y los efectos DSP en tiempo real sin salir de la pantalla principal.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenMasterIntro = true))
            }
        )
        RadioDialogType.ACTIVITY_SELECTOR, RadioDialogType.ROUTE_PLANNER -> {
            var tempRouteName by remember { mutableStateOf("") }
            var destinationText by remember { mutableStateOf("") }
            var selectedActivity by remember { mutableStateOf<ActivityProfile?>(state.activeProfile.takeIf { it != ActivityProfile.NORMAL } ?: ActivityProfile.MOTO) }
            var isLaunching by remember { mutableStateOf(false) }
            var tempWaypoints by remember { mutableStateOf(state.routeWaypoints.toMutableList()) }
            var selectedPois by remember { mutableStateOf(setOf<String>()) }
            val scope = rememberCoroutineScope()
            
            var lastSearchLat by remember { mutableStateOf<Double?>(null) }
            var lastSearchLon by remember { mutableStateOf<Double?>(null) }

            LaunchedEffect(tempWaypoints) {
                if (tempWaypoints.isNotEmpty()) {
                    val jsonWaypoints = "[" + tempWaypoints.joinToString(",") { 
                        """{"name":"${it.name.replace("\"", "'")}","lat":${it.lat},"lon":${it.lon}}""" 
                    } + "]"
                    onExecuteEngineeringAction("SET_MISSION_ROUTE|$jsonWaypoints")
                }
            }

            LaunchedEffect(destinationText) {
                if (destinationText.length > 2) {
                    delay(500)
                    onExecuteEngineeringAction("GET_LOCATION_SUGGESTIONS|$destinationText")
                }
            }

            LaunchedEffect(Unit) {
                onWaypointReceived { name, lat, lon ->
                    if (tempWaypoints.none { it.lat == lat && it.lon == lon }) {
                        tempWaypoints = (tempWaypoints + RouteSuggestion(name, lat, lon)).toMutableList()
                    }
                }
            }
            
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .border(1.dp, LuxeColors.Gold.copy(0.2f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) { detectTapGestures { /* Bloqueo PTT */ } },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Route, null, tint = LuxeColors.Gold, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("PLANIFICADOR TÁCTICO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text("Configura tu misión y descubre paradas", fontSize = 10.sp, color = LuxeColors.Gold.copy(0.6f))
                        }
                        IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f)) }
                    }
                },
                text = {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        // --- COLUMNA 1: CONFIGURACIÓN ---
                        Column(modifier = Modifier.weight(1f)) {
                            Text("1. ACTIVIDAD Y NOMBRE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActivityProfile.entries.filter { it != ActivityProfile.NORMAL }.forEach { act ->
                                    val isSelected = selectedActivity == act
                                    Surface(
                                        onClick = { selectedActivity = act },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, if (isSelected) LuxeColors.Gold else Color.White.copy(0.1f))
                                        ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(getActivityIcon(act), null, tint = if (isSelected) LuxeColors.Gold else Color.White.copy(0.5f), modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tempRouteName,
                                onValueChange = { if (it.length <= 30) tempRouteName = it.uppercase() },
                                placeholder = { Text("NOMBRE DE MISIÓN", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            Text("2. RUTA (PARADAS)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                            Spacer(Modifier.height(8.dp))
                            Surface(modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Search, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    BasicTextField(
                                        value = destinationText,
                                        onValueChange = { destinationText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                        cursorBrush = SolidColor(LuxeColors.Gold),
                                        decorationBox = { if(destinationText.isEmpty()) Text("Buscar destino...", color = Color.White.copy(0.2f), fontSize = 13.sp); it() }
                                    )
                                }
                            }
                            
                            Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                                LazyColumn {
                                    if (state.routeSuggestions.isNotEmpty()) {
                                        item { Text("SUGERENCIAS", fontSize = 9.sp, color = LuxeColors.Gold.copy(0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp)) }
                                        items(state.routeSuggestions) { sug ->
                                            Surface(
                                                onClick = { 
                                                    if (tempWaypoints.none { it.lat == sug.lat && it.lon == sug.lon }) {
                                                        tempWaypoints = (tempWaypoints + sug).toMutableList()
                                                    }
                                                    lastSearchLat = sug.lat; lastSearchLon = sug.lon; destinationText = ""
                                                    onExecuteEngineeringAction("CLEAR_SUGGESTIONS")
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                                color = LuxeColors.Gold.copy(0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(sug.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(8.dp), maxLines = 1)
                                            }
                                        }
                                    }
                                    
                                    item { Spacer(Modifier.height(8.dp)); Text("PARADAS ACTUALES", fontSize = 9.sp, color = LuxeColors.Gold.copy(0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp)) }
                                    items(tempWaypoints.size) { index ->
                                        val wp = tempWaypoints[index]
                                        Surface(
                                            color = Color.White.copy(0.03f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                        ) {
                                            Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if(index == 0) Icons.Rounded.MyLocation else Icons.Rounded.LocationOn, null, tint = LuxeColors.Gold, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(wp.name, color = Color.White, fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 1)
                                                IconButton(onClick = { tempWaypoints = tempWaypoints.toMutableList().apply { removeAt(index) } }, modifier = Modifier.size(20.dp)) {
                                                    Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.4f), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- COLUMNA 2: EXPLORACIÓN ---
                        Column(modifier = Modifier.weight(1f)) {
                            Text("3. EXPLORACIÓN TÁCTICA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                            Spacer(Modifier.height(8.dp))
                            
                            val cats = listOf(
                                Icons.Rounded.LocalGasStation to "GASOLINERAS", 
                                Icons.Rounded.Restaurant to "RESTAURANTES", 
                                Icons.Rounded.LocalParking to "PARKINGS", 
                                Icons.Rounded.AccountBalance to "MONUMENTOS", 
                                Icons.Rounded.Terrain to "NATURALEZA"
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                cats.forEach { (icon, cat) ->
                                    val isSel = selectedPois.contains(cat)
                                    Surface(
                                        onClick = { 
                                            selectedPois = if (isSel) selectedPois - cat else selectedPois + cat
                                            if (!isSel) {
                                                if (lastSearchLat != null && lastSearchLon != null) {
                                                    onExecuteEngineeringAction("FETCH_POIS|$cat|$lastSearchLat|$lastSearchLon")
                                                } else {
                                                    onExecuteEngineeringAction("FETCH_POIS|$cat")
                                                }
                                            } else {
                                                onExecuteEngineeringAction("CLEAR_POIS|$cat")
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, if (isSel) LuxeColors.Gold else Color.White.copy(0.1f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) { 
                                            Icon(icon, null, tint = if(isSel) LuxeColors.Gold else Color.White.copy(0.5f), modifier = Modifier.size(20.dp)) 
                                        }
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                                if (state.poiSuggestions.isNotEmpty()) {
                                    LazyColumn {
                                        items(state.poiSuggestions) { poi ->
                                            val isAlreadyAdded = tempWaypoints.any { it.lat == poi.lat && it.lon == poi.lon }
                                            Surface(
                                                onClick = { 
                                                    if (!isAlreadyAdded) {
                                                        tempWaypoints = (tempWaypoints + poi).toMutableList()
                                                        onPlaySound("click")
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                                color = if (isAlreadyAdded) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, if (isAlreadyAdded) LuxeColors.Gold else LuxeColors.Gold.copy(0.2f))
                                            ) {
                                                Row(
                                                    Modifier.padding(12.dp), 
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Icon(
                                                            if (isAlreadyAdded) Icons.Rounded.CheckCircle else Icons.Rounded.AddLocation, 
                                                            null, 
                                                            tint = LuxeColors.Gold, 
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(poi.name, color = Color.White, fontSize = 11.sp, maxLines = 1)
                                                    }
                                                    if (!isAlreadyAdded) {
                                                        Text("AÑADIR", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text("Selecciona una categoría para explorar el área.", color = Color.White.copy(0.3f), fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            LuxeButton(
                                text = if(isLaunching) "INICIANDO..." else "✨ LANZAR MISIÓN",
                                onClick = { 
                                    if (selectedActivity != null && tempWaypoints.isNotEmpty()) {
                                        isLaunching = true
                                        onStateChange(state.copy(activeProfile = selectedActivity!!, channel = tempRouteName.ifBlank { "MISIÓN TÁCTICA" }, routeWaypoints = tempWaypoints))
                                        onExecuteEngineeringAction("UPDATE_ACTIVE_PROFILE|${selectedActivity!!.name}")
                                        onDismiss(); onActivityPanelRequest()
                                    }
                                },
                                enabled = selectedActivity != null && tempWaypoints.isNotEmpty() && !isLaunching,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                containerColor = LuxeColors.Gold,
                                contentColor = Color.Black
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }
        RadioDialogType.SEARCH_DESTINATION -> {}
        RadioDialogType.SOS_CONFIRM -> SOSConfirmDialog(
            onConfirm = {
                onStateChange(state.copy(activeProfile = ActivityProfile.SOCORRISTAS, channel = "EMERGENCIAS", isMotoModeEnabled = true))
                onExecuteEngineeringAction("UPDATE_ACTIVE_PROFILE|SOCORRISTAS")
                onDismiss()
            },
            onDismiss = onDismiss
        )
        RadioDialogType.MIC_REQUEST -> MicRequestDialog(
            onAccept = {
                onDismiss()
                onStateChange(state.copy(hasAcceptedMicExplain = true))
                onMic(true, 0.7f)
                onMic(false, 0f)
            },
            onDismiss = onDismiss
        )
        RadioDialogType.DELETE_ROOM -> if (channelToDelete != null) {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Red,
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
                title = { Row { Icon(Icons.Rounded.DeleteSweep, null, tint = LuxeColors.Red); Text(" ELIMINAR SALA") } },
                text = { Text("¿Eliminar sala $channelToDelete?") },
                confirmButton = {
                    Button(onClick = {
                        val newState = state.copy(favoriteChannels = state.favoriteChannels - channelToDelete)
                        onStateChange(if (state.channel == channelToDelete) newState.copy(channel = state.city) else newState)
                        onNotification(AppNotification("SALA ELIMINADA", "Has vuelto al Canal Público", NotificationType.Info))
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Red)) { Text("ELIMINAR") }
                }
            )
        }
        RadioDialogType.DELETE_DATA -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.Red,
            title = { Text("BORRAR RASTRO") },
            text = { Text("Eliminará permanentemente tus datos en este dispositivo.") },
            confirmButton = { Button(onClick = { onDismiss(); onLogoutConfirm() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuxeColors.Red)) { Text("BORRAR TODO") } }
        )
        RadioDialogType.SUBTONO -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
            title = { Text("CÓDIGO DE PRIVACIDAD", fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "Al activar un código creas un canal privado. Solo quienes tengan tu mismo código podrán escucharte.",
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
                if (tempSubtone.length == 4) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val finalSub = tempSubtone.padStart(4, '0')
                            onStateChange(state.copy(subtone = finalSub))
                            onShare(state.channel, finalSub, null, null)
                            onDismiss()
                        }) {
                            Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("COMPARTIR", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val finalSub = tempSubtone.padStart(4, '0')
                            onStateChange(state.copy(subtone = finalSub))
                            onDismiss()
                        }) { Text("ACTIVAR", color = LuxeColors.Gold, fontWeight = FontWeight.Bold) }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR", color = Color.White.copy(0.4f))
                }
            }
        )
        RadioDialogType.CREATE_CHANNEL -> {
            var newChannelSubtone by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = onDismiss, 
                containerColor = LuxeColors.DeepSea, 
                titleContentColor = LuxeColors.Gold, 
                textContentColor = Color.White, 
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("SALA PRIVADA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("Crea un grupo privado en ${state.city}. Solo quienes tengan tu código podrán oírte.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                        Spacer(Modifier.height(20.dp))
                        
                        Text("CÓDIGO DE ACCESO (4 DÍGITOS)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.6f))
                        Text("Introduce 4 números. Usa 0000 para volver al canal público.", fontSize = 9.sp, color = Color.White.copy(0.4f))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newChannelSubtone,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newChannelSubtone = it },
                            placeholder = { Text("EJ: 1234", color = Color.White.copy(0.2f)) },
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
                        
                        Spacer(Modifier.height(16.dp))
                        
                        if (state.subtone != "0000") {
                            Surface(
                                onClick = { 
                                    onStateChange(state.copy(subtone = "0000"))
                                    onDismiss()
                                },
                                color = LuxeColors.Red.copy(0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, LuxeColors.Red.copy(0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "SALIR DE SALA PRIVADA", 
                                    modifier = Modifier.padding(12.dp), 
                                    textAlign = TextAlign.Center,
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (newChannelSubtone.length == 4) {
                        LuxeButton("ENTRAR A SALA", {
                            onStateChange(state.copy(
                                channel = state.city, 
                                subtone = newChannelSubtone
                            ))
                            onDismiss()
                        }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                    }
                }
            )
        }
        RadioDialogType.RADAR_MAP -> {
            LaunchedEffect(Unit) {
                onExecuteEngineeringAction("INIT_REAL_MAP")
                // --- 🛡️ BUSCAR REPETIDORES REALES (OSM) ---
                onExecuteEngineeringAction("FETCH_POIS|ESTACIONES")
            }

            // --- 🛰️ MOTOR DE RADAR EN VIVO: Sincronizar todos los usuarios con posición ---
            LaunchedEffect(users) {
                // Incluimos a todos los usuarios. Si no tienen lat/lon, usamos el centro de su ciudad.
                val participants = users.filter { it.nick != nick }.map { user ->
                    val coords = if (user.lat != null && user.lon != null) {
                        Pair(user.lat!!, user.lon!!)
                    } else {
                        CityCoordinates.get(user.city) ?: Pair(40.4637, -3.7492) // Fallback a centro de España
                    }
                    
                    // Añadimos un pequeño jitter (desviación) si vienen de centro de ciudad para que no se solapen
                    val jitter = if (user.lat == null) ( (user.id.hashCode() % 100) / 5000.0 ) else 0.0
                    val lat = coords.first + jitter
                    val lon = coords.second + jitter

                    """{"nick":"${user.nick}","lat":$lat,"lon":$lon,"isTransmitting":${user.isTransmitting},"isMe":false}"""
                }

                val myCoords = if (state.motoLatitude != null && state.motoLongitude != null) {
                    Pair(state.motoLatitude!!, state.motoLongitude!!)
                } else {
                    CityCoordinates.get(state.city) ?: Pair(40.4637, -3.7492)
                }
                
                val me = """{"nick":"${nick} (YO)","lat":${myCoords.first},"lon":${myCoords.second},"isTransmitting":false,"isMe":true}"""

                val json = "[" + (listOf(me) + participants).joinToString(",") + "]"
                onExecuteEngineeringAction("UPDATE_MAP_MARKERS|$json")
            }
            
            AlertDialog(
                onDismissRequest = {
                    onExecuteEngineeringAction("HIDE_MAP_OVERLAY")
                    onDismiss()
                },
                containerColor = Color.Transparent, // Transparent para ver el mapa real debajo
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                title = {
                    Surface(
                        color = LuxeColors.DeepSea.copy(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Radar, null, tint = LuxeColors.Gold)
                            Spacer(Modifier.width(12.dp))
                            Text("RADAR NACIONAL", color = Color.White, fontWeight = FontWeight.Black)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                onExecuteEngineeringAction("HIDE_MAP_OVERLAY")
                                onDismiss()
                            }) { Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f)) }
                        }
                    }
                },
                text = {
                    // El mapa se inyecta desde JS en el activity-map-container
                    // Aquí solo dejamos el espacio vacío
                    Box(Modifier.fillMaxSize())
                },
                confirmButton = {}
            )
        }
        RadioDialogType.BLACKLIST -> FeatureHelpDialog(
            title = "Lista de Bloqueados",
            icon = Icons.Rounded.Block,
            iconColor = Color.Red,
            description = "Aquí puedes gestionar a los usuarios que has silenciado. No podrás oír sus voces ni ver sus mensajes.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenBlacklistIntro = true))
            }
        )
        RadioDialogType.ONBOARDING -> WelcomeOnboarding(
            nick = nick,
            onStart = {
                onStateChange(state.copy(hasSeenWelcome = true))
                onDismiss()
                triggerUiSound("click")
            }
        )
        RadioDialogType.SELECT_CITY -> {
            var searchText by remember { mutableStateOf(state.city) }
            val filteredCities = remember(searchText) {
                if (searchText.length >= 1) {
                    SPAIN_CITIES.filter { it.contains(searchText, ignoreCase = true) }.take(8)
                } else SPAIN_CITIES.take(8)
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = { Text("CAMBIAR DE CIUDAD", fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        Text("Busca y elige tu canal oficial de ciudad.", color = Color.White.copy(0.6f), fontSize = 12.sp)
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it.uppercase() },
                            placeholder = { Text("BUSCAR CIUDAD...", color = Color.White.copy(0.2f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedTextColor = Color.White)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        Text("CANALES DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.6f))
                        Spacer(Modifier.height(8.dp))
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(filteredCities) { cityName ->
                                val isCurrent = cityName == state.city
                                Surface(
                                    onClick = { 
                                        onStateChange(state.copy(city = cityName, channel = cityName, subtone = "0000"))
                                        onDismiss()
                                    },
                                    color = if(isCurrent) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.04f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    border = BorderStroke(1.dp, if(isCurrent) LuxeColors.Gold else LuxeColors.Gold.copy(0.2f))
                                ) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.LocationCity, null, tint = LuxeColors.Gold, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(cityName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        if(isCurrent) {
                                            Spacer(Modifier.weight(1f))
                                            Text("ACTUAL", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (SPAIN_CITIES.contains(searchText)) {
                        LuxeButton("SINTONIZAR",                        {
                            onStateChange(state.copy(city = searchText, channel = searchText, subtone = "0000"))
                            onDismiss()
                        }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                    } else {
                        Text("Selecciona una ciudad de la lista", color = Color.Red.copy(0.7f), fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            )
        }
        RadioDialogType.SELECT_NICK -> NickSelectorDialog(
            initialNick = nick,
            onConfirm = { newNick ->
                onNickChange(newNick)
                onDismiss()
            },
            onDismiss = onDismiss
        )
        RadioDialogType.NASA_IMAGE -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.Black,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxSize(),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = LuxeColors.ElectricBlue)
                    Spacer(Modifier.width(12.dp))
                    Text("IMAGEN DEL DÍA (NASA)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = Color.White) }
                }
            },
            text = {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (state.nasaImageUrl != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.DarkGray
                        ) {
                            // En Web se renderiza vía background o similar, aquí es un placeholder visual
                            Text("Cargando imagen espacial...", color = Color.White.copy(0.5f), modifier = Modifier.padding(20.dp))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(state.nasaImageTitle ?: "", color = LuxeColors.ElectricBlue, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(state.nasaImageExplanation ?: "", color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(100.dp))
                }
            },
            confirmButton = {}
        )
        RadioDialogType.SETTINGS -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("AJUSTES DE EQUIPO", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Configuración de hardware, red y privacidad.", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(24.dp))
                    
                    // --- 🔔 AJUSTES DE NOTIFICACIÓN ---
                    Text("PREFERENCIAS", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones de Red", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Alertas SOS y mensajes importantes.", color = Color.White.copy(0.4f), fontSize = 10.sp)
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { onStateChange(state.copy(notificationsEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacidad GPS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Añadir jitter de 200m a mi zona.", color = Color.White.copy(0.4f), fontSize = 10.sp)
                        }
                        Switch(
                            checked = state.isGpsPrivacyEnabled,
                            onCheckedChange = { onStateChange(state.copy(isGpsPrivacyEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // --- 🎚️ CONTROLES ANALÓGICOS ---
                    Text("SENSIBILIDAD Y FILTRADO", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    
                    EliteSlider("SQUELCH (FILTRO)", state.squelch) { onStateChange(state.copy(squelch = it)) }
                    Spacer(Modifier.height(12.dp))
                    EliteSlider("GANANCIA DE RF", state.rfGain) { onStateChange(state.copy(rfGain = it)) }
                    
                    Spacer(Modifier.height(24.dp))

                    // --- ⚙️ BOTÓN ACCESO NATIVO ---
                    LuxeButton(
                        text = "AJUSTES AVANZADOS ANDROID",
                        onClick = { onOpenSettings() },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        containerColor = Color.White.copy(0.05f),
                        contentColor = Color.White,
                        icon = Icons.Rounded.Settings
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // --- 📶 VERIFICACIÓN WIFI ---
                    Surface(
                        color = Color.White.copy(0.03f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.SignalWifiStatusbarConnectedNoInternet4, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ESTADO DE RED", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (wifiVerificationResult != null) "Diagnóstico: $wifiVerificationResult" else "Verificando integridad de red...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    // --- 🗑️ DERECHO AL OLVIDO ---
                    Text("SEGURIDAD Y PRIVACIDAD", color = LuxeColors.Red.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        onClick = { onPendingDialogChange(RadioDialogType.DELETE_DATA, null) },
                        color = LuxeColors.Red.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxeColors.Red.copy(0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DeleteSweep, null, tint = LuxeColors.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Derecho al Olvido", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Borrar permanentemente mis datos locales.", color = Color.White.copy(0.4f), fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            },
            confirmButton = { LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black) }
        )
        RadioDialogType.HERTZ_SENTINEL -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxSize().padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Sensors, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("HERTZ SENTINEL", color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f)) }
                }
            },
            text = {
                Box(Modifier.fillMaxSize()) {
                    Text("Escaneando espectro electromagnético...", color = LuxeColors.Gold, modifier = Modifier.align(Alignment.Center))
                }
            },
            confirmButton = {}
        )
        RadioDialogType.HELP_SQUELCH -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.Waves, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("¿QUÉ ES EL SQUELCH?", fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Column {
                    Text(
                        "El Squelch (Silenciador) filtra el ruido de fondo. Solo deja pasar voces que tengan una potencia superior al nivel marcado.",
                        fontSize = 13.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Si lo pones muy bajo, oirás ruido. Si lo pones muy alto, solo oirás a los que estén muy cerca de ti.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.HELP_GAIN -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.SettingsVoice, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("GANANCIA DE RF", fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Column {
                    Text(
                        "La ganancia ajusta la sensibilidad de tu antena virtual. Aumentarla permite captar estaciones más lejanas.",
                        fontSize = 13.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cuidado: Si hay mucha gente hablando a la vez, una ganancia muy alta puede saturar el audio de tu equipo.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
            }
        )
        RadioDialogType.HELP_PRIVACY -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.ElectricBlue,
            modifier = Modifier.border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.Security, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(40.dp)) },
            title = { Text("ZONA PRIVADA (Ocultar mi casa)", fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Column {
                    Text(
                        "Este escudo añade un error aleatorio de ~200 metros a tu posición real antes de subirla a la red.",
                        fontSize = 13.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "De esta forma, tus compañeros sabrán que estás en la zona de la ruta pero nadie podrá localizar tu portal exacto. Ideal para cuando salgas o llegues a casa.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.ElectricBlue, Color.White)
            }
        )
        RadioDialogType.FINISH_ACTIVITY_CONFIRM -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            icon = { Icon(Icons.Rounded.Flag, null, tint = LuxeColors.Red, modifier = Modifier.size(40.dp)) },
            title = { Text("¿HAS LLEGADO A TU DESTINO?", color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Al finalizar la ruta, volverás al canal público general y dejarás de compartir tu posición exacta.", textAlign = TextAlign.Center, color = Color.White.copy(0.7f), fontSize = 13.sp)
                }
            },
            confirmButton = {
                LuxeButton("SÍ, FINALIZAR RUTA", { 
                    onStateChange(state.copy(
                        activeProfile = ActivityProfile.NORMAL,
                        isMotoModeEnabled = false,
                        channel = state.city,
                        subtone = "0000"
                    ))
                    onDismiss()
                }, true, Modifier.fillMaxWidth().height(56.dp), LuxeColors.Red, Color.White)
            },
            dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("CONTINUAR NAVEGANDO", color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold)
                }
            }
        )
        else -> {}
    }
}

@Composable
private fun SOSConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.Slate900,
        icon = { Icon(Icons.Rounded.Warning, null, tint = Color.Red) },
        title = { Text("¿ACTIVAR SOS?", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Text(
                "Esto cambiará tu perfil a SOCORRISTAS y entrarás en el canal de EMERGENCIAS. Úsalo solo en caso de necesidad real.",
                color = Color.White.copy(0.7f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            LuxeButton("SÍ, ACTIVAR", onConfirm, true, Modifier.fillMaxWidth().height(48.dp), Color.Red, Color.White)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = LuxeColors.Gold, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun WelcomeOnboarding(nick: String, onStart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {}, 
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        title = { Text("BIENVENIDO, $nick", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
        text = {
            Column {
                Text(
                    "Estás entrando en la red de radio táctica ON. Aquí la voz es lo más importante.",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "• Usa el PTT para hablar.\n• Respeta los turnos.\n• Mantén la red limpia.",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            LuxeButton("EMPEZAR", onStart, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
        }
    )
}

@Composable
private fun NickSelectorDialog(initialNick: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var tempNick by remember { mutableStateOf(initialNick) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        title = { Text("CAMBIAR INDICATIVO", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
        text = {
            OutlinedTextField(
                value = tempNick,
                onValueChange = { if (it.length <= 15) tempNick = it.uppercase() },
                label = { Text("TU NICK / INDICATIVO") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedTextColor = Color.White)
            )
        },
        confirmButton = {
            LuxeButton("GUARDAR", { onConfirm(tempNick) }, tempNick.isNotBlank(), Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
        }
    )
}

@Composable
private fun PoiChip(
    label: String,
    category: String,
    isSelected: Boolean = false,
    onExecuteAction: (String) -> Unit
) {
    Surface(
        onClick = { onExecuteAction("FETCH_POIS|$category") },
        color = if (isSelected) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) LuxeColors.Gold else Color.White.copy(0.1f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) LuxeColors.Gold else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}
