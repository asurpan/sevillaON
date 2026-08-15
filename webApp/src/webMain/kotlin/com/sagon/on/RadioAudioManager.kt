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
                            var v = window.app.moniVolume || 0.5;
                            window.app.masterOut.gain.setTargetAtTime(v * 1.5, window.app.ctx.currentTime, 0.1);
                        }
                    };
                    
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

                if (active && !window.app.rawStream) {
                    window.requestMicPermission();
                }

                if (window.app.pttStateInternal === active) return;
                window.app.pttStateInternal = active;
                window.app.isTransmittingInternal = active;
                
                if(window.dispatch_ptt_live) window.dispatch_ptt_live(active);
                
                var now = window.app.ctx.currentTime;
                if (active) {
                    // 🔒 HARD-LOCK: SILENCIO ABSOLUTO DURANTE TRANSMISIÓN
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: true, pwr: power || 0.7 });
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(1.0, now, 0.01);
                    if (window.app.masterRxGain) window.app.masterRxGain.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.noise) window.app.noise.gain.setTargetAtTime(0, now, 0.01);
                    if (window.app.lfoGain) window.app.lfoGain.gain.setTargetAtTime(0, now, 0.01);
                } else {
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, now, 0.01);
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
            window.playReplay = function() {
                console.log("Replay...");
            };
        """)
    }
}
