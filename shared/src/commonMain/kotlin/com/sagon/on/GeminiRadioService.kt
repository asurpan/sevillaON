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

    // 🧠 HISTORIAL RECIENTE PARA DIÁLOGO INTELIGENTE CON MEMORIA (ÚLTIMAS 6 INTERACCIONES)
    private val conversationHistory = mutableListOf<String>()
    private const val MAX_HISTORY_SIZE = 6

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
                onResult("¡Excelente modulación $userNick! Soy FELIPE. Invita a tus amigos y colegas compartiendo el canal para montar aquí una buena rueda en $cleanCity. ¡A la escucha! Cambio.")
                return
            }
        }

        // 4. Si se superó el límite horario o no hay claves de API, usar Caché de Respaldo Local (0€ Coste, 0% Fallo)
        val activeKey = getActiveKey()
        if (hourlyRequestCount >= MAX_HOURLY_REQUESTS || activeKey == null) {
            val fallback = getFallbackTacticalResponse(city, userNick, isSilentBeacon, userMessage)
            onResult(fallback)
            return
        }

        hourlyRequestCount++

        // 5. Generar prompt táctico e inteligente para Gemini
        val cleanCity = city.split("-")[0].trim().uppercase()
        val currentTime = getCurrentFormattedTime()
        val systemPrompt = "Te llamas FELIPE y eres el operador virtual estrella y compañero de radio de la Estación Base ON AIR SPAIN en $cleanCity. Tono: Súper ameno, enganchador, fresco, natural y directo. Tu indicativo de radio es 'Soy FELIPE, Estación Base $cleanCity'. Identifícate SIEMPRE diciendo 'Soy FELIPE' (NUNCA digas 'Felipe te escucha' ni 'Aquí Felipe'). La hora local exacta es $currentTime. Tu misión es ser la voz imprescindible de $cleanCity: cuando te pregunten o des el parte de la ciudad, proporciona un informe meteo y de tráfico resumido pero ULTRA PRÁCTICO Y ÚTIL (cielos, temperatura estimada realista para la hora $currentTime, viento y estado exacto del tráfico). Si detectas o hay cambios respecto a hace poco, dilo con naturalidad. Termina SIEMPRE con el enganche cálido: 'Hace un [día/noche] precioso para hablar por radio con amigos o conocer Gente Real'. Mantén estilo de radioaficionado ágil y cercano, con frases cortas y vibrantes. NUNCA pidas que dejen el canal libre ni utilices expresiones de carretera como 'al volante' o 'buen viaje'. Responde SIEMPRE de forma directa, útil y coherente. Máximo 2 o 3 frases dinámicas (termina con 'Cambio' o 'A la escucha')."

        val promptContent = if (isSilentBeacon) {
            "Saluda de forma muy acogedora en la radio de $cleanCity e invita a hablar."
        } else {
            val userText = userMessage ?: "Hola, prueba de radio"
            val historyContext = if (conversationHistory.isNotEmpty()) {
                "Historial de conversación previo en el canal:\n" + conversationHistory.takeLast(MAX_HISTORY_SIZE).joinToString("\n") + "\n\n"
            } else ""
            "${historyContext}El usuario $userNick en $cleanCity dice por la radio: \"$userText\". Responde de forma inteligente, útil y coherente a su mensaje."
        }

        // Ejecutar llamada HTTP a la API de Gemini con manejo automático de errores
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$activeKey"
        val payload = buildJsonPayload(systemPrompt, promptContent)

        fetchGeminiApi(endpoint, payload) { response ->
            val extractedText = parseGeminiTextResponse(response)
            if (extractedText != null) {
                if (!isSilentBeacon && userMessage != null) {
                    conversationHistory.add("$userNick: $userMessage")
                    conversationHistory.add("Emisora: $extractedText")
                    while (conversationHistory.size > MAX_HISTORY_SIZE) {
                        conversationHistory.removeAt(0)
                    }
                }
                onResult(extractedText)
            } else {
                // Si la clave falló, rotar a la siguiente clave y usar respuesta de respaldo
                rotateKey()
                val fallback = getFallbackTacticalResponse(city, userNick, isSilentBeacon, userMessage)
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
            val regex = Regex(""""text"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            val match = regex.find(json)
            if (match != null) {
                var text = match.groupValues[1]
                text = text.replace("\\\"", "\"")
                           .replace("\\n", " ")
                           .replace("\\r", " ")
                           .replace("\\\\", "\\")
                           .trim()
                if (text.isNotBlank()) return text
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
        isSilentBeacon: Boolean,
        userMessage: String? = null
    ): String {
        val cleanCity = city.split("-")[0].trim().uppercase()
        val nick = userNick.trim().uppercase()
        val currentTime = getCurrentFormattedTime()

        val currentHour = try { currentTime.split(":")[0].toInt() } catch (e: Exception) { 12 }
        val isNight = currentHour !in 7..20
        val moment = if (isNight) "noche" else "día"

        val msgLower = userMessage?.lowercase() ?: ""
        if (msgLower.contains("quien eres") || msgLower.contains("quién eres") || msgLower.contains("como te llamas") || msgLower.contains("cómo te llamas") || msgLower.contains("que eres") || msgLower.contains("qué eres") || msgLower.contains("quien habla")) {
            return "¡Hola $nick! Soy FELIPE, el operador virtual de la Estación Base ON AIR en $cleanCity. ¡Un placer saludarte por la frecuencia! Cambio."
        }

        if (msgLower.contains("hora")) {
            return "¡Hola $nick! Soy FELIPE. Son exactamente las $currentTime en $cleanCity. Te recibo fuerte y claro. ¡Cambio!"
        }

        if (msgLower.contains("tiempo") || msgLower.contains("clima") || msgLower.contains("meteo") || msgLower.contains("parte") || msgLower.contains("trafico") || msgLower.contains("tráfico") || msgLower.contains("cambio") || msgLower.contains("novedad")) {
            val tempText = if (currentHour in 12..18) "unos 23°C con sol agradable" else if (currentHour in 7..11) "18°C agradables para la mañana" else if (currentHour in 19..22) "19°C con brisa estupenda" else "15°C y noche fresca"
            return "¡Hola $nick! Soy FELIPE. En $cleanCity tenemos cielos claros, $tempText y viento suave. El tráfico está fluido y sin retenciones en los accesos. Hace un $moment precioso para hablar por radio con amigos o conocer Gente Real. ¡Cambio!"
        }

        if (isSilentBeacon) {
            val tempText = if (currentHour in 12..18) "23°C soleados" else if (currentHour in 7..11) "18°C agradables" else "15°C frescos"
            val beacons = listOf(
                "¡Atención $cleanCity! Soy FELIPE. $tempText, viento suave y tráfico totalmente fluido a las $currentTime. Hace un $moment precioso para hablar por radio con amigos o conocer Gente Real. ¡Entra en frecuencia!",
                "¡Buenas por $cleanCity! Soy FELIPE a la escucha a las $currentTime. Cielos despejados y circulación ágil sin retenciones. Hace un $moment estupendo para charlar por radio y conocer Gente Real. Cambio.",
                "¡Hola $cleanCity! Soy FELIPE reportando a las $currentTime. Todo tranquilo en las carreteras, buen clima en la ciudad y señal fuerte. Hace un $moment precioso para hablar por radio con amigos. Cambio."
            )
            return beacons.random()
        }

        val responses = listOf(
            "¡Hola $nick! Soy FELIPE. Te escucho perfecto en $cleanCity. Cielos despejados, tráfico fluido a las $currentTime y hace un $moment precioso para hablar por radio con amigos o conocer Gente Real. ¡Cambio!",
            "¡Buenas $nick! Soy FELIPE, te recibo alto y claro desde $cleanCity. Todo tranquilo en las calles, buen tiempo y un $moment ideal para charlar y conocer Gente Real. ¡Adelante! Cambio.",
            "¡Qué tal $nick! Soy FELIPE. Llegas con señal impecable por $cleanCity. Cielos despejados, temperatura suave y un $moment precioso para hablar por radio con amigos o conocer Gente Real. ¡Cambio!",
            "¡Recibido $nick! Soy FELIPE, te escucho estupendamente a las $currentTime en $cleanCity. Tráfico ágil sin retenciones. Hace un $moment precioso para hablar por radio con amigos o conocer Gente Real. Cambio."
        )
        return responses.random()
    }
}
