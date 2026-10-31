# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V18.0 ELITE SUPREME)

Este documento establece las LEYES SUPREMAS de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado tras una auditoría profunda de WebRTC, AudioContext, PeerJS y servicios en segundo plano Android. Cualquier cambio no autorizado romperá la estabilidad de la voz, los LEDs o la jerarquía de potencia.

## 🚫 PROHIBICIÓN TOTAL (HARD-LOCK)
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Blindaje de Voz y LEDs (WebRTC Sink)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` con `muted=true` y `playsinline`. 
    - **Decoding Bridge**: Se debe conectar cada stream remoto a un nodo `dummy` de volumen **0.01** (Aumentado para Chrome Mobile) directo a la salida física. Esto engaña al navegador para que mantenga la decodificación activa y los LEDs brillen.
    - **ICE/TURN 4G Reforzado**: El ruteo WebRTC debe incluir servidores TURN-TCP para saltar CG-NAT de operadoras móviles.
    - **Hardware Keep-Alive (Motor 5Hz)**: Mantener el oscilador **infrasónico de 5Hz** inaudible en la salida física para evitar el modo suspensión del chip de audio. Debe silenciarse automáticamente cuando se detecta voz real entrante.
    - **Tono de Modo Oreja (Piripi)**: Al activar el Modo Oreja se ejecuta obligatoriamente la señal audible "incoming" (piripi) y la vibración háptica de 35ms.
    - **Permiso de Micrófono Android 11+ (MIUI/OneUI)**: Invocación obligatoria mediante `@JavascriptInterface fun requestMicPermission()` en `runOnUiThread`. Evita la cancelación silenciosa de permisos de WebView en Xiaomi y Samsung.

2. **Sanitización de SessionID y Reconexión Inteligente (V18.0)**:
    - **Sanitización Estricta de PeerJS**: Toda `sessionID` pasada a `new Peer()` DEBE estar sanitizada mediante `cleanNick.replace(/[^A-Z0-9_-]/gi, "_")`. Queda PROHIBIDO enviar espacios, tildes o símbolos a PeerJS para evitar el error catastrófico `invalid-id`.
    - **Reconexión por Hardware (`deviceId`)**: En `onUsersUpdate`, se DEBE verificar `otherDeviceId == myDeviceID`. Si la conexión previa proviene del mismo terminal físico, es una reconexión legítima y NO debe marcar conflicto ni expulsar al usuario.
    - **Prohibición de Logout en Bucle**: Queda prohibido llamar a `RadioPersistence.logout()` o borrar `localStorage` automáticamente en caso de coincidencia de nick.

3. **Garantía de Roger Beep y Re-activación de AudioContext**:
    - **Re-activación de AudioContext**: En `broadcastPTT`, si `window.app.ctx.state !== 'running'`, se DEBE ejecutar `window.app.ctx.resume()` inmediatamente antes de procesar emisión o recepción.
    - **Filtro de Emisión Real para Roger Beep**: El tono Roger Beep de 1955Hz SOLO debe sonar si el usuario estuvo activamente transmitiendo (`wasTransmitting === true`). Al cambiar de canal o pulsar flechas de sintonía con el micro suelto, queda PROHIBIDO emitir el Roger Beep (solo emitirá el clic de 800Hz `switch`).

4. **Reglas del Chat Táctico y Auto-Scroll**:
    - **Hora de Publicación**: Todos los mensajes de chat DEBEN mostrar la hora de publicación en formato `[HH:mm]`.
    - **Sonido y Notificación de Privados**: Los mensajes privados y avisos de amigos conectándose DEBEN emitir siempre notificación sonora nativa.
    - **Opción de Silenciar Chat Público**: El chat público se puede silenciar mediante `isChatMuted`, pero los mensajes privados NUNCA se silencian.
    - **Flujo Multilínea y Auto-Scroll**: Los mensajes se renderizan mediante `buildAnnotatedString` permitiendo el salto de línea automático sin recortes `...`. La lista desplegada cuenta con `verticalScroll` y Auto-Scroll automático (`animateScrollTo(maxValue)`).

5. **Arquitectura de Segundo Plano Estilo Walkie-Talkie (Android 14/15/16)**:
    - **Foreground Service**: La app nativa debe ejecutar `RadioService` en primer plano con tipos `mediaPlayback|microphone`.
    - **FCM High Priority + WakeLock**: `MyFirebaseMessagingService` debe adquirir un `WakeLock` de CPU de 10s y despertar a `RadioService` al recibir notificaciones push.
    - **Llamadas Nativas (`RadioConnectionService`)**: Registrado con `ConnectionService` y permiso `MANAGE_OWN_CALLS` para integrarse con la pila nativa de telefonía de Android.

6. **Guiado PWA para Usuarios de iPhone (iOS / Safari)**:
    - La versión Web debe detectar a los usuarios de iPhone en Safari (`isIos && !isStandalone`) y mostrar el cartel flotante `ios-install-banner` indicando pulsar *"Añadir a la pantalla de inicio"* para habilitar segundo plano y notificaciones push.

7. **Motor de Veteranía y Pisado Real (V12.5)**:
    - **Progresión de Potencia (Gamificación)**: Inicio en 1W (0.02 pwr) -> Máximo 50W (1.0 pwr). Progresión: +0.00000833 pwr por cada 5 segundos de TX (~50h de voz activa). Cada +1 Watt ganado genera notificación y sonido de logro (Dopamina UX).
    - **Efecto Pisado (Dynamic Distortion)**: Diferencia > 0.05 activa el `WaveShaper` de distorsión armónica. La voz débil debe sonar "crushed" (rota) y de fondo. Diferencia > 0.25 activa aplastamiento total.

8. **Gestión de Memoria Replay (One-Shot V4.2)**:
    - **Vaciado Inmediato**: Al pulsar Replay, la memoria se borra tras pasar a la playlist. No se permite repetir el mismo audio.
    - **Ducking Táctico**: Durante la reproducción de Replay, la radio en vivo y el ruido bajan al **2%** de forma automática.

9. **Identidad Unificada por Hardware y Redirecciones**:
    - **Hardware Unificado (Un móvil = Un usuario)**: La consulta `registered_devices/$deviceID` en Firebase AUTO-RESTAURA el indicativo si el dispositivo ya fue registrado, IMPIDIENDO crear múltiples usuarios desde el mismo teléfono.
    - **Pestaña Única**: Detección de pestaña duplicada con bloqueo total de interfaz para evitar ecos.

10. **Sistema de Portadora Fija, Potencia de Agente/Bot y Pisotones CB 27MHz**:
    - **Portadora Fija en RX**: En recepción (RX), los LEDs del S-Meter deben permanecer **TOTALMENTE FIJOS Y SÓLIDOS** marcando la potencia RF de señal.
    - **Potencia Máxima de Agente / Bot / Estación Base (50W)**: Toda respuesta automática o transmisión de Bot / Estación de Control (`BASE ...` o `CONTROL ...`) se emite automáticamente con **50 Watts (cbSignalFactor = 1.0f)**.

11. **Prohibición de Modificación del Flujo de Entrada, Canales, Micrófono y Emisión/Recepción (BLINDAJE ABSOLUTO V18.0)**:
    - **Leyes de Flujo Intocable**: El sistema completo de entrada de usuarios (`connectRadio`, `checkHardwareRestore`), cambio de canales/ciudades (`onCityChange`, `onChannelChange`), captura/permisos de micrófono (`ensureMicAccess`, `requestMicPermission`), y flujo de emisión (TX / PTT) y recepción (RX / PeerJS / Audio Sinks / S-Meter) queda **BLINDADO Y SELLADO DE FORMA ABSOLUTA**.
    - **Permiso Explícito Obligatorio**: Queda **ESTRICTAMENTE PROHIBIDO** para cualquier IA, agente o desarrollador alterar, modificar, refactorizar o reestructurar cualquiera de estos flujos por cuenta propia o por error. ÚNICAMENTE se permite modificar estas secciones tras solicitar y recibir el **permiso explícito y por escrito del usuario**.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. NO apliques parches que eliminen los nodos inaudibles o el bridge silencioso, ya que son los pilares que mantienen la voz viva en móviles modernos.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y PROTEGIDO BAJO LICENCIA DE PERMISO EXPLÍCITO (V18.0 ELITE SUPREME).**
