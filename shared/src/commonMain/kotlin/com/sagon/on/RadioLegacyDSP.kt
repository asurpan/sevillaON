package com.sagon.on

import kotlin.math.PI
import kotlin.math.abs

/**
 * 📻 RADIO LEGACY DSP - MÓDULO DE PROCESADO DE AUDIO
 * Clase quirúrgica para imitar el sonido de emisoras profesionales (Policía, Aviación, CB).
 * 
 * Basado en:
 * 1. Compresión Dinámica Extrema (Muro de sonido)
 * 2. Filtrado Pasabanda Estricto (300Hz - 3kHz)
 * 3. Saturación Armónica (Calidez analógica)
 */

object RadioLegacyDSP {

    /**
     * Parámetros óptimos para el compresor de radio.
     * Estos valores hacen que la voz suene "pegada" y constante.
     */
    val CompressorConfig = object {
        val threshold = -24.0f // Umbral profesional equilibrado
        val ratio = 8.0f       // Compresión firme pero controlada
        val attack = 0.005f    // Ataque suave para transitorios naturales
        val release = 0.200f   // Recuperación profesional
        val knee = 30.0f       
    }

    /**
     * Configuración de filtrado para el sonido "metálico" característico.
     */
    val FilterConfig = object {
        val type = "peaking"
        val centerFrequency = 1600.0f
        val qualityQ = 1.0f     // Calidez de locutor real
    }

    /**
     * Curva de saturación para imitar el clipping analógico.
     * (Para usar con WaveShaperNode en Web Audio)
     */
    fun getSaturationCurve(amount: Int = 50): FloatArray {
        val samples = 44100
        val curve = FloatArray(samples)
        val deg = PI.toFloat() / 180
        for (i in 0 until samples) {
            val x = (i * 2f / samples) - 1
            // Fórmula de distorsión clásica
            curve[i] = (3 + amount) * x * 20 * deg / (PI.toFloat() + amount * abs(x))
        }
        return curve
    }
    
    /**
     * Nota: Para implementar esto sin romper el audio actual, se debe instanciar 
     * un WaveShaperNode y conectarlo entre el filtro y el compresor.
     */
}
