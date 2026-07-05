package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - GESTIÓN DE DIÁLOGOS Y CONFIGURACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN 1.8 (FM & SETTINGS FIX)
 * 
 * Este archivo gestiona todas las ventanas emergentes, configuración de FM y 
 * ajustes de precisión. Blindado tras la estabilización de los filtros de radio.
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

enum class RadioDialogType {
    ANTENNA, WATTS, FRIENDS, DSP, RADAR, ECO, LOCK, REPLAY, PRO, VOX, MONI, ROGER, REVERB, CHAT, SCAN, FMSCAN, ADS,    INVITE, MIC_REQUEST, DELETE_ROOM, DELETE_DATA, PORTADORA, SUBTONO, CREATE_CHANNEL, RADAR_MAP, SOS_CONFIRM, BLACKLIST, ONBOARDING, SELECT_CITY, SETTINGS, NASA_IMAGE, HERTZ_SENTINEL, DISCRETE, ACTIVITY_SELECTOR, SELECT_NICK
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
        RadioDialogType.WATTS -> FeatureHelpDialog(
            title = "Potencia y Watts (W)",
            icon = Icons.Rounded.Speed,
            description = "Tu indicativo gana potencia cuanto más usas la radio. Al emitir verás tus vatios (W) reales.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenWattsIntro = true))
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
        RadioDialogType.DSP -> FeatureHelpDialog(
            title = "Sonido Profesional (DSP)",
            icon = Icons.Rounded.SettingsVoice,
            iconColor = LuxeColors.ElectricBlue,
            description = "Activa el procesador DSP para aplicar compresión y filtros reales.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenDspIntro = true, isDspEnabled = true))
                triggerUiSound("switch")
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
        RadioDialogType.PRO -> FeatureHelpDialog(
            title = "Módulo Profesional",
            icon = Icons.Rounded.Handshake,
            iconColor = LuxeColors.ElectricBlue,
            description = "Indica los servicios que buscas u ofreces en tiempo real.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenProIntro = true, isWorkModeActive = true))
            }
        )
        RadioDialogType.VOX -> FeatureHelpDialog(
            title = "Manos Libres (VOX)",
            icon = Icons.Rounded.Mic,
            description = "Activa el micrófono automáticamente al detectar tu voz.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenVoxIntro = true, isVoxEnabled = true))
                triggerUiSound("switch")
            }
        )
        RadioDialogType.MONI -> FeatureHelpDialog(
            title = "Monitor de Voz",
            icon = Icons.Rounded.Headset,
            description = "Escucha tu propia voz mientras hablas. Se recomienda usar AURICULARES.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenMoniIntro = true, isMonitorEnabled = true))
                triggerUiSound("switch")
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
        RadioDialogType.REVERB -> FeatureHelpDialog(
            title = "Efectos de Audio",
            icon = Icons.Rounded.GraphicEq,
            description = "Añade Reverb o Eco a tu transmisión.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenReverbIntro = true, isEchoEnabled = true))
                triggerUiSound("switch")
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
                                onBgRadioScan(state.city, selectedGenre)
                                onDismiss()
                            },
                            enabled = true,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            containerColor = LuxeColors.Gold,
                            contentColor = Color.Black
                        )
                        
                        TextButton(
                            onClick = { onBgRadioStop(); onDismiss() },
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
                            onBgRadioScan(state.city, "ANUNCIOS")
                            onDismiss()
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
        RadioDialogType.DISCRETE -> FeatureHelpDialog(
            title = "Modo Discreto",
            icon = Icons.Rounded.VolumeOff,
            description = "Para tu privacidad, si la radio está guardada o la pantalla apagada, no soltará voces automáticamente. Te daremos un aviso y tú decides si quieres escuchar.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(hasSeenDiscreteIntro = true, isDiscreteModeEnabled = !state.isDiscreteModeEnabled))
                triggerUiSound("switch")
            }
        )
        RadioDialogType.ACTIVITY_SELECTOR -> {
            var tempRouteName by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Route, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("CREAR NUEVA RUTA", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        Text("Pon un nombre a tu ruta y selecciona tu perfil para empezar a coordinar al grupo.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                        
                        Spacer(Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = tempRouteName,
                            onValueChange = { if (it.length <= 15) tempRouteName = it.uppercase() },
                            label = { Text("NOMBRE DE LA RUTA (Ej: RUTA66)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxeColors.Gold,
                                focusedLabelColor = LuxeColors.Gold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(0.1f)
                            )
                        )

                        Spacer(Modifier.height(24.dp))
                        Text("SELECCIONA TU ACTIVIDAD:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                        Spacer(Modifier.height(12.dp))
                        
                        val activities = ActivityProfile.entries
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 240.dp)) {
                            items(activities) { act ->
                                val isSelected = state.activeProfile == act
                                val isEnabled = tempRouteName.isNotBlank()
                                
                                Surface(
                                    onClick = { 
                                        if (isEnabled) {
                                            val finalChannel = if (tempRouteName.isNotBlank()) tempRouteName else state.channel
                                            onStateChange(state.copy(
                                                activeProfile = act, 
                                                isMotoModeEnabled = act != ActivityProfile.NORMAL,
                                                channel = finalChannel
                                            ))
                                            onDismiss()
                                            if (act != ActivityProfile.NORMAL) {
                                                onActivityPanelRequest()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.05f),
                                    border = BorderStroke(1.dp, if (isSelected) LuxeColors.Gold else Color.White.copy(0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val contentAlpha = if (isEnabled) 1f else 0.3f
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val icon = when(act) {
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
                                            else -> Icons.Rounded.Person
                                        }
                                        Icon(icon, null, tint = (if (isSelected) LuxeColors.Gold else Color.White.copy(0.4f)).copy(alpha = contentAlpha))
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            val label = when(act) {
                                                ActivityProfile.MOTO -> "MODO MOTO"
                                                ActivityProfile.CICLISMO -> "MODO CICLISMO"
                                                ActivityProfile.SENDERISMO -> "SENDERISMO"
                                                ActivityProfile.PASEO -> "CAMINAR / PASEO"
                                                ActivityProfile.MONTANA -> "MONTAÑA"
                                                ActivityProfile.SOCORRISTAS -> "SOCORRISTAS"
                                                ActivityProfile.CAMIONEROS -> "CAMIONEROS"
                                                ActivityProfile.CARAVANAS -> "CARAVANAS"
                                                ActivityProfile.OFFROAD -> "OFFROAD / 4X4"
                                                ActivityProfile.TACTICO -> "CANAL TÁCTICO"
                                                ActivityProfile.RUNNING -> "RUNNING"
                                                else -> "ESTÁNDAR"
                                            }
                                            Text(label, color = Color.White.copy(alpha = contentAlpha), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            val desc = when(act) {
                                                ActivityProfile.MOTO -> "Filtro Viento/Motor + G-Force"
                                                ActivityProfile.CICLISMO -> "Filtro Viento + G-Force"
                                                ActivityProfile.SENDERISMO -> "Filtro Voz + Radar GPS"
                                                ActivityProfile.PASEO -> "Ahorro Batería + Compañía"
                                                ActivityProfile.MONTANA -> "Radar GPS + Emergencia"
                                                ActivityProfile.SOCORRISTAS -> "Prioridad SOS + Voz Nítida"
                                                ActivityProfile.CAMIONEROS -> "Canal de Ruta + Tráfico"
                                                ActivityProfile.CARAVANAS -> "Viaje en Grupo + Chat"
                                                ActivityProfile.OFFROAD -> "Radar de Polvo + GPS Táctico"
                                                ActivityProfile.TACTICO -> "Malla WiFi + Encriptación"
                                                ActivityProfile.RUNNING -> "Manos Libres + Ritmo"
                                                else -> "Modo Estándar"
                                            }
                                            Text(desc, color = Color.White.copy(alpha = contentAlpha * 0.4f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.White.copy(0.4f)) } }
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
                        onNotification(AppNotification("SALA ELIMINADA", "Has vuelto a GENERAL", NotificationType.Info))
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
            title = { Text("CONFIGURAR SUBTONO (CTCSS)", fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "Al activar un subtono creas un canal privado. Solo quienes tengan tu mismo código podrán escucharte.",
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
                .groupBy { it.channel }.mapValues { it.value.size }
                .keys.toList().sorted()

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
                            onValueChange = { if (it.length <= 15) newChannelName = it.uppercase() }, 
                            placeholder = { Text("BARRIO, PUEBLO, SALA...", color = Color.White.copy(0.2f)) },
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(16.dp), 
                            singleLine = true, 
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = LuxeColors.Gold)
                        )

                        Spacer(Modifier.height(16.dp))

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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LuxeColors.Gold, focusedBorderColor = LuxeColors.Gold)
                            )
                        }
                        
                        if (activeRooms.isNotEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            Text("CANALES DISPONIBLES:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold.copy(0.6f))
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(activeRooms) { room ->
                                    val isFav = state.favoriteChannels.contains(room)
                                    Surface(
                                        onClick = { 
                                            onStateChange(state.copy(channel = room))
                                            onDismiss() 
                                        },
                                        color = Color.White.copy(0.05f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isFav) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.1f))
                                    ) {
                                        Text(room, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                                item { Spacer(Modifier.width(180.dp)) }
                            }
                        }
                        Spacer(Modifier.height(180.dp))
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
                                    onDismiss() 
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
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color.White.copy(0.4f))
                    }
                }
            )
        }
        RadioDialogType.RADAR_MAP -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            text = { NationalRadarMap(users = users, onCitySelect = { onStateChange(state.copy(city = it, channel = "GENERAL")); onDismiss() }) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("CERRAR") } }
        )
        RadioDialogType.SOS_CONFIRM -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.Red,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(12.dp)),
            icon = { Icon(Icons.Rounded.Warning, null, tint = Color.White, modifier = Modifier.size(40.dp)) },
            title = { Text("¿ACTIVAR ALERTA SOS?", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Al activar el SOS ocurrirá lo siguiente:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("🚨 Sonará una SIRENA en los móviles de todos los usuarios de ${state.city}.", fontSize = 12.sp)
                    Text("📲 Recibirán una NOTIFICACIÓN CRÍTICA con tu indicativo.", fontSize = 12.sp)
                    Text("📍 Se enviará tu UBICACIÓN GPS a toda la red.", fontSize = 12.sp)
                    Text("🔴 Te marcarás en ROJO parpadeante en la lista profesional.", fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("⚠️ AVISO: Esta es una herramienta de COMUNIDAD. En caso de emergencia real con riesgo vital, llame siempre al 112.", 
                        fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("El mal uso conlleva baneo permanente.", 
                        fontSize = 10.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                LuxeButton(
                    text = if (isLocatingGps) "LOCALIZANDO..." else "ACTIVAR Y ENVIAR GPS",
                    onClick = {
                        isLocatingGps = true
                        onGpsRequestPro { url ->
                            isLocatingGps = false
                            onStateChange(state.copy(myIsSOS = true, myGpsUrl = url))
                            triggerUiSound("siren")
                            onDismiss()
                        }
                    },
                    enabled = !isLocatingGps,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = Color.White,
                    contentColor = LuxeColors.Red
                )
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR", color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold)
                }
            }
        )
        RadioDialogType.PORTADORA -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.Red,
            title = { Text("PORTADORA") },
            text = { Text("No envíe portadora sin voz.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        RadioDialogType.BLACKLIST -> BlacklistDialog(
            blockedUsers = state.blockedUsers,
            allUsers = users,
            onUnblock = { id -> onStateChange(state.copy(blockedUsers = state.blockedUsers - id)) },
            onDismiss = onDismiss
        )
        RadioDialogType.ONBOARDING -> OnboardingDialog(onDismiss = onDismiss)
        RadioDialogType.SELECT_CITY -> {
            var citySearch by remember { mutableStateOf("") }
            val filteredCities = remember(citySearch) {
                SPAIN_CITIES.filter { it.contains(citySearch.uppercase()) }
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationCity, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("SINTONIZAR CIUDAD", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        // --- 📡 BOTÓN GPS AUTO-TUNE ---
                        Surface(
                            onClick = {
                                isLocatingGps = true
                                onGpsCityRequestPro { detectedCity ->
                                    isLocatingGps = false
                                    if (detectedCity != null) {
                                        onStateChange(state.copy(city = detectedCity.uppercase(), channel = "GENERAL"))
                                        onNotification(AppNotification("SINTONIZACIÓN GPS", "Conectado a $detectedCity", NotificationType.Success))
                                        onDismiss()
                                    } else {
                                        onNotification(AppNotification("GPS NO EXACTO", "No hemos podido determinar tu ciudad con precisión.", NotificationType.Warning))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = LuxeColors.ElectricBlue.copy(0.1f),
                            border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.4f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                if (isLocatingGps) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LuxeColors.ElectricBlue, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Rounded.MyLocation, null, tint = LuxeColors.ElectricBlue)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(if (isLocatingGps) "LOCALIZANDO..." else "SINTONIZAR POR GPS (EXACTO)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Text(
                            "Esta función utiliza tu ubicación solo para sintonizar la ciudad más cercana. Los datos no se almacenan.", 
                            fontSize = 9.sp, 
                            color = Color.White.copy(0.4f),
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = citySearch,
                            onValueChange = { citySearch = it },
                            placeholder = { Text("BUSCAR CIUDAD...", color = Color.White.copy(0.2f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = LuxeColors.Gold)
                        )

                        Spacer(Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            // --- 🆕 OPCIÓN PARA SINTONIZAR PUEBLO PERSONALIZADO ---
                            if (citySearch.isNotBlank() && !SPAIN_CITIES.contains(citySearch.uppercase())) {
                                item {
                                    Surface(
                                        onClick = {
                                            onStateChange(state.copy(city = citySearch.uppercase(), channel = "GENERAL"))
                                            onDismiss()
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = LuxeColors.ElectricBlue.copy(0.1f),
                                        border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f))
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.AddLocation, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text("SINTONIZAR: ${citySearch.uppercase()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            items(filteredCities) { city ->
                                val isSelected = city == state.city
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) LuxeColors.Gold.copy(0.1f) else Color.Transparent)
                                        .clickable { 
                                            onStateChange(state.copy(city = city, channel = "GENERAL"))
                                            onDismiss() 
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        city, 
                                        color = if (isSelected) LuxeColors.Gold else Color.White, 
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Rounded.Check, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("CERRAR", color = Color.White.copy(0.4f)) }
                }
            )
        }
        RadioDialogType.SETTINGS -> {
            /**
             * 🔒 HARD-LOCK: CONSOLA DE PRECISIÓN - FOCO DE INTERACCIÓN
             * PROHIBIDO ELIMINAR DialogProperties O MODIFICAR EL MODIFIER DE FOCO.
             * Asegura que los botones respondan al primer toque en Android WebView.
             */
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                // 🛡️ FIX: Forzamos el foco y el aislamiento para evitar que la ventana se quede "congelada" o sin respuesta
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Tune, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("CONSOLA DE PRECISIÓN", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        // --- 🛰️ ACCESO PRIORITARIO AL RADAR HERTZ (VINCULACIÓN NATIVA) ---
                        Surface(
                            onClick = { 
                                onDismiss()
                                onHertzSentinelRequest()
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF06B6D4).copy(0.15f), // Cian eléctrico para máxima visibilidad
                            border = BorderStroke(2.dp, Color(0xFF06B6D4).copy(0.5f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(Color(0xFF06B6D4).copy(0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Radar, null, tint = Color(0xFF06B6D4), modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("ACTIVAR RADAR HERTZ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text("Escáner real de presencia tras muros", color = Color(0xFF06B6D4), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = Color.White.copy(0.4f))
                            }
                        }

                        // --- 🏃 ACCESO AL MODO ACTIVIDAD (DEPORTES / MANOS LIBRES) ---
                        Surface(
                            onClick = { 
                                onDismiss()
                                if (state.activeProfile == ActivityProfile.NORMAL) {
                                    onPendingDialogChange(RadioDialogType.ACTIVITY_SELECTOR)
                                } else {
                                    onActivityPanelRequest()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = LuxeColors.Gold.copy(0.1f),
                            border = BorderStroke(2.dp, LuxeColors.Gold.copy(0.3f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(LuxeColors.Gold.copy(0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when(state.activeProfile) {
                                        ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
                                        ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
                                        ActivityProfile.SENDERISMO -> Icons.Rounded.DirectionsWalk
                                        ActivityProfile.MONTANA -> Icons.Rounded.Terrain
                                        ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
                                        else -> Icons.Rounded.DirectionsRun
                                    }
                                    Icon(icon, null, tint = LuxeColors.Gold, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("MODO ACTIVIDAD / DEPORTES", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text(if (state.activeProfile != ActivityProfile.NORMAL) "Activo: ${state.activeProfile.name}" else "Configura manos libres y red de supervivencia", color = LuxeColors.Gold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = Color.White.copy(0.4f))
                            }
                        }

                        EliteSlider("SQUELCH", state.squelch) { onStateChange(state.copy(squelch = it)) }
                        Spacer(Modifier.height(20.dp))
                        EliteSlider("RF GAIN", state.rfGain) { onStateChange(state.copy(rfGain = it)) }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EliteSwitch(Modifier.weight(1f), "R. BEEP", state.isRogerBeepEnabled) { onStateChange(state.copy(isRogerBeepEnabled = it)) }
                            EliteSwitch(Modifier.weight(1f), "VOX AUTO", state.isVoxEnabled) { onStateChange(state.copy(isVoxEnabled = it)) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EliteSwitch(Modifier.weight(1f), "MONITOR", state.isMonitorEnabled) { onStateChange(state.copy(isMonitorEnabled = it)) }
                            EliteSwitch(Modifier.weight(1f), "ECHO DSP", state.isEchoEnabled) { onStateChange(state.copy(isEchoEnabled = it)) }
                        }
                        
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
                                onClick = { onDismiss(); onShowHelp() },
                                modifier = Modifier.weight(1.5f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = LuxeColors.ElectricBlue.copy(0.1f),
                                border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.4f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.BatteryChargingFull, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("SIN RESTRICCIONES", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Mapa Radar (Movido aquí)
                            Surface(
                                onClick = { 
                                    onDismiss()
                                    onPendingDialogChange(RadioDialogType.RADAR_MAP)
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.Radar, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("MAPA", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                                }
                            }

                            // Tutorial Completo
                            Surface(
                                onClick = { 
                                    onDismiss()
                                    onPendingDialogChange(RadioDialogType.ONBOARDING)
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.Info, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("TUTORIAL", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                                }
                            }

                            // Derecho al Olvido (Borrar datos)
                            Surface(
                                onClick = { 
                                    onDismiss()
                                    onPendingDialogChange(RadioDialogType.DELETE_DATA)
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Red.copy(0.1f),
                                border = BorderStroke(1.dp, Color.Red.copy(0.3f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.DeleteSweep, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("OLVIDO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(120.dp)) // 🛡️ FIX: Asegura que el botón no sea tapado por el banner en móviles
                    }
                },
                confirmButton = {}
            )
        }
        RadioDialogType.HERTZ_SENTINEL -> Box(Modifier.fillMaxSize()) {
            HertzSentinelScreen(
                onGetWifiVariance = onGetWifiVariance,
                onGetHeading = onGetHeading,
                onGetTilt = onGetTilt,
                onEstadoCambio = onEstadoCambio,
                onShare = onShare,
                onNotification = onNotification,
                onPlaySound = onPlaySound,
                onExecuteEngineeringAction = onExecuteEngineeringAction,
                onRequestPermission = onRequestLocationPermission,
                onOpenSettings = onOpenSettings,
                initialRfSensitivity = state.radarRfSensitivity,
                initialMagSensitivity = state.radarMagSensitivity,
                onSensitivityChange = { rf, mag ->
                    onStateChange(state.copy(radarRfSensitivity = rf, radarMagSensitivity = mag))
                },
                onClose = onDismiss,
                wifiAuthResult = if (state.capturedCodes.any { it.proto == "WIFI_VERIFY" }) state.capturedCodes.last { it.proto == "WIFI_VERIFY" }.data else null,
                engineeringPanelVisible = engineeringPanelVisible,
                onEngineeringPanelChange = onEngineeringPanelChange
            )
        }
        RadioDialogType.NASA_IMAGE -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.AutoAwesome, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { 
                Text(
                    state.nasaImageTitle ?: "BOLETÍN NASA: IMAGEN DEL DÍA", 
                    fontWeight = FontWeight.Black, 
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                ) 
            },
            text = {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    // --- 🌌 PREVISUALIZACIÓN DE IMAGEN (APOD) ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { 
                                state.nasaImageUrl?.let { uriHandler.openUri(it) }
                            },
                        color = Color.Black.copy(0.4f),
                        border = BorderStroke(1.dp, Color.White.copy(0.15f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.RocketLaunch, null, tint = Color.White.copy(0.05f), modifier = Modifier.size(100.dp))
                            Text(
                                "TOCA PARA ABRIR IMAGEN EN ALTA RESOLUCIÓN",
                                color = Color.White.copy(0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 120.dp).padding(horizontal = 20.dp)
                            )
                        }
                    }

                    /* Eliminado el título repetido del cuerpo para mayor limpieza visual */

                    if (state.nasaImageExplanation != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            state.nasaImageExplanation,
                            fontSize = 13.sp,
                            color = Color.White.copy(0.8f),
                            textAlign = TextAlign.Justify,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LuxeButton(
                            text = "COMPARTIR",
                            onClick = { 
                                onShare(state.city, state.channel, "NASA", null) 
                            },
                            enabled = true,
                            modifier = Modifier.weight(1f).height(54.dp),
                            containerColor = LuxeColors.ElectricBlue.copy(0.2f),
                            contentColor = Color.White,
                            icon = Icons.Rounded.Share
                        )

                        LuxeButton(
                            text = "DESCARGAR",
                            onClick = { 
                                state.nasaImageUrl?.let { uriHandler.openUri(it) }
                            },
                            enabled = state.nasaImageUrl != null,
                            modifier = Modifier.weight(1f).height(54.dp),
                            containerColor = LuxeColors.Gold,
                            contentColor = Color.Black,
                            icon = Icons.Rounded.Download
                        )
                    }
                    
                    Spacer(Modifier.height(180.dp)) // 🛡️ FIX: Espacio para que el scroll permita ver todo y no tape el banner
                }
            },
            confirmButton = {}
        )
        RadioDialogType.SELECT_NICK -> {
            var tempNick by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { /* No cerrar si no hay nick */ },
                containerColor = LuxeColors.DeepSea,
                titleContentColor = LuxeColors.Gold,
                modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PersonAdd, null, tint = LuxeColors.Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("IDENTIFICARSE", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        Text("¡Bienvenido a la ruta! Introduce un nombre o indicativo para que tus compañeros te vean en el radar.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                        
                        Spacer(Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = tempNick,
                            onValueChange = { if (it.length <= 15) tempNick = it.uppercase().replace(" ", "") },
                            label = { Text("TU NOMBRE / INDICATIVO", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxeColors.Gold,
                                focusedLabelColor = LuxeColors.Gold,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(0.1f)
                            )
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        Text("Al entrar aceptas el uso del micrófono para transmitir tu voz al grupo.", fontSize = 9.sp, color = Color.White.copy(0.3f), textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    LuxeButton(
                        text = "ENTRAR A RUTA",
                        onClick = {
                            if (tempNick.isNotBlank()) {
                                onStateChange(state.copy(hasAcceptedMicExplain = true))
                                onNickChange(tempNick)
                                onPermissionRequest(tempNick)
                                onDismiss()
                            }
                        },
                        enabled = tempNick.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        containerColor = LuxeColors.Gold,
                        contentColor = Color.Black
                    )
                }
            )
        }
        else -> {}
    }
}
