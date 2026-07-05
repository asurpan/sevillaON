package com.sagon.on

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LuxeColors {
    // Colores Base
    val Gold = Color(0xFFFACC15)
    val GoldDim = Color(0xFFEAB308).copy(0.6f)
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
    val BackgroundGradient = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))
    val GlossyGold = Brush.verticalGradient(listOf(Color(0xFFFFE082), Color(0xFFFACC15), Color(0xFFEAB308)))
    val LiquidGlass = Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.02f)))
    
    val Scheme = darkColorScheme(
        primary = Color(0xFFFACC15),
        surface = Color(0xFF0F172A),
        background = Color(0xFF0F172A),
        onSurface = Color.White
    )
}
