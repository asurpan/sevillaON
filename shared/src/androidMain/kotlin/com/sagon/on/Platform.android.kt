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
            val cleanCity = city.split("-")[0].trim().uppercase()
            // 📰 RSS DE ALTO IMPACTO (SUCESOS, POLÉMICAS, CRIMEN, ÚLTIMA HORA Y DEBATE)
            val url = java.net.URL("https://news.google.com/rss/search?q=sucesos+crimen+polemica+actualidad+ultima+hora&hl=es&gl=ES&ceid=ES:es")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            val xmlText = connection.inputStream.bufferedReader().readText()
            
            var bestHeadline: String? = null
            var highestScore = -1
            
            if (xmlText.contains("<item>")) {
                val items = xmlText.split("<item>").drop(1).take(25)
                val impactKeywords = listOf(
                    "última hora", "urgente", "crimen", "asesinato", "detenido", "policía", 
                    "tribunal", "juez", "prisión", "robo", "agresión", "droga", "tiroteo", 
                    "incendio", "polémica", "dimisión", "escándalo", "multa", "condena", 
                    "crisis", "alquiler", "vivienda", "huelga", "protesta", "fraude", 
                    "investigado", "accidente", "tragedia", "alerta", "fallece", "suceso"
                )
                
                for (itemBlock in items) {
                    val rawTitle = itemBlock.substringAfter("<title>").substringBefore("</title>")
                    val cleanTitle = rawTitle.substringBefore(" - ")
                        .replace("<![CDATA[", "")
                        .replace("]]>", "")
                        .replace("&amp;", "&")
                        .trim()
                        
                    if (cleanTitle.length > 12) {
                        val lower = cleanTitle.lowercase()
                        var score = 0
                        for (kw in impactKeywords) {
                            if (lower.contains(kw)) score += 2
                        }
                        if (cleanTitle.length in 15..90) score += 1
                        
                        if (score > highestScore) {
                            highestScore = score
                            bestHeadline = cleanTitle
                        }
                    }
                }
                
                if (bestHeadline == null && items.isNotEmpty()) {
                    val rawTitle = items[0].substringAfter("<title>").substringBefore("</title>")
                    bestHeadline = rawTitle.substringBefore(" - ").replace("<![CDATA[", "").replace("]]>", "").trim()
                }
            }

            withContext(Dispatchers.Main) {
                if (bestHeadline != null) {
                    val formatted = "📰 TEMA CANDENTE EN EL DEBATE: $bestHeadline"
                    updateCachedNewsTopic(cleanCity, formatted)
                    callback(formatted)
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

