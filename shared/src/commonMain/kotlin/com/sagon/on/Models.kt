package com.sagon.on

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MODELOS DE ESTADO Y DATOS
 * ESTADO: SELLADO TOTAL - VERSIÓN ESTABLE 7.0 (PURE RADIO)
 */

val SPAIN_CITIES = listOf(
    "A CORUÑA", "ÁLAVA", "ALBACETE", "ALICANTE", "ALMERÍA", "ASTURIAS", "ÁVILA", 
    "BADAJOZ", "BARCELONA", "BIZKAIA", "BURGOS", "CÁCERES", "CÁDIZ", "CANTABRIA", 
    "CASTELLÓN", "CEUTA", "CIUDAD REAL", "CÓRDOBA", "CUENCA", "GIPUZKOA", "GIRONA", 
    "GRANADA", "GUADALAJARA", "GUIPÚZCOA", "HUELVA", "HUESCA", "ISLAS BALEARES", 
    "JAÉN", "LA RIOJA", "LAS PALMAS", "LEÓN", "LLEIDA", "LOGROÑO", "LUGO", 
    "MADRID", "MÁLAGA", "MELILLA", "MURCIA", "NAVARRA", "OURENSE", "PALENCIA", 
    "PALMA DE MALLORCA", "PONTEVEDRA", "SALAMANCA", "STA. CRUZ TENERIFE", 
    "SEGOVIA", "SEVILLA", "SORIA", "TARRAGONA", "TERUEL", "TOLEDO", "VALENCIA", 
    "VALLADOLID", "VIZCAYA", "ZAMORA", "ZARAGOZA"
)

val CITY_CHANNELS = mapOf(
    "SEVILLA" to 27, "MADRID" to 19, "BARCELONA" to 9, "VALENCIA" to 14,
    "ALICANTE" to 12, "MÁLAGA" to 20, "MURCIA" to 15, "CÁDIZ" to 21,
    "BIZKAIA" to 30, "A CORUÑA" to 35, "ISLAS BALEARES" to 5, "LAS PALMAS" to 3,
    "STA. CRUZ TENERIFE" to 4, "ASTURIAS" to 31, "ZARAGOZA" to 25, "PONTEVEDRA" to 36,
    "GRANADA" to 22, "TARRAGONA" to 10, "CÓRDOBA" to 26, "GIPUZKOA" to 32,
    "GIRONA" to 11, "ALMERÍA" to 23, "TOLEDO" to 18, "BADAJOZ" to 24,
    "NAVARRA" to 33, "JAÉN" to 28, "CASTELLÓN" to 13, "CANTABRIA" to 34,
    "HUELVA" to 29, "VALLADOLID" to 17, "CIUDAD REAL" to 16, "LEÓN" to 37,
    "LLEIDA" to 38, "ALBACETE" to 39, "BURGOS" to 40, "SORIA" to 1,
    "SALAMANCA" to 2, "ÁVILA" to 6, "LOGROÑO" to 7, "ÁLAVA" to 8,
    "CÁCERES" to 1, "SEGOVIA" to 2, "LUGO" to 6, "OURENSE" to 7,
    "PALENCIA" to 8, "ZAMORA" to 1, "CUENCA" to 2, "TERUEL" to 6,
    "GUADALAJARA" to 7, "CEUTA" to 8, "MELILLA" to 1, "HUESCA" to 2,
    "GUIPÚZCOA" to 32, "VIZCAYA" to 30, "LA RIOJA" to 7, "PALMA DE MALLORCA" to 5
)

enum class Screen { Welcome, RadioCB }

data class RemoteUser(
    val id: String, 
    val nick: String, 
    val isTransmitting: Boolean = false,
    val subtone: String = "0000",
    val city: String = "SEVILLA",
    val channel: String = "SEVILLA",
    val isFriend: Boolean = false,
    val signal: Float = 0.8f,
    val txPower: Float = 0.7f,
    val roger: Boolean = true,
    val lastSeen: Long = 0
)

data class RadioState(
    val isVoxEnabled: Boolean = false,
    val voxSensitivity: Float = 0.5f,
    val isMonitorEnabled: Boolean = false,
    val monitorVolume: Float = 0.7f,
    val systemVolume: Float = 0.7f,
    val isEchoEnabled: Boolean = false,
    val echoDelay: Float = 0.3f,
    val isRogerBeepEnabled: Boolean = true,
    val isScanning: Boolean = false,
    val city: String = "SEVILLA",
    val channel: String = "SEVILLA",
    val subtone: String = "0000",
    val favoriteChannels: Set<String> = emptySet(),
    val favoriteCities: Set<String> = emptySet(),
    val friends: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(), 
    val isPttLatched: Boolean = false,
    val isEcoMode: Boolean = false,
    val squelch: Float = 0.55f,
    val rfGain: Float = 0.5f,
    val veteranPower: Float = 0.7f, 
    val isInterfaceLocked: Boolean = false, 
    val isDiscreteModeEnabled: Boolean = false,
    val hasAcceptedMicExplain: Boolean = false,
    val isDspEnabled: Boolean = true,
    val dspLevel: Float = 0.5f,
    val isReverbEnabled: Boolean = false,
    val reverbLevel: Float = 0.5f,
    val isNightMode: Boolean = false,
    val isChatVisible: Boolean = false,
    val unreadCount: Int = 0,
    val activeProfile: ActivityProfile = ActivityProfile.NORMAL
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

enum class NotificationType { Info, Warning, Success }

enum class ActivityProfile {
    NORMAL, MOTO, CICLISMO, SENDERISMO, PASEO, SOCORRISTAS, CARAVANAS
}

fun getActivityIcon(profile: ActivityProfile): ImageVector {
    return when (profile) {
        ActivityProfile.MOTO -> Icons.Rounded.TwoWheeler
        ActivityProfile.CICLISMO -> Icons.AutoMirrored.Rounded.DirectionsBike
        ActivityProfile.SENDERISMO -> Icons.Rounded.Hiking
        ActivityProfile.PASEO -> Icons.AutoMirrored.Rounded.DirectionsWalk
        ActivityProfile.SOCORRISTAS -> Icons.Rounded.MedicalServices
        ActivityProfile.CARAVANAS -> Icons.Rounded.RvHookup
        else -> Icons.Rounded.Person
    }
}
