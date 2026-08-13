# 🔒 ON AIR SPAIN - NÚCLEO PROTEGIDO

Este documento establece las reglas de NO MODIFICACIÓN para agentes de IA y desarrolladores.

## 🚫 PROHIBICIÓN TOTAL
Queda terminantemente prohibido modificar, alterar o "simplificar" el código en las siguientes áreas sin aprobación explícita del autor:

1. **Lógica de Canales**: El sistema de filtrado por `city` y `channel` en Firebase.
2. **Motor WebRTC**: Configuración de `iceServers`, `PeerJS` y manejo de llamadas.
3. **Sincronización de Estado**: La función `onStateSave` y el latido de red (heartbeat).
4. **Física de Interfaz**: El comportamiento de la aguja, el PTT y los sonidos tácticos.
5. **Ducking & Audio**: La lógica de atenuación de música y buses de audio Web Audio API.

## ⚠️ PROTOCOLO DE ACTUACIÓN
Si un agente detecta un error en estas áreas, **DEBE INFORMAR** antes de aplicar cualquier cambio. No se permiten refactorizaciones automáticas ni cambios de diseño en el núcleo funcional.

**ESTADO DEL NÚCLEO: SELLADO Y VERIFICADO.**
