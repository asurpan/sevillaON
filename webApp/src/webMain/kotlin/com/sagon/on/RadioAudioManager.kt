package com.sagon.on

import kotlinx.browser.window

/**
 * 🎙️ RADIO AUDIO MANAGER: MOTOR DE SONIDO PROFESIONAL
 * 🔒 HARD-LOCK: PROTECTED CORE - SELLADO TOTAL v9.8
 * ⚠️ AVISO: Lógica WebRTC y Ruteo Blindada. NO MODIFICAR.
 */
object RadioAudioManager {
    fun install() {
        js("""
            window.app = window.app || {};
            window.app.activeCalls = window.app.activeCalls || {};
            window.app.remoteSources = window.app.remoteSources || {};
            window.app.remoteAnalysers = window.app.remoteAnalysers || {};
            window.app.remoteGains = window.app.remoteGains || {};
            window.app.remotePowers = window.app.remotePowers || {};
            window.app.remoteDummies = window.app.remoteDummies || {};
            window.app.remoteSinks = window.app.remoteSinks || {};
            
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
                    if (window.app.ctx.state === 'suspended') {
                        window.app.ctx.resume().then(() => { console.log("🔊 AudioContext reanudado."); });
                    }
                    return;
                }
                console.log("🏗️ Inicializando Motor de Audio...");
                var AC = window.AudioContext || window.webkitAudioContext;
                try {
                    window.app.ctx = new AC({ latencyHint: 'interactive' });
                    window.app.diag.ctxState = window.app.ctx.state;
                    
                    // --- 🏗️ ARQUITECTURA DE SALIDA (REFUERZO v8.8) ---
                    window.app.masterOut = window.app.ctx.createGain();
                    window.app.masterOut.connect(window.app.ctx.destination);
                    window.app.masterOut.gain.setValueAtTime(1.0, window.app.ctx.currentTime);
                    
                    window.app.masterRxGain = window.app.ctx.createGain();
                    window.app.masterRxGain.gain.value = 2.5; 
                    
                    window.app.mainCompressor = window.app.ctx.createDynamicsCompressor();
                    window.app.mainCompressor.threshold.value = -18; 
                    window.app.mainCompressor.ratio.value = 12; 
                    window.app.mainCompressor.attack.value = 0.003; 
                    window.app.mainCompressor.release.value = 0.25; 

                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass"; 
                    window.app.filter.frequency.value = 1500; 
                    window.app.filter.Q.value = 1.2; 

                    // --- 🎛️ FILTRO DE PRESENCIA DSP DE VOZ ---
                    window.app.dspPresence = window.app.ctx.createBiquadFilter();
                    window.app.dspPresence.type = "peaking";
                    window.app.dspPresence.frequency.value = 2500;
                    window.app.dspPresence.Q.value = 1.0;
                    window.app.dspPresence.gain.value = 6.0; 

                    window.app.filter.connect(window.app.dspPresence);
                    window.app.dspPresence.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.mainCompressor);
                    window.app.mainCompressor.connect(window.app.masterOut);

                    window.updateDspSettings = function(enabled, level) {
                        if (!window.app || !window.app.ctx) return;
                        var now = window.app.ctx.currentTime;
                        var lvl = (level !== undefined) ? level : 0.5;

                        window.app.dspEnabled = enabled;
                        window.app.dspLevel = lvl;

                        if (window.app.filter) {
                            if (enabled) {
                                window.app.filter.type = "bandpass";
                                window.app.filter.frequency.setTargetAtTime(1200 + (lvl * 1000), now, 0.05);
                                window.app.filter.Q.setTargetAtTime(0.8 + (lvl * 1.2), now, 0.05);
                            } else {
                                window.app.filter.type = "allpass";
                            }
                        }

                        if (window.app.dspPresence) {
                            if (enabled) {
                                window.app.dspPresence.type = "peaking";
                                window.app.dspPresence.frequency.setTargetAtTime(2500, now, 0.05);
                                window.app.dspPresence.gain.setTargetAtTime(3.0 + (lvl * 9.0), now, 0.05);
                            } else {
                                window.app.dspPresence.gain.setTargetAtTime(0, now, 0.05);
                            }
                        }

                        if (window.app.mainCompressor) {
                            if (enabled) {
                                window.app.mainCompressor.threshold.setTargetAtTime(-18 - (lvl * 12), now, 0.05);
                                window.app.mainCompressor.ratio.setTargetAtTime(8 + (lvl * 8), now, 0.05);
                            } else {
                                window.app.mainCompressor.threshold.setTargetAtTime(0, now, 0.05);
                                window.app.mainCompressor.ratio.setTargetAtTime(1, now, 0.05);
                            }
                        }
                    };
                    
                    console.log("🔊 Nodos de Audio Conectados v8.5");
                    
                    window.app.currentMasterGain = 1.0; 

                    window.app.silenceKeepAlive = window.app.ctx.createOscillator();
                    window.app.silenceKeepAlive.frequency.value = 5;
                    window.app.silenceKeepAliveGain = window.app.ctx.createGain();
                    window.app.silenceKeepAliveGain.gain.value = 0;
                    window.app.silenceKeepAlive.connect(window.app.silenceKeepAliveGain);
                    window.app.silenceKeepAliveGain.connect(window.app.masterOut);
                    window.app.silenceKeepAlive.start();

                    window.app.androidKeepAlive = window.app.ctx.createOscillator();
                    window.app.androidKeepAlive.frequency.value = 20500; 
                    window.app.androidKeepAliveGain = window.app.ctx.createGain();
                    window.app.androidKeepAliveGain.gain.value = 0.001; 
                    var lfoA16 = window.app.ctx.createOscillator();
                    lfoA16.frequency.value = 0.5; 
                    var lfoA16Gain = window.app.ctx.createGain();
                    lfoA16Gain.gain.value = 100; 
                    lfoA16.connect(lfoA16Gain);
                    lfoA16Gain.connect(window.app.androidKeepAlive.frequency);
                    window.app.androidKeepAlive.connect(window.app.androidKeepAliveGain);
                    window.app.androidKeepAliveGain.connect(window.app.ctx.destination); 
                    window.app.androidKeepAlive.start();
                    lfoA16.start();
                    
                    // 🛡️ SISTEMA DITHER (v9.8): Ruido Oculto Suave pero persistente
                    var ditherSize = window.app.ctx.sampleRate;
                    var ditherBuffer = window.app.ctx.createBuffer(1, ditherSize, window.app.ctx.sampleRate);
                    var ditherData = ditherBuffer.getChannelData(0);
                    for (var i = 0; i < ditherSize; i++) { ditherData[i] = (Math.random() * 2 - 1) * 0.05; }
                    var ditherSource = window.app.ctx.createBufferSource();
                    ditherSource.buffer = ditherBuffer;
                    ditherSource.loop = true;
                    var ditherGain = window.app.ctx.createGain();
                    ditherGain.gain.value = 0.005; // v9.8: Bajado a nivel inaudible
                    ditherSource.connect(ditherGain);
                    ditherGain.connect(window.app.masterOut);
                    ditherSource.start();
                    
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
                            window.app.noise.gain.setValueAtTime(0, now);
                        } else {
                            window.app.currentNoiseTarget = (v * 0.28); 
                            window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget, now, 0.1);
                        }
                    };

                    window.setMasterVolume = function(v) {
                        if (!window.app || !window.app.masterOut) return;
                        window.app.currentMasterGain = v * 3.0; 
                        if (!window.app.pttStateInternal) {
                            window.app.masterOut.gain.setTargetAtTime(window.app.currentMasterGain, window.app.ctx.currentTime, 0.1);
                        }
                    };

                    window.updateMasterVolume = function() {
                        if (window.app && window.app.masterOut && window.app.ctx) {
                            if (window.app.pttStateInternal) return;
                            try { window.app.masterOut.connect(window.app.ctx.destination); } catch(e) {}
                            window.app.masterOut.gain.setTargetAtTime(window.app.currentMasterGain || 1.0, window.app.ctx.currentTime, 0.1);
                        }
                    };

                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);

                    window.app.keepAlive = window.app.ctx.createOscillator();
                    window.app.keepAlive.frequency.value = 20000;
                    window.app.keepAliveGain = window.app.ctx.createGain();
                    window.app.keepAliveGain.gain.value = 0.01; 
                    window.app.keepAlive.connect(window.app.keepAliveGain);
                    window.app.keepAliveGain.connect(window.app.txBus);
                    window.app.keepAlive.start();

                    window.app.moniGainNode = window.app.ctx.createGain();
                    window.app.moniGainNode.gain.value = 0;
                    window.app.moniGainNode.connect(window.app.masterOut);

                    // 🎙️ BUS DE GRABACIÓN MASTER PARA REPLAY (PTT PROPIO + RX REMOTO)
                    window.app.masterRecordBus = window.app.ctx.createGain();
                    window.app.masterRecordBus.gain.value = 1.5;

                    if (window.app.txGate) window.app.txGate.connect(window.app.masterRecordBus);
                    if (window.app.masterRxGain) window.app.masterRxGain.connect(window.app.masterRecordBus);

                    window.app.replayCompressor = window.app.ctx.createDynamicsCompressor();
                    window.app.replayCompressor.threshold.value = -24;
                    window.app.replayCompressor.ratio.value = 8;
                    window.app.masterRecordBus.connect(window.app.replayCompressor);
                } catch(e) { }
            };

            window.broadcastPTT = function(active, roger, power) {
                if(!window.app) return;
                if (!window.app.ctx && window.initAudio) window.initAudio();
                if (!window.app.ctx) return; 
                if (window.app.ctx.state !== 'running') window.app.ctx.resume();

                var now = window.app.ctx.currentTime;
                var sessionRef = window.app.db ? window.app.db.ref("users/" + window.app.sessionID) : null;

                if (active) {
                    if (window.app.rogerReleaseTimeout) {
                        clearTimeout(window.app.rogerReleaseTimeout);
                        window.app.rogerReleaseTimeout = null;
                    }
                    window.app.pttStateInternal = true; 
                    window.app.isTransmittingInternal = true; 
                    window.app.isBeeping = false;

                    if (sessionRef) sessionRef.update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.updateMoniGain) window.updateMoniGain();
                } else {
                    window.app.pttStateInternal = false;

                    // 1. Cortar micrófono inmediatamente para no transmitir respiración/ruido tras soltar PTT
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, now, 0.01);

                    var shouldPlayRoger = (roger === true || window.app.rogerEnabled === true);

                    if (shouldPlayRoger && window.app.ctx) {
                        window.app.isBeeping = true;
                        window.app.isTransmittingInternal = true; // MANTENER PORTADORA ACTIVA DURANTE EL BEEP

                        // 2. Generar el Roger Beep y transmitirlo TANTO al altavoz local COMO por aire (txBus)
                        var beepOsc = window.app.ctx.createOscillator();
                        var beepGain = window.app.ctx.createGain();
                        
                        beepOsc.type = "triangle";
                        beepOsc.frequency.setValueAtTime(1955, now);
                        beepGain.gain.setValueAtTime(0.20, now);
                        beepGain.gain.setValueAtTime(0, now + 0.30);

                        beepOsc.connect(beepGain);
                        
                        if (window.app.masterOut) beepGain.connect(window.app.masterOut);
                        if (window.app.txBus) beepGain.connect(window.app.txBus);

                        beepOsc.start(now);
                        beepOsc.stop(now + 0.30);

                        // 3. APAGAR LA PORTADORA SOLO CUANDO HAYA TERMINADO EL ROGER BEEP
                        if (window.app.rogerReleaseTimeout) clearTimeout(window.app.rogerReleaseTimeout);
                        window.app.rogerReleaseTimeout = setTimeout(function() {
                            window.app.isTransmittingInternal = false;
                            window.app.isBeeping = false;
                            if (sessionRef) sessionRef.update({ tx: false });

                            var endNow = window.app.ctx ? window.app.ctx.currentTime : now;
                            if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.5, endNow, 0.15);
                            if (window.app.noise) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget || 0, endNow, 0.15);
                            if (window.updateMoniGain) window.updateMoniGain();
                        }, 350);

                    } else {
                        // Cierre inmediato sin Roger Beep
                        window.app.isTransmittingInternal = false;
                        window.app.isBeeping = false;
                        if (sessionRef) sessionRef.update({ tx: false });

                        if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(3.5, now, 0.15);
                        if (window.app.noise) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget || 0, now, 0.15);
                        if (window.updateMoniGain) window.updateMoniGain();
                    }
                }
            };

            window.setupCallStream = function(call) {
                console.log("🛡️ AUDITORÍA: Vinculando flujo WebRTC:", call.peer);
                call.on('error', function(err) { console.error("❌ ERROR WebRTC en llamada:", err); });

                var remoteAudio = document.createElement("audio");
                remoteAudio.setAttribute("autoplay", "true");
                remoteAudio.setAttribute("playsinline", "true");
                remoteAudio.muted = false; remoteAudio.volume = 0.01;
                remoteAudio.style.cssText = "position:fixed;width:1px;height:1px;top:0;left:0;opacity:0.001;pointer-events:none;z-index:-1";
                document.body.appendChild(remoteAudio);
                
                call.on('stream', function(remoteStream) {
                    console.log("🔊 VOZ RECIBIDA. Tracks:", remoteStream.getAudioTracks().length);
                    
                    if (remoteStream.getAudioTracks().length === 0) console.error("⚠️ ALERTA: Stream recibido pero tiene 0 pistas de audio.");
                    remoteAudio.srcObject = remoteStream;
                    remoteAudio.play().catch(function(e) { });

                    if (!window.app.ctx && window.initAudio) window.initAudio();
                    if (!window.app.ctx) return;
                    
                    var source = window.app.ctx.createMediaStreamSource(remoteStream);
                    var analyser = window.app.ctx.createAnalyser();
                    var gainNode = window.app.ctx.createGain();
                    analyser.fftSize = 256;

                    source.connect(analyser); 
                    source.connect(gainNode);
                    if (window.app.masterRxGain) gainNode.connect(window.app.masterRxGain);
                    // 🛡️ REPLAY RECORDING: Conectar audio remoto al bus de grabación para Replay
                    if (window.app.masterRecordBus) gainNode.connect(window.app.masterRecordBus);
                    
                    var dummy = window.app.ctx.createGain();
                    dummy.gain.value = 0.0001; 
                    source.connect(dummy); 
                    dummy.connect(window.app.ctx.destination); 

                    window.app.remoteSources[call.peer] = source;
                    window.app.remoteAnalysers[call.peer] = analyser;
                    window.app.remoteGains[call.peer] = gainNode;
                    window.app.remoteDummies = window.app.remoteDummies || {};
                    window.app.remoteDummies[call.peer] = dummy;
                    window.app.remoteSinks = window.app.remoteSinks || {};
                    window.app.remoteSinks[call.peer] = remoteAudio;
                    
                    if (window.app.ctx.state !== 'running') {
                        window.app.ctx.resume().then(() => { 
                            remoteAudio.volume = 0.2;
                            setTimeout(() => { remoteAudio.volume = 0.01; }, 1000);
                        });
                    }
                    if (window.app.masterOut) window.app.masterOut.connect(window.app.ctx.destination);
                });
                call.on('close', function() {
                    if (window.app.remoteSources[call.peer]) window.app.remoteSources[call.peer].disconnect();
                    if (window.app.remoteGains[call.peer]) window.app.remoteGains[call.peer].disconnect();
                    delete window.app.remoteAnalysers[call.peer];
                    delete window.app.activeCalls[call.peer];
                });
            };

            window.app.discreteBeepPlayed = {};

            function updateRemotePriorities() {
                if(!window.app || !window.app.ctx) return;
                
                if (window.app.ctx.state !== 'running') window.app.ctx.resume().catch(e=>{});

                var peers = Object.keys(window.app.remoteSources);
                var rxActive = window.app.rxActiveInternal || false;

                if (!window._lastDiag || Date.now() - window._lastDiag > 5000) {
                    console.log("📊 AUDIO-DIAG: " + window.app.ctx.state + " | Peers: " + peers.length + " | Out: " + (window.app.masterOut?window.app.masterOut.gain.value.toFixed(2):'0'));
                    window._lastDiag = Date.now();
                }

                if (window.app.noise) {
                    var now = window.app.ctx.currentTime;
                    if (rxActive) window.app.noise.gain.setTargetAtTime(0, now, 0.05);
                    else if (window.app.currentNoiseTarget > 0 && !window.app.pttStateInternal && !window.app.isBeeping) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget, now, 0.2);
                }

                if (window.app.masterRxGain && !window.app.pttStateInternal && !window.app.isBeeping) {
                    if (window.app.masterRxGain.gain.value < 1.0) {
                        window.app.masterRxGain.gain.setTargetAtTime(3.5, window.app.ctx.currentTime, 0.1);
                    }
                }

                if (window.app.masterOut && !window.app.pttStateInternal) {
                    try { window.app.masterOut.connect(window.app.ctx.destination); } catch(e) {}
                    if (window.app.masterOut.gain.value < 0.1) {
                        window.app.masterOut.gain.setTargetAtTime(window.app.currentMasterGain || 1.0, window.app.ctx.currentTime, 0.1);
                    }
                }

                peers.forEach(id => {
                    var gain = window.app.remoteGains[id];
                    var source = window.app.remoteSources[id];
                    var ana = window.app.remoteAnalysers[id];
                    if(!gain || !source || !ana) return;

                    // 🎙️ DETECCIÓN DUAL: Medir energía real de audio en el canal WebRTC
                    var audioVol = 0;
                    if (ana) {
                        var buf = new Uint8Array(ana.fftSize);
                        ana.getByteTimeDomainData(buf);
                        var maxDev = 0;
                        for (var i = 0; i < buf.length; i++) {
                            var dev = Math.abs(buf[i] - 128);
                            if (dev > maxDev) maxDev = dev;
                        }
                        audioVol = maxDev / 128.0;
                    }

                    var hasVoiceAudio = (audioVol > 0.06);
                    var firebaseTx = (window.remoteTxStates && window.remoteTxStates[id] === true);

                    // Si hay voz en la llamada WebRTC O Firebase marca tx, la portadora DEBE encenderse
                    var isTx = firebaseTx || hasVoiceAudio;
                    window.remoteTxStates[id] = isTx;

                    if (window.app.discreteMode) {
                        // 👂 MODO OREJA / DISCRETO: Muto voz por altavoz y aviso con un beep
                        gain.gain.setTargetAtTime(0, window.app.ctx.currentTime, 0.01);
                        if (isTx && !window.app.discreteBeepPlayed[id]) {
                            window.app.discreteBeepPlayed[id] = true;
                            if (window.playUiSound) window.playUiSound("incoming");
                        } else if (!isTx) {
                            window.app.discreteBeepPlayed[id] = false;
                        }
                    } else {
                        window.app.discreteBeepPlayed[id] = false;
                        gain.gain.setTargetAtTime(isTx ? 1.2 : 1.0, window.app.ctx.currentTime, 0.05);
                    }
                });
            }
            setInterval(updateRemotePriorities, 100);

            window.ensureMicAccess = function() {
                console.log("🎙️ Solicitando acceso al micrófono...");
                if (window.initAudio) window.initAudio();
                if (window.app.ctx && window.app.ctx.state !== 'running') {
                    window.app.ctx.resume().then(() => { 
                        console.log("🔊 AudioContext reanudado por usuario.");
                        if (typeof window.manosLibres_requestMic === 'function') window.manosLibres_requestMic();
                    });
                } else {
                    if (typeof window.manosLibres_requestMic === 'function') window.manosLibres_requestMic();
                }
            };
        """)
        ManosLibres.install()
        RadioSignaling.install()
        MoniGuard.install()
        ReplayEngine.install()
    }

    fun setPtt(active: Boolean, roger: Boolean, power: Float?) {
        val w = window.asDynamic()
        if (w.broadcastPTT != null) w.broadcastPTT(active, roger, power)
    }

    fun playReplay() { js("window.playReplay();") }
}

object RadioSignaling {
    fun install() {
        js("""
            window.app.canPlaySounds = false; 
            window.playUiSound = function(type) {
                if(!window.app.ctx || !window.app.canPlaySounds) return;
                var now = window.app.ctx.currentTime;
                if (type === "incoming") {
                    var pairs = [[697, 1209], [770, 1336], [852, 1477], [941, 1633]];
                    pairs.forEach((f, i) => { f.forEach(freq => { var o = window.app.ctx.createOscillator(); var g = window.app.ctx.createGain(); o.type = "sine"; o.frequency.setValueAtTime(freq, now + (i * 0.12)); g.gain.setValueAtTime(0, now + (i * 0.12)); g.gain.linearRampToValueAtTime(0.15, now + (i * 0.12) + 0.01); g.gain.linearRampToValueAtTime(0, now + (i * 0.12) + 0.1); o.connect(g); g.connect(window.app.masterOut); o.start(now + (i * 0.12)); o.stop(now + (i * 0.12) + 0.12); }); });
                    return;
                }
                var isRoger = (type === "ptt_off" || type === "rx_off");
                var freq = isRoger ? 1955 : 1800;
                var duration = isRoger ? 0.3 : 0.08;
                var o = window.app.ctx.createOscillator(); var g = window.app.ctx.createGain(); o.type = "triangle"; o.frequency.setValueAtTime(freq, now); g.gain.setValueAtTime(isRoger ? 0.12 : 0.08, now); g.gain.setValueAtTime(0, now + duration); o.connect(g); g.connect(window.app.masterOut); o.start(now); o.stop(now + duration);
            };
        """)
    }
}

object MoniGuard {
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

object ReplayEngine {
    fun install() {
        js("""
            var replayBlobs = [];
            var activityDetected = false;
            var isPlaying = false;
            window.initReplayRecorder = function() {
                if (!window.app.ctx || !window.app.masterRecordBus || !window.app.replayCompressor) { setTimeout(window.initReplayRecorder, 1000); return; }
                var streamDest = window.app.ctx.createMediaStreamDestination();
                window.app.replayCompressor.connect(streamDest);
                var replayAnalyser = window.app.ctx.createAnalyser();
                replayAnalyser.fftSize = 256;
                window.app.replayCompressor.connect(replayAnalyser);
                function monitor() { if (replayAnalyser) { var d = new Uint8Array(replayAnalyser.fftSize); replayAnalyser.getByteTimeDomainData(d); for(var i=0; i<d.length; i++) { if (Math.abs(d[i] - 128) > 3) { activityDetected = true; break; } } } requestAnimationFrame(monitor); }
                monitor();
                function startChunk() {
                    var chunks = []; var hasVoiceInThisChunk = false; activityDetected = false; 
                    try {
                        var mimeType = "audio/webm;codecs=opus";
                        if (!MediaRecorder.isTypeSupported(mimeType)) mimeType = "audio/webm";
                        var recorder = new MediaRecorder(streamDest.stream, mimeType ? { mimeType: mimeType } : {});
                        recorder.ondataavailable = function(e) { if (e.data && e.data.size > 0) chunks.push(e.data); };
                        recorder.onstop = function() { if (chunks.length > 0 && hasVoiceInThisChunk) { var blob = new Blob(chunks, { type: 'audio/webm' }); replayBlobs.push(blob); if (replayBlobs.length > 6) replayBlobs.shift(); if (window.dispatch_replay_available) window.dispatch_replay_available(replayBlobs.length > 0); } startChunk(); };
                        var checkInterval = setInterval(function() { if (activityDetected) hasVoiceInThisChunk = true; }, 100);
                        setTimeout(function() { clearInterval(checkInterval); if (recorder.state === "recording") recorder.stop(); }, 5000);
                        recorder.start();
                    } catch(e) { setTimeout(startChunk, 2000); }
                }
                startChunk();
            };
            window.playReplay = function() {
                if (replayBlobs.length === 0 || isPlaying) return;
                isPlaying = true; var playlist = [...replayBlobs]; replayBlobs = []; if (window.dispatch_replay_available) window.dispatch_replay_available(false);
                var index = 0;
                function finishReplay() { isPlaying = false; window.app.masterRxGain.gain.setTargetAtTime(3.5, window.app.ctx.currentTime, 0.3); }
                function playNext() {
                    if (index >= playlist.length) { finishReplay(); return; }
                    var url = URL.createObjectURL(playlist[index]); var audio = new Audio(url); var source = window.app.ctx.createMediaElementSource(audio); source.connect(window.app.masterOut); audio.playbackRate = 1.15;
                    audio.onended = function() { URL.revokeObjectURL(url); source.disconnect(); index++; playNext(); };
                    audio.play().catch(finishReplay);
                }
                playNext();
            };
            setTimeout(window.initReplayRecorder, 3000);
        """)
    }
}
