# 🎙️ ON AIR SPAIN - PROTECTED CORE

**Architecture & Engineering by Jose Manuel Gonzalez Lorence**
*Senior Software Architect & Elite Multiplatform Developer*

![Version](https://img.shields.io/badge/Version-Premium_Stable-gold)
![Platform](https://img.shields.io/badge/Platform-Android_%7C_Web-blue)
![Stack](https://img.shields.io/badge/Stack-Kotlin_Multiplatform_%7C_WebRTC-orange)

Esta es la infraestructura central de **ON AIR SPAIN**, la red social de voz real líder en España. La plataforma que recupera la **"Mística de la Radio"** y la compañía de la voz humana.

---

## 🌟 La Visión: "La Compañía de la Voz"
En un mundo saturado de textos, **ON AIR** apuesta por el directo. Escuchar a alguien modulando en tiempo real reduce la sensación de soledad de forma mucho más efectiva que cualquier red social tradicional. No buscamos "contenido", buscamos **presencia**.

---

## 🚀 Características de la Plataforma

### 💼 Networking y Comunidad
- **Networking Profesional**: Canales específicos para conductores, hostelería, construcción y servicios.
- **Terminal Pro**: Herramientas para contacto profesional en tiempo real.

### 📻 Radio CB Digital y Audio Premium
- **Emulación de Banda Ciudadana**: Experiencia real de CB (27MHz) con cobertura nacional.
- **Audio de Alta Fidelidad**: Uso de códec Opus para una claridad cristalina.
- **DSP Radio (Procesador de Voz)**: Hardware simulado para dar pegada y cuerpo de emisora profesional.
- **Roger Beep & Replay**: Historial inteligente de 15 segundos para no perder ni un mensaje.

### 🏍️ Modo Moto y Multideporte (Elite Gear)
- **Filtro de Viento Adaptativo**: Procesado DSP agresivo (Corte 300Hz) para eliminar el ruido del aire y motor en cascos Bluetooth.
- **VOX de Alta Precisión**: Sensibilidad milimétrica ajustable para cada tipo de deporte (Ciclismo, Montaña, Senderismo).
- **Mapa de Ruta en Tiempo Real**: Visualización de compañeros sobre OpenStreetMap con etiquetas de Nick y estado de TX.
- **Red de Supervivencia WiFi Malla**: Sistema híbrido que activa comunicaciones WiFi Direct (P2P) automáticamente si falla la cobertura 4G/5G. Los móviles actúan como puentes repetidores.
- **Perfiles Especializados**: Configuraciones preestablecidas para Socorristas, Montañismo y Grupos Ciclistas.

### 📡 Radar de Presencia y Actividad (WiFi Sensing)
- **Motor de Sensibilidad 80X**: Control de precisión exponencial desde filtrado de ruido máximo hasta escucha absoluta (0.05f).
- **Detección de Presencia Pro**: Localización de actividad física y movimiento tras muros o en ruta mediante fluctuaciones WiFi con discriminación inteligente.
- **Modo Ruta & Grupo**: Visualiza la actividad de compañeros cercanos para salidas en moto, ciclismo o rutas 4x4.
- **Escáner Magnético & Modo Pared**: Localización técnica de cables con tensión y estructuras metálicas.
- **Modo Misterio (EMF)**: Escáner especializado en energía pura con umbral de 0.15f para investigación de campo.
- **Auditoría WiFi Proactiva**: Sistema de detección de vulnerabilidades con consola táctica y conexión automática.
- **Escudo Sónico Ultrasónico**: Bloqueo de privacidad a 21kHz (totalmente silencioso e inaudible).
- **Protocolos de Ingeniería**: Sistema de ráfagas temporizadas de 10s para Apertura de Barreras (Dual IR/EMF), Bloqueo de Arranque y Vending Master.
- **Interfaz Táctica Premium**: Jerarquía visual Emerald con iconos dinámicos y mapa 360º de alta persistencia.

---

## 🛠️ Especificaciones Técnicas
- **Despliegue Multiplataforma**: Android Nativo y WebApp PWA de alto rendimiento (KMP).
- **Bridge Nativo**: Soporte para manos libres, PTT de hardware (Headset/Media) y botones de auriculares. El volumen del sistema se mantiene independiente para máxima compatibilidad.
- **Eficiencia Energética**: Modo ECO inteligente que reduce el consumo de batería significativamente.
- **VOX Adaptativo**: Sistema manos libres con aprendizaje de perfil de voz y ruido ambiente.
- **### Locutor Virtual Inteligente
- **Información de Tráfico Real**: Integración directa con el portal de Datos Abiertos de la **DGT** para informar sobre accidentes y cortes de carretera verídicos.
- **Boletines de Servicio**: Datos meteorológicos, efemérides y curiosidades (NASA) para amenizar la ruta.
- **Ducking Automático**: Sistema de atenuación de la radio FM para priorizar los avisos de seguridad y mensajes del sistema.

---

## ⚖️ Seguridad y Cumplimiento (Play Store)
Esta es una herramienta de comunicación VOIP comunitaria.
*   **Aviso SOS**: En caso de emergencia real con riesgo vital, contacte siempre con el **112**. La alerta SOS de la app es una herramienta de apoyo vecinal y profesional.
*   **Aviso DGT**: Los informes de tráfico proceden de fuentes de datos abiertos (DGT). Se ofrecen con carácter informativo y no eximen al conductor de su responsabilidad.
*   **Seguridad Vial**: **PROHIBIDO** manipular la aplicación manualmente mientras se conduce. El sistema está diseñado para ser escuchado o usado con manos libres/VOX.
*   **Privacidad**: Cumplimiento estricto de RGPD. Los datos de ubicación (GPS) solo se transmiten bajo activación voluntaria del usuario en situaciones de emergencia.

---

## ⚠️ Protocolo de Despliegue y Producción (Play Store)
Para garantizar la estabilidad en las versiones distribuidas a través de Google Play:
1. **Ofuscación (R8/ProGuard)**: El archivo `androidApp/proguard-rules.pro` contiene reglas críticas para evitar el borrado de librerías vitales (`WorkManager`, `App Startup`). **NUNCA** eliminar las reglas de `-keep` sin pruebas exhaustivas en modo release.
2. **Versionado Obligatorio**: Cada subida a producción requiere un incremento del `versionCode` en `androidApp/build.gradle.kts`.
3. **Compilación Limpia**: Antes de generar el App Bundle (AAB), ejecutar siempre `Build > Clean Project` para evitar artefactos residuales de versiones anteriores.

---

## 🔒 PROTOCOLO DE EDICIÓN QUIRÚRGICA (SOLO PARA IA)
**ESTADO: MANDATORIO ABSOLUTO**

Cualquier asistente de IA que trabaje en este proyecto debe seguir estas reglas sin excepción:
1. **OBJETIVO ÚNICO**: Haz **solo y exclusivamente** lo que se te pide en la solicitud actual. limitate a reparar el problema quirurgicamente, sin cambios ediciones probando y suponiendo, limitate a reparar directamente el problema siguiendo las indicaciones especificas sin dar tanto rodeo.
2. **PROHIBIDA LA LECTURA EXTRA**: No analices, leas ni resumas archivos que no sean estrictamente necesarios para la tarea solicitada. Ignora el contexto que no sea relevante al cambio quirúrgico.
3. **PROHIBIDA LA REFACTORIZACIÓN**: No limpies, optimices ni mejores código por iniciativa propia. No toques nada que no se haya pedido explícitamente.
4. **MODO SILENCIOSO**: No propongas mejoras estéticas ni arquitecturales. Si funciona, no se toca salvo orden directa.
5. **CERO PLACEHOLDERS**: Todo código debe ser real y funcional, conectado al sistema actual.
6. **CONTROL DE SUBIDAS**: No realizar `git push` ni `git commit` de forma automática tras cada cambio. Solo subir cuando el usuario lo indique explícitamente al final de la sesión para evitar fragmentar el historial de commits.

---

*© 2024 ON AIR SPAIN - Ingeniería de élite para la comunicación humana.*
