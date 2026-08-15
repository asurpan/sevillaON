package com.sagon.on

/**
 * 🗺️ RADIO BRIDGE: PUENTE DE EVENTOS ENTRE NÚCLEO JS Y COMPOSE UI
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.0 (PURE RADIO)
 */
object RadioBridge {
    fun install() {
        // La lógica de install ha sido movida a main.kt para asegurar el orden de carga
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
        onIncomingAlert: (String, String, String) -> Unit,
        onVoxSync: (Boolean) -> Unit,
        onRoomUpdate: (String) -> Unit,
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
        win.dispatch_users_update = onUsersUpdate
        win.dispatch_chat_update = onChatUpdate
        win.dispatch_replay_progress = onReplayProgress
        win.dispatch_replay_available = onReplayAvailable
        win.dispatch_chat_open = onChatOpen
        win.dispatch_mic_failure = onMicFailure
        win.dispatch_integrity_status = onIntegrityStatus
        win.dispatch_incoming_alert = onIncomingAlert
        win.dispatch_vox_sync = onVoxSync
        win.dispatch_room_update = onRoomUpdate
        win.dispatch_ptt_live = onPttLive
    }
}
