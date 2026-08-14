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

            window.initFirebaseListener = function() {
                if (window.app.db) {
                    window.app.db.ref("users").on('value', function(s) { 
                        if(window.update_remote_users) window.update_remote_users(s.val()); 
                    });
                }
            };

            window.updateChatListener = function(target) {
                if (!window.app || !window.app.db) return;
                var nick = localStorage.getItem("indicativo") || "ANÓNIMO";
                var city = localStorage.getItem("lastCity") || "SEVILLA";
                var channel = localStorage.getItem("lastChannel") || city;
                var chatPath = target ? ("private_messages/" + window.sanitizePath([nick, target].sort()[0]) + "_" + window.sanitizePath([nick, target].sort()[1])) 
                                      : ("messages/" + window.sanitizePath(city) + "/" + window.sanitizePath(channel));
                
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
        
        val chatPath = if (target != null) {
            val sortedNicks = listOf(nick, target).sorted()
            "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
        } else {
            "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
        }

        if (win.app?.db != null) {
            val m: dynamic = js("{}")
            m.senderNick = nick
            m.text = text
            m.timestamp = Date.now()
            win.app.db.ref(chatPath).push(m)
        }
    }

    fun deleteMessage(msgId: String, target: String?) {
        val win: dynamic = window
        val nick = (localStorage.getItem("indicativo") ?: "ANÓNIMO").trim().uppercase()
        val city = localStorage.getItem("lastCity") ?: "SEVILLA"
        val channel = localStorage.getItem("lastChannel") ?: city
        
        val chatPath = if (target != null) {
            val sortedNicks = listOf(nick, target).sorted()
            "private_messages/${win.sanitizePath(sortedNicks[0])}_${win.sanitizePath(sortedNicks[1])}"
        } else {
            "messages/${win.sanitizePath(city)}/${win.sanitizePath(channel)}"
        }

        if (win.app?.db != null) {
            win.app.db.ref("$chatPath/$msgId").remove()
        }
    }
}
