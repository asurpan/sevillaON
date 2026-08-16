package com.sagon.on

import kotlinx.browser.localStorage
import kotlinx.browser.window

/**
 * 🎙️ RADIO AUDIO MANAGER: MOTOR DE SONIDO PROFESIONAL
 * 🔒 HARD-LOCK: PROTECTED CORE - NO MODIFICAR LÓGICA DE RUTEO
 * ⚠️ AVISO: El motor VOX ha sido movido a ManosLibres.kt. MANTENER SEPARADO.
 */
object RadioAudioManager {
    fun install() {
        js("""
            window.app = window.app || { 
                activeCalls: {}, remoteSources: {}, remoteAnalysers: {}, 
                remoteGains: {}, remotePowers: {} 
            };
            
            window.initAudio = function() {
                if (window.app.ctx) {
                    if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                    return;
                }
                var AC = window.AudioContext || window.webkitAudioContext;
                try {
                    window.app.ctx = new AC({ latencyHint: 'interactive', sampleRate: 48000 });
                    
                    window.app.masterOut = window.app.ctx.createGain();
                    window.app.masterOut.connect(window.app.ctx.destination);
                    
                    window.app.masterRxGain = window.app.ctx.createGain();
                    window.app.masterRxGain.gain.value = 3.5; // 🚀 GANANCIA DE RECEPCIÓN EXTRA
                    
                    window.app.currentMasterGain = 2.1; // 🛡️ INICIO POTENTE (Equivale a 70% con multiplicador x3)

                    window.app.compressor = window.app.ctx.createDynamicsCompressor();
                    window.app.compressor.threshold.value = -20;
                    window.app.compressor.ratio.value = 8;

                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass"; 
                    window.app.filter.frequency.value = 1500; 
                    window.app.filter.Q.value = 1.2; 
                    
                    window.app.filter.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.compressor);
                    window.app.compressor.connect(window.app.masterOut);
                    
                    // --- 🌊 GENERADOR DE QRM REAL (FILTRADO Y OSCILANTE) ---
                    var bufferSize = 2 * window.app.ctx.sampleRate,
                        noiseBuffer = window.app.ctx.createBuffer(1, bufferSize, window.app.ctx.sampleRate),
                        output = noiseBuffer.getChannelData(0);
                    for (var i = 0; i < bufferSize; i++) { output[i] = Math.random() * 2 - 1; }
                    
                    var noiseSource = window.app.ctx.createBufferSource();
                    noiseSource.buffer = noiseBuffer;
                    noiseSource.loop = true;
                    
                    window.app.noise = window.app.ctx.createGain();
                    window.app.noise.gain.value = 0; 

                    // 📻 FILTRO PASO-BANDA NATURAL (HISS REALISTA, SIN RESONANCIA METÁLICA)
                    var noiseFilter = window.app.ctx.createBiquadFilter();
                    noiseFilter.type = "bandpass";
                    noiseFilter.frequency.value = 1450; 
                    noiseFilter.Q.value = 1.8; // Más ancho para un siseo suave y natural

                    window.app.lfo = window.app.ctx.createOscillator();
                    window.app.lfo.frequency.value = 0.05; // Oscilación lentísima
                    window.app.lfoGain = window.app.ctx.createGain();
                    window.app.lfoGain.gain.value = 0; // Empieza apagado
                    
                    window.app.lfo.connect(window.app.lfoGain);
                    window.app.lfoGain.connect(window.app.noise.gain);
                    window.app.lfo.start();

                    noiseSource.connect(noiseFilter);
                    noiseFilter.connect(window.app.noise);
                    window.app.noise.connect(window.app.filter); 
                    noiseSource.start();

                    window.setNoiseVolume = function(v) {
                        if (!window.app.noise || window.app.isTransmittingInternal) return;
                        
                        var now = window.app.ctx.currentTime;
                        if (v <= 0) {
                            // 🔒 SILENCIO TOTAL
                            window.app.currentNoiseTarget = 0;
                            window.app.noise.gain.setTargetAtTime(0, now, 0.1);
                            if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.1);
                        } else {
                            // 📻 RUIDO ACTIVO (SUTIL Y METÁLICO)
                            window.app.currentNoiseTarget = (v * 0.28) + 0.01; 
                            window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget, now, 0.1);
                            if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0.008, now, 0.1);
                        }
                    };

                    window.setMasterVolume = function(v) {
                        if (!window.app || !window.app.masterOut) return;
                        var now = window.app.ctx.currentTime;
                        // 🚀 MOTOR DE POTENCIA: Mapeo agresivo para sonar por encima del sistema
                        // 0.0 -> 0.0, 0.7 -> 1.8, 1.0 -> 3.0
                        window.app.currentMasterGain = v * 3.0; 
                        if (!window.app.pttStateInternal) {
                            window.app.masterOut.gain.setTargetAtTime(window.app.currentMasterGain, now, 0.1);
                        }
                    };

                    window.updateMasterVolume = function() {
                        if (window.app && window.app.masterOut) {
                            if (window.app.pttStateInternal) return;
                            var now = window.app.ctx.currentTime;
                            window.app.masterOut.gain.setTargetAtTime(window.app.currentMasterGain || 1.0, now, 0.1);
                        }
                    };

                    // 🛡️ WAKE LOCK (PREVENIR SUSPENSIÓN)
                    window.app.requestWakeLock = function() {
                        if ('wakeLock' in navigator && document.visibilityState === 'visible') {
                            navigator.wakeLock.request('screen')
                                .then(lock => {
                                    window.app.screenLock = lock;
                                    console.log("🛡️ Wake Lock Activo");
                                })
                                .catch(err => { console.log("Wake Lock fallido:", err.message); });
                        }
                    };
                    window.app.requestWakeLock();
                    
                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);

                    // 🛡️ AUDIO KEEP-ALIVE: Oscilador ultrasónico (21kHz) inaudible
                    // Esto evita que el canal WebRTC se cierre por "inactividad" de audio.
                    window.app.keepAlive = window.app.ctx.createOscillator();
                    window.app.keepAlive.frequency.value = 21000;
                    window.app.keepAliveGain = window.app.ctx.createGain();
                    window.app.keepAliveGain.gain.value = 0.001; // Amplitud mínima
                    window.app.keepAlive.connect(window.app.keepAliveGain);
                    window.app.keepAliveGain.connect(window.app.txBus);
                    window.app.keepAlive.start();

                    // 🛡️ DITHER ANTI-GATE: Ruido de confort infinitesimal para evitar que el navegador anule la voz.
                    // Genera un flujo de datos constante para que el canal WebRTC nunca se cierre por silencio.
                    var ditherBuffer = window.app.ctx.createBuffer(1, window.app.ctx.sampleRate, window.app.ctx.sampleRate),
                        ditherData = ditherBuffer.getChannelData(0);
                    for (var i = 0; i < ditherData.length; i++) { ditherData[i] = (Math.random() * 2 - 1) * 0.0001; }
                    var ditherSource = window.app.ctx.createBufferSource();
                    ditherSource.buffer = ditherBuffer;
                    ditherSource.loop = true;
                    ditherSource.connect(window.app.txBus);
                    ditherSource.start();

                    window.app.moniGainNode = window.app.ctx.createGain();
                    window.app.moniGainNode.gain.value = 0;
                    window.app.moniGainNode.connect(window.app.masterOut);

                    // 📻 BUS DE RECEPCIÓN (PURO): Sólo voz remota, sin ruido local.
                    window.app.rxReplayBus = window.app.ctx.createGain();
                    window.app.rxReplayBus.connect(window.app.filter);
                } catch(e) { }
            };

            window.requestMicPermission = function() {
                // 🔒 REDIRECCIÓN AL NÚCLEO PROTEGIDO DE MANOS LIBRES
                if (window.manosLibres_requestMic) return window.manosLibres_requestMic();
                return Promise.resolve(false);
            };

            window.broadcastPTT = function(active, roger, power) {
                if(!window.app) return;
                var appRoger = (window.app.rogerEnabled !== undefined) ? window.app.rogerEnabled : true;
                var effectiveRoger = (roger !== undefined) ? roger : appRoger;

                if (!window.app.ctx && window.initAudio) window.initAudio();
                if (!window.app.ctx) return; 

                if (active) {
                    if (window.app.ctx.state === 'suspended') window.app.ctx.resume();

                    // --- 🛡️ TOT: TIME-OUT TIMER (60 SEGUNDOS) ---
                    if (window.app.totTimer) clearTimeout(window.app.totTimer);
                    window.app.totTimer = setTimeout(function() {
                        if (window.app.pttStateInternal) {
                            console.warn("⚠️ TOT LIMIT REACHED");
                            window.broadcastPTT(false, false);
                            if (window.playUiSound) window.playUiSound("error_tot");
                        }
                    }, 60000); // 1 Minuto máximo de portadora continua
                } else {
                    if (window.app.totTimer) { clearTimeout(window.app.totTimer); window.app.totTimer = null; }
                }

                if (active && !window.app.rawStream) {
                    window.requestMicPermission();
                }

                if (window.app.pttStateInternal === active) return;
                window.app.pttStateInternal = active;
                window.app.isTransmittingInternal = active;
                
                if(window.dispatch_ptt_live) window.dispatch_ptt_live(active);
                
                var now = window.app.ctx.currentTime;
                if (active) {
                    // 🔒 HARD-LOCK: SILENCIO ABSOLUTO LOCAL DURANTE TRANSMISIÓN
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    }
                    if (window.app.masterOut) {
                        window.app.masterOut.gain.cancelScheduledValues(now);
                        window.app.masterOut.gain.setValueAtTime(0, now);
                    }
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    // 📻 ROGER BEEP HACK: Mantener la puerta abierta 400ms para que el tono llegue al otro lado
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(0, now + 0.4, 0.01);
                    }
                    
                    if (window.app.masterOut) {
                        window.app.masterOut.gain.cancelScheduledValues(now);
                        var targetVol = (window.app.currentMasterGain !== undefined) ? window.app.currentMasterGain : 1.5;
                        window.app.masterOut.gain.setValueAtTime(targetVol, now);
                    }
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(2.0, now, 0.2);
                    // 🔒 Restaurar ruido de fondo de forma suave
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget || 0, now, 0.2);
                    // 🔒 Restaurar LFO de forma ultra-sutil
                    if (window.app.lfoGain && window.app.currentNoiseTarget > 0) window.app.lfoGain.gain.setTargetAtTime(0.008, now, 0.2);
                    
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                    if (effectiveRoger && window.playUiSound) window.playUiSound("ptt_off");
                }
            };
        """)
        
        RadioSignaling.install()
        js("""
            window.setupCallStream = function(call) {
                console.log("📞 Recibiendo llamada de:", call.peer);
                call.on('stream', function(remoteStream) {
                    if (!window.app.ctx) return;
                    console.log("🔊 Stream recibido de:", call.peer);
                    var source = window.app.ctx.createMediaStreamSource(remoteStream);
                    var analyser = window.app.ctx.createAnalyser();
                    var gainNode = window.app.ctx.createGain();
                    analyser.fftSize = 256;
                    
                    source.connect(analyser);
                    source.connect(gainNode);
                    if (window.app.rxReplayBus) gainNode.connect(window.app.rxReplayBus);
                    else gainNode.connect(window.app.filter);
                    
                    window.app.remoteSources[call.peer] = source;
                    window.app.remoteAnalysers[call.peer] = analyser;
                    window.app.remoteGains[call.peer] = gainNode;
                });
                call.on('close', function() {
                    if (window.app.remoteSources[call.peer]) {
                        window.app.remoteSources[call.peer].disconnect();
                        delete window.app.remoteSources[call.peer];
                    }
                    if (window.app.remoteGains[call.peer]) {
                        window.app.remoteGains[call.peer].disconnect();
                        delete window.app.remoteGains[call.peer];
                    }
                    delete window.app.remoteAnalysers[call.peer];
                    delete window.app.activeCalls[call.peer];
                    delete window.app.remotePowers[call.peer];
                });
            };

            // 📻 LÓGICA DE "PISARSE" (POWER PRIORITY)
            function updateRemotePriorities() {
                if(!window.app) return;
                var peers = Object.keys(window.app.remoteSources);
                if(peers.length <= 1) {
                    peers.forEach(id => { if(window.app.remoteGains[id]) window.app.remoteGains[id].gain.value = 1.0; });
                    return;
                }

                // Encontrar la potencia máxima actual
                var maxPwr = 0;
                peers.forEach(id => {
                    var p = window.app.remotePowers[id] || 0.7;
                    if(p > maxPwr) maxPwr = p;
                });

                peers.forEach(id => {
                    var gain = window.app.remoteGains[id];
                    if(!gain) return;
                    var p = window.app.remotePowers[id] || 0.7;
                    
                    if(p >= maxPwr) {
                        gain.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.1);
                    } else {
                        // El más débil se oye de fondo con ruido (efecto pisado)
                        var reduction = Math.max(0.1, 1.0 - (maxPwr - p));
                        gain.gain.setTargetAtTime(reduction * 0.3, window.app.ctx.currentTime, 0.1);
                    }
                });
            }
            setInterval(updateRemotePriorities, 200);
        """)
        ManosLibres.install()
        MoniGuard.install()
        ReplayEngine.install()
    }

    fun setPtt(active: Boolean, roger: Boolean, power: Float?) {
        val w = window.asDynamic()
        if (w.broadcastPTT != null) {
            w.broadcastPTT(active, roger, power)
        }
    }

    fun playReplay() {
        js("window.playReplay();")
    }
}

private object RadioSignaling {
    fun install() {
        js("""
            window.playUiSound = function(type) {
                if(!window.app.ctx) return;
                var now = window.app.ctx.currentTime;
                
                if (type === "incoming") {
                    // 🎵 PIRIPI POLICÍA NACIONAL: Tres tonos ultra-agudos y rápidos
                    [2800, 3400, 4000].forEach((f, i) => {
                        var o = window.app.ctx.createOscillator();
                        var g = window.app.ctx.createGain();
                        o.type = "sine";
                        o.frequency.setValueAtTime(f, now + (i * 0.04));
                        g.gain.setValueAtTime(0, now + (i * 0.04));
                        g.gain.linearRampToValueAtTime(0.08, now + (i * 0.04) + 0.01);
                        g.gain.linearRampToValueAtTime(0, now + (i * 0.04) + 0.035);
                        o.connect(g); g.connect(window.app.masterOut);
                        o.start(now + (i * 0.04)); o.stop(now + (i * 0.04) + 0.04);
                    });
                    return;
                }
                
                if (type === "user_in") {
                    // 🎵 BEEP GRAVE: Tono corto y bajo para entrada de usuario
                    var o = window.app.ctx.createOscillator();
                    var g = window.app.ctx.createGain();
                    o.type = "triangle";
                    o.frequency.setValueAtTime(440, now);
                    g.gain.setValueAtTime(0, now);
                    g.gain.linearRampToValueAtTime(0.08, now + 0.01);
                    g.gain.linearRampToValueAtTime(0, now + 0.12);
                    o.connect(g); g.connect(window.app.masterOut);
                    o.start(now); o.stop(now + 0.12);
                    return;
                }

                if (type === "error_tot") {
                    // 🎵 ERROR TOT: Dos tonos bajos y secos
                    [220, 220].forEach((f, i) => {
                        var o = window.app.ctx.createOscillator();
                        var g = window.app.ctx.createGain();
                        o.type = "square";
                        o.frequency.setValueAtTime(f, now + (i * 0.15));
                        g.gain.setValueAtTime(0, now + (i * 0.15));
                        g.gain.linearRampToValueAtTime(0.05, now + (i * 0.15) + 0.01);
                        g.gain.linearRampToValueAtTime(0, now + (i * 0.15) + 0.12);
                        o.connect(g); g.connect(window.app.masterOut);
                        o.start(now + (i * 0.15)); o.stop(now + (i * 0.15) + 0.15);
                    });
                    return;
                }

                var isRoger = (type === "ptt_off" || type === "rx_off");
                var freq = isRoger ? 1955 : 1800;
                var duration = isRoger ? 0.3 : 0.08;

                if (type === "ptt_off") {
                    window.app.isBeeping = true;
                    if(window.dispatch_beeping) window.dispatch_beeping(true);
                }

                var o = window.app.ctx.createOscillator();
                var g = window.app.ctx.createGain();
                
                o.type = "triangle"; 
                o.frequency.setValueAtTime(freq, now);
                
                var volume = isRoger ? 0.12 : 0.08;
                g.gain.setValueAtTime(volume, now); 
                g.gain.setValueAtTime(0, now + duration);
                
                o.connect(g); 
                g.connect(window.app.masterOut);
                if (window.app.txBus) g.connect(window.app.txBus);
                
                o.onended = function() {
                    if (type === "ptt_off") {
                        window.app.isBeeping = false;
                        if(window.dispatch_beeping) window.dispatch_beeping(false);
                    }
                };
                o.start(now); 
                o.stop(now + duration);
            };
        """)
    }
}

private object MoniGuard {
    fun install() {
        js("""
            window.updateMoniGain = function() {
                if(window.app && window.app.moniGainNode) {
                    var target = window.app.moniActive ? (window.app.moniVolume * 0.05) : 0;
                    window.app.moniGainNode.gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.1);
                }
            };
        """)
    }
}

private object ReplayEngine {
    fun install() {
        js("""
            var replayBlobs = [];
            var currentRecorder = null;
            var activityDetected = false;
            var replayAnalyser = null;

            window.initReplayRecorder = function() {
                if (!window.app.ctx || !window.app.rxReplayBus) return;
                
                var streamDest = window.app.ctx.createMediaStreamDestination();
                window.app.rxReplayBus.connect(streamDest);
                
                // Analizador para detectar silencio/voz
                replayAnalyser = window.app.ctx.createAnalyser();
                replayAnalyser.fftSize = 256;
                window.app.rxReplayBus.connect(replayAnalyser);

                function captureSegment() {
                    if (currentRecorder && currentRecorder.state === "recording") {
                        currentRecorder.stop();
                    }
                    
                    activityDetected = false;
                    var chunks = [];
                    
                    // Intentar encontrar el mejor codec soportado
                    var mimeType = "audio/webm;codecs=opus";
                    if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "audio/webm";
                    if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "audio/ogg;codecs=opus";
                    if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "";

                    try {
                        currentRecorder = new MediaRecorder(streamDest.stream, mimeType ? { mimeType: mimeType } : {});
                        currentRecorder.ondataavailable = function(e) { if (e.data.size > 0) chunks.push(e.data); };
                        currentRecorder.onstop = function() {
                            // 🛡️ REGLA DE ORO: Solo guardar si hubo actividad real (voz)
                            if (chunks.length > 0 && activityDetected) {
                                var blob = new Blob(chunks, { type: currentRecorder.mimeType || 'audio/webm' });
                                replayBlobs.push(blob);
                                // 🕒 Buffer de 1 minuto (12 bloques de 5s)
                                if (replayBlobs.length > 12) replayBlobs.shift();
                                if (window.dispatch_replay_available) window.dispatch_replay_available(true);
                            }
                        };
                        currentRecorder.start();
                    } catch(e) { console.error("Replay Recorder Error:", e); }
                }

                // Monitoreo de actividad (VOX de Replay)
                function monitorActivity() {
                    if (replayAnalyser && currentRecorder && currentRecorder.state === "recording") {
                        var d = new Uint8Array(replayAnalyser.fftSize);
                        replayAnalyser.getByteTimeDomainData(d);
                        for(var i=0; i<d.length; i++) {
                            if (Math.abs(d[i] - 128) > 2) { // 🛡️ SENSIBILIDAD AUMENTADA
                                activityDetected = true;
                                break;
                            }
                        }
                    }
                    requestAnimationFrame(monitorActivity);
                }

                captureSegment();
                monitorActivity();
                setInterval(captureSegment, 5000);
            };

            window.playReplay = function() {
                if (replayBlobs.length === 0) {
                    if (window.dispatch_notification) window.dispatch_notification('REPLAY VACÍO', 'No hay grabaciones con voz reciente.', 'info');
                    return;
                }
                
                var playlist = [...replayBlobs];
                var index = 0;

                function playNext() {
                    if (index >= playlist.length) {
                        if (window.dispatch_replay_progress) window.dispatch_replay_progress(0);
                        return;
                    }

                    var url = URL.createObjectURL(playlist[index]);
                    var audio = new Audio(url);
                    
                    // 🚀 PASAR RÁPIDO: Reproducción a 1.15x para ganar tiempo
                    audio.playbackRate = 1.15;
                    
                    audio.onplay = function() {
                        if (window.dispatch_replay_progress) {
                            window.dispatch_replay_progress((index + 1) / playlist.length);
                        }
                    };

                    audio.onended = function() {
                        URL.revokeObjectURL(url);
                        index++;
                        playNext();
                    };

                    audio.onerror = function() {
                        URL.revokeObjectURL(url);
                        index++;
                        playNext();
                    };

                    audio.play().catch(function(err) {
                        console.error("Replay segment error:", err);
                        index++;
                        playNext();
                    });
                }

                playNext();
            };
            
            setTimeout(window.initReplayRecorder, 3000);
        """)
    }
}
