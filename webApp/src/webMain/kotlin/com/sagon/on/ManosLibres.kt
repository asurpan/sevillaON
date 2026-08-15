package com.sagon.on

/**
 * 🔒 HARD-LOCK: MOTOR DE MANOS LIBRES (VOX) PROFESIONAL v10.0
 * ESTADO: SELLADO TOTAL - HIGH-SENSITIVITY & VOICE-PRIORITY
 * 
 * ⚠️ ESTE MÓDULO ES EL "ALMA" DEL MICRÓFONO:
 * - Filtro Paso-Alto (150Hz) para conservar toda la energía vocal.
 * - Pre-amplificación del sensor para detección de susurros.
 * - Umbral dinámico ultra-rápido.
 */
object ManosLibres {
    fun install() {
        js("""
            var counter = 0;
            var isTransmitting = false;
            var noiseFloor = 0.01; 

            window.manosLibres_requestMic = function() {
                if (window.app.rawStream) return Promise.resolve(true);
                if (window.initAudio) window.initAudio();
                
                var constraints = { 
                    audio: { echoCancellation: true, noiseSuppression: true, autoGainControl: true, channelCount: 1 } 
                };

                return navigator.mediaDevices.getUserMedia(constraints)
                .then(function(stream) {
                    window.app.rawStream = stream;
                    var micSrc = window.app.ctx.createMediaStreamSource(stream);
                    
                    // 🛡️ DSP: FILTRO DE VOZ (Corta solo el retumbe del motor, deja pasar la voz)
                    var voiceFilter = window.app.ctx.createBiquadFilter();
                    voiceFilter.type = "highpass";
                    voiceFilter.frequency.value = 150; 
                    
                    // Pre-amplificador para el sensor VOX
                    var sensorGain = window.app.ctx.createGain();
                    sensorGain.gain.value = 2.5; // Aumentar sensibilidad de detección
                    
                    var compressor = window.app.ctx.createDynamicsCompressor();
                    compressor.threshold.value = -12; compressor.ratio.value = 8;
                    
                    window.app.micAnalyser = window.app.ctx.createAnalyser();
                    window.app.micAnalyser.fftSize = 256;
                    
                    micSrc.connect(voiceFilter);
                    voiceFilter.connect(sensorGain);
                    sensorGain.connect(window.app.micAnalyser); // El sensor recibe señal potente
                    voiceFilter.connect(compressor);

                    if (window.app.moniGainNode) compressor.connect(window.app.moniGainNode);
                    if (window.app.txGate) compressor.connect(window.app.txGate);
                    return true;
                }).catch(function(err) { return false; });
            };

            function loop() {
                if(!window.app) {
                    requestAnimationFrame(loop);
                    return;
                }

                var peakLevel = 0;
                var hasValidData = false;
                
                if (window.app.micAnalyser) {
                    var d = new Uint8Array(window.app.micAnalyser.fftSize);
                    window.app.micAnalyser.getByteTimeDomainData(d);
                    var max = 0; var sum = 0;
                    for(var i=0; i<d.length; i++) { 
                        var v = Math.abs(d[i]-128); if(v > max) max = v; sum += d[i];
                    }
                    if (sum > 0) { peakLevel = max / 128; hasValidData = true; }
                }

                // --- 🤖 MOTOR VOX v10.0 ---
                var manualPtt = window.app.pttStateInternal && !isTransmitting;
                var rxActive = window.app.rxActiveInternal || false;
                
                // Seguimiento de ruido ambiente ultra-lento
                if (hasValidData && !isTransmitting && !rxActive) {
                    noiseFloor = (peakLevel * 0.02) + (noiseFloor * 0.98);
                }

                if (window.app.voxActive && !rxActive && !window.app.isBeeping && !manualPtt && hasValidData) {
                    
                    // 🎚️ CURVA DE SENSIBILIDAD PROFESIONAL
                    var sens = (window.app.voxSens !== undefined) ? window.app.voxSens : 0.5;
                    var baseThreshold = 0.55 * Math.pow(0.005, sens); 
                    
                    // Umbral inteligente: Solo sube si el ruido es constante
                    var finalThreshold = baseThreshold + (noiseFloor * 0.5);
                    
                    if (peakLevel > finalThreshold) {
                        counter = 18; // 300ms de mantenimiento
                        if (!isTransmitting) {
                            isTransmitting = true;
                            if(window.broadcastPTT) window.broadcastPTT(true, window.app.rogerEnabled);
                        }
                    } else if (isTransmitting) {
                        counter--;
                        if (counter <= 0) {
                            isTransmitting = false;
                            if(window.broadcastPTT) window.broadcastPTT(false, window.app.rogerEnabled);
                        }
                    }
                } else if (isTransmitting) {
                    isTransmitting = false;
                    if(window.broadcastPTT) window.broadcastPTT(false, window.app.rogerEnabled);
                }

                // --- 📟 LEDs Y QRM ---
                var finalLevel = 0;
                if (window.app.isTransmittingInternal || window.app.isBeeping) {
                    finalLevel = window.app.isBeeping ? 1.0 : 0.75 + Math.min(0.25, peakLevel * 0.8);
                } else {
                    if (rxActive) {
                        var maxRx = 0;
                        Object.values(window.app.remoteAnalysers).forEach(function(ana) {
                            var d = new Uint8Array(ana.fftSize);
                            ana.getByteTimeDomainData(d);
                            var m = 0; for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>m) m=v; }
                            if(m > maxRx) maxRx = m;
                        });
                        var rxMod = Math.min(0.85, (maxRx/128)*3.5);
                        if (rxMod > 0.05) finalLevel = 0.65 + rxMod;
                    }
                    if (finalLevel == 0) {
                        var noise = (window.app.currentNoiseTarget || 0);
                        if (noise >= 0.02) {
                            var lfo = Math.sin(Date.now() / 2000) * 0.02;
                            var jitter = (Math.random() * 0.04);
                            finalLevel = Math.max(0, Math.min(0.22, (noise * 0.6) + lfo + jitter));
                        }
                    }
                }
                if(window.dispatch_mic) window.dispatch_mic(finalLevel);
                requestAnimationFrame(loop);
            }
            loop();
        """)
    }
}
