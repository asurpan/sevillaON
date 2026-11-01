package com.sagon.on

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.js.Date

/**
 * 🌐 RADIO NETWORK MANAGER: GESTIÓN DE FIREBASE Y CHAT
 */
object RadioNetworkManager {
    fun install() {
        js("""
            var _p1 = "AIza"; var _p2 = "SyBA7tMb"; var _p3 = "cvbrl2lt"; var _p4 = "Tweqydmk7"; var _p5 = "PRfk-R7fWw";
            var cfg = { 
                apiKey: _p1+_p2+_p3+_p4+_p5, 
                authDomain: "sevilla-on-200b3.firebaseapp.com", 
                databaseURL: "https://sevilla-on-200b3-default-rtdb.europe-west1.firebasedatabase.app",
                projectId: "sevilla-on-200b3" 
            };
            if (typeof firebase !== 'undefined' && firebase.initializeApp && !firebase.apps.length) firebase.initializeApp(cfg);
            window.app = window.app || {};
            window.app.db = (typeof firebase !== 'undefined' && typeof firebase.database === 'function') ? firebase.database() : null;

            window.sanitizePath = function(p) {
                return (p || "").toString().replace(/[.#${'$'}\[\]]/g, "_");
            };

            window.initFirebaseListener = function() {
                if (window.app.db) {
                    window.app.db.ref("users").on('value', function(s) { 
                        if(window.dispatch_users_update) window.dispatch_users_update(s.val()); 
                    });

                    var nick = (localStorage.getItem("indicativo") || "").trim().toUpperCase();
                    if (nick && nick !== "ANÓNIMO" && nick !== "RADIO") {
                        var sanitizedNick = window.sanitizePath(nick);
                        if (window.privateInboxRef) try { window.privateInboxRef.off(); } catch(e){}
                        window.privateInboxRef = window.app.db.ref("private_inbox/" + sanitizedNick);
                        window.privateInboxRef.on('child_added', function(snapshot) {
                            var msg = snapshot.val();
                            if (!msg) return;
                            var now = Date.now();
                            if (msg.timestamp && (now - msg.timestamp < 120000)) {
                                var sender = (msg.senderNick || "ALGUIEN").toString().toUpperCase();
                                var text = (msg.text || "").toString();
                                if (window.playUiSound) try { window.playUiSound("chat_msg"); } catch(e){}
                                if (window.showSystemNotification) {
                                    try { window.showSystemNotification("💬 MENSAJE PRIVADO DE " + sender, text); } catch(e){}
                                }
                                if (window.dispatch_chat_open) {
                                    try { window.dispatch_chat_open(sender); } catch(e){}
                                }
                            }
                            try { snapshot.ref.remove(); } catch(e){}
                        });
                    }
                }
            };

            window.updateChatListener = function(target) {
                if (!window.app || !window.app.db) return;
                var nick = localStorage.getItem("indicativo") || "ANÓNIMO";
                var city = localStorage.getItem("lastCity") || "SEVILLA";
                var channel = localStorage.getItem("lastChannel") || city;
                var subtone = localStorage.getItem("lastSubtone") || "0000";
                var channelKey = (subtone && subtone !== "0000") ? (channel + "_" + subtone) : channel;
                var chatPath = target ? ("private_messages/" + window.sanitizePath([nick, target].sort()[0]) + "_" + window.sanitizePath([nick, target].sort()[1])) 
                                      : ("messages/" + window.sanitizePath(city) + "/" + window.sanitizePath(channelKey));
                
                if (window.currentChatPath === chatPath) return;
                window.currentChatPath = chatPath;

                if (window.currentChatRef) try { window.currentChatRef.off(); } catch(e) {}
                window.currentChatRef = window.app.db.ref(chatPath).limitToLast(50);
                window.currentChatRef.on('value', function(snapshot) { 
                    if (window.dispatch_chat_update) window.dispatch_chat_update(snapshot.val()); 
                });
            };
        """)
    }

    fun connect(nick: String) {
        js("window.connectRadio(nick);")
    }

    fun sendMessage(text: String, target: String?) {
        val win: dynamic = window
        val nick = (localStorage.getItem("indicativo") ?: "ANÓNIMO").trim().uppercase()
        val city = localStorage.getItem("lastCity") ?: "SEVILLA"
        val channel = localStorage.getItem("lastChannel") ?: city
        val subtone = localStorage.getItem("lastSubtone") ?: "0000"
        val channelKey = if (subtone != "0000") "${channel}_$subtone" else channel
        
        val chatPath = if (target != null) {
            val sortedNicks = listOf(nick, target).sorted()
            "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
        } else {
            "messages/${win.sanitizePath(city)}/${win.sanitizePath(channelKey)}"
        }

        if (win.app != null && win.app.db != null) {
            if (win.updateChatListener != null) {
                win.updateChatListener(target)
            }
            val m: dynamic = js("{}")
            m.senderNick = nick
            m.text = text
            m.timestamp = Date.now()
            win.app.db.ref(chatPath).push(m)

            if (target != null) {
                val targetSanitized = win.sanitizePath(target)
                val notifObj: dynamic = js("{}")
                notifObj.senderNick = nick
                notifObj.text = text
                notifObj.timestamp = Date.now()
                win.app.db.ref("private_inbox/$targetSanitized").push(notifObj)
            } else {
                win.app.lastUserTranscript = text
                js("if (window.checkControlStationResponse) setTimeout(function() { window.checkControlStationResponse(); }, 800);")
            }
        }
    }

    fun deleteMessage(msgId: String, target: String?) {
        val win: dynamic = window
        val nick = (localStorage.getItem("indicativo") ?: "ANÓNIMO").trim().uppercase()
        val city = localStorage.getItem("lastCity") ?: "SEVILLA"
        val channel = localStorage.getItem("lastChannel") ?: city
        val subtone = localStorage.getItem("lastSubtone") ?: "0000"
        val channelKey = if (subtone != "0000") "${channel}_$subtone" else channel
        
        val chatPath = if (target != null) {
            val sortedNicks = listOf(nick, target).sorted()
            "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
        } else {
            "messages/${win.sanitizePath(city)}/${win.sanitizePath(channelKey)}"
        }

        if (win.app?.db != null) {
            win.app.db.ref("$chatPath/$msgId").remove()
        }
    }
}
