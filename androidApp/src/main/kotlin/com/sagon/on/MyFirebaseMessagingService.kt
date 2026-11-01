package com.sagon.on

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 🔔 FIREBASE MESSAGING SERVICE: GUARDIÁN DE NOTIFICACIONES PUSH
 * Gestiona los mensajes entrantes para despertar la app y avisar al usuario.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Suscribir automáticamente al tema global para notificaciones masivas gratuitas
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("comunidad_on_air")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Refuerzo de suscripción al recibir
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("comunidad_on_air")

        // 🚀 WAKELOCK TEMPORAL (10s) PARA DESPERTAR CPU EN RECIPCIÓN DE AUDIO/VOZ PUSH
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "ON_AIR_SPAIN:FCMWakeLock")
            wakeLock.acquire(10000L)
        } catch (_: Exception) {}

        // 🚀 DESPERTAR Y MANTENER EL FOREGROUND SERVICE DE RADIO ACTIVO
        try {
            val serviceIntent = Intent(this, RadioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (_: Exception) {}

        // Prioridad a notificaciones de consola directas
        remoteMessage.notification?.let {
            val senderNick = remoteMessage.data["sender_nick"]
            sendNotification(it.title ?: "📻 ON AIR", it.body ?: "Alguien está modulando...", senderNick)
            return
        }

        // Procesamiento de datos (para filtrado por ciudad o chat privado)
        if (remoteMessage.data.isNotEmpty()) {
            val type = remoteMessage.data["type"]
            val senderNick = remoteMessage.data["sender_nick"]
            val title = remoteMessage.data["title"] ?: senderNick?.let { "💬 MENSAJE PRIVADO DE ${it.uppercase()}" } ?: "📻 ON AIR"
            val message = remoteMessage.data["message"] ?: remoteMessage.data["body"] ?: "¡Nueva actividad en la frecuencia!"
            val targetCity = remoteMessage.data["target_city"]

            if (type == "private_chat" || senderNick != null) {
                sendNotification(title, message, senderNick)
            } else if (targetCity != null) {
                val prefs = getSharedPreferences("on_air_prefs", android.content.Context.MODE_PRIVATE)
                val userCity = prefs.getString("last_city", "") ?: ""
                
                if (userCity.uppercase() == targetCity.uppercase()) {
                    sendNotification(title, message, null)
                }
            } else {
                // DESPERTADOR GLOBAL
                sendNotification(title, message, null)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String, senderNick: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_CHAT"
            if (!senderNick.isNullOrBlank()) {
                putExtra("chat_sender", senderNick)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (!senderNick.isNullOrBlank()) "chat_notifications" else "on_air_alerts"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // 🎨 LOGO REDONDO: Avatar de la notificación
        val largeIcon = android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat) 
            .setLargeIcon(largeIcon) // 🛡️ CIRCULAR AUTOMÁTICO
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 100, 50, 100)) // 🛡️ BEEP-BEEP VIBRATION
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX) // 🚀 PRIORIDAD MÁXIMA PARA DESPERTAR
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (channelId == "chat_notifications") "Mensajes de Chat" else "Alertas de Radio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de voz entrante y mensajes de chat"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = if (!senderNick.isNullOrBlank()) senderNick.hashCode() else System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
