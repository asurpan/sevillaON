package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - IDENTIDAD VISUAL ESMERALDA
 * ESTADO: SELLADO Y PROTEGIDO / PROHIBIDA MODIFICACIÓN ESTRUCTURAL
 * 
 * Este archivo define la piel premium de la aplicación.
 * Cualquier cambio de color o gradiente requiere permiso explícito nivel 0.
 */

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LuxeColors {
    // Colores Base (REDISEÑO ESMERALDA PREMIUM)
    val Gold = Color(0xFF22C55E) // Antes Amarillo, ahora Verde Esmeralda
    val GoldDim = Color(0xFF16A34A).copy(0.6f)
    val Red = Color(0xFFEF4444)
    val Green = Color(0xFF22C55E)
    
    // Colores Premium "Gama Alta"
    val DeepSea = Color(0xFF0F172A)
    val ElectricBlue = Color(0xFF3B82F6)
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    
    // Glassmorphism Avanzado
    val GlassWhite = Color.White.copy(0.04f)
    val GlassBorder = Color.White.copy(0.12f)
    val GlassDeep = Color.Black.copy(0.4f)
    
    // Gradientes Editoriales
    val BackgroundGradient = Brush.verticalGradient(listOf(Color.Black, Color(0xFF1E1B4B)))
    val NightGradient = Brush.verticalGradient(listOf(Color(0xFF020617), Color(0xFF1E3A8A))) // Negro a Azul Profundo Táctico
    val GlossyGold = Brush.verticalGradient(listOf(Color(0xFFBBF7D0), Color(0xFF22C55E), Color(0xFF16A34A)))
    val NightAmber = Color(0xFFF59E0B) // Ámbar para LEDs y Dial nocturno
    val LiquidGlass = Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.02f)))
    
    val Scheme = darkColorScheme(
        primary = Color(0xFF22C55E),
        surface = Color(0xFF0F172A),
        background = Color(0xFF0F172A),
        onSurface = Color.White
    )
}
