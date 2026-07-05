package com.sagon.on

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sqrt
import on.shared.generated.resources.Res
import on.shared.generated.resources.mapa_nacional

/**
 * 📡 RADAR VISUAL PRO - LA EVOLUCIÓN DE LA RADIO CB
 * Visualización hipnótica y profesional de la actividad nacional.
 * Diseñado para cautivar y mostrar una red viva.
 */

data class MapPoint(val name: String, val x: Float, val y: Float)

@Composable
fun NationalRadarMap(
    users: List<RemoteUser>,
    onCitySelect: (String) -> Unit
) {
    val points = remember {
        listOf(
            MapPoint("SEVILLA", 0.25f, 0.72f),
            MapPoint("MADRID", 0.48f, 0.45f),
            MapPoint("BARCELONA", 0.85f, 0.30f),
            MapPoint("VALENCIA", 0.68f, 0.53f),
            MapPoint("ALICANTE", 0.66f, 0.63f),
            MapPoint("MÁLAGA", 0.38f, 0.83f),
            MapPoint("MURCIA", 0.61f, 0.71f),
            MapPoint("CÁDIZ", 0.25f, 0.84f),
            MapPoint("BIZKAIA", 0.48f, 0.12f),
            MapPoint("A CORUÑA", 0.12f, 0.15f),
            MapPoint("ISLAS BALEARES", 0.88f, 0.52f),
            MapPoint("LAS PALMAS", 0.14f, 0.91f),
            MapPoint("STA. CRUZ TENERIFE", 0.06f, 0.90f),
            MapPoint("ASTURIAS", 0.32f, 0.13f),
            MapPoint("ZARAGOZA", 0.63f, 0.28f),
            MapPoint("PONTEVEDRA", 0.11f, 0.26f),
            MapPoint("GRANADA", 0.45f, 0.80f),
            MapPoint("TARRAGONA", 0.75f, 0.35f),
            MapPoint("CÓRDOBA", 0.36f, 0.69f),
            MapPoint("GIPUZKOA", 0.56f, 0.13f),
            MapPoint("GIRONA", 0.86f, 0.22f),
            MapPoint("ALMERÍA", 0.56f, 0.83f),
            MapPoint("TOLEDO", 0.45f, 0.54f),
            MapPoint("BADAJOZ", 0.25f, 0.59f),
            MapPoint("NAVARRA", 0.60f, 0.16f),
            MapPoint("JAÉN", 0.47f, 0.73f),
            MapPoint("CASTELLÓN", 0.70f, 0.45f),
            MapPoint("CANTABRIA", 0.43f, 0.12f),
            MapPoint("HUELVA", 0.19f, 0.76f),
            MapPoint("VALLADOLID", 0.41f, 0.33f),
            MapPoint("CIUDAD REAL", 0.44f, 0.64f),
            MapPoint("LEÓN", 0.31f, 0.22f),
            MapPoint("LLEIDA", 0.74f, 0.26f),
            MapPoint("ALBACETE", 0.56f, 0.63f),
            MapPoint("BURGOS / SORIA", 0.51f, 0.26f),
            MapPoint("SALAMANCA / ÁVILA", 0.35f, 0.43f),
            MapPoint("LOGROÑO / ÁLAVA", 0.54f, 0.20f),
            MapPoint("CÁCERES / SEGOVIA", 0.33f, 0.53f),
            MapPoint("LUGO / OURENSE / PALENCIA / ZAMORA", 0.23f, 0.22f),
            MapPoint("CUENCA / TERUEL / GUADALAJARA / CEUTA / MELILLA", 0.57f, 0.47f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarMapAnim")
    
    // Animaciones para las ondas de radio PRO
    val wave1Radius by infiniteTransition.animateFloat(0f, 100f, infiniteRepeatable(tween(3000), RepeatMode.Restart), label = "Wave1")
    val wave1Alpha by infiniteTransition.animateFloat(0.8f, 0f, infiniteRepeatable(tween(3000), RepeatMode.Restart), label = "Wave1Alpha")
    
    val wave2Radius by infiniteTransition.animateFloat(0f, 100f, infiniteRepeatable(tween(3000, delayMillis = 1000), RepeatMode.Restart), label = "Wave2")
    val wave2Alpha by infiniteTransition.animateFloat(0.8f, 0f, infiniteRepeatable(tween(3000, delayMillis = 1000), RepeatMode.Restart), label = "Wave2Alpha")
    
    val wave3Radius by infiniteTransition.animateFloat(0f, 100f, infiniteRepeatable(tween(3000, delayMillis = 2000), RepeatMode.Restart), label = "Wave3")
    val wave3Alpha by infiniteTransition.animateFloat(0.8f, 0f, infiniteRepeatable(tween(3000, delayMillis = 2000), RepeatMode.Restart), label = "Wave3Alpha")

    val scanLineY by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(4000), RepeatMode.Restart), label = "ScanLine")
    val ambientGlow by infiniteTransition.animateFloat(0.1f, 0.4f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "AmbientGlow")

    // Partículas de señal
    val particleAnim by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(8000), RepeatMode.Restart), label = "Particles")

    var scale by remember { mutableStateOf(1.2f) } 
    var offset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF020617)) 
            .border(1.dp, LuxeColors.Gold.copy(0.2f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    val maxX = (size.width * (scale - 1)) / 2
                    val maxY = (size.height * (scale - 1)) / 2
                    offset = Offset(
                        x = (offset.x + pan.x * scale).coerceIn(-maxX, maxX),
                        y = (offset.y + pan.y * scale).coerceIn(-maxY, maxY)
                    )
                }
            }
    ) {
        // --- 🌌 EFECTO NEÓN DE FONDO ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(LuxeColors.ElectricBlue.copy(0.05f), Color.Transparent),
                        center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f),
                        radius = constraints.maxWidth.toFloat()
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    rotationX = 15f 
                    cameraDistance = 8 * density
                }
        ) {
            Image(
                painter = painterResource(Res.drawable.mapa_nacional),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(LuxeColors.ElectricBlue, BlendMode.Screen)
            )

            Canvas(Modifier.fillMaxSize().pointerInput(scale, offset) {
                detectTapGestures { tapOffset ->
                    val adjustedX = (tapOffset.x - size.width / 2 - offset.x) / scale + size.width / 2
                    val adjustedY = (tapOffset.y - size.height / 2 - offset.y) / scale + size.height / 2
                    
                    val clickedPoint = points.minByOrNull { pt ->
                        val px = size.width * pt.x
                        val py = size.height * pt.y
                        (px - adjustedX) * (px - adjustedX) + (py - adjustedY) * (py - adjustedY)
                    }
                    
                    clickedPoint?.let { pt ->
                        val px = size.width * pt.x
                        val py = size.height * pt.y
                        val dist = sqrt(((px - adjustedX) * (px - adjustedX) + (py - adjustedY) * (py - adjustedY)).toDouble())
                        if (dist < 40 / scale) onCitySelect(pt.name)
                    }
                }
            }) {
                // --- 🛡️ GRID DE CONEXIONES ---
                points.forEach { p1 ->
                    points.forEach { p2 ->
                        if (p1 != p2) {
                            val d = sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y))
                            if (d < 0.15f) {
                                val isConnected = users.any { it.city == p1.name } && users.any { it.city == p2.name }
                                drawLine(
                                    color = if (isConnected) LuxeColors.Gold.copy(0.15f) else Color.White.copy(0.03f),
                                    start = Offset(size.width * p1.x, size.height * p1.y),
                                    end = Offset(size.width * p2.x, size.height * p2.y),
                                    strokeWidth = 1f / scale
                                )
                            }
                        }
                    }
                }

                points.forEach { pt ->
                    val cityUsers = users.filter { it.city == pt.name }
                    val isActive = cityUsers.isNotEmpty()
                    val isTransmitting = cityUsers.any { it.isTransmitting }

                    val centerX = size.width * pt.x
                    val centerY = size.height * pt.y

                    if (isTransmitting) {
                        // --- 📡 ONDAS DE RADIO ÉLITE ---
                        drawCircle(LuxeColors.Gold.copy(alpha = wave1Alpha), radius = wave1Radius / scale, center = Offset(centerX, centerY), style = Stroke(2f / scale))
                        drawCircle(LuxeColors.Gold.copy(alpha = wave2Alpha), radius = wave2Radius / scale, center = Offset(centerX, centerY), style = Stroke(1.5f / scale))
                        drawCircle(LuxeColors.Gold.copy(alpha = wave3Alpha), radius = wave3Radius / scale, center = Offset(centerX, centerY), style = Stroke(1f / scale))
                        
                        // --- 🕯️ EFECTO BEAM (HAZ DE LUZ 2.5D) ---
                        val beamHeight = 40f / scale
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, LuxeColors.Gold.copy(0.4f), LuxeColors.Gold.copy(0.8f)),
                                startY = centerY - beamHeight,
                                endY = centerY
                            ),
                            topLeft = Offset(centerX - 2f / scale, centerY - beamHeight),
                            size = androidx.compose.ui.geometry.Size(4f / scale, beamHeight)
                        )

                        // Resplandor central
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(LuxeColors.Gold.copy(0.6f), Color.Transparent),
                                center = Offset(centerX, centerY),
                                radius = 20f / scale
                            ),
                            radius = 20f / scale,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(LuxeColors.Gold, radius = 4f / scale, center = Offset(centerX, centerY))
                    } else if (isActive) {
                        drawCircle(
                            color = LuxeColors.ElectricBlue.copy(alpha = ambientGlow),
                            radius = 12f / scale,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(LuxeColors.ElectricBlue, radius = 3f / scale, center = Offset(centerX, centerY))
                    } else {
                        drawCircle(Color.White.copy(0.15f), radius = 1.2f / scale, center = Offset(centerX, centerY))
                    }
                }

                drawLine(
                    color = LuxeColors.ElectricBlue.copy(0.1f),
                    start = Offset(0f, size.height * scanLineY),
                    end = Offset(size.width, size.height * scanLineY),
                    strokeWidth = 2f
                )

                // --- 🛡️ PARTÍCULAS DE SEÑAL ---
                repeat(15) { i ->
                    val progress = (particleAnim + (i * 0.07f)) % 1f
                    val startX = (i * 73 * size.width / 100) % size.width
                    val startY = (i * 37 * size.height / 100) % size.height
                    drawCircle(
                        color = Color.White.copy(alpha = (1f - progress) * 0.3f),
                        radius = 1f / scale,
                        center = Offset(startX + (progress * 50), startY - (progress * 50))
                    )
                }
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(0.4f))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                .clickable { triggerUiSound("switch") }
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(LuxeColors.Gold))
                    Spacer(Modifier.width(8.dp))
                    Text("RADAR VISUAL PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Text("BANDA CIUDADANA VIVA", color = LuxeColors.Gold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("ACTIVIDAD: ${users.count { it.isTransmitting }} EMITIENDO", color = Color.White.copy(0.5f), fontSize = 7.sp, fontWeight = FontWeight.Medium)
            }
        }

        val liveAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse))
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).graphicsLayer(alpha = liveAlpha).clip(CircleShape).background(Color.Red))
            Spacer(Modifier.width(6.dp))
            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }

        Box(Modifier.align(Alignment.BottomEnd).padding(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(0.6f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text("RADAR NACIONAL ACTIVO", color = LuxeColors.Gold, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("UBICACIÓN GPS PRIVADA", color = Color.White.copy(0.4f), fontSize = 6.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            "USA DOS DEDOS PARA AMPLIAR Y MOVER EL MAPA",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
            color = Color.White.copy(0.3f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
