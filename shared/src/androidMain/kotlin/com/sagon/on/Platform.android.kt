package com.sagon.on

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun playScanClick() {}

actual fun vibratePtt() {}

actual fun triggerUiSound(type: String) {}

actual fun getTimeMillis(): Long = System.currentTimeMillis()

actual fun tryOpenNativeApp() {}

actual fun playIntroMusic() {}

actual fun stopIntroMusic() {}

actual fun playWelcomeSequence() {}

actual fun getCurrentHour(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

actual fun getCurrentFormattedTime(): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

actual fun setVirtualOperatorText(text: String) {
    // En Android esto se podría inyectar en un motor TTS nativo si fuera necesario
    // Por ahora lo dejamos listo para el puente con la WebApp
}

actual fun showSystemNotification(title: String, message: String) {
    // La lógica real está en el módulo androidApp. 
    // Desde el shared (common) en Android no disparamos notificaciones locales directas
    // para evitar duplicidades con el sistema FCM nativo.
}

private val tourismScope = CoroutineScope(Dispatchers.IO)

actual fun fetchTourismInfo(city: String, callback: (String?) -> Unit) {
    tourismScope.launch {
        try {
            val url = java.net.URL("https://es.wikipedia.org/api/rest_v1/page/summary/${city.replace(" ", "_")}")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            val text = connection.inputStream.bufferedReader().readText()
            
            val extract = text.substringAfter("\"extract\":\"", "").substringBefore("\",\"extract_html\"")
            val cleanExtract = extract.replace("\\n", " ").replace("\\u00", "") 
            
            withContext(Dispatchers.Main) {
                callback(if (cleanExtract.length > 20) cleanExtract else null)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }
}

actual fun fetchLiveNewsTopic(city: String, callback: (String?) -> Unit) {
    tourismScope.launch {
        try {
            // 📰 CARGAR DIRECTAMENTE EL RSS OFICIAL DE GOOGLE NEWS ESPAÑA (TITULARES DE MÁXIMO IMPACTO NACIONAL)
            val url = java.net.URL("https://news.google.com/rss?hl=es&gl=ES&ceid=ES:es")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            val xmlText = connection.inputStream.bufferedReader().readText()
            
            var headline: String? = null
            if (xmlText.contains("<item>")) {
                val itemBlock = xmlText.substringAfter("<item>").substringBefore("</item>")
                val rawTitle = itemBlock.substringAfter("<title>").substringBefore("</title>")
                val cleanTitle = rawTitle.substringBefore(" - ")
                    .replace("<![CDATA[", "")
                    .replace("]]>", "")
                    .trim()
                if (cleanTitle.length > 10) {
                    headline = cleanTitle
                }
            }

            withContext(Dispatchers.Main) {
                callback(headline)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }
}

actual fun fetchGeminiApi(endpoint: String, jsonBody: String, callback: (String?) -> Unit) {
    tourismScope.launch {
        try {
            val url = java.net.URL(endpoint)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            
            connection.outputStream.use { os ->
                val input = jsonBody.toByteArray(charset("UTF-8"))
                os.write(input, 0, input.size)
            }
            
            val responseText = connection.inputStream.bufferedReader().readText()
            withContext(Dispatchers.Main) {
                callback(responseText)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }
}
