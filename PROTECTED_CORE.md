# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V15.0 ELITE SUPREME)

Este documento establece las LEYES SUPREMAS de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado tras una auditoría profunda de WebRTC y AudioContext. Cualquier cambio no autorizado romperá la estabilidad de la voz, los LEDs o la jerarquía de potencia.

## 🚫 PROHIBICIÓN TOTAL (HARD-LOCK)
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Blindaje de Voz y LEDs (WebRTC Sink)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` con `muted=true` y `playsinline`. 
    - **Decoding Bridge**: Se debe conectar cada stream remoto a un nodo `dummy` de volumen **0.01** (Aumentado para Chrome Mobile) directo a la salida física. Esto engaña al navegador para que mantenga la decodificación activa y los LEDs brillen.
    - **ICE/TURN 4G Reforzado**: El ruteo WebRTC debe incluir servidores TURN-TCP para saltar CG-NAT de operadoras móviles.
    - **Hardware Keep-Alive (Motor 5Hz)**: Mantener el oscilador **infrasónico de 5Hz** inaudible en la salida física para evitar el modo suspensión del chip de audio. Debe silenciarse automáticamente cuando se detecta voz real entrante.
    - **Tono de Modo Oreja (Piripi)**: Al activar el Modo Oreja se ejecuta obligatoriamente la señal audible "incoming" (piripi) y la vibración háptica de 35ms.
    - **Permiso de Micrófono Android 11+ (MIUI/OneUI)**: Invocación obligatoria mediante `@JavascriptInterface fun requestMicPermission()` en `runOnUiThread`. Evita la cancelación silenciosa de permisos de WebView en Xiaomi y Samsung.

2. **Motor de Veteranía y Pisado Real (V12.5)**:
    - **Progresión de Potencia (Gamificación)**: Inicio en 1W (0.02 pwr) -> Máximo 50W (1.0 pwr). Progresión: +0.00000833 pwr por cada 5 segundos de TX (~50h de voz activa). Cada +1 Watt ganado genera notificación y sonido de logro (Dopamina UX).
    - **Efecto Pisado (Dynamic Distortion)**: Diferencia > 0.05 activa el `WaveShaper` de distorsión armónica. La voz débil debe sonar "crushed" (rota) y de fondo. Diferencia > 0.25 activa aplastamiento total.

3. **Gestión de Memoria Replay (One-Shot V4.2)**:
    - **Vaciado Inmediato**: Al pulsar Replay, la memoria se borra tras pasar a la playlist. No se permite repetir el mismo audio.
    - **Ducking Táctico**: Durante la reproducción de Replay, la radio en vivo y el ruido bajan al **2%** de forma automática.

4. **Lógica de Red y Conectividad**:
    - **Llamada Inteligente**: Solo el dispositivo con la ID lexicográficamente menor inicia la llamada (`sessionID < k`). Esto evita el "Glare" (choque de llamadas).
    - **Identidad Fija**: La `sessionID` debe ser `Nick + DeviceID` para garantizar estabilidad en PeerJS y evitar duplicados.

5. **Diseño Visual Nexus (UI FREEZE)**:
    - **Contenedor de Estado**: Altura fija de **32dp** (prohibido saltos de línea).
    - **LEDs S-Meter**: 20 diodos con efecto resplandor (Glow) en verde esmeralda táctico.
    - **RX Tag**: Color esmeralda (`0xFF10B981`) con fundido lateral y resplandor.

6. **Estrategia SEO y Seguridad**:
    - **SEO Marca**: Mantener términos *DSELECTRONICA, reparar emisora Sevilla, ligar radio, conocer gente*.
    - **Derecho de Admisión**: Mantener el diálogo de borrado de datos con lenguaje disuasorio y advertencia de **BAN-HARDWARE**.

7. **Identidad Unificada por Hardware y Redirecciones**:
    - **Hardware Unificado (Un móvil = Un usuario)**: La consulta `registered_devices/$deviceID` en Firebase AUTO-RESTAURA el indicativo si el dispositivo ya fue registrado, IMPIDIENDO crear múltiples usuarios desde el mismo teléfono. La función `connectRadio` y `checkHardwareRestore` fuerzan el nick registrado automáticamente.
    - **Pestaña Única**: Detección de pestaña duplicada con bloqueo total de interfaz para evitar ecos.
    - **Redirección Android Web -> App Nativa**: Navegadores en Android NUNCA ejecutan la radio Web (`webApp.js` no se carga). Deben abrir la App Nativa o la Play Store mediante Intent URL, mientras que iOS/Safari y PC Desktop ejecutan la versión Web.

8. **Sistema de Portadora Fija, Potencia de Agente/Bot y Pisotones CB 27MHz (REGLA DE ORO)**:
    - **Portadora Fija en RX**: En recepción (RX), los LEDs del S-Meter deben permanecer **TOTALMENTE FIJOS Y SÓLIDOS** marcando la potencia RF de señal. Está prohibido introducir oscilaciones, parpadeos o modulaciones falsas a la barra de LEDs en RX.
    - **Potencia Máxima de Agente / Bot / Estación Base (50W)**: Toda respuesta automática o transmisión de Bot / Estación de Control (`BASE ...` o `CONTROL ...`) se emite automáticamente con **potencia a tope (50 Watts / cbSignalFactor = 1.0f)**. Enciende los **20 LEDs fijos** (S9+30dB en verde/amarillo/rojo) y marca 50W.
    - **Sistema de Pisotones (Override)**: Las estaciones con mayor millaje/vatios se imponen fijando un nivel de LEDs más alto, imitando el comportamiento exacto de una radio CB de 27 MHz.

9. **Blindaje Audio WebRTC Android 11+ y Motor TTS Nativo (REGLA CRÍTICA)**:
    - **Desbloqueo de Audio Remoto (Reanudación Forzosa)**: Para evitar que el sistema operativo en Android 11+ bloquee el flujo de voz entrante mientras la señal se ve cogida, es OBLIGATORIO mantener la reanudación forzosa del volumen del nodo `remoteAudio` (`remoteAudio.volume = 0.2` seguido de `0.01` dummy) en el evento `call.on('stream')`.
    - **Invocación TTS Nativo en Android**: Las locuciones del Bot/Estación Base en Android se deben dirigir OBLIGATORIAMENTE a través del puente nativo `window.AndroidApp.speak(text, rate, pitch)` enlazado con `TextToSpeech` de Android, asegurando la respuesta auditiva y el evento visual `BASE LOCAL`.

10. **Blindaje Anti-`undefined` y Roger Beep de Agente (V16.5 HARD-LOCK SUPREME)**:
    - **Conexión Síncrona Inmediata**: `connectRadio(nick)` DEBE asignar `window.app.sessionID` de forma síncrona en la primera línea de ejecución antes de cualquier promesa asíncrona de Firebase. Queda TERMINANTEMENTE PROHIBIDO dejar `window.app.sessionID` en `undefined` o nulo durante el arranque.
    - **Prohibición Total de Escritura en `users/undefined`**: Todo intento de escritura en Firebase RTDB (`RadioPersistence.saveState`, `RadioAudioManager.broadcastPTT`, `onCityChange`, `onChannelChange`, etc.) DEBE verificar explícitamente que `sessionID` sea no nulo, no vacío y no sea `"undefined"`.
    - **Filtro Remoto Anti-`undefined`**: El listener `onUsersUpdate` DEBE descartar cualquier clave que sea o contenga `"undefined"` para evitar modulaciones o portadoras fantasma en la red.
    - **Roger Beep de Agente / Bot**: La transmisión del Agente / Bot (TTS) DEBE emitir OBLIGATORIAMENTE el sonido Roger Beep al finalizar su alocución, tanto en la app nativa Android (`onDone` en `UtteranceProgressListener` + `ToneGenerator.TONE_PROP_BEEP`) como en la versión Web (`u.onend` + `playUiSound('roger')`).

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. NO apliques parches que eliminen los nodos inaudibles o el bridge silencioso, ya que son los pilares que mantienen la voz viva en móviles modernos.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y OPTIMIZADO (V16.5 ELITE SUPREME).**
