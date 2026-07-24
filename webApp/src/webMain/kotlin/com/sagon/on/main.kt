package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - INFRAESTRUCTURA DE AUDIO Y COMUNICACIONES (JS/WebRTC)
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

// --- 🛡️ MONI GUARD: SEGURIDAD DE MONITORIZACIÓN Y ACOPLAMIENTO ---
object MoniGuard {
    fun install() {
        js("""
            window.checkHeadphones = function() {
                if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) return Promise.resolve(false);
                return navigator.mediaDevices.enumerateDevices().then(function(devices) {
                    return devices.some(function(device) {
                        return device.kind === 'audiooutput' && 
                        (device.label.toLowerCase().includes('head') || device.label.toLowerCase().includes('blue'));
                    });
                }).catch(function(e) { return false; });
            };

            window.updateMoniGain = function() {
                if (!window.app.ctx || !window.app.moniGain) return;
                
                var now = Date.now();
                var shouldCheck = !window.app.lastDeviceCheck || (now - window.app.lastDeviceCheck > 10000);
                
                var applyGain = function(has) {
                    var isTest = window.app.isAntennaTesting === true;
                    var isMoni = window.app.moniActive === true;
                    var isTx = window.app.isTransmittingInternal === true;

                    if (isMoni || isTest) {
                        var target = 0;
                        if (isTest) {
                            target = has === true ? 0.7 : 0.12; 
                        } else if (isTx && isMoni) {
                            target = has === true ? window.app.moniVolume : (window.app.moniVolume * 0.02);
                        }
                        
                        if (target > 0) {
                            if (window.app.moniGain) window.app.moniGain.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.05);
                            if (window.app.moniGainNode) window.app.moniGainNode.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.05);
                        } else {
                            if (window.app.moniGain) window.app.moniGain.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                            if (window.app.moniGainNode) window.app.moniGainNode.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                        }
                    } else {
                        if (window.app.moniGain) window.app.moniGain.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.05);
                        if (window.app.moniGainNode) window.app.moniGainNode.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.05);
                    }
                };

                if (shouldCheck) {
                    window.app.lastDeviceCheck = now;
                    applyGain(window.app.hasHeadphonesCache || false);
                    window.checkHeadphones().then(function(has) {
                        window.app.hasHeadphonesCache = has;
                        applyGain(has);
                    });
                } else {
                    applyGain(window.app.hasHeadphonesCache);
                }
            };

            window.updateMasterVolume = function() {
                if (!window.app.ctx || !window.app.masterOut) return;
                var gain = (window.app.rfGain || 0.5) * 6.0;
                window.app.masterOut.gain.setTargetAtTime(gain, window.app.ctx.currentTime, 0.05);
            };

            window.updateNoiseVolume = function() {
                if (!window.app.ctx || !window.app.noise) return;
                var noise = window.app.lastNoiseLevel || 0.0001;
                var isRadioActive = (window.fmEngine && window.fmEngine.currentStation);
                var isBusy = window.app.isTransmittingInternal || window.app.rxActiveInternal || window.app.isBeeping || window.app.isAnnouncerTalking || isRadioActive;
                var target = isBusy ? 0.0001 : noise;
                window.app.noise.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.1);
            };
        """)
    }
}

object RadioCore {
    fun install() {
        js("""
            (function() {
                window.APP_VERSION = 1715600000041; 
                
                window.sanitizePath = function(s) {
                    if (!s) return "unknown";
                    var out = s.toString();
                    var forbidden = [".", "${'$'}", "#", "[", "]", "/"];
                    for(var i=0; i<forbidden.length; i++) {
                        out = out.split(forbidden[i]).join("_");
                    }
                    return out;
                };

                var _p1 = "AIza"; var _p2 = "SyBA7tMb"; var _p3 = "cvbrl2lt"; var _p4 = "Tweqydmk7"; var _p5 = "PRfk-R7fWw";
                var cfg = { 
                    apiKey: _p1+_p2+_p3+_p4+_p5, 
                    authDomain: "sevilla-on-200b3.firebaseapp.com", 
                    databaseURL: "https://sevilla-on-200b3-default-rtdb.europe-west1.firebasedatabase.app",
                    projectId: "sevilla-on-200b3" 
                };
                if (typeof firebase !== 'undefined' && firebase.initializeApp && !firebase.apps.length) firebase.initializeApp(cfg);
                
                window.app = {
                    nick: "", sessionID: "", peer: null,
                    instanceID: Math.random().toString(36).substring(2, 11),
                    db: (typeof firebase !== 'undefined' && typeof firebase.database === 'function') ? firebase.database() : null,
                    ctx: null, noise: null, stream: null, rawStream: null, activeCalls: {}, 
                    lastActivity: {}, moniGain: null, 
                    moniVolume: parseFloat(localStorage.getItem("moniVol")) || 0.5, 
                    moniActive: localStorage.getItem("moniActive") === "true",
                    currentCity: localStorage.getItem("lastCity") || "ESPAÑA (NACIONAL)",
                    currentChannel: localStorage.getItem("lastChannel") || "GENERAL", 
                    voxActive: localStorage.getItem("voxActive") === "true", 
                    isBeeping: false, masterOut: null, remoteSources: {}, rxActiveInternal: false,
                    micAnalyser: null, remoteAnalysers: {}, remotePower: {}
                };

                window.initAudio = function() {
                    if (window.app.ctx) return;
                    var AC = window.AudioContext || window.webkitAudioContext;
                    window.app.ctx = new AC();
                    window.app.masterOut = window.app.ctx.createGain();
                    window.app.masterOut.connect(window.app.ctx.destination);
                    
                    window.app.noise = window.app.ctx.createGain();
                    window.app.noise.connect(window.app.masterOut);
                    
                    window.app.masterRxGain = window.app.ctx.createGain();
                    window.app.masterRxGain.connect(window.app.masterOut);
                };

                window.initRealMap = function(containerId, lat, lon) {
                    if (window.app.map) { try { window.app.map.remove(); } catch(e) {} }
                    var map = L.map(containerId, { zoomControl: false, attributionControl: false }).setView([lat || 37.3891, lon || -5.9845], 13);
                    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png').addTo(map);
                    window.app.map = map;
                    window.app.mapMarkers = {};
                };

                window.updateMapMarkers = function(usersJson) {
                    if (!window.app.map) return;
                    var users = JSON.parse(usersJson);
                    var myLat = parseFloat(localStorage.getItem("last_lat") || 37.3891);
                    var myLon = parseFloat(localStorage.getItem("last_lon") || -5.9845);
                    
                    users.forEach(function(user) {
                        if (user.lat && user.lon) {
                            var color = user.isMe ? "#06B6D4" : (user.isTransmitting ? "#EF4444" : "#22C55E");
                            if (window.app.mapMarkers[user.nick]) {
                                window.app.mapMarkers[user.nick].setLatLng([user.lat, user.lon]);
                                window.app.mapMarkers[user.nick].setStyle({ color: color, fillColor: color });
                            } else {
                                window.app.mapMarkers[user.nick] = L.circleMarker([user.lat, user.lon], { radius: 10, fillColor: color, color: "#FFF", weight: 2, fillOpacity: 0.8 }).addTo(window.app.map);
                                window.app.mapMarkers[user.nick].bindTooltip(user.nick, { permanent: true, direction: 'top' });
                            }
                        }
                    });
                };
            })();
        """)
    }
}

object RadioSignaling {
    fun install() {
        js("""
            window.broadcastPTT = function(active, roger, power) {
                if(!window.app || !window.app.db) return;
                window.app.isTransmittingInternal = active;
                window.app.db.ref("users/" + window.app.sessionID).update({ tx: active, pwr: power || 0.7 });
            };
            window.playUiSound = function(type) { console.log("Sound:", type); };
        """)
    }
}

object RadioBridge {
    fun install() {
        js("""
            window.setupBluetoothPTT = function() { console.log("BT PTT Ready"); };
            window.initFirebaseListener = function() {
                if (window.app.db) {
                    window.app.db.ref("users").on('value', function(s) { if(window.update_remote_users) window.update_remote_users(s.val()); });
                }
            };
            window.connectRadio = function(n) { 
                window.app.nick = n; 
                window.app.sessionID = n + "_" + Date.now(); 
                window.initAudio();
            };
        """)
    }

    fun setupDispatchers(
        win: dynamic,
        onMic: (Float) -> Unit,
        onBeep: (Boolean) -> Unit,
        onPttSync: (Boolean) -> Unit,
        onPttBlocked: () -> Unit,
        onUsersUpdate: (dynamic) -> Unit,
        onBack: () -> Unit,
        onNickConflict: (String) -> Unit,
        onMicFailure: () -> Unit,
        onIntegrityStatus: (Boolean) -> Unit,
        onBgStation: (String?) -> Unit,
        onBgGenreChange: (String) -> Unit,
        onVoxSync: (Boolean) -> Unit,
        onNasaImage: (String?, String?, String?) -> Unit,
        onDgtUpdate: (String?, String?) -> Unit,
        onEngineeringFinished: () -> Unit,
        onIncomingAlert: (String, String, String) -> Unit
    ) {
        win.dispatch_mic = onMic
        win.dispatch_beeping = onBeep
        win.dispatch_ptt_sync = onPttSync
        win.dispatch_ptt_blocked = onPttBlocked
        win.update_remote_users = onUsersUpdate
        win.trigger_back = onBack
        win.dispatch_nick_conflict = onNickConflict
        win.dispatch_mic_failure = onMicFailure
        win.dispatch_integrity_status = onIntegrityStatus
        win.dispatch_bg_station = onBgStation
        win.dispatch_bg_genre_change = onBgGenreChange
        win.dispatch_vox_sync = onVoxSync
        win.dispatch_nasa_image = onNasaImage
        win.dispatch_dgt_update = onDgtUpdate
        win.dispatch_engineering_finished = onEngineeringFinished
        win.dispatch_incoming_alert = onIncomingAlert
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val win: dynamic = js("window")
    
    RadioCore.install()
    MoniGuard.install()
    RadioSignaling.install()
    RadioBridge.install()

    ComposeViewport(document.body!!) {
        val micLevelState = remember { mutableStateOf(0f) }
        val isBeepingState = remember { mutableStateOf(false) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }
        val rxNameState = remember { mutableStateOf<String?>(null) }
        val notificationState = remember { mutableStateOf<AppNotification?>(null) }
        val backTrigger = remember { mutableStateOf(0) }
        val pttExternalState = remember { mutableStateOf(false) }

        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { isBeepingState.value = it },
            onPttSync = { pttExternalState.value = it },
            onPttBlocked = { },
            onUsersUpdate = { users ->
                remoteUsersState.clear()
                if (users != null && users != undefined) {
                    val keys = js("Object").keys(users)
                    for (i in 0 until (keys.length as Int)) {
                        val k = keys[i] as String
                        val u = users[k]
                        if (u != null) {
                            remoteUsersState.add(RemoteUser(id = k, nick = u.nick as String, isTransmitting = u.tx == true, lat = u.lat as? Double, lon = u.lon as? Double, bgGenre = u.bgGenre as? String))
                        }
                    }
                }
            },
            onBack = { backTrigger.value++ },
            onNickConflict = { },
            onMicFailure = { },
            onIntegrityStatus = { },
            onBgStation = { },
            onBgGenreChange = { },
            onVoxSync = { },
            onNasaImage = { _, _, _ -> },
            onDgtUpdate = { _, _ -> },
            onEngineeringFinished = { },
            onIncomingAlert = { _, _, _ -> }
        )

        App(
            savedNick = localStorage.getItem("indicativo") ?: "",
            isFirstTime = localStorage.getItem("onboarding_done") == null,
            onOnboardingFinish = { localStorage.setItem("onboarding_done", "true") },
            onPermissionRequest = { n -> win.connectRadio(n) },
            onLogout = { localStorage.clear(); win.location.reload() },
            onInstallRequest = { },
            externalShowExitConfirm = false,
            onExternalExitRequest = { _, _ -> },
            onShareRequest = { city, channel, subtone, proRole, platform, img -> },
            onNoiseVolumeChange = { },
            onMoniVolumeChange = { },
            onEchoChange = { _, _ -> },
            onCityChange = { },
            onSubtoneChange = { },
            onChannelChange = { },
            onSendMessage = { _, _ -> },
            onPrivateChatRequest = { },
            onPublicChatRequest = { },
            onStateSave = { s -> 
                if (win.app != null && win.app.db != null && win.app.sessionID != null) {
                    val ref = win.app.db.ref("users/" + win.app.sessionID)
                    val updates: dynamic = js("{}")
                    if (s.motoLatitude != null) updates.lat = s.motoLatitude
                    if (s.motoLongitude != null) updates.lon = s.motoLongitude
                    ref.update(updates)
                }
            },
            onMicEnable = { a, r, p -> win.broadcastPTT(a, r, p) },
            onReport = { },
            onNotificationDismiss = { notificationState.value = null },
            micLevel = micLevelState.value,
            isBeeping = isBeepingState.value,
            remoteUsers = remoteUsersState,
            remoteTransmitterName = rxNameState.value,
            chatMessages = emptyList(),
            forceInitialScreen = false,
            externalNotification = notificationState.value,
            externalBackPressCount = backTrigger.value,
            externalPtt = pttExternalState.value,
            onGpsRequest = { cb -> 
                if (win.navigator.geolocation) {
                    win.navigator.geolocation.getCurrentPosition({ pos: dynamic ->
                        cb("q=" + pos.coords.latitude + "," + pos.coords.longitude)
                    })
                }
            },
            onGpsCityRequest = { cb -> cb(null) },
            onGetHeading = { 0f },
            onGetTilt = { 0f },
            onExecuteEngineeringAction = { action ->
                val parts = action.split("|")
                when (parts[0]) {
                    "INIT_REAL_MAP" -> {
                        val containerId = if (parts.size >= 2) parts[1] else "activity-map-container"
                        js("window.initRealMap(containerId, 37.3891, -5.9845)")
                    }
                    "UPDATE_MAP_MARKERS" -> {
                        val json = parts[1]
                        js("window.updateMapMarkers(json)")
                    }
                    "ROTATE_MAP" -> {
                        val angle = parts[1]
                        js("var c = document.getElementById('activity-map-container'); if(c) c.style.transform = 'rotate(' + (-angle) + 'deg)';")
                    }
                    "HIDE_MAP_OVERLAY" -> {
                        js("var c = document.getElementById('activity-map-container'); if(c) c.style.display = 'none';")
                    }
                }
            },
            initialState = RadioState()
        )
    }
}
