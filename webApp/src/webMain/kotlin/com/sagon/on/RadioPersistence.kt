package com.sagon.on

import kotlinx.browser.localStorage
import kotlinx.browser.window

/**
 * 🔒 RADIO PERSISTENCE: GESTIÓN DE PREFERENCIAS Y ESTADO LOCAL
 */
object RadioPersistence {
    fun loadInitialState(): RadioState {
        return try {
            val params = js("new URLSearchParams(window.location.search)")
            val urlCity = params.get("city")?.toString()
            val urlChannel = params.get("channel")?.toString()
            val urlSubtone = params.get("subtone")?.toString()
            val urlPro = params.get("pro")?.toString()
            val urlNasa = params.get("nasa")?.toString()
            val urlActivity = params.get("activity")?.toString()
            val urlImg = params.get("img")?.toString()

            val savedCity = localStorage.getItem("lastCity")
            val finalCity = (urlCity ?: (savedCity ?: "SEVILLA")).trim().uppercase()
            
            val savedChannel = localStorage.getItem("lastChannel")
            val initialChannel = if (urlChannel != null) urlChannel.trim().uppercase() 
                                 else if (savedChannel == null || savedChannel == "") finalCity
                                 else savedChannel.trim().uppercase()

            RadioState(
                city = finalCity,
                channel = initialChannel,
                subtone = urlSubtone ?: (localStorage.getItem("lastSubtone") ?: "0000"),
                isWorkModeActive = urlPro == "true",
                forceShowNasa = urlNasa == "true",
                activeProfile = if (urlActivity == "true") ActivityProfile.MOTO else ActivityProfile.NORMAL,
                routeImage = urlImg,
                voxSensitivity = localStorage.getItem("voxSens")?.toFloatOrNull() ?: 0.5f,
                monitorVolume = localStorage.getItem("moniVol")?.toFloatOrNull() ?: 0.5f,
                rfGain = localStorage.getItem("rfGain")?.toFloatOrNull() ?: 0.5f,
                isRogerBeepEnabled = localStorage.getItem("roger")?.toBoolean() ?: true,
                isVoxEnabled = localStorage.getItem("voxActive")?.toBoolean() ?: false,
                isMonitorEnabled = localStorage.getItem("moniActive")?.toBoolean() ?: false,
                isEcoMode = localStorage.getItem("ecoMode")?.toBoolean() ?: false,
                isInterfaceLocked = localStorage.getItem("isLocked")?.toBoolean() ?: false,
                isAntennaTesting = localStorage.getItem("antTest")?.toBoolean() ?: false,
                isSystemVoiceEnabled = localStorage.getItem("systemVoice")?.toBoolean() ?: false,
                veteranPower = localStorage.getItem("vetPwr")?.toFloatOrNull() ?: 0.7f,
                isAvoidingHighways = localStorage.getItem("avoid_highways")?.toBoolean() ?: false,
                favoriteChannels = (localStorage.getItem("favoriteChannels") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                friends = (localStorage.getItem("friends") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                blockedUsers = (localStorage.getItem("blockedUsers") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                isDspEnabled = localStorage.getItem("dspEnabled")?.toBoolean() ?: true,
                bgRadioGenre = localStorage.getItem("bgGenre") ?: "MIX",
                isDiscreteModeEnabled = localStorage.getItem("disMode") == "true",
                nasaImageUrl = localStorage.getItem("cache_nasa_img"),
                nasaImageTitle = localStorage.getItem("cache_nasa_title"),
                nasaImageExplanation = localStorage.getItem("cache_nasa_desc")
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
            localStorage.setItem("bgGenre", s.bgRadioGenre)
            localStorage.setItem("disMode", s.isDiscreteModeEnabled.toString())
            localStorage.setItem("activeProfile", s.activeProfile.name)
            s.nasaImageUrl?.let { localStorage.setItem("cache_nasa_img", it) }
        } catch(e: Exception) {}

        if (win.app != null) {
            win.app.currentCity = normCity
            win.app.currentChannel = normCh
            win.app.voxActive = s.isVoxEnabled
            win.app.voxSens = s.voxSensitivity
            win.app.moniActive = s.isMonitorEnabled
            win.app.moniVolume = s.monitorVolume
            win.app.rfGain = s.rfGain
            
            js("if(window.updateMoniGain) window.updateMoniGain();")
            js("if(window.updateMasterVolume) window.updateMasterVolume();")
            if (s.isDspEnabled) js("if(window.updateDspSettings) window.updateDspSettings(true);")
            else js("if(window.updateDspSettings) window.updateDspSettings(false);")

            if (win.app.db != null && win.app.sessionID != null) {
                val updates: dynamic = js("{}")
                updates.city = normCity
                updates.channel = normCh
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
