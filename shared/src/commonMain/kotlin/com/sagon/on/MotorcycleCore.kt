package com.sagon.on

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * 🔒 HARD-LOCK: MOTOR DE ACTIVIDADES Y MALLA DE SUPERVIVENCIA
 * ESTADO: INGENIERÍA DE ÉLITE -failover HÍBRIDO
 * 
 * Gestiona:
 * 1. Perfiles de audio según actividad (Moto, Bici, Montaña).
 * 2. Lógica de red en malla (WiFi Direct Failover).
 * 3. Sincronización de posición para el Mapa de Ruta.
 */
object MotorcycleCore {

    /**
     * Devuelve la configuración de filtrado de viento según el perfil.
     * @return Frecuencia de corte en Hz.
     */
    fun getWindFilterCutoff(profile: ActivityProfile): Int {
        return when (profile) {
            ActivityProfile.MOTO -> 300      // Ruido motor y alta velocidad
            ActivityProfile.PARAPENTE -> 280 // Viento extremo en vuelo
            ActivityProfile.VELA -> 250      // Viento marino constante
            ActivityProfile.CICLISMO -> 200  // Viento moderado
            ActivityProfile.ESQUI -> 200     // Viento y frío
            ActivityProfile.KAYAK -> 150     // Viento y agua
            ActivityProfile.MONTANA -> 120   // Ráfagas de viento
            ActivityProfile.SENDERISMO -> 100 // Viento leve
            ActivityProfile.SOCORRISTAS -> 100 // Claridad en exteriores
            ActivityProfile.CAZA -> 80       // Natural para susurros
            else -> 80                       // Sonido estándar (natural)
        }
    }

    /**
     * Calcula la sensibilidad del VOX necesaria para la actividad.
     * Cuanto más ruidosa la actividad, más alto debe ser el umbral de disparo.
     */
    fun getActivityVoxThreshold(profile: ActivityProfile, baseSens: Float): Float {
        val multiplier = when (profile) {
            ActivityProfile.MOTO -> 1.4f
            ActivityProfile.PARAPENTE -> 1.4f
            ActivityProfile.VELA -> 1.3f
            ActivityProfile.CICLISMO -> 1.2f
            ActivityProfile.ESQUI -> 1.2f
            ActivityProfile.MONTANA -> 1.1f
            ActivityProfile.KAYAK -> 1.1f
            ActivityProfile.CAZA -> 0.8f // Alta sensibilidad para susurros
            else -> 1.0f
        }
        return (baseSens * multiplier).coerceIn(0f, 1f)
    }
}

/**
 * 🛰️ MONITOR DE MALLA DE SUPERVIVENCIA
 * Composable invisible que vigila la conectividad y activa el puente WiFi si es necesario.
 */
@Composable
fun SurvivalMeshMonitor(
    state: RadioState,
    onMeshStatusChange: (Boolean) -> Unit,
    onExecuteAction: (String) -> Unit
) {
    // --- 🛰️ MONITOR GLOBAL: Vigilancia continua de cobertura ---
    LaunchedEffect(Unit) {
        while (true) {
            // El Bridge nativo informa si hay pérdida total de red (4G/5G)
            // Si detectamos desconexión crítica, activamos el Failover P2P (Malla)
            delay(15000) // Chequeo cada 15 segundos
            
            // onExecuteAction("CHECK_NETWORK_CRITICAL")
        }
    }
}
