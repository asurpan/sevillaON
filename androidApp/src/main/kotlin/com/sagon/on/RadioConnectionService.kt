package com.sagon.on

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi

/**
 * 📞 RADIO CONNECTION SERVICE: INTEGRACIÓN DE LLAMADAS DE VOZ NATIVAS
 * Permite registrar llamadas e interacciones de voz de la radio con la pila de audio nativa de Android.
 */
@RequiresApi(Build.VERSION_CODES.O)
class RadioConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = RadioConnection()
        connection.setInitializing()
        connection.setAddress(request?.address, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        connection.setCallerDisplayName("ON AIR SPAIN", android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        connection.setConnectionCapabilities(Connection.CAPABILITY_HOLD or Connection.CAPABILITY_MUTE)
        connection.setActive()
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = RadioConnection()
        connection.setInitializing()
        connection.setAddress(request?.address, android.telecom.TelecomManager.PRESENTATION_ALLOWED)
        connection.setConnectionCapabilities(Connection.CAPABILITY_HOLD or Connection.CAPABILITY_MUTE)
        connection.setActive()
        return connection
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class RadioConnection : Connection() {
    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    override fun onAnswer() {
        setActive()
    }

    override fun onReject() {
        setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
        destroy()
    }
}
