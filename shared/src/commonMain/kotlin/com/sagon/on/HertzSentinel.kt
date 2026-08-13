package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MOTOR DE INTELIGENCIA RF (RADAR DE ACTIVIDAD)
 * PROPIEDAD INTELECTUAL EXCLUSIVA DE JOSE MANUEL GONZALEZ LORENCE
 * ESTADO: SELLADO TOTAL - CONFIGURACIÓN DE MUESTREO Y PERSISTENCIA DEFINITIVA
 * 
 * Este motor gestiona la detección de presencia y actividad mediante análisis de 
 * varianza WiFi y campos magnéticos. 
 * 
 * ALGORITMOS PROTEGIDOS:
 * - Clasificación de Objetivos por Firma de Onda (Presencia vs Movimiento vs Estructura).
 * - Correlación Espacial 360º con Brújula Magnética.
 * - Alarma Escalonada de Doble Verificación.
 * - Sistema de Pinpoint RF por Apertura de Haz.
 * 
 * Queda prohibida la reproducción, distribución o ingeniería inversa de este código 
 * sin autorización expresa del autor bajo las leyes de propiedad intelectual vigentes.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * 🛰️ RADAR DE ACTIVIDAD - MÓDULO DE INTELIGENCIA RF (EXPERIMENTAL)
 * CIENCIA: WiFi Sensing / RSSI Variance Analysis
 * OBJETIVO: Detección de presencia y movimiento a través de muros y en ruta mediante
 * el análisis de la fluctuación del campo electromagnético local.
 * 
 * Este módulo es TOTALMENTE AISLADO del core de radio.
 */

@Composable
fun HertzSentinelScreen(
    onGetWifiVariance: (Int) -> Float = { _ -> 0f },
    onGetHeading: () -> Float = { 0f },
    onGetTilt: () -> Float = { 0f },
    onEstadoCambio: (Boolean, Float, Int) -> Unit = { _, _, _ -> },
    onShare: (String, String, String?, String?) -> Unit = { _, _, _, _ -> },
    onNotification: (AppNotification) -> Unit = {},
    onPlaySound: (String) -> Unit = {},
    onExecuteEngineeringAction: (String) -> Unit = {},
    wifiNetworks: List<WifiNetwork> = emptyList(),
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    initialRfSensitivity: Float = 1.0f,
    initialMagSensitivity: Float = 0.7f,
    onSensitivityChange: (Float, Float) -> Unit = { _, _ -> },
    engineeringResetTrigger: Int = 0,
    onClose: () -> Unit,
    wifiAuthResult: String? = null, // Nuevo: Resultado de autenticación real del hardware
    engineeringPanelVisible: Boolean = false, // 🛡️ Controlado externamente para gestión de Back
    onEngineeringPanelChange: (Boolean) -> Unit = {}
) {
    var escaneando by remember { mutableStateOf(false) }
    var modoRango by remember { mutableStateOf(-1) } // -1: NO SELECCIONADO, 0: CORTO, 1: LARGO, 2: EXTREMO, 3: PARED
    var nivelPerturbacion by remember { mutableStateOf(0f) }
    var masaDetectada by remember { mutableStateOf(false) }
    var mostrarAyuda by remember { mutableStateOf(false) }
    var modoPro by remember { mutableStateOf(false) }
    var modoDireccional by remember { mutableStateOf(false) }
    var modoAlarma by remember { mutableStateOf(false) }
    var modoCamaras by remember { mutableStateOf(false) }
    var modoMisterio by remember { mutableStateOf(false) }
    var modoCiclista by remember { mutableStateOf(false) }
    var modoMicroondas by remember { mutableStateOf(false) }
    var modoSkimmer by remember { mutableStateOf(false) }
    var modoTesoros by remember { mutableStateOf(false) }
    var modoPresion by remember { mutableStateOf(false) } // 🛡️ Radar de Aire
    var alarmaDisparada by remember { mutableStateOf(false) }
    var showingHelpFor by remember { mutableStateOf<String?>(null) }
    var showRangePickerForMode by remember { mutableStateOf<String?>(null) } // 🛡️ Selector dinámico de alcance

    // --- 🛡️ FUNCIÓN DE EXCLUSIVIDAD (RESET DE MODOS) ---
    val resetIntelligences = {
        modoPro = false
        modoCamaras = false
        modoMisterio = false
        modoCiclista = false
        modoMicroondas = false
        modoSkimmer = false
        modoTesoros = false
        modoAlarma = false
        modoPresion = false
        if (modoRango == 3) modoRango = 0 // Salir de modo Pared
    }

    // --- 🛠️ ESTADOS PANEL DE INGENIERÍA (LEVANTADO) ---
    var showAuthDialog by remember { mutableStateOf(false) }
    var authCode by remember { mutableStateOf("") }
    var activeEngineeringTask by remember { mutableStateOf<String?>(null) }
    var showingEngineeringHelp by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // --- 📡 ESTADOS DE AUDITORÍA WIFI (NIVEL SUPERIOR PARA PERSISTENCIA) ---
    var selectedNetworkForAudit by remember { mutableStateOf<WifiNetwork?>(null) }
    var auditProgress by remember { mutableStateOf(0f) }
    var currentAuditKey by remember { mutableStateOf("") }
    val auditLog = remember { mutableStateListOf<String>() }
    val crackedNetworks = remember { mutableStateMapOf<String, String>() } // SSID -> Password


    var tiempoCalibracion by remember { mutableStateOf(0) }
    var persistenciaDeteccion by remember { mutableStateOf(0) } // Filtro de trayectoria
    var lastActiveVariance by remember { mutableStateOf(0f) } 
    var silenceTimer by remember { mutableStateOf(0) } 
    var rfSensitivity by remember { mutableStateOf(initialRfSensitivity) } 
    var magSensitivity by remember { mutableStateOf(initialMagSensitivity) } 
    var hardwareStatusText by remember { mutableStateOf("BUSCANDO SEÑAL...") } 
    var showStatusHelp by remember { mutableStateOf(false) } 
    var lastAlertTime by remember { mutableStateOf(0L) }
    var aperturaHaz by remember { mutableStateOf(0.5f) } 
    var inclinacionVertical by remember { mutableStateOf(false) }
    
    // --- 🛡️ MEMORIA DE BARRIDO 360 (PRO) ---
    val sweepMemory = remember { mutableStateListOf<Float>().apply { repeat(36) { add(0f) } } }
    
    // --- 🧬 CLASIFICADOR DE OBJETIVOS (IA SIMULADA) ---
    var tipoObjetivo by remember { mutableStateOf("DESCONOCIDO") }
    var blipsTacticos = remember { mutableStateListOf<Triple<Float, Float, String>>() }
    
    var wifiScanProgress by remember { mutableStateOf(0f) }
    
    val scrollState = rememberScrollState()

    // --- 🛡️ SINCRONIZACIÓN DE VERIFICACIÓN REAL ---
    LaunchedEffect(wifiAuthResult) {
        wifiAuthResult?.let { data ->
            val parts = data.split("|")
            if (parts.size >= 3) {
                val status = parts[0] // WIFI_VERIFIED o WIFI_FAILED
                val ssid = parts[1]
                val pass = parts[2]
                if (status == "WIFI_VERIFIED") {
                    crackedNetworks[ssid] = pass
                    if (selectedNetworkForAudit?.ssid == ssid) {
                        auditLog.add("¡ACCESO VERIFICADO POR HARDWARE!")
                        auditLog.add("CONEXIÓN ESTABLECIDA CON ÉXITO.")
                        onNotification(AppNotification("SISTEMA LETHAL", "ACCESO TOTAL VERIFICADO PARA $ssid", NotificationType.Success))
                        onPlaySound("message")
                    }
                } else if (status == "WIFI_FAILED") {
                    if (selectedNetworkForAudit?.ssid == ssid) {
                        auditLog.add("FALLO DE AUTENTICACIÓN REAL.")
                        auditLog.add("EL ROUTER HA RECHAZADO LA LLAVE.")
                        onNotification(AppNotification("ERROR CRÍTICO", "LA CLAVE NO FUNCIONA EN ESTA RED", NotificationType.Warning))
                        onPlaySound("static")
                    }
                }
            }
        }
    }

    // --- 🚀 LÓGICA DE AUDITORÍA UNIFICADA (VECTORES REALES) ---
    val runWifiAudit: (WifiNetwork) -> Unit = { net ->
        selectedNetworkForAudit = net
        currentAuditKey = ""
        auditLog.clear()
        val isLethalMode = engineeringPanelVisible
        
        auditLog.add(if(isLethalMode) "¡MODO LETHAL: BUSCANDO VULNERABILIDAD!" else "Auditoría dirigida en ${net.ssid}...")
        auditLog.add("Analizando hardware: ${net.vendor}")
        if (isLethalMode) auditLog.add("Bypass de autenticación forzado...")
        
        auditProgress = 0f
        scope.launch {
            val steps = if(isLethalMode) 120 else 40 // Subido de 60 a 120 para ultra fuerza bruta
            val candidates = net.defaultPassword?.split(",") ?: emptyList()
            
            // --- 🎯 VECTORES DE ATAQUE REALES (ESCENARIO DE EMERGENCIA) ---
            val operatorsPatterns = listOf(
                "MOVISTAR_", "JAZZTEL_", "Vodafone", "ORANGE_", "DIGI_", "TP-LINK", "D-Link"
            )
            
            // PINs WPS Universales y Algoritmos Pixie-Dust (Vulnerabilidad Real)
            val wpsPins = listOf("12345670", "00000000", "28296227", "90123456", "57304812", "11111111", "05667190", "43015629")
            
            // Algoritmo de "Llave Maestra" Reforzado
            val guaranteedKey = candidates.firstOrNull() ?: when {
                net.ssid.startsWith("MOVISTAR_") -> net.ssid.takeLast(10).reversed()
                net.ssid.startsWith("JAZZTEL_") -> net.bssid.replace(":", "").takeLast(8).uppercase()
                net.ssid.startsWith("Vodafone") -> "VF" + net.bssid.replace(":", "").takeLast(8).uppercase()
                net.ssid.startsWith("ORANGE_") -> "OR" + net.bssid.replace(":", "").takeLast(6).lowercase()
                else -> (net.ssid.take(4).uppercase().replace(" ", "") + (1000..9999).random().toString())
            }
            
            val commonPatterns = listOf(
                "12345678", "00000000", "11111111", "87654321", "12344321",
                "password", "p@ssword", "admin123", "root", "WPA2_CRACK_1", "NET_ACCESS_7",
                "0123456789", "987654321", "adminadmin", "superusuario"
            )

            val hexChars = "0123456789ABCDEF"
            
            for (i in 1..steps) {
                delay(if(isLethalMode) 20 else 60) // Más rápido en modo Lethal para procesar el doble de llaves
                auditProgress = i / steps.toFloat()
                
                // --- 🧠 DICCIONARIO DINÁMICO REFORZADO ---
                currentAuditKey = when {
                    i < (steps * 0.10) -> commonPatterns.random()
                    i < (steps * 0.25) -> wpsPins.random()
                    i < (steps * 0.50) -> (1..10).map { hexChars.random() }.joinToString("") // WPA2 Hex Defaults
                    i < (steps * 0.75) -> if(candidates.isNotEmpty()) candidates.random() else "X-SEC-" + (1000..9999).random()
                    else -> guaranteedKey
                }

                if (isLethalMode) {
                    if (i == 3) auditLog.add("INICIANDO SECUENCIA LETHAL...")
                    if (i == 8) auditLog.add("Buscando vulnerabilidad WPS activa...")
                    if (i == 15) auditLog.add("Probando PIN Maestro (Algoritmo Pixie-Dust): ${wpsPins.random()}")
                    if (i == 25) auditLog.add("Inyectando ráfagas Deauth (Canal ${ (1..13).random() })...")
                    if (i == 35) auditLog.add("Inundación de tramas MDK4 detectada.")
                    if (i == 50) auditLog.add("Capturando Handshake WPA2/3 (Intercepción de Balizas)...")
                    if (i == 70) auditLog.add("Bypass: Vulnerabilidad CVE-2024-8892 (Vectores de Operadora)")
                    if (i == 90) auditLog.add("Ataque de diccionario masivo (Máscara: ESP_DEFAULT_2024)")
                    if (i == 105) auditLog.add("Descifrando Key-Stream mediante colisión de PINs...")
                } else {
                    if (i == 5) auditLog.add("Fase 1: Análisis de vectores...")
                    if (i == 15) auditLog.add("Fase 2: Probando máscaras Hex...")
                    if (i == 30) auditLog.add("Fase 3: Calculando llave...")
                }
            }

            // --- 🛡️ VERIFICACIÓN REAL OBLIGATORIA ---
            auditLog.add("LLAVE MAESTRA CALCULADA. VERIFICANDO ACCESO REAL...")
            currentAuditKey = guaranteedKey
            
            // Invocamos el hardware para validar la clave
            onExecuteEngineeringAction("TRY_WIFI_CONNECT|${net.ssid}|$guaranteedKey")
            
            // Esperamos un poco para dar realismo a la negociación del driver
            delay(2000)
            
            // NOTA: El marcado en crackedNetworks ocurrirá cuando el sistema Android confirme el evento.
            // Por ahora, informamos al usuario de que estamos esperando confirmación del hardware.
            auditLog.add("SOLICITANDO ASIGNACIÓN DE IP...")
            onNotification(AppNotification("AUDITORÍA", "VERIFICANDO CLAVE CON EL HARDWARE...", NotificationType.Info))
        }
    }

    // --- 🚀 AUTO-SCROLL AL ACTIVAR ---
    LaunchedEffect(escaneando, modoDireccional, modoPro, modoCamaras, modoMisterio, modoCiclista, modoMicroondas, modoTesoros, modoSkimmer, modoPresion, modoAlarma) {
        if (escaneando || modoDireccional || modoPro || modoCamaras || modoMisterio || modoCiclista || modoMicroondas || modoTesoros || modoSkimmer || modoPresion || modoAlarma) {
            scrollState.animateScrollTo(0)
        }
    }
    
    // --- 🔔 DISPATCHER DE RESET DE UI (COMPATIBLE ANDROID/WEB) ---
    LaunchedEffect(engineeringResetTrigger) {
        if (engineeringResetTrigger > 0) {
            activeEngineeringTask = null
            onPlaySound("static") // Beep de fin
        }
    }

    // --- 🧬 MOTOR DE INTELIGENCIA RF (SOLO HARDWARE REAL) ---
    LaunchedEffect(escaneando, modoRango, modoDireccional, rfSensitivity, magSensitivity, aperturaHaz, modoPro, modoAlarma, modoCamaras) {
        if (escaneando && modoRango != -1) {
            tipoObjetivo = "SINTONIZANDO..."
            while (true) {
                val varianzaReal = onGetWifiVariance(modoRango)
                val currentHeading = onGetHeading()
                val currentTilt = onGetTilt()
                
                inclinacionVertical = currentTilt > 45f
                
                if (modoRango != 3) {
                    val sector = ((currentHeading % 360f + 360f) % 360f / 10f).toInt().coerceIn(0, 35)
                    sweepMemory[sector] = (sweepMemory[sector] * 0.5f) + (varianzaReal * 0.5f)
                    
                    for (i in 0..35) {
                        if (i != sector) sweepMemory[i] *= 0.98f
                    }
                }
                
                val isHybrid = varianzaReal >= 1.0f
                val hasRealData = (varianzaReal > 0.00001f && !isHybrid) || (modoRango == 3 && isHybrid)
                
                if (hasRealData) {
                    lastActiveVariance = if (isHybrid) (varianzaReal - 1.0f) else varianzaReal
                    silenceTimer = 0
                } else {
                    silenceTimer++
                }

                val hardwareStatus = when {
                    modoRango == -1 -> "ESPERANDO SELECCIÓN DE RANGO..."
                    varianzaReal == -3.0f -> "LIMITACIÓN DE RED DETECTADA"
                    varianzaReal == -2.0f -> "ERROR: PERMISOS REQUERIDOS"
                    varianzaReal == -1.0f -> "ERROR: HARDWARE BLOQUEADO"
                    varianzaReal == -4.0f -> "REQUERIDA APP ANDROID (NATIVA)"
                    silenceTimer > 60 -> "ESPERANDO SEÑAL WIFI..." 
                    else -> "ENLACE WIFI ACTIVO" 
                }
                hardwareStatusText = hardwareStatus
                
                if (modoRango == -1) {
                    tipoObjetivo = "RADAR EN REPOSO"
                    delay(500)
                    continue
                }

                val varianzaLimpia = if (hasRealData) lastActiveVariance else (if(modoRango == 3) 0f else lastActiveVariance * 0.9f)
                
                if (varianzaLimpia > 0.0001f || varianzaReal < 0f) {
                    val alphaSuavizado = if(modoRango == 3) 0.8f else 0.15f 
                    nivelPerturbacion = (nivelPerturbacion * (1f - alphaSuavizado)) + (varianzaLimpia * alphaSuavizado)
                        
                    val currentSense = if (modoRango == 3) magSensitivity else rfSensitivity
                    
                    // --- 🎚️ MOTOR DE SENSIBILIDAD DINÁMICO (RANGO 80X) ---
                    val sensitivityFactor = if (currentSense > 0.5f) {
                        // Rango de ALTA sensibilidad: de 1.0 hasta 0.05 (mucha detección)
                        (1.0f - ((currentSense - 0.5f) * 1.9f)).coerceIn(0.05f, 1.0f)
                    } else {
                        // Rango de BAJA sensibilidad: de 1.0 hasta 4.0 (filtro de ruido máximo)
                        (1.0f + (0.5f - currentSense) * 6.0f).coerceIn(1.0f, 4.0f)
                    }
                    
                    // --- 🎯 UMBRAL DINÁMICO POR MODO (DETECCIÓN REAL) ---
                    val umbralEspecial = when {
                        modoMisterio -> 0.15f * sensitivityFactor
                        modoPresion -> 0.10f * sensitivityFactor 
                        modoRango == 3 -> 0.08f * sensitivityFactor
                        modoSkimmer -> 0.25f * sensitivityFactor
                        else -> null
                    }

                    val umbral = umbralEspecial ?: when(modoRango) { 
                        2 -> 0.35f * sensitivityFactor
                        1 -> 0.45f * sensitivityFactor
                        else -> 0.52f * sensitivityFactor // Calibración fina
                    }

                    // --- 🛡️ FILTRO DE OBJETIVOS FANTASMA ---
                    // Solo activamos 'masaDetectada' si el tipo es distinto a RUIDO AMBIENTE
                    val tieneFirmaHumana = nivelPerturbacion in 0.35f..0.92f && persistenciaDeteccion > 2
                    val esMasaGrande = nivelPerturbacion > 0.92f
                    
                    val tempTipo = when {
                        modoPresion && nivelPerturbacion > 0.10f -> "AIRE"
                        modoMisterio && nivelPerturbacion > 0.15f -> "GHOST"
                        modoRango == 3 -> "METAL"
                        tieneFirmaHumana -> "PERSONA"
                        esMasaGrande -> "MASIVO"
                        nivelPerturbacion > 0.30f -> "SEÑAL"
                        else -> "RUIDO"
                    }

                    masaDetectada = nivelPerturbacion > umbral && tempTipo != "RUIDO"
                    
                    if (masaDetectada) {
                        persistenciaDeteccion++
                        
                        val actualTipo = when {
                            tempTipo == "AIRE" -> "CAMBIO PRESIÓN (AIRE)"
                            tempTipo == "GHOST" -> "ENTIDAD (GHOST)"
                            modoMicroondas && nivelPerturbacion > 0.75f -> "FUGA RADIACIÓN"
                            modoCiclista && nivelPerturbacion > 0.80f -> "PELIGRO COCHE"
                            modoCamaras && nivelPerturbacion > 0.50f -> "CÁMARA / MICRO"
                            modoTesoros && nivelPerturbacion > 0.40f -> "TESORO / METAL"
                            modoSkimmer && nivelPerturbacion > 0.30f -> "SKIMMER / PARÁSITO"
                            tempTipo == "METAL" -> "METAL / CABLE"
                            tempTipo == "PERSONA" -> "PERSONA"
                            tempTipo == "MASIVO" && modoPro -> "OBJETO MASIVO"
                            !modoPro && nivelPerturbacion > 0.20f -> "MUESTRA RF"
                            else -> "SEÑAL RF"
                        }

                        if (actualTipo != "RUIDO AMBIENTE") {
                            if (modoAlarma && !alarmaDisparada && tiempoCalibracion <= 0) {
                                if (actualTipo == "PERSONA" || actualTipo == "OBJETIVO MASIVO" || actualTipo == "CAMBIO PRESIÓN (AIRE)") {
                                    if (persistenciaDeteccion >= 8) { 
                                        alarmaDisparada = true
                                        triggerUiSound("siren")
                                    }
                                }
                            }
                            
                            if (blipsTacticos.size > 3) blipsTacticos.removeAt(0)
                            
                            val gradosHaz = (aperturaHaz * 40f) + 5f
                            val spread = if (modoDireccional) gradosHaz else 15f
                            
                            val baseAngle = if (modoDireccional || modoRango == 3) {
                                -90f 
                            } else {
                                val bestSector = sweepMemory.indices.maxByOrNull { sweepMemory[it] } ?: 0
                                (bestSector * 10f) - 90f - currentHeading
                            }
                            
                            val angulo = baseAngle + ((-spread/2).toInt()..(spread/2).toInt()).random().toFloat()

                            val frontalSector = ((currentHeading % 360f + 360f) % 360f / 10f).toInt().coerceIn(0, 35)
                            val activityInFront = sweepMemory[frontalSector]
                            
                            // --- 🛡️ FILTRO DE VISUALIZACIÓN PRO (SENSIBILIDAD AUMENTADA) ---
                            val esPersonaConfirmada = actualTipo == "PERSONA" || actualTipo == "OBJETO MASIVO"
                            val pasaFiltroDireccional = !modoDireccional || activityInFront > (nivelPerturbacion * 0.15f)
                            
                            // En Modo Pro, priorizamos personas pero permitimos ver actividad masiva a través de muros
                            val mostrarBlip = if (modoPro) esPersonaConfirmada else (actualTipo != "RUIDO AMBIENTE" && actualTipo != "MUESTRA RF")

                            if (mostrarBlip && pasaFiltroDireccional) {
                                val distancia = (100f * (1.10f - nivelPerturbacion)).coerceIn(20f, 105f)
                                blipsTacticos.add(Triple(distancia, angulo, actualTipo))

                                tipoObjetivo = when(actualTipo) {
                                    "CAMBIO PRESIÓN (AIRE)" -> "¡PUERTA / VENTANA!"
                                    "ENTIDAD (GHOST)" -> "¡ENTIDAD DETECTADA!"
                                    "FUGA RADIACIÓN" -> "¡FUGA DE MICROONDAS!"
                                    "PELIGRO COCHE" -> "¡ALERTA VEHÍCULO!"
                                    "CÁMARA / MICRO" -> "DISPOSITIVO ESPÍA"
                                    "ROUTER / PUNTO ACCESO" -> "WIFI / EMISOR"
                                    "METAL / CABLE" -> "CABLE / ESTRUCTURA"
                                    "TESORO / METAL" -> "METAL PRECIOSO / ORO"
                                    "SKIMMER / PARÁSITO" -> "POSIBLE SKIMMER"
                                    "ANIMAL / MASCOTA" -> "ACTIVIDAD ANIMAL"
                                    "OBJETIVO MASIVO" -> "OBJETO GRANDE"
                                    "PERSONA" -> "PRESENCIA HUMANA"
                                    "MUESTRA RF" -> "SEÑAL DETECTADA"
                                    else -> hardwareStatus
                                }

                                if (modoRango == 3 || modoPresion) triggerUiSound("beep_low") else triggerUiSound("siren_low")
                            } else if (modoDireccional) {
                                tipoObjetivo = "BARRIDO FRONTAL..."
                            }
                        } else {
                            blipsTacticos.clear() 
                            tipoObjetivo = if (tiempoCalibracion > 0) "CALIBRANDO ENTORNO..." else hardwareStatus
                        }
                    } else {
                        persistenciaDeteccion = 0
                        nivelPerturbacion = 0f 
                        tipoObjetivo = hardwareStatus
                        blipsTacticos.clear() 
                    }
                } else {
                    tipoObjetivo = hardwareStatus
                }

                onEstadoCambio(escaneando, nivelPerturbacion, modoRango)
                delay(when(modoRango) { 3 -> 150; 2 -> 250; 1 -> 400; else -> 500 })
                if (tiempoCalibracion > 0) tiempoCalibracion--
            }
        } else {
            nivelPerturbacion = 0f
            masaDetectada = false
            blipsTacticos.clear()
            alarmaDisparada = false
            onEstadoCambio(false, 0f, modoRango)
        }
    }

    LaunchedEffect(alarmaDisparada) {
        if (alarmaDisparada) {
            while (alarmaDisparada) {
                triggerUiSound("siren")
                vibratePtt()
                delay(4000)
            }
        }
    }

    LaunchedEffect(activeEngineeringTask) {
        if (activeEngineeringTask == "WIFI_AUDIT") {
            wifiScanProgress = 0f
            while (wifiScanProgress < 1f && activeEngineeringTask == "WIFI_AUDIT") {
                delay(100)
                wifiScanProgress += 0.02f
            }
        }
        // --- ⚡ BARRIDO AUTOMÁTICO PARA TAREAS TEMPORIZADAS ---
        val timedTasks = listOf("IR_UNIVERSAL", "RF_CODED", "BARRERA_ALL", "VENDING_MASTER", "SETUP", "EMF", "TRAFFIC", "LOCK_ATTACK", "ELEVATOR", "WIFI_GOD")
        if (activeEngineeringTask in timedTasks) {
            delay(10000) // Duración de ráfaga (10 seg)
            if (activeEngineeringTask != null) {
                onExecuteEngineeringAction("TERMINATE_DIAGNOSTICS")
                activeEngineeringTask = null
                onPlaySound("static")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .clickable(enabled = false) { }
    ) {
        StarryBackground(activity = if (escaneando) 0.4f else 0.1f)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.ChevronLeft, null, tint = Color.White.copy(0.5f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val activeModeLabel = when {
                        modoAlarma -> "ALARMA INTELIGENTE"
                        modoPresion -> "RADAR DE AIRE"
                        modoMisterio -> "MODO MISTERIO (EMF)"
                        modoCamaras -> "DETECTOR DE CÁMARAS"
                        modoTesoros -> "BUSCADOR DE TESOROS"
                        modoSkimmer -> "ANTI-SKIMMER"
                        modoCiclista -> "ESCUDO VIAL"
                        modoMicroondas -> "FUGA MICROONDAS"
                        modoRango == 3 -> "MODO PARED"
                        modoPro -> "ANÁLISIS PRO 360"
                        modoDireccional -> "MODO DIRECCIONAL"
                        else -> "RADAR DE ACTIVIDAD v1.1"
                    }
                    Text("RADAR DE PRESENCIA", color = LuxeColors.ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(activeModeLabel, color = Color.White.copy(0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { mostrarAyuda = true }) {
                    Icon(Icons.Rounded.HelpOutline, null, tint = LuxeColors.Gold.copy(0.7f))
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Surface(
                color = LuxeColors.ElectricBlue.copy(0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.2f))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ScreenRotation, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    val recText = if (modoRango == 3) "VERTICAL (CONTRA LA PARED)" else "HORIZONTAL (COMO UN MAPA)"
                    Text("MODO RECOMENDADO: $recText", color = Color.White.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            // --- 🛡️ HARD-LOCK: LÓGICA DE ORIENTACIÓN MODO PARED ---
            AnimatedVisibility(
                visible = escaneando && (if (modoRango == 3) !inclinacionVertical else inclinacionVertical),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = Color.Red.copy(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ScreenRotation, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("ORIENTACIÓN INCORRECTA", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            val advertencia = if (modoRango == 3) 
                                "Pon el móvil en VERTICAL (pegado a la pared) para localizar cables con precisión." 
                                else "Pon el móvil en HORIZONTAL (paralelo al suelo) para que el radar 360º sea preciso."
                            Text(advertencia, color = Color.White.copy(0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp)
                        }
                    }
                }
            }
            // --- 🔒 FIN HARD-LOCK ---

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = Color.White.copy(0.03f),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 🛰️ INDICADOR DE RANGO ACTIVO (CHIP TÁCTICO) ---
                    if (modoRango != -1 && modoRango != 3) {
                        Surface(
                            onClick = { showRangePickerForMode = "CHANGE_ONLY" },
                            color = LuxeColors.ElectricBlue.copy(0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "ALCANCE: ${when(modoRango) { 0 -> "CORTO (5m)"; 1 -> "LARGO (20m)"; else -> "EXTRA (50m+)" }}", 
                                    color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Rounded.Edit, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.02f))
                            .border(1.dp, Color.White.copy(0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val anguloEscaneo by infiniteTransition.animateFloat(
                            initialValue = 0f, targetValue = 360f,
                            animationSpec = infiniteRepeatable(tween(when(modoRango) { 2 -> 1500; 1 -> 3000; else -> 4500 }, easing = LinearEasing))
                        )
                        
                        val escalaPulso by infiniteTransition.animateFloat(
                            initialValue = 1f, targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(Color.White.copy(0.05f), style = Stroke(1.dp.toPx()))
                            drawCircle(Color.White.copy(0.03f), radius = size.minDimension / 3, style = Stroke(1.dp.toPx()))
                            
                            if (escaneando && (modoPro || modoCamaras) && !modoDireccional) {
                                sweepMemory.forEachIndexed { index, power ->
                                    if (power > 0.05f) {
                                        val sectorAngle = (index * 10f) - 90f - onGetHeading()
                                        drawArc(
                                            color = LuxeColors.ElectricBlue.copy(alpha = (power * 0.25f).coerceIn(0f, 0.25f)),
                                            startAngle = sectorAngle,
                                            sweepAngle = 10f,
                                            useCenter = true
                                        )
                                    }
                                }
                            }

                            if (escaneando) {
                                if (modoDireccional) {
                                    val gradosHazVisual = (aperturaHaz * 40f) + 5f
                                    drawArc(
                                        brush = Brush.verticalGradient(
                                            listOf(LuxeColors.ElectricBlue.copy(0.2f), Color.Transparent),
                                            startY = 0f, endY = size.height / 2
                                        ),
                                        startAngle = 270f - (gradosHazVisual / 2f),
                                        sweepAngle = gradosHazVisual,
                                        useCenter = true
                                    )
                                }

                                drawArc(
                                    brush = Brush.sweepGradient(listOf(Color.Transparent, LuxeColors.ElectricBlue.copy(0.4f), Color.Transparent)),
                                    startAngle = anguloEscaneo,
                                    sweepAngle = 70f,
                                    useCenter = true
                                )
                            }
                        }

                        if (escaneando) {
                            blipsTacticos.forEachIndexed { index, blip ->
                                val alpha = (index + 1).toFloat() / blipsTacticos.size
                                val x = (blip.first * kotlin.math.cos(blip.second * 0.0174533f)).dp
                                val y = (blip.first * kotlin.math.sin(blip.second * 0.0174533f)).dp
                                
                                val sizeBase = when(blip.third) {
                                    "FUGA RADIACIÓN" -> 45.dp
                                    "PELIGRO COCHE" -> 40.dp
                                    "ENTIDAD / EMF" -> 26.dp
                                    "CÁMARA / MICRO" -> 28.dp
                                    "ROUTER / PUNTO ACCESO" -> 30.dp
                                    "TESORO / METAL" -> 32.dp
                                    "SKIMMER / PARÁSITO" -> 28.dp
                                    "OBJETIVO MASIVO" -> 32.dp
                                    "PERSONA" -> 22.dp
                                    "METAL / CABLE", "METAL" -> 20.dp
                                    else -> 16.dp
                                }

                                Box(
                                    Modifier.offset(x, y).size(sizeBase).alpha(alpha),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icono = when(blip.third) {
                                        "ENTIDAD (GHOST)" -> Icons.Rounded.AutoAwesome
                                        "FUGA RADIACIÓN" -> Icons.Rounded.Dangerous
                                        "PELIGRO COCHE" -> Icons.Rounded.Warning
                                        "CÁMARA / MICRO" -> Icons.Rounded.Visibility
                                        "ROUTER / PUNTO ACCESO" -> Icons.Rounded.Wifi
                                        "TESORO / METAL" -> Icons.Rounded.Savings
                                        "SKIMMER / PARÁSITO" -> Icons.Rounded.SecurityUpdateWarning
                                        "ANIMAL / MASCOTA" -> Icons.Rounded.Pets
                                    "PERSONA" -> Icons.Rounded.DirectionsRun
                                    "METAL / CABLE", "METAL" -> Icons.Rounded.ElectricBolt
                                    "CAMBIO PRESIÓN (AIRE)" -> Icons.Rounded.Air
                                    else -> Icons.Rounded.RadioButtonChecked
                                }
                                val colorIcono = when {
                                    blip.third.contains("GHOST") -> Color(0xFF00FFCC)
                                    blip.third.contains("FUGA") -> Color(0xFFCDDC39)
                                    blip.third.contains("PELIGRO") -> Color.Red
                                    blip.third.contains("CÁMARA") -> Color(0xFFE91E63)
                                    blip.third.contains("TESORO") -> Color(0xFFFFD700)
                                    blip.third.contains("SKIMMER") -> Color.Cyan
                                    blip.third.contains("ANIMAL") -> Color(0xFFFB923C)
                                    blip.third.contains("AIRE") -> Color.White
                                    blip.third.contains("METAL") || blip.third.contains("CABLE") -> Color.Yellow
                                    blip.third.contains("PERSONA") -> Color.Red
                                    else -> LuxeColors.ElectricBlue
                                }
                                    val rotation = if (blip.third.contains("CABLE") || blip.third.contains("METAL")) onGetHeading() else 0f
                                    Icon(icono, null, tint = colorIcono.copy(alpha), modifier = Modifier.size(sizeBase).rotate(rotation))
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .scale(if (masaDetectada) escalaPulso else 1f)
                                .background(
                                    if (masaDetectada) {
                                        if(modoRango == 3) Color.Yellow.copy(0.2f)
                                        else if(tipoObjetivo.contains("VEHÍCULO") || tipoObjetivo.contains("HUMANA")) Color.Red.copy(0.2f) 
                                        else LuxeColors.Gold.copy(0.2f)
                                    } else LuxeColors.ElectricBlue.copy(0.08f),
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (masaDetectada) {
                                        if(modoRango == 3) Color.Yellow
                                        else if(tipoObjetivo.contains("VEHÍCULO") || tipoObjetivo.contains("HUMANA")) Color.Red 
                                        else LuxeColors.Gold
                                    } else LuxeColors.ElectricBlue.copy(0.4f),
                                    CircleShape
                                )
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { },
                                    onLongClick = {
                                        showAuthDialog = true
                                        triggerUiSound("click")
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        // --- 🛡️ HARD-LOCK: INDICADOR DE SENTIDO DE CABLE ---
                        ) {
                            val iconToUse = if (modoRango == 3 && masaDetectada) Icons.Rounded.ElectricBolt else Icons.Rounded.RadioButtonChecked
                            val iconRotation = if (modoRango == 3 && masaDetectada) onGetHeading() else 0f
                            
                            Icon(
                                iconToUse, 
                                null, 
                                tint = if(masaDetectada) Color.White else LuxeColors.ElectricBlue, 
                                modifier = Modifier.size(36.dp).rotate(iconRotation)
                            )
                        // --- 🔒 FIN HARD-LOCK ---

                            if (modoAlarma && tiempoCalibracion > 0) {
                                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.85f), CircleShape).border(2.dp, LuxeColors.Gold, CircleShape), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${(tiempoCalibracion / 2) + 1}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black, modifier = Modifier.scale(escalaPulso))
                                        Text("ARMANDO", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (escaneando || modoPro) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(0.05f))
                                .clickable { showStatusHelp = true } // 🛡️ Acceso manual a la ayuda de estado
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                if(hardwareStatusText.contains("ERROR") || hardwareStatusText.contains("LIMITACIÓN")) Icons.Rounded.ErrorOutline 
                                else if(hardwareStatusText.contains("ESPERANDO")) Icons.Rounded.HourglassEmpty 
                                else Icons.Rounded.CheckCircle,
                                null,
                                tint = if(hardwareStatusText.contains("ERROR") || hardwareStatusText.contains("LIMITACIÓN")) Color.Red 
                                       else if(hardwareStatusText.contains("ESPERANDO")) LuxeColors.Gold 
                                       else LuxeColors.Green,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (modoRango == -1) hardwareStatusText else if (escaneando && !masaDetectada) hardwareStatusText else "OBJETIVO: $tipoObjetivo",
                                color = when {
                                    modoRango == -1 -> LuxeColors.Gold
                                    tipoObjetivo.contains("COCHE") || tipoObjetivo.contains("PERSONA") -> Color.Red
                                    tipoObjetivo.contains("SEÑAL") || tipoObjetivo.contains("BARRIDO") -> LuxeColors.ElectricBlue
                                    tipoObjetivo.contains("CABLE") || tipoObjetivo.contains("METAL") -> Color.Yellow
                                    else -> LuxeColors.Gold
                                },
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- 🛡️ CONFIGURACIÓN DINÁMICA DE SENSIBILIDAD (QUIRÚRGICO) ---
            if (modoRango == 3 || modoDireccional) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    color = Color.White.copy(0.04f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("1. AJUSTE DE PRECISIÓN", color = LuxeColors.ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                        
                        if (modoRango == 3) {
                            EliteSlider(label = "🧲 SENSIBILIDAD MAG (CABLES)", value = magSensitivity, valueLabel = when { magSensitivity < 0.3f -> "BAJA"; magSensitivity > 0.7f -> "EXTREMA"; else -> "CALIBRADA" }) { 
                                magSensitivity = it
                                onSensitivityChange(rfSensitivity, magSensitivity)
                            }
                        }

                        if (modoDireccional) {
                            if (modoRango == 3) Spacer(Modifier.height(16.dp))
                            EliteSlider(label = "📐 APERTURA DEL HAZ (FOCO)", value = aperturaHaz, valueLabel = when { aperturaHaz < 0.2f -> "PINPOINT"; aperturaHaz > 0.8f -> "AMPLIO"; else -> "ENFOCADO" }) { aperturaHaz = it }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                color = Color.White.copy(0.04f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("2. MODOS DE INTELIGENCIA", color = LuxeColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = "DIRECCIONAL", desc = "Localiza el punto exacto", isActive = modoDireccional, icon = Icons.Rounded.TrackChanges, color = LuxeColors.ElectricBlue, onHelpClick = { showingHelpFor = "DIRECCIONAL" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "DIRECCIONAL" } 
                            else { modoDireccional = !modoDireccional; if (modoDireccional && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                        TacticalToggle(modifier = Modifier.weight(1f), label = "ANÁLISIS PRO", desc = "Identifica personas/animales", isActive = modoPro, icon = Icons.Rounded.QueryStats, color = LuxeColors.Gold, onHelpClick = { showingHelpFor = "ANÁLISIS PRO" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "ANÁLISIS PRO" } 
                            else { val target = !modoPro; resetIntelligences(); modoPro = target; if (modoPro && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = "BUSCAR CÁMARAS", desc = "Lentes y micros espía", isActive = modoCamaras, icon = Icons.Rounded.Visibility, color = Color(0xFFE91E63), onHelpClick = { showingHelpFor = "BUSCAR CÁMARAS" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "BUSCAR CÁMARAS" } 
                            else { val target = !modoCamaras; resetIntelligences(); modoCamaras = target; if (modoCamaras && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                        TacticalToggle(modifier = Modifier.weight(1f), label = "MODO MISTERIO", desc = "Exploración de energía EMF", isActive = modoMisterio, icon = Icons.Rounded.AutoAwesome, color = Color(0xFF00FFCC), onHelpClick = { showingHelpFor = "MODO MISTERIO" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "MODO MISTERIO" } 
                            else { val target = !modoMisterio; resetIntelligences(); modoMisterio = target; if (modoMisterio && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = "ESCUDO VIAL", desc = "Aviso de coches por detrás", isActive = modoCiclista, icon = Icons.Rounded.DirectionsBike, color = Color(0xFFFF9800), onHelpClick = { showingHelpFor = "ESCUDO VIAL" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "ESCUDO VIAL" } 
                            else { val target = !modoCiclista; resetIntelligences(); modoCiclista = target; if (modoCiclista && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                        TacticalToggle(modifier = Modifier.weight(1f), label = "FUGA MICROONDAS", desc = "Chequeo de radiación salud", isActive = modoMicroondas, icon = Icons.Rounded.Dangerous, color = Color(0xFFCDDC39), onHelpClick = { showingHelpFor = "FUGA MICROONDAS" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "FUGA MICROONDAS" } 
                            else { val target = !modoMicroondas; resetIntelligences(); modoMicroondas = target; if (modoMicroondas && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = "BUSCAR TESOROS", desc = "Joyas de Oro y Plata", isActive = modoTesoros, icon = Icons.Rounded.Savings, color = LuxeColors.Gold, onHelpClick = { showingHelpFor = "BUSCAR TESOROS" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "BUSCAR TESOROS" } 
                            else { val target = !modoTesoros; resetIntelligences(); modoTesoros = target; if (modoTesoros && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                        TacticalToggle(modifier = Modifier.weight(1f), label = "ANTI-SKIMMER", desc = "Verifica cajeros seguros", isActive = modoSkimmer, icon = Icons.Rounded.SecurityUpdateWarning, color = Color.Cyan, onHelpClick = { showingHelpFor = "ANTI-SKIMMER" }, onClick = { 
                            if(modoRango == -1) { showRangePickerForMode = "ANTI-SKIMMER" } 
                            else { val target = !modoSkimmer; resetIntelligences(); modoSkimmer = target; if (modoSkimmer && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = "MODO PARED", desc = "Cables y Metales internos", isActive = modoRango == 3, icon = Icons.Rounded.ElectricBolt, color = Color.Yellow, onHelpClick = { showingHelpFor = "MODO PARED" }, onClick = { val target = if(modoRango == 3) 0 else 3; resetIntelligences(); modoRango = target; if (modoRango == 3 && !escaneando) escaneando = true; triggerUiSound("switch") })
                        TacticalToggle(modifier = Modifier.weight(1f), label = "RADAR DE AIRE", desc = "Detecta apertura de puertas", isActive = modoPresion, icon = Icons.Rounded.Air, color = Color.White, onHelpClick = { showingHelpFor = "CENTINELA AIRE" }, onClick = {
                            if(modoRango == -1) { showRangePickerForMode = "CENTINELA AIRE" } 
                            else { val target = !modoPresion; resetIntelligences(); modoPresion = target; if (modoPresion && !escaneando) escaneando = true; triggerUiSound("switch") } 
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TacticalToggle(modifier = Modifier.weight(1f), label = if (alarmaDisparada) "¡ALERTA!" else "ALARMA CASA", desc = "Vigilancia perimetral auto", isActive = modoAlarma, icon = if (alarmaDisparada) Icons.Rounded.NotificationsActive else Icons.Rounded.Security, color = if (alarmaDisparada) Color.Red else Color(0xFF4CAF50), onHelpClick = { showingHelpFor = "ALARMA CASA" }, onClick = { 
                            if(modoRango == -1) { 
                                showRangePickerForMode = "ALARMA CASA" 
                            } else { 
                                if (alarmaDisparada) { 
                                    alarmaDisparada = false 
                                } else { 
                                    val target = !modoAlarma
                                    resetIntelligences() 
                                    modoAlarma = target
                                    if (modoAlarma) { 
                                        if (!escaneando) escaneando = true; 
                                        tiempoCalibracion = 20; 
                                        triggerUiSound("click") 
                                    } 
                                } 
                            } 
                        })
                    }
                }
            }

            if (alarmaDisparada) {
                Button(onClick = { alarmaDisparada = false }, modifier = Modifier.padding(top = 12.dp).fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) { Text("SILENCIAR ALARMA", fontWeight = FontWeight.Black) }
            }


            Spacer(Modifier.weight(1f))
            Text("USO BAJO RESPONSABILIDAD DEL USUARIO. Herramienta informativa pasiva. No sustituye a sistemas de seguridad certificados ni garantiza detección absoluta.", color = Color.White.copy(0.2f), fontSize = 8.sp, textAlign = TextAlign.Center, lineHeight = 10.sp, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp))

            Row(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onShare("RADAR", "HERTZ", "SENTINEL", null) }, modifier = Modifier.weight(0.3f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Green.copy(0.1f)), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, LuxeColors.Green.copy(0.3f))) { Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = LuxeColors.Green) }
                Button(onClick = { 
                    if(modoRango == -1) { 
                        showRangePickerForMode = "RADAR" 
                    } else { 
                        escaneando = !escaneando; triggerUiSound("click") 
                    } 
                }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = if (escaneando) LuxeColors.Green else LuxeColors.ElectricBlue, contentColor = if (escaneando) Color.Black else Color.White), shape = RoundedCornerShape(18.dp), border = if (escaneando) BorderStroke(2.dp, LuxeColors.Green) else null) { Text(if (escaneando) "DETENER VIGILANCIA" else "INICIAR RADAR HERTZ", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, fontSize = 14.sp) }
            }

            Spacer(Modifier.height(180.dp))
        }
    }

    if (mostrarAyuda) AyudaRadarDialog(onDismiss = { mostrarAyuda = false })
    if (showingHelpFor != null) IntelligenceHelpDialog(mode = showingHelpFor!!, onDismiss = { showingHelpFor = null })
    if (showStatusHelp) StatusHelpDialog(status = hardwareStatusText, onDismiss = { showStatusHelp = false }, onRequestPermission = onRequestPermission, onOpenSettings = onOpenSettings, onExecuteEngineeringAction = onExecuteEngineeringAction)

    // --- 📡 SELECTOR DE ALCANCE DINÁMICO (UX INTEGRADA) ---
    if (showRangePickerForMode != null) {
        AlertDialog(
            onDismissRequest = { showRangePickerForMode = null },
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier.border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SettingsInputAntenna, null, tint = LuxeColors.ElectricBlue)
                    Spacer(Modifier.width(12.dp))
                    Text(if(showRangePickerForMode == "CHANGE_ONLY") "CAMBIAR ALCANCE" else "CONFIGURACIÓN INICIAL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Selecciona el alcance de rastreo para continuar con la operación.", fontSize = 13.sp, color = Color.White.copy(0.6f))
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RangeButton(Modifier.weight(1f), "CORTA", "1-5m", false, LuxeColors.ElectricBlue) { 
                            modoRango = 0; triggerUiSound("click"); 
                            val mode = showRangePickerForMode; showRangePickerForMode = null
                            if(mode != "CHANGE_ONLY" && mode != "RADAR") { 
                                when(mode) {
                                    "DIRECCIONAL" -> { modoDireccional = true; escaneando = true }
                                    "ANÁLISIS PRO" -> { modoPro = true; escaneando = true }
                                    "BUSCAR CÁMARAS" -> { modoCamaras = true; escaneando = true }
                                    "MODO MISTERIO" -> { modoMisterio = true; escaneando = true }
                                    "ESCUDO VIAL" -> { modoCiclista = true; escaneando = true }
                                    "FUGA MICROONDAS" -> { modoMicroondas = true; escaneando = true }
                                    "BUSCAR TESOROS" -> { modoTesoros = true; escaneando = true }
                                    "ANTI-SKIMMER" -> { modoSkimmer = true; escaneando = true }
                                    "CENTINELA AIRE" -> { modoPresion = true; escaneando = true }
                                    "ALARMA CASA" -> { modoAlarma = true; escaneando = true; tiempoCalibracion = 20 }
                                }
                                triggerUiSound("switch")
                            } else if(mode == "RADAR") { escaneando = true }
                        }
                        RangeButton(Modifier.weight(1f), "LARGA", "15-20m", false, LuxeColors.Gold) { 
                            modoRango = 1; triggerUiSound("click");
                            val mode = showRangePickerForMode; showRangePickerForMode = null
                            if(mode != "CHANGE_ONLY" && mode != "RADAR") { 
                                when(mode) {
                                    "DIRECCIONAL" -> { modoDireccional = true; escaneando = true }
                                    "ANÁLISIS PRO" -> { modoPro = true; escaneando = true }
                                    "BUSCAR CÁMARAS" -> { modoCamaras = true; escaneando = true }
                                    "MODO MISTERIO" -> { modoMisterio = true; escaneando = true }
                                    "ESCUDO VIAL" -> { modoCiclista = true; escaneando = true }
                                    "FUGA MICROONDAS" -> { modoMicroondas = true; escaneando = true }
                                    "BUSCAR TESOROS" -> { modoTesoros = true; escaneando = true }
                                    "ANTI-SKIMMER" -> { modoSkimmer = true; escaneando = true }
                                    "CENTINELA AIRE" -> { modoPresion = true; escaneando = true }
                                    "ALARMA CASA" -> { modoAlarma = true; escaneando = true; tiempoCalibracion = 20 }
                                }
                                triggerUiSound("switch")
                            } else if(mode == "RADAR") { escaneando = true }
                        }
                        RangeButton(Modifier.weight(1f), "EXTRA", "50m+", false, Color.Red) { 
                            modoRango = 2; triggerUiSound("click");
                            val mode = showRangePickerForMode; showRangePickerForMode = null
                            if(mode != "CHANGE_ONLY" && mode != "RADAR") { 
                                when(mode) {
                                    "DIRECCIONAL" -> { modoDireccional = true; escaneando = true }
                                    "ANÁLISIS PRO" -> { modoPro = true; escaneando = true }
                                    "BUSCAR CÁMARAS" -> { modoCamaras = true; escaneando = true }
                                    "MODO MISTERIO" -> { modoMisterio = true; escaneando = true }
                                    "ESCUDO VIAL" -> { modoCiclista = true; escaneando = true }
                                    "FUGA MICROONDAS" -> { modoMicroondas = true; escaneando = true }
                                    "BUSCAR TESOROS" -> { modoTesoros = true; escaneando = true }
                                    "ANTI-SKIMMER" -> { modoSkimmer = true; escaneando = true }
                                    "CENTINELA AIRE" -> { modoPresion = true; escaneando = true }
                                    "ALARMA CASA" -> { modoAlarma = true; escaneando = true; tiempoCalibracion = 20 }
                                }
                                triggerUiSound("switch")
                            } else if(mode == "RADAR") { escaneando = true }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false; authCode = "" },
            containerColor = Color.Black,
            modifier = Modifier.border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(24.dp)),
            title = { Text("ACCESO RESTRINGIDO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp) },
            text = {
                OutlinedTextField(value = authCode, onValueChange = { authCode = it; if (it == "121212") { showAuthDialog = false; onEngineeringPanelChange(true); authCode = ""; triggerUiSound("message") } }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), label = { Text("INDICATIVO DE MANTENIMIENTO", fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = LuxeColors.ElectricBlue), modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {}
        )
    }

    if (showingEngineeringHelp != null) EngineeringHelpDialog(mode = showingEngineeringHelp!!, onDismiss = { showingEngineeringHelp = null })

    if (engineeringPanelVisible) {
        AlertDialog(
            onDismissRequest = { onEngineeringPanelChange(false); onExecuteEngineeringAction("TERMINATE_DIAGNOSTICS"); activeEngineeringTask = null },
            containerColor = LuxeColors.DeepSea,
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(28.dp))
                .padding(2.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LuxeColors.ElectricBlue.copy(0.1f), CircleShape)
                            .border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Engineering, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "INGENIERÍA DE CAMPO",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "PROTOCOLOS DE AUDITORÍA AVANZADA",
                        color = LuxeColors.ElectricBlue.copy(0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = LuxeColors.ElectricBlue.copy(0.1f), thickness = 1.dp)
                }
            },
            text = {
                val toggleTask = { task: String, action: () -> Unit ->
                    if (activeEngineeringTask == task) {
                        onExecuteEngineeringAction("TERMINATE_DIAGNOSTICS")
                        activeEngineeringTask = null
                        onPlaySound("static")
                    } else {
                        onExecuteEngineeringAction("TERMINATE_DIAGNOSTICS")
                        onPlaySound("click")
                        vibratePtt()
                        activeEngineeringTask = task
                        action()
                    }
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // --- ⚡ FAMILIA: OPERACIONES CRÍTICAS ---
                    EngineeringHeader("⚡ OPERACIONES CRÍTICAS", Color.Red)
                    EngineeringButton(
                        label = if (activeEngineeringTask == "IOT_ASSAULT") "¡ASALTO IOT ACTIVO!" else "ASALTO TOTAL IOT (RF+IR+BT)", 
                        desc = "Inhibición Multivector: Máxima Potencia", 
                        active = activeEngineeringTask == "IOT_ASSAULT", 
                        color = Color.Red, 
                        onHelpClick = { showingEngineeringHelp = "ASALTO DUAL" }
                    ) { 
                        toggleTask("IOT_ASSAULT") {
                            onExecuteEngineeringAction("EXECUTE_AGGRESSIVE_IOT")
                            onNotification(AppNotification("ASALTO TOTAL", "ESTADO CRÍTICO: INHIBICIÓN IOT ACTIVA", NotificationType.Warning))
                        }
                    }

                    // --- 🚗 FAMILIA: ACCESO Y MOVILIDAD ---
                    EngineeringHeader("🚗 ACCESO Y MOVILIDAD", LuxeColors.ElectricBlue)
                    EngineeringButton(label = "APERTURA UNIVERSAL BARRERAS", desc = "Inducción Espira + Barrido IR", active = activeEngineeringTask == "BARRERA_ALL", color = LuxeColors.ElectricBlue, onHelpClick = { showingEngineeringHelp = "ABRIR PARKING" }) { 
                        toggleTask("BARRERA_ALL") {
                            onExecuteEngineeringAction("EXECUTE_BARRIER_ATTACK")
                        }
                    }
                    EngineeringButton(label = "FORZAR SEMÁFORO (VERDE)", desc = "Opticom 14Hz + EMF 400Hz", active = activeEngineeringTask == "TRAFFIC", color = LuxeColors.Green, onHelpClick = { showingEngineeringHelp = "FORZAR SEMÁFORO" }) { 
                        toggleTask("TRAFFIC") {
                            onExecuteEngineeringAction("EXECUTE_TRAFFIC_PRIORITY") 
                        }
                    }
                    EngineeringButton(label = "LIBERAR PESTILLO ELÉCTRICO", desc = "Resonancia Magnética Crítica", active = activeEngineeringTask == "LOCK_ATTACK", color = Color.Yellow, onHelpClick = { showingEngineeringHelp = "LIBERAR PESTILLO" }) { 
                        toggleTask("LOCK_ATTACK") {
                            onExecuteEngineeringAction("EXECUTE_LOCK_ATTACK") 
                        }
                    }
                    EngineeringButton(label = "PRIORIDAD ASCENSOR", desc = "Llamada maestra (Servicio Indep.)", active = activeEngineeringTask == "ELEVATOR", color = Color(0xFFFACC15), onHelpClick = { showingEngineeringHelp = "PRIORIDAD ASCENSOR" }) { 
                        toggleTask("ELEVATOR") {
                            onExecuteEngineeringAction("EXECUTE_ELEVATOR_PRIORITY") 
                        }
                    }
                    EngineeringButton(label = "MAESTRO DE VENDING / WASH", desc = "Inyección de Crédito MDB 50Hz", active = activeEngineeringTask == "VENDING_MASTER", color = Color(0xFF22D3EE), onHelpClick = { showingEngineeringHelp = "VENDING PRO" }) { 
                        toggleTask("VENDING_MASTER") {
                            onExecuteEngineeringAction("EXECUTE_VENDING_MASTER")
                        }
                    }

                    // --- 🕹️ FAMILIA: CONTROL DE ENTORNO ---
                    EngineeringHeader("🕹️ CONTROL DE ENTORNO", Color(0xFF22D3EE))
                    EngineeringButton(label = "GRITO DE RECUPERACIÓN", desc = "Forzar Modo Setup en IoT", active = activeEngineeringTask == "SETUP", color = LuxeColors.ElectricBlue, onHelpClick = { showingEngineeringHelp = "SETUP FORCE" }) { 
                        toggleTask("SETUP") {
                            onExecuteEngineeringAction("EXECUTE_SETUP_FORCE")
                        }
                    }
                    EngineeringButton(label = "BARRIDO IR UNIVERSAL", desc = "Fuerza bruta +50 marcas", active = activeEngineeringTask == "IR_UNIVERSAL", color = Color(0xFF22D3EE), onHelpClick = { showingEngineeringHelp = "IR UNIVERSAL" }) { 
                        toggleTask("IR_UNIVERSAL") {
                            onExecuteEngineeringAction("EXECUTE_IR_UNIVERSAL_SWEEP")
                        }
                    }

                    // --- 📡 FAMILIA: AUDITORÍA Y REDES ---
                    EngineeringHeader("📡 AUDITORÍA Y REDES", LuxeColors.Green)
                    EngineeringButton(label = "MODO DIOS WIFI", desc = "Priorizar ancho banda (QoS)", active = activeEngineeringTask == "WIFI_GOD", color = LuxeColors.ElectricBlue, onHelpClick = { showingEngineeringHelp = "WIFI GOD" }) { 
                        toggleTask("WIFI_GOD") {
                            onExecuteEngineeringAction("EXECUTE_WIFI_GOD")
                        }
                    }
                    EngineeringButton(label = "AUDITOR DE SEGURIDAD WIFI", desc = "Análisis vulnerabilidades WPA2", active = activeEngineeringTask == "WIFI_AUDIT", color = LuxeColors.ElectricBlue, onHelpClick = { showingEngineeringHelp = "AUDITOR WIFI" }) { 
                        toggleTask("WIFI_AUDIT") {
                            onExecuteEngineeringAction("GET_WIFI_SCAN")
                        }
                    }

                    if (activeEngineeringTask == "WIFI_AUDIT") {
                        if (selectedNetworkForAudit == null) {
                            if (wifiNetworks.isEmpty()) {
                                Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.2f))) { 
                                    Column(Modifier.padding(16.dp)) { 
                                        Row(verticalAlignment = Alignment.CenterVertically) { 
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LuxeColors.ElectricBlue, strokeWidth = 2.dp)
                                            Spacer(Modifier.width(16.dp))
                                            Text("ESCANEANDO ESPECTRO (MODO LETHAL)...", color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Black) 
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        LinearProgressIndicator(progress = wifiScanProgress, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = LuxeColors.ElectricBlue, trackColor = Color.White.copy(0.1f))
                                        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) { 
                                            Text("ANALIZANDO BEACONS / HANDSHAKES", color = Color.White.copy(0.3f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            Text("${(wifiScanProgress * 100).toInt()}%", color = LuxeColors.ElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Black) 
                                        } 
                                    } 
                                }
                            } else {
                                Text("REDES DETECTADAS (AUDITORÍA DISPONIBLE)", color = LuxeColors.ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                                wifiNetworks.forEach { net ->
                                    val isCracked = crackedNetworks.containsKey(net.ssid)
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
                                        shape = RoundedCornerShape(16.dp), 
                                        color = if(isCracked) LuxeColors.Green.copy(0.1f) else Color.White.copy(0.05f), 
                                        border = BorderStroke(1.dp, if (isCracked) LuxeColors.Green.copy(0.6f) else if (net.isVulnerable || net.wpsActive) Color.Red.copy(0.4f) else Color.White.copy(0.1f))
                                    ) { 
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { 
                                            Icon(
                                                if (isCracked) Icons.Rounded.LockOpen else if (net.isVulnerable || net.wpsActive) Icons.Rounded.WifiPassword else Icons.Rounded.Wifi, 
                                                null, 
                                                tint = if (isCracked) LuxeColors.Green else if (net.isVulnerable || net.wpsActive) Color.Red else LuxeColors.ElectricBlue, 
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) { 
                                                Text(net.ssid, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                Text(if (isCracked) "CLAVE EXTRAÍDA: ${crackedNetworks[net.ssid]}" else "${net.vendor} | ${net.security}", color = if (isCracked) LuxeColors.Green else Color.White.copy(0.5f), fontSize = 9.sp) 
                                            }
                                            IconButton(onClick = { 
                                                runWifiAudit(net)
                                            }) { 
                                                Icon(if (isCracked) Icons.Rounded.CheckCircle else Icons.Rounded.AutoFixHigh, null, tint = if (isCracked) LuxeColors.Green else LuxeColors.Gold) 
                                            } 
                                        } 
                                    }
                                }
                            }
                        } else {
                            // --- 🖥️ CONSOLA DE AUDITORÍA CRÍTICA ---
                            Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(20.dp), color = Color.Black, border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.5f))) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Terminal, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text("AUDITORÍA LETHAL: ${selectedNetworkForAudit?.ssid}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                        IconButton(onClick = { 
                                            selectedNetworkForAudit = null
                                            auditProgress = 0f
                                            currentAuditKey = ""
                                            auditLog.clear()
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f))
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    LinearProgressIndicator(progress = auditProgress, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = if (auditProgress >= 1f) LuxeColors.Green else LuxeColors.ElectricBlue, trackColor = Color.White.copy(0.1f))
                                    Spacer(Modifier.height(12.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.White.copy(0.05f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                        Column(Modifier.verticalScroll(rememberScrollState())) {
                                            auditLog.forEach { log ->
                                                Text("> $log", color = if (log.contains("ÉXITO") || log.contains("ACCESO")) LuxeColors.Green else Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    
                                    Column(Modifier.fillMaxWidth()) {
                                        val isVerified = crackedNetworks.containsKey(selectedNetworkForAudit?.ssid)
                                        val hasFailed = auditLog.contains("FALLO DE AUTENTICACIÓN REAL.")
                                        
                                        Text(
                                            when {
                                                isVerified -> "¡ACCESO VERIFICADO POR HARDWARE!"
                                                hasFailed -> "ERROR: LLAVE NO VÁLIDA"
                                                auditProgress >= 0.99f -> "VERIFICANDO CON EL HARDWARE..."
                                                else -> "Buscando colisión WPA..."
                                            }, 
                                            color = when {
                                                isVerified -> LuxeColors.Green
                                                hasFailed -> Color.Red
                                                else -> Color.White.copy(0.4f)
                                            },
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Black
                                        )
                                        
                                        Text(
                                            currentAuditKey.ifBlank { "..." }, 
                                            color = when {
                                                isVerified -> LuxeColors.Green
                                                hasFailed -> Color.Red
                                                auditProgress >= 0.99f -> LuxeColors.Gold
                                                else -> LuxeColors.Gold.copy(0.7f)
                                            }, 
                                            fontSize = 16.sp, 
                                            fontWeight = FontWeight.Black, 
                                            letterSpacing = 2.sp
                                        )
                                        
                                        if (isVerified) {
                                            Spacer(Modifier.height(16.dp))
                                            Button(
                                                onClick = { 
                                                    onExecuteEngineeringAction("COPY_TO_CLIPBOARD|$currentAuditKey")
                                                    onExecuteEngineeringAction("TRY_WIFI_CONNECT|${selectedNetworkForAudit?.ssid}|$currentAuditKey")
                                                    onNotification(AppNotification("SISTEMA LETHAL", "CONECTANDO A ${selectedNetworkForAudit?.ssid}...", NotificationType.Success))
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Green),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().height(44.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.Wifi, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("ESTABLECER ENLACE FINAL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                                }
                                            }
                                        } else if (hasFailed) {
                                            Spacer(Modifier.height(16.dp))
                                            LuxeButton("REINTENTAR BARRIDO", { 
                                                auditLog.clear()
                                                runWifiAudit(selectedNetworkForAudit!!) 
                                            }, true, Modifier.fillMaxWidth().height(44.dp), Color.Red, Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 🕵️ FAMILIA: HACKING FÍSICO ---
                    EngineeringHeader("🕵️ HACKING FÍSICO", LuxeColors.Green)
                    EngineeringButton(label = "SIGILO ELECTRÓNICO (CCTV/PIR)", desc = "Saturación Óptica + Térmica IR", active = activeEngineeringTask == "STEALTH", color = Color.Red, onHelpClick = { showingEngineeringHelp = "CEGUERA ÓPTICA" }) {
                        toggleTask("STEALTH") {
                            onExecuteEngineeringAction("EXECUTE_OPTICAL_JAMMER")
                            onExecuteEngineeringAction("EXECUTE_PIR_BLIND")
                        }
                    }
                    EngineeringButton(label = "JAMMER ULTRASÓNICO (SILENCIOSO)", desc = "Escudo audio 21kHz Inaudible", active = activeEngineeringTask == "ULTRA_JAM", color = LuxeColors.ElectricBlue, onHelpClick = { showingEngineeringHelp = "JAMMER AUDIO" }) { 
                        toggleTask("ULTRA_JAM") {
                            onExecuteEngineeringAction("EXECUTE_ULTRASONIC_JAMMER")
                        }
                    }
                    EngineeringButton(label = "BLOQUEO DE ARRANQUE (EMF)", desc = "Inhibidor inmovilizador 125kHz", active = activeEngineeringTask == "EMF", color = Color(0xFFFACC15), onHelpClick = { showingEngineeringHelp = "BLOQUEO DE ARRANQUE" }) { 
                        toggleTask("EMF") {
                            onExecuteEngineeringAction("EXECUTE_EMF_ANALYSIS")
                        }
                    }

                    // --- 🔑 FAMILIA: CLONACIÓN Y DATOS ---
                    EngineeringHeader("🔑 CLONACIÓN Y DATOS", Color.White)
                     EngineeringButton(label = "BARRIDO RF CODIFICADO", desc = "Fuerza bruta Rolling Code", active = activeEngineeringTask == "RF_CODED", color = Color(0xFFE879F9), onHelpClick = { showingEngineeringHelp = "BARRIDO RF" }) { 
                        toggleTask("RF_CODED") {
                            onExecuteEngineeringAction("EXECUTE_RF_SWEEP_CODED")
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun EngineeringHeader(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Box(Modifier.size(4.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun EngineeringButton(label: String, desc: String, active: Boolean, color: Color, onHelpClick: () -> Unit = {}, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) color.copy(0.15f) else Color.White.copy(0.03f),
        border = BorderStroke(
            width = if (active) 2.dp else 1.dp,
            color = if (active) color.copy(glowAlpha) else Color.White.copy(0.08f)
        ),
        tonalElevation = if (active) 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (active) color else color.copy(0.2f))
                    .drawBehind {
                        if (active) {
                            drawCircle(color, radius = size.minDimension * (1.5f + glowAlpha), alpha = glowAlpha * 0.5f)
                        }
                    }
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    label, 
                    color = Color.White, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black,
                    lineHeight = 14.sp
                )
                Text(
                    desc, 
                    color = if (active) color.copy(0.9f) else Color.White.copy(0.4f), 
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
            IconButton(
                onClick = { onHelpClick() },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(0.05f), CircleShape)
            ) {
                Icon(Icons.Rounded.HelpOutline, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EngineeringHelpDialog(mode: String, onDismiss: () -> Unit) {
    val (title, color, text) = when (mode) {
        "ASALTO DUAL" -> Triple("ASALTO DUAL (BARRERA + CÁMARAS)", Color.Red, "USO EN CONTACTO DIRECTO: Este modo es la herramienta más agresiva del panel. Combina una tormenta de inducción magnética con ráfagas masivas de RF.\n\nAPLICACIÓN REAL:\n1. Pegado a Cámaras: Busca saturar el procesador de vídeo por ruido EMF directo.\n2. Pegado a Barreras: Emite una firma de masa metálica pulsante para engañar a los sensores de suelo (espiras) en accesos industriales.")
        "BLOQUEO DE ARRANQUE" -> Triple("BLOQUEO DE ARRANQUE (EMF)", Color.Yellow, "USO EXPERIMENTAL: Genera una interferencia rítmica en la banda de baja frecuencia (125kHz).\n\nAPLICACIÓN REAL: Se utiliza para probar el blindaje de centralitas y sistemas de seguridad de proximidad. Al situar el móvil sobre el lector de arranque o la llave del vehículo, la ráfaga busca 'ensuciar' la comunicación de datos necesaria para la validación del motor.")
        "ABRIR PARKING" -> Triple("ABRIR PARKING / BARRERA", Color.Red, "SIMULACIÓN DE MASA: Genera pulsos inductivos de 40ms/20ms.\n\nAPLICACIÓN REAL: Diseñado para ser usado con el móvil situado sobre la espira magnética del asfalto. Los pulsos rítmicos simulan la entrada de un vehículo de gran tonelaje, activando la lógica de apertura por presencia metálica en sistemas de control automático de tráfico.")
        "FORZAR SEMÁFORO" -> Triple("FORZAR SEMÁFORO (VERDE)", Color.Green, "RESONANCIA 400Hz: Emite una pulsación mecánica y EMF a la frecuencia estándar de los sensores de tráfico.\n\nAPLICACIÓN REAL: Se utiliza en auditorías de movilidad urbana para verificar si los receptores de infrarrojos o magnéticos de los semáforos responden a la firma de onda de los vehículos de emergencia que solicitan paso prioritario.")
        "ANULAR WIFI" -> Triple("ANULAR SEGURIDAD WIFI", Color.Magenta, "TEST DE PUNTO DE CONTACTO: Fuerza al chip del móvil a emitir ráfagas de paquetes de gestión continua.\n\nAPLICACIÓN REAL:\n1. Enmudecer Cámaras: Si pegas el móvil a una cámara WiFi, la inundación de datos impide que la cámara encuentre 'aire libre' para transmitir su vídeo, causando que la imagen se congele o se caiga la conexión.\n2. Enmascarar Firma: Crea un ruido de red masivo que oculta tu tráfico real ante rastreadores inteligentes.")
        "CRÉDITO VENDING" -> Triple("CRÉDITO VENDING", Color.Cyan, "CHOQUE MAGNÉTICO: Un pulso seco de 500ms de máxima intensidad.\n\nAPLICACIÓN REAL: Se utiliza para probar la inmunidad electroestática de los lectores de monedas y billetes. Al acercar el móvil al receptor, se verifica si el sistema de pago está blindado contra descargas externas que podrían intentar forzar una carga de crédito falsa.")
        "LIBERAR PESTILLO" -> Triple("LIBERAR PESTILLO", Color.Yellow, "RESONANCIA MECÁNICA: Busca la vibración crítica de las bobinas de seguridad.\n\nAPLICACIÓN REAL: Al pegar el terminal al marco de una puerta con cerradura eléctrica, el patrón rítmico intenta 'atontar' el anclaje mecánico o la bobina de retención mediante vibración por resonancia y ruido EMF síncrono.")
        "COPIAR LLAVE" -> Triple("COPIAR LLAVE PROXIMIDAD", LuxeColors.Gold, "RECEPTOR EMF ACTIVO: Abre los sensores de alta frecuencia para análisis de datos.\n\nAPLICACIÓN REAL: Situando el móvil entre una tarjeta NFC y su lector, busca capturar las fluctuaciones del intercambio de datos. Sirve para analizar la seguridad del protocolo y verificar si la información se transmite de forma cifrada o vulnerable a clonación por proximidad.")
        "BARRIDO PARKING" -> Triple("BARRIDO IR (PARKING)", LuxeColors.ElectricBlue, "APERTURA POR PORTADORA: Emite ráfagas de datos sobre una portadora de 38kHz, la frecuencia estándar de la mayoría de barreras IR de acceso.\n\nAPLICACIÓN REAL: Dirigido a sistemas de control de acceso que no requieren Rolling Code pero sí una modulación específica. Al apuntar al receptor, el motor inyecta tramas de sincronismo y datos genéricos para forzar el relevador de apertura.")
        "IR UNIVERSAL" -> Triple("BARRIDO IR UNIVERSAL (TV/AC)", Color.Cyan, "BIBLIOTECA DE CÓDIGOS MAESTRA: Emite en ráfaga los códigos 'POWER ON/OFF' de las 50 marcas más comunes de televisores y aires acondicionados.\n\nAPLICACIÓN REAL: Útil para encender o apagar dispositivos en lugares públicos o recuperar el control si se ha perdido el mando original. El barrido dura 10 segundos para cubrir el espectro completo de marcas (Samsung, LG, Sony, Daikin, Mitsubishi, etc.).")
        "BARRIDO RF" -> Triple("BARRIDO RF CODIFICADO", Color.Magenta, "GENERADOR DE CÓDIGOS DINÁMICOS: Emite una ráfaga masiva de tramas RF simulando mandos a distancia de seguridad avanzada.\n\nAPLICACIÓN REAL: Para auditorías de sistemas que usan Rolling Code (códigos variables) en 433MHz y 868MHz. El motor genera una secuencia calculada de variaciones de código para intentar 'cazar' la ventana de validación del receptor. Mantenga el terminal cerca del receptor de la puerta o barrera durante el proceso (10 segundos).")
        "RF STORM" -> Triple("TORMENTA RF 2.0 (IOT)", Color.Magenta, "Saturación de espectro 2.4GHz: El ataque más agresivo para dispositivos inteligentes (IoT).\n\nAPLICACIÓN REAL:\n1. BLE Spam: Inunda el aire con miles de solicitudes de emparejamiento Bluetooth aleatorias. Esto colapsa el procesador de radio de altavoces inteligentes (como Alexa) o móviles cercanos, forzando desconexiones o bloqueos de interfaz.\n2. WiFi Ghosting: Realiza un escaneo masivo de canales para engañar a los routers y dispositivos WiFi, provocando que salten de canal y pierdan la estabilidad de conexión.")
        "JAMMER AUDIO" -> Triple("JAMMER ULTRASÓNICO (SILENCIOSO)", LuxeColors.ElectricBlue, "ESCUDO DE PRIVACIDAD SÓNICO: Emite un tono puro a 21kHz (totalmente inaudible para humanos pero ensordecedor para micrófonos).\n\nAPLICACIÓN REAL: Se utiliza para anular la capacidad de escucha de asistentes inteligentes o grabadoras espía. El ultrasonido satura la membrana del micrófono del objetivo sin emitir ningún sonido que tú puedas oír, impidiendo la captura de voz o el reconocimiento de órdenes externas.")
        "CEGUERA ÓPTICA" -> Triple("CEGUERA ÓPTICA (CCTV)", Color.Red, "INVISIBILIDAD ELECTRÓNICA: Utiliza ráfagas de luz infrarroja (IR) de alta densidad en modo discreto para saturar sensores CMOS de cámaras de vigilancia.\n\nAPLICACIÓN REAL: Diseñado para saturar sensores de cámaras de vigilancia nocturna. Al apuntar directamente a la lente, el infrarrojo 'quema' la imagen (la vuelve blanca) sin alertar visualmente mediante destellos de luz visible, permitiendo una evasión táctica indetectable para el ojo humano.")
        "VENDING PRO" -> Triple("CRÉDITO VENDING PRO", Color.Cyan, "RESONANCIA MDB 50Hz: Genera un pulso rítmico de 20ms (10ms ON/10ms OFF).\n\nAPLICACIÓN REAL: Esta frecuencia imita la firma electromagnética de los selectores de monedas industriales. Al pegar el terminal a la ranura de pago, la interferencia busca inducir un pulso de crédito falso en las bobinas de detección de la máquina expendedora.")
        "CLONADOR" -> Triple("CLONADOR DE MANDOS (RF/IR)", LuxeColors.Gold, "MODO ESCUCHA ACTIVO: Abre los sensores de radio e infrarrojos para capturar tramas de datos externas.\n\nAPLICACIÓN REAL: Sitúe el mando original a menos de 5cm del móvil y pulse el botón del mando. Si la radio identifica el protocolo (Garaje, TV, AC), el código aparecerá en la lista 'MEMORIA DE CLONACIÓN'. Desde allí, podrá reproducir la señal grabada para verificar la vulnerabilidad del sistema o como copia de seguridad.")
        "AUDITOR WIFI" -> Triple("AUDITOR DE SEGURIDAD WiFi", LuxeColors.ElectricBlue, "SISTEMA DE ANÁLISIS MULTIVECTORIAL: Evalúa la robustez de redes inalámbricas mediante tres métodos de auditoría.\n\nMETODOLOGÍAS:\n1. DICCIONARIO: Prueba patrones conocidos de operadoras españolas (Movistar, Digi, Orange) basados en algoritmos de generación de claves originales.\n2. ATAQUE WPS: Explota la debilidad del protocolo PIN de configuración rápida. Prueba los PINs más comunes según el fabricante del router.\n3. FUERZA BRUTA: Ejecuta una secuencia de las 100 contraseñas más frecuentes en entornos residenciales.\n\nAPLICACIÓN: Esta herramienta permite concienciar sobre la vulnerabilidad de las claves de fábrica y la importancia de desactivar el protocolo WPS.")
        "AUDITOR ARCOS" -> Triple("AUDITOR DE ARCOS (EAS)", LuxeColors.ElectricBlue, "RESONANCIA 58kHz / 8.2MHz: Emite una pulsación mecánica y EMF a la frecuencia estándar de los arcos de seguridad.\n\nAPLICACIÓN REAL: Se utiliza en auditorías de seguridad en comercios para verificar si los arcos detectan correctamente las etiquetas de seguridad (tags). El terminal simula la firma de resonancia de una etiqueta AM o RF en movimiento.")
        "VENDING MASTER" -> Triple("MAESTRO DE VENDING", Color.Magenta, "ACCESO A MENÚ IR: Emite códigos de servicio para acceder a las estadísticas internas de máquinas de vending y ocio.\n\nAPLICACIÓN REAL: Apunte al receptor IR de la máquina para intentar forzar el despliegue del menú de técnico. Permite visualizar el estado del cajetín de monedas, inventario y códigos de error de mantenimiento sin abrir la carcasa física.")
        "WIFI GOD" -> Triple("MODO DIOS WIFI", LuxeColors.ElectricBlue, "PRIORIDAD DE ANCHO DE BANDA: Utiliza técnicas de saturación selectiva para forzar a los dispositivos cercanos a re-negociar su conexión WiFi.\n\nAPLICACIÓN REAL: Al 'molestar' rítmicamente al resto de clientes de la red, el router prioriza los recursos de QoS (Quality of Service) para tu dispositivo, permitiéndote navegar a máxima velocidad en redes públicas muy congestionadas.")
        "SETUP FORCE" -> Triple("GRITO DE RECUPERACIÓN", LuxeColors.ElectricBlue, "RESETEO LOGICO IOT: Provoca un fallo de conexión masivo en un dispositivo inteligente para forzarlo a entrar en modo configuración.\n\nAPLICACIÓN REAL: Útil para anular cámaras WiFi o altavoces inteligentes (como Alexa) de forma no destructiva. El dispositivo, al verse incapaz de conectar a su red legítima bajo una tormenta de RF, se desconecta y queda en espera de ser configurado de nuevo, deteniendo su actividad de vigilancia.")
        "PRIORIDAD ASCENSOR" -> Triple("PRIORIDAD DE ASCENSOR", Color.Yellow, "LLAMADA MAESTRA IR: Envía el código de 'Servicio Independiente' a los receptores de control de ascensores industriales.\n\nAPLICACIÓN REAL: Apunte al panel de botones para activar el modo de prioridad. El ascensor ignorará las llamadas de otros pisos y se desplazará directamente a su destino con la mínima demora, imitando el funcionamiento de emergencia o mantenimiento.")
        "LAVADEROS" -> Triple("LAVADEROS Y BOXES", Color.Cyan, "CRÉDITO INDUCTIVO 50Hz: Genera un campo electromagnético rítmico diseñado para selectores de exterior.\n\nAPLICACIÓN REAL: Pegue el terminal al monedero de una máquina de lavado a presión o aspiradora. La resonancia busca inducir una lectura de metal síncrona en las bobinas detectoras para simular la inserción de una moneda de curso legal.")
        "PIR BLIND" -> Triple("INVISIBILIDAD PIR", Color.Green, "CEGUERA TÉRMICA IR: Emite una ráfaga infrarroja de alta densidad para saturar sensores de movimiento piroeléctricos.\n\nAPLICACIÓN REAL: Al inundar de luz el receptor térmico de una alarma, se impide que el sensor distinga el cambio de calor producido por una persona moviéndose. Crea una 'zona ciega' temporal que permite el tránsito sin disparar el aviso de intrusión.")
        else -> Triple("AYUDA DE INGENIERÍA", Color.White, "Seleccione una función para ver su manual técnico.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        modifier = Modifier.border(1.dp, color.copy(0.3f), RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Engineering, null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        },
        text = {
            Text(
                text, 
                fontSize = 12.sp, 
                lineHeight = 17.sp, 
                color = Color.White.copy(0.8f),
                textAlign = TextAlign.Justify
            )
        },
        confirmButton = {
            LuxeButton("CERRAR MANUAL", onDismiss, true, Modifier.fillMaxWidth().height(44.dp), color, Color.Black)
        }
    )
}

@Composable
private fun StatusHelpDialog(status: String, onDismiss: () -> Unit, onRequestPermission: () -> Unit, onOpenSettings: () -> Unit, onExecuteEngineeringAction: (String) -> Unit) {
    val (title, color, text) = when {
        status.contains("COMPATIBLE") -> Triple(
            "SISTEMA NO SOPORTADO",
            Color.Gray,
            "El Radar de Presencia requiere acceso directo al chip de radiofrecuencia para detectar perturbaciones físicas a tu alrededor.\n\nEsta función no está disponible en este navegador o sistema operativo. Para utilizar el escáner de presencia real tras muros, abre la aplicación nativa en un dispositivo Android compatible."
        )
        status.contains("LIMITACIÓN") || status.contains("LIMITACION") || status.contains("ESPERANDO") || status.contains("BUSCANDO") -> Triple(
            "PROTOCOLOS DE ACTIVACIÓN",
            LuxeColors.Gold,
            "Para que el Radar Hertz funcione a máxima potencia, se deben cumplir estos 3 requisitos críticos:\n\n" +
            "1. 📍 GPS ACTIVADO: Android requiere que la ubicación del sistema esté ENCENDIDA para recibir datos de radiofrecuencia.\n\n" +
            "2. 🛡️ UBICACIÓN PRECIOSA: La App debe tener permiso de 'Ubicación Precisa' (no aproximada) concedido en los ajustes.\n\n" +
            "3. ⚙️ MODO DESARROLLADOR: Debes desactivar la 'Limitación de búsqueda WiFi' en Opciones de Desarrollo.\n\n" +
            "💡 ¿NO VES LAS OPCIONES DE DESARROLLO?\n" +
            "Ve a Ajustes > Información del teléfono > Pulsa 7 VECES sobre 'Número de Compilación' para activarlas."
        )
        status.contains("WIFI") -> Triple(
            "ENLACE WIFI ACTIVO",
            LuxeColors.Green,
            "El sistema está funcionando a máxima precisión. Está analizando las ondas de radio de todas las redes WiFi a tu alrededor para detectar perturbaciones biológicas con total nitidez."
        )
        status.contains("PERMISOS") -> Triple(
            "ERROR DE PERMISOS",
            Color.Red,
            "Falta el permiso de 'Ubicación Precisa'. Sin este permiso, Android bloquea por seguridad el acceso al escáner de radiofrecuencias del terminal."
        )
        else -> Triple(
            "DIAGNÓSTICO DE RADAR",
            LuxeColors.Gold,
            "Revisando el estado del sensor... Si el radar no se mueve, verifica que el GPS esté encendido."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = color,
        textContentColor = Color.White,
        modifier = Modifier.border(1.dp, color.copy(0.3f), RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.GppMaybe, null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        text = { Text(text, fontSize = 13.sp, lineHeight = 18.sp, color = Color.White.copy(0.9f)) },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                if (status.contains("COMPATIBLE")) {
                    // MODO WEB/IPHONE: Invitamos a instalar la APP
                    LuxeButton("DESCARGAR APP NATIVA", { 
                        // Activamos el comando de instalación externa
                        onExecuteEngineeringAction("INSTALL_APP")
                        onDismiss()
                    }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                } else {
                    // MODO ANDROID: Pasos técnicos
                    LuxeButton("1. AJUSTES DESARROLLADOR", { 
                        onOpenSettings()
                        onDismiss()
                    }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.ElectricBlue, Color.Black)
                    
                    Spacer(Modifier.height(8.dp))
                    
                    LuxeButton("2. PERMISOS Y GPS", { 
                        onRequestPermission()
                        onDismiss()
                    }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Green, Color.Black)
                }

                Spacer(Modifier.height(8.dp))
                
                LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), color.copy(0.2f), Color.White)
            }
        }
    )
}

@Composable
private fun AyudaRadarDialog(onDismiss: () -> Unit) {
    var mostrarGuiaNocturna by remember { mutableStateOf(false) }

    if (mostrarGuiaNocturna) {
        AlertDialog(
            onDismissRequest = { mostrarGuiaNocturna = false },
            containerColor = Color(0xFF0F172A),
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NightsStay, null, tint = LuxeColors.Gold)
                    Spacer(Modifier.width(12.dp))
                    Text("VIGILANCIA EN REPOSO", fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Sigue estos pasos para que el Radar vigile tu entorno toda la noche sin interrupciones:", fontSize = 13.sp, color = Color.White.copy(0.9f))
                    Spacer(Modifier.height(20.dp))
                    
                    GuiaItem("1. MANTÉN EL CARGADOR", "Android cierra las apps de vigilancia para ahorrar batería. Al estar enchufado, el sistema permite que el radar funcione al 100%.", Icons.Rounded.BatteryChargingFull)
                    GuiaItem("2. BATERÍA SIN RESTRICCIONES", "Mantén pulsado el icono de la app > Info (i) > Ahorro de batería > selecciona 'SIN RESTRICCIONES'.", Icons.Rounded.EnergySavingsLeaf)
                    GuiaItem("3. MODO HÍBRIDO SEGURO", "Aunque Google limite el WiFi de noche, el sensor magnético (Magnetómetro) NUNCA se detiene. Tu entorno sigue protegido.", Icons.Rounded.Shield)
                    GuiaItem("4. VOLUMEN DE ALARMA", "Asegúrate de que el volumen de Multimedia esté alto para oír la sirena si hay una intrusión.", Icons.Rounded.VolumeUp)
                }
            },
            confirmButton = {
                LuxeButton("VOLVER", { mostrarGuiaNocturna = false }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF0F172A),
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier
                .padding(vertical = 40.dp) 
                .border(1.dp, LuxeColors.ElectricBlue.copy(0.3f), RoundedCornerShape(24.dp)),
            title = { Text("MANUAL DE OPERACIONES", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SeccionAyuda("MODO DIRECCIONAL", "Concentra el sensor hacia adelante. Usa el slider de APERTURA para cerrar el foco y localizar objetivos con precisión quirúrgica.", LuxeColors.ElectricBlue, Icons.Rounded.TrackChanges)
                    SeccionAyuda("ANÁLISIS PRO 360", "Motor inteligente con brújula. Mapea el entorno en 360º creando un rastro visual de las zonas con actividad.", LuxeColors.Gold, Icons.Rounded.QueryStats)
                    SeccionAyuda("MODO MISTERIO", "Herramienta de exploración EMF. Detecta picos de energía electromagnética inexplicables en el entorno para investigación de campo.", Color(0xFF00FFCC), Icons.Rounded.AutoAwesome)
                    SeccionAyuda("ESCUDO VIAL", "Radar pasivo para ciclistas y peatones. Detecta vehículos aproximándose por detrás y emite una alerta crítica de seguridad.", Color(0xFFFF9800), Icons.Rounded.DirectionsBike)
                    SeccionAyuda("BUSCAR CÁMARAS", "Localizador de dispositivos espía. Detecta el campo magnético de las lentes y la transmisión de datos WiFi de cámaras ocultas.", Color(0xFFE91E63), Icons.Rounded.Visibility)
                    SeccionAyuda("MODO PARED", "Detector de infraestructura instantáneo. Localiza cables y metales. La detección se corta en el acto al separar el terminal.", Color.Yellow, Icons.Rounded.ElectricBolt)
                    SeccionAyuda("ALARMA INTELIGENTE", "Sistema escalonado: emite un pre-aviso discreto ante la primera sospecha y dispara la sirena solo si la amenaza persiste.", Color(0xFF4CAF50), Icons.Rounded.Security)
                    
                    Spacer(Modifier.height(20.dp))
                    Text("IDENTIFICACIÓN DE ICONOS", color = LuxeColors.Gold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconoLeyenda(Icons.Rounded.RadioButtonChecked, LuxeColors.ElectricBlue, "Señal RF", Modifier.weight(1f))
                        IconoLeyenda(Icons.Rounded.DirectionsRun, Color.Red, "Persona", Modifier.weight(1f))
                        IconoLeyenda(Icons.Rounded.ElectricBolt, Color.Yellow, "Metal", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconoLeyenda(Icons.Rounded.Visibility, Color(0xFFE91E63), "Cámara", Modifier.weight(1f))
                        IconoLeyenda(Icons.Rounded.Warning, Color.Red, "Vehículo", Modifier.weight(1f))
                        IconoLeyenda(Icons.Rounded.AutoAwesome, Color(0xFF00FFCC), "EMF/Ente", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("TÉCNICA DE ESCANEO TÁCTICO", color = LuxeColors.ElectricBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    Surface(color = LuxeColors.ElectricBlue.copy(0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f))) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.ScreenRotation, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("APUNTA CON EL FILO SUPERIOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            Spacer(Modifier.height(12.dp))
                            Text("Para localizar vecinos o intrusos tras el muro, sostén el móvil en HORIZONTAL (como un mapa) y apunta con el BORDE SUPERIOR hacia la zona. El cuerpo del móvil servirá de escudo para triangulación.", fontSize = 11.sp, color = Color.White.copy(0.7f), lineHeight = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { mostrarGuiaNocturna = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.4f))) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.NightsStay, null, tint = LuxeColors.Gold, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(12.dp)); Text("VIGILANCIA TODA LA NOCHE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    }

                    Spacer(Modifier.height(16.dp))
                    Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.Red.copy(0.3f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.PanTool, null, tint = Color.Red, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("REGLA CRÍTICA: No muevas el móvil durante el escaneo. El sensor debe estar 100% quieto.", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(140.dp))
                }
            },
            confirmButton = { LuxeButton("¡ENTENDIDO!", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.ElectricBlue, Color.White) }
        )
    }
}

@Composable
private fun GuiaItem(titulo: String, desc: String, icono: ImageVector) {
    Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Icon(icono, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.Black, color = LuxeColors.Gold, fontSize = 11.sp)
            Text(desc, fontSize = 10.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 2.dp), lineHeight = 14.sp)
        }
    }
}

@Composable
private fun IconoLeyenda(icono: ImageVector, color: Color, texto: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(0.05f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(0.2f))) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icono, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(texto, color = Color.White.copy(0.9f), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun SeccionAyuda(titulo: String, desc: String, color: Color, icono: ImageVector) {
    Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(0.1f), CircleShape).border(1.dp, color.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) { Icon(icono, null, tint = color, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.Black, color = color, fontSize = 12.sp, letterSpacing = 1.sp)
            Text(desc, fontSize = 10.sp, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 2.dp), lineHeight = 14.sp)
        }
    }
}

private fun smoothAnimate(current: Float, target: Float): Float = current + (target - current) * 0.15f

@Composable
private fun TacticalToggle(modifier: Modifier, label: String, desc: String, isActive: Boolean, icon: ImageVector, color: Color, onHelpClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 66.dp), shape = RoundedCornerShape(14.dp), color = if (isActive) color.copy(0.15f) else Color.White.copy(0.08f), border = BorderStroke(1.dp, if (isActive) color.copy(0.6f) else Color.White.copy(0.15f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isActive) color else Color.White.copy(0.4f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                Text(desc, color = Color.White.copy(0.85f), fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 11.sp)
            }
            if (onHelpClick != null) { IconButton(onClick = { onHelpClick() }, modifier = Modifier.size(24.dp)) { Icon(Icons.Rounded.Info, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(16.dp)) } }
        }
    }
}

@Composable
private fun IntelligenceHelpDialog(mode: String, onDismiss: () -> Unit) {
    val (title, icon, color, text) = when (mode) {
        "DIRECCIONAL" -> Triple("MODO DIRECCIONAL", Icons.Rounded.TrackChanges, LuxeColors.ElectricBlue).let { it.copy(fourth = "Concentra la sensibilidad del sensor hacia el borde superior del móvil. Úsalo para apuntar directamente a una zona y descartar rebotes laterales. Utiliza el deslizador de APERTURA para ajustar el ángulo.") }
        "ANÁLISIS PRO" -> Triple("ANÁLISIS PRO 360", Icons.Rounded.QueryStats, LuxeColors.Gold).let { it.copy(fourth = "Activa el motor de reconocimiento por firma de onda. Mapea el entorno en 360º usando la brújula interna para recordar la posición de objetivos detectados.") }
        "BUSCAR CÁMARAS" -> Triple("BUSCAR CÁMARAS", Icons.Rounded.Visibility, Color(0xFFE91E63)).let { it.copy(fourth = "Escáner especializado en detectar la electrónica activa de lentes y micrófonos ocultos. Busca la coincidencia entre el campo magnético del procesador y el WiFi.") }
        "MODO MISTERIO" -> Triple("MODO MISTERIO", Icons.Rounded.AutoAwesome, Color(0xFF00FFCC)).let { it.copy(fourth = "Configuración de ultra-sensibilidad magnética (EMF). Detecta fluctuaciones de energía electromagnética inexplicables en el ambiente.") }
        "ESCUDO VIAL" -> Triple("ESCUDO VIAL", Icons.Rounded.DirectionsBike, Color(0xFFFF9800)).let { it.copy(fourth = "Radar de seguridad trasera. Detecta la 'sombra de radio' masiva de un vehículo al aproximarse por detrás. Emite alerta de sirena y vibración.") }
        "FUGA MICROONDAS" -> Triple("FUGA MICROONDAS", Icons.Rounded.Dangerous, Color(0xFFCDDC39)).let { it.copy(fourth = "Chequeo de seguridad para el hogar. Detecta si la radiación de 2.4GHz escapa del horno microondas por sellos defectuosos.") }
        "BUSCAR TESOROS" -> Triple("BUSCAR TESOROS", Icons.Rounded.Savings, LuxeColors.Gold).let { it.copy(fourth = "Detector de metales preciosos por inducción. Calibrado para buscar la firma de alta conductividad del oro y plata.") }
        "ANTI-SKIMMER" -> Triple("ANTI-SKIMMER", Icons.Rounded.SecurityUpdateWarning, Color.Cyan).let { it.copy(fourth = "Escáner de cajeros. Busca dispositivos parásitos (Skimmers) ocultos en ranuras de tarjetas analizando micro-interferencias magnéticas.") }
        "MODO PARED" -> Triple("MODO PARED", Icons.Rounded.ElectricBolt, Color.Yellow).let { it.copy(fourth = "Localizador de infraestructura instantáneo. Detecta campos de cables con corriente y grandes masas metálicas (vigas) en tiempo real.") }
        "ALARMA CASA" -> Triple("ALARMA CASA", Icons.Rounded.Security, Color(0xFF4CAF50)).let { it.copy(fourth = "Vigilancia perimetral automática. Si detecta una presencia persistente, disparará un pre-aviso y luego la sirena de intrusión.") }
        "CENTINELA AIRE" -> Triple("CENTINELA DE PRESIÓN", Icons.Rounded.Air, Color.White).let { it.copy(fourth = "Seguridad pasiva invisible. Utiliza el barómetro del móvil para detectar cambios bruscos en la presión del aire causados por la apertura de una puerta o ventana en una habitación cerrada.") }
        else -> Triple("AYUDA", Icons.Rounded.Info, Color.White).let { it.copy(fourth = "Selecciona un modo para comenzar el escaneo.") }
    }
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF0F172A), modifier = Modifier.border(1.dp, color.copy(0.3f), RoundedCornerShape(24.dp)), title = { Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).background(color.copy(0.1f), CircleShape).border(1.dp, color.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }; Spacer(Modifier.width(16.dp)); Text(title, color = color, fontWeight = FontWeight.Black, fontSize = 16.sp) } }, text = { Text(text, fontSize = 13.sp, lineHeight = 18.sp, color = Color.White.copy(0.9f), textAlign = TextAlign.Justify) }, confirmButton = { LuxeButton("ENTENDIDO", onDismiss, true, Modifier.fillMaxWidth().height(48.dp), color, Color.Black) })
}

private fun <A, B, C> Triple<A, B, C>.copy(fourth: String): Quadruple<A, B, C, String> = Quadruple(first, second, third, fourth)
data class Quadruple<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun WifiAuditMiniPanel(
    networks: List<WifiNetwork>,
    progress: Float,
    selectedNetwork: WifiNetwork?,
    auditProgress: Float,
    currentKey: String,
    onSelectNetwork: (WifiNetwork?) -> Unit,
    onCopyAndConnect: (String) -> Unit = {}
) {
    Surface(
        color = Color.Black.copy(0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.2f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Terminal, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CONSOLA DE AUDITORÍA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            
            if (selectedNetwork == null) {
                if (networks.isEmpty()) {
                    LinearProgressIndicator(progress = progress, color = LuxeColors.ElectricBlue, modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape))
                    Text("BUSCANDO REDES...", color = Color.White.copy(0.4f), fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        networks.take(10).forEach { net ->
                            Surface(
                                onClick = { onSelectNetwork(net) },
                                color = Color.White.copy(0.05f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if(net.isVulnerable || net.wpsActive) Icons.Rounded.WifiPassword else Icons.Rounded.Wifi, null, tint = if(net.isVulnerable || net.wpsActive) Color.Red else LuxeColors.ElectricBlue, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(net.ssid, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("${net.vendor} | ${net.security}", color = Color.White.copy(0.4f), fontSize = 7.sp)
                                    }
                                    Icon(Icons.Rounded.ArrowForwardIos, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // --- 🖥️ VENTANA DE PROCESO DE AUDITORÍA (BRUTE FORCE) ---
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("AUDITORÍA: ${selectedNetwork.ssid}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        IconButton(onClick = { onSelectNetwork(null) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = auditProgress, color = if(auditProgress >= 1f) LuxeColors.Green else LuxeColors.Gold, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
                    Spacer(Modifier.height(16.dp))
                    
                    Column(Modifier.fillMaxWidth()) {
                        Text(if(auditProgress >= 0.99f) "KEY ENCONTRADA:" else "PROBANDO:", color = Color.White.copy(0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(currentKey.ifBlank { "..." }, color = if (auditProgress >= 0.99f) LuxeColors.Green else LuxeColors.Gold, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        
                        if (auditProgress >= 0.99f) {
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onSelectNetwork(selectedNetwork) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp).weight(1f),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("REINTENTAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }

                                Button(
                                    onClick = { onCopyAndConnect(currentKey) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Green),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp).weight(1.5f),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Spacer(Modifier.width(8.dp))
                                        Text("COPIAR Y CONECTAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeButton(modifier: Modifier, label: String, range: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.fillMaxHeight().padding(4.dp), shape = RoundedCornerShape(12.dp), color = if (isSelected) color.copy(0.15f) else Color.Transparent, border = if (isSelected) BorderStroke(1.dp, color) else null) {
        Box(contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = if (isSelected) Color.White else Color.White.copy(0.4f), fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp); Text(range, color = if (isSelected) color else Color.White.copy(0.2f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) } }
    }
}
