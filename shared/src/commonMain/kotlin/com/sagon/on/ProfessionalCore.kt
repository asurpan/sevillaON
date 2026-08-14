package com.sagon.on

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 🛠️ MÓDULO PROFESIONAL - CORE (AISLADO)
 * GESTIÓN DE ROLES Y LÓGICA LABORAL
 */

data class ProRole(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String
)

val PROFESSIONAL_ROLES = listOf(
    // --- CATEGORÍA: ESTÁNDAR ---
    ProRole("CIUDADANO", "CIUDADANO", Icons.Rounded.Person, "Estación de radio estándar"),

    ProRole("OTROS", "SERVICIOS (VARIOS)", Icons.Rounded.Person, "Pequeños trabajos, recados o servicios no clasificados"),
    
    // --- CATEGORÍA: HOSTELERÍA Y ALIMENTACIÓN ---
    ProRole("CAMARERO", "CAMARERO/A", Icons.Rounded.LocalBar, "Servicio de sala, barra y eventos"),
    ProRole("COCINA", "COCINA / CHEF", Icons.Rounded.Restaurant, "Cocinero/a, ayudante o personal de office"),
    ProRole("PIZZERO", "PIZZERO", Icons.Rounded.LocalPizza, "Elaboración de pizzas y gestión de hornos"),
    ProRole("PANADERIA", "PAN / PASTELERÍA", Icons.Rounded.BakeryDining, "Panadero, pastelero o despacho de pan"),
    
    // --- CATEGORÍA: OFICIOS Y REPARACIONES ---
    ProRole("ALBAÑIL", "ALBAÑILERÍA", Icons.Rounded.Foundation, "Reformas, construcción y albañilería"),
    ProRole("PINTOR", "PINTOR", Icons.Rounded.Brush, "Pintura decorativa e industrial"),
    ProRole("FONTANERO", "FONTANERO", Icons.Rounded.Water, "Reparaciones de fontanería y calefacción"),
    ProRole("ELECTRICISTA", "ELECTRICISTA", Icons.Rounded.Bolt, "Instalaciones y averías eléctricas"),
    ProRole("MECANICO_COCHE", "MECÁNICO COCHE", Icons.Rounded.Build, "Mecánica general de automóviles"),
    ProRole("MECANICO_MOTO", "MECÁNICO MOTO", Icons.Rounded.TwoWheeler, "Mecánica y puesta a punto de motos"),
    ProRole("FRIO", "FRÍO INDUSTRIAL", Icons.Rounded.AcUnit, "Instalación de aire, cámaras y climatización"),
    ProRole("ELECTRODOMESTICOS", "ELECTRODOMÉSTICOS", Icons.Rounded.Kitchen, "Reparación de lavadoras, hornos y hogar"),
    ProRole("REPARACIONES", "REPARA-TODO", Icons.Rounded.Handyman, "Pequeñas reparaciones, persianas y manitas"),

    // --- CATEGORÍA: TECNOLOGÍA ---
    ProRole("INFORMATICA", "INFORMÁTICO / TÉCNICO", Icons.Rounded.Computer, "Reparación de PC, móviles y redes WiFi"),
    
    // --- CATEGORÍA: TRANSPORTE Y LOGÍSTICA ---
    ProRole("MUDANZAS", "MUDANZAS / PORTES", Icons.Rounded.LocalShipping, "Transporte de mercancías y mudanzas"),
    ProRole("REPARTIDOR", "REPARTIDOR", Icons.Rounded.DeliveryDining, "Reparto de paquetería o comida"),
    ProRole("CONDUCTOR", "CONDUCTOR / CHOFER", Icons.Rounded.DirectionsCar, "Servicios de transporte privado o mensajería"),
    
    // --- CATEGORÍA: SALUD Y CUIDADOS ---
    ProRole("SALUD", "CUIDADOS / SALUD", Icons.Rounded.MedicalServices, "Cuidado de mayores, niños o enfermería"),
    ProRole("PELUQUERIA", "PELUQUERÍA / ESTÉTICA", Icons.Rounded.ContentCut, "Peluquero/a, barbería o estética"),
    ProRole("LIMPIEZA", "LIMPIEZA / JARDÍN", Icons.Rounded.CleaningServices, "Mantenimiento, limpieza y jardinería"),
    ProRole("CLASES", "CLASES / PROFESOR", Icons.Rounded.School, "Clases particulares, idiomas o refuerzo"),
    
    // --- CATEGORÍA: SEGURIDAD Y OTROS ---
    ProRole("SEGURIDAD", "SEGURIDAD", Icons.Rounded.Security, "Vigilancia, control de accesos y escolta"),
    ProRole("COMERCIAL", "COMERCIAL / VENTAS", Icons.Rounded.Storefront, "Ventas, atención al cliente y comercial"),

    // --- EMERGENCIA ---
    ProRole("SOS", "EMERGENCIA / SOS", Icons.Rounded.Warning, "Necesito ayuda inmediata o aviso de peligro")
)

fun getRoleById(id: String) = PROFESSIONAL_ROLES.find { it.id == id } ?: PROFESSIONAL_ROLES[0]
