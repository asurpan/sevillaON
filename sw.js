/**
 * 🔒 HARD-LOCK: SERVICE WORKER - MOTOR DE SUPER-CACHÉ OFFLINE
 * ESTADO: SECHEDULER TÁCTICO V1.0
 *
 * Gestiona la persistencia de mapas real y la resiliencia de la radio
 * en zonas de baja cobertura (Rutas de Montaña / Carreteras).
 */

const CACHE_NAME = 'on-air-spain-v1';
const MAP_CACHE = 'on-air-maps-v1';

/* Recursos base para que la radio arranque sin internet */
const ASSETS_TO_CACHE = [
    './',
    './index.html',
    './manifest.json',
    './logo.png',
    './webApp.js',
    './version.json'
];

self.addEventListener('install', (event) => {
    self.skipWaiting();
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(ASSETS_TO_CACHE);
        })
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(clients.claim());
});

self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url);

    /* --- 🗺️ ESTRATEGIA PARA MAPAS (TILES) --- */
    /* Capturamos peticiones a servidores de mapas (OSM, CartoDB, etc) */
    if (url.hostname.includes('tile.openstreetmap.org') ||
        url.hostname.includes('basemaps.cartocdn.com') ||
        url.hostname.includes('unpkg.com')) {

        event.respondWith(
            caches.open(MAP_CACHE).then((cache) => {
                return cache.match(event.request).then((response) => {
                    /* Si está en caché, lo servimos inmediatamente (Velocidad rayo en ruta) */
                    if (response) return response;

                    /* Si no está, lo descargamos y lo guardamos para el futuro */
                    return fetch(event.request).then((networkResponse) => {
                        cache.put(event.request, networkResponse.clone());
                        return networkResponse;
                    }).catch(() => {
                        /* Fallback silencioso si no hay red ni caché */
                        return null;
                    });
                });
            })
        );
        return;
    }

    /* --- 📻 ESTRATEGIA PARA LA APP (NETWORK-FIRST) --- */
    /* Aseguramos que la radio use siempre la última versión si hay red */
    event.respondWith(
        fetch(event.request).catch(() => {
            return caches.match(event.request);
        })
    );
});
