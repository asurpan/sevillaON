package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - COMPONENTES REUTILIZABLES
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.0 (PURE RADIO)
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
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StarryBackground(activity: Float = 0.1f, isEcoMode: Boolean = false, viewport: Rect? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "Starry")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "StarAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val count = if (isEcoMode) 20 else 60
        repeat(count) { i ->
            val x = (i * 137.5f) % size.width
            val y = (i * 221.7f) % size.height
            val radius = (i % 3 + 1).dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = starAlpha * (0.1f + activity)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun EliteSlider(label: String, value: Float, valueLabel: String? = null, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(valueLabel ?: "${(value * 100).toInt()}%", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Slider(value, onValueChange, colors = SliderDefaults.colors(thumbColor = LuxeColors.Gold, activeTrackColor = LuxeColors.Gold))
    }
}

@Composable
fun EliteSwitch(modifier: Modifier = Modifier, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = LuxeColors.Gold))
    }
}

@Composable
fun UserCard(
    user: RemoteUser,
    isMe: Boolean,
    onFriendToggle: () -> Unit,
    onPrivateChat: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onAvatarClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CarrierPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "Pulse"
    )

    // 🔵 TU USUARIO EN AZUL CIAN / 🟢 DEMÁS USUARIOS EN VERDE ESMERALDA
    val myBlueColor = Color(0xFF00E5FF) // Azul Cian Neón para ti
    val otherGreenColor = Color(0xFF10B981) // Verde Esmeralda para los demás
    
    val baseColor = if (isMe) myBlueColor else otherGreenColor

    Surface(
        modifier = Modifier.width(72.dp).height(78.dp).clickable(enabled = !isMe) { onAvatarClick() },
        color = baseColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.45f))
    ) {
        Column(Modifier.padding(horizontal = 4.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (user.isTransmitting) {
                    Box(Modifier.size(30.dp).scale(pulseScale).background(Color(0xFFEF4444).copy(0.25f), CircleShape))
                }
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = Color.Black,
                    border = BorderStroke(1.2.dp, if (user.isTransmitting) Color(0xFFEF4444) else baseColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (user.isTransmitting) Icons.Rounded.Mic else Icons.Rounded.Person,
                            contentDescription = null,
                            tint = if (user.isTransmitting) Color(0xFFEF4444) else baseColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // 🔵/🟢 Punto LED de estado "En Línea" (Azul para ti, Verde para los demás)
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .align(Alignment.BottomEnd)
                        .background(baseColor, CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.nick, 
                color = Color.White, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.Black, 
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LuxeSlider(label: String, value: Float, color: Color = LuxeColors.Gold, showIcon: Boolean = true, onValueChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showIcon) Icon(Icons.Rounded.GraphicEq, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Text("${(value * 100).toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Slider(value = value, onValueChange = onValueChange, colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color))
    }
}

@Composable
fun LuxeButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    containerColor: Color = LuxeColors.Gold,
    contentColor: Color = Color.Black,
    icon: ImageVector? = null,
    fontSize: TextUnit = 14.sp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.padding(16.dp).border(1.5.dp, Color(0xFF00E5FF).copy(0.6f), RoundedCornerShape(28.dp)),
        icon = { Icon(Icons.Rounded.Radio, null, tint = LuxeColors.Gold, modifier = Modifier.size(44.dp)) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BIENVENIDO A ON AIR SPAIN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Surface(color = Color(0xFF10B981).copy(0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(0.4f))) {
                    Text("🔒 100% GRATIS • AL SERVICIO PÚBLICO Y SOCIAL", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Esta aplicación ha sido creada de forma totalmente gratuita y al servicio público, desarrollada para y por las personas que aman la magia de la radio, las ondas y las conversaciones reales.",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Aquí no hay contraseñas, ni perfiles falsos, ni registros obligatorios. Solo la pureza de la voz en directo para conocer gente de tu ciudad, compartir ruta o pasar buenos ratos.",
                    color = Color.White.copy(0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                // Card 1: Radio CB
                Surface(color = Color.White.copy(0.04f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Radio, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("BANDA CIUDADANA Y RADIO SOCIAL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("Conecta en directo con gente de tu ciudad de forma abierta y transparente.", color = Color.White.copy(0.7f), fontSize = 10.sp)
                        }
                    }
                }
                // Card 2: Red de Malla P2P
                Surface(color = Color(0xFF00E5FF).copy(0.08f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(0.35f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Router, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("RED DE MALLA P2P EN EMERGENCIA", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("Voz móvil a móvil sin 4G/Internet. Ideal para rutas de montaña, eventos o catástrofes.", color = Color.White.copy(0.85f), fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton("🚀 COMENZAR LA EXPERIENCIA", onDismiss, true, Modifier.fillMaxWidth().height(50.dp), LuxeColors.Gold, Color.Black)
        }
    )
}

@Composable
fun FeatureHelpDialog(
    title: String,
    icon: ImageVector,
    iconColor: Color = LuxeColors.Gold,
    description: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        icon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(40.dp)) },
        title = { Text(title, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
        text = { Text(description, fontSize = 13.sp, color = Color.White.copy(0.7f), textAlign = TextAlign.Center) },
        confirmButton = { LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp)) }
    )
}

@Composable
fun PrivacyConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.padding(16.dp).border(1.5.dp, Color(0xFF00E5FF).copy(0.6f), RoundedCornerShape(28.dp)),
        icon = { Icon(Icons.Rounded.Security, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(42.dp)) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMPROMISO DE SEGURIDAD Y PRIVACIDAD", fontWeight = FontWeight.Black, fontSize = 15.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Surface(color = Color(0xFF10B981).copy(0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(0.4f))) {
                    Text("🔒 100% TRANSPARENTE Y SEGURO", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = Color.White.copy(0.04f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Mic, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("MICRÓFONO EN DIRECTO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("Solo se activa cuando mantengas pulsado el botón HABLAR (PTT) o uses Manos Libres. Tu voz NUNCA se graba.", color = Color.White.copy(0.75f), fontSize = 10.sp)
                        }
                    }
                }
                Surface(color = Color.White.copy(0.04f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("UBICACIÓN (OPCIONAL)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("Se procesa la distancia solo para medir la intensidad RF en el S-Meter (S9+30dB). Sin rastreos ni bases de datos.", color = Color.White.copy(0.75f), fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton("🚀 ENTENDIDO, ENTRAR A LA FRECUENCIA", onAccept, true, Modifier.fillMaxWidth().height(50.dp), LuxeColors.Gold, Color.Black)
        }
    )
}

@Composable
fun NotificationConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        title = { Text("NOTIFICACIONES", fontWeight = FontWeight.Black, color = Color.White) },
        text = { Text("Te avisaremos cuando tus amigos entren en frecuencia.", fontSize = 13.sp, color = Color.White.copy(0.7f)) },
        confirmButton = { LuxeButton("ACTIVAR", onAccept, true, Modifier.fillMaxWidth().height(48.dp)) }
    )
}

@Composable
fun MicRequestDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        title = { Text("🎙️ MICRÓFONO Y TRANSMISIÓN", fontWeight = FontWeight.Black, color = LuxeColors.Gold, fontSize = 16.sp) },
        text = { 
            Text(
                "Para transmitir tu voz en directo por las ondas de radio, tu teléfono Android te solicitará permiso de micrófono.\n\n" +
                "🛡️ PRIVACIDAD TOTAL: ON AIR SPAIN NUNCA graba ni almacena tus conversaciones. El micrófono solo se activa en tiempo real mientras mantengas pulsado el botón HABLAR (PTT).", 
                fontSize = 12.sp, 
                color = Color.White.copy(0.9f),
                lineHeight = 17.sp
            ) 
        },
        confirmButton = { LuxeButton("ACTIVAR TRANSMISIÓN EN VIVO", onAccept, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black) }
    )
}

@Composable
fun BlacklistDialog(blockedUsers: Set<String>, users: List<RemoteUser>, onUnblock: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        title = { Text("USUARIOS BLOQUEADOS", fontWeight = FontWeight.Black, color = Color.White) },
        text = {
            Column {
                if (blockedUsers.isEmpty()) Text("No hay usuarios bloqueados.", color = Color.White.copy(0.5f))
                blockedUsers.forEach { id ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(id, color = Color.White)
                        TextButton({ onUnblock(id) }) { Text("DESBLOQUEAR", color = Color.Red) }
                    }
                }
            }
        },
        confirmButton = { LuxeButton("CERRAR", onDismiss) }
    )
}

@Composable
fun BatteryChecklistDialog(
    statusJsonProvider: () -> String,
    onIgnoreBattery: () -> Unit,
    onOpenNotif: () -> Unit,
    onOpenAutoStart: () -> Unit,
    onDismiss: () -> Unit
) {
    var statusJson by remember { mutableStateOf(statusJsonProvider()) }
    var autoStartClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            statusJson = statusJsonProvider()
        }
    }

    val isIgnoringBattery = statusJson.contains("\"isIgnoringBattery\":true")
    val notifsEnabled = statusJson.contains("\"notifsEnabled\":true")
    val isCustomOs = statusJson.contains("\"isXiaomiOrCustom\":true")
    val isAutoStartOk = autoStartClicked || isIgnoringBattery

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.padding(12.dp).border(2.dp, LuxeColors.Gold.copy(0.6f), RoundedCornerShape(28.dp)),
        icon = { Icon(Icons.Rounded.GppMaybe, null, tint = LuxeColors.Gold, modifier = Modifier.size(44.dp)) },
        title = { Text("DIAGNÓSTICO SEGUNDO PLANO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Para evitar que Android corte la voz al apagar la pantalla, verifica los ajustes del sistema:",
                    color = Color.White.copy(0.8f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // 1. MODO BATERÍA
                ChecklistItem(
                    title = "1. Batería Sin Restricciones",
                    description = if (isIgnoringBattery) "✓ Correcto: La radio no se cortará en reposo." else "Pendiente: Pulsa para seleccionar 'Sin Restricciones'.",
                    isOk = isIgnoringBattery,
                    actionText = if (isIgnoringBattery) "✓ OPTIMIZADO" else "AJUSTAR",
                    onAction = onIgnoreBattery
                )

                Spacer(Modifier.height(10.dp))

                // 2. NOTIFICACIONES
                ChecklistItem(
                    title = "2. Notificaciones y Alertas",
                    description = if (notifsEnabled) "✓ Activas: Avisos y servicio de voz en segundo plano." else "Pendiente: Activa el permiso de avisos.",
                    isOk = notifsEnabled,
                    actionText = if (notifsEnabled) "✓ ACTIVAS" else "PERMITIR",
                    onAction = onOpenNotif
                )

                if (isCustomOs) {
                    Spacer(Modifier.height(10.dp))

                    // 3. INICIO AUTOMÁTICO (Xiaomi/Poco/Samsung/Huawei)
                    ChecklistItem(
                        title = "3. Inicio Automático / Autorun",
                        description = if (isAutoStartOk) "✓ Activado o verificado para HyperOS/MIUI/Samsung." else "Recomendado para Xiaomi/HyperOS para evitar cierres.",
                        isOk = isAutoStartOk,
                        actionText = if (isAutoStartOk) "✓ VERIFICADO" else "AUTORUN",
                        onAction = {
                            autoStartClicked = true
                            onOpenAutoStart()
                        }
                    )
                }
            }
        },
        confirmButton = {
            LuxeButton(
                text = if (isIgnoringBattery && notifsEnabled) "✓ ENTRAR A LA RADIO" else "CONFIGURAR AJUSTES",
                onClick = { 
                    if (!isIgnoringBattery) {
                        onIgnoreBattery()
                    } else if (!notifsEnabled) {
                        onOpenNotif()
                    } else {
                        onDismiss()
                    }
                },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                containerColor = LuxeColors.Gold,
                contentColor = Color.Black,
                fontSize = 13.sp
            )
        },
        dismissButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text("CERRAR AVISO", color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    )
}

@Composable
private fun ChecklistItem(
    title: String,
    description: String,
    isOk: Boolean,
    actionText: String,
    onAction: () -> Unit
) {
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)

    Surface(
        color = if (isOk) greenColor.copy(0.12f) else redColor.copy(0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isOk) greenColor.copy(0.5f) else redColor.copy(0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (isOk) greenColor else redColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                Text(description, color = Color.White.copy(0.7f), fontSize = 10.sp, lineHeight = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            if (!isOk) {
                Surface(
                    onClick = onAction,
                    color = LuxeColors.Gold,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        actionText,
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            } else {
                Surface(
                    color = greenColor.copy(0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, greenColor.copy(0.5f))
                ) {
                    Text(
                        "✓ OK",
                        color = greenColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppBackground(
    style: BackgroundStyle,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundBrush = when (style) {
        BackgroundStyle.DEEP_SEA -> LuxeColors.BackgroundGradient
        BackgroundStyle.LUXE_GOLD -> Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0F172A), Color(0xFF020617)))
        BackgroundStyle.MILITARY -> Brush.verticalGradient(listOf(Color(0xFF142E18), Color(0xFF0B1F0E), Color(0xFF051207)))
        BackgroundStyle.NEON_ROSA -> Brush.verticalGradient(listOf(Color(0xFF831843), Color(0xFF500724), Color(0xFF18020C)))
        BackgroundStyle.CIRCUIT -> Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF022C22), Color(0xFF011A14)))
        BackgroundStyle.CARBON -> Brush.verticalGradient(listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B)))
        BackgroundStyle.WALKIE_ABS -> Brush.verticalGradient(listOf(Color(0xFF3F3F46), Color(0xFF27272A), Color(0xFF18181B)))
        BackgroundStyle.STEEL -> Brush.verticalGradient(listOf(Color(0xFF52525B), Color(0xFF3F3F46), Color(0xFF18181B)))
        BackgroundStyle.OLED_BLACK -> Brush.linearGradient(listOf(Color.Black, Color.Black))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    when (style.overlayType) {
                        "CIRCUIT" -> {
                            val strokeWidth = 1.5.dp.toPx()
                            val lineColor = Color(0xFF10B981).copy(0.12f)
                            val dotColor = Color(0xFF34D399).copy(0.25f)
                            val w = size.width
                            val h = size.height
                            
                            for (i in 0..12) {
                                val y = h * (i / 12f)
                                drawLine(lineColor, Offset(0f, y), Offset(w * 0.4f, y), strokeWidth)
                                drawLine(lineColor, Offset(w * 0.4f, y), Offset(w * 0.6f, y + 40f), strokeWidth)
                                drawLine(lineColor, Offset(w * 0.6f, y + 40f), Offset(w, y + 40f), strokeWidth)
                                drawCircle(dotColor, 3.dp.toPx(), Offset(w * 0.4f, y))
                                drawCircle(dotColor, 3.dp.toPx(), Offset(w * 0.6f, y + 40f))
                            }
                        }
                        "CARBON" -> {
                            val color1 = Color.White.copy(0.03f)
                            val color2 = Color.Black.copy(0.20f)
                            val step = 12.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                var y = 0f
                                while (y < size.height) {
                                    drawRect(if ((x / step + y / step).toInt() % 2 == 0) color1 else color2, Offset(x, y), androidx.compose.ui.geometry.Size(step, step))
                                    y += step
                                }
                                x += step
                            }
                        }
                        "STEEL" -> {
                            val lineColor = Color.White.copy(0.02f)
                            var y = 0f
                            while (y < size.height) {
                                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                                y += 4.dp.toPx()
                            }
                        }
                        "ABS_WALKIE" -> {
                            val dotColor = Color.White.copy(0.015f)
                            var y = 0f
                            while (y < size.height) {
                                var x = (y % 16f)
                                while (x < size.width) {
                                    drawCircle(dotColor, 1.dp.toPx(), Offset(x, y))
                                    x += 12.dp.toPx()
                                }
                                y += 8.dp.toPx()
                            }
                        }
                    }
                }
            },
        content = content
    )
}

@Composable
fun LuxeNotificationOverlay(notification: AppNotification?, onDismiss: () -> Unit) {
    AnimatedVisibility(visible = notification != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
        if (notification != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { notification.onAction?.invoke(); onDismiss() },
                color = LuxeColors.DeepSea,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.5f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NotificationsActive, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(notification.title, fontWeight = FontWeight.Black, color = Color.White)
                        Text(notification.message, fontSize = 12.sp, color = Color.White.copy(0.7f))
                    }
                }
            }
        }
    }
}
