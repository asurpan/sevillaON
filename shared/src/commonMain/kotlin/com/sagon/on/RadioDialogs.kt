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
    ANTENNA, WATTS, FRIENDS, DSP, RADAR, ECO, LOCK, REPLAY, VOX, MONI, ROGER, REVERB, CHAT, SCAN, FMSCAN, ADS, 
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
    onPendingDialogChange: (RadioDialogType?) -> Unit,
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
    var newChannelName by remember(type) { mutableStateOf("") }
    var isLocatingGps by remember(type) { mutableStateOf(false) }

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
        RadioDialogType.SCAN -> FeatureHelpDialog(
            title = "Escáner de Frecuencias",
            icon = Icons.Rounded.Sensors,
            description = "Busca actividad automáticamente en otras ciudades de España.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenScanIntro = true, isScanning = true))
                triggerUiSound("static")
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
                            onClick = { onPendingDialogChange(RadioDialogType.NASA_IMAGE) },
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
            var tempRouteRules by remember { mutableStateOf("") }
            var destinationText by remember { mutableStateOf("") }
            var selectedActivity by remember { mutableStateOf<ActivityProfile?>(null) }
            var isLaunching by remember { mutableStateOf(false) }
            var tempWaypoints by remember { mutableStateOf(state.routeWaypoints.toMutableList()) }
            var selectedPois by remember { mutableStateOf(setOf<String>()) }
            val scope = rememberCoroutineScope()
            
            // --- 🧠 ESTADO DE BÚSQUEDA CONTEXTUAL (DOPAMINA) ---
            var lastSearchLat by remember { mutableStateOf<Double?>(null) }
            var lastSearchLon by remember { mutableStateOf<Double?>(null) }

            // --- 🗺️ PREVISUALIZACIÓN EN TIEMPO REAL ---
            LaunchedEffect(tempWaypoints) {
                if (tempWaypoints.isNotEmpty()) {
                    val jsonWaypoints = "[" + tempWaypoints.joinToString(",") { 
                        """{"name":"${it.name.replace("\"", "'")}","lat":${it.lat},"lon":${it.lon}}""" 
                    } + "]"
                    onExecuteEngineeringAction("SET_MISSION_ROUTE|$jsonWaypoints")
                    
                    // AUTO-BÚSQUEDA EN TRAYECTO: Si hay ruta, buscar POIs relevantes automáticamente
                    val activity = selectedActivity ?: state.activeProfile
                    val autoCat = when(activity) {
                        ActivityProfile.MOTO, ActivityProfile.CARAVANAS -> "NATURALEZA" // Miradores
                        ActivityProfile.CICLISMO -> "FUENTES"
                        ActivityProfile.PASEO, ActivityProfile.SENDERISMO -> "MONUMENTOS"
                        ActivityProfile.SOCORRISTAS -> "HOSPITALES"
                        else -> "MONUMENTOS"
                    }
                    onExecuteEngineeringAction("FETCH_POIS_ALONG_ROUTE|$autoCat")
                }
            }

            LaunchedEffect(Unit) {
                onWaypointReceived { name, lat, lon ->
                    if (tempWaypoints.none { it.lat == lat && it.lon == lon }) {
                        tempWaypoints = (tempWaypoints + RouteSuggestion(name, lat, lon)).toMutableList()
                        triggerUiSound("switch") // Feedback auditivo de éxito
                    }
                }
            }

            LaunchedEffect(destinationText) {
                if (destinationText.length > 2) {
                    delay(500)
                    onExecuteEngineeringAction("GET_LOCATION_SUGGESTIONS|$destinationText")
                }
            }
            
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                modifier = Modifier.fillMaxWidth(0.95f).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Route, null, tint = LuxeColors.Gold, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("PLANIFICADOR DE RUTA", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                            Text("Configura tu misión táctica", fontSize = 10.sp, color = LuxeColors.Gold.copy(0.6f))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f))
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Text("1. SELECCIONA TU ACTIVIDAD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(12.dp))
                        
                        // --- ✨ MODO AVENTURA (BOTÓN DESTACADO) ---
                        var isCalculating by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { 
                                isCalculating = true
                                if (selectedActivity == null) {
                                    selectedActivity = state.activeProfile.takeIf { it != ActivityProfile.NORMAL } ?: ActivityProfile.MOTO
                                }
                                val activity = selectedActivity!!
                                onExecuteEngineeringAction("GENERATE_ADVENTURE_ROUTE|${activity.name}")
                                triggerUiSound("click")
                                // Reset de estado tras un pequeño delay
                                scope.launch { delay(3000); isCalculating = false }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCalculating) LuxeColors.Gold.copy(0.1f) else LuxeColors.ElectricBlue.copy(0.15f),
                            border = BorderStroke(1.dp, if (isCalculating) LuxeColors.Gold else LuxeColors.ElectricBlue)
                        ) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(
                                        if (isCalculating) Icons.Rounded.Sync else Icons.Rounded.AutoAwesome, 
                                        null, 
                                        tint = if (isCalculating) LuxeColors.Gold else LuxeColors.ElectricBlue, 
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        if (isCalculating) "CALCULANDO RUTA..." else "✨ GENERAR RUTA DE AVENTURA", 
                                        color = Color.White, 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                if (isCalculating) {
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(2.dp),
                                        color = LuxeColors.Gold,
                                        trackColor = LuxeColors.Gold.copy(0.1f)
                                    )
                                } else {
                                    Text(
                                        "Misión sorpresa de ida y vuelta con paradas interesantes.", 
                                        color = Color.White.copy(0.4f), 
                                        fontSize = 9.sp, 
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        val activities = ActivityProfile.entries.filter { it != ActivityProfile.NORMAL }
                        activities.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { act ->
                                    val isSelected = selectedActivity == act
                                    Surface(
                                        onClick = { selectedActivity = act },
                                        modifier = Modifier.weight(1f).height(80.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) LuxeColors.Gold.copy(0.2f) else Color.White.copy(0.05f),
                                        border = BorderStroke(1.dp, if (isSelected) LuxeColors.Gold else Color.White.copy(0.1f))
                                    ) {
                                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            val icon = getActivityIcon(act)
                                            Icon(icon, null, tint = if (isSelected) LuxeColors.Gold else Color.White.copy(0.4f), modifier = Modifier.size(32.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text(if (act == ActivityProfile.SOCORRISTAS) "SOCORRO" else act.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedActivity != null) {
                            Column {
                                Spacer(Modifier.height(24.dp))
                                Text("2. IDENTIFICACIÓN DE RUTA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold, letterSpacing = 1.sp)
                                Spacer(Modifier.height(12.dp))
                                Surface(color = Color.White.copy(0.03f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f)), modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                                    Column(Modifier.padding(16.dp)) {
                                        OutlinedTextField(value = tempRouteName, onValueChange = { if (it.length <= 40) tempRouteName = it.uppercase() }, label = { Text("NOMBRE / INDICATIVO RUTA", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedLabelColor = LuxeColors.Gold, focusedTextColor = Color.White, unfocusedTextColor = Color.White), leadingIcon = { Icon(Icons.Rounded.Label, null, tint = LuxeColors.Gold.copy(0.5f)) })
                                        Spacer(Modifier.height(12.dp))
                                        OutlinedTextField(value = tempRouteRules, onValueChange = { if (it.length <= 150) tempRouteRules = it }, label = { Text("NOTAS / PUNTO DE ENCUENTRO", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), maxLines = 2, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White.copy(0.4f), focusedTextColor = Color.White, unfocusedTextColor = Color.White), leadingIcon = { Icon(Icons.Rounded.Description, null, tint = Color.White.copy(0.3f)) })
                                    }
                                }
                                var editingWaypointIndex by remember { mutableStateOf<Int?>(null) }
                                var editingWaypointName by remember { mutableStateOf("") }
                                
                                if (editingWaypointIndex != null) {
                                    AlertDialog(
                                        onDismissRequest = { editingWaypointIndex = null },
                                        containerColor = LuxeColors.DeepSea,
                                        title = { Text("EDITAR PARADA", color = Color.White, fontWeight = FontWeight.Black) },
                                        text = {
                                            OutlinedTextField(
                                                value = editingWaypointName,
                                                onValueChange = { editingWaypointName = it },
                                                label = { Text("Nombre de la parada") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                            )
                                        },
                                        confirmButton = {
                                            Row {
                                                TextButton(onClick = {
                                                    val newList = tempWaypoints.toMutableList()
                                                    newList.removeAt(editingWaypointIndex!!)
                                                    tempWaypoints = newList
                                                    editingWaypointIndex = null
                                                }) { Text("ELIMINAR", color = Color.Red.copy(0.7f), fontWeight = FontWeight.Bold) }
                                                
                                                Spacer(Modifier.width(8.dp))
                                                
                                                LuxeButton("GUARDAR", {
                                                    val newList = tempWaypoints.toMutableList()
                                                    newList[editingWaypointIndex!!] = newList[editingWaypointIndex!!].copy(name = editingWaypointName)
                                                    tempWaypoints = newList
                                                    editingWaypointIndex = null
                                                }, true, Modifier.height(44.dp), LuxeColors.Gold, Color.Black)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { editingWaypointIndex = null }) { Text("CANCELAR", color = Color.White.copy(0.4f)) }
                                        }
                                    )
                                }

                                Text("3. DESTINO Y PARADAS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold, letterSpacing = 1.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Toca una parada para cambiar su nombre o reordénalas.", fontSize = 10.sp, color = Color.White.copy(0.4f), lineHeight = 14.sp)
                                
                                // --- 📊 PANEL DE TELEMETRÍA (DOPAMINA) ---
                                if (state.routeDistanceKm != null || state.routeDurationMin != null) {
                                    Spacer(Modifier.height(16.dp))
                                    Surface(
                                        color = LuxeColors.Gold.copy(0.1f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("DISTANCIA", fontSize = 9.sp, color = LuxeColors.Gold, fontWeight = FontWeight.Black)
                                                Text(state.routeDistanceKm ?: "-- KM", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(LuxeColors.Gold.copy(0.2f)))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("TIEMPO EST.", fontSize = 9.sp, color = LuxeColors.Gold, fontWeight = FontWeight.Black)
                                                Text(state.routeDurationMin ?: "-- MIN", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Surface(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                                    Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Search, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Box(Modifier.weight(1f)) {
                                            if (destinationText.isEmpty()) {
                                                Text("Ej: Aracena, Huelva", color = Color.White.copy(0.2f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            }
                                            BasicTextField(value = destinationText, onValueChange = { destinationText = it }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold), cursorBrush = SolidColor(LuxeColors.Gold))
                                        }
                                    }
                                }
                                if (state.routeSuggestions.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Surface(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp), shape = RoundedCornerShape(12.dp), color = Color.Black.copy(0.3f), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
                                        LazyColumn {
                                            items(state.routeSuggestions) { suggestion ->
                                                Row(modifier = Modifier.fillMaxWidth().clickable { 
                                                    onStateChange(state.copy(poiSuggestions = emptyList()))
                                                    if (tempWaypoints.none { it.lat == suggestion.lat && it.lon == suggestion.lon }) { 
                                                        tempWaypoints = (tempWaypoints + suggestion).toMutableList() 
                                                    }
                                                    lastSearchLat = suggestion.lat
                                                    lastSearchLon = suggestion.lon
                                                    destinationText = ""
                                                    onExecuteEngineeringAction("CLEAR_SUGGESTIONS")
                                                    
                                                    // DOPAMINA: Buscar POIs automáticamente al seleccionar destino
                                                    val activity = selectedActivity ?: state.activeProfile
                                                    val autoCat = when(activity) {
                                                        ActivityProfile.MOTO, ActivityProfile.CARAVANAS -> "NATURALEZA"
                                                        ActivityProfile.PASEO, ActivityProfile.SENDERISMO -> "MONUMENTOS"
                                                        else -> "MONUMENTOS"
                                                    }
                                                    onExecuteEngineeringAction("FETCH_POIS|$autoCat|${suggestion.lat}|${suggestion.lon}")
                                                }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.AddLocation, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(suggestion.name, color = Color.White.copy(0.8f), fontSize = 11.sp, maxLines = 2)
                                                }
                                                HorizontalDivider(color = Color.White.copy(0.05f))
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val activity = selectedActivity ?: state.activeProfile
                                    
                                    val categories = when (activity) {
                                        ActivityProfile.MOTO, ActivityProfile.CARAVANAS -> listOf("⛽ GASOLINERA" to "GASOLINERAS", "🍽️ COMIDA" to "RESTAURANTES", "🅿️ PARKING" to "PARKINGS", "🏛️ MONUMENTOS" to "MONUMENTOS")
                                        ActivityProfile.CICLISMO -> listOf("💧 FUENTES" to "FUENTES", "🚲 TALLERES" to "TALLERES", "🌲 NATURALEZA" to "NATURALEZA")
                                        ActivityProfile.SENDERISMO, ActivityProfile.PASEO -> listOf("🏞️ MIRADORES" to "NATURALEZA", "🌳 PARQUES" to "PARQUES", "🚻 ASEOS" to "ASEOS", "🏛️ MONUMENTOS" to "MONUMENTOS")
                                        ActivityProfile.SOCORRISTAS -> listOf("🏥 HOSPITAL" to "HOSPITALES", "💊 FARMACIA" to "FARMACIAS", "🏛️ MONUMENTOS" to "MONUMENTOS")
                                        else -> listOf("🏛️ MONUMENTOS" to "MONUMENTOS", "🎭 EVENTOS" to "EVENTOS", "🌲 NATURALEZA" to "NATURALEZA")
                                    }

                                    categories.forEach { (label, cat) ->
                                        PoiChip(
                                            label = label,
                                            category = cat,
                                            isSelected = selectedPois.contains(cat),
                                                onExecuteAction = { 
                                                    onStateChange(state.copy(poiSuggestions = emptyList()))
                                                    val becomingSelected = !selectedPois.contains(cat)
                                                    selectedPois = if (becomingSelected) selectedPois + cat else selectedPois - cat
                                                    
                                                    if (becomingSelected) {
                                                        // --- 📡 FEEDBACK DE BÚSQUEDA ---
                                                        onNotification(AppNotification("BUSCANDO...", "Localizando $label más cercanos...", NotificationType.Info))

                                                        // Si hay una búsqueda previa (destino), buscar allí. Si no, en posición actual.
                                                        if (lastSearchLat != null && lastSearchLon != null) {
                                                            onExecuteEngineeringAction("FETCH_POIS|$cat|$lastSearchLat|$lastSearchLon")
                                                        } else {
                                                            // Forzar búsqueda en última posición guardada
                                                            onExecuteEngineeringAction("FETCH_POIS|$cat") 
                                                        }
                                                        triggerUiSound("click")
                                                    } else {
                                                        // Si se deselecciona, limpiamos marcadores de esa categoría
                                                        onExecuteEngineeringAction("CLEAR_POIS|$cat")
                                                    }
                                                }
                                        )
                                    }
                                }
                                
                                // --- 🏛️ LISTA DE SITIOS ENCONTRADOS (POI SUGGESTIONS) ---
                                if (state.poiSuggestions.isNotEmpty()) {
                                    Spacer(Modifier.height(16.dp))
                                    Text("SITIOS DE INTERÉS ENCONTRADOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.7f), letterSpacing = 1.sp)
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(state.poiSuggestions) { poi ->
                                            Surface(
                                                onClick = {
                                                    if (tempWaypoints.none { it.lat == poi.lat && it.lon == poi.lon }) {
                                                        val newList = tempWaypoints.toMutableList()
                                                        if (newList.size >= 2) {
                                                            // Insertar antes del último (meta)
                                                            newList.add(newList.size - 1, poi)
                                                        } else {
                                                            newList.add(poi)
                                                        }
                                                        tempWaypoints = newList
                                                        triggerUiSound("switch")
                                                    }
                                                },
                                                color = LuxeColors.Gold.copy(0.1f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.2f))
                                            ) {
                                                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.LocationOn, null, tint = LuxeColors.Gold, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(poi.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (tempWaypoints.isNotEmpty()) {
                                    Spacer(Modifier.height(16.dp))
                                    val isCircular = tempWaypoints.size > 1 && tempWaypoints.first().lat == tempWaypoints.last().lat && tempWaypoints.first().lon == tempWaypoints.last().lon
                                    
                                    // --- 🔄 BOTÓN AÑADIR VUELTA (INTELIGENTE) ---
                                    if (!isCircular) {
                                        val blinkInfinite = rememberInfiniteTransition()
                                        val blinkAlpha by blinkInfinite.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))
                                        
                                        Surface(
                                            onClick = { 
                                                onExecuteEngineeringAction("ADD_RETURN_POINT")
                                                triggerUiSound("switch")
                                            },
                                            modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = blinkAlpha),
                                            shape = RoundedCornerShape(12.dp),
                                            color = LuxeColors.ElectricBlue.copy(0.15f),
                                            border = BorderStroke(1.5.dp, LuxeColors.ElectricBlue)
                                        ) {
                                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                Icon(Icons.Rounded.Loop, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(10.dp))
                                                Text("🔄 AÑADIR VUELTA A CASA (CERRAR RUTA)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                        Spacer(Modifier.height(12.dp))
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    tempWaypoints.forEachIndexed { index, wp ->
                                        val isFirst = index == 0
                                        val isLast = index == tempWaypoints.size - 1
                                        
                                        val metaInfinite = rememberInfiniteTransition()
                                        val metaGlow by if (isLast) metaInfinite.animateFloat(0f, 15f, infiniteRepeatable(tween(1000), RepeatMode.Reverse)) else remember { mutableStateOf(0f) }
                                        
                                        Surface(
                                            onClick = { 
                                                editingWaypointIndex = index
                                                editingWaypointName = wp.name
                                            },
                                            color = if (isLast) LuxeColors.Gold.copy(0.12f) else Color.White.copy(0.05f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = if (isLast) BorderStroke(2.dp, LuxeColors.Gold) else BorderStroke(0.5.dp, Color.White.copy(0.1f)),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).graphicsLayer(shadowElevation = if (isLast) metaGlow else 0f)
                                        ) {
                                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(24.dp).background(if (isLast) LuxeColors.Gold else LuxeColors.Gold.copy(0.6f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val icon = when {
                                                        isFirst -> Icons.Rounded.MyLocation
                                                        isLast && isCircular -> Icons.Rounded.Loop
                                                        isLast -> Icons.Rounded.Flag
                                                        else -> null
                                                    }
                                                    
                                                    if (icon != null) {
                                                        Icon(icon, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                    } else {
                                                        Text("${index + 1}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            wp.name, 
                                                            color = if (isLast) LuxeColors.Gold else Color.White, 
                                                            fontSize = 12.sp, 
                                                            fontWeight = FontWeight.Bold, 
                                                            maxLines = 1
                                                        )
                                                        if (isLast) {
                                                            Spacer(Modifier.width(8.dp))
                                                            val activityIcon = getActivityIcon(selectedActivity ?: state.activeProfile)
                                                            Icon(activityIcon, null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                    if (isLast) {
                                                        Text(if (isCircular) "RUTA CIRCULAR (RETORNO)" else "DESTINO FINAL", fontSize = 8.sp, color = LuxeColors.Gold.copy(0.6f), fontWeight = FontWeight.Black)
                                                    }
                                                }
                                                
                                                Row {
                                                    if (index > 0) {
                                                        IconButton(
                                                            onClick = { 
                                                                val newList = tempWaypoints.toMutableList()
                                                                val item = newList.removeAt(index)
                                                                newList.add(index - 1, item)
                                                                tempWaypoints = newList
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.KeyboardArrowUp, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(20.dp))
                                                        }
                                                    }
                                                    if (index < tempWaypoints.size - 1) {
                                                        IconButton(
                                                            onClick = { 
                                                                val newList = tempWaypoints.toMutableList()
                                                                val item = newList.removeAt(index)
                                                                newList.add(index + 1, item)
                                                                tempWaypoints = newList
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(20.dp))
                                                        }
                                                    }
                                                    Spacer(Modifier.width(8.dp))
                                                    IconButton(
                                                        onClick = { tempWaypoints = tempWaypoints.toMutableList().apply { removeAt(index) } },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text("4. CONFIGURACIÓN DE RUTA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold, letterSpacing = 1.sp)
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(onClick = { isPrivateSelection = !isPrivateSelection }, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(16.dp), color = if (isPrivateSelection) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f), border = BorderStroke(1.dp, if (isPrivateSelection) LuxeColors.Gold else Color.White.copy(0.1f))) {
                                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Icon(if (isPrivateSelection) Icons.Rounded.Lock else Icons.Rounded.LockOpen, null, tint = if (isPrivateSelection) LuxeColors.Gold else Color.White.copy(0.4f), modifier = Modifier.size(20.dp))
                                            Text("PRIVADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (isPrivateSelection) LuxeColors.Gold else Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Switch(checked = state.isAvoidingHighways || selectedActivity == ActivityProfile.MOTO, onCheckedChange = { if (selectedActivity != ActivityProfile.MOTO) onStateChange(state.copy(isAvoidingHighways = it)) }, enabled = selectedActivity != ActivityProfile.MOTO, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold, checkedTrackColor = LuxeColors.Gold.copy(0.3f)))
                                        Text("MODO CURVAS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                if (isPrivateSelection) {
                                    OutlinedTextField(value = tempSubtone, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) tempSubtone = it }, placeholder = { Text("CÓDIGO (4 CIFRAS)", color = Color.White.copy(0.2f)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuxeColors.Gold, focusedBorderColor = LuxeColors.Gold), leadingIcon = { Icon(Icons.Rounded.Key, null, tint = LuxeColors.Gold) })
                                }
                                Spacer(Modifier.height(32.dp))
                                LuxeButton(text = if (isLaunching) "INICIANDO..." else "INICIAR RUTA", onClick = {
                                    if (tempRouteName.isNotBlank()) {
                                        isLaunching = true
                                        onStateChange(state.copy(activeProfile = selectedActivity!!, isMotoModeEnabled = true, isAvoidingHighways = state.isAvoidingHighways || selectedActivity == ActivityProfile.MOTO, channel = tempRouteName, subtone = if (isPrivateSelection) tempSubtone.padStart(4, '0') else "0000", routeRules = tempRouteRules.ifBlank { null }, routeWaypoints = tempWaypoints))
                                        onExecuteEngineeringAction("UPDATE_ACTIVE_PROFILE|${selectedActivity!!.name}")
                                        if (tempWaypoints.isNotEmpty()) {
                                            val jsonWaypoints = "[" + tempWaypoints.joinToString(",") { """{"name":"${it.name}","lat":${it.lat},"lon":${it.lon}}""" } + "]"
                                            onExecuteEngineeringAction("SET_MISSION_ROUTE|$jsonWaypoints")
                                        } else if (destinationText.isNotBlank()) {
                                            onExecuteEngineeringAction("SEARCH_LOCATION|$destinationText")
                                        } else {
                                            val poiCat = when(selectedActivity) {
                                                ActivityProfile.MOTO, ActivityProfile.CARAVANAS -> "GASOLINERAS"
                                                ActivityProfile.CICLISMO -> "FUENTES"
                                                ActivityProfile.SENDERISMO, ActivityProfile.PASEO -> "NATURALEZA"
                                                ActivityProfile.SOCORRISTAS -> "HOSPITALES"
                                                else -> "MONUMENTOS"
                                            }
                                            onExecuteEngineeringAction("FETCH_POIS|$poiCat")
                                        }
                                        onDismiss()
                                        onActivityPanelRequest()
                                    }
                                }, enabled = tempRouteName.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp), containerColor = LuxeColors.Gold, contentColor = Color.Black)
                            }
                        }
                        Spacer(Modifier.height(100.dp)) 
                    }
                },
                confirmButton = {}
            )
        }
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
                        onStateChange(if (state.channel == channelToDelete) newState.copy(channel = "GENERAL") else newState)
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
            val activeRooms = users.filter { it.city == state.city && it.channel != "GENERAL" }
                .groupBy { it.channel }
                .map { (name, uInRoom) ->
                    val activity = uInRoom.map { it.activity }.find { it != ActivityProfile.NORMAL } ?: ActivityProfile.NORMAL
                    val subtone = uInRoom.firstOrNull()?.subtone ?: "0000"
                    QuadItem(name, uInRoom.size, activity, subtone)
                }.sortedByDescending { it.second }

            AlertDialog(
                onDismissRequest = onDismiss, 
                containerColor = LuxeColors.DeepSea, 
                titleContentColor = LuxeColors.Gold, 
                textContentColor = Color.White, 
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AddCircleOutline, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("CREAR O ENTRAR CANAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("Crea tu Barrio, tu pueblo, tu lugar, tu sala... privada o pública.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                        Text("Escribe el nombre de un nuevo canal o selecciona uno de los activos en ${state.city}.", fontSize = 11.sp, color = Color.White.copy(0.6f), modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = newChannelName,
                            onValueChange = { if (it.length <= 20) newChannelName = it.uppercase() },
                            placeholder = { Text("NOMBRE CANAL (EJ: BARRIO OESTE)", color = Color.White.copy(0.2f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LuxeColors.Gold,
                                unfocusedBorderColor = Color.White.copy(0.1f)
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        if (activeRooms.isNotEmpty()) {
                            Text("SALAS ACTIVAS EN TU ZONA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.6f))
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(activeRooms) { room ->
                                    Surface(
                                        onClick = { 
                                            onStateChange(state.copy(channel = room.first, subtone = room.fourth, activeProfile = room.third))
                                            onDismiss()
                                            onNotification(AppNotification("CAMBIO DE SALA", "Has entrado en ${room.first}", NotificationType.Info))
                                        },
                                        color = Color.White.copy(0.03f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(getActivityIcon(room.third), null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text(room.first, modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("${room.second}", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Rounded.Group, null, tint = LuxeColors.Gold.copy(0.5f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (newChannelName.isNotBlank()) {
                        LuxeButton("CREAR SALA", {
                            onStateChange(state.copy(channel = newChannelName, favoriteChannels = state.favoriteChannels + newChannelName))
                            onDismiss()
                        }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.White.copy(0.4f)) }
                }
            )
        }
        RadioDialogType.RADAR_MAP -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxSize().padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Radar, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("RADAR NACIONAL", color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.4f)) }
                }
            },
            text = {
                Box(Modifier.fillMaxSize()) {
                    Text("Iniciando motor de radar...", color = LuxeColors.Gold, modifier = Modifier.align(Alignment.Center))
                }
            },
            confirmButton = {}
        )
        RadioDialogType.SOS_CONFIRM -> SOSConfirmDialog(
            onConfirm = {
                onStateChange(state.copy(activeProfile = ActivityProfile.SOCORRISTAS, channel = "EMERGENCIAS", isMotoModeEnabled = true))
                onExecuteEngineeringAction("UPDATE_ACTIVE_PROFILE|SOCORRISTAS")
                onDismiss()
            },
            onDismiss = onDismiss
        )
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
        RadioDialogType.SELECT_CITY -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("CAMBIAR DE CIUDAD", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Busca y desplázate a otra ubicación táctica.", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = state.city,
                        onValueChange = { onStateChange(state.copy(city = it.uppercase())) },
                        placeholder = { Text("EJ: MADRID", color = Color.White.copy(0.2f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxeColors.Gold, focusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = { LuxeButton("ENTRAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black) }
        )
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
                    Text("Configuración avanzada de hardware y red.", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(24.dp))
                    
                    // --- ⚙️ BOTÓN ACCESO NATIVO ---
                    LuxeButton(
                        text = "ABRIR AJUSTES ANDROID",
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
                            Text("ESTADO DE RED", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (wifiVerificationResult != null) "Red: $wifiVerificationResult" else "Verificando integridad...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                        channel = "GENERAL",
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
