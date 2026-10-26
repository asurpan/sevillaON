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
        title = { Text("MICRÓFONO", fontWeight = FontWeight.Black, color = Color.White) },
        text = { Text("Necesitamos acceso al micrófono para que puedas transmitir tu voz.", fontSize = 13.sp, color = Color.White.copy(0.7f)) },
        confirmButton = { LuxeButton("PERMITIR", onAccept, true, Modifier.fillMaxWidth().height(48.dp)) }
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
    statusJson: String,
    onIgnoreBattery: () -> Unit,
    onOpenNotif: () -> Unit,
    onOpenAutoStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val isIgnoringBattery = statusJson.contains("\"isIgnoringBattery\":true")
    val notifsEnabled = statusJson.contains("\"notifsEnabled\":true")
    val isCustomOs = statusJson.contains("\"isXiaomiOrCustom\":true")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.padding(12.dp).border(2.dp, LuxeColors.Gold.copy(0.6f), RoundedCornerShape(28.dp)),
        icon = { Icon(Icons.Rounded.GppMaybe, null, tint = LuxeColors.Gold, modifier = Modifier.size(44.dp)) },
        title = { Text("DIAGNÓSTICO SEGUNDO PLANO", fontWeight = FontWeight.Black, fontSize = 17.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Para evitar que Android corte la voz al apagar la pantalla, verifica tu lista de configuración:",
                    color = Color.White.copy(0.8f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. MODO BATERÍA
                ChecklistItem(
                    title = "1. Batería Sin Restricciones",
                    description = if (isIgnoringBattery) "Configuración correcta. Canal de voz protegido." else "Pendiente: Cambia a 'Sin Restricciones'.",
                    isOk = isIgnoringBattery,
                    actionText = if (isIgnoringBattery) "✓ CORRECTO" else "AJUSTAR BATERÍA",
                    onAction = onIgnoreBattery
                )

                Spacer(Modifier.height(10.dp))

                // 2. NOTIFICACIONES
                ChecklistItem(
                    title = "2. Notificaciones y Alertas",
                    description = if (notifsEnabled) "Alertas y servicio de red activos." else "Pendiente: Permitir notificaciones.",
                    isOk = notifsEnabled,
                    actionText = if (notifsEnabled) "✓ ACTIVAS" else "PERMITIR AVISOS",
                    onAction = onOpenNotif
                )

                if (isCustomOs) {
                    Spacer(Modifier.height(10.dp))

                    // 3. INICIO AUTOMÁTICO (Xiaomi/Poco/Samsung/Huawei)
                    ChecklistItem(
                        title = "3. Inicio Automático / Autorun",
                        description = "Recomendado para MIUI/HyperOS/Samsung para evitar el cierre por reposo.",
                        isOk = false,
                        actionText = "ABRIR INICIO AUTOMÁTICO",
                        onAction = onOpenAutoStart
                    )
                }
            }
        },
        confirmButton = {
            LuxeButton("CONFIGURAR TODO EN AJUSTES", { onIgnoreBattery() }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
        },
        dismissButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text("ENTENDIDO, LUEGO", color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
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
    Surface(
        color = if (isOk) Color(0xFF10B981).copy(0.1f) else LuxeColors.Gold.copy(0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isOk) Color(0xFF10B981).copy(0.4f) else LuxeColors.Gold.copy(0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isOk) Color(0xFF10B981) else LuxeColors.Gold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text(description, color = Color.White.copy(0.7f), fontSize = 10.sp, lineHeight = 14.sp)
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
                Text("✓ OK", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
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
