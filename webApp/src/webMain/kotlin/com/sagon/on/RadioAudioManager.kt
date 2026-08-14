package com.sagon.on

import kotlinx.browser.window
import kotlinx.browser.localStorage

/**
 * 🎙️ RADIO AUDIO MANAGER: MOTOR DE SONIDO, WEBRTC Y PTT
 * ARQUITECTURA PROTEGIDA - SELLADO TOTAL
 */
object RadioAudioManager {
    fun install() {
        js("""
            window.app = window.app || {};
            
            window.initAudio = function() {
                if (window.app.ctx) {
                    if (window.app.ctx.state === 'suspended') window.app.ctx.resume();
                    return;
                }
                var AC = window.AudioContext || window.webkitAudioContext;
                try {
                    window.app.ctx = new AC({ latencyHint: 'interactive', sampleRate: 48000 });
                } catch(e) {
                    window.app.ctx = new AC();
                }
                
                // Grafo de Audio Base
                window.app.filter = window.app.ctx.createBiquadFilter();
                window.app.filter.type = "bandpass";
                window.app.filter.frequency.value = 1600; 
                window.app.filter.Q.value = 0.5;

                window.app.moniGain = window.app.ctx.createGain(); 
                window.app.moniGain.gain.value = 0;
                
                window.app.masterRxGain = window.app.ctx.createGain();
                window.app.masterRxGain.gain.value = 2.0;

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
                window.app.noise.connect(window.app.compressor);

                window.app.txBus = window.app.ctx.createMediaStreamDestination();
                window.app.txGate = window.app.ctx.createGain();
                window.app.txGate.gain.value = 0;
                window.app.txGate.connect(window.app.txBus);

                window.app.filter.connect(window.app.masterRxGain);
                window.app.masterRxGain.connect(window.app.compressor);
                window.app.moniGain.connect(window.app.compressor);
                window.app.noise.connect(window.app.compressor);
                
                window.app.masterOut = window.app.ctx.createGain();
                window.app.compressor.connect(window.app.masterOut);
                window.app.masterOut.connect(window.app.ctx.destination);
                
                window.app.replayDest = window.app.ctx.createMediaStreamDestination();
                window.app.compressor.connect(window.app.replayDest);
            };

            window.broadcastPTT = function(active, roger, power) {
                if(!window.app || !window.app.db) return;
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

            window.setupCallStream = function(call) {
                call.on('stream', function(remoteStream) {
                    if (window.initAudio) window.initAudio();
                    var audioTag = document.createElement('audio');
                    audioTag.srcObject = remoteStream;
                    audioTag.volume = 0.5;
                    audioTag.play().catch(function(e){});
                    document.body.appendChild(audioTag);
                });
            };

            window.voiceWatchdog = {
                check: function() {
                    if (window.app && window.app.ctx && window.app.ctx.state === 'suspended') window.app.ctx.resume();
                }
            };
            setInterval(function() { if(window.voiceWatchdog) window.voiceWatchdog.check(); }, 3000);
        """)
        
        RadioSignaling.install()
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
                    var level = Math.min(1.0, (max/128)*6.5);
                    if(window.dispatch_mic) window.dispatch_mic(level);
                    
                    if(window.app.voxActive && level > (1.0-(window.app.voxSens*0.99)) && !window.app.isTransmittingInternal) {
                        if(!window.app.isVoxTransmitting) {
                            window.broadcastPTT(true, true);
                            window.app.isVoxTransmitting=true;
                            if(window.dispatch_vox_sync) window.dispatch_vox_sync(true);
                        }
                        window.app.voxHangTimer=60;
                    } else if(window.app.isVoxTransmitting) {
                        if(window.app.voxHangTimer>0) window.app.voxHangTimer--;
                        else {
                            window.app.isVoxTransmitting=false;
                            window.broadcastPTT(false, true);
                            if(window.dispatch_vox_sync) window.dispatch_vox_sync(false);
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
            window.updateMasterVolume = function() {
                if(window.app && window.app.masterOut) {
                    var gain = (window.app.rfGain || 0.5) * 4.0;
                    window.app.masterOut.gain.setTargetAtTime(gain, window.app.ctx.currentTime, 0.1);
                }
            };
        """)
    }
}

private object ReplayEngine {
    fun install() {
        js("""
            window.playReplay = function() {
                if(window.app && window.app.replayChunks && window.app.replayChunks.length > 0) {
                    console.log("Iniciando Replay...");
                }
            };
        """)
    }
}
