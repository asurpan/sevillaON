# 📻 ON AIR SPAIN: La Emisora de Radio Social Pura (V11.0)

**ON AIR SPAIN** es la evolución digital de la mítica Radio CB (Banda Ciudadana). Una aplicación diseñada para la comunicación de voz directa, sin distracciones, centrada en la pureza de la señal y la calidad técnica.

![Logo](logo.png)

## 🎯 El Propósito: Emisora Ciudadana Nacional
Hemos eliminado todo lo irrelevante para centrar la experiencia en lo que importa: **la voz y la señal**. Esta es una herramienta para radioaficionados y personas que buscan una comunicación auténtica, con el timbre y comportamiento de una emisora real.

## 🚀 Características Principales (Pure Radio v11.0)

### 🎙️ Motor de Audio Profesional y Realista
- **Push-To-Talk (PTT) Táctico:** Protocolo instantáneo de alta fidelidad con gesto táctil preciso.
- **VOX v10.0 (Manos Libres):** Motor independiente con cancelación de ruido adaptativa, pre-amplificación de sensor y captura de primera sílaba. Optimizado para **coches, motos y autobuses**.
- **QRM Vivo y Oscilante:** Ruido de banda analógico (1450Hz) con oscilación atmosférica real sincronizada con los LEDs.
- **Portadora Real (Carrier):** Los LEDs marcan la potencia del emisor de forma estática; la voz solo oscila en los segmentos rojos finales.
- **Efecto "Pisarse":** La estación con más potencia (Watts) domina la frecuencia, desplazando a las señales más débiles con ruido de fondo realista.

### 🛡️ Privacidad Garantizada
- **Sintonización por IP:** Ubicamos tu ciudad automáticamente. No pedimos ni rastreamos tu GPS exacto.
- **Sin Registros:** Tu identidad es tu indicativo. Privacidad total sin correos ni teléfonos.
- **Audio Efímero:** Las comunicaciones fluyen punto a punto y desaparecen al instante.

### 🏙️ Dial Nacional y Balanceo Automático
- **Canales CB 1-40:** Cobertura nacional organizada por ciudades.
- **Evitación de Saturación (-X):** El sistema crea sub-salas automáticamente (ej. `SEVILLA-2`) cuando hay más de 8 usuarios para proteger la claridad del audio y la batería del móvil.
- **Notificaciones de Amigos:** Sistema de aviso inteligente cuando tus contactos habituales entran en frecuencia.

## 🛠️ Arquitectura Blindada (Protected Core)
- **ManosLibres.kt:** Núcleo independiente para la captura y limpieza DSP de voz.
- **RadioAudioManager.kt:** Gestión de ruteo, QRM y prioridades de potencia.
- **Heartbeat & Zombie Cleaning:** Limpieza automática de sesiones inactivas cada 5 segundos.

## 📱 Optimización Android
- **Modo Background:** Diseñada para funcionar en segundo plano.
- **Batería:** Se recomienda desactivar las restricciones de energía en Android para un manos libres ininterrumpido en ruta.

---
&copy; 2026 **ON AIR SPAIN** - Ingeniería de Radio Profesional.
