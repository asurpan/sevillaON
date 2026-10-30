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

    win.getGeminiRadioResponse = { city: String, nick: String, msg: dynamic, isBeacon: Boolean, callback: (String) -> Unit ->
        val safeMsg = msg?.toString()
        GeminiRadioService.processRadioMessage(city, nick, safeMsg, isBeacon) { response ->
            callback(response)
        }
    }

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

        window.checkHardwareRestore = function(onRestored) {
            var hwID = (window.AndroidApp && window.AndroidApp.getAndroidId) ? window.AndroidApp.getAndroidId() : localStorage.getItem("web_device_id");
            if (!hwID || !window.app || !window.app.db) return;
            window.app.db.ref("registered_devices/" + hwID).once("value").then(function(snapshot) {
                var val = snapshot.val();
                if (val && val.nick && val.nick.trim() !== "") {
                    var restoredNick = val.nick.trim().toUpperCase();
                    console.log("📻 Auto-Restauración Hardware via registered_devices:", restoredNick);
                    localStorage.setItem("indicativo", restoredNick);
                    if (window.dispatch_hardware_restored) {
                        window.dispatch_hardware_restored(restoredNick);
                    }
                    if (onRestored) onRestored(restoredNick);
                } else {
                    var savedLocalNick = localStorage.getItem("indicativo");
                    if (savedLocalNick && savedLocalNick.trim() !== "") {
                        var localNickClean = savedLocalNick.trim().toUpperCase();
                        window.app.db.ref("registered_devices/" + hwID).set({
                            nick: localNickClean,
                            deviceId: hwID,
                            updatedAt: Date.now()
                        });
                        if (window.dispatch_hardware_restored) {
                            window.dispatch_hardware_restored(localNickClean);
                        }
                        if (onRestored) onRestored(localNickClean);
                    }
                }
            });
        };

        window.remoteTxStates = {};
        window.connectRadio = function(nick) {
            var hwID = (window.AndroidApp && window.AndroidApp.getAndroidId) ? window.AndroidApp.getAndroidId() : null;
            var deviceID = hwID || localStorage.getItem("web_device_id") || ("web_" + Math.random().toString(36).substring(2, 12));
            localStorage.setItem("web_device_id", deviceID);

            function doConnect(finalNick) {
                if (window.app.peer && !window.app.peer.destroyed) {
                    if (window.app.peer.disconnected) window.app.peer.reconnect();
                    window.app.nick = finalNick.toUpperCase();
                    return;
                }

                var cleanNick = (finalNick || "RADIO").toString().replace(/[.#${'$'}\[\]]/g, "_").toUpperCase();
                localStorage.setItem("indicativo", cleanNick); 
                if (window.dispatch_hardware_restored) {
                    window.dispatch_hardware_restored(cleanNick);
                }
                
                var sessionID = (cleanNick + "_" + deviceID).trim();
                window.app.nick = cleanNick;
                window.app.deviceID = deviceID;
                window.app.sessionID = sessionID;
                
                console.log("🚀 Iniciando Radio (Pure-Link):", sessionID);
                if (window.initAudio) window.initAudio();
                
                if (window.app.db) {
                    window.app.db.ref("users/" + sessionID).set({
                        nick: cleanNick,
                        deviceId: deviceID,
                        city: localStorage.getItem("lastCity") || "SEVILLA",
                        tx: false,
                        pwr: parseFloat(localStorage.getItem("vetPwr_" + cleanNick)) || 0.02,
                        roger: (localStorage.getItem("roger") === "true"),
                        lastSeen: Date.now()
                    });
                    window.app.db.ref("registered_devices/" + deviceID).set({
                        nick: cleanNick,
                        deviceId: deviceID,
                        updatedAt: Date.now()
                    });
                    window.app.currentCity = localStorage.getItem("lastCity") || "SEVILLA";
                    window.app.db.ref("users/" + sessionID).onDisconnect().remove();

                    setInterval(function() {
                        if(window.app.db && window.app.sessionID) {
                            var updates = { lastSeen: Date.now() };
                            if (window.app.pttStateInternal) {
                                var currentNick = window.app.nick || "RADIO";
                                var oldPwr = parseFloat(localStorage.getItem("vetPwr_" + currentNick)) || 0.02;
                                var oldWatt = Math.floor(oldPwr * 50);
                                if (oldPwr < 1.0) {
                                    var newPwr = Math.min(1.0, oldPwr + 0.00000833);
                                    var newWatt = Math.floor(newPwr * 50);
                                    localStorage.setItem("vetPwr_" + currentNick, newPwr.toString());
                                    localStorage.setItem("vetPwr", newPwr.toString());
                                    updates.pwr = newPwr;
                                    
                                    // ⚡ GAMIFICACIÓN / RECOMPENSA DOPAMINA AL SUBIR DE VATIO
                                    if (newWatt > oldWatt && newWatt <= 50) {
                                        if (window.dispatch_notification) {
                                            window.dispatch_notification("⚡ ¡AUMENTO DE POTENCIA!", "¡Felicidades! Tu emisora ha alcanzado los " + newWatt + " Watts de potencia.", "success");
                                        }
                                        if (window.playUiSound) window.playUiSound("roger");
                                    }
                                }
                            }
                            window.app.db.ref("users/" + window.app.sessionID).update(updates);
                        }
                    }, 5000);
                    if (window.initFirebaseListener) window.initFirebaseListener();
                    if (window.updateChatListener) window.updateChatListener(null);
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
            }

            // 🛡️ HARD-LOCK: SI EL DISPOSITIVO YA TIENE NICK REGISTRADO EN FIREBASE, FORZAR ESE NICK
            if (window.app && window.app.db) {
                window.app.db.ref("registered_devices/" + deviceID).once("value").then(function(snapshot) {
                    var val = snapshot.val();
                    var chosenNick = nick;
                    if (val && val.nick && val.nick.trim() !== "") {
                        chosenNick = val.nick.trim().toUpperCase();
                        console.log("🔒 HARDWARE BLOQUEADO - Forzando Nick registrado:", chosenNick);
                    }
                    doConnect(chosenNick);
                }).catch(function() {
                    doConnect(nick);
                });
            } else {
                doConnect(nick);
            }
        };

        // 🛡️ MEJORA v8.1: Portadora Ultrasónica para mantener el túnel 4G abierto por bi-direccionalidad
        window.getStream = function() { 
            if (window.app.txBus && window.app.txBus.stream && window.app.txBus.stream.getAudioTracks().length > 0) {
                return window.app.txBus.stream;
            }
            if (window.app.ctx) {
                if (!window.app.dummyStream) {
                    var dest = window.app.ctx.createMediaStreamDestination();
                    var osc = window.app.ctx.createOscillator();
                    var g = window.app.ctx.createGain();
                    osc.frequency.value = 20000; // Inaudible
                    g.gain.value = 0.0001; 
                    osc.connect(g); g.connect(dest);
                    osc.start();
                    window.app.dummyStream = dest.stream;
                }
                return window.app.dummyStream;
            }
            return null;
        };

        window.failedPeerAttempts = window.failedPeerAttempts || {};
        window.establishOutgoingCall = function(id) {
            if (!window.app.peer || window.app.peer.destroyed || window.app.activeCalls[id]) return;
            if (window.failedPeerAttempts[id] && Date.now() < window.failedPeerAttempts[id]) return;
            var stream = window.getStream();
            if (!stream) return;
            console.log("🚀 PeerJS: Llamando a:", id);
            try {
                var call = window.app.peer.call(id, stream);
                if (call) {
                    window.app.activeCalls[id] = call;
                    if (window.setupCallStream) window.setupCallStream(call);
                }
            } catch(e) {
                window.failedPeerAttempts[id] = Date.now() + 30000;
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

        val communityMutedSet = remember { mutableSetOf<String>() }
        win.dispatch_community_mute = { mutedNick: String ->
            if (mutedNick.isNotEmpty() && !communityMutedSet.contains(mutedNick)) {
                communityMutedSet.add(mutedNick)
                val updatedBlocks = radioState.value.blockedUsers + mutedNick
                radioState.value = radioState.value.copy(blockedUsers = updatedBlocks)
                showSystemNotification(
                    "⚠️ SANCIONADO POR LA COMUNIDAD",
                    "El usuario $mutedNick ha sido silenciado automáticamente tras recibir 3 reportes de portadora."
                )
                if (win.playUiSound != null) win.playUiSound("alert")
            }
        }

        LaunchedEffect(Unit) {
            js("if (window.checkHardwareRestore) window.checkHardwareRestore(function(n) { if (n && window.dispatch_hardware_restored) window.dispatch_hardware_restored(n); });")
            js("""
                if (window.app && window.app.db) {
                    window.app.db.ref("reports").on("value", function(snapshot) {
                        var data = snapshot.val();
                        if (!data) return;
                        var now = Date.now();
                        var nicks = Object.keys(data);
                        nicks.forEach(function(targetNick) {
                            var userReports = data[targetNick];
                            if (!userReports) return;
                            var reporters = Object.keys(userReports);
                            var recentCount = 0;
                            reporters.forEach(function(rId) {
                                var time = userReports[rId];
                                if (now - time < 300000) recentCount++;
                            });
                            if (recentCount >= 3) {
                                if (window.dispatch_community_mute) {
                                    window.dispatch_community_mute(targetNick);
                                }
                            }
                        });
                    });
                }
            """)
            while(true) {
                delay(5000)
                val app = window.asDynamic().app
                if (app != null && app.peer != null && !app.peer.destroyed && !app.peer.disconnected) {
                    remoteUsersState.forEach { user ->
                        if (user.id != app.sessionID && app.activeCalls[user.id] == null) {
                            window.asDynamic().establishOutgoingCall(user.id)
                        }
                    }
                }
            }
        }

        val voxPttState = remember { mutableStateOf(false) }
        val backPressCountState = remember { mutableStateOf(0) }
        val isReplayReadyState = remember { mutableStateOf(false) }
        val replayProgressState = remember { mutableStateOf(0f) }
        val chatMessagesState = remember { mutableStateListOf<ChatMessage>() }

        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { },
            onPttSync = { active -> voxPttState.value = active },
            onPttBlocked = { },
            onReplayEmpty = { },
            onReplayStart = { },
            onBack = { backPressCountState.value = backPressCountState.value + 1 },
            onNickConflict = { conflictingNick ->
                showSystemNotification("⚠️ INDICATIVO DUPLICADO", "El indicativo $conflictingNick ya está activo en otro terminal. Por favor elige uno diferente.")
                localStorage.removeItem("indicativo")
                RadioPersistence.logout()
            },
            onUsersUpdate = { users ->
                try {
                    val list = mutableListOf<RemoteUser>()
                    val now = Date.now()
                    val myCityBase = (win.app.currentCity as? String ?: "").split("-")[0]
                    val isInitialLoad = usersNotified.isEmpty()
                    val myNick = (win.app.nick as? String ?: "").trim().uppercase()
                    val mySessionID = (win.app.sessionID as? String ?: "")

                    if (users != null) {
                        val keys = js("Object").keys(users)

                        // 🛡️ VERIFICACIÓN DE INDICATIVO ÚNICO EN RED (NO DUPLICADOS ACTIVOS)
                        var hasConflict = false
                        if (myNick.isNotEmpty() && mySessionID.isNotEmpty()) {
                            for (i in 0 until (keys.length as Int)) {
                                val k = keys[i] as String
                                val u = users[k] ?: continue
                                val otherNick = (u.nick as? String ?: "").trim().uppercase()
                                val lastSeen = (u.lastSeen as? Double ?: 0.0)
                                if (k != mySessionID && otherNick == myNick && (now - lastSeen < 45000)) {
                                    hasConflict = true
                                    win.dispatch_nick_conflict(myNick)
                                    break
                                }
                            }
                        }

                        if (!hasConflict) {
                            for (i in 0 until (keys.length as Int)) {
                                val k = keys[i] as String
                                val u = users[k] ?: continue
                                val userCityBase = (u.city as? String ?: "").split("-")[0]
                                val userSubtone = (u.subtone as? String ?: "0000")
                                val mySubtone = radioState.value.subtone
                                if (k != win.app.sessionID && (myCityBase != userCityBase || mySubtone != userSubtone)) continue
                                val isCallActive = (win.app != null && win.app.activeCalls != null && win.app.activeCalls[k] != null)
                                if (now - (u.lastSeen as? Double ?: 0.0) > 45000 && !isCallActive) continue 
                                
                                val isMe = (k == win.app.sessionID)
                                var isTransmitting = if (isMe) (win.app?.isTransmittingInternal == true) else (u.tx == true)
                                
                                // 🛡️ HARD-LOCK: CORTAFUEGOS MÁXIMO DE 60s PARA TRANSMISIONES REMOTAS
                                if (win.remoteTxStartTimes == null) win.remoteTxStartTimes = js("{}")
                                if (isTransmitting && !isMe) {
                                    val txStart = (win.remoteTxStartTimes[k] as? Double ?: 0.0)
                                    if (txStart == 0.0) {
                                        win.remoteTxStartTimes[k] = now
                                    } else if (now - txStart > 60000.0) {
                                        isTransmitting = false
                                        win.remoteTxStartTimes[k] = 0.0
                                    }
                                } else {
                                    win.remoteTxStartTimes[k] = 0.0
                                }

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
                    }
                    remoteUsersState.clear(); remoteUsersState.addAll(list)
                    val activeRemoteTx = list.find { it.isTransmitting && it.id != win.app.sessionID }
                    if (activeRemoteTx != null) {
                        remoteTransmitterName.value = activeRemoteTx.nick
                    } else if (remoteTransmitterName.value?.startsWith("BASE ") != true && remoteTransmitterName.value?.startsWith("CONTROL ") != true) {
                        remoteTransmitterName.value = null
                    }
                    if (win.app != null) win.app.rxActiveInternal = (activeRemoteTx != null || remoteTransmitterName.value != null)
                } catch(e: Exception) { }
            },
            onChatUpdate = { rawData ->
                try {
                    val list = mutableListOf<ChatMessage>()
                    if (rawData != null) {
                        val keys = js("Object").keys(rawData)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            val m = rawData[k] ?: continue
                            val sender = (m.senderNick as? String ?: "ANÓNIMO").trim().uppercase()
                            val text = (m.text as? String ?: "").trim()
                            val ts = (m.timestamp as? Double ?: 0.0).toLong()
                            val isSys = (m.isSystem as? Boolean ?: false)
                            if (text.isNotEmpty()) {
                                list.add(ChatMessage(id = k, senderNick = sender, text = text, timestamp = ts, isSystem = isSys))
                            }
                        }
                        list.sortBy { it.timestamp }
                    }
                    val previousCount = chatMessagesState.size
                    chatMessagesState.clear()
                    chatMessagesState.addAll(list)
                    if (list.size > previousCount && list.isNotEmpty()) {
                        val lastMsg = list.last()
                        val myNick = (win.app?.nick as? String ?: "").trim().uppercase()
                        if (lastMsg.senderNick != myNick && !lastMsg.isSystem) {
                            if (win.playUiSound != null) win.playUiSound("chat_msg")
                            showSystemNotification("💬 MENSAJE DE " + lastMsg.senderNick, lastMsg.text)
                        }
                    }
                } catch (e: Exception) {}
            },
            onReplayProgress = { progress -> replayProgressState.value = progress },
            onReplayAvailable = { available -> isReplayReadyState.value = available },
            onChatOpen = { },
            onMicFailure = { },
            onIntegrityStatus = { },
            onIncomingAlert = { _, _, _ -> },
            onNotification = { _, _, _ -> },
            onVoxSync = { },
            onRoomUpdate = { },
            onPttLive = { },
            onVolumeSync = { newVol -> if (win.setMasterVolume != null) win.setMasterVolume(newVol) },
            onDiagRequest = { RadioDiagData() },
            onIsRoomEmpty = {
                val mySessionId = (win.app?.sessionID as? String ?: "")
                remoteUsersState.none { it.id != mySessionId }
            },
            onControlActive = { name, active ->
                if (active) {
                    remoteTransmitterName.value = name
                } else {
                    if (remoteTransmitterName.value?.startsWith("BASE ") == true || remoteTransmitterName.value?.startsWith("CONTROL ") == true) {
                        remoteTransmitterName.value = null
                    }
                }
            }
        )

        val isDuplicateTabAlert = remember { mutableStateOf(false) }
        win.dispatch_duplicate_tab = { isDup: Boolean -> if (isDup) isDuplicateTabAlert.value = true }

        val savedNickState = remember { mutableStateOf(localStorage.getItem("indicativo") ?: "") }
        win.dispatch_hardware_restored = { restoredNick: String -> 
            if (restoredNick.isNotEmpty()) savedNickState.value = restoredNick 
        }

        App(
            savedNick = savedNickState.value,
            initialState = radioState.value,
            forceInitialScreen = false,
            isFirstTime = false,
            isDuplicateTab = isDuplicateTabAlert.value,
            onClaimTab = { 
                isDuplicateTabAlert.value = false
                js("if(window.claimTabControl) window.claimTabControl();")
            },
            onPermissionRequest = { RadioNetworkManager.connect(it); js("window.ensureMicAccess()"); js("if(window.app) window.app.canPlaySounds = true;") },
            onLogout = { RadioPersistence.logout() },
            onNoiseVolumeChange = { if(win.setNoiseVolume != null) win.setNoiseVolume(it) },
            onMoniVolumeChange = { vol -> if(win.app != null) { win.app.moniVolume = vol; win.app.moniActive = (vol > 0) }; js("if(window.updateMoniGain) window.updateMoniGain();") },
            onEchoChange = { _, _ -> },
            onCityChange = { newCity ->
                RadioAudioManager.clearReplay()
                js("if(window.app) { window.app.suppressRxOff = true; window.app.suppressRoger = true; if(window.app.pttStateInternal && window.broadcastPTT) window.broadcastPTT(false, false); }")
                js("setTimeout(function() { if(window.app) { window.app.suppressRxOff = false; window.app.suppressRoger = false; } }, 800);")
                js("window.remoteTxStates = {}")
                js("if(window.AndroidApp && window.AndroidApp.stopSpeak) try{ window.AndroidApp.stopSpeak(); }catch(e){}")
                js("if('speechSynthesis' in window) try{ window.speechSynthesis.cancel(); }catch(e){}")
                js("if(window.app) window.app.isControlSpeaking = false;")
                js("if(window.dispatch_control_active) try{ window.dispatch_control_active(null, false); }catch(e){}")

                if (win.app != null && win.app.db != null && win.app.sessionID != null) {
                    win.app.currentCity = newCity
                    win.app.db.ref("users/" + win.app.sessionID).update(js("{city: newCity, channel: newCity}"))
                    js("if(window.initFirebaseListener) window.initFirebaseListener();")
                }
            },
            onChannelChange = { newCh ->
                RadioAudioManager.clearReplay()
                js("if(window.app) { window.app.suppressRxOff = true; window.app.suppressRoger = true; if(window.app.pttStateInternal && window.broadcastPTT) window.broadcastPTT(false, false); }")
                js("setTimeout(function() { if(window.app) { window.app.suppressRxOff = false; window.app.suppressRoger = false; } }, 800);")
                js("window.remoteTxStates = {}")
                js("if(window.AndroidApp && window.AndroidApp.stopSpeak) try{ window.AndroidApp.stopSpeak(); }catch(e){}")
                js("if('speechSynthesis' in window) try{ window.speechSynthesis.cancel(); }catch(e){}")
                js("if(window.app) window.app.isControlSpeaking = false;")
                js("if(window.dispatch_control_active) try{ window.dispatch_control_active(null, false); }catch(e){}")

                if (win.app != null && win.app.db != null && win.app.sessionID != null) {
                    win.app.db.ref("users/" + win.app.sessionID).update(js("{channel: newCh}"))
                }
            },

            onSendMessage = { t, tg -> RadioNetworkManager.sendMessage(t, tg) },
            onDeleteMessage = { id, tg -> RadioNetworkManager.deleteMessage(id, tg) },
            onPrivateChatRequest = { },
            onPublicChatRequest = { },
            onStateSave = { newState -> 
                if (radioState.value != newState) {
                    val wasDiscrete = radioState.value.isDiscreteModeEnabled
                    radioState.value = newState
                    RadioPersistence.saveState(newState)
                    if(win.app != null) { 
                        win.app.voxActive = newState.isVoxEnabled
                        win.app.voxSens = newState.voxSensitivity
                        win.app.rogerEnabled = newState.isRogerBeepEnabled
                        win.app.isBotEnabled = newState.isBotEnabled
                        win.app.discreteMode = newState.isDiscreteModeEnabled
                        localStorage.setItem("roger", newState.isRogerBeepEnabled.toString())
                        if (newState.isDiscreteModeEnabled && !wasDiscrete) {
                            js("if(window.AndroidApp && window.AndroidApp.stopSpeak) try{ window.AndroidApp.stopSpeak(); }catch(e){}")
                            js("if('speechSynthesis' in window) try{ window.speechSynthesis.cancel(); }catch(e){}")
                            js("if(window.app) window.app.isControlSpeaking = false;")
                            js("if(window.dispatch_control_active) try{ window.dispatch_control_active(null, false); }catch(e){}")
                        }
                    }
                    if (win.updateDspSettings != null) {
                        win.updateDspSettings(newState.isDspEnabled, newState.dspLevel)
                    }
                }
            },
            onConnectRadio = { nick ->
                RadioNetworkManager.connect(nick)
                js("if(window.app) window.app.isRadioActive = true;")
            },
            onDiagRequest = { RadioDiagData() },
            onMicEnable = { a, r, p -> 
                if (a && radioState.value.isDiscreteModeEnabled) { val newState = radioState.value.copy(isDiscreteModeEnabled = false); radioState.value = newState; if (win.app != null) win.app.discreteMode = false }
                RadioAudioManager.setPtt(a, r, p) 
            },
            onReport = { targetNick ->
                val win: dynamic = window
                if (win.app != null && win.app.db != null && targetNick.isNotEmpty()) {
                    val sanitizedTarget = js("targetNick.replace(/[.#${'$'}\\[\\]]/g, '_')") as String
                    val reporterId = js("(win.app.sessionID || 'anon').replace(/[.#${'$'}\\[\\]]/g, '_')") as String
                    win.app.db.ref("reports/" + sanitizedTarget + "/" + reporterId).set(Date.now())
                }
            },
            onBlockUser = { },
            onNotificationDismiss = { },
            onReplayRequest = { RadioAudioManager.playReplay() },
            onBatteryCheckRequest = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.isBatteryOptimized != null) {
                    win.AndroidApp.isBatteryOptimized() as Boolean
                } else false
            },
            onBatteryStatusRequest = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.getBatteryCheckStatus != null) {
                    win.AndroidApp.getBatteryCheckStatus() as? String ?: "{}"
                } else "{}"
            },
            onIgnoreBatteryOptimizations = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.requestIgnoreBatteryOptimizations != null) {
                    win.AndroidApp.requestIgnoreBatteryOptimizations()
                }
            },
            onOpenNotifSettings = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.openNotificationSettings != null) {
                    win.AndroidApp.openNotificationSettings()
                }
            },
            onOpenAutoStartSettings = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.openAutoStartSettings != null) {
                    win.AndroidApp.openAutoStartSettings()
                }
            },
            onGpsRequest = { it(null) },
            onGpsCityRequest = { it(null) },
            onPlaySound = { if (win.playUiSound != null) win.playUiSound(it) },
            showInstallPrompt = remember {
                val win: dynamic = window
                val ua = (win.navigator?.userAgent as? String ?: "").uppercase()
                val isWebView = ua.contains("WV") || ua.contains("ANDROIDAPP") || win.AndroidApp != null
                val isAndroidBrowser = ua.contains("ANDROID") && !isWebView
                val isIos = ua.contains("IPHONE") || ua.contains("IPAD") || ua.contains("IPOD")
                
                // Solo mostrar banner de instalación en un navegador Android externo (NO en la App Nativa ni en iPhone)
                isAndroidBrowser && !isIos
            },
            onInstallConfirm = {
                val win: dynamic = window
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
            replayProgress = replayProgressState.value,
            isReplayReady = isReplayReadyState.value,
            remoteUsers = remoteUsersState,
            myId = (win.app?.sessionID as? String) ?: "",
            remoteTransmitterName = remoteTransmitterName.value,
            chatMessages = chatMessagesState,
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
                    val url = "https://asurpan.github.io/sevillaON/?city=" + city + "&channel=" + channel + "&v=100"
                    val shareText = "🎙️ *DEBATE EN VIVO — $cleanCity*\n\n" +
                        "💬 \"$topic\"\n\n" + url
                    val nav = win.navigator
                    if (nav != null && nav.share != null) {
                        val shareData = js("{}")
                        shareData.title = "ON AIR GLOBAL"
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
            onSubtoneChange = { newSub ->
                if (win.app != null && win.app.db != null && win.app.sessionID != null) {
                    val subVal = newSub
                    win.app.db.ref("users/" + win.app.sessionID).update(js("{subtone: subVal}"))
                    js("if(window.updateChatListener) window.updateChatListener(null);")
                }
            }
        )
    }
}
