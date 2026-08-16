# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO

Este documento establece las reglas de NO MODIFICACIÓN para agentes de IA y desarrolladores.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido modificar, alterar o "simplificar" el código en las siguientes áreas sin aprobación explícita del autor:

1. **Motor de Audio Profesional (`RadioAudioManager.kt`)**: 
    - **Cadena de Audio**: Prohibido alterar el orden de conexión (Señal -> RxGain -> MasterOut). El MasterOut (Volumen) debe ser siempre el último nodo.
    - **Roger Beep**: Blindado a **1955Hz**, **0.3s** y envolvente de ataque plano (usado en `ptt_off` y `rx_off`).
    - **UI Sounds**: Tonos de interacción (clicks, switches) configurados a **1800Hz** y **0.08s** para diferenciarlos de la radio.
    - **QRM Atmosférico**: Generador de **Brown Noise** filtrado a 1500Hz/Q1.2.
    - **Independencia de Señal**: El volumen maestro **NUNCA** debe afectar al nivel medido por los analizadores de los LEDs.
2. **Motor de LEDs (`VOXEngine`)**:
    - **Escala de Standby**: Limitado a **0.05-0.12** (1-2 segmentos) con Squelch abierto.
    - **Cero Estricto**: Con Squelch cerrado, el nivel reportado debe ser **0.0 absoluto** (ahorro de batería).
    - **S-Meter RX**: El nivel de recepción de voz debe saltar a **0.65+ (S9)**.
3. **Lógica de Interfaz (`Screens.kt`)**:
    - **Cálculo de LEDs**: La UI tiene **prohibido** calcular intensidades de señal. Debe usar exclusivamente el valor `mic` enviado por el motor de audio.
4. **Lógica de Canales**: El sistema de filtrado por `city` y `channel` en Firebase.
5. **Motor WebRTC**: Configuración de `PeerJS` y manejo de llamadas (Full Mesh 10 usuarios).
6. **Ducking & Silencio**: Silencio absoluto en recepción durante la transmisión (TX).
7. **VOX (Transmisión por Voz)**: 
    - El disparador automático debe basarse en la modulación del micro local.
    - Sensibilidad inversa: A mayor valor de `voxSens`, menor umbral de disparo.
    - Mantener portadora activa (hang time) para evitar cortes bruscos.
8. **Rangos de Usuario (Veteranía)**:
    - Solo usuarios con potencia acumulada **>= 0.85W** pueden acceder a canales privados (Subtonos).
    - Esto protege la red de la saturación por usuarios temporales y fomenta la participación pública.
9. **Diseño Visual Nexus (`Screens.kt`)**:
    - **ESTADO: CONGELADO**. Queda prohibido modificar el diseño visual de la pantalla Nexus, la disposición de los botones, tamaños o colores.
    - **Bloque Central**: Debe mantener su anchura fija de 140dp para evitar saltos horizontales.
    - **Botones de Canal**: Deben flanquear el bloque central con un tamaño de 54dp y forma circular.
    - **Dúo de Control**: Solo deben aparecer VOL y SQL en la columna técnica principal.
    - **Efectos Reclamo**: El icono de compartir debe mantener su parpadeo sutil aleatorio.
10. **Lógica de Sintonización**:
    - **Sintonía**: Los saltos deben ser siempre en orden numérico (1-40) y circulares.
    - **Escaneo**: Debe detenerse automáticamente al detectar voz (3s) o al pulsar el PTT.
    - **Parada Manual**: Un clic simple en las flechas de canal debe detener el escaneo si este se encuentra activo.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si un agente detecta un error en estas áreas, **DEBE INFORMAR** antes de aplicar cualquier cambio. No se permiten refactorizaciones automáticas ni cambios de diseño en el núcleo funcional.

**ESTADO DEL NÚCLEO: SELLADO, OPTIMIZADO Y VERIFICADO.**
