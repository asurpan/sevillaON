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
            var initialMoniVol = localStorage.getItem("moniVol")?.toFloatOrNull() ?: 0.5f
            
            // 🛡️ SINCRONIZACIÓN CON EL VOLUMEN DEL SISTEMA (SÓLO SI ES LA PRIMERA CARGA)
            if (win.AndroidApp != null && win.AndroidApp.getSystemVolume != null) {
                try {
                    initialMoniVol = win.AndroidApp.getSystemVolume() as Float
                } catch(e: Exception) {}
            }

            RadioState(
                city = finalCity,
                channel = initialChannel,
                subtone = urlSubtone ?: (localStorage.getItem("lastSubtone") ?: "0000"),
                voxSensitivity = localStorage.getItem("voxSens")?.toFloatOrNull() ?: 0.5f,
                monitorVolume = initialMoniVol,
                rfGain = localStorage.getItem("rfGain")?.toFloatOrNull() ?: 0.5f, 
                isRogerBeepEnabled = localStorage.getItem("roger")?.toBoolean() ?: true,
                isVoxEnabled = localStorage.getItem("voxActive")?.toBoolean() ?: false,
                isMonitorEnabled = localStorage.getItem("moniActive")?.toBoolean() ?: false,
                isEcoMode = localStorage.getItem("ecoMode")?.toBoolean() ?: false,
                isInterfaceLocked = localStorage.getItem("isLocked")?.toBoolean() ?: false,
                veteranPower = localStorage.getItem("vetPwr")?.toFloatOrNull() ?: 0.7f,
                isDspEnabled = localStorage.getItem("dspEnabled")?.toBoolean() ?: true,
                activeProfile = profile,
                isDiscreteModeEnabled = localStorage.getItem("disMode") == "true",
                squelch = localStorage.getItem("squelch")?.toFloatOrNull() ?: 0.55f 
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
            localStorage.setItem("disMode", s.isDiscreteModeEnabled.toString())
            localStorage.setItem("activeProfile", s.activeProfile.name)
            localStorage.setItem("squelch", s.squelch.toString())
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
            
            js("if(window.updateMoniGain) window.updateMoniGain();")
            js("if(window.updateMasterVolume) window.updateMasterVolume();")
            if (s.isDspEnabled) js("if(window.updateDspSettings) window.updateDspSettings(true);")
            else js("if(window.updateDspSettings) window.updateDspSettings(false);")

            if (win.app.db != null && win.app.sessionID != null) {
                val updates: dynamic = js("{}")
                updates.city = normCity
                updates.channel = normCh
                updates.roger = s.isRogerBeepEnabled 
                updates.blocks = s.blockedUsers.joinToString(",")
                win.app.db.ref("users/" + win.app.sessionID).update(updates)
            }
        }
    }

    fun logout() {
        localStorage.clear()
        js("""if(window.app && window.app.db && window.app.sessionID) { window.app.db.ref("users/" + window.app.sessionID).remove(); }""")
        js("window.location.reload();")
    }
}
