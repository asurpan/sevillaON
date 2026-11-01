# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V19.0 ELITE SUPREME)

Este documento establece las LEYES SUPREMAS de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido auditado y sellado tras resolver la canalización de voz WebRTC, ganancia de micrófono y prevención de colisiones. Cualquier cambio no autorizado romperá la transmisión de voz, los LEDs o la jerarquía de potencia.

## 🚫 PROHIBICIÓN TOTAL Y SELLADO (HARD-LOCK V19.0)
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas sin permiso explícito por escrito del usuario:

1. **Cadena de Voz Intocable (`voiceBoost` ➔ `txGate` ➔ `txBus`)**:
    - **Aislamiento e Inyección Directa**: La señal del micrófono capturada por `getUserMedia` DEBE pasar por `micSrc ➔ voiceFilter (150Hz) ➔ sensorGain (4.0) ➔ compressor ➔ voiceBoost (2.5)`. `sensorGain` DEBE conectarse obligatoriamente a `compressor` (`sensorGain.connect(compressor)`) para llevar la amplificación x4.0 completa al flujo de red.
    - **Reemplazo Dinámico de Pistas (`replaceTrack`)**: `window.updatePeerSenders()` DEBE actualizar las llamadas activas de PeerJS reinsertando `txBus.stream.getAudioTracks()[0]` para asegurar que el audio no se quede estancado en `dummyStream`.
    - **Conexión Multicapa Re-Asegurada**: `window.app.voiceBoost` DEBE guardarse globalmente y conectarse OBLIGATORIAMENTE a `window.app.txGate` tanto en `manosLibres_requestMic()` como en `initAudio()`. Queda prohibido dejar `txGate` sin entrada.
    - **Apertura de Puerta PTT (`txGate`)**: En `broadcastPTT`, la ganancia de `txGate` se abre a **2.5x** (`setTargetAtTime(2.5, now, 0.005)`) garantizando volumen de voz limpio y potente en el bus WebRTC (`txBus`).
    - **AGC Hardware Obligatorio**: `autoGainControl: true` DEBE mantenerse habilitado en `getUserMedia` para que los chips de micrófono de teléfonos Android (Xiaomi, Motorola, Samsung) entreguen ganancia de voz completa.

2. **Llamada Inteligente Anti-Colisión (`mySess < user.id`)**:
    - **Prevención de Glare WebRTC**: En el bucle de mantenimiento de red de `main.kt`, SOLO el cliente con la ID de sesión lexicográficamente menor (`mySess < user.id`) inicia la llamada saliente.
    - Queda estrictamente prohibido que ambos terminales intenten llamarse mutuamente a la vez, evitando choques de red en PeerJS.

3. **Blindaje de Voz y LEDs (WebRTC Sink)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` con `muted=true` y `playsinline`. 
    - **Decoding Bridge**: Se debe conectar cada stream remoto a un nodo `dummy` de volumen **0.01** directo a la salida física.
    - **ICE/TURN 4G Reforzado**: El ruteo WebRTC debe incluir servidores TURN-TCP para saltar CG-NAT de operadoras móviles.
    - **Hardware Keep-Alive (Motor 5Hz)**: Mantener el oscilador **infrasónico de 5Hz** inaudible en la salida física para evitar el modo suspensión del chip de audio.

4. **Sanitización de SessionID y Reconexión Inteligente**:
    - **Sanitización Estricta de PeerJS**: Toda `sessionID` enviada a `new Peer()` DEBE estar sanitizada mediante `cleanNick.replace(/[^A-Z0-9_-]/gi, "_")`. Queda PROHIBIDO enviar espacios, tildes o símbolos a PeerJS.
    - **Reconexión por Hardware (`deviceId`)**: En `onUsersUpdate`, se DEBE verificar `otherDeviceId == myDeviceID`. Si la conexión proviene del mismo terminal físico, es una reconexión legítima y NO debe marcar conflicto ni expulsar al usuario.

5. **Garantía de Roger Beep, Re-activación de AudioContext y Protección Anti-Chime**:
    - **Parámetros Inamovibles del Roger Beep (1955Hz - 300ms)**: El Roger Beep de emisión DEBE mantener tono constante plano de ganancia **0.40** durante **292ms**, seguido de un micro-rampa de finalización de **8ms** (`linearRampToValueAtTime(0.001, now + 0.30)`). Queda prohibido aplicar fundidos progresivos largos que le resten fuerza al tono.
    - **Handshake de Confirmación de Voz Real (LED Nexus)**: `updateRemotePriorities()` mide las muestras PCM entrantes (`audioVol > 0.05`). Al detectar voz real sonando en el altavoz receptor, el nodo escribe `rxFrom: peerNick` en Firebase, activando el LED **`🔴 TX CONFIRMADO`** en la Pantalla Nexus del emisor.
    - **Re-activación de AudioContext**: En `broadcastPTT`, si `window.app.ctx.state !== 'running'`, se DEBE ejecutar `window.app.ctx.resume()` inmediatamente antes de procesar emisión o recepción.
    - **Filtro de Emisión Real para Roger Beep**: El tono Roger Beep de 1955Hz SOLO debe sonar si el usuario estuvo activamente transmitiendo (`wasTransmitting === true`). Al cambiar de canal o pulsar flechas de sintonía con el micro suelto, queda PROHIBIDO emitir el Roger Beep (solo emitirá el clic de 800Hz `switch`).
    - **Protección Anti-Chime de PTT**: Queda ESTRICTAMENTE PROHIBIDO invocar `webkitSpeechRecognition` durante el PTT en la app nativa Android (`window.AndroidApp != null`) para evitar que el sistema emita el pitido ("piri") de Búsqueda por Voz de Google.

6. **Reglas del Chat Táctico, Botón Mute y Buzón Privado en Tiempo Real**:
    - **Botón Mute de Chat Obligatorio**: En `InlineTacticalChat` (`Screens.kt`), el encabezado DEBE incluir el icono táctil de silenciar/activar sonido (`VolumeOff` en rojo cuando está silenciado, `VolumeUp` cuando está activo) llamando a `onChatMutedChange(!isChatMuted)`.
    - **Buzón de Notificaciones Privadas (`private_inbox/NICK`)**: Al enviar un mensaje privado, `RadioNetworkManager.kt` DEBE inyectar una notificación en directo a `private_inbox/TARGET_NICK`. `initFirebaseListener` mantiene la escucha permanente en `private_inbox/MY_NICK` reproduciendo el tono `chat_msg`, lanzando `showSystemNotification("💬 MENSAJE PRIVADO DE...")` y abriendo el chat privado en el móvil destino.
    - **Hora de Publicación**: Todos los mensajes de chat DEBEN mostrar la hora de publicación en formato `[HH:mm]`.
    - **Flujo Multilínea y Auto-Scroll**: Mensajes con `buildAnnotatedString` permitiendo el salto de línea automático sin recortes `...`. La lista desplegada cuenta con `verticalScroll` y Auto-Scroll automático (`animateScrollTo(maxValue)`).

7. **Arquitectura de Segundo Plano Estilo Walkie-Talkie (Android 14/15/16)**:
    - **Foreground Service**: La app nativa debe ejecutar `RadioService` en primer plano con tipos `mediaPlayback|microphone`.
    - **FCM High Priority + WakeLock**: `MyFirebaseMessagingService` debe adquirir un `WakeLock` de CPU de 10s y despertar a `RadioService` al recibir notificaciones push.
    - **Llamadas Nativas (`RadioConnectionService`)**: Registrado con `ConnectionService` y permiso `MANAGE_OWN_CALLS`.

8. **Guiado PWA para Usuarios de iPhone (iOS / Safari)**:
    - La versión Web debe detectar a los usuarios de iPhone en Safari (`isIos && !isStandalone`) y mostrar el cartel flotante `ios-install-banner` indicando pulsar *"Añadir a la pantalla de inicio"*.

9. **Sistema de Acciones de Usuario, Reporte de Portadora, Sanción Comunitaria y Bloqueo (HARD-LOCK)**:
    - **Menú de Acciones de Usuario (`USER_ACTIONS`)**: Al pulsar o mantener pulsada la tarjeta de usuario (`UserCard`), DEBE desplegarse de forma obligatoria el menú con opciones de Chat Privado, Marcar/Quitar Favorito, Reportar Portadora y Bloquear/Desbloquear Estación.
    - **Sanción Comunitaria por Portadora**: El sistema acumula los reportes de portadora en Firebase (`reports`). Al alcanzar 3 reportes en 5 minutos (300.000 ms), se ejecuta automáticamente `dispatch_community_mute`, bloqueando a la estación infractora en toda la frecuencia y emitiendo el aviso de sanción comunitaria.

10. **Prohibición de Modificación del Flujo de Entrada, Canales, Micrófono y Emisión/Recepción (BLINDAJE ABSOLUTO V19.0)**:
    - **Leyes de Flujo Intocable**: El sistema completo de entrada de usuarios (`connectRadio`, `checkHardwareRestore`), cambio de canales/ciudades (`onCityChange`, `onChannelChange`), captura/permisos de micrófono (`ensureMicAccess`, `requestMicPermission`), y flujo de emisión (TX / PTT) y recepción (RX / PeerJS / Audio Sinks / S-Meter) queda **BLINDADO Y SELLADO DE FORMA ABSOLUTA**.
    - **Permiso Explícito Obligatorio**: Queda **ESTRICTAMENTE PROHIBIDO** para cualquier IA, agente o desarrollador alterar, modificar, refactorizar o reestructurar cualquiera de estos flujos por cuenta propia o por error. ÚNICAMENTE se permite modificar estas secciones tras solicitar y recibir el **permiso explícito y por escrito del usuario**.

11. **Sistema de Notificaciones Push Gratuitas Spark (FCM Directo & Menciones @NICK)**:
    - **Push Directo en Plan Spark (0€)**: En `RadioNetworkManager.kt`, al enviar un mensaje de chat privado o una mención pública con `@NICK`, la app realiza una petición Push directa a FCM hacia el canal `user_TARGET` con clave ofuscada.
    - **Despertador y Apertura Directa de Chat**: `MyFirebaseMessagingService.kt` y `MainActivity.kt` procesan los mensajes `private_chat` y `public_mention`, adquiriendo un `WakeLock` de CPU, haciendo sonar la notificación y abriendo el chat privado directamente al pulsar la alerta (`OPEN_CHAT`).

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. NO apliques parches que eliminen los nodos inaudibles o el bridge silencioso, ya que son los pilares que mantienen la voz viva en móviles modernos.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y PROTEGIDO BAJO LICENCIA DE PERMISO EXPLÍCITO (V19.0 ELITE SUPREME).**
