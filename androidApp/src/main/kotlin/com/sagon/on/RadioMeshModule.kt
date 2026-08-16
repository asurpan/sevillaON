package com.sagon.on

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.*
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

/**
 * 🔒 HARD-LOCK: MÓDULO DE SUPERVIVENCIA P2P (INDEPENDIENTE)
 * ESTADO: MODO EMERGENCIA ACTIVO
 * 
 * Permite la comunicación por voz móvil a móvil sin 4G, 5G ni Routers.
 * Utiliza WiFi Direct para el descubrimiento y UDP para el streaming.
 */
class RadioMeshModule(private val context: Context) {

    private val MESH_PORT = 50005
    private var isEmergencyModeActive = false
    private var isTransmitting = false
    
    private val manager: WifiP2pManager? by lazy { context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager }
    private val channel: WifiP2pManager.Channel? by lazy { manager?.initialize(context, context.mainLooper, null) }
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var receiverThread: Thread? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    // 📡 DESCUBRIMIENTO AUTOMÁTICO DE PARES
    @SuppressLint("MissingPermission")
    fun startEmergencyDiscovery() {
        if (isEmergencyModeActive) return
        isEmergencyModeActive = true
        Log.d("ON_AIR_MESH", "☢️ Iniciando Protocolo de Emergencia P2P...")

        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { Log.d("ON_AIR_MESH", "Escaneo de aire iniciado.") }
            override fun onFailure(reason: Int) { Log.e("ON_AIR_MESH", "Fallo escaneo: $reason") }
        })
        
        startListening()
    }

    // 🎙️ TRANSMISIÓN DE VOZ EN MALLA (SIN RED)
    @SuppressLint("MissingPermission")
    fun setEmergencyPtt(active: Boolean) {
        if (active == isTransmitting) return
        isTransmitting = active

        if (active) {
            thread {
                try {
                    val sampleRate = 8000
                    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                    audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                    
                    audioRecord?.startRecording()
                    val buffer = ByteArray(1024)
                    val socket = DatagramSocket()
                    socket.broadcast = true
                    // Dirección de broadcast de WiFi Direct suele ser 192.168.49.255
                    val address = InetAddress.getByName("192.168.49.255") 

                    while (isTransmitting) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            val packet = DatagramPacket(buffer, read, address, MESH_PORT)
                            socket.send(packet)
                        }
                    }
                    socket.close()
                } catch (e: Exception) {
                    Log.e("ON_AIR_MESH", "Error TX Mesh: ${e.message}")
                } finally {
                    audioRecord?.stop()
                    audioRecord?.release()
                    audioRecord = null
                }
            }
        }
    }

    // 🔊 RECEPTOR DE MALLA SIEMPRE ABIERTO (CALIBRADO PARA ANDROID 13+)
    private fun startListening() {
        if (receiverThread != null) return
        receiverThread = thread {
            try {
                val socket = DatagramSocket(MESH_PORT)
                val buffer = ByteArray(1024)
                
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                
                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(8000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(2048)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                audioTrack?.play()
                Log.d("ON_AIR_MESH", "🔊 Escucha P2P Activa (Puerto $MESH_PORT)")

                while (isEmergencyModeActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    audioTrack?.write(packet.data, 0, packet.length)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("ON_AIR_MESH", "Error RX Mesh: ${e.message}")
            } finally {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            }
        }
    }

    fun stopEmergencyMode() {
        isEmergencyModeActive = false
        isTransmitting = false
        receiverThread?.interrupt()
        receiverThread = null
        manager?.stopPeerDiscovery(channel, null)
    }
}
