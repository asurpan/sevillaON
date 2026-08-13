package com.sagon.on

/**
 * 🗺️ BASE DE DATOS DE COORDENADAS NACIONALES
 * Proporciona el centro geográfico de las provincias para el Radar Privado.
 */
object CityCoordinates {
    private val coords = mapOf(
        "SEVILLA" to Pair(37.3891, -5.9845),
        "MADRID" to Pair(40.4168, -3.7038),
        "BARCELONA" to Pair(41.3851, 2.1734),
        "VALENCIA" to Pair(39.4699, -0.3763),
        "ALICANTE" to Pair(38.3452, -0.4810),
        "MÁLAGA" to Pair(36.7213, -4.4214),
        "MURCIA" to Pair(37.9922, -1.1307),
        "CÁDIZ" to Pair(36.5271, -6.2886),
        "BIZKAIA" to Pair(43.2630, -2.9350),
        "A CORUÑA" to Pair(43.3623, -8.4115),
        "ISLAS BALEARES" to Pair(39.5696, 2.6502),
        "LAS PALMAS" to Pair(28.1235, -15.4363),
        "STA. CRUZ TENERIFE" to Pair(28.4636, -16.2518),
        "ASTURIAS" to Pair(43.3603, -5.8448),
        "ZARAGOZA" to Pair(41.6488, -0.8891),
        "PONTEVEDRA" to Pair(42.4310, -8.6444),
        "GRANADA" to Pair(37.1773, -3.5986),
        "TARRAGONA" to Pair(41.1189, 1.2445),
        "CÓRDOBA" to Pair(37.8882, -4.7794),
        "GIPUZKOA" to Pair(43.3183, -1.9812),
        "GIRONA" to Pair(41.9794, 2.8214),
        "ALMERÍA" to Pair(36.8340, -2.4637),
        "TOLEDO" to Pair(39.8628, -4.0273),
        "BADAJOZ" to Pair(38.8794, -6.9706),
        "NAVARRA" to Pair(42.8125, -1.6458),
        "JAÉN" to Pair(37.7796, -3.7849),
        "CASTELLÓN" to Pair(39.9864, -0.0513),
        "CANTABRIA" to Pair(43.4623, -3.8099),
        "HUELVA" to Pair(37.2614, -6.9447),
        "VALLADOLID" to Pair(41.6523, -4.7245),
        "CIUDAD REAL" to Pair(38.9848, -3.9274),
        "LEÓN" to Pair(42.5987, -5.5671),
        "LLEIDA" to Pair(41.6176, 0.6200),
        "ALBACETE" to Pair(38.9944, -1.8585),
        "BURGOS / SORIA" to Pair(42.3440, -3.6969),
        "SALAMANCA / ÁVILA" to Pair(40.9701, -5.6635),
        "LOGROÑO / ÁLAVA" to Pair(42.4627, -2.4450),
        "CÁCERES / SEGOVIA" to Pair(39.4753, -6.3722),
        "LUGO / OURENSE / PALENCIA / ZAMORA" to Pair(43.0125, -7.5522),
        "CUENCA / TERUEL / GUADALAJARA / CEUTA / MELILLA" to Pair(40.0704, -2.1374),
        "ESPAÑA (NACIONAL)" to Pair(40.4637, -3.7492)
    )

    fun get(city: String): Pair<Double, Double>? {
        return coords[city.uppercase()]
    }
}
