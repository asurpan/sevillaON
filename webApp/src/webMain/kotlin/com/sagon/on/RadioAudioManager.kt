package com.sagon.on

import kotlinx.browser.window
import kotlinx.browser.localStorage

/**
 * 🎙️ RADIO AUDIO MANAGER: MOTOR DE SONIDO PROFESIONAL
 * REPARACIÓN CRÍTICA: BARRAS DE LEDS Y MICRÓFONO
 */
object RadioAudioManager {
    fun install() {
        js("""
            window.app = window.app || { activeCalls: {}, remoteSources: {}, remoteAnalysers: {} };
            
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
                    
                    // --- 🌊 GENERADOR DE RUIDO MARRÓN (QRM REALISTA) ---
                    var bufferSize = 2 * window.app.ctx.sampleRate,
                        noiseBuffer = window.app.ctx.createBuffer(1, bufferSize, window.app.ctx.sampleRate),
                        output = noiseBuffer.getChannelData(0);
                    var lastOut = 0.0;
                    for (var i = 0; i < bufferSize; i++) {
                        var white = Math.random() * 2 - 1;
                        output[i] = (lastOut + (0.02 * white)) / 1.02;
                        lastOut = output[i];
                        output[i] *= 3.5; // Compensación de volumen
                    }
                    
                    var noiseSource = window.app.ctx.createBufferSource();
                    noiseSource.buffer = noiseBuffer;
                    noiseSource.loop = true;
                    
                    window.app.noise = window.app.ctx.createGain();
                    window.app.noise.gain.value = 0; 
                    window.app.currentNoiseTarget = 0;
                    
                    noiseSource.connect(window.app.noise);
                    // 📻 Conectar al filtro para que tenga textura de radio
                    window.app.noise.connect(window.app.filter); 
                    noiseSource.start();

                    window.setNoiseVolume = function(v) {
                        if (!window.app.noise || window.app.isTransmittingInternal) return;
                        window.app.currentNoiseTarget = v * 0.25; 
                        window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget, window.app.ctx.currentTime, 0.1);
                    };

                    // --- 🔊 CONTROL DE VOLUMEN MAESTRO (INDEPENDIENTE DE SEÑAL) ---
                    window.updateMasterVolume = function() {
                        if (window.app && window.app.masterOut) {
                            var v = window.app.moniVolume || 0.5;
                            // El volumen maestro solo afecta a la salida final, no a los analizadores
                            window.app.masterOut.gain.setTargetAtTime(v * 1.5, window.app.ctx.currentTime, 0.1);
                        }
                    };
                    
                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);
                    
                    console.log("🏗️ [AUDIO] Motor Radioaficionado (Squelch Enabled) listo.");
                } catch(e) { console.error("Error Audio:", e); }
            };

            window.requestMicPermission = function() {
                if (window.app.rawStream) return Promise.resolve(true);
                window.initAudio();
                
                return navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: true, noiseSuppression: true } })
                .then(function(stream) {
                    window.app.rawStream = stream;
                    var micSrc = window.app.ctx.createMediaStreamSource(stream);
                    
                    var txFilter = window.app.ctx.createBiquadFilter();
                    txFilter.type = "highpass"; txFilter.frequency.value = 120;
                    
                    var txCompressor = window.app.ctx.createDynamicsCompressor();
                    txCompressor.threshold.value = -18; txCompressor.ratio.value = 12;
                    
                    micSrc.connect(txFilter);
                    txFilter.connect(txCompressor);
                    
                    // --- 🎙️ ANALIZADOR PARA LOS LEDS ---
                    window.app.micAnalyser = window.app.ctx.createAnalyser();
                    window.app.micAnalyser.fftSize = 256;
                    txCompressor.connect(window.app.micAnalyser);
                    
                    txCompressor.connect(window.app.txGate);
                    console.log("🎙️ [MICRO] Activo y vinculado a LEDs.");
                    return true;
                }).catch(function(err) { 
                    console.error("Fallo de micro:", err); 
                    return false;
                });
            };

            window.broadcastPTT = function(active, roger, power) {
                if(!window.app || !window.app.db) return;
                
                if (active && !window.app.rawStream) {
                    window.requestMicPermission();
                }

                if (window.app.pttStateInternal === active) return;
                window.app.pttStateInternal = active;
                window.app.isTransmittingInternal = active;
                
                if(window.dispatch_ptt_live) window.dispatch_ptt_live(active);
                
                var now = window.app.ctx.currentTime;
                if (active) {
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(2.0, now, 0.2);
                    // Restaurar el ruido al nivel que dicte el Squelch
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(window.app.currentNoiseTarget || 0, now, 0.2);
                    
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                    if (roger && window.playUiSound) window.playUiSound("ptt_off");
                }
            };
        """)
        
        RadioSignaling.install()
        js("""
            window.setupCallStream = function(call) {
                call.on('stream', function(remoteStream) {
                    if (!window.app.ctx) return;
                    var source = window.app.ctx.createMediaStreamSource(remoteStream);
                    var analyser = window.app.ctx.createAnalyser();
                    analyser.fftSize = 256;
                    
                    source.connect(analyser);
                    source.connect(window.app.filter);
                    
                    window.app.remoteSources[call.peer] = source;
                    window.app.remoteAnalysers[call.peer] = analyser;
                    window.app.rxActiveInternal = true;
                    console.log("🔊 [AUDIO] Stream conectado: " + call.peer);
                });
                call.on('close', function() {
                    if (window.app.remoteSources[call.peer]) {
                        window.app.remoteSources[call.peer].disconnect();
                        delete window.app.remoteSources[call.peer];
                    }
                    delete window.app.remoteAnalysers[call.peer];
                    delete window.app.activeCalls[call.peer];
                    window.app.rxActiveInternal = Object.keys(window.app.remoteSources).length > 0;
                });
            };
        """)
        VOXEngine.install()
        MoniGuard.install()
        ReplayEngine.install()
    }

    fun setPtt(active: Boolean, roger: Boolean, power: Float?) {
        js("window.broadcastPTT(active, roger, power);")
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
                if(window.app.ctx.state === 'suspended') window.app.ctx.resume();
                
                var now = window.app.ctx.currentTime;
                // 📻 ROGER BEEP PERFECTO: 1955Hz - 0.3s
                var freq = 1955;
                var duration = 0.3;

                if (type === "ptt_off") {
                    window.app.isBeeping = true;
                    if(window.dispatch_beeping) window.dispatch_beeping(true);
                }

                var o = window.app.ctx.createOscillator();
                var g = window.app.ctx.createGain();
                
                o.type = "triangle"; 
                o.frequency.setValueAtTime(freq, now);
                
                // Envolvente plana para ataque profesional
                g.gain.setValueAtTime(0.12, now); 
                g.gain.setValueAtTime(0.12, now + duration - 0.02);
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

private object VOXEngine {
    fun install() {
        js("""
            function voxLoop() {
                if(window.app) {
                    var finalLevel = 0;
                    
                    if (window.app.isTransmittingInternal || window.app.isBeeping) {
                        // --- 🎙️ MODO TX: LEDs DE POTENCIA ---
                        if (window.app.isBeeping) {
                            finalLevel = 1.0;
                        } else if (window.app.micAnalyser) {
                            var d = new Uint8Array(window.app.micAnalyser.fftSize);
                            window.app.micAnalyser.getByteTimeDomainData(d);
                            var max = 0; for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>max) max=v; }
                            var mod = Math.min(0.25, (max/128)*2.5);
                            finalLevel = 0.75 + mod;
                        }
                    } else {
                        // --- 📡 MODO RX / STANDBY: LEDs DE SEÑAL (S-METER) ---
                        var rxModulation = 0;
                        if (window.app.rxActiveInternal) {
                            var maxRx = 0;
                            Object.values(window.app.remoteAnalysers).forEach(function(ana) {
                                var d = new Uint8Array(ana.fftSize);
                                ana.getByteTimeDomainData(d);
                                var m = 0; for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>m) m=v; }
                                if(m > maxRx) maxRx = m;
                            });
                            // La voz remota dispara el S-Meter hacia S9 (0.7+)
                            rxModulation = Math.min(0.85, (maxRx/128)*3.5);
                            if (rxModulation > 0.05) finalLevel = 0.65 + rxModulation;
                        }
                        
                        // Si no hay voz fuerte, mostramos el QRM (Squelch abierto)
                        if (finalLevel < 0.2) {
                            // 🌊 QRM METER: Solo 1-2 LEDs (0.10 - 0.18) si el squelch está abierto
                            var noiseBase = (window.app.currentNoiseTarget || 0) * 0.8;
                            var jitter = (Math.random() * 0.05); 
                            finalLevel = Math.max(finalLevel, Math.min(0.18, noiseBase + jitter));
                        }
                    }

                    if(window.dispatch_mic) {
                        window.dispatch_mic(finalLevel);
                    }
                }
                requestAnimationFrame(voxLoop);
            }
            voxLoop();
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
            window.playReplay = function() {
                console.log("Replay...");
            };
        """)
    }
}
