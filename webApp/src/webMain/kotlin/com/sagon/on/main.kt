package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - INFRAESTRUCTURA DE AUDIO Y COMUNICACIONES (JS/WebRTC)
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 * 
 * Este archivo gestiona el motor de audio, WebRTC, Guardian de Voz y Chat.
 * Blindado tras la estabilización final de la recepción y física de aguja.
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
                            // En modo Test, si no hay cascos, limitamos drásticamente para evitar acople
                            target = has === true ? 0.7 : 0.12; 
                        } else if (isTx && isMoni) {
                            // En modo Monitor, lo mismo
                            // 🛡️ REPARACIÓN QUIRÚRGICA: Reducimos de 0.2 a 0.08 para eliminar retroalimentación por altavoz
                            target = has === true ? window.app.moniVolume : (window.app.moniVolume * 0.08);
                        }
                        
                        if (target > 0) {
                            if (window.app.moniGain) window.app.moniGain.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.05);
                            if (window.app.moniGainNode) window.app.moniGainNode.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.05);
                        } else {
                            // --- 🎯 FIX: CIERRE INSTANTÁNEO PARA EVITAR ECO ---
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
                    // Aplicar inmediatamente con el cache para evitar silencio inicial
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
                // --- 🔒 HARD-LOCK: MOTOR DE VOLUMEN GENERAL (PROTEGIDO) ---
                // PROHIBIDO TOCAR: Asegura que Roger Beep y sonidos de UI se atenúen con el slider.
                // BOOST: Subimos el multiplicador de 4.0 a 6.0 para máxima potencia en móviles.
                var gain = (window.app.rfGain || 0.5) * 6.0;
                window.app.masterOut.gain.setTargetAtTime(gain, window.app.ctx.currentTime, 0.05);
            };

            window.updateNoiseVolume = function() {
                if (!window.app.ctx || !window.app.noise) return;
                var noise = window.app.lastNoiseLevel || 0.0001;
                // --- 🛡️ QRM PROTECTED: Mute si hay actividad, radio FM o se está buscando emisora ---
                var isRadioActive = (window.fmEngine && window.fmEngine.currentStation);
                var isBusy = window.app.isTransmittingInternal || window.app.rxActiveInternal || window.app.isBeeping || window.app.isAnnouncerTalking || isRadioActive;
                var target = isBusy ? 0.0001 : noise;
                window.app.noise.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.1);
            };
        """)
    }
}

// --- 📻 RADIO CORE: MOTOR DE INFRAESTRUCTURA (ENGINE) ---
object RadioCore {
    fun install() {
        js("""
            (function() {
                window.APP_VERSION = 1715600000040; // Referencia interna para Auto-Update

                window.sanitizePath = function(s) {
                    if (!s) return "unknown";
                    var out = s.toString();
                    var forbidden = [".", "${'$'}", "#", "[", "]", "/"];
                    for(var i=0; i<forbidden.length; i++) {
                        out = out.split(forbidden[i]).join("_");
                    }
                    return out;
                };

                window.checkAppUpdate = function() {
                    if (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1") return;
                    console.log("Comprobando actualizaciones...");
                    // Precargar voces para el locutor
                    if (window.speechSynthesis) window.speechSynthesis.getVoices();

                    fetch('version.json?t=' + Date.now(), { cache: 'no-store' })
                        .then(function(response) { 
                            if (!response.ok) throw new Error('Network response was not ok');
                            const contentType = response.headers.get('content-type');
                            if (!contentType || !contentType.includes('application/json')) {
                                throw new TypeError("Oops, we haven't got JSON!");
                            }
                            return response.json(); 
                        })
                        .then(function(data) {
                            if (data && data.version && data.version > window.APP_VERSION) {
                                console.log("Nueva versión detectada: " + data.version);
                                
                                // --- ANTI-LOOP: No recargar si la URL ya tiene esta versión ---
                                var url = new URL(window.location.href);
                                if (url.searchParams.get('v') === String(data.version)) {
                                    console.log("Versión ya cargada vía URL.");
                                    return;
                                }

                                localStorage.setItem("app_version_installed", data.version);
                                // Forzar refresco ignorando caché mediante cambio de URL
                                url.searchParams.set('v', data.version);
                                window.location.href = url.toString();
                            }
                        })
                        .catch(function(err) { console.warn("Update check skipped:", err.message); });
                };

                // Comprobar cada 5 minutos
                setInterval(window.checkAppUpdate, 300000);
                // Y al arrancar
                setTimeout(window.checkAppUpdate, 5000);

                // --- SEGURIDAD: CLAVE ROTADA Y PROTEGIDA POR RESTRICCIONES DE GOOGLE CLOUD ---
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
                    instanceID: Math.random().toString(36).substring(2, 11), // ID único para esta pestaña/instancia
                    db: (typeof firebase !== 'undefined' && typeof firebase.database === 'function') ? firebase.database() : null,
                    ctx: null, noise: null, stream: null, rawStream: null, activeCalls: {}, 
                    lastActivity: {}, // Registro de última vez que cada ID transmitió
                    moniGain: null, 
                    moniVolume: parseFloat(localStorage.getItem("moniVol")) || 0.5, 
                    moniActive: localStorage.getItem("moniActive") === "true",
                    currentCity: localStorage.getItem("lastCity") || "ESPAÑA (NACIONAL)",
                    currentChannel: localStorage.getItem("lastChannel") || "GENERAL", 
                    currentSubtone: localStorage.getItem("lastSubtone") || "0000",
                    voxActive: localStorage.getItem("voxActive") === "true", 
                    voxSens: parseFloat(localStorage.getItem("voxSens")) || 0.5,
                    isVoxTransmitting: false, voxHangTimer: 0, voxLockout: 0,
                    isBeeping: false, lastNoiseLevel: 0, outputAudio: null, masterOut: null,
                    echoDelay: null, echoFeedback: null, echoWet: null, isTransmittingInternal: false, lastChCount: 0,
                    lastPttState: false, 
                    txBus: null, txGate: null, remoteSources: {}, rxActiveInternal: false,
                    micAnalyser: null, remoteAnalysers: {},
                    replayRecorder: null, replayChunks: [], lastReplayURL: null, replayAudio: null,
                    hasInteracted: false,
                    bgRadio: null, bgRadioGain: null, currentBgStation: null,
                    bgGenre: localStorage.getItem("bgGenre") || "MIX",
                    isDiscreteModeEnabled: localStorage.getItem("disMode") === "true",
                    motoActive: localStorage.getItem("motoActive") === "true"
                };

                window.updateDiscreteMode = function() {
                    if (!window.app || !window.app.ctx || !window.app.masterRxGain) return;
                    var now = window.app.ctx.currentTime;
                    var shouldMute = window.app.isDiscreteModeEnabled && document.hidden;
                    var rxVol = (localStorage.getItem("dspEnabled") === "false") ? 1.5 : 2.5;
                    var target = shouldMute ? 0.0001 : rxVol;
                    window.app.masterRxGain.gain.setTargetAtTime(target, now, 0.1);
                };

                window.updateBgDucking = function(forceVal) {
                    if (!window.app || !window.app.bgRadioGain || !window.app.ctx) return;
                    
                    var isBusy = window.app.isTransmittingInternal || 
                                 window.app.rxActiveInternal || 
                                 window.app.isBeeping || 
                                 window.app.isAnnouncerTalking;
                    
                    var baseVol = parseFloat(localStorage.getItem("bgVol")) || 0.7;
                    var target = isBusy ? (baseVol * 0.1) : baseVol;
                    
                    if (forceVal !== undefined) target = forceVal * baseVol;
                    
                    var now = window.app.ctx.currentTime;
                    window.app.bgRadioGain.gain.cancelScheduledValues(now);
                    window.app.bgRadioGain.gain.setTargetAtTime(target, now, 0.3); // Fundido suave vía WebAudio
                    
                    if (window.fmEngine && window.fmEngine.audio) {
                        var audio = window.fmEngine.audio;
                        // --- 🎯 FIX: FUNDIDO MANUAL PARA ELEMENTO NATIVO (ELIMINA SALTOS) ---
                        if (window.app._duckInterval) clearInterval(window.app._duckInterval);
                        
                        var steps = 15;
                        var stepTime = 20; // 300ms total de fundido
                        var current = audio.volume;
                        var diff = (target - current) / steps;
                        var count = 0;
                        
                        window.app._duckInterval = setInterval(function() {
                            count++;
                            current += diff;
                            audio.volume = Math.max(0, Math.min(1, current));
                            if (count >= steps) {
                                audio.volume = target;
                                clearInterval(window.app._duckInterval);
                            }
                        }, stepTime);
                    }
                };

                // Registrar interacción para habilitar audio/micro en navegadores
                var interactionHandler = function() { 
                    if(!window.app) return;
                    window.app.hasInteracted = true; 
                    
                    // --- AUDIO RESILIENCE: Fix all connections on first touch ---
                    if (window.app.ctx) {
                        window.app.ctx.resume().then(function() {
                            if (window.app.masterOut) {
                                try {
                                    window.app.masterOut.disconnect();
                                    // --- VOLUME FIX: Direct hardware connection restored ---
                                    window.app.masterOut.connect(window.app.ctx.destination);
                                    if (window.app.masterDest) window.app.masterOut.connect(window.app.masterDest);
                                } catch(e) {}
                            }
                            
                            // --- PROTECTED CORE: Sincronización inmediata de volúmenes tras interacción ---
                            if (window.updateMasterVolume) window.updateMasterVolume();
                            if (window.updateNoiseVolume) window.updateNoiseVolume();
                            if (window.updateMoniGain) window.updateMoniGain();

                            // --- PROTECTED CORE: Despertar Locutor (Speech Synthesis Prime) ---
                            if (window.speechSynthesis) {
                                if (window.fmEngine && window.fmEngine.unlock) {
                                    window.fmEngine.unlock();
                                } else {
                                    var msg = new SpeechSynthesisUtterance(" ");
                                    msg.volume = 0.01;
                                    window.speechSynthesis.speak(msg);
                                }
                            }

                            // Asegurar carga de voces en cambios
                            if (window.speechSynthesis && window.speechSynthesis.addEventListener) {
                                window.speechSynthesis.addEventListener('voiceschanged', function() {
                                    window.speechSynthesis.getVoices();
                                });
                            }

                            if (window.app.nick && !window.app.rawStream) {
                                if (typeof window.requestMicPermission === 'function') {
                                    window.requestMicPermission();
                                }
                            }
                        });
                    }
                };
                window.addEventListener('mousedown', interactionHandler, { once: true });
                window.addEventListener('touchstart', interactionHandler, { once: true });
                window.addEventListener('keydown', interactionHandler, { once: true });
                
                // --- 🛡️ SISTEMA GUARDIÁN DE VOZ (ANTIDEAFNESS) ---
                // Monitoriza si hay señal de red pero silencio en altavoces
                // 🔒 HARD-LOCK: PROTOCOLO DE INTEGRIDAD DE VOZ (PROHIBIDO TOCAR)
                window.voiceWatchdog = {
                    failures: 0,
                    txFailures: 0,
                    lastHealthCheck: Date.now(),
                    check: function() {
                        if (!window.app || !window.app.ctx) return;
                        
                        // Si el contexto está suspendido y ya hemos interactuado, forzamos reanimación
                        if (window.app.ctx.state === 'suspended' && window.app.hasInteracted) {
                            window.app.ctx.resume();
                        }

                        // --- 🛡️ TEST DE INTEGRIDAD DE SALIDA (TX & TEST ANTENA) ---
                        var isTx = window.app.isTransmittingInternal === true;
                        var isTest = window.app.isAntennaTesting === true;

                        if ((isTx || isTest) && !window.app.isBeeping) {
                            var micEnergy = 0;
                            if (window.app.micAnalyser) {
                                var d = new Uint8Array(128); // Aumentado de 32 a 128 para mayor precisión
                                window.app.micAnalyser.getByteTimeDomainData(d);
                                for(var i=0; i<d.length; i++) micEnergy += Math.abs(d[i] - 128);
                            }

                            // Verificación de Red (PeerJS)
                            var networkOk = window.app.peer && !window.app.peer.disconnected && !window.app.peer.destroyed;

                            if (micEnergy === 0) {
                                this.txFailures++;
                                if (this.txFailures > 20) { // Umbral de reanimación equilibrado (20s)
                                    console.warn("Guardian: Reanimacion de emergencia iniciada...");
                                    if (window.dispatch_integrity_status) window.dispatch_integrity_status(false);
                                    
                                    // --- MANIOBRA DE REANIMACION AGRESIVA ---
                                    // Limpiamos el analizador para forzar re-creacion de nodos en requestMicPermission
                                    window.app.micAnalyser = null;
                                    
                                    if (window.app.rawStream) {
                                        window.app.rawStream.getTracks().forEach(function(t) { t.stop(); });
                                    }
                                    window.app.rawStream = null;
                                    
                                    // Reset de permisos en sesión para evitar bloqueos del navegador
                                    sessionStorage.removeItem('mic_denied');
                                    
                                    if (typeof window.requestMicPermission === 'function') {
                                        window.requestMicPermission();
                                    }
                                    
                                    // Si no hay red, forzamos reconexión de PeerJS
                                    if (window.app.peer && window.app.peer.disconnected && !window.app.peer.destroyed) {
                                        window.app.peer.reconnect();
                                    }

                                    this.txFailures = 0;
                                }
                            } else {
                                this.txFailures = 0;
                                if (window.dispatch_integrity_status) window.dispatch_integrity_status(true);
                            }

                            if (!networkOk && (this.txFailures % 5 === 0)) {
                                console.warn("Guardián: Detectada desconexión de red. Reintentando peer...");
                                if (window.app.peer && window.app.peer.disconnected && !window.app.peer.destroyed) window.app.peer.reconnect();
                            }
                        } else {
                            if (window.dispatch_integrity_status) window.dispatch_integrity_status(true);
                        }

                        // --- 🛡️ TEST DE INTEGRIDAD DE RECEPCIÓN (RX) ---
                        if (window.app.rxActiveInternal && !window.app.isTransmittingInternal) {
                            // ... lógica existente ...
                            var totalEnergy = 0;
                            for (var id in window.app.remoteAnalysers) {
                                var rd = new Uint8Array(32);
                                window.app.remoteAnalysers[id].getByteTimeDomainData(rd);
                                for(var j=0; j<rd.length; j++) totalEnergy += Math.abs(rd[j] - 128);
                            }

                            if (totalEnergy === 0) {
                                this.failures++;
                                if (this.failures === 10) {
                                    console.warn("Guardián: Sordera persistente (Firewall?). Forzando RE-CONEXIÓN WebRTC...");
                                    // Si tras 10s vemos portadora pero no hay audio, el P2P está bloqueado.
                                    // Intentamos cerrar y reabrir todas las llamadas activas.
                                    for (var id in window.app.activeCalls) {
                                        window.app.activeCalls[id].close();
                                        delete window.app.activeCalls[id];
                                    }
                                }
                                if (this.failures > 3) {
                                    console.warn("Guardián: Recepción sorda detectada. Ejecutando RE-PATCH...");
                                    if (window.app.masterOut) {
                                        try { window.app.masterOut.disconnect(); } catch(e){}
                                        window.app.masterOut.connect(window.app.ctx.destination);
                                        if (window.app.masterDest) window.app.masterOut.connect(window.app.masterDest);
                                        for (var sid in window.app.remoteSources) {
                                            try { 
                                                window.app.remoteSources[sid].src.disconnect();
                                                window.app.remoteSources[sid].src.connect(window.app.masterRxGain || window.app.masterOut); 
                                            } catch(e){}
                                        }
                                    }
                                    window.app.ctx.resume();
                                    this.failures = 0;
                                }
                            } else {
                                this.failures = 0;
                            }
                        }
                    }
                };
                setInterval(function() { if(window.voiceWatchdog) window.voiceWatchdog.check(); }, 1000);

                // --- EMERGENCY RECOVERY ---
                if (!window.app.db) {
                    console.error("Firebase Database no detectado. Reintentando conexión...");
                    setTimeout(function() {
                        if (typeof firebase !== 'undefined' && typeof firebase.database === 'function') {
                            window.app.db = firebase.database();
                            console.log("Firebase Database recuperado.");
                            if (window.initFirebaseListener) window.initFirebaseListener();
                        }
                    }, 2000);
                }

                // --- RADIO GHOST ENGINE: ATMÓSFERA DE PROPAGACIÓN REAL ---
                // Genera eventos aleatorios (silbidos, ecos lejanos) para que la radio parezca viva
                window.playRadioLife = function() {
                    if (!window.app || !window.app.ctx || !window.app.noise) return;
                    var ctx = window.app.ctx;
                    var now = ctx.currentTime;
                    
                    // --- LÓGICA ESTACIONAL (PROPAGACIÓN DE VERANO) ---
                    var month = new Date().getMonth();
                    var isSummer = (month >= 5 && month <= 8); // Junio a Septiembre
                    var propMultiplier = isSummer ? 2.5 : 1.0; // En verano, los eventos son más probables y "brillantes"
                    
                    // Decidir qué evento disparar de forma aleatoria
                    var dice = Math.random();
                    
                    if (dice > (0.94 / propMultiplier)) {
                        // 1. SILBIDO HETERODINO (Tuning Whistle)
                        var o = ctx.createOscillator();
                        var g = ctx.createGain();
                        o.type = 'sine';
                        var freq = 800 + (Math.random() * 2000);
                        o.frequency.setValueAtTime(freq, now);
                        o.frequency.exponentialRampToValueAtTime(freq * 1.05, now + 3);
                        
                        g.gain.setValueAtTime(0, now);
                        g.gain.linearRampToValueAtTime(0.001 * propMultiplier, now + 1.5); 
                        g.gain.linearRampToValueAtTime(0, now + 4);
                        
                        o.connect(g); g.connect(window.app.noise); 
                        o.start(now); o.stop(now + 3.3);
                    } else if (dice > (0.88 / propMultiplier)) {
                        // 2. VOZ FANTASMA (Distant Transmission)
                        var audio = new Audio('emisora.mp3');
                        audio.currentTime = Math.random() * 15; 
                        audio.volume = 0.05;
                        var src = ctx.createMediaElementSource(audio);
                        var f = ctx.createBiquadFilter();
                        var g = ctx.createGain();
                        
                        f.type = 'bandpass';
                        f.frequency.value = 1600; 
                        f.Q.value = 3.0; // Resonancia suavizada (antes 10.0) para evitar acoples metálicos
                        
                        g.gain.setValueAtTime(0, now);
                        g.gain.linearRampToValueAtTime(0.002 * propMultiplier, now + 1.0);
                        g.gain.linearRampToValueAtTime(0, now + 4.0);
                        
                        src.connect(f); f.connect(g); g.connect(window.app.noise);
                        audio.play().catch(function(e){});
                        setTimeout(function() { audio.pause(); audio.src = ""; }, 3000);
                    }

                    // --- MODULACION DINAMICA DE PROPAGACION ---
                    // El volumen del ruido (QRM) oscila muy lentamente para imitar cambios atmosfericos
                    if (!window.app.isPropagating) {
                        window.app.isPropagating = true;
                        setInterval(function() {
                            if (window.app.noise) {
                                // --- PROTECCION PTT/RX: BLOQUEAR MODULACION DURANTE ACTIVIDAD ---
                                // Si hay alguien hablando (RX) o estamos transmitiendo (TX), no variamos el ruido
                                if (window.app.isTransmittingInternal || window.app.isBeeping || window.app.isVoxTransmitting || window.app.rxActiveInternal) return;
                                
                                var baseNoise = window.app.lastNoiseLevel || 0;
                                var drift = 0.9 + (Math.random() * 0.2); // Rango más estrecho (90% a 110%) para evitar picos
                                window.app.noise.gain.setTargetAtTime(baseNoise * drift, ctx.currentTime, 5.0);
                            }
                        }, 8000);
                    }
                    
                    // Programar el siguiente evento (Más frecuentes en verano)
                    var next = (5000 / propMultiplier) + (Math.random() * (10000 / propMultiplier));
                    setTimeout(window.playRadioLife, next);
                };


                window.startReplayRecording = function() {
                    if (!window.app || !window.app.replayDest) return;
                    if (window.app.replayRecorder) return;
                    
                    setTimeout(function() {
                        try {
                            var stream = window.app.replayDest.stream;
                            var mime = 'audio/webm;codecs=opus';
                            if (!MediaRecorder.isTypeSupported(mime)) mime = 'audio/ogg;codecs=opus';
                            
                            window.app.replayRecorder = new MediaRecorder(stream, MediaRecorder.isTypeSupported(mime) ? { mimeType: mime } : {});
                            window.app.replayChunks = [];
                            window.app.headerChunk = null; 
                            
                            window.app.replayRecorder.ondataavailable = function(e) {
                                if (e.data.size > 0) {
                                    if (!window.app.headerChunk) {
                                        // El primer bloque contiene las cabeceras vitales para el decodificador
                                        window.app.headerChunk = e.data;
                                    } else {
                                        window.app.replayChunks.push(e.data);
                                        if (window.dispatch_replay_available) window.dispatch_replay_available(true);
                                        // Mantenemos unos 15-18 segundos (5 bloques de 3s)
                                        if (window.app.replayChunks.length > 5) window.app.replayChunks.shift();
                                    }
                                }
                            };
                            window.app.replayRecorder.start(3000); 
                        } catch(e) { console.error("Error Replay:", e); }
                    }, 1000);
                };

                window.playReplay = function() {
                    if (!window.app || !window.app.ctx) return;
                    if (navigator.vibrate) navigator.vibrate(20);

                    // --- 🔄 LÓGICA TOGGLE (PARAR SI YA ESTÁ SONANDO) ---
                    if (window.app.currentReplaySource) {
                        try { 
                            window.app.currentReplaySource.stop(); 
                            window.app.currentReplaySource = null;
                            if (window.dispatch_replay_progress) window.dispatch_replay_progress(0);
                        } catch(e) {}
                        return;
                    }

                    if (window.app.replayRecorder && window.app.replayRecorder.state === 'recording') {
                        try { window.app.replayRecorder.requestData(); } catch(e) {}
                    }

                    setTimeout(function() {
                        if (!window.app.headerChunk || window.app.replayChunks.length === 0) {
                            if(window.dispatch_replay_empty) window.dispatch_replay_empty();
                            return;
                        }
                        try {
                            // Reconstruimos el audio uniendo la cabecera con los bloques recientes
                            var fullChunks = [window.app.headerChunk].concat(window.app.replayChunks);
                            var blob = new Blob(fullChunks, { type: window.app.replayRecorder.mimeType || 'audio/webm' });
                            
                                    blob.arrayBuffer().then(function(buffer) {
                                        window.app.ctx.decodeAudioData(buffer, function(audioBuffer) {
                                            if (window.app.currentReplaySource) {
                                                try { window.app.currentReplaySource.stop(); } catch(e) {}
                                            }

                                            // --- 🧠 MOTOR DE REPLAY INTELIGENTE (ANTI-SILENCIO) ---
                                            var data = audioBuffer.getChannelData(0);
                                            var threshold = 0.005; // Umbral de detección de voz ultrasensible
                                            var firstVoiceIndex = -1;
                                            
                                            // Escaneamos el buffer buscando el primer pico de señal real
                                            for (var i = 0; i < data.length; i += 100) { // Salto de 100 para eficiencia
                                                if (Math.abs(data[i]) > threshold) {
                                                    firstVoiceIndex = i;
                                                    break;
                                                }
                                            }

                                            if (firstVoiceIndex === -1) {
                                                // Si todo el buffer es silencio, informamos y salimos
                                                if(window.dispatch_replay_empty) window.dispatch_replay_empty();
                                                return;
                                            }

                                            var startTimeOffset = firstVoiceIndex / audioBuffer.sampleRate;
                                            // Dejamos 0.2s de margen previo para no cortar la primera palabra
                                            startTimeOffset = Math.max(0, startTimeOffset - 0.2);
                                            
                                            if(window.dispatch_replay_start) window.dispatch_replay_start();
                                            
                                            var source = window.app.ctx.createBufferSource();
                                            source.buffer = audioBuffer;
                                            source.connect(window.app.masterOut);
                                            window.app.currentReplaySource = source;
                                            
                                            var oldNoise = window.app.noise ? window.app.noise.gain.value : 0;
                                            if (window.app.noise) {
                                                window.app.noise.gain.cancelScheduledValues(window.app.ctx.currentTime);
                                                window.app.noise.gain.setTargetAtTime(0.0001, window.app.ctx.currentTime, 0.1);
                                            }
                                            
                                            source.onended = function() {
                                                if (window.app.noise) {
                                                    window.app.noise.gain.cancelScheduledValues(window.app.ctx.currentTime);
                                                    window.app.noise.gain.setTargetAtTime(oldNoise, window.app.ctx.currentTime, 1.0);
                                                }
                                                if (window.app.replayProgressInterval) clearInterval(window.app.replayProgressInterval);
                                                if (window.dispatch_replay_progress) window.dispatch_replay_progress(0);
                                                window.app.currentReplaySource = null;
                                            };
                                            
                                            source.start(0, startTimeOffset);
                                            var startTime = window.app.ctx.currentTime;
                                            var duration = audioBuffer.duration - startTimeOffset;
                                            
                                            if (window.app.replayProgressInterval) clearInterval(window.app.replayProgressInterval);
                                            window.app.replayProgressInterval = setInterval(function() {
                                                var elapsed = window.app.ctx.currentTime - startTime;
                                                var progress = Math.min(1.0, elapsed / duration);
                                                if (window.dispatch_replay_progress) window.dispatch_replay_progress(progress);
                                                if (progress >= 1.0) clearInterval(window.app.replayProgressInterval);
                                            }, 100);
                                        }, function(err) {
                                    console.error("Decode error:", err);
                                    // Si falla el decodificador por cabeceras corruptas, reiniciamos el ciclo
                                    window.app.headerChunk = null;
                                    window.app.replayChunks = [];
                                });
                            }).catch(function(e) { console.error("Buffer error:", e); });
                        } catch(e) { console.error("Error Replay:", e); }
                    }, 200);
                };

            window.setupBluetoothPTT = function() {
                    if ('mediaSession' in navigator) {
                        navigator.mediaSession.metadata = new MediaMetadata({ 
                            title: 'ON AIR SPAIN',
                            artist: 'PULSA EL BOTÓN CENTRAL PARA HABLAR',
                            album: 'Radio CB / PTT Remoto',
                            artwork: [{ src: 'https://sevillaon.es/preview.png', sizes: '512x512', type: 'image/png' }]
                        });
                        
                        // Handler para dispositivos que solo envían Play/Pause (Bluetooth)
                        var pttToggle = function() {
                            var target = !window.app.isTransmittingInternal;
                            window.broadcastPTT(target, true, 0.7);
                            if(window.dispatch_ptt_sync) window.dispatch_ptt_sync(target);
                            navigator.mediaSession.playbackState = target ? 'playing' : 'paused';
                        };

                        navigator.mediaSession.setActionHandler('play', pttToggle);
                        navigator.mediaSession.setActionHandler('pause', pttToggle);
                        navigator.mediaSession.setActionHandler('stop', function() {
                            window.broadcastPTT(false, true);
                            if(window.dispatch_ptt_sync) window.dispatch_ptt_sync(false);
                            navigator.mediaSession.playbackState = 'paused';
                        });
                        
                        // Soportar botones de "Siguiente" para PTT (común en cascos de moto)
                        navigator.mediaSession.setActionHandler('nexttrack', pttToggle);
                        navigator.mediaSession.setActionHandler('previoustrack', pttToggle);
                    }
                    
                    // --- 🎧 PTT DE HARDWARE (USB / CABLE / ANDROID BRIDGE) ---
                    // Estas funciones las llama el MainActivity de Android
                    window.external_ptt_down = function() {
                        if (!window.app.isTransmittingInternal) {
                            window.broadcastPTT(true, true, 0.7);
                            if(window.dispatch_ptt_sync) window.dispatch_ptt_sync(true);
                        }
                    };
                    
                    window.external_ptt_up = function() {
                        if (window.app.isTransmittingInternal) {
                            // Solo disparamos el fin de transmisión. 
                            // El 'dispatch_ptt_sync(false)' se moverá al final real del sonido.
                            window.broadcastPTT(false, true, 0.7);
                        }
                    };

                    // --- 🛡️ SISTEMA DE SILENCIO EXTERNO (WHATSAPP/LLAMADAS) ---
                    window.set_external_mute = function(mute) {
                        if (!window.app || !window.app.ctx || !window.app.masterOut) return;
                        window.app.isExternalMuted = mute;
                        if (mute) {
                            // Silencio absoluto inmediato
                            window.app.masterOut.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.02);
                            // Cortar PTT si estaba activo para liberar el micro
                            if (window.app.isTransmittingInternal) {
                                window.broadcastPTT(false, true);
                                if (window.dispatch_ptt_sync) window.dispatch_ptt_sync(false);
                            }
                        } else {
                            // Restaurar volumen progresivamente según el slider de la radio
                            var currentGain = (window.app.rfGain || 0.5) * 4.0;
                            window.app.masterOut.gain.setTargetAtTime(currentGain, window.app.ctx.currentTime, 0.1);
                        }
                    };
                };

                window.requestNotificationPermission = function() {
                    if ("Notification" in window && Notification.permission === "default") {
                        Notification.requestPermission();
                    }
                };

                window.sendSystemNotification = function(title, body, type) {
                    // --- 🛡️ BRIDGE NATIVO: NOTIFICACIONES ANDROID ---
                    if (window.AndroidApp && typeof window.AndroidApp.showNotification === 'function') {
                        window.AndroidApp.showNotification(title, body);
                        return;
                    }

                    if ("Notification" in window && Notification.permission === "granted" && document.hidden) {
                        try { 
                            var n = new Notification(title, { 
                                body: body,
                                icon: 'https://asurpan.github.io/sevillaON/logo.png' 
                            }); 
                            n.onclick = function() {
                                window.focus();
                                if (type === 'chat' && window.dispatch_chat_open) window.dispatch_chat_open();
                            };
                        } catch(e) {}
                    }
                    if (navigator.vibrate) navigator.vibrate([30, 50, 30]);
                };

                // --- 🧹 CONNECTION REAPER (OPTIMIZACIÓN DE ESCALABILIDAD) ---
                setInterval(function() {
                    if (!window.app.activeCalls) return;
                    var now = Date.now();
                    var mySub = localStorage.getItem("lastSubtone") || "0000";
                    var friends = (localStorage.getItem("friends") || "").split(",");

                    for (var id in window.app.activeCalls) {
                        var lastTx = window.app.lastActivity[id] || 0;
                        var isVIP = mySub !== "0000" || (window.app.remoteSources[id] && friends.includes(window.app.remoteSources[id].nick));
                        
                        // Si es VIP (Privado o Amigo), mantenemos la conexión mucho más tiempo (30 min de silencio)
                        // Si es un canal público normal, cerramos a los 2 min para ahorrar.
                        var timeout = isVIP ? 1800000 : 120000;

                        if (now - lastTx > timeout && !window.app.isTransmittingInternal) {
                            if (window.app.activeCalls[id]) {
                                window.app.activeCalls[id].close();
                                delete window.app.activeCalls[id];
                                if (window.app.remoteSources) {
                                    // Limpieza de recursos de audio para evitar fugas de memoria
                                    for (var sid in window.app.remoteSources) {
                                        var srcObj = window.app.remoteSources[sid];
                                        if (srcObj.peerID === id) {
                                            if (srcObj.tag && srcObj.tag.parentNode) srcObj.tag.parentNode.removeChild(srcObj.tag);
                                            delete window.app.remoteSources[sid];
                                        }
                                    }
                                }
                                if (window.app.remoteAnalysers && window.app.remoteAnalysers[id]) delete window.app.remoteAnalysers[id];
                            }
                        }
                    }
                }, 30000);

                window.initAudio = function() {
                    if (window.app.ctx) {
                        if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                        return;
                    }
                    var AC = window.AudioContext || window.webkitAudioContext;
                    // Forzamos 48kHz pero con fallback seguro para hardware antiguo
                    try {
                        window.app.ctx = new AC({ latencyHint: 'interactive', sampleRate: 48000 });
                    } catch(e) {
                        window.app.ctx = new AC();
                    }
                    
                    // --- 🛡️ SISTEMA DE COEXISTENCIA (LLAMADAS/WHATSAPP) ---
                    // Si el sistema operativo suspende el audio (por una llamada entrante), liberamos el PTT
                    window.app.ctx.onstatechange = function() {
                        if (window.app.ctx && window.app.ctx.state === 'suspended') {
                            if (window.app.isTransmittingInternal) {
                                if (window.broadcastPTT) window.broadcastPTT(false, true);
                                if (window.dispatch_ptt_sync) window.dispatch_ptt_sync(false);
                            }
                            // Liberar hardware de micro para que WhatsApp/Llamadas lo usen sin conflicto
                            if (window.app.rawStream) {
                                window.app.rawStream.getTracks().forEach(function(t) { t.enabled = false; });
                            }
                        } else if (window.app.ctx && window.app.ctx.state === 'running') {
                            // Al volver de la llamada, recuperamos el micro automáticamente
                            if (window.app.rawStream) {
                                window.app.rawStream.getTracks().forEach(function(t) { t.enabled = true; });
                            }
                        }
                    };
                    
                    // --- 🛡️ GESTIÓN DE VISIBILIDAD (MODO SIEMPRE ACTIVO) ---
                    document.addEventListener('visibilitychange', function() {
                        if (!document.hidden) {
                            // AL VOLVER A LA RADIO: Recuperamos volumen de micro y despertamos audio
                            if (window.app.ctx) window.app.ctx.resume();
                            if (!window.app.rawStream && window.app.nick) {
                                if (typeof window.requestMicPermission === 'function') {
                                    window.requestMicPermission();
                                }
                            }
                        }
                        if(window.updateDiscreteMode) window.updateDiscreteMode();
                    });

                    // Sistema de auto-mantenimiento (despertador periódico)
                    // Mantiene el AudioContext y el Bridge vivos incluso en segundo plano
                    setInterval(function() {
                        if (window.app.ctx) {
                            if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                            if (window.app.outputAudio && window.app.outputAudio.paused) {
                                window.app.outputAudio.play().catch(function(e){});
                            }
                            if ('mediaSession' in navigator) {
                                navigator.mediaSession.playbackState = 'playing';
                            }
                        }
                    }, 3000);

                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass";
                    window.app.filter.frequency.value = 1600; 
                    window.app.filter.Q.value = 0.5;

                    window.app.moniGain = window.app.ctx.createGain(); 
                    window.app.moniGain.gain.value = 0;
                    
                    window.app.masterRxGain = window.app.ctx.createGain();
                    window.app.masterRxGain.gain.value = 5.0; // Subimos base de 3.0 a 5.0

                    window.app.compressor = window.app.ctx.createDynamicsCompressor();
                    window.app.compressor.threshold.setValueAtTime(-24, window.app.ctx.currentTime);
                    window.app.compressor.ratio.setValueAtTime(12, window.app.ctx.currentTime);
                    
                    window.app.echoDelay = window.app.ctx.createDelay(1.0);
                    window.app.echoDelay.delayTime.value = 0.3;
                    window.app.echoFeedback = window.app.ctx.createGain();
                    window.app.echoFeedback.gain.value = 0; 
                    window.app.echoWet = window.app.ctx.createGain();
                    window.app.echoWet.gain.value = 0; 

                    window.app.echoDelay.connect(window.app.echoFeedback);
                    window.app.echoFeedback.connect(window.app.echoDelay);
                    window.app.echoDelay.connect(window.app.echoWet);

                    window.app.noise = window.app.ctx.createGain();
                    var initNoise = window.app.lastNoiseLevel || 0;
                    window.app.noise.gain.setValueAtTime(initNoise, window.app.ctx.currentTime);

                    var noiseFilter = window.app.ctx.createBiquadFilter();
                    noiseFilter.type = "lowpass";
                    noiseFilter.frequency.value = 1200; 
                    noiseFilter.Q.value = 1.0;

                    var bufferSize = window.app.ctx.sampleRate * 2;
                    var buffer = window.app.ctx.createBuffer(1, bufferSize, window.app.ctx.sampleRate);
                    var data = buffer.getChannelData(0);
                    var lastOut = 0.0;
                    for (var i = 0; i < bufferSize; i++) { 
                        var white = (Math.random() * 2 - 1) * 0.5;
                        data[i] = (lastOut + (0.02 * white)) / 1.02;
                        lastOut = data[i];
                        data[i] *= 3.5; 
                    }
                    
                    var noiseSource = window.app.ctx.createBufferSource();
                    noiseSource.buffer = buffer; noiseSource.loop = true;
                    noiseSource.connect(noiseFilter);
                    noiseFilter.connect(window.app.noise);
                    noiseSource.start();

                    window.app.masterOut = window.app.ctx.createGain();

                    // --- 📻 CONFIGURACIÓN RADIO DE FONDO ---
                    window.app.bgRadioGain = window.app.ctx.createGain();
                    window.app.bgRadioGain.gain.value = 0;
                    window.app.bgRadioGain.connect(window.app.masterOut);

                    // --- 🔒 SOFT START SINCRONIZADO (PROHIBIDO TOCAR) ---
                    // Sincronizado con la precarga de la UI para una entrada instantánea
                    var initialRfGain = parseFloat(localStorage.getItem("rfGain")) || 0.5;
                    var initialSquelch = parseFloat(localStorage.getItem("squelch")) || 0.2;
                    var targetVol = initialRfGain * 6.0; // Sincronizado con el boost de masterOut
                    
                    // Pre-calcular ruido inicial (QRM) por si la UI tarda en sincronizar
                    var initialNoise = 0;
                    if (initialRfGain > initialSquelch) {
                        initialNoise = (initialRfGain - initialSquelch) * 0.5;
                    }
                    if (initialNoise < 0.0001) initialNoise = 0.0001;
                    window.app.lastNoiseLevel = initialNoise;
                    
                    window.app.masterOut.gain.setValueAtTime(0, window.app.ctx.currentTime);
                    window.app.rfGain = initialRfGain;

                    setTimeout(function() {
                        if (window.app && window.app.ctx && window.app.masterOut) {
                            var cur = window.app.ctx.currentTime;
                            // Rampa un poco más rápida (0.2 en lugar de 0.5) para que no parezca sordo al entrar
                            window.app.masterOut.gain.setTargetAtTime(targetVol, cur, 0.2);
                            // También forzamos el ruido inicial
                            if (window.app.noise) window.app.noise.gain.setTargetAtTime(initialNoise, cur, 0.2);
                        }
                    }, 1000);

                    window.updateDspSettings = function(enabled) {
                        if (!window.app.ctx || !window.app.filter || !window.app.compressor || !window.app.masterRxGain) return;
                        var now = window.app.ctx.currentTime;
                        
                        if (enabled) {
                            // MODO ÉLITE: Procesado avanzado para claridad máxima
                            window.app.filter.frequency.setTargetAtTime(1600, now, 0.1);
                            window.app.filter.Q.setTargetAtTime(0.8, now, 0.1);
                            
                            window.app.compressor.threshold.setTargetAtTime(-28, now, 0.1);
                            window.app.compressor.ratio.setTargetAtTime(8, now, 0.1);
                            window.app.compressor.attack.setTargetAtTime(0.003, now, 0.1);
                            window.app.compressor.release.setTargetAtTime(0.150, now, 0.1);
                            
                            window.app.masterRxGain.gain.setTargetAtTime(2.2, now, 0.1); // Nivel de escucha pro
                        } else {
                            // MODO ESTÁNDAR: Sonido natural
                            window.app.filter.frequency.setTargetAtTime(1200, now, 0.1);
                            window.app.filter.Q.setTargetAtTime(0.4, now, 0.1);
                            
                            window.app.compressor.threshold.setTargetAtTime(-20, now, 0.1);
                            window.app.compressor.ratio.setTargetAtTime(4, now, 0.1);
                            window.app.compressor.attack.setTargetAtTime(0.010, now, 0.1);
                            window.app.compressor.release.setTargetAtTime(0.250, now, 0.1);
                            
                            window.app.masterRxGain.gain.setTargetAtTime(1.8, now, 0.1);
                        }
                    };

                    // --- 🎙️ BUS DE TRANSMISIÓN (TX) ---
                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);

                    window.app.filter.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.compressor);
                    window.app.moniGain.connect(window.app.compressor);
                    window.app.noise.connect(window.app.compressor);
                    
                    // Conexión del compresor a la salida y a la grabación de replay
                    window.app.compressor.connect(window.app.masterOut);
                    
                    window.app.replayDest = window.app.ctx.createMediaStreamDestination();
                    // Grabamos lo que sale del compresor (RX + Noise + Moni + Vox)
                    window.app.compressor.connect(window.app.replayDest);
                    
                    window.app.masterDest = window.app.ctx.createMediaStreamDestination();
                    // El masterDest es lo que realmente oímos por el bridge (Permite segundo plano y manos libres)
                    window.app.masterOut.connect(window.app.masterDest);
                    
                    if (!window.app.outputAudio) {
                        window.app.outputAudio = document.createElement('audio');
                        window.app.outputAudio.style.display = "none";
                        window.app.outputAudio.id = "radio-output-bridge";
                        // --- 🛡️ ANTI-ECHO: Muted para evitar doble salida con AudioContext (Elimina retardos) ---
                        window.app.outputAudio.muted = true;
                    // --- MEDIA SHIELD: Forzar modo multimedia en moviles ---
                    window.app.outputAudio.title = "ON AIR SPAIN";
                    window.app.outputAudio.setAttribute('preload', 'auto');
                    document.body.appendChild(window.app.outputAudio);
                    window.app.outputAudio.setAttribute('autoplay', 'true');
                    window.app.outputAudio.setAttribute('playsinline', 'true');
                }
                window.app.outputAudio.srcObject = window.app.masterDest.stream;
                // --- VOLUME FIX: Direct connection to hardware for foreground priority ---
                window.app.masterOut.connect(window.app.ctx.destination);
                window.app.outputAudio.play().catch(function(e){});
                
                if ('mediaSession' in navigator) navigator.mediaSession.playbackState = 'playing';
                window.startReplayRecording();
                
                // --- 🛡️ MOTOR DE CALENTAMIENTO (ANTI-LAG) ---
                // Envía un pulso de 1ms totalmente silencioso para despertar el hardware.
                // Esto evita que el primer Roger Beep suene distorsionado o con "lag".
                try {
                    var warmOsc = window.app.ctx.createOscillator();
                    var warmGain = window.app.ctx.createGain();
                    warmGain.gain.value = 0;
                    warmOsc.connect(warmGain); warmGain.connect(window.app.ctx.destination);
                    warmOsc.start(0); warmOsc.stop(0.01);
                } catch(e) {}

                // --- ACTIVACION DEL MOTOR DE VIDA (ATMOSFERA REAL) ---
                if (typeof window.playRadioLife === 'function') {
                    setTimeout(function() { window.playRadioLife(); }, 10000);
                }

                // --- KEEP-ALIVE ULTRASONICO (INAUDIBLE) ---
                    // Emite un tono a 20kHz con volumen mínimo para evitar que el chip de audio entre en sleep
                    var ultrasound = window.app.ctx.createOscillator();
                    var ultraGain = window.app.ctx.createGain();
                    ultrasound.type = 'sine';
                    ultrasound.frequency.setValueAtTime(20000, window.app.ctx.currentTime);
                    ultraGain.gain.setValueAtTime(0.0001, window.app.ctx.currentTime);
                    ultrasound.connect(ultraGain);
                    ultraGain.connect(window.app.ctx.destination);
                    ultrasound.start();
                };

                window.getStream = function() {
                    if (window.app.txBus) return window.app.txBus.stream;
                    return null;
                };

                window.shareWhatsAppCustom = function(text) {
                // --- 🛡️ BRIDGE NATIVO: COMPARTIR ---
                if (window.AndroidApp && typeof window.AndroidApp.shareText === 'function') {
                    window.AndroidApp.shareText(text);
                    return;
                }
                var url = "whatsapp://send?text=" + encodeURIComponent(text);
                window.open(url, "_blank");
            };

            window.shareSocialCustom = function(text, platform) {
                // --- 🛡️ BRIDGE NATIVO: COMPARTIR ---
                if (window.AndroidApp && typeof window.AndroidApp.shareText === 'function') {
                    window.AndroidApp.shareText(text);
                    return;
                }
                
                if (navigator.share) {
                    // Extraer la URL del texto para pasarla como parámetro url si es posible
                    var urlMatch = text.match(/https:\/\/asurpan\.github\.io\/sevillaON\/[^\s]*/);
                    var shareUrl = urlMatch ? urlMatch[0] : 'https://asurpan.github.io/sevillaON/';
                    navigator.share({ title: 'ON AIR SPAIN', text: text, url: shareUrl });
                } else {
                    window.shareWhatsAppCustom(text);
                }
            };

            window.connectRadio = function(nick) {
                    var safeNick = nick.replace(/[^a-zA-Z0-9]/g, "");
                    
                    var deviceID = "";
                    if (window.AndroidApp && typeof window.AndroidApp.getAndroidId === 'function') {
                        deviceID = "A_" + window.AndroidApp.getAndroidId();
                    } else {
                        deviceID = localStorage.getItem("on_device_id");
                        if (!deviceID) {
                            deviceID = "D_" + Math.random().toString(36).substring(2, 11);
                            localStorage.setItem("on_device_id", deviceID);
                        }
                    }
                    
                    // Restore unique session ID per nick+device to avoid PeerJS conflicts
                    var sessionID = safeNick + "_" + deviceID;
                    localStorage.setItem("session_id_" + safeNick, sessionID);
                    
                    window.app.nick = safeNick; 
                    window.app.sessionID = sessionID;
                    
                    // --- 🛡️ ACTIVAR GUARDIÁN ANDROID ---
                    if (window.AndroidApp && typeof window.AndroidApp.startRadioService === 'function') {
                        window.AndroidApp.startRadioService();
                    }
                    
                    window.initAudio();
                    
                    if (window.app.db) {
                        // --- 🧹 BARRIDO DE SEGURIDAD (ANTI-ZOMBIS) ---
                        // Eliminamos rastro de estaciones que no cerraron limpiamente (más de 45s de silencio)
                        window.app.db.ref("users").once('value', function(snap) {
                            var u = snap.val(); if(!u) return;
                            var now = Date.now();
                            for(var id in u) {
                                if (now - (u[id].lastSeen || 0) > 45000) window.app.db.ref("users/" + id).remove();
                            }
                        });

                        // Limpieza del nodo de usuario
                        window.app.db.ref("users/" + sessionID).remove();
                        window.app.db.ref("users/" + sessionID).onDisconnect().remove();

                        // --- 🛡️ MONITOR DE SESIÓN ACTIVA (TAKEOVER) ---
                        // Registramos esta sesión como la activa para este dispositivo
                        var deviceRef = window.app.db.ref("device_sessions/" + deviceID);
                        deviceRef.set({
                            sessionID: sessionID,
                            instanceID: window.app.instanceID,
                            nick: safeNick
                        });
                        deviceRef.onDisconnect().remove();

                        // Listener para detectar si otra sesión toma el control
                        deviceRef.on('value', function(snap) {
                            var data = snap.val();
                            if (data && data.instanceID !== window.app.instanceID) {
                                // Si la instancia activa no somos nosotros, nos desconectamos silenciosamente
                                window.app.isTerminated = true;
                                if (window.app.peer) {
                                    window.app.peer.destroy();
                                    window.app.peer = null;
                                }
                                if (window.app.heartbeatInterval) clearInterval(window.app.heartbeatInterval);

                                // Mostrar aviso visual de bloqueo (Glassmorphism Premium)
                                if (!document.getElementById('takeover-overlay')) {
                                    var div = document.createElement('div');
                                    div.id = 'takeover-overlay';
                                    div.style = "position:fixed;top:0;left:0;width:100%;height:100%;background:#0F172A;color:white;display:flex;flex-direction:column;justify-content:center;align-items:center;z-index:99999;font-family:sans-serif;text-align:center;padding:20px;overflow-y:auto;";
                                    div.innerHTML = "<div style='border:1px solid rgba(34, 197, 94, 0.2);padding:25px 15px;border-radius:32px;background:rgba(255,255,255,0.03);max-width:340px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,0.8);position:relative;overflow:hidden;margin:auto;'>" +
                                        "<div style='width:60px;height:60px;margin:0 auto 15px;display:flex;justify-content:center;align-items:center;'>" +
                                        "<svg viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg' style='width:45px;height:45px;'><path d='M12 2L12 22M12 2L15 6M12 2L9 6M7 10L17 10M8 14L16 14M9 18L15 18M12 22L16 22M12 22L8 22' stroke='#22C55E' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/><path d='M5 12C3 10 3 7 5 5' stroke='#22C55E' stroke-width='1' stroke-opacity='0.4' stroke-linecap='round'/><path d='M19 12C21 10 21 7 19 5' stroke='#22C55E' stroke-width='1' stroke-opacity='0.4' stroke-linecap='round'/></svg></div>" +
                                        "<h1 style='color:white;font-weight:900;letter-spacing:4px;margin-bottom:6px;font-size:18px;'>ON AIR SPAIN</h1>" +
                                        "<h2 style='color:#3B82F6;font-weight:900;letter-spacing:2px;margin-bottom:18px;font-size:10px;text-transform:uppercase;'>Protocolo de Seguridad</h2>" +
                                        "<p style='color:rgba(255,255,255,0.6);line-height:1.5;font-size:13px;margin-bottom:26px;font-weight:500;padding:0 10px;'>Se ha detectado una <b>Sesión Duplicada</b>. Esta terminal ha entrado en modo escucha.</p>" +
                                        "<button onclick='location.reload()' style='background:#22C55E;border:none;padding:16px 24px;border-radius:20px;font-weight:900;cursor:pointer;color:#0F172A;width:calc(100% - 10px);margin:0 5px;box-shadow:0 12px 30px rgba(34,197,94,0.3);letter-spacing:1px;font-size:12px;text-transform:uppercase;'>Recuperar Control</button>" +
                                        "<p style='margin-top:24px;color:rgba(255,255,255,0.2);font-size:8px;font-weight:900;letter-spacing:1.5px;text-transform:uppercase;'>La red de voz más auténtica de España</p></div>";
                                    document.body.appendChild(div);
                                }
                                console.warn("Sesión sustituida por otra instancia.");
                            }
                        });
                    }

                    window.app.peer = new Peer(sessionID, { 
                        debug: 1, // Solo errores en producción
                        secure: true,
                        config: {
                            'iceServers': [
                                { 'urls': 'stun:stun.l.google.com:19302' },
                                { 'urls': 'stun:stun1.l.google.com:19302' },
                                { 'urls': 'stun:stun2.l.google.com:19302' },
                                { 'urls': 'stun:stun3.l.google.com:19302' },
                                { 'urls': 'stun:stun4.l.google.com:19302' },
                                { 'urls': 'stun:stun.services.mozilla.com' },
                                { 'urls': 'stun:stun.relay.metered.ca:80' },
                                { 'urls': 'stun:stun.ekiga.net' },
                                { 
                                    'urls': 'turn:openrelay.metered.ca:80', 
                                    'username': 'openrelay', 
                                    'credential': 'openrelay' 
                                },
                                { 
                                    'urls': 'turn:openrelay.metered.ca:443', 
                                    'username': 'openrelay', 
                                    'credential': 'openrelay' 
                                }
                            ],
                            'iceCandidatePoolSize': 10
                        } 
                    });
                    
                    window.app.peer.on('disconnected', function() {
                        if (window.app.peer && !window.app.peer.destroyed) window.app.peer.reconnect();
                    });

                    window.app.peer.on('error', function(err) {
                        if (err.type === 'network' || err.type === 'server-error') {
                            setTimeout(function() { 
                                if(window.app.peer && window.app.peer.disconnected && !window.app.peer.destroyed) {
                                    window.app.peer.reconnect(); 
                                }
                            }, 5000);
                        }
                    });

                    // --- 🎙️ GESTIÓN DE MICRO INTELIGENTE (PRIVACIDAD & RENDIMIENTO) ---
                    window.requestMicPermission = function() {
                        if (!window.app || window.app.rawStream || window.app.isRequestingMic) return;
                        
                        if (sessionStorage.getItem("mic_denied") === "true") return;

                        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                            window.app.isRequestingMic = true;
                            var constraints = {
                                audio: { 
                                    echoCancellation: true, 
                                    autoGainControl: true, 
                                    noiseSuppression: true
                                }
                            };
                            navigator.mediaDevices.getUserMedia(constraints).then(function(s) {
                                window.app.rawStream = s;
                                window.app.isRequestingMic = false;
                                if (window.app.ctx && window.app.ctx.state === 'suspended') window.app.ctx.resume();
                                
                                var micSrc = window.app.ctx.createMediaStreamSource(s);
                                
                                // --- 🎙️ TX DSP: PROCESADOR DE VOZ PROFESIONAL (PUNCH) ---
                                // 1. Filtro de Corte (Elimina ruidos sordos de fondo)
                                var txFilter = window.app.ctx.createBiquadFilter();
                                txFilter.type = "highpass";
                                
                                // --- 🏍️ MODO MOTO: FILTRO DE VIENTO AGRESIVO ---
                                // Si está en modo moto, subimos el corte a 300Hz para matar el ruido de motor/viento
                                var cutoff = window.app.motoActive ? 300 : 80;
                                txFilter.frequency.value = cutoff;
                                window.app.txFilter = txFilter; // Guardar referencia para cambios en caliente

                                // 2. Filtro de Cuerpo (Calidez de locutor)
                                var txBody = window.app.ctx.createBiquadFilter();
                                txBody.type = "peaking";
                                txBody.frequency.value = 250;
                                txBody.Q.value = 0.8;
                                txBody.gain.value = 2.0; // Calidez natural de locutor
                                
                                // 3. Enhancer de Presencia (Claridad y Brillo)
                                var txEnhancer = window.app.ctx.createBiquadFilter();
                                txEnhancer.type = "peaking";
                                txEnhancer.frequency.value = 3200; // Subimos frecuencia para brillo cristalino
                                txEnhancer.Q.value = 1.0;          
                                txEnhancer.gain.value = 2.5;       // Brillo elegante

                                // 4. Compresor de Emisora (Voz constante y potente)
                                // --- 🚀 AJUSTE BROADCAST PROFESIONAL ---
                                var txCompressor = window.app.ctx.createDynamicsCompressor();
                                txCompressor.threshold.value = -24; // Umbral pro: menos ruido de fondo, más dinámica
                                txCompressor.knee.value = 30;       
                                txCompressor.ratio.value = 6;       // Ratio pro: voz firme pero sin distorsión
                                txCompressor.attack.value = 0.005;  // Ataque suave para transitorios naturales
                                txCompressor.release.value = 0.20;  

                                micSrc.connect(txFilter);
                                txFilter.connect(txBody);
                                txBody.connect(txEnhancer);
                                txEnhancer.connect(txCompressor);

                                window.app.micAnalyser = window.app.ctx.createAnalyser(); 
                                window.app.micAnalyser.fftSize = 256; 
                                txCompressor.connect(window.app.micAnalyser);

                                var makeupGain = window.app.ctx.createGain();
                                makeupGain.gain.value = 1.2; // 🛡️ NIVEL LIMPIO: Evita el clipping digital en Replay
                                txCompressor.connect(makeupGain);

                                // --- 🛡️ TRANSMISOR DE RED (LIMPIEZA Y CONEXIÓN ABSOLUTA) ---
                                if (window.app.txGate) {
                                    try { makeupGain.disconnect(window.app.txGate); } catch(e) {}
                                    makeupGain.connect(window.app.txGate);
                                }
                                
                                // --- 🛡️ MONITOR ZERO-LATENCY BYPASS ---
                                var moniDirect = window.app.ctx.createGain();
                                moniDirect.gain.value = 0; 
                                makeupGain.connect(moniDirect);
                                moniDirect.connect(window.app.masterOut); 
                                window.app.moniGainNode = moniDirect; 

                                // --- 🎸 CONEXIÓN DSP DE EFECTOS ---
                                makeupGain.connect(window.app.echoDelay);

                                // Conectamos la salida del efecto (Wet) a los destinos críticos con limpieza
                                if (window.app.txGate) {
                                    try { window.app.echoWet.disconnect(window.app.txGate); } catch(e) {}
                                    window.app.echoWet.connect(window.app.txGate);
                                }
                                try { window.app.echoWet.disconnect(moniDirect); } catch(e) {}
                                window.app.echoWet.connect(moniDirect);
                                
                                var txGrab = window.app.ctx.createGain();
                                txGrab.gain.value = 0;
                                makeupGain.connect(txGrab);
                                window.app.echoWet.connect(txGrab);
                                if (window.app.replayDest) txGrab.connect(window.app.replayDest);
                                window.app.txGrab = txGrab;
                                
                                // Forzar activación de tracks a nivel de hardware
                                s.getAudioTracks().forEach(function(t) { 
                                    t.enabled = true; 
                                    if (t.contentHint) t.contentHint = "speech";
                                });
                                
                                console.log("🎙️ Voz vinculada al transmisor de red (Modo Potencia Máxima).");
                            }).catch(function(err) {
                                window.app.isRequestingMic = false;
                                console.warn("Fallo micro:", err);
                            });
                        }
                    };

                    window.releaseMic = function() {
                        if (window.app && window.app.rawStream) {
                            window.app.rawStream.getTracks().forEach(function(track) { track.stop(); });
                            window.app.rawStream = null;
                            window.app.micAnalyser = null;
                            console.log("🎙️ Micrófono liberado (Modo Privacidad Activo).");
                        }
                    };

                    // --- 🛡️ MOTOR DE VISIBILIDAD (CONSOLIDADO) ---
                    // Gestionado en la sección de RadioCore para evitar duplicidad y conflictos

                    window.setupCallStream = function(call) {
                        call.on('stream', function(remoteStream) {
                            // 🔒 HARD-LOCK: PROHIBIDO TOCAR - RECONEXIÓN DE EMERGENCIA
                            // Forzamos la inicialización del audio en cada flujo entrante
                            window.initAudio();
                            
                            if (window.app.ctx) {
                                if (window.app.ctx.state === 'suspended') {
                                    window.app.ctx.resume().catch(function(e){});
                                }
                                
                                var streamId = remoteStream.id;
                                // Si ya existe, nos aseguramos de que esté conectado al destino
                                if (window.app.remoteSources[streamId]) {
                                    try { 
                                        window.app.remoteSources[streamId].src.connect(window.app.filter || window.app.masterRxGain);
                                    } catch(e) {}
                                    return;
                                }
                                
                                // 🔒 HARD-LOCK: PROHIBIDO TOCAR - BLINDAJE DE RECEPCIÓN
                                // Controlamos el volumen de recepción para evitar acoples (Feedback Guard)
                                if (window.app.masterRxGain) {
                                    var rxVol = (localStorage.getItem("dspEnabled") === "false") ? 1.5 : 2.5;
                                    window.app.masterRxGain.gain.setTargetAtTime(rxVol, window.app.ctx.currentTime, 0.01);
                                }
                                
                                // BLINDAJE: Muchos navegadores requieren que el audio esté en el DOM para procesarlo
                                var audioTag = document.createElement('audio');
                                audioTag.style.display = "none";
                                audioTag.srcObject = remoteStream;
                                // audioTag.muted = true; // ELIMINADO: Algunos dispositivos necesitan que no esté muteado si es el único tag
                                audioTag.volume = 0.001; // Volumen casi cero para no duplicar pero activar el stream
                                audioTag.setAttribute('playsinline', 'true');
                                document.body.appendChild(audioTag);
                                audioTag.play().catch(function(e){}); 
                                
                                var src = window.app.ctx.createMediaStreamSource(remoteStream);
                                var analyser = window.app.ctx.createAnalyser();
                                analyser.fftSize = 256;
                                src.connect(analyser);
                                
                                // --- 🔊 BLINDAJE DE AUDIO RX (GAMA ALTA) ---
                                // Aplicamos el filtro de radio (bandpass) para un sonido más real
                                if (window.app.filter) {
                                    src.connect(window.app.filter);
                                } else {
                                    src.connect(window.app.masterRxGain);
                                }
                                
                                window.app.remoteSources[streamId] = { src: src, tag: audioTag, analyser: analyser, peerID: call.peer };
                                window.app.remoteAnalysers[call.peer] = analyser;
                                
                                // Forzar el despertar del motor de audio al recibir señal
                                if(window.app.ctx.state === 'suspended') window.app.ctx.resume();
                                console.log("🎙️ Stream recibido de: " + call.peer);
                            }
                        });
                        
                        call.on('close', function() {
                            if (window.app.remoteAnalysers[call.peer]) delete window.app.remoteAnalysers[call.peer];
                        });
                    };
                    
                    window.app.peer.on('call', function(call) {
                        window.app.activeCalls[call.peer] = call;
                        call.answer(window.getStream());
                        window.setupCallStream(call);
                    });

                    window.establishOutgoingCall = function(id) {
                        if (!window.app.peer || window.app.activeCalls[id]) return;
                        var call = window.app.peer.call(id, window.getStream());
                        if (call) {
                            window.app.activeCalls[id] = call;
                            window.setupCallStream(call);
                        }
                    };

                    window.app.heartbeatInterval = setInterval(function() {
                        if (window.app.isTerminated) return;
                        if (window.app.db && window.app.sessionID) {
                            var currentCity = "ESPAÑA (NACIONAL)";
                            var currentCh = "GENERAL";
                            try { 
                                currentCity = localStorage.getItem("lastCity") || "ESPAÑA (NACIONAL)";
                                currentCh = localStorage.getItem("lastChannel") || "GENERAL"; 
                            } catch(e) {}
                            window.app.db.ref("users/" + window.app.sessionID).update({
                                lastSeen: Date.now(), nick: window.app.nick, city: currentCity, channel: currentCh
                            });
                        }
                    }, 10000);
                };

                window.initFirebaseListener = function() {
                    if (window.app.db) {
                        window.app.db.ref("users").on('value', function(s) { 
                            if(s.val()) window.update_remote_users(s.val()); 
                            else window.update_remote_users(null);
                        });

                        // --- 🔔 INBOX LISTENER (CHIVATO PRIVADO) ---
                        // Escuchamos si alguien nos manda un toque para avisarnos de un mensaje privado
                        var mySafeNick = window.sanitizePath(localStorage.getItem("indicativo") || localStorage.getItem("last_indicativo") || "");
                        if (mySafeNick) {
                            window.app.db.ref("inbox/" + mySafeNick).on('value', function(snapshot) {
                                var val = snapshot.val();
                                if (val && val.from && val.timestamp > (Date.now() - 10000)) {
                                    // Si recibimos un aviso fresco (últimos 10s)
                                    if (window.app.ctx) {
                                        window.playUiSound('click'); // Sonido de aviso
                                        if (navigator.vibrate) navigator.vibrate([100, 50, 100]);
                                    }
                                    
                                    // Notificamos a Compose
                                    if (window.sendSystemNotification) {
                                        window.sendSystemNotification("✉️ MENSAJE PRIVADO", "De: " + val.from, "chat_alert");
                                    }
                                    
                                    // Limpiamos el aviso para no repetir
                                    window.app.db.ref("inbox/" + mySafeNick).remove();
                                }
                            });
                        }

                        // --- 💬 CHAT ENGINE: ESCUCHA DE MENSAJES (PÚBLICO/PRIVADO) ---
                        window.updateChatListener = function(target) {
                            if (!window.app || !window.app.db) return;
                            
                            var city = localStorage.getItem("lastCity") || "SEVILLA";
                            var channel = localStorage.getItem("lastChannel") || "GENERAL";
                            var nick = localStorage.getItem("indicativo") || localStorage.getItem("last_indicativo") || "ANÓNIMO";
                            
                            var chatPath = "";
                            if (target) {
                                window.app.currentChatIsPrivate = true;
                                var nicks = [nick, target].sort();
                                chatPath = "private_messages/" + window.sanitizePath(nicks[0]) + "_" + window.sanitizePath(nicks[1]);
                            } else {
                                window.app.currentChatIsPrivate = false;
                                chatPath = "messages/" + window.sanitizePath(city) + "/" + window.sanitizePath(channel);
                            }
                            
                            if (window.currentChatRef) {
                                try { window.currentChatRef.off(); } catch(e) {}
                            }
                            
                            console.log("📡 SINTONIZANDO CHAT: " + chatPath);
                            
                            try {
                                window.currentChatRef = window.app.db.ref(chatPath).limitToLast(50);
                                window.currentChatRef.on('value', function(snapshot) {
                                    var val = snapshot.val();
                                    console.log("📥 DATOS RECIBIDOS [" + chatPath + "]:", val ? Object.keys(val).length + " msg" : "vacío");
                                    if (window.dispatch_chat_update) {
                                        window.dispatch_chat_update(val);
                                    }
                                });
                            } catch(e) {
                                console.error("❌ Error en listener de chat:", e);
                            }
                        };
                        window.updateChatListener();
                    }
                };
            })();
        """)
    }
}

// --- 📡 RADIO SIGNALING: COMPORTAMIENTO FISICO (AGUJA, BEEP, PORTADORA) ---
/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MOTOR DE SEÑALIZACIÓN Y PORTADORA
 * PROHIBIDA LA MODIFICACIÓN DE ESTA LÓGICA SIN PERMISO EXPLÍCITO.
 * GESTIONA: ROGER BEEP, BUSY TONE Y SINCRONIZACIÓN DE RED (AGUJA).
 */
object RadioSignaling {
    fun install() {
        js("""
            window.playSoftEntrySound = function() {
                if (!window.app.ctx) return;
                var ctx = window.app.ctx;
                var now = ctx.currentTime;
                // --- 🔔 BEEP DE CORTESÍA (ENTRADA EN CANAL) ---
                // Sonido limpio y profesional: Doble tono electrónico ascendente
                [600, 900].forEach(function(f, i) {
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'sine';
                    o.frequency.setValueAtTime(f, now + (i * 0.1));
                    g.gain.setValueAtTime(0, now + (i * 0.1));
                    g.gain.linearRampToValueAtTime(0.08, now + (i * 0.1) + 0.02);
                    g.gain.linearRampToValueAtTime(0, now + (i * 0.1) + 0.1);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now + (i * 0.1)); o.stop(now + (i * 0.1) + 0.1);
                });
            };

            window.playBusyTone = function() {
                if (!window.app.ctx) return;
                var now = window.app.ctx.currentTime;
                // Sonido de "Ocupado" Premium: Doble bip electrónico limpio (estilo equipo de élite)
                [880, 880].forEach(function(f, i) {
                    var o = window.app.ctx.createOscillator();
                    var g = window.app.ctx.createGain();
                    o.type = 'sine';
                    o.frequency.setValueAtTime(f, now + (i * 0.1));
                    g.gain.setValueAtTime(0, now + (i * 0.1));
                    g.gain.linearRampToValueAtTime(0.05, now + (i * 0.1) + 0.01);
                    g.gain.linearRampToValueAtTime(0, now + (i * 0.1) + 0.06);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now + (i * 0.1)); o.stop(now + (i * 0.1) + 0.06);
                });
                if (navigator.vibrate) navigator.vibrate([30, 40, 30]);
            };

                window.broadcastPTT = function(active, roger, power) {
                    if(!window.app || !window.app.peer || !window.app.db || window.app.isTerminated) return;
                    
                    // --- 🔒 BLOQUEO DE REBOTE: Solo bloquea el encendido, NUNCA el apagado ---
                    if (active && window.app.isBeeping) return;

                    // --- 🔒 PRIORIDAD DE ESTADO: BLOQUEO DE FUGAS ---
                    window.app.isTransmittingInternal = active;

                    // --- 🛡️ SILENCIAR LOCUTOR AL TRANSMITIR (PRIORIDAD HUMANA) ---
                    if (active && window.speechSynthesis) window.speechSynthesis.cancel();

                    // --- 🛡️ ACTUALIZAR MÚSICA DE FONDO ---
                    setTimeout(function() { if(window.updateBgDucking) window.updateBgDucking(); }, 50);
                    // 🔒 HARD-LOCK: PROHIBIDO TOCAR - MOTOR DE AUDIO TX/RX
                    // Asegura que la voz entre y salga sin cortes.

                    // --- 🛡️ ACTIVACIÓN DE MICRO INTELIGENTE (PRIVACIDAD) ---
                    if (active && !window.app.rawStream) {
                        if (sessionStorage.getItem("mic_denied") === "true") {
                            if (window.dispatch_mic_failure) window.dispatch_mic_failure();
                            return;
                        }
                        if (typeof window.requestMicPermission === 'function') {
                            window.requestMicPermission();
                        }
                    }

                // --- 🛡️ ANTI-COLLISION SYSTEM (REALISTIC RADIO LOCK) ---
                if (active && window.app.rxActiveInternal && !window.app.lastPttState) {
                    if (typeof window.playBusyTone === 'function') window.playBusyTone();
                    if (window.dispatch_ptt_blocked) window.dispatch_ptt_blocked();
                    return;
                }

                if (window.app.lastPttState === active) {
                    // Si ya estábamos transmitiendo y solo cambia la potencia, no disparamos sonidos
                    if (active) {
                        if (power !== undefined) window.app.db.ref("users/" + window.app.sessionID).update({ pwr: power });
                    }
                    return;
                }
                
                var isNewTx = active && !window.app.lastPttState;
                window.app.lastPttState = active;

                // --- 📳 HAPTIC FEEDBACK (PTT FEEL) ---
                if (active && navigator.vibrate) {
                    // navigator.vibrate(40); 
                }

                // --- 🛡️ SINCRONIZACIÓN ATÓMICA DE ESTADOS ---
                if (active) {
                    window.app.isBeeping = false;
                    if(window.dispatch_beeping) window.dispatch_beeping(false);
                    
                    // --- 🚀 DESPERTADOR PUSH (TRIGER GRATUITO) ---
                    // Registramos que hay alguien hablando para que el servidor avise a los que están "dormidos"
                    try {
                        var myCity = localStorage.getItem("lastCity") || "SEVILLA";
                        var myNick = localStorage.getItem("indicativo") || "Estación";
                        var lastPush = localStorage.getItem("last_tx_push") || 0;
                        var now = Date.now();
                        
                        // Solo mandamos un "toque" de red cada 2 minutos para no saturar
                        if (now - lastPush > 120000) {
                            window.app.db.ref("tx_active_notifications").set({
                                city: myCity,
                                nick: myNick,
                                timestamp: now
                            });
                            localStorage.setItem("last_tx_push", now);
                        }
                    } catch(e) {}
                }

                try {
                    var currentSub = localStorage.getItem("lastSubtone") || "0000";
                    var updates = { tx: active, subtone: currentSub };
                    if (active && power !== undefined) updates.pwr = power;
                    
                    // --- 🛡️ SINCRONIZACIÓN ROGER BEEP (AGUJA) ---
                    // Si soltamos con Roger Beep, NO quitamos la portadora (tx) de Firebase aún.
                    // Esto mantiene la aguja arriba en los receptores hasta que termine el pitido.
                    if (!active && roger) {
                        delete updates.tx; // Retrasamos el fin de TX
                        window.app.db.ref("users/" + window.app.sessionID).update(updates);
                    } else {
                        window.app.db.ref("users/" + window.app.sessionID).update(updates);
                    }
                } catch(e) {}

                if (window.app.ctx) {
                    if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                    if (active) {
                        // --- 🛡️ FEEDBACK INSTANTÁNEO AL PULSAR ---
                        if (typeof window.playUiSound === 'function') window.playUiSound('ptt_on');

                        window.app.isTransmittingInternal = true;
                        if ('mediaSession' in navigator) navigator.mediaSession.playbackState = 'playing';
                        if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.02);
                        if (window.app.txGrab) window.app.txGrab.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.02);
                        if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.05);
                        if (window.app.noise) {
                            window.app.noise.gain.cancelScheduledValues(window.app.ctx.currentTime);
                            window.app.noise.gain.setTargetAtTime(0.0001, window.app.ctx.currentTime, 0.01); 
                        }
                        if (typeof window.updateMoniGain === 'function') window.updateMoniGain();
                        
                        // ELIMINADO: Pitido de inicio de PTT (ahora es entrada silenciosa)

                        // --- 🛡️ OPTIMIZACIÓN DE BATERÍA: CONEXIÓN BAJO DEMANDA ---
                        window.app.db.ref("users").once('value', function(snap) {
                            var users = snap.val();
                            var myCity = localStorage.getItem("lastCity") || "SEVILLA";
                            var myChannel = localStorage.getItem("lastChannel") || "GENERAL";
                            var mySubtone = localStorage.getItem("lastSubtone") || "0000";
                            var now = Date.now();
                            var connectedCount = 0;

                            for(var id in users) { 
                                if(id !== window.app.sessionID && users[id].city === myCity && users[id].channel === myChannel && users[id].subtone === mySubtone) {
                                    // --- 🛡️ ESCALABILIDAD: No intentar conectar con más de 25 a la vez ---
                                    if (connectedCount >= 25) break;

                                    var lastSeen = users[id].lastSeen || 0;
                                    if (now - lastSeen < 300000) { 
                                        if (!window.app.activeCalls[id] || !window.app.activeCalls[id].open) {
                                            window.app.activeCalls[id] = window.app.peer.call(id, window.getStream());
                                            connectedCount++;
                                        } else {
                                            connectedCount++;
                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        // --- 🛡️ SINCRONIZACIÓN ATÓMICA TX -> BEEP ---
                        if (roger) {
                            window.app.isBeeping = true;
                            if(window.dispatch_beeping) window.dispatch_beeping(true);
                        }
                        
                        window.app.isTransmittingInternal = false;
                        
                        // --- 🛡️ MANTENIMIENTO DE MICRO (MODO SIEMPRE ACTIVO) ---
                        // El micro se mantiene abierto para permitir VOX y PTT instantáneo en segundo plano.

                        if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.02);
                        if (window.app.txGrab) window.app.txGrab.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.02);
                        if (typeof window.updateMoniGain === 'function') window.updateMoniGain();
                        
                        // --- 🛡️ FEEDBACK INSTANTÁNEO AL SOLTAR ---
                        if (typeof window.playUiSound === 'function') window.playUiSound('ptt_off');
                        
                        if (roger) {
                            // --- 🛡️ BLINDAJE DE SEGURIDAD PTT (CONTUNDENCIA) ---
                            // Al soltar el PTT, silenciamos el micro inmediatamente para que el Roger Beep sea puro.
                            if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                            
                            window.app.isBeeping = true;
                            if(window.dispatch_beeping) window.dispatch_beeping(true);
                            
                            // --- 🔒 MOTOR DE AUDIO: ROGER BEEP LABORATORIO (100% PURO) ---
                            var osc = window.app.ctx.createOscillator();
                            var gLocal = window.app.ctx.createGain();
                            var gRemote = window.app.ctx.createGain();
                            var now = window.app.ctx.currentTime;
                            
                            osc.type = 'sine'; // Pureza senoidal absoluta
                            osc.frequency.setValueAtTime(1955, now); 
                            
                            // Envolvente Remota: Potencia constante para la red
                            gRemote.gain.setValueAtTime(0.0001, now);
                            gRemote.gain.linearRampToValueAtTime(0.10, now + 0.002); 
                            gRemote.gain.setValueAtTime(0.10, now + 0.28); 
                            gRemote.gain.exponentialRampToValueAtTime(0.0001, now + 0.3); 
                            
                            // Envolvente Local: Volumen 4x más bajo y conexión DIRECTA
                            // Conectamos directamente a ctx.destination para evitar el phasing/vibración de los buses
                            gLocal.gain.setValueAtTime(0.0001, now);
                            gLocal.gain.linearRampToValueAtTime(0.03, now + 0.002);
                            gLocal.gain.setValueAtTime(0.03, now + 0.28);
                            gLocal.gain.exponentialRampToValueAtTime(0.0001, now + 0.3);

                            osc.connect(gRemote); 
                            if (window.app.txBus) gRemote.connect(window.app.txBus); 
                            
                            osc.connect(gLocal); 
                            // --- 🛡️ FIX CRÍTICO: Bypass de masterOut para eliminar interferencia de fase ---
                            gLocal.connect(window.app.ctx.destination);

                            // --- 🎯 EXACT HARDWARE SYNC ---
                            osc.onended = function() {
                                // El pitido ha terminado físicamente en el motor de audio
                                window.app.isBeeping = false;
                                if(window.dispatch_beeping) window.dispatch_beeping(false);
                                
                                // Restauramos ambiente
                                if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.0, window.app.ctx.currentTime, 0.05);
                                if (window.app.noise) {
                                    window.app.noise.gain.cancelScheduledValues(window.app.ctx.currentTime);
                                    window.app.noise.gain.setTargetAtTime(Math.max(0.0001, window.app.lastNoiseLevel), window.app.ctx.currentTime, 0.05);
                                }
                                
                                // 📡 DETERMINISTIC TAIL: La aguja baja SOLO cuando el sonido termina físicamente.
                                if (!window.app.isTransmittingInternal) {
                                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                                    if(window.dispatch_ptt_sync) window.dispatch_ptt_sync(false);
                                }

                                window.app.voxLockoutTimestamp = Date.now() + 2500;
                            };

                            osc.start(now); 
                            osc.stop(now + 0.3);
// Margen de seguridad para la caída exponencial
                        }
else {
                            // Cierre sin Roger Beep: Sincronización directa
                            if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.0, window.app.ctx.currentTime, 0.05);
                            if (window.app.noise) {
                                window.app.noise.gain.cancelScheduledValues(window.app.ctx.currentTime);
                                window.app.noise.gain.setTargetAtTime(Math.max(0.0001, window.app.lastNoiseLevel), window.app.ctx.currentTime, 0.05);
                            }
                            window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                        }
                    }
                }
            };

            // ELIMINADO: El antiguo loop de aguja ha sido reemplazado por VOXEngine.install()
        """)
    }
}

// --- 🎙️ VOX ENGINE: GESTIÓN DE VOZ AUTOMÁTICA (BLINDADO) ---
/**
 * 🔒 HARD-LOCK: MOTOR DE DETECCIÓN VOX PRO-GRADE
 * ESTADO: INGENIERÍA DE ÉLITE - AUTO-ADAPTATIVO
 * 
 * IMPLEMENTA:
 * - Dynamic Noise Floor Tracking (Elimina ruido de motores/ventiladores).
 * - Roger Beep Lockout (Sordera sincronizada post-transmisión).
 * - Inteligencia de Frecuencia (Filtra picos inaudibles).
 */
object VOXEngine {
    fun install() {
        js("""
            window.voxEngine = {
                lastProcess: 0,
                localLevel: 0,
                noiseFloor: 0,
                voiceMax: 0.1, // Perfil de voz aprendido
                
                process: function() {
                    if (!window.app || !window.app.micAnalyser) return;
                    
                    var now = Date.now();
                    if (now - this.lastProcess < 16) return;
                    this.lastProcess = now;

                    var d = new Uint8Array(window.app.micAnalyser.fftSize); 
                    window.app.micAnalyser.getByteTimeDomainData(d);
                    var max = 0;
                    for(var i=0; i<d.length; i++) {
                        var v = Math.abs(d[i] - 128);
                        if (v > max) max = v;
                    }
                    var instantLevel = Math.min(1.0, (max / 128) * 6.5); // Calibración: 6.5 para que la voz mueva la aguja con elegancia
                    this.localLevel = instantLevel;

                    // 1. APRENDIZAJE CONTINUO DEL RUIDO (Noise Floor)
                    // Si el nivel es bajo, lo asimilamos como ruido de fondo (motor, ventilador)
                    if (instantLevel < this.noiseFloor || this.noiseFloor === 0) {
                        this.noiseFloor = (this.noiseFloor * 0.98) + (instantLevel * 0.02);
                    } else {
                        // El ruido de fondo se sigue muy lentamente para ignorar picos de voz
                        this.noiseFloor = (this.noiseFloor * 0.9995) + (instantLevel * 0.0005);
                    }

                    // 2. PERFIL DE VOZ (Aprende tu volumen máximo al hablar)
                    if (window.app.isVoxTransmitting && instantLevel > this.voiceMax) {
                        this.voiceMax = (this.voiceMax * 0.9) + (instantLevel * 0.1);
                    }

                    var remoteLevel = 0;
                    for (var peerID in window.app.remoteAnalysers) {
                        var analyser = window.app.remoteAnalysers[peerID];
                        if (analyser) {
                            var rd = new Uint8Array(analyser.fftSize);
                            analyser.getByteTimeDomainData(rd);
                            var rMax = 0;
                            for(var j=0; j<rd.length; j++) {
                                var rv = Math.abs(rd[j] - 128);
                                if (rv > rMax) rMax = rv;
                            }
                            
                            var pwr = (window.app.remotePower && window.app.remotePower[peerID]) ? window.app.remotePower[peerID] : 0.7;
                            var rL = Math.min(1.0, (rMax / 128) * 5.5 * pwr);
                            if (rL > remoteLevel) remoteLevel = rL;
                        }
                    }

                    // 3. Notificar nivel a la aguja (Compose)
                    // 🔒 HARD-LOCK: PROHIBIDO TOCAR - FÍSICA DE AGUJA (PORTADORA + VOZ)
                    var isSignaling = window.app.isTransmittingInternal || window.app.isBeeping || window.app.isAntennaTesting;
                    var displayLevel = 0;

                    if (isSignaling) {
                        // En modo TEST o TX, la aguja muestra la voz real
                        var basePower = window.app.isAntennaTesting ? 0.75 : 0.75;
                        displayLevel = Math.max(basePower, this.localLevel);
                    } else if (window.app.rxActiveInternal) {
                        // Buscamos el nivel de voz del que está hablando ahora mismo
                        var voiceLevel = 0;
                        var carrierPower = 0.7;
                        for (var peerID in window.app.remoteAnalysers) {
                             var analyser = window.app.remoteAnalysers[peerID];
                             if (analyser) {
                                 var rd = new Uint8Array(analyser.fftSize);
                                 analyser.getByteTimeDomainData(rd);
                                 var rMax = 0;
                                 for(var j=0; j<rd.length; j++) {
                                     var rv = Math.abs(rd[j] - 128);
                                     if (rv > rMax) rMax = rv;
                                 }
                                 voiceLevel = Math.min(1.0, (rMax / 128) * 1.5); // Modulación sutil (factor 1.5)
                                 carrierPower = (window.app.remotePower && window.app.remotePower[peerID]) ? window.app.remotePower[peerID] : 0.7;
                                 break; // Solo seguimos al primer transmisor activo
                             }
                        }
                        // La aguja se sitúa en la potencia de portadora + un pequeño baile de voz
                        displayLevel = carrierPower + (voiceLevel * 0.15);
                    } else {
                        displayLevel = remoteLevel; // Ruido de fondo/QRM
                    }

                    if (window.app.isBeeping) displayLevel = 0.98; 
                    if(window.dispatch_mic) window.dispatch_mic(displayLevel);

                    // 4. Lógica VOX Adaptativa
                    if (window.app.voxActive) {
                        // --- 🛡️ BLINDAJE ANTI-RECOIL (OBLIGATORIO) ---
                        // Bloqueamos VOX si: hay alguien hablando (RX), estamos pitando (Beep) o estamos en periodo de guarda (Lockout)
                        var isLocked = window.app.isBeeping || 
                                     (window.app.voxLockoutTimestamp && now < window.app.voxLockoutTimestamp) || 
                                     (window.app.rxActiveInternal && !window.app.isVoxTransmitting);
                        
                        if (isLocked) {
                            if (window.app.isVoxTransmitting) {
                                window.app.isVoxTransmitting = false;
                                window.broadcastPTT(false, true);
                                // Tras un corte forzado, 1 segundo de "sordera" de seguridad
                                window.app.voxLockoutTimestamp = now + 1000;
                            }
                            return;
                        }
                        
                        var threshold = 1.0 - (window.app.voxSens * 0.99); 
                        // REGLA DE ORO: Solo disparamos si el nivel está por encima del ruido aprendido
                        var effectiveLevel = this.localLevel - (this.noiseFloor * 0.6); 
                        
                        if (effectiveLevel > Math.max(0.05, threshold)) {
                            if (!window.app.isVoxTransmitting) {
                                window.broadcastPTT(true, true);
                                window.app.isVoxTransmitting = true;
                                if(window.dispatch_vox_sync) window.dispatch_vox_sync(true);
                            }
                            window.app.voxHangTimer = 60; // ~1.0 segundo de cola (Hang Time) para cierre más ágil
                        } else if (window.app.isVoxTransmitting) {
                            if (window.app.voxHangTimer > 0) {
                                window.app.voxHangTimer--;
                            } else {
                                window.app.isVoxTransmitting = false;
                                window.broadcastPTT(false, true);
                                if(window.dispatch_vox_sync) window.dispatch_vox_sync(false);
                                // --- 🔒 LOCKOUT POST-TRANSMISIÓN ---
                                // Sordera total absoluta tras soltar (2.5 segundos para cubrir Roger Beep + Eco)
                                window.app.voxLockoutTimestamp = now + 2500; 
                            }
                        }
                    }

                    // --- 🛡️ HARD-LOCK: SEGURIDAD CRÍTICA DE MICRO (PREVENCIÓN MICRO ABIERTO) ---
                    // Si el sistema NO está transmitiendo por PTT manual, ni por VOX, ni está el Roger Beep activo:
                    // Forzamos el cierre físico total de los buses de audio para garantizar privacidad absoluta.
                    if (!window.app.isTransmittingInternal && !window.app.isVoxTransmitting && !window.app.isBeeping) {
                         if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                         if (window.app.txGrab) window.app.txGrab.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                    }
                }
            };
            
            function voxLoop() {
                try {
                    window.voxEngine.process();
                } catch(e) { console.error("VOX Error:", e); }
                requestAnimationFrame(voxLoop);
            }
            voxLoop();
        """)
    }
}
object RadioBridge {
    fun install() {
        js("""
            window.shareWhatsApp = function(city, channel, subtone, proRole) {
                window.shareSocial(city, channel, subtone, proRole, "WhatsApp");
            };

            window.shareSocial = function(city, channel, subtone, proRole, platform) {
                // --- 🔒 HARD-LOCK: MOTOR DE COMPARTIR SOCIAL (PROTEGIDO) ---
                // Gestión inteligente de plataformas y copia al portapapeles
                var cities = ["ESPAÑA (NACIONAL)", "SEVILLA", "MADRID", "BARCELONA", "VALENCIA", "ALICANTE", "MÁLAGA", "MURCIA", "CÁDIZ", "BIZKAIA", "A CORUÑA", "ISLAS BALEARES", "LAS PALMAS", "STA. CRUZ TENERIFE", "ASTURIAS", "ZARAGOZA", "PONTEVEDRA", "GRANADA", "TARRAGONA", "CÓRDOBA", "GIPUZKOA", "GIRONA", "ALMERÍA", "TOLEDO", "BADAJOZ", "NAVARRA", "JAÉN", "CASTELLÓN", "CANTABRIA", "HUELVA", "VALLADOLID", "CIUDAD REAL", "LEÓN", "LLEIDA", "ALBACETE", "BURGOS / SORIA", "SALAMANCA / ÁVILA", "LOGROÑO / ÁLAVA", "CÁCERES / SEGOVIA", "LUGO / OURENSE / PALENCIA / ZAMORA", "CUENCA / TERUEL / GUADALAJARA / CEUTA / MELILLA"];
                var canalIdx = cities.indexOf(city); // Ajustado para que España sea el 0 o 1 según prefieras
                var baseUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
                var shareUrl = baseUrl + "?city=" + encodeURIComponent(city) + "&channel=" + encodeURIComponent(channel) + "&subtone=" + encodeURIComponent(subtone);
                
                var text = "";
                var isActivity = (window.app && window.app.motoActive);
                
                if (isActivity) {
                    text = "🏍️ ¡ESTOY EN RUTA!\n\nÚnete a mi grupo en directo. Con filtro de viento y red WiFi sin cobertura. Pincha aquí:\n" + shareUrl;
                } else if (proRole === "RADIO_STATION") {
                    var stationName = window.app.currentBgStation || "Radio FM";
                    text = "🎙️ ¡ESTOY ESCUCHANDO " + stationName.toUpperCase() + "!\n\nTe invito a sintonizar conmigo en directo desde aquí:\n" + shareUrl;
                } else if (proRole && proRole !== "CIUDADANO") {
                    var finalShareUrl = shareUrl + "&pro=true";
                    text = "💼 RADAR PROFESIONAL: " + city + "\n\nEstamos conectando ofertas y profesionales por voz en tiempo real. Entra aquí:\n" + finalShareUrl;
                } else {
                    var salaText = (channel === 'GENERAL') ? "SALA GENERAL" : "SALA " + channel;
                    text = "📻 ¡ENTRA EN MI FRECUENCIA!\n\nTe espero en " + city + " [" + salaText + "]. Música, noticias y voz en directo. ¡Pínchale! 👇\n" + shareUrl;
                }
                
                // --- 🛡️ BRIDGE NATIVO: COMPARTIR ---
                if (window.AndroidApp && typeof window.AndroidApp.shareText === 'function') {
                    window.AndroidApp.shareText(text);
                    return;
                }

                var url = "";
                switch(platform) {
                    case "WhatsApp": url = "https://wa.me/?text=" + encodeURIComponent(text); break;
                    case "Facebook": url = "https://www.facebook.com/sharer/sharer.php?u=" + encodeURIComponent(shareUrl); break;
                    case "Instagram": 
                    case "TikTok":
                    case "InfoJobs":
                        // Estas redes no permiten compartir texto/URL directamente vía web intent de forma fiable,
                        // copiamos al portapapeles y avisamos al usuario.
                        navigator.clipboard.writeText(text).then(function() {
                            alert("Texto copiado al portapapeles. ¡Pégalo ahora en " + platform + "!");
                        });
                        if (platform === "InfoJobs") {
                            window.open("https://www.infojobs.net/", "_blank");
                        }
                        return;
                    default: url = "https://wa.me/?text=" + encodeURIComponent(text);
                }

                if (url) {
                    if (/Android|iPhone|iPad|iPod/i.test(navigator.userAgent)) {
                        window.location.href = url;
                    } else {
                        window.open(url, '_blank');
                    }
                }
            };

            /**
             * 🔒 HARD-LOCK: PROTECCIÓN DE IDENTIDAD Y SESIÓN
             * Evita conflictos de "Indicativo Ocupado" mediante validación de SessionID persistente.
             */
            window.checkNickAvailability = function(nick, city) {
                if (!window.app.db) return Promise.resolve(true);
                
                // --- 🛡️ MEJORA: RECUPERAR SESIÓN ANTES DE CHEQUEAR DISPONIBILIDAD ---
                var safeNick = nick.replace(/[^a-zA-Z0-9]/g, "");
                
                // Intentamos reconstruir el ID de sesión para compararlo correctamente
                var deviceID = "";
                if (window.AndroidApp && typeof window.AndroidApp.getAndroidId === 'function') {
                    deviceID = "A_" + window.AndroidApp.getAndroidId();
                } else {
                    deviceID = localStorage.getItem("on_device_id");
                }
                
                var currentSessionID = window.app.sessionID || localStorage.getItem("session_id_" + safeNick) || (safeNick + "_" + deviceID);

                return window.app.db.ref("users").once('value').then(function(snapshot) {
                    var users = snapshot.val();
                    if (!users) return true;
                    var keys = Object.keys(users);
                    for (var i = 0; i < keys.length; i++) {
                        var u = users[keys[i]];
                        // Si el nick (limpio) existe en la misma ciudad y no somos nosotros (por ID de sesión persistente)
                        if (u.nick === safeNick && u.city === city && keys[i] !== currentSessionID) {
                            return false; 
                        }
                    }
                    return true;
                });
            };

            window.setupSystemListeners = function() {
                window.addEventListener('popstate', function(event) {
                    history.pushState(null, document.title, location.href);
                    if(window.trigger_back) window.trigger_back();
                });
                history.pushState(null, document.title, location.href);

                // --- 🛡️ MULTI-TAB CONTROLLER (PREMIUM) ---
                if (window.BroadcastChannel) {
                    window.app.bc = new BroadcastChannel('on_air_spain_sync');
                    window.app.bc.onmessage = function(e) {
                if (!window.app) return; // Protección contra inicialización temprana
                if (e.data.type === 'QUERY_ACTIVE') {
                    // SÓLO RESPONDER SI NO ESTAMOS EN MODO REDIRECCIÓN/SINCRONIZACIÓN
                    if (document.getElementById('redir-ui')) return;
                    if (window.app.bc) window.app.bc.postMessage({ type: 'I_AM_ACTIVE', instanceID: window.app.instanceID });
                } else if (e.data.type === 'REMOTE_NAVIGATE') {
                            var p = e.data.params;
                            if (p.city) localStorage.setItem("lastCity", p.city);
                            if (p.channel) localStorage.setItem("lastChannel", p.channel);
                            if (p.subtone) localStorage.setItem("lastSubtone", p.subtone);
                            window.app.bc.postMessage({ type: 'NAVIGATE_ACK' });
                            location.reload(); 
                        }
                    };
                }
            };

            window.handleEarlyRedirect = function() {
                // --- 🛡️ MODO DIRECTO (REQUERIDO POR USUARIO) ---
                // Desactivamos la intercepción de sincronización para que los enlaces de WhatsApp
                // abran la app inmediatamente en la nueva pestaña/ventana.
                // El sistema de TAKEOVER por Firebase (Monitor de Sesión Activa) ya se encarga
                // de detectar sesiones duplicadas y avisar en la pestaña antigua.
                return Promise.resolve(false);
            };

            // --- 🔊 MOTOR DE SONIDO INSTANTÁNEO (LATENCIA CERO) ---
            window.playUiSound = function(type) {
                if (!window.app || !window.app.ctx) return;
                var ctx = window.app.ctx;
                var now = ctx.currentTime;
                
                if (type === 'ptt_on') {
                    // --- 🛡️ RELÉ MECÁNICO ELIMINADO ---
                    // Entrada silenciosa requerida por el usuario
                } else if (type === 'ptt_off') {
                    // --- 🛡️ RELÉ DE DESENCLAVAMIENTO SUTIL ---
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'sine';
                    o.frequency.setValueAtTime(100, now);
                    o.frequency.exponentialRampToValueAtTime(50, now + 0.012);
                    g.gain.setValueAtTime(0, now);
                    g.gain.linearRampToValueAtTime(0.02, now + 0.002);
                    g.gain.linearRampToValueAtTime(0, now + 0.012);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 0.012);
                } else if (type === 'click') {
                    // Beep instantáneo (Sin rampas lentas)
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'sine';
                    o.frequency.setValueAtTime(880, now);
                    g.gain.setValueAtTime(0.06, now);
                    g.gain.setValueAtTime(0.001, now + 0.04);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 0.04);
                } else if (type === 'switch') {
                    // Relé ultra-rápido
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'triangle';
                    o.frequency.setValueAtTime(600, now);
                    g.gain.setValueAtTime(0.03, now);
                    g.gain.setValueAtTime(0.001, now + 0.02);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 0.02);
                } else if (type === 'static') {
                    // Mismo sonido que los botones (Instantáneo y Profesional)
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'triangle';
                    o.frequency.setValueAtTime(600, now);
                    g.gain.setValueAtTime(0.03, now);
                    g.gain.setValueAtTime(0.001, now + 0.02);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 0.02);
                } else if (type === 'message') {
                    // --- 🔔 SONIDO DE NOTIFICACIÓN PREMIUM (DING-DONG) ---
                    // Doble tono armónico limpio y cristalino
                    [1046.50, 1318.51].forEach(function(f, i) {
                        var o = ctx.createOscillator();
                        var g = ctx.createGain();
                        o.type = 'sine';
                        o.frequency.setValueAtTime(f, now + (i * 0.08));
                        g.gain.setValueAtTime(0, now + (i * 0.08));
                        g.gain.linearRampToValueAtTime(0.06, now + (i * 0.08) + 0.02);
                        g.gain.linearRampToValueAtTime(0, now + (i * 0.08) + 0.2);
                        o.connect(g); g.connect(window.app.masterOut);
                        o.start(now + (i * 0.08)); o.stop(now + (i * 0.08) + 0.2);
                    });
                } else if (type === 'siren') {
                    // Sirena de emergencia (Disuasoria) - Versión Extendida Profesional
                    var o = ctx.createOscillator();
                    var g = ctx.createGain();
                    o.type = 'triangle';
                    o.frequency.setValueAtTime(400, now);
                    // 4 ciclos de subida/bajada (0.8s cada uno)
                    for(var i=0; i<4; i++) {
                        o.frequency.exponentialRampToValueAtTime(800, now + (i * 0.8) + 0.4);
                        o.frequency.exponentialRampToValueAtTime(400, now + (i * 0.8) + 0.8);
                    }
                    g.gain.setValueAtTime(0, now);
                    g.gain.linearRampToValueAtTime(0.15, now + 0.1);
                    g.gain.linearRampToValueAtTime(0.15, now + 3.0);
                    g.gain.linearRampToValueAtTime(0, now + 3.2);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 3.2);
                }
            };

            window.vibratePtt = function() {
                if (navigator.vibrate) {
                    // navigator.vibrate(35); // ELIMINADO: Chasquido molesto
                }
            };

            // --- 🛡️ SISTEMA DE LOOPBACK (TEST DE ANTENA) ---
            window.toggleAntennaLoopback = function(active) {
                if (!window.app || !window.app.moniGain) return;
                window.app.isAntennaTesting = (active === true);
                
                console.log("📡 MODO TEST ANTENA: " + (active ? "ON" : "OFF"));
                
                if (active) {
                    // --- 🛡️ MEJORA CRÍTICA: ACTIVAR MICRO PARA EL TEST ---
                    if (!window.app.rawStream && window.requestMicPermission) {
                        window.requestMicPermission();
                    }
                    
                    // Obligamos al vúmetro a subir un poco de base para indicar actividad
                    if (window.dispatch_mic) window.dispatch_mic(0.75);
                }
                
                // Sincronizamos el volumen del monitor inmediatamente
                if (window.updateMoniGain) window.updateMoniGain();
            };

            window.getGpsLink = function() {
                return new Promise(function(resolve) {
                    if (!navigator.geolocation) return resolve(null);
                    navigator.geolocation.getCurrentPosition(function(pos) {
                        var url = "https://www.google.com/maps?q=" + pos.coords.latitude + "," + pos.coords.longitude;
                        resolve(url);
                    }, function(err) { 
                        console.warn("GPS Error:", err);
                        resolve(null); 
                    }, { 
                        timeout: 10000, 
                        enableHighAccuracy: true,
                        maximumAge: 0
                    });
                });
            };

            window.detectCityByGps = function() {
                return new Promise(function(resolve) {
                    if (!navigator.geolocation) return resolve(null);
                    navigator.geolocation.getCurrentPosition(function(pos) {
                        var lat = pos.coords.latitude;
                        var lon = pos.coords.longitude;
                        // Usamos el servicio Nominatim de OSM para geocodificación inversa
                        // Se incluye un User-Agent según políticas de OSM
                        fetch("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&zoom=10", {
                            headers: { "Accept-Language": "es" }
                        })
                            .then(function(r) { return r.json(); })
                            .then(function(data) {
                                if (data && data.address) {
                                    var city = data.address.city || data.address.town || data.address.village || data.address.county || data.address.state;
                                    console.log("📍 Ciudad detectada por GPS:", city);
                                    resolve(city);
                                } else {
                                    resolve(null);
                                }
                            })
                            .catch(function(e) { 
                                console.error("Error Nominatim:", e);
                                resolve(null); 
                            });
                    }, function(err) { 
                        console.warn("GPS Error City:", err);
                        resolve(null); 
                    }, { 
                        timeout: 10000, 
                        enableHighAccuracy: true,
                        maximumAge: 300000
                    });
                });
            };
        """)
        js("window.setupSystemListeners();")
    }

    fun setupDispatchers(
        win: dynamic,
        onMic: (Float) -> Unit,
        onBeep: (Boolean) -> Unit,
        onPttSync: (Boolean) -> Unit,
        onPttBlocked: () -> Unit,
        onReplayEmpty: () -> Unit,
        onReplayStart: () -> Unit,
        onBack: () -> Unit,
        onNickConflict: (String) -> Unit,
        onUsersUpdate: (dynamic) -> Unit,
        onChatUpdate: (dynamic) -> Unit,
        onReplayProgress: (Float) -> Unit,
        onReplayAvailable: (Boolean) -> Unit,
        onChatOpen: (String?) -> Unit,
        onMicFailure: () -> Unit,
        onIntegrityStatus: (Boolean) -> Unit,
        onBgStation: (String?) -> Unit,
        onBgGenreChange: (String) -> Unit,
        onIncomingAlert: (String, String, String) -> Unit,
        onVoxSync: (Boolean) -> Unit,
        onNasaImage: (String?, String?, String?) -> Unit,
        onDgtUpdate: (String?, String?) -> Unit,
        onCodeCaptured: (String, String) -> Unit,
        onWifiListReceived: (String) -> Unit,
        onEngineeringFinished: () -> Unit
    ) {
        win.dispatch_mic = onMic
        win.dispatch_beeping = onBeep
        win.dispatch_ptt_sync = onPttSync
        win.dispatch_ptt_blocked = onPttBlocked
        win.dispatch_replay_empty = onReplayEmpty
        win.dispatch_replay_start = onReplayStart
        win.dispatch_replay_progress = onReplayProgress
        win.dispatch_replay_available = onReplayAvailable
        win.trigger_back = onBack
        win.dispatch_nick_conflict = onNickConflict
        win.update_remote_users = onUsersUpdate
        win.dispatch_chat_update = onChatUpdate
        win.dispatch_mic_failure = onMicFailure
        win.dispatch_integrity_status = onIntegrityStatus
        win.dispatch_bg_station = onBgStation
        win.dispatch_bg_genre_change = onBgGenreChange
        win.dispatch_vox_sync = onVoxSync
        win.dispatch_nasa_image = onNasaImage
        win.dispatch_dgt_update = onDgtUpdate
        win.dispatch_code_captured = onCodeCaptured
        win.dispatch_wifi_list = onWifiListReceived
        win.dispatch_engineering_finished = onEngineeringFinished
        win.dispatch_chat_open = { target: String? -> 
            onChatOpen(target)
            // Forzar apertura de chat en la terminal local
            js("if(window.app) { window.app.forceChatOpen = true; window.app.forceChatTarget = target; }")
        }
        win.dispatch_incoming_alert = onIncomingAlert
    }

    fun getDeviceHeading(win: dynamic): Float {
        return try {
            if (win.AndroidApp != null && win.AndroidApp.getDeviceHeading != null) {
                win.AndroidApp.getDeviceHeading() as Float
            } else 0f
        } catch (e: Exception) { 0f }
    }

    fun getDeviceTilt(win: dynamic): Float {
        return try {
            if (win.AndroidApp != null && win.AndroidApp.getDeviceTilt != null) {
                win.AndroidApp.getDeviceTilt() as Float
            } else 0f
        } catch (e: Exception) { 0f }
    }
}

// =======================================================
// 🔒 HARD-LOCK: WEBAPP ENTRY POINT (JS STABLE)
// PROTECCIÓN CRÍTICA: No usar anotaciones WasmJsInterop.
// =======================================================
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val win: dynamic = js("window")
    win.app_start_time = getTimeMillis()
    
    // --- 🛡️ MONITOR DE ERRORES CRÍTICOS ---
    win.onerror = fun(msg: dynamic, url: dynamic, line: dynamic): Boolean {
        val message = msg.toString()
        // Ignorar errores de reconexión de PeerJS (son manejados por el Guardián)
        if (message.contains("cannot reconnect") || message.contains("disconnected from the server")) {
            return true 
        }
        // Solo mostrar alerta si es un error fatal de arranque (antes de 5 segundos)
        val startTime = win.app_start_time.toString().toDouble()
        if (getTimeMillis() - startTime < 5000) {
            win.alert("⚠️ ERROR DE ARRANQUE:\n" + msg + "\nEn: " + url + " línea " + line)
        }
        return false
    }

    println("🚀 ON AIR SPAIN: Iniciando motor de radio...")
    val root = document.body ?: run {
        println("❌ Error: No se encontró document.body")
        return
    }
    
    // 🛡️ Activar Infraestructura Blindada
    RadioCore.install()
    RadioFmEngine.install()
    MoniGuard.install()
    VOXEngine.install()
    RadioSignaling.install()
    RadioBridge.install()

    win.handleEarlyRedirect().then(fun(redirected: Boolean): Any? {
        if (redirected == true) return null

        ComposeViewport(root) {
            // 🚀 QUITAR CARGADOR HTML CUANDO COMPOSE ARRANCA
            LaunchedEffect(Unit) {
                delay(1500)
                js("""
                    var loader = document.getElementById('app-loader');
                    if (loader) {
                        loader.style.opacity = '0';
                        setTimeout(function() { loader.remove(); }, 600);
                    }
                """)
            }

            val micLevelState = remember { mutableStateOf(0f) }
        val isBeepingState = remember { mutableStateOf(false) }
        val voxActiveState = remember { mutableStateOf(false) }
        val remoteUsersState = remember { mutableStateListOf<RemoteUser>() }
        val chatMessagesState = remember { mutableStateListOf<ChatMessage>() }
        val transmittingFriends = remember { mutableStateMapOf<String, Boolean>() }
        val knownNicks = remember { mutableStateOf(setOf<String>()) } // Rastreo para bienvenidas
        val rxNameState = remember { mutableStateOf<String?>(null) }
        val isCodedRxState = remember { mutableStateOf(false) } // Nuevo estado para señal codificada
        val notificationState = remember { mutableStateOf<AppNotification?>(null) }
        val showInstallDialog = remember { mutableStateOf(false) }
        val backTrigger = remember { mutableStateOf(0) }
        val pttExternalState = remember { mutableStateOf(false) }
        val pttBlockedTrigger = remember { mutableStateOf(false) }
        val replayProgressState = remember { mutableStateOf(0f) }
        val isReplayReadyState = remember { mutableStateOf(false) }
        val forceInitialScreenState = remember { mutableStateOf(false) }
        val audioIntegrityState = remember { mutableStateOf(true) }
        val bgStationNameState = remember { mutableStateOf<String?>(null) }
        val nasaImageUrlState = remember { mutableStateOf<String?>(localStorage.getItem("cache_nasa_img")) }
        val nasaImageTitleState = remember { mutableStateOf<String?>(localStorage.getItem("cache_nasa_title")) }
        val nasaImageExplanationState = remember { mutableStateOf<String?>(localStorage.getItem("cache_nasa_desc")) }
        val dgtTextState = remember { mutableStateOf<String?>(localStorage.getItem("cache_dgt")) }
        val dgtImageUrlState = remember { mutableStateOf<String?>(localStorage.getItem("cache_dgt_img")) }
        val forceChatOpenState = remember { mutableStateOf(false) }
        val forceChatTargetState = remember { mutableStateOf<String?>(null) }
        val wifiVerificationResultState = remember { mutableStateOf<String?>(null) }

        val win: dynamic = js("window")
        
        // Configurar el puente de eventos JS -> Compose
        RadioBridge.setupDispatchers(
            win = win,
            onMic = { micLevelState.value = it },
            onBeep = { isBeepingState.value = it },
            onPttSync = { pttExternalState.value = it },
            onPttBlocked = { pttBlockedTrigger.value = true },
            onReplayEmpty = {
                notificationState.value = AppNotification(
                    title = "HISTORIAL VACÍO",
                    message = "Aún no ha sonado nada en este barrio para repetir.",
                    type = NotificationType.Info
                )
            },
            onReplayStart = {
                notificationState.value = AppNotification(
                    title = "REPLAY ACTIVO",
                    message = "Escuchando los últimos 15 segundos del barrio.",
                    type = NotificationType.Success
                )
            },
            onBack = { backTrigger.value++ },
            onNickConflict = { conflictNick ->
                notificationState.value = AppNotification(
                    title = "INDICATIVO OCUPADO",
                    message = "El nombre '$conflictNick' ya está en uso en esta ciudad. Por favor, elige otro.",
                    type = NotificationType.Warning
                )
                forceInitialScreenState.value = true
            },
            onUsersUpdate = { users ->
                try {
                    remoteUsersState.clear()
                    var currentTx: String? = null
                    var anyCodedTx = false
                    var channelUsersCount = 0
                    var isAnyRemoteTx = false
                    val myCity = localStorage.getItem("lastCity") ?: "SEVILLA"
                    val myCh = localStorage.getItem("lastChannel") ?: "GENERAL"
                    val mySub = localStorage.getItem("lastSubtone") ?: "0000"
                    val mySessionID = if (win.app != null) win.app.sessionID as? String else null
                    val now = Date.now()
                    
                    val currentFriends = (localStorage.getItem("friends") ?: "").split(",").toSet()
                    val currentBlocked = (localStorage.getItem("blockedUsers") ?: "").split(",").toSet()

                    val newKnownNicks = mutableSetOf<String>()
                    val arrivalGreetings = mutableListOf<String>()

                    if (users != null && users != undefined) {
                        val keys = js("Object").keys(users)
                        for (i in 0 until (keys.length as Int)) {
                            val k = keys[i] as String
                            if (k == mySessionID) continue 
                            val u = users[k]
                            if (u == null || u == undefined) continue
                            
                            val userNick = u.nick as? String ?: ""
                            val lastSeen = try { if (u.lastSeen != null) u.lastSeen.toString().toDouble() else 0.0 } catch(e: Exception) { 0.0 }
                            
                            // --- 🛡️ PROTOCOLO DE EXPIRACIÓN ACELERADA (15s) ---
                            if (userNick.isNotEmpty() && (now - lastSeen) < 15000.0) {
                                val isTransmitting = u.tx == true
                                val userCity = u.city as? String ?: "SEVILLA"
                                val userChannel = u.channel as? String ?: "GENERAL"
                                
                                // --- 🎙️ DETECCIÓN DE NUEVOS PARA SALUDO ---
                                if (userCity == myCity && userChannel == myCh) {
                                    newKnownNicks.add(userNick)
                                    if (!knownNicks.value.contains(userNick) && knownNicks.value.isNotEmpty()) {
                                        arrivalGreetings.add(userNick)
                                    }
                                }
                                val userSubtone = u.subtone as? String ?: "0000"
                                val userPwr = try { (u.pwr as? Double ?: 0.7).toFloat() } catch(e: Exception) { 0.7f }
                                val proRole = u.proRole as? String ?: "CIUDADANO"
                                val isProSeeking = u.proSeeking == true
                                val isWorkAvailable = u.workStatus == true
                                val isSOS = u.isSOS == true
                                val proRep = try { (u.proRep as? Double ?: 1.0).toFloat() } catch(e: Exception) { 1.0f }
                                val banned = u.banned == true
                                val isMotoUser = u.isMoto == true
                                val userLat = try { u.lat as? Double } catch(e: Exception) { null }
                                val userLon = try { u.lon as? Double } catch(e: Exception) { null }
                                
                                // --- 🛡️ AUTO-BLOQUEO DEL MAL ACTOR ---
                                if (k == mySessionID && banned) {
                                    js("window.location.href = 'privacy.html?banned=true';")
                                }

                                if (currentBlocked.contains(k) || banned) continue

                                if (isTransmitting && currentFriends.contains(userNick)) {
                                    if (transmittingFriends[userNick] != true) {
                                        if (userCity != myCity || userChannel != myCh) {
                                            val displayChannel = if (userChannel == "GENERAL") "CREAR/ENTRAR" else userChannel
                                            notificationState.value = AppNotification(
                                                title = "FAVORITO AL AIRE",
                                                message = "$userNick está emitiendo en $userCity - $displayChannel",
                                                type = NotificationType.Success
                                            )
                                        }
                                        transmittingFriends[userNick] = true
                                    }
                                } else if (!isTransmitting) {
                                    transmittingFriends.remove(userNick)
                                }

                                if (isTransmitting && win.app != null) {
                                    if (win.app.remotePower == null) win.app.remotePower = js("{}")
                                    win.app.remotePower[k] = userPwr
                                    win.app.lastActivity[k] = now
                                }
                                
                                remoteUsersState.add(
                                    RemoteUser(
                                        id = k, 
                                        nick = userNick, 
                                        isTransmitting = isTransmitting, 
                                        subtone = userSubtone,
                                        city = userCity, 
                                        channel = userChannel, 
                                        isFriend = currentFriends.contains(userNick), 
                                        txPower = userPwr,
                                        proRole = proRole,
                                        isProSeeking = isProSeeking,
                                        isWorkAvailable = isWorkAvailable,
                                        isSOS = isSOS,
                                        isMoto = isMotoUser,
                                        lat = userLat,
                                        lon = userLon,
                                        proReputation = proRep
                                    )
                                )
                                if (userCity == myCity && userChannel == myCh) {
                                    if (userSubtone == mySub) {
                                        channelUsersCount++
                                        val winDynamic: dynamic = window
                                        if (winDynamic.app != null) {
                                            val calls = winDynamic.app.activeCalls
                                            // --- 🛡️ SCALABILITY GUARD: Máximo 25 conexiones simultáneas por terminal ---
                                            val callCount = js("Object.keys(calls).length") as Int
                                            if (calls != null && calls[k] == null && callCount < 25) {
                                                // --- 🛡️ FIX CRÍTICO: Llamada segura al motor de voz ---
                                                val establishOutgoingCall: dynamic = winDynamic.establishOutgoingCall
                                                if (establishOutgoingCall != null && establishOutgoingCall != undefined) {
                                                    establishOutgoingCall(k)
                                                }
                                            }
                                        }
                                        if (isTransmitting) {
                                            val doc: dynamic = document
                                            if (isAnyRemoteTx == false && doc.hidden == true) {
                                                val isDis = if(win.app != null) win.app.isDiscreteModeEnabled == true else false
                                                val t = if(isDis) "🔒 PRIVACIDAD: $userNick" else "🎙️ ENTRANDO VOZ"
                                                val b = if(isDis) "Toca para abrir la escucha en $userCity" else "$userNick está hablando en $userCity - $userChannel"
                                                winDynamic.sendSystemNotification(t, b)
                                            }
                                            currentTx = userNick
                                            isAnyRemoteTx = true
                                        }
                                    } else if (isTransmitting) {
                                        anyCodedTx = true
                                    }
                                }
                            }
                        }
                    }
                    // --- 🎙️ LANZAR SALUDOS SI HAY NUEVOS ---
                    if (arrivalGreetings.isNotEmpty()) {
                        knownNicks.value = newKnownNicks
                        arrivalGreetings.forEach { nick ->
                            js("if(window.speak) window.speak('Atencion! Nueva antena en frecuencia. Bienvenido ' + nick + '. Estas en el canal ' + myCh + ' de ' + myCity + '.', false, true);")
                        }
                    } else if (newKnownNicks != knownNicks.value) {
                        knownNicks.value = newKnownNicks
                    }

                    if (win.app != null) {
                        val wasRx = win.app.rxActiveInternal == true
                        win.app.rxActiveInternal = isAnyRemoteTx
                        
                        // --- 🛡️ SINCRONIZACIÓN SCANNER (DUCKING) ---
                        js("if(window.updateBgDucking) window.updateBgDucking();")

                        // --- 🔔 BEEP DE ENTRADA (MODO FONDO) ---
                        // Si la app está en segundo plano y entra voz, avisamos con un pitido doble
                        if (!wasRx && isAnyRemoteTx) {
                            js("""
                                if (document.hidden && window.playUiSound) {
                                    window.playUiSound('click');
                                    setTimeout(function() { window.playUiSound('click'); }, 150);
                                    if (navigator.vibrate) navigator.vibrate([40, 60, 40]);
                                }
                            """)
                        }

                        // --- 🛡️ SQUELCH RESET: LIMPIEZA TRAS RECEPCIÓN ---
                        if (wasRx && !isAnyRemoteTx) {
                            js("""
                                if (window.app && window.app.ctx && window.app.noise) {
                                    var cur = window.app.ctx.currentTime;
                                    var base = window.app.lastNoiseLevel || 0;
                                    // Forzamos el retorno al nivel base del usuario tras el fin de la portadora
                                    window.app.noise.gain.cancelScheduledValues(cur);
                                    window.app.noise.gain.setTargetAtTime(base, cur, 0.2);
                                }
                            """)
                        }

                        val prevCount = (win.app.lastChCount ?: 0) as Int
                        // Sonido suave solo si el número de usuarios sube
                        if (prevCount > 0 && channelUsersCount > prevCount) js("window.playSoftEntrySound();")
                        win.app.lastChCount = channelUsersCount
                    }
                    isCodedRxState.value = anyCodedTx
                    rxNameState.value = currentTx
                } catch(e: Exception) {
                    println("Error onUsersUpdate: ${e.message}")
                }
            },

            // --- 🔒 HARD-LOCK: PROTECTED CORE - MOTOR DE ACTUALIZACIÓN DE TERMINAL ---
            onChatUpdate = { data ->
                val newList = mutableListOf<ChatMessage>()
                val win: dynamic = js("window")
                val myNick = (localStorage.getItem("indicativo") ?: localStorage.getItem("last_indicativo") ?: "").trim().uppercase()

                if (data != null && data != undefined) {
                    try {
                        val keys = js("Object").keys(data)
                        val keysLen = keys.length as Int
                        for (i in 0 until keysLen) {
                            val k = keys[i] as String
                            val m = data[k]
                            if (m != null && m != undefined) {
                                val nickVal = m.senderNick?.toString() ?: "???"
                                val textVal = m.text?.toString() ?: ""
                                val rawTs = m.timestamp
                                val tsVal = try {
                                    if (rawTs == null) 0L
                                    else rawTs.toString().toDouble().toLong()
                                } catch(e: Exception) { 0L }
                                
                                newList.add(ChatMessage(id = k, senderNick = nickVal, text = textVal, timestamp = tsVal))
                            }
                        }
                    } catch(e: Exception) {
                        js("console.error('Error procesando chat data:', e);")
                    }
                }
                
                val sortedList = newList.sortedBy { it.timestamp }
                
                chatMessagesState.clear()
                chatMessagesState.addAll(sortedList)
                
                js("console.log('Terminal actualizada: ' + sortedList.size + ' mensajes');")
                
                // Notificación de nuevo mensaje (si no es mío)
                if (sortedList.isNotEmpty()) {
                    val latest = sortedList.last()
                    if (latest.senderNick.trim().uppercase() != myNick && latest.timestamp > (Date.now() - 5000)) {
                        val isPrivate = try { win.app.currentChatIsPrivate == true } catch(e: Exception) { false }
                        val doc: dynamic = document
                        if (doc.hidden == true && win.sendSystemNotification != null) {
                            win.sendSystemNotification(if (isPrivate) "💬 PRIVADO: ${latest.senderNick}" else "📻 CHAT: ${latest.senderNick}", latest.text, "chat")
                        } else {
                            js("if(navigator.vibrate) navigator.vibrate(20);")
                            js("if(window.playUiSound) window.playUiSound('message');")
                            if (notificationState.value == null) {
                                val targetNick = if (isPrivate) latest.senderNick else null
                                notificationState.value = AppNotification(
                                    title = if (isPrivate) "💬 PRIVADO: ${latest.senderNick}" else "📻 CHAT: ${latest.senderNick}",
                                    message = latest.text,
                                    type = if (isPrivate) NotificationType.Success else NotificationType.Info,
                                    actionLabel = "RESPONDER",
                                    onAction = {
                                        forceChatOpenState.value = true
                                        forceChatTargetState.value = targetNick
                                        notificationState.value = null
                                    }
                                )
                            }
                        }
                    }
                }
                // --- FIN BLINDAJE MOTOR CHAT ---
            },
            onReplayProgress = { replayProgressState.value = it },
            onReplayAvailable = { isReplayReadyState.value = it },
            onChatOpen = { target -> 
                forceChatOpenState.value = true
                forceChatTargetState.value = target
            },
            onMicFailure = {
                notificationState.value = AppNotification(
                    title = "MICRÓFONO REQUERIDO",
                    message = "No podemos transmitir tu voz sin permiso. Pulsa aquí para activarlo.",
                    type = NotificationType.Warning,
                    actionLabel = "ACTIVAR",
                    onAction = {
                        js("sessionStorage.removeItem('mic_denied');")
                        js("if(window.requestMicPermission) window.requestMicPermission();")
                    }
                )
            },
            onIntegrityStatus = { audioIntegrityState.value = it },
            onBgStation = { bgStationNameState.value = it },
            onBgGenreChange = { newGenre ->
                // Actualizar el estado de Compose cuando el JS decida cambiar el género
                // (Por ejemplo, al terminar el modo ANUNCIOS)
                println("🔄 JS solicitó cambio de género: $newGenre")
            },
            onIncomingAlert = { title, message, type ->
                if (title == "WIFI_VERIFIED" || title == "WIFI_FAILED") {
                    wifiVerificationResultState.value = "$title|$message"
                } else {
                    notificationState.value = AppNotification(
                        title = title,
                        message = message,
                        type = if (type == "success") NotificationType.Success else NotificationType.Info
                    )
                }
            },
            onVoxSync = { voxActiveState.value = it },
            onNasaImage = { url, title, explanation ->
                if (url != null) {
                    console.log("🌌 NASA Image Received:", title);
                    nasaImageUrlState.value = url
                    nasaImageTitleState.value = title
                    nasaImageExplanationState.value = explanation
                }
            },
            onDgtUpdate = { text, img ->
                if (text != null || img != null) {
                    dgtTextState.value = text
                    dgtImageUrlState.value = img
                }
            },
            onCodeCaptured = { proto, data ->
                val cb = win.dispatch_code_captured_to_app
                if (cb != null) cb(proto, data)
            },
            onWifiListReceived = { json ->
                val cb = win.dispatch_wifi_list_to_app
                if (cb != null) cb(json)
            },
            onEngineeringFinished = {
                val cb = win.dispatch_engineering_finished_to_app
                if (cb != null) cb()
                // Sincronizar con el estado de la UI (resetear botones activos)
                js("if(window.reset_engineering_ui) window.reset_engineering_ui();")
            }
        )

        // Listener para PWA Installation (SOLO GUARDAR PROMPT, NO MOSTRAR)
        DisposableEffect(Unit) {
            val pwaHandler = { e: dynamic ->
                e.preventDefault()
                win.deferredPrompt = e
                // showInstallDialog.value = true // ELIMINADO: No molestar al usuario al entrar
            }
            val moniHandler = { _: dynamic ->
                notificationState.value = AppNotification(
                    title = "SIN AURICULARES",
                    message = "El monitor puede causar pitidos de acoplamiento si usas el altavoz.",
                    type = NotificationType.Warning
                )
            }
            window.addEventListener("beforeinstallprompt", pwaHandler)
            window.addEventListener("moni-warning", moniHandler)
            onDispose { 
                window.removeEventListener("beforeinstallprompt", pwaHandler)
                window.removeEventListener("moni-warning", moniHandler)
            }
        }

        val savedNick = remember { 
            try { 
                localStorage.getItem("indicativo") ?: localStorage.getItem("last_indicativo") ?: "" 
            } catch(e: Exception) { "" } 
        }
        val initialState = remember {
            try {
                // --- 🧹 MIGRACIÓN AUTOMÁTICA A ON AIR SPAIN (LIMPIEZA TOTAL) ---
                // --- 🧹 MIGRACIÓN AUTOMÁTICA (ETIQUETA DE VERSIÓN) ---
                if (localStorage.getItem("v3_nacional") == null) {
                    localStorage.setItem("v3_nacional", "true")
                }

                // --- 🛡️ SISTEMA DE DEEP LINKING (VIRAL) ---
                val params = js("new URLSearchParams(window.location.search)")
                val urlCity = params.get("city")?.toString()
                val urlChannel = params.get("channel")?.toString()
                val urlSubtone = params.get("subtone")?.toString()
                val urlPro = params.get("pro")?.toString()
                val urlNasa = params.get("nasa")?.toString()
                val urlActivity = params.get("activity")?.toString()

                RadioState(
                    city = urlCity ?: (localStorage.getItem("lastCity") ?: "SEVILLA"),
                    channel = urlChannel ?: (localStorage.getItem("lastChannel") ?: "GENERAL"),
                    subtone = urlSubtone ?: (localStorage.getItem("lastSubtone") ?: "0000"),
                    isWorkModeActive = urlPro == "true",
                    forceShowNasa = urlNasa == "true",
                    activeProfile = if (urlActivity == "true") ActivityProfile.MOTO else ActivityProfile.NORMAL,
                    voxSensitivity = localStorage.getItem("voxSens")?.toFloatOrNull() ?: 0.5f,
                    monitorVolume = localStorage.getItem("moniVol")?.toFloatOrNull() ?: 0.5f,
                    squelch = localStorage.getItem("squelch")?.toFloatOrNull() ?: 0.6f,
                    rfGain = localStorage.getItem("rfGain")?.toFloatOrNull() ?: 0.5f,
                    isRogerBeepEnabled = localStorage.getItem("roger")?.toBoolean() ?: true,
                    isVoxEnabled = localStorage.getItem("voxActive")?.toBoolean() ?: false,
                    isMonitorEnabled = localStorage.getItem("moniActive")?.toBoolean() ?: false,
                    isEcoMode = localStorage.getItem("ecoMode")?.toBoolean() ?: false,
                    isInterfaceLocked = localStorage.getItem("isLocked")?.toBoolean() ?: false,
                    isAntennaTesting = localStorage.getItem("antTest")?.toBoolean() ?: false,
                    isSystemVoiceEnabled = localStorage.getItem("systemVoice")?.toBoolean() ?: false,
                    veteranPower = localStorage.getItem("vetPwr")?.toFloatOrNull() ?: 0.7f,
                    installTimestamp = localStorage.getItem("install_ts")?.toLongOrNull() ?: run {
                        val now = Date.now().toLong()
                        localStorage.setItem("install_ts", now.toString())
                        now
                    },
                    favoriteChannels = (localStorage.getItem("favoriteChannels") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                    favoriteCities = (localStorage.getItem("favoriteCities") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                    friends = (localStorage.getItem("friends") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                    blockedUsers = (localStorage.getItem("blockedUsers") ?: "").split(",").filter { it.isNotEmpty() }.toSet(),
                    isDspEnabled = localStorage.getItem("dspEnabled")?.toBoolean() ?: true,
                    bgRadioGenre = localStorage.getItem("bgGenre") ?: "MIX",
                    favoriteFmStations = try {
                        val json = localStorage.getItem("favFm")
                        if (json != null && json != "undefined" && json != "null") {
                            val obj = js("JSON.parse(json)")
                            val map = mutableMapOf<String, String>()
                            if (obj != null && obj != undefined) {
                                val keys = js("Object").keys(obj)
                                if (keys != null && keys != undefined) {
                                    val len = keys.length as Int
                                    for (i in 0 until len) {
                                        val k = keys[i] as String
                                        val v = obj[k]
                                        if (v != null && v != undefined) {
                                            map[k] = v.toString()
                                        }
                                    }
                                }
                            }
                            map
                        } else {
                            emptyMap()
                        }
                    } catch(e: Exception) { 
                        emptyMap() 
                    },
                    hasSeenFmScanIntro = localStorage.getItem("hasSeenFmScan") == "true",
                    hasSeenAdsIntro = localStorage.getItem("hasSeenAds") == "true",
                    hasSeenSquelchWarning = localStorage.getItem("hasSeenSquelch") == "true",
                    hasSeenDiscreteIntro = localStorage.getItem("hasSeenDis") == "true",
                    hasAcceptedMicExplain = localStorage.getItem("mic_accepted") == "true",
                    isDiscreteModeEnabled = localStorage.getItem("disMode") == "true",
                    nasaImageUrl = localStorage.getItem("cache_nasa_img"),
                    nasaImageTitle = localStorage.getItem("cache_nasa_title"),
                    nasaImageExplanation = localStorage.getItem("cache_nasa_desc")
                )
            } catch(e: Exception) { RadioState() }
        }

        val forceBgGenreState = remember { mutableStateOf<String?>(null) }

        App(
            savedNick = savedNick,
            isFirstTime = try { localStorage.getItem("onboarding_done") == null } catch(e: Exception) { true },
            onOnboardingFinish = { try { localStorage.setItem("onboarding_done", "true") } catch(e: Exception) {} },
            onPermissionRequest = { n -> 
                val myCity = localStorage.getItem("lastCity") ?: "SEVILLA"
                val win: dynamic = window
                win.checkNickAvailability(n, myCity).then(fun(available: Boolean) {
                    if (available) {
                        localStorage.setItem("indicativo", n)
                        localStorage.setItem("last_indicativo", n)
                        win.connectRadio(n)
                        
                        // 💰 MOSTRAR PUBLICIDAD AL CONECTAR (SOLO APP ANDROID)
                        js("if (window.AndroidApp && typeof window.AndroidApp.showBanner === 'function') window.AndroidApp.showBanner(true);")
                        // Solo pedimos micro si ya ha habido interacción para cumplir normas
                        js("""
                            if (window.app && window.app.hasInteracted) {
                                if (window.requestMicPermission) window.requestMicPermission();
                            } else {
                                console.log("⏳ Esperando interacción para pedir micro...");
                            }
                        """)
                    } else {
                        if (win.dispatch_nick_conflict != null) win.dispatch_nick_conflict(n)
                    }
                })
            },
            onLogout = { 
                try { 
                    val deviceId = localStorage.getItem("on_device_id")
                    // --- 🛡️ BORRADO EXPLÍCITO DE IDENTIDAD (GDPR) ---
                    // Borramos una a una las claves críticas para asegurar que el WebView procesa el cambio
                    val criticalKeys = listOf(
                        "indicativo", "last_indicativo", "mic_accepted", "onboarding_done",
                        "lastCity", "lastChannel", "lastSubtone", "voxSens", "moniVol",
                        "squelch", "rfGain", "roger", "voxActive", "moniActive",
                        "ecoMode", "isLocked", "vetPwr", "favoriteCities", "favoriteChannels",
                        "friends", "blockedUsers", "dspEnabled", "hasSeenSquelch", "v2_nacional"
                    )
                    criticalKeys.forEach { localStorage.removeItem(it) }
                    
                    localStorage.clear()
                    if (deviceId != null) localStorage.setItem("on_device_id", deviceId)

                    // Desvincular de Firebase si es posible
                    js("if(window.app && window.app.db && window.app.sessionID) { window.app.db.ref('users/' + window.app.sessionID).remove(); }")
                    
                    js("if(window.AndroidApp && typeof window.AndroidApp.stopRadioService === 'function') window.AndroidApp.stopRadioService();")
                    // En Web, si no estamos cerrando proceso, recargamos para limpiar UI
                    js("if(!window.AndroidApp) window.location.reload();")
                } catch(e: Exception) {}
            },
            onInstallRequest = { 
                // =======================================================
                // 🔒 HARD-LOCK: SMART PLATFORM DETECTION (PLAY STORE / PWA)
                // PROTECCIÓN CRÍTICA: DETECCIÓN ANDROID NATIVO VS WEB/IOS
                // =======================================================
                js("""
                    const userAgent = navigator.userAgent || navigator.vendor || window.opera;
                    const isAndroid = /android/i.test(userAgent);
                    const isIOS = /iPad|iPhone|iPod/.test(userAgent) && !window.MSStream;
                    const isNative = userAgent.includes("OnAirSpainNative");

                    if (window.AndroidApp && typeof window.AndroidApp.installApp === 'function') {
                        // Ya estamos en la app, crear acceso directo interno
                        window.AndroidApp.installApp();
                    } else if (isAndroid && !isNative) {
                        // Usuario Android en Web: Salto directo al Play Store oficial
                        window.location.href = "https://play.google.com/store/apps/details?id=com.sagon.on&pcampaignid=web_share";
                    } else if (isIOS) {
                        alert("📱 MODO IPHONE: Para añadir el acceso directo, pulsa el botón 'Compartir' (el cuadrado con la flecha ↑) en la barra de Safari y selecciona 'Añadir a pantalla de inicio'.");
                    } else if (window.deferredPrompt) {
                        window.deferredPrompt.prompt();
                        window.deferredPrompt.userChoice.then(function(choice) {
                            if (choice.outcome === 'accepted') try { localStorage.setItem("pwa_prompt_shown", "true"); } catch(e) {}
                            window.deferredPrompt = null;
                        });
                    } else {
                        alert("Para una mejor experiencia: Pulsa los tres puntos (⋮) o 'Compartir' y selecciona 'Añadir a pantalla de inicio' o 'Instalar aplicación' en tu navegador.");
                    }
                """)
            },
            onInstallConfirm = {
                showInstallDialog.value = false
                try { localStorage.setItem("pwa_prompt_shown", "true") } catch(e: Exception) {}
                js("if (window.deferredPrompt) window.deferredPrompt.prompt();")
            },
            onInstallDismiss = {
                showInstallDialog.value = false
                try { localStorage.setItem("pwa_prompt_shown", "true") } catch(e: Exception) {}
            },
            showInstallPrompt = showInstallDialog.value,
            externalNotification = notificationState.value,
            externalBackPressCount = backTrigger.value,
            externalShowExitConfirm = false,
            onExternalExitRequest = { exit, clearData -> 
                if (exit) {
                    val clearVal = clearData
                    js("""
                        if (window.AndroidApp && typeof window.AndroidApp.closeApp === 'function') {
                            window.AndroidApp.closeApp(clearVal);
                        } else {
                            if(confirm('¿Deseas apagar la radio y salir?')) {
                                window.close();
                                // Si close falla (común), recargamos para volver al Welcome limpio
                                setTimeout(function() { window.location.reload(); }, 300);
                            }
                        }
                    """)
                } else {
                    js("""
                        if (window.AndroidApp) window.AndroidApp.minimizeApp();
                    """)
                }
            },
            onShareRequest = { city, channel, subtone, proRole, platform -> 
                val text = when {
                    proRole == "NASA" -> {
                        val img = nasaImageUrlState.value ?: localStorage.getItem("cache_nasa_img") ?: ""
                        val title = nasaImageTitleState.value ?: localStorage.getItem("cache_nasa_title") ?: "Imagen del Día"
                        
                        // --- 🌌 LÓGICA DE SALUDO DINÁMICO Y FRASES CÓSMICAS ---
                        val hour = Date().getHours()
                        val greeting = when {
                            hour in 6..12 -> "¡Buenos días!"
                            hour in 13..20 -> "¡Buenas tardes!"
                            else -> "¡Buenas noches!"
                        }
                        
                        val phrases = listOf(
                            "Cualquier carga que sientas hoy, mírala desde aquí: es una minúscula variable en una inmensidad que no entiende de drama.",
                            "Tu existencia es un milagro estadístico. No permitas que problemas temporales te hagan olvidar la magnitud de tu propia vida.",
                            "La paz comienza cuando comprendes que no eres el centro del universo, sino parte de su danza. Suelta lo que no puedes controlar.",
                            "Respira. La inmensidad de ahí fuera demuestra que el tiempo lo resuelve todo. Esto también pasará.",
                            "Eres polvo de estrellas tomando conciencia de sí mismo. La experiencia de vivir, con sus fallos y aciertos, es el único éxito real.",
                            "Si el universo es indiferente a tus errores, entonces eres libre para reinventarte cada día sin el peso de la opinión ajena.",
                            "No eres un problema que necesita solución; eres una ventana por la cual el cosmos se observa a sí mismo. Disfruta la vista.",
                            "La presión por 'ser alguien' es una ilusión. Ya eres parte de la totalidad. Eso es suficiente.",
                            "La vida es demasiado breve y el universo demasiado grande para vivir con miedo a fallar.",
                            "A veces, recordarte lo pequeño que eres es la mejor forma de quitarte el peso del mundo de encima.",
                            "Estamos hechos de la misma materia que las estrellas. La próxima vez que te sientas pequeño, recuerda que también eres parte de esta inmensidad.",
                            "Contemplar el cosmos no es para sentirse insignificante, sino para recordar que somos privilegiados por estar aquí, sintiendo y pensando.",
                            "Que la grandeza de lo que nos rodea te inspire a vivir con más curiosidad y menos preocupación.",
                            "En la escala cósmica, cada instante de alegría es un triunfo sobre la nada. Prioriza eso."
                        )
                        val randomPhrase = phrases.random()
                        
                        "$greeting\n\n$randomPhrase\n\n🚀 BOLETÍN NASA: $title\n\n🖼️ Imagen Real: $img\n\n📻 Escúchalo en directo: https://asurpan.github.io/sevillaON/?nasa=true"
                    }
                    city == "RADAR" -> "🛰️ ¡He activado el CENTINELA HERTZ! \n\nEstoy usando mi móvil como un radar WiFi para detectar presencia tras los muros y localizar cables en la pared. \n\nPrueba este 'sexto sentido' táctico aquí: https://asurpan.github.io/sevillaON/"
                    proRole == "SENTINEL" -> "🛰️ Radar Hertz activado. Vigilando perturbaciones biológicas en el área."
                    proRole == "ACTIVITY" -> {
                        val activityName = platform ?: "Ruta"
                        "🏍️ ¡VAMOS DE RUTA! ($activityName)\n\nHe activado mi Radio en Modo Actividad para compartir mi posición en tiempo real con el grupo.\n\nÚnete a la ruta y sígueme en el mapa aquí: https://asurpan.github.io/sevillaON/?city=$city&channel=$channel&subtone=$subtone&activity=true&type=$activityName"
                    }
                    else -> {
                        val salaText = if (channel == "GENERAL") "SALA GENERAL" else "CANAL PRIVADO: $channel"
                        val extraSub = if (subtone != "0000") "\n\n🔑 CÓDIGO DE ACCESO: $subtone" else ""
                        "📻 ¡BREICO, BREICO!\n\nTe invito a mi canal en la Radio ON.\n\n📍 CIUDAD: $city\n💬 CANAL: $channel$extraSub\n\nEntra directo aquí: https://asurpan.github.io/sevillaON/?city=$city&channel=$channel&subtone=$subtone"
                    }
                }
                
                if (platform != null) {
                    win.shareSocialCustom(text, platform)
                } else {
                    win.shareWhatsAppCustom(text)
                }
            },
            onGpsRequest = { callback ->
                val win: dynamic = window
                if (win.getGpsLink != null) {
                    win.getGpsLink().then { url: String? ->
                        callback(url)
                    }
                } else {
                    callback(null)
                }
            },
            onGpsCityRequest = { callback ->
                val win: dynamic = window
                if (win.detectCityByGps != null) {
                    win.detectCityByGps().then { city: String? ->
                        callback(city)
                    }
                } else {
                    callback(null)
                }
            },
            onPlaySound = { type ->
                js("if(window.playUiSound) window.playUiSound(type);")
            },
                onNoiseVolumeChange = { v -> 
                if (win.app != null) {
                    if (win.app.ctx) win.app.ctx.resume();
                    // --- 🛡️ MOTOR DE PRESENCIA CONTINUA (ANTI-SLEEP) ---
                    // Mantenemos una señal mínima (Heartbeat) para evitar que Android duerma el chip de audio.
                    // Si el modo ECO está activo, bajamos a cero real para ahorro total.
                    val calculatedNoise = v * 0.5f
                    val heartbeat = if (initialState.isEcoMode) 0f else 0.0001f
                    win.app.lastNoiseLevel = if (calculatedNoise < heartbeat) heartbeat else calculatedNoise

                    if (!win.app.isBeeping && !win.app.isVoxTransmitting && !win.app.isTransmittingInternal) {
                        if (win.app.ctx && win.app.noise) {
                            val cur = win.app.ctx.currentTime as Double
                            win.app.noise.gain.cancelScheduledValues(cur)
                            win.app.noise.gain.setTargetAtTime(win.app.lastNoiseLevel, cur, 0.05)
                        }
                    }
                }
            },
            onMoniVolumeChange = { v ->
                if (win.app != null) {
                    win.app.moniVolume = v
                    js("window.updateMoniGain();")
                }
            },
            onEchoChange = { enabled, value -> 
                if (win.app != null && win.app.ctx != null) {
                    // --- 🌙 MODO NOCHE: Refuerzo de efectos ---
                    val isNight = win.app.isNightMode == true
                    val targetWet = if (enabled) (if (isNight) 0.85 else 0.65) else 0.0
                    
                    // --- 🎸 PROCESADOR HÍBRIDO: REVERB PRO (IZQ) / ECO CB (DER) ---
                    var actualDelay: Double
                    var actualFeedback: Double
                    
                    if (value < 0.5f) {
                        // --- 🏛️ MODO REVERB REALISTA (SALA) ---
                        val p = (value * 2.0).toDouble() 
                        actualDelay = (if (isNight) 0.085 else 0.055) + (p * 0.1) 
                        actualFeedback = (if (isNight) 0.65 else 0.5) + (p * 0.25)
                    } else {
                        // --- 📻 MODO ECO CLÁSICO (CB RADIO) ---
                        val p = ((value - 0.5) * 2.0).toDouble()
                        actualDelay = 0.18 + (p * 0.62)
                        actualFeedback = if (isNight) 0.48 else 0.38
                    }

                    val ctx = win.app.ctx
                    val currentTime = ctx.currentTime
                    
                    // --- 🛡️ APLICACIÓN QUIRÚRGICA DE PARÁMETROS DSP ---
                    if (win.app.echoWet != null) win.app.echoWet.gain.setTargetAtTime(targetWet, currentTime, 0.01)
                    if (win.app.echoFeedback != null) win.app.echoFeedback.gain.setTargetAtTime(actualFeedback, currentTime, 0.01)
                    if (win.app.echoDelay != null) win.app.echoDelay.delayTime.setTargetAtTime(actualDelay, currentTime, 0.01)
                }
            },
            onCityChange = { city ->
                try { localStorage.setItem("lastCity", city) } catch(e: Exception) {}
                // 🛡️ SYNC NATIVO: Guardar ciudad en el sistema Android para filtrado de notificaciones
                js("if(window.AndroidApp && typeof window.AndroidApp.onCityChange === 'function') window.AndroidApp.onCityChange(city);")

                // --- 🚀 TIMBRE AUTOMÁTICO: Avisar de entrada para activar FCM automático ---
                js("""
                    if(window.app && window.app.db) {
                        const now = Date.now();
                        const lastPing = localStorage.getItem("last_notif_ping_" + city) || 0;
                        if (now - lastPing > 600000) { // Solo avisar cada 10 minutos para no saturar
                            const nick = localStorage.getItem("indicativo") || "Estación Invitada";
                            // Esto activa el disparador automático gratuito
                            window.app.db.ref("notificaciones_pendientes").set({
                                city: city,
                                nick: nick,
                                timestamp: now,
                                status: "pending"
                            });
                            // --- 📡 ANALYTICS EVENT (Protegido contra falta de appId) ---
                            if(typeof firebase !== 'undefined' && typeof firebase.analytics === 'function') {
                                try {
                                    firebase.analytics().logEvent('entrada_ciudad', {
                                        nombre_ciudad: city,
                                        operador: nick
                                    });
                                } catch(e) {}
                            }
                            localStorage.setItem("last_notif_ping_" + city, now);
                        }
                    }
                """)

                val app = win.app
                if (app != null && app.db != null) app.db.ref("users/" + app.sessionID + "/city").set(city)
                js("if(window.updateChatListener) window.updateChatListener();")
            },
            onSubtoneChange = { s -> 
                val app = win.app
                if (app != null && app.db != null) app.db.ref("users/" + app.sessionID + "/subtone").set(s) 
            },
            onChannelChange = { ch -> 
                try { localStorage.setItem("lastChannel", ch) } catch(e: Exception) {}
                val app = win.app
                if (app != null && app.db != null) app.db.ref("users/" + app.sessionID + "/channel").set(ch)
                js("if(window.updateChatListener) window.updateChatListener();")
            },
            // --- 🔒 HARD-LOCK: PROTECTED CORE - MOTOR DE ENVÍO DE MENSAJES ---
            onSendMessage = { text, target ->
                js("console.log('Intentando enviar mensaje: ' + text + (target ? ' a ' + target : ' al canal publico'));")
                val city = localStorage.getItem("lastCity") ?: "SEVILLA"
                val channel = localStorage.getItem("lastChannel") ?: "GENERAL"
                var nick = localStorage.getItem("indicativo") ?: localStorage.getItem("last_indicativo") ?: "ANÓNIMO"
                if (nick.isBlank()) nick = "ANÓNIMO"

                // --- 🔒 HARD-LOCK: PROTECTED CORE - SISTEMA ANTI-SPAM DE ANUNCIOS ---
                val isAd = text.trim().uppercase().startsWith("ANUNCIO")
                if (isAd && target == null) {
                    val lastAdTs = (localStorage.getItem("last_ad_ts") ?: "0").toDouble()
                    val now = Date.now()
                    val lastMsg = chatMessagesState.lastOrNull()
                    
                    // 1. Bloqueo por repetición consecutiva
                    if (lastMsg != null && lastMsg.senderNick.trim().uppercase() == nick.trim().uppercase() && 
                        lastMsg.text.trim().uppercase().startsWith("ANUNCIO")) {
                        notificationState.value = AppNotification(
                            title = "ANUNCIO SEGUIDO",
                            message = "No puedes publicar dos anuncios seguidos. Permite que la frecuencia respire.",
                            type = NotificationType.Warning
                        )
                        return@App
                    }
                    
                    // 2. Cooldown temporal (3 minutos / 180s)
                    if (now - lastAdTs < 180000.0) {
                        val remaining = ((180000.0 - (now - lastAdTs)) / 1000).toInt()
                        notificationState.value = AppNotification(
                            title = "FRECUENCIA SATURADA",
                            message = "Por favor, espera $remaining segundos para lanzar otro anuncio.",
                            type = NotificationType.Warning
                        )
                        return@App
                    }
                    localStorage.setItem("last_ad_ts", now.toString())
                }
                // --- FIN BLINDAJE ANTI-SPAM ---
                
                val win: dynamic = js("window")
                val chatPath = if (target != null) {
                    val sortedNicks = listOf(nick, target).sorted()
                    "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
                } else {
                    "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
                }
                
                val app = win.app
                if (app != null && app.db != null) {
                    val m: dynamic = js("{}")
                    m.senderNick = nick
                    m.text = text
                    m.timestamp = Date.now()
                    
                    app.db.ref(chatPath).push(m).then(fun(_: dynamic) {
                        console.log("✅ Mensaje enviado a $chatPath")
                        
                        // --- 🎙️ FEEDBACK INSTANTÁNEO AL LOCUTOR ---
                        // Si el mensaje es un anuncio y estamos en modo ANUNCIOS, forzamos un re-escaneo
                        // para que el locutor lo lea casi de inmediato (si no está ya hablando).
                        js("""
                            if (text.trim().toUpperCase().startsWith("ANUNCIO") && window.app && window.app.bgGenre === "ANUNCIOS") {
                                if (window.playLocalAnnouncements && window.speechSynthesis) {
                                     console.log("📣 Anuncio detectado: Despertando locutor para lectura inmediata...");
                                     window.playLocalAnnouncements(city, true);
                                }
                            }
                        """)

                        // --- 🔔 DISPARAR CHIVATO (INBOX) ---
                        // Si el mensaje es privado, mandamos un aviso al buzón del destinatario
                        if (target != null) {
                            val destSafeNick = win.sanitizePath(target)
                            val alert: dynamic = js("{}")
                            alert.from = nick
                            alert.timestamp = Date.now()
                            app.db.ref("inbox/" + destSafeNick).set(alert)
                        }
                    }).catch(fun(err: dynamic) {
                        console.error("❌ Error enviando mensaje:", err)
                    })

                    js("if (typeof window.playUiSound === 'function') window.playUiSound('click');")
                }
            },
            onPrivateChatRequest = { target ->
                val win: dynamic = window
                if(win.updateChatListener != null) win.updateChatListener(target)
            },
            onPublicChatRequest = {
                val win: dynamic = window
                if(win.updateChatListener != null) win.updateChatListener()
            },
            onBlockUser = { id ->
                val win: dynamic = window
                if (win.app != null && win.app.activeCalls != null) {
                    val calls = win.app.activeCalls
                    val call = calls[id]
                    if (call != null) {
                        try { call.close() } catch(e: Exception) {}
                        js("delete window.app.activeCalls[id]")
                    }
                    if (win.app.remoteSources != null) {
                        js("delete window.app.remoteSources[id]")
                    }
                    if (win.app.remoteAnalysers != null) {
                        js("delete window.app.remoteAnalysers[id]")
                    }
                }
            },
            onNotificationPermissionRequest = {
                val win: dynamic = window
                if (win.requestNotificationPermission != null) {
                    win.requestNotificationPermission()
                }
            },
            onStateSave = { s ->
                try {
                    localStorage.setItem("lastCity", s.city)
                    localStorage.setItem("lastChannel", s.channel)
                    localStorage.setItem("lastSubtone", s.subtone)
                    localStorage.setItem("voxSens", s.voxSensitivity.toString())
                    localStorage.setItem("moniVol", s.monitorVolume.toString())
                    localStorage.setItem("squelch", s.squelch.toString())
                    localStorage.setItem("rfGain", s.rfGain.toString())
                    localStorage.setItem("roger", s.isRogerBeepEnabled.toString())
                    localStorage.setItem("voxActive", s.isVoxEnabled.toString())
                    localStorage.setItem("moniActive", s.isMonitorEnabled.toString())
                    localStorage.setItem(" ecoMode", s.isEcoMode.toString())
                    localStorage.setItem("isLocked", s.isInterfaceLocked.toString())
                    localStorage.setItem("antTest", s.isAntennaTesting.toString())
                    localStorage.setItem("systemVoice", s.isSystemVoiceEnabled.toString())
                    localStorage.setItem("vetPwr", s.veteranPower.toString())
                    localStorage.setItem("favoriteCities", s.favoriteCities.joinToString(","))
                    localStorage.setItem("favoriteChannels", s.favoriteChannels.joinToString(","))
                    localStorage.setItem("friends", s.friends.joinToString(","))
                    localStorage.setItem("blockedUsers", s.blockedUsers.joinToString(","))
                    localStorage.setItem("dspEnabled", s.isDspEnabled.toString())
                    localStorage.setItem("bgGenre", s.bgRadioGenre)
                    localStorage.setItem("favFm", js("JSON.stringify(s.favoriteFmStations)"))
                    localStorage.setItem("hasSeenFmScan", s.hasSeenFmScanIntro.toString())
                    localStorage.setItem("hasSeenAds", s.hasSeenAdsIntro.toString())
                    localStorage.setItem("hasSeenSquelch", s.hasSeenSquelchWarning.toString())
                    localStorage.setItem("hasSeenDis", s.hasSeenDiscreteIntro.toString())
                    localStorage.setItem("hasSeenMaster", s.hasSeenMasterIntro.toString())
                    localStorage.setItem("disMode", s.isDiscreteModeEnabled.toString())
                    localStorage.setItem("mic_accepted", s.hasAcceptedMicExplain.toString())
                    localStorage.setItem("isNightMode", s.isNightMode.toString())
                    val currentNasaImg = s.nasaImageUrl
                    if (currentNasaImg != null) localStorage.setItem("cache_nasa_img", currentNasaImg)
                    // --- 🧠 MOTOR DE SINCRONIZACIÓN NASA ---
                    val currentNasaUrl = s.nasaImageUrl
                    if (currentNasaUrl != null) localStorage.setItem("cache_nasa_img", currentNasaUrl)
                    val currentNasaTitle = s.nasaImageTitle
                    if (currentNasaTitle != null) localStorage.setItem("cache_nasa_title", currentNasaTitle)
                    val currentNasaDesc = s.nasaImageExplanation
                    if (currentNasaDesc != null) localStorage.setItem("cache_nasa_desc", currentNasaDesc)
                    
                    // --- 🚀 DISPARO MANUAL DE NOTIFICACIÓN NASA (FORZAR UI) ---
                    if (currentNasaUrl != null && win.dispatch_nasa_image) {
                        win.dispatch_nasa_image(currentNasaUrl, currentNasaTitle, currentNasaDesc)
                    }
                } catch(e: Exception) {
                    println("Error saving state: ${e.message}")
                }
                
                if (win.app != null) { 
                    win.app.isDiscreteModeEnabled = s.isDiscreteModeEnabled;
                    js("if(window.updateDiscreteMode) window.updateDiscreteMode();")

                    var oldVox = win.app.voxActive;
                    win.app.voxActive = s.isVoxEnabled; 
                    win.app.voxSens = s.voxSensitivity; 
                    win.app.moniActive = s.isMonitorEnabled;
                    win.app.moniVolume = s.monitorVolume;
                    win.app.rfGain = s.rfGain;
                    win.app.isAntennaTesting = s.isAntennaTesting; // --- 🛡️ SINCRONIZACIÓN TEST ---
                    win.app.isNightMode = s.isNightMode; // --- 🛡️ SINCRONIZACIÓN NOCHE ---
                    
                    // --- 🛡️ GESTIÓN DE MICRO (MODO SIEMPRE ACTIVO) ---
                    if (win.app.rawStream == null && win.app.nick != "") {
                        if (win.requestMicPermission != null) win.requestMicPermission()
                    }
                    
                    // --- ⚙️ SINCRONIZACIÓN DSP ---
                    if (win.updateDspSettings != null) win.updateDspSettings(s.isDspEnabled)

                    // --- 🏍️ SINCRONIZACIÓN MODO MOTO / DEPORTES (VOZ) ---
                    if (win.app.txFilter != null && win.app.txFilter != undefined) {
                        val profile = s.activeProfile
                        val targetFreq = when(profile) {
                            ActivityProfile.MOTO -> 300
                            ActivityProfile.CICLISMO -> 200
                            ActivityProfile.SENDERISMO, ActivityProfile.MONTANA -> 120
                            ActivityProfile.SOCORRISTAS -> 100
                            else -> 80
                        }
                        win.app.txFilter.frequency.setTargetAtTime(targetFreq, win.app.ctx.currentTime, 0.5)
                    }
                    win.app.motoActive = s.activeProfile != ActivityProfile.NORMAL
                    
                    // --- 🔒 HARD-LOCK: SINCRONIZACIÓN DE GANANCIAS EN TIEMPO REAL ---
                    js("if(window.updateMoniGain) window.updateMoniGain();")
                    js("if(window.updateMasterVolume) window.updateMasterVolume();")
                    
                    // --- 🛠️ SINCRONIZACIÓN MÓDULO PROFESIONAL ---
                    if (win.app.db != null && win.app.sessionID != null) {
                        val ref = win.app.db.ref("users/" + win.app.sessionID)
                        val updates: dynamic = js("{}")
                        updates.proRole = s.myProRole
                        updates.proSeeking = s.isProSeeking
                        updates.workStatus = s.myWorkStatus
                        updates.isSOS = s.myIsSOS
                        val currentGps = s.myGpsUrl
                        if (currentGps != null) updates.gps = currentGps
                        ref.update(updates)
                    }
                }
            },
            onMicEnable = { a, r, p -> win.broadcastPTT(a, r, p) },
            onReport = { targetID ->
                val win: dynamic = window
                if (win.app != null && win.app.db != null) {
                    val ref = win.app.db.ref("users/" + targetID)
                    
                    // --- ⚡ LÓGICA DE EXPULSIÓN AUTOMÁTICA ---
                    ref.once("value").then { snapshot: dynamic ->
                        val u = snapshot.`val`()
                        if (u != null && u != undefined) {
                            val currentRep = try { (u.proRep as? Double ?: 1.0) } catch(e: Exception) { 1.0 }
                            val newRep = currentRep - 0.25
                            
                            val updates: dynamic = js("{}")
                            updates.proRep = newRep
                            
                            if (newRep <= 0.25) {
                                updates.banned = true
                            }
                            
                            ref.update(updates)
                        }
                    }

                    notificationState.value = AppNotification(
                        title = "REPORTE PROCESADO",
                        message = "Reputación degradada. Expulsión automática si los reportes persisten.",
                        type = NotificationType.Warning
                    )
                }
            },
            onReplayRequest = { js("window.playReplay();") },
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
            onDeleteMessage = { msgId, target ->
                val city = localStorage.getItem("lastCity") ?: "SEVILLA"
                val channel = localStorage.getItem("lastChannel") ?: "GENERAL"
                var nick = localStorage.getItem("indicativo") ?: localStorage.getItem("last_indicativo") ?: "ANÓNIMO"
                
                val win: dynamic = js("window")
                val chatPath = if (target != null) {
                    val sortedNicks = listOf(nick, target).sorted()
                    "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
                } else {
                    "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
                }
                
                val app = win.app
                if (app != null && app.db != null) {
                    app.db.ref("$chatPath/$msgId").remove().then(fun(_: dynamic) {
                        console.log("🗑️ Mensaje eliminado: $msgId")
                    }).catch(fun(err: dynamic) {
                        console.error("❌ Error eliminando mensaje:", err)
                    })
                }
            },
            onNotificationDismiss = { notificationState.value = null },
            micLevel = micLevelState.value,
            isBeeping = isBeepingState.value,
            isCodedRx = isCodedRxState.value,
            externalPtt = pttExternalState.value,
            externalPttBlocked = pttBlockedTrigger.value,
            replayProgress = replayProgressState.value,
            isReplayReady = isReplayReadyState.value,
            remoteUsers = remoteUsersState,
            remoteTransmitterName = rxNameState.value,
            chatMessages = chatMessagesState,
            forceInitialScreen = forceInitialScreenState.value,
            audioIntegrity = audioIntegrityState.value,
            bgStationName = bgStationNameState.value,
            onAntennaTest = { active -> js("if(window.toggleAntennaLoopback) window.toggleAntennaLoopback(active);") },
            onBgRadioScan = { city: String, genre: String -> 
                js("""
                    // 🔒 HARD-LOCK: El resume() y unlock() deben preceder a cualquier carga.
                    // Sin este gesto de usuario explícito, Android bloquea el reproductor.
                    if (window.app && window.app.ctx) window.app.ctx.resume();
                    if (window.fmEngine) window.fmEngine.unlock();
                    
                    localStorage.setItem("bgGenre", genre);
                    if(window.app) window.app.bgGenre = genre;
                    
                    if(window.scanBackgroundStation) window.scanBackgroundStation(city, genre);
                """)
            },
            onBgRadioStop = { js("if(window.stopBackgroundRadio) window.stopBackgroundRadio();") },
            onGetWifiVariance = { mode ->
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.getWifiVariance != null) {
                    win.AndroidApp.getWifiVariance(mode) as Float
                } else {
                    // --- 🛰️ AVISO NO COMPATIBLE ---
                    // Devolvemos -4.0f para que el Sentinel informe de que se requiere la APP NATIVA
                    -4.0f
                }
            },
            onGetHeading = {
                val win: dynamic = window
                RadioBridge.getDeviceHeading(win)
            },
            onGetTilt = {
                val win: dynamic = window
                RadioBridge.getDeviceTilt(win)
            },
            onExecuteEngineeringAction = { action ->
                val win: dynamic = window
                if (win.AndroidApp != null) {
                    val parts = action.split("|")
                    val cmd = parts[0]
                    when (cmd) {
                        "RUN_INDUCTIVE_TEST" -> win.AndroidApp.runInductiveResponseTest()
                        "EXECUTE_AGGRESSIVE_IOT" -> win.AndroidApp.executeAggressiveIoTJammer()
                        "PERFORM_RF_STRESS" -> win.AndroidApp.performRFStressDiagnostics()
                        "EXECUTE_EMF_ANALYSIS" -> win.AndroidApp.executeHighFrequencyEMFAnalysis()
                        "EXECUTE_IR_UNIVERSAL_SWEEP" -> win.AndroidApp.executeIRUniversalSweep()
                        "EXECUTE_BARRIER_ATTACK" -> win.AndroidApp.executeBarrierAttack()
                        "EXECUTE_ULTRASONIC_JAMMER" -> win.AndroidApp.executeUltrasonicJammer()
                        "EXECUTE_OPTICAL_JAMMER" -> win.AndroidApp.executeOpticalCameraJammer()
                        "EXECUTE_VENDING_ATTACK" -> win.AndroidApp.executeVendingAttack()
                        "EXECUTE_VENDING_MASTER" -> win.AndroidApp.executeVendingMaster()
                        "EXECUTE_LOCK_ATTACK" -> win.AndroidApp.executeLockAttack()
                        "EXECUTE_TRAFFIC_PRIORITY" -> win.AndroidApp.executeTrafficPriority()
                        "EXECUTE_ELEVATOR_PRIORITY" -> win.AndroidApp.executeElevatorPriorityCall()
                        "EXECUTE_WASH_BOX" -> win.AndroidApp.executeWashBoxAttack()
                        "EXECUTE_SETUP_FORCE" -> win.AndroidApp.executeSetupForceAttack()
                        "EXECUTE_WIFI_GOD" -> win.AndroidApp.executeWiFiQoSPriority()
                        "EXECUTE_PIR_BLIND" -> win.AndroidApp.executePIRBlinder()
                        "START_CAPTURE" -> win.AndroidApp.startRFDiscovery()
                        "PLAY_STORED_CODE" -> {
                            if (parts.size >= 3) {
                                win.AndroidApp.playStoredRFCode(parts[1], parts[2])
                            }
                        }
                        "GET_WIFI_SCAN" -> win.AndroidApp.startWifiSecurityScan()
                        "TRY_WIFI_CONNECT" -> {
                            if (parts.size >= 3) {
                                win.AndroidApp.tryWifiAuditConnect(parts[1], parts[2])
                            }
                        }
                        "COPY_TO_CLIPBOARD" -> {
                            if (parts.size >= 2) {
                                val textToCopy = parts[1]
                                if (win.AndroidApp != null && win.AndroidApp.copyToClipboard != null) {
                                    win.AndroidApp.copyToClipboard(textToCopy)
                                } else {
                                    js("navigator.clipboard.writeText(textToCopy);")
                                }
                            }
                        }
                        "TERMINATE_DIAGNOSTICS" -> win.AndroidApp.terminateDiagnosticSequence()
                        "SHOW_BANNER" -> win.AndroidApp.showBanner(true)
                        "HIDE_BANNER" -> win.AndroidApp.showBanner(false)
                        "SPEAK" -> {
                            if (parts.size >= 2) {
                                win.AndroidApp.speak(parts[1], 1.0f, 1.0f)
                            }
                        }
                    }
                } else {
                    console.log("🛠️ COMANDO DE INGENIERÍA ENVIADO: " + action + " (Requiere hardware Android)");
                    // Feedback visual en el navegador
                    if (action != "TERMINATE_DIAGNOSTICS") {
                        val parts = action.split("|")
                        val cmd = parts[0]
                        when(cmd) {
                            "START_CAPTURE" -> {
                                js("setTimeout(function(){ if(window.dispatch_code_captured) window.dispatch_code_captured('RF_433', 'A1B2C3D4'); }, 3000);")
                            }
                            "GET_WIFI_SCAN" -> {
                                js("""
                                    setTimeout(function(){
                                        if(window.dispatch_wifi_list) {
                                            // MODO SIMULACIÓN ACTIVA: Devolvemos datos de prueba para que la UI no se bloquee
                                            var mockData = "MOVISTAR_A1B2|00:11:22:33:44:55|-60|WPA2|MOVISTAR|true|false|PATRON_CONOCIDO_123|;Vodafone-C234|AA:BB:CC:DD:EE:FF|-45|WPA2|VODAFONE|true|true|VODAFONE_DEFAULT_99|12345670;TP-LINK_EXT|BB:CC:DD:EE:FF:00|-70|WPA2|TP-LINK|false|true|ADMIN_1234|;";
                                            window.dispatch_wifi_list(mockData);
                                        }
                                    }, 2000);
                                """)
                            }
                        }
                        notificationState.value = AppNotification(
                            title = "SIMULACIÓN ACTIVA",
                            message = "Ejecutando " + action + ". En el móvil esto activaría el hardware inductivo.",
                            type = NotificationType.Info
                        )
                    }
                }
            },
            onWifiListReceived = { callback ->
                win.dispatch_wifi_list_to_app = callback
            },
            onWifiAuthResultReceived = { callback ->
                win.dispatch_wifi_auth_callback = callback
            },
            onEngineeringFinished = { callback ->
                win.dispatch_engineering_finished_to_app = callback
            },
            onRequestLocationPermission = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.requestLocationPermission != null) {
                    win.AndroidApp.requestLocationPermission()
                }
            },
            onOpenSettings = {
                val win: dynamic = window
                if (win.AndroidApp != null && win.AndroidApp.openDeveloperOptions != null) {
                    win.AndroidApp.openDeveloperOptions()
                } else {
                    // --- 🛡️ FALLBACK WEB: INSTRUCCIONES ---
                    js("alert('⚙️ AJUSTES DE RADAR (ANDROID)\\n\\nEn dispositivos Android, esta opción abre las Opciones de Desarrollo para desactivar el \\'Limitador de búsqueda WiFi\\', permitiendo escaneos cada 4 segundos en lugar de cada 30.\\n\\nEn la web, el radar funciona en modo simulación continua.');")
                }
            },
            onBgVolumeChange = { v: Float -> 
                js("""
                    localStorage.setItem("bgVol", v);
                    if (window.app && window.app.ctx) window.app.ctx.resume();
                    if (window.app && window.app.bgRadioGain) {
                        var cur = window.app.ctx.currentTime;
                        window.app.bgRadioGain.gain.setTargetAtTime(v, cur, 0.1);
                    }
                    if (window.fmEngine && window.fmEngine.audio) {
                        window.fmEngine.audio.volume = v;
                    }
                """)
            },
            onChatOpenConsumed = { 
                forceChatOpenState.value = false
                js("if(window.app) window.app.forceChatOpen = false;") 
            },
            onChatTargetConsumed = {
                forceChatTargetState.value = null
                js("if(window.app) window.app.forceChatTarget = null;")
            },
            forceChatOpen = forceChatOpenState.value,
            forceChatTarget = forceChatTargetState.value,
            nasaImageUrl = nasaImageUrlState.value ?: initialState.nasaImageUrl,
            nasaImageTitle = nasaImageTitleState.value ?: initialState.nasaImageTitle,
            nasaImageExplanation = nasaImageExplanationState.value ?: initialState.nasaImageExplanation,
            onDgtUpdate = { text ->
                // Actualización vía locutor (opcional)
            },
            dgtText = dgtTextState.value,
            dgtImageUrl = dgtImageUrlState.value,
            voxActive = voxActiveState.value,
            wifiVerificationResult = wifiVerificationResultState.value,
            initialState = initialState
        )
        
        LaunchedEffect(pttBlockedTrigger.value) {
            if (pttBlockedTrigger.value) {
                delay(500)
                pttBlockedTrigger.value = false
            }
        }

        LaunchedEffect(forceInitialScreenState.value) {
            if (forceInitialScreenState.value) {
                delay(100)
                forceInitialScreenState.value = false
            }
        }
        
        LaunchedEffect(Unit) { 
            js("if(window.initFirebaseListener) window.initFirebaseListener();") 
            js("if(window.setupBluetoothPTT) window.setupBluetoothPTT();")
        }
    }
    return null
    })
}
