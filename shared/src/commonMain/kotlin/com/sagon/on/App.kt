package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - LÓGICA DE APLICACIÓN Y PERSISTENCIA
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 1.2 (AUDIO & ROUTE FIX)
 * 
 * Este archivo gestiona el estado global de la emisora, persistencia y navegación global.
 * Sincronización maestra de Audio Core e Interfaz de Usuario.
 * Blindado contra modificaciones en la gestión de flujos de estado y seguridad.
 * ⚠️ NOTA CRÍTICA: PROHIBIDO MODIFICAR EL DISEÑO VISUAL, ICONOS O COLORES ESTABLECIDOS.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.jetbrains.compose.resources.painterResource
import on.shared.generated.resources.Res
import on.shared.generated.resources.logo

@Composable
fun App(
    savedNick: String,
    isFirstTime: Boolean,
    onOnboardingFinish: () -> Unit,
    onPermissionRequest: (String) -> Unit,
    onLogout: () -> Unit,
    onInstallRequest: () -> Unit,
    externalShowExitConfirm: Boolean,
    onExternalExitRequest: (Boolean, Boolean) -> Unit,
    onShareRequest: (String, String, String, String?, String?, String?) -> Unit,
    onNoiseVolumeChange: (Float) -> Unit,
    onMoniVolumeChange: (Float) -> Unit,
    onEchoChange: (Boolean, Float) -> Unit,
    onCityChange: (String) -> Unit,
    onSubtoneChange: (String) -> Unit,
    onChannelChange: (String) -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onDeleteMessage: (String, String?) -> Unit = { _, _ -> },
    onPrivateChatRequest: (String) -> Unit,
    onPublicChatRequest: () -> Unit,
    onStateSave: (RadioState) -> Unit,
    onMicEnable: (Boolean, Boolean, Float) -> Unit,
    onReport: (String) -> Unit,
    onBlockUser: (String) -> Unit = {},
    onNotificationDismiss: () -> Unit,
    onNotificationPermissionRequest: () -> Unit = {},
    onReplayRequest: () -> Unit = {},
    onBatteryCheckRequest: () -> Boolean = { false },
    onIgnoreBatteryOptimizations: () -> Unit = {},
    onGpsRequest: (callback: (String?) -> Unit) -> Unit = { it(null) },
    onGpsCityRequest: (callback: (String?) -> Unit) -> Unit = { it(null) },
    onPlaySound: (String) -> Unit = {},
    showInstallPrompt: Boolean = false,
    onInstallConfirm: () -> Unit = {},
    onInstallDismiss: () -> Unit = {},
    externalNotification: AppNotification?,
    externalBackPressCount: Int = 0,
    micLevel: Float,
    isBeeping: Boolean,
    isCodedRx: Boolean = false,
    externalPtt: Boolean = false,
    externalPttBlocked: Boolean = false,
    replayProgress: Float = 0f,
    isReplayReady: Boolean = false,
    remoteUsers: List<RemoteUser>,
    remoteTransmitterName: String?,
    chatMessages: List<ChatMessage>,
    forceInitialScreen: Boolean,
    forceChatOpen: Boolean = false,
    forceChatTarget: String? = null,
    audioIntegrity: Boolean = true,
    bgStationName: String? = null,
    forceBgGenre: String? = null,
    onAntennaTest: (Boolean) -> Unit = {},
    onBgRadioScan: (String, String) -> Unit = { _, _ -> },
    onBgRadioStop: () -> Unit = {},
    onBgVolumeChange: (Float) -> Unit = {},
    onGetWifiVariance: (Int) -> Float = { _ -> 0f },
    onGetHeading: () -> Float = { 0f },
    onGetTilt: () -> Float = { 0f },
    onExecuteEngineeringAction: (String) -> Unit = {},
    onWifiListReceived: ((String) -> Unit) -> Unit = { _ -> },
    onWifiAuthResultReceived: ((String, String, String) -> Unit) -> Unit = { _ -> },
    onEngineeringFinished: (() -> Unit) -> Unit = { _ -> },
    onRouteSuggestionsReceived: ((String) -> Unit) -> Unit = { _ -> },
    onPoiResultsReceived: ((String) -> Unit) -> Unit = { _ -> },
    onWaypointReceived: ((String, Double, Double) -> Unit) -> Unit = { _ -> },
    onRequestLocationPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onChatOpenConsumed: () -> Unit = {},
    onChatTargetConsumed: () -> Unit = {},
    onBgGenreConsumed: () -> Unit = {},
    onBgGenreChangeExternal: (String) -> Unit = {},
    onDgtUpdate: (String?) -> Unit = {},
    dgtText: String? = null,
    dgtImageUrl: String? = null,
    voxActive: Boolean = false,
    isPttLive: Boolean = false,
    wifiVerificationResult: String? = null,
    nasaImageUrl: String? = null,
    nasaImageTitle: String? = null,
    nasaImageExplanation: String? = null,
    routeDistanceKm: String? = null,
    routeDurationMin: String? = null,
    routeDestinationName: String? = null,
    nextNavigationStep: String? = null,
    routeWaypoints: List<RouteSuggestion> = emptyList(),
    initialState: RadioState
) {
    var screenState by remember { 
        mutableStateOf(
            if (savedNick.isNotEmpty() && initialState.hasAcceptedMicExplain) Screen.RadioCB 
            else Screen.Welcome
        ) 
    }

    var channelToDelete by remember { mutableStateOf<String?>(null) }

    // --- ⏳ GESTIÓN DE CARGA INICIAL ---
    var isAppReady by remember { mutableStateOf(true) }

    var nick by remember { mutableStateOf(savedNick) }
    var radioState by remember { 
        mutableStateOf(
            initialState.copy(
                nasaImageUrl = nasaImageUrl ?: initialState.nasaImageUrl,
                nasaImageTitle = nasaImageTitle ?: initialState.nasaImageTitle,
                nasaImageExplanation = nasaImageExplanation ?: initialState.nasaImageExplanation,
                routeDistanceKm = routeDistanceKm ?: initialState.routeDistanceKm,
                routeDurationMin = routeDurationMin ?: initialState.routeDurationMin,
                routeDestinationName = routeDestinationName ?: initialState.routeDestinationName,
                nextNavigationStep = nextNavigationStep ?: initialState.nextNavigationStep,
                routeWaypoints = if (routeWaypoints.isNotEmpty()) routeWaypoints else initialState.routeWaypoints
            )
        ) 
    }
    var localNotification by remember { mutableStateOf<AppNotification?>(null) }
    var showActivityRadar by remember { mutableStateOf(false) } // 🛡️ Pantalla de Radar de Presencia / Actividad
    var showActivityMap by remember { mutableStateOf(initialState.activeProfile != ActivityProfile.NORMAL) } // 🛡️ Nueva pantalla de Deporte/Ruta
    var engineeringPanelVisible by remember { mutableStateOf(false) } // 🛡️ Levantado para control quirúrgico de Back
    var engineeringResetTrigger by remember { mutableStateOf(0) }
    var wifiVerificationResult by remember { mutableStateOf<String?>(null) }

    // --- 🛠️ ESTADO DE AUDITORÍA ---
    val wifiNetworks = remember { mutableStateListOf<WifiNetwork>() }

    // --- 🛰️ SUSCRIPCIÓN A EVENTOS NATIVOS ---
    LaunchedEffect(Unit) {
        onWifiAuthResultReceived { status, ssid, pass ->
            wifiVerificationResult = "$status|$ssid|$pass"
        }

        onWifiListReceived { json ->
            try {
                // Parsing manual para el bridge nativo
                // Formato esperado: "SSID|BSSID|Signal|Security|Vendor|isVulnerable|wpsActive|defaultPass|wpsPin;..."
                val networks = json.split(";").filter { it.isNotBlank() }.map { line ->
                    val parts = line.split("|")
                    WifiNetwork(
                        ssid = parts.getOrNull(0) ?: "DESCONOCIDA",
                        bssid = parts.getOrNull(1) ?: "",
                        signal = parts.getOrNull(2)?.toIntOrNull() ?: -100,
                        security = parts.getOrNull(3) ?: "WPA2",
                        vendor = parts.getOrNull(4) ?: "GENÉRICO",
                        isVulnerable = parts.getOrNull(5) == "true",
                        wpsActive = parts.getOrNull(6) == "true",
                        defaultPassword = parts.getOrNull(7),
                        wpsPin = parts.getOrNull(8)
                    )
                }
                wifiNetworks.clear()
                wifiNetworks.addAll(networks)
            } catch(e: Exception) {
                println("Error parsing WiFi list: ${e.message}")
            }
        }

        onEngineeringFinished {
            engineeringResetTrigger++
        }

        onRouteSuggestionsReceived { json ->
            try {
                val list = json.split(";").filter { it.isNotBlank() }.map { line ->
                    val parts = line.split("|")
                    RouteSuggestion(
                        name = parts.getOrNull(0) ?: "Ubicación Desconocida",
                        lat = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                        lon = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    )
                }
                radioState = radioState.copy(routeSuggestions = list)
            } catch(e: Exception) {}
        }

        onPoiResultsReceived { json ->
            try {
                val list = json.split(";").filter { it.isNotBlank() }.map { line ->
                    val parts = line.split("|")
                    RouteSuggestion(
                        name = parts.getOrNull(0) ?: "Punto de Interés",
                        lat = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                        lon = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    )
                }
                radioState = radioState.copy(poiSuggestions = list)
            } catch(e: Exception) {}
        }
    }

    // --- 🌙 DETECTOR DE MODO NOCHE AUTOMÁTICO (Idea 1) ---
    LaunchedEffect(Unit) {
        while(true) {
            val hour = getCurrentHour()
            val isNight = hour >= 22 || hour < 7
            if (radioState.isNightMode != isNight) {
                radioState = radioState.copy(isNightMode = isNight)
            }
            delay(60000) // Comprobar cada minuto
        }
    }
    
    // --- 🌍 DETECTAR PLATAFORMA ---
    val isWebPlatform = remember {
        val name = getPlatform().name.uppercase()
        name.contains("CHROME") || name.contains("SAFARI") || 
        name.contains("FIREFOX") || name.contains("EDGE") ||
        name.contains("WEB") || name.contains("UNKNOWN")
    }
    
    // --- 🔔 ESTADOS DE FLUJO Y PERMISOS ---
    var radarActivo by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showPrivacy by remember { mutableStateOf(false) }
    var showNotificationConsent by remember { mutableStateOf(false) }
    var showBatteryWarning by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 🛡️ PROTECCIÓN DE RUTA INICIAL: Si entramos directamente por enlace
    LaunchedEffect(showActivityMap) {
        if (showActivityMap && !isWebPlatform) {
            if (onBatteryCheckRequest()) {
                showBatteryWarning = true
            }
        }
    }
    var showWebHelpDialog by remember { mutableStateOf(false) }
    var nivelPerturbacion by remember { mutableStateOf(0f) }
    var radarModoRango by remember { mutableStateOf(0) }

    // --- 🔔 SENSOR DE AMIGOS Y ALERTAS SOS ---
    var lastUsers by remember { mutableStateOf(emptyList<RemoteUser>()) }
    LaunchedEffect(remoteUsers) {
        val currentFriendNicks = remoteUsers.filter { it.nick in radioState.friends }.map { it.nick }
        val lastFriendNicks = lastUsers.filter { it.nick in radioState.friends }.map { it.nick }
        
        // Alerta de Amigo
        remoteUsers.find { it.nick in currentFriendNicks && it.nick !in lastFriendNicks }?.let { friend ->
            localNotification = AppNotification(
                title = "¡AMIGO CONECTADO!",
                message = "${friend.nick} está en ${friend.city}",
                type = NotificationType.Success,
                actionLabel = "UNIRSE",
                onAction = {
                    radioState = radioState.copy(city = friend.city, channel = friend.channel)
                    localNotification = null
                }
            )
            triggerUiSound("click")
        }



        lastUsers = remoteUsers
    }

    val notificationToShow = externalNotification ?: localNotification
    
    // --- 🚨 MOTOR DE SIRENA CÍCLICA (MODO PAGER) ---
    // Este efecto se encarga de que la sirena se repita mientras haya una alerta activa que no hayamos visto
    LaunchedEffect(remoteUsers, radioState.city, notificationToShow) {
        val hasActiveSos = remoteUsers.any { it.city == radioState.city && it.isSOS }
        // Si hay un SOS activo y la notificación está visible (el usuario no la ha cerrado/visto)
        if (hasActiveSos && notificationToShow?.title?.contains("SOS") == true) {
            while(true) {
                triggerUiSound("siren")
                delay(6000) // Esperamos 6 segundos entre ciclos
                
                // Verificamos si el SOS sigue activo y si el usuario no ha cerrado la alerta
                val stillActive = remoteUsers.any { it.city == radioState.city && it.isSOS }
                if (!stillActive) break
            }
        }
    }
    
    // =======================================================
    // 🔒 HARD-LOCK: GUARDIÁN DE ACTIVIDAD (WATCHDOG)
    // Detecta si el sistema "congela" la app por ahorro de batería
    // =======================================================
    var lastCheckTime by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while(true) {
            val now = getTimeMillis()
            // Si el salto de tiempo es mayor a 12s entre ciclos de 5s, el SO nos durmió
            if (lastCheckTime != 0L && (now - lastCheckTime) > 12000) {
                // Solo disparamos el aviso si estamos en Android (WebApp no tiene estas restricciones)
                if (!isWebPlatform) {
                    showBatteryWarning = true
                    triggerUiSound("siren")
                }
            }
            lastCheckTime = now
            delay(5000)
        }
    }
    var backPressCount by remember { mutableStateOf(0) }
    var isPttLive by remember { mutableStateOf(false) }

    var lastBackPressTime by remember { mutableStateOf(0L) }
    
    var pendingDialog by remember { mutableStateOf<RadioDialogType?>(null) }
    var hasAutoTunedInSession by remember { mutableStateOf(!isFirstTime) }

    // --- 🌍 GPS TRACKING PARA MODO RUTA (REAL) ---
    var lastKnownGpsLocation by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(radioState.activeProfile) {
        if (radioState.activeProfile != ActivityProfile.NORMAL) {
            while(true) {
                // 1. Obtener coordenadas exactas para el radar
                onGpsRequest { url ->
                    if (url != null) {
                        val regex = "q=([-0-9.]+),([-0-9.]+)".toRegex()
                        val match = regex.find(url)
                        if (match != null) {
                            val rawLat = match.groupValues[1].toDoubleOrNull()
                            val rawLon = match.groupValues[2].toDoubleOrNull()
                            if (rawLat != null && rawLon != null) {
                                val lat = (rawLat * 1000.0).toInt() / 1000.0
                                val lon = (rawLon * 1000.0).toInt() / 1000.0
                                radioState = radioState.copy(motoLatitude = lat, motoLongitude = lon, myGpsUrl = url)
                            }
                        }
                    }
                }

                // 2. 🤖 SEGUIMIENTO INTELIGENTE: Detectar por dónde vamos (Barrio/Calle/Pueblo)
                delay(5000)
                onGpsCityRequest { detectedPlace ->
                    if (detectedPlace != null && detectedPlace != lastKnownGpsLocation) {
                        lastKnownGpsLocation = detectedPlace
                        
                        // Si es un pueblo nuevo (está en nuestra lista oficial), cambiamos la radio
                        if (SPAIN_CITIES.contains(detectedPlace.uppercase())) {
                            radioState = radioState.copy(city = detectedPlace.uppercase())
                        }

                        // El locutor informa sobre el cambio (sea barrio o pueblo)
                        VirtualOperator.onZoneChange(detectedPlace, nick) { text ->
                            setVirtualOperatorText(text)
                            // Disparamos el motor FM para que lea el boletín
                            onBgRadioScan(radioState.city, "ANUNCIOS")
                        }
                    }
                }
                
                delay(120000) // Actualizar cada 2 minutos para no drenar batería
            }
        }
    }

    // --- 🚀 DISPARO DE DEEP LINKING (NASA / RUTA) ---
    LaunchedEffect(Unit) {
        if (initialState.forceShowNasa) {
            delay(1000)
            pendingDialog = RadioDialogType.NASA_IMAGE
        }
        
        if (initialState.activeProfile != ActivityProfile.NORMAL) {
            showActivityMap = true
            if (savedNick.isEmpty()) {
                // Si viene de un enlace de ruta y no tiene nick, pedimos identificación inmediata
                pendingDialog = RadioDialogType.SELECT_NICK
            } else {
                // Si ya tiene nick, saltamos directamente a la radio para ver el mapa
                screenState = Screen.RadioCB
            }
        }
    }

    // --- 🌍 AUTO-SINTONIZACIÓN INICIAL ---
    LaunchedEffect(screenState) {
        // 🛡️ FIX: Forzar selector de ciudad si estamos en SEVILLA por defecto al entrar
        if (isFirstTime && screenState == Screen.RadioCB && !hasAutoTunedInSession) {
            delay(1500)
            if (radioState.city == "SEVILLA" && pendingDialog == null) {
                pendingDialog = RadioDialogType.SELECT_CITY
                hasAutoTunedInSession = true
            }
        }
    }

    LaunchedEffect(externalBackPressCount) {
        val now = getTimeMillis()
        // --- 🛡️ FILTRO ANTI-REBOTE (DEBOUNCE): Ignorar pulsaciones accidentales (< 500ms) ---
        if (externalBackPressCount > 0 && (now - lastBackPressTime) > 500) {
            lastBackPressTime = now
            
            // --- 🔒 HARD-LOCK: NAVEGACIÓN JERÁRQUICA (PROTEGIDO - PROHIBIDO SALIR) ---
            // REGLA: El botón atrás NUNCA debe cerrar la Activity, solo retroceder o minimizar.
            // NOTA PARA NAVEGACIÓN: Cualquier nueva ventana o overlay DEBE añadirse al inicio de este 'when'.
            when {
                showWebHelpDialog -> showWebHelpDialog = false
                engineeringPanelVisible -> engineeringPanelVisible = false // 🛡️ Cierre quirúrgico de Consola de Ingeniería
                showActivityRadar -> showActivityRadar = false // 🛡️ Cierre quirúrgico de Radar de Presencia
                showActivityMap -> {
                    // --- 🛡️ FIX: ABRIR DIÁLOGO DE CIERRE DE RUTA ---
                    pendingDialog = RadioDialogType.FINISH_ACTIVITY_CONFIRM
                }
                pendingDialog != null -> pendingDialog = null
                showOnboarding -> showOnboarding = false
                showPrivacy -> showPrivacy = false
                showNotificationConsent -> showNotificationConsent = false
                showBatteryWarning -> showBatteryWarning = false
                localNotification != null -> localNotification = null
                radioState.isWorkModeActive -> radioState = radioState.copy(isWorkModeActive = false)
                radioState.isChatVisible -> radioState = radioState.copy(isChatVisible = false)
                showExitDialog -> {
                    // --- 🛡️ MEJORA NAVEGACIÓN: SEGUNDO PLANO (NUNCA SALIR) ---
                    // Si el diálogo ya está abierto, el siguiente "Atrás" minimiza la radio (segundo plano)
                    onExternalExitRequest(false, false) 
                    showExitDialog = false
                }
                screenState == Screen.RadioCB -> {
                    // En la pantalla principal, el atrás muestra el diálogo de minimización
                    showExitDialog = true
                }
                screenState == Screen.Welcome -> {
                    // En Welcome, minimizamos si pulsa atrás para evitar salir del proceso
                    onExternalExitRequest(false, false)
                }
                else -> {
                    screenState = Screen.RadioCB
                }
            }
        }
    }

    // =======================================================
    // 🔒 HARD-LOCK: GESTIÓN DE BIENVENIDA Y PERMISOS (ANTIBLOQUEO GOOGLE)
    // PROTECCIÓN CRÍTICA: NO MOVER NI CAMBIAR EL ORDEN DE LOS DIÁLOGOS
    // =======================================================
    val startPermissionFlow = {
        if (isFirstTime) {
            showOnboarding = true
        } else {
            // Si no es la primera vez, pasamos a la radio directamente
            if (nick.isNotBlank()) {
                screenState = Screen.RadioCB
            }
        }
    }


    // --- 🔔 SOLICITUD DE NOTIFICACIONES Y BIENVENIDA A NUEVAS FUNCIONES ---
    LaunchedEffect(screenState) {
        if (screenState == Screen.RadioCB) {
            // Esperamos un poco para no saturar al entrar
            delay(3000)
            
            delay(7000)
            if (!radioState.hasAcceptedMicExplain) {
                showNotificationConsent = true
            }
        }
    }

    // --- 🛡️ SISTEMA DE REDIRECCIÓN NATIVA (ANTI-DUPLICADO WEB) ---
    LaunchedEffect(Unit) {
        tryOpenNativeApp()
    }

    LaunchedEffect(forceBgGenre) {
        if (forceBgGenre != null) {
            radioState = radioState.copy(bgRadioGenre = forceBgGenre)
            onBgGenreConsumed()
        }
    }

    // --- 💬 MOTOR DE CONTEO DE MENSAJES NO LEÍDOS ---
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            val latest = chatMessages.last()
            val myNick = nick.trim().uppercase()
            // Solo procesamos si el mensaje no es nuestro y es muy reciente (evitar historial)
            if (latest.senderNick.trim().uppercase() != myNick && latest.timestamp > (getTimeMillis() - 3000)) {
                if (!radioState.isChatVisible) {
                    radioState = radioState.copy(unreadCount = radioState.unreadCount + 1)
                    
                    // --- 🔔 AVISO VISUAL Y SONORO ---
                    triggerUiSound("click")
                    
                    if (latest.text.startsWith("ANUNCIO:")) {
                        val textToRead = latest.text.removePrefix("ANUNCIO: ")
                        onExecuteEngineeringAction("SPEAK|Mensaje de ${latest.senderNick}. $textToRead")
                    }

                    localNotification = AppNotification(
                        title = "MENSAJE DE ${latest.senderNick}",
                        message = if (latest.text.startsWith("ANUNCIO:")) latest.text.removePrefix("ANUNCIO: ") else latest.text,
                        type = NotificationType.Info,
                        actionLabel = "ABRIR",
                        onAction = {
                            radioState = radioState.copy(isChatVisible = true, unreadCount = 0)
                            localNotification = null
                        }
                    )
                } else if (latest.text.startsWith("ANUNCIO:")) {
                    // Si el chat está abierto pero es un ANUNCIO, también lo leemos
                    val textToRead = latest.text.removePrefix("ANUNCIO: ")
                    onExecuteEngineeringAction("SPEAK|Mensaje de ${latest.senderNick}. $textToRead")
                }
            }
        }
    }

    // --- 🎙️ GUARDIÁN DE FRECUENCIA: NOTIFICACIÓN AUTOMÁTICA DE NUEVOS OPERADORES ---
    var lastUserCount by remember { mutableStateOf(0) }
    var lastActiveTxNick by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(remoteUsers, remoteTransmitterName) {
        val currentUsers = remoteUsers.filter { it.city == radioState.city && it.channel == radioState.channel && it.nick != nick }
        
        // 1. Aviso de entrada en frecuencia (Nuevos Operadores)
        if (currentUsers.size > lastUserCount && lastUserCount > 0) {
            val newUser = currentUsers.last()
            localNotification = AppNotification(
                title = "🎙️ NUEVA ESTACIÓN: ${newUser.city}",
                message = "El operador ${newUser.nick} acaba de entrar en frecuencia. ¡Saluda!",
                type = NotificationType.Info
            )
            triggerUiSound("click") // Pitido de aviso
        }
        lastUserCount = currentUsers.size

        // 2. 🤖 ANUNCIADOR POR VOZ (MODO GPS/FONDO)
        // Si alguien empieza a hablar y no es quien hablaba antes, lo anunciamos
        if (remoteTransmitterName != null && remoteTransmitterName != lastActiveTxNick) {
            // Solo anunciamos si el usuario no está viendo la radio (está en Maps o modo Actividad)
            if (radioState.activeProfile != ActivityProfile.NORMAL) {
                val announceText = "Estación $remoteTransmitterName al aire"
                onExecuteEngineeringAction("SPEAK|$announceText")
            }
        }
        lastActiveTxNick = remoteTransmitterName
    }

    LaunchedEffect(radioState.isChatVisible) {
        if (radioState.isChatVisible) {
            radioState = radioState.copy(unreadCount = 0)
        }
    }

    LaunchedEffect(radioState.bgRadioGenre) {
        onBgGenreChangeExternal(radioState.bgRadioGenre)
    }

    LaunchedEffect(dgtText, dgtImageUrl) {
        if (dgtText != null || dgtImageUrl != null) {
            radioState = radioState.copy(dgtText = dgtText, dgtImageUrl = dgtImageUrl)
        }
    }

    LaunchedEffect(nasaImageUrl, nasaImageTitle, nasaImageExplanation, routeDistanceKm, routeDurationMin, routeDestinationName) {
        if (nasaImageUrl != null && nasaImageUrl != radioState.nasaImageUrl) {
            radioState = radioState.copy(
                nasaImageUrl = nasaImageUrl,
                nasaImageTitle = nasaImageTitle,
                nasaImageExplanation = nasaImageExplanation
            )
            // Abrir automáticamente el diálogo al recibir una nueva imagen
            pendingDialog = RadioDialogType.NASA_IMAGE
        }
        if (routeDistanceKm != radioState.routeDistanceKm || 
            routeDurationMin != radioState.routeDurationMin || 
            routeDestinationName != radioState.routeDestinationName ||
            nextNavigationStep != radioState.nextNavigationStep ||
            routeWaypoints != radioState.routeWaypoints) {
            radioState = radioState.copy(
                routeDistanceKm = routeDistanceKm,
                routeDurationMin = routeDurationMin,
                routeDestinationName = routeDestinationName,
                nextNavigationStep = nextNavigationStep,
                routeWaypoints = if (routeWaypoints.isNotEmpty()) routeWaypoints else radioState.routeWaypoints
            )
        }
    }

    // --- 🛡️ GESTIÓN DE SEGURIDAD: RETORNO POR CONFLICTO ---
    LaunchedEffect(forceInitialScreen) {
        if (forceInitialScreen) {
            screenState = Screen.Welcome
        }
    }

    // --- ⏳ GESTIÓN DE ENTRADA A RADIO ---
    var hasInitializedRadio by remember { mutableStateOf(false) }
    LaunchedEffect(screenState) {
        if (screenState == Screen.RadioCB && !hasInitializedRadio) {
            hasInitializedRadio = true
            // --- 🌍 SINTONIZACIÓN GEOGRÁFICA (BAJO DEMANDA) ---
            if (isFirstTime && !hasAutoTunedInSession) {
                onGpsCityRequest { detectedCity ->
                    val upper = detectedCity?.uppercase()
                    if (upper != null && SPAIN_CITIES.contains(upper)) {
                        radioState = radioState.copy(city = upper)
                        hasAutoTunedInSession = true
                    } else {
                        // Si es un barrio o no se detecta bien, forzamos el selector
                        pendingDialog = RadioDialogType.SELECT_CITY
                        hasAutoTunedInSession = true
                    }
                }
            }
            
            // --- 🧠 EFECTO DOPAMINA: Bienvenida por voz del Locutor ---
            if (isFirstTime || savedNick.isNotEmpty()) {
                playWelcomeSequence()
            }
        }
    }

    LaunchedEffect(radioState.hasAcceptedMicExplain, screenState) {
        if (radioState.hasAcceptedMicExplain && nick.isNotBlank() && (screenState == Screen.RadioCB)) {
            onPermissionRequest(nick)
        }
    }

    LaunchedEffect(radioState) { onStateSave(radioState) }
    LaunchedEffect(radioState.city, radioState.isSystemVoiceEnabled, screenState, bgStationName) { 
        onCityChange(radioState.city)
        // --- 🤖 ACTIVACIÓN DE BOT LOCAL (INTEGRACIÓN FM) ---
        if (screenState == Screen.RadioCB && radioState.isSystemVoiceEnabled) {
            // El bot solo habla automáticamente si la radio FM está activa
            val allowAutoBulletins = bgStationName != null
            VirtualOperator.start(radioState.city, nick, allowAutoBulletins) { text ->
                // Guardamos el texto en el puente para que el motor FM lo lea
                if (text.startsWith("TURISMO: ")) {
                    radioState = radioState.copy(tourismInfo = text.removePrefix("TURISMO: "))
                }
                setVirtualOperatorText(text)
                onBgRadioScan(radioState.city, "ANUNCIOS")
            }
        } else {
            VirtualOperator.stop()
        }
    }
    LaunchedEffect(radioState.subtone) { onSubtoneChange(radioState.subtone) }
    LaunchedEffect(radioState.channel) { onChannelChange(radioState.channel) }

    LaunchedEffect(radioState.isMonitorEnabled, radioState.monitorVolume) {
        onMoniVolumeChange(if (radioState.isMonitorEnabled) radioState.monitorVolume else 0f)
    }

    LaunchedEffect(radioState.isReverbEnabled, radioState.reverbLevel) {
        onEchoChange(radioState.isReverbEnabled, radioState.reverbLevel)
    }

    LaunchedEffect(audioIntegrity) {
        if (!audioIntegrity) {
            localNotification = AppNotification(
                title = "⚠️ FALLO DE MODULACIÓN",
                message = "Tu voz no está saliendo. El Guardián está intentando recuperar el micrófono automáticamente...",
                type = NotificationType.Warning
            )
            triggerUiSound("siren")
        }
    }

    MaterialTheme(colorScheme = LuxeColors.Scheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = LuxeColors.Slate900) {
            val background = if (radioState.isNightMode) LuxeColors.NightGradient else LuxeColors.BackgroundGradient
            Box(Modifier.fillMaxSize().background(background)) {
                
                // --- 🛰️ GUARDIÁN DE CONECTIVIDAD (HÍBRIDO) ---
                SurvivalMeshMonitor(
                    state = radioState,
                    onMeshStatusChange = { radioState = radioState.copy(isMeshActive = it) },
                    onExecuteAction = onExecuteEngineeringAction
                )
                if (showOnboarding) {
                    OnboardingDialog(onDismiss = { showOnboarding = false; showPrivacy = true; onOnboardingFinish() })
                }
                
                if (showPrivacy) {
                    PrivacyConsentDialog(
                        onAccept = { 
                            showPrivacy = false
                            if (nick.isNotBlank()) screenState = Screen.RadioCB
                        },
                        onDismiss = { showPrivacy = false }
                    )
                }

                if (showNotificationConsent) {
                    NotificationConsentDialog(
                        onAccept = {
                            showNotificationConsent = false
                            onNotificationPermissionRequest()
                            if (!isWebPlatform && onBatteryCheckRequest()) showBatteryWarning = true
                        },
                        onDismiss = { 
                            showNotificationConsent = false
                            if (!isWebPlatform && onBatteryCheckRequest()) showBatteryWarning = true
                        }
                    )
                }

                if (showBatteryWarning) {
                    AlertDialog(
                        onDismissRequest = { showBatteryWarning = false },
                        containerColor = LuxeColors.DeepSea,
                        modifier = Modifier.padding(16.dp).border(2.dp, LuxeColors.Gold.copy(0.5f), RoundedCornerShape(32.dp)),
                        icon = { Icon(Icons.Rounded.Warning, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
                        title = { Text("🚀 RUTA SEGURA Y SIN CORTES", fontWeight = FontWeight.Black, fontSize = 20.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("¡Vamos a evitar que Android 'duerma' tu radio!", fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(12.dp))
                                Text("Para que no pierdas la posición de tus compañeros en mitad de la ruta y el GPS no se pare, necesitamos que nos pongas como 'Siempre activa'.", fontSize = 13.sp, color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Surface(color = LuxeColors.Gold.copy(0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxeColors.Gold.copy(0.2f))) {
                                    Text("Pulsa CONFIGURAR y elige 'SIN RESTRICCIONES' (Ahorro de batería)", modifier = Modifier.padding(12.dp), fontSize = 11.sp, color = LuxeColors.Gold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                            }
                        },
                        confirmButton = {
                            LuxeButton("¡VALE, CONFIGURAR!", { showBatteryWarning = false; onIgnoreBatteryOptimizations() }, true, Modifier.fillMaxWidth().height(54.dp), LuxeColors.Gold, Color.Black)
                        },
                        dismissButton = { TextButton({ showBatteryWarning = false }) { Text("LUEGO", color = Color.White.copy(0.4f)) } }
                    )
                }



                Crossfade(
                    targetState = if (!isAppReady) null else screenState,
                    modifier = Modifier.fillMaxSize()
                ) { target ->
                    if (target == null) {
                        LoadingScreen(radioState.isNightMode)
                    } else {
                        when (target) {
                            Screen.Welcome -> WelcomeScreen(
                                nick = nick, 
                                onNickChange = { nick = it }, 
                                totalUsers = remoteUsers.size,
                                activeUsers = remoteUsers, 
                                onInstall = onInstallRequest,
                                hasAcceptedMic = radioState.hasAcceptedMicExplain,
                                onMicAccept = {
                                    radioState = radioState.copy(hasAcceptedMicExplain = true)
                                    // Al aceptar el micro, pasamos a la radio directamente
                                    screenState = Screen.RadioCB
                                },
                                onMicRequest = { active, power -> onMicEnable(active, radioState.isRogerBeepEnabled, power) },
                                onConnect = { genre ->
                                    if (nick.isNotBlank()) {
                                        // 🛡️ FIX: Al conectar desde bienvenida, aseguramos entrar en modo radio normal
                                        radioState = radioState.copy(activeProfile = ActivityProfile.NORMAL)
                                        showActivityMap = false

                                        if (genre != null) {
                                            radioState = radioState.copy(bgRadioGenre = genre)
                                            // Disparamos el scan para que al entrar ya esté sonando o buscando
                                            onBgRadioScan(radioState.city, genre)
                                        }
                                        startPermissionFlow()
                                    }
                                },
                                onShowRadar = { pendingDialog = RadioDialogType.RADAR_MAP },
                                isNightMode = radioState.isNightMode
                            )
                            Screen.RadioCB -> RadioPanel(
                                nick = nick,
                                mic = micLevel, 
                                users = remoteUsers, 
                                rx = remoteTransmitterName != null, 
                                transmitterNick = remoteTransmitterName,
                                isBeeping = isBeeping,
                                isPttLive = isPttLive,
                                isCodedRx = isCodedRx,
                                voxActiveExternal = voxActive,
                                onNoise = onNoiseVolumeChange, 
                                onMic = { active, power -> onMicEnable(active, radioState.isRogerBeepEnabled, power) },
                                onInstall = onInstallRequest,
                                onShare = { channel, subtone, proRole, platform -> onShareRequest(radioState.city, channel, subtone, proRole, platform, radioState.routeImage) },
                                onExit = { showExitDialog = true },
                                onLogoutConfirm = { 
                                    // --- 🛡️ LIMPIEZA PROFUNDA (DERECHO AL OLVIDO) ---
                                    nick = "" // Limpiar estado local de Compose inmediatamente
                                    radioState = RadioState() // Resetear configuración a fábrica
                                    onLogout() // Borra nick de localStorage
                                    scope.launch {
                                        delay(800) // Dar tiempo a que el sistema persista los cambios en disco y cierre Firebase
                                        onExternalExitRequest(true, true) // Mata el proceso y limpia datos nativos
                                    }
                                },
                                onMinimizeRequest = {
                                    showExitDialog = false
                                    onExternalExitRequest(false, false) // MINIMIZAR APP
                            },
                            state = radioState,
                            onStateChange = { radioState = it },
                            externalPtt = externalPtt,
                            externalPttBlocked = externalPttBlocked,
                            replayProgress = replayProgress,
                            isReplayReady = isReplayReady,
                            chatMessages = chatMessages,
                            forceChatOpen = forceChatOpen,
                            forceChatTarget = forceChatTarget,
                            onChatOpenConsumed = onChatOpenConsumed,
                            onChatTargetConsumed = onChatTargetConsumed,
                            onSendMessage = onSendMessage,
                            onDeleteMessage = onDeleteMessage,
                            onPrivateChat = onPrivateChatRequest,
                            onPublicChat = onPublicChatRequest,
                            onNotification = { localNotification = it },
                            onReport = onReport,
                            onBlock = { id -> 
                                radioState = radioState.copy(blockedUsers = radioState.blockedUsers + id)
                                onBlockUser(id)
                            },
                            onReplay = onReplayRequest,
                            onGpsRequestPro = onGpsRequest,
                            onShowHelp = { 
                                showWebHelpDialog = true 
                                radioState = radioState.copy(hasSeenWattsIntro = true)
                            },
                            audioIntegrity = audioIntegrity,
                            onAntennaTest = onAntennaTest,
                            bgStationName = bgStationName,
                            onBgRadioScan = onBgRadioScan,
                            onBgRadioStop = onBgRadioStop,
                            onBgVolumeChange = onBgVolumeChange,
                            onBgGenreChange = { newGenre ->
                                radioState = radioState.copy(bgRadioGenre = newGenre)
                            },
                            onVirtualOperatorTrigger = {
                                VirtualOperator.triggerBulletin(radioState.city) { text ->
                                    setVirtualOperatorText(text)
                                    onBgRadioScan(radioState.city, "ANUNCIOS")
                                }
                            },
                            showExitConfirmExternal = showExitDialog,
                            onExitConfirmDismiss = { showExitDialog = false },
                            pendingDialog = pendingDialog,
                            onPendingDialogChange = { dialog, payload -> 
                                pendingDialog = dialog
                                if (payload != null) channelToDelete = payload
                            },
                            radarActivo = radarActivo,
                            radarNivel = nivelPerturbacion,
                            isPttLive = isPttLive,
                            onHertzSentinelRequest = { showActivityRadar = true },
                            onActivityPanelRequest = { showActivityMap = true }
                        )
                    }
                }
            }



            LuxeNotificationOverlay(
                    notification = notificationToShow,
                    onDismiss = { 
                        if (externalNotification != null) onNotificationDismiss()
                        else localNotification = null 
                    }
                )

                // --- 🔒 HARD-LOCK: DIÁLOGO DE SALIDA GLOBAL (MODIFICADO POR SEGURIDAD) ---
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        containerColor = LuxeColors.DeepSea,
                        titleContentColor = LuxeColors.Gold,
                        textContentColor = Color.White,
                        modifier = Modifier.border(1.dp, LuxeColors.GlassBorder, RoundedCornerShape(12.dp)),
                        title = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Rounded.ExitToApp, null, tint = LuxeColors.Gold)
                                Spacer(Modifier.width(12.dp))
                                Text("ESTADO DE LA RADIO", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        },
                        text = { 
                            Column {
                                Text(
                                    "La radio seguirá funcionando en segundo plano para que no pierdas la comunicación.",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(0.7f)
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Text(
                                    "CÓMO APAGARLA POR COMPLETO:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LuxeColors.Gold,
                                    letterSpacing = 1.sp
                                )
                                
                                Text(
                                    "Para cerrar la radio totalmente, sal al escritorio de tu móvil y desliza la aplicación hacia arriba en la pantalla de 'Aplicaciones Recientes'.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        },
                        confirmButton = {
                            // Opción Minimizar (Única vía de salida visual) - Forzada a ROJO por petición del autor
                            Button(
                                onClick = { 
                                    onExternalExitRequest(false, false) 
                                    showExitDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Red, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) { Text("MINIMIZAR RADIO", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) { 
                                Text("CANCELAR", color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold) 
                            }
                        }
                    )
                }

                // Diálogo de Instalación PWA Premium
                if (showInstallPrompt) {
                    AlertDialog(
                        onDismissRequest = onInstallDismiss,
                        containerColor = LuxeColors.Slate800,
                        tonalElevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        icon = { 
                            Image(
                                painter = painterResource(Res.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, LuxeColors.Gold, CircleShape)
                            )
                        },
                        title = { 
                            Text("AÑADIR ACCESO DIRECTO", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Esta función crea un acceso directo en tu pantalla de inicio para entrar rápido a la radio. No instala archivos adicionales ni ocupa espacio extra.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(0.7f),
                                    lineHeight = 20.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "¡SÓLO ES UN ACCESO DIRECTO!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = LuxeColors.Gold
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = onInstallConfirm,
                                colors = ButtonDefaults.buttonColors(containerColor = LuxeColors.Gold, contentColor = Color.Black),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("AÑADIR A INICIO", fontWeight = FontWeight.Black)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onInstallDismiss) {
                                Text("LUEGO", color = Color.White.copy(0.4f))
                            }
                        }
                    )
                }

                // --- 📱 GUÍA ANTI-CORTE (IPHONE / ANDROID / WEB) ---
                if (showWebHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showWebHelpDialog = false },
                        containerColor = LuxeColors.Slate900,
                        modifier = Modifier.padding(16.dp).border(1.dp, LuxeColors.Gold.copy(0.2f), RoundedCornerShape(28.dp)),
                        title = { Text("CENTRO DE AYUDA Y ESTABILIDAD", fontWeight = FontWeight.Black, color = LuxeColors.Gold, fontSize = 16.sp) },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text("Para que la radio no se detenga al apagar la pantalla, sigue la guía según tu caso:", fontSize = 13.sp, color = Color.White.copy(0.8f))
                                
                                Spacer(Modifier.height(20.dp))
                                Text("📱 APP NATIVA (ANDROID):", fontWeight = FontWeight.Bold, color = LuxeColors.ElectricBlue, fontSize = 12.sp)
                                Text("1. Sal de la radio y busca el icono de la app.\n2. Mantén pulsado el icono y dale a 'Información' (i).\n3. Busca 'Ahorro de batería' y selecciona 'SIN RESTRICCIONES'.", fontSize = 12.sp, color = Color.White.copy(0.6f))

                                Spacer(Modifier.height(20.dp))
                                Text("🍏 IPHONE (SAFARI):", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 12.sp)
                                Text("1. Pulsa el botón COMPARTIR (cuadrado con flecha ↑).\n2. Busca y pulsa 'Añadir a la pantalla de inicio'.\n3. Entra desde el nuevo icono de tu escritorio.", fontSize = 12.sp, color = Color.White.copy(0.6f))
                                
                                Spacer(Modifier.height(20.dp))
                                Text("🤖 ANDROID (CHROME):", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 12.sp)
                                Text("1. Pulsa los 3 PUNTOS arriba a la derecha.\n2. Pulsa 'Instalar aplicación' o 'Añadir a pantalla de inicio'.\n3. Entra desde el nuevo icono de tu escritorio.", fontSize = 12.sp, color = Color.White.copy(0.6f))

                                Spacer(Modifier.height(20.dp))
                                Text("⚠️ NOTA PARA WEB:", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 11.sp)
                                Text("En navegadores, asegúrate de que el ahorro de batería de Chrome/Safari esté en 'SIN RESTRICCIONES'.", fontSize = 10.sp, color = Color.White.copy(0.5f))
                                
                                Spacer(Modifier.height(24.dp))
                                HorizontalDivider(color = Color.White.copy(0.1f))
                                Spacer(Modifier.height(16.dp))

                                // --- ⚡ INFO DE POTENCIA (SOLICITADO POR USUARIO PARA AYUDA) ---
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LuxeColors.Gold.copy(0.05f))
                                        .padding(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Speed, null, tint = LuxeColors.Gold, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("POTENCIA Y WATTS (W)", fontWeight = FontWeight.Bold, color = LuxeColors.Gold, fontSize = 13.sp)
                                        Text("Tu indicativo gana potencia cuanto más usas la radio. Al emitir verás tus vatios (W) reales en el vúmetro digital.", fontSize = 11.sp, color = Color.White.copy(0.7f))
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                
                                TextButton(
                                    onClick = { 
                                        showWebHelpDialog = false
                                        showOnboarding = true 
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Info, null, tint = LuxeColors.Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("VER TUTORIAL DE FUNCIONES", color = LuxeColors.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        },
                        confirmButton = {
                            LuxeButton("ENTENDIDO", { showWebHelpDialog = false }, true, Modifier.fillMaxWidth().height(48.dp), LuxeColors.Gold, Color.Black)
                        }
                    )
                }

                if (showActivityRadar) {
                    HertzSentinelScreen(
                        onGetWifiVariance = onGetWifiVariance,
                        onGetHeading = onGetHeading,
                        onGetTilt = onGetTilt,
                        onEstadoCambio = { activo, nivel, modo ->
                            radarActivo = activo
                            nivelPerturbacion = nivel
                            radarModoRango = modo
                        },
                        onShare = { c, s, u, g -> onShareRequest(radioState.city, c, s, u, g, radioState.routeImage) },
                        onNotification = { localNotification = it },
                        onPlaySound = onPlaySound,
                        onExecuteEngineeringAction = onExecuteEngineeringAction,
                        wifiNetworks = wifiNetworks,
                        onRequestPermission = onRequestLocationPermission,
                        onOpenSettings = onOpenSettings,
                        initialRfSensitivity = radioState.radarRfSensitivity,
                        initialMagSensitivity = radioState.radarMagSensitivity,
                        onSensitivityChange = { rf, mag ->
                            radioState = radioState.copy(radarRfSensitivity = rf, radarMagSensitivity = mag)
                        },
                        engineeringResetTrigger = engineeringResetTrigger,
                        onClose = { showActivityRadar = false },
                        wifiAuthResult = wifiVerificationResult,
                        engineeringPanelVisible = engineeringPanelVisible,
                        onEngineeringPanelChange = { engineeringPanelVisible = it }
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showActivityMap,
                    modifier = Modifier.fillMaxSize(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                        ActivityPanel(
                        nick = nick,
                        state = radioState,
                        users = remoteUsers,
                        voxActive = voxActive,
                        rx = remoteTransmitterName != null,
                        onStateChange = { newState -> 
                            radioState = newState
                            // Si el usuario cambia el VOX manualmente, lo propagamos al core
                            onMicEnable(voxActive, radioState.isRogerBeepEnabled, newState.veteranPower)
                            onEchoChange(radioState.isReverbEnabled, radioState.reverbLevel)
                        },
                        onMic = { a, p -> onMicEnable(a, radioState.isRogerBeepEnabled, p) },
                        onExecuteEngineeringAction = onExecuteEngineeringAction,
                        onGpsRequest = onGpsRequest,
                        onShare = { c, s, u, g -> onShareRequest(radioState.city, c, s, u, g, radioState.routeImage) },
                        onPendingDialogChange = { dialog, payload -> 
                            pendingDialog = dialog
                            if (payload != null) channelToDelete = payload
                        },
                        bgStationName = bgStationName,
                        onBgRadioScan = onBgRadioScan,
                        onBgRadioStop = onBgRadioStop,
                        onBgVolumeChange = onBgVolumeChange,
                        onBgGenreChange = { genre -> 
                            radioState = radioState.copy(bgRadioGenre = genre)
                            onBgGenreChangeExternal(genre)
                        },
                        onNotification = { localNotification = it },
                        onGetHeading = onGetHeading,
                        nextInstruction = radioState.nextNavigationStep,
                        isBeeping = isBeeping,
                        externalPtt = externalPtt,
                        externalPttBlocked = externalPttBlocked,
                        replayProgress = replayProgress,
                        isReplayReady = isReplayReady,
                        onReplay = onReplayRequest,
                        onClose = { pendingDialog = RadioDialogType.FINISH_ACTIVITY_CONFIRM },
                        onFinish = { 
                            showActivityMap = false 
                            // --- 🛡️ LIMPIEZA DE RUTA AL FINALIZAR ---
                            radioState = radioState.copy(
                                activeProfile = ActivityProfile.NORMAL,
                                isMotoModeEnabled = false,
                                channel = "GENERAL",
                                subtone = "0000"
                            )
                        },
                        isPttLive = isPttLive
                    )
                }

                // =======================================================
                // 🔒 HARD-LOCK: CAPA MAESTRA DE DIÁLOGOS (Z-INDEX MÁXIMO)
                // PROHIBIDO MOVER: Esta pieza debe ser la ÚLTIMA del Box principal.
                // =======================================================
                if (pendingDialog != null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.3f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { /* Bloqueo de fondo */ }
                            )
                    ) {
                        RadioDialogs(
                            type = pendingDialog,
                            onDismiss = { 
                                pendingDialog = null
                                channelToDelete = null
                            },
                            state = radioState,
                            onStateChange = { newState ->
                                // 🛡️ SINCRONIZACIÓN MAESTRA: Propagar cambios a radioState y al motor de audio
                                val oldState = radioState
                                val oldProfile = radioState.activeProfile
                                radioState = newState
                                
                                // 1. Propagar cambios de micrófono (SOLO SI CAMBIAN REALMENTE)
                                if (oldState.isRogerBeepEnabled != newState.isRogerBeepEnabled || 
                                    oldState.veteranPower != newState.veteranPower) {
                                    onMicEnable(voxActive, newState.isRogerBeepEnabled, newState.veteranPower)
                                }
                                
                                // 2. Propagar cambios de Eco/Reverb (SOLO SI CAMBIAN REALMENTE)
                                if (oldState.isReverbEnabled != newState.isReverbEnabled || 
                                    oldState.reverbLevel != newState.reverbLevel) {
                                    onEchoChange(newState.isReverbEnabled, newState.reverbLevel)
                                }
                                
                                // 3. Propagar cierre de actividad si el perfil vuelve a ser NORMAL
                                if (oldProfile != ActivityProfile.NORMAL && newState.activeProfile == ActivityProfile.NORMAL) {
                                    showActivityMap = false
                                }
                                
                                // 4. 🛡️ PROTECCIÓN DE RUTA: Verificar batería al empezar actividad
                                if (oldProfile == ActivityProfile.NORMAL && newState.activeProfile != ActivityProfile.NORMAL) {
                                    if (!isWebPlatform && onBatteryCheckRequest()) {
                                        showBatteryWarning = true
                                    }
                                }
                            },
                            onAntennaTest = onAntennaTest,
                            onReplay = onReplayRequest,
                            onPublicChat = onPublicChatRequest,
                            onBgRadioScan = onBgRadioScan,
                            onBgRadioStop = onBgRadioStop,
                            onShare = { c, s, u, g -> onShareRequest(c, s, nick, u, g, radioState.routeImage) },
                            onNotification = { localNotification = it },
                            onPlaySound = onPlaySound,
                            onLogoutConfirm = onLogout,
                            onPermissionRequest = onPermissionRequest,
                            onMic = { a, p -> onMicEnable(a, radioState.isRogerBeepEnabled, p) },
                            onGpsRequestPro = onGpsRequest,
                            onGpsCityRequestPro = onGpsCityRequest,
                            onPendingDialogChange = { dialog, payload -> 
                                pendingDialog = dialog
                                if (payload != null) channelToDelete = payload
                            },
                            onGetWifiVariance = onGetWifiVariance,
                            onGetHeading = onGetHeading,
                            onGetTilt = onGetTilt,
                            onNickChange = { 
                                nick = it
                                if (showActivityMap) screenState = Screen.RadioCB
                            },
                            onEstadoCambio = { activo, nivel, modo ->
                                radarActivo = activo
                                nivelPerturbacion = nivel
                                radarModoRango = modo
                            },
                            onExecuteEngineeringAction = { action ->
                                if (action == "INSTALL_APP") {
                                    onInstallRequest()
                                } else {
                                    onExecuteEngineeringAction(action)
                                }
                            },
                            onRequestLocationPermission = onRequestLocationPermission,
                            onOpenSettings = onOpenSettings,
                            users = remoteUsers,
                            nick = nick,
                            channelToDelete = channelToDelete,
                            onPrivateChat = onPrivateChatRequest,
                            onShowHelp = { 
                                showWebHelpDialog = true 
                                radioState = radioState.copy(hasSeenWattsIntro = true)
                            },
                            onHertzSentinelRequest = { showActivityRadar = true },
                            onActivityPanelRequest = { showActivityMap = true },
                            onWaypointReceived = onWaypointReceived,
                            engineeringPanelVisible = engineeringPanelVisible,
                            onEngineeringPanelChange = { engineeringPanelVisible = it }
                        )
                    }
                }
            }
        }
    }
}
