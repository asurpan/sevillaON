package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - GESTIÓN DE DIÁLOGOS Y CONFIGURACIÓN
 * ESTADO: SELLADO TOTAL - VERSIÓN 3.0 (RADIO LIMPITA)
 * 
 * Gestiona todas las ventanas emergentes y configuraciones.
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

enum class RadioDialogType {
    ANTENNA, WATTS, FRIENDS, DSP, RADAR, ECO, LOCK, REPLAY, VOX, MONI, ROGER, REVERB, CHAT, 
    INVITE, MIC_REQUEST, DELETE_ROOM, DELETE_DATA, PORTADORA, SUBTONO, CREATE_CHANNEL, 
    BLACKLIST, ONBOARDING, SELECT_CITY, SETTINGS, DISCRETE, SELECT_NICK, 
    HELP_SQUELCH, HELP_GAIN, HELP_PRIVACY, USER_ACTIONS, SQUELCH_CONTROL, GAIN_CONTROL, VOLUME_CONTROL
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
    onShare: (String, String, String?, String?) -> Unit,
    onNotification: (AppNotification) -> Unit,
    onLogoutConfirm: () -> Unit,
    onMic: (Boolean, Float) -> Unit,
    onPendingDialogChange: (RadioDialogType?, String?) -> Unit,
    onNickChange: (String) -> Unit = {},
    users: List<RemoteUser>,
    nick: String,
    channelToDelete: String? = null
) {
    var tempSubtone by remember(type) { mutableStateOf(state.subtone) }

    when (type) {
        RadioDialogType.ANTENNA -> FeatureHelpDialog(
            title = "Sistema de Calibración",
            icon = Icons.Rounded.SettingsInputAntenna,
            description = "Antes de salir al aire, verifica tu modulación. Al activarlo, entrarás en modo de 'retorno local' para oír tu propia voz en tiempo real.",
            onDismiss = { 
                onDismiss()
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
                        "• Las estaciones nuevas empiezan con 0.7W.\n• El rango VETERANO se alcanza a los 0.85W.\n• Con 0.85W desbloqueas la creación de códigos de privacidad (Subtonos).\n• El máximo permitido es 15W.",
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
            title = "Radar de España",
            icon = Icons.Rounded.Radar,
            description = "El radar te informa de cuántos operadores hay activos en cada ciudad.",
            onDismiss = { 
                onDismiss()
            }
        )
        RadioDialogType.ECO -> FeatureHelpDialog(
            title = "Modo Eco Inteligente",
            icon = Icons.Rounded.Eco,
            iconColor = Color(0xFF4CAF50),
            description = "Ahorra batería pausando los efectos visuales avanzados.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(isEcoMode = !state.isEcoMode))
            }
        )
        RadioDialogType.LOCK -> FeatureHelpDialog(
            title = "Bloqueo de Equipo",
            icon = Icons.Rounded.Lock,
            description = "Evita cambios accidentales bloqueando los controles.",
            onDismiss = { 
                onDismiss()
                onStateChange(state.copy(isInterfaceLocked = true))
            }
        )
        RadioDialogType.REPLAY -> FeatureHelpDialog(
            title = "Rebobinado (Replay)",
            icon = Icons.Rounded.History,
            description = "¿No has oído bien? El Replay te permite repetir los últimos 15 segundos.",
            onDismiss = { 
                onDismiss()
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
                onStateChange(state.copy(isRogerBeepEnabled = true))
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
                onPublicChat()
                triggerUiSound("click")
            }
        )
        RadioDialogType.INVITE -> FeatureHelpDialog(
            title = "Invitar a la Red",
            icon = Icons.Rounded.Share,
            description = "Comparte un enlace directo.",
            onDismiss = { 
                onDismiss()
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
                        "Para tu privacidad, cuando este modo está activo, la radio no emitirá voces automáticamente si tienes la pantalla apagada.",
                        fontSize = 13.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center
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
                title = { Row { Icon(Icons.Rounded.DeleteSweep, null, tint = LuxeColors.Red); Text(" ELIMINAR CANAL") } },
                text = { Text("¿Eliminar canal $channelToDelete?") },
                confirmButton = {
                    Button(onClick = {
                        val newState = state.copy(favoriteChannels = state.favoriteChannels - channelToDelete)
                        onStateChange(if (state.channel == channelToDelete) newState.copy(channel = state.city) else newState)
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Red)) { Text("ELIMINAR") }
                }
            )
        }
        RadioDialogType.DELETE_DATA -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(2.dp, Color.Red.copy(0.6f), RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.GppMaybe, null, tint = Color.Red, modifier = Modifier.size(54.dp)) },
            title = { Text("☢️ ALERTA DE SEGURIDAD CRÍTICA", fontWeight = FontWeight.Black, color = Color.Red, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "AL BORRAR DATOS PERDERÁS TODA TU POTENCIA (WATTS) ACUMULADA.",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "La veteranía es la única forma de dominar el canal (efecto de pisado) y acceder a funciones Élite. " +
                        "Tu dispositivo está registrado en la base de datos central; el cambio reiterado de indicativo se detecta como ACTIVIDAD HOSTIL. " +
                        "El abuso de esta función para evadir bloqueos o suplantar identidades resultará en la RESTRICCIÓN FÍSICA PERMANENTE (BAN-HARDWARE) de tu terminal para proteger la red.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "¿DESEAS PROCEDER Y DESTRUIR TU RANGO ACTUAL?",
                        fontSize = 11.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }
            },
            confirmButton = { 
                LuxeButton("SÍ, DESTRUIR VETERANÍA", { onDismiss(); onLogoutConfirm() }, true, Modifier.fillMaxWidth().height(52.dp), Color.Red, Color.White) 
            },
            dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 10.dp)) { 
                    Text("CANCELAR Y MANTENER MIS VATIOS", color = LuxeColors.Gold, fontWeight = FontWeight.Black, fontSize = 12.sp) 
                }
            }
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
                    if (state.veteranPower < 0.85f) {
                        Surface(
                            color = Color.Red.copy(0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.Lock, null, tint = Color.Red, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("FUNCIÓN BLOQUEADA", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Solo las estaciones con rango VETERANO (0.85W+) pueden crear canales privados.",
                                    color = Color.White.copy(0.7f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            "Al activar un código creas un canal privado.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.White.copy(0.7f)
                        )
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = tempSubtone,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() } ) tempSubtone = it },
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
                    }
                }
            },
            confirmButton = {
                if (state.veteranPower >= 0.85f && tempSubtone.length == 4) {
                    TextButton(onClick = {
                        val finalSub = tempSubtone.padStart(4, '0')
                        onStateChange(state.copy(subtone = finalSub))
                        onShare(state.channel, finalSub, null, null)
                        onDismiss()
                    }) {
                        Icon(Icons.Rounded.Share, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ACTIVAR Y COMPARTIR", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (state.veteranPower < 0.85f) {
                    LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), Color.White.copy(0.1f), Color.White)
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
                        Text("CÓDIGO PRIVADO", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("Crea un grupo privado en ${state.city}.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuxeColors.Gold)
                        Spacer(Modifier.height(20.dp))
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
                        
                        if (state.subtone != "0000") {
                            Spacer(Modifier.height(16.dp))
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
                                    "SALIR DEL CÓDIGO PRIVADO", 
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
                        LuxeButton("ENTRAR AL CANAL", {
                            onStateChange(state.copy(subtone = newChannelSubtone))
                            onDismiss()
                        }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                    }
                }
            )
        }
        RadioDialogType.USER_ACTIONS -> {
            val targetUser = users.find { it.id == channelToDelete } // Reusamos channelToDelete como generic payload ID
            if (targetUser != null) {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    containerColor = LuxeColors.DeepSea,
                    modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.Black, border = BorderStroke(2.dp, LuxeColors.Gold)) {
                                Icon(Icons.Rounded.Person, null, tint = LuxeColors.Gold, modifier = Modifier.padding(12.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(targetUser.nick, fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                            Text("ESTACIÓN EN ZONA", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("¿Qué deseas hacer con esta estación?", color = Color.White.copy(0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            
                            LuxeButton(
                                text = if (state.friends.contains(targetUser.nick)) "QUITAR DE AMIGOS" else "MARCAR COMO AMIGO",
                                onClick = {
                                    val newFriends = if (state.friends.contains(targetUser.nick)) state.friends - targetUser.nick else state.friends + targetUser.nick
                                    onStateChange(state.copy(friends = newFriends))
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                containerColor = if (state.friends.contains(targetUser.nick)) Color.Gray else LuxeColors.Gold,
                                contentColor = Color.Black,
                                icon = Icons.Rounded.Favorite
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            LuxeButton(
                                text = "REPORTAR USUARIO",
                                onClick = { 
                                    onNotification(AppNotification("REPORTE ENVIADO", "Tu queja sobre ${targetUser.nick} ha sido recibida por el equipo de control.", NotificationType.Success))
                                    onDismiss() 
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                containerColor = Color.White.copy(0.05f),
                                contentColor = Color.White,
                                icon = Icons.Rounded.Report
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            LuxeButton(
                                text = "BLOQUEAR ESTACIÓN",
                                onClick = { 
                                    onStateChange(state.copy(blockedUsers = state.blockedUsers + targetUser.id))
                                    onDismiss() 
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                containerColor = Color.Red.copy(0.1f),
                                contentColor = Color.Red,
                                icon = Icons.Rounded.Block
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.White.copy(0.4f)) }
                    }
                )
            }
        }
        RadioDialogType.BLACKLIST -> BlacklistDialog(
            blockedUsers = state.blockedUsers,
            users = users,
            onUnblock = { id -> onStateChange(state.copy(blockedUsers = state.blockedUsers - id)) },
            onDismiss = onDismiss
        )
        RadioDialogType.ONBOARDING -> WelcomeOnboarding(
            nick = nick,
            onStart = {
                onDismiss()
                triggerUiSound("click")
            }
        )
        RadioDialogType.SELECT_CITY -> {
            var searchText by remember { mutableStateOf(state.city) }
            
            val filteredCities = remember(searchText) {
                val normalizedSearch = searchText.uppercase()
                    .replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U')
                
                if (normalizedSearch.length >= 1) {
                    SPAIN_CITIES.filter { 
                        val normalizedCity = it.uppercase()
                            .replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U')
                        normalizedCity.contains(normalizedSearch) 
                    }.take(10)
                } else SPAIN_CITIES.take(10)
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
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (SPAIN_CITIES.contains(searchText)) {
                        LuxeButton("SINTONIZAR", {
                            onStateChange(state.copy(city = searchText, channel = searchText, subtone = "0000"))
                            onDismiss()
                        }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
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
        RadioDialogType.SETTINGS -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("AJUSTES DE EQUIPO", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Configuración de audio y sistema.", color = Color.White.copy(0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(20.dp))
                    
                    Text("AUDIO Y MODULACIÓN", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Roger Beep", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isRogerBeepEnabled, onCheckedChange = { onStateChange(state.copy(isRogerBeepEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Manos Libres (VOX)", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isVoxEnabled, onCheckedChange = { onStateChange(state.copy(isVoxEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }
                    if (state.isVoxEnabled) {
                        EliteSlider("SENSIBILIDAD VOX", state.voxSensitivity) { onStateChange(state.copy(voxSensitivity = it)) }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Monitor", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isMonitorEnabled, onCheckedChange = { onStateChange(state.copy(isMonitorEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }
                    if (state.isMonitorEnabled) {
                        EliteSlider("VOLUMEN MONITOR", state.monitorVolume) { onStateChange(state.copy(monitorVolume = it)) }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DSP", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isDspEnabled, onCheckedChange = { onStateChange(state.copy(isDspEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("SISTEMA", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Modo Discreto", modifier = Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
                        Switch(checked = state.isDiscreteModeEnabled, onCheckedChange = { onStateChange(state.copy(isDiscreteModeEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
                    }

                    Spacer(Modifier.height(20.dp))
                    
                    Text("HARDWARE", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    EliteSlider("SQUELCH", state.squelch) { onStateChange(state.copy(squelch = it)) }
                    EliteSlider("RF GAIN", state.rfGain) { onStateChange(state.copy(rfGain = it)) }
                    
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        onClick = { onPendingDialogChange(RadioDialogType.BLACKLIST, null) },
                        color = LuxeColors.Gold.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.Block, null, tint = LuxeColors.Gold, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("GESTIONAR BLOQUEADOS", color = LuxeColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    
                    Surface(
                        onClick = { onPendingDialogChange(RadioDialogType.DELETE_DATA, null) },
                        color = LuxeColors.Red.copy(0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxeColors.Red.copy(0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.DeleteSweep, null, tint = LuxeColors.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("BORRAR TODO Y SALIR", color = LuxeColors.Red, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = { LuxeButton("CERRAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black) }
        )
        RadioDialogType.SQUELCH_CONTROL -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("NIVEL DE SQUELCH", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
            text = {
                Column {
                    Text("Filtra el ruido de fondo. A mayor nivel, más limpia la señal pero menos sensibilidad.", fontSize = 12.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    EliteSlider("SQUELCH", state.squelch) { onStateChange(state.copy(squelch = it)) }
                }
            },
            confirmButton = { LuxeButton("ACEPTAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp)) }
        )
        RadioDialogType.GAIN_CONTROL -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("GANANCIA DE RF", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
            text = {
                Column {
                    Text("Ajusta la sensibilidad de recepción de tu antena.", fontSize = 12.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    EliteSlider("RF GAIN", state.rfGain) { onStateChange(state.copy(rfGain = it)) }
                }
            },
            confirmButton = { LuxeButton("ACEPTAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp)) }
        )
        RadioDialogType.VOLUME_CONTROL -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            title = { Text("VOLUMEN DE SALIDA", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
            text = {
                Column {
                    Text("Ajusta el volumen general del equipo. Sincronizado con el sistema.", fontSize = 12.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    EliteSlider("VOLUMEN", state.systemVolume) { onStateChange(state.copy(systemVolume = it)) }
                }
            },
            confirmButton = { LuxeButton("ACEPTAR", onDismiss, true, Modifier.fillMaxWidth().height(48.dp)) }
        )
        RadioDialogType.HELP_SQUELCH -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
            icon = { Icon(Icons.Rounded.Waves, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("¿QUÉ ES EL SQUELCH?", fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "El Squelch filtra el ruido de fondo.",
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
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
                Text(
                    "La ganancia ajusta la sensibilidad de tu antena.",
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
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
            title = { Text("UBICACIÓN", fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "Identificamos tu provincia mediante tu dirección IP.",
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.ElectricBlue, Color.White)
            }
        )
        else -> {}
    }
}

@Composable
private fun WelcomeOnboarding(nick: String, onStart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {}, 
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        title = { Text("ESTACIÓN CONFIGURADA", fontWeight = FontWeight.Black, color = LuxeColors.Gold) },
        text = {
            Text(
                "Bienvenido, $nick. Estás a punto de entrar en la red.",
                color = Color.White,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            LuxeButton("¡ADELANTE!", onStart, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
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
                label = { Text("TU NICK") },
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
