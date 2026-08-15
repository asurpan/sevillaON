package com.sagon.on

/**
 * 🔒 WEBAPP ENTRY POINT - ARQUITECTURA MODULAR
 * ESTADO: PROTECTED CORE - VERSIÓN 5.0 (CLEAN SPLIT)
 * 
 * Este archivo inicializa la aplicación y coordina los managers.
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.browser.localStorage
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
            currentCity: 'SEVILLA', currentChannel: 'SEVILLA'
        };

        window.sanitizePath = function(s) { 
            return (s ? s.toString().replace(/[.${'$'}#[\]/]/g, "_") : "unknown"); 
        };

        window.remoteTxStates = {};
        window.connectRadio = function(nick) {
            var sessionID = nick + "_" + Math.random().toString(36).substring(2, 11);
            window.app.nick = nick;
            window.app.sessionID = sessionID;
            if (window.initAudio) window.initAudio();
            
            if (window.app.db) {
                window.app.db.ref("users/" + sessionID).set({
                    nick: nick,
                    city: localStorage.getItem("lastCity") || "SEVILLA",
                    channel: localStorage.getItem("lastChannel") || "SEVILLA",
                    tx: false,
                    lastSeen: Date.now()
                });
                if (window.initFirebaseListener) window.initFirebaseListener();
            }
            
            if (typeof Peer !== 'undefined') {
                window.app.peer = new Peer(sessionID, { secure: true });
                window.app.peer.on("call", function(call) {
                    window.app.activeCalls[call.peer] = call;
                    call.answer(window.getStream());
                    if (window.setupCallStream) window.setupCallStream(call);
                });
            }
        };

        window.getStream = function() {
            return (window.app.txBus) ? window.app.txBus.stream : null;
        };

        window.establishOutgoingCall = function(id) {
            if (!window.app.peer || window.app.activeCalls[id]) return;
            var call = window.app.peer.call(id, window.getStream());
            if (call) {
                window.app.activeCalls[id] = call;
                if (window.setupCallStream) window.setupCallStream(call);
            }
        };

        window.setupSystemListeners = function() {
            window.addEventListener('popstate', function(event) {
                history.pushState(null, document.title, location.href);
                if(window.trigger_back) window.trigger_back();
            });
            history.pushState(null, document.title, location.href);
        };
    """)

    // 🏗️ INSTALACIÓN DE MOTORES (En orden de dependencia)
    RadioPersistence.loadInitialState() // Carga previa
    RadioNetworkManager.install()
    RadioAudioManager.install()
    RadioMapsManager.install()
    RadioBridge.install()

    ComposeViewport(root) {
        val initialState = remember { RadioPersistence.loadInitialState() }
        
        // ESTADOS REACTIVOS GLOBALES
        val micLevelState = remember { mutableStateOf(0f) }
        val isBeepingState = remember { mutableStateOf(false) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }
        val chatMessagesState = remember { mutableStateListOf<ChatMessage>() }
        val isPttLiveState = remember { mutableStateOf(false) }
        
        val notificationState = remember { mutableStateOf<AppNotification?>(null) }

        // --- 📡 CONFIGURACIÓN DEL PUENTE DE EVENTOS ---
        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { isBeepingState.value = it },
            onPttSync = { },
            onPttBlocked = { },
            onReplayEmpty = { },
            onReplayStart = { },
            onBack = { },
            onNickConflict = { },
            onUsersUpdate = { users ->
                try {
                    val list = mutableListOf<RemoteUser>()
                    if (users != null && users != undefined) {
                        val keys = js("Object").keys(users)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            val u = users[k] ?: continue
                            
                            val isTransmitting = u.tx == true
                            
                            // 📡 DETECCIÓN DE FIN DE TRANSMISIÓN REMOTA (ROGER BEEP ENTRANTE)
                            val prevState = win.remoteTxStates[k] ?: false
                            if (prevState == true && isTransmitting == false) {
                                if (win.playUiSound) win.playUiSound("rx_off")
                            }
                            win.remoteTxStates[k] = isTransmitting

                            list.add(RemoteUser(
                                id = k, 
                                nick = u.nick as? String ?: "ESTACIÓN", 
                                isTransmitting = isTransmitting,
                                city = "FRECUENCIA ÚNICA",
                                channel = "DEBUG",
                                txPower = (u.pwr as? Double ?: 0.7).toFloat()
                            ))
                            
                            if (k != win.app.sessionID && win.app.activeCalls[k] == null) {
                                win.establishOutgoingCall(k)
                            }
                        }
                    }
                    remoteUsersState.clear()
                    remoteUsersState.addAll(list)
                } catch(e: Exception) { }
            },
            onChatUpdate = { data ->
                val nl = mutableListOf<ChatMessage>()
                if (data != null && data != undefined) {
                    val ks = js("Object").keys(data)
                    for (i in 0 until (ks.length as Int)) {
                        val k = ks[i] as String
                        val m = data[k] ?: continue
                        nl.add(ChatMessage(k, m.senderNick?.toString() ?: "???", m.text?.toString() ?: "", m.timestamp?.toString()?.toDouble()?.toLong() ?: 0L))
                    }
                }
                chatMessagesState.clear()
                chatMessagesState.addAll(nl.sortedBy { it.timestamp })
            },
            onReplayProgress = { },
            onReplayAvailable = { },
            onChatOpen = { },
            onMicFailure = { },
            onIntegrityStatus = { },
            onBgStation = { },
            onBgGenreChange = { },
            onIncomingAlert = { _, _, _ -> },
            onVoxSync = { },
            onNasaImage = { _, _, _ -> },
            onDgtUpdate = { _, _ -> },
            onCodeCaptured = { _, _ -> },
            onWifiListReceived = { },
            onEngineeringFinished = { },
            onRouteSuggestions = { },
            onPoiResults = { },
            onRouteInfo = { _, _, _ -> },
            onNavigationStep = { },
            onPttLive = { isPttLiveState.value = it }
        )

        // --- 📻 RENDERIZADO DE LA APP ---
        App(
            savedNick = localStorage.getItem("indicativo") ?: "",
            initialState = initialState,
            isFirstTime = false,
            onOnboardingFinish = { },
            onPermissionRequest = { RadioNetworkManager.connect(it) },
            onLogout = { RadioPersistence.logout() },
            onInstallRequest = { },
            externalShowExitConfirm = false,
            onExternalExitRequest = { _, _ -> },
            onShareRequest = { _, _, _, _, _, _ -> },
            onNoiseVolumeChange = { vol -> 
                js("if(window.setNoiseVolume) window.setNoiseVolume(vol);")
            },
            onMoniVolumeChange = { vol ->
                js("if(window.app) window.app.moniVolume = vol;")
                js("if(window.updateMasterVolume) window.updateMasterVolume();")
            },
            onEchoChange = { _, _ -> },
            onCityChange = { },
            onSubtoneChange = { },
            onChannelChange = { },
            onSendMessage = { t, tg -> RadioNetworkManager.sendMessage(t, tg) },
            onDeleteMessage = { id, tg -> RadioNetworkManager.deleteMessage(id, tg) },
            onPrivateChatRequest = { },
            onPublicChatRequest = { },
            onStateSave = { RadioPersistence.saveState(it) },
            onConnectRadio = { RadioNetworkManager.connect(it) },
            onMicEnable = { a, r, p -> RadioAudioManager.setPtt(a, r, p) },
            onReport = { },
            onBlockUser = { },
            onNotificationDismiss = { },
            onNotificationPermissionRequest = { },
            onReplayRequest = { RadioAudioManager.playReplay() },
            onBatteryCheckRequest = { false },
            onIgnoreBatteryOptimizations = { },
            onGpsRequest = { it(null) },
            onGpsCityRequest = { it(null) },
            onPlaySound = { },
            showInstallPrompt = false,
            onInstallConfirm = { },
            onInstallDismiss = { },
            externalNotification = notificationState.value,
            micLevel = micLevelState.value,
            isBeeping = isBeepingState.value,
            isCodedRx = false,
            externalPtt = isPttLiveState.value,
            externalPttBlocked = false,
            replayProgress = 0f,
            isReplayReady = false,
            remoteUsers = remoteUsersState,
            remoteTransmitterName = null,
            chatMessages = chatMessagesState,
            forceInitialScreen = false,
            audioIntegrity = true,
            bgStationName = null,
            onAntennaTest = { },
            onBgRadioScan = { _, _ -> },
            onBgRadioStop = { },
            onBgVolumeChange = { },
            onGetWifiVariance = { 0f },
            onGetHeading = { 0f },
            onExecuteEngineeringAction = { },
            onWifiListReceived = { },
            onWifiAuthResultReceived = { },
            onEngineeringFinished = { },
            onRouteSuggestionsReceived = { },
            onPoiResultsReceived = { },
            onWaypointReceived = { },
            onRequestLocationPermission = { },
            onOpenSettings = { },
            onChatOpenConsumed = { },
            onChatTargetConsumed = { },
            voxActive = false,
            wifiVerificationResult = null,
            nasaImageUrl = null,
            nasaImageTitle = null,
            nasaImageExplanation = null,
            routeDistanceKm = null,
            routeDurationMin = null,
            routeDestinationName = null,
            nextNavigationStep = null,
            routeWaypoints = emptyList(),
            onBgGenreConsumed = { },
            onBgGenreChangeExternal = { },
            onDgtUpdate = { },
            dgtText = null,
            dgtImageUrl = null
        )
    }
}
