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
                    window.app.masterRxGain.gain.value = 3.5; 
                    
                    window.app.currentMasterGain = 2.1; 

                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass"; 
                    window.app.filter.frequency.value = 1500; 
                    window.app.filter.Q.value = 1.2; 
                    
                    // 🛡️ MOTOR DE SUPERVIVENCIA (iOS/ANDROID/CHROME):
                    // No usamos compresores al final que puedan silenciar señales débiles.
                    window.app.filter.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.masterOut);
                    
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

                    // 🛡️ REFUERZO DE MOTOR (HOT-WIRE): Forzar al navegador a mantener vivo el procesamiento
                    // Conectamos el canal de transmisión a una salida local silenciada para que el 
                    // AudioContext nunca deje de bombear datos al nodo de red (WebRTC).
                    var txHotWire = window.app.ctx.createGain();
                    txHotWire.gain.value = 0;
                    window.app.txGate.connect(txHotWire);
                    txHotWire.connect(window.app.ctx.destination);

                    // 🛡️ AUDIO KEEP-ALIVE: Oscilador ultrasónico (20kHz) inaudible
                    window.app.keepAlive = window.app.ctx.createOscillator();
                    window.app.keepAlive.frequency.value = 20000;
                    window.app.keepAliveGain = window.app.ctx.createGain();
                    window.app.keepAliveGain.gain.value = 0.01; 
                    window.app.keepAlive.connect(window.app.keepAliveGain);
                    window.app.keepAliveGain.connect(window.app.txBus);
                    window.app.keepAlive.start();

                    // 🛡️ DITHER ANTI-GATE: Ruido de confort para evitar que el navegador anule la voz.
                    var ditherBuffer = window.app.ctx.createBuffer(1, window.app.ctx.sampleRate, window.app.ctx.sampleRate),
                        ditherData = ditherBuffer.getChannelData(0);
                    for (var i = 0; i < ditherData.length; i++) { ditherData[i] = (Math.random() * 2 - 1) * 0.001; }
                    var ditherSource = window.app.ctx.createBufferSource();
                    ditherSource.buffer = ditherBuffer;
                    ditherSource.loop = true;
                    ditherSource.connect(window.app.txBus);
                    ditherSource.start();

                    window.app.moniGainNode = window.app.ctx.createGain();
                    window.app.moniGainNode.gain.value = 0;
                    window.app.moniGainNode.connect(window.app.masterOut);

                    // 📻 BUS DE RECEPCIÓN (AUDIBLE): Voz remota pura para los altavoces.
                    window.app.rxReplayBus = window.app.ctx.createGain();
                    window.app.rxReplayBus.gain.value = 1.0;
                    window.app.rxReplayBus.connect(window.app.filter);

                    // 🛡️ BUS MAESTRO DE GRABACIÓN (SILENCIOSO): Agrega todo lo que se debe grabar (RX + TX)
                    // No está conectado a los altavoces, por lo que no causa eco ni feedback.
                    window.app.masterRecordBus = window.app.ctx.createGain();
                    window.app.rxReplayBus.connect(window.app.masterRecordBus);

                    // 🛡️ REPLAY COMPRESSOR: Asegura que la grabación tenga volumen constante y potente
                    window.app.replayCompressor = window.app.ctx.createDynamicsCompressor();
                    window.app.replayCompressor.threshold.value = -15;
                    window.app.replayCompressor.ratio.value = 10;
                    
                    window.app.masterRecordBus.connect(window.app.replayCompressor);
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
                
                // 🛡️ SINCRONIZACIÓN DE MONITOR (MONI)
                if(window.updateMoniGain) window.updateMoniGain();
                
                var now = window.app.ctx.currentTime;
                if (active) {
                    // 🔒 HARD-LOCK: SILENCIO DE RECEPCIÓN DURANTE TRANSMISIÓN
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    }
                    // 🛡️ FIX MONITOR: No silenciar masterOut (que cortaría el MONI), solo la radio entrante y ruido
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    // 📻 ROGER BEEP HACK: Mantener la puerta abierta 400ms para que el tono llegue al otro lado
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(0, now + 0.4, 0.01);
                    }
                    
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.5, now, 0.2);
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
                
                // 🛡️ DOM SINK FIX (PROTECTED): Elemento de audio invisible para decodificación WebRTC
                var remoteAudio = document.createElement("audio");
                remoteAudio.setAttribute("autoplay", "true");
                remoteAudio.setAttribute("playsinline", "true");
                remoteAudio.muted = true; // 🔒 OBLIGATORIO: Muted para evitar bloqueos de auto-play
                
                remoteAudio.style.display = "none";
                document.body.appendChild(remoteAudio);
                
                call.on('stream', function(remoteStream) {
                    if (!window.app.ctx) return;
                    
                    remoteAudio.srcObject = remoteStream;
                    
                    // 🛡️ DESPERTAR MOTOR: Solo si es estrictamente necesario
                    if (window.app.ctx.state === 'suspended') {
                        window.app.ctx.resume();
                    }
                    
                    remoteAudio.play().catch(function(e) { /* Silenciar error de play en muted */ });

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
                    window.app.remoteSinks = window.app.remoteSinks || {};
                    window.app.remoteSinks[call.peer] = remoteAudio;
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
                    if (window.app.remoteSinks && window.app.remoteSinks[call.peer]) {
                        window.app.remoteSinks[call.peer].remove();
                        delete window.app.remoteSinks[call.peer];
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
                    peers.forEach(id => { if(window.app.remoteGains[id]) window.app.remoteGains[id].gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.1); });
                    return;
                }

                // Encontrar la potencia máxima actual entre los que están hablando
                var maxPwr = 0;
                peers.forEach(id => {
                    var p = window.app.remotePowers[id] || 0.7;
                    if(p > maxPwr) maxPwr = p;
                });

                peers.forEach(id => {
                    var gain = window.app.remoteGains[id];
                    var source = window.app.remoteSources[id];
                    if(!gain || !source) return;
                    
                    var p = window.app.remotePowers[id] || 0.7;
                    var diff = maxPwr - p;
                    
                    // 🛡️ MOTOR DE DISTORSIÓN DINÁMICA (EFECTO PISADO REAL)
                    if (!window.app.remoteDistortion) window.app.remoteDistortion = {};
                    if (!window.app.remoteDistortion[id]) {
                        var dist = window.app.ctx.createWaveShaper();
                        dist.oversample = '4x';
                        window.app.remoteDistortion[id] = dist;
                        
                        // Ruteo: Source -> Dist -> Gain
                        source.disconnect();
                        source.connect(dist);
                        dist.connect(gain);
                    }

                    var distNode = window.app.remoteDistortion[id];
                    
                    if(diff <= 0.05) {
                        // Señal limpia
                        gain.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.1);
                        distNode.curve = null; 
                    } else if (diff > 0.25) {
                        // APLASTAMIENTO TOTAL (Distorsión extrema + Casi silencio)
                        gain.gain.setTargetAtTime(0.01, window.app.ctx.currentTime, 0.1);
                        distNode.curve = null;
                    } else {
                        // PISADO REAL (Voz deformada y de fondo)
                        var reduction = Math.max(0.05, 0.3 - diff);
                        gain.gain.setTargetAtTime(reduction, window.app.ctx.currentTime, 0.1);
                        
                        // Generar curva de saturación según la diferencia
                        var k = diff * 800; // Nivel de "suciedad"
                        var n_samples = 44100, curve = new Float32Array(n_samples);
                        for (var i = 0; i < n_samples; ++i ) {
                            var x = i * 2 / n_samples - 1;
                            curve[i] = ( 3 + k ) * x / ( Math.PI + k * Math.abs(x) );
                        }
                        distNode.curve = curve; 
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
            window.app.canPlaySounds = false; // 🛡️ BLOQUEO INICIAL: No pitar durante la carga

            window.playUiSound = function(type) {
                if(!window.app.ctx || !window.app.canPlaySounds) return;
                var now = window.app.ctx.currentTime;
                
                if (type === "incoming") {
                    // 🎵 PIRIPI POLICÍA NACIONAL REFORZADO: Tres tonos ultra-agudos, potentes y rítmicos
                    [3200, 3800, 4800].forEach((f, i) => {
                        var o = window.app.ctx.createOscillator();
                        var g = window.app.ctx.createGain();
                        o.type = "sine";
                        o.frequency.setValueAtTime(f, now + (i * 0.045));
                        g.gain.setValueAtTime(0, now + (i * 0.045));
                        g.gain.linearRampToValueAtTime(0.15, now + (i * 0.045) + 0.01);
                        g.gain.linearRampToValueAtTime(0, now + (i * 0.045) + 0.04);
                        o.connect(g); g.connect(window.app.masterOut);
                        o.start(now + (i * 0.045)); o.stop(now + (i * 0.045) + 0.045);
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
                    var now = window.app.ctx.currentTime;
                    // 🛡️ REGLA TÁCTICA: Solo activar si MONI está ON y además el PTT está pulsado
                    var target = (window.app.moniActive && window.app.pttStateInternal) ? (window.app.moniVolume * 0.18) : 0;
                    window.app.moniGainNode.gain.setTargetAtTime(target, now, 0.05);
                }
            };
        """)
    }
}

private object ReplayEngine {
    fun install() {
        js("""
            var replayBlobs = [];
            var activityDetected = false;
            var isPlaying = false;

            window.initReplayRecorder = function() {
                if (!window.app.ctx || !window.app.rxReplayBus || !window.app.replayCompressor) {
                    setTimeout(window.initReplayRecorder, 1000);
                    return;
                }
                
                var streamDest = window.app.ctx.createMediaStreamDestination();
                window.app.replayCompressor.connect(streamDest);
                
                var replayAnalyser = window.app.ctx.createAnalyser();
                replayAnalyser.fftSize = 256;
                window.app.replayCompressor.connect(replayAnalyser);

                function monitor() {
                    if (replayAnalyser) {
                        var d = new Uint8Array(replayAnalyser.fftSize);
                        replayAnalyser.getByteTimeDomainData(d);
                        for(var i=0; i<d.length; i++) {
                            // 🛡️ UMBRAL TÁCTICO: Filtrar estática para no grabar "vacío"
                            if (Math.abs(d[i] - 128) > 8) { 
                                activityDetected = true;
                                break;
                            }
                        }
                    }
                    requestAnimationFrame(monitor);
                }
                monitor();

                function startChunk() {
                    var chunks = [];
                    var hasVoiceInThisChunk = false;
                    activityDetected = false; 

                    try {
                        // 🚀 COMPATIBILIDAD TOTAL DE CODECS
                        var mimeType = "audio/webm;codecs=opus";
                        if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "audio/webm";
                        if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "";
                        
                        var recorder = new MediaRecorder(streamDest.stream, mimeType ? { mimeType: mimeType } : {});
                        
                        recorder.ondataavailable = function(e) { 
                            if (e.data && e.data.size > 0) chunks.push(e.data); 
                        };

                        recorder.onstop = function() {
                            if (chunks.length > 0 && hasVoiceInThisChunk) {
                                var blob = new Blob(chunks, { type: 'audio/webm' });
                                replayBlobs.push(blob);
                                if (replayBlobs.length > 6) replayBlobs.shift();
                                if (window.dispatch_replay_available) window.dispatch_replay_available(true);
                            }
                            startChunk(); 
                        };

                        var checkInterval = setInterval(function() {
                            if (activityDetected) hasVoiceInThisChunk = true;
                        }, 100);

                        setTimeout(function() {
                            clearInterval(checkInterval);
                            if (recorder.state === "recording") recorder.stop();
                        }, 5000);

                        recorder.start();
                    } catch(e) { 
                        console.error("Replay Recorder Error:", e);
                        setTimeout(startChunk, 2000); 
                    }
                }
                startChunk();
            };

            window.playReplay = function() {
                if (replayBlobs.length === 0 || isPlaying) return;
                
                isPlaying = true;
                // 🛡️ ONE-SHOT REPLAY: Mover contenido a playlist y vaciar la memoria inmediatamente
                var playlist = [...replayBlobs];
                replayBlobs = []; 
                
                // Apagar el aviso de "audio disponible"
                if (window.dispatch_replay_available) window.dispatch_replay_available(false);

                var index = 0;

                // 🛡️ DUCKING TÁCTICO: Bajar la radio en vivo al mínimo absoluto para un audio de Replay perfecto
                var targetRx = 3.5; 
                var targetNoise = window.app.currentNoiseTarget || 0;
                
                window.app.masterRxGain.gain.setTargetAtTime(0.02, window.app.ctx.currentTime, 0.15);
                window.app.noise.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.15);

                function finishReplay() {
                    isPlaying = false;
                    var now = window.app.ctx.currentTime;
                    // Restaurar volumen original de forma suave
                    window.app.masterRxGain.gain.setTargetAtTime(targetRx, now, 0.3);
                    window.app.noise.gain.setTargetAtTime(targetNoise, now, 0.3);
                    if (window.dispatch_replay_progress) window.dispatch_replay_progress(0);
                }

                function playNext() {
                    if (index >= playlist.length) {
                        finishReplay();
                        return;
                    }

                    var url = URL.createObjectURL(playlist[index]);
                    var audio = new Audio(url);
                    
                    // 🛡️ SYNC: Conectar el audio de replay a nuestra cadena de salida
                    var source = window.app.ctx.createMediaElementSource(audio);
                    source.connect(window.app.masterOut);
                    
                    audio.playbackRate = 1.15;
                    audio.onplay = function() {
                        if (window.dispatch_replay_available) window.dispatch_replay_available(true);
                        if (window.dispatch_replay_progress) window.dispatch_replay_progress((index + 1) / playlist.length);
                    };
                    audio.onended = function() {
                        URL.revokeObjectURL(url);
                        source.disconnect();
                        index++;
                        playNext();
                    };
                    audio.onerror = function() {
                        URL.revokeObjectURL(url);
                        source.disconnect();
                        index++;
                        playNext();
                    };
                    audio.play().catch(finishReplay);
                }

                playNext();
            };
            
            setTimeout(window.initReplayRecorder, 3000);
        """)
    }
}
