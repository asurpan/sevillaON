package com.sagon.on

import kotlinx.browser.localStorage
import kotlinx.browser.window

/**
 * 🔒 RADIO PERSISTENCE: GESTIÓN DE PREFERENCIAS Y ESTADO LOCAL
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.0 (PURE RADIO)
 */
object RadioPersistence {
    fun loadInitialState(): RadioState {
        return try {
            val params = js("new URLSearchParams(window.location.search)")
            val urlCity = params.get("city")?.toString()
            val urlChannel = params.get("channel")?.toString()
            val urlSubtone = params.get("subtone")?.toString()
            val urlActivity = params.get("activity")?.toString()

            val savedCity = localStorage.getItem("lastCity")
            val finalCity = (urlCity ?: (savedCity ?: "SEVILLA")).trim().uppercase()
            
            val savedChannel = localStorage.getItem("lastChannel")
            val initialChannel = if (urlChannel != null) urlChannel.trim().uppercase() 
                                 else if (savedChannel == null || savedChannel == "") finalCity
                                 else savedChannel.trim().uppercase()

            val profileStr = urlActivity ?: localStorage.getItem("activeProfile")
            val profile = ActivityProfile.entries.find { it.name == profileStr } ?: ActivityProfile.NORMAL

            val win: dynamic = window
            val initialMoniVol = localStorage.getItem("moniVol")?.toFloatOrNull() ?: 0.5f
            
            val savedNick = (localStorage.getItem("indicativo") ?: "").trim()
            val hasMicExplain = localStorage.getItem("hasMicExplain")?.toBoolean() ?: savedNick.isNotBlank()

            // 🛡️ SINCRONIZACIÓN PERSISTENTE CON EL VOLUMEN DEL SISTEMA
            var initialSysVol = localStorage.getItem("sysVol")?.toFloatOrNull()
            if (initialSysVol == null && win.AndroidApp != null && win.AndroidApp.getSystemVolume != null) {
                try {
                    initialSysVol = win.AndroidApp.getSystemVolume() as Float
                } catch(e: Exception) {}
            }
            if (initialSysVol == null) initialSysVol = 0.90f

            val bgStyleStr = localStorage.getItem("bgStyle")
            val bgStyle = BackgroundStyle.entries.find { it.id == bgStyleStr } ?: BackgroundStyle.DEEP_SEA

            RadioState(
                city = finalCity,
                channel = initialChannel,
                subtone = urlSubtone ?: (localStorage.getItem("lastSubtone") ?: "0000"),
                voxSensitivity = localStorage.getItem("voxSens")?.toFloatOrNull() ?: 0.5f,
                monitorVolume = initialMoniVol,
                rfGain = localStorage.getItem("rfGain")?.toFloatOrNull() ?: 0.50f, 
                isRogerBeepEnabled = localStorage.getItem("roger")?.toBoolean() ?: true,
                isVoxEnabled = localStorage.getItem("voxActive")?.toBoolean() ?: false,
                isMonitorEnabled = localStorage.getItem("moniActive")?.toBoolean() ?: false,
                isEcoMode = localStorage.getItem("ecoMode")?.toBoolean() ?: false,
                isInterfaceLocked = localStorage.getItem("isLocked")?.toBoolean() ?: false,
                veteranPower = localStorage.getItem("vetPwr")?.toFloatOrNull() ?: 0.02f,
                isDspEnabled = localStorage.getItem("dspEnabled")?.toBoolean() ?: true,
                isBotEnabled = localStorage.getItem("botEnabled")?.toBoolean() ?: true,
                botGender = localStorage.getItem("botGender") ?: "MALE",
                botSpeed = localStorage.getItem("botSpeed")?.toFloatOrNull() ?: 0.95f,
                botPitch = localStorage.getItem("botPitch")?.toFloatOrNull() ?: 0.90f,
                activeProfile = profile,
                isDiscreteModeEnabled = localStorage.getItem("disMode") == "true",
                isChatMuted = localStorage.getItem("chatMuted") == "true",
                isNotificationsEnabled = localStorage.getItem("notifsActive")?.toBoolean() ?: true,
                squelch = localStorage.getItem("squelch")?.toFloatOrNull() ?: 0.90f,
                systemVolume = initialSysVol,
                hasAcceptedMicExplain = hasMicExplain,
                backgroundStyle = bgStyle
            )
        } catch(e: Exception) { RadioState() }
    }

    fun saveState(s: RadioState) {
        val win: dynamic = window
        val normCity = s.city.trim().uppercase()
        var normCh = s.channel.trim().uppercase()
        
        try {
            val myNick = (localStorage.getItem("indicativo") ?: "").trim().uppercase()
            if (normCh == myNick || normCh == "") normCh = normCity

            localStorage.setItem("lastCity", normCity)
            localStorage.setItem("lastChannel", normCh)
            localStorage.setItem("lastSubtone", s.subtone)
            localStorage.setItem("voxSens", s.voxSensitivity.toString())
            localStorage.setItem("moniVol", s.monitorVolume.toString())
            localStorage.setItem("rfGain", s.rfGain.toString())
            localStorage.setItem("roger", s.isRogerBeepEnabled.toString())
            localStorage.setItem("voxActive", s.isVoxEnabled.toString())
            localStorage.setItem("moniActive", s.isMonitorEnabled.toString())
            localStorage.setItem("dspEnabled", s.isDspEnabled.toString())
            localStorage.setItem("botEnabled", s.isBotEnabled.toString())
            localStorage.setItem("botGender", s.botGender)
            localStorage.setItem("botSpeed", s.botSpeed.toString())
            localStorage.setItem("botPitch", s.botPitch.toString())
            localStorage.setItem("disMode", s.isDiscreteModeEnabled.toString())
            localStorage.setItem("chatMuted", s.isChatMuted.toString())
            localStorage.setItem("notifsActive", s.isNotificationsEnabled.toString())
            localStorage.setItem("activeProfile", s.activeProfile.name)
            localStorage.setItem("squelch", s.squelch.toString())
            localStorage.setItem("sysVol", s.systemVolume.toString())
            localStorage.setItem("hasMicExplain", s.hasAcceptedMicExplain.toString())
            localStorage.setItem("bgStyle", s.backgroundStyle.id)
        } catch(e: Exception) {}

        if (win.app != null) {
            win.app.currentCity = normCity
            win.app.currentChannel = normCh
            win.app.voxActive = s.isVoxEnabled
            win.app.voxSens = s.voxSensitivity
            win.app.moniActive = s.isMonitorEnabled
            win.app.moniVolume = s.monitorVolume
            win.app.rfGain = s.rfGain
            win.app.squelch = s.squelch
            win.app.rogerEnabled = s.isRogerBeepEnabled
            win.app.isBotEnabled = s.isBotEnabled
            win.app.botGender = s.botGender
            win.app.botSpeed = s.botSpeed
            win.app.botPitch = s.botPitch
            
            // 🛡️ ACTUALIZACIÓN DE VOLUMEN MAESTRO Y ANDROID STREAM
            if (win.setMasterVolume != null) {
                win.setMasterVolume(s.systemVolume)
            }
            if (win.AndroidApp != null && win.AndroidApp.setSystemVolume != null) {
                try { win.AndroidApp.setSystemVolume(s.systemVolume) } catch(e: Exception) {}
            }
            
            js("if(window.updateMoniGain) window.updateMoniGain();")
            js("if(window.updateMasterVolume) window.updateMasterVolume();")
            js("if(window.updateDspSettings) window.updateDspSettings(s.isDspEnabled, s.dspLevel);")

            val sessId = win.app.sessionID as? String
            if (win.app.db != null && !sessId.isNullOrBlank() && sessId != "undefined" && !sessId.contains("undefined")) {
                val updates: dynamic = js("{}")
                updates.city = normCity
                updates.channel = normCh
                updates.roger = s.isRogerBeepEnabled 
                updates.blocks = s.blockedUsers.joinToString(",")
                win.app.db.ref("users/" + sessId).update(updates)
            }
        }
    }

    fun logout() {
        val win: dynamic = window
        try {
            val hwID = if (win.AndroidApp != null && win.AndroidApp.getAndroidId != null) win.AndroidApp.getAndroidId() as? String else localStorage.getItem("web_device_id")
            val sessId = win.app?.sessionID as? String
            if (win.app != null && win.app.db != null) {
                if (!sessId.isNullOrBlank() && sessId != "undefined" && !sessId.contains("undefined")) {
                    win.app.db.ref("users/" + sessId).remove()
                }
                if (hwID != null && hwID != "") {
                    win.app.db.ref("registered_devices/" + hwID).remove()
                    win.app.db.ref("devices/" + hwID).remove()
                }
            }
        } catch(e: Exception) {}
        localStorage.clear()
        js("window.location.reload();")
    }
}
