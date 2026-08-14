package com.sagon.on

import kotlinx.browser.localStorage
import kotlin.js.Date

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MOTOR DE RADIO FM Y LOCUTOR VIRTUAL
 * ESTADO: SELLADO TOTAL - CONFIGURACIÓN DE BITRATE, VOLUMEN Y RNE DEFINITIVA
 * 
 * Gestiona sintonización HQ, avisos DGT inmediatos y locución inteligente.
 * 
 * AJUSTES CRÍTICOS (PROHIBIDO MODIFICAR):
 * - Filtro de Bitrate: Obligatorio order=bitrate para audio HQ.
 * - Ciclo de Búsqueda: 1s de reintento para escaneo ultra-rápido.
 * - Refresco DGT: Forzado inmediato al entrar en modo ANUNCIOS.
 * - Volumen FM: Base 0.7 para potencia máxima sobre el QRM.
 * - Boletín RNE: Sincronización mediante anclaje de permisos al clic.
 */
object RadioFmEngine {

    fun install() {
        js("""
            window.fmEngine = {
                audio: null,
                announcementTimer: null,
                currentStation: null,
                currentCity: "SEVILLA",
                currentGenre: "MIX",
                isUnlocked: false,
                headlines: [],
                
                /* --- 🌍 DETECCIÓN DE UBICACIÓN --- */
                detectLocalCity: function() {
                    var current = localStorage.getItem("lastCity") || "SEVILLA";
                    if (current === "ESPAÑA (NACIONAL)") {
                        fetch('https://ipapi.co/json/').then(r => r.json()).then(data => {
                            if (data && data.city) {
                                var city = data.city.toUpperCase();
                                this.currentCity = city;
                                console.log("📍 Ciudad detectada por IP:", city);
                                if (window.dispatch_incoming_alert) {
                                    window.dispatch_incoming_alert("📍 UBICACIÓN", "Sintonizado en " + city, "success");
                                }
                                this.refreshCache();
                            }
                        }).catch(e => console.warn("GeoIP Error:", e));
                    } else {
                        this.currentCity = current;
                    }
                },

                /* --- 📦 BANCO DE DATOS --- */
                refreshCache: function(callback) {
                    console.log("📥 Refrescando boletines...");
                    var self = this;
                    var city = this.currentCity || "SEVILLA";
                    
                    var pending = 2; /* Esperamos incidencias y cámaras DGT */
                    var decrementPending = function() {
                        pending--;
                        if (pending === 0 && callback) callback();
                    };
                    
                    /* DGT Incidencias */
                    var dgtUrl = "https://services1.arcgis.com/nCKYv2vChZEOqt60/arcgis/rest/services/Incidencias_Trafico_DGT/FeatureServer/0/query?f=json&where=1%3D1&outFields=carretera,poblacion,causa,descripcion,tipo&returnGeometry=false";
                    fetch(dgtUrl).then(r => r.json()).then(data => {
                        if (data && data.features) {
                            var local = data.features.filter(f => {
                                var a = f.attributes;
                                return (a.poblacion && a.poblacion.toUpperCase().includes(city)) || 
                                       (a.descripcion && a.descripcion.toUpperCase().includes(city));
                            });
                            if (local.length > 0) {
                                var alert = local[0].attributes;
                                var text = "Aviso DGT: " + alert.causa + " en " + alert.carretera + ". " + alert.descripcion;
                                localStorage.setItem("cache_dgt", text);
                                if (window.dispatch_dgt_update) window.dispatch_dgt_update(text, null);
                            } else {
                                localStorage.removeItem("cache_dgt");
                            }
                        }
                        decrementPending();
                    }).catch(e => { console.warn("DGT Fetch Error"); decrementPending(); });

                    /* DGT Cámaras (Mejora: Buscar cámara local con búsqueda flexible) */
                    var camsUrl = "https://services1.arcgis.com/nCKYv2vChZEOqt60/arcgis/rest/services/Cámaras_Trafico_DGT/FeatureServer/0/query?f=json&where=1%3D1&outFields=nombre,carretera,url_imagen&returnGeometry=false&resultRecordCount=100";
                    fetch(camsUrl).then(r => r.json()).then(data => {
                        if (data && data.features) {
                            var cityBase = city.split(" / ")[0].split(" (")[0].trim().toUpperCase();
                            var cam = data.features.find(f => {
                                var n = (f.attributes.nombre || "").toUpperCase();
                                var c = (f.attributes.carretera || "").toUpperCase();
                                return n.includes(cityBase) || c.includes(cityBase);
                            });
                            
                            if (cam) {
                                var imgUrl = cam.attributes.url_imagen;
                                localStorage.setItem("cache_dgt_img", imgUrl);
                                var dgtText = localStorage.getItem("cache_dgt") || "Cámara de tráfico activa en " + cam.attributes.carretera;
                                if (window.dispatch_dgt_update) window.dispatch_dgt_update(dgtText, imgUrl);
                            }
                        }
                        decrementPending();
                    }).catch(e => { decrementPending(); });

                    /* NASA (Segundo plano, no bloquea callback) con Traducción Automática Multi-Motor */
                    fetch('https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY').then(r => r.json()).then(data => {
                        if (data && data.title) {
                            var imgUrl = data.hdurl || data.url;
                            var titleEn = data.title;
                            var descEn = data.explanation || "";
                            
                            /* Mostramos versión original inmediatamente por si la traducción tarda */
                            if (window.dispatch_nasa_image) window.dispatch_nasa_image(imgUrl, titleEn, descEn);

                            var translate = function(q, cb) {
                                /* Motor 1: Google Translate Gtx (Más fiable que MyMemory para bloques largos) */
                                var proxyUrl = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=" + encodeURIComponent(q);
                                fetch(proxyUrl)
                                    .then(r => r.json())
                                    .then(j => {
                                        var translated = j[0].map(item => item[0]).join("");
                                        cb(translated || q);
                                    })
                                    .catch(() => {
                                        /* Motor 2: MyMemory (Respaldo) */
                                        fetch('https://api.mymemory.translated.net/get?q=' + encodeURIComponent(q) + '&langpair=en|es')
                                            .then(res => res.json())
                                            .then(json => {
                                                var result = json.responseData.translatedText;
                                                cb(result || q);
                                            }).catch(() => cb(q));
                                    });
                            };

                            translate(titleEn, function(titleEs) {
                                /* Traducimos un bloque mayor de descripción para cubrir el APOD completo */
                                var descSnippet = descEn.substring(0, 1500); 
                                translate(descSnippet, function(descEs) {
                                    localStorage.setItem("cache_nasa", "Boletín espacial: " + titleEs);
                                    localStorage.setItem("cache_nasa_img", imgUrl);
                                    localStorage.setItem("cache_nasa_title", titleEs);
                                    localStorage.setItem("cache_nasa_desc", descEs + " [TRADUCCIÓN AUTOMÁTICA]");
                                    
                                    /* RE-DISPARO: Forzar a la UI a actualizar a español con los datos traducidos */
                                    if (window.dispatch_nasa_image) {
                                        window.dispatch_nasa_image(imgUrl, titleEs, descEs + " [TRADUCCIÓN AUTOMÁTICA]");
                                    }
                                });
                            });
                        }
                    }).catch(e => { console.warn("NASA Fetch Error"); });

                    /* El Tiempo */
                    if (this.currentCity) {
                        fetch('https://wttr.in/' + encodeURIComponent(this.currentCity) + '?format=3')
                            .then(r => r.text())
                            .then(t => localStorage.setItem("cache_weather", "El tiempo: " + t))
                            .catch(e => {});

                        /* --- 📰 TITULARES DE PRENSA (MODO ALEXA RESUMIDO) --- */
                        var searchCity = city.split(" / ")[0].split(" (")[0].trim();
                        var newsUrl = 'https://api.allorigins.win/get?url=' + encodeURIComponent('https://news.google.com/rss/search?q=noticias+' + searchCity + '+spain&hl=es-ES&gl=ES&ceid=ES:es');
                        fetch(newsUrl).then(r => r.json()).then(data => {
                            var parser = new DOMParser();
                            var xml = parser.parseFromString(data.contents, "text/xml");
                            var items = xml.querySelectorAll("item");
                            if (items.length > 0) {
                                /* Antonio elige la noticia más potente y lee su resumen */
                                var item = items[0];
                                var title = item.querySelector("title").innerHTML.split(" - ")[0];
                                /* Limpieza de descripción (resumen gratuito) */
                                var desc = item.querySelector("description").innerHTML.replace(/<[^>]*>?/gm, '').substring(0, 150);
                                
                                var report = "Boletín informativo. La noticia del día en " + searchCity + " es: " + title + ". " + desc + ". Seguiremos informando.";
                                localStorage.setItem("cache_news", report);
                            }
                        }).catch(e => {
                            localStorage.setItem("cache_news", "Conectando con el centro de control nacional. Sin noticias locales en " + searchCity + " por el momento.");
                        });
                    }
                },

                /* --- 🔓 DESBLOQUEO TTS --- */
                unlock: function() {
                    if (this.isUnlocked) return;
                    console.log("🔓 Desbloqueando TTS...");
                    if (!window.speechSynthesis) return;
                    window.speechSynthesis.cancel();
                    var msg = new SpeechSynthesisUtterance(" ");
                    msg.volume = 0;
                    window.speechSynthesis.speak(msg);
                    this.isUnlocked = true;
                    this.detectLocalCity();
                },

                /* --- 🔍 ESCÁNER DE RADIO --- */
                /* 🔒 HARD-LOCK: En Android WebView, el .play() debe llamarse en el mismo */
                /* ciclo que el click del usuario. Iniciamos un "silencio" para ganar el permiso. */
                scan: function(city, genre, forceName) {
                    var self = this;
                    if (window.app && window.app.ctx) window.app.ctx.resume();
                    this.unlock();
                    
                    /* --- 🛡️ GESTURE SHIELD: Activar audio ANTES del fetch --- */
                    if (!this.audio) {
                        this.audio = new Audio();
                        this.audio.id = "fm-radio-element";
                        this.audio.style.position = "fixed";
                        this.audio.style.bottom = "0";
                        this.audio.style.opacity = "0.01";
                        this.audio.style.width = "10px";
                        this.audio.style.height = "10px";
                        this.audio.setAttribute('playsinline', 'true');
                        this.audio.setAttribute('preload', 'auto');
                        document.body.appendChild(this.audio);
                    }
                    if (window.app) window.app.bgRadio = this.audio;
                    
                    /* --- 🛡️ NEWS SHIELD: Pre-calentar motor de noticias --- */
                    if (!this.newsAudio) {
                        this.newsAudio = new Audio();
                        this.newsAudio.setAttribute('playsinline', 'true');
                        this.newsAudio.style.display = "none";
                        document.body.appendChild(this.newsAudio);
                    }

                    /* Marcamos el elemento como "activo" inmediatamente si no estaba ya sonando. */
                    if (!this.audio.src || this.audio.paused) {
                        this.audio.src = "data:audio/wav;base64,UklGRigAAABXQVZFRm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQQAAAAAAA==";
                        this.audio.play().catch(function(e){});
                    }
                    
                    /* Despertamos también el motor de noticias en silencio absoluto */
                    this.newsAudio.src = "data:audio/wav;base64,UklGRigAAABXQVZFRm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQQAAAAAAA==";
                    this.newsAudio.play().catch(function(e){});

                    this.currentCity = city || this.currentCity || "SEVILLA";
                    this.currentGenre = genre || "MIX";
                    
                    /* --- 🎯 FEEDBACK INMEDIATO --- */
                    if (window.dispatch_bg_station) window.dispatch_bg_station("BUSCANDO...");

                    if (this.currentGenre === "ANUNCIOS") {
                        /* --- 🛡️ PERMISSION ANCHOR: Arrancar noticias YA (en silencio) --- */
                        if (this.newsAudio) {
                            this.newsAudio.src = "https://shoutcast.rtve.es/radio5_main.mp3";
                            this.newsAudio.volume = 0.0001;
                            this.newsAudio.play().catch(function(){});
                        }

                        /* --- 🚀 FIX: RESPUESTA INMEDIATA SI HAY TEXTO PENDIENTE --- */
                        var customText = window.virtualOperatorPendingText;
                        if (customText) {
                            this.currentStation = "ON AIR";
                            if (window.app) window.app.currentBgStation = "ON AIR";
                            if (window.dispatch_bg_station) window.dispatch_bg_station("ON AIR");
                            
                            var finalSpeech = customText;
                            if (customText === "NOTICIA_LOCAL") finalSpeech = localStorage.getItem("cache_news") || "Sin noticias por ahora.";
                            if (customText === "TRAFICO_LOCAL") finalSpeech = localStorage.getItem("cache_dgt") || "Tráfico fluido.";
                            
                            this.speak(finalSpeech);
                            window.virtualOperatorPendingText = null;
                            return;
                        }

                        /* Si no hay texto manual, hacemos el ciclo normal con refresco */
                        this.refreshCache(function() {
                            self.currentStation = "ON AIR";
                            if (window.app) window.app.currentBgStation = "ON AIR";
                            if (window.dispatch_bg_station) window.dispatch_bg_station("ON AIR");
                            self.announce(self.currentCity, true);
                        });
                        return;
                    }
                    
                    /* --- 🛡️ API RESILIENCE: Filtro de alta calidad y HTTPS --- */
                    var apiBase = "https://all.api.radio-browser.info/json/stations/search?countrycode=ES&hidebroken=true&order=bitrate&reverse=true&limit=20&https=true";
                    var genreTag = "";
                    if (this.currentGenre === "NOTICIAS") genreTag = "news";
                    else if (this.currentGenre === "MUSICA") genreTag = "music";
                    else if (this.currentGenre === "PODCAST") genreTag = "talk";
                    
                    var api = apiBase;
                    if (forceName) api += "&name=" + encodeURIComponent(forceName);
                    else {
                        var cleanCity = this.currentCity.split(" / ")[0].split(" (")[0].trim();
                        api += "&city=" + encodeURIComponent(cleanCity);
                    }
                    if (genreTag) api += "&tag=" + genreTag;

                    fetch(api)
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            if (data && data.length > 0) {
                                var s = data[Math.floor(Math.random() * Math.min(data.length, 5))];
                                self.play(s);
                            } else if (genreTag) {
                                /* --- 🚀 SMART BROADEN: Si no hay local, buscar el género en toda España --- */
                                console.log("🔍 Sin emisora local de " + genreTag + ", ampliando búsqueda nacional...");
                                fetch(apiBase + "&tag=" + genreTag)
                                    .then(r => r.json())
                                    .then(nationalData => {
                                        if (nationalData && nationalData.length > 0) {
                                            self.play(nationalData[Math.floor(Math.random() * Math.min(nationalData.length, 5))]);
                                        } else {
                                            self.play({ name: "Radio Nacional HQ", url: "https://rtve-live-p.rtve.es/rne_rne_main.mp3" });
                                        }
                                    });
                            } else {
                                console.log("🔍 Usando emisora de respaldo (HQ)...");
                                self.play({ name: "Radio Nacional HQ", url: "https://rtve-live-p.rtve.es/rne_rne_main.mp3" });
                            }
                        })
                        .catch(function(e) {
                            console.error("Radio API Error:", e);
                            self.play({ name: "Radio Nacional (Backup)", url: "https://rtve-live-p.rtve.es/rne_rne_main.mp3" });
                        });
                },

                /* --- 🎶 REPRODUCTOR --- */
                /* 🔒 HARD-LOCK: Reproducción directa para evitar bloqueos CORS. */
                /* OPTIMIZACIÓN: Cambio de fuente ultra-rápido sin pausas redundantes. */
                play: function(station) {
                    var self = this;
                    if (!this.audio) return;
                    
                    var streamUrl = station.url_resolved || station.url;
                    console.log("📻 Cambiando a: " + station.name + " (" + (station.bitrate || '??') + "kbps)");
                    
                    /* --- 🚀 FAST SWITCH: Cortar audio anterior inmediatamente --- */
                    this.audio.pause();
                    this.audio.src = "";
                    this.audio.load(); /* Forzar limpieza de buffer */
                    
                    this.audio.src = streamUrl;
                    this.syncVolume();
                    
                    var p = this.audio.play();
                    if (p !== undefined) {
                        p.then(function() {
                            self.currentStation = station.name;
                            if (window.app) window.app.currentBgStation = station.name;
                            if (window.dispatch_bg_station) window.dispatch_bg_station(station.name);
                            self.syncVolume();

                            /* --- 🎙️ RADIO LIFE: Programar primer boletín automático en 10 min --- */
                            if (!self.announcementTimer) {
                                self.announcementTimer = setTimeout(function() { self.announce(self.currentCity); }, 600000);
                            }
                        }).catch(function(err) {
                            console.error("Play Error:", err);
                            if (window.dispatch_bg_station) window.dispatch_bg_station("ERROR");
                            /* Si falla, intentamos otra emisora tras un delay para no saturar */
                            setTimeout(function() { self.scan(); }, 1000); /* Reducido de 3000 a 1000 para búsqueda rápida */
                        });
                    }
                },

                syncVolume: function() {
                    if (!this.audio) return;
                    var baseVol = parseFloat(localStorage.getItem("bgVol"));
                    if (isNaN(baseVol)) baseVol = 0.7;
                    
                    /* Sincronizar el nodo de ganancia por si alguien escucha por ahí (Replay/Monitor) */
                    if (window.app && window.app.bgRadioGain && window.app.ctx) {
                        window.app.bgRadioGain.gain.setTargetAtTime(baseVol, window.app.ctx.currentTime, 0.1);
                    }
                    
                    /* APLICACIÓN DIRECTA: Única forma de garantizar sonido en streams sin CORS */
                    this.audio.volume = baseVol;
                },

                /* --- 🎙️ LOCUCIÓN --- */
                /* 🔒 HARD-LOCK: No eliminar el unlock(). */
                /* Android requiere re-activar el motor de voz tras periodos de silencio. */
                announce: function(city, force) {
                    if (!this.isUnlocked) this.unlock();
                    
                    /* --- 🛡️ PRIORIDAD HUMANA: El bot no habla si hay TX o RX --- */
                    var isRx = window.app && window.app.rxActiveInternal;
                    if (window.app.isTransmittingInternal || isRx) return;

                    var dgt = localStorage.getItem("cache_dgt");
                    var weather = localStorage.getItem("cache_weather");
                    var nasa = localStorage.getItem("cache_nasa");
                    var news = localStorage.getItem("cache_news");
                    var nick = window.app.nick || "compañero";
                    
                    var texts = [
                        "Saludos para " + nick + " en " + city + ". La antena ON AIR está operativa.",
                        "ON AIR SPAIN: Conectando tu ciudad. Son las " + new Date().getHours() + " horas y " + new Date().getMinutes() + " minutos.",
                        "Recuerda mantener la cortesía en la frecuencia de tu ciudad."
                    ];
                    
                    if (dgt) texts.push(dgt);
                    if (weather) texts.push(weather);
                    if (nasa) texts.push(nasa);
                    if (news) texts.push(news);

                    var selected = force ? (news || dgt || texts[0]) : texts[Math.floor(Math.random() * texts.length)];
                    
                    /* --- 🛡️ SAFETY CHECK: Si no hay avisos DGT en caché, informar al usuario --- */
                    if (force && !dgt && !weather && !nasa) {
                        selected = "Consultando el boletín de servicio de " + city + ". Por ahora no hay incidencias DGT reportadas en la zona.";
                    }
                    
                    /* Si el boletín es el de la NASA, disparamos la imagen y el título a la UI */
                    if (selected && selected.indexOf("espacial") !== -1 && window.dispatch_nasa_image) {
                        var imgUrl = localStorage.getItem("cache_nasa_img");
                        var title = localStorage.getItem("cache_nasa_title");
                        var desc = localStorage.getItem("cache_nasa_desc");
                        if (imgUrl) window.dispatch_nasa_image(imgUrl, title, desc);
                    }

                    this.speak(selected);
                },

                speak: function(text) {
                    var self = this;
                    if (!window.speechSynthesis) return;

                    /* --- 🛡️ PRIORIDAD HUMANA: Cancelar si hay actividad --- */
                    var isRx = window.app && window.app.rxActiveInternal;
                    if (window.app.isTransmittingInternal || isRx) {
                        window.speechSynthesis.cancel();
                        if (this.newsAudio) this.newsAudio.pause();
                        return;
                    }
                    
                    /* --- ♂️ CARGA DE SEGURIDAD: Re-intentar si las voces no están listas --- */
                    var voices = window.speechSynthesis.getVoices();
                    if (voices.length === 0) {
                        console.warn("⏳ Voces no listas, re-intentando en 500ms...");
                        setTimeout(function() { self.speak(text); }, 500);
                        return;
                    }

                    /* --- ♂️ MOTOR DE SELECCIÓN DE VOZ MASCULINA --- */
                    var getMaleVoice = function() {
                        var voices = window.speechSynthesis.getVoices();
                        if (voices.length === 0) {
                            /* Si no hay voces cargadas, pedimos una carga y esperamos lo peor */
                            window.speechSynthesis.getVoices();
                            return null;
                        }

                        /* 1. Buscar voces masculinas confirmadas (Prioridad Máxima) */
                        var male = voices.find(function(v) {
                            var n = v.name.toLowerCase();
                            return v.lang.indexOf('es') === 0 && (n.indexOf('pablo') !== -1 || n.indexOf('male') !== -1 || n.indexOf('david') !== -1 || n.indexOf('alvaro') !== -1 || n.indexOf('enrique') !== -1 || n.indexOf('sharp') !== -1);
                        });
                        if (male) return male;

                        /* 2. Fallback: Evitar nombres femeninos conocidos a toda costa */
                        var femaleNames = ['helena', 'sabina', 'lucia', 'zira', 'mónica', 'monica', 'laura', 'cristina', 'elsy', 'maria', 'victoria', 'juana', 'pilar', 'juana'];
                        return voices.find(function(v) {
                            var n = v.name.toLowerCase();
                            if (v.lang.indexOf('es') !== 0) return false;
                            /* Si no tiene nombre de mujer, lo aceptamos como "posible hombre" */
                            return !femaleNames.some(function(fn) { return n.indexOf(fn) !== -1; });
                        });
                    };

                    /* --- 📰 MODO INFORMATIVO REAL (RNE RADIO 5) --- */
                    if (text === "MODO_NOTICIAS_REALES") {

                        var intro = "Atención operadores. Conectamos con Radio Nacional de España para el boletín informativo. Activa tu receptor FM para sintonizar. Cambio.";
                        var msg = new SpeechSynthesisUtterance(intro);
                        msg.lang = 'es-ES';
                        msg.rate = 0.8;
                        msg.pitch = 0.45; /* Muy grave */
                        msg.voice = getMaleVoice();

                        msg.volume = Math.min(1.0, (parseFloat(localStorage.getItem("bgVol")) || 0.7) * 1.2);
                        
                        msg.onstart = function() { 
                            if (window.app) window.app.isAnnouncerTalking = true; 
                            if (window.updateBgDucking) window.updateBgDucking();
                            
                            /* --- 📡 SINTONIZACIÓN AUTOMÁTICA --- */
                            self.currentGenre = "NOTICIAS";
                            localStorage.setItem("bgGenre", "NOTICIAS");
                            if (window.dispatch_bg_genre_change) window.dispatch_bg_genre_change("NOTICIAS");

                            /* --- 📡 PRE-CARGA AGRESIVA (ANTI-LAG) --- */
                            if (self.newsAudio) {
                                self.newsAudio.src = "https://shoutcast.rtve.es/radio5_main.mp3";
                                self.newsAudio.volume = 0.0001; 
                                self.newsAudio.play().catch(function(e){});
                            }
                        };
                        
                        msg.onend = function() {
                            if (!self.newsAudio) return;
                            
                            var baseVol = parseFloat(localStorage.getItem("bgVol")) || 0.5;
                            self.newsAudio.volume = Math.min(1.0, baseVol * 1.6);
                            
                            var playPromise = self.newsAudio.play();
                            if (playPromise !== undefined) {
                                playPromise.catch(function(e) { 
                                    self.newsAudio.src = "https://rtve-mp3.flumotion.com/rtve/radio5.mp3";
                                    self.newsAudio.play().catch(function(e2) {
                                        if (window.app) window.app.isAnnouncerTalking = false;
                                        if (window.updateBgDucking) window.updateBgDucking(1.0);
                                    });
                                });
                            }

                            /* Marcar la emisora en la interfaz */
                            self.currentStation = "RNE RADIO 5";
                            if (window.app) window.app.currentBgStation = "RNE RADIO 5";
                            if (window.dispatch_bg_station) window.dispatch_bg_station("RNE RADIO 5");
                            
                            self.newsAudio.onended = function() {
                                if (window.app) window.app.isAnnouncerTalking = false;
                                if (window.updateBgDucking) window.updateBgDucking(1.0);
                            };
                        };
                        window.speechSynthesis.speak(msg);
                        return;
                    }
                    
                    /* --- 🛡️ AUDIO RESILIENCE: Despertar contexto antes de hablar --- */
                    if (window.app && window.app.ctx) window.app.ctx.resume();
                    
                    window.speechSynthesis.cancel();
                    
                    var msg = new SpeechSynthesisUtterance(text);
                    msg.lang = 'es-ES';
                    var maleVoice = getMaleVoice();
                    if (maleVoice) {
                        msg.voice = maleVoice;
                        console.log("♂️ Usando voz:", maleVoice.name);
                    } else {
                        console.warn("⚠️ No se encontró voz de hombre, usando default.");
                    }

                    msg.rate = 0.9; /* Ajustado de 0.8 a 0.9 para velocidad natural */
                    msg.pitch = 0.45; /* Más grave, más hombre */
                    
                    /* --- 🛡️ BRIDGE NATIVO: Prioridad a la voz de la APP ANDROID --- */
                    if (window.AndroidApp && typeof window.AndroidApp.speak === 'function') {
                        window.AndroidApp.speak(text, 0.9, 0.45);
                        /* Sincronizar ducking manual para el bridge */
                        if (window.app) window.app.isAnnouncerTalking = true;
                        if (window.updateBgDucking) window.updateBgDucking();
                        setTimeout(function() {
                            if (window.app) window.app.isAnnouncerTalking = false;
                            if (window.updateBgDucking) window.updateBgDucking(1.0);
                        }, text.length * 80); 
                        return;
                    }

                    /* --- 🛡️ ANTI-MOÑA FIX: Si por error sale voz de mujer, bajamos el tono al extremo --- */
                    if (!maleVoice) msg.pitch = 0.35;
                    
                    msg.volume = Math.min(1.0, (parseFloat(localStorage.getItem("bgVol")) || 0.7) * 1.2);

                    msg.onstart = function() {
                        if (window.app) window.app.isAnnouncerTalking = true;
                        if (window.updateBgDucking) window.updateBgDucking();
                    };
                    msg.onend = function() {
                        if (window.app) window.app.isAnnouncerTalking = false;
                        if (window.updateBgDucking) window.updateBgDucking(1.0);
                        clearTimeout(this.announcementTimer);
                        this.announcementTimer = setTimeout(function() { self.announce(self.currentCity); }, 300000 + Math.random() * 300000);
                    };

                    window.speechSynthesis.speak(msg);
                },

                stop: function() {
                    this.currentStation = null;
                    if (window.app) window.app.currentBgStation = null;
                    if (this.audio) { this.audio.pause(); this.audio.src = ""; }
                    if (window.speechSynthesis) window.speechSynthesis.cancel();
                    if (window.dispatch_bg_station) window.dispatch_bg_station(null);
                    
                    /* Limpiar el temporizador de boletines al apagar la radio */
                    if (this.announcementTimer) {
                        clearTimeout(this.announcementTimer);
                        this.announcementTimer = null;
                    }
                }
            };

            /* Bridge */
            window.scanBackgroundStation = (c, g, f) => window.fmEngine.scan(c, g, f);
            window.stopBackgroundRadio = () => window.fmEngine.stop();
            window.speak = (t) => window.fmEngine.speak(t);

            /* Pre-cargar voces para evitar voz de mujer al arrancar */
            if (window.speechSynthesis) {
                window.speechSynthesis.getVoices();
                if (window.speechSynthesis.onvoiceschanged !== undefined) {
                    window.speechSynthesis.onvoiceschanged = () => window.speechSynthesis.getVoices();
                }
            }
        """)
    }
}
