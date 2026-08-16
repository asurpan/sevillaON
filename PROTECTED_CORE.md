# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO (V11.0 PURE RADIO)

Este documento establece las leyes fundamentales de NO MODIFICACIÓN para agentes de IA y desarrolladores. El sistema ha sido optimizado para una experiencia de radio profesional y cualquier cambio no autorizado romperá la estabilidad del audio o la sintonización.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido alterar, "simplificar" o re-estructurar el código en las siguientes áreas:

1. **Blindaje de Voz y Transmisión (`RadioAudioManager.kt` & `ManosLibres.kt`)**:
    - **DOM Sink Fix**: Es OBLIGATORIO crear elementos `<audio>` invisibles vinculados a cada stream remoto. Sin esto, los navegadores móviles NO decodifican la voz.
    - **Audio Keep-Alive**: Mantener el oscilador ultrasónico de **20kHz** en el bus de salida para evitar que el canal de datos entre en suspensión.
    - **Dither Anti-Gate**: Inyección de ruido rosa infinitesimal (0.001) para que el navegador no anule la voz pensando que es silencio.
    - **Voice Boost**: El micro local debe tener un multiplicador de ganancia de **1.8x** antes de ser enviado a la red.

2. **Motor de Audio Profesional**: 
    - **Cadena de Audio**: Prohibido alterar el orden: Señal -> RxGain -> MasterOut (último nodo).
    - **Potencia Dominante**: El volumen maestro tiene un multiplicador **x3.0** para sonar por encima del volumen del sistema.
    - **Roger Beep**: Blindado a **1955Hz**, **0.3s** y ataque plano. Conectado tanto a salida local como al bus de transmisión (`txBus`).
    - **PTT Tail**: Retardo de **400ms** al soltar el PTT para asegurar que el Roger Beep se transmita completo.
    - **Monitor (MONI)**: Solo debe funcionar durante la transmisión (**PTT presionado**). Ganancia fijada en **0.18x** para un retorno potente.

3. **Motor de Replay Inteligente**:
    - **Aislamiento RX**: Solo graba el `rxReplayBus` (voz de otros). Prohibido grabar QRM local o pitidos propios.
    - **Capacidad Táctica**: Buffer de **30 segundos** exactos (6 bloques de 5s).
    - **Detección de Silencio**: Uso de **Memoria por Segmento**. Los bloques sin voz (umbral < 2) deben ser descartados automáticamente.
    - **Velocidad Táctica**: Reproducción fija a **1.15x** para revisión rápida.

4. **Diseño Visual Nexus (`Screens.kt`)**:
    - **ESTADO: CONGELADO TOTAL**. Prohibido mover botones o cambiar tamaños.
    - **Blindaje Geométrico**: Bloque central fijo a **140dp**, botones laterales a **54dp**. No debe haber saltos horizontales al cambiar de canal.
    - **RX Tag (Nombre)**: La etiqueta del operador que habla debe usar el color **Verde Esmeralda (`0xFF10B981`)** con efecto fundido y resplandor suave.
    - **Contenedor de Estado**: La línea de estado (donde aparece SQUELCH/AIRE/NOMBRE) debe tener una altura fija de **32dp** para evitar saltos de línea.
    - **S-Meter**: 20 LEDs con efecto resplandor (Cyan -> Amarillo -> Rojo). Prohibido bajar la resolución del medidor.

5. **Lógica de Sintonización y Escaneo**:
    - **Orden Numérico**: Los saltos deben ser estrictamente **1 al 40** y circulares.
    - **Squelch-Lock**: El escaneo TIENE PROHIBIDO iniciarse si el Squelch está abierto (notificación en pantalla).
    - **Control Total**: Un clic simple en las flechas durante un escaneo debe detenerlo instantáneamente. El PTT también detiene el escaneo.

6. **Identidad y Persistencia**:
    - **Hardware ID**: Uso de **Android ID** en nativo y **Web Device ID** persistente en navegador para reconocimiento único de equipos.
    - **Inicio Seguro**: Volumen inicial al **70%** y Squelch cerrado (**SQL 60% / RFG 40%**) para evitar estática al primer inicio.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si detectas un fallo, informa al autor. No apliques "mejoras" estéticas que comprometan estos blindajes técnicos. El núcleo está sellado para garantizar que la voz fluya siempre.

**ESTADO DEL NÚCLEO: SELLADO, BLINDADO Y VERIFICADO (V11.0).**
