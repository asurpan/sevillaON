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
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "Pulse"
    )

    Surface(
        modifier = Modifier.width(90.dp).height(120.dp).clickable { if(!isMe) onAvatarClick() },
        color = if (user.isTransmitting) Color.Red.copy(0.1f) else Color.White.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (user.isTransmitting) Color.Red.copy(0.4f) else Color.White.copy(0.1f))
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) {
                if (user.isTransmitting) {
                    Box(Modifier.size(44.dp).scale(pulseScale).background(Color.Red.copy(0.2f), CircleShape))
                }
                Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.Black) {
                    Icon(Icons.Rounded.Person, null, tint = if (isMe) LuxeColors.Gold else Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(user.nick, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
            if (isMe) Text("(YO)", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            else Text("${(user.txPower * 10).toInt()}W", color = Color.White.copy(0.4f), fontSize = 9.sp)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(text, fontWeight = FontWeight.Black, fontSize = fontSize, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 4

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.Gold, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text("BIENVENIDO A ON AIR", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().height(180.dp)) {
                Crossfade(targetState = step) { currentStep ->
                    Column {
                        when(currentStep) {
                            1 -> {
                                Text("📻 RADIO CB NACIONAL", fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                                Text("Estás ante una emisora de Banda Ciudadana real. Sintoniza tu ciudad y habla en directo con otros operadores de toda España.", fontSize = 13.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(top = 8.dp))
                            }
                            2 -> {
                                Text("🌀 QRM Y SEÑALES REALES", fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                                Text("Siente el 'viento' de la radio con el QRM vivo. Los LEDs marcan la portadora real y la potencia de tus compañeros.", fontSize = 13.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(top = 8.dp))
                            }
                            3 -> {
                                Text("⚖️ BALANCEO ANTISATURACIÓN", fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                                Text("Si una sala se llena, el sistema crea sub-canales (-2, -3) automáticamente. Podrás ver y saltar entre ellos desde la lista inferior.", fontSize = 13.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(top = 8.dp))
                            }
                            4 -> {
                                Text("🔋 OPTIMIZACIÓN MÓVIL", fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                                Text("Para un manos libres perfecto en coche o moto, desactiva las restricciones de batería del móvil. ¡Disfruta de la ruta!", fontSize = 13.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton(
                text = if (step < totalSteps) "SIGUIENTE" else "¡A LA ESCUCHA!",
                onClick = { if (step < totalSteps) step++ else onDismiss() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
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
        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(24.dp)),
        title = { Text("PRIVACIDAD", fontWeight = FontWeight.Black, color = Color.White) },
        text = { Text("Utilizamos tu ubicación aproximada para conectarte a la frecuencia local más cercana.", fontSize = 13.sp, color = Color.White.copy(0.7f)) },
        confirmButton = { LuxeButton("ACEPTAR", onAccept, true, Modifier.fillMaxWidth().height(48.dp)) }
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
