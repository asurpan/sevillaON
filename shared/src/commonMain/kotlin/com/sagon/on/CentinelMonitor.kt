package com.sagon.on

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 🛰️ CENTINEL MONITOR: PROYECTOR DINÁMICO PARA EL DIAL CENTRAL
 * Muestra información de la NASA y Eventos Callejeros.
 */
@Composable
fun CentinelMonitor(
    state: RadioState,
    isTransmitting: Boolean,
    rx: Boolean,
    level: Float = 0f,
    showLeds: Boolean = true,
    modifier: Modifier = Modifier
) {
    val showVisuals = true // Siempre visible para los LEDs
    
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // --- 🌌 FONDO: ESPACIO PROFUNDO (NASA) ---
        AnimatedVisibility(
            visible = showVisuals,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
            ) {
                // Si tuviéramos un cargador de imágenes, iría aquí. 
                // Por ahora usamos un efecto técnico.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.3f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black)
                            )
                        )
                )
                
                if (showLeds) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // --- 🚥 VÚMETRO DE LEDS (ESTILO EQUIPO PROFESIONAL) ---
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "QRM")
                            val qrmPulse by infiniteTransition.animateFloat(
                                initialValue = 0f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(150), RepeatMode.Reverse),
                                label = "Pulse"
                            )

                            repeat(10) { i ->
                                val isLit = if (isTransmitting || rx) {
                                    (level * 10) > i
                                } else {
                                    // MODO QRM: Alternancia técnica de los 2 primeros LEDs con el ruido
                                    val noiseThreshold = 0.02f
                                    if (level > noiseThreshold) {
                                        if (i == 0) qrmPulse > 0.3f
                                        else if (i == 1) qrmPulse < 0.7f
                                        else false
                                    } else false
                                }

                                val segmentColor = when {
                                    i < 6 -> LuxeColors.Gold // Azul Piscina
                                    i < 8 -> Color(0xFFFFD700) // Amarillo
                                    else -> Color.Red          // Rojo
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(width = 6.dp, height = 12.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(if (isLit) segmentColor else Color.White.copy(0.05f))
                                        .border(0.5.dp, if (isLit) segmentColor.copy(0.3f) else Color.Transparent, RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "BANDA CIUDADANA",
                            color = LuxeColors.ElectricBlue.copy(0.4f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        // El teletipo redundante de SECTOR se ha eliminado para mayor limpieza visual
    }
}

@Composable
private fun ScrollingEventBar(state: RadioState) {
    val events = remember(state.city, state.dgtText) {
        val list = mutableListOf(
            "📍 SECTOR: ${state.city}"
        )
        if (state.dgtText != null) list.add("🚗 TRÁFICO: ${state.dgtText}")
        if (state.nasaImageTitle != null) list.add("🚀 NASA: ${state.nasaImageTitle}")
        list
    }
    
    var currentEventIdx by remember { mutableStateOf(0) }
    
    LaunchedEffect(state.city) {
        while (true) {
            delay(5000)
            currentEventIdx = (currentEventIdx + 1) % events.size
        }
    }

    AnimatedContent(
        targetState = events[currentEventIdx],
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                slideOutVertically { height -> -height } + fadeOut()
            )
        }
    ) { text ->
        Text(
            text = text,
            color = LuxeColors.Gold,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
