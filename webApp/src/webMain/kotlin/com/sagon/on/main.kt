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
            if (window.app.peer && !window.app.peer.destroyed) {
                console.log("🛡️ AUDITORÍA: Radio ya conectada. Actualizando Nick...");
                window.app.nick = nick.toUpperCase();
                return;
            }

            // 🛡️ IDENTIFICADOR DE DISPOSITIVO PERSISTENTE
            var deviceID = localStorage.getItem("web_device_id");
            if (!deviceID) {
                deviceID = "web_" + Math.random().toString(36).substring(2, 15);
                localStorage.setItem("web_device_id", deviceID);
            }
            
            var cleanNick = (nick || "RADIO").toString().replace(/[.#${'$'}\[\]]/g, "_").toUpperCase();
            var sessionID = (cleanNick + "_" + deviceID.substring(0, 8)).trim();
            
            window.app.nick = cleanNick;
            window.app.deviceID = deviceID;
            window.app.sessionID = sessionID;
            
            console.log("🚀 Iniciando conexión Radio: ", sessionID);
            if (window.initAudio) window.initAudio();
            if (window.initAudio) window.initAudio();
            
            var baseCity = localStorage.getItem("lastCity") || "SEVILLA";
            
            if (window.app.db) {
                window.app.db.ref("users").once('value', function(snapshot) {
                    var users = snapshot.val() || {};
                    var currentRoomUsers = 0;
                    var roomSuffix = "";
                    var subIndex = 1;
                    var now = Date.now();
                    
                    // 🛡️ FILTRO DE SESIONES REALES: Solo contar usuarios activos (vistos hace menos de 30s)
                    Object.values(users).forEach(function(u) {
                        var lastSeen = u.lastSeen || 0;
                        if (u.city === baseCity && (now - lastSeen < 30000)) currentRoomUsers++;
                    });
                    
                    while (currentRoomUsers >= 10) { 
                        subIndex++;
                        roomSuffix = "-" + subIndex;
                        currentRoomUsers = 0;
                        Object.values(users).forEach(function(u) {
                            var lastSeen = u.lastSeen || 0;
                            if (u.city === baseCity + roomSuffix && (now - lastSeen < 30000)) currentRoomUsers++;
                        });
                    }
                    
                    var finalCityRoom = baseCity + roomSuffix;
                    window.app.currentCity = finalCityRoom;
                    
                    if (window.dispatch_room_update) window.dispatch_room_update(finalCityRoom);

                    // 🛡️ NOTIFICAR CAMBIO DE SALA SI NO ES LA PRINCIPAL
                    if (roomSuffix !== "") {
                        setTimeout(function() {
                            if (window.dispatch_notification) {
                                window.dispatch_notification("AVISO DE SALA", "La sala principal está llena. Estás en la sala de respaldo: " + finalCityRoom, "info");
                            }
                        }, 5000);
                    }

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
                            var updates = { lastSeen: Date.now() };
                            
                            // 🛡️ AUDITORÍA: Estadísticas de Transmisión
                            if (window.app.peer && window.app.activeCalls) {
                                Object.keys(window.app.activeCalls).forEach(id => {
                                    var call = window.app.activeCalls[id];
                                    if (call && call.peerConnection) {
                                        call.peerConnection.getStats(null).then(stats => {
                                            stats.forEach(report => {
                                                if (report.type === "outbound-rtp" && report.kind === "audio") {
                                                    window.app.diag.txPackets = report.packetsSent;
                                                }
                                            });
                                        });
                                    }
                                });
                            }

                            // 📈 MOTOR DE VETERANÍA (POTENCIA PROGRESIVA)
                            // Si el usuario está transmitiendo, sumamos veteranía (tiempo de aire)
                            if (window.app.isTransmittingInternal) {
                                var curVet = parseFloat(localStorage.getItem("vetPwr")) || 0.7;
                                if (curVet < 1.0) { // Máximo 1.0 (Potencia Profesional)
                                    // +0.002 por cada ciclo de 5s (~0.024 por cada 10 min de charla)
                                    curVet = Math.min(1.0, curVet + 0.002);
                                    localStorage.setItem("vetPwr", curVet.toString());
                                    updates.pwr = curVet;
                                    
                                    // Notificar a la UI si hay un cambio significativo
                                    if (window.dispatch_volume_sync) window.dispatch_volume_sync(curVet);
                                }
                            }
                            
                            window.app.db.ref("users/" + window.app.sessionID).update(updates);
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
                    console.log("🚀 PeerJS: Llamada entrante de:", call.peer);
                    window.app.activeCalls[call.peer] = call;
                
                // 🔒 FILTRO DE BLOQUEO SIGILOSO PARA LLAMADAS ENTRANTES
                var remoteID = call.peer;
                var currentBlocked = window.app.currentBlockedList || [];
                if (currentBlocked.indexOf(remoteID) !== -1) {
                    console.log("🚫 Bloqueando llamada entrante de ID en lista negra:", remoteID);
                    call.close();
                    return;
                }

                var stream = window.getStream();
                console.log("🚀 PeerJS: Contestando a:", call.peer, "Tracks enviados:", stream ? stream.getAudioTracks().length : 0);
                call.answer(stream);
                    if (window.setupCallStream) window.setupCallStream(call);
                });
            }
        };

        window.getStream = function() {
            return (window.app.txBus) ? window.app.txBus.stream : null;
        };

        window.establishOutgoingCall = function(id) {
            if (!window.app.peer || window.app.activeCalls[id]) return;
            var stream = window.getStream();
            if (!stream) {
                console.warn("🛡️ AUDITORÍA: No hay stream local para llamar a:", id);
                return;
            }
            console.log("🚀 PeerJS: Llamando a:", id, "Tracks enviados:", stream.getAudioTracks().length);
            var call = window.app.peer.call(id, stream);
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

    // 🛡️ SINCRONIZACIÓN INICIAL DE ESTADO CRÍTICO
    val initialState = RadioPersistence.loadInitialState()
    if (win.app != null) {
        win.app.discreteMode = initialState.isDiscreteModeEnabled
        win.app.rogerEnabled = initialState.isRogerBeepEnabled
        win.app.voxActive = initialState.isVoxEnabled
        win.app.voxSens = initialState.voxSensitivity
    }

    // --- 🛡️ PREVENCIÓN DE CIERRE EN WEB (UNLOAD HACK) ---
    window.addEventListener("beforeunload", { event ->
        val e = event.asDynamic()
        e.preventDefault()
        e.returnValue = ""
    })

    ComposeViewport(root) {
        val initialState = remember { RadioPersistence.loadInitialState() }

        var screenState by remember { 
            mutableStateOf(
                if ((localStorage.getItem("indicativo") ?: "").isNotEmpty() && initialState.hasAcceptedMicExplain) Screen.RadioCB 
                else Screen.Welcome
            ) 
        }
        
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
                            
                            if (userNick.isEmpty()) continue
                            if (now - lastSeen > 30000) continue 
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
                            val isMe = (k == win.app.sessionID)
                            
                            // 🔒 FILTRO DE SALA ESTRICTO: Solo conectar con gente en TU misma sub-sala (-2, -3, etc)
                            // Excepto a ti mismo, que siempre debes verte para confirmar conexión.
                            if (!isMe && myCity != userCity) continue

                            val isTransmitting = u.tx == true
                            val userPwr = (u.pwr as? Double ?: 0.7).toFloat()
                            win.app.remotePowers[k] = userPwr
                            
                            // 🛡️ SQUELCH INDIVIDUAL: Si no transmite o estamos en modo discreto, volumen a CERO real
                            val gNode = win.app.remoteGains[k]
                            if (gNode != null && gNode != undefined) {
                                val isDiscrete = radioState.value.isDiscreteModeEnabled
                                val targetVol = if (isTransmitting && !(isDiscrete && !isMe)) 1.0 else 0.0
                                if (gNode.gain.value != targetVol) {
                                    gNode.gain.setTargetAtTime(targetVol, win.app.ctx.currentTime, 0.05)
                                }
                            }

                            val prevState = win.remoteTxStates[k] ?: false
                            if (prevState && !isTransmitting) {
                                val isMe = (k == win.app.sessionID)
                                val senderRoger = u.roger == true
                                // 🛡️ SILENCIO EN CARGA: Solo sonar si no estamos en la pantalla de bienvenida/carga
                                if (!isMe && senderRoger && win.playUiSound != null && screenState != Screen.Welcome) {
                                    win.playUiSound("rx_off")
                                }
                            }
                            
                            // 🎵 PIRIPI (Modo Discreto / Inicio Transmisión)
                            if (!prevState && isTransmitting) {
                                val isMe = (k == win.app.sessionID)
                                // 🛡️ SILENCIO EN CARGA: Solo sonar si no estamos en la pantalla de bienvenida/carga
                                if (!isMe && radioState.value.isDiscreteModeEnabled && win.playUiSound != null && screenState != Screen.Welcome) {
                                    win.playUiSound("incoming")
                                }
                            }
                            
                            win.remoteTxStates[k] = isTransmitting

                            // 🎵 AVISO ENTRADA USUARIO (BEEP O NOTIFICACIÓN AMIGO)
                            if (k != win.app.sessionID && !usersNotified.contains(k)) {
                                usersNotified.add(k)
                                // 🛡️ SILENCIO EN CARGA: Solo sonar si no estamos en la pantalla de bienvenida/carga
                                if (screenState != Screen.Welcome) {
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
                                // 🛡️ LLAMADA INTELIGENTE (ANTI-CONFLICTO):
                                // Solo el dispositivo con la ID lexicográficamente menor inicia la llamada.
                                // Esto es sagrado para evitar el "Glare" (choque de llamadas) en 4G/WiFi.
                                if (win.app.sessionID < k) {
                                    console.log("🚀 Sincronizando túnel WebRTC con:", userNick);
                                    win.establishOutgoingCall(k);
                                }
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
            onNotification = { title, message, type ->
                val nType = when(type.lowercase()) {
                    "success" -> NotificationType.Success
                    "danger", "error" -> NotificationType.Danger
                    "warning" -> NotificationType.Warning
                    else -> NotificationType.Info
                }
                notificationState.value = AppNotification(title, message, nType)
            },
            onVoxSync = { },
            onRoomUpdate = { newRoom ->
                radioState.value = radioState.value.copy(city = newRoom, channel = newRoom)
                // 🛡️ RE-SINCRONIZACIÓN DE FIREBASE AL CAMBIAR DE CANAL/SALA
                val w = window.asDynamic()
                if (w.app != null && w.app.db != null && w.app.sessionID != null) {
                    val updates: dynamic = js("{}")
                    updates.city = newRoom
                    updates.channel = newRoom
                    w.app.db.ref("users/" + w.app.sessionID).update(updates)
                }
            },
            onPttLive = { isPttLiveState.value = it },
            onVolumeSync = { newVol ->
                systemVolumeState.value = newVol
                val w = window.asDynamic()
                if (w.setMasterVolume != null) w.setMasterVolume(newVol)
            },
            onDiagRequest = {
                val app = window.asDynamic().app
                val d = if (app != null && app != undefined) app.diag else null
                val rxMap = mutableMapOf<String, Int>()
                
                if (d != null && d != undefined) {
                    if (d.rxPackets != null && d.rxPackets != undefined) {
                        val keys = js("Object").keys(d.rxPackets)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            rxMap[k] = d.rxPackets[k] as Int
                        }
                    }
                    RadioDiagData(
                        micPermission = d.micPermission as? String ?: "unknown",
                        ctxState = d.ctxState as? String ?: "none",
                        txPackets = (d.txPackets as? Double ?: 0.0).toInt(),
                        rxPackets = rxMap
                    )
                } else {
                    RadioDiagData(micPermission = "not_initialized")
                }
            }
        )

        App(
            savedNick = localStorage.getItem("indicativo") ?: "",
            initialState = radioState.value,
            forceInitialScreen = (screenState == Screen.Welcome),
            isFirstTime = false,
            onOnboardingFinish = { },
            onPermissionRequest = { 
                RadioNetworkManager.connect(it) 
                js("window.ensureMicAccess()")
                screenState = Screen.RadioCB
                // 🛡️ ACTIVACIÓN DE SONIDO TRAS CARGA
                js("if(window.app) window.app.canPlaySounds = true;")
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
                val subText = if (subtone != "0000") " | 🔐 *$subtone*" else ""
                val shareText = "📻 *ON AIR SPAIN*\n📍 *$city* | 🔊 *CH $channel*$subText\n\n¡Modulamos! 🚀\nhttps://asurpan.github.io/sevillaON/?city=$city&channel=$channel&subtone=$subtone"
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
                    w.app.discreteMode = newState.isDiscreteModeEnabled
                    localStorage.setItem("roger", newState.isRogerBeepEnabled.toString())
                    js("if(window.broadcastPTT) window.broadcastPTT(window.app.pttStateInternal || false, newState.isRogerBeepEnabled);")
                }
            },
            onConnectRadio = { RadioNetworkManager.connect(it) },
            onDiagRequest = {
                val app = window.asDynamic().app
                val d = if (app != null && app != undefined) app.diag else null
                val rxMap = mutableMapOf<String, Int>()
                
                if (d != null && d != undefined) {
                    if (d.rxPackets != null && d.rxPackets != undefined) {
                        val keys = js("Object").keys(d.rxPackets)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            rxMap[k] = d.rxPackets[k] as Int
                        }
                    }
                    RadioDiagData(
                        micPermission = d.micPermission as? String ?: "unknown",
                        ctxState = d.ctxState as? String ?: "none",
                        txPackets = (d.txPackets as? Double ?: 0.0).toInt(),
                        rxPackets = rxMap
                    )
                } else {
                    RadioDiagData(micPermission = "not_initialized")
                }
            },
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
            onPlaySound = { type ->
                val w = window.asDynamic()
                if (w.playUiSound != null) w.playUiSound(type)
            },
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
