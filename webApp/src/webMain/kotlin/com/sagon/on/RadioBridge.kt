package com.sagon.on

/**
 * 🗺️ RADIO BRIDGE: PUENTE DE EVENTOS ENTRE NÚCLEO JS Y COMPOSE UI
 */
object RadioBridge {
    fun install() {
        js("""
            window.setupSystemListeners = function() {
                window.addEventListener('popstate', function(event) {
                    history.pushState(null, document.title, location.href);
                    if(window.trigger_back) window.trigger_back();
                });
                history.pushState(null, document.title, location.href);
            };

            window.checkNickAvailability = function(nick, city) {
                if (!window.app || !window.app.db) return Promise.resolve(true);
                var safeNick = nick.replace(/[^a-zA-Z0-9]/g, "").toUpperCase();
                return window.app.db.ref("users").once('value').then(function(snapshot) {
                    var users = snapshot.val();
                    if (!users) return true;
                    var keys = Object.keys(users);
                    for (var i = 0; i < keys.length; i++) {
                        var u = users[keys[i]];
                        if (u && u.nick === safeNick && u.city === city) return false; 
                    }
                    return true;
                });
            };

            window.shareSocial = function(text, platform) {
                if (window.AndroidApp && typeof window.AndroidApp.shareText === 'function') {
                    window.AndroidApp.shareText(text);
                    return;
                }
                if (navigator.share && platform !== 'WhatsApp') {
                    navigator.share({ title: 'ON AIR SPAIN', text: text, url: 'https://asurpan.github.io/sevillaON/' });
                } else {
                    var url = "https://wa.me/?text=" + encodeURIComponent(text);
                    window.open(url, "_blank");
                }
            };

            window.getGpsLink = function() {
                return new Promise(function(resolve) {
                    if (!navigator.geolocation) { resolve(null); return; }
                    navigator.geolocation.getCurrentPosition(function(pos) {
                        resolve("https://www.google.com/maps?q=" + pos.coords.latitude + "," + pos.coords.longitude);
                    }, function() { resolve(null); }, { timeout: 5000 });
                });
            };

            window.detectCityByGps = function() {
                return new Promise(function(resolve) {
                    if (!navigator.geolocation) { resolve("SEVILLA"); return; }
                    navigator.geolocation.getCurrentPosition(function(pos) {
                        fetch("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + pos.coords.latitude + "&lon=" + pos.coords.longitude)
                            .then(function(r) { return r.json(); })
                            .then(function(data) {
                                resolve(data.address.city || data.address.town || "SEVILLA");
                            }).catch(function() { resolve("SEVILLA"); });
                    }, function() { resolve("SEVILLA"); }, { timeout: 5000 });
                });
            };
        """)
    }

    fun setupDispatchers(
        win: dynamic,
        onMic: (Float) -> Unit,
        onBeep: (Boolean) -> Unit,
        onPttSync: (Boolean) -> Unit,
        onPttBlocked: () -> Unit,
        onReplayEmpty: () -> Unit,
        onReplayStart: () -> Unit,
        onBack: () -> Unit,
        onNickConflict: (String) -> Unit,
        onUsersUpdate: (dynamic) -> Unit,
        onChatUpdate: (dynamic) -> Unit,
        onReplayProgress: (Float) -> Unit,
        onReplayAvailable: (Boolean) -> Unit,
        onChatOpen: (String?) -> Unit,
        onMicFailure: () -> Unit,
        onIntegrityStatus: (Boolean) -> Unit,
        onBgStation: (String?) -> Unit,
        onBgGenreChange: (String) -> Unit,
        onIncomingAlert: (String, String, String) -> Unit,
        onVoxSync: (Boolean) -> Unit,
        onNasaImage: (String?, String?, String?) -> Unit,
        onDgtUpdate: (String?, String?) -> Unit,
        onCodeCaptured: (String, String) -> Unit,
        onWifiListReceived: (String) -> Unit,
        onEngineeringFinished: () -> Unit,
        onRouteSuggestions: (String) -> Unit,
        onPoiResults: (String) -> Unit,
        onRouteInfo: (String?, String?, String?) -> Unit,
        onNavigationStep: (String?) -> Unit,
        onPttLive: (Boolean) -> Unit
    ) {
        win.dispatch_mic = onMic
        win.dispatch_beeping = onBeep
        win.dispatch_ptt_sync = onPttSync
        win.dispatch_ptt_blocked = onPttBlocked
        win.dispatch_replay_empty = onReplayEmpty
        win.dispatch_replay_start = onReplayStart
        win.trigger_back = onBack
        win.dispatch_nick_conflict = onNickConflict
        win.update_remote_users = onUsersUpdate
        win.dispatch_chat_update = onChatUpdate
        win.dispatch_replay_progress = onReplayProgress
        win.dispatch_replay_available = onReplayAvailable
        win.dispatch_chat_open = { target: String? -> 
            onChatOpen(target)
            js("if(window.app) { window.app.forceChatOpen = true; window.app.forceChatTarget = target; }")
        }
        win.dispatch_mic_failure = onMicFailure
        win.dispatch_integrity_status = onIntegrityStatus
        win.dispatch_bg_station = onBgStation
        win.dispatch_bg_genre_change = onBgGenreChange
        win.dispatch_vox_sync = onVoxSync
        win.dispatch_nasa_image = onNasaImage
        win.dispatch_dgt_update = onDgtUpdate
        win.dispatch_code_captured = onCodeCaptured
        win.dispatch_wifi_list = onWifiListReceived
        win.dispatch_engineering_finished = onEngineeringFinished
        win.dispatch_route_suggestions = onRouteSuggestions
        win.dispatch_poi_results = onPoiResults
        win.dispatch_ptt_live = onPttLive
        win.dispatch_route_info = { dist: String?, dur: String?, dest: String? -> onRouteInfo(dist, dur, dest) }
        win.dispatch_navigation_step = { step: String? -> onNavigationStep(step) }
        win.dispatch_incoming_alert = onIncomingAlert
        
        js("window.setupSystemListeners();")
    }
}
