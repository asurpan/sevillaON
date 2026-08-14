package com.sagon.on

/**
 * 🔒 CLEAN ARCHITECTURE REFACTOR - VERSIÓN 4.4
 * ESTADO: PROTECTED CORE - ARQUITECTURA LIMPIA Y DESACOPLADA
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.delay
import kotlinx.browser.window
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlin.js.Date

// =======================================================
// 1. RADIO PERSISTENCE: GESTIÓN DE PREFERENCIAS
// =======================================================
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

// =======================================================
// 2. RADIO NETWORK MANAGER: FIREBASE & CHAT
// =======================================================
object RadioNetworkManager {
    fun install() {
        js("""
            var _p1 = "AIza"; var _p2 = "SyBA7tMb"; var _p3 = "cvbrl2lt"; var _p4 = "Tweqydmk7"; var _p5 = "PRfk-R7fWw";
            var cfg = { 
                apiKey: _p1+_p2+_p3+_p4+_p5, 
                authDomain: "sevilla-on-200b3.firebaseapp.com", 
                databaseURL: "https://sevilla-on-200b3-default-rtdb.europe-west1.firebasedatabase.app",
                projectId: "sevilla-on-200b3" 
            };
            if (typeof firebase !== 'undefined' && firebase.initializeApp && !firebase.apps.length) firebase.initializeApp(cfg);
            window.app = window.app || {};
            window.app.db = (typeof firebase !== 'undefined' && typeof firebase.database === 'function') ? firebase.database() : null;

            window.initFirebaseListener = function() {
                if (window.app.db) {
                    window.app.db.ref("users").on('value', function(s) { if(window.update_remote_users) window.update_remote_users(s.val()); });
                    var mySafeNick = window.sanitizePath(localStorage.getItem("indicativo") || "");
                    if (mySafeNick) {
                        window.app.db.ref("inbox/" + mySafeNick).on('value', function(snapshot) {
                            var val = snapshot.val();
                            if (val && val.timestamp > (Date.now() - 10000)) {
                                if (window.playUiSound) window.playUiSound('click');
                                window.app.db.ref("inbox/" + mySafeNick).remove();
                            }
                        });
                    }
                }
            };

            window.updateChatListener = function(target) {
                if (!window.app || !window.app.db) return;
                var nick = localStorage.getItem("indicativo") || "ANÓNIMO";
                var city = localStorage.getItem("lastCity") || "SEVILLA";
                var channel = localStorage.getItem("lastChannel") || city;
                var chatPath = target ? ("private_messages/" + window.sanitizePath([nick, target].sort()[0]) + "_" + window.sanitizePath([nick, target].sort()[1])) 
                                      : ("messages/" + window.sanitizePath(city) + "/" + window.sanitizePath(channel));
                if (window.currentChatRef) try { window.currentChatRef.off(); } catch(e) {}
                window.currentChatRef = window.app.db.ref(chatPath).limitToLast(50);
                window.currentChatRef.on('value', function(snapshot) { if (window.dispatch_chat_update) window.dispatch_chat_update(snapshot.val()); });
            };
        """)
    }

    fun connect(nick: String) { js("window.connectRadio(nick);") }

    fun sendMessage(text: String, target: String?) {
        val win: dynamic = window
        val nick = (localStorage.getItem("indicativo") ?: "ANÓNIMO").trim().uppercase()
        val city = localStorage.getItem("lastCity") ?: "SEVILLA"
        val channel = localStorage.getItem("lastChannel") ?: city
        val chatPath = if (target != null) "private_messages/${win.sanitizePath(listOf(nick, target).sorted()[0])}_${win.sanitizePath(listOf(nick, target).sorted()[1])}"
                       else "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
        if (win.app?.db != null) {
            val m: dynamic = js("{}"); m.senderNick = nick; m.text = text; m.timestamp = Date.now()
            win.app.db.ref(chatPath).push(m)
        }
    }

    fun deleteMessage(msgId: String, target: String?) {
        val win: dynamic = window
        val nick = (localStorage.getItem("indicativo") ?: "ANÓNIMO").trim().uppercase()
        val city = localStorage.getItem("lastCity") ?: "SEVILLA"
        val channel = localStorage.getItem("lastChannel") ?: city
        val chatPath = if (target != null) "private_messages/${win.sanitizePath(listOf(nick, target).sorted()[0])}_${win.sanitizePath(listOf(nick, target).sorted()[1])}"
                       else "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
        if (win.app?.db != null) win.app.db.ref("$chatPath/$msgId").remove()
    }
}

// =======================================================
// 3. RADIO AUDIO MANAGER: MOTOR DE SONIDO & WEBRTC
// =======================================================
object RadioAudioManager {
    fun install() {
        js("""
            window.app = window.app || {};
            window.initAudio = function() {
                if (window.app.ctx) return;
                var AC = window.AudioContext || window.webkitAudioContext;
                window.app.ctx = new AC();
                window.app.masterOut = window.app.ctx.createGain();
                window.app.masterOut.connect(window.app.ctx.destination);
                window.app.masterRxGain = window.app.ctx.createGain();
                window.app.masterRxGain.gain.value = 2.0;
                window.app.compressor = window.app.ctx.createDynamicsCompressor();
                window.app.filter = window.app.ctx.createBiquadFilter();
                window.app.filter.type = "bandpass"; window.app.filter.frequency.value = 1600; window.app.filter.Q.value = 0.5;
                window.app.filter.connect(window.app.masterRxGain);
                window.app.masterRxGain.connect(window.app.compressor);
                window.app.compressor.connect(window.app.masterOut);
                window.app.noise = window.app.ctx.createGain();
                window.app.noise.connect(window.app.compressor);
                window.app.txBus = window.app.ctx.createMediaStreamDestination();
                window.app.txGate = window.app.ctx.createGain();
                window.app.txGate.connect(window.app.txBus);
            };
            window.broadcastPTT = function(active, roger, power) {
                if(!window.app || !window.app.db) return;
                if (window.app.pttStateInternal === active) return;
                window.app.pttStateInternal = active;
                window.app.isTransmittingInternal = active;
                if(window.dispatch_ptt_live) window.dispatch_ptt_live(active);
                if (active) {
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.01);
                } else {
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                    if (roger && window.playUiSound) window.playUiSound("ptt_off");
                }
            };
        """)
        RadioSignaling.install(); VOXEngine.install(); MoniGuard.install()
    }
    fun setPtt(active: Boolean, roger: Boolean, power: Float?) { js("window.broadcastPTT(active, roger, power);") }
    fun playReplay() { js("window.playReplay();") }
}

// --- SUB-MÓDULOS ---
object RadioSignaling { fun install() { js("""window.playUiSound = function(type) { if(!window.app.ctx) return; var o = window.app.ctx.createOscillator(); var g = window.app.ctx.createGain(); o.type = "sine"; o.frequency.value = (type === "ptt_on" ? 880 : 440); g.gain.value = 0.05; o.connect(g); g.connect(window.app.masterOut); o.start(); o.stop(window.app.ctx.currentTime + 0.1); };""") } }
object VOXEngine { fun install() { js("""function voxLoop() { if(window.app && window.app.micAnalyser) { var d = new Uint8Array(window.app.micAnalyser.fftSize); window.app.micAnalyser.getByteTimeDomainData(d); var max = 0; for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>max) max=v; } var level = Math.min(1.0, (max/128)*6.5); if(window.dispatch_mic) window.dispatch_mic(level); if(window.app.voxActive && level > (1.0-(window.app.voxSens*0.99)) && !window.app.isTransmittingInternal) { if(!window.app.isVoxTransmitting) { window.broadcastPTT(true, true); window.app.isVoxTransmitting=true; if(window.dispatch_vox_sync) window.dispatch_vox_sync(true); } window.app.voxHangTimer=60; } else if(window.app.isVoxTransmitting) { if(window.app.voxHangTimer>0) window.app.voxHangTimer--; else { window.app.isVoxTransmitting=false; window.broadcastPTT(false, true); if(window.dispatch_vox_sync) window.dispatch_vox_sync(false); } } } requestAnimationFrame(voxLoop); } voxLoop();""") } }
object MoniGuard { fun install() { js("""window.updateMoniGain = function() { if(window.app && window.app.moniGainNode) window.app.moniGainNode.gain.value = (window.app.moniActive ? 0.5 : 0); }; window.updateMasterVolume = function() { if(window.app && window.app.masterOut) window.app.masterOut.gain.value = (window.app.rfGain * 2); };""") } }
object RadioCore { fun install() { js("""window.sanitizePath = function(s) { return (s ? s.toString().replace(/[.${'$'}#[\]/]/g, "_") : "unknown"); }; window.connectRadio = function(nick) { var sessionID = nick + "_" + Math.random().toString(36).substring(2, 11); window.app.nick = nick; window.app.sessionID = sessionID; window.initAudio(); if (window.app.db) { window.app.db.ref("users/" + sessionID).set({ nick: nick, tx: false }); window.initFirebaseListener(); } window.app.peer = new Peer(sessionID, { secure: true }); window.app.peer.on("call", function(call) { call.answer(window.app.txBus.stream); }); };""") } }

// --- 🗺️ RADIO BRIDGE: DISPATCHERS ---
object RadioBridge {
    fun setupDispatchers(win: dynamic, onMic: (Float) -> Unit, onBeep: (Boolean) -> Unit, onPttSync: (Boolean) -> Unit, onPttBlocked: () -> Unit, onReplayEmpty: () -> Unit, onReplayStart: () -> Unit, onBack: () -> Unit, onNickConflict: (String) -> Unit, onUsersUpdate: (dynamic) -> Unit, onChatUpdate: (dynamic) -> Unit, onReplayProgress: (Float) -> Unit, onReplayAvailable: (Boolean) -> Unit, onChatOpen: (String?) -> Unit, onMicFailure: () -> Unit, onIntegrityStatus: (Boolean) -> Unit, onBgStation: (String?) -> Unit, onBgGenreChange: (String) -> Unit, onIncomingAlert: (String, String, String) -> Unit, onVoxSync: (Boolean) -> Unit, onNasaImage: (String?, String?, String?) -> Unit, onDgtUpdate: (String?, String?) -> Unit, onCodeCaptured: (String, String) -> Unit, onWifiListReceived: (String) -> Unit, onEngineeringFinished: () -> Unit, onRouteSuggestions: (String) -> Unit, onPoiResults: (String) -> Unit, onRouteInfo: (String?, String?, String?) -> Unit, onNavigationStep: (String?) -> Unit, onPttLive: (Boolean) -> Unit) {
        win.dispatch_mic = onMic; win.dispatch_beeping = onBeep; win.dispatch_ptt_sync = onPttSync; win.dispatch_ptt_blocked = onPttBlocked; win.dispatch_replay_empty = onReplayEmpty; win.dispatch_replay_start = onReplayStart; win.trigger_back = onBack; win.dispatch_nick_conflict = onNickConflict; win.update_remote_users = onUsersUpdate; win.dispatch_chat_update = onChatUpdate; win.dispatch_replay_progress = onReplayProgress; win.dispatch_replay_available = onReplayAvailable; win.dispatch_chat_open = onChatOpen; win.dispatch_mic_failure = onMicFailure; win.dispatch_integrity_status = onIntegrityStatus; win.dispatch_bg_station = onBgStation; win.dispatch_bg_genre_change = onBgGenreChange; win.dispatch_vox_sync = onVoxSync; win.dispatch_nasa_image = onNasaImage; win.dispatch_dgt_update = onDgtUpdate; win.dispatch_code_captured = onCodeCaptured; win.dispatch_wifi_list = onWifiListReceived; win.dispatch_engineering_finished = onEngineeringFinished; win.dispatch_route_suggestions = onRouteSuggestions; win.dispatch_poi_results = onPoiResults; win.dispatch_ptt_live = onPttLive; win.dispatch_route_info = onRouteInfo; win.dispatch_navigation_step = onNavigationStep; win.dispatch_incoming_alert = onIncomingAlert
    }
}

// =======================================================
// 🔒 WEBAPP ENTRY POINT
// =======================================================
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val win: dynamic = js("window"); val root = document.getElementById("radio-root") ?: document.body!!
    if (win.app_initialized == true) return; win.app_initialized = true
    RadioCore.install(); RadioAudioManager.install(); RadioNetworkManager.install()
    win.establishOutgoingCall = { id: String -> if(win.app.peer) win.app.peer.call(id, win.app.txBus.stream) }

    ComposeViewport(root) {
        val initialState = remember { RadioPersistence.loadInitialState() }
        val micLevelState = remember { mutableStateOf(0f) }; val isBeepingState = remember { mutableStateOf(false) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }; val chatMessagesState = remember { mutableStateListOf<ChatMessage>() }
        val isPttLiveState = remember { mutableStateOf(false) }

        RadioBridge.setupDispatchers(win, { micLevelState.value = it }, { isBeepingState.value = it }, { _ -> }, { }, { }, { }, { }, { _ -> }, { users ->
            val list = mutableListOf<RemoteUser>()
            if (users != null && users != undefined) {
                val keys = js("Object").keys(users)
                for (i in 0 until (keys.length as Int)) {
                    val k = keys[i] as String; val u = users[k] ?: continue
                    // 🛡️ NUCLEAR FIX: VISIBILIDAD GLOBAL SIN FILTROS
                    list.add(RemoteUser(k, u.nick as? String ?: "ESTACIÓN", u.tx == true, "0000", "FRECUENCIA ÚNICA", "DEBUG", false, 0.8f, (u.pwr as? Double ?: 0.7).toFloat(), u.proRole as? String ?: "CIUDADANO"))
                    if (k != win.app.sessionID && win.app.activeCalls[k] == null) win.establishOutgoingCall(k)
                }
            }
            remoteUsersState.clear(); remoteUsersState.addAll(list)
        }, { data ->
            val nl = mutableListOf<ChatMessage>()
            if (data != null && data != undefined) {
                val ks = js("Object").keys(data)
                for (i in 0 until (ks.length as Int)) {
                    val k = ks[i] as String; val m = data[k] ?: continue
                    nl.add(ChatMessage(k, m.senderNick?.toString() ?: "???", m.text?.toString() ?: "", m.timestamp?.toString()?.toDouble()?.toLong() ?: 0L))
                }
            }
            chatMessagesState.clear(); chatMessagesState.addAll(nl.sortedBy { it.timestamp })
        }, { _ -> }, { _ -> }, { _ -> }, { }, { _ -> }, { _ -> }, { _ -> }, { _, _, _ -> }, { _ -> }, { _, _, _ -> }, { _, _ -> }, { _, _ -> }, { _ -> }, { }, { _ -> }, { _ -> }, { _, _, _ -> }, { _ -> }, { isPttLiveState.value = it })

        App(
            savedNick = localStorage.getItem("indicativo") ?: "", initialState = initialState, isFirstTime = false,
            onOnboardingFinish = { }, onPermissionRequest = { RadioNetworkManager.connect(it) }, onLogout = { RadioPersistence.logout() },
            onInstallRequest = { }, externalShowExitConfirm = false, onExternalExitRequest = { _, _ -> },
            onShareRequest = { _, _, _, _, _, _ -> }, onNoiseVolumeChange = { }, onMoniVolumeChange = { }, onEchoChange = { _, _ -> },
            onCityChange = { }, onSubtoneChange = { }, onChannelChange = { }, onSendMessage = { t, tg -> RadioNetworkManager.sendMessage(t, tg) },
            onDeleteMessage = { id, tg -> RadioNetworkManager.deleteMessage(id, tg) }, onPrivateChatRequest = { }, onPublicChatRequest = { },
            onStateSave = { RadioPersistence.saveState(it) }, onConnectRadio = { RadioNetworkManager.connect(it) }, onMicEnable = { a, r, p -> RadioAudioManager.setPtt(a, r, p) },
            onReport = { }, onNotificationDismiss = { }, onNotificationPermissionRequest = { }, onReplayRequest = { RadioAudioManager.playReplay() },
            onBatteryCheckRequest = { false }, onIgnoreBatteryOptimizations = { }, onGpsRequest = { it(null) }, onGpsCityRequest = { it(null) },
            onPlaySound = { }, showInstallPrompt = false, onInstallConfirm = { }, onInstallDismiss = { }, externalNotification = null,
            externalBackPressCount = 0, micLevel = micLevelState.value, isBeeping = isBeepingState.value, isCodedRx = false,
            externalPtt = false, externalPttBlocked = false, replayProgress = 0f, isReplayReady = false, remoteUsers = remoteUsersState,
            remoteTransmitterName = null, chatMessages = chatMessagesState, forceInitialScreen = false, audioIntegrity = true,
            bgStationName = null, onAntennaTest = { }, onBgRadioScan = { _, _ -> }, onBgRadioStop = { }, onBgVolumeChange = { },
            onGetWifiVariance = { 0f }, onGetHeading = { 0f }, onExecuteEngineeringAction = { }, onWifiListReceived = { },
            onWifiAuthResultReceived = { }, onEngineeringFinished = { }, onRequestLocationPermission = { }, onOpenSettings = { },
            onChatOpenConsumed = { }, onChatTargetConsumed = { }, voxActive = false, wifiVerificationResult = null,
            nasaImageUrl = null, nasaImageTitle = null, nasaImageExplanation = null, routeDistanceKm = null,
            routeDurationMin = null, routeDestinationName = null, nextNavigationStep = null
        )
    }
}
