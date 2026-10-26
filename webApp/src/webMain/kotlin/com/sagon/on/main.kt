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

    // 🛡️ CREACIÓN INMEDIATA DEL OBJETO APP Y CONTROL DE PESTAÑA ÚNICA
    js("""
        window.app = window.app || { 
            activeCalls: {}, remoteSources: {}, remoteAnalysers: {}, 
            remoteGains: {}, remotePowers: {} 
        };

        // 🛡️ GUARDIÁN DE PESTAÑA ÚNICA (PREVIENE DUPLICADOS Y ECOS)
        var tabId = "tab_" + Math.random().toString(36).substring(2, 10);
        window.currentTabId = tabId;
        window.isDuplicateTab = false;

        function claimTab() {
            localStorage.setItem("active_radio_tab", tabId);
            localStorage.setItem("active_radio_hb", Date.now().toString());
            window.isDuplicateTab = false;
        }

        claimTab();
        setInterval(function() {
            var activeTab = localStorage.getItem("active_radio_tab");
            var lastHb = parseInt(localStorage.getItem("active_radio_hb") || "0", 10);
            if (activeTab === tabId) {
                localStorage.setItem("active_radio_hb", Date.now().toString());
                window.isDuplicateTab = false;
            } else if (Date.now() - lastHb > 4000) {
                claimTab();
            } else {
                window.isDuplicateTab = true;
                if (window.dispatch_duplicate_tab) window.dispatch_duplicate_tab(true);
            }
        }, 1500);

        window.claimTabControl = claimTab;

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

        val voxPttState = remember { mutableStateOf(false) }
        val backPressCountState = remember { mutableStateOf(0) }

        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { },
            onPttSync = { active -> voxPttState.value = active },
            onPttBlocked = { },
            onReplayEmpty = { },
            onReplayStart = { },
            onBack = { backPressCountState.value = backPressCountState.value + 1 },
            onNickConflict = { },
            onUsersUpdate = { users ->
                try {
                    val list = mutableListOf<RemoteUser>()
                    val now = Date.now()
                    val myCityBase = (win.app.currentCity as? String ?: "").split("-")[0]
                    val isInitialLoad = usersNotified.isEmpty()
                    if (users != null) {
                        val keys = js("Object").keys(users)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            val u = users[k] ?: continue
                            val userCityBase = (u.city as? String ?: "").split("-")[0]
                            if (k != win.app.sessionID && myCityBase != userCityBase) continue
                            val isCallActive = (win.app != null && win.app.activeCalls != null && win.app.activeCalls[k] != null)
                            if (now - (u.lastSeen as? Double ?: 0.0) > 45000 && !isCallActive) continue 
                            
                            val isMe = (k == win.app.sessionID)
                            val audioIsTx = if (isMe) (win.app?.isTransmittingInternal == true) else (win.remoteTxStates != null && win.remoteTxStates[k] == true)
                            val isTransmitting = if (isMe) (win.app?.isTransmittingInternal == true) else (u.tx == true || audioIsTx)
                            win.app.remotePowers[k] = (u.pwr as? Double ?: 0.7).toFloat()
                            val prevState = win.remoteTxStates[k] ?: false
                            if (prevState && !isTransmitting && !isMe && u.roger == true) win.playUiSound("rx_off")
                            win.remoteTxStates[k] = isTransmitting
                            var userNick = (u.nick as? String ?: "").trim().uppercase()
                            if (userNick.isEmpty()) userNick = k.split("_")[0].uppercase()
                            if (k != win.app.sessionID && !usersNotified.contains(k)) {
                                usersNotified.add(k)
                                if (!isInitialLoad) {
                                    val isFriend = radioState.value.friends.contains(userNick)
                                    win.playUiSound(if (isFriend) "incoming" else "user_in")
                                    if (isFriend) {
                                        val userCityName = (u.city as? String ?: "tu ciudad").split("-")[0]
                                        showSystemNotification("📻 ¡TU AMIGO SE HA CONECTADO!", "¡$userNick ha entrado en la radio de $userCityName!")
                                    }
                                }
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

        val isDuplicateTabAlert = remember { mutableStateOf(false) }
        win.dispatch_duplicate_tab = { isDup: Boolean -> if (isDup) isDuplicateTabAlert.value = true }

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
                if(win.app != null) { 
                    win.app.voxActive = newState.isVoxEnabled
                    win.app.voxSens = newState.voxSensitivity
                    win.app.rogerEnabled = newState.isRogerBeepEnabled
                    win.app.discreteMode = newState.isDiscreteModeEnabled
                    localStorage.setItem("roger", newState.isRogerBeepEnabled.toString())
                }
                if (win.updateDspSettings != null) {
                    win.updateDspSettings(newState.isDspEnabled, newState.dspLevel)
                }
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
            onBatteryCheckRequest = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.isBatteryOptimized != null) {
                    win.AndroidApp.isBatteryOptimized() as Boolean
                } else false
            },
            onIgnoreBatteryOptimizations = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.requestIgnoreBatteryOptimizations != null) {
                    win.AndroidApp.requestIgnoreBatteryOptimizations()
                }
            },
            onGpsRequest = { it(null) },
            onGpsCityRequest = { it(null) },
            onPlaySound = { if (win.playUiSound != null) win.playUiSound(it) },
            showInstallPrompt = remember {
                val win: dynamic = window
                val ua = (win.navigator?.userAgent as? String ?: "").uppercase()
                ua.contains("ANDROID") && win.AndroidApp == null
            },
            onInstallConfirm = {
                val win: dynamic = window
                // 🛡️ BORRAR Y DESCONECTAR SESIÓN WEB PARA EVITAR DUPLICADOS AL ABRIR LA APP NATIVA
                if (win.app != null) {
                    if (win.app.db != null && win.app.sessionID != null) {
                        try { win.app.db.ref("users/" + win.app.sessionID).remove() } catch (e: dynamic) {}
                        try { win.app.db.goOffline() } catch (e: dynamic) {}
                    }
                    if (win.app.peer != null) {
                        try { win.app.peer.destroy() } catch (e: dynamic) {}
                    }
                }
                val playStoreUrl = "https://play.google.com/store/apps/details?id=com.sagon.on&pcampaignid=web_share"
                val intentUrl = "intent://open#Intent;scheme=onairspain;package=com.sagon.on;S.browser_fallback_url=" + win.encodeURIComponent(playStoreUrl) + ";end"
                win.location.href = intentUrl
            },
            onInstallDismiss = { },
            externalNotification = null,
            externalVolume = 0.7f,
            externalBackPressCount = backPressCountState.value,
            micLevel = micLevelState.value,
            isBeeping = false,
            isCodedRx = false,
            externalPtt = voxPttState.value,
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
            onShareRequest = { city, channel, subtone, proRole, platform, _ ->
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.share != null) {
                    win.AndroidApp.share(city, channel, subtone, proRole)
                } else {
                    val topic = getDailyDebateTopic(city)
                    val cleanCity = city.split("-")[0].uppercase()
                    val url = "https://asurpan.github.io/sevillaON/?city=" + city + "&channel=" + channel + (if (!subtone.isNullOrEmpty()) "&subtone=" + subtone else "") + "&v=57"
                    val shareText = "🎙️ *RADIO Y DEBATE EN DIRECTO — $cleanCity*\n\n" +
                        "💬 *TEMA DE HOY EN $cleanCity:*\n\"$topic\"\n\n" +
                        "📻 *ON AIR SPAIN — Radio CB 27MHz*\n📍 *$city* | 🔊 *CH $channel*\n\n" +
                        "Entra gratis en vivo ahora o únete al debate de las 21:00h:\n" + url
                    val nav = win.navigator
                    if (nav != null && nav.share != null) {
                        val shareData = js("{}")
                        shareData.title = "ON AIR SPAIN"
                        shareData.text = shareText
                        shareData.url = url
                        try {
                            val promise = nav.share(shareData)
                            if (promise != null && promise.catch != null) {
                                promise.catch {
                                    try { nav.clipboard.writeText(shareText) } catch(e: dynamic) {}
                                }
                            }
                        } catch(e: dynamic) {
                            try { nav.clipboard.writeText(shareText) } catch(e: dynamic) {}
                        }
                    } else if (nav != null && nav.clipboard != null) {
                        try { nav.clipboard.writeText(shareText) } catch(e: dynamic) {}
                        if (win.playUiSound != null) win.playUiSound("incoming")
                    }
                }
            },
            onSubtoneChange = { }
        )
    }
}
