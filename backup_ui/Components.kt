package com.sagon.on

/**
 * PROTECTED CORE: COMPONENTES VISUALES LUXE (ESTADO: GAMA ALTA)
 * FUNCIONES BLINDADAS CON ESTÉTICA PREMIUM:
 * - STARRY BACKGROUND (INMERSIVO).
 * - ANALOG METER (EFECTO CRISTAL CÓNCAVO).
 * - STATUS LED (NEÓN).
 * - LUXE SLIDERS & GLOW KNOBS.
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
fun StatusLed(label: String, active: Boolean) {
    val color = if (label == "TX") LuxeColors.Red else LuxeColors.Green
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
    val animValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (isTransmitting) Spring.StiffnessHigh else Spring.StiffnessMediumLow // Suave en recepción, rápido en TX
        ),
        label = "Needle"
    )

    Box(modifier = modifier.padding(horizontal = 8.dp).padding(top = 28.dp, bottom = 8.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height * 0.95f) // Pegado a la base para ganar arco
            val r = (size.width * 0.48f).coerceAtMost(size.height * 0.85f) // Máximo ancho disponible
            
            // Fondo cóncavo (Efecto profundidad)
            drawArc(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(0.05f),
                    1.0f to Color.Transparent,
                    center = c,
                    radius = r
                ),
                startAngle = 180f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2)
            )

            // Arco principal (Neón sutil más ancho)
            drawArc(
                color = Color.White.copy(0.12f),
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Escala Profesional (S-Meter Realista)
            for (i in 0..12) { // De S0 a S9 +30 +60
                val angle = 180f + i * 15f
                val rad = angle * (PI / 180f).toFloat()
                
                // Determinamos si es una marca mayor (S1, S3, S5, S7, S9, +30, +60)
                val isMajor = i % 2 == 0 || i >= 9
                val markLen = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                
                // Color: Verde hasta S9, Rojo en adelante
                val color = if (i >= 9) LuxeColors.Red else Color(0xFF22C55E)
                
                drawLine(
                    color = color,
                    start = Offset(c.x + (r - markLen) * cos(rad), c.y + (r - markLen) * sin(rad)),
                    end = Offset(c.x + r * cos(rad), c.y + r * sin(rad)),
                    strokeWidth = (if (isMajor) 3.5.dp else 1.8.dp).toPx(),
                    cap = StrokeCap.Round
                )

                // Añadir etiquetas de texto profesional (S5, 7, 9, +30)
                if (isMajor && i in listOf(0, 4, 6, 8, 10, 12)) {
                    val label = when(i) {
                        0 -> "1"
                        4 -> "5"
                        6 -> "7"
                        8 -> "9"
                        10 -> "+30"
                        12 -> "+60"
                        else -> ""
                    }
                    val labelPos = Offset(
                        c.x + (r + 10.dp.toPx()) * cos(rad),
                        c.y + (r + 10.dp.toPx()) * sin(rad)
                    )
                    
                    val textStyle = TextStyle(
                        color = color.copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                    val textLayout = textMeasurer.measure(label, textStyle)
                    drawText(
                        textLayout,
                        topLeft = Offset(labelPos.x - textLayout.size.width / 2, labelPos.y - textLayout.size.height / 2)
                    )
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
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
                val textLayoutResult = textMeasurer.measure(
                    text = "${watts}W RF",
                    style = textStyle
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(c.x - textLayoutResult.size.width / 2, c.y - r * 0.5f) // Centrado en el hueco del vúmetro
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
fun ControlKnob(label: String, active: Boolean, icon: ImageVector, onToggle: () -> Unit) {
    val scale by animateFloatAsState(if (active) 1.1f else 1f, label = "Scale")
    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "GlowAlpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    if (active) LuxeColors.Gold.copy(0.08f) 
                    else Color.White.copy(0.03f)
                )
                .border(
                    BorderStroke(
                        1.5.dp, 
                        if (active) LuxeColors.Gold.copy(alpha = glowAlpha) else Color.White.copy(0.1f)
                    ), 
                    CircleShape
                )
                .drawBehind {
                    if (active) {
                        // Aura de neón exterior
                        drawCircle(
                            LuxeColors.Gold.copy(alpha = 0.15f * glowAlpha),
                            radius = size.width / 2 * 1.3f
                        )
                    }
                }
                .clickable { onToggle() },
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
        Spacer(Modifier.height(8.dp))
        Text(
            label, 
            color = if (active) LuxeColors.Gold.copy(0.8f) else Color.White.copy(0.3f),
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
            .clip(RoundedCornerShape(28.dp)) 
            .background(
                if (isActive) LuxeColors.Gold.copy(0.08f) 
                else LuxeColors.GlassWhite
            )
            .border(
                1.dp, 
                if (isActive) LuxeColors.Gold.copy(0.4f) else LuxeColors.GlassBorder, 
                RoundedCornerShape(28.dp)
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
                Text("$userCount EST.", color = Color.White.copy(0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold) // Antes 7.sp
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
    onBlock: () -> Unit = {}
) {
    val displayNick = user.nick.ifBlank { "ANÓNIMO" }
    Column(
        modifier = Modifier
            .width(130.dp) // Antes 110.dp
            .height(155.dp) // Antes 135.dp
            .clip(RoundedCornerShape(28.dp)) // Antes 24.dp
            .background(LuxeColors.LiquidGlass)
            .border(
                1.dp, 
                when {
                    user.isTransmitting -> LuxeColors.Red.copy(0.5f)
                    isMe -> LuxeColors.Gold.copy(0.3f)
                    else -> LuxeColors.GlassBorder
                }, 
                RoundedCornerShape(28.dp)
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
        if (user.proRole != "CIUDADANO") {
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
                        role.name, 
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
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Antes 10.dp
            ) {
                // Favorito
                Icon(
                    if (user.isFriend) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    null,
                    tint = if (user.isFriend) LuxeColors.Gold else Color.White.copy(0.3f),
                    modifier = Modifier.size(18.dp).clickable { onFriendToggle() }
                )
                // Chat
                Icon(
                    Icons.AutoMirrored.Rounded.Chat,
                    null,
                    tint = LuxeColors.Gold.copy(0.6f),
                    modifier = Modifier.size(18.dp).clickable { onPrivateChat() }
                )
                // Reportar/Banear (Votación)
                Icon(
                    Icons.Rounded.GppBad,
                    null,
                    tint = LuxeColors.Red.copy(0.6f),
                    modifier = Modifier.size(18.dp).pointerInput(Unit) {
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
fun LuxeSlider(label: String, value: Float, color: Color, onValue: (Float) -> Unit) {
    val animatedValue by animateFloatAsState(value)
    
    Column(
        Modifier
            .padding(vertical = 4.dp)
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
            Text(label, color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("${(value * 100).toInt()}%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value, onValueChange = onValue,
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
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "Scale")
    
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier
            .scale(scale)
            .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor, 
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(0.2f)
        ),
        interactionSource = interaction,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
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
                fontSize = 13.sp, // Reducimos un poco para evitar saltos de línea
                letterSpacing = 0.5.sp, // Ajustamos espaciado
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 12
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        textContentColor = Color.White,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(LuxeColors.Gold.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.Gold)
                }
                Spacer(Modifier.width(16.dp))
                Text("ON AIR SPAIN", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Crossfade(targetState = step) { currentStep ->
                    Column(Modifier.height(130.dp)) {
                        when(currentStep) {
                            1 -> {
                                Text("🎙️ LA EMISORA ONLINE", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Elige tu ciudad base y conecta con toda España. Tu privacidad es sagrada: solo se muestra actividad por ciudad, nunca tu posición exacta ni GPS.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            2 -> {
                                Text("🎧 PTT DE HARDWARE", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("¡Como un Walkie real! Usa las teclas de VOLUMEN (+/-) de tu móvil, el botón de tus auriculares o el mando del coche para hablar sin tocar la pantalla.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            3 -> {
                                Text("🔄 REBOBINADO (REPLAY)", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("¿No has oído bien lo último? Pulsa el botón de historial para repetir los últimos 30 segundos de radio con ruido atenuado.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            4 -> {
                                Text("🌟 TUS AMIGOS EN ORO", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Marca favoritos con el corazón; la app te avisará si hablan en otro barrio y sus nombres brillarán en ORO.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            5 -> {
                                Text("⚡ POTENCIA Y WATTS (W)", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Tu indicativo gana potencia (Watts) cuanto más usas la radio. Al emitir verás tus vatios reales. Un veterano tiene más 'pegada' y puede pasar por encima de otros en caso de pisotón (colisión).", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            6 -> {
                                Text("🛡️ ANTI-PORTADORA", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("¡No bloquees el canal! Si pulsas PTT sin hablar por mucho tiempo, el sistema te avisará. Si insistes, tu potencia será reseteada por seguridad.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            7 -> {
                                Text("📡 RADAR INTERACTIVO", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Pulsa el Radar para ver el mapa de España. ¡Usa dos dedos para ampliar y navegar! Los puntos dorados indican actividad en tiempo real.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            8 -> {
                                Text("🔋 MODO ECO INTELIGENTE", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("¿Poca batería? Activa el MODO ECO (hoja verde) para ahorrar hasta un 40% de energía durante el día.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            9 -> {
                                Text("🔔 NOTIFICACIONES SEGURAS", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Solo las usamos para avisarte si un amigo favorito entra al aire o si alguien te habla por chat privado mientras la app está en segundo plano. Nunca para publicidad ni spam. ¡Tu radio siempre lista!", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            10 -> {
                                Text("🔒 BLOQUEO DE EQUIPO", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("Evita cambios accidentales: pulsa el candado bajo el radar para bloquear los controles (Canal, Ciudad, Squelch...). El PTT siempre seguirá activo.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            11 -> {
                                Text("💼 EMPLEO Y NETWORKING", fontWeight = FontWeight.Bold, color = LuxeColors.ElectricBlue, fontSize = 14.sp)
                                Text("Pulsa el Maletín para el Módulo Pro. Indica si buscas empleo (BUSCO TRABAJO) o si buscas servicios/profesionales (BUSCO PERSONAL) en tiempo real. ¡Voz directa!", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                            12 -> {
                                Text("⭐ CALIDAD Y SEGURIDAD", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 14.sp)
                                Text("El sistema de estrellas mide la calidad de profesionales y negocios. Si detectas abusos o fraudes, usa el botón de REPORTE. Mantenemos la red limpia de perfiles falsos.", fontSize = 13.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(totalSteps) { i ->
                        Box(
                            Modifier.size(6.dp).clip(CircleShape).background(if (step == i+1) LuxeColors.Gold else Color.White.copy(0.1f))
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton(
                text = if (step < totalSteps) "SIGUIENTE" else "¡A EMITIR!",
                onClick = { if (step < totalSteps) step++ else onDismiss() },
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                containerColor = LuxeColors.Gold,
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
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
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
                    "• RGPD: Tus datos (nick/ciudad/rol) se procesan solo para la conexión técnica y visibilidad en el panel de empleo. No se ceden a terceros.\n• GPS: Solo se utiliza si activas el botón SOS voluntariamente para emergencias.\n• Responsabilidad: El desarrollador no gestiona servicios públicos ni se hace responsable de acuerdos entre particulares.\n• Micrófono: Solo se activa bajo tu control (PTT/VOX).",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(0.7f)
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
fun BlacklistDialog(blockedUsers: Set<String>, allUsers: List<RemoteUser>, onUnblock: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxeColors.DeepSea,
        titleContentColor = LuxeColors.Gold,
        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(32.dp)),
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Block, null, tint = LuxeColors.Red)
                Spacer(Modifier.width(16.dp))
                Text("LISTA NEGRA", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                Text("Usuarios que has bloqueado. No pueden hablarte ni los verás en la radio.", fontSize = 12.sp, color = Color.White.copy(0.6f))
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
fun LuxeNotificationOverlay(notification: AppNotification?, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        if (notification != null) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                    color = LuxeColors.DeepSea.copy(0.9f),
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
                            Text(notification.message, color = Color.White.copy(0.6f), fontSize = 11.sp)
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
