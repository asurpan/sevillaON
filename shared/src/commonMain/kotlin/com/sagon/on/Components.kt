package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - COMPONENTES DE HARDWARE SIMULADO
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 * 
 * Gestiona el dibujo del Vúmetro, Botones, Potenciadores y física de aguja.
 * Blindado contra alteraciones en la física y estética de los componentes.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun StarryBackground(activity: Float = 0f, isEcoMode: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "Stars")
    val alpha by if (isEcoMode) remember { mutableStateOf(0.4f) } else infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "StarAlpha"
    )
    val starProgress by if (isEcoMode) remember { mutableStateOf(0f) } else infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "StarMove"
    )
    
    val auroraAlpha by animateFloatAsState(
        targetValue = if (isEcoMode) 0f else (if (activity > 0) 0.6f else 0.15f),
        animationSpec = tween(1000), label = "AuroraAlpha"
    )

    Canvas(Modifier.fillMaxSize()) {
        // --- 🌌 CAPA 1: NEBULOSA/AURORA LUXE (Desactivada en ECO) ---
        if (!isEcoMode) {
            val auroraBrush = Brush.radialGradient(
                colors = listOf(
                    LuxeColors.ElectricBlue.copy(0.1f), // Siempre azul sutil, sin amarillear
                    Color.Transparent
                ),
                center = Offset(size.width * (0.5f + sin(starProgress * 2 * PI.toFloat()) * 0.2f), size.height * 0.3f),
                radius = size.width * 1.5f
            )
            drawRect(brush = auroraBrush, alpha = auroraAlpha)
        }

        // --- ✨ CAPA 2: ESTRELLAS ---
        val center = Offset(size.width / 2, size.height / 2)
        val maxDist = size.width.coerceAtLeast(size.height)
        val stars = if (isEcoMode) 60 else 120 
        for (i in 0 until stars) {
            val random = Random(i.toLong())
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val startDistMult = random.nextFloat()
            
            val currentProgress = if (isEcoMode) startDistMult else (1f - (startDistMult + starProgress).rem(1f))
            val dist = currentProgress * maxDist
            
            val x = center.x + cos(angle) * dist
            val y = center.y + sin(angle) * dist
            
            val radius = (currentProgress * 2.5.dp.toPx()).coerceAtLeast(0.1.dp.toPx())
            val individualAlpha = (currentProgress * alpha * 0.7f).coerceIn(0f, 1f)
            
            drawCircle(
                color = if (i % 10 == 0) Color.White.copy(0.5f) else Color.White, // Eliminamos puntos dorados
                radius = radius,
                center = Offset(x, y),
                alpha = individualAlpha
            )
        }

        // --- 🌊 CAPA 3: ONDAS DE PROPAGACIÓN (Desactivadas en ECO) ---
        if (activity > 0 && !isEcoMode) {
            val waveProgress = (starProgress * 5).rem(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, LuxeColors.ElectricBlue.copy(0.1f * (1f - waveProgress)), Color.Transparent),
                    center = center,
                    radius = maxDist * waveProgress
                ),
                radius = maxDist * waveProgress,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
fun EliteSlider(label: String, value: Float, valueLabel: String? = null, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(valueLabel ?: "${(value * 100).toInt()}%", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Slider(value, onValueChange, colors = SliderDefaults.colors(thumbColor = LuxeColors.Gold, activeTrackColor = LuxeColors.Gold))
    }
}

@Composable
fun EliteSwitch(modifier: Modifier, label: String, active: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        onClick = { onToggle(!active) },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.03f),
        border = BorderStroke(1.dp, if (active) LuxeColors.Gold.copy(0.4f) else Color.White.copy(0.05f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = if (active) Color.White else Color.White.copy(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (active) LuxeColors.Gold else Color.White.copy(0.1f)))
        }
    }
}


@Composable
fun StatusLed(label: String, active: Boolean, isNightMode: Boolean = false) {
    val baseColor = if (label == "TX") LuxeColors.Red else LuxeColors.Green
    val color = if (isNightMode) LuxeColors.NightAmber else baseColor
    val infiniteTransition = rememberInfiniteTransition(label = "LedGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "Alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (active) 1f else 0f)
            .clip(CircleShape)
            .background(Color.White.copy(0.05f))
            .border(1.dp, color.copy(0.2f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            Modifier.size(6.dp).clip(CircleShape).background(color)
                .drawBehind {
                    if (active) {
                        drawCircle(color, radius = size.width * 2.5f, alpha = glowAlpha)
                    }
                }
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun AnalogMeter(value: Float, isTransmitting: Boolean, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    
    // --- 🔋 OPTIMIZACIÓN: CACHÉ DE TEXTOS (EVITA RECALCULAR 60FPS) ---
    val labelStyle = remember { TextStyle(color = Color.White.copy(0.4f), fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
    val signalLayout = remember(textMeasurer) { textMeasurer.measure("SIGNAL", labelStyle) }
    val pwrLayout = remember(textMeasurer) { textMeasurer.measure("PWR", labelStyle) }
    
    val animValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (isTransmitting) Spring.StiffnessHigh else Spring.StiffnessMediumLow 
        ),
        label = "Needle"
    )

    // --- 🧠 MOTOR DE DOPAMINA: TEMBLOR Y BRILLO DE SOBRECARGA ---
    val infiniteTransition = rememberInfiniteTransition(label = "Dopamine")
    val rumble by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse),
        label = "Rumble"
    )
    val overloadGlow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "Overload"
    )

    val isOverloading = value > 0.92f || (isTransmitting && value > 0.88f)
    val shakeOffset = if (isOverloading) 0.5.dp * rumble else 0.dp

    Box(modifier = modifier
        .padding(horizontal = 8.dp)
        .padding(top = 28.dp, bottom = 8.dp)
        .offset(y = shakeOffset) // Efecto físico de potencia
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height * 0.95f) 
            val r = (size.width * 0.48f).coerceAtMost(size.height * 0.85f) 
            
            // Brillo de sobrecarga (Dopamina pura)
            if (isOverloading) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LuxeColors.Red.copy(0.2f * overloadGlow), Color.Transparent),
                        center = c,
                        radius = r * 1.2f
                    ),
                    center = c,
                    radius = r * 1.2f
                )
            }

            drawArc(
                color = Color.White.copy(0.12f),
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = Color.White.copy(0.05f),
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(c.x - r * 0.85f, c.y - r * 0.85f),
                size = Size(r * 1.7f, r * 1.7f),
                style = Stroke(width = 1.dp.toPx())
            )
            
            // --- 💡 ILUMINACIÓN INTERNA (BOMBILLAS VINTAGE) ---
            // Simula dos pequeñas bombillas incandescentes en las esquinas superiores
            val bulbWarmth = Color(0xFFFFCC80) // Ámbar suave vintage
            val bulbRadius = r * 0.8f
            
            // Bombilla Izquierda (Top-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to bulbWarmth.copy(alpha = 0.15f),
                    1.0f to Color.Transparent,
                    center = Offset(c.x - r * 0.6f, c.y - r * 0.8f),
                    radius = bulbRadius
                ),
                center = Offset(c.x - r * 0.6f, c.y - r * 0.8f),
                radius = bulbRadius
            )
            
            // Bombilla Derecha (Top-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to bulbWarmth.copy(alpha = 0.15f),
                    1.0f to Color.Transparent,
                    center = Offset(c.x + r * 0.6f, c.y - r * 0.8f),
                    radius = bulbRadius
                ),
                center = Offset(c.x + r * 0.6f, c.y - r * 0.8f),
                radius = bulbRadius
            )

            // --- 🏷️ USAR LAYOUTS CACHEADOS ---
            drawText(signalLayout, topLeft = Offset(c.x - r * 0.7f, c.y - r * 0.35f))
            drawText(pwrLayout, topLeft = Offset(c.x + r * 0.45f, c.y - r * 0.35f))
            
            // Escala Profesional (S-Meter Realista)
            for (i in 0..24) { 
                // ... (lógica de marcas existente)
                val angle = 180f + i * 7.5f
                val rad = angle * (PI / 180f).toFloat()
                val isMajor = i % 4 == 0
                val isMedium = i % 2 == 0 && !isMajor
                val markLen = if (isMajor) 18.dp.toPx() else if (isMedium) 12.dp.toPx() else 6.dp.toPx()
                val color = if (i >= 16) LuxeColors.Red else Color(0xFF22C55E)
                
                drawLine(
                    color = if (isMajor || isMedium) color else color.copy(0.3f),
                    start = Offset(c.x + (r - markLen) * cos(rad), c.y + (r - markLen) * sin(rad)),
                    end = Offset(c.x + r * cos(rad), c.y + r * sin(rad)),
                    strokeWidth = (if (isMajor) 3.5.dp else if (isMedium) 2.dp else 1.dp).toPx(),
                    cap = StrokeCap.Round
                )

                if (isMajor) {
                    val label = when(i) {
                        0 -> "1"
                        4 -> "3"
                        8 -> "5"
                        12 -> "7"
                        16 -> "9"
                        20 -> "+30"
                        24 -> "+60"
                        else -> ""
                    }
                    if (label.isNotEmpty()) {
                        val labelPos = Offset(c.x + (r + 10.dp.toPx()) * cos(rad), c.y + (r + 10.dp.toPx()) * sin(rad))
                        // OPTIMIZACIÓN: Aquí se podría cachear también si las etiquetas fueran dinámicas, 
                        // pero al ser estáticas, el impacto es menor que SIGNAL/PWR.
                        val textLayout = textMeasurer.measure(label, TextStyle(color = color.copy(alpha = 0.85f), fontSize = 7.sp, fontWeight = FontWeight.Black))
                        drawText(textLayout, topLeft = Offset(labelPos.x - textLayout.size.width / 2, labelPos.y - textLayout.size.height / 2))
                    }
                }
            }
            
            // Aguja de Fibra (Gama Alta - Más Gorda y Visible)
            val nRad = (180f + animValue * 180f) * (PI / 180f).toFloat()
            val nColor = if (isTransmitting) LuxeColors.Red else LuxeColors.Gold
            
            // Sombra de la aguja (Reforzada)
            drawLine(
                color = Color.Black.copy(0.4f),
                start = c.copy(y = c.y + 2.dp.toPx()),
                end = Offset(c.x + (r - 4.dp.toPx()) * cos(nRad), c.y + (r - 4.dp.toPx()) * sin(nRad) + 2.dp.toPx()),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Aguja principal (Grosor Profesional)
            drawLine(
                color = nColor,
                start = c,
                end = Offset(c.x + r * cos(nRad), c.y + r * sin(nRad)),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Hub central (Tornillo de lujo - Reforzado)
            drawCircle(nColor, radius = 7.dp.toPx(), center = c)
            drawCircle(LuxeColors.Slate900, radius = 3.dp.toPx(), center = c)

            // --- ⚡ INDICADOR DIGITAL DE POTENCIA (RF WATTS) ---
            if (isTransmitting) {
                val watts = (animValue * 100).toInt()
                val textStyle = TextStyle(
                    color = nColor.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                val textLayoutResult = textMeasurer.measure(
                    text = "${watts}W RF",
                    style = textStyle
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(c.x - textLayoutResult.size.width / 2, c.y - r * 0.45f) // Posicionado en el hueco superior
                )
            }
            
            // Brillo de cristal (Efecto Glass final)
            drawArc(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(0.1f), Color.Transparent)
                ),
                startAngle = 190f, sweepAngle = 160f, useCenter = false,
                topLeft = Offset(c.x - r * 0.95f, c.y - r * 0.95f),
                size = Size(r * 1.9f, r * 1.9f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ControlKnob(
    label: String, 
    active: Boolean, 
    icon: ImageVector, 
    isBlinking: Boolean = false,
    onToggle: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val scale by animateFloatAsState(if (active) 1.1f else 1f, label = "Scale")
    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "GlowAlpha"
    )

    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (isBlinking) 400 else 1000), RepeatMode.Reverse), label = "BlinkAlpha"
    )

    // Efecto de pulso de descubrimiento (Círculo expansivo detrás del botón)
    val discoveryScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "DiscoveryPulse"
    )

    val currentOnToggle by rememberUpdatedState(onToggle)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // --- 🛡️ PULSO DE DESCUBRIMIENTO (Solo si no ha sido usado nunca) ---
            if (active && isBlinking) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(discoveryScale)
                        .border(1.dp, (if (label == "RADIO FM") LuxeColors.ElectricBlue else LuxeColors.Gold).copy(alpha = 1f - discoveryScale + 1f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (active || isBlinking) (if (isBlinking) LuxeColors.ElectricBlue.copy(0.1f) else LuxeColors.Gold.copy(0.08f))
                        else Color.White.copy(0.03f)
                    )
                .combinedClickable(
                    onClick = { onToggle() },
                    onLongClick = { onLongClick() }
                )
                .border(
                    BorderStroke(
                        1.5.dp, 
                        if (active || isBlinking) {
                            if (isBlinking) LuxeColors.ElectricBlue.copy(alpha = blinkAlpha)
                            else LuxeColors.Gold.copy(alpha = glowAlpha)
                        } else Color.White.copy(0.1f)
                    ), 
                    CircleShape
                )
                .drawBehind {
                    if (active) {
                        drawCircle(
                            LuxeColors.Gold.copy(alpha = 0.15f * glowAlpha),
                            radius = size.width / 2 * 1.3f
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) LuxeColors.Gold else Color.White.copy(0.3f),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(alpha = if(active) 1f else 0.6f)
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        label, 
        color = if (active) LuxeColors.Gold.copy(0.8f) else Color.White.copy(0.6f),
        fontSize = 8.sp, 
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
}
}

@Composable
fun ChannelCard(
    name: String, 
    userCount: Int, 
    isActive: Boolean, 
    isFavorite: Boolean = false,
    isPrivate: Boolean = false,
    isGeneral: Boolean = false, // Nueva propiedad para identificar la sala principal
    onFavoriteClick: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(130.dp) 
            .height(155.dp) 
            .clip(RoundedCornerShape(8.dp)) 
            .background(
                if (isActive) LuxeColors.Gold.copy(0.08f) 
                else LuxeColors.GlassWhite
            )
            .border(
                1.dp, 
                if (isActive) LuxeColors.Gold.copy(0.4f) else LuxeColors.GlassBorder, 
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isPrivate) {
                Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold, modifier = Modifier.size(12.dp))
            }
            Icon(
                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                null,
                tint = if (isFavorite) LuxeColors.Gold else Color.White.copy(0.1f),
                modifier = Modifier.size(16.dp).clickable { onFavoriteClick() }
            )
        }

        // Icono de borrar (Solo si no es la sala principal de la ciudad)
        if (onDelete != null && !isGeneral) {
            Icon(
                Icons.Rounded.Delete,
                null,
                tint = LuxeColors.Red.copy(0.3f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 14.dp), 
            verticalArrangement = Arrangement.Center
        ) {
            // Icono decorativo: Ciudad para General, Pin para barrios
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(if (isActive) LuxeColors.Gold.copy(0.1f) else Color.White.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if(isGeneral) Icons.Rounded.LocationCity else Icons.Rounded.FmdGood,
                    null, 
                    tint = if (isActive) LuxeColors.Gold else Color.White.copy(0.2f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.height(12.dp))

            Text(
                if (isGeneral) "SALA $name" else name, 
                color = if (isActive) LuxeColors.Gold else Color.White, 
                fontSize = 12.sp,
                fontWeight = FontWeight.Black, 
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.basicMarquee()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(6.dp).clip(CircleShape).background(if (userCount > 0) LuxeColors.Green else Color.Gray) // Antes 4.dp
                        .drawBehind { if(userCount > 0) drawCircle(LuxeColors.Green, radius = size.width * 2f, alpha = 0.2f) }
                )
                Spacer(Modifier.width(8.dp))
                Text("$userCount EST.", color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold) // Antes 7.sp
            }
        }
    }
}

@Composable
fun UserCard(
    user: RemoteUser, 
    isMe: Boolean = false, 
    onFriendToggle: () -> Unit = {}, 
    onPrivateChat: () -> Unit = {},
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val displayNick = user.nick.ifBlank { "ANÓNIMO" }
    Column(
        modifier = Modifier
            .width(130.dp) // Antes 110.dp
            .height(155.dp) // Antes 135.dp
            .clip(RoundedCornerShape(8.dp)) // Antes 24.dp
            .background(LuxeColors.LiquidGlass)
            .clickable { onClick() }
            .border(
                1.dp, 
                when {
                    user.isTransmitting -> LuxeColors.Red.copy(0.5f)
                    isMe -> LuxeColors.Gold.copy(0.3f)
                    else -> LuxeColors.GlassBorder
                }, 
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp), // Antes 8.dp
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape) // Antes 42.dp
                .background(
                    when {
                        user.isTransmitting -> LuxeColors.Red 
                        isMe -> LuxeColors.Gold.copy(0.1f)
                        else -> Color.White.copy(0.08f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Person, 
                null, 
                tint = if (isMe && !user.isTransmitting) LuxeColors.Gold else Color.White, 
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(Modifier.height(10.dp)) // Antes 8.dp
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isMe) "$displayNick (YO)" else displayNick, 
                color = if (isMe) LuxeColors.Gold else Color.White, 
                fontSize = 11.sp, // Antes 10.sp
                fontWeight = FontWeight.Bold, 
                maxLines = 1
            )
            if (user.isFriend && !isMe) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Rounded.Favorite, null, tint = LuxeColors.Gold, modifier = Modifier.size(12.dp)) // Antes 10.dp
            }
        }

        // --- 🏷️ CARTEL PROFESIONAL (BADGE) ---
        if (user.proRole != "CIUDADANO" && (user.isWorkAvailable || user.isSOS)) {
            val role = getRoleById(user.proRole)
            Surface(
                color = if (user.isSOS) LuxeColors.Red.copy(0.2f) else LuxeColors.ElectricBlue.copy(0.1f),
                shape = RoundedCornerShape(6.dp), // Antes 4.dp
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), // Antes 4, 2
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(role.icon, null, tint = if (user.isSOS) LuxeColors.Red else LuxeColors.ElectricBlue, modifier = Modifier.size(10.dp)) // Antes 8.dp
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (user.isSOS) "S.O.S." else role.name, 
                        color = if (user.isSOS) LuxeColors.Red else LuxeColors.ElectricBlue, 
                        fontSize = 8.sp, // Antes 7.sp
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        Spacer(Modifier.height(10.dp)) // Antes 8.dp
        
        if (!isMe) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(16.dp) 
            ) {
                // Favorito
                Icon(
                    if (user.isFriend) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    null,
                    tint = if (user.isFriend) LuxeColors.Gold else Color.White.copy(0.6f),
                    modifier = Modifier.size(24.dp).clickable { onFriendToggle() }
                )
                // Chat
                Icon(
                    Icons.AutoMirrored.Rounded.Chat,
                    null,
                    tint = LuxeColors.Gold.copy(0.6f),
                    modifier = Modifier.size(24.dp).clickable { onPrivateChat() }
                )
                // Reportar/Banear (Votación)
                Icon(
                    Icons.Rounded.GppBad,
                    null,
                    tint = LuxeColors.Red.copy(0.6f),
                    modifier = Modifier.size(24.dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onReport() },
                            onLongPress = { onBlock() }
                        )
                    }
                )
            }
        } else {
            Text("TU ESTACIÓN", color = LuxeColors.Gold.copy(0.4f), fontSize = 7.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun LuxeSlider(label: String, value: Float, color: Color, enabled: Boolean = true, onValue: (Float) -> Unit) {
    val animatedValue by animateFloatAsState(value)
    
    Column(
        Modifier
            .padding(vertical = 4.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .drawBehind {
                // Brillo de fondo dinámico: De menos (izquierda) a más (derecha/thumb)
                // Efecto "aura de potencia" que crece con el valor
                if (animatedValue > 0.01f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent, 
                                color.copy(alpha = 0.25f * animatedValue)
                            ),
                            startX = 0f,
                            endX = (size.width * animatedValue).coerceAtLeast(1f)
                        )
                    )
                }
            }
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("${(value * 100).toInt()}%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value, onValueChange = onValue,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Color.White, 
                activeTrackColor = color, 
                inactiveTrackColor = color.copy(alpha = 0.1f)
            ),
            modifier = Modifier.height(20.dp)
        )
        // Escalita de precisión analógica (0..10)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp), 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(11) { i ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(1.dp, 3.dp).background(if ((value * 10).toInt() >= i) color.copy(0.6f) else Color.White.copy(0.08f)))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if(i % 2 == 0) "$i" else "", 
                        color = if ((value * 10).toInt() >= i) color.copy(0.5f) else Color.White.copy(0.1f),
                        fontSize = 6.sp, 
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LuxeButton(
    text: String, 
    onClick: () -> Unit, 
    enabled: Boolean, 
    modifier: Modifier, 
    containerColor: Color, 
    contentColor: Color,
    icon: ImageVector? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "Scale")
    
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier
            .scale(scale)
            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor, 
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(0.2f)
        ),
        interactionSource = interaction,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text, 
                fontWeight = FontWeight.Black, 
                fontSize = fontSize, 
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 6
    
    // --- 🧠 EFECTO NEURONAL: Sonido al cambiar de paso ---
    LaunchedEffect(step) {
        if (step > 1) triggerUiSound("click")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        textContentColor = Color.White,
        modifier = Modifier
            .padding(16.dp)
            .border(
                BorderStroke(
                    1.dp, 
                    Brush.linearGradient(listOf(LuxeColors.Gold, LuxeColors.ElectricBlue))
                ), 
                RoundedCornerShape(24.dp) // Esquinas más redondeadas y premium
            ),
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "NeuralIcon")
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "Scale"
                )
                
                Box(
                    Modifier
                        .size(44.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(LuxeColors.Gold.copy(0.15f)), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.Gold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("ON AIR SPAIN", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("PROTOCOLO DE BIENVENIDA", fontSize = 8.sp, color = LuxeColors.Gold, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Crossfade(targetState = step) { currentStep ->
                    Column(Modifier.height(160.dp)) {
                        when(currentStep) {
                            1 -> {
                                Text("🎙️ LA RED DE VOZ MÁS PURA", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 15.sp)
                                Text("Recupera el placer de la radioafición real. Sin fotos, sin filtros, solo tu voz modulando en directo. Siente la dopamina de conectar con otros en una red viva y auténtica.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                            2 -> {
                                Text("🌍 CONEXIÓN INTERNACIONAL", fontWeight = FontWeight.Bold, color = LuxeColors.ElectricBlue, fontSize = 15.sp)
                                Text("Hemos habilitado túneles TURN para saltar firewalls en Sudamérica y el resto del mundo. Sintoniza 'MUNDO (INTERNACIONAL)' y usa el canal 'INTERNACIONAL MUNDO' para hablar con cualquier rincón del planeta.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                            3 -> {
                                Text("🏛️ GUÍA TURÍSTICA DINÁMICA", fontWeight = FontWeight.Bold, color = LuxeColors.Green, fontSize = 15.sp)
                                Text("Nuestro operador virtual utiliza Wikipedia para informarte sobre la historia, monumentos y secretos del lugar donde sintonices. Disfruta de una radio que te enseña mientras viajas.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                            4 -> {
                                Text("💼 RADAR PROFESIONAL Y SOS", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 15.sp)
                                Text("Mucho más que una radio. Ofrece tus servicios o solicita ayuda SOS en tu ciudad. La comunidad ON AIR es una red de apoyo real para profesionales y ciudadanos.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                            5 -> {
                                Text("🗺️ NAVEGACIÓN TÁCTICA", fontWeight = FontWeight.Bold, color = LuxeColors.ElectricBlue, fontSize = 15.sp)
                                Text("Localiza a tus compañeros en el radar. Pulsa sobre el icono de cualquier usuario en el mapa para iniciar automáticamente una ruta GPS hacia su posición exacta.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                            6 -> {
                                Text("🎧 PTT DE HARDWARE Y CONTROL", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 15.sp)
                                Text("Control total sin mirar la pantalla. Usa el mando de tu coche o el botón de tus auriculares para hablar. RECUERDA: Ahora puedes usar los botones de volumen de tu móvil con normalidad.", fontSize = 13.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(totalSteps) { i ->
                        val isActive = step == i + 1
                        val color = if (isActive) LuxeColors.Gold else Color.White.copy(0.1f)
                        val widthAnim by animateDpAsState(if (isActive) 24.dp else 8.dp)
                        
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(widthAnim, 4.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton(
                text = if (step < totalSteps) "SIGUIENTE PASO" else "¡INICIAR SECUENCIA!",
                onClick = { 
                    if (step < totalSteps) {
                        step++
                    } else {
                        triggerUiSound("switch")
                        onDismiss()
                    }
                },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                containerColor = LuxeColors.Gold,
                contentColor = Color.Black
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
        titleContentColor = iconColor,
        textContentColor = Color.White,
        modifier = Modifier.padding(16.dp).border(1.dp, iconColor.copy(0.3f), RoundedCornerShape(12.dp)),
        icon = { 
            Box(Modifier.size(48.dp).background(iconColor.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor)
            }
        },
        title = { Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp) },
        text = {
            Text(description, fontSize = 13.sp, textAlign = TextAlign.Center, color = Color.White)
        },
        confirmButton = {
            LuxeButton(
                text = "CONTINUAR",
                onClick = onDismiss,
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                containerColor = iconColor,
                contentColor = Color.Black
            )
        }
    )
}

@Composable
fun PrivacyConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        textContentColor = Color.White,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
        title = { Text("AVISO LEGAL Y PRIVACIDAD", fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    "Esta aplicación es una herramienta de comunicación VOIP desarrollada por un particular. Al usarla, aceptas:",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "• RGPD: Tus datos se procesan solo para la conexión técnica. No se ceden a terceros.\n• Publicidad y Cookies: Utilizamos identificadores de Google para mostrar anuncios y mantener el servicio gratuito.\n• GPS: Se utiliza para sintonizar tu ciudad y en alertas SOS voluntarias.\n• Información Real: Conectamos con APIs públicas (DGT/NASA) para ofrecer noticias en directo.\n• Responsabilidad: El desarrollador no se hace responsable del uso de la red por particulares.\n• Micrófono: Solo se utiliza bajo demanda (PTT/VOX) para comunicarte con el grupo.\n• Derecho al Olvido: Puedes borrar tus datos en cualquier momento desde Ajustes > Olvido.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(0.9f)
                )
            }
        },
        confirmButton = {
            LuxeButton(
                text = "ACEPTAR PERMISOS",
                onClick = onAccept,
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                containerColor = LuxeColors.Gold,
                contentColor = Color.Black
            )
        }
    )
}

@Composable
fun NotificationConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        textContentColor = Color.White,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
        title = { Text("NOTIFICACIONES DE RADIO", fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    "Para que no te pierdas ni un solo mensaje de la red mientras la radio está en segundo plano, necesitamos activar las notificaciones.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "• MENSAJES: Recibe alertas de nuevos mensajes en el chat.\n• ALERTAS SOS: Te avisaremos si hay una emergencia en tu ciudad.\n• ESTADO: Icono en la barra superior para saber que la radio está encendida.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(0.9f)
                )
            }
        },
        confirmButton = {
            LuxeButton(
                text = "ACTIVAR NOTIFICACIONES",
                onClick = onAccept,
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                containerColor = LuxeColors.Gold,
                contentColor = Color.Black
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("MÁS TARDE", color = Color.White.copy(0.4f))
            }
        }
    )
}

@Composable
fun MicRequestDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A), // Fondo Slate muy profundo
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.Green.copy(0.3f), RoundedCornerShape(12.dp)),
        icon = { 
            Box(
                modifier = Modifier.size(64.dp).background(LuxeColors.Green.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Mic, null, tint = LuxeColors.Green, modifier = Modifier.size(32.dp)) 
            }
        },
        title = { 
            Text(
                "TRANSMISIÓN DE VOZ", 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp,
                color = Color.White
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Para que puedas comunicarte con los demás usuarios mientras usas la aplicación, el sistema necesita acceso al micrófono.",
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(0.9f)
                )
                Spacer(Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.RecordVoiceOver, null, tint = LuxeColors.Green, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("AUDIO EN DIRECTO: Nada se graba ni se guarda.", fontSize = 11.sp, color = Color.White.copy(0.8f))
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Green, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("PRIVACIDAD: El micro solo se abre al pulsar PTT.", fontSize = 11.sp, color = Color.White.copy(0.8f))
                }
            }
        },
        confirmButton = {
            LuxeButton(
                text = "ACTIVAR MICRÓFONO",
                onClick = onAccept,
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                containerColor = LuxeColors.Green,
                contentColor = Color.Black
            )
        }
    )
}

@Composable
fun BlacklistDialog(blockedUsers: Set<String>, allUsers: List<RemoteUser>, onUnblock: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Block, null, tint = LuxeColors.Red)
                Spacer(Modifier.width(16.dp))
                Text("LISTA NEGRA", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                Text("Usuarios que has bloqueado. No pueden hablarte ni los verás en la radio.", fontSize = 12.sp, color = Color.White.copy(0.8f))
                Spacer(Modifier.height(16.dp))
                if (blockedUsers.isEmpty()) {
                    Text("NO HAY USUARIOS BLOQUEADOS", color = LuxeColors.Gold.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(blockedUsers.toList()) { userId ->
                            val userNick = allUsers.find { it.id == userId }?.nick ?: "Usuario ID: ${userId.take(6)}"
                            Surface(
                                color = Color.White.copy(0.05f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(userNick, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { onUnblock(userId) }) {
                                        Text("RECUPERAR", color = LuxeColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CERRAR", color = Color.White.copy(0.4f)) }
        }
    )
}

@Composable
fun FmDial(
    frequency: Float, 
    modifier: Modifier = Modifier, 
    isNightMode: Boolean = false,
    onShareClick: (() -> Unit)? = null
) {
    val animFreq by animateFloatAsState(
        targetValue = frequency.coerceIn(87.5f, 108.0f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    val dialColor = if (isNightMode) LuxeColors.NightAmber else Color.White
    val needleColor = if (isNightMode) LuxeColors.NightAmber else LuxeColors.Red

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(25.dp)) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val startFreq = 87.5f
            val endFreq = 108.0f
            val range = endFreq - startFreq
            val needleX = ((animFreq - startFreq) / range) * width

            Canvas(modifier = Modifier.fillMaxSize()) {
                // --- 📏 ESCALA DE SINTONÍA ---
                for (i in 88..108) {
                    val x = ((i - startFreq) / range) * size.width
                    val isMajor = i % 2 == 0
                    drawLine(
                        color = dialColor.copy(if (isMajor) 0.5f else 0.2f),
                        start = Offset(x, 0f),
                        end = Offset(x, if (isMajor) 10.dp.toPx() else 5.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // --- 📍 AGUJA DE SEÑAL (Gama Alta) ---
                drawLine(
                    color = needleColor,
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, size.height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Brillo de sintonía activa
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(needleColor.copy(0.3f), Color.Transparent),
                        center = Offset(needleX, size.height / 2),
                        radius = 15.dp.toPx()
                    ),
                    center = Offset(needleX, size.height / 2),
                    radius = 15.dp.toPx()
                )
            }

            // --- 🚀 ICONO DE COMPARTIR FLOTANTE (DOPAMINA) ---
            if (onShareClick != null) {
                val density = androidx.compose.ui.platform.LocalDensity.current
                val needleXdp = with(density) { needleX.toDp() }
                
                val infiniteTransition = rememberInfiniteTransition(label = "ShareGlow")
                val floatAnim by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = -4f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Float"
                )
                val alphaAnim by infiniteTransition.animateFloat(
                    initialValue = 0.6f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Alpha"
                )

                Box(
                    modifier = Modifier
                        .offset(x = needleXdp - 14.dp, y = floatAnim.dp - 10.dp)
                        .size(28.dp)
                        .graphicsLayer(alpha = alphaAnim)
                        .clip(CircleShape)
                        .background(LuxeColors.ElectricBlue.copy(0.2f))
                        .border(1.dp, LuxeColors.ElectricBlue.copy(0.5f), CircleShape)
                        .clickable { onShareClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Share, 
                        null, 
                        tint = LuxeColors.ElectricBlue, 
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("88", color = dialColor.copy(0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
            
            // Display Digital del Dial
            Surface(
                color = Color.Black.copy(0.4f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, (if (isNightMode) LuxeColors.NightAmber else LuxeColors.Gold).copy(0.2f))
            ) {
                Text(
                    text = "${(animFreq * 10).toInt() / 10f} MHz", 
                    color = if (isNightMode) LuxeColors.NightAmber else LuxeColors.Gold, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    letterSpacing = 1.sp
                )
            }
            
            Text("108", color = dialColor.copy(0.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PremiumChatMessage(
    msg: ChatMessage,
    isMe: Boolean,
    isPro: Boolean,
    isHighPower: Boolean,
    onDelete: () -> Unit
) {
    val bubbleColor = if (isMe) LuxeColors.Gold.copy(0.12f) else Color.White.copy(0.06f)
    val borderColor = if (isMe) LuxeColors.Gold.copy(0.3f) else Color.White.copy(0.1f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "MessageGlow")
    val glowIntensity by if (isHighPower) infiniteTransition.animateFloat(
        0.3f, 0.7f, infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    ) else remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if(isPro) LuxeColors.ElectricBlue.copy(0.2f) else Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if(isPro) Icons.Rounded.Verified else Icons.Rounded.Person,
                    null,
                    tint = if(isPro) LuxeColors.ElectricBlue else Color.White.copy(0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = BorderStroke(1.dp, if (isHighPower) borderColor.copy(alpha = glowIntensity) else borderColor),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .then(if (isHighPower) Modifier.graphicsLayer {
                    shadowElevation = 8f * glowIntensity
                    spotShadowColor = LuxeColors.Gold
                } else Modifier)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = msg.senderNick,
                        color = if (isMe) LuxeColors.Gold else (if (isHighPower) LuxeColors.Gold else Color.White.copy(0.6f)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "18:45", // Placeholder for actual time if added to ChatMessage
                        color = Color.White.copy(0.2f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = msg.text,
                    color = Color.White.copy(0.95f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp).alpha(0.4f)
            ) {
                Icon(Icons.Rounded.Delete, null, tint = LuxeColors.Red, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun LuxeNotificationOverlay(notification: AppNotification?, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        if (notification != null) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    color = LuxeColors.DeepSea.copy(0.95f),
                    border = BorderStroke(1.dp, LuxeColors.GlassBorder)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when(notification.type) {
                                NotificationType.Info -> Icons.Rounded.Info
                                NotificationType.Warning -> Icons.Rounded.Warning
                                NotificationType.Success -> Icons.Rounded.CheckCircle
                            },
                            contentDescription = null,
                            tint = LuxeColors.Gold
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(notification.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text(notification.message, color = Color.White.copy(0.85f), fontSize = 11.sp)
                        }
                        if (notification.actionLabel != null && notification.onAction != null) {
                            Spacer(Modifier.width(12.dp))
                            TextButton(
                                onClick = { notification.onAction.invoke() },
                                colors = ButtonDefaults.textButtonColors(contentColor = LuxeColors.Gold)
                            ) {
                                Text(notification.actionLabel.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                        // --- ❌ BOTÓN DE CERRAR SIEMPRE DISPONIBLE ---
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            LaunchedEffect(notification) {
                // Tiempo de lectura inteligente: 6s base, más si el mensaje es largo
                val baseTime = if (notification.actionLabel != null) 8000L else 6000L
                val extraTime = (notification.message.length * 20L) // Un poco más por cada letra
                delay(baseTime + extraTime)
                onDismiss()
            }
        }
    }
}
