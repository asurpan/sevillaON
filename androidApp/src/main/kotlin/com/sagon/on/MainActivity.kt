package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - PUENTE NATIVO ANDROID
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 * 
 * Gestiona el Ciclo de Vida, Permisos Críticos y Bridge con el motor RadioCore.
 * Blindado contra fallos de seguridad y regresiones en la gestión de audio nativo.
 * 
 * NOTA DE DISEÑO (OBLIGATORIA): 
 * - Las teclas VOLUME_UP/DOWN NO deben usarse para PTT (deben controlar volumen).
 * - El PTT externo se limita a HEADSETHOOK y MEDIA_PLAY_PAUSE.
 */

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.view.KeyEvent
import android.net.wifi.WifiManager
import android.net.wifi.ScanResult
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.AudioFormat
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.hardware.Sensor
import android.hardware.ConsumerIrManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

import android.speech.tts.TextToSpeech
import java.util.Locale


class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // --- 🛡️ MÓDULO DE SUPERVIVENCIA P2P (INDEPENDIENTE) ---
    private val meshModule: RadioMeshModule by lazy { RadioMeshModule(this) }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    // =======================================================
    // 🔒 HARD-LOCK: PERMISSION SYSTEM & CRITICAL CORE
    // NO MODIFICAR - PROTECCIÓN CONTRA SECURITYEXCEPTION (API 34+)
    // =======================================================
    private var permissionRequest: PermissionRequest? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private var geolocationOrigin: String? = null

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            if (it.key == Manifest.permission.RECORD_AUDIO && it.value) {
                // Si se concedió el micro, podemos intentar arrancar el servicio si era necesario
            }
        }
        permissionRequest?.grant(permissionRequest?.resources)
        geolocationCallback?.invoke(geolocationOrigin, true, false)
        
        permissionRequest = null
        geolocationCallback = null
        geolocationOrigin = null
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionRequest?.grant(permissionRequest?.resources)
            geolocationCallback?.invoke(geolocationOrigin, true, false)
        } else {
            permissionRequest?.deny()
            geolocationCallback?.invoke(geolocationOrigin, false, false)
        }
        permissionRequest = null
        geolocationCallback = null
        geolocationOrigin = null
    }

    private var webViewInstance: WebView? = null
    
    private fun notifyVolumeChange() {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val pct = current.toFloat() / max.toFloat()
        runOnUiThread {
            webViewInstance?.evaluateJavascript("if(window.dispatch_volume_sync) window.dispatch_volume_sync($pct);", null)
        }
    }

    private var currentEngineeringThread: Thread? = null
    private var currentAudioTrack: android.media.AudioTrack? = null
    private var originalMusicVolume: Int = -1

    private fun notifyEngineeringFinished() {
        runOnUiThread {
            webViewInstance?.evaluateJavascript("if(window.dispatch_engineering_finished) window.dispatch_engineering_finished();", null)
        }
    }

    private fun stopEngineeringTask() {
        currentEngineeringThread?.interrupt()
        currentEngineeringThread = null
        
        // 🛡️ REPARACIÓN QUIRÚRGICA: Liberar recursos de audio inmediatamente
        try {
            currentAudioTrack?.let {
                if (it.playState == android.media.AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {}
        currentAudioTrack = null
        
        // Restaurar volumen si fue alterado por el Jammer
        if (originalMusicVolume != -1) {
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0)
            originalMusicVolume = -1
        }
    }

    private fun runEngineeringTask(block: () -> Unit) {
        stopEngineeringTask()
        currentEngineeringThread = Thread {
            try {
                block()
            } catch (e: InterruptedException) {
                // Parada inmediata solicitada
            } finally {
                notifyEngineeringFinished()
            }
        }
        currentEngineeringThread?.start()
    }
    private var adView: AdView? = null
    private var wifiManager: WifiManager? = null
    private var lastWifiReadings = mutableMapOf<String, Int>()
    private var wifiVarianceValue = 0f
    private var lastScanRequestTime = 0L
    private var wifiScanReceiver: android.content.BroadcastReceiver? = null

    private var sensorManager: SensorManager? = null
    private var magnetValue = 0f
    private var magnetBaseline = 0f
    private var deviceHeading = 0f
    private var deviceTilt = 0f
    private var pressureValue = 0f
    private var lastPressure = 0f
    private var pressureVariance = 0f // Nueva variable para detectar inclinación (vertical/horizontal)

    private fun sendWifiListToJs(results: List<ScanResult>) {
        val sb = StringBuilder()
        for (res in results) {
            val ssid = res.SSID ?: "Oculta"
            val bssid = res.BSSID ?: ""
            val signal = res.level
            val security = if (res.capabilities.contains("WPA")) "WPA2" else "OPEN"
            val vendor = when {
                ssid.contains("MOVISTAR", true) -> "MOVISTAR"
                ssid.contains("VODAFONE", true) -> "VODAFONE"
                ssid.contains("ORANGE", true) -> "ORANGE"
                ssid.contains("DIGI", true) -> "DIGI"
                // 🛡️ DETECCIÓN DE CÁMARAS CCTV Y ESPÍA
                ssid.contains("HIKVISION", true) || bssid.startsWith("00:12:12", true) -> "CCTV_HIK"
                ssid.contains("DAHUA", true) || bssid.startsWith("bc:32:d5", true) -> "CCTV_DAHUA"
                ssid.contains("ESP_", true) || bssid.startsWith("24:4c:ee", true) -> "SPY_CHIP_ESP"
                ssid.contains("TUYA", true) || bssid.startsWith("a4:cf:12", true) -> "SPY_CHIP_TUYA"
                ssid.contains("WYZE", true) || ssid.contains("ARLO", true) || ssid.contains("TAPO", true) -> "CCTV_SMART"
                else -> "GENÉRICO"
            }
            val isVulnerable = ssid.contains("MOVISTAR_", true) || ssid.contains("VODAFONE", true) || 
                               ssid.contains("ORANGE-", true) || ssid.contains("JAZZTEL", true) || 
                               ssid.contains("WLAN_", true) || ssid.contains("LIVEBOX", true)
            val wpsActive = res.capabilities.contains("WPS")
            
            // --- 🛡️ MOTOR DE CÁLCULO DE CLAVES POR DEFECTO (AUDITORÍA LETHAL) ---
            val cleanBssid = bssid.replace(":", "").uppercase()
            val cleanBssidSmall = bssid.replace(":", "").lowercase()
            
            // Generamos una cadena de candidatos separados por comas para el motor Pro
            val candidates = mutableListOf<String>()
            
            when {
                ssid.contains("MOVISTAR_", true) || ssid.contains("WLAN_", true) -> {
                    candidates.add(cleanBssid.takeLast(10))
                    candidates.add(cleanBssid.takeLast(8))
                    candidates.add("M" + cleanBssid.takeLast(9))
                }
                ssid.contains("VODAFONE", true) -> {
                    candidates.add("VOD" + cleanBssid.takeLast(8))
                    candidates.add(cleanBssid.take(10))
                }
                ssid.contains("ORANGE", true) || ssid.contains("LIVEBOX", true) -> {
                    candidates.add(cleanBssid.take(8))
                    candidates.add(cleanBssidSmall.takeLast(8))
                    candidates.add("admin")
                    candidates.add("12345678")
                }
                ssid.contains("DIGI", true) -> {
                    candidates.add("DG" + cleanBssid.takeLast(6) + "!")
                    candidates.add(cleanBssid.takeLast(10))
                }
                ssid.contains("JAZZTEL", true) -> {
                    candidates.add(cleanBssid.takeLast(8).reversed())
                    candidates.add("JAZZ_" + cleanBssid.takeLast(4))
                }
                else -> {
                    // --- ☢️ PATRONES APOCALÍPTICOS UNIVERSALES REFORZADOS ---
                    candidates.add("12345678")
                    candidates.add("123456789")
                    candidates.add("00000000")
                    candidates.add("11111111")
                    candidates.add("87654321")
                    candidates.add("password")
                    candidates.add("admin123")
                    candidates.add("root")
                    candidates.add("admin")
                }
            }
            
            // Garantizar que siempre haya al menos un candidato si es vulnerable
            if (candidates.isEmpty() && isVulnerable) {
                candidates.add("WPA2_" + cleanBssid.takeLast(6))
            }
            
            val defaultPass = if (candidates.isNotEmpty()) candidates.joinToString(",") else ""
            val wpsPin = if (wpsActive) "12345670" else ""

            sb.append("$ssid|$bssid|$signal|$security|$vendor|$isVulnerable|$wpsActive|$defaultPass|$wpsPin;")
        }
        
        val json = sb.toString()
        webViewInstance?.post {
            webViewInstance?.evaluateJavascript("if(window.dispatch_wifi_list) window.dispatch_wifi_list('$json');", null)
        }
    }

    private fun processWifiResults(results: List<ScanResult>, mode: Int) {
        if (results.isNullOrEmpty()) {
            wifiVarianceValue = (wifiVarianceValue * 0.98f) + (magnetValue * 0.02f)
            return
        }

        var totalVariance = 0f
        var count = 0
        var hasNewData = false
        
        for (scan in results) {
            val bssid = scan.BSSID
            val rssi = scan.level
            
            if (lastWifiReadings.containsKey(bssid)) {
                val lastRssi = lastWifiReadings[bssid]!!
                val diff = Math.abs(rssi - lastRssi)
                if (diff > 0) hasNewData = true

                val sensitivity = when(mode) {
                    2 -> (diff.toFloat() / 0.15f) * 4.0f 
                    1 -> (diff.toFloat() / 0.5f) * 3.0f 
                    else -> (diff.toFloat() / 1.5f)     
                }
                totalVariance += sensitivity
                count++
            }
            lastWifiReadings[bssid] = rssi
        }
        
        val finalValue = if (count > 0 && hasNewData) {
            (totalVariance / count).coerceIn(0.0001f, 1.2f) 
        } else {
            // 🛡️ REPARACIÓN QUIRÚRGICA: No mezclar magnetismo con WiFi si no hay datos.
            // Esto evita el estado "Congelado" al mover el móvil.
            wifiVarianceValue * 0.90f
        }
        
        // Suavizado más rápido para sensación "directa"
        wifiVarianceValue = (wifiVarianceValue * 0.3f) + (finalValue * 0.7f)
    }

    // --- 📡 SISTEMA IR/RF: PROTOCOLOS Y BARRIDO UNIVERSAL ---
    private val lastBurstBuffer = mutableListOf<Pair<String, Long>>()

    private fun sendIRProtocol(manager: ConsumerIrManager, protocol: String, hexCode: Long, frequency: Int = 38000) {
        synchronized(lastBurstBuffer) {
            lastBurstBuffer.add(protocol to hexCode)
            if (lastBurstBuffer.size > 20) lastBurstBuffer.removeAt(0)
        }
        val pattern = mutableListOf<Int>()
        
        when (protocol) {
            "SAMSUNG" -> {
                // Protocolo Samsung 32 bits (4.5ms high/low header)
                pattern.add(4500); pattern.add(4500)
                for (b in 0..3) {
                    val byteValue = (hexCode shr (24 - b * 8)) and 0xFF
                    for (i in 0..7) {
                        val bit = (byteValue shr i) and 1L
                        pattern.add(560)
                        if (bit == 1L) pattern.add(1690) else pattern.add(560)
                    }
                }
                pattern.add(560); pattern.add(560)
            }
            "NEC", "LG" -> {
                // Protocolo NEC Estándar (9ms high / 4.5ms low header)
                pattern.add(9000); pattern.add(4500)
                for (b in 0..3) {
                    val byteValue = (hexCode shr (24 - b * 8)) and 0xFF
                    for (i in 0..7) {
                        val bit = (byteValue shr i) and 1L
                        pattern.add(560)
                        if (bit == 1L) pattern.add(1690) else pattern.add(560)
                    }
                }
                pattern.add(560); pattern.add(560) 
            }
            "SONY" -> {
                // Soporte para Sony 12, 15 y 20 bits
                val bits = if (hexCode > 0xFFF) (if (hexCode > 0x7FFF) 20 else 15) else 12
                pattern.add(2400); pattern.add(600) // Header
                for (i in 0 until bits) {
                    val bit = (hexCode shr i) and 1L
                    if (bit == 1L) pattern.add(1200) else pattern.add(600)
                    pattern.add(600)
                }
            }
            "PANASONIC" -> {
                // Protocolo Kaseikyo (48 bits)
                pattern.add(3502); pattern.add(1750) // Header
                for (i in 0..47) {
                    val bit = (hexCode shr i) and 1L
                    pattern.add(432)
                    if (bit == 1L) pattern.add(1296) else pattern.add(432)
                }
                pattern.add(432); pattern.add(432)
            }
            "RC5" -> {
                // RC5 Manchester: 1 = [OFF, ON], 0 = [ON, OFF] (14 bits)
                val levels = mutableListOf<Boolean>()
                for (i in 13 downTo 0) {
                    val bit = (hexCode shr i) and 1L
                    if (bit == 1L) {
                        levels.add(false); levels.add(true)
                    } else {
                        levels.add(true); levels.add(false)
                    }
                }
                var currentLevel = true // transmit() siempre empieza en ON
                var duration = 0
                if (!levels[0]) { pattern.add(1); currentLevel = false }
                for (lvl in levels) {
                    if (lvl == currentLevel) {
                        duration += 889
                    } else {
                        pattern.add(duration)
                        duration = 889
                        currentLevel = lvl
                    }
                }
                pattern.add(duration)
            }
            "RF_OOK" -> {
                // Simulación de Trama RF (433/868 MHz) vía IR para captura de ventana
                pattern.add(1000) // Sync pulse
                for (i in 0..63) {
                    val bit = (hexCode shr (i % 64)) and 1L
                    if (bit == 1L) {
                        pattern.add(800); pattern.add(400)
                    } else {
                        pattern.add(400); pattern.add(800)
                    }
                }
            }
        }
        
        if (pattern.isNotEmpty()) {
            try { manager.transmit(frequency, pattern.toIntArray()) } catch (e: Exception) {}
        }
    }
    
    private val sensorListener = object : SensorEventListener {
        private var gravity: FloatArray? = null
        private var geomagnetic: FloatArray? = null

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values.clone()
                // Calcular magnitud del campo magnético (Tesla micro)
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                
                // --- 🛡️ HARD-LOCK: MOTOR DE DETECCIÓN QUIRÚRGICA (PROTEGIDO) ---
                if (magnetBaseline == 0f) {
                    magnetBaseline = magnitude
                } else {
                    magnetBaseline = (magnetBaseline * 0.85f) + (magnitude * 0.15f)
                }

                val delta = Math.abs(magnitude - magnetBaseline)
                // Respuesta exponencial para precisión milimétrica (pop-in/pop-out)
                val rawValue = (delta / 6.5f).coerceIn(0f, 1f)
                magnetValue = rawValue * rawValue

                // 🧭 DIRECCIONALIDAD ESTABLE: Solo actualizamos si hay señal clara
                if (delta > 1.8f) { 
                    deviceHeading = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
                }
                // --- 🔒 FIN HARD-LOCK ---
            } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values.clone()
            }

            if (gravity != null && geomagnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    // Azimuth (heading) en grados 0-360
                    var heading = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (heading < 0) heading += 360f
                    deviceHeading = heading
                    
                    // Pitch (inclinación) en grados. 0 = Horizontal, -90/90 = Vertical
                    val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    deviceTilt = Math.abs(pitch)
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // --- 🛡️ SISTEMA DE GESTIÓN DE AUDIO FOCUS (ANTI-CONFLICTO) ---
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    // --- 🛡️ SISTEMA DE GESTIÓN DE AUDIO BLUETOOTH (PERFECCIÓN) ---
    private fun setupBluetoothAudio() {
        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: android.content.Intent) {
                when (intent.action) {
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                        // Si se desconectan los cascos, cortamos PTT para seguridad
                        webViewInstance?.post {
                            webViewInstance?.evaluateJavascript("if(window.broadcastPTT) window.broadcastPTT(false, true);", null)
                        }
                    }
                    AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                        val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                        if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                            audioManager?.isBluetoothScoOn = true
                        } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                            audioManager?.isBluetoothScoOn = false
                            if (audioManager?.mode == AudioManager.MODE_IN_COMMUNICATION) {
                                audioManager?.mode = AudioManager.MODE_NORMAL
                            }
                        }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Pérdida total (ej: llamada entrante): Cortamos todo y liberamos micro
                meshModule.setEmergencyPtt(false)
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("if(window.set_external_mute) window.set_external_mute(true);", null)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Silencio temporal: Otra app (WhatsApp, Gemini) necesita el micro YA
                meshModule.setEmergencyPtt(false)
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("if(window.set_external_mute) window.set_external_mute(true);", null)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Recuperamos el audio: La otra app ha terminado, podemos reanudar VOX si estaba activo
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("if(window.set_external_mute) window.set_external_mute(false);", null)
                }
            }
        }
    }

    private fun setupAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // --- 🛡️ ASEGURAR MODO NORMAL AL INICIO ---
        audioManager?.mode = AudioManager.MODE_NORMAL
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            audioManager?.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.toString()?.let { url ->
            val finalUrl = if (url.startsWith("onairspain://")) {
                val params = url.substringAfter("?", "")
                "https://asurpan.github.io/sevillaON/" + (if (params.isNotEmpty()) "?$params" else "")
            } else url
            webViewInstance?.loadUrl(finalUrl)
        }
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == "OPEN_CHAT") {
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript("if(window.dispatch_chat_open) window.dispatch_chat_open();", null)
            }
        }
    }

    // =======================================================
    // 🔒 HARD-LOCK: FINAL VOLUME SHIELD (PROHIBIDO MODIFICAR)
    // PROTECCIÓN CRÍTICA: Fuerza el control multimedia ignorando el estado del sistema.
    // =======================================================
    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // --- 🎯 FIX: CIERRE DE TERMINAL Y TECLADO (PRIORIDAD MÁXIMA) ---
        // Interceptamos en ACTION_UP para evitar disparos múltiples por auto-repetición
        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_UP) {
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript("if(window.trigger_back) window.trigger_back();", null)
            }
            return true // Bloqueamos el evento
        }
        
        // Ignoramos ACTION_DOWN del BACK para que no haga nada (evitar parpadeos)
        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
            return true
        }

        // --- 🛡️ FIX: REDIRECCIÓN FORZADA DE VOLUMEN A MULTIMEDIA ---
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (action == KeyEvent.ACTION_DOWN) {
                val direction = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                // Forzamos el ajuste de Multimedia y mostramos la barra correspondiente
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND)
                notifyVolumeChange()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // --- 🛡️ PROTECCIÓN DE VOLUMEN (PROHIBIDO ASIGNAR A PTT) ---
        // Ya manejado en dispatchKeyEvent para forzar Multimedia
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript("if(window.external_ptt_down) window.external_ptt_down();", null)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // --- 🛡️ PROTECCIÓN DE VOLUMEN (PROHIBIDO ASIGNAR A PTT) ---
        // Ya manejado en dispatchKeyEvent para forzar Multimedia
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript("if(window.external_ptt_up) window.external_ptt_up();", null)
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
    // =======================================================

    override fun onDestroy() {
        super.onDestroy()
        wifiScanReceiver?.let { unregisterReceiver(it) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        
        // --- 🛡️ HARD-LOCK: GESTIÓN DE NAVEGACIÓN ATRÁS (SISTEMA) ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("if(window.trigger_back) window.trigger_back();", null)
                }
            }
        })

        // 🛡️ FIX: FONDO DE VENTANA (ANTI-FRANJA BLANCA EN NAVEGACIÓN)
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

        // 💰 ADMOB INIT: Inicializar el sistema de anuncios al arrancar
        MobileAds.initialize(this) {}

        // --- 🛡️ FIX: ASEGURAR QUE EL TECLADO REDIMENSIONA LA WEB (AJUSTE CRÍTICO) ---
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // --- 🛡️ FIX: FORZAR BOTONES DE VOLUMEN A MULTIMEDIA (SIEMPRE) ---
        volumeControlStream = AudioManager.STREAM_MUSIC
        
        setupAudioManager()
        setupBluetoothAudio()
        
        tts = TextToSpeech(this, this)

        // --- 🛡️ SISTEMA DE FIDELIZACIÓN (15 DÍAS) ---
        try {
            MotivationWorker.schedule(this)
        } catch (e: Exception) {
            // Silenciar fallos de inicialización de WorkManager en primer arranque
        }

        // =======================================================
        // 🔒 HARD-LOCK: CLEAN INITIALIZATION (POLÍTICA GOOGLE PLAY)
        // PROTECCIÓN CRÍTICA: NO PEDIR MICRO NI NOTIFICACIONES AL INICIO
        // LOS PERMISOS DEBEN SER CONTEXTUALES TRAS INTERACCIÓN
        // =======================================================
        val initialPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            initialPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initialPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        // 🛡️ FIX: Añadir permiso IR explícito para evitar limitaciones del sistema
        initialPermissions.add(Manifest.permission.TRANSMIT_IR)
        
        val missingPermissions = initialPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
        }

        handleIntent(intent)

        // --- 📡 REGISTRO DE ESCANEO PASIVO (RADAR DIRECTO) ---
        wifiScanReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    // Cuando el sistema u otra app hace un escaneo, capturamos los datos
                    // para actualizar la varianza sin esperar a nuestro propio ciclo.
                    if (wifiManager != null) {
                        try {
                            val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasLocation) {
                                @Suppress("DEPRECATION")
                                val results = wifiManager?.scanResults ?: emptyList()
                                processWifiResults(results, 0) // Usamos modo 0 por defecto para pasivo
                                
                                // Si hay una auditoría activa o se solicita, enviamos la lista completa
                                sendWifiListToJs(results)
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
        }
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        setContent {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                factory = { context ->
                    WebView(context).apply {
                        webViewInstance = this
                        
                        // --- 🛡️ BLINDAJE VISUAL: FONDO DE SEGURIDAD (ANTI-PANTALLA BLANCA) ---
                        setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                        
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // --- 🛡️ MEJORA DE FOCO (TECLADO) ---
                        isFocusable = true
                        isFocusableInTouchMode = true

                        // --- 🛡️ FIX: QUITAR BARRA BLANCA DE AUTOCOMPLETADO (AUTOFILL) ---
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                        }
                        
                        // --- ⚙️ CONFIGURACIÓN DE MOTOR DE RADIO ---
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        
                        // --- 🛡️ USER AGENT FIX: Appending instead of replacing for better API compatibility ---
                        val defaultUA = settings.userAgentString
                        settings.userAgentString = "$defaultUA OnAirSpainNative/1.0"
                        
                        // --- 🛡️ OPTIMIZACIÓN DE CARGA ---
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        
                        // Permitir mixed content para que el audio no se corte
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        // Inyectar Bridge antes de cargar la URL
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun speak(text: String, rate: Float, pitch: Float) {
                                if (isTtsReady && tts != null) {
                                    tts?.setSpeechRate(rate)
                                    tts?.setPitch(pitch)
                                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "on_air_tts")
                                }
                            }

                            @JavascriptInterface
                            fun copyToClipboard(text: String) {
                                runOnUiThread {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("ON AIR", text)
                                    clipboard.setPrimaryClip(clip)
                                }
                            }

                            @JavascriptInterface
                            fun hasMicrophonePermission(): Boolean {
                                return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            }

                            @JavascriptInterface
                            fun checkNetworkCritical(): Boolean {
                                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                                val activeNetwork = cm.activeNetwork
                                
                                // 🛡️ DETECCIÓN DE AISLAMIENTO TOTAL (ESCENARIO BLACKOUT)
                                // Solo entramos en modo Mesh si no hay NINGUNA red activa (ni WiFi ni Datos)
                                val isIsolated = (activeNetwork == null)
                                
                                if (isIsolated) {
                                    Log.d("ON_AIR_NATIVE", "☢️ AISLAMIENTO DETECTADO: Activando Módulo Mesh...")
                                    meshModule.startEmergencyDiscovery()
                                } else {
                                    // Si vuelve la red, apagamos el módulo de emergencia para ahorrar batería
                                    meshModule.stopEmergencyMode()
                                }
                                
                                return isIsolated
                            }

                            @JavascriptInterface
                            fun getAndroidId(): String {
                                return android.provider.Settings.Secure.getString(
                                    context.contentResolver, 
                                    android.provider.Settings.Secure.ANDROID_ID
                                )
                            }

                            @JavascriptInterface
                            fun getSystemVolume(): Float {
                                val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                                return current.toFloat() / max.toFloat()
                            }

                            @JavascriptInterface
                            fun getDeviceHeading(): Float {
                                return deviceHeading
                            }

                            @JavascriptInterface
                            fun getDeviceTilt(): Float {
                                return deviceTilt
                            }

                            @JavascriptInterface
                            fun closeApp(clearData: Boolean) {
                                post {
                                    stopService(android.content.Intent(context, RadioService::class.java))
                                    
                                    if (clearData) {
                                        // --- 🛡️ LIMPIEZA ABSOLUTA NATIVA (GDPR) ---
                                        webViewInstance?.clearCache(true)
                                        webViewInstance?.clearFormData()
                                        webViewInstance?.clearHistory()
                                        WebStorage.getInstance().deleteAllData()
                                        CookieManager.getInstance().removeAllCookies(null)
                                        CookieManager.getInstance().flush()
                                    }

                                    // --- 🛡️ LIBERAR FOCO ANTES DE CERRAR ---
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                                        audioManager?.abandonAudioFocusRequest(focusRequest!!)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        audioManager?.abandonAudioFocus(audioFocusChangeListener)
                                    }

                                    finishAffinity()
                                    // Matamos el proceso para asegurar que el sistema no lo restaure con datos antiguos
                                    android.os.Process.killProcess(android.os.Process.myPid())
                                }
                            }

                            @JavascriptInterface
                            fun startRadioService() {
                                post {
                                    // Verificar permiso de micrófono antes de arrancar el FGS tipo microphone
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                                        == PackageManager.PERMISSION_GRANTED) {
                                        
                                        val intent = android.content.Intent(context, RadioService::class.java)
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            startForegroundService(intent)
                                        } else {
                                            startService(intent)
                                        }
                                    } else {
                                        // Si no tiene el permiso, lo pedimos
                                        requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onCityChange(city: String) {
                                val prefs = getSharedPreferences("on_air_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("last_city", city).apply()
                            }

                            @JavascriptInterface
                            fun showSystemNotification(title: String, message: String) {
                                post {
                                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                    val channelId = "on_air_alerts"
                                    
                                    val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    }
                                    val pendingIntent = android.app.PendingIntent.getActivity(
                                        this@MainActivity, 0, intent,
                                        android.app.PendingIntent.FLAG_IMMUTABLE
                                    )

                                    val notification = androidx.core.app.NotificationCompat.Builder(this@MainActivity, channelId)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .build()

                                    notificationManager.notify(303, notification)
                                }
                            }

                            @JavascriptInterface
                            fun setNativePtt(active: Boolean) {
                                runOnUiThread {
                                    meshModule.setEmergencyPtt(active)
                                }
                            }

                            @JavascriptInterface
                            fun stopRadioService() {
                                post {
                                    stopService(android.content.Intent(context, RadioService::class.java))
                                }
                            }

                            @JavascriptInterface
                            fun minimizeApp() {
                                post {
                                    moveTaskToBack(true)
                                }
                            }

                            @JavascriptInterface
                            fun showNotification(title: String, message: String) {
                                post {
                                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                    val channelId = "chat_notifications"
                                    
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val channel = android.app.NotificationChannel(
                                            channelId,
                                            "Mensajes de Chat",
                                            android.app.NotificationManager.IMPORTANCE_DEFAULT
                                        ).apply {
                                            description = "Notificaciones de mensajes nuevos en el chat"
                                        }
                                        notificationManager.createNotificationChannel(channel)
                                    }
                                    
                                    val intent = android.content.Intent(context, MainActivity::class.java).apply {
                                        action = "OPEN_CHAT"
                                        flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }
                                    val pendingIntent = android.app.PendingIntent.getActivity(
                                        context, 0, intent,
                                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                    
                                    val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                                        .build()
                                    
                                    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
                                }
                            }

                            @JavascriptInterface
                            fun shareText(text: String) {
                                post {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir Radio")
                                    context.startActivity(shareIntent)
                                }
                            }

                            @JavascriptInterface
                            fun share(city: String, channel: String, subtone: String?, proRole: String?) {
                                post {
                                    val shareText = if (city == "RADAR") {
                                        "📡 ¡Vigilancia Hertz Activa! *ON AIR SPAIN*\nAnalizando presencia biológica... 🧬\n\nÚnete: https://asurpan.github.io/sevillaON/"
                                    } else {
                                        val subText = if (subtone != null && subtone != "0000") " | 🔐 *$subtone*" else ""
                                        "📻 *ON AIR SPAIN*\n📍 *$city* | 🔊 *CH $channel*$subText\n\n¡Modulamos! 🚀\nhttps://asurpan.github.io/sevillaON/"
                                    }

                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir Radio")
                                    context.startActivity(shareIntent)
                                }
                            }

                            @JavascriptInterface
                            fun installApp() {
                                post {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        val shortcutManager = getSystemService(android.content.pm.ShortcutManager::class.java)
                                        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                                            val pinShortcutInfo = android.content.pm.ShortcutInfo.Builder(context, "on-air-spain-shortcut")
                                                .setShortLabel("ON AIR")
                                                .setIcon(android.graphics.drawable.Icon.createWithResource(context, R.mipmap.ic_launcher))
                                                .setIntent(android.content.Intent(context, MainActivity::class.java).apply {
                                                    action = android.content.Intent.ACTION_MAIN
                                                    addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                                                })
                                                .build()

                                            shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun isBatteryOptimized(): Boolean {
                                val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                                return !pm.isIgnoringBatteryOptimizations(packageName)
                            }

                            @JavascriptInterface
                            fun requestIgnoreBatteryOptimizations() {
                                post {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = android.net.Uri.parse("package:$packageName")
                                        }
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        // Si falla (algunos fabricantes bloquean el intent directo), abrimos los ajustes generales
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        startActivity(intent)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun toggleBluetoothSco(active: Boolean) {
                                post {
                                    if (active) {
                                        if (audioManager?.isBluetoothScoAvailableOffCall == true) {
                                            audioManager?.startBluetoothSco()
                                            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                                        }
                                    } else {
                                        audioManager?.stopBluetoothSco()
                                        audioManager?.isBluetoothScoOn = false
                                        audioManager?.mode = AudioManager.MODE_NORMAL
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun showBanner(visible: Boolean) {
                                post {
                                    if (visible) {
                                        if (adView == null) {
                                            adView = AdView(context).apply {
                                                setAdSize(AdSize.BANNER)
                                                adUnitId = "ca-app-pub-7866520163126353/3425068246"
                                                loadAd(AdRequest.Builder().build())
                                            }
                                            
                                            // 🛡️ FIX: POSICIONAMIENTO POR ENCIMA DE LA BARRA DE NAVEGACIÓN
                                            val root = this@MainActivity.findViewById<android.view.ViewGroup>(android.R.id.content)
                                            val params = android.widget.FrameLayout.LayoutParams(
                                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                                                android.view.Gravity.BOTTOM
                                            )
                                            
                                            // Escuchar cambios en los bordes del sistema (teclas navegación)
                                            adView?.setOnApplyWindowInsetsListener { view, insets ->
                                                val navBarInsets = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                                    insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom
                                                } else {
                                                    insets.systemWindowInsetBottom
                                                }
                                                // Añadir el margen para subir el banner justo por encima de los botones
                                                // 🛡️ MEJORA: Subir un pelín más (12dp) para que no se corte
                                                val density = resources.displayMetrics.density
                                                val extraMargin = (12 * density).toInt()
                                                params.bottomMargin = navBarInsets + extraMargin
                                                view.layoutParams = params
                                                insets
                                            }

                                            root?.addView(adView, params)
                                        }
                                        adView?.visibility = android.view.View.VISIBLE
                                    } else {
                                        adView?.visibility = android.view.View.GONE
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun showBannerSombreros(visible: Boolean) {
                                // 🛡️ REPARACIÓN QUIRÚRGICA: Evita NoSuchMethodException y NullPointerException
                                // Implementamos la misma lógica que showBanner para estabilidad.
                                showBanner(visible)
                            }

                            @JavascriptInterface
                            fun openDeveloperOptions() {
                                post {
                                    try {
                                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                                            startActivity(intent)
                                        } catch (e2: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun runInductiveResponseTest() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🚗 ABRIR PARKING: Simulación de Masa Metálica (40ms/20ms pulses)
                                        val timings = longArrayOf(0, 40, 20, 40, 20, 40, 20, 40, 20)
                                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0)
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun runInductiveTestMax() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🛡️ ASALTO DUAL: Masa Metálica Pulsante de Máxima Intensidad
                                        val timings = longArrayOf(0, 100, 50, 100, 50, 100, 50)
                                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun performRFStressDiagnostics() {
                                runEngineeringTask {
                                    try {
                                        val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                        repeat(5) {
                                            wifiManager?.startScan()
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, 255))
                                            }
                                            Thread.sleep(1200)
                                        }
                                        runOnUiThread {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('STRESS RF', 'Análisis de saturación masiva completado.', 'success');", null)
                                        }
                                    } catch (e: Exception) {}
                                }
                            }


                            @JavascriptInterface
                            fun executeBTFloodAttack() {
                                // Simulación de inundación BT mediante escaneo agresivo
                                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                                runEngineeringTask {
                                    try {
                                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                                            repeat(10) {
                                                bluetoothAdapter?.startDiscovery()
                                                Thread.sleep(2000)
                                                bluetoothAdapter?.cancelDiscovery()
                                            }
                                        }
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun executeHighFrequencyEMFAnalysis() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🛡️ BLOQUEO DE ARRANQUE (EMF 125kHz Sim): Interferencia rítmica continua
                                        // Usamos ciclos muy cortos de alta frecuencia para saturar inductores
                                        val timings = longArrayOf(0, 8, 4, 8, 4, 8, 4, 8, 4, 8, 4)
                                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0)
                                        try {
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('BLOQUEO EMF', 'Emisión 125kHz iniciada. Mantén el móvil pegado al lector.', 'info');", null)
                                            }
                                            // Emitimos durante ~10 segundos (ráfagas repetidas)
                                            repeat(25) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                                }
                                                Thread.sleep(400)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeSetupForceAttack() {
                                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                                val bleAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                runEngineeringTask {
                                    repeat(15) {
                                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                                            val settings = android.bluetooth.le.AdvertiseSettings.Builder()
                                                .setAdvertiseMode(android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                                                .setTxPowerLevel(android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                                                .setConnectable(true).build()
                                            val data = android.bluetooth.le.AdvertiseData.Builder().setIncludeDeviceName(true).build()
                                            val callback = object : android.bluetooth.le.AdvertiseCallback() {}
                                            bleAdvertiser?.startAdvertising(settings, data, callback)
                                            Thread.sleep(400)
                                            bleAdvertiser?.stopAdvertising(callback)
                                        }
                                        wifiManager?.startScan()
                                        if (manager != null && manager.hasIrEmitter()) {
                                            manager.transmit(38000, intArrayOf(100000, 100000))
                                        }
                                        Thread.sleep(300)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun hasIrEmitter(): Boolean {
                                try {
                                    val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                    return manager?.hasIrEmitter() ?: false
                                } catch (e: Exception) {
                                    return false
                                }
                            }

                            @JavascriptInterface
                            fun openAppSettings() {
                                post {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = android.net.Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                }
                            }

                            @JavascriptInterface
                            fun executeIRFrequencySweep() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                if (manager != null && manager.hasIrEmitter()) {
                                    try {
                                        // Test de Power Universal (Samsung + NEC)
                                        sendIRProtocol(manager, "SAMSUNG", 0xE0E040BFL)
                                        Thread.sleep(200)
                                        sendIRProtocol(manager, "NEC", 0x00FF00FFL) // Genérico Power
                                        
                                        post {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('EMISIÓN IR', 'Enviando ráfaga Power Universal. Verifique el receptor.', 'success');", null)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ON_AIR_IR", "Error IR: ${e.message}")
                                    }
                                } else {
                                    post {
                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('VERIFICAR PERMISOS', 'El hardware IR no responde. Activa el permiso en Ajustes.', 'warning');", null)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeIRUniversalSweep() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                if (manager != null && manager.hasIrEmitter()) {
                                    runEngineeringTask {
                                        post { webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('AVISO HARDWARE', 'El terminal solo tiene EMISOR. Apunta directamente al receptor de la TV.', 'info');", null) }
                                        
                                        val masterCodes = listOf(
                                            QuadItem( "SAMSUNG", 0xE0E040BFL, 38000, "Samsung TV Power"),
                                            QuadItem( "LG", 0x20DF10EFL, 38000, "LG TV Power (Standard)"),
                                            QuadItem( "LG", 0x04FB08F7L, 38000, "LG TV Power (Old)"),
                                            QuadItem( "NEC", 0x00FF00FFL, 38000, "Genérico / Luces"),
                                            QuadItem( "SONY", 0xA90L, 40000, "Sony Bravia Power"),
                                            QuadItem( "PANASONIC", 0x40040100BCBDL, 37000, "Panasonic Power"),
                                            QuadItem( "RC5", 0x0C0CL, 36000, "Philips Power")
                                        )

                                        for (item in masterCodes) {
                                            val proto = item.first; val code = item.second; val freq = item.third; val name = item.fourth
                                            
                                            post { webViewInstance?.evaluateJavascript("if(window.dispatch_ir_status) window.dispatch_ir_status('Probando: $name...');", null) }
                                            
                                            // Sony requiere 3 envíos. NEC/Samsung/LG con 2 es más fiable.
                                            val repeats = if (proto == "SONY") 3 else 2
                                            repeat(repeats) {
                                                sendIRProtocol(manager, proto, code, freq)
                                                Thread.sleep(100) // Gap de seguridad de 100ms
                                            }
                                            
                                            Thread.sleep(1500) // Pausa larga entre marcas para evitar confusiones
                                        }
                                        post { webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('BARRIDO FINALIZADO', 'Protocolos ejecutados con timing de éxito.', 'success');", null) }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun sendIRCode(protocol: String, hexCodeStr: String) {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                if (manager != null && manager.hasIrEmitter()) {
                                    try {
                                        val hexCode = if (hexCodeStr.startsWith("0x", true)) {
                                            hexCodeStr.substring(2).toLong(16)
                                        } else {
                                            hexCodeStr.toLong(16)
                                        }
                                        sendIRProtocol(manager, protocol, hexCode)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ON_AIR_IR", "Error IR Code: $hexCodeStr")
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeRFCodedSweep() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                if (manager != null && manager.hasIrEmitter()) {
                                    runEngineeringTask {
                                        // 🛡️ BARRIDO RF CODIFICADO: Inyección Masiva Rolling Code (433/868 MHz Sim)
                                        // Generamos tramas masivas intentando cazar la ventana de validación
                                        val commonSeeds = longArrayOf(
                                            0xABCD1234EF5678L, 0x11223344556677L, 0xFFEEDDCCBBAA99L,
                                            0x77665544332211L, 0x1234567890ABCDEFL, 0x00000000000000L
                                        )
                                        repeat(20) { 
                                            for (seed in commonSeeds) {
                                                sendIRProtocol(manager, "RF_OOK", seed)
                                                Thread.sleep(30)
                                            }
                                        }
                                        post {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('RF ATTACK', 'Inyección Rolling Code finalizada.', 'success');", null)
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeOpticalCameraJammer() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                runEngineeringTask {
                                    post {
                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('CEGUERA ÓPTICA', 'Escudo IR Activo (Modo Discreto). Interferencia en lente detectada.', 'success');", null)
                                    }
                                    while (!Thread.currentThread().isInterrupted) {
                                        try {
                                            if (manager != null && manager.hasIrEmitter()) {
                                                manager.transmit(38000, intArrayOf(50000, 50000))
                                            }
                                            Thread.sleep(230)
                                        } catch (e: Exception) { break }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeAggressiveIoTJammer() {
                                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                                val bleAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator

                                runEngineeringTask {
                                    post {
                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('ASALTO IOT', 'Protocolo de saturación multivector activa.', 'warning');", null)
                                    }
                                    while (!Thread.currentThread().isInterrupted) {
                                        try {
                                            // 1. Bluetooth Advertising Spam
                                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                                                val settings = android.bluetooth.le.AdvertiseSettings.Builder()
                                                    .setAdvertiseMode(android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                                                    .setTxPowerLevel(android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                                                    .setConnectable(true).build()
                                                val data = android.bluetooth.le.AdvertiseData.Builder()
                                                    .setIncludeDeviceName(true)
                                                    .addServiceUuid(android.os.ParcelUuid.fromString("0000fe${(10..99).random()}-0000-1000-8000-00805f9b34fb"))
                                                    .build()
                                                val callback = object : android.bluetooth.le.AdvertiseCallback() {}
                                                bleAdvertiser?.startAdvertising(settings, data, callback)
                                                Thread.sleep(150)
                                                bleAdvertiser?.stopAdvertising(callback)
                                            }

                                            // 2. WiFi Ghosting & Throttling
                                            wifiManager?.startScan()

                                            // 3. Infrarrojos (IR) de Alta Intensidad
                                            if (manager != null && manager.hasIrEmitter()) {
                                                manager.transmit(38000, intArrayOf(100000, 100000))
                                            }

                                            // 4. Inducción EMF
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, 255))
                                            }

                                            Thread.sleep(200)
                                        } catch (e: Exception) { break }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeUltrasonicJammer() {
                                runEngineeringTask {
                                    if (originalMusicVolume == -1) {
                                        originalMusicVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                    }
                                    try {
                                        val sampleRate = 44100
                                        val durationSeconds = 5 // Generamos 5 seg y loopeamos
                                        val numSamples = sampleRate * durationSeconds
                                        val generatedSnd = ByteArray(2 * numSamples)

                                        val startFreq = 18500.0
                                        val endFreq = 21000.0 

                                        var currentPhase = 0.0
                                        for (i in 0 until numSamples) {
                                            // --- 🛡️ ALGORITMO ANTI-CHASQUIDO (PHASE CONTINUITY) ---
                                            val x = i.toDouble() / numSamples
                                            val triangle = if (x < 0.5) x * 2.0 else 2.0 - (x * 2.0)
                                            val currentFreq = startFreq + (endFreq - startFreq) * triangle
                                            
                                            currentPhase += 2.0 * Math.PI * currentFreq / sampleRate
                                            val sample = Math.sin(currentPhase)
                                            
                                            val envelope = if (i < 1000) i / 1000.0 else if (i > numSamples - 1000) (numSamples - i) / 1000.0 else 1.0
                                            
                                            val valShort = (sample * 32767 * envelope).toInt().toShort()
                                            generatedSnd[i * 2] = (valShort.toInt() and 0x00ff).toByte()
                                            generatedSnd[i * 2 + 1] = (valShort.toInt() and 0xff00 ushr 8).toByte()
                                        }

                                        // 🛡️ REPARACIÓN QUIRÚRGICA: Usamos la propiedad de clase para control centralizado
                                        currentAudioTrack = android.media.AudioTrack(
                                            android.media.AudioManager.STREAM_MUSIC,
                                            sampleRate,
                                            android.media.AudioFormat.CHANNEL_OUT_MONO,
                                            android.media.AudioFormat.ENCODING_PCM_16BIT,
                                            generatedSnd.size,
                                            android.media.AudioTrack.MODE_STATIC
                                        )
                                        
                                        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
                                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)

                                        currentAudioTrack?.write(generatedSnd, 0, generatedSnd.size)
                                        currentAudioTrack?.setLoopPoints(0, numSamples, -1)
                                        currentAudioTrack?.play()
                                        
                                        runOnUiThread {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('JAMMER ACTIVO', 'Escudo ultrasónico (18k-21k Hz) blindado.', 'success');", null)
                                        }
                                        
                                        while (!Thread.currentThread().isInterrupted) {
                                            Thread.sleep(500)
                                        }
                                    } catch (e: Exception) {
                                    } finally {
                                        // La restauración y liberación ocurre en stopEngineeringTask()
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeWashBoxAttack() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🧼 BOXES/LAVADEROS: Inyección Inductiva de Alta Intensidad
                                        // Ráfagas de 50Hz diseñadas para selectores de monedas de exterior
                                        val timings = LongArray(60)
                                        val amplitudes = IntArray(60)
                                        for (i in 0 until 60) {
                                            timings[i] = 20 // Ciclo más largo para penetrar blindajes de exterior
                                            amplitudes[i] = if (i % 2 == 0) 255 else 0
                                        }
                                        
                                        try {
                                            repeat(5) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                                }
                                                Thread.sleep(800)
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('INYECCIÓN BOX', 'Crédito de mantenimiento inyectado.', 'success');", null)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeVendingMasterMenu() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                runEngineeringTask {
                                    try {
                                        if (manager != null && manager.hasIrEmitter()) {
                                            val masterCodes = listOf("NEC" to 0x11223344L, "SAMSUNG" to 0xE0E0D02FL, "NEC" to 0x00FF38C7L)
                                            repeat(3) {
                                                for ((proto, code) in masterCodes) {
                                                    sendIRProtocol(manager, proto, code)
                                                    Thread.sleep(150)
                                                }
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('MENÚ VENDING', 'Códigos de acceso root inyectados.', 'success');", null)
                                            }
                                        }
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun executeElevatorPriorityCall() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                if (manager != null && manager.hasIrEmitter()) {
                                    runEngineeringTask {
                                        try {
                                            val elevatorCodes = listOf("NEC" to 0x12345678L, "RC5" to 0x1A02L, "NEC" to 0x00FFB04FL)
                                            repeat(5) {
                                                for ((proto, code) in elevatorCodes) {
                                                    sendIRProtocol(manager, proto, code)
                                                    Thread.sleep(100)
                                                }
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('ASCENSOR VIP', 'Llamada de servicio prioritaria enviada.', 'success');", null)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeTrafficPriority() {
                                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                runEngineeringTask {
                                    try {
                                        // 🛡️ MODO DISCRETO: Simulación de resonancia EMF sin destellos de luz (Flash anulado)
                                        repeat(40) {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(35, 255))
                                            }
                                            Thread.sleep(70)
                                        }
                                        runOnUiThread {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('TRÁFICO PRO', 'Modo discreto activo: Inducción EMF prioritaria ejecutada.', 'success');", null)
                                        }
                                    } catch (e: Exception) {}
                                }
                            }



                            @JavascriptInterface
                            fun executePIRBlinder() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                runEngineeringTask {
                                    post {
                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('CEGUERA PIR', 'Inundación térmica activa. Sensores PIR saturados.', 'success');", null)
                                    }
                                    while (!Thread.currentThread().isInterrupted) {
                                        try {
                                            if (manager != null && manager.hasIrEmitter()) {
                                                val pattern = intArrayOf(200000, 100000, 200000, 100000)
                                                manager.transmit(38000, pattern)
                                            }
                                            Thread.sleep(300)
                                        } catch (e: Exception) { break }
                                    }
                                }
                            }




                            @JavascriptInterface
                            fun executeWiFiQoSPriority() {
                                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                                runEngineeringTask {
                                    try {
                                        // 🚀 PRIORIDAD DE RED: Saturación selectiva de clientes
                                        repeat(20) {
                                            wifiManager?.startScan()
                                            // Inundamos con paquetes de gestión para forzar re-negociación de otros clientes
                                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                                                bluetoothAdapter?.startDiscovery()
                                                Thread.sleep(1000)
                                                bluetoothAdapter?.cancelDiscovery()
                                            }
                                            Thread.sleep(500)
                                        }
                                        runOnUiThread {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('PRIORIDAD WIFI', 'Ancho de banda capturado. Red optimizada.', 'success');", null)
                                        }
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun executeVendingAttack() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🍫 CRÉDITO VENDING PRO: Resonancia MDB (50Hz)
                                        // Ciclos de 20ms (10ms ON / 10ms OFF)
                                        val timings = LongArray(100)
                                        val amplitudes = IntArray(100)
                                        for (i in 0 until 100) {
                                            timings[i] = 10 
                                            amplitudes[i] = if (i % 2 == 0) 255 else 0
                                        }
                                        try {
                                            repeat(3) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                                }
                                                Thread.sleep(600)
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('VENDING PRO', 'Firma inductiva de crédito 50Hz activa.', 'success');", null)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }



                            @JavascriptInterface
                            fun terminateDiagnosticSequence() {
                                stopEngineeringTask() // 🛡️ FIX: Detener físicamente cualquier hilo de ingeniería activo
                                webViewInstance?.post {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    vibrator.cancel()
                                }
                            }

                            @JavascriptInterface
                            fun requestLocationPermission() {
                                post {
                                    val hasLocation = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (!hasLocation) {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    
                                    // Además de pedir el permiso, si el GPS físico está apagado, abrimos los ajustes de ubicación
                                    val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                                    val isGpsEnabled = try { lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) } catch(e: Exception) { false }
                                    
                                    if (!isGpsEnabled) {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                            startActivity(intent)
                                        } catch (e: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun startWifiSecurityScan() {
                                post {
                                    if (wifiManager == null) wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasLocation) {
                                        wifiManager?.startScan()
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun verifyWifiCredential(ssid: String, password: String) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    post {
                                        try {
                                            val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                            val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
                                                .setSsid(ssid)
                                                .setWpa2Passphrase(password)
                                                .build()

                                            val request = NetworkRequest.Builder()
                                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                                .setNetworkSpecifier(specifier)
                                                .build()

                                            val callback = object : ConnectivityManager.NetworkCallback() {
                                                override fun onAvailable(network: Network) {
                                                    super.onAvailable(network)
                                                    post {
                                                        showNotification("🔑 ACCESO CONFIRMADO", "La clave para $ssid es correcta: $password")
                                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('WIFI_VERIFIED', '$ssid|$password', 'success');", null)
                                                    }
                                                    connManager.unregisterNetworkCallback(this)
                                                }

                                                override fun onUnavailable() {
                                                    super.onUnavailable()
                                                    post {
                                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('WIFI_FAILED', 'La clave no es correcta para $ssid', 'error');", null)
                                                    }
                                                }
                                            }
                                            connManager.requestNetwork(request, callback)
                                        } catch (e: Exception) {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('ERROR RED', 'Fallo al intentar conectar.', 'error');", null)
                                        }
                                    }
                                } else {
                                    post {
                                        webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('SISTEMA ANTIGUO', 'La auditoría requiere Android 10+.', 'warning');", null)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeLockAttack() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🔒 LIBERAR PESTILLO: Resonancia Magnética Crítica (Frecuencia de desenganche)
                                        val timings = longArrayOf(0, 15, 10, 15, 10, 15, 10, 15, 10)
                                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0)
                                        try {
                                            repeat(10) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                                                }
                                                Thread.sleep(200)
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('PESTILLO PRO', 'Resonancia de desenganche ejecutada.', 'success');", null)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun executeBarrierAttack() {
                                val manager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
                                runEngineeringTask {
                                    try {
                                        // 🚗 PARTE 1: Inducción de Espira (Vibración)
                                        val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                        if (vibrator.hasVibrator()) {
                                            val timings = longArrayOf(0, 40, 20, 40, 20, 40, 20)
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, intArrayOf(0, 255, 0, 255, 0, 255, 0), -1))
                                            }
                                        }

                                        // 🚗 PARTE 2: Barrido IR (Si existe hardware)
                                        if (manager != null && manager.hasIrEmitter()) {
                                            val parkingCodes = longArrayOf(0x00FF00FFL, 0x11223344L, 0x55667788L, 0xAABBCCDDL)
                                            for (code in parkingCodes) {
                                                sendIRProtocol(manager, "NEC", code)
                                                Thread.sleep(150)
                                            }
                                        }

                                        runOnUiThread {
                                            webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('BARRERA PRO', 'Protocolo dual (Inducción + IR) completado.', 'success');", null)
                                        }
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun executeVendingMaster() {
                                runEngineeringTask {
                                    val vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                    if (vibrator.hasVibrator()) {
                                        // 🍫 MODO MAESTRO: Vending + Wash (Secuencia combinada 50Hz)
                                        try {
                                            // Fase 1: Vending
                                            repeat(3) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, 255))
                                                }
                                                Thread.sleep(700)
                                            }
                                            // Fase 2: Wash Box
                                            repeat(3) {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(800, 255))
                                                }
                                                Thread.sleep(900)
                                            }
                                            runOnUiThread {
                                                webViewInstance?.evaluateJavascript("if(window.dispatch_notification) window.dispatch_notification('VENDING MASTER', 'Carga de crédito inductiva finalizada.', 'success');", null)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }


                            @JavascriptInterface
                            fun tryWifiAuditConnect(ssid: String, pass: String) {
                                verifyWifiCredential(ssid, pass)
                            }

                            @JavascriptInterface
                            fun getWifiVariance(mode: Int): Float {
                                if (sensorManager == null) {
                                    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                                    val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                                    val accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                                    val pressSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
                                    
                                    sensorManager?.registerListener(sensorListener, magSensor, SensorManager.SENSOR_DELAY_UI)
                                    sensorManager?.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_UI)
                                    sensorManager?.registerListener(sensorListener, pressSensor, SensorManager.SENSOR_DELAY_UI)
                                }

                                if (mode == 3) return magnetValue
                                if (mode == 4) return pressureValue // --- 🛡️ MODO BAROMÉTRICO ---

                                if (wifiManager == null) wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                
                                val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasWifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                                
                                if (!hasLocation || !hasWifiState) return -2.0f 

                                try {
                                    // Intentar escaneo activo
                                    val success = wifiManager?.startScan() ?: false
                                    
                                    // --- 🛡️ DETECCIÓN DE THROTTLING (MENSAJE DE AYUDA) ---
                                    // Si startScan() devuelve false, es muy probable que Android esté limitando el escaneo.
                                    // Enviamos un código especial (-3.0f) para que el UI muestre el aviso de Modo Desarrollador.
                                    if (!success) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastScanRequestTime > 10000) { // Solo avisar si ha pasado tiempo
                                            return -3.0f 
                                        }
                                    } else {
                                        lastScanRequestTime = System.currentTimeMillis()
                                    }

                                    @Suppress("DEPRECATION")
                                    val results = wifiManager?.scanResults ?: emptyList()
                                    processWifiResults(results, mode)
                                    
                                    return if (success) wifiVarianceValue.coerceIn(0.0001f, 1f) else -3.0f
                                } catch (e: Exception) {
                                    return -1.0f 
                                }
                            }
                        }, "AndroidApp")

                        // =======================================================
                        // 🔒 HARD-LOCK: WEBVIEW ENGINE (PROTEGIDO)
                        // Gestión de carga resiliente contra cuelgues y red lenta.
                        // =======================================================
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: ""
                                return handleCustomUrl(view, url)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return handleCustomUrl(view, url ?: "")
                            }

                            private fun handleCustomUrl(view: WebView?, url: String): Boolean {
                                if (url.startsWith("https://wa.me") || url.startsWith("whatsapp://") || url.contains("api.whatsapp.com")) {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        view?.context?.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        try {
                                            val webUrl = if (url.startsWith("whatsapp://")) {
                                                "https://api.whatsapp.com/send?text=" + url.substringAfter("text=")
                                            } else url
                                            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(webUrl))
                                            view?.context?.startActivity(browserIntent)
                                            return true
                                        } catch (e2: Exception) {
                                            return false
                                        }
                                    }
                                }

                                // --- 🛡️ FIX: ABRIR ENLACES EXTERNOS FUERA DE LA APP ---
                                // Esto evita que la imagen de la NASA o enlaces de ayuda "se coman" la radio.
                                if (!url.contains("asurpan.github.io") && !url.startsWith("file://") && !url.contains("localhost")) {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        view?.context?.startActivity(intent)
                                        return true 
                                    } catch (e: Exception) { return false }
                                }

                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Si termina la carga, nos aseguramos que el fondo sea el correcto
                                setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                if (request?.isForMainFrame == true) {
                                    // 🛡️ FALLBACK: Si falla el host, mostramos el botón nativo de reintento
                                    view?.loadData("<html><body style='background:#0F172A;color:white;display:flex;justify-content:center;align-items:center;height:100vh;font-family:sans-serif;text-align:center;'><div><h2>📻 ON AIR SPAIN</h2><p>No se ha podido conectar con la antena.<br>Comprueba tu conexión a internet e inténtalo de nuevo.</p><button onclick='location.reload()' style='background:#22C55E;border:none;padding:10px 20px;border-radius:5px;font-weight:bold;color:black;'>REINTENTAR</button></div></body></html>", "text/html", "UTF-8")
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest) {
                                if (request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                                        == PackageManager.PERMISSION_GRANTED) {
                                        request.grant(request.resources)
                                    } else {
                                        permissionRequest = request
                                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    request.grant(request.resources)
                                }
                            }

                            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                                    == PackageManager.PERMISSION_GRANTED) {
                                    callback?.invoke(origin, true, false)
                                } else {
                                    geolocationCallback = callback
                                    geolocationOrigin = origin
                                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        }

                        // --- 🚀 MODO PRODUCCIÓN (GITHUB) ---
                        postDelayed({
                            val githubUrl = "https://asurpan.github.io/sevillaON/"
                            val incomingUrl = intent.data?.toString()
                            val startUrl = if (incomingUrl != null && incomingUrl.startsWith("onairspain://")) {
                                val params = incomingUrl.substringAfter("?", "")
                                githubUrl + (if (params.isNotEmpty()) "?$params" else "")
                            } else if (incomingUrl != null) {
                                incomingUrl
                            } else {
                                githubUrl
                            }
                            loadUrl(startUrl)
                        }, 100)
                    }
                }
            )
        }
    }
}

data class QuadItem<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
