package com.sagon.on

/**
 * 🔒 WEBAPP ENTRY POINT - ARQUITECTURA MODULAR
 * ESTADO: PROTECTED CORE - VERSIÓN 9.5 (PURE RADIO)
 */

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlin.js.Date

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val win: dynamic = window
    val root = document.getElementById("radio-root") ?: document.body!!
    
    if (win.app_initialized == true) return
    win.app_initialized = true

    // 🛡️ CREACIÓN INMEDIATA DEL OBJETO APP Y DEFINICIÓN DE FUNCIONES CRÍTICAS
    js("""
        window.app = window.app || { 
            activeCalls: {}, remoteSources: {}, remoteAnalysers: {}, 
            remoteGains: {}, remotePowers: {} 
        };

        window.remoteTxStates = {};
        window.connectRadio = function(nick) {
            if (window.app.peer && !window.app.peer.destroyed) {
                if (window.app.peer.disconnected) window.app.peer.reconnect();
                window.app.nick = nick.toUpperCase();
                return;
            }

            var deviceID = localStorage.getItem("web_device_id") || ("web_" + Math.random().toString(36).substring(2, 12));
            localStorage.setItem("web_device_id", deviceID);
            
            var cleanNick = (nick || "RADIO").toString().replace(/[.#${'$'}\[\]]/g, "_").toUpperCase();
            localStorage.setItem("indicativo", cleanNick); 
            
            var sessionID = (cleanNick + "_" + deviceID).trim();
            window.app.nick = cleanNick;
            window.app.deviceID = deviceID;
            window.app.sessionID = sessionID;
            
            console.log("🚀 Iniciando Radio v9.2 (Pure-Link):", sessionID);
            if (window.initAudio) window.initAudio();
            
            if (window.app.db) {
                window.app.db.ref("users/" + sessionID).set({
                    nick: cleanNick,
                    city: localStorage.getItem("lastCity") || "SEVILLA",
                    tx: false,
                    pwr: parseFloat(localStorage.getItem("vetPwr_" + cleanNick)) || 0.7,
                    roger: (localStorage.getItem("roger") === "true"),
                    lastSeen: Date.now()
                });
                window.app.currentCity = localStorage.getItem("lastCity") || "SEVILLA";
                window.app.db.ref("users/" + sessionID).onDisconnect().remove();

                setInterval(function() {
                    if(window.app.db && window.app.sessionID) {
                        window.app.db.ref("users/" + window.app.sessionID).update({ lastSeen: Date.now() });
                    }
                }, 5000);
                if (window.initFirebaseListener) window.initFirebaseListener();
            }
            
            if (typeof Peer !== 'undefined') {
                window.app.peer = new Peer(sessionID, { 
                    secure: true,
                    config: { 
                        'iceServers': [
                            { 'urls': 'stun:stun.l.google.com:19302' },
                            { 'urls': 'stun:stun1.l.google.com:19302' },
                            { 'urls': 'stun:stun2.l.google.com:19302' },
                            { 'urls': 'stun:stun3.l.google.com:19302' },
                            { 'urls': 'stun:stun4.l.google.com:19302' },
                            { 'urls': 'stun:stun.cloudflare.com:3478' },
                            { 'urls': 'turn:openrelay.metered.ca:80', 'username': 'openrelayproject', 'credential': 'openrelayproject' },
                            { 'urls': 'turn:openrelay.metered.ca:443', 'username': 'openrelayproject', 'credential': 'openrelayproject' },
                            { 'urls': 'turn:openrelay.metered.ca:443?transport=tcp', 'username': 'openrelayproject', 'credential': 'openrelayproject' }
                        ],
                        'iceTransportPolicy': 'all',
                        'iceCandidatePoolSize': 10
                    }
                });

                window.app.peer.on('error', function(err) {
                    console.error("🛑 PeerJS Error:", err.type);
                    if (err.type === 'disconnected' || err.type === 'network') {
                        window.app.peer.destroy();
                        setTimeout(function() { window.connectRadio(window.app.nick); }, 3000);
                    }
                });

                window.app.peer.on("call", function(call) {
                    console.log("🚀 PeerJS: Recibiendo de:", call.peer);
                    window.app.activeCalls[call.peer] = call;
                    call.answer(window.getStream());
                    if (window.setupCallStream) window.setupCallStream(call);
                });
            }
        };

        // 🛡️ MEJORA v8.1: Portadora Ultrasónica para mantener el túnel 4G abierto por bi-direccionalidad
        window.getStream = function() { 
            if (window.app.txBus && window.app.txBus.stream && window.app.txBus.stream.getAudioTracks().length > 0) {
                return window.app.txBus.stream;
            }
            if (window.app.ctx) {
                var dest = window.app.ctx.createMediaStreamDestination();
                var osc = window.app.ctx.createOscillator();
                var g = window.app.ctx.createGain();
                osc.frequency.value = 20000; // Inaudible
                g.gain.value = 0.0001; 
                osc.connect(g); g.connect(dest);
                osc.start();
                return dest.stream;
            }
            return null;
        };

        window.establishOutgoingCall = function(id) {
            if (!window.app.peer || window.app.peer.destroyed || window.app.activeCalls[id]) return;
            var stream = window.getStream();
            if (!stream) return;
            console.log("🚀 PeerJS: Llamando a:", id);
            var call = window.app.peer.call(id, stream);
            if (call) {
                window.app.activeCalls[id] = call;
                if (window.setupCallStream) window.setupCallStream(call);
            }
        };
    """)

    RadioNetworkManager.install()
    RadioAudioManager.install()
    RadioMapsManager.install()
    RadioBridge.install()

    ComposeViewport(root) {
        val radioState = remember { mutableStateOf(RadioPersistence.loadInitialState()) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }
        val micLevelState = remember { mutableStateOf(0f) }
        val remoteTransmitterName = remember { mutableStateOf<String?>(null) }
        val usersNotified = remember { mutableSetOf<String>() }

        LaunchedEffect(Unit) {
            while(true) {
                delay(5000)
                val app = window.asDynamic().app
                if (app != null && app.peer != null && !app.peer.destroyed && !app.peer.disconnected) {
                    remoteUsersState.forEach { user ->
                        if (user.id != app.sessionID && app.activeCalls[user.id] == null) {
                            if (app.sessionID < user.id) window.asDynamic().establishOutgoingCall(user.id)
                        }
                    }
                }
            }
        }

        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { },
            onPttSync = { },
            onPttBlocked = { },
            onReplayEmpty = { },
            onReplayStart = { },
            onBack = { },
            onNickConflict = { },
            onUsersUpdate = { users ->
                try {
                    val list = mutableListOf<RemoteUser>()
                    val now = Date.now()
                    val myCityBase = (win.app.currentCity as? String ?: "").split("-")[0]
                    if (users != null) {
                        val keys = js("Object").keys(users)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            val u = users[k] ?: continue
                            val userCityBase = (u.city as? String ?: "").split("-")[0]
                            if (k != win.app.sessionID && myCityBase != userCityBase) continue
                            if (now - (u.lastSeen as? Double ?: 0.0) > 30000) continue 
                            val isTransmitting = u.tx == true
                            win.app.remotePowers[k] = (u.pwr as? Double ?: 0.7).toFloat()
                            val prevState = win.remoteTxStates[k] ?: false
                            if (prevState && !isTransmitting && k != win.app.sessionID && u.roger == true) win.playUiSound("rx_off")
                            win.remoteTxStates[k] = isTransmitting
                            var userNick = (u.nick as? String ?: "").trim().uppercase()
                            if (userNick.isEmpty()) userNick = k.split("_")[0].uppercase()
                            if (k != win.app.sessionID && !usersNotified.contains(k)) {
                                usersNotified.add(k); win.playUiSound(if (radioState.value.friends.contains(userNick)) "incoming" else "user_in")
                            }
                            list.add(RemoteUser(id = k, nick = userNick, isTransmitting = isTransmitting, city = u.city as? String ?: "SEVILLA", channel = u.channel as? String ?: "SEVILLA", txPower = (u.pwr as? Double ?: 0.7).toFloat(), isFriend = radioState.value.friends.contains(userNick), roger = (u.roger == true)))
                        }
                    }
                    remoteUsersState.clear(); remoteUsersState.addAll(list)
                    val activeRemoteTx = list.find { it.isTransmitting && it.id != win.app.sessionID }
                    remoteTransmitterName.value = activeRemoteTx?.nick
                    if (win.app) win.app.rxActiveInternal = (activeRemoteTx != null)
                } catch(e: Exception) { }
            },
            onChatUpdate = { },
            onReplayProgress = { },
            onReplayAvailable = { },
            onChatOpen = { },
            onMicFailure = { },
            onIntegrityStatus = { },
            onIncomingAlert = { _, _, _ -> },
            onNotification = { _, _, _ -> },
            onVoxSync = { },
            onRoomUpdate = { },
            onPttLive = { },
            onVolumeSync = { newVol -> if (win.setMasterVolume != null) win.setMasterVolume(newVol) },
            onDiagRequest = { RadioDiagData() }
        )

        App(
            savedNick = localStorage.getItem("indicativo") ?: "",
            initialState = radioState.value,
            forceInitialScreen = false,
            isFirstTime = false,
            onPermissionRequest = { RadioNetworkManager.connect(it); js("window.ensureMicAccess()"); js("if(window.app) window.app.canPlaySounds = true;") },
            onLogout = { RadioPersistence.logout() },
            onNoiseVolumeChange = { if(win.setNoiseVolume != null) win.setNoiseVolume(it) },
            onMoniVolumeChange = { vol -> if(win.app != null) { win.app.moniVolume = vol; win.app.moniActive = (vol > 0) }; js("if(window.updateMoniGain) window.updateMoniGain();") },
            onEchoChange = { _, _ -> },
            onCityChange = { newCity ->
                if (win.app != null && win.app.db != null && win.app.sessionID != null) {
                    win.app.currentCity = newCity
                    win.app.db.ref("users/" + win.app.sessionID).update(js("{city: newCity, channel: newCity}"))
                    js("if(window.initFirebaseListener) window.initFirebaseListener();")
                }
            },
            onChannelChange = { newCh -> if (win.app != null && win.app.db != null && win.app.sessionID != null) { win.app.db.ref("users/" + win.app.sessionID).update(js("{channel: newCh}")) } },
            onSendMessage = { t, tg -> RadioNetworkManager.sendMessage(t, tg) },
            onDeleteMessage = { id, tg -> RadioNetworkManager.deleteMessage(id, tg) },
            onPrivateChatRequest = { },
            onPublicChatRequest = { },
            onStateSave = { newState -> 
                RadioPersistence.saveState(newState); radioState.value = newState
                if(win.app != null) { win.app.voxActive = newState.isVoxEnabled; win.app.voxSens = newState.voxSensitivity; win.app.rogerEnabled = newState.isRogerBeepEnabled; win.app.discreteMode = newState.isDiscreteModeEnabled; localStorage.setItem("roger", newState.isRogerBeepEnabled.toString()); }
            },
            onConnectRadio = { RadioNetworkManager.connect(it) },
            onDiagRequest = { RadioDiagData() },
            onMicEnable = { a, r, p -> 
                if (a && radioState.value.isDiscreteModeEnabled) { val newState = radioState.value.copy(isDiscreteModeEnabled = false); radioState.value = newState; if (win.app != null) win.app.discreteMode = false }
                RadioAudioManager.setPtt(a, r, p) 
            },
            onReport = { },
            onBlockUser = { },
            onNotificationDismiss = { },
            onReplayRequest = { RadioAudioManager.playReplay() },
            onBatteryCheckRequest = { false },
            onIgnoreBatteryOptimizations = { },
            onGpsRequest = { it(null) },
            onGpsCityRequest = { it(null) },
            onPlaySound = { if (win.playUiSound != null) win.playUiSound(it) },
            showInstallPrompt = false,
            onInstallConfirm = { },
            onInstallDismiss = { },
            externalNotification = null,
            externalVolume = 0.7f,
            externalBackPressCount = 0,
            micLevel = micLevelState.value,
            isBeeping = false,
            isCodedRx = false,
            externalPtt = false,
            externalPttBlocked = false,
            replayProgress = 0f,
            isReplayReady = false,
            remoteUsers = remoteUsersState,
            myId = (win.app?.sessionID as? String) ?: "",
            remoteTransmitterName = remoteTransmitterName.value,
            chatMessages = emptyList(),
            audioIntegrity = true,
            onAntennaTest = { },
            onRequestLocationPermission = { },
            onOpenSettings = { },
            onChatOpenConsumed = { },
            onChatTargetConsumed = { },
            voxActive = radioState.value.isVoxEnabled,
            onExternalExitRequest = { _, _ -> if (win.AndroidApp != null && win.AndroidApp.minimizeApp != null) win.AndroidApp.minimizeApp() },
            externalShowExitConfirm = false,
            onShareRequest = { _, _, _, _, _, _ -> },
            onSubtoneChange = { }
        )
    }
}
