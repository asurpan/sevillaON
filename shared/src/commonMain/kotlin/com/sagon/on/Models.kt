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

val INTERNATIONAL_CITIES = listOf(
    "🌍 CANAL 12 DX GLOBAL",
    "🌎 HISPANOAMÉRICA",
    "🇪🇺 EUROPA DX",
    "🇲🇽 MÉXICO",
    "🇲🇽 CD. DE MÉXICO",
    "🇲🇽 GUADALAJARA",
    "🇲🇽 MONTERREY",
    "🇲🇽 CANCÚN",
    "🇲🇽 TIJUANA",
    "🇦🇷 ARGENTINA",
    "🇦🇷 BUENOS AIRES",
    "🇦🇷 CÓRDOBA",
    "🇦🇷 ROSARIO",
    "🇨🇴 COLOMBIA",
    "🇨🇴 BOGOTÁ",
    "🇨🇴 MEDELLÍN",
    "🇨🇴 CALI",
    "🇨🇱 CHILE",
    "🇨🇱 SANTIAGO",
    "🇵🇪 PERÚ",
    "🇵🇪 LIMA",
    "🇻🇪 VENEZUELA",
    "🇻🇪 CARACAS",
    "🇪🇨 ECUADOR",
    "🇪🇨 QUITO",
    "🇺🇾 URUGUAY",
    "🇺🇾 MONTEVIDEO",
    "🇵🇾 PARAGUAY",
    "🇵🇾 ASUNCIÓN",
    "🇧🇴 BOLIVIA",
    "🇧🇴 LA PAZ",
    "🇵🇷 PUERTO RICO",
    "🇩🇴 REPÚBLICA DOMINICANA",
    "🇨🇺 CUBA",
    "🇬🇹 GUATEMALA",
    "🇨🇷 COSTA RICA",
    "🇵🇦 PANAMÁ",
    "🇸🇻 EL SALVADOR",
    "🇭🇳 HONDURAS",
    "🇳🇮 NICARAGUA",
    "🇺🇸 ESTADOS UNIDOS (USA)",
    "🇺🇸 USA / CALIFORNIA",
    "🇺🇸 USA / MIAMI",
    "🇺🇸 USA / FLORIDA",
    "🇺🇸 USA / NEW YORK",
    "🇺🇸 USA / LOS ANGELES",
    "🇺🇸 USA / TEXAS",
    "🇺🇸 USA / CHICAGO",
    "🇨🇦 CANADÁ",
    "🇵🇹 PORTUGAL",
    "🇵🇹 LISBOA",
    "🇫🇷 FRANCIA",
    "🇫🇷 PARÍS",
    "🇬🇧 REINO UNIDO (UK)",
    "🇬🇧 LONDRES",
    "🇮🇹 ITALIA",
    "🇮🇹 ROMA",
    "🇩🇪 ALEMANIA",
    "🇩🇪 BERLÍN",
    "🇳🇱 PAÍSES BAJOS",
    "🇧🇪 BÉLGICA",
    "🇨🇭 SUIZA",
    "🇦🇹 AUSTRIA",
    "🇮🇪 IRLANDA",
    "🇵🇱 POLONIA",
    "🇷🇴 RUMANÍA",
    "🇲🇦 MARRUECOS",
    "🇲🇦 TÁNGER",
    "🇧🇷 BRASIL",
    "🇧🇷 RÍO DE JANEIRO",
    "🇯🇵 JAPÓN",
    "🇯🇵 TOKIO",
    "🇦🇺 AUSTRALIA"
)

val ALL_CITIES = SPAIN_CITIES + INTERNATIONAL_CITIES

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
    "GUIPÚZCOA" to 32, "VIZCAYA" to 30, "LA RIOJA" to 7, "PALMA DE MALLORCA" to 5,
    "🌍 CANAL 12 DX GLOBAL" to 12, "🌎 HISPANOAMÉRICA" to 14, "🇪🇺 EUROPA DX" to 12,
    "🇲🇽 MÉXICO" to 12, "🇲🇽 CD. DE MÉXICO" to 12, "🇲🇽 GUADALAJARA" to 15, "🇲🇽 MONTERREY" to 18,
    "🇲🇽 CANCÚN" to 21, "🇲🇽 TIJUANA" to 24, "🇦🇷 ARGENTINA" to 14, "🇦🇷 BUENOS AIRES" to 14,
    "🇦🇷 CÓRDOBA" to 16, "🇦🇷 ROSARIO" to 20, "🇨🇴 COLOMBIA" to 15, "🇨🇴 BOGOTÁ" to 15,
    "🇨🇴 MEDELLÍN" to 17, "🇨🇴 CALI" to 22, "🇨🇱 CHILE" to 16, "🇨🇱 SANTIAGO" to 16,
    "🇵🇪 PERÚ" to 17, "🇵🇪 LIMA" to 17, "🇻🇪 VENEZUELA" to 18, "🇻🇪 CARACAS" to 18,
    "🇪🇨 ECUADOR" to 19, "🇪🇨 QUITO" to 19, "🇺🇾 URUGUAY" to 20, "🇺🇾 MONTEVIDEO" to 20,
    "🇵🇾 PARAGUAY" to 21, "🇵🇾 ASUNCIÓN" to 21, "🇧🇴 BOLIVIA" to 22, "🇧🇴 LA PAZ" to 22,
    "🇵🇷 PUERTO RICO" to 23, "🇩🇴 REPÚBLICA DOMINICANA" to 24, "🇨🇺 CUBA" to 25,
    "🇬🇹 GUATEMALA" to 26, "🇨🇷 COSTA RICA" to 27, "🇵ⓐ PANAMÁ" to 28, "🇸🇻 EL SALVADOR" to 29,
    "🇭🇳 HONDURAS" to 30, "🇳🇮 NICARAGUA" to 31, "🇺🇸 ESTADOS UNIDOS (USA)" to 19,
    "🇺🇸 USA / CALIFORNIA" to 19, "🇺🇸 USA / MIAMI" to 19, "🇺🇸 USA / FLORIDA" to 19,
    "🇺🇸 USA / NEW YORK" to 19, "🇺🇸 USA / LOS ANGELES" to 19, "🇺🇸 USA / TEXAS" to 19,
    "🇺🇸 USA / CHICAGO" to 19, "🇨🇦 CANADÁ" to 19, "🇵🇹 PORTUGAL" to 21,
    "🇵🇹 LISBOA" to 21, "🇫🇷 FRANCIA" to 27, "🇫🇷 PARÍS" to 27, "🇬🇧 REINO UNIDO (UK)" to 27,
    "🇬🇧 LONDRES" to 27, "🇮🇹 ITALIA" to 27, "🇮🇹 ROMA" to 27, "🇩🇪 ALEMANIA" to 27,
    "🇩🇪 BERLÍN" to 27, "🇳🇱 PAÍSES BAJOS" to 27, "🇧🇪 BÉLGICA" to 27, "🇨🇭 SUIZA" to 27,
    "🇦🇹 AUSTRIA" to 27, "🇮🇪 IRLANDA" to 27, "🇵🇱 POLONIA" to 27, "🇷🇴 RUMANÍA" to 27,
    "🇲🇦 MARRUECOS" to 28, "🇲🇦 TÁNGER" to 28, "🇧🇷 BRASIL" to 12, "🇧🇷 RÍO DE JANEIRO" to 12,
    "🇯🇵 JAPÓN" to 12, "🇯🇵 TOKIO" to 12, "🇦🇺 AUSTRALIA" to 12
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
    val systemVolume: Float = 0.90f,
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
    val squelch: Float = 0.90f,
    val rfGain: Float = 0.50f,
    val veteranPower: Float = 0.02f, // 1W Inicial para Novatos (0.02 * 50W = 1W)
    val isInterfaceLocked: Boolean = false, 
    val isDiscreteModeEnabled: Boolean = false,
    val hasAcceptedMicExplain: Boolean = false,
    val isDspEnabled: Boolean = true,
    val dspLevel: Float = 0.5f,
    val isBotEnabled: Boolean = true,
    val isReverbEnabled: Boolean = false,
    val reverbLevel: Float = 0.5f,
    val isNightMode: Boolean = false,
    val isChatVisible: Boolean = false,
    val unreadCount: Int = 0,
    val activeProfile: ActivityProfile = ActivityProfile.NORMAL,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.DEEP_SEA
)

enum class BackgroundStyle(
    val id: String,
    val title: String,
    val subtitle: String,
    val overlayType: String = "NONE"
) {
    DEEP_SEA("deep_sea", "Luxe Deep Sea", "Azul marino táctico original", "NONE"),
    LUXE_GOLD("luxe_gold", "Luxe Cyan Royal", "Degradado azul brillante con destellos", "NONE"),
    MILITARY("military", "Verde Militar Táctico", "Camuflaje de aviación militar", "NONE"),
    NEON_ROSA("neon_rosa", "Rosa Cyberpunk", "Neón magenta-rosa de alto contraste", "NONE"),
    CIRCUIT("circuit", "Circuito PCB Electrónico", "Textura de placa electrónica integrada", "CIRCUIT"),
    CARBON("carbon", "Fibra de Carbono 3D", "Trama entramada de carbono técnico", "CARBON"),
    WALKIE_ABS("walkie_abs", "Walkie-Talkie Rugoso", "Plástico técnico ABS de equipo profesional", "ABS_WALKIE"),
    STEEL("steel", "Acero Cepillado", "Textura metálica de chasis de emisora", "STEEL"),
    OLED_BLACK("oled_black", "Negro OLED Puro", "Negro absoluto para ahorro de batería", "NONE")
}

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

enum class NotificationType { Info, Warning, Success, Danger }

enum class ActivityProfile {
    NORMAL, MOTO, CICLISMO, SENDERISMO, PASEO, SOCORRISTAS, CARAVANAS
}

data class RadioDiagData(
    val micPermission: String = "unknown",
    val ctxState: String = "none",
    val txPackets: Int = 0,
    val rxPackets: Map<String, Int> = emptyMap()
)

val cachedLiveNewsMap = mutableMapOf<String, Pair<String, Long>>()

fun updateCachedNewsTopic(city: String, headline: String) {
    val cleanCity = city.split("-")[0].trim().uppercase()
    cachedLiveNewsMap[cleanCity] = Pair(headline, getTimeMillis())
}

fun getDailyDebateTopic(city: String): String {
    val cleanCity = city.split("-")[0].trim().uppercase()
    val cached = cachedLiveNewsMap[cleanCity]
    if (cached != null && (getTimeMillis() - cached.second < 15 * 60 * 1000L)) {
        return cached.first
    }
    
    val dayOfYear = (getTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
    val cityHash = cleanCity.hashCode().let { if (it < 0) -it else it }
    
    val topics = listOf(
        "¿Cuáles son los cruces y rotondas con más peligro y retenciones en $cleanCity?",
        "¿Qué opinas de las nuevas zonas peatonales y regulaciones de tráfico en $cleanCity?",
        "¿Cuáles son los mejores lugares de $cleanCity para una escapada de fin de semana?",
        "¿Deberían regularse de forma más estricta los patinetes eléctricos en $cleanCity?",
        "¿Cuál es el plato o tapa más representativa de la gastronomía de $cleanCity?",
        "¿Preferirías una jornada laboral de 4 días o mantener los horarios actuales?",
        "¿Qué obras o mejoras de infraestructuras necesita urgentemente $cleanCity?",
        "¿Qué tradiciones o eventos culturales definen mejor la identidad de $cleanCity?",
        "¿Cómo ha cambiado el ambiente y el ritmo de vida en $cleanCity en los últimos años?",
        "¿Cuál es tu lugar o rincón favorito de $cleanCity para desconectar?",
        "¿Qué consejo le darías a alguien que viene a visitar por primera vez $cleanCity?",
        "¿Cómo podemos mejorar la convivencia y los espacios verdes en $cleanCity?",
        "¿Qué transporte público funciona mejor en $cleanCity y qué línea necesita mejoras?",
        "¿Cuál ha sido la mejor época histórica o de crecimiento de $cleanCity?",
        "¿Qué tipo de eventos deportivos o culturales te gustaría que se organizaran en $cleanCity?",
        "¿Crees que el teletrabajo ha mejoardo la calidad de vida en $cleanCity?",
        "¿Cuál es el secreto para disfrutar de un buen fin de semana al aire libre en $cleanCity?",
        "¿Cómo fomentar el pequeño comercio y las tiendas tradicionales en $cleanCity?",
        "¿Qué rutas de montaña, senderismo o paisajes recomiendas cerca de $cleanCity?",
        "¿Qué recuerdas con más cariño de la radio analógica y las comunicaciones clásicas?",
        "¿Cuál es el mayor atractivo turístico desconocido que tiene $cleanCity?",
        "¿Qué servicios o equipamientos faltan en los nuevos desarrollos de $cleanCity?",
        "¿Cómo influye el clima y las estaciones del año en la vida cotidiana de $cleanCity?",
        "¿Qué lugares recomendarías en $cleanCity para ir en familia o con amigos el domingo?",
        "¿Cuál es la actividad cultural o festival más destacado de $cleanCity?",
        "¿Qué opinas de la evolución de las comunicaciones de voz frente al texto escrito?",
        "¿Cómo ha evolucionado el ocio nocturno y los puntos de encuentro en $cleanCity?",
        "¿Qué iniciativas comunitarias o de voluntariado destacarías en $cleanCity?",
        "¿Cuál es la carretera o ruta escénica más bonita para recorrer cerca de $cleanCity?",
        "Rueda Libre de Radio en $cleanCity: Saludos, pruebas de modulación e intercambios."
    )
    
    val topicIndex = (dayOfYear + cityHash) % topics.size
    return topics[topicIndex]
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
