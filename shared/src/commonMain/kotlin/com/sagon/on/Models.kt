package com.sagon.on

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.*
import kotlin.math.PI

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MODELOS DE ESTADO Y DATOS
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 * 
 * Define la estructura de datos, estados de la radio y constantes nacionales.
 * Blindado contra cambios estructurales que puedan romper la persistencia.
 */

val SPAIN_CITIES = listOf(
    "SEVILLA", "MADRID", "BARCELONA", "VALENCIA", "ALICANTE", "MÁLAGA", "MURCIA", 
    "CÁDIZ", "BIZKAIA", "A CORUÑA", "ISLAS BALEARES", "LAS PALMAS", "STA. CRUZ TENERIFE", 
    "ASTURIAS", "ZARAGOZA", "PONTEVEDRA", "GRANADA", "TARRAGONA", "CÓRDOBA", 
    "GIPUZKOA", "GIRONA", "ALMERÍA", "TOLEDO", "BADAJOZ", "NAVARRA", "JAÉN", 
    "CASTELLÓN", "CANTABRIA", "HUELVA", "VALLADOLID", "CIUDAD REAL", "LEÓN", 
    "LLEIDA", "ALBACETE", "BURGOS / SORIA", "SALAMANCA / ÁVILA", "LOGROÑO / ÁLAVA", 
    "CÁCERES / SEGOVIA", "LUGO / OURENSE / PALENCIA / ZAMORA", 
    "CUENCA / TERUEL / GUADALAJARA / CEUTA / MELILLA", "ESPAÑA (NACIONAL)", "MUNDO (INTERNACIONAL)"
)

enum class Screen { Welcome, RadioCB }

data class QuadItem<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

enum class ActivityProfile {
    NORMAL, MOTO, CICLISMO, SENDERISMO, PASEO, MONTANA, SOCORRISTAS, CAMIONEROS, CARAVANAS, OFFROAD, TACTICO, RUNNING,
    ESQUI, VELA, PARAPENTE, CAZA, PESCA, KAYAK
}

data class RemoteUser(
    val id: String, 
    val nick: String, 
    val isTransmitting: Boolean = false,
    val subtone: String = "0000",
    val city: String = "SEVILLA",
    val channel: String = "GENERAL",
    val isFriend: Boolean = false,
    val signal: Float = 0.8f,
    val txPower: Float = 0.7f,
    // --- 🛠️ MÓDULO PROFESIONAL (AISLADO) ---
    val proRole: String = "CIUDADANO", // Ej: CAMARERO, ELECTRICISTA, SOS
    val isProSeeking: Boolean = false, // True = Busco personal, False = Ofrezco servicio
    val isJustBrowsing: Boolean = false, // Nuevo: Modo espectador en Terminal
    val isWorkAvailable: Boolean = false, // Verde si está activo en el panel
    val isSOS: Boolean = false, // Prioridad absoluta si es una emergencia
    val isMoto: Boolean = false, // Indica si el compañero va en moto
    val bgGenre: String? = null, // Género o emisora que está escuchando (DJ de Ruta)
    val activity: ActivityProfile = ActivityProfile.NORMAL,
    val lat: Double? = null, // Latitud en tiempo real (Modo Moto)
    val lon: Double? = null, // Longitud en tiempo real (Modo Moto)
    val proReputation: Float = 1.0f, // 0.0 a 1.0
    val isBanned: Boolean = false, // Si es true, el equipo se bloquea permanentemente
    val gpsUrl: String? = null // Enlace a Google Maps en caso de SOS
)

data class RadioState(
    val isVoxEnabled: Boolean = false,
    val voxSensitivity: Float = 0.5f,
    val isMonitorEnabled: Boolean = false,
    val monitorVolume: Float = 0.5f,
    val isEchoEnabled: Boolean = false,
    val echoDelay: Float = 0.3f,
    val isRogerBeepEnabled: Boolean = true,
    val isScanning: Boolean = false,
    val city: String = "SEVILLA",
    val channel: String = "GENERAL",
    val subtone: String = "0000",
    val bass: Float = 0.5f,
    val mid: Float = 0.5f,
    val treble: Float = 0.5f,
    val favoriteChannels: Set<String> = emptySet(),
    val favoriteCities: Set<String> = emptySet(),
    val friends: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(), 
    val isPttLatched: Boolean = false,
    val isEcoMode: Boolean = false,
    val squelch: Float = 0.6f,
    val rfGain: Float = 0.5f,
    val veteranPower: Float = 0.7f, 
    val lastActiveTimestamp: Long = 0L, 
    val installTimestamp: Long = 0L, 
    val isInterfaceLocked: Boolean = false, 
    val hasSeenSquelchWarning: Boolean = false,
    // --- 🛠️ ESTADO MÓDULO PROFESIONAL ---
    val isWorkModeActive: Boolean = false, // ¿Está viendo el panel pro?
    val myProRole: String = "CIUDADANO",
    val isProSeeking: Boolean = false,
    val isJustBrowsing: Boolean = false,
    val myIsSOS: Boolean = false,
    val isGpsPrivacyEnabled: Boolean = true, // Protección de zona exacta activada por defecto
    val isAntennaTesting: Boolean = false, // Modo de auto-escucha (Loopback)
    val isDiscreteModeEnabled: Boolean = false, // Modo Discreto: No hablar si la app está de fondo
    val isMotoModeEnabled: Boolean = false, // Modo Moto: Filtro viento + Mapa + Malla WiFi
    val activeProfile: ActivityProfile = ActivityProfile.NORMAL,
    val motoLatitude: Double? = null,
    val motoLongitude: Double? = null,
    val isMeshActive: Boolean = false, // Si está conectado vía WiFi Direct
    val bgRadioVolume: Float = 0.25f, // Volumen de la radio de fondo (Scanner)
    val bgRadioGenre: String = "MIX", // MIX, NOTICIAS, MUSICA
    val favoriteFmStations: Map<String, String> = emptyMap(), // Ciudad -> Nombre Emisora
    val hasSeenProIntro: Boolean = false,
    val hasSeenRadarMagic: Boolean = false,
    val hasSeenEcoIntro: Boolean = false,
    val hasSeenDspIntro: Boolean = false,
    val hasSeenLockIntro: Boolean = false,
    val hasSeenReplayIntro: Boolean = false,
    val hasSeenWattsIntro: Boolean = false,
    val hasSeenFriendsIntro: Boolean = false,
    val hasSeenVoxIntro: Boolean = false,
    val hasSeenMoniIntro: Boolean = false,
    val hasSeenRogerIntro: Boolean = false,
    val hasSeenReverbIntro: Boolean = false,
    val hasSeenChatIntro: Boolean = false,
    val hasSeenScanIntro: Boolean = false,
    val hasSeenFmScanIntro: Boolean = false,
    val hasSeenAdsIntro: Boolean = false,
    val hasSeenInviteIntro: Boolean = false,
    val hasSeenAntennaIntro: Boolean = false,
    val hasSeenDiscreteIntro: Boolean = false,
    val hasSeenMotoIntro: Boolean = false,
    val hasSeenMasterIntro: Boolean = false,
    val hasAcceptedMicExplain: Boolean = false,
    val hasMicPermission: Boolean = false,
    val isSystemVoiceEnabled: Boolean = false, // Control de Red (Apagado por defecto)
    val isDspEnabled: Boolean = true,
    val dspLevel: Float = 0.5f,
    val isReverbEnabled: Boolean = false,
    val reverbLevel: Float = 0.5f,
    val radarRfSensitivity: Float = 1.0f, // Máxima sensibilidad por defecto
    val radarMagSensitivity: Float = 0.7f,  // Calibrada para muros estándar
    val isNightMode: Boolean = false, // Modo nocturno automático
    val isChatVisible: Boolean = false, // Control global de terminal de texto
    val unreadCount: Int = 0, // Contador de mensajes no leídos
    val myGpsUrl: String? = null,
    val dgtText: String? = null, // Información de tráfico textual
    val dgtImageUrl: String? = null, // URL de imagen de cámara DGT
    val tourismInfo: String? = null, // Información turística/histórica de la ciudad
    val nasaImageUrl: String? = null, // Imagen espacial activa del locutor
    val nasaImageTitle: String? = null, // Título de la imagen
    val nasaImageExplanation: String? = null, // Explicación de la imagen
    val forceShowNasa: Boolean = false, // Disparador para deep-linking
    val routeRules: String? = null, // Normas o info de la ruta activa
    val routeImage: String? = null, // Imagen opcional de la ruta
    val capturedCodes: List<CapturedCode> = emptyList(),
    val wifiNetworks: List<WifiNetwork> = emptyList()
)

data class CapturedCode(val id: String, val proto: String, val data: String)

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val signal: Int,
    val security: String,
    val vendor: String = "DESCONOCIDO",
    val isVulnerable: Boolean = false,
    val defaultPassword: String? = null,
    val wpsActive: Boolean = false,
    val wpsPin: String? = null
)

data class AppNotification(
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.Info,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

data class ChatMessage(
    val id: String,
    val senderNick: String,
    val text: String,
    val timestamp: Long,
    val isSystem: Boolean = false
)

// --- 🗺️ UTILIDADES DE GEOLOCALIZACIÓN TÁCTICA ---
fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val phi1 = lat1 * PI / 180.0
    val phi2 = lat2 * PI / 180.0
    val deltaLambda = (lon2 - lon1) * PI / 180.0

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val theta = atan2(y, x)

    return (((theta * 180.0 / PI) + 360.0) % 360.0).toFloat()
}

fun calculateDistanceKms(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Radio de la Tierra en KM
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun getActivityIcon(profile: ActivityProfile): ImageVector {
    return when(profile) {
        ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
        ActivityProfile.CICLISMO -> Icons.Rounded.PedalBike
        ActivityProfile.SENDERISMO -> Icons.Rounded.Terrain
        ActivityProfile.PASEO -> Icons.Rounded.DirectionsWalk
        ActivityProfile.MONTANA -> Icons.Rounded.Landscape
        ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
        ActivityProfile.CAMIONEROS -> Icons.Rounded.LocalShipping
        ActivityProfile.CARAVANAS -> Icons.Rounded.AirportShuttle
        ActivityProfile.OFFROAD -> Icons.Rounded.Agriculture
        ActivityProfile.TACTICO -> Icons.Rounded.Security
        ActivityProfile.RUNNING -> Icons.Rounded.DirectionsRun
        ActivityProfile.ESQUI -> Icons.Rounded.DownhillSkiing
        ActivityProfile.VELA -> Icons.Rounded.Sailing
        ActivityProfile.PARAPENTE -> Icons.Rounded.AirplanemodeActive
        ActivityProfile.CAZA -> Icons.Rounded.Radar
        ActivityProfile.PESCA -> Icons.Rounded.Phishing
        ActivityProfile.KAYAK -> Icons.Rounded.Kayaking
        else -> Icons.Rounded.Person
    }
}

enum class NotificationType { Info, Warning, Success }
