package com.sagon.on

/**
 * PROTECTED CORE: PUENTE DE PLATAFORMA (JS/WASM)
 * ESTADO: CONGELADO / NO MODIFICAR
 * - Interfaz con el navegador.
 * - Implementación de RadioAudioEngine (Web Audio API).
 * - Manejo de contextos de audio y permisos del navegador.
 */

import kotlinx.browser.window

class JsPlatform: Platform {
    private val userAgent = window.navigator.userAgent
    override val name: String = userAgent
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun playScanClick() {
    val win: dynamic = window
    if(win.app != null && win.app.ctx != null) { 
        val ctx = win.app.ctx
        
        // --- 🛡️ MEJORA DE PERSISTENCIA (IOS/ANDROID WEB) ---
        // Si no existe el "despertador de audio", lo creamos para que no se corte al apagar la pantalla
        if (win.bgAudio == null) {
            val audio = win.document.createElement("audio")
            audio.loop = true
            // Bucle de silencio casi absoluto para mantener vivo el proceso en iOS
            audio.src = "data:audio/wav;base64,UklGRigAAABXQVZFRm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQQAAAAAAA== "
            win.bgAudio = audio
            
            // Configurar MediaSession para que aparezca en la pantalla de bloqueo (Solo si existe)
            try {
                if (win.navigator.mediaSession != null) {
                    val metadata: dynamic = js("{}")
                    metadata.title = "ON AIR SPAIN"
                    metadata.artist = "Radio en Directo"
                    metadata.album = "Red Nacional"
                    win.navigator.mediaSession.metadata = win.MediaMetadata(metadata)
                }
            } catch(e: dynamic) {}
        }
        
        // Intentar arrancar el audio de fondo en cada interacción para asegurar que iOS lo permite
        try { win.bgAudio.play() } catch(e: dynamic) {}

        val o = ctx.createOscillator()
        val g = ctx.createGain()
        o.type = "square"
        o.frequency.value = 600
        g.gain.setValueAtTime(0.01, ctx.currentTime)
        g.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.02)
        o.connect(g)
        g.connect(ctx.destination)
        o.start()
        o.stop(ctx.currentTime + 0.02)
    }
}

actual fun playIntroMusic() {
    val win: dynamic = window
    win.allowIntroMusic = true
    if (win.introAudio == null) {
        val audio = win.document.createElement("audio")
        audio.src = "emisora.mp3"
        audio.loop = true
        win.introAudio = audio
    }
    try { win.introAudio.play() } catch(e: dynamic) {}
}

actual fun stopIntroMusic() {
    val win: dynamic = window
    win.allowIntroMusic = false
    if (win.introAudio != null) {
        try { win.introAudio.pause() } catch(e: dynamic) {}
    }
}

actual fun playWelcomeSequence() {
    val win: dynamic = window
    if(win.playWelcomeSequence != null) win.playWelcomeSequence()
}

actual fun vibratePtt() {
    val win: dynamic = window
    try {
        if (win.navigator != null && win.navigator.vibrate != null) {
            win.navigator.vibrate(30)
        }
        if (win.AndroidApp != null && win.AndroidApp.vibratePtt != null) {
            win.AndroidApp.vibratePtt()
        }
    } catch(e: dynamic) {}
}

actual fun triggerUiSound(type: String) {
    val win: dynamic = window
    if(win.playUiSound != null) win.playUiSound(type)
}

actual fun getTimeMillis(): Long = kotlin.js.Date.now().toLong()

actual fun getCurrentHour(): Int = kotlin.js.Date().getHours()

actual fun getCurrentFormattedTime(): String {
    val d = kotlin.js.Date()
    val h = d.getHours().toString().padStart(2, '0')
    val m = d.getMinutes().toString().padStart(2, '0')
    return "$h:$m"
}

actual fun setVirtualOperatorText(text: String) {
    js("window.virtualOperatorPendingText = text;")
}

actual fun showSystemNotification(title: String, message: String) {
    val win: dynamic = window
    if (win.AndroidApp != null && win.AndroidApp.showSystemNotification != null) {
        win.AndroidApp.showSystemNotification(title, message)
    } else {
        // Fallback para Web: Notificación nativa del navegador
        js("""
            if (Notification.permission === 'granted') {
                new Notification(title, { body: message });
            }
        """)
    }
}

actual fun tryOpenNativeApp() {
    val win: dynamic = window
    if (win == null || win.navigator == null || win.location == null) return

    val userAgent = win.navigator.userAgent.toString().uppercase()
    val currentUrl = win.location.href.toString()
    
    // 🔒 SI YA HAY UNA SESIÓN DE RADIO CONECTADA O ES LA APP NATIVA, NUNCA DESCONECTAR NI BORRAR
    if (userAgent.contains("ONAIRSPAINNATIVE") || (win.app != null && win.app.sessionID != null)) {
        return
    }

    // 🛡️ REGLA: Si se abre desde un teléfono Android, redirigir DIRECTAMENTE a la Google Play Store
    if (userAgent.contains("ANDROID") && !currentUrl.contains("forceWeb=true")) {
        try {
            val playStoreUrl = "https://play.google.com/store/apps/details?id=com.sagon.on&pcampaignid=web_share"
            win.location.href = playStoreUrl
        } catch(e: dynamic) {}
    }
}

actual fun fetchTourismInfo(city: String, callback: (String?) -> Unit) {
    val url = "https://es.wikipedia.org/api/rest_v1/page/summary/${city.replace(" ", "_")}"
    js("""
        fetch(url)
            .then(response => response.json())
            .then(data => {
                if (data.extract) {
                    callback(data.extract);
                } else {
                    callback(null);
                }
            })
            .catch(error => callback(null));
    """)
}

actual fun fetchLiveNewsTopic(city: String, callback: (String?) -> Unit) {
    val cleanCity = city.split("-")[0].trim().uppercase()
    val url = "https://news.google.com/rss?hl=es&gl=ES&ceid=ES:es"

    js("""
        fetch(url)
            .then(function(response) { return response.text(); })
            .then(function(xmlText) {
                if (xmlText && xmlText.indexOf("<item>") !== -1) {
                    var items = xmlText.split("<item>").slice(1, 10);
                    var chosenHeadline = null;
                    for (var i = 0; i < items.length; i++) {
                        var itemBlock = items[i];
                        var rawTitle = itemBlock.split("<title>")[1].split("</title>")[0];
                        var cleanTitle = rawTitle.split(" - ")[0].replace("<![CDATA[", "").replace("]]>", "").trim();
                        if (cleanTitle.length > 10) {
                            var lower = cleanTitle.toLowerCase();
                            if (lower.indexOf("última hora") !== -1 || lower.indexOf("urgente") !== -1 || lower.indexOf("accidente") !== -1 || lower.indexOf("ave") !== -1 || lower.indexOf("tragedia") !== -1 || lower.indexOf("alerta") !== -1 || lower.indexOf("fallece") !== -1 || lower.indexOf("impacto") !== -1 || lower.indexOf("suceso") !== -1) {
                                chosenHeadline = cleanTitle;
                                break;
                            }
                        }
                    }
                    if (!chosenHeadline && items.length > 0) {
                        var rawTitle = items[0].split("<title>")[1].split("</title>")[0];
                        chosenHeadline = rawTitle.split(" - ")[0].replace("<![CDATA[", "").replace("]]>", "").trim();
                    }
                    if (chosenHeadline) {
                        var formatted = "📰 ÚLTIMA HORA: " + chosenHeadline;
                        callback(formatted);
                        return;
                    }

                }
                callback(null);
            })
            .catch(function(error) { callback(null); });
    """)
}


actual fun fetchGeminiApi(endpoint: String, jsonBody: String, callback: (String?) -> Unit) {
    js("""
        fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonBody
        })
        .then(function(response) { return response.text(); })
        .then(function(text) { callback(text); })
        .catch(function(error) { callback(null); });
    """)
}

actual fun fetchHttpGetApi(url: String, callback: (String?) -> Unit) {
    js("""
        fetch(url, { method: 'GET' })
        .then(function(response) { return response.text(); })
        .then(function(text) { callback(text); })
        .catch(function(error) { callback(null); });
    """)
}

