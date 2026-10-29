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
import androidx.compose.foundation.layout.fillMaxWidth
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
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId == "on_air_tts") {
                            runOnUiThread {
                                webViewInstance?.evaluateJavascript("if (window.dispatch_control_active) { window.dispatch_control_active('BASE LOCAL', true); }", null)
                            }
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "on_air_tts") {
                            runOnUiThread {
                                webViewInstance?.evaluateJavascript("if (window.dispatch_control_active) { window.dispatch_control_active(null, false); }", null)
                            }
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        if (utteranceId == "on_air_tts") {
                            runOnUiThread {
                                webViewInstance?.evaluateJavascript("if (window.dispatch_control_active) { window.dispatch_control_active(null, false); }", null)
                            }
                        }
                    }
                })
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
    // =========================================================================
    // 🔒 HARD-LOCK: BANDEROLA ADMOB OFICIAL - MÓDULO SELLADO (NIVEL 0)
    // PROHIBIDA CUALQUIER MODIFICACIÓN SIN PERMISO EXPLÍCITO DEL USUARIO
    // ID ANUNCIO: ca-app-pub-7866520163126353/3425068246
    // =========================================================================
    private var adView: AdView? = null

    fun showAdBanner(visible: Boolean) {
        runOnUiThread {
            adView?.let {
                it.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
                if (visible) {
                    it.loadAd(AdRequest.Builder().build())
                }
            }
        }
    }

    fun launchForegroundRadioService() {
        runOnUiThread {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, RadioService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }
            } catch (e: Exception) {}
        }
    }
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
                "https://asurpan.github.io/sevillaON/?native=true" + (if (params.isNotEmpty()) "&$params" else "")
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
    private fun registerSensorsIfNecessary() {
        sensorManager?.let { sm ->
            val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            val accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val pressSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE)
            sm.unregisterListener(sensorListener)
            sm.registerListener(sensorListener, magSensor, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(sensorListener, pressSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensors() {
        try {
            sensorManager?.unregisterListener(sensorListener)
        } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        unregisterSensors()
    }

    override fun onResume() {
        super.onResume()
        registerSensorsIfNecessary()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensors()
        wifiScanReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
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

        // 💰 ADMOB INIT: Inicializar el sistema de anuncios al arrancar y mostrar el banner
        MobileAds.initialize(this) {
            showAdBanner(true)
        }

        // 🧠 IA TÁCTICA: Configuración de claves seguras desde BuildConfig (local.properties)
        try {
            val geminiKeysRaw = com.sagon.on.BuildConfig.GEMINI_API_KEYS
            if (geminiKeysRaw.isNotBlank()) {
                val keysList = geminiKeysRaw.split(",").map { it.trim() }
                GeminiRadioService.configureKeys(keysList)
            }
        } catch (e: Exception) {}

        // 🛡️ GUARDIÁN DE SEGUNDO PLANO Y REPRODUCTOR MULTIMEDIA PERMANENTE
        try {
            launchForegroundRadioService()
        } catch (e: Exception) {}

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
        // 🔒 HARD-LOCK: ARRANQUE LIMPIO Y DE MÁXIMA CONFIANZA
        // CERO PERMISOS AL INICIAR LA APP - SOLO PERMISOS CONTEXTUALES TRAS INTERACCIÓN
        // =======================================================
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
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(Modifier)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                webViewInstance = this
                        
                        // --- 🛡️ BLINDAJE VISUAL: FONDO DE SEGURIDAD (ANTI-PANTALLA BLANCA) ---
                        setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                        
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // --- 🛡️ MEJORA DE FOCO (TECLADO Y NAVEGACIÓN ATRÁS SISTEMA) ---
                        isFocusable = true
                        isFocusableInTouchMode = true

                        setOnKeyListener { _, keyCode, keyEvent ->
                            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                                evaluateJavascript("if(window.trigger_back) window.trigger_back();", null)
                                true
                            } else false
                        }

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
                                speak(text, rate, pitch, 0.60f)
                            }

                            @JavascriptInterface
                            fun speak(text: String, rate: Float, pitch: Float, volume: Float) {
                                if (isTtsReady && tts != null) {
                                    tts?.setSpeechRate(rate)
                                    tts?.setPitch(pitch)
                                    val params = Bundle()
                                    params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
                                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "on_air_tts")
                                }
                            }

                            @JavascriptInterface
                            fun stopSpeak() {
                                runOnUiThread {
                                    try {
                                        if (tts != null) {
                                            tts?.stop()
                                        }
                                        webViewInstance?.evaluateJavascript("if (window.dispatch_control_active) { window.dispatch_control_active(null, false); }", null)
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun vibratePtt() {
                                runOnUiThread {
                                    try {
                                        webViewInstance?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                        webViewInstance?.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)

                                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                        if (vibrator.hasVibrator()) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK))
                                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                            } else {
                                                @Suppress("DEPRECATION")
                                                vibrator.vibrate(50)
                                            }
                                        }
                                    } catch (e: Exception) {}
                                }
                            }

                            @JavascriptInterface
                            fun playUiSound(type: String) {
                                post {
                                    try {
                                        if (type == "incoming" || type == "alert") {
                                            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80)
                                            toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
                                        } else if (type == "switch" || type == "click") {
                                            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 40)
                                            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
                                        }
                                    } catch (e: Exception) {}
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
                            fun requestMicPermission() {
                                runOnUiThread {
                                    try {
                                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) 
                                            != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            launchForegroundRadioService()
                                        }
                                    } catch (e: Exception) {}
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

                                    val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                                    val notification = androidx.core.app.NotificationCompat.Builder(this@MainActivity, channelId)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                                        .setSound(defaultSoundUri)
                                        .setVibrate(longArrayOf(0, 150, 100, 150))
                                        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
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
                                    val topic = getDailyDebateTopic(city)
                                    val cleanCity = city.split("-")[0].uppercase()
                                    val subText = if (subtone != null && subtone != "0000") " | 🔐 *$subtone*" else ""

                                    val shareText = if (city == "RADAR") {
                                        "📡 ¡Vigilancia Hertz Activa! *ON AIR SPAIN*\nAnalizando presencia biológica... 🧬\n\nhttps://asurpan.github.io/sevillaON/?v=58"
                                    } else {
                                        val shortTopic = if (topic.length > 70) topic.take(67) + "..." else topic
                                        "🎙️ *DEBATE EN VIVO — $cleanCity*\n\n" +
                                        "💬 \"$shortTopic\"\n\n" +
                                        "https://asurpan.github.io/sevillaON/?city=$city&channel=$channel&v=58"
                                    }

                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir Debate de Radio")
                                    context.startActivity(shareIntent)
                                }
                            }

                            @JavascriptInterface
                            fun installApp() {
                                Thread {
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
                                }.start()
                            }

                            @JavascriptInterface
                            fun isBatteryOptimized(): Boolean {
                                val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                                return !pm.isIgnoringBatteryOptimizations(packageName)
                            }

                            @JavascriptInterface
                            fun getBatteryCheckStatus(): String {
                                val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                                val isIgnoringBattery = pm.isIgnoringBatteryOptimizations(packageName)
                                val notifsEnabled = androidx.core.app.NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()
                                val manufacturer = android.os.Build.MANUFACTURER.lowercase(java.util.Locale.ROOT)
                                val brand = android.os.Build.BRAND.lowercase(java.util.Locale.ROOT)
                                val isXiaomiOrCustom = manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ||
                                                       manufacturer.contains("samsung") || manufacturer.contains("huawei") ||
                                                       brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
                                return "{\"isIgnoringBattery\":$isIgnoringBattery,\"notifsEnabled\":$notifsEnabled,\"isXiaomiOrCustom\":$isXiaomiOrCustom}"
                            }

                            @JavascriptInterface
                            fun openNotificationSettings() {
                                post {
                                    try {
                                        val intent = android.content.Intent().apply {
                                            action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                                        }
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = android.net.Uri.parse("package:$packageName")
                                            }
                                            startActivity(intent)
                                        } catch (e2: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun openAutoStartSettings() {
                                post {
                                    try {
                                        val xiaomiIntent = android.content.Intent().apply {
                                            component = android.content.ComponentName(
                                                "com.miui.securitycenter",
                                                "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                            )
                                        }
                                        startActivity(xiaomiIntent)
                                    } catch (e: Exception) {
                                        try {
                                            val appSettingsIntent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = android.net.Uri.parse("package:$packageName")
                                            }
                                            startActivity(appSettingsIntent)
                                        } catch (e2: Exception) {}
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun requestIgnoreBatteryOptimizations() {
                                post {
                                    try {
                                        // 🛡️ Abrir exclusivamente la solicitud de Batería Sin Restricciones (Doze Mode)
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = android.net.Uri.parse("package:$packageName")
                                        }
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            startActivity(intent)
                                        } catch (e2: Exception) {
                                            try {
                                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = android.net.Uri.parse("package:$packageName")
                                                }
                                                startActivity(intent)
                                            } catch (e3: Exception) {}
                                        }
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
                                showAdBanner(visible)
                            }

                            @JavascriptInterface
                            fun showBannerSombreros(visible: Boolean) {
                                showAdBanner(visible)
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
                                runOnUiThread {
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
                            val githubUrl = "https://asurpan.github.io/sevillaON/?native=true"
                            val incomingUrl = intent.data?.toString()
                            val startUrl = if (incomingUrl != null && incomingUrl.startsWith("onairspain://")) {
                                val params = incomingUrl.substringAfter("?", "")
                                githubUrl + (if (params.isNotEmpty()) "&$params" else "")
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

        // =========================================================================
        // 🔒 HARD-LOCK: BANNER ADMOB INFERIOR PERMANENTE - MÓDULO SELLADO (NIVEL 0)
        // PROHIBIDA CUALQUIER MODIFICACIÓN SIN PERMISO EXPLÍCITO DEL USUARIO
        // =========================================================================
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-7866520163126353/3425068246"
                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            update = { view ->
                view.visibility = android.view.View.VISIBLE
            }
        )
    }
}
}
}
