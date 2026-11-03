package com.sagon.on

/**
 * 🔒 HARD-LOCK: MOTOR DE AUDIO Y CADENA DE MODULACIÓN WEBAUDIO v20.0
 * ESTADO: SELLADO TOTAL - CADENA INTOCABLE (voiceBoost ➔ txGate ➔ txBus)
 */
object RadioAudioEngine {
    fun install() {
        js("""
            window.RadioAudioEngine = {
                ensureAudioContext: function() {
                    if (window.app && window.app.ctx) {
                        if (window.app.ctx.state === 'suspended') {
                            window.app.ctx.resume().catch(function(e){});
                        }
                        if (window.app.voiceBoost && window.app.txGate) {
                            try { window.app.voiceBoost.connect(window.app.txGate); } catch(e){}
                        }
                        return window.app.ctx;
                    }
                    if (window.initAudio) window.initAudio();
                    return window.app ? window.app.ctx : null;
                },

                ensureMicCaptured: function() {
                    if (typeof window.manosLibres_requestMic === 'function') {
                        return window.manosLibres_requestMic();
                    }
                    return Promise.resolve(false);
                },

                connectVoiceChain: function() {
                    if (!window.app) return;
                    if (window.app.voiceBoost && window.app.txGate) {
                        try { window.app.voiceBoost.connect(window.app.txGate); } catch(e){}
                    }
                    if (window.app.voiceBoost && window.app.moniGainNode) {
                        try { window.app.voiceBoost.connect(window.app.moniGainNode); } catch(e){}
                    }
                    if (window.app.voiceBoost && window.app.masterRecordBus) {
                        try { window.app.voiceBoost.connect(window.app.masterRecordBus); } catch(e){}
                    }
                },

                setTxGateGain: function(gainValue, transitionMs) {
                    if (!window.app || !window.app.txGate || !window.app.ctx) return;
                    var now = window.app.ctx.currentTime;
                    var rampSec = (transitionMs || 30) / 1000.0;
                    try {
                        window.app.txGate.gain.setTargetAtTime(gainValue, now, rampSec);
                    } catch(e) {}
                }
            };
        """)
    }
}
