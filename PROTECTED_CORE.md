# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V12.2 ELITE SEO & ONE-SHOT REPLAY)

Este documento establece las leyes fundamentales de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado para una experiencia de radio profesional, un posicionamiento orgánico y una gestión de memoria táctica.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Motor de Veteranía y Potencia (V12.1)**:
    - **Progresión de Aire**: El usuario gana +0.002 de potencia por cada 5 segundos de transmisión efectiva.
    - **Efecto Pisado Real (Dynamic Distortion)**: Si un usuario potente pisa a uno débil, la voz del débil sufre distorsión armónica no lineal y reducción de volumen. Prohibido eliminar el `WaveShaper` de distorsión.

2. **Blindaje de Audio y Grabación (Feedback Fix)**:
    - **Bus de Grabación Maestro**: El Replay debe usar el `masterRecordBus` (SILENCIOSO). Prohibido conectar el micro local directamente al `rxReplayBus` audible para evitar bucles de eco.
    - **DOM Sink Fix**: Es OBLIGATORIO mantener elementos `<audio>` invisibles con `.play()` para asegurar la decodificación WebRTC y los LEDs.

3. **Motor de Replay Inteligente (V4.2 - One-Shot Edition)**:
    - **One-Shot Playback**: Al pulsar Replay, la memoria se vacía inmediatamente tras mover el contenido a la lista de reproducción. El icono se apaga al terminar la reproducción y no se puede repetir el mismo audio.
    - **Grabación Total**: Graba voz local y remota sin causar eco.
    - **Codec Universal**: Detección dinámica de formato para compatibilidad total en Android/iOS.
    - **Tactical Ducking**: Radio en vivo al 2% durante la reproducción.

4. **Viralidad Táctica (Sharing V12.2)**:
    - **Mensaje de Invitación**: Formato reducido e impactante con negritas de WhatsApp: 📻 *ON AIR SPAIN* | 📍 *CIUDAD*.

5. **Estrategia SEO y Marketing**:
    - **Indexación**: Prohibido eliminar o alterar `robots.txt` y `sitemap.xml`.
    - **Metadatos**: Título y descripción optimizados para Google e integrando DSELECTRONICA para soporte técnico.

6. **Diseño Visual Nexus**:
    - **ESTADO: CONGELADO TOTAL**. Bloque central 140dp, botones 54dp, contenedor estado 32dp.
    - **Bordes Élite**: Pantalla con redondeo de 22dp, borde en degradado vertical y resplandor suave (Soft Glow).
    - **RX Tag**: Verde Esmeralda (`0xFF10B981`) con fundido y glow.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. El núcleo está sellado para garantizar la jerarquía de potencia, la gestión de memoria y el éxito comercial del proyecto.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y OPTIMIZADO (V12.2).**
