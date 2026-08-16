# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V12.1 ÉLITE SEO)

Este documento establece las leyes fundamentales de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado para una experiencia de radio profesional y un posicionamiento orgánico en buscadores. Cualquier cambio no autorizado romperá la estabilidad del audio, la jerarquía de potencia o la indexación en Google.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Motor de Veteranía y Potencia (V12.1)**:
    - **Progresión de Aire**: El usuario gana +0.002 de potencia por cada 5 segundos de transmisión efectiva.
    - **Efecto Pisado Real (Dynamic Distortion)**: Si un usuario potente pisa a uno débil, la voz del débil sufre distorsión armónica no lineal y reducción de volumen. Prohibido eliminar el `WaveShaper` de distorsión.

2. **Blindaje de Audio y Grabación (Feedback Fix)**:
    - **Bus de Grabación Maestro**: El Replay debe usar el `masterRecordBus` (SILENCIOSO). Prohibido conectar el micro local directamente al `rxReplayBus` audible para evitar bucles de eco.
    - **DOM Sink Fix**: Es OBLIGATORIO mantener elementos `<audio>` invisibles con `.play()` para asegurar la decodificación WebRTC y los LEDs.
    - **Voice Boost**: Micro local con multiplicador **1.8x**.

3. **Motor de Replay Inteligente (V4.2)**:
    - **Grabación Total**: Graba voz local y remota sin causar eco.
    - **Codec Universal**: Detección dinámica de formato para compatibilidad total en Android/iOS.
    - **Tactical Ducking**: Radio en vivo al 2% durante la reproducción.

4. **Estrategia SEO y Marketing**:
    - **Indexación**: Prohibido eliminar o alterar `robots.txt` y `sitemap.xml`.
    - **Meta-Tags**: Mantener los títulos y descripciones optimizados para Google ("Radio CB Online", "Walkie Talkie Digital").
    - **Landing Page**: La sección "¿Cómo funciona?" y las tarjetas de funciones Élite son inamovibles.

5. **Diseño Visual Nexus**:
    - **ESTADO: CONGELADO TOTAL**. Bloque central 140dp, botones 54dp, contenedor estado 32dp.
    - **RX Tag**: Verde Esmeralda (`0xFF10B981`) con fundido y glow.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. El núcleo está sellado para garantizar la jerarquía de potencia y el éxito comercial en la web.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y OPTIMIZADO (V12.1).**
