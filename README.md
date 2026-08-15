# 📻 ON AIR SPAIN: La Emisora de Radio Social Pura (V5.0)

**ON AIR SPAIN** es la evolución digital de la mítica Radio CB (Banda Ciudadana). Una aplicación diseñada para la comunicación de voz directa, sin distracciones, centrada en la privacidad y la calidad técnica.

![Logo](logo.png)

## 🎯 El Propósito: Esencia Pura
Hemos eliminado lo irrelevante (chats, fotos, mapas complejos y GPS intrusivo) para centrarnos en lo que importa: **la voz**. Esta es una herramienta para radioaficionados y personas que buscan una comunicación real y humana.

## 🚀 Características Principales (Protegidas)

### 🎙️ Motor de Audio Profesional
- **Push-To-Talk (PTT):** Protocolo instantáneo de alta fidelidad.
- **VOX Inteligente:** Transmisión activada por voz con umbral de sensibilidad ajustable.
- **QRM Atmosférico:** Inyección de **Brown Noise** filtrado para una textura de radio analógica auténtica.
- **Roger Beep:** Tono de finalización blindado a **1955Hz / 0.3s**.
- **Procesado DSP:** Filtro Bandpass (1500Hz) y ecualización de voz para máxima claridad.

### 🛡️ Privacidad Total (Sin GPS)
- **Sintonización por IP:** Ubicamos tu ciudad automáticamente mediante tu dirección IP. No pedimos ni rastreamos tu GPS exacto.
- **Sin Registros:** Tu identidad es tu indicativo. No requerimos correos, teléfonos ni datos personales.
- **Audio Efímero:** Las conversaciones fluyen punto a punto (Full Mesh) y nunca se graban ni se almacenan en servidores.

### 🏙️ Dial Nacional Completo (1-40)
- **40 Canales CB:** Cobertura total de todas las provincias de España.
- **Balanceo de Carga:** Si un canal se satura (más de 8 usuarios), el sistema crea sub-canales automáticamente (Sevilla-2, Sevilla-3...) para proteger el rendimiento y la batería de los terminales.
- **Rango de Veterano:** Solo los usuarios con potencia acumulada (>= 0.85W) pueden usar códigos de privacidad (Subtonos).

## 🛠️ Arquitectura Técnica (Protected Core)
El proyecto utiliza una arquitectura **KMP (Kotlin Multiplatform)** con un motor web altamente optimizado:
- **RadioAudioManager.kt:** Gestión de AudioContext y Web Audio API.
- **RadioPersistence.kt:** Persistencia blindada de vatios y configuraciones.
- **RadioFmEngine.kt:** Sintonizador de música de fondo HQ.
- **Screens.kt:** Interfaz NEXUS optimizada (0 recálculo de señal en UI).

## 📥 Instalación y Despliegue
La aplicación es una PWA (Progressive Web App) de alto rendimiento, optimizada para Android mediante Chrome WebView.
- El despliegue es automático vía **GitHub Actions**.
- Compatible con modo manos libres y auriculares.

---
&copy; 2026 **ON AIR SPAIN** - Ingeniería de Radio Profesional.
