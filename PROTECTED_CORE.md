# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V11.0 PURE RADIO)

Este documento establece las leyes fundamentales de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado para una experiencia de radio profesional y cualquier cambio no autorizado romperá la estabilidad del audio o la sintonización.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Blindaje de Voz y Transmisión (`RadioAudioManager.kt` & `ManosLibres.kt`)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` invisibles vinculados a cada stream remoto y forzar su reproducción (`.play()`) para asegurar la decodificación WebRTC y el funcionamiento de los LEDs.
    - **Audio Keep-Alive**: Mantener el oscilador ultrasónico de **20kHz** en el bus de salida para evitar que el canal de datos entre en suspensión.
    - **Voice Boost**: El micro local debe tener un multiplicador de ganancia de **1.8x** antes de ser enviado a la red.

2. **Motor de Audio Profesional**: 
    - **Cadena de Audio**: Prohibido alterar el orden: Señal -> RxGain -> MasterOut (último nodo).
    - **Potencia Dominante**: El volumen maestro tiene un multiplicador **x3.0** para sonar por encima del volumen del sistema.
    - **Monitor (MONI)**: Solo debe funcionar durante la transmisión (**PTT presionado**). Ganancia fijada en **0.18x** para un retorno potente.
    - **Roger Beep**: Blindado a **1955Hz**, **0.3s** y ataque plano.

3. **Motor de Replay Inteligente (V4.1)**:
    - **Grabación Total**: Graba tanto la voz local como la remota (vía `rxReplayBus`). Prohibido aislar al usuario de su propia grabación.
    - **Tactical Ducking**: Al reproducir Replay, la radio en vivo debe bajar al **2%** de volumen de forma automática.
    - **Dedicación de Compresión**: Uso obligado del `replayCompressor` para que las grabaciones tengan volumen constante.
    - **Codec Universal**: Detección dinámica de formato (WebM/Opus/Nativo) para garantizar compatibilidad en todos los navegadores móviles.
    - **Capacidad Táctica**: Buffer de **30 segundos** exactos (6 bloques de 5s) con memoria por segmento.
    - **Velocidad Táctica**: Reproducción fija a **1.15x**.

4. **Diseño Visual Nexus (`Screens.kt`)**:
    - **ESTADO: CONGELADO TOTAL**. Prohibido mover botones o cambiar tamaños.
    - **RX Tag (Nombre)**: La etiqueta del operador que habla debe usar el color **Verde Esmeralda (`0xFF10B981`)** con efecto fundido y resplandor suave.
    - **Contenedor de Estado**: La línea de estado (donde aparece SQUELCH/AIRE/NOMBRE) debe tener una altura fija de **32dp** para evitar saltos de línea.
    - **S-Meter**: 20 LEDs con efecto resplandor (Glow).
    - **Replay Icon**: Debe tener fundido progresivo de brillo y glow durante la reproducción. Solo brilla si hay grabaciones con voz real.

5. **Identidad y Persistencia**:
    - **Hardware ID (Hard-Lock)**: La `sessionID` en Firebase está vinculada estrictamente al ID del dispositivo. Prohibido crear sesiones basadas solo en el Nick para evitar duplicados.
    - **Reglas del Nick**: Mínimo **2 letras**, máximo **32 letras**. Sanitización obligatoria para Firebase.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. No apliques "mejoras" estéticas que comprometan estos blindajes técnicos. El núcleo está sellado para garantizar la máxima potencia y claridad de la voz.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y VERIFICADO (V11.0).**
