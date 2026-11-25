# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V21.0 MASTER SUPREME - BUILD 91)

Este documento establece las LEYES SUPREMAS DE NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido auditado, optimizado y sellado en la versión 21.0 / Build 91. CUALQUIER CAMBIO NO AUTORIZADO ROMPERÁ LA TRANSMISIÓN DE VOZ, LOS LEDS, LA SEGURIDAD FCM, EL WIDGET TÁCTICO O EL SISTEMA DE DEEP LINKING.

---

## 🚫 PROHIBICIÓN TOTAL Y SELLADO (HARD-LOCK V21.0)

Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas sin permiso explícito por escrito del usuario:

### 1. 🔊 CADENA DE VOZ INTOCABLE (`voiceBoost` ➔ `txGate` ➔ `txBus`)
- **Inyección Directa**: La señal del micrófono capturada por `getUserMedia` DEBE pasar por `micSrc ➔ voiceFilter (150Hz) ➔ sensorGain (4.0) ➔ compressor ➔ voiceBoost (1.2)`. `sensorGain` DEBE conectarse obligatoriamente a `compressor` (`sensorGain.connect(compressor)`).
- **Reemplazo Dinámico de Pistas (`replaceTrack`)**: `window.updatePeerSenders()` DEBE actualizar las llamadas activas de PeerJS reinsertando `txBus.stream.getAudioTracks()[0]`.
- **Conexión Multicapa**: `window.app.voiceBoost` DEBE guardarse globalmente y conectarse a `window.app.txGate` tanto en `manosLibres_requestMic()` como en `initAudio()`.
- **Apertura de Puerta PTT (`txGate`)**: En `broadcastPTT`, la ganancia de `txGate` se abre a **1.2x** (`setTargetAtTime(1.2, now, 0.005)`) y `masterRxGain` a **1.2x** para evitar clipping/saturación.
- **Compresor y Ganancias Anti-Distorsión**: `mainCompressor` configurado en `threshold = -10dB`, `ratio = 4:1`, `attack = 15ms` para voz limpia, transparente y sin artefactos de bombeo.
- **AGC Hardware Obligatorio**: `autoGainControl: true` DEBE mantenerse habilitado en `getUserMedia`.

### 2. 🛡️ SEGURIDAD FCM PUSH VÍA CLOUD FUNCTIONS (CERO SERVER KEYS EN CLIENTE)
- **Despacho Backend Exclusivo**: Queda strictly prohibido reintroducir claves de servidor FCM (`fcmKey`) o peticiones HTTP `fetch("https://fcm.googleapis.com/fcm/send", ...)` en el cliente Web/Kotlin (`RadioNetworkManager.kt`).
- **Nodos Realtime DB**: El envío de notificaciones push para chats privados se realiza escribiendo en `/private_inbox/{targetNick}`, y para menciones públicas escribiendo en `/public_mentions/{targetNick}`. La Cloud Function en `functions/index.js` despacha las notificaciones de forma segura.

### 3. ⚡ OPTIMIZACIÓN DE RENDIMIENTO UI & S-METER CANVAS
- **Renderizado de Alto Rendimiento**: Los 20 LEDs del S-Meter en `Screens.kt` DEBEN dibujarse sobre un único componente `Canvas` (`drawRoundRect`). Queda prohibido volver a envolver cada LED en filas de componentes `Box` individuales para evitar recomposiciones masivas.

### 4. 🔗 DEEP LINKING SIN RECARGA DE PÁGINA (`onNewIntent` & `window.onDeepLinkParams`)
- **Sintonía Suave en Vivo**: En `MainActivity.kt`, al abrir un enlace de sintonía con la app ya activa, `onNewIntent` DEBE extraer los parámetros (`city`, `channel`, `subtone`) y evaluar `window.onDeepLinkParams(...)` sin llamar a `webViewInstance?.loadUrl()`.
- **Preservación de Sesión**: Prohibido reiniciar la WebView o recargar la página al tocar enlaces compartidos para no romper las llamadas WebRTC en vivo ni disparar alertas de pestaña duplicada.

### 5. 📟 WIDGET TÁCTICO WALKIE-TALKIE (`RadioWidgetProvider`)
- **Componentes del Widget**: El Widget nativo (`RadioWidgetProvider.kt`, `widget_radio.xml`, `widget_radio_info.xml`) incluye el chasis *Glassmorphism* (`bg_widget_glass.xml`), botón PTT neón cian (`bg_widget_button_ptt.xml`), indicador LED de estado e insignia de canal.
- **Anclaje de 1 Clic**: Se mantiene la función `@JavascriptInterface fun pinWidget()` en `MainActivity.kt` y el diálogo emergente `WIDGET_PROMPT` tras 3.5s de entrada a la radio.

### 6. 📱 COPY COMPATIBLE CON X / TWITTER (LÍMITE 280 CARACTERES)
- **Recorte Inteligente**: Los mensajes de compartir para debates públicos DEBEN limitar el titular a un máximo de 87 caracteres (`topic.take(87) + "..."`), garantizando que la publicación completa nunca supere los 245 caracteres (compatible con la cuenta gratuita de X / Twitter).

### 7. 🎨 REGLA DE ORO DE COMPOSE UI (PREVENCIÓN DE TEXTO CORTADO EN BOTONES)
- **Altura Flexible**: Prohibido asignar `.height(44.dp)` rígido a botones con texto largo. Usar siempre **`.heightIn(min = 44.dp)`** y `contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)`.

### 8. 🎙️ RETENCIÓN, COLA DE PORTADORA Y LEDS S-METER EN EMISIÓN (660ms)
- **Cola de Portadora Analógica**: En `RadioAudioManager.kt`, tras finalizar el tono Roger Beep de 1955Hz, el desenganche de portadora `setTimeout` DEBE mantenerse en **660ms**. En `MainActivity.kt`, la voz TTS DEBE llamar a `playRogerBeepTone(true)` y desenganchar a los **660ms**.
- **Tono Roger Beep Pleno**: `playRogerBeepTone` mantiene ganancia constante `0.50` durante **292ms** con un remate final de **8ms** sin fundidos tempranos.
- **S-Meter LEDs Rojos**: Durante la emisión, Roger Beep o cola de portadora (`isTransmitting || isBeeping`), `signalLevel` DEBE mantenerse en la **zona roja (diodos 17-20 / nivel 0.95f - 1.0f)** con insignia roja `"TX ROGER"` / `"TX PORTADORA"`.

### 9. 📻 SELECTOR DE MODO AL COMPARTIR (`RadioDialogType.SHARE_OPTIONS`)
- **Menú Selector de Formato**: Al pulsar el botón de compartir del canal en la pantalla Nexus o el botón/tarjeta de "FRECUENCIA LIBRE EN... / INVITAR", el sistema despliega `RadioDialogType.SHARE_OPTIONS` permitiendo elegir entre:
  1. `📻 INVITAR A MI CANAL O FRECUENCIA` (`proRole = "INVITE"`): Envía la invitación limpia al canal sin noticia.
  2. `🔥 COMPARTIR DEBATE DE ÚLTIMA HORA` (`proRole = "DEBATE"`): Envía la noticia de última hora acortada para X/WhatsApp.

### 10. 🌐 MATRIZ DE SERVIDORES WEBRTC STUN/TURN GRATUITOS (COSTE 0€)
- **Redundancia Total**: `main.kt` DEBE preservar la matriz redundante de servidores STUN y TURN gratuitos (Google, Cloudflare, Nextcloud, OpenRelay Metered UDP/TCP/TLS) para garantizar que la app funcione siempre de forma gratuita e instantánea en redes móviles 4G/5G con CGNAT sin ningún coste.
  2. `🔥 COMPARTIR DEBATE DE ÚLTIMA HORA` (`proRole = "DEBATE"`): Envía la noticia de última hora acortada para X/WhatsApp.

---

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al usuario. ÚNICAMENTE se permite modificar las secciones selladas tras recibir el **permiso explícito y por escrito del usuario**.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y PROTEGIDO EN VERSIÓN 21.0 / BUILD 91.**
