package com.sagon.on

/**
 * 🔒 HARD-LOCK: MOTOR RTC Y GESTIÓN DE PEERJS v20.0
 * ESTADO: SELLADO TOTAL - SANITIZACIÓN Y CONEXIONES WEBRTC
 */
object RadioRtcEngine {
    fun install() {
        js("""
            window.RadioRtcEngine = {
                sanitizePeerId: function(rawNick) {
                    if (!rawNick) return "ANONIMO";
                    return rawNick.trim().replace(/[^A-Z0-9_-]/gi, "_").toUpperCase();
                },

                syncTxTracks: function() {
                    if (window.updatePeerSenders) {
                        try { window.updatePeerSenders(); } catch(e){}
                    }
                },

                getActiveTxStream: function() {
                    if (window.getStream) {
                        return window.getStream();
                    }
                    return null;
                }
            };
        """)
    }
}
