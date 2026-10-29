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
            val cleanCity = city.split("-")[0].trim()
            val encodedCity = java.net.URLEncoder.encode(cleanCity, "UTF-8")
            
            // 📰 1. INTENTAR PRIMERO MÁXIMA NOTICIA DE IMPACTO Y ÚLTIMA HORA EN ESPAÑA
            val topUrl = java.net.URL("https://api.rss2json.com/v1/api.json?rss_url=" + java.net.URLEncoder.encode("https://news.google.com/rss/search?q=ultima+hora+España+cuando:1d&hl=es&gl=ES&ceid=ES:es", "UTF-8"))
            val connection = topUrl.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val jsonText = connection.inputStream.bufferedReader().readText()
            
            var headline: String? = null
            if (jsonText.contains("\"title\":\"")) {
                val rawTitle = jsonText.substringAfter("\"title\":\"", "").substringBefore("\",\"pubDate\"")
                val cleanTitle = rawTitle.substringBefore(" - ").replace("\\\"", "\"").trim()
                if (cleanTitle.length > 10 && !cleanTitle.contains("Google News", ignoreCase = true)) {
                    headline = cleanTitle
                }
            }

            // 📰 2. SI NO HAY NOTICIA NACIONAL DE IMPACTO, BUSCAR NOTICIA DE LA CIUDAD
            if (headline == null) {
                val cityUrl = java.net.URL("https://api.rss2json.com/v1/api.json?rss_url=https://news.google.com/rss/search?q=${encodedCity}+cuando:1d&hl=es&gl=ES&ceid=ES:es")
                val cityConn = cityUrl.openConnection() as java.net.HttpURLConnection
                cityConn.requestMethod = "GET"
                cityConn.connectTimeout = 3000
                cityConn.readTimeout = 3000
                val cityJson = cityConn.inputStream.bufferedReader().readText()
                if (cityJson.contains("\"title\":\"")) {
                    val rawTitle = cityJson.substringAfter("\"title\":\"", "").substringBefore("\",\"pubDate\"")
                    val cleanTitle = rawTitle.substringBefore(" - ").replace("\\\"", "\"").trim()
                    if (cleanTitle.length > 10) headline = cleanTitle
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
