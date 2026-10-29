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
                onResult("¡Excelente modulación $userNick! Invita a tus amigos y colegas compartiendo el canal para montar aquí una buena rueda en $cleanCity. ¡A la escucha! Cambio.")
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
        val systemPrompt = "Eres la Estación Base virtual inteligente de ON AIR SPAIN en $cleanCity. Tu función es ser un compañero de radio simpático, servicial y conversacional en la frecuencia. Si te preguntan quién eres, cómo te llamas o qué eres, preséntate con orgullo como la Estación Base de ON AIR SPAIN en $cleanCity. La hora local exacta es $currentTime. Responde SIEMPRE de forma directa e inteligente a lo que el usuario pregunte o diga (preguntas de todo tipo, hora, clima, dudas, bromas, opiniones o charla). NUNCA pidas que dejen el canal libre ni des órdenes de silencio. NUNCA utilices expresiones de carretera como 'buena ruta', 'buen viaje' o 'al volante' (el usuario puede estar en casa o paseando). Mantén estilo de radioaficionado breve (con 'Cambio' o 'A la escucha'). Máximo 2 o 3 frases cortas."

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

        val msgLower = userMessage?.lowercase() ?: ""
        if (msgLower.contains("quien eres") || msgLower.contains("quién eres") || msgLower.contains("como te llamas") || msgLower.contains("cómo te llamas") || msgLower.contains("que eres") || msgLower.contains("qué eres")) {
            return "¡Hola $nick! Soy la Estación Base virtual de ON AIR SPAIN en $cleanCity, tu compañero en esta frecuencia de radio. ¡Un placer hablar contigo! Cambio."
        }

        if (msgLower.contains("hora")) {
            return "¡Hola $nick! Son exactamente las $currentTime en $cleanCity. Te recibo fuerte y claro. ¡Cambio!"
        }

        if (isSilentBeacon) {
            val beacons = listOf(
                "¡Hola a todos en $cleanCity! Son las $currentTime y la frecuencia está activa. ¡Anímate a compartir el canal!",
                "¡Buenas por $cleanCity! Frecuencia libre a las $currentTime. Trae a tus colegas para montar rueda. A la escucha.",
                "¡Hola $cleanCity! Buena cobertura a las $currentTime. Frecuencia abierta, invita a tu gente a unirse. Cambio."
            )
            return beacons.random()
        }

        val responses = listOf(
            "¡Hola $nick! Te escucho genial a las $currentTime en $cleanCity. ¡Comparte el canal con tus amigos y montamos rueda! Cambio.",
            "¡Buenas $nick! Te recibo alto y claro a las $currentTime desde $cleanCity. Invita a tus colegas a la frecuencia. ¡Adelante! Cambio.",
            "¡Qué tal $nick! Llegas con señal fuerte por $cleanCity. Pasa la voz a tus amigos para que se unan. ¡Cambio!",
            "¡Recibido $nick! Se te escucha estupendamente por $cleanCity a las $currentTime. Comparte el enlace y hablamos. Cambio."
        )
        return responses.random()
    }
}
