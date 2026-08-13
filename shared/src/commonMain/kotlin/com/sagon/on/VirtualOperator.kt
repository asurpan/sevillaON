package com.sagon.on

import kotlinx.coroutines.*

/**
 * 🤖 VIRTUAL OPERATOR CORE: SISTEMA DE VIDA ARTIFICIAL (GRATIS)
 * Clase independiente que gestiona la lógica de los bots locales.
 */
object VirtualOperator {
    private var job: Job? = null
    private var lastLocationInformed: String? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private const val BULLETIN_INTERVAL = 480000L // 8 minutos

    fun start(
        city: String,
        nick: String,
        bulletinsEnabled: Boolean,
        onAnnounce: (String) -> Unit
    ) {
        stop()
        
        // 🛡️ REGLA DE ORO: Solo permitimos actividad automática si la radio FM está encendida
        // El parámetro bulletinsEnabled vendrá condicionado por (bgStationName != null) desde App.kt
        if (!bulletinsEnabled) return 

        job = scope.launch {
            // Bienvenida inicial (solo si la radio está en marcha)
            delay(5000)
            val welcome = generateWelcome(city, nick)
            withContext(Dispatchers.Main) { onAnnounce(welcome) }
            
            while (isActive) {
                delay(BULLETIN_INTERVAL)
                val bulletin = fetchFreshBulletin(city)
                withContext(Dispatchers.Main) { onAnnounce(bulletin) }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * 🛰️ SEGUIMIENTO INTELIGENTE: El locutor detecta que hemos cambiado de zona
     */
    fun onZoneChange(newZone: String, nick: String, onAnnounce: (String) -> Unit) {
        // Evitar redundancia (no informar de lo mismo en menos de 5 min)
        if (newZone == lastLocationInformed) return
        lastLocationInformed = newZone

        scope.launch {
            val hour = getCurrentHour()
            val saludo = when (hour) {
                in 6..12 -> "Atención equipo."
                in 13..20 -> "Buenas tardes."
                else -> "Buenas noches."
            }
            
            // Boletín inmediato sobre la nueva zona
            val info = fetchFreshBulletin(newZone)
            val text = "$saludo Estación $nick, acabas de entrar en $newZone. Te cuento algo sobre este lugar. $info"
            
            withContext(Dispatchers.Main) { 
                onAnnounce(text.replace("TURISMO: ", "")) 
            }
        }
    }

    fun triggerBulletin(city: String, onAnnounce: (String) -> Unit) {
        scope.launch {
            val bulletin = fetchFreshBulletin(city)
            withContext(Dispatchers.Main) { onAnnounce(bulletin) }
        }
    }

    private fun generateWelcome(city: String, nick: String): String {
        val hour = getCurrentHour()
        val saludo = when (hour) {
            in 6..12 -> "Buenos días"
            in 13..20 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return "$saludo $nick. Has sintonizado la frecuencia local de $city. Estación de control a la escucha. ¿Hay algún compañero por ahí?"
    }

    private suspend fun fetchFreshBulletin(city: String): String {
        // Intentar obtener info turística dinámica vía Wikipedia/Mapas
        val tourism = suspendCancellableCoroutine<String?> { cont ->
            fetchTourismInfo(city) { info ->
                if (cont.isActive) cont.resumeWith(Result.success(info))
            }
        }

        if (tourism != null) {
            val shortTourism = if (tourism.length > 250) tourism.take(247) + "..." else tourism
            return "TURISMO: $shortTourism"
        }

        val options = listOf(
            "MODO_NOTICIAS_REALES", // Dispara el audio real de RNE
            "Reporte de antena en $city: Señal estable y repetidores operativos.",
            "Recordatorio de cortesía: Se recomienda brevedad en las transmisiones.",
            "TRAFICO_LOCAL", // Reporte de la DGT
            "ON AIR SPAIN: Estación de control a la escucha. ¿Alguna estación para reporte?"
        )
        return options.random()
    }
}
