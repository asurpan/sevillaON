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

fun parseRssHeadline(xmlText: String, cleanCity: String): String? {
    if (!xmlText.contains("<item", ignoreCase = true)) return null
    val items = xmlText.split(Regex("<item[^>]*>")).drop(1).take(20)
    for (itemBlock in items) {
        if (!itemBlock.contains("<title>", ignoreCase = true)) continue
        var title = itemBlock.substringAfter("<title>").substringBefore("</title>")
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()

        if (title.contains(" - ")) {
            title = title.substringBeforeLast(" - ").trim()
        }
        if (title.length in 15..120 && !title.contains("Google", ignoreCase = true) && !title.contains("Weather", ignoreCase = true)) {
            return "🔴 ÚLTIMA HORA EN $cleanCity:\n\"$title\""
        }
    }
    return null
}

actual fun fetchLiveNewsTopic(city: String, callback: (String?) -> Unit) {
    tourismScope.launch {
        try {
            val cleanCity = city.split("-")[0].trim().uppercase()
            val encodedCity = java.net.URLEncoder.encode(cleanCity, "UTF-8")
            var headline: String? = null

            // 1. Noticia específica de la ciudad
            try {
                val cityUrl = java.net.URL("https://news.google.com/rss/search?q=${encodedCity}+actualidad&hl=es&gl=ES&ceid=ES:es")
                val conn = cityUrl.openConnection() as java.net.HttpURLConnection
                conn.instanceFollowRedirects = true
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val xml = conn.inputStream.bufferedReader().readText()
                headline = parseRssHeadline(xml, cleanCity)
            } catch (_: Exception) {}

            // 2. Noticia Principal Top del Día a Nivel Nacional
            if (headline == null) {
                try {
                    val nationalUrl = java.net.URL("https://news.google.com/rss?hl=es&gl=ES&ceid=ES:es")
                    val conn = nationalUrl.openConnection() as java.net.HttpURLConnection
                    conn.instanceFollowRedirects = true
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    val xml = conn.inputStream.bufferedReader().readText()
                    headline = parseRssHeadline(xml, cleanCity)
                } catch (_: Exception) {}
            }

            withContext(Dispatchers.Main) {
                if (headline != null) {
                    updateCachedNewsTopic(cleanCity, headline)
                    callback(headline)
                } else {
                    callback(null)
                }
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

actual fun fetchHttpGetApi(url: String, callback: (String?) -> Unit) {
    tourismScope.launch {
        try {
            val u = java.net.URL(url)
            val connection = u.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
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

actual fun testBotVoice(text: String) {}

actual fun requestPinWidget() {}

