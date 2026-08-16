# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V12.0 ELITE)

Este documento establece las leyes fundamentales de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado para una experiencia de radio profesional y cualquier cambio no autorizado romperá la estabilidad del audio, la sintonización o el motor de veteranía.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Motor de Veteranía y Potencia (V12.0)**:
    - **Progresión de Aire**: El usuario gana +0.002 de potencia por cada 5 segundos de transmisión efectiva.
    - **Límite Profesional**: La potencia (`vetPwr`) escala desde 0.7 (Novato) hasta 1.0 (Élite).
    - **Efecto Pisado (Power Override)**: Si dos usuarios hablan a la vez, el de mayor potencia domina. Si la diferencia es > 0.25, el más débil es **aplastado totalmente** (silencio). Si la diferencia es menor, se oye de fondo.
    - **Visualización S-Meter**: Los LEDs deben reflejar fielmente la portadora (`pwr`) del emisor. El usuario con más potencia llenará más segmentos (hasta 3/4 partes + modulación).

2. **Blindaje de Voz y Transmisión**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` invisibles vinculados a cada stream remoto para asegurar la decodificación y los LEDs.
    - **Voice Boost**: El micro local tiene un multiplicador de ganancia de **1.8x** antes del envío.

3. **Motor de Audio Profesional**: 
    - **Potencia Dominante**: El volumen maestro tiene un multiplicador **x3.0**.
    - **Monitor (MONI)**: Solo audible en TX (PTT pulsado). Ganancia fija **0.18x**.
    - **Roger Beep**: Blindado a **1955Hz**, **0.3s**.

4. **Motor de Replay Inteligente (V4.1)**:
    - **Grabación Total**: Graba voz local y remota.
    - **Tactical Ducking**: Radio en vivo al 2% durante la reproducción.
    - **Codec Universal**: Detección dinámica de formato.

5. **Diseño Visual Nexus (`Screens.kt`)**:
    - **ESTADO: CONGELADO TOTAL**. Bloque central 140dp, botones 54dp.
    - **RX Tag**: Verde Esmeralda (`0xFF10B981`) con fundido y glow.
    - **S-Meter**: 20 LEDs con efecto resplandor.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. No apliques "mejoras" estéticas que comprometan el motor de veteranía o los blindajes técnicos. El núcleo está sellado para garantizar la jerarquía de potencia en el canal.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y VERIFICADO (V12.0).**
