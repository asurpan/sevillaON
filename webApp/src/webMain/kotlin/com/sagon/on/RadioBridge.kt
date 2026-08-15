package com.sagon.on

/**
 * 🗺️ RADIO BRIDGE: PUENTE DE EVENTOS ENTRE NÚCLEO JS Y COMPOSE UI
 */
object RadioBridge {
    fun install() {
        // La lógica de install ha sido movida a main.kt para asegurar el orden de carga y evitar Script Errors
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
        onRoomUpdate: (String) -> Unit,
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
            val app = win.app
            if (app != null) {
                app.forceChatOpen = true
                app.forceChatTarget = target
            }
        }
        win.dispatch_mic_failure = onMicFailure
        win.dispatch_integrity_status = onIntegrityStatus
        win.dispatch_bg_station = onBgStation
        win.dispatch_bg_genre_change = onBgGenreChange
        win.dispatch_vox_sync = onVoxSync
        win.dispatch_room_update = onRoomUpdate
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
        
        if (win.setupSystemListeners != null) win.setupSystemListeners();
    }
}
