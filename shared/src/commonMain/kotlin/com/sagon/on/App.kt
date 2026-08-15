package com.sagon.on

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - LÓGICA DE APLICACIÓN Y PERSISTENCIA
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 6.0 (RADIO PURA)
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
    onOnboardingFinish: () -> Unit = {},
    onPermissionRequest: (String) -> Unit,
    onLogout: () -> Unit,
    onInstallRequest: () -> Unit = {},
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
    onPrivateChatRequest: (String) -> Unit = {},
    onPublicChatRequest: () -> Unit,
    onStateSave: (RadioState) -> Unit,
    onConnectRadio: (String) -> Unit = {},
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
    onAntennaTest: (Boolean) -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onChatOpenConsumed: () -> Unit = {},
    onChatTargetConsumed: () -> Unit = {},
    voxActive: Boolean = false,
    initialState: RadioState
) {
    var screenState by remember { 
        mutableStateOf(
            if (savedNick.isNotEmpty() && initialState.hasAcceptedMicExplain) Screen.RadioCB 
            else Screen.Welcome
        ) 
    }

    var channelToDelete by remember { mutableStateOf<String?>(null) }
    var isAppReady by remember { mutableStateOf(true) }
    var nick by remember { mutableStateOf(savedNick) }

    LaunchedEffect(nick) {
        if (nick.isNotBlank()) {
            onConnectRadio(nick)
        }
    }

    var radioState by remember { mutableStateOf(initialState) }
    var localNotification by remember { mutableStateOf<AppNotification?>(null) }

    LaunchedEffect(Unit) {
        while(true) {
            val hour = getCurrentHour()
            val isNight = hour >= 22 || hour < 7
            if (radioState.isNightMode != isNight) {
                radioState = radioState.copy(isNightMode = isNight)
            }
            delay(60000)
        }
    }
    
    val isWebPlatform = remember {
        val name = getPlatform().name.uppercase()
        name.contains("CHROME") || name.contains("SAFARI") || 
        name.contains("FIREFOX") || name.contains("EDGE") ||
        name.contains("WEB") || name.contains("UNKNOWN")
    }
    
    var showPrivacy by remember { mutableStateOf(false) }
    var showNotificationConsent by remember { mutableStateOf(false) }
    var showBatteryWarning by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    var lastBackPressTime by remember { mutableStateOf(0L) }
    var pendingDialog by remember { mutableStateOf<RadioDialogType?>(null) }

    val notificationToShow = externalNotification ?: localNotification
    
    LaunchedEffect(externalBackPressCount) {
        val now = getTimeMillis()
        if (externalBackPressCount > 0 && (now - lastBackPressTime) > 500) {
            lastBackPressTime = now
            when {
                pendingDialog != null -> pendingDialog = null
                showPrivacy -> showPrivacy = false
                showNotificationConsent -> showNotificationConsent = false
                showBatteryWarning -> showBatteryWarning = false
                localNotification != null -> localNotification = null
                radioState.isChatVisible -> radioState = radioState.copy(isChatVisible = false)
                showExitDialog -> {
                    onExternalExitRequest(false, false) 
                    showExitDialog = false
                }
                screenState == Screen.RadioCB -> showExitDialog = true
                screenState == Screen.Welcome -> onExternalExitRequest(false, false)
                else -> screenState = Screen.RadioCB
            }
        }
    }

    val startPermissionFlow = {
        if (isFirstTime) {
            onOnboardingFinish()
            if (nick.isNotBlank()) screenState = Screen.RadioCB
        } else {
            if (nick.isNotBlank()) screenState = Screen.RadioCB
        }
    }

    LaunchedEffect(screenState) {
        if (screenState == Screen.RadioCB) {
            delay(3000)
            if (!radioState.hasAcceptedMicExplain) {
                showNotificationConsent = true
            }
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            val latest = chatMessages.last()
            val myNick = nick.trim().uppercase()
            if (latest.senderNick.trim().uppercase() != myNick && latest.timestamp > (getTimeMillis() - 3000)) {
                if (!radioState.isChatVisible) {
                    radioState = radioState.copy(unreadCount = radioState.unreadCount + 1)
                    triggerUiSound("click")
                    localNotification = AppNotification(
                        title = "MENSAJE DE ${latest.senderNick}",
                        message = latest.text,
                        type = NotificationType.Info,
                        actionLabel = "ABRIR",
                        onAction = {
                            radioState = radioState.copy(isChatVisible = true, unreadCount = 0)
                            localNotification = null
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(radioState.isChatVisible) {
        if (radioState.isChatVisible) {
            radioState = radioState.copy(unreadCount = 0)
        }
    }

    LaunchedEffect(forceInitialScreen) {
        if (forceInitialScreen) {
            screenState = Screen.Welcome
        }
    }

    LaunchedEffect(radioState.hasAcceptedMicExplain, screenState) {
        if (radioState.hasAcceptedMicExplain && nick.isNotBlank() && (screenState == Screen.RadioCB)) {
            onPermissionRequest(nick)
        }
    }

    LaunchedEffect(radioState) { onStateSave(radioState) }
    LaunchedEffect(radioState.city) { onCityChange(radioState.city) }
    LaunchedEffect(radioState.subtone) { onSubtoneChange(radioState.subtone) }
    LaunchedEffect(radioState.channel) { onChannelChange(radioState.channel) }

    // 🔒 PROTECCIÓN: Silenciar QRM en pantallas de login/landing
    LaunchedEffect(screenState) {
        if (screenState == Screen.Welcome) {
            onNoiseVolumeChange(0f)
        }
    }

    LaunchedEffect(radioState.isMonitorEnabled, radioState.monitorVolume) {
        onMoniVolumeChange(if (radioState.isMonitorEnabled) radioState.monitorVolume else 0f)
    }

    LaunchedEffect(radioState.isReverbEnabled, radioState.reverbLevel) {
        onEchoChange(radioState.isReverbEnabled, radioState.reverbLevel)
    }

    MaterialTheme(colorScheme = LuxeColors.Scheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = LuxeColors.Slate900) {
            val background = if (radioState.isNightMode) LuxeColors.NightGradient else LuxeColors.BackgroundGradient
            Box(Modifier.fillMaxSize().background(background)) {
                
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
                        title = { Text("AHORRO DE BATERÍA", fontWeight = FontWeight.Black, fontSize = 20.sp, color = LuxeColors.Gold, textAlign = TextAlign.Center) },
                        text = {
                            Text("Para que la radio no se corte, necesitamos que nos pongas como 'Sin restricciones' en los ajustes de batería.", color = Color.White, textAlign = TextAlign.Center)
                        },
                        confirmButton = {
                            LuxeButton("CONFIGURAR", { showBatteryWarning = false; onIgnoreBatteryOptimizations() }, true, Modifier.fillMaxWidth().height(54.dp), LuxeColors.Gold, Color.Black)
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
                                hasAcceptedMic = radioState.hasAcceptedMicExplain,
                                onMicAccept = {
                                    radioState = radioState.copy(hasAcceptedMicExplain = true)
                                    screenState = Screen.RadioCB
                                },
                                onConnect = { 
                                    if (nick.isNotBlank()) {
                                        onConnectRadio(nick)
                                        startPermissionFlow()
                                    }
                                },
                                isNightMode = radioState.isNightMode
                            )
                            Screen.RadioCB -> RadioPanel(
                                nick = nick,
                                mic = micLevel, 
                                users = remoteUsers, 
                                rx = remoteTransmitterName != null, 
                                transmitterNick = remoteTransmitterName,
                                isBeeping = isBeeping,
                                onNoise = onNoiseVolumeChange, 
                                onMic = { active, power -> onMicEnable(active, radioState.isRogerBeepEnabled, power) },
                                onShare = { channel, subtone, proRole, platform -> onShareRequest(radioState.city, channel, subtone, proRole, platform, null) },
                                onExit = { showExitDialog = true },
                                state = radioState,
                                onStateChange = { radioState = it },
                                externalPtt = externalPtt,
                                externalPttBlocked = externalPttBlocked,
                                replayProgress = replayProgress,
                                isReplayReady = isReplayReady,
                                onSendMessage = onSendMessage,
                                onReport = onReport,
                                onBlock = { id -> 
                                    radioState = radioState.copy(blockedUsers = radioState.blockedUsers + id)
                                    onBlockUser(id)
                                },
                                onReplay = onReplayRequest,
                                pendingDialog = pendingDialog,
                                onPendingDialogChange = { dialog, payload -> 
                                    pendingDialog = dialog
                                    if (payload != null) channelToDelete = payload
                                }
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
                            Text("La radio seguirá funcionando en segundo plano.", fontSize = 13.sp, color = Color.White.copy(0.7f))
                        },
                        confirmButton = {
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

                if (pendingDialog != null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.3f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { /* Lock */ }
                            )
                    ) {
                        RadioDialogs(
                            type = pendingDialog,
                            onDismiss = { 
                                pendingDialog = null
                                channelToDelete = null
                            },
                            state = radioState,
                            onStateChange = { radioState = it },
                            onAntennaTest = onAntennaTest,
                            onReplay = onReplayRequest,
                            onPublicChat = onPublicChatRequest,
                            onShare = { c, s, u, g -> onShareRequest(radioState.city, c, s, u, g, null) },
                            onNotification = { localNotification = it },
                            onLogoutConfirm = onLogout,
                            onMic = { a, p -> onMicEnable(a, radioState.isRogerBeepEnabled, p) },
                            onPendingDialogChange = { dialog, payload -> 
                                pendingDialog = dialog
                                if (payload != null) channelToDelete = payload
                            },
                            onNickChange = { nick = it },
                            users = remoteUsers,
                            nick = nick,
                            channelToDelete = channelToDelete
                        )
                    }
                }
            }
        }
    }
}
