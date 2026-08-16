package com.sagon.on

/**
 * 🔒 WEBAPP ENTRY POINT - ARQUITECTURA MODULAR
 * ESTADO: PROTECTED CORE - VERSIÓN 7.0 (PURE RADIO)
 */

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
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
            remoteGains: {}, remotePowers: {} 
        };

        window.remoteTxStates = {};
        window.connectRadio = function(nick) {
            // 🛡️ IDENTIFICADOR DE DISPOSITIVO PERSISTENTE (WEB HARDWARE ID)
            var deviceID = localStorage.getItem("web_device_id");
            if (!deviceID) {
                deviceID = "web_" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
                localStorage.setItem("web_device_id", deviceID);
            }
            
            var sessionID = nick + "_" + deviceID.substring(4, 12);
            window.app.nick = nick;
            window.app.deviceID = deviceID;
            window.app.sessionID = sessionID;
            if (window.initAudio) window.initAudio();
            
            var baseCity = localStorage.getItem("lastCity") || "SEVILLA";
            
            if (window.app.db) {
                window.app.db.ref("users").once('value', function(snapshot) {
                    var users = snapshot.val() || {};
                    var currentRoomUsers = 0;
                    var roomSuffix = "";
                    var subIndex = 1;
                    
                    Object.values(users).forEach(function(u) {
                        if (u.city === baseCity) currentRoomUsers++;
                    });
                    
                    while (currentRoomUsers >= 6) {
                        subIndex++;
                        roomSuffix = "-" + subIndex;
                        currentRoomUsers = 0;
                        Object.values(users).forEach(function(u) {
                            if (u.city === baseCity + roomSuffix) currentRoomUsers++;
                        });
                    }
                    
                    var finalCityRoom = baseCity + roomSuffix;
                    window.app.currentCity = finalCityRoom;
                    
                    if (window.dispatch_room_update) window.dispatch_room_update(finalCityRoom);

                    window.app.db.ref("users/" + sessionID).set({
                        nick: nick,
                        city: finalCityRoom,
                        channel: finalCityRoom,
                        tx: false,
                        pwr: 0.7,
                        roger: (localStorage.getItem("roger") === "true"),
                        lastSeen: Date.now()
                    });
                    
                    window.app.db.ref("users/" + sessionID).onDisconnect().remove();

                    setInterval(function() {
                        if(window.app.db && window.app.sessionID) {
                            window.app.db.ref("users/" + window.app.sessionID).update({ lastSeen: Date.now() });
                        }
                    }, 5000);

                    if (window.initFirebaseListener) window.initFirebaseListener();
                });
            }
            
            if (typeof Peer !== 'undefined') {
                window.app.peer = new Peer(sessionID, { 
                    secure: true,
                    config: { 'iceServers': [
                        { 'urls': 'stun:stun.l.google.com:19302' },
                        { 'urls': 'stun:stun1.l.google.com:19302' },
                        { 'urls': 'stun:stun.cloudflare.com:3478' },
                        { 'urls': 'stun:global.stun.twilio.com:3478' },
                        { 'urls': 'stun:stun.services.mozilla.com' }
                    ] }
                });
                window.app.peer.on("call", function(call) {
                    window.app.activeCalls[call.peer] = call;
                
                // 🔒 FILTRO DE BLOQUEO SIGILOSO PARA LLAMADAS ENTRANTES
                var remoteID = call.peer;
                var currentBlocked = window.app.currentBlockedList || [];
                if (currentBlocked.indexOf(remoteID) !== -1) {
                    console.log("🚫 Bloqueando llamada entrante de ID en lista negra:", remoteID);
                    call.close();
                    return;
                }

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

        // 🛡️ SOLICITUD PROACTIVA DE MICRÓFONO
        window.ensureMicAccess = function() {
            if (window.requestMicPermission) window.requestMicPermission();
        };
    """)

    RadioNetworkManager.install()
    RadioAudioManager.install()
    RadioMapsManager.install()
    RadioBridge.install()

    // --- 🛡️ PREVENCIÓN DE CIERRE EN WEB (UNLOAD HACK) ---
    window.addEventListener("beforeunload", { event ->
        val e = event.asDynamic()
        e.preventDefault()
        e.returnValue = ""
    })

    ComposeViewport(root) {
        val initialState = remember { RadioPersistence.loadInitialState() }
        
        val micLevelState = remember { mutableStateOf(0f) }
        val isBeepingState = remember { mutableStateOf(false) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }
        val chatMessagesState = remember { mutableStateListOf<ChatMessage>() }
        val isPttLiveState = remember { mutableStateOf(false) }
        val voxActiveState = remember { mutableStateOf(initialState.isVoxEnabled) }
        val notificationState = remember { mutableStateOf<AppNotification?>(null) }
        val radioState = remember { mutableStateOf(initialState) }
        val remoteTransmitterName = remember { mutableStateOf<String?>(null) }
        
        val usersNotified = remember { mutableSetOf<String>() }
        
        val isReplayReadyState = remember { mutableStateOf(false) }
        val replayProgressState = remember { mutableStateOf(0f) }
        val systemVolumeState = remember { mutableStateOf(0.7f) }
        val backPressCount = remember { mutableStateOf(0) }

        // --- 🛡️ VINCULACIÓN DEL BOTÓN ATRÁS DEL NAVEGADOR (WEB) ---
        LaunchedEffect(Unit) {
            window.history.pushState(null, "", window.location.href)
            window.addEventListener("popstate", {
                backPressCount.value++
                window.history.pushState(null, "", window.location.href)
            })
        }

        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { isBeepingState.value = it },
            onPttSync = { },
            onPttBlocked = { },
            onReplayEmpty = { },
            onReplayStart = { },
            onBack = { backPressCount.value++ },
            onNickConflict = { },
            onUsersUpdate = { users ->
                try {
                    // Actualizar lista global de bloqueados para el motor de audio
                    window.asDynamic().app.currentBlockedList = radioState.value.blockedUsers.toTypedArray()
                    
                    val list = mutableListOf<RemoteUser>()
                    val nicksSeen = mutableSetOf<String>()
                    val currentIDs = mutableSetOf<String>()
                    val now = Date.now()

                    if (users != null && users != undefined) {
                        val keys = js("Object").keys(users)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            val u = users[k] ?: continue
                            currentIDs.add(k)
                            
                            val userNick = (u.nick as? String ?: "ESTACIÓN").trim().uppercase()
                            val lastSeen = (u.lastSeen as? Double ?: 0.0)
                            
                            if (userNick.length < 3) continue
                            if (now - lastSeen > 15000) continue 
                            if (nicksSeen.contains(userNick)) continue
                            
                            // 🔒 SISTEMA DE BLOQUEO SIGILOSO (MUTUO)
                            val myID = win.app.sessionID as? String ?: ""
                            val remoteBlocks = (u.blocks as? String ?: "").split(",")
                            val IAmBlockedByThem = remoteBlocks.contains(myID)
                            val TheyAreBlockedByMe = radioState.value.blockedUsers.contains(k)
                            
                            if (IAmBlockedByThem || TheyAreBlockedByMe) {
                                // Cortar comunicación si existe
                                val activeCall = win.app.activeCalls[k]
                                if (activeCall != null && activeCall != undefined) {
                                    try { js("activeCall.close();") } catch(e: Exception) {}
                                    js("delete window.app.activeCalls[k];")
                                }
                                continue
                            }

                            nicksSeen.add(userNick)

                            val myCity = win.app.currentCity as? String ?: ""
                            val userCity = u.city as? String ?: ""
                            
                            // 🔒 FILTRO DE SALA ESTRICTO: Solo conectar con gente en TU misma sub-sala (-2, -3, etc)
                            // Esto evita que el móvil se caliente intentando gestionar toda la ciudad a la vez.
                            if (myCity != userCity) continue

                            val isTransmitting = u.tx == true
                            val userPwr = (u.pwr as? Double ?: 0.7).toFloat()
                            win.app.remotePowers[k] = userPwr

                            val prevState = win.remoteTxStates[k] ?: false
                            if (prevState && !isTransmitting) {
                                val isMe = (k == win.app.sessionID)
                                val senderRoger = u.roger == true
                                if (!isMe && senderRoger && win.playUiSound != null) win.playUiSound("rx_off")
                            }
                            
                            // 🎵 PIRIPI (Modo Discreto / Inicio Transmisión)
                            if (!prevState && isTransmitting) {
                                val isMe = (k == win.app.sessionID)
                                if (!isMe && radioState.value.isDiscreteModeEnabled && win.playUiSound != null) {
                                    win.playUiSound("incoming")
                                }
                            }
                            
                            win.remoteTxStates[k] = isTransmitting

                            // 🎵 AVISO ENTRADA USUARIO (BEEP O NOTIFICACIÓN AMIGO)
                            if (k != win.app.sessionID && !usersNotified.contains(k)) {
                                usersNotified.add(k)
                                if (radioState.value.friends.contains(userNick)) {
                                    // Es un amigo: Notificación especial
                                    if (win.playUiSound != null) win.playUiSound("incoming")
                                    notificationState.value = AppNotification(
                                        title = "¡AMIGO EN FRECUENCIA!",
                                        message = "Tu compañero $userNick acaba de entrar en ${win.app.currentCity}.",
                                        type = NotificationType.Success
                                    )
                                    // Intentar notificación nativa si estamos en Android
                                    js("if(window.AndroidApp && window.AndroidApp.showNotification) window.AndroidApp.showNotification('AMIGO CONECTADO', 'El operador ' + userNick + ' está en frecuencia.');")
                                } else {
                                    // Usuario normal: Solo beep grave
                                    if (win.playUiSound != null) win.playUiSound("user_in")
                                }
                            }

                            list.add(RemoteUser(
                                id = k, 
                                nick = userNick, 
                                isTransmitting = isTransmitting,
                                city = u.city as? String ?: "SEVILLA",
                                channel = u.channel as? String ?: "SEVILLA",
                                txPower = userPwr,
                                isFriend = radioState.value.friends.contains(userNick),
                                roger = (u.roger == true)
                            ))
                            
                            if (k != win.app.sessionID && win.app.activeCalls[k] == null) {
                                win.establishOutgoingCall(k)
                            }
                        }
                    }
                    
                    // 🛡️ LIMPIEZA: Si un usuario desaparece, permitir que vuelva a pitar al entrar
                    usersNotified.retainAll(currentIDs)

                    remoteUsersState.clear()
                    remoteUsersState.addAll(list)
                    
                    // 🛡️ FIX: Detectar si alguien que NO soy yo está transmitiendo
                    val activeRemoteTx = list.find { it.isTransmitting && it.id != win.app.sessionID }
                    remoteTransmitterName.value = activeRemoteTx?.nick
                    
                    // 📻 Sincronizar estado de recepción para el motor de audio y VOX
                    if (win.app) {
                        win.app.rxActiveInternal = (activeRemoteTx != null)
                    }
                } catch(e: Exception) { }
            },
            onChatUpdate = { data ->
                val nl = mutableListOf<ChatMessage>()
                if (data != null && data != undefined) {
                    val keys = js("Object").keys(data)
                    for (i in 0 until (keys.length as Int)) {
                        val k = keys[i] as String
                        val m = data[k] ?: continue
                        nl.add(ChatMessage(k, m.senderNick?.toString() ?: "???", m.text?.toString() ?: "", m.timestamp?.toString()?.toDouble()?.toLong() ?: 0L))
                    }
                }
                chatMessagesState.clear()
                chatMessagesState.addAll(nl.sortedBy { it.timestamp })
            },
            onReplayProgress = { replayProgressState.value = it },
            onReplayAvailable = { isReplayReadyState.value = it },
            onChatOpen = { },
            onMicFailure = { },
            onIntegrityStatus = { },
            onIncomingAlert = { _, _, _ -> },
            onVoxSync = { },
            onRoomUpdate = { newRoom ->
                radioState.value = radioState.value.copy(city = newRoom, channel = newRoom)
            },
            onPttLive = { isPttLiveState.value = it },
            onVolumeSync = { newVol ->
                systemVolumeState.value = newVol
            }
        )

        App(
            savedNick = localStorage.getItem("indicativo") ?: "",
            initialState = radioState.value,
            isFirstTime = false,
            onOnboardingFinish = { },
            onPermissionRequest = { 
                RadioNetworkManager.connect(it) 
                js("window.ensureMicAccess()")
            },
            onLogout = { RadioPersistence.logout() },
            onInstallRequest = { },
            externalShowExitConfirm = false,
            onExternalExitRequest = { _, _ -> 
                val w = window.asDynamic()
                if (w.AndroidApp != null && w.AndroidApp.minimizeApp != null) {
                    w.AndroidApp.minimizeApp()
                }
            },
            onShareRequest = { city, channel, subtone, _, _, _ -> 
                val shareText = "📻 ¡Únete a mi frecuencia en ON AIR!\n📍 Ciudad: $city\n📡 Canal: $channel\n🔐 Subtono: $subtone\n\nEntra aquí: https://asurpan.github.io/sevillaON/?city=$city&channel=$channel&subtone=$subtone"
                val w = window.asDynamic()
                val encoded = w.encodeURIComponent(shareText)
                window.open("https://api.whatsapp.com/send?text=$encoded", "_blank")
            },
            onNoiseVolumeChange = { vol -> 
                val w = window.asDynamic()
                if(w.setNoiseVolume != null) w.setNoiseVolume(vol)
            },
            onMoniVolumeChange = { vol ->
                val w = window.asDynamic()
                if(w.app != null) {
                    w.app.moniVolume = vol
                    w.app.moniActive = (vol > 0)
                }
                js("if(window.updateMoniGain) window.updateMoniGain();")
                js("if(window.updateMasterVolume) window.updateMasterVolume();")
            },
            onEchoChange = { _, _ -> },
            onCityChange = { newCity ->
                val w = window.asDynamic()
                if (w.app != null && w.app.db != null && w.app.sessionID != null) {
                    w.app.currentCity = newCity
                    val updates: dynamic = js("{}")
                    updates.city = newCity
                    updates.channel = newCity
                    w.app.db.ref("users/" + w.app.sessionID).update(updates)
                    js("if(window.initFirebaseListener) window.initFirebaseListener();")
                }
            },
            onSubtoneChange = { },
            onChannelChange = { newCh ->
                val w = window.asDynamic()
                if (w.app != null && w.app.db != null && w.app.sessionID != null) {
                    val updates: dynamic = js("{}")
                    updates.channel = newCh
                    w.app.db.ref("users/" + w.app.sessionID).update(updates)
                }
            },
            onSendMessage = { t, tg -> RadioNetworkManager.sendMessage(t, tg) },
            onDeleteMessage = { id, tg -> RadioNetworkManager.deleteMessage(id, tg) },
            onPrivateChatRequest = { },
            onPublicChatRequest = { },
            onStateSave = { newState -> 
                RadioPersistence.saveState(newState)
                radioState.value = newState 
                voxActiveState.value = newState.isVoxEnabled
                
                val w = window.asDynamic()
                if(w.app != null) {
                    w.app.voxActive = newState.isVoxEnabled
                    w.app.voxSens = newState.voxSensitivity
                    w.app.rogerEnabled = newState.isRogerBeepEnabled
                    localStorage.setItem("roger", newState.isRogerBeepEnabled.toString())
                    js("if(window.broadcastPTT) window.broadcastPTT(window.app.pttStateInternal || false, newState.isRogerBeepEnabled);")
                }
            },
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
            externalVolume = systemVolumeState.value,
            externalBackPressCount = backPressCount.value,
            micLevel = micLevelState.value,
            isBeeping = isBeepingState.value,
            isCodedRx = false,
            externalPtt = isPttLiveState.value,
            externalPttBlocked = false,
            replayProgress = replayProgressState.value,
            isReplayReady = isReplayReadyState.value,
            remoteUsers = remoteUsersState,
            remoteTransmitterName = remoteTransmitterName.value,
            chatMessages = chatMessagesState,
            forceInitialScreen = false,
            audioIntegrity = true,
            onAntennaTest = { _ -> },
            onRequestLocationPermission = { },
            onOpenSettings = { },
            onChatOpenConsumed = { },
            onChatTargetConsumed = { },
            voxActive = voxActiveState.value
        )
    }
}
