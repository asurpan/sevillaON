package com.sagon.on

import kotlinx.browser.window
import kotlinx.browser.localStorage

/**
 * 🗺️ RADIO MAPS MANAGER: MOTOR DE MAPAS Y RUTAS (LEAFLET BRIDGE)
 */
object RadioMapsManager {
    fun install() {
        js("""
            console.log("🗺️ Instalando RadioMapsManager...");
            window.initRealMap = function(containerId, lat, lon) {
                try {
                    if (typeof L === 'undefined') { console.warn("Leaflet no cargado aún"); return; }
                    if (window.app && window.app.map) { try { window.app.map.remove(); } catch(e) {} }
                    var map = L.map(containerId, { zoomControl: false, attributionControl: false }).setView([lat || 37.3891, lon || -5.9845], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                    if (window.app) {
                        window.app.map = map;
                        window.app.mapMarkers = {};
                    }
                } catch(e) { console.error("Error initRealMap:", e); }
            };

            window.setMapDestination = function(destLat, destLon, destName) {
                if (!window.app || !window.app.map) return;
                console.log("📍 Destino fijado en: " + destName);
            };

            window.updateMapMarkers = function(usersJson) {
                try {
                    if (typeof L === 'undefined' || !window.app || !window.app.map || !usersJson) return;
                    var users = JSON.parse(usersJson);
                    var markers = window.app.mapMarkers || {};
                    users.forEach(function(user) {
                        if (user.lat && user.lon) {
                            if (markers[user.nick]) {
                                markers[user.nick].setLatLng([user.lat, user.lon]);
                            } else {
                                markers[user.nick] = L.circleMarker([user.lat, user.lon], { radius: 8, color: user.isMe ? '#06B6D4' : '#22C55E' }).addTo(window.app.map);
                            }
                        }
                    });
                    window.app.mapMarkers = markers;
                } catch(e) {}
            };
        """)
    }
}
