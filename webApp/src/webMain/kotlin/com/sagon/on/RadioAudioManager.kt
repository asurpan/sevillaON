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
                    window.app.masterRxGain.gain.value = 2.0;
                    
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

                    window.updateMasterVolume = function() {
                        if (window.app && window.app.masterOut) {
                            // 🔒 HARD-LOCK: No subir el volumen si estamos transmitiendo
                            if (window.app.pttStateInternal) return;
                            window.app.masterOut.gain.setTargetAtTime(1.5, window.app.ctx.currentTime, 0.1);
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

                    window.app.moniGainNode = window.app.ctx.createGain();
                    window.app.moniGainNode.gain.value = 0;
                    window.app.moniGainNode.connect(window.app.masterOut);
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

                // --- 🛡️ TOT: TIME-OUT TIMER (60 SEGUNDOS) ---
                if (active) {
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
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    if (window.app.masterOut) window.app.masterOut.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.masterOut) window.app.masterOut.gain.setTargetAtTime(1.5, now, 0.1);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(2.0, now, 0.2);
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
                    gainNode.connect(window.app.filter);
                    
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
                g.gain.setValueAtTime(volume, now + duration - 0.02);
                g.gain.linearRampToValueAtTime(0.0, now + duration);
                
                o.connect(g); 
                g.connect(window.app.masterOut);
                
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
            var replayChunks = [];
            var recorder = null;
            var isRecording = false;

            window.initReplayRecorder = function() {
                if (!window.app.ctx || recorder) return;
                
                var streamDest = window.app.ctx.createMediaStreamDestination();
                window.app.filter.connect(streamDest);
                
                // Intentamos usar un codec compatible con la mayoría de navegadores móviles
                var options = { mimeType: 'audio/webm;codecs=opus' };
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    options = { mimeType: 'audio/ogg;codecs=opus' };
                }
                
                try {
                    recorder = new MediaRecorder(streamDest.stream, options);
                    
                    recorder.ondataavailable = function(e) {
                        if (e.data.size > 0) {
                            replayChunks.push(e.data);
                            // Mantener los últimos ~15 segundos (aprox 15 trozos de 1s)
                            if (replayChunks.length > 15) replayChunks.shift();
                            if (window.dispatch_replay_available) window.dispatch_replay_available(true);
                        }
                    };

                    // Grabación continua en trozos de 1 segundo para mayor fluidez
                    recorder.start(1000); 
                } catch(e) { console.error("MediaRecorder init error:", e); }
            };

            window.playReplay = function() {
                if (replayChunks.length === 0) return;
                
                // Consolidar todos los trozos en un solo Blob
                var blob = new Blob(replayChunks, { type: recorder.mimeType });
                var url = URL.createObjectURL(blob);
                var audio = new Audio(url);
                
                audio.onplay = function() {
                   if (window.dispatch_replay_progress) window.dispatch_replay_progress(0.1);
                };
                
                audio.onended = function() {
                    if (window.dispatch_replay_progress) window.dispatch_replay_progress(0);
                    URL.revokeObjectURL(url);
                };

                audio.play().catch(e => console.error("Error replay playback:", e));
            };
            
            // Iniciar grabador tras un breve delay para asegurar que el audio está listo
            setTimeout(window.initReplayRecorder, 2000);
        """)
    }
}
