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
                    window.app.filter = window.app.ctx.createBiquadFilter();
                    window.app.filter.type = "bandpass"; window.app.filter.frequency.value = 1600; window.app.filter.Q.value = 0.5;
                    
                    window.app.filter.connect(window.app.masterRxGain);
                    window.app.masterRxGain.connect(window.app.compressor);
                    window.app.compressor.connect(window.app.masterOut);
                    
                    window.app.noise = window.app.ctx.createGain();
                    window.app.noise.connect(window.app.compressor);
                    
                    window.app.txBus = window.app.ctx.createMediaStreamDestination();
                    window.app.txGate = window.app.ctx.createGain();
                    window.app.txGate.gain.value = 0;
                    window.app.txGate.connect(window.app.txBus);
                    
                    console.log("🏗️ [AUDIO] Motor listo.");
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
                
                // Si pulsamos y no hay micro, lo pedimos volando
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
                } else {
                    if (window.app.txGate) window.app.txGate.gain.setTargetAtTime(0, now, 0.01);
                    window.app.db.ref("users/" + window.app.sessionID).update({ tx: false });
                    if (roger && window.playUiSound) window.playUiSound("ptt_off");
                }
            };
        """)
        
        RadioSignaling.install()
        VOXEngine.install()
        MoniGuard.install()
        ReplayEngine.install()
    }

    fun setPtt(active: Boolean, roger: Boolean, power: Float?) {
        js("window.broadcastPTT(active, roger, power);")
    }
}

private object RadioSignaling {
    fun install() {
        js("""
            window.playUiSound = function(type) {
                if(!window.app.ctx) return;
                var now = window.app.ctx.currentTime;
                var o = window.app.ctx.createOscillator();
                var g = window.app.ctx.createGain();
                o.type = "sine";
                o.frequency.setValueAtTime(type === "ptt_off" ? 440 : 880, now);
                g.gain.setValueAtTime(0.04, now);
                g.gain.exponentialRampToValueAtTime(0.0001, now + 0.1);
                o.connect(g); g.connect(window.app.masterOut);
                o.start(); o.stop(now + 0.1);
            };
        """)
    }
}

private object VOXEngine {
    fun install() {
        js("""
            function voxLoop() {
                if(window.app && window.app.micAnalyser) {
                    var d = new Uint8Array(window.app.micAnalyser.fftSize);
                    window.app.micAnalyser.getByteTimeDomainData(d);
                    var max = 0;
                    for(var i=0; i<d.length; i++) {
                        var v = Math.abs(d[i]-128);
                        if(v>max) max=v;
                    }
                    
                    // Modulación de voz pura
                    var modulation = Math.min(0.25, (max/128)*2.5);
                    
                    if(window.dispatch_mic) {
                        if (window.app.isBeeping) {
                            // Roger Beep: LEDs al Máximo
                            window.dispatch_mic(1.0);
                        } else if (window.app.isTransmittingInternal) {
                            // 🚀 PORTADORA TX: Base 0.75 + Voz
                            window.dispatch_mic(0.75 + modulation);
                        } else if (window.app.rxActiveInternal) {
                            // 📡 PORTADORA RX: Base 0.70 + Voz (simulada o real)
                            window.dispatch_mic(0.70 + modulation);
                        } else {
                            // Silencio absoluto
                            window.dispatch_mic(0);
                        }
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
