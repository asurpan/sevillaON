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
            
            // 🛡️ MOTOR DE DIAGNÓSTICO WEBRTC
            window.app.diag = {
                micPermission: "unknown",
                ctxState: "none",
                txPackets: 0,
                rxPackets: {},
                lastError: null,
                inputDevice: "default",
                outputDevice: "default"
            };

            window.initAudio = function() {
                if (window.app.ctx) {
                    if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                    return;
                }
                var AC = window.AudioContext || window.webkitAudioContext;
                try {
                    // 🛡️ AUTO-LATENCY: Dejamos que el hardware decida el sampleRate para evitar conflictos en Android 13
                    window.app.ctx = new AC({ latencyHint: 'interactive' });
                    window.app.diag.ctxState = window.app.ctx.state;
                    
                    window.app.ctx.onstatechange = function() {
                        window.app.diag.ctxState = window.app.ctx.state;
                        // 🛡️ ANDROID 16 WATCHDOG: Si el sistema suspende el audio, intentar revivirlo
                        if (window.app.ctx.state !== 'running') {
                            window.app.ctx.resume().catch(e => {});
                        }
                    };

                    // 📟 REGISTRO MEDIASESSION (OBLIGATORIO ANDROID 16)
                    if ('mediaSession' in navigator) {
                        navigator.mediaSession.metadata = new MediaMetadata({
                            title: 'ON AIR SPAIN',
                            artist: 'Radio CB Digital',
                            album: 'Comunicación Élite',
                            artwork: [{ src: 'logo.png', sizes: '512x512', type: 'image/png' }]
                        });
                        navigator.mediaSession.playbackState = "playing";
                    }

                    // --- 🏗️ ARQUITECTURA DE SALIDA ---
                    window.app.masterOut = window.app.ctx.createGain();
                    window.app.masterOut.connect(window.app.ctx.destination);
                    window.app.masterOut.gain.setValueAtTime(1.0, window.app.ctx.currentTime);
                    
                    window.app.masterRxGain = window.app.ctx.createGain();
                    window.app.masterRxGain.gain.value = 2.5; 
                    
                    window.app.mainCompressor = window.app.ctx.createDynamicsCompressor();
                    window.app.mainCompressor.threshold.value = -18; 
                    window.app.mainCompressor.ratio.value = 12; 
                    window.app.mainCompressor.attack.value = 0.003; // Respuesta instantánea a picos
                    window.app.mainCompressor.release.value = 0.25; 

                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass"; 
                    window.app.filter.frequency.value = 1500; 
                    window.app.filter.Q.value = 1.2; 
                    
                    // 🔒 CADENA DE RECEPCIÓN (RUIDO): Filtro -> RxGain -> Compressor -> MasterOut
                    window.app.filter.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.mainCompressor);
                    window.app.mainCompressor.connect(window.app.masterOut);
                    
                    window.app.currentMasterGain = 1.0; 
                    window.app.masterOut.gain.setValueAtTime(1.0, window.app.ctx.currentTime);

                    // 🛡️ SISTEMA DE SUPERVIVENCIA (KEEP-ALIVE INFRASÓNICO)
                    // Inyectamos una frecuencia de 5Hz (inaudible) para mantener el hardware despierto.
                    window.app.silenceKeepAlive = window.app.ctx.createOscillator();
                    window.app.silenceKeepAlive.frequency.value = 5;
                    window.app.silenceKeepAliveGain = window.app.ctx.createGain();
                    window.app.silenceKeepAliveGain.gain.value = 0;
                    window.app.silenceKeepAlive.connect(window.app.silenceKeepAliveGain);
                    window.app.silenceKeepAliveGain.connect(window.app.masterOut);
                    window.app.silenceKeepAlive.start();

                    // 🛡️ MOTOR DE VIDA DINÁMICO (ANTI-ANDROID 16 SLEEP)
                    // Usamos una frecuencia que oscila para que el sistema no la filtre como ruido
                    window.app.androidKeepAlive = window.app.ctx.createOscillator();
                    window.app.androidKeepAlive.frequency.value = 20500; 
                    window.app.androidKeepAliveGain = window.app.ctx.createGain();
                    window.app.androidKeepAliveGain.gain.value = 0.008; // Un poco más fuerte para el DAC de A16
                    
                    var lfoA16 = window.app.ctx.createOscillator();
                    lfoA16.frequency.value = 0.5; // Oscilación lenta
                    var lfoA16Gain = window.app.ctx.createGain();
                    lfoA16Gain.gain.value = 100; // Mueve la frecuencia +-100Hz
                    lfoA16.connect(lfoA16Gain);
                    lfoA16Gain.connect(window.app.androidKeepAlive.frequency);
                    
                    window.app.androidKeepAlive.connect(window.app.androidKeepAliveGain);
                    window.app.androidKeepAliveGain.connect(window.app.ctx.destination);
                    window.app.androidKeepAlive.start();
                    lfoA16.start();
                    
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

                    var noiseFilter = window.app.ctx.createBiquadFilter();
                    noiseFilter.type = "bandpass";
                    noiseFilter.frequency.value = 1450; 
                    noiseFilter.Q.value = 1.8;

                    window.app.lfo = window.app.ctx.createOscillator();
                    window.app.lfo.frequency.value = 0.05; 
                    window.app.lfoGain = window.app.ctx.createGain();
                    window.app.lfoGain.gain.value = 0; 
                    
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
                            window.app.currentNoiseTarget = 0;
                            window.app.noise.gain.cancelScheduledValues(now);
                            window.app.noise.gain.setValueAtTime(0, now);
                            if (window.app.lfoGain) {
                                window.app.lfoGain.gain.cancelScheduledValues(now);
                                window.app.lfoGain.gain.setValueAtTime(0, now);
                            }
                        } else {
                            window.app.currentNoiseTarget = (v * 0.28); 
                            window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget, now, 0.1);
                            if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0.008, now, 0.1);
                        }
                    };

                    window.setMasterVolume = function(v) {
                        if (!window.app || !window.app.masterOut) return;
                        var now = window.app.ctx.currentTime;
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

                    // 🛡️ WAKE LOCK
                    window.app.requestWakeLock = function() {
                        if ('wakeLock' in navigator && document.visibilityState === 'visible') {
                            navigator.wakeLock.request('screen')
                                .then(lock => { window.app.screenLock = lock; })
                                .catch(err => { });
                        }
                    };
                    window.app.requestWakeLock();
                    
                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);

                    var txHotWire = window.app.ctx.createGain();
                    txHotWire.gain.value = 0;
                    window.app.txGate.connect(txHotWire);
                    // 🛡️ ECO-FIX: Desconexión física para garantizar silencio local al emitir
                    // txHotWire.connect(window.app.ctx.destination); 

                    window.app.keepAlive = window.app.ctx.createOscillator();
                    window.app.keepAlive.frequency.value = 20000;
                    window.app.keepAliveGain = window.app.ctx.createGain();
                    window.app.keepAliveGain.gain.value = 0.01; 
                    window.app.keepAlive.connect(window.app.keepAliveGain);
                    window.app.keepAliveGain.connect(window.app.txBus);
                    window.app.keepAlive.start();

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
                    // 🛡️ ECO-FIX: Desconexión física del monitor local
                    // window.app.moniGainNode.connect(window.app.masterOut); 

                    window.app.rxReplayBus = window.app.ctx.createGain();
                    window.app.rxReplayBus.gain.value = 1.0;
                    window.app.rxReplayBus.connect(window.app.filter);

                    window.app.masterRecordBus = window.app.ctx.createGain();
                    window.app.rxReplayBus.connect(window.app.masterRecordBus);

                    window.app.replayCompressor = window.app.ctx.createDynamicsCompressor();
                    window.app.replayCompressor.threshold.value = -15;
                    window.app.replayCompressor.ratio.value = 10;
                    window.app.masterRecordBus.connect(window.app.replayCompressor);
                } catch(e) { }
            };

            window.requestMicPermission = function() {
                if (window.manosLibres_requestMic) return window.manosLibres_requestMic();
                return Promise.resolve(false);
            };

            window.broadcastPTT = function(active, roger, power) {
                if(!window.app) return;
                var effectiveRoger = (roger !== undefined) ? roger : (window.app.rogerEnabled || true);

                if (!window.app.ctx && window.initAudio) window.initAudio();
                if (!window.app.ctx) return; 

                if (active) {
                    if (window.app.ctx.state !== 'running') {
                        window.app.ctx.resume().catch(function(e) { });
                    }
                    
                    // 🛡️ MOTOR DE VIDA DINÁMICO (WAKE-UP PULSE)
                    if (window.app.silenceKeepAliveGain) {
                        var nowPulse = window.app.ctx.currentTime;
                        window.app.silenceKeepAliveGain.gain.setValueAtTime(0.02, nowPulse);
                        window.app.silenceKeepAliveGain.gain.linearRampToValueAtTime(0, nowPulse + 0.15);
                    }

                    if (window.app.totTimer) clearTimeout(window.app.totTimer);
                    window.app.totTimer = setTimeout(function() {
                        if (window.app.pttStateInternal) {
                            window.broadcastPTT(false, false);
                            if (window.playUiSound) window.playUiSound("error_tot");
                        }
                    }, 60000); 
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
                if(window.updateMoniGain) window.updateMoniGain();
                
                var now = window.app.ctx.currentTime;
                if (active) {
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    }
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    if (window.app.txGate) {
                        window.app.txGate.gain.cancelScheduledValues(now);
                        window.app.txGate.gain.setTargetAtTime(0, now + 0.4, 0.01);
                    }
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.5, now, 0.2);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget || 0, now, 0.2);
                    if (window.app.lfoGain && window.app.currentNoiseTarget > 0) window.app.lfoGain.gain.setTargetAtTime(0.008, now, 0.2);
                    
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                    if (effectiveRoger && window.playUiSound) window.playUiSound("ptt_off");
                }
            };
        """)
        
        RadioSignaling.install()
        js("""
            window.setupCallStream = function(call) {
                console.log("🛡️ AUDITORÍA: Vinculando flujo WebRTC:", call.peer);
                
                var remoteAudio = document.createElement("audio");
                remoteAudio.setAttribute("autoplay", "true");
                remoteAudio.setAttribute("playsinline", "true");
                // 🛡️ ECO-FIX: Silencio físico total en el elemento DOM para evitar bucles del navegador
                remoteAudio.muted = true; 
                remoteAudio.volume = 0; 
                remoteAudio.style.cssText = "position:fixed;width:1px;height:1px;top:0;opacity:0.01;pointer-events:none;z-index:-1";
                document.body.appendChild(remoteAudio);
                
                call.on('stream', function(remoteStream) {
                    console.log("🔊 VOZ RECIBIDA. Tracks:", remoteStream.getAudioTracks().length);
                    remoteAudio.srcObject = remoteStream;
                    remoteAudio.play().catch(function(e) { });

                    if (!window.app.ctx && window.initAudio) window.initAudio();
                    if (!window.app.ctx) return;
                    
                    // 🚀 CREACIÓN INMEDIATA DE NODOS (No esperar a resume para evitar bloqueos de visibilidad)
                    var source = window.app.ctx.createMediaStreamSource(remoteStream);
                    var analyser = window.app.ctx.createAnalyser();
                    var gainNode = window.app.ctx.createGain();
                    analyser.fftSize = 256;
                    
                    source.connect(analyser);
                    source.connect(gainNode);
                    
                    // 🛡️ DECODING BRIDGE: Conexión técnica inaudible para estabilidad de hilos
                    var dummy = window.app.ctx.createGain();
                    dummy.gain.value = 0.005; // Recuperado para evitar que el navegador cierre la voz
                    source.connect(dummy); 
                    dummy.connect(window.app.ctx.destination);

                    window.app.remoteSources[call.peer] = source;
                    window.app.remoteAnalysers[call.peer] = analyser;
                    window.app.remoteGains[call.peer] = gainNode;
                    window.app.remoteDummies = window.app.remoteDummies || {};
                    window.app.remoteDummies[call.peer] = dummy;
                    window.app.remoteSinks = window.app.remoteSinks || {};
                    window.app.remoteSinks[call.peer] = remoteAudio;

                    // 🚀 HARD WAKEUP: Intentar despertar el audio en cada recepción
                    if (window.app.ctx.state !== 'running') {
                        window.app.ctx.resume().then(() => {
                            console.log("🔊 Audio despertado por RX entrante.");
                            if (window.updateMasterVolume) window.updateMasterVolume();
                        }).catch(e => {});
                    }
                    
                    // 🛡️ WATCHDOG: Re-vincular stream tras 1s por si el navegador bloqueó el arranque
                    setTimeout(function() {
                        if (remoteAudio.paused) remoteAudio.play().catch(function() { });
                    }, 1000);

                    // AUDITORÍA RX
                    window.app.diag.rxPackets[call.peer] = 0;
                    var diagInterval = setInterval(function() {
                        if (!window.app.peer || !call.peerConnection) { clearInterval(diagInterval); return; }
                        call.peerConnection.getStats(null).then(stats => {
                            stats.forEach(report => {
                                if (report.type === "inbound-rtp" && report.kind === "audio") {
                                    window.app.diag.rxPackets[call.peer] = report.packetsReceived;
                                }
                            });
                        }).catch(e => {});
                    }, 2000);
                });
                call.on('close', function() {
                    if (window.app.remoteSources[call.peer]) window.app.remoteSources[call.peer].disconnect();
                    if (window.app.remoteGains[call.peer]) window.app.remoteGains[call.peer].disconnect();
                    if (window.app.remoteSinks && window.app.remoteSinks[call.peer]) {
                        window.app.remoteSinks[call.peer].remove();
                        delete window.app.remoteSinks[call.peer];
                    }
                    delete window.app.remoteAnalysers[call.peer];
                    delete window.app.activeCalls[call.peer];
                });
            };

            function updateRemotePriorities() {
                if(!window.app) return;
                var peers = Object.keys(window.app.remoteSources);
                var isDiscrete = (window.app.discreteMode === true);

                if(peers.length <= 1) {
                    peers.forEach(id => { 
                        if(window.app.remoteGains[id]) {
                            var target = isDiscrete ? 0 : 1.0;
                            window.app.remoteGains[id].gain.setTargetAtTime(target, window.app.ctx.currentTime, 0.1); 
                        }
                        if (window.app.remoteDistortion && window.app.remoteDistortion[id]) {
                            window.app.remoteDistortion[id].curve = null;
                        }
                    });
                    return;
                }
                var maxPwr = 0;
                peers.forEach(id => {
                    var p = window.app.remotePowers[id] || 0.7;
                    if(p > maxPwr) maxPwr = p;
                });

                peers.forEach(id => {
                    var gain = window.app.remoteGains[id];
                    var source = window.app.remoteSources[id];
                    var ana = window.app.remoteAnalysers[id];
                    if(!gain || !source) return;

                    if (isDiscrete) {
                        gain.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.1);
                        if (window.app.remoteDummies && window.app.remoteDummies[id]) {
                            window.app.remoteDummies[id].gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.1);
                        }
                        return;
                    } else {
                        if (window.app.remoteDummies && window.app.remoteDummies[id]) {
                            window.app.remoteDummies[id].gain.setTargetAtTime(0.005, window.app.ctx.currentTime, 0.1);
                        }
                    }

                    var p = window.app.remotePowers[id] || 0.7;
                    var diff = maxPwr - p;
                    
                    // 🛡️ MOTOR DE VIDA DINÁMICO (KEEP-ALIVE DURANTE RECEPCIÓN)
                    if (window.app.silenceKeepAliveGain) {
                        var d = new Uint8Array(ana.fftSize);
                        ana.getByteTimeDomainData(d);
                        var peak = 0; 
                        for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>peak) peak=v; }
                        
                        var nowS = window.app.ctx.currentTime;
                        if (peak < 12) { // Silencio detectado (Umbral 12)
                            window.app.silenceKeepAliveGain.gain.setTargetAtTime(0.01, nowS, 0.1);
                        } else { // Voz detectada
                            window.app.silenceKeepAliveGain.gain.setTargetAtTime(0, nowS, 0.05);
                        }
                    }

                    var distNode = window.app.remoteDistortion[id];
                    if(!gain || !distNode) return;

                    if (isDiscrete) {
                        gain.gain.setTargetAtTime(1.0, window.app.ctx.currentTime, 0.1);
                        distNode.curve = null; 
                    } else if (diff > 0.25) {
                        gain.gain.setTargetAtTime(0.01, window.app.ctx.currentTime, 0.1);
                        distNode.curve = null;
                    } else {
                        var reduction = Math.max(0.05, 0.3 - diff);
                        gain.gain.setTargetAtTime(reduction, window.app.ctx.currentTime, 0.1);
                        var k = diff * 800;
                        var n_samples = 44100, curve = new Float32Array(n_samples);
                        for (var i = 0; i < n_samples; ++i ) {
                            var x = i * 2 / n_samples - 1;
                            curve[i] = ( 3 + k ) * x / ( Math.PI + k * Math.abs(x) );
                        }
                        distNode.curve = curve; 
                    }
                });
            }
            setInterval(updateRemotePriorities, 100);
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
            window.app.canPlaySounds = false; 

            window.playUiSound = function(type) {
                if(!window.app.ctx || !window.app.canPlaySounds) return;
                var now = window.app.ctx.currentTime;
                
                if (type === "incoming") {
                    var pairs = [[697, 1209], [770, 1336], [852, 1477], [941, 1633]]; // Secuencia DTMF Potente (1, 5, 9, D)
                    pairs.forEach((f, i) => {
                        f.forEach(freq => {
                            var o = window.app.ctx.createOscillator();
                            var g = window.app.ctx.createGain();
                            o.type = "sine";
                            o.frequency.setValueAtTime(freq, now + (i * 0.12));
                            g.gain.setValueAtTime(0, now + (i * 0.12));
                            g.gain.linearRampToValueAtTime(0.15, now + (i * 0.12) + 0.01);
                            g.gain.linearRampToValueAtTime(0, now + (i * 0.12) + 0.1);
                            o.connect(g); g.connect(window.app.masterOut);
                            o.start(now + (i * 0.12)); o.stop(now + (i * 0.12) + 0.12);
                        });
                    });
                    return;
                }
                
                if (type === "user_in") {
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
                // 🛡️ FIX: No enviar el Roger Beep a la red para evitar el "doble pitido" en los receptores
                // if (window.app.txBus) g.connect(window.app.txBus); 
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
                        var mimeType = "audio/webm;codecs=opus";
                        if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "audio/webm";
                        if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "";
                        var recorder = new MediaRecorder(streamDest.stream, mimeType ? { mimeType: mimeType } : {});
                        recorder.ondataavailable = function(e) { if (e.data && e.data.size > 0) chunks.push(e.data); };
                        recorder.onstop = function() {
                            if (chunks.length > 0 && hasVoiceInThisChunk) {
                                var blob = new Blob(chunks, { type: 'audio/webm' });
                                replayBlobs.push(blob);
                                if (replayBlobs.length > 6) replayBlobs.shift();
                                if (window.dispatch_replay_available) window.dispatch_replay_available(replayBlobs.length > 0);
                            }
                            startChunk(); 
                        };
                        var checkInterval = setInterval(function() { if (activityDetected) hasVoiceInThisChunk = true; }, 100);
                        setTimeout(function() {
                            clearInterval(checkInterval);
                            if (recorder.state === "recording") recorder.stop();
                        }, 5000);
                        recorder.start();
                    } catch(e) { setTimeout(startChunk, 2000); }
                }
                startChunk();
            };

            window.playReplay = function() {
                if (replayBlobs.length === 0 || isPlaying) return;
                isPlaying = true;
                var playlist = [...replayBlobs];
                replayBlobs = []; 
                if (window.dispatch_replay_available) window.dispatch_replay_available(false);
                var index = 0;
                var targetRx = 3.5; 
                var targetNoise = window.app.currentNoiseTarget || 0;
                window.app.masterRxGain.gain.setTargetAtTime(0.02, window.app.ctx.currentTime, 0.15);
                window.app.noise.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.15);

                function finishReplay() {
                    isPlaying = false;
                    var now = window.app.ctx.currentTime;
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
