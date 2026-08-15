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
    // Colores Base (REDISEÑO TURQUESA TÁCTICO)
    val Gold = Color(0xFF22D3EE) // Turquesa / Cyan vibrante
    val GoldDim = Color(0xFF0891B2).copy(0.6f)
    val Red = Color(0xFFEF4444)
    val Green = Color(0xFF22D3EE) // Cyan Brillante
    
    // Colores Premium "Gama Alta"
    val DeepSea = Color(0xFF020617) // Azul Profundo
    val ElectricBlue = Color(0xFF0EA5E9)
    val Slate900 = Color(0xFF020617)
    val Slate800 = Color(0xFF0F172A)
    
    // Glassmorphism Avanzado
    val GlassWhite = Color.White.copy(0.04f)
    val GlassBorder = Color.White.copy(0.1f)
    val GlassDeep = Color.Black.copy(0.6f)
    
    // Gradientes Editoriales
    val BackgroundGradient = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
    val NightGradient = Brush.verticalGradient(listOf(Color(0xFF020617), Color(0xFF0F172A))) 
    val GlossyGold = Brush.verticalGradient(listOf(Color(0xFF7DD3FC), Color(0xFF06B6D4), Color(0xFF0891B2)))
    val NightAmber = Color(0xFF0EA5E9) 
    val LiquidGlass = Brush.linearGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.01f)))
    
    val Scheme = darkColorScheme(
        primary = Color(0xFF06B6D4),
        surface = Color(0xFF020617),
        background = Color(0xFF020617),
        onSurface = Color.White
    )
}

object EliteTheme {
    val DeepGradient = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
}
