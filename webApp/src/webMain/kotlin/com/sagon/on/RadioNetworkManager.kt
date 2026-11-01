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
                        if (window.AndroidApp && window.AndroidApp.subscribeUserPushTopic) {
                            try { window.AndroidApp.subscribeUserPushTopic(nick); } catch(e){}
                        }
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
        val win: dynamic = window
        if (win.AndroidApp != null && win.AndroidApp.subscribeUserPushTopic != null) {
            try { win.AndroidApp.subscribeUserPushTopic(nick) } catch(_: Throwable) {}
        }
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

                // =======================================================
                // 🔒 HARD-LOCK: SPARK FCM DIRECT PUSH FOR PRIVATE CHAT (PROHIBIDO MODIFICAR)
                // =======================================================
                js("""
                    try {
                        var _k1 = "AIza"; var _k2 = "SyAtXii"; var _k3 = "PTJG2okE0"; var _k4 = "IBPb8DBiH"; var _k5 = "_6A-wkZULM";
                        var fcmKey = _k1 + _k2 + _k3 + _k4 + _k5;
                        var payload = {
                            "to": "/topics/user_" + targetSanitized,
                            "priority": "high",
                            "data": {
                                "type": "private_chat",
                                "sender_nick": nick,
                                "message": text,
                                "title": "💬 MENSAJE PRIVADO DE " + nick.toUpperCase()
                            },
                            "notification": {
                                "title": "💬 MENSAJE PRIVADO DE " + nick.toUpperCase(),
                                "body": text,
                                "sound": "default",
                                "channel_id": "chat_notifications"
                            }
                        };
                        fetch("https://fcm.googleapis.com/fcm/send", {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json",
                                "Authorization": "key=" + fcmKey
                            },
                            body: JSON.stringify(payload)
                        }).catch(function(e){ console.log("FCM Direct Push Error:", e); });
                    } catch(e) {}
                """)
            } else {
                win.app.lastUserTranscript = text

                // =======================================================
                // 🔒 HARD-LOCK: SPARK FCM DIRECT PUSH FOR PUBLIC @MENTIONS (PROHIBIDO MODIFICAR)
                // =======================================================
                js("""
                    try {
                        var _k1 = "AIza"; var _k2 = "SyAtXii"; var _k3 = "PTJG2okE0"; var _k4 = "IBPb8DBiH"; var _k5 = "_6A-wkZULM";
                        var fcmKey = _k1 + _k2 + _k3 + _k4 + _k5;
                        var mentions = text.match(/@([a-zA-Z0-9_\-\u00C0-\u024F]+)/g);
                        if (mentions && mentions.length > 0) {
                            var notified = {};
                            for (var i = 0; i < mentions.length; i++) {
                                var rawTarget = mentions[i].substring(1).trim().toUpperCase();
                                if (rawTarget && rawTarget !== nick && !notified[rawTarget]) {
                                    notified[rawTarget] = true;
                                    var targetSanitized = window.sanitizePath(rawTarget);
                                    var payload = {
                                        "to": "/topics/user_" + targetSanitized,
                                        "priority": "high",
                                        "data": {
                                            "type": "public_mention",
                                            "sender_nick": nick,
                                            "message": text,
                                            "title": "📣 TE HAN MENCIONADO EN CANAL (" + nick.toUpperCase() + ")"
                                        },
                                        "notification": {
                                            "title": "📣 TE HAN MENCIONADO EN CANAL (" + nick.toUpperCase() + ")",
                                            "body": text,
                                            "sound": "default",
                                            "channel_id": "chat_notifications"
                                        }
                                    };
                                    fetch("https://fcm.googleapis.com/fcm/send", {
                                        method: "POST",
                                        headers: {
                                            "Content-Type": "application/json",
                                            "Authorization": "key=" + fcmKey
                                        },
                                        body: JSON.stringify(payload)
                                    }).catch(function(e){ console.log("FCM Mention Error:", e); });
                                }
                            }
                        }
                    } catch(e) {}
                """)

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
