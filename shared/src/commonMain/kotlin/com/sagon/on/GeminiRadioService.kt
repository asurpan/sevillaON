package com.sagon.on

/**
 * 🔒 MÓDULO AISLADO E INDEPENDIENTE: SERVICIO DE IA TÁCTICA PARA RADIO (GEMINI + RESPALDO LOCAL)
 * ESTADO: TOTALMENTE AISLADO - CERO RIESGO PARA EL CÓDIGO CORE
 * 
 * Funcionalidades:
 * 1. Rotación automática de claves de API (Failover).
 * 2. Sistema de protección anti-abuso y control de cuotas (Cooldown 3 min, límites horarios).
 * 3. Respuestas de máximo 15-20 palabras con lenguaje táctico de radioaficionado.
 * 4. Caché de respuesta local (100% offline / 0% fallo) si se agota la cuota o no hay red.
 */
object GeminiRadioService {

    private var apiKeys: List<String> = emptyList()
    private var currentKeyIndex = 0

    // 🛡️ BARRERAS ANTI-ABUSO Y CONTROL DE CUOTA
    private var consecutiveUserTalkCount = 0
    private var lastUserTalkTimestamp = 0L
    private var hourlyRequestCount = 0
    private var hourlyResetTimestamp = 0L
    private var cooldownUntilTimestamp = 0L

    private const val MAX_CONSECUTIVE_TALKS = 2
    private const val COOLDOWN_DURATION_MS = 3 * 60 * 1000L // 3 minutos
    private const val MAX_HOURLY_REQUESTS = 25
    private const val ONE_HOUR_MS = 60 * 60 * 1000L

    fun configureKeys(keys: List<String>) {
        this.apiKeys = keys.filter { it.isNotBlank() }
        this.currentKeyIndex = 0
    }

    fun getActiveKey(): String? {
        if (apiKeys.isEmpty()) return null
        return apiKeys[currentKeyIndex % apiKeys.size]
    }

    private fun rotateKey() {
        if (apiKeys.isNotEmpty()) {
            currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        }
    }

    /**
     * Evalúa la interacción del usuario y devuelve la respuesta táctica de la Estación Base.
     * Gestiona automáticamente la protección contra spam y agotamiento de claves.
     */
    fun processRadioMessage(
        city: String,
        userNick: String,
        userMessage: String?,
        isSilentBeacon: Boolean = false,
        onResult: (String) -> Unit
    ) {
        val now = getTimeMillis()

        // 1. Verificar si la estación está en Cooldown por uso intensivo
        if (now < cooldownUntilTimestamp) {
            // Silencio de radio respetuoso
            return
        }

        // 2. Control de reseteo horario
        if (now - hourlyResetTimestamp > ONE_HOUR_MS) {
            hourlyRequestCount = 0
            hourlyResetTimestamp = now
        }

        // 3. Verificación de uso consecutivo por usuario (Anti-Monopolio de canal)
        if (!isSilentBeacon) {
            if (now - lastUserTalkTimestamp < 60_000L) {
                consecutiveUserTalkCount++
            } else {
                consecutiveUserTalkCount = 1
            }
            lastUserTalkTimestamp = now

            if (consecutiveUserTalkCount > MAX_CONSECUTIVE_TALKS) {
                cooldownUntilTimestamp = now + COOLDOWN_DURATION_MS
                consecutiveUserTalkCount = 0
                val cleanCity = city.split("-")[0].trim().uppercase()
                onResult("Estación $userNick, canal ocupado. Dejamos frecuencia libre para otras estaciones de $cleanCity. Cambio y corto.")
                return
            }
        }

        // 4. Si se superó el límite horario o no hay claves de API, usar Caché de Respaldo Local (0€ Coste, 0% Fallo)
        val activeKey = getActiveKey()
        if (hourlyRequestCount >= MAX_HOURLY_REQUESTS || activeKey == null) {
            val fallback = getFallbackTacticalResponse(city, userNick, isSilentBeacon)
            onResult(fallback)
            return
        }

        hourlyRequestCount++

        // 5. Generar prompt táctico para Gemini
        val cleanCity = city.split("-")[0].trim().uppercase()
        val systemPrompt = "Eres la Estación Base de Control de Radio CB en $cleanCity. Tono: Profesional, sobrio, conciso y táctico. Máximo 2 frases (menos de 20 palabras). Usa lenguaje de radio CB (cambio, recibido, QRZ, QTH, S-Meter)."

        val promptContent = if (isSilentBeacon) {
            "Genera una baliza informativa muy breve de la estación de control de $cleanCity invitando a modular."
        } else {
            "Usuario $userNick en $cleanCity transmite: \"${userMessage ?: "Prueba de radio"}\". Responde brevemente como estación base."
        }

        // Ejecutar llamada HTTP a la API de Gemini con manejo automático de errores
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$activeKey"
        val payload = buildJsonPayload(systemPrompt, promptContent)

        fetchGeminiApi(endpoint, payload) { response ->
            val extractedText = parseGeminiTextResponse(response)
            if (extractedText != null) {
                onResult(extractedText)
            } else {
                // Si la clave falló, rotar a la siguiente clave y usar respuesta de respaldo
                rotateKey()
                val fallback = getFallbackTacticalResponse(city, userNick, isSilentBeacon)
                onResult(fallback)
            }
        }
    }

    private fun buildJsonPayload(systemInstruction: String, promptText: String): String {
        val safeSys = systemInstruction.replace("\"", "\\\"").replace("\n", " ")
        val safePrompt = promptText.replace("\"", "\\\"").replace("\n", " ")
        return "{\"system_instruction\":{\"parts\":[{\"text\":\"$safeSys\"}]},\"contents\":[{\"parts\":[{\"text\":\"$safePrompt\"}]}],\"generationConfig\":{\"maxOutputTokens\":60,\"temperature\":0.6}}"
    }

    private fun parseGeminiTextResponse(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            if (json.contains("\"text\":")) {
                val startIndex = json.indexOf("\"text\":") + 7
                val firstQuote = json.indexOf("\"", startIndex) + 1
                val lastQuote = json.indexOf("\"", firstQuote)
                if (firstQuote > 0 && lastQuote > firstQuote) {
                    var text = json.substring(firstQuote, lastQuote)
                    text = text.replace("\\n", " ").replace("\\\"", "\"").trim()
                    if (text.isNotBlank()) return text
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * CACHÉ DE RESPUESTA LOCAL TÁCTICA (0€ Coste, 100% Inmune a fallos de red/API)
     */
    fun getFallbackTacticalResponse(
        city: String,
        userNick: String,
        isSilentBeacon: Boolean
    ): String {
        val cleanCity = city.split("-")[0].trim().uppercase()
        val nick = userNick.trim().uppercase()

        if (isSilentBeacon) {
            val beacons = listOf(
                "Estación Base de Control $cleanCity en Canal. Frecuencia activa con portadora limpia. Adelante la estación que llame.",
                "Aquí Control $cleanCity. Recordatorio: mantener frecuencia despejada para llamadas de ruta y asistencia. A la escucha.",
                "Boletín de Estación Base en $cleanCity. Visibilidad y cobertura óptimas en la zona. Frecuencia abierta para modulación. Cambio.",
                "Llamada general en el canal de $cleanCity. Se solicita reporte de señal y modulación S-Meter para prueba de zona."
            )
            return beacons.random()
        }

        val responses = listOf(
            "Recibido $nick en $cleanCity. Te escucho con modulación perfecta y señal S9 en el repetidor. Quedo a la escucha. Cambio.",
            "Estación $nick, reporte de señal correcto desde $cleanCity. Llegas potente de audio en esta frecuencia. Adelante. Cambio.",
            "Atención $nick, te recibo alto y claro en la zona de $cleanCity. Buena modulación de equipo. Saludos y buena ruta. Cambio.",
            "Recibido $nick. Portadora y voz llegando con total claridad al control de $cleanCity. Frecuencia despejada. Cambio."
        )
        return responses.random()
    }
}
