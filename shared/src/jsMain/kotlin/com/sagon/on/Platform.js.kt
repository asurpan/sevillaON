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
    if(win.vibratePtt != null) win.vibratePtt()
}

actual fun triggerUiSound(type: String) {
    val win: dynamic = window
    if(win.playUiSound != null) win.playUiSound(type)
}

actual fun getTimeMillis(): Long = kotlin.js.Date.now().toLong()

actual fun getCurrentHour(): Int = kotlin.js.Date().getHours()

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
    
    // 🛡️ REGLA: Si es Android y no está dentro de la App Nativa, redirigir a la App / Google Play
    if (!userAgent.contains("ONAIRSPAINNATIVE") && userAgent.contains("ANDROID") && !currentUrl.contains("forceWeb=true")) {
        try {
            // Desconectar sesión web para evitar duplicados cuando la app nativa se abra
            if (win.app != null) {
                if (win.app.db != null && win.app.sessionID != null) {
                    try { win.app.db.ref("users/" + win.app.sessionID).remove(); } catch(e: dynamic) {}
                    try { win.app.db.goOffline(); } catch(e: dynamic) {}
                }
                if (win.app.peer != null) {
                    try { win.app.peer.destroy(); } catch(e: dynamic) {}
                }
            }

            val playStoreUrl = "https://play.google.com/store/apps/details?id=com.sagon.on&pcampaignid=web_share"
            val search = win.location.search.toString()
            val intentUrl = "intent://open" + search + "#Intent;scheme=onairspain;package=com.sagon.on;S.browser_fallback_url=" + win.encodeURIComponent(playStoreUrl) + ";end"
            win.location.href = intentUrl
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
