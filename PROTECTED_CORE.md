# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V12.5 ELITE SUPREME)

Este documento establece las LEYES SUPREMAS de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado tras una auditoría profunda de WebRTC y AudioContext. Cualquier cambio no autorizado romperá la estabilidad de la voz, los LEDs o la jerarquía de potencia.

## 🚫 PROHIBICIÓN TOTAL (HARD-LOCK)
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Blindaje de Voz y LEDs (WebRTC Sink)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` con `muted=true` y `playsinline`. 
    - **Decoding Bridge**: Se debe conectar cada stream remoto a un nodo `dummy` de volumen **0.005** directo a la salida física. Esto engaña al navegador para que mantenga la decodificación activa y los LEDs brillen.
    - **Hardware Keep-Alive (Motor 5Hz)**: Mantener el oscilador **infrasónico de 5Hz** inaudible en la salida física para evitar el modo suspensión del chip de audio. Debe silenciarse automáticamente cuando se detecta voz real entrante.

2. **Motor de Veteranía y Pisado Real (V12.5)**:
    - **Progresión de Potencia**: +0.002 pwr por cada 5 segundos de TX. Rango: 0.7 (Novato) a 1.0 (Élite).
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

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. NO apliques parches que eliminen los nodos inaudibles o el bridge silencioso, ya que son los pilares que mantienen la voz viva en móviles modernos.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y OPTIMIZADO (V14.0).**
