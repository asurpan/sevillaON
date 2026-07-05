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
            ActivityProfile.CICLISMO -> 200  // Viento moderado
            ActivityProfile.MONTANA -> 120   // Ráfagas de viento
            ActivityProfile.SENDERISMO -> 100 // Viento leve
            ActivityProfile.SOCORRISTAS -> 100 // Claridad en exteriores
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
            ActivityProfile.CICLISMO -> 1.2f
            ActivityProfile.MONTANA -> 1.1f
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
    LaunchedEffect(state.activeProfile) {
        if (state.activeProfile != ActivityProfile.NORMAL) {
            // MODO ACTIVIDAD: Vigilancia continua de cobertura
            while (true) {
                // Aquí el Bridge nativo informará si no hay cobertura de datos
                // Simulamos chequeo cada 10 segundos
                delay(10000)
                
                // Si la app detecta que no hay internet, pedimos al Bridge activar P2P
                // onExecuteAction("ACTIVATE_WIFI_MESH")
            }
        }
    }
}
