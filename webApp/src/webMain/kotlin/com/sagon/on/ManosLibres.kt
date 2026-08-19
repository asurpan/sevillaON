package com.sagon.on

/**
 * 🔒 HARD-LOCK: MOTOR DE MANOS LIBRES (VOX) PROFESIONAL v11.0
 * ESTADO: SELLADO TOTAL - CARRIER STRENGTH & MULTI-USER OVERRIDE
 * 
 * ⚠️ ESTE MÓDULO GESTIONA LA MODULACIÓN VISUAL (LEDs):
 * - Representa la Portadora (Carrier) según la potencia del emisor.
 * - La voz oscila únicamente en los segmentos rojos finales.
 * - Soporta el efecto de "pisarse" entre varios usuarios.
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
                    audio: { 
                        echoCancellation: true, 
                        noiseSuppression: false, 
                        autoGainControl: false, 
                        channelCount: 1 
                    } 
                };

                return navigator.mediaDevices.getUserMedia(constraints)
                .then(function(stream) {
                    console.log("🛡️ AUDITORÍA: Micrófono concedido. Tracks:", stream.getAudioTracks().length);
                    if (stream.getAudioTracks().length > 0) {
                        var track = stream.getAudioTracks()[0];
                        console.log("🛡️ AUDITORÍA: Track Local:", track.label, "Habilitado:", track.enabled);
                        window.app.diag.micPermission = "granted";
                    }

                    window.app.rawStream = stream;
                    if (window.app.ctx.state !== 'running') window.app.ctx.resume();
                    var micSrc = window.app.ctx.createMediaStreamSource(stream);
                    var voiceFilter = window.app.ctx.createBiquadFilter();
                    voiceFilter.type = "highpass"; voiceFilter.frequency.value = 150; 
                    var sensorGain = window.app.ctx.createGain();
                    sensorGain.gain.value = 2.5; 
                    var compressor = window.app.ctx.createDynamicsCompressor();
                    compressor.threshold.value = -12; compressor.ratio.value = 8;
                    window.app.micAnalyser = window.app.ctx.createAnalyser();
                    window.app.micAnalyser.fftSize = 256;
                    micSrc.connect(voiceFilter);
                    voiceFilter.connect(sensorGain);
                    sensorGain.connect(window.app.micAnalyser);
                    voiceFilter.connect(compressor);
                    
                    // 🚀 VOICE BOOST FINAL: Multiplicador de potencia antes de enviar a la red
                    var voiceBoost = window.app.ctx.createGain();
                    voiceBoost.gain.value = 1.8; 
                    compressor.connect(voiceBoost);

                    if (window.app.moniGainNode) voiceBoost.connect(window.app.moniGainNode);
                    if (window.app.txGate) voiceBoost.connect(window.app.txGate);
                    
                    // 🛡️ REPLAY RECORDING (SILENT BUS): Conectar al bus maestro de grabación (no al de audición)
                    if (window.app.masterRecordBus) voiceBoost.connect(window.app.masterRecordBus);
                    
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

                var manualPtt = window.app.pttStateInternal && !isTransmitting;
                var rxActive = window.app.rxActiveInternal || false;
                
                if (hasValidData && !isTransmitting && !rxActive) {
                    noiseFloor = (peakLevel * 0.02) + (noiseFloor * 0.98);
                }

                if (window.app.voxActive && !rxActive && !window.app.isBeeping && !manualPtt && hasValidData) {
                    var sens = (window.app.voxSens !== undefined) ? window.app.voxSens : 0.5;
                    var baseThreshold = 0.55 * Math.pow(0.005, sens); 
                    var finalThreshold = baseThreshold + (noiseFloor * 0.5);
                    
                    if (peakLevel > finalThreshold) {
                        counter = 18; 
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

                // --- 📟 LÓGICA DE LEDs PROFESIONAL (CARRIER + MODULACIÓN) ---
                var finalLevel = 0;
                var isTransInternal = window.app.isTransmittingInternal || false;
                if (isTransInternal || window.app.isBeeping) {
                    finalLevel = window.app.isBeeping ? 1.0 : 0.75 + Math.min(0.25, peakLevel * 0.8);
                } else {
                    if (rxActive) {
                        var maxRxMod = 0;
                        var maxPwr = 0;
                        
                        // Encontrar la señal más fuerte
                        Object.keys(window.app.remoteAnalysers).forEach(function(id) {
                            var ana = window.app.remoteAnalysers[id];
                            var pwr = window.app.remotePowers[id] || 0.7;
                            
                            var d = new Uint8Array(ana.fftSize);
                            ana.getByteTimeDomainData(d);
                            var m = 0; for(var i=0; i<d.length; i++) { var v = Math.abs(d[i]-128); if(v>m) m=v; }
                            var mod = (m / 128);
                            
                            if (pwr > maxPwr) {
                                maxPwr = pwr;
                                maxRxMod = mod;
                            }
                        });

                        // 📡 PORTADORA: Los LEDs marcan la potencia base (0.0 a 0.8 aprox)
                        var carrierLevel = Math.min(0.8, maxPwr); 
                        // 🎙️ VOZ: La oscilación se suma arriba, llegando a los ROJOS (0.8 a 1.0)
                        var voiceOscillation = Math.min(0.2, maxRxMod * 0.5);
                        
                        finalLevel = carrierLevel + voiceOscillation;
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
